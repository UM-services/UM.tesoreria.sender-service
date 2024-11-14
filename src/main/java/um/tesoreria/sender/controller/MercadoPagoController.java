package um.tesoreria.sender.controller;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.service.MercadoPagoService;

@RestController
@RequestMapping("/api/tesoreria/sender/mercadopago")
public class MercadoPagoController {

    private final MercadoPagoService service;

    public MercadoPagoController(MercadoPagoService service) {
        this.service = service;
    }

    @GetMapping("/cuota/{chequeraCuotaId}")
    public ResponseEntity<String> sendCuota(@PathVariable Long chequeraCuotaId) throws MessagingException {
        return ResponseEntity.ok(service.sendCuota(chequeraCuotaId));
    }

}
