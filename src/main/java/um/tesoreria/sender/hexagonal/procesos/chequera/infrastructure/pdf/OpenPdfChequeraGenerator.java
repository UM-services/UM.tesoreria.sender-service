package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.pdf;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BarcodeInter25;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.datos.catalogo.application.service.CatalogoDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequera;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosChequeraReemplazo;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosCuota;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.ports.out.ChequeraPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuota;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraCuotaReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerie;
import um.tesoreria.sender.hexagonal.shared.domain.model.ChequeraSerieReemplazo;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.LectivoAlternativa;
import um.tesoreria.sender.hexagonal.shared.domain.model.PreferenciaPago;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenPdfChequeraGenerator implements ChequeraPdfGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String UM_LOGO = "marca_um_65.png";
    private static final String ETEC_LOGO = "marca_etec.png";
    private static final int ETEC_FACULTAD_ID = 15;
    private static final int FACULTAD_SIN_CARRERA_ID = 6;

    private final CatalogoDatosService catalogoDatosService;

    @Value("${path.reports}")
    private String pathReports;

    @Override
    public DocumentoPdf generarChequera(DatosChequera datos, boolean codigoBarras, boolean completa) {
        ChequeraSerie serie = datos.serie();
        String filename = String.format("%schequera-%s-%s-%s-%s.pdf", pathReports, serie.getPersonaId(),
                serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId());
        log.debug("Generando {}", filename);
        try {
            Document document = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            agregarEncabezado(document, serie.getFacultadId(),
                    Objects.requireNonNull(serie.getFacultad()).getNombre());
            agregarInfoComun(document,
                    Objects.requireNonNull(serie.getTipoChequera()).getNombre(),
                    Objects.requireNonNull(serie.getLectivo()).getNombre(),
                    Objects.requireNonNull(serie.getArancelTipo()).getDescripcion(),
                    codigoBarras ? "RapiPago" : "MercadoPago",
                    serie.getAlternativaId(),
                    datos.persona(),
                    datos.carrera(),
                    "Chequera",
                    serie.getChequeraSerieId(),
                    serie.getFacultadId(),
                    serie.getTipoChequeraId(),
                    serie.getBecaPorcentaje());

            for (PreferenciaPago preferencia : datos.preferencias()) {
                var cuota = preferencia.getChequeraCuota();
                if (cuota == null) {
                    continue;
                }
                boolean imprimir = completa ? cuota.conImporte() : cuota.pendienteDePago();
                if (imprimir) {
                    agregarTablaCuota(document, writer, cuota, serie, preferencia, codigoBarras,
                            datos.alternativas());
                }
            }
            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF", ex);
            return DocumentoPdf.vacio();
        }
        return DocumentoPdf.de(filename);
    }

    @Override
    public DocumentoPdf generarChequeraReemplazo(DatosChequeraReemplazo datos, boolean completa) {
        ChequeraSerieReemplazo serie = datos.serie();
        String filename = String.format("%schequera-reemplazo-%s-%s-%s-%s.pdf", pathReports, serie.getPersonaId(),
                serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId());
        log.debug("Generando {}", filename);
        try {
            Document document = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            agregarEncabezado(document, serie.getFacultadId(),
                    Objects.requireNonNull(serie.getFacultad()).getNombre());
            agregarInfoComun(document,
                    Objects.requireNonNull(serie.getTipoChequera()).getNombre(),
                    Objects.requireNonNull(serie.getLectivo()).getNombre(),
                    Objects.requireNonNull(serie.getArancelTipo()).getDescripcion(),
                    "RapiPago",
                    serie.getAlternativaId(),
                    datos.persona(),
                    datos.carrera(),
                    "Chequera Reemplazo",
                    serie.getChequeraSerieId(),
                    serie.getFacultadId(),
                    serie.getTipoChequeraId(),
                    BigDecimal.ZERO);

            for (ChequeraCuotaReemplazo cuota : datos.cuotas()) {
                boolean imprimir = completa ? cuota.conImporte() : cuota.pendienteDePago();
                if (imprimir) {
                    agregarTablaCuotaReemplazo(document, writer, cuota, serie, datos.alternativas());
                }
            }
            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF de reemplazo", ex);
            return DocumentoPdf.vacio();
        }
        return DocumentoPdf.de(filename);
    }

    @Override
    public DocumentoPdf generarCuota(DatosCuota datos) {
        var serie = datos.serie();
        var cuota = datos.cuota();
        String filename = String.format("%scuota-%s-%s-%s-%s-%s-%s.pdf", pathReports, serie.getPersonaId(),
                serie.getFacultadId(), serie.getTipoChequeraId(), serie.getChequeraSerieId(), cuota.getProductoId(),
                cuota.getCuotaId());
        log.debug("Generando {}", filename);
        try {
            Document document = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            agregarEncabezado(document, serie.getFacultadId(),
                    Objects.requireNonNull(serie.getFacultad()).getNombre());
            agregarInfoComun(document,
                    Objects.requireNonNull(serie.getTipoChequera()).getNombre(),
                    Objects.requireNonNull(serie.getLectivo()).getNombre(),
                    Objects.requireNonNull(serie.getArancelTipo()).getDescripcion(),
                    "RapiPago",
                    serie.getAlternativaId(),
                    datos.persona(),
                    datos.carrera(),
                    "Chequera",
                    serie.getChequeraSerieId(),
                    serie.getFacultadId(),
                    serie.getTipoChequeraId(),
                    serie.getBecaPorcentaje());

            agregarTablaCuota(document, writer, cuota, serie, null, true, datos.alternativas());
            document.close();
        } catch (Exception ex) {
            log.error("Error generando PDF de cuota", ex);
            return DocumentoPdf.vacio();
        }
        return DocumentoPdf.de(filename);
    }

    private Document crearDocumento() {
        Document document = new Document(PageSize.A4);
        document.setMargins(40, 25, 40, 30);
        return document;
    }

    private void agregarEncabezado(Document document, Integer facultadId, String facultadNombre)
            throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(new float[]{1, 1});
        table.setWidthPercentage(100);

        String logoPath = facultadId != null && facultadId == ETEC_FACULTAD_ID ? ETEC_LOGO : UM_LOGO;
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

    private void agregarInfoComun(Document document,
                                  String tipoChequeraNombre,
                                  String lectivoNombre,
                                  String arancelTipoNombre,
                                  String tipoImpresion,
                                  Integer alternativaId,
                                  um.tesoreria.sender.hexagonal.shared.domain.model.Persona persona,
                                  um.tesoreria.sender.hexagonal.shared.domain.model.Carrera carrera,
                                  String chequeraLabel,
                                  Long chequeraSerieId,
                                  Integer facultadId,
                                  Integer tipoChequeraId,
                                  BigDecimal becaPorcentaje) throws DocumentException {
        Paragraph paragraph = new Paragraph(tipoChequeraNombre, new Font(Font.HELVETICA, 16, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph(lectivoNombre, new Font(Font.HELVETICA, 12));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase(arancelTipoNombre + " - Beca: ", new Font(Font.HELVETICA, 12)));
        paragraph.add(new Phrase(String.format("%.2f%%", becaPorcentaje.multiply(BigDecimal.valueOf(100))),
                new Font(Font.HELVETICA, 12, Font.BOLD)));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph(tipoImpresion, new Font(Font.HELVETICA, 12, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase("         Alumno: (" + persona.getPersonaId() + ") ",
                new Font(Font.HELVETICA, 11)));
        paragraph.add(new Phrase(persona.getApellido() + ", " + persona.getNombre(),
                new Font(Font.HELVETICA, 11, Font.BOLD)));
        if (facultadId != FACULTAD_SIN_CARRERA_ID) {
            paragraph.add(new Phrase(" - (" + carrera.getNombre() + ")", new Font(Font.HELVETICA, 11)));
        }
        document.add(paragraph);

        paragraph = new Paragraph(new Phrase(chequeraLabel + ": ", new Font(Font.HELVETICA, 11)));
        paragraph.add(new Phrase(Objects.requireNonNull(chequeraSerieId).toString(),
                new Font(Font.HELVETICA, 11, Font.BOLD)));
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

    private void agregarTablaCuota(Document document, PdfWriter writer, ChequeraCuota cuota, ChequeraSerie serie,
                                   PreferenciaPago preferencia, boolean codigoBarras,
                                   List<LectivoAlternativa> alternativas) throws DocumentException {
        var lectivoAlternativa = buscarAlternativa(alternativas, cuota.getProductoId(), serie);
        PdfPTable table = crearEstructuraTablaCuota(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(),
                cuota.getAnho(), cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());

        if (preferencia != null && preferencia.getMercadoPagoContext() != null) {
            agregarEnlaceMercadoPago(table, preferencia.getMercadoPagoContext().getInitPoint(), codigoBarras);
        }
        if (codigoBarras) {
            agregarCodigoBarras(table, writer, cuota.getCodigoBarras());
        }
        document.add(table);
    }

    private void agregarTablaCuotaReemplazo(Document document, PdfWriter writer, ChequeraCuotaReemplazo cuota,
                                            ChequeraSerieReemplazo serie, List<LectivoAlternativa> alternativas)
            throws DocumentException {
        var lectivoAlternativa = buscarAlternativaReemplazo(alternativas, cuota.getProductoId(), serie);
        PdfPTable table = crearEstructuraTablaCuota(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(),
                cuota.getAnho(), cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());
        agregarCodigoBarras(table, writer, cuota.getCodigoBarras());
        document.add(table);
    }

    private LectivoAlternativa buscarAlternativa(List<LectivoAlternativa> alternativas, Integer productoId,
                                                 ChequeraSerie serie) {
        LectivoAlternativa alternativa = alternativas == null ? null : alternativas.stream()
                .filter(a -> Objects.equals(a.getProductoId(), productoId))
                .findFirst()
                .orElse(null);
        if (alternativa == null) {
            alternativa = catalogoDatosService.obtenerAlternativa(serie.getFacultadId(), serie.getLectivoId(),
                    serie.getTipoChequeraId(), productoId, serie.getAlternativaId());
        }
        return alternativa;
    }

    private LectivoAlternativa buscarAlternativaReemplazo(List<LectivoAlternativa> alternativas, Integer productoId,
                                                          ChequeraSerieReemplazo serie) {
        LectivoAlternativa alternativa = alternativas == null ? null : alternativas.stream()
                .filter(a -> Objects.equals(a.getProductoId(), productoId))
                .findFirst()
                .orElse(null);
        if (alternativa == null) {
            alternativa = catalogoDatosService.obtenerAlternativa(serie.getFacultadId(), serie.getLectivoId(),
                    serie.getTipoChequeraId(), productoId, serie.getAlternativaId());
        }
        return alternativa;
    }

    private PdfPTable crearEstructuraTablaCuota(LectivoAlternativa lectivoAlternativa, Integer cuotaId, Integer mes,
                                                Integer anho, OffsetDateTime vencimiento1,
                                                OffsetDateTime vencimiento2, OffsetDateTime vencimiento3,
                                                BigDecimal importe1, BigDecimal importe2, BigDecimal importe3) {
        PdfPTable table = new PdfPTable(new float[]{1, 1, 1, 1});
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.addElement(new Paragraph(new Phrase(
                String.format("%s: %d de %d", lectivoAlternativa.getTitulo(), cuotaId, lectivoAlternativa.getCuotas()),
                new Font(Font.HELVETICA, 8, Font.BOLD))));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        var paragraph = new Paragraph(new Phrase("Período: ", new Font(Font.HELVETICA, 8)));
        paragraph.add(new Phrase(String.format("%d/%d", mes, anho), new Font(Font.HELVETICA, 8, Font.BOLD)));
        cell.addElement(paragraph);
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.addElement(crearParrafoVencimiento("Primer Vencimiento: ", vencimiento1));
        cell.addElement(crearParrafoVencimiento("Segundo Vencimiento: ", vencimiento2));
        cell.addElement(crearParrafoVencimiento("Tercer Vencimiento: ", vencimiento3));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        cell.addElement(crearParrafoImporte(importe1, decimalFormat));
        cell.addElement(crearParrafoImporte(importe2, decimalFormat));
        cell.addElement(crearParrafoImporte(importe3, decimalFormat));
        table.addCell(cell);

        return table;
    }

    private Paragraph crearParrafoVencimiento(String label, OffsetDateTime date) {
        Paragraph p = new Paragraph(new Phrase(label, new Font(Font.HELVETICA, 8)));
        p.add(new Phrase(FORMATO_FECHA.format(date.withOffsetSameInstant(ZoneOffset.UTC)),
                new Font(Font.HELVETICA, 8, Font.BOLD)));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private Paragraph crearParrafoImporte(BigDecimal importe, DecimalFormat formatter) {
        Paragraph p = new Paragraph(new Phrase("Importe: ", new Font(Font.HELVETICA, 8)));
        p.add(new Phrase(formatter.format(importe), new Font(Font.HELVETICA, 8, Font.BOLD)));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void agregarEnlaceMercadoPago(PdfPTable table, String initPoint, boolean codigoBarras) {
        Chunk link = new Chunk("Click aquí para pagar",
                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 255)));
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

    private void agregarCodigoBarras(PdfPTable table, PdfWriter writer, String barcodeText) {
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

}
