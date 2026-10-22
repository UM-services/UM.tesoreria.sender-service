package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in;

import java.math.BigDecimal;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;

public interface ObtenerLegajoUseCase {

    Legajo obtener(Integer facultadId, BigDecimal personaId, Integer documentoId);

}
