package um.tesoreria.sender.hexagonal.datos.validacionEmail.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.in.ValidarEmailsUseCase;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.out.ValidadorEmail;

@Component
@RequiredArgsConstructor
public class ValidarEmailsUseCaseImpl implements ValidarEmailsUseCase {

    private final ValidadorEmail validadorEmail;

    @Override
    public boolean validar(List<String> emails) {
        return Boolean.TRUE.equals(validadorEmail.validar(emails));
    }

}
