package um.tesoreria.sender.hexagonal.gateways.correo.domain.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Correo {

    private String from;
    private List<String> para;
    private String replyTo;
    private String asunto;
    private String cuerpo;

    @Builder.Default
    private boolean html = false;

    @Builder.Default
    private List<AdjuntoCorreo> adjuntos = new ArrayList<>();

    @Builder.Default
    private List<ImagenEnLinea> imagenesEnLinea = new ArrayList<>();

}
