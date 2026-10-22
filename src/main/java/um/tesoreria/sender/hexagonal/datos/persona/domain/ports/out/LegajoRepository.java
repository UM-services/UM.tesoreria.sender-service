package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out;

import java.math.BigDecimal;
import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;

public interface LegajoRepository {

    Optional<Legajo> findByFacultadIdAndPersonaIdAndDocumentoId(Integer facultadId, BigDecimal personaId,
                                                                Integer documentoId);

}
