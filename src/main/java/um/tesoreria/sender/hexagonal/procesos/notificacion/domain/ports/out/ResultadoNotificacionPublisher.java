package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.out;

import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;

/**
 * Publica el resultado del envío para que el productor pueda actualizar su estado de negocio
 * (por ejemplo, {@code persona_suspendido.notificado}) sin acoplar el envío a un callback HTTP.
 */
public interface ResultadoNotificacionPublisher {

    void publicar(CorreoEvent event, boolean enviado, String error);

}
