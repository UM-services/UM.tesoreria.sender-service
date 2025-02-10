package um.tesoreria.sender.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
@EnableTransactionManagement
public class RabbitMQConfig {

    public static final String QUEUE_INVOICE = "recibo_queue";
    public static final String QUEUE_TESTER = "tester_queue";

    @Bean
    public Queue reciboQueue() {
        return new Queue(QUEUE_INVOICE, true);
    }

    @Bean
    public Queue testerQueue() {
        return new Queue(QUEUE_TESTER, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setChannelTransacted(true);
        template.setUsePublisherConnection(true);
        return template;
    }

    @Bean
    public PlatformTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        
        // Valores reducidos para optimizar recursos
        factory.setPrefetchCount(5);
        factory.setBatchSize(5);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(2);
        
        return factory;
    }
}
