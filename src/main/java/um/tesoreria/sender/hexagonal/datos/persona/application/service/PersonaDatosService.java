package um.tesoreria.sender.hexagonal.datos.persona.application.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerCarreraUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerInscripcionFullUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerLegajoUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerPersonaUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.SincronizarCarreraAlumnoUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

@Service
@RequiredArgsConstructor
public class PersonaDatosService {

    private final ObtenerPersonaUseCase obtenerPersonaUseCase;
    private final ObtenerInscripcionFullUseCase obtenerInscripcionFullUseCase;
    private final ObtenerLegajoUseCase obtenerLegajoUseCase;
    private final ObtenerCarreraUseCase obtenerCarreraUseCase;
    private final SincronizarCarreraAlumnoUseCase sincronizarCarreraAlumnoUseCase;

    public Persona obtenerPersona(BigDecimal personaId, Integer documentoId) {
        return obtenerPersonaUseCase.obtener(personaId, documentoId);
    }

    public InscripcionFull obtenerInscripcionFull(Integer facultadId, BigDecimal personaId, Integer documentoId,
                                                  Integer lectivoId) {
        return obtenerInscripcionFullUseCase.obtener(facultadId, personaId, documentoId, lectivoId);
    }

    public Legajo obtenerLegajo(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        return obtenerLegajoUseCase.obtener(facultadId, personaId, documentoId);
    }

    public Carrera obtenerCarrera(Integer facultadId, Integer planId, Integer carreraId) {
        return obtenerCarreraUseCase.obtener(facultadId, planId, carreraId);
    }

    public void sincronizarCarreraAlumno(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        sincronizarCarreraAlumnoUseCase.sincronizar(facultadId, personaId, documentoId);
    }

}
