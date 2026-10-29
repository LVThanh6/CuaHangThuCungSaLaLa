package GUI.ThongKe;

import GUI.Chung.RangBuocGiaoDien;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;

public class BieuDoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int BIEU_DO_COT = 1;
    public static final int BIEU_DO_DUONG = 2;
    public static final int BIEU_DO_DIEM = 3;
    public static final int BIEU_DO_QUAT = 4;

    private static final Color[] MAU = {
            new Color(13, 148, 136), new Color(14, 165, 233),
            new Color(245, 158, 11), new Color(139, 92, 246),
            new Color(236, 72, 153), new Color(16, 185, 129)
    };

    private final DecimalFormat dinhDangSo = new DecimalFormat("#,##0.##");
    private String tieuDe = "";
    private String donVi = "";
    private String[] nhan = new String[0];
    private String[] chuoi = new String[0];
    private double[][] giaTri = new double[0][];
    private int loaiBieuDo = BIEU_DO_COT;
    private boolean hienNhan = true;
    private boolean hienLuoi = true;
    private boolean hienChuGiai = true;

    public BieuDoPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(640, 400));
        setToolTipText("");
    }

    public void datDuLieu(String tieuDe, String[] nhan, String[] chuoi,
            double[][] giaTri, String donVi) {
        if (nhan == null || chuoi == null || giaTri == null || giaTri.length != chuoi.length) {
            throw new IllegalArgumentException("Dữ liệu biểu đồ phải có một dòng giá trị cho mỗi chuỗi.");
        }
        for (double[] hang : giaTri) {
            if (hang == null || hang.length != nhan.length) {
                throw new IllegalArgumentException("Mỗi chuỗi biểu đồ phải có một giá trị cho mỗi nhãn.");
            }
        }
        this.tieuDe = tieuDe == null ? "" : tieuDe;
        this.nhan = Arrays.copyOf(nhan, nhan.length);
        this.chuoi = Arrays.copyOf(chuoi, chuoi.length);
        this.giaTri = new double[giaTri.length][];
        for (int i = 0; i < giaTri.length; i++) {
            this.giaTri[i] = Arrays.copyOf(giaTri[i], giaTri[i].length);
        }
        this.donVi = donVi == null ? "" : donVi;
        repaint();
    }

    public void datTuyChon(boolean hienNhan, boolean hienLuoi, boolean hienChuGiai) {
        this.hienNhan = hienNhan;
        this.hienLuoi = hienLuoi;
        this.hienChuGiai = hienChuGiai;
        repaint();
    }

    public void setLoaiBieuDo(int loai) {
        if (loai < BIEU_DO_COT || loai > BIEU_DO_QUAT) {
            throw new IllegalArgumentException("Loại biểu đồ không được hỗ trợ.");
        }
        this.loaiBieuDo = loai;
        repaint();
    }

    public int getLoaiBieuDo() {
        return loaiBieuDo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!tieuDe.isEmpty()) {
            g2.setFont(RangBuocGiaoDien.fontDam(14));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            g2.drawString(tieuDe, 20, 24);
        }
        if (nhan.length == 0 || giaTri.length == 0) {
            g2.setFont(RangBuocGiaoDien.fontThuong(13));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString("Không có dữ liệu trong khoảng thời gian đã chọn.", 20, 54);
            g2.dispose();
            return;
        }

        if (loaiBieuDo == BIEU_DO_QUAT) {
            veBieuDoQuat(g2);
        } else {
            veBieuDoTheoTruc(g2);
        }
        g2.dispose();
    }

    private void veBieuDoTheoTruc(Graphics2D g2) {
        int le = 76;
        int phai = 24;
        int tren = tieuDe.isEmpty() ? 24 : 44;
        int day = hienChuGiai ? getHeight() - 62 : getHeight() - 34;
        int rong = getWidth() - le - phai;
        int cao = day - tren;
        if (rong <= 0 || cao <= 0) {
            return;
        }

        double max = 0;
        for (double[] hang : giaTri) {
            for (double value : hang) {
                max = Math.max(max, value);
            }
        }
        double tran = mucTran(max);
        FontMetrics fm = g2.getFontMetrics(RangBuocGiaoDien.fontThuong(11));
        g2.setFont(RangBuocGiaoDien.fontThuong(11));
        for (int i = 0; i <= 4; i++) {
            int y = day - cao * i / 4;
            if (i > 0 && hienLuoi) {
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(le, y, le + rong, y);
            }
            String text = dinhDangSo.format(tran * i / 4);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString(text, le - fm.stringWidth(text) - 8, y + fm.getAscent() / 2);
        }
        g2.setColor(new Color(203, 213, 225));
        g2.drawLine(le, day, le + rong, day);

        int count = nhan.length;
        double slot = rong / (double) count;
        int soChuoi = giaTri.length;
        for (int s = 0; s < soChuoi; s++) {
            g2.setColor(MAU[s % MAU.length]);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < count; i++) {
                int x = (int) Math.round(le + slot * (i + 0.5));
                int y = day - (int) Math.round(giaTri[s][i] / tran * cao);
                if (loaiBieuDo == BIEU_DO_COT) {
                    double groupWidth = Math.min(slot * 0.72, 72);
                    double barWidth = groupWidth / soChuoi;
                    double xBar = x - groupWidth / 2 + s * barWidth;
                    g2.fillRoundRect((int) xBar, y, Math.max(1, (int) barWidth - 2),
                            Math.max(1, day - y), 5, 5);
                } else if (loaiBieuDo == BIEU_DO_DUONG && i > 0) {
                    int prevX = (int) Math.round(le + slot * (i - 0.5));
                    int prevY = day - (int) Math.round(giaTri[s][i - 1] / tran * cao);
                    g2.drawLine(prevX, prevY, x, y);
                }
                if (loaiBieuDo == BIEU_DO_DIEM || loaiBieuDo == BIEU_DO_DUONG) {
                    g2.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
                }
                if (hienNhan) {
                    String label = dinhDangSo.format(giaTri[s][i])
                            + (donVi.isEmpty() ? "" : " " + donVi);
                    g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                    g2.drawString(label, x - g2.getFontMetrics().stringWidth(label) / 2, y - 8);
                    g2.setColor(MAU[s % MAU.length]);
                }
            }
        }

        g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
        int maxLabels = Math.max(1, rong / 90);
        int stride = Math.max(1, (int) Math.ceil(count / (double) maxLabels));
        for (int i = 0; i < count; i += stride) {
            String label = nhan[i];
            int x = (int) Math.round(le + slot * (i + 0.5));
            g2.drawString(label, x - g2.getFontMetrics().stringWidth(label) / 2, day + 18);
        }
        if (hienChuGiai) {
            veChuGiai(g2, getHeight() - 16);
        }
    }

    private void veBieuDoQuat(Graphics2D g2) {
        double[] values = giaTri[0];
        double total = 0;
        for (double value : values) {
            total += Math.max(0, value);
        }
        if (total <= 0) {
            g2.setFont(RangBuocGiaoDien.fontThuong(13));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString("Không có giá trị dương để vẽ biểu đồ quạt.", 20, 54);
            return;
        }

        int size = Math.max(1, Math.min(getHeight() - 100, getWidth() / 2 - 80));
        int x = 24;
        int y = Math.max(42, (getHeight() - size) / 2);
        double start = 90;
        for (int i = 0; i < values.length; i++) {
            double value = Math.max(0, values[i]);
            if (value == 0) {
                continue;
            }
            double angle = value / total * 360;
            g2.setColor(MAU[i % MAU.length]);
            g2.fill(new Arc2D.Double(x, y, size, size, start, angle, Arc2D.PIE));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(x, y, size, size, start, angle, Arc2D.PIE));
            start += angle;
        }

        int xChuGiai = x + size + 22;
        int yChuGiai = Math.max(52, y + 16);
        int chieuRongConLai = Math.max(1, getWidth() - xChuGiai - 12);
        int soHangToiDa = Math.max(1, (getHeight() - 88) / 24);
        int soCot = (nhan.length + soHangToiDa - 1) / soHangToiDa;
        int rongCot = Math.max(1, chieuRongConLai / soCot);
        g2.setFont(RangBuocGiaoDien.fontThuong(12));
        for (int i = 0; i < nhan.length; i++) {
            int cot = i / soHangToiDa;
            int hang = i % soHangToiDa;
            int viTriX = xChuGiai + cot * rongCot;
            int viTriY = yChuGiai + hang * 24;
            g2.setColor(MAU[i % MAU.length]);
            g2.fillRoundRect(viTriX, viTriY - 10, 12, 12, 4, 4);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            String text = nhan[i] + ": " + dinhDangSo.format(values[i]) + " (" +
                    String.format("%.1f%%", Math.max(0, values[i]) * 100 / total) + ")";
            int maxTextWidth = Math.max(1, rongCot - 22);
            FontMetrics fm = g2.getFontMetrics();
            while (!text.isEmpty() && fm.stringWidth(text) > maxTextWidth) {
                text = text.substring(0, text.length() - 1);
            }
            g2.drawString(text, viTriX + 20, viTriY);
        }
    }

    private void veChuGiai(Graphics2D g2, int y) {
        g2.setFont(RangBuocGiaoDien.fontThuong(11));
        int x = 20;
        for (int i = 0; i < chuoi.length; i++) {
            g2.setColor(MAU[i % MAU.length]);
            g2.fillRoundRect(x, y - 9, 10, 10, 3, 3);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString(chuoi[i], x + 15, y);
            x += 28 + g2.getFontMetrics().stringWidth(chuoi[i]);
        }
    }

    private static double mucTran(double max) {
        if (max <= 0) {
            return 1;
        }
        double raw = max / 4;
        double base = Math.pow(10, Math.floor(Math.log10(raw)));
        double fraction = raw / base;
        double step = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 5 ? 5 : 10;
        return step * base * 4;
    }

    public BufferedImage xuatHinhAnh(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, width, height);
        setSize(width, height);
        paint(g2);
        g2.dispose();
        return image;
    }
}
