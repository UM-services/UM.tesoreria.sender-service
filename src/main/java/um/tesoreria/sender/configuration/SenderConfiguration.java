package um.tesoreria.sender.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableFeignClients(basePackages = "um.tesoreria.sender.client")
@PropertySource("classpath:config/reports.properties")
public class SenderConfiguration {
}
