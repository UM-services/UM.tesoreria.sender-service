package um.tesoreria.sender.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.event.SendChequeraEvent;
import um.tesoreria.sender.service.FormulariosToPdfService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChequeraEventListener {

    private final FormulariosToPdfService formulariosToPdfService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "send-chequera", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Object message) {
        log.debug("Received message: {}", message);
        try {
            SendChequeraEvent event;
            if (message instanceof String) {
                event = objectMapper.readValue((String) message, SendChequeraEvent.class);
            } else {
                event = objectMapper.convertValue(message, SendChequeraEvent.class);
            }

            log.info("Processing SendChequeraEvent: {}", event);
            formulariosToPdfService.generateChequeraPdf(
                    event.getFacultadId(),
                    event.getTipoChequeraId(),
                    event.getChequeraSerieId(),
                    event.getAlternativaId(),
                    event.getCodigoBarras(),
                    event.getIncluyeMatricula(), // mapping 'incluyeMatricula' to 'completa' parameter if appropriate,
                                                 // checking signature next.
                    null // preferences list, assuming null is handled as per original code
            );
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
