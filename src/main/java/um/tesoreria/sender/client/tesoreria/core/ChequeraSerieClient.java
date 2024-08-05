package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraSerieDto;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "tesoreria-core-service", path = "/api/tesoreria/core/chequeraSerie")
public interface ChequeraSerieClient {

    @GetMapping("/persona/{personaId}/{documentoId}")
    List<ChequeraSerieDto> findAllByPersona(@PathVariable BigDecimal personaId,
                                            @PathVariable Integer documentoId);

    @GetMapping("/personaextended/{personaId}/{documentoId}")
    List<ChequeraSerieDto> findAllByPersonaExtended(@PathVariable BigDecimal personaId,
                                                    @PathVariable Integer documentoId);

    @GetMapping("/facultad/{personaId}/{documentoId}/{facultadId}")
    List<ChequeraSerieDto> findAllByFacultad(@PathVariable BigDecimal personaId,
                                             @PathVariable Integer documentoId, @PathVariable Integer facultadId);

    @GetMapping("/facultadextended/{personaId}/{documentoId}/{facultadId}")
    List<ChequeraSerieDto> findAllByFacultadExtended(@PathVariable BigDecimal personaId,
                                                     @PathVariable Integer documentoId, @PathVariable Integer facultadId);

    @GetMapping("/personaLectivo/{personaId}/{documentoId}/{lectivoId}")
    List<ChequeraSerieDto> findAllByPersonaLectivo(@PathVariable BigDecimal personaId,
                                                   @PathVariable Integer documentoId, @PathVariable Integer lectivoId);

    @GetMapping("/bynumber/{facultadId}/{chequeraserieId}")
    List<ChequeraSerieDto> findAllByNumber(@PathVariable Integer facultadId,
                                           @PathVariable Long chequeraserieId);

    @PostMapping("/documentos/{facultadId}/{lectivoId}/{geograficaId}")
    List<ChequeraSerieDto> findAllByDocumentos(@PathVariable Integer facultadId,
                                               @PathVariable Integer lectivoId, @PathVariable Integer geograficaId,
                                               @RequestBody List<BigDecimal> personaIds);

    @GetMapping("/{chequeraId}")
    ChequeraSerieDto findByChequeraId(@PathVariable Long chequeraId);

    @GetMapping("/extended/{chequeraId}")
    ChequeraSerieDto findByChequeraIdExtended(@PathVariable Long chequeraId);

    @GetMapping("/unique/{facultadId}/{tipochequeraId}/{chequeraserieId}")
    ChequeraSerieDto findByUnique(@PathVariable Integer facultadId,
                                  @PathVariable Integer tipochequeraId, @PathVariable Long chequeraserieId);

    @GetMapping("/uniqueextended/{facultadId}/{tipochequeraId}/{chequeraserieId}")
    ChequeraSerieDto findByUniqueExtended(@PathVariable Integer facultadId,
                                          @PathVariable Integer tipochequeraId, @PathVariable Long chequeraserieId);

    @GetMapping("/setpaypertic/{facultadId}/{tipochequeraId}/{chequeraserieId}/{flag}")
    ChequeraSerieDto setPayPerTic(@PathVariable Integer facultadId,
                                  @PathVariable Integer tipochequeraId, @PathVariable Long chequeraserieId, @PathVariable Byte flag);

    @PutMapping("/{chequeraId}")
    ChequeraSerieDto update(@RequestBody ChequeraSerieDto chequeraserie,
                            @PathVariable Long chequeraId);
}
