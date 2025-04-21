package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraPagoDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequeraPago")
public interface ChequeraPagoClient {

    @GetMapping("/{chequeraPagoId}")
    ChequeraPagoDto findByChequeraPagoId(@PathVariable Long chequeraPagoId);

}
