package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ComprobanteDto {

    private Integer comprobanteId;
    private String descripcion = "";
    private Integer tipoTransaccionId;
    private byte ordenPago = 0;
    private byte aplicaPendiente = 0;
    private byte cuentaCorriente = 0;
    private byte debita = 0;
    private long diasVigencia = 0;
    private byte facturacionElectronica = 0;
    private Integer comprobanteAfipId;
    private Integer puntoVenta;
    private String letraComprobante;
    private ComprobanteAfipDto comprobanteAfip;

}
