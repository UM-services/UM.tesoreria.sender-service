package um.tesoreria.sender.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import um.tesoreria.sender.client.tesoreria.core.ChequeraMessageCheckClient;
import um.tesoreria.sender.configuration.RabbitMQConfig;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraMessageCheckDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.message.ChequeraMessageDto;
import um.tesoreria.sender.service.ChequeraService;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class ChequeraConsumer {

    private final ChequeraService chequeraService;
    private final ChequeraMessageCheckClient chequeraMessageCheckClient;

    public ChequeraConsumer(ChequeraService chequeraService, ChequeraMessageCheckClient chequeraMessageCheckClient) {
        this.chequeraService = chequeraService;
        this.chequeraMessageCheckClient = chequeraMessageCheckClient;
    }

    @PostConstruct
    public void init() {
        log.info("Consumidor de RabbitMQ inicializado y escuchando en la cola: {}", RabbitMQConfig.QUEUE_CHEQUERA);
        log.error("TEST - PostConstruct ChequeraConsumer ejecutado");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CHEQUERA, ackMode = "MANUAL")
    @Transactional
    public void handleChequeraMessage(ChequeraMessageDto chequeraMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Processing ChequeraConsumer.handleChequeraMessage");
        logChequeraMessage(chequeraMessage);
        
        try {
            log.debug("handleChequeraMessage - buscando si ya fue enviado");
            boolean sentMessage = verifySentMessage(chequeraMessage.getUuid());
            
            if (sentMessage) {
                log.info("handleChequeraMessage - mensaje ya fue enviado previamente, UUID: {}", chequeraMessage.getUuid());
                channel.basicAck(tag, false);
                return;
            }
            
            String result = chequeraService.sendChequera(
                chequeraMessage.getFacultadId(),
                chequeraMessage.getTipoChequeraId(),
                chequeraMessage.getChequeraSerieId(),
                chequeraMessage.getAlternativaId(),
                chequeraMessage.getCopiaInformes(),
                chequeraMessage.getCodigoBarras(),
                chequeraMessage.getIncluyeMatricula()
            );
            
            log.info("handleChequeraMessage - resultado del envío: {}", result);
            channel.basicAck(tag, false);
            log.debug("handleChequeraMessage - guardando chequeraMessageCheck");
            var chequeraMessageCheck = new ChequeraMessageCheckDto.Builder()
                    .chequeraMessageCheckId(chequeraMessage.getUuid())
                    .facultadId(chequeraMessage.getFacultadId())
                    .tipoChequeraId(chequeraMessage.getTipoChequeraId())
                    .chequeraSerieId(chequeraMessage.getChequeraSerieId())
                    .payload(jsonChequeraMessage(chequeraMessage))
                    .build();

            chequeraMessageCheckClient.add(chequeraMessageCheck);

        } catch (Exception e) {
            log.error("handleChequeraMessage - error al procesar mensaje, UUID: {}, error: {}", 
                chequeraMessage.getUuid(), e.getMessage(), e);
            channel.basicNack(tag, false, true);
        }
    }

    private boolean verifySentMessage(UUID uuid) {
        try {
            chequeraMessageCheckClient.findByChequeraMessageCheckId(uuid);
            return true;
        } catch (Exception e) {
            log.debug("verifySentMessage - mensaje no encontrado, UUID: {}", uuid);
            return false;
        }
    }

    private void logChequeraMessage(ChequeraMessageDto chequeraMessage) {
        log.debug("ChequeraMessage -> {}", jsonChequeraMessage(chequeraMessage));
    }

    private String jsonChequeraMessage(ChequeraMessageDto chequeraMessage) {
        try {
            return JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraMessage);
        } catch (JsonProcessingException e) {
            return "ChequeraMessage jsonify error";
        }
    }

}
