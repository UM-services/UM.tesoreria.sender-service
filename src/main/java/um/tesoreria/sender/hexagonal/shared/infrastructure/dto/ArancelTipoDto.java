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
public class ArancelTipoDto {

    private Integer arancelTipoId;
    private String descripcion = "";
    private byte medioArancel = 0;
    private Integer arancelTipoIdCompleto;

}
