package um.tesoreria.sender.hexagonal.integration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

class CuotaIntegracionIT extends IntegracionSoporte {

    @Test
    void consumeElEventoDeCuotaYEnviaElRecordatorio() throws Exception {
        StubsCore.chequeraCompleta(true);
        StubsCore.enlacePago(true);

        publicarCuota(9L);

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertTrue(mensaje.getSubject().contains("Perez, Ana"));
        assertEquals(1, CORREO.getReceivedMessages().length);
    }

    private void publicarCuota(Long chequeraCuotaId) throws Exception {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        propiedades.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propiedades.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        String evento = "{\"chequeraCuotaId\":" + chequeraCuotaId + "}";
        try (var productor = new KafkaProducer<String, byte[]>(propiedades)) {
            var registro = new ProducerRecord<String, byte[]>("send-cuota", null, evento.getBytes(UTF_8));
            productor.send(registro).get(10, TimeUnit.SECONDS);
        }
    }

}
