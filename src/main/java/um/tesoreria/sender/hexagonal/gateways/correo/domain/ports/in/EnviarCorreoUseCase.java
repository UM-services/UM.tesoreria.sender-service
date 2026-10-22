package um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.in;

import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;

public interface EnviarCorreoUseCase {

    void enviar(Correo correo);

}
