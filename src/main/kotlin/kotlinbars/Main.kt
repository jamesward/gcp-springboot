package kotlinbars

import com.google.cloud.spanner.*
import com.google.cloud.spring.autoconfigure.spanner.GcpSpannerAutoConfiguration
import com.google.cloud.spring.data.spanner.core.admin.DatabaseIdProvider
import com.google.cloud.spring.data.spanner.core.mapping.Column
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey
import com.google.cloud.spring.data.spanner.core.mapping.Table
import com.google.cloud.spring.data.spanner.repository.SpannerRepository
import kotlinx.html.dom.serialize
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*


@Table(name="bars")
data class Bar(
    @PrimaryKey @Column(name="id") val id: UUID,
    val name: String
)

@Repository
interface BarRepo : SpannerRepository<Bar, String>


@SpringBootApplication
@RestController
class WebApp(val barRepo: BarRepo) {

    @GetMapping("/")
    fun index(): String {
        return Html.index.serialize(false)
    }

    @GetMapping("/bars")
    fun getBars(): Iterable<Bar> {
        return barRepo.findAll()
    }

    @PostMapping("/bars")
    fun addBar(@RequestBody json: Map<String, String>) = run {
        json.get("name")?.let { name ->
            barRepo.save(Bar(java.util.UUID.randomUUID(),name))
            ResponseEntity<kotlin.Unit>(org.springframework.http.HttpStatus.NO_CONTENT)
        } ?: ResponseEntity<Unit>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

}

fun initDatabase(database: Database, dbAdminClient: DatabaseAdminClient): Database? {
    val query = "CREATE TABLE Bars (id STRING(36) NOT NULL, name STRING(255)) PRIMARY KEY (id)"
    return dbAdminClient.createDatabase(database, listOf(query)).get()
}

@Configuration(proxyBeanMethods = false)
class InitConfiguration {

    @Bean
    @Profile("init")
    fun commandLineRunner(databaseIdProvider: DatabaseIdProvider, databaseAdminClient: DatabaseAdminClient): CommandLineRunner {
        return CommandLineRunner {
            val database = Database(databaseIdProvider.get(), DatabaseInfo.State.UNSPECIFIED, databaseAdminClient)
            initDatabase(database, databaseAdminClient)
        }
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
    }
}