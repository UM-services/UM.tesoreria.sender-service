package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.FacultadDto;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/facultad")
public interface FacultadClient {

    @GetMapping("/")
    List<FacultadDto> findAll();

    @GetMapping("/facultades")
    List<FacultadDto> findFacultades();

    @GetMapping("/{facultadId}")
    FacultadDto findByFacultadId(@PathVariable Integer facultadId);
}
