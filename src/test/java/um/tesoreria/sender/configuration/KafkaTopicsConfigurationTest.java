package um.tesoreria.sender.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicsConfigurationTest {

    private final KafkaTopicsConfiguration configuration = new KafkaTopicsConfiguration();

    @Test
    void declaraElTopicDeNotificacionResultado() {
        assertTopic(configuration.notificacionResultadoTopic(1, 1), "notificacion-resultado", 1, 1);
    }

    @Test
    void declaraLasDltDeLosListenersQueDeriva() {
        NewTopic chequera = configuration.sendChequeraDltTopic(1, 1);
        NewTopic correo = configuration.sendCorreoDltTopic(1, 1);
        NewTopic cuota = configuration.sendCuotaDltTopic(1, 1);

        assertTopic(chequera, "send-chequera.DLT", 1, 1);
        assertTopic(correo, "send-correo.DLT", 1, 1);
        assertTopic(cuota, "send-cuota.DLT", 1, 1);
        assertThat(chequera.configs()).containsEntry("retention.ms", "1209600000");
        assertThat(correo.configs()).containsEntry("retention.ms", "1209600000");
        assertThat(cuota.configs()).containsEntry("retention.ms", "1209600000");
    }

    @Test
    void ajustaMinInsyncReplicasAlReplicationFactor() {
        assertThat(configuration.notificacionResultadoTopic(1, 1).configs())
                .containsEntry("min.insync.replicas", "1");
        assertThat(configuration.notificacionResultadoTopic(3, 3).configs())
                .containsEntry("min.insync.replicas", "2");
        // configs() de TopicBuilder fusiona: la DLT conserva min.isr y su retención
        assertThat(configuration.sendChequeraDltTopic(3, 3).configs())
                .containsEntry("min.insync.replicas", "2")
                .containsEntry("retention.ms", "1209600000");
    }

    private void assertTopic(NewTopic topic, String name, int partitions, int replicas) {
        assertThat(topic.name()).isEqualTo(name);
        assertThat(topic.numPartitions()).isEqualTo(partitions);
        assertThat(topic.replicationFactor()).isEqualTo((short) replicas);
    }
}
