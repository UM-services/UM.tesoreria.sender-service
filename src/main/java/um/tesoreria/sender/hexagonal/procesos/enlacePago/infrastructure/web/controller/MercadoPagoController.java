package um.tesoreria.sender.hexagonal.procesos.enlacePago.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.hexagonal.procesos.enlacePago.application.service.EnlacePagoService;

@RestController
@RequestMapping("/api/tesoreria/sender/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final EnlacePagoService service;

    @GetMapping("/cuota/{chequeraCuotaId}")
    public ResponseEntity<String> sendCuota(@PathVariable Long chequeraCuotaId) {
        return ResponseEntity.ok(service.enviarCuota(chequeraCuotaId));
    }

}
