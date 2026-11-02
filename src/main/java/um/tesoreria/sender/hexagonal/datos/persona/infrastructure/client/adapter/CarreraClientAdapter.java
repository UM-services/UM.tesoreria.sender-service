package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.adapter;

import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.CarreraRepository;
import um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.CarreraClient;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.infrastructure.mapper.PersonaDtoMapper;

@Component
@RequiredArgsConstructor
public class CarreraClientAdapter implements CarreraRepository {

    private final CarreraClient carreraClient;
    private final PersonaDtoMapper personaDtoMapper;

    @Override
    public Optional<Carrera> findByFacultadIdAndPlanIdAndCarreraId(Integer facultadId, Integer planId,
                                                                   Integer carreraId) {
        try {
            return Optional.ofNullable(personaDtoMapper.toDomain(
                    carreraClient.findByFacultadIdAndPlanIdAndCarreraId(facultadId, planId, carreraId)));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

}
