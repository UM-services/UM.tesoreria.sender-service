package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.LegajoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "legajoClient",
        path = "/api/tesoreria/core/legajo")
public interface LegajoClient {

    @GetMapping("/unique/{facultadId}/{personaId}/{documentoId}")
    LegajoDto findByFacultadIdAndPersonaIdAndDocumentoId(@PathVariable Integer facultadId,
                                                         @PathVariable BigDecimal personaId,
                                                         @PathVariable Integer documentoId);

}
