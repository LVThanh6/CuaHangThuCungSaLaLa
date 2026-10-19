package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Thành phần vẽ biểu đồ trực quan (Custom Charting Component) chuẩn phong cách SALALA Pet Shop.
 * Hỗ trợ 4 loại biểu đồ:
 * 1. Biểu đồ Cột (Bar Chart) - Doanh thu theo 12 tháng
 * 2. Biểu đồ Đường & Miền (Line & Area Chart) - Xu hướng tăng trưởng
 * 3. Biểu đồ Donut / Tròn (Donut Chart) - Cơ cấu nhóm doanh thu
 * 4. Biểu đồ Phân bố ngang (Horizontal Bar Chart) - Chủng loại thú cưng
 * Hỗ trợ chụp xuất hình ảnh độ phân giải cao phục vụ chèn vào file PDF.
 */
public class BieuDoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int BIEU_DO_COT = 1;
    public static final int BIEU_DO_DUONG = 2;
    public static final int BIEU_DO_TRON = 3;
    public static final int BIEU_DO_PHAN_BO = 4;

    private int loaiBieuDo = BIEU_DO_COT;

    // Dữ liệu cho các biểu đồ
    private Map<String, Double> duLieuDoanhThuThang;
    private Map<String, Long> duLieuCoCau;
    private Map<String, Integer> duLieuPhanBoThuCung;

    private final DecimalFormat dfTien = new DecimalFormat("#,### ₫");
    private final DecimalFormat dfTrieu = new DecimalFormat("#,##0.# Tr");

    // Màu sắc hiện đại
    private final Color[] PALETTE = {
            new Color(13, 148, 136),   // Teal
            new Color(14, 165, 233),   // Sky Blue
            new Color(245, 158, 11),   // Amber
            new Color(139, 92, 246),   // Purple
            new Color(236, 72, 153),   // Pink
            new Color(16, 185, 129)    // Emerald
    };

    public BieuDoPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(540, 290));

        // Tooltip gợi ý số liệu khi rê chuột
        setToolTipText("");
    }

    public void setLoaiBieuDo(int loai) {
        this.loaiBieuDo = loai;
        repaint();
    }

    public int getLoaiBieuDo() {
        return loaiBieuDo;
    }

    public void capNhatDuLieu(Map<String, Double> doanhThuThang,
                              Map<String, Long> coCau,
                              Map<String, Integer> phanBoThuCung) {
        this.duLieuDoanhThuThang = doanhThuThang;
        this.duLieuCoCau = coCau;
        this.duLieuPhanBoThuCung = phanBoThuCung;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        RangBuocGiaoDien.batKhuRangCua(g2);

        int w = getWidth();
        int h = getHeight();

        switch (loaiBieuDo) {
            case BIEU_DO_COT:
                veBieuDoCot(g2, w, h);
                break;
            case BIEU_DO_DUONG:
                veBieuDoDuong(g2, w, h);
                break;
            case BIEU_DO_TRON:
                veBieuDoDonut(g2, w, h);
                break;
            case BIEU_DO_PHAN_BO:
                veBieuDoPhanBo(g2, w, h);
                break;
            default:
                veBieuDoCot(g2, w, h);
        }
        g2.dispose();
    }

    // =========================================================================
    // 1. BIỂU ĐỒ CỘT (BAR CHART) - DOANH THU 12 THÁNG
    // =========================================================================
    private void veBieuDoCot(Graphics2D g2, int w, int h) {
        if (duLieuDoanhThuThang == null || duLieuDoanhThuThang.isEmpty()) return;

        int padLeft = 65, padRight = 24, padTop = 35, padBottom = 38;
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBottom;

        double maxVal = 1000000.0;
        for (double v : duLieuDoanhThuThang.values()) {
            if (v > maxVal) maxVal = v;
        }
        maxVal = Math.ceil(maxVal / 5000000.0) * 5000000.0; // Làm tròn mốc cao nhất

        // 1. Vẽ lưới ngang và mốc tiền trục Y
        int numLines = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= numLines; i++) {
            int y = padTop + (int) (chartH * (1.0 - (double) i / numLines));
            double val = maxVal * i / numLines;

            // Kẻ đường lưới mờ
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(padLeft, y, padLeft + chartW, y);

            // Nhãn số tiền bên trái
            g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
            String txtY = (val >= 1000000) ? dfTrieu.format(val / 1000000.0) : dfTien.format(val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txtY, padLeft - fm.stringWidth(txtY) - 8, y + 4);
        }

        // 2. Vẽ từng cột doanh thu
        int n = duLieuDoanhThuThang.size();
        int slotW = chartW / n;
        int barW = Math.max(14, (int) (slotW * 0.58));

        int idx = 0;
        for (Map.Entry<String, Double> entry : duLieuDoanhThuThang.entrySet()) {
            int cx = padLeft + idx * slotW + slotW / 2;
            int bx = cx - barW / 2;
            double val = entry.getValue();
            int bh = (int) (chartH * (val / maxVal));
            int by = padTop + chartH - bh;

            // Gradient màu teal chuyển sang xanh ngọc
            GradientPaint gp = new GradientPaint(
                    bx, by, new Color(13, 148, 136),
                    bx, by + bh, new Color(20, 184, 166)
            );
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(bx, by, barW, bh, 6, 6));

            // Nhãn tháng trục X
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String lblX = entry.getKey();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lblX, cx - fm.stringWidth(lblX) / 2, padTop + chartH + 18);

            // Số tiền trên đầu cột (chỉ hiện nếu cột đủ cao hoặc mỗi 2 cột)
            if (bh > 28 && (idx % 2 == 0 || n <= 6)) {
                g2.setColor(new Color(13, 148, 136));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String lblV = String.format("%.0fM", val / 1000000.0);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(lblV, cx - fm2.stringWidth(lblV) / 2, by - 6);
            }
            idx++;
        }

        // Tiêu đề nhỏ của biểu đồ
        g2.setFont(RangBuocGiaoDien.fontDam(12));
        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
        g2.drawString("Doanh thu bán hàng & dịch vụ theo 12 tháng (VNĐ)", padLeft, 20);
    }

    // =========================================================================
    // 2. BIỂU ĐỒ ĐƯỜNG & MIỀN (LINE & AREA CHART) - XU HƯỚNG TĂNG TRƯỞNG
    // =========================================================================
    private void veBieuDoDuong(Graphics2D g2, int w, int h) {
        if (duLieuDoanhThuThang == null || duLieuDoanhThuThang.isEmpty()) return;

        int padLeft = 65, padRight = 24, padTop = 35, padBottom = 38;
        int chartW = w - padLeft - padRight;
        int chartH = h - padTop - padBottom;

        double maxVal = 1000000.0;
        for (double v : duLieuDoanhThuThang.values()) {
            if (v > maxVal) maxVal = v;
        }
        maxVal = Math.ceil(maxVal / 5000000.0) * 5000000.0;

        // Kẻ lưới tọa độ
        int numLines = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= numLines; i++) {
            int y = padTop + (int) (chartH * (1.0 - (double) i / numLines));
            double val = maxVal * i / numLines;

            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(padLeft, y, padLeft + chartW, y);

            g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
            String txtY = dfTrieu.format(val / 1000000.0);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txtY, padLeft - fm.stringWidth(txtY) - 8, y + 4);
        }

        int n = duLieuDoanhThuThang.size();
        int[] xPoints = new int[n];
        int[] yPoints = new int[n];

        int idx = 0;
        int slotW = chartW / (n > 1 ? n - 1 : 1);
        for (Map.Entry<String, Double> entry : duLieuDoanhThuThang.entrySet()) {
            xPoints[idx] = padLeft + idx * slotW;
            double val = entry.getValue();
            yPoints[idx] = padTop + (int) (chartH * (1.0 - (val / maxVal)));

            // Nhãn trục X
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String lblX = entry.getKey();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lblX, xPoints[idx] - fm.stringWidth(lblX) / 2, padTop + chartH + 18);

            idx++;
        }

        // Tô miền dưới đường biểu diễn (Gradient bán trong suốt)
        Path2D area = new Path2D.Double();
        area.moveTo(xPoints[0], padTop + chartH);
        for (int i = 0; i < n; i++) {
            area.lineTo(xPoints[i], yPoints[i]);
        }
        area.lineTo(xPoints[n - 1], padTop + chartH);
        area.closePath();

        GradientPaint areaGp = new GradientPaint(
                0, padTop, new Color(14, 165, 233, 100),
                0, padTop + chartH, new Color(14, 165, 233, 10)
        );
        g2.setPaint(areaGp);
        g2.fill(area);

        // Vẽ đường nét đậm
        g2.setColor(new Color(14, 165, 233));
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        // Vẽ điểm tròn tại từng nút dữ liệu
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
            g2.setColor(new Color(14, 165, 233));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
        }

        g2.setFont(RangBuocGiaoDien.fontDam(12));
        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
        g2.drawString("Xu hướng tăng trưởng doanh thu liên tục (Area / Line Chart)", padLeft, 20);
    }

    // =========================================================================
    // 3. BIỂU ĐỒ DONUT / TRÒN (DONUT CHART) - CƠ CẤU DOANH THU
    // =========================================================================
    private void veBieuDoDonut(Graphics2D g2, int w, int h) {
        if (duLieuCoCau == null || duLieuCoCau.isEmpty()) return;

        double tong = 0;
        for (long v : duLieuCoCau.values()) tong += v;
        if (tong == 0) return;

        int size = Math.min(w / 2 - 40, h - 60);
        int cx = 35 + size / 2;
        int cy = h / 2;

        int outerR = size / 2;
        int innerR = (int) (outerR * 0.58);

        double curAngle = 90.0;
        int idx = 0;

        // Vẽ từng múi tròn
        for (Map.Entry<String, Long> entry : duLieuCoCau.entrySet()) {
            double angle = (entry.getValue() / tong) * 360.0;
            Color col = PALETTE[idx % PALETTE.length];

            g2.setColor(col);
            g2.fill(new Arc2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2, curAngle, angle, Arc2D.PIE));

            curAngle += angle;
            idx++;
        }

        // Lỗ thủng ở giữa tạo hình Donut sang trọng
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

        // Chữ ở tâm Donut
        g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        String txtTam1 = "TỔNG";
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(txtTam1, cx - fm1.stringWidth(txtTam1) / 2, cy - 6);

        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
        g2.setFont(RangBuocGiaoDien.fontDam(13));
        String txtTam2 = dfTrieu.format(tong / 1000000.0);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(txtTam2, cx - fm2.stringWidth(txtTam2) / 2, cy + 12);

        // Chú thích (Legend) ở nửa bên phải
        int legX = cx + outerR + 28;
        int legY = cy - (duLieuCoCau.size() * 36) / 2 + 10;
        idx = 0;

        for (Map.Entry<String, Long> entry : duLieuCoCau.entrySet()) {
            Color col = PALETTE[idx % PALETTE.length];
            double pct = (entry.getValue() / tong) * 100.0;

            // Chấm màu
            g2.setColor(col);
            g2.fillRoundRect(legX, legY + 2, 12, 12, 4, 4);

            // Tên nhóm
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            g2.setFont(RangBuocGiaoDien.fontDam(12));
            g2.drawString(entry.getKey(), legX + 20, legY + 12);

            // Số tiền và phần trăm
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String sub = String.format("%s (%.1f%%)", dfTien.format(entry.getValue()), pct);
            g2.drawString(sub, legX + 20, legY + 26);

            legY += 36;
            idx++;
        }

        g2.setFont(RangBuocGiaoDien.fontDam(12));
        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
        g2.drawString("Cơ cấu tỷ trọng doanh thu theo nhóm sản phẩm & dịch vụ", 24, 20);
    }

    // =========================================================================
    // 4. BIỂU ĐỒ PHÂN BỐ (HORIZONTAL BAR CHART) - CHỦNG LOẠI THÚ CƯNG
    // =========================================================================
    private void veBieuDoPhanBo(Graphics2D g2, int w, int h) {
        if (duLieuPhanBoThuCung == null || duLieuPhanBoThuCung.isEmpty()) return;

        int padLeft = 90, padRight = 70, padTop = 38, padBottom = 20;
        int chartW = w - padLeft - padRight;
        int totalItems = duLieuPhanBoThuCung.size();
        int slotH = (h - padTop - padBottom) / (totalItems > 0 ? totalItems : 1);
        int barH = Math.min(24, (int) (slotH * 0.5));

        int maxVal = 1;
        int sumVal = 0;
        for (int v : duLieuPhanBoThuCung.values()) {
            if (v > maxVal) maxVal = v;
            sumVal += v;
        }

        int idx = 0;
        for (Map.Entry<String, Integer> entry : duLieuPhanBoThuCung.entrySet()) {
            int cy = padTop + idx * slotH + slotH / 2;
            int by = cy - barH / 2;
            int val = entry.getValue();
            int bw = (int) (chartW * ((double) val / maxVal));
            double pct = (sumVal > 0) ? (val * 100.0 / sumVal) : 0;

            Color col = PALETTE[idx % PALETTE.length];

            // Tên loài thú cưng bên trái
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            g2.setFont(RangBuocGiaoDien.fontDam(12));
            FontMetrics fm = g2.getFontMetrics();
            String name = entry.getKey();
            g2.drawString(name, padLeft - fm.stringWidth(name) - 12, cy + 4);

            // Nền thanh mờ
            g2.setColor(new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Double(padLeft, by, chartW, barH, 6, 6));

            // Thanh tiến trình có màu
            g2.setColor(col);
            g2.fill(new RoundRectangle2D.Double(padLeft, by, Math.max(bw, 8), barH, 6, 6));

            // Số lượng & tỷ lệ % bên phải
            g2.setColor(col);
            g2.setFont(RangBuocGiaoDien.fontDam(11));
            String stat = String.format("%d bé (%.1f%%)", val, pct);
            g2.drawString(stat, padLeft + bw + 10, cy + 4);

            idx++;
        }

        g2.setFont(RangBuocGiaoDien.fontDam(12));
        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
        g2.drawString("Thống kê phân bổ thú cưng theo chủng loại (Khách hàng đăng ký)", 24, 20);
    }

    // =========================================================================
    // XUẤT HÌNH ẢNH BIỂU ĐỒ ĐỘ PHÂN GIẢI CAO (CHO FILE PDF)
    // =========================================================================
    /**
     * Xuất biểu đồ hiện tại ra đối tượng BufferedImage chất lượng cao để chèn vào PDF
     */
    public BufferedImage xuatHinhAnh(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        RangBuocGiaoDien.batKhuRangCua(g2);
        switch (loaiBieuDo) {
            case BIEU_DO_COT:
                veBieuDoCot(g2, width, height);
                break;
            case BIEU_DO_DUONG:
                veBieuDoDuong(g2, width, height);
                break;
            case BIEU_DO_TRON:
                veBieuDoDonut(g2, width, height);
                break;
            case BIEU_DO_PHAN_BO:
                veBieuDoPhanBo(g2, width, height);
                break;
        }
        g2.dispose();
        return img;
    }
}
