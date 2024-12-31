package um.tesoreria.sender.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraSerieClient;
import um.tesoreria.sender.client.tesoreria.core.MercadoPagoContextClient;
import um.tesoreria.sender.client.tesoreria.mercadopago.ChequeraClient;
import um.tesoreria.sender.client.tesoreria.mercadopago.PreferenceClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraCuotaDto;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChequeraService {

    private final FormulariosToPdfService formulariosToPdfService;
    private final JavaMailSender javaMailSender;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraClient chequeraClient;

    public ChequeraService(FormulariosToPdfService formulariosToPdfService, JavaMailSender javaMailSender, ChequeraSerieClient chequeraSerieClient, ChequeraClient chequeraClient) {
        this.formulariosToPdfService = formulariosToPdfService;
        this.javaMailSender = javaMailSender;
        this.chequeraSerieClient = chequeraSerieClient;
        this.chequeraClient = chequeraClient;
    }

    public String sendChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                               Boolean copiaInformes, Boolean codigoBarras, Boolean incluyeMatricula) throws MessagingException {

        var chequeraSerie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        var domicilio = chequeraSerie.getDomicilio();

        var preferences = chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);

        String filenameChequera = formulariosToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId, alternativaId, codigoBarras, false, preferences);
        // Obtener el nombre del alumno
        String nombreAlumno = chequeraSerie.getPersona().getApellidoNombre();

        // Crear tabla con los detalles
        // Código HTML para el cuerpo del correo electrónico
        String data = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; margin: 0; }" +
                ".container { background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 600px; margin: auto; }" +
                ".header { background-color: #007BFF; color: white; padding: 20px; text-align: center; position: relative; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 20px; }" +
                ".info-section { background-color: #f4f4f4; padding: 15px; margin-bottom: 20px; border-radius: 8px; display: flex; flex-wrap: wrap; }" +
                ".info-details { flex: 1; min-width: 60%; }" +
                ".info-logo { flex: 0 0 150px; margin-top: 85px; text-align: right; }" +
                ".info-logo img { width: 150px; height: auto; }" +
                ".details-table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                ".details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }" +
                ".details-table th { background-color: #007BFF; color: white; }" +
                ".footer { background-color: #e9ecef; padding: 10px; text-align: center; font-size: 12px; color: #333; }" +
                ".qr-section { text-align: center; margin-top: 20px; }" +
                "@media only screen and (max-width: 600px) {" +
                "    .container { width: 100%; padding: 0; }" +
                "    .header h1 { font-size: 20px; }" +
                "    .content, .info-section { padding: 10px; }" +
                "    .details-table th, .details-table td { font-size: 14px; padding: 8px; }" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Universidad de Mendoza te envió un enlace de pago</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p><strong>Estimad@ " + nombreAlumno + ",</strong></p>" +
                "<p>Para agilizar tu gestión de pago te acercamos los enlaces de las próximas cuotas a vencer:</p>" +
                "<p><strong>¡Que tengas un excelente 2025 y que concretes todos tus proyectos!</strong></p>" +
                "<table class='details-table'>" +
                "<tr>" +
                "<th>Producto</th>" +
                "<th>Período</th>" +
                "<th>Fecha de Vencimiento</th>" +
                "<th>Importe</th>" +
                "<th>Enlace</th>" +
                "</tr>";
        for (var umPreferenceMPDto : preferences) {
            var chequeraCuota = umPreferenceMPDto.getChequeraCuota();
            if (chequeraCuota.getPagado() == 0 && chequeraCuota.getBaja() == 0
                    && chequeraCuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                if (umPreferenceMPDto.getMercadoPagoContext() != null) {
                    // Formatear la fecha de vencimiento
                    String fechaVencimientoFormatted = umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    data = data + "<tr>" +
                            "<td>" + chequeraCuota.getProducto().getNombre() + "</td>" +
                            "<td>" + chequeraCuota.getMes() + "/" + chequeraCuota.getAnho() + "</td>" +
                            "<td>" + fechaVencimientoFormatted + "</td>" +
                            "<td>$" + String.format("%.2f", umPreferenceMPDto.getMercadoPagoContext().getImporte()) + "</td>" +
                            "<td><a href='" + umPreferenceMPDto.getMercadoPagoContext().getInitPoint() + "' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>" +
                            "</tr>";
                }
            }
        }
        data = data + "</table>" +
                "<div class='info-section'>" +
                    "<div class='info-details'>" +
                        "<p>Unidad Académica: <strong>" + chequeraSerie.getFacultad().getNombre() + "</strong></p>" +
                        "<br/>" +
                        "<p>Tipo de Chequera: <strong>" + chequeraSerie.getTipoChequera().getNombre() + "</strong></p>" +
                        "<br/>" +
                        "<p>Sede: <strong>" + chequeraSerie.getGeografica().getNombre() + "</strong></p>" +
                        "<br/>" +
                        "<p>Ciclo Lectivo: <strong>" + chequeraSerie.getLectivo().getNombre() + "</strong></p>" +
                        "<br/>" +
                        "<p>Tipo de Arancel: <strong>" + chequeraSerie.getArancelTipo().getDescripcion() + "</strong></p>" +
                        "<br/>" +
                        "<p>Alternativa: <strong>" + chequeraSerie.getAlternativaId() + "</strong></p>" +
                    "</div>" +
                    "<div class='info-logo'>" +
                        "<img src='cid:logoImage' alt='Logo'/>" +
                    "</div>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Hasta pronto, seguimos en contacto</p>" +
                "<p>Si ya has cancelado tu cuota desestima este recordatorio. </p>" +
                "<br/>" +
                "<p>Universidad de Mendoza</p>" +
                "<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>" +
                "</div>" +
                "</div>" +
                "<div class='container'>" +
                "<div class='content'>" +
                    "<img src='cid:medioPago1' alt='Medio de Pago 1' style='width: 100%; height: auto; margin-bottom: 20px;'/>" +
                    "<img src='cid:medioPago2' alt='Medio de Pago 2' style='width: 100%; height: auto;'/>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Crear el mensaje de correo
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        List<String> addresses = new ArrayList<>();

        if (!domicilio.getEmailPersonal().isEmpty()) {
            addresses.add(domicilio.getEmailPersonal());
            log.debug("adding personal email -> {}", domicilio.getEmailPersonal());
        }

        if (copiaInformes) {
            addresses.add("informes@etec.um.edu.ar");
            log.debug("adding informes email -> {}", "informes@etec.um.edu.ar");
        } else {
            if (!domicilio.getEmailInstitucional().isEmpty()) {
                addresses.add(domicilio.getEmailInstitucional());
                log.debug("adding institutional email -> {}", domicilio.getEmailInstitucional());
            }
        }

        helper.setTo(addresses.toArray(new String[0]));
        helper.setSubject("Recordatorio - Pago cuota UM -> " + nombreAlumno);
        helper.setReplyTo("no-reply@um.edu.ar");
        helper.setText(data, true); // Permitir el envío de contenido HTML

        // Adjuntar el archivo de chequera
        FileSystemResource fileChequera = new FileSystemResource(filenameChequera);
        helper.addAttachment(filenameChequera, fileChequera);

        // Adjuntar el logo en línea
        var filenameLogo = "marca_um.png"; // Ruta al logo de la UM
        if (facultadId == 15) {
            filenameLogo = "marca_etec.png"; // Ruta al logo de ETEC si aplica
        }

        FileSystemResource logo = new FileSystemResource(filenameLogo);
        helper.addInline("logoImage", logo); // Usar un CID explícito

        // Adjuntar el archivo de medios de pago 1
        String filenameMedios1 = "medio_pago_1.png";
        FileSystemResource fileMedios1 = new FileSystemResource(filenameMedios1);
        helper.addInline("medioPago1", fileMedios1);

        // Adjuntar el archivo de medios de pago 1
        String filenameMedios2 = "medio_pago_2.png";
        FileSystemResource fileMedios2 = new FileSystemResource(filenameMedios2);
        helper.addInline("medioPago2", fileMedios2);

        // Enviar el mensaje
        javaMailSender.send(message);

        return "Envío de Chequera Ok!!!";
    }

}
