package um.tesoreria.sender.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.event.SendChequeraEvent;
import um.tesoreria.sender.service.ChequeraService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChequeraEventListener {

    private final ChequeraService chequeraService;

    @KafkaListener(topics = "send-chequera", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(SendChequeraEvent event) {
        log.info("Processing SendChequeraEvent: {}", event);
        try {
            chequeraService.sendChequera(
                    event.getFacultadId(),
                    event.getTipoChequeraId(),
                    event.getChequeraSerieId(),
                    event.getAlternativaId(),
                    event.getCopiaInformes(),
                    event.getCodigoBarras(),
                    event.getIncluyeMatricula()
            );
        } catch (Exception e) {
            log.error("Error processing message: {}", event, e);
        }
    }
}

