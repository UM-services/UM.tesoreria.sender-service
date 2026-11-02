package um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.hexagonal.procesos.notificacion.domain.ports.in.EnviarNotificacionUseCase;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto.CorreoRequest;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.dto.CorreoResponse;
import um.tesoreria.sender.hexagonal.procesos.notificacion.infrastructure.web.mapper.CorreoRequestMapper;

/**
 * Punto de entrada genérico para correos sin documento adjunto. Los flujos con PDF (chequera,
 * recibo) tienen sus propios endpoints porque el sender debe generarlos.
 */
@RestController
@RequestMapping("/api/tesoreria/sender/correo")
@RequiredArgsConstructor
@Slf4j
public class CorreoController {

    private final EnviarNotificacionUseCase enviarCorreoUseCase;
    private final CorreoRequestMapper correoRequestMapper;

    @PostMapping("/enviar")
    public ResponseEntity<CorreoResponse> enviar(@Valid @RequestBody CorreoRequest request) {
        enviarCorreoUseCase.enviar(correoRequestMapper.toPayload(request));
        return ResponseEntity.ok(new CorreoResponse("Correo enviado"));
    }

}
