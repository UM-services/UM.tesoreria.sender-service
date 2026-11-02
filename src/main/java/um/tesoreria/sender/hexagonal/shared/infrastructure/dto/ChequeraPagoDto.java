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
public class ChequeraPagoDto implements Jsonifyable {

    private Long chequeraPagoId;
    private Long chequeraCuotaId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    private Integer orden;
    private int mes = 0;
    private int anho = 0;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime acreditacion;
    private BigDecimal importe = BigDecimal.ZERO;
    private String path = "";
    private String archivo = "";
    private String observaciones = "";
    private Long archivoBancoId;
    private Long archivoBancoIdAcreditacion;
    private int verificador = 0;
    private Integer tipoPagoId;
    private TipoPagoDto tipoPago;
    private ProductoDto producto;
    private ChequeraCuotaDto chequeraCuota;

}
