package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.CarreraDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "carreraClient", path = "/api/tesoreria/core/carrera")
public interface CarreraClient {

    @GetMapping("/")
    List<CarreraDto> findAll();

    @GetMapping("/unique/{facultadId}/{planId}/{carreraId}")
    CarreraDto findByFacultadIdAndPlanIdAndCarreraId(@PathVariable Integer facultadId, @PathVariable Integer planId, @PathVariable Integer carreraId);

}
