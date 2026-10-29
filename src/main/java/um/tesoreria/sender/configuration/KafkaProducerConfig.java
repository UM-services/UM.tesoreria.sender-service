package um.tesoreria.sender.configuration;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

/**
 * Productores del sender. Se usan para republicar solicitudes (shim REST → topic) y para
 * informar el resultado de una notificación a core.
 *
 * <p>El contrato del topic es el JSON del evento, no el FQCN de la clase Java: se escribe con
 * {@code addTypeInfo(false)} y no se emite {@code __TypeId__}.</p>
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public JacksonJsonSerializer<Object> kafkaJsonSerializer() {
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();
        serializer.setAddTypeInfo(false);
        return serializer;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(JacksonJsonSerializer<Object> kafkaJsonSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Sin KEY/VALUE_SERIALIZER_CLASS: se pasan las instancias al factory.
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), kafkaJsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(JacksonJsonSerializer<Object> kafkaJsonSerializer) {
        return new KafkaTemplate<>(producerFactory(kafkaJsonSerializer));
    }

    /**
     * Template exclusivo del DLT: los records que no se pudieron deserializar se republican
     * como bytes crudos para no perder el payload original.
     */
    @Bean
    public ProducerFactory<String, byte[]> dltProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, byte[]> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

}
