package um.tesoreria.sender.hexagonal.gateways.correo.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.gateways.correo.application.exception.CorreoException;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.AdjuntoCorreo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.ImagenEnLinea;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.ports.out.CorreoEnviador;

@Component
@RequiredArgsConstructor
@Slf4j
public class JavaMailCorreoEnviadorAdapter implements CorreoEnviador {

    private final JavaMailSender javaMailSender;

    @Override
    public void enviar(Correo correo) {
        log.debug("Enviando correo a {} - asunto: {}", correo.getPara(), correo.getAsunto());
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (correo.getFrom() != null && !correo.getFrom().isEmpty()) {
                helper.setFrom(correo.getFrom());
            }
            helper.setTo(correo.getPara().toArray(new String[0]));
            if (correo.getReplyTo() != null && !correo.getReplyTo().isEmpty()) {
                helper.setReplyTo(correo.getReplyTo());
            }
            helper.setSubject(correo.getAsunto());
            helper.setText(correo.getCuerpo(), correo.isHtml());
            for (AdjuntoCorreo adjunto : correo.getAdjuntos()) {
                helper.addAttachment(adjunto.nombre(), new FileSystemResource(adjunto.ruta()));
            }
            for (ImagenEnLinea imagen : correo.getImagenesEnLinea()) {
                helper.addInline(imagen.cid(), new FileSystemResource(imagen.ruta()));
            }
        } catch (MessagingException e) {
            throw new CorreoException(CorreoException.Tipo.MENSAJE, e.getMessage(), e);
        }

        try {
            javaMailSender.send(message);
        } catch (MailSendException e) {
            throw new CorreoException(CorreoException.Tipo.DIRECCION_INVALIDA, e.getMessage(), e);
        }
        log.debug("Correo enviado");
    }

}
