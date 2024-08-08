package um.tesoreria.sender.client.tesoreria.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.TipoChequeraDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/tipoChequera")
public interface TipoChequeraClient {

    @GetMapping("/")
    List<TipoChequeraDto> findAll();

    @GetMapping("/asignable/{facultadId}/{lectivoId}/{geograficaId}/{claseChequeraId}")
    List<TipoChequeraDto> findAllAsignable(@PathVariable Integer facultadId,
                                           @PathVariable Integer lectivoId,
                                           @PathVariable Integer geograficaId,
                                           @PathVariable Integer claseChequeraId);

    @GetMapping("/facultad/{facultadId}/geografica/{geograficaId}")
    List<TipoChequeraDto> findAllByFacultadIdAndGeograficaId(@PathVariable Integer facultadId,
                                                             @PathVariable Integer geograficaId);

    @GetMapping("/{tipoChequeraId}")
    TipoChequeraDto findByTipoChequeraId(@PathVariable Integer tipoChequeraId);

    @GetMapping("/last")
    TipoChequeraDto findLast();

    @PostMapping("/")
    TipoChequeraDto add(@RequestBody TipoChequeraDto tipochequera);

    @PutMapping("/{tipochequeraId}")
    TipoChequeraDto update(@RequestBody TipoChequeraDto tipochequera,
                           @PathVariable Integer tipochequeraId);

    @DeleteMapping("/{tipochequeraId}")
    void delete(@PathVariable Integer tipochequeraId);

    @GetMapping("/unmark")
    void unmark();

    @GetMapping("/mark/{tipochequeraId}/{imprimir}")
    void mark(@PathVariable Integer tipochequeraId, @PathVariable Byte imprimir);

}
