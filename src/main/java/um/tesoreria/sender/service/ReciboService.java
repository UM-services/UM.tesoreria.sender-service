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
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import um.tesoreria.sender.client.tesoreria.core.ChequeraCuotaClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraFacturacionElectronicaClient;
import um.tesoreria.sender.client.tesoreria.core.ChequeraSerieClient;
import um.tesoreria.sender.client.tesoreria.core.FacturacionElectronicaClient;
import um.tesoreria.sender.kotlin.dto.tesoreria.core.*;

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
public class ReciboService {

    private final Environment environment;
    private final FacturacionElectronicaClient facturacionElectronicaClient;
    private final JavaMailSender javaMailSender;
    private final ChequeraCuotaClient chequeraCuotaClient;
    private final ChequeraSerieClient chequeraSerieClient;
    private final ChequeraFacturacionElectronicaClient chequeraFacturacionElectronicaClient;

    public ReciboService(Environment environment, FacturacionElectronicaClient facturacionElectronicaClient, JavaMailSender javaMailSender,
                         ChequeraCuotaClient chequeraCuotaClient, ChequeraSerieClient chequeraSerieClient,
                         ChequeraFacturacionElectronicaClient chequeraFacturacionElectronicaClient) {
        this.environment = environment;
        this.facturacionElectronicaClient = facturacionElectronicaClient;
        this.javaMailSender = javaMailSender;
        this.chequeraCuotaClient = chequeraCuotaClient;
        this.chequeraSerieClient = chequeraSerieClient;
        this.chequeraFacturacionElectronicaClient = chequeraFacturacionElectronicaClient;
    }

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

        Image imageQr = null;
        if (facturacionElectronica == null) {
            facturacionElectronica = facturacionElectronicaClient.findByFacturacionElectronicaId(facturacionElectronicaId);
            try {
                log.debug("FacturacionElectronicaDto: {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facturacionElectronica));
            } catch (JsonProcessingException e) {
                log.debug("FacturacionElectronicaDto: {}", e.getMessage());
            }
        }
        ComprobanteDto comprobante = facturacionElectronica.getComprobante();
        ChequeraPagoDto chequeraPago = facturacionElectronica.getChequeraPago();
        assert chequeraPago != null;
        ChequeraCuotaDto chequeraCuota = chequeraCuotaClient.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getProductoId(), chequeraPago.getAlternativaId(), chequeraPago.getCuotaId());
        if (chequeraSerie == null) {
            chequeraSerie = chequeraSerieClient.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId());
        }
        ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica = null;
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

    public String send(Long facturacionElectronicaId, FacturacionElectronicaDto facturacionElectronica) throws MessagingException {

        if (facturacionElectronica == null) {
            facturacionElectronica = facturacionElectronicaClient.findByFacturacionElectronicaId(facturacionElectronicaId);
        }
        ChequeraSerieDto chequeraSerie = chequeraSerieClient.findByUnique(Objects.requireNonNull(facturacionElectronica.getChequeraPago()).getFacultadId(), facturacionElectronica.getChequeraPago().getTipoChequeraId(), facturacionElectronica.getChequeraPago().getChequeraSerieId());
        ChequeraPagoDto chequeraPago = facturacionElectronica.getChequeraPago();
        String chequeraString = MessageFormat.format("Chequera {0}/{1}/{2}/{3}/{4}/{5}", chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getAlternativaId(), chequeraPago.getProductoId(), chequeraPago.getCuotaId());
        ChequeraFacturacionElectronicaDto chequeraFacturacionElectronica = null;
        try {
            chequeraFacturacionElectronica = chequeraFacturacionElectronicaClient.findByChequeraId(chequeraSerie.getChequeraId());
        } catch (Exception e) {
            chequeraFacturacionElectronica = new ChequeraFacturacionElectronicaDto();
        }

        // Genera PDF
        String filenameRecibo = this.generatePdf(facturacionElectronicaId, facturacionElectronica, chequeraSerie);
        log.info("Filename_recibo -> " + filenameRecibo);
        if (filenameRecibo.isEmpty()) {
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            return MessageFormat.format("ERROR: {0} Sin Recibo para ENVIAR", chequeraString);
        }

        DomicilioDto domicilio = chequeraSerie.getDomicilio();
        if (domicilio == null) {
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            return MessageFormat.format("ERROR: {0} Sin Recibo para ENVIAR", chequeraString);
        }
        if (domicilio.getEmailPersonal().isEmpty() && domicilio.getEmailInstitucional().isEmpty()) {
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            return MessageFormat.format("ERROR: {0} Sin e-mails para ENVIAR", chequeraString);
        }

        String data = "";

        data = "Estimad@ Estudiante:" + (char) 10;
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
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        List<String> addresses = new ArrayList<String>();

        if (!domicilio.getEmailPersonal().isEmpty()) {
            addresses.add(domicilio.getEmailPersonal());
        }
        if (!domicilio.getEmailInstitucional().isEmpty()) {
            addresses.add(domicilio.getEmailInstitucional());
        }
        if (!chequeraFacturacionElectronica.getEmail().isEmpty()) {
            addresses.add(chequeraFacturacionElectronica.getEmail());
        }

//		addresses.add("daniel.quinterospinto@gmail.com");

        try {
            helper.setTo(addresses.toArray(new String[0]));
            helper.setText(data);
            helper.setReplyTo("no-reply@um.edu.ar");
            helper.setSubject("Envío Automático de Recibo -> " + filenameRecibo);

            FileSystemResource fileRecibo = new FileSystemResource(filenameRecibo);
            helper.addAttachment(filenameRecibo, fileRecibo);

        } catch (MessagingException e) {
            facturacionElectronica.setRetries(facturacionElectronica.getRetries() + 1);
            facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
            return MessageFormat.format("ERROR: {0} No pudo ENVIARSE", chequeraString);
        }

        javaMailSender.send(message);
        facturacionElectronica.setEnviada((byte) 1);
        facturacionElectronica = facturacionElectronicaClient.update(facturacionElectronica, facturacionElectronicaId);
        String json = "";
        try {
            json = JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facturacionElectronica);
        } catch (JsonProcessingException e) {
            json = "";
        }
        return MessageFormat.format("{0} Envío de Correo Ok!!!", chequeraString);
    }

    public String sendNext() {
        FacturacionElectronicaDto facturacionElectronica = facturacionElectronicaClient.findNextPendiente();
        try {
            log.info("SendNext: {}", JsonMapper.builder().findAndAddModules().build().writerWithDefaultPrettyPrinter().writeValueAsString(facturacionElectronica));
        } catch (JsonProcessingException e) {
            log.info("SendNext jsonify error: {}", e.getMessage());
        }
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
            return MessageFormat.format("ERROR: Chequera {0}/{1}/{2}/{3}/{4}/{5} Problemas de Envío", chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getAlternativaId(), chequeraPago.getProductoId(), chequeraPago.getCuotaId());
        }
    }
}

