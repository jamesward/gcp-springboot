package kotlinbars

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.html.dom.serialize
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*


data class Bar(@Id val id: Long?, val name: String)

interface BarRepo : ReactiveCrudRepository<Bar, Long>

@SpringBootApplication
@RestController
class WebApp(val barRepo: BarRepo, val publisherTemplate: PubSubPublisherTemplate) {

    @GetMapping("/")
    fun index(): String {
        return Html.index.serialize(false)
    }

    @GetMapping("/bars")
    suspend fun getBars(): List<Bar> {
        return barRepo.findAll().collectList().awaitFirst()
    }

    @PostMapping("/bars")
    suspend fun addBar(@RequestBody bar: Bar) = run {
        barRepo.save(bar).awaitFirstOrNull()?.let {
            publisherTemplate.publish("bars", it)
            ResponseEntity<Unit>(HttpStatus.NO_CONTENT)
        } ?: ResponseEntity<Unit>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

}

@Configuration(proxyBeanMethods = false)
class InitConfiguration {

    @Bean
    @Profile("init")
    fun commandLineRunner(databaseClient: DatabaseClient, @Value("classpath:init.sql") initSql: Resource): CommandLineRunner {
        return CommandLineRunner {
            val lines = initSql.inputStream.bufferedReader().use { it.readText() }
            runBlocking {
                databaseClient.sql(lines).await()
            }
        }
    }

    @Bean
    fun pubSubMessageConverter(objectMapper: ObjectMapper): PubSubMessageConverter? {
        return JacksonPubSubMessageConverter(objectMapper)
    }

}

fun main(args: Array<String>) {
    val props = Properties()

    runApplication<WebApp>(*args) {
        if (args.contains("init")) {
            webApplicationType = WebApplicationType.NONE
            setAdditionalProfiles("init")
            props["spring.devtools.add-properties"] = false
            props["spring.devtools.livereload.enabled"] = false
        }
        setDefaultProperties(props)
    }
}