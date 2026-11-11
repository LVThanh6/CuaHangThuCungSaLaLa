package GUI.Chung;

import Service.Chung.NenGiaoDienService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/**
 * Gom toàn bộ phần giao diện liên quan đến ảnh nền của ứng dụng:
 * - NenAnhPanel : panel có ảnh nền phủ kín + thanh kính mờ (header).
 * - TamNenNoiDung : tấm nền kem bán trong suốt phía sau vùng nội dung.
 * - LamMoNen : làm mờ/trong suốt các panel con để ảnh nền hiện lên.
 * Phần xử lý ảnh và phân loại màu nền nằm ở
 * controller.Chung.NenGiaoDienService.
 */
public final class NenGiaoDienGUI {

    private NenGiaoDienGUI() {
    }

    // ======================================================================
    // NenAnhPanel
    // ======================================================================
    /**
     * Panel có ảnh nền phủ kín (căn giữa, giữ đúng tỉ lệ ảnh) và một lớp phủ trắng
     * nhẹ.
     * Có thể chỉ định một "thanh mờ" (thường là header): vùng đó được vẽ lại bằng
     * phiên bản ảnh đã làm mờ + lớp phủ kem đậm hơn (hiệu ứng kính mờ).
     */
    public static class NenAnhPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final NenGiaoDienService CONTROLLER = new NenGiaoDienService();

