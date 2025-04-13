package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.FacturacionElectronicaDto;

import java.time.OffsetDateTime;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/facturacionElectronica")
public interface FacturacionElectronicaClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    List<FacturacionElectronicaDto> findAllByChequera(@PathVariable Integer facultadId,
                                                      @PathVariable Integer tipoChequeraId,
                                                      @PathVariable Long chequeraSerieId);

    @GetMapping("/{facturacionElectronicaId}")
    FacturacionElectronicaDto findByFacturacionElectronicaId(@PathVariable Long facturacionElectronicaId);

    @GetMapping("/periodo")
    List<FacturacionElectronicaDto> findAllByPeriodo(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta);

    @GetMapping("/nextPendiente")
    FacturacionElectronicaDto findNextPendiente();

    @PostMapping("/")
    FacturacionElectronicaDto add(@RequestBody FacturacionElectronicaDto facturacionElectronicaDto);

    @PutMapping("/{facturacionElectronicaId}")
    FacturacionElectronicaDto update(@RequestBody FacturacionElectronicaDto facturacionElectronicaDto,
                                     @PathVariable Long facturacionElectronicaId);

    @GetMapping("/pendientes")
    List<FacturacionElectronicaDto> find100Pendientes();

}
