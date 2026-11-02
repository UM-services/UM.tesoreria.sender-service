package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in;

import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;

public interface ObtenerCarreraUseCase {

    Carrera obtener(Integer facultadId, Integer planId, Integer carreraId);

}
