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
public class ChequeraPago {

    private Long chequeraPagoId;
    private Long chequeraCuotaId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private int mes;
    private int anho;
    private OffsetDateTime fecha;
    private OffsetDateTime acreditacion;

    @Builder.Default
    private BigDecimal importe = BigDecimal.ZERO;

    private String observaciones;

}
