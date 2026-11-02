package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Envelope del topic {@code send-correo}. {@code eventType} es un string estable (nunca un
 * FQCN) y {@code version} sólo se incrementa ante cambios incompatibles del payload.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CorreoEvent {

    public static final String TIPO = "CORREO";
    public static final int VERSION_ACTUAL = 1;

    private String eventType;
    private Integer version;
    private String eventId;
    private OffsetDateTime occurredAt;
    private CorreoPayload payload;

}
