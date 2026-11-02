package um.tesoreria.sender.hexagonal.datos.validacionEmail.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.in.ValidarEmailsUseCase;

@Service
@RequiredArgsConstructor
public class ValidacionEmailService {

    private final ValidarEmailsUseCase validarEmailsUseCase;

    public boolean validar(String email) {
        return validarEmailsUseCase.validar(List.of(email));
    }

    public boolean validar(List<String> emails) {
        return validarEmailsUseCase.validar(emails);
    }

}
