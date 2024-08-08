package um.tesoreria.sender.client.tesoreria.core.facade;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/sincronize")
public interface SincronizeClient {

    @GetMapping("/matricula/{lectivoId}/{facultadId}")
    void sincronizeMatricula(
            @PathVariable("lectivoId") Integer lectivoId,
            @PathVariable("facultadId") Integer facultadId) throws CloneNotSupportedException;

    @GetMapping("/institucional/{lectivoId}/{facultadId}")
    void sincronizeInstitucional(
            @PathVariable("lectivoId") Integer lectivoId,
            @PathVariable("facultadId") Integer facultadId);

    @GetMapping("/carreraalumno/{facultadId}/{personaId}/{documentoId}")
    void sincronizeCarreraAlumno(
            @PathVariable("facultadId") Integer facultadId,
            @PathVariable("personaId") BigDecimal personaId,
            @PathVariable("documentoId") Integer documentoId);

    @GetMapping("/carrera/{facultadId}")
    void sincronizeCarrera(@PathVariable("facultadId") Integer facultadId);
}
