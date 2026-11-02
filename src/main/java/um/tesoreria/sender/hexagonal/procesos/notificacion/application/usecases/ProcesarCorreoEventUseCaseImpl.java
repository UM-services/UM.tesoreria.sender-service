package um.tesoreria.sender.hexagonal.procesos.notificacion.application.usecases;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.notificacion.application.exception.NotificacionException;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in.EnviarNotificacionUseCase;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in.ProcesarCorreoEventUseCase;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.out.ResultadoNotificacionPublisher;

/**
 * Procesa un {@code send-correo}. La entrega de Kafka es <em>at-least-once</em>: el
 * {@code eventId} se registra en una caché acotada para no reenviar el mismo correo ante una
 * redelivery. Es una idempotencia <em>best effort</em> (el sender no tiene base propia); la
 * ventana cubre los reintentos y el DLT.
 */
@Component
@Slf4j
public class ProcesarCorreoEventUseCaseImpl implements ProcesarCorreoEventUseCase {

    private static final int MAX_EVENTOS = 100_000;
    private static final Duration RETENCION = Duration.ofHours(24);

    private final EnviarNotificacionUseCase enviarCorreoUseCase;
    private final ResultadoNotificacionPublisher resultadoPublisher;

    private final Cache<String, Boolean> procesados = Caffeine.newBuilder()
            .maximumSize(MAX_EVENTOS)
            .expireAfterWrite(RETENCION)
            .build();

    public ProcesarCorreoEventUseCaseImpl(EnviarNotificacionUseCase enviarCorreoUseCase,
                                          ResultadoNotificacionPublisher resultadoPublisher) {
        this.enviarCorreoUseCase = enviarCorreoUseCase;
        this.resultadoPublisher = resultadoPublisher;
    }

    @Override
    public void procesar(CorreoEvent event) {
        validar(event);
        if (procesados.getIfPresent(event.getEventId()) != null) {
            log.info("Evento {} ya procesado: no se reenvía", event.getEventId());
            return;
        }

        try {
            enviarCorreoUseCase.enviar(event.getPayload());
            procesados.put(event.getEventId(), Boolean.TRUE);
            publicarResultado(event, true, null);
        } catch (RuntimeException e) {
            log.error("Error enviando el evento {}: {}", event.getEventId(), e.getMessage(), e);
            publicarResultado(event, false, e.getMessage());
            // Se relanza para que el contenedor reintente y, agotados los intentos, derive al DLT.
            throw e;
        }
    }

    private void validar(CorreoEvent event) {
        if (event == null) {
            throw new NotificacionException(NotificacionException.Tipo.EVENTO_INVALIDO, "El evento es obligatorio");
        }
        if (!CorreoEvent.TIPO.equals(event.getEventType())) {
            throw new NotificacionException(NotificacionException.Tipo.EVENTO_INVALIDO,
                    "eventType desconocido: " + event.getEventType());
        }
        if (event.getVersion() == null || event.getVersion() != CorreoEvent.VERSION_ACTUAL) {
            throw new NotificacionException(NotificacionException.Tipo.EVENTO_INVALIDO,
                    "version no soportada: " + event.getVersion());
        }
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new NotificacionException(NotificacionException.Tipo.EVENTO_INVALIDO, "eventId es obligatorio");
        }
    }

    private void publicarResultado(CorreoEvent event, boolean enviado, String error) {
        try {
            resultadoPublisher.publicar(event, enviado, error);
        } catch (RuntimeException e) {
            // No se relanza: el correo ya se envió (o ya falló) y reintentar el evento lo duplicaría.
            log.error("No se pudo publicar el resultado del evento {}: {}", event.getEventId(), e.getMessage(), e);
        }
    }

}
