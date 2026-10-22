package um.tesoreria.sender.hexagonal.shared.domain.model;

import java.util.UUID;
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
public class ReciboMessageCheck {

    private UUID reciboMessageCheckId;
    private Long facturacionElectronicaId;
    private Long chequeraPagoId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;

    @Builder.Default
    private String payload = "";

}
