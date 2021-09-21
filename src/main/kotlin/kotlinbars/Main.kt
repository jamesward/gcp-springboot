package kotlinbars

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.spring.data.datastore.core.mapping.Entity
import com.google.cloud.spring.data.datastore.repository.DatastoreRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Entity
data class Bar(@Id val id: Long, val name: String)

data class PubSubData(val data: String)

data class PubSubMessage(val message: PubSubData) {
    inline fun <reified T> parse(objectMapper: ObjectMapper): T {
        val decoded = Base64.getDecoder().decode(message.data)
        return objectMapper.readValue(decoded)
    }
}

interface BarRepo : DatastoreRepository<Bar, Long>

@SpringBootApplication
@RestController
class WebApp(val barRepo: BarRepo, val objectMapper: ObjectMapper) {

    @GetMapping("/")
    fun index() = run {
        ResponseEntity<Unit>(HttpStatus.OK)
    }

    @PostMapping("/")
    fun bar(@RequestBody pubSubMessage: PubSubMessage) = run {
        val bar = pubSubMessage.parse<Bar>(objectMapper)
        println(bar)
        barRepo.save(bar)
    }

}

fun main(args: Array<String>) {
    runApplication<WebApp>(*args)
}