        private NenGiaoDienService.AnhNenDaXuLy anhDaXuLy;
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
            NenGiaoDienService.taiAnhNen();
        }

        /** Có nạp được ảnh nền hay không. */
        public boolean coAnh() {
            return CONTROLLER.coAnhNen();
        }

        /**
         * Thành phần (con trực tiếp của panel này) sẽ được vẽ nền kính mờ, ví dụ
         * header.
         */
        public void setThanhMo(Component c) {
            this.thanhMo = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // tô màu nền phẳng trước
            if (!coAnh()) {
                return;
            }
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            if (anhDaXuLy == null || w != rongCache || h != caoCache) {
                anhDaXuLy = CONTROLLER.taoAnhNen(w, h);
                rongCache = w;
                caoCache = h;
            }
            if (anhDaXuLy == null) {
                return;
            }
            g.drawImage(anhDaXuLy.anhNen, 0, 0, null);

            // Thanh kính mờ (header)
            if (thanhMo != null && thanhMo.getParent() == this && thanhMo.getHeight() > 0) {
                int y1 = Math.max(0, thanhMo.getY());
                int y2 = Math.min(h, thanhMo.getY() + thanhMo.getHeight());
                if (y2 > y1) {
                    g.drawImage(anhDaXuLy.anhMo, 0, y1, w, y2, 0, y1, w, y2, null);
                    g.setColor(new Color(255, 252, 245, RangBuocGiaoDien.DO_DUC_THANH_MO));
                    g.fillRect(0, y1, w, y2 - y1);
                }
            }
        }
    }

    // ======================================================================
    // TamNenNoiDung
    // ======================================================================
    /**
     * Vùng chứa nội dung: vẽ một "tấm nền" kem bán trong suốt ngay phía sau nội
     * dung
     * để chữ luôn rõ nhưng ảnh vẫn thấp thoáng bên dưới.
     * Mặc định (lề = 0) tấm nền phủ kín 100% vùng còn lại dưới header; nếu đặt lề >
     * 0
     * thì tấm nền bo góc, có bóng và ảnh nền hiện ra như khung viền quanh mép.
     */
    public static class TamNenNoiDung extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int BO_GOC = 18;

        public TamNenNoiDung(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(
                    RangBuocGiaoDien.LE_NOI_DUNG_DOC, RangBuocGiaoDien.LE_NOI_DUNG_NGANG,
                    RangBuocGiaoDien.LE_NOI_DUNG_DOC, RangBuocGiaoDien.LE_NOI_DUNG_NGANG));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Insets in = getInsets();
            boolean kinMep = in.left == 0 && in.top == 0 && in.right == 0 && in.bottom == 0;
            int x = in.left;
            int y = in.top;
            int w = getWidth() - in.left - in.right;
            int h = getHeight() - in.top - in.bottom;
            if (kinMep) {
                // Phủ kín toàn bộ vùng nội dung: không bo góc, không bóng, không hở khe
                Color kem = RangBuocGiaoDien.MAU_TAM_NOI_DUNG;
                g2.setColor(new Color(kem.getRed(), kem.getGreen(), kem.getBlue(),
                        RangBuocGiaoDien.DO_DUC_TAM_NOI_DUNG));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Đường kẻ mảnh phân cách với header
                g2.setColor(new Color(226, 232, 240, 220));
                g2.drawLine(0, 0, getWidth(), 0);
            } else if (w > 0 && h > 0) {
                // Bóng đổ nhẹ
                for (int i = 1; i <= 4; i++) {
                    g2.setColor(new Color(60, 40, 20, 10));
                    g2.fillRoundRect(x - i, y - i + 2, w + 2 * i, h + 2 * i, BO_GOC + i, BO_GOC + i);
                }
                // Tấm nền kem bán trong suốt
                Color kem = RangBuocGiaoDien.MAU_TAM_NOI_DUNG;
                g2.setColor(new Color(kem.getRed(), kem.getGreen(), kem.getBlue(),
                        RangBuocGiaoDien.DO_DUC_TAM_NOI_DUNG));
                g2.fillRoundRect(x, y, w, h, BO_GOC, BO_GOC);
                // Viền mảnh
                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawRoundRect(x, y, w - 1, h - 1, BO_GOC, BO_GOC);
            }
            g2.dispose();
        }
    }

    // ======================================================================
    // LamMoNen
    // ======================================================================
    /**
     * Làm "mờ" nền các màn hình con để ảnh nền của GiaoDienChinhGUI hiện lên:
     * - JPanel nền xám/xanh nhạt của trang (#F8FAFC, màu Panel mặc định) -> trong
     * suốt hoàn toàn.
     * - JPanel nền trắng (thẻ/card) -> trắng bán trong suốt (độ đục:
     * RangBuocGiaoDien.DO_DUC_THE).
     * - JScrollPane bọc panel -> viewport trong suốt (bảng JTable vẫn giữ nền đặc
     * cho dễ đọc).
     * Các thành phần được thêm vào sau này cũng tự được xử lý.
     *
     * Với header (apDungChoHeader): mọi panel nền trắng/xám nhạt đều trong suốt
     * hoàn toàn,
     * vì nền kính mờ đã được NenAnhPanel vẽ sẵn phía sau (tránh chồng nhiều lớp
     * trắng).
     */
    public static final class LamMoNen {

        private static final String DA_XU_LY = "salala.lamMoNen";
        private static final NenGiaoDienService CONTROLLER = new NenGiaoDienService();

        private LamMoNen() {
        }

        /**
         * Dùng cho vùng nội dung: thẻ trắng -> trắng bán trong suốt, nền trang -> trong
         * suốt.
         */
        public static void apDung(Component c) {
            duyet(c, false);
        }

        /**
         * Dùng cho header: toàn bộ panel nền trắng/xám nhạt -> trong suốt hoàn toàn.
         */
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
            // Quyết định (trong suốt / thẻ mờ / giữ nguyên) do controller đưa ra
            switch (CONTROLLER.phanLoaiNen(bg)) {
                case TRONG_SUOT:
                    p.setOpaque(false);
                    break;
                case THE_MO:
                    // Thẻ trắng hoặc thẻ phủ màu rất nhạt: giữ màu, chỉ làm bán trong suốt
                    p.setOpaque(false);
                    if (!cheDoHeader) {
                        p.setBorder(new VienNenMo(p.getBorder(), CONTROLLER.mauTheMo(bg)));
                    }
                    break;
                default:
                    break;
            }
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
}
