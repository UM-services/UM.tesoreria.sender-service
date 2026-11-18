package um.tesoreria.sender.hexagonal.datos.borrador.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.BorradorImpresionDto;

@FeignClient(name = "tesoreria-core-service", contextId = "borradorChequeraClient",
        path = "/api/tesoreria/core/emision/chequera/borradores")
public interface BorradorChequeraClient {

    @GetMapping("/{borradorId}/impresion")
    BorradorImpresionDto impresion(@PathVariable Long borradorId);
}
