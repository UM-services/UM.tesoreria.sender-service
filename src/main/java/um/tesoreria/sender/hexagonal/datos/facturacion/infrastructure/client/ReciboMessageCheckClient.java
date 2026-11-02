package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ReciboMessageCheckDto;

@FeignClient(name = "tesoreria-core-service", contextId = "reciboMessageCheckClient",
        path = "/api/tesoreria/core/reciboMessageCheck")
public interface ReciboMessageCheckClient {

    @PostMapping("/")
    ReciboMessageCheckDto add(@RequestBody ReciboMessageCheckDto reciboMessageCheck);

}
