package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel danh sach hoa don - co the dat truc tiep vao CENTER cua BorderLayout.
 * Khong bao gom header/sidebar cua ung dung.
 *
 * Cach dung:
 *   JPanel center = new HoaDonPanel();
 *   mainPanel.add(center, BorderLayout.CENTER);
 */
public class DSHoaDon_gui extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color BG = new Color(247, 249, 252);
    private static final Color BORDER = new Color(225, 232, 240);
    private static final Color TEXT = new Color(38, 49, 66);
    private static final Color MUTED = new Color(115, 129, 148);
    private static final Color TEAL = new Color(15, 128, 125);
    private static final Color TEAL_LIGHT = new Color(232, 248, 246);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);

<<<<<<< Updated upstream
    private final JTextField searchField = new JTextField();
    private final JTextField fromDate = new JTextField("01/10/2026");
    private final JTextField toDate = new JTextField("05/10/2026");
    private final JComboBox<String> statusFilter =
            new JComboBox<>(new String[]{"Tất cả trạng thái", "Đã thanh toán", "Chưa thanh toán", "Thanh toán một phần"});
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel pageInfo = new JLabel("Hiển thị 1–8 trong 128 hóa đơn");
    private final JLabel pageNumber = new JLabel("1");
    private int currentPage = 1;
    private final int pageSize = 8;
=======
    private final Color XANH = Color.decode("#0F766E");
    private final Color NEN = new Color(248, 250, 252);
    private final Color VIEN = new Color(226, 232, 240);
    private final Color CHU = new Color(30, 41, 59);

    private final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField txtTimKiem = new JTextField();

    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {
            "Tất cả",
            "Đã thanh toán",
            "Chưa thanh toán"
    });

    private final JLabel lblTong = new JLabel("0");
    private final JLabel lblDaThanhToan = new JLabel("0");
    private final JLabel lblChuaThanhToan = new JLabel("0");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {
                    "Mã Hóa Đơn",
                    "Ngày Lập",
                    "Trạng Thái",
                    "Mã Khách Hàng",
                    "Mã Nhân Viên",
                    "Mã Khuyến Mãi",
                    "Mã Thuế",
                    "Mã Thanh Toán",
                    "Thao tác"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private ArrayList<HoaDon_entity> danhSach = new ArrayList<>();

    // Nhận hành động chuyển sang màn hình lập hóa đơn nếu cần.
    private final Runnable hanhDongLapHoaDon;
>>>>>>> Stashed changes

    public DSHoaDon_gui() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setOpaque(true);
        setBorder(new EmptyBorder(14, 18, 12, 18));

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

        content.add(buildTitle(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildStats(), BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout(0, 0));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new LineBorder(BORDER, 1, true));
        listPanel.add(buildToolbar(), BorderLayout.NORTH);

        String[] columns = {"Mã hóa đơn", "Ngày lập", "Khách hàng", "Nhân viên",
                "Tổng tiền", "Khuyến mãi", "Thành tiền", "Phương thức thanh toán", "Trạng thái", "Thao tác"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        addSampleRows();
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        styleTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(buildFooter(), BorderLayout.SOUTH);

        center.add(listPanel, BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterRows(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterRows(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterRows(); }
        });
        statusFilter.addActionListener(e -> filterRows());
    }

    private JPanel buildTitle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel breadcrumb = label("Hóa đơn / Danh sách hóa đơn", 11, MUTED, false);
        JLabel title = label("Danh sách hóa đơn", 21, TEXT, true);
        JLabel sub = label("Theo dõi hóa đơn dịch vụ, khuyến mãi và tình trạng thanh toán của khách hàng.", 11, MUTED, false);
        left.add(breadcrumb);
        left.add(Box.createVerticalStrut(3));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);
        p.add(left, BorderLayout.CENTER);

        JButton create = button("+  Lập hóa đơn", TEAL, Color.WHITE);
        create.setPreferredSize(new Dimension(112, 34));
        create.addActionListener(e -> {
            GiaoDienChinhGUI gd = (GiaoDienChinhGUI) SwingUtilities.getAncestorOfClass(GiaoDienChinhGUI.class, this);
            if (gd != null) {
                gd.hienThiTaoHoaDonMoi();
            } else {
<<<<<<< Updated upstream
                JOptionPane.showMessageDialog(this,
                        "Bạn có thể mở form lập hóa đơn tại đây.", "Lập hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        p.add(create, BorderLayout.EAST);
        p.setBorder(new EmptyBorder(0, 0, 2, 0));
        return p;
    }

    private JPanel buildStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 0));
        stats.setOpaque(false);
        stats.add(statCard("▤", "Tổng hóa đơn", "128", "Trong tháng 10"));
        stats.add(statCard("✓", "Đã thanh toán", "112", "87,5% hóa đơn"));
        stats.add(statCard("◷", "Chờ thanh toán", "16", "Gồm thanh toán một phần"));
        stats.add(statCard("▣", "Doanh thu đã thu", "86,4 tr", "Trong tháng 10"));
        return stats;
=======
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy kết nối nút này với màn hình lập hóa đơn.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        tieuDe.add(thongTinTieuDe, BorderLayout.CENTER);
        tieuDe.add(btnLapHoaDon, BorderLayout.EAST);

        // Ba thẻ thống kê
        JPanel thongKe = new JPanel(new GridLayout(1, 3, 12, 0));
        thongKe.setOpaque(false);

        thongKe.add(taoTheThongKe(
                "Tổng số hóa đơn", lblTong));

        thongKe.add(taoTheThongKe(
                "Đã thanh toán", lblDaThanhToan));

        thongKe.add(taoTheThongKe(
                "Chưa thanh toán", lblChuaThanhToan));

        // Khu vực bảng
        JPanel khuVucBang = new JPanel(new BorderLayout(0, 12));
        khuVucBang.setBackground(Color.WHITE);
        khuVucBang.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN, 1, true),
                new EmptyBorder(12, 10, 8, 10)));

        JPanel boLoc = taoBoLoc();

        caiDatBang();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(VIEN, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        khuVucBang.add(boLoc, BorderLayout.NORTH);
        khuVucBang.add(scroll, BorderLayout.CENTER);

        JPanel phiaTren = new JPanel(new BorderLayout(0, 14));
        phiaTren.setOpaque(false);
        phiaTren.add(tieuDe, BorderLayout.NORTH);
        phiaTren.add(thongKe, BorderLayout.CENTER);

        main.add(phiaTren, BorderLayout.NORTH);
        main.add(khuVucBang, BorderLayout.CENTER);

        return main;
    }

    // ==================== THẺ THỐNG KÊ ====================

    private JPanel taoTheThongKe(String tieuDe, JLabel giaTri) {

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(new Color(240, 253, 250));

        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(204, 251, 241), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel chu = new JPanel();
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.setOpaque(false);

        JLabel lblTen = new JLabel(tieuDe);
        lblTen.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 116, 139));

        giaTri.setFont(new Font("Arial", Font.BOLD, 21));
        giaTri.setForeground(CHU);

        chu.add(lblTen);
        chu.add(Box.createVerticalStrut(4));
        chu.add(giaTri);

        the.add(chu, BorderLayout.CENTER);

        return the;
