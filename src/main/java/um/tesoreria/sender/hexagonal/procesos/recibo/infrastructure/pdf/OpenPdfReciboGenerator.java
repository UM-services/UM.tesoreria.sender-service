package um.tesoreria.sender.hexagonal.procesos.recibo.infrastructure.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfImportedPage;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.ports.out.ReciboPdfGenerator;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;
import um.tesoreria.sender.hexagonal.shared.domain.model.FacturacionElectronica;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenPdfReciboGenerator implements ReciboPdfGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CUIT_EMPRESA = "30-51859446-6";

    private final QrAfipGenerator qrAfipGenerator;

    @Value("${path.reports}")
    private String pathReports;

    @Override
    public DocumentoPdf generar(Long facturacionElectronicaId, DatosRecibo datos) {
        log.debug("\n\nProcessing OpenPdfReciboGenerator.generar\n\n");
        String path = pathReports;
        Image imageQr = qrAfipGenerator.generar(datos, path);

        String[] titulosCopias = {"ORIGINAL", "DUPLICADO"};
        String filename = "";
        List<String> filenames = new ArrayList<>();
        for (int copia = 0; copia < titulosCopias.length; copia++) {
            filename = path + facturacionElectronicaId + "." + titulosCopias[copia].toLowerCase() + ".pdf";
            filenames.add(filename);
            hacerPagina(filename, titulosCopias[copia], datos, imageQr);
        }

        filename = path + facturacionElectronicaId + ".pdf";
        try {
            unirPdf(filename, filenames);
        } catch (DocumentException ex) {
            log.info("Document Exception in PDF generation");
        } catch (IOException ex) {
            log.info("IOException in PDF generation");
        }
        log.debug("\n\nFilename -> {}\n\n", filename);
        return DocumentoPdf.de(filename);
    }

    private void unirPdf(String filename, List<String> filenames) throws DocumentException, IOException {
        log.debug("\n\nMerging pdf\n\n");
        OutputStream outputStream = new FileOutputStream(filename);
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        document.open();
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        for (String name : filenames) {
            PdfReader pdfReader = new PdfReader(new FileInputStream(name));
            for (int pagina = 0; pagina < pdfReader.getNumberOfPages(); ) {
                document.newPage();
                PdfImportedPage page = pdfWriter.getImportedPage(pdfReader, ++pagina);
                pdfContentByte.addTemplate(page, 0, 0);
            }
        }
        outputStream.flush();
        document.close();
        outputStream.close();
    }

    private void hacerPagina(String filename, String titulo, DatosRecibo datos, Image imageQr) {
        log.debug("\n\nMaking PDF Page\n\n");
        var facturacionElectronica = datos.facturacionElectronica();
        var comprobante = datos.comprobante();
        var chequeraCuota = datos.chequeraCuota();
        var chequeraPago = datos.chequeraPago();
        var chequeraSerie = datos.chequeraSerie();

        PdfPTable table;
        PdfPCell cell;

        Document document = new Document(new Rectangle(PageSize.A4));
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.setMargins(20, 20, 20, 20);
            document.open();

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            Paragraph paragraph = new Paragraph(titulo, new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell = new PdfPCell();
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{48, 4, 48});
            cell = new PdfPCell();
            paragraph = new Paragraph("Universidad de Mendoza", new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            cell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            paragraph = new Paragraph(new Phrase("Razón Social: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("Universidad de Mendoza", new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Domicilio: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("Boulogne Sur Mer 683 - Mendoza - 4202017",
                    new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            paragraph = new Paragraph(
                    new Phrase("Condición frente al IVA: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("IVA Sujeto Exento", new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(comprobante.getLetraComprobante(), new Font(Font.HELVETICA, 24, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Cod: ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(comprobante.getComprobanteAfipId()).toString(),
                    new Font(Font.HELVETICA, 6, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(Objects.requireNonNull(comprobante.getComprobanteAfip()).getLabel(),
                    new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Punto de Venta: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(new DecimalFormat("0000").format(comprobante.getPuntoVenta()),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.add(new Phrase("          Comprobante Nro: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(new DecimalFormat("00000000").format(facturacionElectronica.getNumeroComprobante()),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Fecha de Emisión: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(facturacionElectronica.getFechaRecibo())
                    .format(FORMATO_FECHA), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("CUIT: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(CUIT_EMPRESA, new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Ingresos Brutos: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("0729590", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Inicio Actividades: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("05/1960", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            cell = new PdfPCell();
            paragraph = new Paragraph(new Phrase("Cliente: ", new Font(Font.HELVETICA, 10, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getApellido() + ", "
                    + facturacionElectronica.getNombre(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Domicilio: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            String domicilio = MessageFormat.format("{0} {1} {2}",
                    Objects.requireNonNull(chequeraSerie.getDomicilio()).getCalle(),
                    chequeraSerie.getDomicilio().getPuerta(), chequeraSerie.getDomicilio().getObservaciones());
            paragraph.add(new Phrase(domicilio, new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase(facturacionElectronica.getTipoDocumento() + ": ",
                    new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getCuit(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.add(new Phrase("                Condición IVA: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getCondicionIva(),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Condición de venta: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("Contado", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            cell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.BOLD)));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{20, 50, 7, 12, 12});
            cell = new PdfPCell();
            paragraph = new Paragraph("Código", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Detalle", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Cantidad", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Precio Unitario", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Subtotal", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            int lineas = 24;

            table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{20, 50, 7, 12, 12});

            lineas--;
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            DecimalFormat decimalFormat = new DecimalFormat("###0");
            String codigo = MessageFormat.format("{0}.{1}.{2}.{3}.{4}.{5}", chequeraCuota.getFacultadId(),
                    chequeraCuota.getTipoChequeraId(), decimalFormat.format(chequeraCuota.getChequeraSerieId()),
                    chequeraCuota.getProductoId(), chequeraCuota.getAlternativaId(), chequeraCuota.getCuotaId());
            paragraph = new Paragraph(codigo, new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Alumno: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraSerie.getPersona()).apellidoNombre(),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(String.valueOf(1), new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            lineas--;
            table = agregarFilaDetalle(table, "Tipo: ",
                    Objects.requireNonNull(chequeraCuota.getProducto()).getNombre());

            lineas--;
            table = agregarFilaDetalle(table, "Unidad Académica: ",
                    Objects.requireNonNull(chequeraCuota.getFacultad()).getNombre());

            lineas--;
            table = agregarFilaDetalle(table, "Sede: ", Objects.requireNonNull(
                    Objects.requireNonNull(chequeraCuota.getTipoChequera()).getGeografica()).getNombre());

            lineas--;
            table = agregarFilaDetalle(table, "Chequera: ",
                    Objects.requireNonNull(chequeraCuota.getTipoChequera()).getNombre());

            lineas--;
            table = agregarFilaDetalle(table, "Tipo de Arancel: ",
                    Objects.requireNonNull(chequeraSerie.getArancelTipo()).getDescripcion());

            lineas--;
            table = agregarFilaDetalle(table, "Periodo: ",
                    MessageFormat.format("{0}/{1}", chequeraCuota.getMes(),
                            decimalFormat.format(chequeraCuota.getAnho())));

            lineas--;
            table = agregarFilaDetalle(table, "Fecha Pago: ",
                    Objects.requireNonNull(chequeraPago.getFecha()).format(FORMATO_FECHA));

            document.add(table);

            for (int i = 0; i < lineas; i++) {
                table = new PdfPTable(1);
                table.setWidthPercentage(100);
                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.addElement(new Paragraph("  ", new Font(Font.COURIER, 8, Font.NORMAL)));
                table.addCell(cell);
                document.add(table);
            }

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            paragraph = new Paragraph(new Phrase("Observaciones: ", new Font(Font.COURIER, 10, Font.BOLD)));
            String observaciones = "";
            paragraph.add(new Phrase(observaciones, new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell = new PdfPCell();
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            cell = new PdfPCell();
            paragraph = new Paragraph(new Phrase("Importe Total: $ ", new Font(Font.COURIER, 10, Font.BOLD)));
            paragraph.add(new Phrase(new DecimalFormat("#,##0.00").format(facturacionElectronica.getImporte().abs()),
                    new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            float[] columnCAE = {1, 3};
            PdfPTable tableCAE = new PdfPTable(columnCAE);
            tableCAE.setWidthPercentage(100);

            cell = new PdfPCell();
            if (imageQr != null) {
                cell.addElement(imageQr);
            } else {
                cell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 8)));
            }
            cell.setBorder(Rectangle.NO_BORDER);
            tableCAE.addCell(cell);

            paragraph = new Paragraph("CAE Nro: ", new Font(Font.COURIER, 10, Font.NORMAL));
            paragraph.add(new Phrase(facturacionElectronica.getCae(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.add(new Phrase("\n", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase("Vencimiento CAE: ", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(facturacionElectronica.getFechaVencimientoCae())
                    .format(FORMATO_FECHA), new Font(Font.HELVETICA, 10, Font.BOLD)));
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setLeading(0, 1.5f);
            tableCAE.addCell(cell);
            document.add(tableCAE);
            document.close();
        } catch (Exception e) {
            log.error("Error generando makePage -> {}", e.getMessage());
        }
    }

    private PdfPTable agregarFilaDetalle(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL)));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph paragraph = new Paragraph(new Phrase(label, new Font(Font.HELVETICA, 8, Font.NORMAL)));
        paragraph.add(new Phrase(value, new Font(Font.HELVETICA, 8, Font.BOLD)));
        paragraph.setAlignment(Element.ALIGN_LEFT);
        cell.addElement(paragraph);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.addElement(new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            table.addCell(cell);
        }
        return table;
    }

}
