package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.Remitente;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorreoRequest {

    @Builder.Default
    private Remitente remitente = Remitente.NOTIFICACION;

    @NotEmpty(message = "Debe indicar al menos un destinatario")
    private List<String> para;

    private String replyTo;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El cuerpo es obligatorio")
    private String cuerpo;

    @Builder.Default
    private boolean html = false;

}
