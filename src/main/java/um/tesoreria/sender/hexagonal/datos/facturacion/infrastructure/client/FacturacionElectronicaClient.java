package um.tesoreria.sender.hexagonal.datos.facturacion.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.FacturacionElectronicaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "facturacionElectronicaClient",
        path = "/api/tesoreria/core/facturacionElectronica")
public interface FacturacionElectronicaClient {

    @GetMapping("/{facturacionElectronicaId}")
    FacturacionElectronicaDto findByFacturacionElectronicaId(@PathVariable Long facturacionElectronicaId);

    @GetMapping("/nextPendiente")
    FacturacionElectronicaDto findNextPendiente();

    @PutMapping("/{facturacionElectronicaId}")
    FacturacionElectronicaDto update(@RequestBody FacturacionElectronicaDto facturacionElectronicaDto,
                                     @PathVariable Long facturacionElectronicaId);

}
