package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.out.ResultadoNotificacionPublisher;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka.event.ResultadoNotificacionEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultadoNotificacionKafkaPublisher implements ResultadoNotificacionPublisher {

    static final String TOPIC = "notificacion-resultado";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publicar(CorreoEvent event, boolean enviado, String error) {
        var resultado = ResultadoNotificacionEvent.builder()
                .eventId(event.getEventId())
                .occurredAt(java.time.OffsetDateTime.now())
                .enviado(enviado)
                .error(error)
                .referencia(event.getPayload() == null ? null : event.getPayload().getReferencia())
                .build();
        kafkaTemplate.send(TOPIC, event.getEventId(), resultado)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("No se pudo publicar el resultado de {} en {}: {}",
                                event.getEventId(), TOPIC, ex.getMessage());
                    }
                });
        log.debug("Resultado publicado para {}: enviado={}", event.getEventId(), enviado);
    }

}
