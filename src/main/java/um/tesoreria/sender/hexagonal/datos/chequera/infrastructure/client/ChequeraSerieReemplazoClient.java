package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraSerieReemplazoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraSerieReemplazoClient",
        path = "/api/tesoreria/core/chequeraSerieReemplazo")
public interface ChequeraSerieReemplazoClient {

    @GetMapping("/unique/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    ChequeraSerieReemplazoDto findByUnique(@PathVariable Integer facultadId,
                                           @PathVariable Integer tipoChequeraId,
                                           @PathVariable Long chequeraSerieId);

}
