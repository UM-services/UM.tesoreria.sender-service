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
import um.tesoreria.sender.client.tesoreria.core.ReciboMessageCheckClient;
import um.tesoreria.sender.configuration.RabbitMQConfig;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ReciboMessageCheckDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.message.ReciboMessageDto;
import um.tesoreria.sender.service.ReciboService;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class ReciboConsumer {

    private final ReciboService reciboService;
    private final ReciboMessageCheckClient reciboMessageCheckClient;

    public ReciboConsumer(ReciboService reciboService,
                          ReciboMessageCheckClient reciboMessageCheckClient) {
        this.reciboService = reciboService;
        this.reciboMessageCheckClient = reciboMessageCheckClient;
    }

    @PostConstruct
    public void init() {
        log.info("Consumidor de RabbitMQ inicializado y escuchando en la cola: {}", RabbitMQConfig.QUEUE_INVOICE);
        log.error("TEST - PostConstruct ReciboConsumer ejecutado");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVOICE, ackMode = "MANUAL")
    @Transactional
    public void handleInvoiceMessage(ReciboMessageDto reciboMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Processing ReciboConsumer.handleInvoiceMessage");
        logReciboMessage(reciboMessage);

        try {
            log.debug("handleInvoiceMessage - buscando si ya fue enviado");
            boolean sentMessage = verifySentMessage(reciboMessage.getUuid());

            if (sentMessage) {
                log.info("handleInvoiceMessage - mensaje ya fue enviado previamente, UUID: {}", reciboMessage.getUuid());
                channel.basicAck(tag, false);
                return;
            }

            String result = reciboService.send(reciboMessage.getFacturacionElectronicaId(), null);

            log.info("handleInvoiceMessage - resultado del envío: {}", result);
            channel.basicAck(tag, false);
            log.debug("handleInvoiceMessage - guardando reciboMessageCheck");
            var reciboMessageCheck = new ReciboMessageCheckDto.Builder()
                    .reciboMessageCheckId(reciboMessage.getUuid())
                    .facturacionElectronicaId(reciboMessage.getFacturacionElectronicaId())
                    .chequeraPagoId(reciboMessage.getChequeraPagoId())
                    .facultadId(reciboMessage.getFacultadId())
                    .tipoChequeraId(reciboMessage.getTipoChequeraId())
                    .chequeraSerieId(reciboMessage.getChequeraSerieId())
                    .productoId(reciboMessage.getProductoId())
                    .alternativaId(reciboMessage.getAlternativaId())
                    .cuotaId(reciboMessage.getCuotaId())
                    .payload(jsonReciboMessage(reciboMessage))
                    .build();

            reciboMessageCheckClient.add(reciboMessageCheck);

        } catch (Exception e) {
            log.error("handleInvoiceMessage - error al procesar mensaje, UUID: {}, error: {}",
                reciboMessage.getUuid(), e.getMessage(), e);
            channel.basicNack(tag, false, true);
        }
    }

    private boolean verifySentMessage(UUID uuid) {
        try {
            reciboMessageCheckClient.findByReciboMessageCheckId(uuid);
            return true;
        } catch (Exception e) {
            log.debug("verifySentMessage - mensaje no encontrado, UUID: {}", uuid);
            return false;
        }
    }

    private void logReciboMessage(ReciboMessageDto reciboMessage) {
        log.debug("ReciboMessage -> {}", jsonReciboMessage(reciboMessage));
    }

    private String jsonReciboMessage(ReciboMessageDto reciboMessage) {
        try {
            return JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(reciboMessage);
        } catch (JsonProcessingException e) {
            return "ReciboMessage jsonify error";
        }
    }

}
