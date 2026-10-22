package um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in;

import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoEvent;

public interface ProcesarCorreoEventUseCase {

    void procesar(CorreoEvent event);

}
