package um.tesoreria.sender.hexagonal.integration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto.CorreoRequest;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto.CorreoResponse;

class NotificacionIntegracionIT extends IntegracionSoporte {

    private static final String EVENTO = """
            {
              "eventType": "CORREO",
              "version": 1,
              "eventId": "%s",
              "occurredAt": "2026-10-22T12:00:00Z",
              "payload": {
                "remitente": "NOTIFICACION",
                "para": ["alumno@um.edu.ar"],
                "replyTo": "no-reply@um.edu.ar",
                "asunto": "Notificación por Deuda",
                "cuerpo": "Estimad@ Estudiante: regularice su situación.",
                "html": false,
                "referencia": { "tipo": "PERSONA_SUSPENDIDO", "id": "7" }
              }
            }
            """;

    @Test
    void consumeElEventoYEnviaElCorreo() throws Exception {
        String eventId = UUID.randomUUID().toString();
        publicar("send-correo", EVENTO.formatted(eventId), false);

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertEquals("Notificación por Deuda", mensaje.getSubject());
        assertEquals("alumno@um.edu.ar", mensaje.getAllRecipients()[0].toString());
        assertTrue(mensaje.getFrom()[0].toString().contains("fromnotificacion"));
    }

    @Test
    void noReenviaElMismoEventId() throws Exception {
        String eventId = UUID.randomUUID().toString();
        publicar("send-correo", EVENTO.formatted(eventId), false);
        publicar("send-correo", EVENTO.formatted(eventId), false);

        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5))
                .until(() -> CORREO.getReceivedMessages().length == 1);
    }

    @Test
    void publicaElResultadoDelEnvioParaCore() throws Exception {
        String eventId = UUID.randomUUID().toString();
        publicar("send-correo", EVENTO.formatted(eventId), false);

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        var resultado = consumirHasta("notificacion-resultado", texto -> texto.contains(eventId));
        assertTrue(resultado.contains("\"enviado\":true"));
        assertTrue(resultado.contains("\"tipo\":\"PERSONA_SUSPENDIDO\""));
        assertTrue(resultado.contains("\"id\":\"7\""));
    }

    @Test
    void enviaPorHttpSinPasarPorKafka() throws Exception {
        var request = CorreoRequest.builder()
                .para(List.of("proveedor@um.edu.ar"))
                .asunto("Aviso de pago")
                .cuerpo("Su orden de pago fue abonada.")
                .build();

        ResponseEntity<CorreoResponse> respuesta = http().post()
                .uri("/api/tesoreria/sender/correo/enviar")
                .body(request)
                .retrieve()
                .toEntity(CorreoResponse.class);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, CORREO.getReceivedMessages().length);
        assertEquals("proveedor@um.edu.ar", CORREO.getReceivedMessages()[0].getAllRecipients()[0].toString());
    }

    private void publicar(String topic, String payload, boolean conTypeId) throws Exception {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        propiedades.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propiedades.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        try (var productor = new KafkaProducer<String, byte[]>(propiedades)) {
            var registro = new ProducerRecord<String, byte[]>(topic, null, payload.getBytes(UTF_8));
            if (conTypeId) {
                registro.headers().add("__TypeId__",
                        "um.tesoreria.core.event.CorreoEvent".getBytes(UTF_8));
            }
            productor.send(registro).get(10, TimeUnit.SECONDS);
        }
    }

    private List<String> consumir(String topic, int cantidad) {
        Map<String, Object> propiedades = propiedadesConsumidor();
        List<String> valores = new ArrayList<>();
        try (var consumidor = new KafkaConsumer<String, String>(propiedades)) {
            consumidor.subscribe(List.of(topic));
            await().atMost(Duration.ofSeconds(20)).until(() -> {
                consumidor.poll(Duration.ofMillis(500)).forEach(record -> valores.add(record.value()));
                return valores.size() >= cantidad;
            });
        }
        return valores;
    }

    private String consumirHasta(String topic, java.util.function.Predicate<String> condicion) {
        Map<String, Object> propiedades = propiedadesConsumidor();
        List<String> valores = new ArrayList<>();
        try (var consumidor = new KafkaConsumer<String, String>(propiedades)) {
            consumidor.subscribe(List.of(topic));
            await().atMost(Duration.ofSeconds(20)).until(() -> {
                consumidor.poll(Duration.ofMillis(500)).forEach(record -> valores.add(record.value()));
                return valores.stream().anyMatch(condicion);
            });
        }
        return valores.stream().filter(condicion).findFirst().orElseThrow();
    }

    private Map<String, Object> propiedadesConsumidor() {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        propiedades.put(ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID());
        propiedades.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propiedades.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propiedades.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return propiedades;
    }

}
