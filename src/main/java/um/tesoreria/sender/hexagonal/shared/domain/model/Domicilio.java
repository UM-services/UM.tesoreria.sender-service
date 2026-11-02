package um.tesoreria.sender.hexagonal.shared.domain.model;

import java.math.BigDecimal;
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
public class Domicilio {

    private Long domicilioId;
    private BigDecimal personaId;
    private Integer documentoId;
    private String calle;
    private String puerta;
    private String piso;
    private String dpto;
    private String observaciones;
    private String emailPersonal;
    private String emailInstitucional;

}
