package um.tesoreria.sender.domain.dto.tesoreria.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraPagoDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ComprobanteDto;
import um.tesoreria.sender.util.Jsonifier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturacionElectronicaDto {

    private Long facturacionElectronicaId;
    private Long chequeraPagoId;
    private Integer comprobanteId;
    private Long numeroComprobante;
    private BigDecimal personaId;
    private String tipoDocumento;
    private String apellido;
    private String nombre;
    private String cuit;
    private String condicionIva;
    private BigDecimal importe;
    private String cae;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime fechaRecibo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime fechaVencimientoCae;
    private Byte enviada;
    private Integer retries;
    private ChequeraPagoDto chequeraPago;
    private ComprobanteDto comprobante;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
