package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.SolicitudCuotaPublisher;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendCuotaEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitudCuotaKafkaPublisher implements SolicitudCuotaPublisher {

    static final String TOPIC = "send-cuota";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publicar(Long chequeraCuotaId) {
        kafkaTemplate.send(TOPIC, SendCuotaEvent.builder().chequeraCuotaId(chequeraCuotaId).build());
        log.info("Solicitud de cuota publicada en {}: chequeraCuotaId={}", TOPIC, chequeraCuotaId);
    }

}
