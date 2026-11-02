package um.tesoreria.sender.hexagonal.integration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.GroupState;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

class KafkaChequeraIntegracionIT extends IntegracionSoporte {

    private static final String EVENTO = "{"
            + "\"facultadId\":1,"
            + "\"tipoChequeraId\":2,"
            + "\"chequeraSerieId\":3,"
            + "\"alternativaId\":1,"
            + "\"copiaInformes\":false,"
            + "\"incluyeMatricula\":true,"
            + "\"codigoBarras\":true"
            + "}";

    @Test
    void consumeElEventoDeCoreYEnviaLaChequera() throws Exception {
        StubsCore.chequeraCompleta(true);

        esperarConsumidorListo();
        publicarEventoDeCore(false);

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertTrue(mensaje.getSubject().contains("Perez, Ana"));
    }

    @Test
    void toleraElHeaderLegacyDeCore() throws Exception {
        StubsCore.chequeraCompleta(true);

        esperarConsumidorListo();
        publicarEventoDeCore(true);

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertTrue(mensaje.getSubject().contains("Perez, Ana"));
    }

    private void esperarConsumidorListo() {
        Map<String, Object> propiedades = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        try (Admin admin = Admin.create(propiedades)) {
            await().atMost(Duration.ofSeconds(20)).until(() -> {
                try {
                    var descripcion = admin.describeConsumerGroups(List.of("test-group"))
                            .all().get().get("test-group");
                    return descripcion != null
                            && descripcion.groupState() == GroupState.STABLE
                            && !descripcion.members().isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });
        }
    }

    private void publicarEventoDeCore(boolean conTypeId) throws Exception {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        propiedades.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propiedades.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        try (var productor = new KafkaProducer<String, byte[]>(propiedades)) {
            var registro = new ProducerRecord<String, byte[]>("send-chequera", null, EVENTO.getBytes(UTF_8));
            if (conTypeId) {
                registro.headers().add("__TypeId__",
                        "um.tesoreria.core.event.SendChequeraEvent".getBytes(UTF_8));
            }
            productor.send(registro).get(10, TimeUnit.SECONDS);
        }
    }

}
