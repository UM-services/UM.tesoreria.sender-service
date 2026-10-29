package um.tesoreria.sender.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

/**
 * Topics de los que {@code tesoreria-sender} es dueño.
 *
 * <p>Declara {@code notificacion-resultado} (lo produce) y los DLT de los listeners que deriva
 * ({@code send-chequera.DLT}, {@code send-cuota.DLT}, {@code send-correo.DLT}). Los topics
 * principales de correo los declara {@code tesoreria-core}, que es quien los produce.</p>
 *
 * <p>Los DLT deben tener las mismas particiones que su topic origen: el
 * {@code DeadLetterPublishingRecoverer} publica en el mismo número de partición del registro
 * original. Spring Boot crea estos {@code NewTopic} al arrancar con el {@code KafkaAdmin}
 * auto-configurado; el réplication factor es ambiental ({@code app.kafka.topics.replicas}).</p>
 */
@Configuration
public class KafkaTopicsConfiguration {

    private static final String DLT_RETENTION_MS = "1209600000"; // 14 días

    @Bean
    NewTopic notificacionResultadoTopic(@Value("${app.kafka.topics.partitions:1}") int partitions,
                                        @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return newTopic("notificacion-resultado", partitions, replicas, null);
    }

    @Bean
    NewTopic sendChequeraDltTopic(@Value("${app.kafka.topics.partitions:1}") int partitions,
                                  @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return newTopic("send-chequera.DLT", partitions, replicas, DLT_RETENTION_MS);
    }

    @Bean
    NewTopic sendCorreoDltTopic(@Value("${app.kafka.topics.partitions:1}") int partitions,
                                @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return newTopic("send-correo.DLT", partitions, replicas, DLT_RETENTION_MS);
    }

    /**
     * DLT de {@code send-cuota}: lo deriva {@code cuotaKafkaListenerContainerFactory}. Faltaba:
     * el listener ya derivaba a este topic pero nadie lo declaraba, así que dependía del
     * auto-create del broker (1 partición, retención default).
     */
    @Bean
    NewTopic sendCuotaDltTopic(@Value("${app.kafka.topics.partitions:1}") int partitions,
                               @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return newTopic("send-cuota.DLT", partitions, replicas, DLT_RETENTION_MS);
    }

    private NewTopic newTopic(String name, int partitions, int replicas, String retentionMs) {
        var builder = TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .config("min.insync.replicas", String.valueOf(Math.min(2, replicas)));
        if (retentionMs != null) {
            builder.configs(Map.of("retention.ms", retentionMs, "cleanup.policy", "delete"));
        }
        return builder.build();
    }
}
