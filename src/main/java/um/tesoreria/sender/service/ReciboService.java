package um.tesoreria.sender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.openjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.*;
import um.tesoreria.sender.client.tesoreria.core.facade.ToolClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;
import um.tesoreria.sender.service.util.Tool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReciboService {

    private final ReciboMessageCheckClient reciboMessageCheckClient;
    private final ChequeraPagoClient chequeraPagoClient;
    private final ComprobanteClient comprobanteClient;
    private final ToolClient toolClient;

    @Value("${app.testing}")
    private Boolean testing;

    private final Environment environment;
    private final FacturacionElectronicaClient facturacionElectronicaClient;
    private final JavaMailSender javaMailSender;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraFacturacionElectronicaClient chequeraFacturacionElectronicaClient;

    private void createQRImage(File qrFile, String qrCodeText, int size, String fileType)
            throws WriterException, IOException {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        var matrixHeight = matrixWidth;
        BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixHeight);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }

    public String generatePdf(Long facturacionElectronicaId, FacturacionElectronicaDto facturacionElectronica, ChequeraSerieDto chequeraSerie) {
        log.debug("Processing ReciboService.generatePdf");
        Image imageQr = null;
        if (facturacionElectronica == null) {
            facturacionElectronica = facturacionElectronicaClient.findByFacturacionElectronicaId(facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);
        }
        ComprobanteDto comprobante = facturacionElectronica.getComprobante();
        if (comprobante == null) {
            comprobante = comprobanteClient.findByComprobanteId(facturacionElectronica.getComprobanteId());
        }
        ChequeraPagoDto chequeraPago = facturacionElectronica.getChequeraPago();
        if (chequeraPago == null) {
            chequeraPago = chequeraPagoClient.findByChequeraPagoId(facturacionElectronica.getChequeraPagoId());
        }
        assert chequeraPago != null;
        ChequeraCuotaDto chequeraCuota = chequeraCuotaClient.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getProductoId(), chequeraPago.getAlternativaId(), chequeraPago.getCuotaId());
        if (chequeraSerie == null) {
            chequeraSerie = chequeraSerieClient.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId());
        }
        ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica;
        try {
            chequeraFacturacionElectronica = chequeraFacturacionElectronicaClient.findByChequeraId(chequeraSerie.getChequeraId());
        } catch (Exception e) {
            chequeraFacturacionElectronica = new ChequeraFacturacionElectronicaDto();
        }

        String path = environment.getProperty("path.reports");
        String empresaCuit = "30-51859446-6";

        try {
            String url = "https://www.afip.gob.ar/fe/qr/?p=";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ver", 1);
            jsonObject.put("fecha", DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .format(Objects.requireNonNull(facturacionElectronica.getFechaRecibo())));
            jsonObject.put("cuit", Long.parseLong(empresaCuit.replaceAll("-", "")));
            assert comprobante != null;
            jsonObject.put("ptoVta", comprobante.getPuntoVenta());
            jsonObject.put("tipoCmp", comprobante.getComprobanteAfipId());
            jsonObject.put("nroCmp", facturacionElectronica.getNumeroComprobante());
            jsonObject.put("importe", facturacionElectronica.getImporte());
            jsonObject.put("moneda", "PES");
            jsonObject.put("ctz", 1);
            jsonObject.put("tipoDocRec", facturacionElectronica.getTipoDocumento());
            jsonObject.put("nroDocRec", facturacionElectronica.getPersonaId());
            jsonObject.put("tipoCodAut", "E");
            jsonObject.put("codAut", new BigDecimal(Objects.requireNonNull(facturacionElectronica.getCae())));
            String datos = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes()));
            String fileType = "png";
            String filePath = path + facturacionElectronica.getCae() + "." + fileType;
            int size = 150;
            File qrFile = new File(filePath);
            createQRImage(qrFile, url + datos, size, fileType);
            imageQr = Image.getInstance(filePath);
        } catch (BadElementException | WriterException | IOException e) {
            log.debug("Sin Imagen");
        }

        int copias = 2;

        String[] titulo_copias = {"ORIGINAL", "DUPLICADO"};

        String filename = "";
        List<String> filenames = new ArrayList<>();
        for (int copia = 0; copia < copias; copia++) {
            filenames.add(filename = path + facturacionElectronicaId + "." + titulo_copias[copia].toLowerCase() + ".pdf");

            makePage(filename, titulo_copias[copia], comprobante, facturacionElectronica, chequeraCuota, chequeraPago, chequeraSerie, chequeraFacturacionElectronica, imageQr);
        }

        try {
            mergePdf(filename = path + facturacionElectronicaId + ".pdf", filenames);
        } catch (DocumentException ex) {
            log.info("Document Exception in PDF generation");
        } catch (IOException ex) {
            log.info("IOException in PDF generation");
        }

        return filename;
    }

    private void mergePdf(String filename, List<String> filenames) throws DocumentException, IOException {
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

    private void makePage(String filename, String titulo, ComprobanteDto comprobante,
                          FacturacionElectronicaDto facturacionElectronica, ChequeraCuotaDto chequeraCuota, ChequeraPagoDto chequeraPago, ChequeraSerieDto chequeraSerie,
                          ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica, Image imageQr) {
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
            paragraph.add(new Phrase("Boulogne Sur Mer 683 - Mendoza - 4202017", new Font(Font.HELVETICA, 10, Font.BOLD)));
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
            paragraph.add(
                    new Phrase(Objects.requireNonNull(comprobante.getComprobanteAfipId()).toString(), new Font(Font.HELVETICA, 6, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(Objects.requireNonNull(comprobante.getComprobanteAfip()).getLabel(), new Font(Font.HELVETICA, 14, Font.BOLD));
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
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("CUIT: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("30-51859446-6", new Font(Font.HELVETICA, 8, Font.BOLD)));
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
            paragraph.add(new Phrase(facturacionElectronica.getApellido() + ", " + facturacionElectronica.getNombre(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Domicilio: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            String domicilio = MessageFormat.format("{0} {1} {2}", Objects.requireNonNull(chequeraSerie.getDomicilio()).getCalle(), chequeraSerie.getDomicilio().getPuerta(), chequeraSerie.getDomicilio().getObservaciones());
            paragraph.add(new Phrase(domicilio, new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase(facturacionElectronica.getTipoDocumento() + ": ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getCuit(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.add(new Phrase("                Condición IVA: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph
                    .add(new Phrase(facturacionElectronica.getCondicionIva(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Condición de venta: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("Contado",
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
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

            // Producto
            lineas--;
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            DecimalFormat decimalFormat = new DecimalFormat("###0");
            String codigo = MessageFormat.format("{0}.{1}.{2}.{3}.{4}.{5}", chequeraCuota.getFacultadId(), chequeraCuota.getTipoChequeraId(), decimalFormat.format(chequeraCuota.getChequeraSerieId()), chequeraCuota.getProductoId(), chequeraCuota.getAlternativaId(), chequeraCuota.getCuotaId());
            paragraph = new Paragraph(codigo, new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Alumno: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraSerie.getPersona()).getApellidoNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(String.valueOf(1),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(
                    new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(
                    new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Producto
            lineas--;
            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Tipo: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraCuota.getProducto()).getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Facultad
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Unidad Académica: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraCuota.getFacultad()).getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Sede
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Sede: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(Objects.requireNonNull(chequeraCuota.getTipoChequera()).getGeografica()).getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Tipo de Chequera
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Chequera: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraCuota.getTipoChequera().getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Arancel Tipo
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Tipo de Arancel: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraSerie.getArancelTipo()).getDescripcion(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Periodo
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Periodo: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(MessageFormat.format("{0}/{1}", chequeraCuota.getMes(), decimalFormat.format(chequeraCuota.getAnho())), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Fecha Pago
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Fecha Pago: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(chequeraPago.getFecha())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

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

            // Datos CAE
            float[] columnCAE = {1, 3};
            PdfPTable tableCAE = new PdfPTable(columnCAE);
            tableCAE.setWidthPercentage(100);

            // Agrega código QR
            cell = new PdfPCell();
            cell.addElement(imageQr);
            cell.setBorder(Rectangle.NO_BORDER);
            tableCAE.addCell(cell);
            //

            paragraph = new Paragraph("CAE Nro: ", new Font(Font.COURIER, 10, Font.NORMAL));
            paragraph.add(new Phrase(facturacionElectronica.getCae(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.add(new Phrase("\n", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase("Vencimiento CAE: ", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase(Objects.requireNonNull(facturacionElectronica.getFechaVencimientoCae())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 10, Font.BOLD)));
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setLeading(0, 1.5f);
            tableCAE.addCell(cell);
            document.add(tableCAE);
            document.close();
        } catch (Exception ex) {
            log.debug(ex.getMessage());
        }

    }

    public String send(Long facturacionElectronicaId, FacturacionElectronicaDto facturacionElectronica) {
        log.debug("Processing ReciboService.send()");

        if (facturacionElectronica == null) {
            facturacionElectronica = facturacionElectronicaClient.findByFacturacionElectronicaId(facturacionElectronicaId);
        }
        logFacturacionElectronica(facturacionElectronica);
        ChequeraPagoDto chequeraPago = facturacionElectronica.getChequeraPago();
        if (chequeraPago == null) {
            chequeraPago = chequeraPagoClient.findByChequeraPagoId(facturacionElectronica.getChequeraPagoId());
        }
        logChequeraPago(chequeraPago);

        ChequeraSerieDto chequeraSerie = chequeraSerieClient.findByUnique(Objects.requireNonNull(facturacionElectronica.getChequeraPago()).getFacultadId(), facturacionElectronica.getChequeraPago().getTipoChequeraId(), facturacionElectronica.getChequeraPago().getChequeraSerieId());
        logChequeraSerie(chequeraSerie);
        String cuotaString = MessageFormat.format("Recibo de Cuota {0}/{1}/{2}/{3}/{4}/{5}", chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getAlternativaId(), chequeraPago.getProductoId(), chequeraPago.getCuotaId());
        ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica;
        try {
            chequeraFacturacionElectronica = chequeraFacturacionElectronicaClient.findByChequeraId(chequeraSerie.getChequeraId());
            logChequeraFacturacionElectronica(chequeraFacturacionElectronica);
        } catch (Exception e) {
            chequeraFacturacionElectronica = new ChequeraFacturacionElectronicaDto();
        }

        // Genera PDF
        String filenameRecibo = this.generatePdf(facturacionElectronicaId, facturacionElectronica, chequeraSerie);
        log.info("Filename_recibo -> {}", filenameRecibo);
        if (filenameRecibo.isEmpty()) {
            log.debug("Sin Recibo para ENVIAR");
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);

            return MessageFormat.format("ERROR: {0} Sin Recibo para ENVIAR", cuotaString);
        }

        DomicilioDto domicilio = chequeraSerie.getDomicilio();
        if (domicilio == null) {
            log.debug("Sin Domicilio para ENVIAR");
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);
            return MessageFormat.format("ERROR: {0} Sin Recibo para ENVIAR", cuotaString);
        }
        if (domicilio.getEmailPersonal().isEmpty() && domicilio.getEmailInstitucional().isEmpty()) {
            log.debug("Sin e-mails para ENVIAR");
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);
            return MessageFormat.format("ERROR: {0} Sin e-mails para ENVIAR", cuotaString);
        }

        String data = "Estimad@ Estudiante:" + (char) 10;
        data = data + (char) 10;
        data = data + "Le enviamos como archivo adjunto su recibo." + (char) 10;
        data = data + (char) 10;
        data = data + "Atentamente." + (char) 10;
        data = data + (char) 10;
        data = data + "Universidad de Mendoza" + (char) 10;
        data = data + (char) 10;
        data = data + (char) 10
                + "Por favor no responda este mail, fue generado automáticamente. Su respuesta no será leída."
                + (char) 10;

        // Envia correo
        MimeMessage message = javaMailSender.createMimeMessage();
        List<String> addresses = new ArrayList<>();

        if (!testing) {
            if (!domicilio.getEmailPersonal().isEmpty()) {
                if (toolClient.mailValidate(Tool.convertStringToList(domicilio.getEmailPersonal()))) {
                    addresses.add(domicilio.getEmailPersonal());
                    log.debug("adding personal email -> {}", domicilio.getEmailPersonal());
                }
            }
            if (!domicilio.getEmailInstitucional().isEmpty()) {
                if (toolClient.mailValidate(Tool.convertStringToList(domicilio.getEmailInstitucional()))) {
                    addresses.add(domicilio.getEmailInstitucional());
                    log.debug("adding institutional email -> {}", domicilio.getEmailInstitucional());
                }
            }
            if (!chequeraFacturacionElectronica.getEmail().isEmpty()) {
                if (toolClient.mailValidate(Tool.convertStringToList(chequeraFacturacionElectronica.getEmail()))) {
                    addresses.add(chequeraFacturacionElectronica.getEmail());
                    log.debug("adding chequera email -> {}", chequeraFacturacionElectronica.getEmail());
                }
            }
        }

        if (testing) {
            log.debug("Testing -> daniel.quinterospinto@gmail.com");
            addresses.add("daniel.quinterospinto@gmail.com");
        }

        if (addresses.isEmpty()) {
            log.debug("Sin e-mails para ENVIAR");
            return "ERROR: Sin e-mails para ENVIAR";
        }

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(addresses.toArray(new String[0]));
            helper.setText(data);
            helper.setReplyTo("no-reply@um.edu.ar");
            helper.setSubject("Envío Automático de Recibo -> " + filenameRecibo);

            FileSystemResource fileRecibo = new FileSystemResource(filenameRecibo);
            helper.addAttachment(filenameRecibo, fileRecibo);

            javaMailSender.send(message);

        } catch (org.springframework.mail.MailSendException e) {
            log.error("Error al enviar el correo por dirección inválida: {}", e.getMessage());
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);
            return MessageFormat.format("ERROR: {0} No pudo ENVIARSE. Dirección de correo inválida.", cuotaString);
        } catch (MessagingException e) {
            log.debug("No pudo ENVIARSE");
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            logFacturacionElectronica(facturacionElectronica);
            return MessageFormat.format("ERROR: {0} No pudo ENVIARSE", cuotaString);
        }

        log.debug("Mail enviado");
        facturacionElectronica.setEnviada((byte) 1);
        facturacionElectronica = facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
        // Agregado por perder la referencia a chequeraPago en el update
        facturacionElectronica.setChequeraPago(chequeraPago);
        logFacturacionElectronica(facturacionElectronica);
        var reciboMessageCheck = new ReciboMessageCheckDto.Builder()
                .reciboMessageCheckId(UUID.randomUUID())
                .facturacionElectronicaId(facturacionElectronica.getFacturacionElectronicaId())
                .chequeraPagoId(facturacionElectronica.getChequeraPagoId())
                .facultadId(Objects.requireNonNull(facturacionElectronica.getChequeraPago()).getFacultadId())
                .tipoChequeraId(facturacionElectronica.getChequeraPago().getTipoChequeraId())
                .chequeraSerieId(facturacionElectronica.getChequeraPago().getChequeraSerieId())
                .productoId(facturacionElectronica.getChequeraPago().getProductoId())
                .alternativaId(facturacionElectronica.getChequeraPago().getAlternativaId())
                .cuotaId(facturacionElectronica.getChequeraPago().getCuotaId())
                .build();
        reciboMessageCheck = reciboMessageCheckClient.add(reciboMessageCheck);
        logReciboMessageCheck(reciboMessageCheck);
        return MessageFormat.format("{0} Envío de Correo Ok!!!", cuotaString);
    }

    private void logChequeraFacturacionElectronica(ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica) {
        log.debug("Processing ReciboService.logChequeraFacturacionElectronica()");
        try {
            log.debug("ChequeraFacturacionElectronica -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraFacturacionElectronica));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraFacturacionElectronica jsonify error -> {}", e.getMessage());
        }
    }

    private void logChequeraPago(ChequeraPagoDto chequeraPago) {
        log.debug("Processing ReciboService.logChequeraPago()");
        try {
            log.debug("ChequeraPago -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraPago));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraPago jsonify error -> {}", e.getMessage());
        }
    }

    private void logChequeraSerie(ChequeraSerieDto chequeraSerie) {
        log.debug("Processing ReciboService.logChequeraSerie()");
        try {
            log.debug("ChequeraSerie -> {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(chequeraSerie));
        } catch (JsonProcessingException e) {
            log.debug("ChequeraSerie jsonify error -> {}", e.getMessage());
        }
    }

    private void logReciboMessageCheck(ReciboMessageCheckDto reciboMessageCheck) {
        try {
            log.debug("ReciboMessageCheck: {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(reciboMessageCheck));
        } catch (JsonProcessingException e) {
            log.debug("ReciboMessageCheck jsonify error: {}", e.getMessage());
        }
    }

    private void logFacturacionElectronica(FacturacionElectronicaDto facturacionElectronica) {
        try {
            log.debug("FacturacionElectronica: {}", JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(facturacionElectronica));
        } catch (JsonProcessingException e) {
            log.debug("FacturacionElectronica jsonify error: {}", e.getMessage());
        }
    }

    public String sendNext() {
        log.debug("Processing ReciboService.sendNext()");
        FacturacionElectronicaDto facturacionElectronica = facturacionElectronicaClient.findNextPendiente();
        logFacturacionElectronica(facturacionElectronica);
        try {
            return this.send(facturacionElectronica.getFacturacionElectronicaId(), facturacionElectronica);
        } catch (Exception e) {
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronica = facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronica.getFacturacionElectronicaId());
            try {
                log.info("SendError: {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facturacionElectronica));
            } catch (JsonProcessingException j) {
                log.info("SendError: JSON Exception");
            }
            ChequeraPagoDto chequeraPago = facturacionElectronica.getChequeraPago();
            assert chequeraPago != null;
            return MessageFormat.format("ERROR: Cuota {0}/{1}/{2}/{3}/{4}/{5} Problemas de Envío", chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getAlternativaId(), chequeraPago.getProductoId(), chequeraPago.getCuotaId());
        }
    }

    public void sendPendientes() {
        log.debug("Processing ReciboService.sendPendientes()");
        for (FacturacionElectronicaDto facturacionElectronica : facturacionElectronicaClient.find100Pendientes()) {
            send(facturacionElectronica.getFacturacionElectronicaId(), facturacionElectronica);
        }
    }
}

