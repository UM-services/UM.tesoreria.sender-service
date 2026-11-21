package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ResultadoEnvioChequeraPublisher;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.ResultadoEnvioChequeraEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResultadoEnvioChequeraKafkaPublisher implements ResultadoEnvioChequeraPublisher {

    static final String TOPIC = "notificacion-resultado";
    private static final String TIPO_CHEQUERA = "CHEQUERA";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publicar(Long chequeraId, boolean enviado, String error) {
        if (chequeraId == null) {
            log.warn("No se puede publicar el resultado de una chequera sin id");
            return;
        }
        var eventId = UUID.randomUUID().toString();
        var resultado = new ResultadoEnvioChequeraEvent(
                eventId,
                OffsetDateTime.now(),
                enviado,
                error,
                new ResultadoEnvioChequeraEvent.Referencia(TIPO_CHEQUERA, String.valueOf(chequeraId)));
        kafkaTemplate.send(TOPIC, eventId, resultado)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("No se pudo publicar el resultado de la chequera {} en {}: {}",
                                chequeraId, TOPIC, ex.getMessage());
                    }
                });
        log.debug("Resultado de chequera {} publicado en {}: enviado={}", chequeraId, TOPIC, enviado);
    }
}
