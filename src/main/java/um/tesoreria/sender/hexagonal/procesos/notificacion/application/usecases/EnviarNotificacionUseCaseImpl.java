package um.tesoreria.sender.hexagonal.procesos.notificacion.application.usecases;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.gateways.correo.application.service.CorreoService;
import um.tesoreria.sender.hexagonal.gateways.correo.domain.model.Correo;
import um.tesoreria.sender.hexagonal.procesos.notificacion.application.exception.NotificacionException;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoPayload;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.Remitente;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in.EnviarNotificacionUseCase;
import um.tesoreria.sender.hexagonal.shared.domain.ports.out.SenderSettings;

/**
 * Envío de un correo genérico: aplica el remitente lógico, el redirect de testing y delega en
 * el gateway único de correo. No maneja adjuntos (eso es responsabilidad de los flujos con
 * documento).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnviarNotificacionUseCaseImpl implements EnviarNotificacionUseCase {

    private static final String TESTING_EMAIL = "daniel.quinterospinto@gmail.com";
    private static final String REPLY_TO_POR_DEFECTO = "no-reply@um.edu.ar";

    private final SenderSettings senderSettings;
    private final CorreoService correoService;

    @Override
    public void enviar(CorreoPayload payload) {
        validar(payload);

        List<String> para = senderSettings.testing()
                ? List.of(TESTING_EMAIL)
                : payload.getPara().stream().filter(this::noVacio).distinct().toList();
        if (para.isEmpty()) {
            throw new NotificacionException(NotificacionException.Tipo.PAYLOAD_INVALIDO,
                    "El correo no tiene destinatarios válidos");
        }

        Correo correo = Correo.builder()
                .from(remitente(payload.getRemitente()))
                .para(para)
                .replyTo(noVacio(payload.getReplyTo()) ? payload.getReplyTo() : REPLY_TO_POR_DEFECTO)
                .asunto(payload.getAsunto())
                .cuerpo(payload.getCuerpo())
                .html(payload.isHtml())
                .build();
        log.debug("Enviando notificación a {} - asunto: {}", para, payload.getAsunto());
        correoService.enviar(correo);
    }

    private void validar(CorreoPayload payload) {
        if (payload == null) {
            throw new NotificacionException(NotificacionException.Tipo.PAYLOAD_INVALIDO, "El payload es obligatorio");
        }
        if (payload.getPara() == null || payload.getPara().isEmpty()) {
            throw new NotificacionException(NotificacionException.Tipo.PAYLOAD_INVALIDO,
                    "El correo no tiene destinatarios");
        }
        if (!noVacio(payload.getAsunto())) {
            throw new NotificacionException(NotificacionException.Tipo.PAYLOAD_INVALIDO, "El asunto es obligatorio");
        }
        if (payload.getCuerpo() == null || payload.getCuerpo().isEmpty()) {
            throw new NotificacionException(NotificacionException.Tipo.PAYLOAD_INVALIDO, "El cuerpo es obligatorio");
        }
    }

    private String remitente(Remitente remitente) {
        Remitente clave = remitente == null ? Remitente.NOTIFICACION : remitente;
        return switch (clave) {
            case CHEQUERA -> senderSettings.fromChequera();
            case RECIBO -> senderSettings.fromRecibo();
            case NOTIFICACION -> senderSettings.fromNotificacion();
        };
    }

    private boolean noVacio(String valor) {
        return valor != null && !valor.isBlank();
    }

}
