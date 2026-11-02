package um.tesoreria.sender.hexagonal.datos.persona.application.usecases;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.application.exception.PersonaDatosException;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerPersonaUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.PersonaRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

@Component
@RequiredArgsConstructor
public class ObtenerPersonaUseCaseImpl implements ObtenerPersonaUseCase {

    private final PersonaRepository personaRepository;

    @Override
    public Persona obtener(BigDecimal personaId, Integer documentoId) {
        return personaRepository.findByUnique(personaId, documentoId)
                .orElseThrow(() -> new PersonaDatosException("No se encontró la persona " + personaId));
    }

}
