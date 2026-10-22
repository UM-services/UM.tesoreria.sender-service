package um.tesoreria.sender.hexagonal.datos.catalogo.infrastructure.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.LectivoAlternativaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "lectivoAlternativaClient",
        path = "/api/tesoreria/core/lectivoAlternativa")
public interface LectivoAlternativaClient {

    @GetMapping("/tipo/{facultadId}/{lectivoId}/{tipoChequeraId}/{alternativaId}")
    List<LectivoAlternativaDto> findAllByTipo(@PathVariable Integer facultadId,
                                              @PathVariable Integer lectivoId,
                                              @PathVariable Integer tipoChequeraId,
                                              @PathVariable Integer alternativaId);

    @GetMapping("/unique/{facultadId}/{lectivoId}/{tipoChequeraId}/{productoId}/{alternativaId}")
    LectivoAlternativaDto findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
            @PathVariable Integer facultadId,
            @PathVariable Integer lectivoId,
            @PathVariable Integer tipoChequeraId,
            @PathVariable Integer productoId,
            @PathVariable Integer alternativaId);

}
