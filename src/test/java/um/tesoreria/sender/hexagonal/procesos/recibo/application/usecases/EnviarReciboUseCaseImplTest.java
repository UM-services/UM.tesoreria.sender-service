package um.tesoreria.sender.hexagonal.procesos.recibo.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.sender.hexagonal.datos.facturacion.application.service.FacturacionDatosService;
import um.tesoreria.sender.hexagonal.datos.validacionEmail.application.service.ValidacionEmailService;
import um.tesoreria.sender.hexagonal.gateways.correo.application.exception.CorreoException;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.recibo.application.service.ReciboDatosAssembler;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.out.ReciboPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraFacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraPago;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.Domicilio;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;
import um.tesoreria.sender.hexagonal.shared.domain.model.Persona;
import um.tesoreria.sender.hexagonal.shared.domain.model.ReciboMessageCheck;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

@ExtendWith(MockitoExtension.class)
class EnviarReciboUseCaseImplTest {

    private static final Long FACTURACION_ID = 5L;

    @Mock
    private ReciboDatosAssembler reciboDatosAssembler;
    @Mock
    private ReciboPdfGenerator reciboPdfGenerator;
    @Mock
    private FacturacionDatosService facturacionDatosService;
    @Mock
    private ValidacionEmailService validacionEmailService;
    @Mock
    private CorreoService correoService;
    @Mock
    private SenderSettings senderSettings;

    @InjectMocks
    private EnviarReciboUseCaseImpl useCase;

    @Test
    void incrementaReintentosCuandoNoSeGeneraElPdf() {
        var datos = datos("a@b.c", "");
        when(reciboDatosAssembler.armar(FACTURACION_ID, null)).thenReturn(datos);
        when(reciboPdfGenerator.generar(FACTURACION_ID, datos)).thenReturn(DocumentoPdf.vacio());

        String resultado = useCase.enviar(FACTURACION_ID);

        assertEquals("ERROR: Recibo de Cuota 1/2/3/1/4/5 Sin Recibo para ENVIAR", resultado);
        assertEquals(1, datos.facturacionElectronica().getRetries());
        verify(facturacionDatosService).actualizar(datos.facturacionElectronica());
        verify(correoService, never()).enviar(any());
    }

    @Test
    void enviaElReciboYMarcaLosRegistros() {
        var datos = datos("a@b.c", "");
        var documento = DocumentoPdf.de("/tmp/5.pdf");
        when(reciboDatosAssembler.armar(FACTURACION_ID, null)).thenReturn(datos);
        when(reciboPdfGenerator.generar(FACTURACION_ID, datos)).thenReturn(documento);
        when(senderSettings.fromRecibo()).thenReturn("from@um.edu.ar");
        when(senderSettings.testing()).thenReturn(false);
        when(validacionEmailService.validar("a@b.c")).thenReturn(true);

        String resultado = useCase.enviar(FACTURACION_ID);

        assertEquals("Recibo de Cuota 1/2/3/1/4/5 Envío de Correo Ok!!!", resultado);
        assertEquals((byte) 1, datos.facturacionElectronica().getEnviada());
        verify(facturacionDatosService).actualizar(datos.facturacionElectronica());
        verify(facturacionDatosService).registrarMessageCheck(any(ReciboMessageCheck.class));

        var captor = ArgumentCaptor.forClass(Correo.class);
        verify(correoService).enviar(captor.capture());
        assertEquals("Envío Automático de Recibo -> /tmp/5.pdf", captor.getValue().getAsunto());
        assertEquals(List.of("a@b.c"), captor.getValue().getPara());
    }

    @Test
    void incrementaReintentosCuandoLaDireccionEsInvalida() {
        var datos = datos("a@b.c", "");
        when(reciboDatosAssembler.armar(FACTURACION_ID, null)).thenReturn(datos);
        when(reciboPdfGenerator.generar(FACTURACION_ID, datos)).thenReturn(DocumentoPdf.de("/tmp/5.pdf"));
        when(senderSettings.fromRecibo()).thenReturn("from@um.edu.ar");
        when(senderSettings.testing()).thenReturn(false);
        when(validacionEmailService.validar("a@b.c")).thenReturn(true);
        org.mockito.Mockito.doThrow(new CorreoException(CorreoException.Tipo.DIRECCION_INVALIDA, "invalida", null))
                .when(correoService).enviar(any());

        String resultado = useCase.enviar(FACTURACION_ID);

        assertTrue(resultado.contains("Dirección de correo inválida"));
        assertEquals(1, datos.facturacionElectronica().getRetries());
    }

    private DatosRecibo datos(String emailPersonal, String emailInstitucional) {
        var facturacionElectronica = FacturacionElectronica.builder()
                .facturacionElectronicaId(FACTURACION_ID)
                .chequeraPagoId(7L)
                .numeroComprobante(1L)
                .personaId(BigDecimal.ONE)
                .apellido("Perez")
                .nombre("Ana")
                .importe(BigDecimal.TEN)
                .cae("123")
                .fechaRecibo(OffsetDateTime.now())
                .fechaVencimientoCae(OffsetDateTime.now())
                .build();
        var pago = ChequeraPago.builder()
                .chequeraPagoId(7L)
                .facultadId(1)
                .tipoChequeraId(2)
                .chequeraSerieId(3L)
                .productoId(4)
                .alternativaId(1)
                .cuotaId(5)
                .build();
        var cuota = ChequeraCuota.builder().chequeraCuotaId(9L).build();
        var serie = ChequeraSerie.builder()
                .chequeraId(10L)
                .facultadId(1)
                .tipoChequeraId(2)
                .chequeraSerieId(3L)
                .personaId(BigDecimal.ONE)
                .documentoId(123)
                .persona(Persona.builder().apellido("Perez").nombre("Ana").build())
                .domicilio(Domicilio.builder()
                        .emailPersonal(emailPersonal)
                        .emailInstitucional(emailInstitucional)
                        .build())
                .build();
        return new DatosRecibo(facturacionElectronica, null, pago, cuota, serie,
                ChequeraFacturacionElectronica.builder().build());
    }

}
