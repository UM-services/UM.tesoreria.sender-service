package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

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
public class CarreraDto implements Jsonifyable {

    private Long uniqueId;
    private Integer facultadId;
    private Integer planId;
    private Integer carreraId;
    private String nombre = "";
    private String iniciales = "";
    private String titulo = "";
    private byte trabajofinal = 0;
    private String resolucion = "";
    private byte chequeraunica = 0;
    private Integer bloqueId;
    private int obligatorias = 0;
    private int optativas = 0;
    private byte vigente = 0;

}
