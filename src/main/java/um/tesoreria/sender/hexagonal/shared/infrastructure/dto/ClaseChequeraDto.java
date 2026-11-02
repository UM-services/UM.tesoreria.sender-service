package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClaseChequeraDto {

    private Integer claseChequeraId;
    private String nombre;
    private byte preuniversitario = 0;
    private byte grado = 0;
    private byte posgrado = 0;
    private byte curso = 0;
    private byte secundario = 0;
    private byte titulo = 0;

}
