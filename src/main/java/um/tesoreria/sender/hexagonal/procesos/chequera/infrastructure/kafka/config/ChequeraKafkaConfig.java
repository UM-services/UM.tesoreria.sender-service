package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendChequeraEvent;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendCuotaEvent;

@EnableKafka
@Configuration
@Slf4j
public class ChequeraKafkaConfig {

    static final String SUFIJO_DLT = ".DLT";
    static final long REINTENTOS = 3L;
    static final long ESPERA_REINTENTO = 1000L;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, SendChequeraEvent> consumerFactory() {
        log.info("Initializing Kafka Consumer Factory with bootstrapServers: {} and groupId: {}", bootstrapServers,
                groupId);
        return new DefaultKafkaConsumerFactory<>(propiedades(), new StringDeserializer(),
                errorHandlingDeserializer(SendChequeraEvent.class));
    }

    @Bean
    public ConsumerFactory<String, SendCuotaEvent> cuotaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(propiedades(), new StringDeserializer(),
                errorHandlingDeserializer(SendCuotaEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendChequeraEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SendChequeraEvent>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler(SendChequeraEvent.class, kafkaTemplate, dltKafkaTemplate));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendCuotaEvent> cuotaKafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SendCuotaEvent>();
        factory.setConsumerFactory(cuotaConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(SendCuotaEvent.class, kafkaTemplate, dltKafkaTemplate));
        return factory;
    }

    private Map<String, Object> propiedades() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        return props;
    }

    private <T> ErrorHandlingDeserializer<T> errorHandlingDeserializer(Class<T> tipo) {
        JsonMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        // El contrato es el JSON del evento: el tipo target manda y __TypeId__ se ignora.
        var jsonDeserializer = new JacksonJsonDeserializer<>(tipo, objectMapper, false);
        return new ErrorHandlingDeserializer<>(jsonDeserializer);
    }

    private <T> DefaultErrorHandler errorHandler(Class<T> tipo, KafkaTemplate<String, Object> kafkaTemplate,
                                                 KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new HashMap<>();
        templates.put(tipo, kafkaTemplate);
        templates.put(byte[].class, dltKafkaTemplate);
        var recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, exception) -> new TopicPartition(record.topic() + SUFIJO_DLT, record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(ESPERA_REINTENTO, REINTENTOS));
    }

}
