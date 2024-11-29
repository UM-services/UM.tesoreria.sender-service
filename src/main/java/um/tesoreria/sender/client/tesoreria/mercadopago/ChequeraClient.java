package um.tesoreria.sender.client.tesoreria.mercadopago;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.domain.dto.UMPreferenceMPDto;

import java.util.List;

@FeignClient(name = "tesoreria-mercadopago-service/api/tesoreria/mercadopago/chequera")
public interface ChequeraClient {

    @GetMapping("/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<UMPreferenceMPDto> createChequeraContext(
        @PathVariable Integer facultadId,
        @PathVariable Integer tipoChequeraId,
        @PathVariable Long chequeraSerieId,
        @PathVariable Integer alternativaId
    );

}
