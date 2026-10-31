package GUI.VatTu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Giao diện "Tồn kho vật tư" - SALALA Pet Shop.
 * Đây là một JPanel, đặt vào vùng nội dung bên dưới thanh tab của frame chính, ví dụ:
 *   contentPanel.add(new VatTu(), "Vật tư");
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class VatTu extends JPanel {

    // ===== Màu sắc =====
    private static final Color BG          = new Color(0xF8FAFC);
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER      = new Color(0xE5E7EB);
    private static final Color TEXT_DARK   = new Color(0x111827);
    private static final Color TEXT_GRAY   = new Color(0x6B7280);
    private static final Color PRIMARY     = new Color(0x0F6B5A);
    private static final Color PRIMARY_BG  = new Color(0xDDF3EE);
    private static final Color GREEN       = new Color(0x047857);
    private static final Color ORANGE      = new Color(0xC2410C);
    private static final Color RED         = new Color(0xB91C1C);
    private static final Color GREEN_BG    = new Color(0xDCFCE7);
    private static final Color ORANGE_BG   = new Color(0xFEF3C7);
    private static final Color RED_BG      = new Color(0xFEE2E2);

    private static final String FONT = "Segoe UI";
    private static final int SAP_HET_NGUONG = 30; // tồn <= ngưỡng này là "Sắp hết"

    // ===== Thành phần =====
    private JLabel lblTong, lblTonDu, lblSapHet, lblHetTon;
    private JTextField txtSearch;
    private JComboBox<String> cboDanhMuc, cboTrangThai;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private static final String CARD_TON_KHO = "TON_KHO";
    private static final String CARD_PHIEU_NHAP = "PHIEU_NHAP";
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final java.util.List<JButton> navButtons = new java.util.ArrayList<>();
    private int tong, tonDu, sapHet, hetTon;
    private static final String CARD_PHIEU_XUAT = "PHIEU_XUAT";

    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    public VatTu() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 24, 24, 24));

        // thanh tab con luôn hiển thị
        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.setOpaque(false);
        navWrap.setBorder(new EmptyBorder(0, 0, 16, 0));
        navWrap.add(createSubNav(), BorderLayout.WEST);
        add(navWrap, BorderLayout.NORTH);

        // màn hình 1: Tồn kho vật tư
        JPanel tonKho = new JPanel(new BorderLayout());
        tonKho.setOpaque(false);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createTitle());
        top.add(Box.createVerticalStrut(14));
        top.add(createStatCards());
        top.add(Box.createVerticalStrut(16));
        tonKho.add(top, BorderLayout.NORTH);
        tonKho.add(createTablePanel(), BorderLayout.CENTER);

        // màn hình 2: Phiếu nhập kho
        PhieuNhapKho phieuNhap = new PhieuNhapKho(
                this::getDanhSachVatTu,
                this::tangTon,
                () -> System.out.println("TODO: chuyển sang Phiếu xuất kho"));

        cardPanel.setOpaque(false);
        cardPanel.add(tonKho, CARD_TON_KHO);
        cardPanel.add(phieuNhap, CARD_PHIEU_NHAP);
        add(cardPanel, BorderLayout.CENTER);

        loadSampleData();
        setThongKe(142, 118, 19, 5);
        showCard(CARD_TON_KHO, 0);
        
     // màn hình 3: Phiếu xuất kho
        PhieuXuatKho phieuXuat = new PhieuXuatKho(
                this::getDanhSachVatTu,
                this::tangTon,      // phiếu xuất gọi tangTon(ma, -soLuong) để trừ tồn
                this::getTonKho);
        cardPanel.add(phieuXuat, CARD_PHIEU_XUAT);
    }
    // =====================================================================
    // Thanh điều hướng con: Vật tư | Nhà cung cấp | Phiếu nhập kho | Phiếu xuất kho
    // =====================================================================
    private JComponent createSubNav() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        String[] items = {"Vật tư", "Nhà cung cấp", "Phiếu nhập kho ▾", "Phiếu xuất kho ▾"};
        for (int i = 0; i < items.length; i++) {
            JButton b = new JButton(items[i]);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setBorder(new EmptyBorder(7, 14, 7, 14));
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            final int idx = i;
            final String name = items[i];
            b.addActionListener((ActionEvent e) -> {
                switch (idx) {
                    case 0: showCard(CARD_TON_KHO, 0); break;
                    case 2: showCard(CARD_PHIEU_NHAP, 2); break;
                    case 3: showCard(CARD_PHIEU_XUAT, 3); break;
                    default: System.out.println("TODO: chuyển tới " + name);
                }
            });
            navButtons.add(b);
            p.add(b);
        }
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    public int getTonKho(String ma) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (ma.equals(model.getValueAt(i, 0))) return ((Number) model.getValueAt(i, 3)).intValue();
        }
        return 0;
    }
	private void showCard(String card, int activeIdx) {
        cards.show(cardPanel, card);
        for (int i = 0; i < navButtons.size(); i++) {
            JButton b = navButtons.get(i);
            boolean on = (i == activeIdx);
            b.setFont(new Font(FONT, on ? Font.BOLD : Font.PLAIN, 13));
            b.setBackground(on ? PRIMARY_BG : BG);
            b.setForeground(on ? PRIMARY : TEXT_DARK);
        }
    }

    private JComponent createTitle() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Tồn kho vật tư");
        t.setFont(new Font(FONT, Font.BOLD, 18));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel("Kiểm tra và cập nhật số lượng tồn kho qua phiếu nhập / xuất");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(TEXT_GRAY);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(s);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    // =====================================================================
    // 4 thẻ thống kê
    // =====================================================================
    private JComponent createStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        lblTong   = new JLabel();
        lblTonDu  = new JLabel();
        lblSapHet = new JLabel();
        lblHetTon = new JLabel();

        row.add(statCard("TỔNG DANH MỤC", lblTong, TEXT_DARK));
        row.add(statCard("TỒN ĐỦ", lblTonDu, GREEN));
        row.add(statCard("SẮP HẾT TỒN", lblSapHet, ORANGE));
        row.add(statCard("HẾT TỒN", lblHetTon, RED));
        return row;
    }

    private JComponent statCard(String title, JLabel value, Color color) {
        RoundedPanel card = new RoundedPanel(12, WHITE, BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel t = new JLabel(title);
        t.setFont(new Font(FONT, Font.BOLD, 10));
        t.setForeground(color == TEXT_DARK ? TEXT_GRAY : color);

        value.setFont(new Font(FONT, Font.BOLD, 20));
        value.setForeground(color);

        t.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(value);
        return card;
    }

    /** Cập nhật 4 số liệu thống kê. */
    public void setThongKe(int tong, int tonDu, int sapHet, int hetTon) {
        this.tong = tong; this.tonDu = tonDu; this.sapHet = sapHet; this.hetTon = hetTon;
        lblTong.setText(tong + " Mặt hàng");
        lblTonDu.setText(tonDu + " Mặt hàng");
        lblSapHet.setText(sapHet + " Mặt hàng");
        lblHetTon.setText(hetTon + " Mặt hàng");
    }

    /** Danh sách vật tư cho phiếu nhập: {mã, tên, đơn vị, giá nhập}. */
    public java.util.List<Object[]> getDanhSachVatTu() {
        java.util.List<Object[]> ds = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            ds.add(new Object[]{model.getValueAt(i, 0), model.getValueAt(i, 1),
                    model.getValueAt(i, 4), ((Number) model.getValueAt(i, 5)).longValue()});
        }
        return ds;
    }

    /** Cộng số lượng vào tồn kho khi phiếu nhập được xác nhận. */
    public void tangTon(String ma, int soLuong) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (ma.equals(model.getValueAt(i, 0))) {
                int cu = ((Number) model.getValueAt(i, 3)).intValue();
                int moi = cu + soLuong;
                String ttCu = tinhTrangThai(cu), ttMoi = tinhTrangThai(moi);
                model.setValueAt(moi, i, 3);
                model.setValueAt(ttMoi, i, 6);
                if (!ttCu.equals(ttMoi)) {            // đổi nhóm trạng thái -> cập nhật 4 thẻ thống kê
                    doiDem(ttCu, -1);
                    doiDem(ttMoi, +1);
                    setThongKe(tong, tonDu, sapHet, hetTon);
                }
                return;
            }
        }
    }

    private void doiDem(String tt, int delta) {
        if ("Còn hàng".equals(tt)) tonDu += delta;
        else if ("Sắp hết".equals(tt)) sapHet += delta;
        else hetTon += delta;
    }

    // =====================================================================
    // Khung chứa bộ lọc + bảng
    // =====================================================================
    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filter.setOpaque(false);

        txtSearch = new SearchField("Tìm theo tên hoặc mã...");
        txtSearch.setPreferredSize(new Dimension(230, 30));

        cboDanhMuc = new JComboBox<>(new String[]{
                "Danh mục", "Kháng sinh", "Vắc-xin", "Giảm đau", "Ký sinh", "Tiêu hao"});
        cboTrangThai = new JComboBox<>(new String[]{
                "Trạng thái", "Còn hàng", "Sắp hết", "Hết tồn"});
        for (JComboBox<String> c : java.util.List.of(cboDanhMuc, cboTrangThai)) {
            c.setFont(new Font(FONT, Font.PLAIN, 12));
            c.setBackground(WHITE);
            c.setPreferredSize(new Dimension(c == cboDanhMuc ? 105 : 110, 30));
            c.addActionListener(e -> applyFilter());
        }
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));

        filter.add(txtSearch);
        filter.add(cboDanhMuc);
        filter.add(cboTrangThai);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 6, 0, 6));
        filterWrap.add(filter, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);

        // ---- bảng ----
        String[] cols = {"MÃ VT", "TÊN VẬT TƯ", "DANH MỤC", "TỒN KHO", "ĐƠN VỊ", "GIÁ NHẬP", "TRẠNG THÁI"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == 3 || c == 5) ? Number.class : String.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font(FONT, Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xF0F1F3));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xE8F5F2));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // header
        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new HeaderRenderer());

        // renderer từng cột
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer(TEXT_DARK, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new TextRenderer(PRIMARY, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(3).setCellRenderer(new StockRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(5).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        int[] widths = {80, 230, 120, 80, 80, 110, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        box.add(filterWrap, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        return box;
    }

    // =====================================================================
    // Dữ liệu
    // =====================================================================
    private void loadSampleData() {
        addRow("VT-001", "Amoxicillin 250mg", "Kháng sinh", 340, "Ống", 45000);
        addRow("VT-002", "Rabisin Vắc-xin Dại", "Vắc-xin", 15, "Lọ", 320000);
        addRow("VT-003", "Prednisolone 5mg", "Giảm đau", 0, "Viên", 8500);
        addRow("VT-004", "Fronil Spot-on Dog", "Ký sinh", 85, "Tuýp", 78000);
        addRow("VT-005", "Bông y tế vô trùng", "Tiêu hao", 60, "Gói", 12000);
        addRow("VT-006", "Kim tiêm 5ml", "Tiêu hao", 22, "Hộp", 35000);
    }

    /** Thêm 1 dòng; trạng thái tự tính theo số lượng tồn. */
    public void addRow(String ma, String ten, String danhMuc, int ton, String donVi, long giaNhap) {
        model.addRow(new Object[]{ma, ten, danhMuc, ton, donVi, giaNhap, tinhTrangThai(ton)});
    }

    /** Xóa toàn bộ dòng (dùng khi nạp lại dữ liệu từ DAO). */
    public void clearTable() {
        model.setRowCount(0);
    }

    private static String tinhTrangThai(int ton) {
        if (ton <= 0) return "Hết tồn";
        if (ton <= SAP_HET_NGUONG) return "Sắp hết";
        return "Còn hàng";
    }

    // =====================================================================
    // Lọc
    // =====================================================================
    private static String normalize(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    private void applyFilter() {
        final String kw = normalize(txtSearch.getText());
        final String dm = (String) cboDanhMuc.getSelectedItem();
        final String tt = (String) cboTrangThai.getSelectedItem();
        final boolean allDm = cboDanhMuc.getSelectedIndex() == 0;
        final boolean allTt = cboTrangThai.getSelectedIndex() == 0;

        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                boolean okKw = kw.isEmpty()
                        || normalize(e.getStringValue(0)).contains(kw)
                        || normalize(e.getStringValue(1)).contains(kw);
                boolean okDm = allDm || dm.equals(e.getStringValue(2));
                boolean okTt = allTt || tt.equals(e.getStringValue(6));
                return okKw && okDm && okTt;
            }
        });
    }

    // =====================================================================
    // Renderers
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(new Font(FONT, Font.BOLD, 10));
            setForeground(TEXT_GRAY);
            setBackground(new Color(0xF9FAFB));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 12, 0, 8)));
            return this;
        }
    }

    private static class TextRenderer extends DefaultTableCellRenderer {
        private final Color color;
        private final int style;
        TextRenderer(Color color, int style, int align) {
            this.color = color;
            this.style = style;
            setHorizontalAlignment(align);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setFont(new Font(FONT, style, 12));
            setForeground(color);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            return this;
        }
    }

    /** Cột tồn kho: 0 -> đỏ, sắp hết -> cam, còn lại -> đen đậm. */
    private static class StockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            int ton = (v instanceof Number) ? ((Number) v).intValue() : 0;
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 12));
            setForeground(ton <= 0 ? RED : (ton <= SAP_HET_NGUONG ? ORANGE : TEXT_DARK));
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setText(String.valueOf(ton));
            return this;
        }
    }

    private static class MoneyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font(FONT, Font.PLAIN, 12));
            setForeground(TEXT_DARK);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            if (v instanceof Number) setText(MONEY.format(((Number) v).longValue()) + " đ");
            return this;
        }
    }

    /** Cột trạng thái: nhãn bo tròn có màu. */
    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Badge badge = new Badge();

        StatusRenderer() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.insets = new Insets(0, 12, 0, 0);
            panel.add(badge, gc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();
            badge.setText(s);
            switch (s) {
                case "Còn hàng": badge.setColors(GREEN_BG, GREEN); break;
                case "Sắp hết":  badge.setColors(ORANGE_BG, ORANGE); break;
                default:         badge.setColors(RED_BG, RED); break;
            }
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    private static class Badge extends JLabel {
        private Color bg = GREEN_BG;
        Badge() {
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 11));
            setBorder(new EmptyBorder(2, 9, 2, 9));
        }
        void setColors(Color bg, Color fg) { this.bg = bg; setForeground(fg); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =====================================================================
    // Component phụ
    // =====================================================================
    /** Panel bo góc có viền. */
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;
        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc; this.fill = fill; this.line = line;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Ô tìm kiếm có icon kính lúp và chữ gợi ý. */
    private static class SearchField extends JTextField {
        private final String hint;
        SearchField(String hint) {
            this.hint = hint;
            setFont(new Font(FONT, Font.PLAIN, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(0, 28, 0, 8)));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            // kính lúp
            g2.setColor(TEXT_GRAY);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(10, cy - 6, 8, 8));
            g2.drawLine(17, cy + 1, 21, cy + 5);
            // hint
            if (getText().isEmpty()) {
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 28, cy + (fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }

    // =====================================================================
    // Chạy thử độc lập
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Hệ thống Quản lý SALALA Pet Shop - Vật tư");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 700);
            f.setLocationRelativeTo(null);
            f.setContentPane(new VatTu());
            f.setVisible(true);
        });
    }
}