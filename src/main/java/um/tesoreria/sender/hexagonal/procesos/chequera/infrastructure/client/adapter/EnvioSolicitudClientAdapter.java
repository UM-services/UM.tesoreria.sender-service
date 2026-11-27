package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.client.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.EnvioSolicitudClaim;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.EnvioSolicitudResultado;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.EnvioSolicitudGateway;
import um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.client.EnvioSolicitudClient;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudFalloDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudOmisionDto;
import um.tesoreria.sender.hexagonal.shared.infrastructure.dto.EnvioSolicitudRequestDto;

@Component
@RequiredArgsConstructor
public class EnvioSolicitudClientAdapter implements EnvioSolicitudGateway {

    private final EnvioSolicitudClient client;

    @Override
    public String crear(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        var response = client.crear(new EnvioSolicitudRequestDto(facultadId, tipoChequeraId,
                chequeraSerieId, null, "SHIM"));
        return response.getSolicitudId();
    }

    @Override
    public EnvioSolicitudClaim reclamar(String solicitudId) {
        var response = client.reclamar(solicitudId);
        return new EnvioSolicitudClaim(
                response.getSolicitudId(),
                Boolean.TRUE.equals(response.getPuedeEnviar()),
                response.getEstado(),
                response.getIntentos() == null ? 0 : response.getIntentos(),
                response.getMotivo());
    }

    @Override
    public void marcarEnviado(String solicitudId) {
        client.marcarEnviado(solicitudId);
    }

    @Override
    public EnvioSolicitudResultado marcarFallido(String solicitudId, String error) {
        var response = client.marcarFallido(solicitudId, new EnvioSolicitudFalloDto(error));
        return new EnvioSolicitudResultado(response.getEstado(),
                response.getIntentos() == null ? 0 : response.getIntentos());
    }

    @Override
    public void marcarOmitida(String solicitudId, String estado, String mensaje) {
        client.marcarOmitida(solicitudId, new EnvioSolicitudOmisionDto(estado, mensaje));
    }

}
