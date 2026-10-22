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
public class ChequeraFacturacionElectronicaDto implements Jsonifyable {

    private Long chequeraFacturacionElectronicaId;
    private Long chequeraId;
    private String cuit = "";
    private String razonSocial = "";
    private String domicilio = "";
    private String email = "";
    private String condicionIva = "";

}
