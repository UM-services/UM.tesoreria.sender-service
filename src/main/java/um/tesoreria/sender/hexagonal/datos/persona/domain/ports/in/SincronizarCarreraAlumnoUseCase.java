package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.in;

import java.math.BigDecimal;

public interface SincronizarCarreraAlumnoUseCase {

    void sincronizar(Integer facultadId, BigDecimal personaId, Integer documentoId);

}
