package GUI.Chung;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/**
 * Làm "mờ" nền các màn hình con để ảnh nền của GiaoDienChinhGUI hiện lên:
 *  - JPanel nền xám/xanh nhạt của trang (#F8FAFC, màu Panel mặc định) -> trong suốt hoàn toàn.
 *  - JPanel nền trắng (thẻ/card) -> trắng bán trong suốt (độ đục: RangBuocGiaoDien.DO_DUC_THE).
 *  - JScrollPane bọc panel -> viewport trong suốt (bảng JTable vẫn giữ nền đặc cho dễ đọc).
 * Các thành phần được thêm vào sau này cũng tự được xử lý.
 *
 * Với header (apDungChoHeader): mọi panel nền trắng/xám nhạt đều trong suốt hoàn toàn,
 * vì nền kính mờ đã được NenAnhPanel vẽ sẵn phía sau (tránh chồng nhiều lớp trắng).
 */
public final class LamMoNen {

    private static final String DA_XU_LY = "salala.lamMoNen";

    private LamMoNen() {
    }

    /** Dùng cho vùng nội dung: thẻ trắng -> trắng bán trong suốt, nền trang -> trong suốt. */
    public static void apDung(Component c) {
        duyet(c, false);
    }

    /** Dùng cho header: toàn bộ panel nền trắng/xám nhạt -> trong suốt hoàn toàn. */
    public static void apDungChoHeader(Component c) {
        duyet(c, true);
    }

    private static void duyet(Component c, boolean cheDoHeader) {
        if (c == null || khongDuyet(c)) {
            return;
        }
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (!Boolean.TRUE.equals(jc.getClientProperty(DA_XU_LY))) {
                jc.putClientProperty(DA_XU_LY, Boolean.TRUE);
                if (jc instanceof JPanel) {
                    xuLyPanel((JPanel) jc, cheDoHeader);
                } else if (jc instanceof JScrollPane) {
                    xuLyCuon((JScrollPane) jc);
                }
                theoDoiThanhPhanMoi(jc, cheDoHeader);
            }
        }
        if (c instanceof Container) {
            for (Component con : ((Container) c).getComponents()) {
                duyet(con, cheDoHeader);
            }
        }
    }

    private static boolean khongDuyet(Component c) {
        return c instanceof JTable || c instanceof JTableHeader || c instanceof JComboBox
                || c instanceof JTextComponent || c instanceof AbstractButton
                || c instanceof JScrollBar || c instanceof JSpinner || c instanceof JList
                || c instanceof JTree || c instanceof JLabel || c instanceof JSlider
                || c instanceof JProgressBar || c instanceof NenAnhPanel;
    }

    private static void xuLyPanel(JPanel p, boolean cheDoHeader) {
        if (!p.isOpaque() || p.getBackground() == null) {
            return;
        }
        Color bg = p.getBackground();
        int rgb = bg.getRGB() & 0xFFFFFF;
        if (laMauNenTrang(rgb) || laNenTrangXamNhat(bg)) {
            p.setOpaque(false);
        } else if (rgb == 0xFFFFFF || laTheMauNhat(bg)) {
            // Thẻ trắng hoặc thẻ phủ màu rất nhạt (xanh mint...): giữ màu, chỉ làm bán trong suốt
            p.setOpaque(false);
            if (!cheDoHeader) {
                p.setBorder(new VienNenMo(p.getBorder(),
                        new Color(bg.getRed(), bg.getGreen(), bg.getBlue(),
                                RangBuocGiaoDien.DO_DUC_THE)));
            }
        }
    }

    /** Nền trang xám/trắng ngà rất nhạt nhưng lệch tông, ví dụ (247, 249, 252). */
    private static boolean laNenTrangXamNhat(Color c) {
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        return min >= 244 && max - min <= 6 && (c.getRGB() & 0xFFFFFF) != 0xFFFFFF;
    }

    /** Thẻ phủ màu rất nhạt (có sắc), ví dụ xanh mint (240, 253, 250). */
    private static boolean laTheMauNhat(Color c) {
        int min = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
        int max = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
        return min >= 235 && max - min > 6;
    }

    private static boolean laMauNenTrang(int rgb) {
        if (rgb == (RangBuocGiaoDien.MAU_NEN_TRANG.getRGB() & 0xFFFFFF)) {
            return true;
        }
        Color macDinh = UIManager.getColor("Panel.background");
        return macDinh != null && rgb == (macDinh.getRGB() & 0xFFFFFF);
    }

    private static void xuLyCuon(JScrollPane sp) {
        JViewport vp = sp.getViewport();
        Component view = vp.getView();
        if (view instanceof JTable || view instanceof JList || view instanceof JTree
                || view instanceof JTextComponent) {
            return; // giữ nền đặc cho bảng/danh sách/văn bản
        }
        sp.setOpaque(false);
        vp.setOpaque(false);
        // Cuộn kiểu blit sẽ để lại vệt khi nền trong suốt
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    }

    private static void theoDoiThanhPhanMoi(JComponent jc, boolean cheDoHeader) {
        jc.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                Component con = e.getChild();
                // Chờ thành phần cấu hình xong màu/viền rồi mới xử lý
                SwingUtilities.invokeLater(() -> duyet(con, cheDoHeader));
            }
        });
    }

    /** Viền "vô hình" tô nền trắng bán trong suốt rồi vẽ tiếp viền gốc (nếu có). */
    private static class VienNenMo implements Border {
        private final Border goc;
        private final Color nen;

        VienNenMo(Border goc, Color nen) {
            this.goc = goc;
            this.nen = nen;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(nen);
            g2.fillRect(x, y, w, h);
            g2.dispose();
            if (goc != null) {
                goc.paintBorder(c, g, x, y, w, h);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return goc != null ? goc.getBorderInsets(c) : new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
