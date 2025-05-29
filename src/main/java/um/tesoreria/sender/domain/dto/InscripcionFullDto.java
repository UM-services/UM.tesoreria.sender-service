package um.tesoreria.sender.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.sender.domain.dto.tesoreria.core.InscripcionDto;
import um.tesoreria.sender.domain.dto.tesoreria.core.InscripcionPagoDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.DomicilioDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.PersonaDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionFullDto {

    private InscripcionDto inscripcion;
    private InscripcionPagoDto inscripcionPago;
    private PersonaDto personaPago;
    private DomicilioDto domicilioPago;

}
