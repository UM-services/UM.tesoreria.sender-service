package um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.in;

import java.util.List;

public interface ValidarEmailsUseCase {

    boolean validar(List<String> emails);

}
