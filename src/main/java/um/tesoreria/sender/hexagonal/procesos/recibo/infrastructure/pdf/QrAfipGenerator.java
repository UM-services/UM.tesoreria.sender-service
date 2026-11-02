package um.tesoreria.sender.hexagonal.procesos.recibo.infrastructure.pdf;

import com.github.openjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Image;
import org.springframework.stereotype.Component;
import um.tesoreria.sender.hexagonal.procesos.recibo.domain.model.DatosRecibo;

@Component
@Slf4j
public class QrAfipGenerator {

    private static final String EMPRESA_CUIT = "30-51859446-6";
    private static final String URL_AFIP = "https://www.afip.gob.ar/fe/qr/?p=";
    private static final String FILE_TYPE = "png";
    private static final int SIZE = 150;

    public Image generar(DatosRecibo datos, String path) {
        var facturacionElectronica = datos.facturacionElectronica();
        var comprobante = datos.comprobante();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ver", 1);
            jsonObject.put("fecha", DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .format(Objects.requireNonNull(facturacionElectronica.getFechaRecibo())));
            jsonObject.put("cuit", Long.parseLong(EMPRESA_CUIT.replace("-", "")));
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
            String encoded = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes());
            String filePath = path + facturacionElectronica.getCae() + "." + FILE_TYPE;
            File qrFile = new File(filePath);
            crearImagen(qrFile, URL_AFIP + encoded, SIZE, FILE_TYPE);
            return Image.getInstance(filePath);
        } catch (Exception e) {
            log.debug("Sin Imagen");
            return null;
        }
    }

    private void crearImagen(File qrFile, String qrCodeText, int size, String fileType)
            throws WriterException, IOException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
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

}
