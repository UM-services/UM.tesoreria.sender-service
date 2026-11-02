package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out;

import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;

/**
 * Solicita el envío de una chequera sin ejecutarlo en el hilo HTTP: el endpoint REST actúa como
 * shim y publica el mismo evento que consume el listener (una sola ruta de composición).
 */
public interface SolicitudChequeraPublisher {

    void publicar(SolicitudEnvioChequera solicitud);

}