>>>>>>> Stashed changes
    }

    private JPanel statCard(String iconText, String caption, String value, String note) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 11, 10, 10)));
        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 15));
        icon.setForeground(TEAL);
        icon.setOpaque(true);
        icon.setBackground(TEAL_LIGHT);
        icon.setPreferredSize(new Dimension(32, 32));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label(caption, 10, MUTED, false));
        text.add(Box.createVerticalStrut(2));
        text.add(label(value, 18, TEXT, true));
        text.add(Box.createVerticalStrut(2));
        text.add(label(note, 9, MUTED, false));
        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(9, 10, 9, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        searchField.setFont(FONT);
        searchField.setForeground(TEXT);
        searchField.setToolTipText("Tìm mã hóa đơn, tên khách hàng...");
        searchField.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 8, 7, 8)));
        c.gridx = 0; c.weightx = 1.4; c.ipady = 1;
        toolbar.add(searchField, c);

        JPanel dates = new JPanel(new GridLayout(1, 3, 5, 0));
        dates.setOpaque(false);
        styleTextField(fromDate);
        styleTextField(toDate);
        dates.add(fromDate);
        JLabel dash = label("–", 12, MUTED, false);
        dash.setHorizontalAlignment(SwingConstants.CENTER);
        dates.add(dash);
        dates.add(toDate);
        c.gridx = 1; c.weightx = 1.0;
        toolbar.add(dates, c);

        statusFilter.setFont(FONT);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setBorder(new LineBorder(BORDER, 1, true));
        c.gridx = 2; c.weightx = 0.9;
        toolbar.add(statusFilter, c);

        JButton export = button("⇩  Xuất danh sách", Color.WHITE, TEAL);
        export.setBorder(new LineBorder(BORDER, 1, true));
        export.addActionListener(e -> exportRows());
        c.gridx = 3; c.weightx = 0; c.insets = new Insets(0, 0, 0, 0);
        toolbar.add(export, c);
        return toolbar;
    }

    private void styleTextField(JTextField field) {
        field.setFont(FONT);
        field.setForeground(TEXT);
        field.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 7, 7, 7)));
    }

    private void styleTable() {
        table.setFont(FONT);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(237, 241, 246));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(235, 246, 246));
        table.setSelectionForeground(TEXT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(new Color(100, 115, 134));
        header.setBackground(new Color(250, 252, 254));
        header.setPreferredSize(new Dimension(10, 31));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);

