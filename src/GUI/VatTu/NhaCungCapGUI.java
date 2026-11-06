package GUI.VatTu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.Normalizer;
import java.util.ArrayList;

import dao.NhaCungCap_DAO;
import entity.NhaCungCap_entity;

/**
 * Giao dien "Quan ly Nha cung cap" - SALALA Pet Shop (Ket noi CSDL that).
 */
public class NhaCungCapGUI extends JPanel {

    private static final Color BG         = new Color(0xF8FAFC);
    private static final Color WHITE      = Color.WHITE;
    private static final Color BORDER     = new Color(0xE5E7EB);
    private static final Color TEXT_DARK  = new Color(0x111827);
    private static final Color TEXT_GRAY  = new Color(0x6B7280);
    private static final Color PRIMARY    = new Color(0x0F6B5A);
    private static final Color RED        = new Color(0xB91C1C);

    private static final String FONT = "Segoe UI";

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JLabel lblTongNCC;

    private final NhaCungCap_DAO nccDAO = new NhaCungCap_DAO();

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
        JLabel s = new JLabel("Them, sua, xoa thong tin nha cung cap vat tu tu co so du lieu");
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

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filter.setOpaque(false);

        txtSearch = new SearchField("Tim theo ten, ma, SDT...");
        txtSearch.setPreferredSize(new Dimension(280, 30));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        filter.add(txtSearch);

        RoundButton btnThem = new RoundButton("+ Them", PRIMARY, PRIMARY, WHITE);
        RoundButton btnSua  = new RoundButton("Sua", WHITE, BORDER, TEXT_DARK);
        RoundButton btnXoa  = new RoundButton("Xoa", WHITE, BORDER, RED);
        btnThem.addActionListener(e -> themNhaCungCap());
        btnSua.addActionListener(e -> suaNhaCungCap());
        btnXoa.addActionListener(e -> xoaNhaCungCap());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        actions.setOpaque(false);
        actions.add(btnThem);
        actions.add(btnSua);
        actions.add(btnXoa);

        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.add(filter, BorderLayout.WEST);
        filterRow.add(actions, BorderLayout.EAST);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 6, 0, 6));
        filterWrap.add(filterRow, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);

        String[] cols = {"MA NCC", "TEN NHA CUNG CAP", "DIA CHI", "SO DIEN THOAI", "EMAIL"};
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new HeaderRenderer());

        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer(PRIMARY, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new TextRenderer(TEXT_DARK, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(3).setCellRenderer(new TextRenderer(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextRenderer(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT));

        int[] widths = {90, 240, 240, 140, 200};
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
    // LOAD DỮ LIỆU THẬT TỪ CƠ SỞ DỮ LIỆU
    // =====================================================================
    public void loadData() {
        model.setRowCount(0);

        ArrayList<NhaCungCap_entity> dsNCC = nccDAO.getAllNhaCungCap();
        if (nccDAO.getLastError() != null) {
            lblTongNCC.setText("0 nha cung cap");
            showError("Khong the tai du lieu tu CSDL!\n\n" + nccDAO.getLastError(), "Loi ket noi");
            return;
        }

        for (NhaCungCap_entity ncc : dsNCC) {
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getDiaChi(),
                ncc.getSdt(),
                ncc.getEmail()
            });
        }
        lblTongNCC.setText(dsNCC.size() + " nha cung cap");
    }

    private void showError(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    // CRUD THỰC TẾ VỚI CSDL
    // =====================================================================
    private void themNhaCungCap() {
        NhaCungCap_entity ncc = openEditor(null);
        if (ncc == null) return;

        if (nccDAO.createNhaCungCap(ncc)) {
            JOptionPane.showMessageDialog(this, "Them nha cung cap vao CSDL thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            showError("Them that bai!\n\n" + nccDAO.getLastError(), "Loi");
        }
    }

    /** Lay chi so dong (model) dang chon; -1 neu chua chon va hien thong bao. */
    private int getSelectedModelRow(String hanhDong) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot nha cung cap de " + hanhDong + ".",
                    "Chua chon dong", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    private void suaNhaCungCap() {
        int modelRow = getSelectedModelRow("sua");
        if (modelRow < 0) return;

        String maNCC  = str(model.getValueAt(modelRow, 0));
        String tenNCC = str(model.getValueAt(modelRow, 1));
        String diaChi = str(model.getValueAt(modelRow, 2));
        String sdt    = str(model.getValueAt(modelRow, 3));
        String email  = str(model.getValueAt(modelRow, 4));

        NhaCungCap_entity old = new NhaCungCap_entity(maNCC, tenNCC, diaChi, sdt, email);
        NhaCungCap_entity updated = openEditor(old);
        if (updated == null) return;

        if (nccDAO.updateNhaCungCap(updated)) {
            JOptionPane.showMessageDialog(this, "Cap nhat nha cung cap thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            showError("Cap nhat that bai!\n\n" + nccDAO.getLastError(), "Loi");
        }
    }

    private void xoaNhaCungCap() {
        int modelRow = getSelectedModelRow("xoa");
        if (modelRow < 0) return;

        String maNCC  = str(model.getValueAt(modelRow, 0));
        String tenNCC = str(model.getValueAt(modelRow, 1));
        int ok = JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon xoa nha cung cap \"" + tenNCC + "\" (Ma: " + maNCC + ")?\nThao tac nay khong the hoan tac.",
                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        if (nccDAO.deleteNhaCungCap(maNCC)) {
            JOptionPane.showMessageDialog(this, "Da xoa nha cung cap khoi CSDL thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            showError("Xoa that bai!\n\n" + nccDAO.getLastError(), "Loi");
        }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private NhaCungCap_entity openEditor(NhaCungCap_entity src) {
        final NhaCungCap_entity[] result = {null};
        boolean isNew = (src == null);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Them nha cung cap moi" : "Sua nha cung cap", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(isNew ? taoMaMoi() : src.getMaNCC());
        txtMa.setEditable(isNew);
        JTextField txtTen = new JTextField(isNew ? "" : src.getTenNCC());
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

            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Ma NCC va Ten NCC khong duoc de trong.", "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sdt.isEmpty() && !sdt.matches("\\d{9,11}")) {
                JOptionPane.showMessageDialog(d, "So dien thoai phai gom 9-11 chu so.", "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
                JOptionPane.showMessageDialog(d, "Email khong hop le.", "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            result[0] = new NhaCungCap_entity(ma, ten, diaChi, sdt, email);
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

    /** Lay ma moi tu CSDL (tranh trung ma); neu loi thi tinh tu du lieu dang hien tren bang. */
    private String taoMaMoi() {
        String ma = nccDAO.taoMaMoi();
        if (ma != null) return ma;

        int max = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String m = (String) model.getValueAt(i, 0);
            if (m != null && m.startsWith("NCC")) {
                try {
                    max = Math.max(max, Integer.parseInt(m.substring(3)));
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("NCC%03d", max + 1);
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

    private static String normalize(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('\u0111', 'd').replace('\u0110', 'D').toLowerCase().trim();
    }

    private void applyFilter() {
        final String kw = normalize(txtSearch.getText());
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                if (kw.isEmpty()) return true;
                for (int i = 0; i < 5; i++) {
                    if (normalize(e.getStringValue(i)).contains(kw)) return true;
                }
                return false;
            }
        });
    }

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

    private static class RoundButton extends JButton {
        private final Color fill, line;
        RoundButton(String text, Color fill, Color line, Color fg) {
            super(text);
            this.fill = fill; this.line = line;
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