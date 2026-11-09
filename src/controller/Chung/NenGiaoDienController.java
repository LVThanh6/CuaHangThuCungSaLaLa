package controller.Chung;

import GUI.Chung.RangBuocGiaoDien;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.UIManager;

/**
 * Xử lý logic cho nền giao diện: nạp ảnh nền, co giãn phủ kín, làm mờ (kính mờ)
 * và phân loại màu nền của panel để quyết định có làm trong suốt hay không.
 * Không vẽ lên màn hình - việc vẽ do GUI.Chung.NenGiaoDienGUI đảm nhiệm.
 */
public class NenGiaoDienController {

    /** Kết quả xử lý ảnh nền cho một kích thước panel. */
    public static class AnhNenDaXuLy {
        /** Ảnh phủ kín + lớp phủ trắng nhẹ. */
        public final BufferedImage anhNen;
        /** Ảnh phủ kín đã làm mờ (dùng cho thanh header kính mờ). */
        public final BufferedImage anhMo;

        AnhNenDaXuLy(BufferedImage anhNen, BufferedImage anhMo) {
            this.anhNen = anhNen;
            this.anhMo = anhMo;
        }
    }

    /** Cách xử lý một panel khi làm mờ nền. */
    public enum PhanLoaiNen {
        /** Giữ nguyên (màu đậm, không phải nền/thẻ). */
        GIU_NGUYEN,
        /** Nền trang -> trong suốt hoàn toàn. */
        TRONG_SUOT,
        /** Thẻ trắng/màu rất nhạt -> bán trong suốt. */
        THE_MO
    }

    private static BufferedImage anhGoc;
    private static boolean daThuTai = false;

    // ==========================================================
    // 1. NẠP ẢNH NỀN
    // ==========================================================
    /** Thứ tự tìm ảnh: classpath (/img/background.jpg) -> đường dẫn tương đối -> tuyệt đối. */
    public static synchronized BufferedImage taiAnhNen() {
        if (daThuTai) {
            return anhGoc;
        }
        daThuTai = true;

        try {
            URL url = NenGiaoDienController.class.getResource("/img/background.jpg");
            if (url != null) {
                anhGoc = ImageIO.read(url);
            }
        } catch (IOException ignored) {
        }

        if (anhGoc == null) {
            String[] duongDan = {
                    RangBuocGiaoDien.DUONG_DAN_ANH_NEN,
                    RangBuocGiaoDien.DUONG_DAN_ANH_NEN_TUYET_DOI
            };
            for (String d : duongDan) {
                File f = new File(d);
                if (f.isFile()) {
                    try {
                        anhGoc = ImageIO.read(f);
                    } catch (IOException ignored) {
                    }
                    if (anhGoc != null) {
                        break;
                    }
                }
            }
        }

        if (anhGoc == null) {
            System.err.println("Không nạp được ảnh nền: " + RangBuocGiaoDien.DUONG_DAN_ANH_NEN_TUYET_DOI);
        }
        return anhGoc;
    }

    public boolean coAnhNen() {
        return taiAnhNen() != null;
    }

    // ==========================================================
    // 2. XỬ LÝ ẢNH THEO KÍCH THƯỚC PANEL
    // ==========================================================
    /** Co giãn ảnh phủ kín (căn giữa, giữ tỉ lệ), tạo bản làm mờ và bản có lớp phủ trắng nhẹ. */
    public AnhNenDaXuLy taoAnhNen(int w, int h) {
        BufferedImage goc = taiAnhNen();
        if (goc == null || w <= 0 || h <= 0) {
            return null;
        }
        double tiLe = Math.max((double) w / goc.getWidth(), (double) h / goc.getHeight());
        int rongAnh = (int) Math.ceil(goc.getWidth() * tiLe);
        int caoAnh = (int) Math.ceil(goc.getHeight() * tiLe);

        BufferedImage co = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = co.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(goc, (w - rongAnh) / 2, (h - caoAnh) / 2, rongAnh, caoAnh, null);
        g2.dispose();

        // Bản làm mờ (tạo từ ảnh sạch, trước khi phủ lớp trắng)
        BufferedImage anhMo = lamMo(co);

        // Bản nền: thêm lớp phủ trắng nhẹ
        Graphics2D g3 = co.createGraphics();
        g3.setColor(new Color(255, 255, 255, RangBuocGiaoDien.DO_DUC_LOP_PHU));
        g3.fillRect(0, 0, w, h);
        g3.dispose();

        return new AnhNenDaXuLy(co, anhMo);
    }

    /** Làm mờ nhanh: thu nhỏ dần 4 lần rồi phóng lại bằng nội suy tuyến tính. */
    private BufferedImage lamMo(BufferedImage nguon) {
        int w = nguon.getWidth();
        int h = nguon.getHeight();
        BufferedImage hienTai = nguon;
        int cw = w;
        int ch = h;
        for (int i = 0; i < 4; i++) {
            cw = Math.max(1, cw / 2);
            ch = Math.max(1, ch / 2);
            hienTai = doiKichThuoc(hienTai, cw, ch);
        }
        return doiKichThuoc(hienTai, w, h);
    }

    private BufferedImage doiKichThuoc(BufferedImage nguon, int w, int h) {
        BufferedImage kq = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = kq.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(nguon, 0, 0, w, h, null);
        g2.dispose();
        return kq;
    }

    // ==========================================================
    // 3. PHÂN LOẠI MÀU NỀN PANEL KHI LÀM MỜ
    // ==========================================================
    public PhanLoaiNen phanLoaiNen(Color bg) {
        if (bg == null) {
            return PhanLoaiNen.GIU_NGUYEN;
        }
        int rgb = bg.getRGB() & 0xFFFFFF;
        if (laMauNenTrang(rgb) || laNenTrangXamNhat(bg)) {
            return PhanLoaiNen.TRONG_SUOT;
        }
        if (rgb == 0xFFFFFF || laTheMauNhat(bg)) {
            return PhanLoaiNen.THE_MO;
        }
        return PhanLoaiNen.GIU_NGUYEN;
    }

    /** Màu thẻ bán trong suốt dùng để tô nền thẻ (giữ màu gốc, độ đục DO_DUC_THE). */
    public Color mauTheMo(Color bg) {
        return new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), RangBuocGiaoDien.DO_DUC_THE);
    }

    /** Nền trang xám/trắng ngà rất nhạt nhưng lệch tông, ví dụ (247, 249, 252). */
    private boolean laNenTrangXamNhat(Color c) {
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        return min >= 244 && max - min <= 6 && (c.getRGB() & 0xFFFFFF) != 0xFFFFFF;
    }

    /** Thẻ phủ màu rất nhạt (có sắc), ví dụ xanh mint (240, 253, 250). */
    private boolean laTheMauNhat(Color c) {
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        return min >= 235 && max - min > 6;
    }

    private boolean laMauNenTrang(int rgb) {
        if (rgb == (RangBuocGiaoDien.MAU_NEN_TRANG.getRGB() & 0xFFFFFF)) {
            return true;
        }
        Color macDinh = UIManager.getColor("Panel.background");
        return macDinh != null && rgb == (macDinh.getRGB() & 0xFFFFFF);
    }
}
