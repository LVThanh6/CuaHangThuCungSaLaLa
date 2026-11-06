package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import controller.NhaCungCapController;
import controller.NhaCungCapController.OperationResult;
import controller.NhaCungCapController.Supplier;
import controller.NhaCungCapController.SupplierInput;

/**
 * Giao dien "Quan ly Nha cung cap" - SALALA Pet Shop.
 * JPanel doc lap voi chuc nang CRUD day du.
 */
public class NhaCungCapGUI extends JPanel {

    // ===== Mau sac =====
    private static final Color BG          = new Color(0xF8FAFC);
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER      = new Color(0xE5E7EB);
    private static final Color TEXT_DARK   = new Color(0x111827);
    private static final Color TEXT_GRAY   = new Color(0x6B7280);
    private static final Color PRIMARY     = new Color(0x0F6B5A);
    private static final Color PRIMARY_BG  = new Color(0xDDF3EE);
    private static final Color GREEN       = new Color(0x047857);
    private static final Color RED         = new Color(0xB91C1C);
    private static final Color GREEN_BG    = new Color(0xDCFCE7);
    private static final Color RED_BG      = new Color(0xFEE2E2);

    private static final String FONT = "Segoe UI";

    // ===== Thanh phan =====
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JLabel lblTongNCC;

    private final NhaCungCapController controller = new NhaCungCapController();

