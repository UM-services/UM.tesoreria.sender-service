package um.tesoreria.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.lowagie.text.Image;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import um.tesoreria.sender.client.tesoreria.core.ChequeraSerieClient;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChequeraService {

    private final FormulariosToPdfService formulariosToPdfService;
    private final JavaMailSender javaMailSender;
    private final ChequeraSerieClient chequeraSerieClient;

    public ChequeraService(FormulariosToPdfService formulariosToPdfService, JavaMailSender javaMailSender, ChequeraSerieClient chequeraSerieClient) {
        this.formulariosToPdfService = formulariosToPdfService;
        this.javaMailSender = javaMailSender;
        this.chequeraSerieClient = chequeraSerieClient;
    }

    public String sendChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                               Boolean copiaInformes, Boolean incluyeMatricula) throws MessagingException {

        var chequeraSerie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        var domicilio = chequeraSerie.getDomicilio();

        String filenameChequera = formulariosToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId, alternativaId, false);

        // Definir el contenido del correo en formato HTML
        String data = "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='text-align: center;'>" +
                "<img src='cid:logoImage' style='width: 200px;' alt='Logo Universidad de Mendoza'/>" +
                "</div>" +
                "<p>Estimad@ Estudiante,</p>" +
                "<p>Le enviamos como archivo adjunto su chequera. También hemos adjuntado información sobre los medios de pago disponibles.</p>" +
                "<p>Atentamente,</p>" +
                "<p><strong>Universidad de Mendoza</strong></p>" +
                "<hr>" +
                "<p style='font-size: 12px; color: #888;'>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</p>" +
                "</body>" +
                "</html>";

        // Crear el mensaje de correo
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        List<String> addresses = new ArrayList<>();

        if (!domicilio.getEmailPersonal().isEmpty()) {
            addresses.add(domicilio.getEmailPersonal());
        }

        if (copiaInformes) {
            addresses.add("informes@etec.um.edu.ar");
        } else {
            if (!domicilio.getEmailInstitucional().isEmpty()) {
                addresses.add(domicilio.getEmailInstitucional());
            }
        }

        helper.setTo(addresses.toArray(new String[0]));
        helper.setSubject("Envío Automático de Chequera -> " + filenameChequera);
        helper.setReplyTo("no-reply@um.edu.ar");
        helper.setText(data, true); // Permitir el envío de contenido HTML

        // Adjuntar el archivo de chequera
        FileSystemResource fileChequera = new FileSystemResource(filenameChequera);
        helper.addAttachment(filenameChequera, fileChequera);

        // Adjuntar el archivo de medios
        String filenameMedios = "medios.pdf";
        FileSystemResource fileMedios = new FileSystemResource(filenameMedios);
        helper.addAttachment(filenameMedios, fileMedios);

        // Adjuntar el logo en línea
        var filenameLogo = "marca_um.png"; // Ruta al logo de la UM
        if (facultadId == 15) {
            filenameLogo = "marca_etec.png"; // Ruta al logo de ETEC si aplica
        }

        FileSystemResource logo = new FileSystemResource(filenameLogo);
        helper.addInline("logoImage", logo); // Usar un CID explícito

        // Enviar el mensaje
        javaMailSender.send(message);

        return "Envío de Chequera Ok!!!";
    }

    public String sendCuota(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId, Integer productoId, Integer cuotaId,
                            Boolean copiaInformes, Boolean incluyeMatricula) throws MessagingException {
        return "";
    }

}
