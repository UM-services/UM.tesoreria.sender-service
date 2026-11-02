package um.tesoreria.sender.hexagonal.datos.persona.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.CarreraDto;

@FeignClient(name = "tesoreria-core-service", contextId = "carreraClient",
        path = "/api/tesoreria/core/carrera")
public interface CarreraClient {

    @GetMapping("/unique/{facultadId}/{planId}/{carreraId}")
    CarreraDto findByFacultadIdAndPlanIdAndCarreraId(@PathVariable Integer facultadId,
                                                     @PathVariable Integer planId,
                                                     @PathVariable Integer carreraId);

}
