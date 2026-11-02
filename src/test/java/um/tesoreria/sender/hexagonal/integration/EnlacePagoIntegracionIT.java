package um.tesoreria.sender.hexagonal.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import org.junit.jupiter.api.Test;

class EnlacePagoIntegracionIT extends IntegracionSoporte {

    @Test
    void enviaElEnlaceDeMercadoPago() throws Exception {
        StubsCore.enlacePago(true);

        var respuesta = get("/api/tesoreria/sender/mercadopago/cuota/9");

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals("Envío de Correo Ok", respuesta.getBody());
        assertTrue(CORREO.getReceivedMessages().length >= 1);
        var mensaje = CORREO.getReceivedMessages()[0];
        assertEquals("Envío de Enlace de MercadoPago: Perez, Ana", mensaje.getSubject());
        assertTrue(texto(mensaje).contains("https://mp/pagar"));
    }

    @Test
    void noEnviaSinDatosDeEmail() {
        StubsCore.enlacePago(false);

        var respuesta = get("/api/tesoreria/sender/mercadopago/cuota/9");

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals("ERROR: Sin datos de e-mail para ENVIAR", respuesta.getBody());
        assertEquals(0, CORREO.getReceivedMessages().length);
    }

    private String texto(Part parte) throws Exception {
        if (parte.isMimeType("text/html") || parte.isMimeType("text/plain")) {
            return String.valueOf(parte.getContent());
        }
        if (parte.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) parte.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                String texto = texto(multipart.getBodyPart(i));
                if (texto != null) {
                    return texto;
                }
            }
        }
        return null;
    }

}
