package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifyable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DomicilioDto implements Jsonifyable {

    private Long domicilioId;
    private BigDecimal personaId;
    private Integer documentoId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha = OffsetDateTime.now();
    private String calle = "";
    private String puerta = "";
    private String piso = "";
    private String dpto = "";
    private String telefono = "";
    private String movil = "";
    private String observaciones = "";
    private String codigoPostal = "";
    private Integer facultadId;
    private Integer provinciaId;
    private Integer localidadId;
    private String emailPersonal = "";
    private String emailInstitucional = "";
    private String laboral = "";

}
