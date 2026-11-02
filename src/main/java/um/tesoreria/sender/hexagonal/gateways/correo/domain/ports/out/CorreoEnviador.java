package um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.out;

import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;

public interface CorreoEnviador {

    void enviar(Correo correo);

}
