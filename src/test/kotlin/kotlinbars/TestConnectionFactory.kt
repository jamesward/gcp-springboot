package kotlinbars

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.*
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration
import com.google.cloud.spring.core.GcpProjectIdProvider
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executors
import javax.annotation.PreDestroy


@Component
class TestPubSubContainer : PubSubEmulatorContainer(
    DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:357.0.0-emulators")
) {

    init {
        start()

        val channel = ManagedChannelBuilder.forTarget(emulatorEndpoint).usePlaintext().build()
        val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))

        val credentialsProvider = NoCredentialsProvider.create()

        val topicAdminSettings = TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()

        TopicAdminClient.create(topicAdminSettings).use { topicAdminClient ->
            val topicName = TopicName.of(projectId, topicId)
            topicAdminClient.createTopic(topicName)
        }

        val subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()

        val subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)
        val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)

        subscriptionAdminClient.createSubscription(
            subscriptionName,
            TopicName.of(projectId, topicId),
            PushConfig.getDefaultInstance(),
            10
        )

        val publisher = Publisher.newBuilder(TopicName.of(projectId, topicId))
            .setChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()

        val objectMapper = jacksonObjectMapper()

        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch {
            var i = System.currentTimeMillis()
            while (true) {
                delay(1000)
                val bar = Bar(i++, "hello, world")
                val data = ByteString.copyFrom(objectMapper.writeValueAsBytes(bar))
                val message = PubsubMessage.newBuilder().setData(data).build()
                println("Sending $bar to $topicId")
                publisher.publish(message) // todo: to coroutines?
            }
        }
    }

    @PreDestroy
    fun destroy() {
        stop()
    }

    companion object {
        const val projectId = "test-project"
        const val topicId = "bars"
        const val subscriptionId = "bars-pull"
    }

}

@Configuration
@AutoConfigureBefore(GcpPubSubAutoConfiguration::class)
class TestConnectionFactory {

    @Bean(name = ["subscriberTransportChannelProvider", "publisherTransportChannelProvider"])
    fun transportChannelProvider(container: PubSubEmulatorContainer): TransportChannelProvider {
        val channel = ManagedChannelBuilder.forTarget(container.emulatorEndpoint).usePlaintext().build()
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    }

    @Bean
    fun blob(): Blob {
        return object : Blob {
            override fun write(path: String, byteArray: ByteArray) {
                val parts = path.split("/")
                val dirPrefix = parts.dropLast(1).joinToString("/")
                val tmpDir = Files.createTempDirectory("blobs").toFile()
                val dir = File(tmpDir, dirPrefix)
                dir.mkdirs()
                val file = File(dir, parts.last())
                file.writeBytes(byteArray)
                println("Wrote to $file")
            }
        }
    }

    @Bean
    fun gcpProjectIdProvider(): GcpProjectIdProvider {
        return GcpProjectIdProvider { TestPubSubContainer.projectId }
    }

    @Bean
    fun credentialsProvider(): CredentialsProvider {
        return NoCredentialsProvider.create()
    }

}

