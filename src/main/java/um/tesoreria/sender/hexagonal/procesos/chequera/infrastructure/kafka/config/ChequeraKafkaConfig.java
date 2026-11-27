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
import org.springframework.kafka.listener.ContainerProperties;
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

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String autoOffsetReset = "latest";

    @Value("${spring.kafka.consumer.max-poll-records:10}")
    private int maxPollRecords = 10;

    @Value("${spring.kafka.consumer.max-poll-interval-ms:900000}")
    private int maxPollIntervalMs = 900_000;

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
        // Lote chico + commit por registro: un rebalanceo no puede reenviar cientos de correos.
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandler(SendChequeraEvent.class, kafkaTemplate, dltKafkaTemplate,
                new FixedBackOff(120000L, 2L)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendCuotaEvent> cuotaKafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SendCuotaEvent>();
        factory.setConsumerFactory(cuotaConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandler(SendCuotaEvent.class, kafkaTemplate, dltKafkaTemplate,
                new FixedBackOff(1000L, 3L)));
        return factory;
    }

    private Map<String, Object> propiedades() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        // Las factories no leen spring.kafka.consumer.*: se pasan explícitas para que un lote de
        // chequeras nunca supere el max.poll.interval (causa del loop de reenvíos).
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        // Las instancias de deserializador se pasan al factory, no se declaran clases acá.
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
                                                 KafkaTemplate<String, byte[]> dltKafkaTemplate,
                                                 FixedBackOff backOff) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new HashMap<>();
        templates.put(tipo, kafkaTemplate);
        templates.put(byte[].class, dltKafkaTemplate);
        var recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, exception) -> new TopicPartition(record.topic() + SUFIJO_DLT, record.partition()));
        return new DefaultErrorHandler(recoverer, backOff);
    }

}
