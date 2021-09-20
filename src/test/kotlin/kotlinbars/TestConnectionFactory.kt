package kotlinbars

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.NotFoundException
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration
import com.google.cloud.spring.core.GcpProjectIdProvider
import com.google.cloud.spring.pubsub.support.PublisherFactory
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.utility.DockerImageName
import javax.annotation.PreDestroy


@Component
class TestPostgresContainer : PostgreSQLContainer<TestPostgresContainer>("postgres:13.1") {

    init {
        withInitScript("init.sql")
        start()
    }

    @PreDestroy
    fun destroy() {
        stop()
    }

}

@Component
class TestPubSubContainer : PubSubEmulatorContainer(
    DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:357.0.0-emulators")
) {

    init {
        start()
    }

    @PreDestroy
    fun destroy() {
        stop()
    }

}

@Configuration
@AutoConfigureBefore(GcpPubSubAutoConfiguration::class)
class TestConnectionFactory {

    val projectId = "test-project"

    @Bean
    fun connectionFactory(container: TestPostgresContainer): ConnectionFactory {
        val connectionConfiguration = PostgresqlConnectionConfiguration.builder()
            .host(container.host)
            .port(container.firstMappedPort)
            .database(container.databaseName)
            .username(container.username)
            .password(container.password)
            .build()

        return PostgresqlConnectionFactory(connectionConfiguration)
    }

    @Bean(name = ["subscriberTransportChannelProvider", "publisherTransportChannelProvider"])
    fun transportChannelProvider(container: PubSubEmulatorContainer): TransportChannelProvider {
        val channel = ManagedChannelBuilder.forTarget(container.emulatorEndpoint).usePlaintext().build()
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    }

    @Bean
    fun publisherFactory(@Qualifier("publisherTransportChannelProvider") transportChannelProvider: TransportChannelProvider, credentialsProvider: CredentialsProvider): PublisherFactory {
        return PublisherFactory { topic ->
            val topicAdminSettings = TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(transportChannelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build()

            TopicAdminClient.create(topicAdminSettings).use { topicAdminClient ->
                val topicName = TopicName.of(projectId, topic)

                try {
                    topicAdminClient.getTopic(topicName)
                } catch (e: NotFoundException) {
                    topicAdminClient.createTopic(topicName)
                    println("CREATED TOPIC: $topicName")
                }

                Publisher.newBuilder(topicName)
                    .setChannelProvider(transportChannelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build()
            }
        }
    }

    @Bean
    fun gcpProjectIdProvider(): GcpProjectIdProvider {
        return GcpProjectIdProvider { projectId }
    }

    @Bean
    fun credentialsProvider(): CredentialsProvider {
        return NoCredentialsProvider.create()
    }

}

