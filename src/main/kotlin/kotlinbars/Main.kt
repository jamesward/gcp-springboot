package kotlinbars

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler


data class Bar(val id: Long, val name: String)

@SpringBootApplication
class Main(val subscriberTemplate: PubSubSubscriberTemplate): CommandLineRunner {

    override fun run(vararg args: String?) {
        subscriberTemplate.subscribeAndConvert("bars-pull", { msg ->
            println("Received message: ${msg.payload}")
            msg.ack()
        }, Bar::class.java)
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