<<<<<<< Updated upstream
        int[] widths = {76, 72, 112, 92, 80, 70, 80, 112, 108, 112};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
=======
        // Nút Xem
        table.getColumnModel().getColumn(8).setCellRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean selected,
                            boolean focus, int row, int column) {

                        JButton btnXem = new JButton("Xem");
                        btnXem.setFont(new Font("Arial", Font.PLAIN, 11));
                        btnXem.setForeground(XANH);
                        btnXem.setBackground(new Color(240, 253, 250));
                        btnXem.setBorder(new LineBorder(
                                new Color(204, 251, 241), 1, true));
                        btnXem.setFocusPainted(false);
                        return btnXem;
                    }
                });

        // Màu trạng thái
        table.getColumnModel().getColumn(2).setCellRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean selected,
                            boolean focus, int row, int column) {

                        JLabel label = new JLabel(
                                value == null ? "" : value.toString());

                        label.setOpaque(true);
                        label.setFont(new Font("Arial", Font.PLAIN, 11));
                        label.setHorizontalAlignment(SwingConstants.CENTER);

                        String tt = value == null
                                ? ""
                                : value.toString();

                        if (laDaThanhToan(tt)) {
                            label.setForeground(new Color(21, 128, 61));
                            label.setBackground(new Color(220, 252, 231));
                        } else {
                            label.setForeground(new Color(180, 83, 9));
                            label.setBackground(new Color(254, 243, 199));
                        }

                        return label;
                    }
                });
>>>>>>> Stashed changes
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(8, 10, 8, 10)));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(pageInfo);
        pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        pageInfo.setForeground(MUTED);
        JLabel hint = label("Thao tác: Xem chi tiết · Chỉnh sửa · In/xuất hóa đơn", 9, MUTED, false);
        left.add(Box.createVerticalStrut(3));
        left.add(hint);
        footer.add(left, BorderLayout.WEST);

        JPanel pages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pages.setOpaque(false);
        pages.add(label("Số dòng / trang", 10, MUTED, false));
        JComboBox<Integer> size = new JComboBox<>(new Integer[]{8, 10, 20, 50});
        size.setSelectedItem(8);
        size.setFont(FONT);
        size.addActionListener(e -> {
            // Demo giao dien: co the gan logic phan trang that tai day.
            updatePageInfo();
        });
        pages.add(size);
        JButton prev = pageButton("‹");
        prev.addActionListener(e -> { if (currentPage > 1) currentPage--; pageNumber.setText(String.valueOf(currentPage)); updatePageInfo(); });
        pages.add(prev);
        JButton current = pageButton("1");
        current.setBackground(TEAL);
        current.setForeground(Color.WHITE);
        current.addActionListener(e -> { currentPage = 1; pageNumber.setText("1"); updatePageInfo(); });
        pages.add(current);
        JButton two = pageButton("2");
        two.addActionListener(e -> { currentPage = 2; pageNumber.setText("2"); updatePageInfo(); });
        pages.add(two);
        JButton three = pageButton("3");
        three.addActionListener(e -> { currentPage = 3; pageNumber.setText("3"); updatePageInfo(); });
        pages.add(three);
        pages.add(label("…", 12, MUTED, false));
        JButton next = pageButton("›");
        next.addActionListener(e -> { currentPage++; pageNumber.setText(String.valueOf(currentPage)); updatePageInfo(); });
        pages.add(next);
        footer.add(pages, BorderLayout.EAST);
        return footer;
    }

    private JButton pageButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(27, 25));
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setBorder(new LineBorder(BORDER, 1, true));
        return b;
    }

    private void addSampleRows() {
        Object[][] rows = {
            {"HD0010", "05/10/2026", "Nguyễn Hoàng Nam", "Lê Minh", "1.070.000 ₫", "50.000 ₫", "1.020.000 ₫", "Chuyển khoản", "Đã thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0009", "05/10/2026", "Trần Thị Mai", "Đặng Thanh Trúc", "450.000 ₫", "22.500 ₫", "427.500 ₫", "Tiền mặt", "Đã thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0008", "04/10/2026", "Phạm Quốc Hưng", "Lê Minh", "800.000 ₫", "120.000 ₫", "680.000 ₫", "Chuyển khoản", "Chưa thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0007", "04/10/2026", "Lê Minh Anh", "Phạm Ngọc Mai", "600.000 ₫", "60.000 ₫", "540.000 ₫", "Tiền mặt", "Đã thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0006", "03/10/2026", "Võ Thanh Hà", "Trần Quốc Huy", "350.000 ₫", "7.000 ₫", "343.000 ₫", "Chuyển khoản", "Thanh toán một phần", "Xem   Sửa   In/xuất"},
            {"HD0005", "03/10/2026", "Đặng Minh Tú", "Đặng Thanh Trúc", "920.000 ₫", "46.000 ₫", "874.000 ₫", "Thẻ ngân hàng", "Đã thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0004", "02/10/2026", "Bùi Ngọc Lan", "Phạm Ngọc Mai", "280.000 ₫", "5.600 ₫", "274.400 ₫", "Tiền mặt", "Chưa thanh toán", "Xem   Sửa   In/xuất"},
            {"HD0003", "02/10/2026", "Hoàng Gia Bảo", "Lê Minh", "750.000 ₫", "75.000 ₫", "675.000 ₫", "Chuyển khoản", "Đã thanh toán", "Xem   Sửa   In/xuất"}
        };
        for (Object[] row : rows) model.addRow(row);
    }

