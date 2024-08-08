package um.tesoreria.sender.client.tesoreria.core;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.LectivoAlternativaDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/lectivoAlternativa")
public interface LectivoAlternativaClient {

    @GetMapping("/tipo/{facultadId}/{lectivoId}/{tipoChequeraId}/{alternativaId}")
    List<LectivoAlternativaDto> findAllByTipo(
            @PathVariable("facultadId") Integer facultadId,
            @PathVariable("lectivoId") Integer lectivoId,
            @PathVariable("tipoChequeraId") Integer tipoChequeraId,
            @PathVariable("alternativaId") Integer alternativaId);

    @GetMapping("/unique/{facultadId}/{lectivoId}/{tipoChequeraId}/{productoId}/{alternativaId}")
    LectivoAlternativaDto findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
            @PathVariable("facultadId") Integer facultadId,
            @PathVariable("lectivoId") Integer lectivoId,
            @PathVariable("tipoChequeraId") Integer tipoChequeraId,
            @PathVariable("productoId") Integer productoId,
            @PathVariable("alternativaId") Integer alternativaId);
}
