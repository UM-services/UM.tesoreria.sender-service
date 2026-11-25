package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.DireccionCorreoValidador;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinatariosResolver {

    private static final String TESTING_EMAIL = "daniel.quinterospinto@gmail.com";

    private final SenderSettings senderSettings;
    private final DireccionCorreoValidador direccionCorreoValidador;

    public List<String> resolver(ChequeraSerie serie, boolean copiaInformes) {
        List<String> addresses = new ArrayList<>();
        if (senderSettings.testing()) {
            log.debug("Testing -> {}", TESTING_EMAIL);
            addresses.add(TESTING_EMAIL);
            return addresses;
        }

        Domicilio domicilio = serie.getDomicilio();
        if (domicilio == null) {
            return addresses;
        }
        agregarValido(addresses, domicilio.getEmailPersonal(), "personal");
        agregarValido(addresses, domicilio.getEmailInstitucional(), "institucional");

        if (copiaInformes && serie.getTipoChequera() != null) {
            agregarValido(addresses, serie.getTipoChequera().getEmailCopia(), "informes");
        }

        log.info("Addresses: {}", addresses);
        return addresses;
    }

    private void agregarValido(List<String> addresses, String email, String tipo) {
        if (direccionCorreoValidador.esValida(email)) {
            addresses.add(email);
            log.debug("adding {} email -> {}", tipo, email);
        }
    }

}
