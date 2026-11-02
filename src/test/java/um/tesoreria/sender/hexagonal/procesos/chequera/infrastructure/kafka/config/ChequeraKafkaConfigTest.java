package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendChequeraEvent;

/**
 * El contrato de {@code send-chequera} es el JSON del evento: el consumidor usa el tipo target
 * y no depende de {@code __TypeId__} (que además puede venir de cualquier FQCN).
 */
class ChequeraKafkaConfigTest {

    private static final String TOPIC = "send-chequera";

    private DefaultKafkaConsumerFactory<String, SendChequeraEvent> factory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        var config = new ChequeraKafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "kafka:9092");
        ReflectionTestUtils.setField(config, "groupId", "test-group");
        factory = (DefaultKafkaConsumerFactory<String, SendChequeraEvent>) config.consumerFactory();
    }

    @Test
    void deserializaElEventoSinHeaderDeTipo() {
        var event = deserializar(new RecordHeaders());

        assertThat(event.getFacultadId()).isEqualTo(1);
        assertThat(event.getTipoChequeraId()).isEqualTo(2);
        assertThat(event.getChequeraSerieId()).isEqualTo(3L);
        assertThat(event.getAlternativaId()).isEqualTo(1);
        assertThat(event.getCopiaInformes()).isFalse();
        assertThat(event.getIncluyeMatricula()).isTrue();
        assertThat(event.getCodigoBarras()).isTrue();
    }

    @Test
    void ignoraElTypeIdQueEmiteCore() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "um.tesoreria.core.event.SendChequeraEvent".getBytes(UTF_8));

        assertThat(deserializar(headers).getChequeraSerieId()).isEqualTo(3L);
    }

    @Test
    void ignoraUnTypeIdDeUnPaqueteDesconocido() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "com.desconocido.Evento".getBytes(UTF_8));

        assertThat(deserializar(headers).getChequeraSerieId()).isEqualTo(3L);
    }

    private SendChequeraEvent deserializar(Headers headers) {
        return factory.getValueDeserializer()
                .deserialize(TOPIC, headers, payload());
    }

    private byte[] payload() {
        return """
                {"facultadId":1,"tipoChequeraId":2,"chequeraSerieId":3,"alternativaId":1,
                 "copiaInformes":false,"incluyeMatricula":true,"codigoBarras":true}
                """.getBytes(UTF_8);
    }

}
