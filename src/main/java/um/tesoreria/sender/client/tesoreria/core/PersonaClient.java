package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.PersonaDto;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/persona")
public interface PersonaClient {

    @GetMapping("/santander")
    List<PersonaDto> findAllSantander();

    @GetMapping("/inscriptossinchequera/{facultadId}/{lectivoId}/{geograficaId}/{curso}")
    List<PersonaDto> findAllInscriptosSinChequera(@PathVariable Integer facultadId,
                                                  @PathVariable Integer lectivoId,
                                                  @PathVariable Integer geograficaId,
                                                  @PathVariable Integer curso);

    @GetMapping("/inscriptossinchequeradefault/{facultadId}/{lectivoId}/{geograficaId}/{claseChequeraId}/{curso}")
    List<PersonaDto> findAllInscriptosSinChequeraDefault(@PathVariable Integer facultadId,
                                                         @PathVariable Integer lectivoId,
                                                         @PathVariable Integer geograficaId,
                                                         @PathVariable Integer claseChequeraId,
                                                         @PathVariable Integer curso);

    @GetMapping("/preinscriptossinchequera/{facultadId}/{lectivoId}/{geograficaId}")
    List<PersonaDto> findAllPreInscriptosSinChequera(@PathVariable Integer facultadId,
                                                     @PathVariable Integer lectivoId,
                                                     @PathVariable Integer geograficaId);

    @GetMapping("/deudoreslectivo/{facultadId}/{lectivoId}/{geograficaId}/{cuotas}")
    List<PersonaDto> findAllDeudorByLectivoId(@PathVariable Integer facultadId,
                                              @PathVariable Integer lectivoId,
                                              @PathVariable Integer geograficaId,
                                              @PathVariable Integer cuotas);

    @PostMapping("/unifieds")
    List<PersonaDto> findByUnifieds(@RequestBody List<String> unifieds);

    @PostMapping("/search")
    List<PersonaDto> findByStrings(@RequestBody List<String> conditions);

    @GetMapping("/unique/{personaId}/{documentoId}")
    PersonaDto findByUnique(@PathVariable BigDecimal personaId, @PathVariable Integer documentoId);

    @GetMapping("/bypersonaId/{personaId}")
    PersonaDto findByPersonaId(@PathVariable BigDecimal personaId);

    @GetMapping("/{uniqueId}")
    PersonaDto findByUniqueId(@PathVariable Long uniqueId);

    @PostMapping("/")
    PersonaDto add(@RequestBody PersonaDto persona);

    @PutMapping("/{uniqueId}")
    PersonaDto update(@RequestBody PersonaDto persona, @PathVariable Long uniqueId);

}
