package um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.in;

import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;

public interface EnviarChequeraUseCase {

    String enviar(SolicitudEnvioChequera solicitud);

}
