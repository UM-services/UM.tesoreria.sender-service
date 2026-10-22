package um.tesoreria.sender.hexagonal.datos.persona.domain.ports.out;

import java.math.BigDecimal;

public interface CarreraAlumnoSincronizacion {

    void sincronizar(Integer facultadId, BigDecimal personaId, Integer documentoId);

}
