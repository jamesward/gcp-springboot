package kotlinbars

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest(properties = ["spring.cloud.gcp.core.enabled=false"])
class BarRepoTest(@Autowired val barRepo: BarRepo) {

    @Test
    fun `barRepo works`() {
        barRepo.save(Bar(UUID.randomUUID(), "foo"))

        val bars = barRepo.findAll().toList()
        assertThat(bars.size).isEqualTo(1)
    }

}