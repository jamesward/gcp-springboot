package kotlinbars

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.*
import java.util.function.Consumer


data class Bar(val id: Long, val name: String)

data class PubSubData(val data: String)

data class PubSubMessage(val message: PubSubData) {
    inline fun <reified T> parse(objectMapper: ObjectMapper): T {
        val decoded = Base64.getDecoder().decode(message.data)
        return objectMapper.readValue(decoded)
    }
}

@SpringBootApplication
class App {

    @Bean
    fun bar(objectMapper: ObjectMapper) = Consumer<PubSubMessage> { pubSubMessage ->
        val bar = pubSubMessage.parse<Bar>(objectMapper)
        println(bar)
    }

}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
