package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraService;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendChequeraEvent;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.mapper.SendChequeraEventMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChequeraEventListener {

    private final ChequeraService chequeraService;
    private final SendChequeraEventMapper sendChequeraEventMapper;

    /**
     * Sin {@code try/catch}: si el envío falla, la excepción tiene que llegar al
     * {@code DefaultErrorHandler} de {@code kafkaListenerContainerFactory} para que Kafka
     * reintente con backoff y, agotados los intentos, derive a {@code send-chequera.DLT}.
     * Atraparla acá commiteaba el offset y el correo se perdía sin reintento.
     */
    @KafkaListener(topics = "send-chequera", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(SendChequeraEvent event) {
        log.info("Processing SendChequeraEvent: {}", event);
        chequeraService.enviarChequera(sendChequeraEventMapper.toSolicitud(event));
    }

}
