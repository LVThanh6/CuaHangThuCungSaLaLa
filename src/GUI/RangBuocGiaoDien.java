package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Lớp RangBuocGiaoDien:
 * Quy định toàn bộ ràng buộc và thông số chuẩn của hệ thống SALALA Pet Shop:
 * - Kích thước tối thiểu và mặc định (Màn hình chính, Tab nội dung)
 * - Bảng màu chủ đạo, màu nền, màu đường viền, màu chữ, huy hiệu trạng thái
 * - Font chữ chuẩn hệ thống và hỗ trợ khử răng cưa đồ họa
 */
public class RangBuocGiaoDien {

    // =========================================================================
    // 1. QUY ĐỊNH KÍCH THƯỚC CHUẨN MÀN HÌNH CHÍNH & NỘI DUNG TAB
    // Phù hợp từ laptop màn hình nhỏ (1366x768) đến Full HD (1920x1080), 2K (2560x1440)
    // =========================================================================
    public static final int MAN_HINH_RONG_TOI_THIEU = 1100;
    public static final int MAN_HINH_CAO_TOI_THIEU = 650;
    public static final int MAN_HINH_RONG_MAC_DINH = 1280;
    public static final int MAN_HINH_CAO_MAC_DINH = 750;

    public static final Dimension KICH_THUOC_TOI_THIEU = new Dimension(MAN_HINH_RONG_TOI_THIEU, MAN_HINH_CAO_TOI_THIEU);
    public static final Dimension KICH_THUOC_MAC_DINH = new Dimension(MAN_HINH_RONG_MAC_DINH, MAN_HINH_CAO_MAC_DINH);

    public static final int TAB_RONG_TOI_THIEU = 1100;
    public static final int TAB_CAO_TOI_THIEU = 550;
    public static final Dimension KICH_THUOC_TAB_TOI_THIEU = new Dimension(TAB_RONG_TOI_THIEU, TAB_CAO_TOI_THIEU);

    // =========================================================================
    // 2. BẢNG MÀU CHUẨN HỆ THỐNG SALALA PET SHOP
    // =========================================================================
    // Màu chủ đạo thương hiệu (Xanh mòng két / Teal)
    public static final Color MAU_CHU_DAO = new Color(14, 116, 101);          // #0E7465
    public static final Color MAU_CHU_DAO_RE_CHUOT = new Color(11, 95, 83);   // Khi rê chuột
    public static final Color MAU_CHU_DAO_NHAT = new Color(230, 244, 241);     // Nền nhẹ
    public static final Color MAU_VIEN_CHU_DAO = new Color(178, 222, 215);

    // Màu nền và khung thẻ
    public static final Color MAU_NEN_TRANG = new Color(248, 250, 252);       // #F8FAFC
    public static final Color MAU_NEN_THE = Color.WHITE;
    public static final Color MAU_DUONG_VIEN = new Color(226, 232, 240);       // #E2E8F0

    // Màu chữ
    public static final Color MAU_CHU_CHINH = new Color(30, 41, 59);          // #1E293B
    public static final Color MAU_CHU_PHU = new Color(100, 116, 139);         // #64748B
    public static final Color MAU_CHU_GOI_Y = new Color(148, 163, 184);       // #94A3B8

    // Màu Bảng Dữ Liệu
    public static final Color MAU_TIEU_DE_BANG = new Color(248, 250, 252);
    public static final Color MAU_DONG_RE_CHUOT = new Color(241, 245, 249);
    public static final Color MAU_DONG_XEN_KE = new Color(254, 254, 255);
    public static final Color MAU_LUOI_BANG = new Color(241, 245, 249);
    public static final Color MAU_LIEN_KET = new Color(14, 116, 101);

    // Màu Huy Hiệu Trạng Thái
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

    // =========================================================================
    // 3. FONT CHỮ CHUẨN HỆ THỐNG
    // =========================================================================
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
     * Bật khử răng cưa mượt mà cho Graphics2D
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
