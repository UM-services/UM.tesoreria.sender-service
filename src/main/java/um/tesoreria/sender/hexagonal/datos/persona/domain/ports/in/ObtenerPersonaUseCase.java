package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in;

import java.math.BigDecimal;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

public interface ObtenerPersonaUseCase {

    Persona obtener(BigDecimal personaId, Integer documentoId);

}
