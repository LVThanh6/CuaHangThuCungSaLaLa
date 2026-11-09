package GUI.Chung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Panel có ảnh nền phủ kín (căn giữa, giữ đúng tỉ lệ ảnh) và một lớp phủ trắng
 * nhẹ.
 * Có thể chỉ định một "thanh mờ" (thường là header): vùng đó được vẽ lại bằng
 * phiên bản ảnh đã làm mờ + lớp phủ kem đậm hơn (hiệu ứng kính mờ).
 *
 * Thứ tự tìm ảnh: classpath (/img/background.jpg) -> đường dẫn tương đối ->
 * đường dẫn tuyệt đối.
 * Ảnh được nạp một lần; ảnh đã co giãn/làm mờ được cache theo kích thước panel.
 */
public class NenAnhPanel extends JPanel {

    private static BufferedImage anhGoc;
    private static boolean daThuTai = false;

    private BufferedImage anhNen; // ảnh phủ kín + lớp phủ nhẹ
    private BufferedImage anhMo; // ảnh phủ kín đã làm mờ
    private int rongCache = -1;
    private int caoCache = -1;

    private Component thanhMo;

    public NenAnhPanel() {
        this(new BorderLayout());
    }

    public NenAnhPanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        taiAnhNen();
    }

    /** Có nạp được ảnh nền hay không. */
    public boolean coAnh() {
        return anhGoc != null;
    }

    /**
     * Thành phần (con trực tiếp của panel này) sẽ được vẽ nền kính mờ, ví dụ
     * header.
     */
    public void setThanhMo(Component c) {
        this.thanhMo = c;
        repaint();
    }

    private static synchronized BufferedImage taiAnhNen() {
        if (daThuTai) {
            return anhGoc;
        }
        daThuTai = true;

        try {
            URL url = NenAnhPanel.class.getResource("/img/background.jpg");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // tô màu nền phẳng trước
        if (anhGoc == null) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (anhNen == null || w != rongCache || h != caoCache) {
            taoAnhCache(w, h);
        }
        g.drawImage(anhNen, 0, 0, null);

        // Thanh kính mờ (header)
        if (thanhMo != null && thanhMo.getParent() == this && thanhMo.getHeight() > 0) {
            int y1 = Math.max(0, thanhMo.getY());
            int y2 = Math.min(h, thanhMo.getY() + thanhMo.getHeight());
            if (y2 > y1) {
                g.drawImage(anhMo, 0, y1, w, y2, 0, y1, w, y2, null);
                g.setColor(new Color(255, 252, 245, RangBuocGiaoDien.DO_DUC_THANH_MO));
                g.fillRect(0, y1, w, y2 - y1);
            }
        }
    }

    private void taoAnhCache(int w, int h) {
        double tiLe = Math.max((double) w / anhGoc.getWidth(), (double) h / anhGoc.getHeight());
        int rongAnh = (int) Math.ceil(anhGoc.getWidth() * tiLe);
        int caoAnh = (int) Math.ceil(anhGoc.getHeight() * tiLe);

        BufferedImage co = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = co.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(anhGoc, (w - rongAnh) / 2, (h - caoAnh) / 2, rongAnh, caoAnh, null);
        g2.dispose();

        // Bản làm mờ (tạo từ ảnh sạch, trước khi phủ lớp trắng)
        anhMo = lamMo(co);

        // Bản nền: thêm lớp phủ trắng nhẹ
        Graphics2D g3 = co.createGraphics();
        g3.setColor(new Color(255, 255, 255, RangBuocGiaoDien.DO_DUC_LOP_PHU));
        g3.fillRect(0, 0, w, h);
        g3.dispose();

        anhNen = co;
        rongCache = w;
        caoCache = h;
    }

    /** Làm mờ nhanh: thu nhỏ dần 4 lần rồi phóng lại bằng nội suy tuyến tính. */
    private static BufferedImage lamMo(BufferedImage nguon) {
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

    private static BufferedImage doiKichThuoc(BufferedImage nguon, int w, int h) {
        BufferedImage kq = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = kq.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(nguon, 0, 0, w, h, null);
        g2.dispose();
        return kq;
    }
}
