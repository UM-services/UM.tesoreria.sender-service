package um.tesoreria.sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SenderApplication.class, args);
    }

}
