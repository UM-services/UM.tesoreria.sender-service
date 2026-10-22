package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.kafka.event;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.ReferenciaNotificacion;

/**
 * Resultado del envío, publicado en {@code notificacion-resultado}. Core lo consume para marcar
 * el estado de negocio (por ejemplo {@code persona_suspendido.notificado}) sólo cuando el correo
 * salió efectivamente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResultadoNotificacionEvent {

    private String eventId;
    private OffsetDateTime occurredAt;
    private boolean enviado;
    private String error;
    private ReferenciaNotificacion referencia;

}
