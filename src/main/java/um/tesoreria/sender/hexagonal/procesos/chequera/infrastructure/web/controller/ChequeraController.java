package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.sender.hexagonal.procesos.chequera.application.service.ChequeraService;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudChequeraPdf;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.SolicitudEnvioChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.SolicitudChequeraPublisher;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.SolicitudCuotaPublisher;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

@RestController
@RequestMapping("/api/tesoreria/sender/chequera")
@Slf4j
@RequiredArgsConstructor
public class ChequeraController {

    private final ChequeraService service;
    private final SolicitudChequeraPublisher solicitudChequeraPublisher;
    private final SolicitudCuotaPublisher solicitudCuotaPublisher;

    static final String MENSAJE_ENCOLADO = "Solicitud de envío en proceso";

    @GetMapping("/generatePdf/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdf(@PathVariable Integer facultadId, @PathVariable Integer tipoChequeraId,
                                                @PathVariable Long chequeraSerieId, @PathVariable Integer alternativaId,
                                                @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        log.debug("\n\nProcessing ChequeraController.generatePdf\n\n");
        var documento = service.generarChequeraPdf(new SolicitudChequeraPdf(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, codigoBarras, false, false));
        return responder(documento, "chequera.pdf");
    }

    @GetMapping("/generatePdf/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdfBulk(@PathVariable Integer facultadId,
                                                    @PathVariable Integer tipoChequeraId,
                                                    @PathVariable Long chequeraSerieId,
                                                    @PathVariable Integer alternativaId,
                                                    @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        log.debug("\n\nProcessing ChequeraController.generatePdfBulk\n\n");
        var documento = service.generarChequeraPdf(new SolicitudChequeraPdf(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, codigoBarras, false, true));
        return responder(documento, "chequera.pdf");
    }

    @GetMapping("/generatePdf/completa/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdfCompleta(@PathVariable Integer facultadId,
                                                        @PathVariable Integer tipoChequeraId,
                                                        @PathVariable Long chequeraSerieId,
                                                        @PathVariable Integer alternativaId,
                                                        @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        var documento = service.generarChequeraPdf(new SolicitudChequeraPdf(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, codigoBarras, true, false));
        return responder(documento, "chequera.pdf");
    }

    @GetMapping("/generatePdf/completa/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{codigoBarras}")
    public ResponseEntity<Resource> generatePdfCompletaBulk(@PathVariable Integer facultadId,
                                                            @PathVariable Integer tipoChequeraId,
                                                            @PathVariable Long chequeraSerieId,
                                                            @PathVariable Integer alternativaId,
                                                            @PathVariable Boolean codigoBarras) throws FileNotFoundException {
        var documento = service.generarChequeraPdf(new SolicitudChequeraPdf(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, codigoBarras, true, true));
        return responder(documento, "chequera.pdf");
    }

    @GetMapping("/generatePdf/reemplazo/completa/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    public ResponseEntity<Resource> generatePdfReemplazoCompleta(@PathVariable Integer facultadId,
                                                                 @PathVariable Integer tipoChequeraId,
                                                                 @PathVariable Long chequeraSerieId,
                                                                 @PathVariable Integer alternativaId) throws FileNotFoundException {
        var documento = service.generarChequeraReemplazoPdf(facultadId, tipoChequeraId, chequeraSerieId,
                alternativaId, true);
        return responder(documento, "chequera.pdf");
    }

    @GetMapping("/generateCuotaPdf/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{productoId}/{cuotaId}")
    public ResponseEntity<Resource> generateCuotaPdf(@PathVariable Integer facultadId,
                                                     @PathVariable Integer tipoChequeraId,
                                                     @PathVariable Long chequeraSerieId,
                                                     @PathVariable Integer alternativaId,
                                                     @PathVariable Integer productoId,
                                                     @PathVariable Integer cuotaId) throws FileNotFoundException {
        var documento = service.generarCuotaPdf(facultadId, tipoChequeraId, chequeraSerieId, alternativaId,
                productoId, cuotaId);
        return responder(documento, "cuota.pdf");
    }

    @GetMapping("/sendChequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{copiaInformes}/{codigoBarras}")
    public ResponseEntity<String> sendChequera(@PathVariable Integer facultadId,
                                               @PathVariable Integer tipoChequeraId,
                                               @PathVariable Long chequeraSerieId,
                                               @PathVariable Integer alternativaId,
                                               @PathVariable Boolean copiaInformes,
                                               @PathVariable Boolean codigoBarras) {
        // Shim: el envío se resuelve en el listener para tener una única ruta de composición.
        solicitudChequeraPublisher.publicar(new SolicitudEnvioChequera(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, copiaInformes, codigoBarras, true, false, true));
        return ResponseEntity.accepted().body(MENSAJE_ENCOLADO);
    }

    @GetMapping("/sendChequera/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}/{copiaInformes}/{codigoBarras}")
    public ResponseEntity<String> sendChequeraBulk(@PathVariable Integer facultadId,
                                                   @PathVariable Integer tipoChequeraId,
                                                   @PathVariable Long chequeraSerieId,
                                                   @PathVariable Integer alternativaId,
                                                   @PathVariable Boolean copiaInformes,
                                                   @PathVariable Boolean codigoBarras) {
        solicitudChequeraPublisher.publicar(new SolicitudEnvioChequera(facultadId, tipoChequeraId,
                chequeraSerieId, alternativaId, copiaInformes, codigoBarras, true, true, true));
        return ResponseEntity.accepted().body(MENSAJE_ENCOLADO);
    }

    @GetMapping("/sendCuota/{chequeraCuotaId}")
    public ResponseEntity<String> sendCuota(@PathVariable Long chequeraCuotaId) {
        solicitudCuotaPublisher.publicar(chequeraCuotaId);
        return ResponseEntity.accepted().body(MENSAJE_ENCOLADO);
    }

    private ResponseEntity<Resource> responder(DocumentoPdf documento, String nombreDescarga)
            throws FileNotFoundException {
        if (documento.estaVacio()) {
            return ResponseEntity.noContent().build();
        }
        log.debug("Generando archivo {}", documento.ruta());
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
