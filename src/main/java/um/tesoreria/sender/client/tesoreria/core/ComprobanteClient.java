package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ComprobanteDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/comprobante")
public interface ComprobanteClient {

    @GetMapping("/{comprobanteId}")
    ComprobanteDto findByComprobanteId(@PathVariable Integer comprobanteId);

}
