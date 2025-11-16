package um.tesoreria.sender.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BarcodeInter25;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import um.tesoreria.sender.client.tesoreria.core.*;
import um.tesoreria.sender.client.tesoreria.core.facade.SincronizeClient;
import um.tesoreria.sender.client.tesoreria.mercadopago.ChequeraClient;
import um.tesoreria.sender.domain.dto.UMPreferenceMPDto;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FormulariosToPdfService {

    private final Environment environment;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final FacultadClient facultadClient;
    private final TipoChequeraClient tipoChequeraClient;
    private final PersonaClient personaClient;
    private final LectivoClient lectivoClient;
    private final LegajoClient legajoClient;
    private final CarreraClient carreraClient;
    private final LectivoAlternativaClient lectivoAlternativaClient;
    private final SincronizeClient sincronizeClient;
    private final ChequeraSerieReemplazoClient chequeraSerieReemplazoClient;
    private final ChequeraCuotaReemplazoClient chequeraCuotaReemplazoClient;
    private final ChequeraClient chequeraClient;

    public String generateChequeraPdf(Integer facultadId,
                                      Integer tipoChequeraId,
                                      Long chequeraSerieId,
                                      Integer alternativaId,
                                      Boolean codigoBarras,
                                      Boolean completa,
                                      List<UMPreferenceMPDto> preferences) {
        log.debug("Processing FormulariosToPdfService.generateChequeraPdf");
        var serie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        if (preferences == null) {
            preferences = chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
        }
        List<ChequeraCuotaDto> cuotas = preferences.stream().map(UMPreferenceMPDto::getChequeraCuota).toList();
        if (cuotas.stream().noneMatch(c -> c.getPagado() == 0 && c.getBaja() == 0 && c.getCompensada() == 0 && c.getImporte1().compareTo(BigDecimal.ZERO) != 0)) {
            log.debug("No hay nada para imprimir.");
            return "";
        }

        var data = fetchData(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getPersonaId(), serie.getDocumentoId(), serie.getLectivoId());

        String path = environment.getProperty("path.reports");
        String filename = String.format("%schequera-%s-%s-%s-%s.pdf", path, serie.getPersonaId(), serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId());
        log.debug("Generando {}", filename);

        try {
            Document document = createDocument();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addHeader(document, facultadId, data.facultad().getNombre());
            addCommonHeaderInfo(document, data.tipoChequera().getNombre(), data.lectivo().getNombre(),
                    codigoBarras ? "RapiPago" : "MercadoPago", alternativaId, data.persona(), data.carrera(),
                    "Chequera", serie.getChequeraSerieId(), serie.getFacultadId(), serie.getTipoChequeraId());

            for (var umPreferenceMPDto : preferences) {
                var cuota = umPreferenceMPDto.getChequeraCuota();
                boolean printCuota = completa ? cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0
                        : cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0;

                if (printCuota) {
                    addCuotaTable(document, writer, cuota, serie, umPreferenceMPDto, codigoBarras);
                }
            }

            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF", ex);
            return null;
        }
        return filename;
    }

    public String generateChequeraReemplazoPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                               Integer alternativaId, Boolean completa) {
        var serie = chequeraSerieReemplazoClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        var cuotas = chequeraCuotaReemplazoClient.findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(
                serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId());

        if (cuotas.stream().noneMatch(c -> c.getPagado() == 0 && c.getBaja() == 0 && c.getCompensada() == 0 && c.getImporte1().compareTo(BigDecimal.ZERO) != 0)) {
            log.debug("No hay nada para imprimir.");
            return "";
        }

        var data = fetchData(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getPersonaId(), serie.getDocumentoId(), serie.getLectivoId());

        String path = environment.getProperty("path.reports");
        String filename = String.format("%schequera-reemplazo-%s-%s-%s-%s.pdf", path, serie.getPersonaId(), serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId());
        log.debug("Generando {}", filename);

        try {
            Document document = createDocument();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addHeader(document, facultadId, data.facultad().getNombre());
            addCommonHeaderInfo(document, data.tipoChequera().getNombre(), data.lectivo().getNombre(), "RapiPago",
                    alternativaId, data.persona(), data.carrera(), "Chequera Reemplazo", serie.getChequeraSerieId(),
                    serie.getFacultadId(), serie.getTipoChequeraId());

            for (ChequeraCuotaReemplazoDto cuota : cuotas) {
                boolean printCuota = completa ? cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0
                        : cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0;

                if (printCuota) {
                    addCuotaTableReemplazo(document, writer, cuota, serie);
                }
            }

            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF de reemplazo", ex);
            return null;
        }
        return filename;
    }

    public String generateCuotaPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId, Integer productoId, Integer cuotaId) {
        ChequeraSerieDto serie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        ChequeraCuotaDto cuota = chequeraCuotaClient.findByUnique(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId(), productoId, cuotaId);

        if (cuota.getPagado() != 0 || cuota.getBaja() != 0 || cuota.getCompensada() != 0 || cuota.getImporte1().compareTo(BigDecimal.ZERO) == 0) {
            log.debug("La cuota no se puede imprimir.");
            return "";
        }

        var data = fetchData(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getPersonaId(), serie.getDocumentoId(), serie.getLectivoId());

        String path = environment.getProperty("path.reports");
        String filename = String.format("%scuota-%s-%s-%s-%s-%s-%s.pdf", path, serie.getPersonaId(), serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), cuota.getProductoId(), cuota.getCuotaId());
        log.debug("Generando {}", filename);

        try {
            Document document = createDocument();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addHeader(document, facultadId, data.facultad().getNombre());
            addCommonHeaderInfo(document, data.tipoChequera().getNombre(), data.lectivo().getNombre(), "RapiPago",
                    alternativaId, data.persona(), data.carrera(), "Chequera", serie.getChequeraSerieId(),
                    serie.getFacultadId(), serie.getTipoChequeraId());

            addCuotaTable(document, writer, cuota, serie, null, true);

            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF de cuota", ex);
            return null;
        }
        return filename;
    }

    private Document createDocument() throws DocumentException {
        Document document = new Document(PageSize.A4);
        document.setMargins(40, 25, 40, 30);
        return document;
    }

    private void addHeader(Document document, Integer facultadId, String facultadNombre) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(new float[]{1, 1});
        table.setWidthPercentage(100);

        String logoPath = (facultadId == 15) ? "marca_etec.png" : "marca_um_65.png";
        Image image = Image.getInstance(logoPath);
        image.scalePercent(80);
        PdfPCell cell = new PdfPCell(image);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.addElement(new Paragraph("UNIVERSIDAD DE MENDOZA", new Font(Font.HELVETICA, 16, Font.BOLD)));
        cell.addElement(new Paragraph(facultadNombre, new Font(Font.HELVETICA, 14, Font.BOLD)));
        table.addCell(cell);

        document.add(table);
    }

    private void addCommonHeaderInfo(Document document, String tipoChequeraNombre, String lectivoNombre, String tipoImpresion,
                                     Integer alternativaId, PersonaDto persona, CarreraDto carrera, String chequeraLabel,
                                     Long chequeraSerieId, Integer facultadId, Integer tipoChequeraId) throws DocumentException {
        Paragraph paragraph = new Paragraph(tipoChequeraNombre, new Font(Font.HELVETICA, 16, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph(lectivoNombre, new Font(Font.HELVETICA, 12));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph(tipoImpresion, new Font(Font.HELVETICA, 12, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase("         Alumno: (" + persona.getPersonaId() + ") ", new Font(Font.HELVETICA, 11)));
        paragraph.add(new Phrase(persona.getApellido() + ", " + persona.getNombre(), new Font(Font.HELVETICA, 11, Font.BOLD)));
        if (facultadId != 6) {
            paragraph.add(new Phrase(" - (" + carrera.getNombre() + ")", new Font(Font.HELVETICA, 11)));
        }
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase(chequeraLabel + ": ", new Font(Font.HELVETICA, 11)));
        paragraph.add(new Phrase(Objects.requireNonNull(chequeraSerieId).toString(), new Font(Font.HELVETICA, 11, Font.BOLD)));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase("Código de Pago Electrónico: ", new Font(Font.HELVETICA, 11)));
        String codigoPago = String.format("%02d%03d%05d", facultadId, tipoChequeraId, chequeraSerieId);
        paragraph.add(new Phrase(codigoPago, new Font(Font.HELVETICA, 11, Font.BOLD)));
        document.add(paragraph);

        paragraph = new Paragraph("Alternativa " + alternativaId, new Font(Font.HELVETICA, 12, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
        document.add(new Paragraph(" ", new Font(Font.HELVETICA, 8)));
    }

    private void addCuotaTable(Document document, PdfWriter writer, ChequeraCuotaDto cuota, ChequeraSerieDto serie, UMPreferenceMPDto preference, boolean codigoBarras) throws DocumentException {
        var lectivoAlternativa = lectivoAlternativaClient.findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(), cuota.getProductoId(), serie.getAlternativaId());

        PdfPTable table = createCuotaTableStructure(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(), cuota.getAnho(),
                cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());

        if (preference != null && preference.getMercadoPagoContext() != null) {
            addMercadoPagoLink(table, preference.getMercadoPagoContext().getInitPoint(), codigoBarras);
        }
        if (codigoBarras) {
            addBarcode(table, writer, cuota.getCodigoBarras());
        }
        document.add(table);
    }

    private void addCuotaTableReemplazo(Document document, PdfWriter writer, ChequeraCuotaReemplazoDto cuota, ChequeraSerieReemplazoDto serie) throws DocumentException {
        var lectivoAlternativa = lectivoAlternativaClient.findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(), cuota.getProductoId(), serie.getAlternativaId());

        PdfPTable table = createCuotaTableStructure(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(), cuota.getAnho(),
                cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());

        addBarcode(table, writer, cuota.getCodigoBarras());
        document.add(table);
    }

    private PdfPTable createCuotaTableStructure(LectivoAlternativaDto lectivoAlternativa, Integer cuotaId, Integer mes, Integer anho,
                                                java.time.OffsetDateTime vencimiento1, java.time.OffsetDateTime vencimiento2, java.time.OffsetDateTime vencimiento3,
                                                BigDecimal importe1, BigDecimal importe2, BigDecimal importe3) {
        PdfPTable table = new PdfPTable(new float[]{1, 1, 1, 1});
        table.setWidthPercentage(100);

        // Row 1
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.addElement(new Paragraph(new Phrase(String.format("%s: %d de %d", lectivoAlternativa.getTitulo(), cuotaId, lectivoAlternativa.getCuotas()), new Font(Font.HELVETICA, 8, Font.BOLD))));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        var paragraph = new Paragraph(new Phrase("Período: ", new Font(Font.HELVETICA, 8)));
        paragraph.add(new Phrase(String.format("%d/%d", mes, anho), new Font(Font.HELVETICA, 8, Font.BOLD)));
        cell.addElement(paragraph);
        table.addCell(cell);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.addElement(createVencimientoParagraph("Primer Vencimiento: ", vencimiento1, formatter));
        cell.addElement(createVencimientoParagraph("Segundo Vencimiento: ", vencimiento2, formatter));
        cell.addElement(createVencimientoParagraph("Tercer Vencimiento: ", vencimiento3, formatter));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        cell.addElement(createImporteParagraph(importe1, decimalFormat));
        cell.addElement(createImporteParagraph(importe2, decimalFormat));
        cell.addElement(createImporteParagraph(importe3, decimalFormat));
        table.addCell(cell);

        return table;
    }

    private Paragraph createVencimientoParagraph(String label, java.time.OffsetDateTime date, DateTimeFormatter formatter) {
        Paragraph p = new Paragraph(new Phrase(label, new Font(Font.HELVETICA, 8)));
        p.add(new Phrase(formatter.format(date.withOffsetSameInstant(ZoneOffset.UTC)), new Font(Font.HELVETICA, 8, Font.BOLD)));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private Paragraph createImporteParagraph(BigDecimal importe, DecimalFormat formatter) {
        Paragraph p = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
        p.add(new Phrase(formatter.format(importe), new Font(Font.HELVETICA, 8, Font.BOLD)));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void addMercadoPagoLink(PdfPTable table, String initPoint, boolean codigoBarras) {
        Chunk link = new Chunk("Click aquí para pagar", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 255)));
        link.setAnchor(initPoint);
        Paragraph paragraph = new Paragraph(new Phrase("\nEnlace de MERCADOPAGO en ", new Font(Font.HELVETICA, 10)));
        paragraph.add(link);
        paragraph.add(new Phrase("\n\n"));

        PdfPCell cell = new PdfPCell(paragraph);
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(codigoBarras ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
        table.addCell(cell);
    }

    private void addBarcode(PdfPTable table, PdfWriter writer, String barcodeText) {
        BarcodeInter25 code25 = new BarcodeInter25();
        code25.setGenerateChecksum(false);
        code25.setCode(barcodeText);
        code25.setX(1.3f);
        Image image = code25.createImageWithBarcode(writer.getDirectContent(), null, null);

        PdfPCell cell = new PdfPCell(image);
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.BOTTOM);
        table.addCell(cell);
    }

    private PdfData fetchData(Integer facultadId, Integer tipoChequeraId, BigDecimal personaId, Integer documentoId, Integer lectivoId) {
        var facultad = facultadClient.findByFacultadId(facultadId);
        var tipoChequera = tipoChequeraClient.findByTipoChequeraId(tipoChequeraId);
        PersonaDto persona;
        try {
            persona = personaClient.findByUnique(personaId, documentoId);
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        LectivoDto lectivo;
        try {
            lectivo = lectivoClient.findByLectivoId(lectivoId);
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        try {
            sincronizeClient.sincronizeCarreraAlumno(facultadId, persona.getPersonaId(), persona.getDocumentoId());
        } catch (Exception e) {
            log.error("Sin sincronizar carrera");
        }
        LegajoDto legajo;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(facultadId, personaId, documentoId);
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        CarreraDto carrera;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(), legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }
        return new PdfData(facultad, tipoChequera, persona, lectivo, legajo, carrera);
    }

    private record PdfData(FacultadDto facultad, TipoChequeraDto tipoChequera, PersonaDto persona, LectivoDto lectivo, LegajoDto legajo, CarreraDto carrera) {}

    public String generateMatriculaPdf(BigDecimal personaId, Integer documentoId, Integer facultadId,
                                       Integer lectivoId) {
        FacultadDto facultad;
        try {
            facultad = facultadClient.findByFacultadId(facultadId);
        } catch (Exception e) {
            facultad = new FacultadDto();
        }
        log.debug(facultad.toString());
        facultad.getApiserver();
        if (facultad.getApiserver().isEmpty())
            return "";
        String url = "http://" + facultad.getApiserver() + ":" + facultad.getApiport() + "/formularios/matricula/"
                + personaId + "/" + documentoId + "/" + facultadId + "/" + lectivoId;
        String path = environment.getProperty("path.reports");

        String filename = path + "matricula-" + personaId + "-" + documentoId + "-" + facultadId + "-" + lectivoId
                + ".pdf";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplateBuilder.build().exchange(url, HttpMethod.GET, entity,
                    byte[].class);
            Files.write(Objects.requireNonNull(response.getBody()), new File(filename));
        } catch (HttpServerErrorException | IOException e) {
            log.debug("No se pudo generar {}", filename);
            filename = null;
        }
        return filename;
    }
}
