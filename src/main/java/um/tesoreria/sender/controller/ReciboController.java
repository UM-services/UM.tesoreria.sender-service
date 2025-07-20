package um.tesoreria.sender.controller;

import jakarta.mail.MessagingException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.service.ReciboService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/tesoreria/sender/recibo")
public class ReciboController {

    private final ReciboService service;

    public ReciboController(ReciboService service) {
        this.service = service;
    }

    @GetMapping("/pdf/{facturacionElectronicaId}")
    public ResponseEntity<Resource> makePdf(@PathVariable Long facturacionElectronicaId) throws FileNotFoundException {
        String filename = service.generatePdf(facturacionElectronicaId, null, null);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/send/{facturacionElectronicaId}")
    public ResponseEntity<String> send(@PathVariable Long facturacionElectronicaId) {
        String result = service.send(facturacionElectronicaId, null);
        if (result.startsWith("ERROR:")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/sendNext")
    public ResponseEntity<String> sendNext() {
        return new ResponseEntity<>(service.sendNext(), HttpStatus.OK);
    }

}
