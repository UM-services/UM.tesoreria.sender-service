package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model;

/**
 * Clave lógica del remitente. El sender decide la dirección real a partir de su configuración,
 * de modo que el productor del evento no puede falsificar el {@code From}.
 */
public enum Remitente {
    CHEQUERA, RECIBO, NOTIFICACION
}
