package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraSerieDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraSerieClient",
        path = "/api/tesoreria/core/chequeraSerie")
public interface ChequeraSerieClient {

    @GetMapping("/unique/{facultadId}/{tipochequeraId}/{chequeraserieId}")
    ChequeraSerieDto findByUnique(@PathVariable Integer facultadId,
                                  @PathVariable Integer tipochequeraId,
                                  @PathVariable Long chequeraserieId);

    @PostMapping("/mark/sent/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    ChequeraSerieDto markSent(@PathVariable Integer facultadId,
                              @PathVariable Integer tipoChequeraId,
                              @PathVariable Long chequeraSerieId);

}
