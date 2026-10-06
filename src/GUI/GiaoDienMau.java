package GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Bang mau va thong so thiet ke chuan cua he thong SALALA Pet Shop
 */
public class GiaoDienMau {
    // Mau chu dao thuong hieu SALALA Pet Shop (Xanh mong ket / Teal)
    public static final Color MAU_CHU_DAO = new Color(14, 116, 101);          // #0E7465
    public static final Color MAU_CHU_DAO_RE_CHUOT = new Color(11, 95, 83);   // Khi re chuot
    public static final Color MAU_CHU_DAO_NHAT = new Color(230, 244, 241);     // Nen nhe
    public static final Color MAU_VIEN_CHU_DAO = new Color(178, 222, 215);

    // Mau nen va khung the
    public static final Color MAU_NEN_TRANG = new Color(248, 250, 252);       // #F8FAFC
    public static final Color MAU_NEN_THE = Color.WHITE;
    public static final Color MAU_DUONG_VIEN = new Color(226, 232, 240);       // #E2E8F0

    // Mau chu
    public static final Color MAU_CHU_CHINH = new Color(30, 41, 59);          // #1E293B
    public static final Color MAU_CHU_PHU = new Color(100, 116, 139);         // #64748B
    public static final Color MAU_CHU_GOI_Y = new Color(148, 163, 184);       // #94A3B8

    // Mau Bang Du Lieu
    public static final Color MAU_TIEU_DE_BANG = new Color(248, 250, 252);
    public static final Color MAU_DONG_RE_CHUOT = new Color(241, 245, 249);
    public static final Color MAU_DONG_XEN_KE = new Color(254, 254, 255);
    public static final Color MAU_LUOI_BANG = new Color(241, 245, 249);
    public static final Color MAU_LIEN_KET = new Color(14, 116, 101);

    // Mau Huy Hieu Trang Thai
    public static final Color MAU_THANH_CONG_NEN = new Color(220, 252, 231);
    public static final Color MAU_THANH_CONG_CHU = new Color(21, 128, 61);
    public static final Color MAU_THANH_CONG_CHAM = new Color(34, 197, 94);

    public static final Color MAU_CANH_BAO_NEN = new Color(254, 243, 199);
    public static final Color MAU_CANH_BAO_CHU = new Color(180, 83, 9);
    public static final Color MAU_CANH_BAO_CHAM = new Color(245, 158, 11);

    public static final Color MAU_NGUY_HIEM_NEN = new Color(254, 226, 226);
    public static final Color MAU_NGUY_HIEM_CHU = new Color(185, 28, 28);
    public static final Color MAU_NGUY_HIEM_CHAM = new Color(239, 68, 68);

    public static final Color MAU_THONG_TIN_NEN = new Color(224, 242, 254);
    public static final Color MAU_THONG_TIN_CHU = new Color(2, 132, 199);
    public static final Color MAU_THONG_TIN_CHAM = new Color(14, 165, 233);

    public static final Color MAU_TRUNG_TINH_NEN = new Color(241, 245, 249);
    public static final Color MAU_TRUNG_TINH_CHU = new Color(71, 85, 105);
    public static final Color MAU_TRUNG_TINH_CHAM = new Color(148, 163, 184);

    // Font chu chuan he thong
    private static final String TEN_FONT = "Segoe UI";

    public static Font fontThuong(float kichThuoc) {
        return new Font(TEN_FONT, Font.PLAIN, (int) kichThuoc);
    }

    public static Font fontVua(float kichThuoc) {
        return new Font(TEN_FONT, Font.PLAIN, (int) kichThuoc);
    }

    public static Font fontDam(float kichThuoc) {
        return new Font(TEN_FONT, Font.BOLD, (int) kichThuoc);
    }

    /**
     * Bat khu rang cua muot ma cho Graphics2D
     */
    public static void batKhuRangCua(Graphics doHoa) {
        if (doHoa instanceof Graphics2D) {
            Graphics2D doHoa2D = (Graphics2D) doHoa;
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            doHoa2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            doHoa2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }
}
