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
public class FacturacionElectronica {

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
    private OffsetDateTime fechaRecibo;
    private OffsetDateTime fechaVencimientoCae;

    @Builder.Default
    private Byte enviada = 0;

    @Builder.Default
    private Integer retries = 0;

    private ChequeraPago chequeraPago;
    private Comprobante comprobante;

    /**
     * DTO original recibido de core. Se conserva para reconstruir el PUT sin perder
     * campos que el negocio no consume; el adaptador lo usa como base del update.
     */
    private Object original;

    public void marcarEnviada() {
        this.enviada = 1;
    }

    public void incrementarReintentos() {
        this.retries = reintentosActuales() + 1;
    }

    public int reintentosActuales() {
        return retries == null ? 0 : retries;
    }

}
