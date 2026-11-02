package GUI.VatTu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.text.Normalizer;
import java.util.ArrayList;

import dao.NhaCungCap_DAO;
import entity.NhaCungCap_entity;

/**
 * Giao diện "Quản lý Nhà cung cấp" - SALALA Pet Shop.
 * Quản lý danh sách nhà cung cấp với thanh toolbar (Thêm, Sửa, Xóa) và tìm kiếm.
 */
public class NhaCungCapGUI extends JPanel {

    // ===== Màu sắc =====
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

    // ===== Thành phần =====
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JLabel lblTongNCC;

    private NhaCungCap_DAO nccDAO = new NhaCungCap_DAO();

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
    // Tiêu đề
    // =====================================================================
    private JComponent createTitle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Quản lý Nhà cung cấp");
        t.setFont(new Font(FONT, Font.BOLD, 18));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel("Thêm, sửa, xóa thông tin nhà cung cấp vật tư");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(TEXT_GRAY);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        lblTongNCC = new JLabel("0 nhà cung cấp");
        lblTongNCC.setFont(new Font(FONT, Font.BOLD, 13));
        lblTongNCC.setForeground(PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblTongNCC);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // Bảng dữ liệu & Toolbar
    // =====================================================================
    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc & nút thao tác ----
        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filterLeft.setOpaque(false);

        txtSearch = new SearchField("Tìm theo tên, mã, SĐT...");
        txtSearch.setPreferredSize(new Dimension(280, 34));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        filterLeft.add(txtSearch);

        RoundButton btnThem = new RoundButton("+ Thêm", PRIMARY, PRIMARY, WHITE);
        RoundButton btnSua = new RoundButton("Sửa", WHITE, BORDER, TEXT_DARK);
        RoundButton btnXoa = new RoundButton("Xóa", WHITE, new Color(0xFCA5A5), RED);

