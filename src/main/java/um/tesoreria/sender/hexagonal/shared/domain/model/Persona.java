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
public class Persona {

    private BigDecimal personaId;
    private Integer documentoId;
    private String apellido;
    private String nombre;

    public String apellidoNombre() {
        return apellido + ", " + nombre;
    }

}
