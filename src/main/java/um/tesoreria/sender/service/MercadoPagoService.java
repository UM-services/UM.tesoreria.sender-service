package um.tesoreria.sender.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.client.tesoreria.core.MercadoPagoContextClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.DomicilioDto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    private final ChequeraCuotaClient chequeraCuotaClient;
    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final JavaMailSender javaMailSender;

    public MercadoPagoService(ChequeraCuotaClient chequeraCuotaClient,
                              MercadoPagoContextClient mercadoPagoContextClient,
                              JavaMailSender javaMailSender) {
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.mercadoPagoContextClient = mercadoPagoContextClient;
        this.javaMailSender = javaMailSender;
    }

    public String sendCuota(Long chequeraCuotaId) throws MessagingException {
        var chequeraCuota = chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId);
        var mercadoPagoContext = mercadoPagoContextClient.findActivoByChequeraCuotaId(chequeraCuotaId);

        DomicilioDto domicilio = chequeraCuota.getChequeraSerie().getDomicilio();
        if (domicilio == null) {
            return "ERROR: Sin datos de e-mail para ENVIAR";
        }
        if (domicilio.getEmailPersonal().isEmpty() && domicilio.getEmailInstitucional().isEmpty()) {
            return "ERROR: Sin datos de e-mail para ENVIAR";
        }

        // Obtener el nombre del alumno
        String nombreAlumno = chequeraCuota.getChequeraSerie().getPersona().getApellidoNombre();

        // Formatear la fecha de vencimiento
        String fechaVencimientoFormatted = mercadoPagoContext.getFechaVencimiento()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Crear tabla con los detalles
        // Código HTML para el cuerpo del correo electrónico
        String data = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #007BFF; color: white; padding: 20px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 20px; }" +
                ".info-section { background-color: #f4f4f4; padding: 15px; margin-bottom: 20px; border-radius: 8px; }" +
                ".info-section p { margin: 5px 0; }" +
                ".details-table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                ".details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }" +
                ".details-table th { background-color: #007BFF; color: white; }" +
                ".footer { background-color: #e9ecef; padding: 10px; text-align: center; font-size: 12px; color: #333; }" +
                ".qr-section { text-align: center; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Universidad de Mendoza te envió un enlace de pago</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p><strong>Estimad@ " + nombreAlumno + ",</strong></p>" +
                "<p>Le compartimos los detalles:</p>" +
                "<div class='info-section'>" +
                "<p>Unidad Académica: <strong>" + chequeraCuota.getFacultad().getNombre() + "</strong></p>" +
                "<br/>" +
                "<p>Tipo de Chequera: <strong>" + chequeraCuota.getTipoChequera().getNombre() + "</strong></p>" +
                "<br/>" +
                "<p>Sede: <strong>" + chequeraCuota.getChequeraSerie().getGeografica().getNombre() + "</strong></p>" +
                "<br/>" +
                "<p>Ciclo Lectivo: <strong>" + chequeraCuota.getChequeraSerie().getLectivo().getNombre() + "</strong></p>" +
                "<br/>" +
                "<p>Tipo de Arancel: <strong>" + chequeraCuota.getChequeraSerie().getArancelTipo().getDescripcion() + "</strong></p>" +
                "<br/>" +
                "<p>Alternativa: <strong>" + chequeraCuota.getChequeraSerie().getAlternativaId() + "</strong></p>" +
                "</div>" +
                "<table class='details-table'>" +
                "<tr>" +
                "<th>Producto</th>" +
                "<th>Período</th>" +
                "<th>Fecha de Vencimiento</th>" +
                "<th>Importe</th>" +
                "<th>Enlace</th>" +
                "</tr>" +
                "<tr>" +
                "<td>" + chequeraCuota.getProducto().getNombre() + "</td>" +
                "<td>" + chequeraCuota.getMes() + "/" + chequeraCuota.getAnho() + "</td>" +
                "<td>" + fechaVencimientoFormatted + "</td>" +
                "<td>$" + String.format("%.2f", mercadoPagoContext.getImporte()) + "</td>" +
                "<td><a href='" + mercadoPagoContext.getInitPoint() + "' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Universidad de Mendoza</p>" +
                "<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Envia correo con logo
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Envia correo
        List<String> addresses = new ArrayList<String>();

        if (!domicilio.getEmailPersonal().isEmpty()) {
            addresses.add(domicilio.getEmailPersonal());
        }
        if (!domicilio.getEmailInstitucional().isEmpty()) {
            addresses.add(domicilio.getEmailInstitucional());
        }

        try {
            helper.setTo(addresses.toArray(new String[addresses.size()]));
            helper.setText(data, true);
            helper.setReplyTo("no-reply@um.edu.ar");
            helper.setSubject("Envío de Enlace de MercadoPago: " + nombreAlumno);

        } catch (MessagingException e) {
            return "Enlace NO pudo enviarse";
        }

        javaMailSender.send(message);
        return "Envío de Correo Ok";

    }

}
