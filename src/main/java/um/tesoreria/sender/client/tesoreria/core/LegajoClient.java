package um.tesoreria.sender.client.tesoreria.core;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.LegajoDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/legajo")
public interface LegajoClient {

    @GetMapping("/unique/{facultadId}/{personaId}/{documentoId}")
    LegajoDto findByFacultadIdAndPersonaIdAndDocumentoId(
            @PathVariable("facultadId") Integer facultadId,
            @PathVariable("personaId") BigDecimal personaId,
            @PathVariable("documentoId") Integer documentoId);

    @PostMapping("/")
    LegajoDto add(@RequestBody LegajoDto legajoDto);

    @PostMapping("/saveAll")
    List<LegajoDto> saveAll(@RequestBody List<LegajoDto> legajoDtos);
}
