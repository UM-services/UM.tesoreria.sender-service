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

    @KafkaListener(topics = "send-chequera", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(SendChequeraEvent event) {
        log.info("Processing SendChequeraEvent: {}", event);
        try {
            chequeraService.enviarChequera(sendChequeraEventMapper.toSolicitud(event));
        } catch (Exception e) {
            log.error("Error processing message: {}", event, e);
        }
    }

}
