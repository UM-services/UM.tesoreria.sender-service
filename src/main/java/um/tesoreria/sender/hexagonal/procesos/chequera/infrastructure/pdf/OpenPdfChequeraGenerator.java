package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.pdf;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
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
    private static final ZoneId ZONA_MENDOZA = ZoneId.of("America/Argentina/Mendoza");
    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");

    private static final Color AZUL = new Color(11, 58, 140);
    private static final Color AZUL_LINK = new Color(0, 102, 204);
    private static final Color GRIS_TEXTO = new Color(90, 98, 110);
    private static final Color GRIS_BORDE = new Color(208, 214, 222);
    private static final Color GRIS_FONDO = new Color(244, 246, 249);

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
            PdfWriter writer = crearWriter(document, filename);
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
            PdfWriter writer = crearWriter(document, filename);
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
            PdfWriter writer = crearWriter(document, filename);
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

    private record Dato(String etiqueta, String valor, float tamano) {
    }

    private static Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    private Document crearDocumento() {
        Document document = new Document(PageSize.A4);
        document.setMargins(36, 36, 36, 40);
        return document;
    }

    private PdfWriter crearWriter(Document document, String filename) throws DocumentException, IOException {
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        writer.setPageEvent(new PieDePagina());
        return writer;
    }

    private static class PieDePagina extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            String texto = String.format("Emitido el %s  ·  Página %d",
                    FORMATO_FECHA.format(LocalDate.now(ZONA_MENDOZA)), writer.getPageNumber());
            float x = (document.getPageSize().getLeft() + document.getPageSize().getRight()) / 2;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase(texto, fuente(8, Font.NORMAL, GRIS_TEXTO)), x, 22, 0);
        }

    }

    private void agregarEncabezado(Document document, Integer facultadId, String facultadNombre)
            throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(new float[]{1, 2});
        table.setWidthPercentage(100);

        String logoPath = facultadId != null && facultadId == ETEC_FACULTAD_ID ? ETEC_LOGO : UM_LOGO;
        Image image = Image.getInstance(logoPath);
        image.scaleToFit(125, 65);
        PdfPCell cell = new PdfPCell(image);
        estilarCeldaEncabezado(cell);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);

        cell = new PdfPCell();
        estilarCeldaEncabezado(cell);
        Paragraph universidad = new Paragraph("UNIVERSIDAD DE MENDOZA", fuente(15, Font.BOLD, AZUL));
        universidad.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(universidad);
        Paragraph facultad = new Paragraph(facultadNombre, fuente(12, Font.NORMAL, GRIS_TEXTO));
        facultad.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(facultad);
        table.addCell(cell);

        document.add(table);
    }

    private void estilarCeldaEncabezado(PdfPCell cell) {
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(AZUL);
        cell.setBorderWidthBottom(1.5f);
        cell.setPaddingBottom(8);
        cell.setPaddingLeft(0);
        cell.setPaddingRight(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
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
        Paragraph paragraph = new Paragraph(tipoChequeraNombre, fuente(18, Font.BOLD, AZUL));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(16);
        document.add(paragraph);

        String subtitulo = lectivoNombre + "  ·  " + arancelTipoNombre;
        if (becaPorcentaje != null && becaPorcentaje.signum() > 0) {
            String beca = becaPorcentaje.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
            subtitulo += "  ·  Beca " + beca + "%";
        }
        paragraph = new Paragraph(subtitulo, fuente(10.5f, Font.NORMAL, GRIS_TEXTO));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(3);
        document.add(paragraph);

        List<Dato> izquierda = new ArrayList<>();
        izquierda.add(new Dato("Alumno",
                persona.getApellido() + ", " + persona.getNombre() + "  (" + persona.getPersonaId() + ")", 11.5f));
        if (facultadId != FACULTAD_SIN_CARRERA_ID && carrera != null) {
            izquierda.add(new Dato("Carrera", carrera.getNombre(), 10.5f));
        }
        izquierda.add(new Dato("Medio de pago", tipoImpresion, 10.5f));

        List<Dato> derecha = new ArrayList<>();
        derecha.add(new Dato(chequeraLabel, Objects.requireNonNull(chequeraSerieId).toString(), 11.5f));
        derecha.add(new Dato("Alternativa", String.valueOf(alternativaId), 11.5f));
        derecha.add(new Dato("Código de Pago Electrónico",
                String.format("%02d%03d%05d", facultadId, tipoChequeraId, chequeraSerieId), 13));

        agregarFicha(document, izquierda, derecha);
    }

    private void agregarFicha(Document document, List<Dato> izquierda, List<Dato> derecha)
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 1});
        table.setWidthPercentage(100);
        table.setSpacingBefore(14);
        table.setSpacingAfter(14);

        int filas = Math.max(izquierda.size(), derecha.size());
        for (int i = 0; i < filas; i++) {
            table.addCell(celdaFicha(i < izquierda.size() ? izquierda.get(i) : null));
            table.addCell(celdaFicha(i < derecha.size() ? derecha.get(i) : null));
        }
        document.add(table);
    }

    private PdfPCell celdaFicha(Dato dato) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(GRIS_FONDO);
        cell.setPadding(7);
        cell.setPaddingLeft(12);
        if (dato != null) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(dato.etiqueta().toUpperCase(LOCALE_AR), fuente(7, Font.BOLD, GRIS_TEXTO)));
            p.add(Chunk.NEWLINE);
            p.add(new Chunk(dato.valor(), fuente(dato.tamano(), Font.BOLD, Color.BLACK)));
            cell.addElement(p);
        }
        return cell;
    }

    private void agregarTablaCuota(Document document, PdfWriter writer, ChequeraCuota cuota, ChequeraSerie serie,
                                   PreferenciaPago preferencia, boolean codigoBarras,
                                   List<LectivoAlternativa> alternativas) throws DocumentException {
        var lectivoAlternativa = buscarAlternativa(alternativas, cuota.getProductoId(), serie);
        PdfPTable table = crearEstructuraTablaCuota(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(),
                cuota.getAnho(), cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());

        if (preferencia != null && preferencia.getMercadoPagoContext() != null) {
            agregarEnlaceMercadoPago(table, preferencia.getMercadoPagoContext().getInitPoint());
        }
        if (codigoBarras) {
            agregarCodigoBarras(table, writer, cuota.getCodigoBarras());
        }
        document.add(envolverTarjeta(table));
    }

    private void agregarTablaCuotaReemplazo(Document document, PdfWriter writer, ChequeraCuotaReemplazo cuota,
                                            ChequeraSerieReemplazo serie, List<LectivoAlternativa> alternativas)
            throws DocumentException {
        var lectivoAlternativa = buscarAlternativaReemplazo(alternativas, cuota.getProductoId(), serie);
        PdfPTable table = crearEstructuraTablaCuota(lectivoAlternativa, cuota.getCuotaId(), cuota.getMes(),
                cuota.getAnho(), cuota.getVencimiento1(), cuota.getVencimiento2(), cuota.getVencimiento3(),
                cuota.getImporte1(), cuota.getImporte2(), cuota.getImporte3());
        agregarCodigoBarras(table, writer, cuota.getCodigoBarras());
        document.add(envolverTarjeta(table));
    }

    private PdfPTable envolverTarjeta(PdfPTable interior) {
        PdfPTable externa = new PdfPTable(1);
        externa.setWidthPercentage(100);
        externa.setKeepTogether(true);
        externa.setSpacingAfter(14);

        PdfPCell cell = new PdfPCell(interior);
        cell.setPadding(0);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(GRIS_BORDE);
        cell.setBorderWidth(0.75f);
        externa.addCell(cell);
        return externa;
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

        PdfPCell cell = celdaCuota(new Phrase(
                String.format("%s: %d de %d", lectivoAlternativa.getTitulo(), cuotaId, lectivoAlternativa.getCuotas()),
                fuente(10.5f, Font.BOLD, Color.WHITE)), 2, Element.ALIGN_LEFT);
        cell.setBackgroundColor(AZUL);
        cell.setPadding(7);
        cell.setPaddingLeft(12);
        table.addCell(cell);

        cell = celdaCuota(new Phrase(String.format("Período: %d/%d", mes, anho),
                fuente(10.5f, Font.NORMAL, Color.WHITE)), 2, Element.ALIGN_RIGHT);
        cell.setBackgroundColor(AZUL);
        cell.setPadding(7);
        cell.setPaddingRight(12);
        table.addCell(cell);

        agregarFilaVencimiento(table, "Primer Vencimiento", vencimiento1, importe1, true);
        agregarFilaVencimiento(table, "Segundo Vencimiento", vencimiento2, importe2, false);
        agregarFilaVencimiento(table, "Tercer Vencimiento", vencimiento3, importe3, false);

        return table;
    }

    private PdfPCell celdaCuota(Phrase contenido, int colspan, int alineacion) {
        PdfPCell cell = new PdfPCell(contenido);
        cell.setColspan(colspan);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private void agregarFilaVencimiento(PdfPTable table, String etiqueta, OffsetDateTime fecha, BigDecimal importe,
                                        boolean destacado) {
        float tamano = destacado ? 11 : 9.5f;
        Phrase vencimiento = new Phrase();
        vencimiento.add(new Chunk(etiqueta + ":  ", fuente(tamano - 1, Font.NORMAL, GRIS_TEXTO)));
        vencimiento.add(new Chunk(FORMATO_FECHA.format(fecha.withOffsetSameInstant(ZoneOffset.UTC)),
                fuente(tamano, Font.BOLD, Color.BLACK)));

        PdfPCell izquierda = celdaCuota(vencimiento, 2, Element.ALIGN_LEFT);
        izquierda.setPaddingLeft(12);
        PdfPCell derecha = celdaCuota(new Phrase(formatearImporte(importe),
                fuente(destacado ? 13 : tamano, Font.BOLD, destacado ? AZUL : Color.BLACK)), 2, Element.ALIGN_RIGHT);
        derecha.setPaddingRight(12);

        for (PdfPCell cell : List.of(izquierda, derecha)) {
            cell.setPaddingTop(destacado ? 9 : 5);
            cell.setPaddingBottom(destacado ? 7 : 5);
            table.addCell(cell);
        }
    }

    private String formatearImporte(BigDecimal importe) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(LOCALE_AR));
        return "$ " + formato.format(importe);
    }

    private void agregarEnlaceMercadoPago(PdfPTable table, String initPoint) {
        Chunk link = new Chunk("Click aquí para pagar", fuente(10.5f, Font.UNDERLINE, AZUL_LINK));
        link.setAnchor(initPoint);

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk("Pagá online con MercadoPago:  ", fuente(10, Font.NORMAL, GRIS_TEXTO)));
        paragraph.add(link);

        PdfPCell cell = new PdfPCell(paragraph);
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(GRIS_BORDE);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(11);
        cell.setPaddingBottom(11);
        table.addCell(cell);
    }

    private void agregarCodigoBarras(PdfPTable table, PdfWriter writer, String barcodeText) {
        BarcodeInter25 code25 = new BarcodeInter25();
        code25.setGenerateChecksum(false);
        code25.setCode(barcodeText);
        code25.setX(1.3f);
        Image image = code25.createImageWithBarcode(writer.getDirectContent(), null, null);

        PdfPCell cell = new PdfPCell(image, true);
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(GRIS_BORDE);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(12);
        cell.setPaddingLeft(12);
        cell.setPaddingRight(12);
        cell.setPaddingBottom(2);
        table.addCell(cell);

        cell = celdaCuota(new Phrase(barcodeText, new Font(Font.COURIER, 8, Font.NORMAL, GRIS_TEXTO)), 4,
                Element.ALIGN_CENTER);
        cell.setPaddingBottom(10);
        table.addCell(cell);
    }

}