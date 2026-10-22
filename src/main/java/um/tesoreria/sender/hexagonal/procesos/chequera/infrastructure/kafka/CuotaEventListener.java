package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in.EnviarCuotaUseCase;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.kafka.event.SendCuotaEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CuotaEventListener {

    private final EnviarCuotaUseCase enviarCuotaUseCase;

    @KafkaListener(topics = "send-cuota", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "cuotaKafkaListenerContainerFactory")
    public void listen(SendCuotaEvent event) {
        log.info("Processing SendCuotaEvent: {}", event);
        enviarCuotaUseCase.enviar(event.getChequeraCuotaId());
    }

}
