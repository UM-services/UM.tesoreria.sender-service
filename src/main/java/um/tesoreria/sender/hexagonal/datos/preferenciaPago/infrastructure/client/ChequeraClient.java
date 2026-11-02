package um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.UMPreferenceMPDto;

@FeignClient(name = "tesoreria-mercadopago-service", contextId = "chequeraPreferenciaClient",
        path = "/api/tesoreria/mercadopago/chequera")
public interface ChequeraClient {

    @GetMapping("/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<UMPreferenceMPDto> createChequeraContext(@PathVariable Integer facultadId,
                                                  @PathVariable Integer tipoChequeraId,
                                                  @PathVariable Long chequeraSerieId,
                                                  @PathVariable Integer alternativaId);

    @GetMapping("/create/context/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<UMPreferenceMPDto> createChequeraContextBulk(@PathVariable Integer facultadId,
                                                      @PathVariable Integer tipoChequeraId,
                                                      @PathVariable Long chequeraSerieId,
                                                      @PathVariable Integer alternativaId);

}
