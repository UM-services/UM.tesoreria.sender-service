package um.tesoreria.sender.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableFeignClients(basePackages = "um.tesoreria.sender.client")
@PropertySource("classpath:config/reports.properties")
public class SenderConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
