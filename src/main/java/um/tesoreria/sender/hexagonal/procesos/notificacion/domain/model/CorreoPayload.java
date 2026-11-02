package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contenido de un correo genérico. No admite adjuntos: los flujos que necesitan documentos
 * (chequera, recibo) usan eventos con IDs y el sender genera el PDF.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CorreoPayload {

    @Builder.Default
    private Remitente remitente = Remitente.NOTIFICACION;

    @Builder.Default
    private List<String> para = new ArrayList<>();

    private String replyTo;
    private String asunto;
    private String cuerpo;

    @Builder.Default
    private boolean html = false;

    private ReferenciaNotificacion referencia;

}
