package um.tesoreria.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.client.tesoreria.core.MercadoPagoContextClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.DomicilioDto;

import java.text.MessageFormat;
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

        String data = "";
        data = "<p>Estimad@ Estudiante:</p>"
             + "<p>Le enviamos un enlace de pago.</p>"
             + "<p><a href='" + mercadoPagoContext.getInitPoint() + "'>"
             + "Click aquí para realizar el pago</a></p>"
             + "<p>Atentamente.</p>"
             + "<p>Universidad de Mendoza</p>"
             + "<hr>"
             + "<p><small>Por favor no responda este mail, fue generado automáticamente. "
             + "Su respuesta no será leída.</small></p>";

        // Envia correo
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
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
            helper.setSubject("Envío de Enlace de Pago");

        } catch (MessagingException e) {
            return "Enlace NO pudo enviarse";
        }

        javaMailSender.send(message);
        return "Envío de Correo Ok";

    }

}
