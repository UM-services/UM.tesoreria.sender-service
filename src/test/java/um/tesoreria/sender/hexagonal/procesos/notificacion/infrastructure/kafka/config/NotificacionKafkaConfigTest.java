package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.Remitente;

/**
 * El contrato de {@code send-correo} es el JSON del envelope: el consumidor usa el tipo target y
 * no depende de {@code __TypeId__}.
 */
class NotificacionKafkaConfigTest {

    private static final String TOPIC = "send-correo";

    private DefaultKafkaConsumerFactory<String, CorreoEvent> factory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        var config = new NotificacionKafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "kafka:9092");
        ReflectionTestUtils.setField(config, "groupId", "test-group");
        factory = (DefaultKafkaConsumerFactory<String, CorreoEvent>) config.correoEventConsumerFactory();
    }

    @Test
    void deserializaElEnvelopeCompleto() {
        var event = deserializar(new RecordHeaders());

        assertThat(event.getEventType()).isEqualTo("CORREO");
        assertThat(event.getVersion()).isEqualTo(1);
        assertThat(event.getEventId()).isEqualTo("evt-1");
        assertThat(event.getPayload().getRemitente()).isEqualTo(Remitente.NOTIFICACION);
        assertThat(event.getPayload().getPara()).containsExactly("alumno@um.edu.ar");
        assertThat(event.getPayload().getAsunto()).isEqualTo("Notificación por Deuda");
        assertThat(event.getPayload().isHtml()).isFalse();
        assertThat(event.getPayload().getReferencia().getTipo()).isEqualTo("PERSONA_SUSPENDIDO");
        assertThat(event.getPayload().getReferencia().getId()).isEqualTo("7");
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void ignoraElTypeIdDelProductor() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "um.tesoreria.core.event.CorreoEvent".getBytes(UTF_8));

        assertThat(deserializar(headers).getEventId()).isEqualTo("evt-1");
    }

    @Test
    void ignoraUnTypeIdDesconocido() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "com.desconocido.Evento".getBytes(UTF_8));

        assertThat(deserializar(headers).getEventId()).isEqualTo("evt-1");
    }

    private CorreoEvent deserializar(Headers headers) {
        return factory.getValueDeserializer().deserialize(TOPIC, headers, payload());
    }

    private byte[] payload() {
        return """
                {"eventType":"CORREO","version":1,"eventId":"evt-1",
                 "occurredAt":"2026-10-22T12:00:00Z",
                 "payload":{"remitente":"NOTIFICACION","para":["alumno@um.edu.ar"],
                            "replyTo":"no-reply@um.edu.ar","asunto":"Notificación por Deuda",
                            "cuerpo":"Hola","html":false,
                            "referencia":{"tipo":"PERSONA_SUSPENDIDO","id":"7"}}}
                """.getBytes(UTF_8);
    }

}
