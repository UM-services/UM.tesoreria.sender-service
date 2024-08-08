package um.tesoreria.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.io.Files;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BarcodeInter25;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import um.tesoreria.sender.client.tesoreria.core.*;
import um.tesoreria.sender.client.tesoreria.core.facade.SincronizeClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
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

    public FormulariosToPdfService(Environment environment, RestTemplateBuilder restTemplateBuilder, ChequeraSerieClient chequeraSerieClient,
                                   ChequeraCuotaClient chequeraCuotaClient, FacultadClient facultadClient, TipoChequeraClient tipoChequeraClient,
                                   PersonaClient personaClient, LectivoClient lectivoClient, LegajoClient legajoClient, CarreraClient carreraClient,
                                   LectivoAlternativaClient lectivoAlternativaClient, SincronizeClient sincronizeClient,
                                   ChequeraSerieReemplazoClient chequeraSerieReemplazoClient, ChequeraCuotaReemplazoClient chequeraCuotaReemplazoClient) {
        this.environment = environment;
        this.restTemplateBuilder = restTemplateBuilder;
        this.chequeraSerieClient = chequeraSerieClient;
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.facultadClient = facultadClient;
        this.tipoChequeraClient = tipoChequeraClient;
        this.personaClient = personaClient;
        this.lectivoClient = lectivoClient;
        this.legajoClient = legajoClient;
        this.carreraClient = carreraClient;
        this.lectivoAlternativaClient = lectivoAlternativaClient;
        this.sincronizeClient = sincronizeClient;
        this.chequeraSerieReemplazoClient = chequeraSerieReemplazoClient;
        this.chequeraCuotaReemplazoClient = chequeraCuotaReemplazoClient;
    }

    public String generateChequeraPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                      Integer alternativaId, Boolean completa) {
        ChequeraSerieDto serie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        try {
            log.debug("ChequeraSerie -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(serie));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraSerie -> {}", e.getMessage());
        }
        List<ChequeraCuotaDto> cuotas = chequeraCuotaClient
                .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId());
        try {
            log.debug("Cuotas -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(cuotas));
        } catch (JsonProcessingException e) {
            log.debug("Cuotas -> {}", e.getMessage());
        }
        boolean hayAlgoParaImprimir = false;
        for (ChequeraCuotaDto cuota : cuotas) {
            if (cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                hayAlgoParaImprimir = true;
            }
        }

        if (!hayAlgoParaImprimir) {
            return "";
        }

        FacultadDto facultad = facultadClient.findByFacultadId(serie.getFacultadId());
        try {
            log.debug("Facultad -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facultad));
        } catch (JsonProcessingException e) {
            log.debug("Facultad -> {}", e.getMessage());
        }
        TipoChequeraDto tipoChequera = tipoChequeraClient.findByTipoChequeraId(serie.getTipoChequeraId());
        try {
            log.debug("TipoChequera -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(tipoChequera));
        } catch (JsonProcessingException e) {
            log.debug("TipoChequera -> {}", e.getMessage());
        }
        PersonaDto persona = null;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        try {
            log.debug("Persona -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(persona));
        } catch (JsonProcessingException e) {
            log.debug("Persona -> {}", e.getMessage());
        }
        LectivoDto lectivo = null;
        try {
            lectivo = lectivoClient.findByLectivoId(serie.getLectivoId());
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        try {
            log.debug("Lectivo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(lectivo));
        } catch (JsonProcessingException e) {
            log.debug("Lectivo -> {}", e.getMessage());
        }
        // Sincroniza carrera
        try {
            sincronizeClient.sincronizeCarreraAlumno(facultadId, persona.getPersonaId(), persona.getDocumentoId());
        } catch (Exception e) {
            log.debug("Sin sincronizar");
        }
        log.debug("Antes");

        LegajoDto legajo = null;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(serie.getFacultadId(),
                    serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        try {
            log.debug("Legajo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(legajo));
        } catch (JsonProcessingException e) {
            log.debug("Legajo -> {}", e.getMessage());
        }
        CarreraDto carrera = null;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(),
                    legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }
        try {
            log.debug("Carrera -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(carrera));
        } catch (JsonProcessingException e) {
            log.debug("Carrera -> {}", e.getMessage());
        }

        String path = environment.getProperty("path.reports");

        String filename = path + "chequera-" + serie.getPersonaId() + "-" + serie.getFacultadId() + "-"
                + serie.getTipoChequeraId() + "-" + serie.getChequeraSerieId() + ".pdf";

        log.debug("Generando {}", filename);

        try {
            Document document = new Document(new Rectangle(PageSize.A4));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.setMargins(40, 25, 40, 30);
            document.open();

            float[] columnHeader = {1, 1};
            PdfPTable table = new PdfPTable(columnHeader);
            table.setWidthPercentage(100);

            Image image = null;
            if (facultadId == 15)
                image = Image.getInstance("marca_etec.png");
            else
                image = Image.getInstance("marca_um.png");
            image.scalePercent(80);
            PdfPCell cell = new PdfPCell(image);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            Paragraph paragraph = new Paragraph("UNIVERSIDAD DE MENDOZA", new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            paragraph = new Paragraph(facultad.getNombre(), new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            paragraph = new Paragraph(tipoChequera.getNombre(), new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph(lectivo.getNombre(), new Font(Font.HELVETICA, 12));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph("RapiPago", new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(
                    new Phrase("         Alumno: (" + persona.getPersonaId() + ") ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(persona.getApellido() + ", " + persona.getNombre(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            if (facultadId != 6)
                paragraph.add(new Phrase(" - (" + carrera.getNombre() + ")", new Font(Font.HELVETICA, 11)));
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Chequera: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(serie.getChequeraSerieId().toString(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Código de Pago Electrónico: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(
                    String.format("%02d", serie.getFacultadId()) + String.format("%03d", serie.getTipoChequeraId())
                            + String.format("%05d", serie.getChequeraSerieId()),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            document.add(paragraph);
            paragraph = new Paragraph("Alternativa " + alternativaId,
                    new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(new Paragraph(" ", new Font(Font.HELVETICA, 8)));

            for (ChequeraCuotaDto cuota : chequeraCuotaClient
                    .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(serie.getFacultadId(),
                            serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId())) {
                Boolean printCuota = false;
                if (completa) {
                    if (cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                        printCuota = true;
                    }
                } else {
                    if (cuota.getPagado() == 0 && cuota.getBaja() == 0
                            && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                        printCuota = true;
                    }
                }
                try {
                    log.debug("print -> {} - cuota -> {}", printCuota, JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(cuota));
                } catch (JsonProcessingException e) {
                    log.debug("print -> {} - cuota -> {}", printCuota, e.getMessage());
                }
                if (printCuota) {
                    LectivoAlternativaDto lectivoAlternativa = lectivoAlternativaClient
                            .findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                                    serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                                    cuota.getProductoId(), serie.getAlternativaId());

                    float[] columnCuota = {1, 1, 1, 1};
                    table = new PdfPTable(columnCuota);
                    table.setWidthPercentage(100);
                    paragraph = new Paragraph(
                            new Phrase(lectivoAlternativa.getTitulo() + ": " + cuota.getCuotaId()
                                    + " de " + lectivoAlternativa.getCuotas(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                    cell = new PdfPCell();
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);
                    paragraph = new Paragraph(new Phrase("Período: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(
                            new Phrase(cuota.getMes() + "/" + cuota.getAnho(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                    cell = new PdfPCell();
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);
                    cell = new PdfPCell();
                    paragraph = new Paragraph(new Phrase("Primer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento1().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento2().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento3().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);

                    cell = new PdfPCell();
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte1()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte2()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte3()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);

                    BarcodeInter25 code25 = new BarcodeInter25();
                    code25.setGenerateChecksum(false);
                    code25.setCode(cuota.getCodigoBarras());
                    code25.setX(1.3f);

                    image = code25.createImageWithBarcode(writer.getDirectContent(), null, null);
                    cell = new PdfPCell(image);
                    cell.setColspan(4);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.BOTTOM);
                    table.addCell(cell);

                    document.add(table);
                }
            }

            document.add(new Paragraph(" "));

            // Finalizamos el documento
            document.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return filename;
    }

    public String generateChequeraReemplazoPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                      Integer alternativaId, Boolean completa) {
        ChequeraSerieReemplazoDto serie = chequeraSerieReemplazoClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        try {
            log.debug("ChequeraSerieReemplazo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(serie));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraSerieReemplazo -> {}", e.getMessage());
        }
        List<ChequeraCuotaReemplazoDto> cuotas = chequeraCuotaReemplazoClient
                .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId());
        try {
            log.debug("Cuotas Reemplazo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(cuotas));
        } catch (JsonProcessingException e) {
            log.debug("Cuotas Reemplazo -> {}", e.getMessage());
        }
        boolean hayAlgoParaImprimir = false;
        for (ChequeraCuotaReemplazoDto cuota : cuotas) {
            if (cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                hayAlgoParaImprimir = true;
            }
        }

        if (!hayAlgoParaImprimir) {
            return "";
        }

        FacultadDto facultad = facultadClient.findByFacultadId(serie.getFacultadId());
        try {
            log.debug("Facultad -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facultad));
        } catch (JsonProcessingException e) {
            log.debug("Facultad -> {}", e.getMessage());
        }
        TipoChequeraDto tipoChequera = tipoChequeraClient.findByTipoChequeraId(serie.getTipoChequeraId());
        try {
            log.debug("TipoChequera -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(tipoChequera));
        } catch (JsonProcessingException e) {
            log.debug("TipoChequera -> {}", e.getMessage());
        }
        PersonaDto persona = null;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        try {
            log.debug("Persona -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(persona));
        } catch (JsonProcessingException e) {
            log.debug("Persona -> {}", e.getMessage());
        }
        LectivoDto lectivo = null;
        try {
            lectivo = lectivoClient.findByLectivoId(serie.getLectivoId());
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        try {
            log.debug("Lectivo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(lectivo));
        } catch (JsonProcessingException e) {
            log.debug("Lectivo -> {}", e.getMessage());
        }

        LegajoDto legajo = null;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(serie.getFacultadId(),
                    serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        try {
            log.debug("Legajo -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(legajo));
        } catch (JsonProcessingException e) {
            log.debug("Legajo -> {}", e.getMessage());
        }
        CarreraDto carrera = null;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(),
                    legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }
        try {
            log.debug("Carrera -> {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(carrera));
        } catch (JsonProcessingException e) {
            log.debug("Carrera -> {}", e.getMessage());
        }

        String path = environment.getProperty("path.reports");

        String filename = path + "chequera-reemplazo-" + serie.getPersonaId() + "-" + serie.getFacultadId() + "-"
                + serie.getTipoChequeraId() + "-" + serie.getChequeraSerieId() + ".pdf";

        log.debug("Generando {}", filename);

        try {
            Document document = new Document(new Rectangle(PageSize.A4));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.setMargins(40, 25, 40, 30);
            document.open();

            float[] columnHeader = {1, 1};
            PdfPTable table = new PdfPTable(columnHeader);
            table.setWidthPercentage(100);

            Image image = null;
            if (facultadId == 15)
                image = Image.getInstance("marca_etec.png");
            else
                image = Image.getInstance("marca_um.png");
            image.scalePercent(80);
            PdfPCell cell = new PdfPCell(image);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            Paragraph paragraph = new Paragraph("UNIVERSIDAD DE MENDOZA", new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            paragraph = new Paragraph(facultad.getNombre(), new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            paragraph = new Paragraph(tipoChequera.getNombre(), new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph(lectivo.getNombre(), new Font(Font.HELVETICA, 12));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph("RapiPago", new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(
                    new Phrase("         Alumno: (" + persona.getPersonaId() + ") ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(persona.getApellido() + ", " + persona.getNombre(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            if (facultadId != 6)
                paragraph.add(new Phrase(" - (" + carrera.getNombre() + ")", new Font(Font.HELVETICA, 11)));
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Chequera Reemplazo: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(serie.getChequeraSerieId().toString(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Código de Pago Electrónico: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(
                    String.format("%02d", serie.getFacultadId()) + String.format("%03d", serie.getTipoChequeraId())
                            + String.format("%05d", serie.getChequeraSerieId()),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            document.add(paragraph);
            paragraph = new Paragraph("Alternativa " + alternativaId,
                    new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(new Paragraph(" ", new Font(Font.HELVETICA, 8)));

            for (ChequeraCuotaReemplazoDto cuota : chequeraCuotaReemplazoClient
                    .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(serie.getFacultadId(),
                            serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId())) {
                Boolean printCuota = false;
                if (completa) {
                    if (cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                        printCuota = true;
                    }
                } else {
                    if (cuota.getPagado() == 0 && cuota.getBaja() == 0
                            && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                        printCuota = true;
                    }
                }
                try {
                    log.debug("print -> {} - cuota -> {}", printCuota, JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(cuota));
                } catch (JsonProcessingException e) {
                    log.debug("print -> {} - cuota -> {}", printCuota, e.getMessage());
                }
                if (printCuota) {
                    LectivoAlternativaDto lectivoAlternativa = lectivoAlternativaClient
                            .findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                                    serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                                    cuota.getProductoId(), serie.getAlternativaId());

                    float[] columnCuota = {1, 1, 1, 1};
                    table = new PdfPTable(columnCuota);
                    table.setWidthPercentage(100);
                    paragraph = new Paragraph(
                            new Phrase(lectivoAlternativa.getTitulo() + ": " + cuota.getCuotaId()
                                    + " de " + lectivoAlternativa.getCuotas(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                    cell = new PdfPCell();
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);
                    paragraph = new Paragraph(new Phrase("Período: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(
                            new Phrase(cuota.getMes() + "/" + cuota.getAnho(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                    cell = new PdfPCell();
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);
                    cell = new PdfPCell();
                    paragraph = new Paragraph(new Phrase("Primer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento1().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento2().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(cuota.getVencimiento3().withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);

                    cell = new PdfPCell();
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte1()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte2()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte3()),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    cell.setBorder(Rectangle.TOP);
                    table.addCell(cell);

                    BarcodeInter25 code25 = new BarcodeInter25();
                    code25.setGenerateChecksum(false);
                    code25.setCode(cuota.getCodigoBarras());
                    code25.setX(1.3f);

                    image = code25.createImageWithBarcode(writer.getDirectContent(), null, null);
                    cell = new PdfPCell(image);
                    cell.setColspan(4);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(Rectangle.BOTTOM);
                    table.addCell(cell);

                    document.add(table);
                }
            }

            document.add(new Paragraph(" "));

            // Finalizamos el documento
            document.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return filename;
    }

    public String generateCuotaPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer alternativaId, Integer productoId, Integer cuotaId) {
        ChequeraSerieDto serie = chequeraSerieClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        ChequeraCuotaDto cuota = chequeraCuotaClient
                .findByUnique(serie.getFacultadId(),
                        serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId(), productoId, cuotaId);
        boolean hayAlgoParaImprimir = cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0;

        if (!hayAlgoParaImprimir) {
            return "";
        }

        FacultadDto facultad = facultadClient.findByFacultadId(serie.getFacultadId());
        TipoChequeraDto tipoChequera = tipoChequeraClient.findByTipoChequeraId(serie.getTipoChequeraId());
        PersonaDto persona = null;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        LectivoDto lectivo = null;
        try {
            lectivo = lectivoClient.findByLectivoId(serie.getLectivoId());
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        // Sincroniza carrera
        try {
            sincronizeClient.sincronizeCarreraAlumno(facultadId, persona.getPersonaId(), persona.getDocumentoId());
        } catch (Exception e) {
            log.debug("Sin sincronizar");
        }
        log.debug("Antes");

        LegajoDto legajo = null;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(serie.getFacultadId(),
                    serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        CarreraDto carrera = null;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(),
                    legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }

        String path = environment.getProperty("path.reports");

        String filename = path + "cuota-" + serie.getPersonaId() + "-" + serie.getFacultadId() + "-"
                + serie.getTipoChequeraId() + "-" + serie.getChequeraSerieId() + "-" + cuota.getProductoId() + "-" + cuota.getCuotaId() + ".pdf";

        try {
            Document document = new Document(new Rectangle(PageSize.A4));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.setMargins(40, 25, 40, 30);
            document.open();

            float[] columnHeader = {1, 1};
            PdfPTable table = new PdfPTable(columnHeader);
            table.setWidthPercentage(100);

            Image image = null;
            if (facultadId == 15)
                image = Image.getInstance("marca_etec.png");
            else
                image = Image.getInstance("marca_um.png");
            image.scalePercent(80);
            PdfPCell cell = new PdfPCell(image);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            Paragraph paragraph = new Paragraph("UNIVERSIDAD DE MENDOZA", new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            paragraph = new Paragraph(facultad.getNombre(), new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            paragraph = new Paragraph(tipoChequera.getNombre(), new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph(lectivo.getNombre(), new Font(Font.HELVETICA, 12));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph("RapiPago", new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(
                    new Phrase("         Alumno: (" + persona.getPersonaId() + ") ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(persona.getApellido() + ", " + persona.getNombre(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            if (facultadId != 6)
                paragraph.add(new Phrase(" - (" + carrera.getNombre() + ")", new Font(Font.HELVETICA, 11)));
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Chequera: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(serie.getChequeraSerieId().toString(),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph = new Paragraph(new Phrase("Código de Pago Electrónico: ", new Font(Font.HELVETICA, 11)));
            paragraph.add(new Phrase(
                    String.format("%02d", serie.getFacultadId()) + String.format("%03d", serie.getTipoChequeraId())
                            + String.format("%05d", serie.getChequeraSerieId()),
                    new Font(Font.HELVETICA, 11, Font.BOLD)));
            document.add(paragraph);
            paragraph = new Paragraph("Alternativa " + alternativaId,
                    new Font(Font.HELVETICA, 12, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(new Paragraph(" ", new Font(Font.HELVETICA, 8)));

            if (cuota.getPagado() == 0 && cuota.getBaja() == 0
                    && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                LectivoAlternativaDto lectivoAlternativa = lectivoAlternativaClient
                        .findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                                serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                                cuota.getProductoId(), serie.getAlternativaId());

                float[] columnCuota = {1, 1, 1, 1};
                table = new PdfPTable(columnCuota);
                table.setWidthPercentage(100);
                paragraph = new Paragraph(
                        new Phrase(lectivoAlternativa.getTitulo() + ": " + cuota.getCuotaId()
                                + " de " + lectivoAlternativa.getCuotas(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                cell = new PdfPCell();
                cell.addElement(paragraph);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);
                paragraph = new Paragraph(new Phrase("Período: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(
                        new Phrase(cuota.getMes() + "/" + cuota.getAnho(), new Font(Font.HELVETICA, 8, Font.BOLD)));
                cell = new PdfPCell();
                cell.addElement(paragraph);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);
                cell = new PdfPCell();
                paragraph = new Paragraph(new Phrase("Primer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(cuota.getVencimiento1().withOffsetSameInstant(ZoneOffset.UTC)),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(cuota.getVencimiento2().withOffsetSameInstant(ZoneOffset.UTC)),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(cuota.getVencimiento3().withOffsetSameInstant(ZoneOffset.UTC)),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);

                cell = new PdfPCell();
                paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte1()),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte2()),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(new DecimalFormat("#.00").format(cuota.getImporte3()),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);

                BarcodeInter25 code25 = new BarcodeInter25();
                code25.setGenerateChecksum(false);
                code25.setCode(cuota.getCodigoBarras());
                code25.setX(1.3f);

                image = code25.createImageWithBarcode(writer.getDirectContent(), null, null);
                cell = new PdfPCell(image);
                cell.setColspan(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);

                document.add(table);
            }

            document.add(new Paragraph(" "));

            // Finalizamos el documento
            document.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return filename;
    }

    public String generateMatriculaPdf(BigDecimal personaId, Integer documentoId, Integer facultadId,
                                       Integer lectivoId) {
        FacultadDto facultad = null;
        try {
            facultad = facultadClient.findByFacultadId(facultadId);
        } catch (Exception e) {
            facultad = new FacultadDto();
        }
        log.debug(facultad.toString());
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
            Files.write(response.getBody(), new File(filename));
        } catch (HttpServerErrorException e) {
            log.debug("No se pudo generar {}", filename);
            filename = null;
        } catch (IOException e) {
            log.debug("No se pudo generar {}", filename);
            filename = null;
        }
        return filename;
    }

}
