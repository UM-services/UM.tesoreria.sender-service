package um.tesoreria.sender.hexagonal.shared.infrastructure.dto;

import lombok.*;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.InscripcionDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.InscripcionPagoDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.DomicilioDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.PersonaDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.support.Jsonifier;

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
