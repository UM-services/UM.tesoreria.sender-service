package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out;

import java.util.Optional;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;

public interface CarreraRepository {

    Optional<Carrera> findByFacultadIdAndPlanIdAndCarreraId(Integer facultadId, Integer planId, Integer carreraId);

}
