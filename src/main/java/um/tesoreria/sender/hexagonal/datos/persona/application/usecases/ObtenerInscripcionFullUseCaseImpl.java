package um.tesoreria.sender.hexagonal.datos.persona.application.usecases;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.application.exception.PersonaDatosException;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerInscripcionFullUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.PersonaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;

@Component
@RequiredArgsConstructor
public class ObtenerInscripcionFullUseCaseImpl implements ObtenerInscripcionFullUseCase {

    private final PersonaRepository personaRepository;

    @Override
    public InscripcionFull obtener(Integer facultadId, BigDecimal personaId, Integer documentoId, Integer lectivoId) {
        return personaRepository.findInscripcionFull(facultadId, personaId, documentoId, lectivoId)
                .orElseThrow(() -> new PersonaDatosException("No se encontró la inscripción de " + personaId));
    }

}
