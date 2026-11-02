package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.SolicitudChequeraPublisher;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.mapper.SendChequeraEventMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitudChequeraKafkaPublisher implements SolicitudChequeraPublisher {

    static final String TOPIC = "send-chequera";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SendChequeraEventMapper mapper;

    @Override
    public void publicar(SolicitudEnvioChequera solicitud) {
        var event = mapper.toEvent(solicitud);
        kafkaTemplate.send(TOPIC, event);
        log.info("Solicitud de chequera publicada en {}: {}", TOPIC, event);
    }

}
