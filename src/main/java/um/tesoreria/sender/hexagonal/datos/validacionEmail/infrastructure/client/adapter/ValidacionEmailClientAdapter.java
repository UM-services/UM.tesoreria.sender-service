package um.tesoreria.sender.hexagonal.datos.validacionEmail.infrastructure.client.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.domain.ports.out.ValidadorEmail;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.infrastructure.client.ValidacionEmailClient;

@Component
@RequiredArgsConstructor
public class ValidacionEmailClientAdapter implements ValidadorEmail {

    private final ValidacionEmailClient validacionEmailClient;

    @Override
    public boolean validar(List<String> emails) {
        return validacionEmailClient.mailValidate(emails);
    }

}
