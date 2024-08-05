package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraFacturacionElectronicaDto;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequeraFacturacionElectronica")
public interface ChequeraFacturacionElectronicaClient {

    @GetMapping("/chequera/{chequeraId}")
    ChequeraFacturacionElectronicaDto findByChequeraId(@PathVariable Long chequeraId);

    @PostMapping("/")
    ChequeraFacturacionElectronicaDto add(@RequestBody ChequeraFacturacionElectronicaDto chequeraFacturacionElectronicaDto);

    @PutMapping("/{chequeraFacturacionElectronicaId}")
    ChequeraFacturacionElectronicaDto update(@RequestBody ChequeraFacturacionElectronicaDto chequeraFacturacionElectronicaDto,
                                             @PathVariable Long chequeraFacturacionElectronicaId);

    @DeleteMapping("/{chequeraFacturacionElectronicaId}")
    void deleteByChequeraFacturacionElectronicaId(@PathVariable Long chequeraFacturacionElectronicaId);

}
