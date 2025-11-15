package um.tesoreria.sender.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class PdfDocumentBuilder {

    private final Document document;
    private final PdfWriter writer;

    public PdfDocumentBuilder(String filename) throws DocumentException, IOException {
        document = new Document(new Rectangle(PageSize.A4));
        writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.setMargins(
            PdfConstants.DEFAULT_MARGIN_LEFT,
            PdfConstants.DEFAULT_MARGIN_RIGHT,
            PdfConstants.DEFAULT_MARGIN_TOP,
            PdfConstants.DEFAULT_MARGIN_BOTTOM
        );
        document.open();
    }

    public PdfDocumentBuilder addHeader(String facultadId, String facultadName) throws IOException, DocumentException {
        float[] columnHeader = {1, 1};
        PdfPTable table = new PdfPTable(columnHeader);
        table.setWidthPercentage(PdfConstants.TABLE_WIDTH_PERCENT);

        // Logo
        Image image = Image.getInstance(Integer.parseInt(facultadId) == PdfConstants.ETEC_FACULTY_ID
            ? PdfConstants.ETEC_LOGO 
            : PdfConstants.UM_LOGO);
        image.scalePercent(PdfConstants.IMAGE_SCALE_PERCENT);
        PdfPCell logoCell = new PdfPCell(image);
        logoCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(logoCell);

        // Títulos
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        
        Paragraph universityTitle = new Paragraph("UNIVERSIDAD DE MENDOZA", PdfConstants.Fonts.TITLE_BOLD);
        universityTitle.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(universityTitle);
        
        Paragraph facultyTitle = new Paragraph(facultadName, PdfConstants.Fonts.SUBTITLE_BOLD);
        facultyTitle.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(facultyTitle);
        
        table.addCell(titleCell);
        document.add(table);
        
        return this;
    }

    public PdfDocumentBuilder addCenteredText(String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
        return this;
    }

    public PdfDocumentBuilder addRightAlignedText(String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);
        return this;
    }

    public PdfDocumentBuilder addParagraph(String text, Font font) throws DocumentException {
        document.add(new Paragraph(text, font));
        return this;
    }

    public PdfDocumentBuilder addSpace() throws DocumentException {
        document.add(new Paragraph(" ", PdfConstants.Fonts.TINY));
        return this;
    }

    public PdfPTable createTable(float[] columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(PdfConstants.TABLE_WIDTH_PERCENT);
        return table;
    }

    public void addTable(PdfPTable table) throws DocumentException {
        document.add(table);
    }

    public void close() {
        if (document != null && document.isOpen()) {
            document.close();
        }
    }

    public Document getDocument() {
        return document;
    }

    public PdfWriter getWriter() {
        return writer;
    }
}
