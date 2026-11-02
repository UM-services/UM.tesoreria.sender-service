package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in;

import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoPayload;

public interface EnviarNotificacionUseCase {

    void enviar(CorreoPayload payload);

}
