package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraMessageCheckDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraMessageCheckClient",
        path = "/api/tesoreria/core/chequeraMessageCheck")
public interface ChequeraMessageCheckClient {

    @PostMapping("/")
    ChequeraMessageCheckDto add(@RequestBody ChequeraMessageCheckDto chequeraMessageCheck);

}
