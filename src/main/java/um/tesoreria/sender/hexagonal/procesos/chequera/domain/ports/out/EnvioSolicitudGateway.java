package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out;

import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.EnvioSolicitudClaim;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.EnvioSolicitudResultado;

/**
 * Puerta hacia el ledger de solicitudes de core.
 *
 * <p>El sender no manda SMTP sin reclamar la solicitud: si ya está enviada, en curso o sin
 * intentos, el evento es un no-op.</p>
 */
public interface EnvioSolicitudGateway {

    /** Crea o recupera la solicitud para una terna facultad/tipo/serie. Devuelve su id. */
    String crear(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

    EnvioSolicitudClaim reclamar(String solicitudId);

    void marcarEnviado(String solicitudId);

    EnvioSolicitudResultado marcarFallido(String solicitudId, String error);

    void marcarOmitida(String solicitudId, String estado, String mensaje);

}
