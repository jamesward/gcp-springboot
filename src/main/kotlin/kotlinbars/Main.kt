package kotlinbars

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.spring.core.GcpProjectIdProvider
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.WritableResource
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component


data class Bar(val id: Long, val name: String)



@SpringBootApplication
class Main(val subscriberTemplate: PubSubSubscriberTemplate, val projectIdProvider: GcpProjectIdProvider, val blob: Blob): CommandLineRunner {

    override fun run(vararg args: String?) {
        subscriberTemplate.subscribeAndConvert("bars-pull", { msg ->
            println("Received message: ${msg.payload}")
            val path = "${projectIdProvider.projectId}-gcp-springboot-pubsub-pull/${msg.payload.id}.txt"
            blob.write(path, msg.payload.name.toByteArray())
            msg.ack()
        }, Bar::class.java)
    }

}

interface Blob {
    fun write(path: String, byteArray: ByteArray)
}

@Component
class GcpBlog(val ctx: ApplicationContext) : Blob {
    override fun write(path: String, byteArray: ByteArray) {
        val location = "gs://$path"
        val resource = ctx.getResource(location) as? WritableResource
        resource?.outputStream?.use {
            it.write(byteArray)
        }
    }
}

@Configuration
class Configs {
    @Bean
    fun pubsubSubscriberThreadPool(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler()
    }

    @Bean
    fun pubSubMessageConverter(): PubSubMessageConverter? {
        return JacksonPubSubMessageConverter(jacksonObjectMapper())
    }
}


fun main(args: Array<String>) {
    runApplication<Main>(*args) {
        webApplicationType = WebApplicationType.NONE
    }
}