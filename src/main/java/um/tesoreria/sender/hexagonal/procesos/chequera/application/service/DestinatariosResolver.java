package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.InscripcionFull;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.DireccionCorreoValidador;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinatariosResolver {

    private static final String TESTING_EMAIL = "daniel.quinterospinto@gmail.com";

    private final SenderSettings senderSettings;
    private final DireccionCorreoValidador direccionCorreoValidador;

    public List<String> resolver(ChequeraSerie serie, InscripcionFull inscripcionFull, boolean copiaInformes) {
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

        if (inscripcionFull != null) {
            var domicilioPago = inscripcionFull.getDomicilioPago();
            if (domicilioPago != null && domicilioPago.getPersonaId() != null
                    && domicilioPago.getPersonaId().compareTo(BigDecimal.ZERO) > 0) {
                agregarValido(addresses, domicilioPago.getEmailPersonal(), "pago personal");
                agregarValido(addresses, domicilioPago.getEmailInstitucional(), "pago institucional");
            }
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
