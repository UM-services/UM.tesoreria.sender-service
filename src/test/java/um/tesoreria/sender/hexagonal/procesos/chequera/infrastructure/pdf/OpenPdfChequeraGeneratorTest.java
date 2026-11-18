package um.tesoreria.sender.hexagonal.procesos.chequera.infrastructure.pdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.sender.hexagonal.datos.catalogo.application.service.CatalogoDatosService;
import um.tesoreria.sender.hexagonal.procesos.chequera.domain.model.DatosBorradorChequera;
import um.tesoreria.sender.hexagonal.shared.domain.model.DocumentoPdf;

/**
 * El documento del borrador debe mostrar la leyenda BORRADOR y no debe contener ningún elemento
 * pagable (código de barras ni código de pago electrónico).
 */
class OpenPdfChequeraGeneratorTest {

    @Test
    void elBorradorSeRenderizaConLeyendaYsinDatosDePago(@TempDir Path tempDir) throws Exception {
        OpenPdfChequeraGenerator generator = new OpenPdfChequeraGenerator(
                org.mockito.Mockito.mock(CatalogoDatosService.class));
        ReflectionTestUtils.setField(generator, "pathReports", tempDir.toString() + "/");

        DocumentoPdf documento = generator.generarBorradorChequera(datos());

        assertFalse(documento.estaVacio(), "El borrador debe generar un PDF");
        Path pdf = Path.of(documento.ruta());
        assertTrue(Files.exists(pdf));
        assertTrue(pdf.getFileName().toString().startsWith("borrador-chequera-"));
        assertTrue(Files.size(pdf) > 0);

        String texto = extraerTexto(pdf);
        assertTrue(texto.contains("BORRADOR"), "Debe incluir la leyenda BORRADOR");
        assertTrue(texto.contains("S/N"), "El número debe ser S/N");
        assertTrue(texto.contains("DOCUMENTO SIN VALIDEZ"));
        assertTrue(texto.contains("Total adeudado"));
        assertFalse(texto.contains("Emitido el"), "Un borrador no debe decir Emitido el");
        assertFalse(texto.contains("Matrícula pagada"), "No debe imprimir cuotas pagadas");
        assertFalse(texto.contains("Cuota compensada"), "No debe imprimir cuotas compensadas");
        assertFalse(texto.contains("Cuota de baja"), "No debe imprimir cuotas de baja");
        assertFalse(texto.contains("PAGADA"), "El borrador no imprime cuotas pagadas");
        assertFalse(texto.contains("Cuota sin importe"), "El borrador no imprime cuotas sin importe");
        assertFalse(texto.contains("Código de Pago Electrónico"),
                "Un borrador no debe imprimir el código de pago electrónico");
    }

    private static String extraerTexto(Path pdf) throws Exception {
        try (org.openpdf.text.pdf.PdfReader reader = new org.openpdf.text.pdf.PdfReader(pdf.toString())) {
            return new org.openpdf.text.pdf.parser.PdfTextExtractor(reader).getTextFromPage(1);
        }
    }

    private static DatosBorradorChequera datos() {
        OffsetDateTime fecha = OffsetDateTime.of(2026, 3, 10, 0, 0, 0, 0, ZoneOffset.UTC);
        return new DatosBorradorChequera("Pérez", "Ana", new BigDecimal("49024730"), 5, "Ingeniería",
                2, "Cuota", "Lectivo 2026 - 2027", "Arancel completo", 1, "Cuota",
                new BigDecimal("0.25"), fecha, "Ingeniería en Sistemas", new BigDecimal("4500.00"),
                List.of(
                        new DatosBorradorChequera.CuotaBorrador(3, "Cuota", 1, 1, 3, 2026,
                                fecha, fecha.plusDays(10), fecha.plusDays(20),
                                new BigDecimal("2100.00"), new BigDecimal("2200.00"), new BigDecimal("2300.00"),
                                false, false, false),
                        // Pagada, compensada y de baja: no deben imprimirse.
                        new DatosBorradorChequera.CuotaBorrador(8, "Matrícula pagada", 1, 1, 3, 2026,
                                fecha, fecha.plusDays(10), fecha.plusDays(20),
                                new BigDecimal("347000.00"), new BigDecimal("347000.00"), new BigDecimal("347000.00"),
                                true, false, false),
                        new DatosBorradorChequera.CuotaBorrador(9, "Cuota compensada", 1, 1, 3, 2026,
                                fecha, fecha.plusDays(10), fecha.plusDays(20),
                                new BigDecimal("50000.00"), new BigDecimal("50000.00"), new BigDecimal("50000.00"),
                                false, false, true),
                        new DatosBorradorChequera.CuotaBorrador(10, "Cuota de baja", 1, 1, 3, 2026,
                                fecha, fecha.plusDays(10), fecha.plusDays(20),
                                new BigDecimal("90000.00"), new BigDecimal("90000.00"), new BigDecimal("90000.00"),
                                false, true, false),
                        new DatosBorradorChequera.CuotaBorrador(11, "Cuota sin importe", 1, 2, 4, 2026,
                                fecha, fecha.plusDays(10), fecha.plusDays(20),
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                false, false, false)));
    }
}
