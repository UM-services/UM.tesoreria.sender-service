package um.tesoreria.sender.hexagonal.datos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.ChequeraFacturacionElectronicaDto;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraFacturacionElectronicaClient",
        path = "/api/tesoreria/core/chequeraFacturacionElectronica")
public interface ChequeraFacturacionElectronicaClient {

    @GetMapping("/chequera/{chequeraId}")
    ChequeraFacturacionElectronicaDto findByChequeraId(@PathVariable Long chequeraId);

}
