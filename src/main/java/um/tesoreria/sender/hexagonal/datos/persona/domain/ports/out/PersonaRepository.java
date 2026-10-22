package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out;

import java.math.BigDecimal;
import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

public interface PersonaRepository {

    Optional<Persona> findByUnique(BigDecimal personaId, Integer documentoId);

    Optional<InscripcionFull> findInscripcionFull(Integer facultadId, BigDecimal personaId, Integer documentoId,
                                                  Integer lectivoId);

}
