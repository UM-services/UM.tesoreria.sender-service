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
import um.tesoreria.sender.util.Jsonifier;

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
import java.util.stream.Collectors;

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
        log.debug("ChequeraSerie-> {}", serie.jsonify());
        if (preferences == null) {
            preferences = chequeraClient.createChequeraContext(facultadId, tipoChequeraId, chequeraSerieId, alternativaId);
        }
        log.debug("Preferences-> {}", Jsonifier.builder(preferences).build());
        List<ChequeraCuotaDto> cuotas = preferences.stream().map(UMPreferenceMPDto::getChequeraCuota).collect(Collectors.toList());
        log.debug("ChequeraCuotas-> {}", Jsonifier.builder(cuotas).build());
        boolean hayAlgoParaImprimir = false;
        for (ChequeraCuotaDto cuota : cuotas) {
            if (cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                hayAlgoParaImprimir = true;
            }
        }
        log.debug("hayAlgoParaImprimir -> {}", hayAlgoParaImprimir);
        if (!hayAlgoParaImprimir) {
            return "";
        }

        var facultad = facultadClient.findByFacultadId(serie.getFacultadId());
        log.debug("Facultad-> {}", facultad.jsonify());
        var tipoChequera = tipoChequeraClient.findByTipoChequeraId(serie.getTipoChequeraId());
        log.debug("TipoChequera -> {}", tipoChequera.jsonify());
        PersonaDto persona;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        log.debug("Persona-> {}", persona.jsonify());
        LectivoDto lectivo;
        try {
            lectivo = lectivoClient.findByLectivoId(serie.getLectivoId());
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        log.debug("Lectivo -> {}", lectivo.jsonify());
        // Sincroniza carrera
        try {
            sincronizeClient.sincronizeCarreraAlumno(facultadId, persona.getPersonaId(), persona.getDocumentoId());
        } catch (Exception e) {
            log.debug("Sin sincronizar carrera");
        }

        LegajoDto legajo;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(serie.getFacultadId(),
                    serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        log.debug("Legajo -> {}", legajo.jsonify());
        CarreraDto carrera;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(),
                    legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }
        log.debug("Carrera -> {}", carrera.jsonify());

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
                image = Image.getInstance("marca_um_65.png");
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
            var tipoImpresion = "MercadoPago";
            if (codigoBarras) {
                tipoImpresion = "RapiPago";
            }
            paragraph = new Paragraph(tipoImpresion, new Font(Font.HELVETICA, 12, Font.BOLD));
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
            paragraph.add(new Phrase(Objects.requireNonNull(serie.getChequeraSerieId()).toString(),
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

            for (var umPreferenceMPDto : preferences) {
                var cuota = umPreferenceMPDto.getChequeraCuota();
                boolean printCuota = false;
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
                log.debug("Cuota -> {}", cuota.jsonify());
                if (printCuota) {
                    var lectivoAlternativa = lectivoAlternativaClient
                            .findByFacultadIdAndLectivoIdAndTipochequeraIdAndProductoIdAndAlternativaId(
                                    serie.getFacultadId(), serie.getLectivoId(), serie.getTipoChequeraId(),
                                    cuota.getProductoId(), serie.getAlternativaId());
                    log.debug("LectivoAlternativa -> {}", lectivoAlternativa.jsonify());

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
                                    .format(Objects.requireNonNull(cuota.getVencimiento1()).withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(Objects.requireNonNull(cuota.getVencimiento2()).withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(Objects.requireNonNull(cuota.getVencimiento3()).withOffsetSameInstant(ZoneOffset.UTC)),
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

                    // Crear un enlace clicable
                    if (umPreferenceMPDto.getMercadoPagoContext() != null) {
                        log.debug("Generando enlace MP");
                        var mercadoPagoContext = umPreferenceMPDto.getMercadoPagoContext();
                        Chunk link = new Chunk("Click aquí para pagar", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 255)));
                        link.setAnchor(mercadoPagoContext.getInitPoint()); // Establecer el enlace
                        paragraph = new Paragraph(new Phrase("\nEnlace de MERCADOPAGO en ", new Font(Font.HELVETICA, 10)));
                        paragraph.add(link); // Agregar el enlace al párrafo
                        paragraph.add(new Phrase("\n\n"));
                        cell = new PdfPCell(paragraph);
                        cell.setColspan(4);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        var tipoBorde = Rectangle.BOTTOM;
                        if (codigoBarras) {
                            tipoBorde = Rectangle.NO_BORDER;
                        }
                        cell.setBorder(tipoBorde);
                        table.addCell(cell);
                    }

                    // código de barras
                    if (codigoBarras) {
                        log.debug("Generando código de barras");
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
                    }

                    document.add(table);
                }
            }

            document.add(new Paragraph(" "));

            // Finalizamos el documento
            document.close();
            log.debug("Documento finalizado");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return filename;
    }

    public String generateChequeraReemplazoPdf(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId,
                                               Integer alternativaId, Boolean completa) {
        var serie = chequeraSerieReemplazoClient.findByUnique(facultadId, tipoChequeraId, chequeraSerieId);
        log.debug("ChequeraSerieReemplazo -> {}", serie.jsonify());
        var cuotas = chequeraCuotaReemplazoClient
                .findAllByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndAlternativaId(serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), serie.getAlternativaId());
        log.debug("ChequeraCuotaReemplazos -> {}", Jsonifier.builder(cuotas).build());
        boolean hayAlgoParaImprimir = false;
        for (ChequeraCuotaReemplazoDto cuota : cuotas) {
            if (cuota.getPagado() == 0 && cuota.getBaja() == 0 && cuota.getImporte1().compareTo(BigDecimal.ZERO) != 0) {
                hayAlgoParaImprimir = true;
            }
        }

        if (!hayAlgoParaImprimir) {
            return "";
        }

        var facultad = facultadClient.findByFacultadId(serie.getFacultadId());
        log.debug("Facultad -> {}", facultad.jsonify());
        var tipoChequera = tipoChequeraClient.findByTipoChequeraId(serie.getTipoChequeraId());
        log.debug("TipoChequera -> {}", tipoChequera.jsonify());
        PersonaDto persona;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        log.debug("Persona -> {}", persona.jsonify());
        LectivoDto lectivo;
        try {
            lectivo = lectivoClient.findByLectivoId(serie.getLectivoId());
        } catch (Exception e) {
            lectivo = new LectivoDto();
        }
        log.debug("Lectivo -> {}", lectivo.jsonify());

        LegajoDto legajo;
        try {
            legajo = legajoClient.findByFacultadIdAndPersonaIdAndDocumentoId(serie.getFacultadId(),
                    serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            legajo = new LegajoDto();
        }
        log.debug("Legajo -> {}", legajo.jsonify());
        CarreraDto carrera;
        try {
            carrera = carreraClient.findByFacultadIdAndPlanIdAndCarreraId(legajo.getFacultadId(), legajo.getPlanId(),
                    legajo.getCarreraId());
        } catch (Exception e) {
            carrera = new CarreraDto();
        }
        log.debug("Carrera -> {}", carrera.jsonify());

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
                image = Image.getInstance("marca_um_65.png");
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
            paragraph.add(new Phrase(Objects.requireNonNull(serie.getChequeraSerieId()).toString(),
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
                boolean printCuota = false;
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
                log.debug("ChequeraCuotaReemplazo -> {}", cuota.jsonify());
                if (printCuota) {
                    var lectivoAlternativa = lectivoAlternativaClient
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
                                    .format(Objects.requireNonNull(cuota.getVencimiento1()).withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(Objects.requireNonNull(cuota.getVencimiento2()).withOffsetSameInstant(ZoneOffset.UTC)),
                            new Font(Font.HELVETICA, 8, Font.BOLD)));
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    cell.addElement(paragraph);
                    paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                    paragraph.add(new Phrase(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(Objects.requireNonNull(cuota.getVencimiento3()).withOffsetSameInstant(ZoneOffset.UTC)),
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
        PersonaDto persona;
        try {
            persona = personaClient.findByUnique(serie.getPersonaId(), serie.getDocumentoId());
        } catch (Exception e) {
            persona = new PersonaDto();
        }
        LectivoDto lectivo;
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
                image = Image.getInstance("marca_um_65.png");
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
            paragraph.add(new Phrase(Objects.requireNonNull(serie.getChequeraSerieId()).toString(),
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
                                .format(Objects.requireNonNull(cuota.getVencimiento1()).withOffsetSameInstant(ZoneOffset.UTC)),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Segundo Vencimiento: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(Objects.requireNonNull(cuota.getVencimiento2()).withOffsetSameInstant(ZoneOffset.UTC)),
                        new Font(Font.HELVETICA, 8, Font.BOLD)));
                paragraph.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(paragraph);
                paragraph = new Paragraph(new Phrase("Tercer Vencimiento: ", new Font(Font.HELVETICA, 8)));
                paragraph.add(new Phrase(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(Objects.requireNonNull(cuota.getVencimiento3()).withOffsetSameInstant(ZoneOffset.UTC)),
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
            Files.write(Objects.requireNonNull(response.getBody()), new File(filename));
        } catch (HttpServerErrorException | IOException e) {
            log.debug("No se pudo generar {}", filename);
            filename = null;
        }
        return filename;
    }

}
