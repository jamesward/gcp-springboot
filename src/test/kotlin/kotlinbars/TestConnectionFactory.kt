package kotlinbars

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.NoCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.spring.autoconfigure.datastore.GcpDatastoreAutoConfiguration
import com.google.cloud.spring.core.GcpProjectIdProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.testcontainers.containers.DatastoreEmulatorContainer
import org.testcontainers.utility.DockerImageName
import javax.annotation.PreDestroy

@Component
class TestPubSubContainer : DatastoreEmulatorContainer(
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
@AutoConfigureBefore(GcpDatastoreAutoConfiguration::class)
class TestConnectionFactory {

    val projectId = "test-project"
    val credentials = NoCredentials.getInstance()

    @Bean
    fun datastoreOptions(container: DatastoreEmulatorContainer): DatastoreOptions {
        return DatastoreOptions.newBuilder()
            .setHost("http://" + container.emulatorEndpoint)
            .setCredentials(credentials)
            .setRetrySettings(ServiceOptions.getNoRetrySettings())
            .setProjectId(projectId)
            .build()
    }

    @Bean
    fun datastore(datastoreOptions: DatastoreOptions): Datastore {
        return datastoreOptions.service
    }

    @Bean
    fun gcpProjectIdProvider(): GcpProjectIdProvider {
        return GcpProjectIdProvider { projectId }
    }

    @Bean
    fun credentialsProvider(): CredentialsProvider {
        return CredentialsProvider { credentials }
    }

}

