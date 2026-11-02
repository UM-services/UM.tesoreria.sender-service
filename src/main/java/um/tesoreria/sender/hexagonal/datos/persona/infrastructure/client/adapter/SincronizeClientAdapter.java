package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.adapter;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out.CarreraAlumnoSincronizacion;
import um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client.SincronizeClient;

@Component
@RequiredArgsConstructor
public class SincronizeClientAdapter implements CarreraAlumnoSincronizacion {

    private final SincronizeClient sincronizeClient;

    @Override
    public void sincronizar(Integer facultadId, BigDecimal personaId, Integer documentoId) {
        sincronizeClient.sincronizeCarreraAlumno(facultadId, personaId, documentoId);
    }

}
