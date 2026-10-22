package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka.config;

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
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;

@EnableKafka
@Configuration
@Slf4j
public class NotificacionKafkaConfig {

    static final String TOPIC = "send-correo";
    static final String SUFIJO_DLT = ".DLT";
    static final long REINTENTOS = 3L;
    static final long ESPERA_REINTENTO = 1000L;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String autoOffsetReset = "latest";

    @Bean
    public ConsumerFactory<String, CorreoEvent> correoEventConsumerFactory() {
        log.info("Inicializando consumer de {} con bootstrapServers={} y groupId={}", TOPIC, bootstrapServers, groupId);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        JsonMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        // El contrato es el JSON del envelope: el tipo target manda y __TypeId__ se ignora.
        var jsonDeserializer = new JacksonJsonDeserializer<>(CorreoEvent.class, objectMapper, false);
        var errorHandlingDeserializer = new ErrorHandlingDeserializer<CorreoEvent>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CorreoEvent> notificacionKafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, CorreoEvent>();
        factory.setConsumerFactory(correoEventConsumerFactory());
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate, dltKafkaTemplate));
        return factory;
    }

    DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                     KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new HashMap<>();
        templates.put(CorreoEvent.class, kafkaTemplate);
        templates.put(byte[].class, dltKafkaTemplate);
        var recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, exception) -> new TopicPartition(record.topic() + SUFIJO_DLT, record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(ESPERA_REINTENTO, REINTENTOS));
    }

}
