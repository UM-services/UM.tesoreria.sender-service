package um.tesoreria.sender.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_INVOICE = "recibo_queue";
    public static final String QUEUE_TESTER = "tester_queue";

    @Bean
    public Queue reciboQueue() {
        return new Queue(QUEUE_INVOICE, true); // Cola persistente
    }

    @Bean
    public Queue testerQueue() {
        return new Queue(QUEUE_TESTER, true); // Cola persistente
    }

}
