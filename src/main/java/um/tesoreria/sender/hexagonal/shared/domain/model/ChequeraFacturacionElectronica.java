package um.tesoreria.sender.hexagonal.shared.domain.model;

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
public class ChequeraFacturacionElectronica {

    private Long chequeraFacturacionElectronicaId;
    private Long chequeraId;
    private String cuit;
    private String razonSocial;
    private String domicilio;

    @Builder.Default
    private String email = "";

    private String condicionIva;

}
