package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraPagoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraPagoClient",
        path = "/api/tesoreria/core/chequeraPago")
public interface ChequeraPagoClient {

    @GetMapping("/{chequeraPagoId}")
    ChequeraPagoDto findByChequeraPagoId(@PathVariable Long chequeraPagoId);

}
