package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.LectivoDto;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/lectivo")
public interface LectivoClient {

    @GetMapping("/")
    List<LectivoDto> findAll();

    @GetMapping("/reverse")
    List<LectivoDto> findAllReverse();

    @GetMapping("/persona/{personaId}/{documentoId}")
    List<LectivoDto> findAllByPersona(@PathVariable BigDecimal personaId,
                                      @PathVariable Integer documentoId);

    @GetMapping("/{lectivoId}")
    LectivoDto findByLectivoId(@PathVariable Integer lectivoId);

    @GetMapping("/last")
    LectivoDto findLast();

    @PostMapping("/")
    LectivoDto add(@RequestBody LectivoDto lectivo);

    @DeleteMapping("/{lectivoId}")
    void deleteByLectivoId(@PathVariable Integer lectivoId);
}
