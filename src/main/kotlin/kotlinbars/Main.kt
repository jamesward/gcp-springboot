package kotlinbars

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody


@SpringBootApplication
@RestController
class WebApp(val webClient: WebClient) {

    @GetMapping("/")
    suspend fun index(@RequestHeader(name="Host") host: String): String {
        // todo: protocol match
        val url = "http://" + host + "/hello";
        val response = webClient.get().uri(url).retrieve().awaitBody<String>()
        return response.uppercase()
    }

    @GetMapping("/hello")
    fun hello(): String {
        return "hello, world";
    }

    companion object {
        @Bean
        fun webClient(): WebClient {
            return WebClient.create();
        }
    }
}



fun main(args: Array<String>) {
    runApplication<WebApp>(*args)
}