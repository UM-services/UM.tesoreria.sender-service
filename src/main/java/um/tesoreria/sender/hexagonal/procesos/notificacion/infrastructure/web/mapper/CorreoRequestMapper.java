package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.model.CorreoPayload;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto.CorreoRequest;

@Component
public class CorreoRequestMapper {

    public CorreoPayload toPayload(CorreoRequest request) {
        return CorreoPayload.builder()
                .remitente(request.getRemitente())
                .para(request.getPara())
                .replyTo(request.getReplyTo())
                .asunto(request.getAsunto())
                .cuerpo(request.getCuerpo())
                .html(request.isHtml())
                .build();
    }

}
