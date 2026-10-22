package um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.out;

import java.util.List;

public interface ValidadorEmail {

    boolean validar(List<String> emails);

}
