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
public class ChequeraCuotaReemplazo {

    private Long chequeraCuotaReemplazoId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;

    @Builder.Default
    private Integer mes = 0;

    @Builder.Default
    private Integer anho = 0;

    private OffsetDateTime vencimiento1;

    @Builder.Default
    private BigDecimal importe1 = BigDecimal.ZERO;

    private OffsetDateTime vencimiento2;

    @Builder.Default
    private BigDecimal importe2 = BigDecimal.ZERO;

    private OffsetDateTime vencimiento3;

    @Builder.Default
    private BigDecimal importe3 = BigDecimal.ZERO;

    @Builder.Default
    private String codigoBarras = "";

    @Builder.Default
    private Byte pagado = 0;

    @Builder.Default
    private Byte baja = 0;

    @Builder.Default
    private Byte compensada = 0;

    public boolean pendienteDePago() {
        return pagado == 0 && baja == 0 && compensada == 0 && importe1.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean conImporte() {
        return importe1.compareTo(BigDecimal.ZERO) != 0;
    }

}
