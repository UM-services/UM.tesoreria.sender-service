package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

/**
 * Resultado del reclamo de una solicitud en el ledger de core.
 */
public record EnvioSolicitudClaim(String solicitudId,
                                  boolean puedeEnviar,
                                  String estado,
                                  int intentos,
                                  String motivo) {
}
