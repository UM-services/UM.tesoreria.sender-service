package um.tesoreria.sender;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration,"
                + "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration,"
                // Sin broker en los tests de contexto: no crear KafkaAdmin (los IT usan @EmbeddedKafka).
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"
})
class SenderApplicationTests {

    @Test
    void contextLoads() {
    }

}
