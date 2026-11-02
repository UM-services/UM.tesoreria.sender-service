package um.tesoreria.sender.hexagonal.datos.persona.application.usecases;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in.SincronizarCarreraAlumnoUseCase;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.CarreraAlumnoSincronizacion;

@Component
@RequiredArgsConstructor
public class SincronizarCarreraAlumnoUseCaseImpl implements SincronizarCarreraAlumnoUseCase {

    private final CarreraAlumnoSincronizacion carreraAlumnoSincronizacion;

    @Override
    public void sincronizar(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        carreraAlumnoSincronizacion.sincronizar(facultadId, personaId, documentoId);
    }

}
