package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
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
public class PersonaDto implements Jsonifyable {

    private Long uniqueId;
    private BigDecimal personaId;
    private Integer documentoId;
    private String apellido;
    private String nombre;
    private String sexo;
    private Byte primero = 0;
    private String cuit = "";
    private String cbu = "";
    private String password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getApellidoNombre() {
        return apellido + ", " + nombre;
    }

}
