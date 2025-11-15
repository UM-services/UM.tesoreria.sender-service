package um.tesoreria.sender.service.pdf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openpdf.text.Font;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PdfConstants {
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String ETEC_LOGO = "marca_etec.png";
    public static final String UM_LOGO = "marca_um.png";
    public static final int ETEC_FACULTY_ID = 15;
    public static final int SPECIAL_FACULTY_ID = 6;
    
    public static final float DEFAULT_MARGIN_LEFT = 40f;
    public static final float DEFAULT_MARGIN_RIGHT = 25f;
    public static final float DEFAULT_MARGIN_TOP = 40f;
    public static final float DEFAULT_MARGIN_BOTTOM = 30f;
    
    public static final float IMAGE_SCALE_PERCENT = 80f;
    public static final float TABLE_WIDTH_PERCENT = 100f;
    
    public static final class FontSizes {
        public static final int TITLE = 16;
        public static final int SUBTITLE = 14;
        public static final int NORMAL = 12;
        public static final int SMALL = 11;
        public static final int TINY = 8;
    }
    
    public static final class Fonts {
        public static final Font TITLE_BOLD = new Font(Font.HELVETICA, FontSizes.TITLE, Font.BOLD);
        public static final Font SUBTITLE_BOLD = new Font(Font.HELVETICA, FontSizes.SUBTITLE, Font.BOLD);
        public static final Font NORMAL = new Font(Font.HELVETICA, FontSizes.NORMAL);
        public static final Font NORMAL_BOLD = new Font(Font.HELVETICA, FontSizes.NORMAL, Font.BOLD);
        public static final Font SMALL = new Font(Font.HELVETICA, FontSizes.SMALL);
        public static final Font SMALL_BOLD = new Font(Font.HELVETICA, FontSizes.SMALL, Font.BOLD);
        public static final Font TINY = new Font(Font.HELVETICA, FontSizes.TINY);
        public static final Font TINY_BOLD = new Font(Font.HELVETICA, FontSizes.TINY, Font.BOLD);
    }
}