        btnThem.addActionListener(e -> themNhaCungCap());
        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp trong bảng để sửa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            suaNhaCungCap(modelRow);
        });
        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp trong bảng để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String maNCC = (String) model.getValueAt(modelRow, 0);
            xoaNhaCungCap(modelRow, maNCC);
        });

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        filterRight.setOpaque(false);
        filterRight.add(btnThem);
        filterRight.add(btnSua);
        filterRight.add(btnXoa);

        JPanel filter = new JPanel(new BorderLayout());
        filter.setOpaque(false);
        filter.setBorder(new EmptyBorder(4, 6, 4, 6));
        filter.add(filterLeft, BorderLayout.WEST);
        filter.add(filterRight, BorderLayout.EAST);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.add(filter, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);

        // ---- bảng ----
        String[] cols = {"MÃ NCC", "TÊN NHÀ CUNG CẤP", "ĐỊA CHỈ", "SỐ ĐIỆN THOẠI", "EMAIL"};
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

        int[] widths = {100, 240, 220, 140, 180};
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
    public void loadData() {
        model.setRowCount(0);
        try {
            ArrayList<NhaCungCap_entity> dsNCC = nccDAO.getAllNhaCungCap();
            for (NhaCungCap_entity ncc : dsNCC) {
                model.addRow(new Object[]{
                    ncc.getMaNCC(),
                    ncc.getTenNCC(),
                    ncc.getDiaChi(),
                    ncc.getSdt(),
                    ncc.getEmail()
                });
            }
            lblTongNCC.setText(dsNCC.size() + " nhà cung cấp");
        } catch (Exception e) {
            e.printStackTrace();
            loadSampleData();
        }
    }

    private void loadSampleData() {
        model.setRowCount(0);
        model.addRow(new Object[]{"NCC001", "Công ty Dược Thú Y VetGroup", "123 Nguyễn Văn Linh, Q.7, TP.HCM", "0901234567", "vetgroup@email.com"});
        model.addRow(new Object[]{"NCC002", "Công ty TNHH Thú Y Á Châu", "456 Lê Văn Việt, Q.9, TP.HCM", "0912345678", "achau@email.com"});
        model.addRow(new Object[]{"NCC003", "Pharma Pet Việt Nam", "789 Phạm Văn Đồng, Thủ Đức, TP.HCM", "0923456789", "pharmapet@email.com"});
        lblTongNCC.setText(model.getRowCount() + " nhà cung cấp");
    }

    // =====================================================================
    // CRUD
    // =====================================================================
    private void themNhaCungCap() {
        NhaCungCap_entity ncc = openEditor(null);
        if (ncc != null) {
            boolean ok = false;
            try {
                ok = nccDAO.createNhaCungCap(ncc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                model.addRow(new Object[]{ncc.getMaNCC(), ncc.getTenNCC(), ncc.getDiaChi(), ncc.getSdt(), ncc.getEmail()});
                lblTongNCC.setText(model.getRowCount() + " nhà cung cấp");
                JOptionPane.showMessageDialog(this, "Đã thêm nhà cung cấp vào bảng (chưa lưu CSDL).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void suaNhaCungCap(int modelRow) {
        String maNCC = (String) model.getValueAt(modelRow, 0);
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        String diaChi = (String) model.getValueAt(modelRow, 2);
        String sdt = (String) model.getValueAt(modelRow, 3);
        String email = (String) model.getValueAt(modelRow, 4);

        NhaCungCap_entity old = new NhaCungCap_entity(maNCC, tenNCC, diaChi, sdt, email);
        NhaCungCap_entity updated = openEditor(old);
        if (updated != null) {
            boolean ok = false;
            try {
                ok = nccDAO.updateNhaCungCap(updated);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhà cung cấp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                model.setValueAt(updated.getTenNCC(), modelRow, 1);
                model.setValueAt(updated.getDiaChi(), modelRow, 2);
                model.setValueAt(updated.getSdt(), modelRow, 3);
                model.setValueAt(updated.getEmail(), modelRow, 4);
                JOptionPane.showMessageDialog(this, "Đã cập nhật nhà cung cấp (chưa lưu CSDL).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void xoaNhaCungCap(int modelRow, String maNCC) {
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhà cung cấp \"" + tenNCC + "\" (Mã: " + maNCC + ")?\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean deleted = false;
        try {
            deleted = nccDAO.deleteNhaCungCap(maNCC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deleted) {
            JOptionPane.showMessageDialog(this, "Đã xóa nhà cung cấp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            model.removeRow(modelRow);
            lblTongNCC.setText(model.getRowCount() + " nhà cung cấp");
            JOptionPane.showMessageDialog(this, "Đã xóa nhà cung cấp khỏi bảng (chưa lưu CSDL).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =====================================================================
    // Hộp thoại thêm / sửa
    // =====================================================================
    private NhaCungCap_entity openEditor(NhaCungCap_entity src) {
        final NhaCungCap_entity[] result = {null};
        boolean isNew = (src == null);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Thêm nhà cung cấp mới" : "Sửa nhà cung cấp", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(isNew ? taoMaMoi() : src.getMaNCC());
        txtMa.setEditable(isNew);
        JTextField txtTen = new JTextField(isNew ? "" : src.getTenNCC());
        JTextField txtDiaChi = new JTextField(isNew ? "" : src.getDiaChi());
        JTextField txtSDT = new JTextField(isNew ? "" : src.getSdt());
        JTextField txtEmail = new JTextField(isNew ? "" : src.getEmail());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(16, 20, 10, 20));
        addFormRow(form, 0, "Mã NCC", txtMa);
        addFormRow(form, 1, "Tên nhà cung cấp (*)", txtTen);
        addFormRow(form, 2, "Địa chỉ", txtDiaChi);
        addFormRow(form, 3, "Số điện thoại", txtSDT);
        addFormRow(form, 4, "Email", txtEmail);

        RoundButton btnLuu = new RoundButton(isNew ? "Thêm mới" : "Lưu thay đổi", PRIMARY, PRIMARY, WHITE);
        RoundButton btnHuy = new RoundButton("Hủy", WHITE, BORDER, TEXT_DARK);

        btnHuy.addActionListener(e -> d.dispose());
        btnLuu.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();

            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Mã NCC không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Tên nhà cung cấp không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            result[0] = new NhaCungCap_entity(ma, ten, diaChi, sdt, email);
            d.dispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btns.setBackground(WHITE);
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

    private String taoMaMoi() {
        int max = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String ma = (String) model.getValueAt(i, 0);
            if (ma != null && ma.startsWith("NCC")) {
                try {
                    int num = Integer.parseInt(ma.substring(3));
                    if (num > max) max = num;
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

    // =====================================================================
    // Lọc
    // =====================================================================
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

    // =====================================================================
    // Component phụ
    // =====================================================================
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
}
