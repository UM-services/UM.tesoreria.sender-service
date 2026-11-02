package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.adapter;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.LegajoRepository;
import um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.LegajoClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.PersonaDtoMapper;

@Component
@RequiredArgsConstructor
public class LegajoClientAdapter implements LegajoRepository {

    private final LegajoClient legajoClient;
    private final PersonaDtoMapper personaDtoMapper;

    @Override
    public Optional<Legajo> findByFacultadIdAndPersonaIdAndDocumentoId(Integer facultadId, BigDecimal personaId,
                                                                       Integer documentoId) {
        try {
            return Optional.ofNullable(personaDtoMapper.toDomain(
                    legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(facultadId, personaId, documentoId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
