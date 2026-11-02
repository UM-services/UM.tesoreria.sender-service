package um.tesoreria.sender.hexagonal.procesos.recibo.infrastructure.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.hexagonal.procesos.recibo.application.service.ReciboService;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@RestController
@RequestMapping("/api/tesoreria/sender/recibo")
@RequiredArgsConstructor
public class ReciboController {

    private final ReciboService service;

    @GetMapping("/pdf/{facturacionElectronicaId}")
    public ResponseEntity<Resource> makePdf(@PathVariable Long facturacionElectronicaId)
            throws FileNotFoundException {
        return responder(service.generarPdf(facturacionElectronicaId), "recibo.pdf");
    }

    @GetMapping("/send/{facturacionElectronicaId}")
    public ResponseEntity<String> send(@PathVariable Long facturacionElectronicaId) {
        service.enviarAsync(facturacionElectronicaId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Proceso de envío iniciado en segundo plano para facturación ID: " + facturacionElectronicaId);
    }

    @GetMapping("/sendNext")
    public ResponseEntity<String> sendNext() {
        return ResponseEntity.ok(service.enviarSiguiente());
    }

    private ResponseEntity<Resource> responder(DocumentoPdf documento, String nombreDescarga)
            throws FileNotFoundException {
        if (documento.estaVacio()) {
            return ResponseEntity.noContent().build();
        }
        File file = new File(documento.ruta());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreDescarga);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

}
