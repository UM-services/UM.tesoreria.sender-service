package um.tesoreria.sender.hexagonal.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

class ChequeraIntegracionIT extends IntegracionSoporte {

    @Test
    void enviaChequeraConPdfAdjuntoEImagenesInline() throws Exception {
        StubsCore.chequeraCompleta(true);

        var respuesta = get("/api/tesoreria/sender/chequera/sendChequera/1/2/3/1/false/true");

        assertEquals(202, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("Solicitud de envío en proceso"));

        await().atMost(Duration.ofSeconds(30)).until(() -> CORREO.getReceivedMessages().length > 0);
        assertEquals(1, CORREO.getReceivedMessages().length);

        var mensaje = CORREO.getReceivedMessages()[0];
        assertEquals("Recordatorio - Pago cuota UM -> Perez, Ana", mensaje.getSubject());
        assertTrue(mensaje.getContent() instanceof Multipart);
        var multipart = (Multipart) mensaje.getContent();

        boolean[] adjunto = {false};
        Set<String> cids = new HashSet<>();
        inspeccionarPartes(multipart, adjunto, cids);
        assertTrue(adjunto[0], "debe adjuntar el PDF");
        assertTrue(cids.stream().anyMatch(cid -> cid.contains("logoImage")), "debe incluir el logo inline");
        assertTrue(cids.stream().anyMatch(cid -> cid.contains("medioPago1")),
                "debe incluir los medios de pago inline");

        var archivo = reporte("chequera-1-1-2-3.pdf");
        assertTrue(Files.exists(archivo));
        try (var reader = new PdfReader(archivo.toString())) {
            assertEquals(1, reader.getNumberOfPages());
            String texto = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(texto.contains("UNIVERSIDAD DE MENDOZA"));
            assertTrue(texto.contains("Chequera"));
        }

        CORE.verify(postRequestedFor(urlEqualTo(StubsCore.CHEQUERA_SERIE_MARK)));
        CORE.verify(postRequestedFor(urlEqualTo(StubsCore.CHEQUERA_CHECK)));
    }

    @Test
    void noEnviaCuandoNoHayCuotasPendientes() {
        StubsCore.chequeraCompleta(false);

        var respuesta = get("/api/tesoreria/sender/chequera/sendChequera/1/2/3/1/false/true");

        assertEquals(202, respuesta.getStatusCode().value());
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(4))
                .until(() -> CORREO.getReceivedMessages().length == 0);
    }

    @Test
    void generaElPdfDeUnaCuota() {
        StubsCore.chequeraCompleta(true);
        StubsCore.cuotaPorClave();

        var respuesta = getBytes(
                "/api/tesoreria/sender/chequera/generateCuotaPdf/1/2/3/1/4/1");

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(Files.exists(reporte("cuota-1-1-2-3-4-1.pdf")));
    }

    @Test
    void generaElPdfPorHttp() {
        StubsCore.chequeraCompleta(true);

        var respuesta = getBytes("/api/tesoreria/sender/chequera/generatePdf/1/2/3/1/false");

        assertEquals(200, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().length > 0);
        assertNotNull(respuesta.getHeaders().getContentType());
        assertEquals("application/octet-stream", respuesta.getHeaders().getContentType().toString());
    }

    private void inspeccionarPartes(Multipart multipart, boolean[] adjunto, Set<String> cids) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            Part parte = multipart.getBodyPart(i);
            if (parte.isMimeType("multipart/*")) {
                inspeccionarPartes((Multipart) parte.getContent(), adjunto, cids);
            } else {
                if (parte.getFileName() != null && parte.getFileName().endsWith(".pdf")) {
                    adjunto[0] = true;
                }
                if (parte instanceof MimeBodyPart mimeBodyPart) {
                    String cid = mimeBodyPart.getContentID();
                    if (cid != null) {
                        cids.add(cid);
                    }
                }
            }
        }
    }

}
