package um.tesoreria.sender.hexagonal.shared.infrastructure.mail;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.DireccionCorreoValidador;

@Component
public class DireccionCorreoValidadorAdapter implements DireccionCorreoValidador {

    @Override
    public boolean esValida(String email) {
        if (email == null || email.isEmpty() || email.toLowerCase().contains("ñ")) {
            return false;
        }
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }

}
