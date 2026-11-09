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

import controller.VatTu.NhaCungCapController;
import controller.VatTu.NhaCungCapController.OperationResult;
import controller.VatTu.NhaCungCapController.Supplier;
import controller.VatTu.NhaCungCapController.SupplierInput;

/**
 * Giao diện "Quản lý Nhà cung cấp" - SALALA Pet Shop.
 * JPanel độc lập với chức năng CRUD đầy đủ.
 */
public class NhaCungCapGUI extends JPanel {



    private static final String FONT = "Segoe UI";

    // ===== Thành phần =====
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JLabel lblTongNCC;
    private RoundButton btnSua, btnXoa;

    private final NhaCungCapController controller = new NhaCungCapController();

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
        t.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel s = new JLabel("Thêm, sửa, xóa thông tin nhà cung cấp vật tư");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        lblTongNCC = new JLabel("0 nhà cung cấp");
        lblTongNCC.setFont(new Font(FONT, Font.BOLD, 13));
        lblTongNCC.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblTongNCC);


        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // Bảng dữ liệu và Thanh công cụ (Tìm kiếm + Thêm + Sửa + Xóa)
    // =====================================================================
    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN);
        box.setLayout(new BorderLayout());

        // ---- Thanh lọc / tìm kiếm ở bên trái, các nút Thêm, Sửa, Xóa ở bên phải ----
        JPanel filter = new JPanel(new BorderLayout());
        filter.setOpaque(false);
        filter.setBorder(new EmptyBorder(10, 12, 10, 12));

        txtSearch = new SearchField("Tìm theo tên, mã, SĐT...");
        txtSearch.setPreferredSize(new Dimension(280, 32));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(this::applyFilter));
        
        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFilter.setOpaque(false);
        leftFilter.add(txtSearch);

        // Khởi tạo các nút Thêm, Sửa, Xóa đặt phía bên phải
        RoundButton btnThem = new RoundButton("+ Thêm nhà cung cấp", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_TRANG);
        btnSua = new RoundButton("Sửa", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnXoa = new RoundButton("Xóa", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        
        // Mặc định ban đầu chưa chọn dòng nào nên khóa nút Sửa & Xóa
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        btnThem.addActionListener(e -> themNhaCungCap());

        btnSua.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                suaNhaCungCap(table.convertRowIndexToModel(selectedRow));
            }
        });

        btnXoa.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                String maNCC = (String) model.getValueAt(modelRow, 0);
                xoaNhaCungCap(modelRow, maNCC);
            }
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnThem);
        rightActions.add(btnSua);
        rightActions.add(btnXoa);

        filter.add(leftFilter, BorderLayout.WEST);
        filter.add(rightActions, BorderLayout.EAST);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.add(filter, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(), BorderLayout.SOUTH);


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
        table.setGridColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(RangBuocGiaoDien.MAU_DUONG_VIEN);
        table.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt nút Sửa & Xóa
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelected = table.getSelectedRow() >= 0;
                btnSua.setEnabled(hasSelected);
                btnXoa.setEnabled(hasSelected);
            }
        });

        // Double click để sửa nhanh
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    suaNhaCungCap(modelRow);
                }
            }
        });

        // Header bảng
        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new HeaderRenderer());


        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }


        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
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
        ArrayList<Supplier> suppliers = new ArrayList<>(controller.taiDanhSach());
        for (Supplier supplier : suppliers) {

            });
        }
        lblTongNCC.setText(suppliers.size() + " nhà cung cấp");
        if (btnSua != null) btnSua.setEnabled(false);
        if (btnXoa != null) btnXoa.setEnabled(false);
    }

    // =====================================================================
    // CRUD
    // =====================================================================
    private void themNhaCungCap() {
        SupplierInput ncc = openEditor(null);
        if (ncc != null) {
            OperationResult result = controller.tao(ncc);
            if (result.isSuccessful()) {

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

                loadData();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaNhaCungCap(int modelRow, String maNCC) {
        String tenNCC = (String) model.getValueAt(modelRow, 1);
        int ok = JOptionPane.showConfirmDialog(this,

            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Hộp thoại thêm / sửa
    // =====================================================================
    private SupplierInput openEditor(SupplierInput src) {
        final SupplierInput[] result = { null };
        boolean isNew = (src == null);

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Thêm nhà cung cấp mới" : "Sửa nhà cung cấp", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(isNew ? controller.taoMaMoi(null) : src.getMa());
        txtMa.setEditable(isNew);
        JTextField txtTen = new JTextField(isNew ? "" : src.getTen());
        JTextField txtDiaChi = new JTextField(isNew ? "" : src.getDiaChi());
        JTextField txtSDT = new JTextField(isNew ? "" : src.getSdt());
        JTextField txtEmail = new JTextField(isNew ? "" : src.getEmail());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 10, 20));
        addFormRow(form, 0, "Mã NCC", txtMa);
        addFormRow(form, 1, "Tên nhà cung cấp (*)", txtTen);
        addFormRow(form, 2, "Địa chỉ", txtDiaChi);
        addFormRow(form, 3, "Số điện thoại", txtSDT);
        addFormRow(form, 4, "Email", txtEmail);

        // Đã chỉnh sửa màu nền nút sang RangBuocGiaoDien.MAU_CHU_DAO và chữ màu trắng rõ ràng
        JButton btnLuu = new JButton(isNew ? "Thêm mới" : "Lưu thay đổi");
        btnLuu.setFont(new Font(FONT, Font.BOLD, 13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(RangBuocGiaoDien.MAU_NEN_TRANG);
        btnLuu.setOpaque(true);
        btnLuu.setBorderPainted(false);
        btnLuu.setFocusPainted(false);
        btnLuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnHuy = new JButton("Hủy");
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
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(new Font(FONT, Font.BOLD, 10));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
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
    // Component phụ
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

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean en = isEnabled();
            g2.setColor(en ? fill : RangBuocGiaoDien.MAU_NEN_TRANG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(en ? line : RangBuocGiaoDien.MAU_DUONG_VIEN);
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
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
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
}