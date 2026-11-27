package um.tesoreria.sender.hexagonal.procesos.chequera.domain.model;

/**
 * Resultado de registrar un fallo en el ledger.
 */
public record EnvioSolicitudResultado(String estado, int intentos) {
}
