package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import Service.VatTu.NhaCungCapService;
import Service.VatTu.NhaCungCapService.OperationResult;
import Service.VatTu.NhaCungCapService.Supplier;
import Service.VatTu.NhaCungCapService.SupplierInput;

/**
 * Giao dien "Quan ly Nha cung cap" - SALALA Pet Shop.
 * JPanel doc lap voi chuc nang CRUD day du.
 */
public class NhaCungCapGUI extends JPanel {


    private static final String FONT = "Segoe UI";

    // ===== Thanh phan =====
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JLabel lblTongNCC;
    private RoundButton btnSua, btnXoa;

    private final NhaCungCapService service = new NhaCungCapService();

    public NhaCungCapGUI() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
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
        t.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel s = new JLabel("Them, sua, xoa thong tin nha cung cap vat tu");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        lblTongNCC = new JLabel("0 nha cung cap");
        lblTongNCC.setFont(new Font(FONT, Font.BOLD, 13));
        lblTongNCC.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblTongNCC);
        RoundButton btnThem = new RoundButton("Thêm nhà cung cấp", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
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
        RoundedPanel box = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        box.setLayout(new BorderLayout());

        // ---- thanh loc ----
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filter.setOpaque(false);

        txtSearch = new SearchField("Tim theo ten, ma, SDT...");
        txtSearch.setPreferredSize(new Dimension(280, 30));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        filter.add(txtSearch);

        btnSua = new RoundButton("Sửa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnSua.setEnabled(false);
        btnSua.addActionListener(e -> suaDongDangChon());
        filter.add(btnSua);

        btnXoa = new RoundButton("Xóa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_NGUY_HIEM_CHAM, RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> xoaDongDangChon());
        filter.add(btnXoa);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 6, 0, 6));
        filterWrap.add(filter, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);

        // ---- bang ----
        String[] cols = { "MA NCC", "TEN NHA CUNG CAP", "DIA CHI", "SO DIEN THOAI", "EMAIL" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font(FONT, Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        table.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
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
        table.getColumnModel().getColumn(0).setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_PHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(3)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_PHU, Font.PLAIN, SwingConstants.LEFT));

        int[] widths = { 100, 240, 220, 140, 200 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Chon dong -> bat nut Sua / Xoa canh o tim kiem
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatNutThaoTac();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
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
        ArrayList<Supplier> suppliers = new ArrayList<>(service.taiDanhSach());
        for (Supplier supplier : suppliers) {
            model.addRow(new Object[] {
                    supplier.getCode(), supplier.getName(), supplier.getAddress(),
                    supplier.getPhone(), supplier.getEmail()
            });
        }
        lblTongNCC.setText(suppliers.size() + " nha cung cap");
        capNhatNutThaoTac();
    }

    // =====================================================================
    // CRUD
    // =====================================================================
    private void themNhaCungCap() {
        SupplierInput ncc = openEditor(null);
        if (ncc != null) {
            OperationResult result = service.tao(ncc);
            if (result.isSuccessful()) {
                JOptionPane.showMessageDialog(this, "Them nha cung cap thanh cong!", "Thanh cong",
                        JOptionPane.INFORMATION_MESSAGE);
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
            OperationResult result = service.capNhat(updated);
            if (result.isSuccessful()) {
                JOptionPane.showMessageDialog(this, "Cap nhat nha cung cap thanh cong!", "Thanh cong",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaNhaCungCap(int modelRow, String maNCC) {
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon xoa nha cung cap \"" + tenNCC + "\" (Ma: " + maNCC
                        + ")?\nThao tac nay khong the hoan tac.",
                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION)
            return;

        OperationResult result = service.xoa(maNCC);
        if (result.isSuccessful()) {
            JOptionPane.showMessageDialog(this, "Da xoa nha cung cap thanh cong!", "Thanh cong",
                    JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Sua / Xoa dong dang chon (nut canh o tim kiem)
    // =====================================================================
    private void capNhatNutThaoTac() {
        boolean coChon = table.getSelectedRow() >= 0;
        btnSua.setEnabled(coChon);
        btnXoa.setEnabled(coChon);
    }

    private void suaDongDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần sửa.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        suaNhaCungCap(table.convertRowIndexToModel(viewRow));
    }

    private void xoaDongDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        xoaNhaCungCap(modelRow, (String) model.getValueAt(modelRow, 0));
    }

    // =====================================================================
    // Hop thoai them / sua
    // =====================================================================
    private SupplierInput openEditor(SupplierInput src) {
        final SupplierInput[] result = { null };
        boolean isNew = (src == null);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Them nha cung cap moi" : "Sua nha cung cap", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(isNew ? service.taoMaMoi(suppliersFromModel()) : src.getMa());
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
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(RangBuocGiaoDien.MAU_NEN_THE);
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

            String validationError = NhaCungCapService.validate(
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
                return service.phuHopTimKiem(supplier, keyword);
            }
        });
    }

    // =====================================================================
    // Renderers
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(new Font(FONT, Font.BOLD, 10));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
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
    // Component phu
    // =====================================================================
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;

        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc;
            this.fill = RangBuocGiaoDien.theMo(fill);
            this.line = line;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
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
            this.fill = fill;
            this.line = line;
            this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 12));
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.SrcOver.derive(0.45f));
            }
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
                    BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 28, 0, 8)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(10, cy - 6, 8, 8));
            g2.drawLine(17, cy + 1, 21, cy + 5);
            if (getText().isEmpty()) {
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 28, cy + (fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;

        SimpleDocListener(Runnable r) {
            this.r = r;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }
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