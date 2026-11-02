package um.tesoreria.sender.hexagonal.datos.preferenciaPago.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.MercadoPagoContextDto;

@FeignClient(name = "tesoreria-core-service", contextId = "mercadoPagoContextClient",
        path = "/api/tesoreria/core/mercadoPagoContext")
public interface MercadoPagoContextClient {

    @GetMapping("/cuota/activo/{chequeraCuotaId}")
    MercadoPagoContextDto findActivoByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

}
