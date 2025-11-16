package um.tesoreria.sender.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraMessageCheckClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraSerieClient;
import um.tesoreria.sender.client.tesoreria.core.PersonaClient;
import um.tesoreria.sender.client.tesoreria.core.facade.ToolClient;
import um.tesoreria.sender.client.tesoreria.mercadopago.ChequeraClient;
import um.tesoreria.sender.domain.dto.InscripcionFullDto;
import um.tesoreria.sender.domain.dto.UMPreferenceMPDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;
import um.tesoreria.sender.util.Jsonifier;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChequeraService {

    private final ChequeraMessageCheckClient chequeraMessageCheckClient;
    private final PersonaClient personaClient;
    private final ToolClient toolClient;
    private final ChequeraCuotaClient chequeraCuotaClient;

    @Value("${app.testing}")
    private Boolean testing;

    private final FormulariosToPdfService formulariosToPdfService;
    private final JavaMailSender javaMailSender;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraClient chequeraClient;

    public String sendChequera(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId,
                               Boolean copiaInformes, Boolean codigoBarras, Boolean incluyeMatricula) throws MessagingException {
        log.debug("Sending chequera for facultadId: {}, tipoChequeraId: {}, chequeraSerieId: {}, alternativaId: {}, copiaInformes: {}, codigoBarras: {}, incluyeMatricula: {}", facultadId, tipoChequeraId, chequeraSerieId, alternativaId, copiaInformes, codigoBarras, incluyeMatricula);

        var chequeraSerie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        var inscripcionFull = personaClient.findInscripcionFull(facultadId, chequeraSerie.getPersonaId(), chequeraSerie.getDocumentoId(), chequeraSerie.getLectivoId());
        var preferences = chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);

        String filenameChequera = formulariosToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId, alternativaId, codigoBarras, false, preferences);
        log.debug("ChequeraService.sendChequera - filenameChequera -> {}", filenameChequera);
        if (filenameChequera.isEmpty()) {
            return "\n\nERROR: Sin CUOTAS para ENVIAR\n\n";
        }

        String nombreAlumno = Objects.requireNonNull(chequeraSerie.getPersona()).getApellidoNombre();

        String emailBodyChequera = buildEmailBodyChequera(nombreAlumno, preferences, chequeraSerie);

        List<String> recipients = collectRecipients(chequeraSerie, inscripcionFull, copiaInformes);

        sendMessage(recipients, nombreAlumno, emailBodyChequera, filenameChequera, facultadId);

        markAsSent(facultadId, tipoChequeraId, chequeraSerieId);

        return "\n\nEnvío de Chequera Ok!!!\n\n";
    }

    public String sendCuota(Long chequeraCuotaId) throws MessagingException {
        log.debug("Sending cuota {}", chequeraCuotaId);

        var chequeraCuota = chequeraCuotaClient.findByChequeraCuotaId(chequeraCuotaId);
        var chequeraSerie = chequeraCuota.getChequeraSerie();
        assert chequeraSerie != null;
        var inscripcionFull = personaClient.findInscripcionFull(chequeraSerie.getFacultadId(), chequeraSerie.getPersonaId(), chequeraSerie.getDocumentoId(), chequeraSerie.getLectivoId());
        var preferences = chequeraClient.createChequeraContext(chequeraSerie.getFacultadId(), chequeraSerie.getTipoChequeraId(), chequeraSerie.getChequeraSerieId(), chequeraSerie.getAlternativaId());

        String nombreAlumno = Objects.requireNonNull(chequeraSerie.getPersona()).getApellidoNombre();

        String emailBodyCuota = buildEmailBodyCuota(nombreAlumno, preferences, chequeraCuota);

        List<String> recipients = collectRecipients(chequeraSerie, inscripcionFull, false);

        sendMessage(recipients, nombreAlumno, emailBodyCuota, null, chequeraCuota.getFacultadId());

        return "\n\nEnvío de Cuota Ok!!!\n\n";
    }

    private String buildEmailBodyChequera(String nombreAlumno, List<UMPreferenceMPDto> preferences, ChequeraSerieDto chequeraSerie) {
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
            if (chequeraCuota.getPagado() == 0 && chequeraCuota.getBaja() == 0 && chequeraCuota.getCompensada() == 0
                    && chequeraCuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                if (umPreferenceMPDto.getMercadoPagoContext() != null) {
                    String fechaVencimientoFormatted = umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    data.append("<tr>").append("<td>").append(Objects.requireNonNull(chequeraCuota.getProducto()).getNombre()).append("</td>").append("<td>").append(chequeraCuota.getMes()).append("/").append(chequeraCuota.getAnho()).append("</td>").append("<td>").append(fechaVencimientoFormatted).append("</td>").append("<td>$").append(String.format("%.2f", umPreferenceMPDto.getMercadoPagoContext().getImporte())).append("</td>").append("<td><a href='").append(umPreferenceMPDto.getMercadoPagoContext().getInitPoint()).append("' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>").append("</tr>");
                }
            }
        }
        data.append("</table>").append("<div class='info-section'>").append("<div class='info-details'>").append("<p>Unidad Académica: <strong>").append(Objects.requireNonNull(chequeraSerie.getFacultad()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Chequera: <strong>").append(Objects.requireNonNull(chequeraSerie.getTipoChequera()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Sede: <strong>").append(Objects.requireNonNull(chequeraSerie.getGeografica()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Ciclo Lectivo: <strong>").append(Objects.requireNonNull(chequeraSerie.getLectivo()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Arancel: <strong>").append(Objects.requireNonNull(chequeraSerie.getArancelTipo()).getDescripcion()).append("</strong></p>").append("<br/>").append("<p>Alternativa: <strong>").append(chequeraSerie.getAlternativaId()).append("</strong></p>").append("</div>").append("<div class='info-logo'>").append("<img src='cid:logoImage' alt='Logo'/>").append("</div>").append("</div>").append("</div>").append("<div class='footer'>").append("<p>Hasta pronto, seguimos en contacto</p>").append("<p>Si ya has cancelado tu cuota desestima este recordatorio. </p>").append("<br/>").append("<p>Universidad de Mendoza</p>").append("<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>").append("</div>").append("</div>").append("<div class='container'>").append("<div class='content'>").append("<img src='cid:medioPago1' alt='Medio de Pago 1' style='width: 100%; height: auto; margin-bottom: 20px;'/>").append("<img src='cid:medioPago2' alt='Medio de Pago 2' style='width: 100%; height: auto;'/>").append("</div>").append("</div>").append("</body>").append("</html>");
        return data.toString();
    }

    private String buildEmailBodyCuota(String nombreAlumno, List<UMPreferenceMPDto> preferences, ChequeraCuotaDto chequeraCuota) {
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
                "<p>Para agilizar tu gestión de pago te acercamos el enlace de la cuota a pagar:</p>" +
                "<table class='details-table'>" +
                "<tr>" +
                "<th>Producto</th>" +
                "<th>Período</th>" +
                "<th>Fecha de Vencimiento</th>" +
                "<th>Importe</th>" +
                "<th>Enlace</th>" +
                "</tr>");
        for (var umPreferenceMPDto : preferences) {
            if (Objects.equals(chequeraCuota.getChequeraCuotaId(), umPreferenceMPDto.getChequeraCuota().getChequeraCuotaId())) {
                if (chequeraCuota.getPagado() == 0 && chequeraCuota.getBaja() == 0 && chequeraCuota.getCompensada() == 0
                        && chequeraCuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                    if (umPreferenceMPDto.getMercadoPagoContext() != null) {
                        String fechaVencimientoFormatted = umPreferenceMPDto.getMercadoPagoContext().getFechaVencimiento()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                        data.append("<tr>").append("<td>").append(Objects.requireNonNull(chequeraCuota.getProducto()).getNombre()).append("</td>").append("<td>").append(chequeraCuota.getMes()).append("/").append(chequeraCuota.getAnho()).append("</td>").append("<td>").append(fechaVencimientoFormatted).append("</td>").append("<td>$").append(String.format("%.2f", umPreferenceMPDto.getMercadoPagoContext().getImporte())).append("</td>").append("<td><a href='").append(umPreferenceMPDto.getMercadoPagoContext().getInitPoint()).append("' style='color: #007BFF; font-weight: bold;'>Click aquí para realizar el pago</a></td>").append("</tr>");
                    }
                }
            }
        }
        data.append("</table>").append("<div class='info-section'>").append("<div class='info-details'>").append("<p>Unidad Académica: <strong>").append(Objects.requireNonNull(chequeraCuota.getFacultad()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Chequera: <strong>").append(Objects.requireNonNull(chequeraCuota.getTipoChequera()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Sede: <strong>").append(Objects.requireNonNull(Objects.requireNonNull(chequeraCuota.getChequeraSerie()).getGeografica()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Ciclo Lectivo: <strong>").append(Objects.requireNonNull(chequeraCuota.getChequeraSerie().getLectivo()).getNombre()).append("</strong></p>").append("<br/>").append("<p>Tipo de Arancel: <strong>").append(Objects.requireNonNull(chequeraCuota.getChequeraSerie().getArancelTipo()).getDescripcion()).append("</strong></p>").append("<br/>").append("<p>Alternativa: <strong>").append(chequeraCuota.getChequeraSerie().getAlternativaId()).append("</strong></p>").append("</div>").append("<div class='info-logo'>").append("<img src='cid:logoImage' alt='Logo'/>").append("</div>").append("</div>").append("</div>").append("<div class='footer'>").append("<p>Hasta pronto, seguimos en contacto</p>").append("<p>Si ya has cancelado tu cuota desestima este recordatorio. </p>").append("<br/>").append("<p>Universidad de Mendoza</p>").append("<p><small>Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída.</small></p>").append("</div>").append("</div>").append("<div class='container'>").append("<div class='content'>").append("<img src='cid:medioPago1' alt='Medio de Pago 1' style='width: 100%; height: auto; margin-bottom: 20px;'/>").append("<img src='cid:medioPago2' alt='Medio de Pago 2' style='width: 100%; height: auto;'/>").append("</div>").append("</div>").append("</body>").append("</html>");
        return data.toString();
    }

    private List<String> collectRecipients(ChequeraSerieDto chequeraSerie, InscripcionFullDto inscripcionFull, Boolean copiaInformes) {
        log.debug("Processing ChequeraService.collectRecipients");
        List<String> addresses = new ArrayList<>();
        if (testing) {
            log.debug("Testing -> daniel.quinterospinto@gmail.com");
            addresses.add("daniel.quinterospinto@gmail.com");
            return addresses;
        }

        DomicilioDto domicilio = chequeraSerie.getDomicilio();

        assert domicilio != null;
        addValidEmail(addresses, domicilio.getEmailPersonal(), "personal");
        addValidEmail(addresses, domicilio.getEmailInstitucional(), "institucional");

        if (copiaInformes) {
            var tipoChequera = chequeraSerie.getTipoChequera();
            assert tipoChequera != null;
            addValidEmail(addresses, tipoChequera.getEmailCopia(), "informes");
        }

        if (inscripcionFull != null) {
            var domicilioPago = inscripcionFull.getDomicilioPago();
            if (domicilioPago != null) {
                if (domicilioPago.getPersonaId() != null) {
                    addValidEmail(addresses, domicilioPago.getEmailPersonal(), "pago personal");
                    addValidEmail(addresses, domicilioPago.getEmailInstitucional(), "pago institucional");
                }
            }
        }
        log.info("Addresses: {}", Jsonifier.builder(addresses).build());
        return addresses;
    }

    private void addValidEmail(List<String> addresses, String email, String type) {
        if (email != null && !email.isEmpty() && toolClient.mailValidate(List.of(email))) {
            addresses.add(email);
            log.debug("adding {} email -> {}", type, email);
        }
    }

    private void sendMessage(List<String> recipients, String nombreAlumno, String emailBody, String filenameChequera, Integer facultadId) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject("Recordatorio - Pago cuota UM -> " + nombreAlumno);
        helper.setReplyTo("no-reply@um.edu.ar");
        helper.setText(emailBody, true);

        if (filenameChequera != null) {
            FileSystemResource fileChequera = new FileSystemResource(filenameChequera);
            helper.addAttachment(filenameChequera, fileChequera);
        }

        String filenameLogo = (facultadId == 15) ? "marca_etec.png" : "marca_um_65.png";
        FileSystemResource logo = new FileSystemResource(filenameLogo);
        helper.addInline("logoImage", logo);

        FileSystemResource fileMedios1 = new FileSystemResource("medio_pago_1.png");
        helper.addInline("medioPago1", fileMedios1);

        FileSystemResource fileMedios2 = new FileSystemResource("medio_pago_2.png");
        helper.addInline("medioPago2", fileMedios2);

        log.debug("Enviando el mensaje");
        javaMailSender.send(message);
        log.debug("Mensaje enviado");
    }

    private void markAsSent(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        log.debug("Marcando como enviado");
        var chequeraSerie = chequeraSerieClient.markSent(facultadId, tipoChequeraId, chequeraSerieId);
        var chequeraMessageCheck = new ChequeraMessageCheckDto.Builder()
                .chequeraMessageCheckId(UUID.randomUUID())
                .facultadId(chequeraSerie.getFacultadId())
                .tipoChequeraId(chequeraSerie.getTipoChequeraId())
                .chequeraSerieId(chequeraSerie.getChequeraSerieId())
                .build();
        chequeraMessageCheck = chequeraMessageCheckClient.add(chequeraMessageCheck);
    }

}
