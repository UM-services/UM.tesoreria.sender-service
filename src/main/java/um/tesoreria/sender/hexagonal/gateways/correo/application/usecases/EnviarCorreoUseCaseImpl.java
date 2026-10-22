package um.tesoreria.sender.hexagonal.gateways.correo.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.in.EnviarCorreoUseCase;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.out.CorreoEnviador;

@Component
@RequiredArgsConstructor
public class EnviarCorreoUseCaseImpl implements EnviarCorreoUseCase {

    private final CorreoEnviador correoEnviador;

    @Override
    public void enviar(Correo correo) {
        correoEnviador.enviar(correo);
    }

}
