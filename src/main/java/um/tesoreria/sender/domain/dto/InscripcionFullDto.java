package um.tesoreria.sender.domain.dto;

import lombok.*;
import um.tesoreria.sender.domain.dto.tesoreria.core.InscripcionDto;
import um.tesoreria.sender.domain.dto.tesoreria.core.InscripcionPagoDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.DomicilioDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.PersonaDto;
import um.tesoreria.sender.util.Jsonifier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionFullDto {

    private InscripcionDto inscripcion;
    private InscripcionPagoDto inscripcionPago;
    private PersonaDto personaPago;
    private DomicilioDto domicilioPago;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
