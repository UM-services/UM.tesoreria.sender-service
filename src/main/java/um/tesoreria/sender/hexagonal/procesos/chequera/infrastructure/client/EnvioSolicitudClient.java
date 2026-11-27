package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudClaimDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudFalloDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudOmisionDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudRequestDto;

@FeignClient(name = "tesoreria-core-service", contextId = "envioSolicitudClient",
        path = "/api/tesoreria/core/envio/chequera/solicitud")
public interface EnvioSolicitudClient {

    @PostMapping
    EnvioSolicitudDto crear(@RequestBody EnvioSolicitudRequestDto request);

    @PostMapping("/{solicitudId}/claim")
    EnvioSolicitudClaimDto reclamar(@PathVariable String solicitudId);

    @PostMapping("/{solicitudId}/sent")
    void marcarEnviado(@PathVariable String solicitudId);

    @PostMapping("/{solicitudId}/failed")
    EnvioSolicitudDto marcarFallido(@PathVariable String solicitudId,
                                    @RequestBody EnvioSolicitudFalloDto request);

    @PostMapping("/{solicitudId}/omitida")
    void marcarOmitida(@PathVariable String solicitudId,
                       @RequestBody EnvioSolicitudOmisionDto request);

}
