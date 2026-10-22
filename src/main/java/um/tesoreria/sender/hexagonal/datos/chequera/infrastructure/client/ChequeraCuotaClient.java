package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraCuotaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraCuotaClient",
        path = "/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/{chequeraCuotaId}")
    ChequeraCuotaDto findByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

    @GetMapping("/unique/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{productoId}/{alternativaId}/{cuotaId}")
    ChequeraCuotaDto findByUnique(@PathVariable Integer facultadId,
                                  @PathVariable Integer tipoChequeraId,
                                  @PathVariable Long chequeraSerieId,
                                  @PathVariable Integer productoId,
                                  @PathVariable Integer alternativaId,
                                  @PathVariable Integer cuotaId);

    @GetMapping("/updateBarras/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    List<ChequeraCuotaDto> updateBarras(@PathVariable Integer facultadId,
                                        @PathVariable Integer tipoChequeraId,
                                        @PathVariable Long chequeraSerieId);

}
