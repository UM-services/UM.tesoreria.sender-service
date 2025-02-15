package um.tesoreria.sender.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import um.tesoreria.sender.configuration.RabbitMQConfig;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.message.ChequeraMessageDto;
import um.tesoreria.sender.service.ChequeraService;

@Component
@Slf4j
public class ChequeraConsumer {

    private final ChequeraService chequeraService;

    public ChequeraConsumer(ChequeraService chequeraService) {
        this.chequeraService = chequeraService;
    }

    @PostConstruct
    public void init() {
        log.info("Consumidor de RabbitMQ inicializado y escuchando en la cola: {}", RabbitMQConfig.QUEUE_CHEQUERA);
        log.error("TEST - PostConstruct ChequeraConsumer ejecutado");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CHEQUERA)
    @Transactional
    public void handleChequeraMessage(ChequeraMessageDto chequeraMessage) throws MessagingException {
        log.info("Processing chequera shipment");
        logChequeraMessage(chequeraMessage);
        log.info(chequeraService.sendChequera(chequeraMessage.getFacultadId(),
                                              chequeraMessage.getTipoChequeraId(),
                                              chequeraMessage.getChequeraSerieId(),
                                              chequeraMessage.getAlternativaId(),
                                              chequeraMessage.getCopiaInformes(),
                                              chequeraMessage.getCodigoBarras(),
                                              chequeraMessage.getIncluyeMatricula()));
    }

    private void logChequeraMessage(ChequeraMessageDto chequeraMessage) {
        try {
            log.debug("ChequeraMessage -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraMessage));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraMessage jsonify error: {}", e.getMessage());
        }
    }

}
