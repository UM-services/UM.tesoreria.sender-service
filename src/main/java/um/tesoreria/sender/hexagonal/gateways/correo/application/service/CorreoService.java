package um.tesoreria.sender.hexagonal.gateways.correo.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.in.EnviarCorreoUseCase;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final EnviarCorreoUseCase enviarCorreoUseCase;

    public void enviar(Correo correo) {
        enviarCorreoUseCase.enviar(correo);
    }

}
