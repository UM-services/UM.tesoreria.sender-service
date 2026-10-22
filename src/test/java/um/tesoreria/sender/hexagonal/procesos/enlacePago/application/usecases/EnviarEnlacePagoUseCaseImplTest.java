package um.tesoreria.sender.hexagonal.procesos.enlacePago.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.procesos.enlacePago.application.service.EnlacePagoEmailComposer;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.MercadoPagoContext;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;

@ExtendWith(MockitoExtension.class)
class EnviarEnlacePagoUseCaseImplTest {

    private static final Long CUOTA_ID = 9L;

    @Mock
    private ChequeraDatosService chequeraDatosService;
    @Mock
    private PreferenciaPagoDatosService preferenciaPagoDatosService;
    @Mock
    private EnlacePagoEmailComposer enlacePagoEmailComposer;
    @Mock
    private CorreoService correoService;

    @InjectMocks
    private EnviarEnlacePagoUseCaseImpl useCase;

    @Test
    void noEnviaCuandoNoHayEmails() {
        when(chequeraDatosService.obtenerCuotaPorId(CUOTA_ID)).thenReturn(cuota("", ""));
        when(preferenciaPagoDatosService.obtenerContextoActivo(CUOTA_ID)).thenReturn(contexto());

        String resultado = useCase.enviar(CUOTA_ID);

        assertEquals("ERROR: Sin datos de e-mail para ENVIAR", resultado);
        verifyNoInteractions(correoService);
    }

    @Test
    void enviaElEnlaceDePago() {
        var cuota = cuota("a@b.c", "");
        var contexto = contexto();
        when(chequeraDatosService.obtenerCuotaPorId(CUOTA_ID)).thenReturn(cuota);
        when(preferenciaPagoDatosService.obtenerContextoActivo(CUOTA_ID)).thenReturn(contexto);
        when(enlacePagoEmailComposer.cuerpo("Perez, Ana", cuota, contexto)).thenReturn("<html/>");

        String resultado = useCase.enviar(CUOTA_ID);

        assertEquals("Envío de Correo Ok", resultado);
        verify(correoService).enviar(any());
    }

    private MercadoPagoContext contexto() {
        return MercadoPagoContext.builder()
                .mercadoPagoContextId(1L)
                .chequeraCuotaId(CUOTA_ID)
                .initPoint("https://mp/pagar")
                .fechaVencimiento(OffsetDateTime.now().plusDays(10))
                .importe(BigDecimal.TEN)
                .build();
    }

    private ChequeraCuota cuota(String emailPersonal, String emailInstitucional) {
        return ChequeraCuota.builder()
                .chequeraCuotaId(CUOTA_ID)
                .chequeraSerie(ChequeraSerie.builder()
                        .personaId(BigDecimal.ONE)
                        .persona(Persona.builder().apellido("Perez").nombre("Ana").build())
                        .domicilio(Domicilio.builder()
                                .emailPersonal(emailPersonal)
                                .emailInstitucional(emailInstitucional)
                                .build())
                        .build())
                .build();
    }

}
