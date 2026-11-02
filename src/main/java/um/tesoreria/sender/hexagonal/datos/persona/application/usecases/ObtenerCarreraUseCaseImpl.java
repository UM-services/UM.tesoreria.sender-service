package um.tesoreria.sender.hexagonal.datos.persona.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.application.exception.PersonaDatosException;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerCarreraUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.CarreraRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;

@Component
@RequiredArgsConstructor
public class ObtenerCarreraUseCaseImpl implements ObtenerCarreraUseCase {

    private final CarreraRepository carreraRepository;

    @Override
    public Carrera obtener(Integer facultadId, Integer planId, Integer carreraId) {
        return carreraRepository.findByFacultadIdAndPlanIdAndCarreraId(facultadId, planId, carreraId)
                .orElseThrow(() -> new PersonaDatosException("No se encontró la carrera " + carreraId));
    }

}