<<<<<<< Updated upstream
    private void filterRows() {
        String q = searchField.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!q.isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                public boolean include(Entry<?, ?> entry) {
                    for (int i : new int[]{0, 2}) {
                        if (entry.getStringValue(i).toLowerCase().contains(q)) return true;
                    }
                    return false;
                }
=======
    private boolean laDaThanhToan(String trangThai) {

        if (trangThai == null) {
            return false;
        }

        return trangThai.equalsIgnoreCase("Đã thanh toán")
                || trangThai.equalsIgnoreCase("Paid");
    }

    private void hienThiBang(ArrayList<HoaDon_entity> ds) {

        model.setRowCount(0);

        for (HoaDon_entity hd : ds) {

            model.addRow(new Object[] {
                    hd.getMaHD(),

                    hd.getNgayLap() == null
                            ? ""
                            : hd.getNgayLap().format(DMY),

                    hd.getTrangThai(),

                    layMa(hd.getMaKH()),
                    layMa(hd.getMaNhanVien()),
                    layMa(hd.getMaKM()),
                    layMa(hd.getMaThue()),
                    layMa(hd.getMaThanhToan()),

                    "Xem"
>>>>>>> Stashed changes
            });
        }
        if (status != null && !"Tất cả trạng thái".equals(status)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 8));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        pageInfo.setText("Hiển thị " + table.getRowCount() + " hóa đơn phù hợp");
    }

