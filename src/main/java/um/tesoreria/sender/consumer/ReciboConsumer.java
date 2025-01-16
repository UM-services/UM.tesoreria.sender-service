package um.tesoreria.sender.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.configuration.RabbitMQConfig;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.FacturacionElectronicaDto;
import um.tesoreria.sender.service.ReciboService;

@Component
@Slf4j
public class ReciboConsumer {

    private final ReciboService reciboService;

    public ReciboConsumer(ReciboService reciboService) {
        this.reciboService = reciboService;
    }

    @PostConstruct
    public void init() {
        log.info("Consumidor de RabbitMQ inicializado y escuchando en la cola: {}", RabbitMQConfig.QUEUE_TESTER);
        log.info("Consumidor de RabbitMQ inicializado y escuchando en la cola: {}", RabbitMQConfig.QUEUE_INVOICE);
        log.error("TEST - PostConstruct ejecutado");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TESTER)
    public void handleTesterMessage(String message) {
        log.debug("Tester Message -> {}", message);
        log.error("TEST - Mensaje recibido: {}", message);
        simular(message);
    }

    private void simular(String message) {
        log.debug("Simulando consumo de {}", message);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVOICE)
    public void handleInvoiceMessage(FacturacionElectronicaDto facturacionElectronica) throws MessagingException {
        log.info("Processing invoice shipment");
        logFacturacionElectronica(facturacionElectronica);
        log.info(reciboService.send(facturacionElectronica.getFacturacionElectronicaId(), facturacionElectronica));
    }

    private void logFacturacionElectronica(FacturacionElectronicaDto facturacionElectronica) {
        try {
            log.info("FacturacionElectronica -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facturacionElectronica));
        } catch (JsonProcessingException e) {
            log.info("FacturacionElectronica jsonify error: {}", e.getMessage());
        }
    }

}
