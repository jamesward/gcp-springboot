package kotlinbars

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.NoCredentials
import com.google.cloud.spanner.*
import com.google.cloud.spring.autoconfigure.spanner.GcpSpannerProperties
import com.google.cloud.spring.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.testcontainers.containers.SpannerEmulatorContainer
import org.testcontainers.utility.DockerImageName
import javax.annotation.PreDestroy


@Component
class TestSpannerContainer : SpannerEmulatorContainer(DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:1.1.0")) {

    init {
        start()
    }

    @PreDestroy
    fun destroy() {
        stop()
    }

}

@Configuration
class TestConfig {

    val projectId = "test-project"

    @Bean
    fun spannerOptions(container: TestSpannerContainer, gcpSpannerProperties: GcpSpannerProperties): SpannerOptions {
        val spannerOptions = SpannerOptions.newBuilder()
            .setEmulatorHost(container.emulatorGrpcEndpoint)
            .setCredentials(NoCredentials.getInstance())
            .setProjectId(projectId)
            .build()

        val instanceConfigId = InstanceConfigId.of(projectId, "emulator-config")
        val instanceId = InstanceId.of(projectId, gcpSpannerProperties.instanceId)
        val instanceAdminClient = spannerOptions.service.instanceAdminClient
        val instanceInfo = InstanceInfo.newBuilder(instanceId).setNodeCount(1).setInstanceConfigId(instanceConfigId).build()
        instanceAdminClient.createInstance(instanceInfo).get()

        val databaseId = DatabaseId.of(instanceId, gcpSpannerProperties.database)
        val database = Database(databaseId, DatabaseInfo.State.UNSPECIFIED, spannerOptions.service.databaseAdminClient)

        initDatabase(database, spannerOptions.service.databaseAdminClient)

        return spannerOptions
    }

    @Bean
    fun gcpProjectIdProvider(): GcpProjectIdProvider {
        return GcpProjectIdProvider { projectId }
    }

    @Bean
    fun credentialsProvider(): CredentialsProvider {
        return CredentialsProvider { NoCredentials.getInstance() }
    }

}
