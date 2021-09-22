package kotlinbars

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class WebApp {

    private val logger = LoggerFactory.getLogger(WebApp::class.java)

    @GetMapping("/")
    fun index(): String {
        logger.info("Endpoint was hit at " + System.currentTimeMillis())
        return "hello, world"
    }

    @GetMapping("/error")
    fun error(): String {
        logger.error("Error was logged at " + System.currentTimeMillis(), Exception("This is an exception"))
        return "hello, error"
    }

}

fun main(args: Array<String>) {
    runApplication<WebApp>(*args)
}