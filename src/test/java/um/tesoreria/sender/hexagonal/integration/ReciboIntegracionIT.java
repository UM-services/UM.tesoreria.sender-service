package um.tesoreria.sender.hexagonal.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.Multipart;
import java.nio.file.Files;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

class ReciboIntegracionIT extends IntegracionSoporte {

    @Test
    void generaPdfOriginalYDuplicadoConQr() throws Exception {
        StubsCore.reciboCompleto(true, true);

        var respuesta = getBytes("/api/tesoreria/sender/recibo/pdf/5");

        assertEquals(200, respuesta.getStatusCode().value());
        var archivo = reporte("5.pdf");
        assertTrue(Files.exists(archivo));
        assertTrue(Files.exists(reporte("12345678901234.png")), "debe generar el QR AFIP");

        try (var reader = new PdfReader(archivo.toString())) {
            assertEquals(2, reader.getNumberOfPages());
            String original = new PdfTextExtractor(reader).getTextFromPage(1);
            String duplicado = new PdfTextExtractor(reader).getTextFromPage(2);
            assertTrue(original.contains("ORIGINAL"));
            assertTrue(duplicado.contains("DUPLICADO"));
            assertTrue(original.contains("CAE Nro"));
            assertTrue(original.contains("20111111112"));
        }
    }

    @Test
    void enviaElReciboYConservaLosCamposNoModificadosEnElPut() throws Exception {
        StubsCore.reciboCompleto(true, true);

        var respuesta = get("/api/tesoreria/sender/recibo/send/5");

        assertEquals(202, respuesta.getStatusCode().value());
        await().atMost(Duration.ofSeconds(20)).until(() -> CORREO.getReceivedMessages().length > 0);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertTrue(mensaje.getSubject().startsWith("Envío Automático de Recibo ->"));
        assertTrue(mensaje.getContent() instanceof Multipart);

        await().atMost(Duration.ofSeconds(20)).until(
                () -> !CORE.findAll(putRequestedFor(urlEqualTo(StubsCore.FACTURACION))).isEmpty());
        String cuerpo = CORE.findAll(putRequestedFor(urlEqualTo(StubsCore.FACTURACION))).get(0).getBodyAsString();

        assertTrue(cuerpo.contains("\"enviada\":1"), "debe marcar la facturación como enviada");
        assertTrue(cuerpo.contains("\"retries\":0"));
        assertTrue(cuerpo.contains("\"cuit\":\"20111111112\""), "debe conservar los campos no modificados");
        assertTrue(cuerpo.contains("\"condicionIva\":\"Consumidor Final\""));
        assertTrue(cuerpo.contains("\"numeroComprobante\":1"));

        CORE.verify(postRequestedFor(urlEqualTo(StubsCore.RECIBO_CHECK)));
    }

    @Test
    void siguienteReciboIncrementaReintentosSiNoHayEmails() {
        StubsCore.reciboCompleto(true, false);

        var respuesta = get("/api/tesoreria/sender/recibo/sendNext");

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("Sin e-mails para ENVIAR"));
        await().atMost(Duration.ofSeconds(20)).until(
                () -> !CORE.findAll(putRequestedFor(urlEqualTo(StubsCore.FACTURACION))).isEmpty());
        String cuerpo = CORE.findAll(putRequestedFor(urlEqualTo(StubsCore.FACTURACION))).get(0).getBodyAsString();
        assertTrue(cuerpo.contains("\"retries\":1"));
    }

}
