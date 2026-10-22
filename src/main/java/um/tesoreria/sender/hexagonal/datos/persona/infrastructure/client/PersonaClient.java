package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.InscripcionFullDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.PersonaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "personaClient",
        path = "/api/tesoreria/core/persona")
public interface PersonaClient {

    @GetMapping("/unique/{personaId}/{documentoId}")
    PersonaDto findByUnique(@PathVariable BigDecimal personaId, @PathVariable Integer documentoId);

    @GetMapping("/inscripcion/full/{facultadId}/{personaId}/{documentoId}/{lectivoId}")
    InscripcionFullDto findInscripcionFull(@PathVariable Integer facultadId, @PathVariable BigDecimal personaId,
                                           @PathVariable Integer documentoId, @PathVariable Integer lectivoId);

}
