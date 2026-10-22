package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ComprobanteDto;

@FeignClient(name = "tesoreria-core-service", contextId = "comprobanteClient",
        path = "/api/tesoreria/core/comprobante")
public interface ComprobanteClient {

    @GetMapping("/{comprobanteId}")
    ComprobanteDto findByComprobanteId(@PathVariable Integer comprobanteId);

}
