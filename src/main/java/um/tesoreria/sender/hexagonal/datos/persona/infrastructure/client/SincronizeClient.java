package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tesoreria-core-service", contextId = "sincronizeClient",
        path = "/api/tesoreria/core/sincronize")
public interface SincronizeClient {

    @GetMapping("/carreraalumno/{facultadId}/{personaId}/{documentoId}")
    void sincronizeCarreraAlumno(@PathVariable Integer facultadId,
                                 @PathVariable BigDecimal personaId,
                                 @PathVariable Integer documentoId);

}
