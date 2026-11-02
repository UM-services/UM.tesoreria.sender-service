package um.tesoreria.sender.hexagonal.datos.persona.application.usecases;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.application.exception.PersonaDatosException;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.ObtenerLegajoUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.LegajoRepository;
import um.tesoreria.sender.hexagonal.shared.domain.model.Legajo;

@Component
@RequiredArgsConstructor
public class ObtenerLegajoUseCaseImpl implements ObtenerLegajoUseCase {

    private final LegajoRepository legajoRepository;

    @Override
    public Legajo obtener(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        return legajoRepository.findByFacultadIdAndPersonaIdAndDocumentoId(facultadId, personaId, documentoId)
                .orElseThrow(() -> new PersonaDatosException("No se encontró el legajo de " + personaId));
    }

}
