package um.tesoreria.sender.hexagonal.procesos.chequera.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.sender.hexagonal.datos.chequera.application.service.ChequeraDatosService;
import um.tesoreria.sender.hexagonal.datos.preferenciaPago.application.service.PreferenciaPagoDatosService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraCorreoFactory;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraEmailComposer;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.DestinatariosResolver;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.EnvioSolicitudClaim;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.EnvioSolicitudGateway;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ResultadoEnvioChequeraPublisher;
import um.tesoreria.sender.hexagonal.shared.domain.model.Carrera;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@ExtendWith(MockitoExtension.class)
class EnviarChequeraUseCaseImplTest {

    private static final SolicitudEnvioChequera SOLICITUD =
            new SolicitudEnvioChequera(1, 2, 3L, 1, false, true, true, false, true);

    @Mock
    private ChequeraDatosService chequeraDatosService;
    @Mock
    private PreferenciaPagoDatosService preferenciaPagoDatosService;
    @Mock
    private ChequeraDatosAssembler chequeraDatosAssembler;
    @Mock
    private ChequeraPdfGenerator chequeraPdfGenerator;
    @Mock
    private ChequeraEmailComposer chequeraEmailComposer;
    @Mock
    private DestinatariosResolver destinatariosResolver;
    @Mock
    private ChequeraCorreoFactory chequeraCorreoFactory;
    @Mock
    private CorreoService correoService;
    @Mock
    private SenderSettings senderSettings;
    @Mock
    private ResultadoEnvioChequeraPublisher resultadoChequeraPublisher;
    @Mock
    private EnvioSolicitudGateway solicitudGateway;

    @InjectMocks
    private EnviarChequeraUseCaseImpl useCase;

    @Test
    void noEnviaCuandoNoHayCuotasPendientes() {
        var serie = serie();
        when(solicitudGateway.crear(1, 2, 3L)).thenReturn("sol-1");
        when(solicitudGateway.reclamar("sol-1")).thenReturn(claim(true));
        when(chequeraDatosService.obtenerSerie(1, 2, 3L)).thenReturn(serie);
        when(preferenciaPagoDatosService.crearContexto(1, 2, 3L, 1))
                .thenReturn(List.of(preferencia((byte) 1)));

        String resultado = useCase.enviar(SOLICITUD);

        assertTrue(resultado.contains("Sin CUOTAS para ENVIAR"));
        verify(chequeraDatosService).actualizarCodigosBarras(1, 2, 3L);
        verifyNoInteractions(chequeraPdfGenerator, correoService);
        verify(solicitudGateway).marcarOmitida("sol-1", "SIN_CUOTAS", "Sin cuotas para enviar");
        verify(resultadoChequeraPublisher).publicar(10L, false, "Sin cuotas para enviar");
    }

    @Test
    void enviaYConfirmaEnElLedgerCuandoHayCuotasPendientes() {
        var serie = serie();
        var preferencias = List.of(preferencia((byte) 0));
        var datos = new DatosChequera(serie, preferencias, serie.getPersona(), new Carrera(), List.of());
        var documento = DocumentoPdf.de("/tmp/chequera.pdf");
        var correo = Correo.builder().para(List.of("a@b.c")).build();

        when(solicitudGateway.crear(1, 2, 3L)).thenReturn("sol-1");
        when(solicitudGateway.reclamar("sol-1")).thenReturn(claim(true));
        when(chequeraDatosService.obtenerSerie(1, 2, 3L)).thenReturn(serie);
        when(preferenciaPagoDatosService.crearContexto(1, 2, 3L, 1)).thenReturn(preferencias);
        when(chequeraDatosAssembler.armar(serie, preferencias)).thenReturn(datos);
        when(chequeraPdfGenerator.generarChequera(datos, true, false)).thenReturn(documento);
        when(chequeraEmailComposer.cuerpoChequera("Perez, Ana", preferencias, serie)).thenReturn("<html/>");
        when(destinatariosResolver.resolver(serie, false)).thenReturn(List.of("a@b.c"));
        when(senderSettings.fromChequera()).thenReturn("from@um.edu.ar");
        when(chequeraCorreoFactory.crear("from@um.edu.ar", List.of("a@b.c"),
                "Recordatorio - Pago cuota UM -> Perez, Ana", "<html/>", "/tmp/chequera.pdf", 1)).thenReturn(correo);

        String resultado = useCase.enviar(SOLICITUD);

        assertEquals("\n\nEnvío de Chequera Ok!!!\n\n", resultado);
        verify(correoService).enviar(correo);
        verify(solicitudGateway).marcarEnviado("sol-1");
        verify(chequeraDatosService, never()).marcarEnviada(any(), any(), any());
    }

    @Test
    void noEnviaSiElLedgerNoLoPermite() {
        when(solicitudGateway.reclamar("sol-1"))
                .thenReturn(new EnvioSolicitudClaim("sol-1", false, "ENVIADO", 1, "YA_ENVIADA"));

        String resultado = useCase.enviar(new SolicitudEnvioChequera("sol-1", 10L, 1, 2, 3L, 1,
                false, true, true, false, true));

        assertTrue(resultado.contains("YA_ENVIADA"));
        verifyNoInteractions(correoService, chequeraPdfGenerator, chequeraDatosService);
        verify(solicitudGateway, never()).marcarEnviado(anyString());
    }

