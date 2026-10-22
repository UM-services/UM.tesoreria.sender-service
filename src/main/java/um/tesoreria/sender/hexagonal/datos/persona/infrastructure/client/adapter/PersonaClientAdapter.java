package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.adapter;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.PersonaRepository;
import um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.PersonaClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.PersonaDtoMapper;

@Component
@RequiredArgsConstructor
public class PersonaClientAdapter implements PersonaRepository {

    private final PersonaClient personaClient;
    private final PersonaDtoMapper personaDtoMapper;

    @Override
    public Optional<Persona> findByUnique(BigDecimal personaId, Integer documentoId) {
        try {
            return Optional.ofNullable(personaDtoMapper.toDomain(
                    personaClient.findByUnique(personaId, documentoId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<InscripcionFull> findInscripcionFull(Integer facultadId, BigDecimal personaId,
                                                         Integer documentoId, Integer lectivoId) {
        try {
            return Optional.ofNullable(personaDtoMapper.toDomain(
                    personaClient.findInscripcionFull(facultadId, personaId, documentoId, lectivoId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
