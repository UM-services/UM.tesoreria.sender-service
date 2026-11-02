package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
public class ChequeraCuotaReemplazoDto implements Jsonifyable {

    private Long chequeraCuotaReemplazoId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private int mes = 0;
    private int anho = 0;
    private Integer arancelTipoId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento1;
    private BigDecimal importe1 = BigDecimal.ZERO;
    private BigDecimal importe1Original = BigDecimal.ZERO;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento2;
    private BigDecimal importe2 = BigDecimal.ZERO;
    private BigDecimal importe2Original = BigDecimal.ZERO;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento3;
    private BigDecimal importe3 = BigDecimal.ZERO;
    private BigDecimal importe3Original = BigDecimal.ZERO;
    private String codigoBarras = "";
    private String i2Of5 = "";
    private byte pagado = 0;
    private byte baja = 0;
    private byte manual = 0;
    private Integer tramoId;
    private byte compensada = 0;
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private ProductoDto producto;
    private ChequeraSerieDto chequeraSerie;

}