    @Test
    void marcaFallidoSiElSmtpFalla() {
        var serie = serie();
        var preferencias = List.of(preferencia((byte) 0));
        var datos = new DatosChequera(serie, preferencias, serie.getPersona(), new Carrera(), List.of());
        var documento = DocumentoPdf.de("/tmp/chequera.pdf");
        var correo = Correo.builder().para(List.of("a@b.c")).build();

        when(solicitudGateway.crear(1, 2, 3L)).thenReturn("sol-1");
        when(solicitudGateway.reclamar("sol-1")).thenReturn(claim(true));
        when(chequeraDatosService.obtenerSerie(1, 2, 3L)).thenReturn(serie);
        when(preferenciaPagoDatosService.crearContexto(1, 2, 3L, 1)).thenReturn(preferencias);
        when(chequeraDatosAssembler.armar(serie, preferencias)).thenReturn(datos);
        when(chequeraPdfGenerator.generarChequera(datos, true, false)).thenReturn(documento);
        when(chequeraEmailComposer.cuerpoChequera("Perez, Ana", preferencias, serie)).thenReturn("<html/>");
        when(destinatariosResolver.resolver(serie, false)).thenReturn(List.of("a@b.c"));
        when(senderSettings.fromChequera()).thenReturn("from@um.edu.ar");
        when(chequeraCorreoFactory.crear("from@um.edu.ar", List.of("a@b.c"),
                "Recordatorio - Pago cuota UM -> Perez, Ana", "<html/>", "/tmp/chequera.pdf", 1)).thenReturn(correo);
        doThrow(new RuntimeException("SMTP caído")).when(correoService).enviar(correo);

        assertThrows(RuntimeException.class, () -> useCase.enviar(SOLICITUD));
        verify(solicitudGateway).marcarFallido("sol-1", "SMTP caído");
        verify(solicitudGateway, never()).marcarEnviado(anyString());
    }

    @Test
    void marcaFallidoSiNoSePuedeConfirmarElEnvio() {
        var serie = serie();
        var preferencias = List.of(preferencia((byte) 0));
        var datos = new DatosChequera(serie, preferencias, serie.getPersona(), new Carrera(), List.of());
        var documento = DocumentoPdf.de("/tmp/chequera.pdf");
        var correo = Correo.builder().para(List.of("a@b.c")).build();

        when(solicitudGateway.crear(1, 2, 3L)).thenReturn("sol-1");
        when(solicitudGateway.reclamar("sol-1")).thenReturn(claim(true));
        when(chequeraDatosService.obtenerSerie(1, 2, 3L)).thenReturn(serie);
        when(preferenciaPagoDatosService.crearContexto(1, 2, 3L, 1)).thenReturn(preferencias);
        when(chequeraDatosAssembler.armar(serie, preferencias)).thenReturn(datos);
        when(chequeraPdfGenerator.generarChequera(datos, true, false)).thenReturn(documento);
        when(chequeraEmailComposer.cuerpoChequera("Perez, Ana", preferencias, serie)).thenReturn("<html/>");
        when(destinatariosResolver.resolver(serie, false)).thenReturn(List.of("a@b.c"));
        when(senderSettings.fromChequera()).thenReturn("from@um.edu.ar");
        when(chequeraCorreoFactory.crear("from@um.edu.ar", List.of("a@b.c"),
                "Recordatorio - Pago cuota UM -> Perez, Ana", "<html/>", "/tmp/chequera.pdf", 1)).thenReturn(correo);
        doThrow(new RuntimeException("core caído")).when(solicitudGateway).marcarEnviado("sol-1");

        assertThrows(RuntimeException.class, () -> useCase.enviar(SOLICITUD));
        verify(solicitudGateway).marcarFallido("sol-1", "Enviado sin confirmar: core caído");
    }

    private static EnvioSolicitudClaim claim(boolean puedeEnviar) {
        return new EnvioSolicitudClaim("sol-1", puedeEnviar, "ENVIANDO", 1, null);
    }

    private ChequeraSerie serie() {
        return ChequeraSerie.builder()
                .chequeraId(10L)
                .facultadId(1)
                .tipoChequeraId(2)
                .chequeraSerieId(3L)
                .personaId(BigDecimal.ONE)
                .documentoId(123)
                .lectivoId(2026)
                .alternativaId(1)
                .persona(Persona.builder().apellido("Perez").nombre("Ana").build())
                .build();
    }

    private PreferenciaPago preferencia(byte pagado) {
        return PreferenciaPago.builder()
                .chequeraCuota(ChequeraCuota.builder()
                        .chequeraCuotaId(9L)
                        .pagado(pagado)
                        .importe1(BigDecimal.TEN)
                        .build())
                .build();
    }

}
