package um.tesoreria.sender.hexagonal.procesos.chequera.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.TipoChequera;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.DireccionCorreoValidador;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

/**
 * El envío toma únicamente los correos del domicilio de tesorería. El pagador de la facultad ya no
 * participa: ese dato dependía del sistema legacy y se dejó de consultar.
 */
@ExtendWith(MockitoExtension.class)
class DestinatariosResolverTest {

    @Mock
    private SenderSettings senderSettings;
    @Mock
    private DireccionCorreoValidador direccionCorreoValidador;

    @InjectMocks
    private DestinatariosResolver resolver;

    @Test
    void usaLosCorreosDelDomicilio() {
        when(senderSettings.testing()).thenReturn(false);
        when(direccionCorreoValidador.esValida(any())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return email != null && !email.isBlank();
        });

        var serie = ChequeraSerie.builder()
                .domicilio(Domicilio.builder()
                        .emailPersonal("personal@um.edu.ar")
                        .emailInstitucional("institucional@um.edu.ar")
                        .build())
                .build();

        List<String> destinatarios = resolver.resolver(serie, false);

        assertEquals(List.of("personal@um.edu.ar", "institucional@um.edu.ar"), destinatarios);
    }

    @Test
    void agregaElCorreoDeCopiaSoloSiSePide() {
        when(senderSettings.testing()).thenReturn(false);
        when(direccionCorreoValidador.esValida(any())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return email != null && !email.isBlank();
        });

        var serie = ChequeraSerie.builder()
                .domicilio(Domicilio.builder().emailPersonal("personal@um.edu.ar").build())
                .tipoChequera(TipoChequera.builder().emailCopia("informes@um.edu.ar").build())
                .build();

        assertFalse(resolver.resolver(serie, false).contains("informes@um.edu.ar"));
        assertTrue(resolver.resolver(serie, true).contains("informes@um.edu.ar"));
    }

    @Test
    void noEnviaSiNoHayDomicilio() {
        when(senderSettings.testing()).thenReturn(false);

        var serie = ChequeraSerie.builder().build();

        assertTrue(resolver.resolver(serie, false).isEmpty());
    }

}
