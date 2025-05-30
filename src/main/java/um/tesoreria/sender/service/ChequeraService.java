package um.tesoreria.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraMessageCheckClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraSerieClient;
import um.tesoreria.sender.client.tesoreria.core.PersonaClient;
import um.tesoreria.sender.client.tesoreria.core.facade.ToolClient;
import um.tesoreria.sender.client.tesoreria.mercadopago.ChequeraClient;
import um.tesoreria.sender.domain.dto.UMPreferenceMPDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraMessageCheckDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.ChequeraSerieDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.DomicilioDto;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ChequeraService {

    private final ChequeraMessageCheckClient chequeraMessageCheckClient;
    private final PersonaClient personaClient;
    private final ToolClient toolClient;
    @Value("${app.testing}")
    private Boolean testing;

    private final FormulariosToPdfService formulariosToPdfService;
    private final JavaMailSender javaMailSender;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraClient chequeraClient;

    public ChequeraService(FormulariosToPdfService formulariosToPdfService, JavaMailSender javaMailSender, ChequeraSerieClient chequeraSerieClient, ChequeraClient chequeraClient, ChequeraMessageCheckClient chequeraMessageCheckClient, PersonaClient personaClient, ToolClient toolClient) {
        this.formulariosToPdfService = formulariosToPdfService;
        this.javaMailSender = javaMailSender;
        this.chequeraSerieClient = chequeraSerieClient;
        this.chequeraClient = chequeraClient;
        this.chequeraMessageCheckClient = chequeraMessageCheckClient;
        this.personaClient = personaClient;
        this.toolClient = toolClient;
    }

    public String sendChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                               Boolean copiaInformes, Boolean codigoBarras, Boolean incluyeMatricula) throws MessagingException {
        log.debug("Sending chequera for facultadId: {}, tipoChequeraId: {}, chequeraSerieId: {}, alternativaId: {}, copiaInformes: {}, codigoBarras: {}, incluyeMatricula: {}", facultadId, tipoChequeraId, chequeraSerieId, alternativaId, copiaInformes, codigoBarras, incluyeMatricula);

        var chequeraSerie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        var tipoChequera = chequeraSerie.getTipoChequera();
        logChequeraSerie(chequeraSerie);

        var domicilio = chequeraSerie.getDomicilio();
        logDomicilio(domicilio);

        var inscripcionFull = personaClient.findInscripcionFull(facultadId, chequeraSerie.getPersonaId(), chequeraSerie.getDocumentoId(), chequeraSerie.getLectivoId());

        var preferences = chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
        logPreferences(preferences);

        String filenameChequera = formulariosToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId, alternativaId, codigoBarras, false, preferences);
        log.debug("ChequeraService.sendChequera - filenameChequera -> {}", filenameChequera);
        if (filenameChequera.isEmpty()) {
            log.debug("Sin CUOTAS para ENVIAR");
            return "ERROR: Sin CUOTAS para ENVIAR";
        }
        // Obtener el nombre del alumno
        String nombreAlumno = Objects.requireNonNull(chequeraSerie.getPersona()).getApellidoNombre();

        // Crear tabla con los detalles
        // Código HTML para el cuerpo del correo electrónico
        StringBuilder data = new StringBuilder("<html>" +
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
                "<table class='details-table'>" +
                "<tr>" +
                "<th>Producto</th>" +
                "<th>Período</th>" +
                "<th>Fecha de Vencimiento</th>" +
                "<th>Importe</th>" +
                "<th>Enlace</th>" +
                "</tr>");
        for (var umPreferenceMPDto : preferences) {
            var chequeraCuota = umPreferenceMPDto.getChequeraCuota();
            if (chequeraCuota.getPagado() == 0 && chequeraCuota.getBaja() == 0
                    && chequeraCuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                if (umPreferenceMPDto.getMercadoPagoContext() != null) {
                    // Formatear la fecha de vencimiento
                    String fechaVencimientoFormatted = umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    data.append("<tr>").append("<td>").append(Objects.requireNonNull(chequeraCuota.getProducto()).getNombre()).append("</td>").append("<td>").append(chequeraCuota.getMes()).append("/").append(chequeraCuota.getAnho()).append("</td>").append("<td>").append(fechaVencimientoFormatted).append("</td>").append("<td>$").append(String.format("%.2f", umPreferenceMPDto.getMercadoPagoContext().getImporte())).append("</td>").append("<td><a href='").append(umPreferenceMPDto.getMercadoPagoContext().getInitPoint()).append("' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>").append("</tr>");
                }
            }
        }
        data.append("</table>").append("<div class='info-section'>").append("<div class='info-details'>").append("<p>Unidad Académica: <strong>").append(Objects.requireNonNull(chequeraSerie.getFacultad()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Chequera: <strong>").append(Objects.requireNonNull(chequeraSerie.getTipoChequera()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Sede: <strong>").append(Objects.requireNonNull(chequeraSerie.getGeografica()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Ciclo Lectivo: <strong>").append(Objects.requireNonNull(chequeraSerie.getLectivo()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Arancel: <strong>").append(Objects.requireNonNull(chequeraSerie.getArancelTipo()).getDescripcion()).append("</strong></p>").append("<br/>").append("<p>Alternativa: <strong>").append(chequeraSerie.getAlternativaId()).append("</strong></p>").append("</div>").append("<div class='info-logo'>").append("<img src='cid:logoImage' alt='Logo'/>").append("</div>").append("</div>").append("</div>").append("<div class='footer'>").append("<p>Hasta pronto, seguimos en contacto</p>").append("<p>Si ya has cancelado tu cuota desestima este recordatorio. </p>").append("<br/>").append("<p>Universidad de Mendoza</p>").append("<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>").append("</div>").append("</div>").append("<div class='container'>").append("<div class='content'>").append("<img src='cid:medioPago1' alt='Medio de Pago 1' style='width: 100%; height: auto; margin-bottom: 20px;'/>").append("<img src='cid:medioPago2' alt='Medio de Pago 2' style='width: 100%; height: auto;'/>").append("</div>").append("</div>").append("</body>").append("</html>");

        // Crear el mensaje de correo
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        List<String> addresses = new ArrayList<>();

        if (!testing) {
            assert domicilio != null;
            if (!domicilio.getEmailPersonal().isEmpty()) {
                if (toolClient.mailValidate(List.of(domicilio.getEmailPersonal()))) {
                    addresses.add(domicilio.getEmailPersonal());
                    log.debug("adding personal email -> {}", domicilio.getEmailPersonal());
                }
            }
        }

        if (!testing) {
            if (copiaInformes) {
                assert tipoChequera != null;
                String emailCopia = tipoChequera.getEmailCopia();
                if (emailCopia != null && !emailCopia.isEmpty()) {
                    if (toolClient.mailValidate(List.of(emailCopia))) {
                        addresses.add(emailCopia);
                        log.debug("adding informes email -> {}", emailCopia);
                    }
                }
            }
            if (!domicilio.getEmailInstitucional().isEmpty()) {
                if (toolClient.mailValidate(List.of(domicilio.getEmailInstitucional()))) {
                    addresses.add(domicilio.getEmailInstitucional());
                    log.debug("adding institucional email -> {}", domicilio.getEmailInstitucional());
                }
            }
            var domicilioPago = inscripcionFull.getDomicilioPago();
            if (domicilioPago != null) {
                domicilioPago.getEmailPersonal();
                if (!domicilioPago.getEmailPersonal().isEmpty()) {
                    if (toolClient.mailValidate(List.of(domicilioPago.getEmailPersonal()))) {
                        addresses.add(domicilioPago.getEmailPersonal());
                        log.debug("adding pago personal email -> {}", domicilioPago.getEmailPersonal());
                    }
                }
                domicilioPago.getEmailInstitucional();
                if (!domicilioPago.getEmailInstitucional().isEmpty()) {
                    if (toolClient.mailValidate(List.of(domicilioPago.getEmailInstitucional()))) {
                        addresses.add(domicilioPago.getEmailInstitucional());
                        log.debug("adding pago institucional email -> {}", domicilioPago.getEmailInstitucional());
                    }
                }
            }
        }

        if (testing) {
            log.debug("Testing -> daniel.quinterospinto@gmail.com");
            addresses.add("daniel.quinterospinto@gmail.com");
        }

        helper.setTo(addresses.toArray(new String[0]));
        helper.setSubject("Recordatorio - Pago cuota UM -> " + nombreAlumno);
        helper.setReplyTo("no-reply@um.edu.ar");
        helper.setText(data.toString(), true); // Permitir el envío de contenido HTML

        // Adjuntar el archivo de chequera
        FileSystemResource fileChequera = new FileSystemResource(filenameChequera);
        helper.addAttachment(filenameChequera, fileChequera);

        // Adjuntar el logo en línea
        var filenameLogo = "marca_um_65.png"; // Ruta al logo de la UM
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
        log.debug("Enviando el mensaje");
        javaMailSender.send(message);
        log.debug("Mensaje enviado");

        // Marcar como enviado
        log.debug("Marcando como enviado");
        chequeraSerie = chequeraSerieClient.markSent(facultadId, tipoChequeraId, chequeraSerieId);
        logChequeraSerie(chequeraSerie);
        var chequeraMessageCheck = new ChequeraMessageCheckDto.Builder()
                .chequeraMessageCheckId(UUID.randomUUID())
                .facultadId(chequeraSerie.getFacultadId())
                .tipoChequeraId(chequeraSerie.getTipoChequeraId())
                .chequeraSerieId(chequeraSerie.getChequeraSerieId())
                .build();
        chequeraMessageCheck = chequeraMessageCheckClient.add(chequeraMessageCheck);
        logChequeraMessageCheck(chequeraMessageCheck);
        log.debug("Envío de Chequera Ok!!!");
        return "Envío de Chequera Ok!!!";
    }

    private void logChequeraMessageCheck(ChequeraMessageCheckDto chequeraMessageCheck) {
        try {
            log.debug("ChequeraMessageCheck -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraMessageCheck));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraMessageCheck jsonify error -> {}", e.getMessage());
        }
    }

    private void logPreferences(List<UMPreferenceMPDto> preferences) {
        try {
            log.debug("Preferences -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(preferences));
        } catch (JsonProcessingException e) {
            log.debug("Preferences jsonify error -> {}", e.getMessage());
        }
    }

    private void logDomicilio(DomicilioDto domicilio) {
        try {
            log.debug("Domicilio -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(domicilio));
        } catch (JsonProcessingException e) {
            log.debug("Domicilio jsonify error -> {}", e.getMessage());
        }
    }

    private void logChequeraSerie(ChequeraSerieDto chequeraSerie) {
        try {
            log.debug("ChequeraSerie -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraSerie));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraSerie jsonify error -> {}", e.getMessage());
        }
    }

}
