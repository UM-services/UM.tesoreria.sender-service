package um.tesoreria.sender;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.autoconfigure.exclude=org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration,org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration")
class SenderApplicationTests {

    @Test
    void contextLoads() {
    }

}
