package um.tesoreria.sender.controller;

import jakarta.mail.MessagingException;
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
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.service.ChequeraService;
import um.tesoreria.sender.service.FormulariosToPdfService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/tesoreria/sender/chequera")
public class ChequeraController {

    private final ChequeraService service;
    private final FormulariosToPdfService formularioToPdfService;
    private final ChequeraCuotaClient chequeraCuotaClient;

    public ChequeraController(ChequeraService service, FormulariosToPdfService formularioToPdfService, ChequeraCuotaClient chequeraCuotaClient) {
        this.service = service;
        this.formularioToPdfService = formularioToPdfService;
        this.chequeraCuotaClient = chequeraCuotaClient;
    }

    @GetMapping("/generatePdf/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdf(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                                @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId,
                                                @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        String filename = formularioToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, codigoBarras, false);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chequera.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/generatePdf/completa/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdfCompleta(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                                        @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId,
                                                        @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        String filename = formularioToPdfService.generateChequeraPdf(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, codigoBarras, true);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chequera.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/generatePdf/reemplazo/completa/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    public ResponseEntity<Resource> generatePdfReemplazoCompleta(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                                                 @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId) throws FileNotFoundException {
        String filename = formularioToPdfService.generateChequeraReemplazoPdf(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, true);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chequera.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/generateCuotaPdf/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{productoId}/{cuotaId}")
    public ResponseEntity<Resource> generateCuotaPdf(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                                     @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId,
                                                     @PathVariable Integer productoId, @PathVariable Integer cuotaId) throws FileNotFoundException {
        String filename = formularioToPdfService.generateCuotaPdf(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, productoId, cuotaId);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cuota.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/sendChequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{copiaInformes}/{codigoBarras}")
    public ResponseEntity<String> sendChequera(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                               @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId,
                                               @PathVariable Boolean copiaInformes, @PathVariable Boolean codigoBarras) throws MessagingException {
        chequeraCuotaClient.updateBarras(facultadId, tipoChequeraId, chequeraSerieId);
        return new ResponseEntity<>(service.sendChequera(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, copiaInformes, codigoBarras, true), HttpStatus.OK);
    }

}