    public NhaCungCapGUI() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 24, 24, 24));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createTitle());
        top.add(Box.createVerticalStrut(16));
        add(top, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    // =====================================================================
    // Tieu de
    // =====================================================================
    private JComponent createTitle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Quan ly Nha cung cap");
        t.setFont(new Font(FONT, Font.BOLD, 18));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel("Them, sua, xoa thong tin nha cung cap vat tu");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(TEXT_GRAY);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        lblTongNCC = new JLabel("0 nha cung cap");
        lblTongNCC.setFont(new Font(FONT, Font.BOLD, 13));
        lblTongNCC.setForeground(PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblTongNCC);
        RoundButton btnThem = new RoundButton("+ Them nha cung cap", PRIMARY, PRIMARY, WHITE);
        btnThem.addActionListener(e -> themNhaCungCap());
        right.add(btnThem);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // Bang du lieu
    // =====================================================================
    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        // ---- thanh loc ----
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filter.setOpaque(false);

        txtSearch = new SearchField("Tim theo ten, ma, SDT...");
        txtSearch.setPreferredSize(new Dimension(280, 30));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        filter.add(txtSearch);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 6, 0, 6));
        filterWrap.add(filter, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);

        // ---- bang ----
        String[] cols = {"MA NCC", "TEN NHA CUNG CAP", "DIA CHI", "SO DIEN THOAI", "EMAIL", "THAO TAC"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
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

        // renderer
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer(PRIMARY, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new TextRenderer(TEXT_DARK, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(3).setCellRenderer(new TextRenderer(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());

        int[] widths = {90, 220, 200, 130, 180, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Click handler cho cot Thao tac
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != 5) return;
                int modelRow = table.convertRowIndexToModel(row);
                String maNCC = (String) model.getValueAt(modelRow, 0);

                int relX = e.getX() - table.getCellRect(row, col, false).x;
                if (relX < 50) {
                    suaNhaCungCap(modelRow);
                } else {
                    xoaNhaCungCap(modelRow, maNCC);
                }
            }
        });
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int c = table.columnAtPoint(e.getPoint());
                int r = table.rowAtPoint(e.getPoint());
                boolean hand = c == 5 && r >= 0;
                table.setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        box.add(filterWrap, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        return box;
    }

    // =====================================================================
    // Du lieu
    // =====================================================================
    public void loadData() {
        model.setRowCount(0);
        ArrayList<Supplier> suppliers = new ArrayList<>(controller.taiDanhSach());
        for (Supplier supplier : suppliers) {
            model.addRow(new Object[]{
                supplier.getCode(), supplier.getName(), supplier.getAddress(),
                supplier.getPhone(), supplier.getEmail(), ""
            });
        }
        lblTongNCC.setText(suppliers.size() + " nha cung cap");
    }

    // =====================================================================
    // CRUD
    // =====================================================================
    private void themNhaCungCap() {
        SupplierInput ncc = openEditor(null);
        if (ncc != null) {
            OperationResult result = controller.tao(ncc);
            if (result.isSuccessful()) {
                JOptionPane.showMessageDialog(this, "Them nha cung cap thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void suaNhaCungCap(int modelRow) {
        String maNCC = (String) model.getValueAt(modelRow, 0);
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        String diaChi = (String) model.getValueAt(modelRow, 2);
        String sdt = (String) model.getValueAt(modelRow, 3);
        String email = (String) model.getValueAt(modelRow, 4);

        SupplierInput old = new SupplierInput(maNCC, tenNCC, diaChi, sdt, email);
        SupplierInput updated = openEditor(old);
        if (updated != null) {
            OperationResult result = controller.capNhat(updated);
            if (result.isSuccessful()) {
                JOptionPane.showMessageDialog(this, "Cap nhat nha cung cap thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaNhaCungCap(int modelRow, String maNCC) {
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon xoa nha cung cap \"" + tenNCC + "\" (Ma: " + maNCC + ")?\nThao tac nay khong the hoan tac.",
                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        OperationResult result = controller.xoa(maNCC);
        if (result.isSuccessful()) {
            JOptionPane.showMessageDialog(this, "Da xoa nha cung cap thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Hop thoai them / sua
    // =====================================================================
    private SupplierInput openEditor(SupplierInput src) {
        final SupplierInput[] result = {null};
        boolean isNew = (src == null);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Them nha cung cap moi" : "Sua nha cung cap", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(isNew ? controller.taoMaMoi(suppliersFromModel()) : src.getMa());
        txtMa.setEditable(isNew);
        JTextField txtTen = new JTextField(isNew ? "" : src.getTen());
        JTextField txtDiaChi = new JTextField(isNew ? "" : src.getDiaChi());
        JTextField txtSDT = new JTextField(isNew ? "" : src.getSdt());
        JTextField txtEmail = new JTextField(isNew ? "" : src.getEmail());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 10, 20));
        addFormRow(form, 0, "Ma NCC", txtMa);
        addFormRow(form, 1, "Ten nha cung cap (*)", txtTen);
        addFormRow(form, 2, "Dia chi", txtDiaChi);
        addFormRow(form, 3, "So dien thoai", txtSDT);
        addFormRow(form, 4, "Email", txtEmail);

        JButton btnLuu = new JButton(isNew ? "Them moi" : "Luu thay doi");
        btnLuu.setFont(new Font(FONT, Font.BOLD, 13));
        btnLuu.setBackground(PRIMARY);
        btnLuu.setForeground(WHITE);
        btnLuu.setFocusPainted(false);
        btnLuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnHuy = new JButton("Huy");
        btnHuy.setFont(new Font(FONT, Font.PLAIN, 13));
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnHuy.addActionListener(e -> d.dispose());
        btnLuu.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();

            String validationError = NhaCungCapController.validate(
                    new SupplierInput(ma, ten, diaChi, sdt, email));
            if (validationError != null) {
                JOptionPane.showMessageDialog(d, validationError, "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            result[0] = new SupplierInput(ma, ten, diaChi, sdt, email);
            d.dispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.add(btnLuu);
        btns.add(btnHuy);

        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(btns, BorderLayout.SOUTH);
        d.setSize(500, 320);
        d.setLocationRelativeTo(this);
        d.setVisible(true);

        return result[0];
    }

    private ArrayList<Supplier> suppliersFromModel() {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            suppliers.add(new Supplier((String) model.getValueAt(i, 0),
                    (String) model.getValueAt(i, 1), (String) model.getValueAt(i, 2),
                    (String) model.getValueAt(i, 3), (String) model.getValueAt(i, 4)));
        }
        return suppliers;
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(4, 0, 4, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(FONT, Font.PLAIN, 12));
        form.add(lbl, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        comp.setFont(new Font(FONT, Font.PLAIN, 12));
        form.add(comp, gc);
    }

    // =====================================================================
    // Loc
    // =====================================================================
    private void applyFilter() {
        final String keyword = txtSearch.getText();
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                Supplier supplier = new Supplier(
                        e.getStringValue(0), e.getStringValue(1), e.getStringValue(2),
                        e.getStringValue(3), e.getStringValue(4));
                return controller.phuHopTimKiem(supplier, keyword);
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

    private static class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ActionRenderer() {
            panel.setBorder(new EmptyBorder(11, 12, 0, 0));
            panel.add(link("Sua", PRIMARY));
            panel.add(link("Xoa", RED));
        }
        private static JLabel link(String text, Color color) {
            JLabel l = new JLabel(text);
            l.setFont(new Font(FONT, Font.BOLD, 12));
            l.setForeground(color);
            l.setPreferredSize(new Dimension(46, 20));
            return l;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    // =====================================================================
    // Component phu
    // =====================================================================
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;
        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc; this.fill = RangBuocGiaoDien.theMo(fill); this.line = line;
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

    private static class RoundButton extends JButton {
        private final Color fill, line, fg;
        RoundButton(String text, Color fill, Color line, Color fg) {
            super(text);
            this.fill = fill; this.line = line; this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 12));
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

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
            g2.setColor(TEXT_GRAY);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(10, cy - 6, 8, 8));
            g2.drawLine(17, cy + 1, 21, cy + 5);
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
    // Chay thu doc lap
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("SALALA Pet Shop - Nha cung cap");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 700);
            f.setLocationRelativeTo(null);
            f.setContentPane(new NhaCungCapGUI());
            f.setVisible(true);
        });
    }
}
