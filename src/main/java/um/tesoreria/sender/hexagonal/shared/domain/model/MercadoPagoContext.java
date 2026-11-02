package um.tesoreria.sender.hexagonal.shared.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
public class MercadoPagoContext {

    private Long mercadoPagoContextId;
    private Long chequeraCuotaId;
    private String initPoint;
    private OffsetDateTime fechaVencimiento;

    @Builder.Default
    private BigDecimal importe = BigDecimal.ZERO;

    @Builder.Default
    private Byte activo = 0;

    private Long chequeraPagoId;
    private String preferenceId;

}