<<<<<<< Updated upstream
    private void updatePageInfo() {
        pageInfo.setText("Hiển thị trang " + currentPage + " · " + table.getRowCount() + " hóa đơn phù hợp");
    }

    private void exportRows() {
        StringBuilder csv = new StringBuilder();
        for (int c = 0; c < model.getColumnCount(); c++) {
            if (c > 0) csv.append(',');
            csv.append('"').append(model.getColumnName(c).replace("\"", "\"\"")).append('"');
        }
        csv.append('\n');
        for (int r = 0; r < table.getRowCount(); r++) {
            int modelRow = table.convertRowIndexToModel(r);
=======
    // ==================== LẤY MÃ KHÓA NGOẠI ====================

    private String layMa(Object doiTuong) {

        if (doiTuong == null) {
            return "NULL";
        }

        String[] cacGetter = {
                "getMaKH",
                "getMaNhanVien",
                "getMaKM",
                "getMaThue",
                "getMaThanhToan",
                "getMaNCC",
                "getMaDV",
                "getMaVT",
                "getMa"
        };

        for (String tenGetter : cacGetter) {

            try {
                Method method = doiTuong.getClass().getMethod(tenGetter);

                Object ketQua = method.invoke(doiTuong);

                if (ketQua != null) {
                    return ketQua.toString();
                }

            } catch (ReflectiveOperationException ignored) {
                // Thử getter tiếp theo
            }
        }

        return doiTuong.toString();
    }

    // ==================== TÌM KIẾM VÀ LỌC TRẠNG THÁI ====================

    private void locDuLieu() {

        String tuKhoa = txtTimKiem.getText()
                .trim().toLowerCase();

        String trangThaiChon = cboTrangThai.getSelectedItem().toString();

        ArrayList<HoaDon_entity> ketQua = new ArrayList<>();

        for (HoaDon_entity hd : danhSach) {

            // Tìm theo mã hóa đơn
            String maHD = hd.getMaHD() == null
                    ? ""
                    : hd.getMaHD().toLowerCase();

            if (!maHD.contains(tuKhoa)) {
                continue;
            }

            // Lọc trạng thái
            boolean daTT = laDaThanhToan(hd.getTrangThai());

            if (trangThaiChon.equals("Đã thanh toán") && !daTT) {
                continue;
            }

            if (trangThaiChon.equals("Chưa thanh toán") && daTT) {
                continue;
            }

            ketQua.add(hd);
        }

        hienThiBang(ketQua);
    }

    // ==================== XEM CHI TIẾT HÓA ĐƠN ====================

    private void hienThiChiTiet(String maHD) {

        // Mở màn hình chi tiết của đúng hóa đơn ở dòng được chọn
        ChiTietHoaDon_gui.hienThi(this, maHD);
    }

    // ==================== XÓA HÓA ĐƠN ====================

    private void xoaHoaDon() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một hóa đơn cần xóa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Đổi chỉ số dòng hiển thị sang chỉ số model (do có sort/lọc)
        int modelRow = table.convertRowIndexToModel(viewRow);
        String maHD = model.getValueAt(modelRow, 0).toString();

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa hóa đơn " + maHD + " không?\n"
                        + "Các chi tiết của hóa đơn cũng sẽ bị xóa.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        if (hoaDonDAO.xoaHD(maHD)) {

            // Tải lại dữ liệu từ CSDL, cập nhật thống kê
            taiDuLieu();

            // Giữ nguyên bộ lọc đang áp dụng
            locDuLieu();

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa hóa đơn " + maHD + ".",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa hóa đơn " + maHD + ".",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== XUẤT DANH SÁCH CSV ====================

    private void xuatCSV() {

        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
                new File("DanhSachHoaDon.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {

            // BOM giúp Excel đọc tiếng Việt UTF-8
            writer.write('\uFEFF');

            // Tiêu đề cột
>>>>>>> Stashed changes
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) csv.append(',');
                csv.append('"').append(String.valueOf(model.getValueAt(modelRow, c)).replace("\"", "\"\"")).append('"');
            }
<<<<<<< Updated upstream
            csv.append('\n');
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("danh_sach_hoa_don.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.Writer writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(chooser.getSelectedFile()), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write('\uFEFF');
                writer.write(csv.toString());
                JOptionPane.showMessageDialog(this, "Đã xuất danh sách CSV.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
=======

            writer.println();

            // Chỉ xuất các dòng đang hiển thị
            for (int r = 0; r < table.getRowCount(); r++) {

                for (int c = 0; c < table.getColumnCount(); c++) {

                    if (c > 0) {
                        writer.print(",");
                    }

                    Object value = table.getValueAt(r, c);

                    String text = value == null
                            ? ""
                            : value.toString();

                    writer.print("\""
                            + text.replace("\"", "\"\"")
                            + "\"");
                }

                writer.println();
>>>>>>> Stashed changes
            }
        }
    }

<<<<<<< Updated upstream
    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }
=======
            JOptionPane.showMessageDialog(
                    this,
                    "Xuất danh sách thành công.");
>>>>>>> Stashed changes

    private JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

<<<<<<< Updated upstream
    private class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 7));
            p.setBackground(selected ? t.getSelectionBackground() : Color.WHITE);
            String s = String.valueOf(value);
            JLabel pill = new JLabel("● " + s);
            pill.setFont(new Font("Segoe UI", Font.BOLD, 9));
            pill.setOpaque(true);
            pill.setBorder(new EmptyBorder(3, 7, 3, 7));
            if ("Đã thanh toán".equals(s)) {
                pill.setForeground(new Color(20, 130, 77)); pill.setBackground(new Color(222, 248, 231));
            } else if ("Chưa thanh toán".equals(s)) {
                pill.setForeground(new Color(195, 65, 65)); pill.setBackground(new Color(255, 229, 229));
            } else {
                pill.setForeground(new Color(166, 117, 0)); pill.setBackground(new Color(255, 243, 194));
            }
            p.add(pill);
            return p;
        }
    }

    private class ActionRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            JLabel l = new JLabel("⊙ Xem   ▧ Sửa   ▣ In/xuất");
            l.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            l.setForeground(TEAL);
            l.setBorder(new EmptyBorder(0, 3, 0, 0));
            l.setBackground(selected ? t.getSelectionBackground() : Color.WHITE);
            l.setOpaque(true);
            return l;
=======
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xuất file: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
>>>>>>> Stashed changes
        }
    }
}
