package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in.ProcesarCorreoEventUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final ProcesarCorreoEventUseCase procesarCorreoEventUseCase;

    /**
     * Sin {@code try/catch}: un error tiene que llegar al {@code DefaultErrorHandler} para que
     * Kafka reintente y, agotados los intentos, derive el record a {@code send-correo.DLT}.
     */
    @KafkaListener(topics = "send-correo", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificacionKafkaListenerContainerFactory")
    public void listen(CorreoEvent event) {
        log.info("Processing CorreoEvent: {}", event);
        procesarCorreoEventUseCase.procesar(event);
    }

}
