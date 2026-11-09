package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;
import controller.VatTu.PhieuXuatKhoController;
import controller.VatTu.PhieuXuatKhoController.Dong;
import controller.VatTu.PhieuXuatKhoController.CatalogItem;
import controller.VatTu.PhieuXuatKhoController.Filter;
import controller.VatTu.PhieuXuatKhoController.Phieu;
import controller.VatTu.PhieuXuatKhoController.TrangThai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Giao diện "Quản lý xuất kho" - SALALA Pet Shop.
 * Dữ liệu lấy trực tiếp từ CSDL SALALA: GUI -> PhieuXuatKhoController ->
 * PhieuXuatKho_DAO -> ConnectDB.
 * Chức năng: danh sách phiếu xuất (tìm kiếm, lọc ngày, lọc trạng thái),
 * lập phiếu xuất, xem / sửa phiếu, xác nhận & trừ tồn kho, hủy phiếu,
 * xuất / in phiếu, xuất danh sách ra CSV.
 * Quy tắc: chỉ phiếu "Đã xuất" mới trừ tồn kho; không được xuất quá số lượng
 * tồn hiện tại.
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class PhieuXuatKhoGUI extends JPanel {

    // ===== Màu sắc =====


    private static final String FONT = "Segoe UI";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    /**
     * Gợi ý lý do xuất (ô lý do cho phép gõ tự do). Đây chỉ là gợi ý nhập liệu,
     * không phải dữ liệu phiếu.
     */
    private static final String[] LY_DO = {
            "Sử dụng cho điều trị", "Bán hàng cho khách", "Chăm sóc thú cưng", "Vật tư hư hỏng"
    };

    // ===== Nguồn dữ liệu: CSDL SALALA qua controller / DAO =====
    private final PhieuXuatKhoController controller = new PhieuXuatKhoController();

    // ===== Dữ liệu & trạng thái màn hình =====
    private java.util.List<Phieu> filtered = new ArrayList<>();
    private final java.util.List<Phieu> pageRows = new ArrayList<>();
    private LocalDate tuNgay, denNgay;
    /** Nhân viên đang đăng nhập (nếu có) - dùng làm mặc định khi lập phiếu. */
    private String maNhanVienHienTai = null;
    private String tenNhanVienHienTai = null;

    // ===== Thành phần =====
    private JLabel lblTongPhieu, lblDaXuat, lblNhap, lblGiaTri, lblShowing;
    private SearchField txtSearch;
    private RoundButton btnDate;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel model;

    public PhieuXuatKhoGUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setOpaque(true);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(createHeader(), BorderLayout.NORTH);
        top.add(createStatCards(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(createListCard(), BorderLayout.CENTER);

        taiLaiTuDB();
    }

    /**
     * Giữ lại để code cũ gọi 3 tham số vẫn biên dịch được.
     * Các tham số bị bỏ qua: danh mục vật tư, tồn kho và cập nhật tồn kho nay đều
     * đọc/ghi thẳng CSDL SALALA.
     */
    @Deprecated
    public PhieuXuatKhoGUI(java.util.function.Supplier<java.util.List<Object[]>> catalogSupplier,
            java.util.function.BiConsumer<String, Integer> stockUpdater,
            java.util.function.Function<String, Integer> tonKho) {
        this();
    }

    /**
     * Đặt nhân viên đang đăng nhập theo họ tên (dùng để chọn sẵn trong hộp thoại
     * lập phiếu).
     */
    public void setNhanVien(String ten) {
        this.tenNhanVienHienTai = ten;
    }

    /** Đặt nhân viên đang đăng nhập theo mã + họ tên. */
    public void setNhanVien(String maNhanVien, String ten) {
        this.maNhanVienHienTai = maNhanVien;
        this.tenNhanVienHienTai = ten;
    }

    // =====================================================================
    // Header
    // =====================================================================
    private JComponent createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel bc = new JLabel("Vật tư / Quản lý xuất kho");
        bc.setFont(new Font(FONT, Font.PLAIN, 11));
        bc.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel t = new JLabel("Quản lý xuất kho");
        t.setFont(new Font(FONT, Font.BOLD, 22));
        t.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel s = new JLabel("Theo dõi phiếu xuất vật tư, lý do sử dụng và giá trị xuất kho.");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        bc.setAlignmentX(LEFT_ALIGNMENT);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(bc);
        left.add(Box.createVerticalStrut(2));
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        RoundButton btnLap = new RoundButton("+  Lập phiếu xuất kho", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_TRANG);
        btnLap.addActionListener(e -> lapPhieu());
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(btnLap);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // 4 thẻ thống kê
    // =====================================================================
    private JComponent createStatCards() {
        lblTongPhieu = new JLabel();
        lblDaXuat = new JLabel();
        lblNhap = new JLabel();
        lblGiaTri = new JLabel();

        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(statCard("☰", "Tổng phiếu xuất", lblTongPhieu, "Tất cả phiếu"));
        row.add(statCard("▣", "Đã xuất kho", lblDaXuat, "Đã ghi nhận tồn kho"));
        row.add(statCard("✎", "Phiếu nháp", lblNhap, "Chưa trừ tồn kho"));
        row.add(statCard("₫", "Tổng giá trị đã xuất", lblGiaTri, "Theo giá nhập"));
        return row;
    }

    private JComponent statCard(String glyph, String title, JLabel value, String sub) {
        RoundedPanel card = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel ic = new JLabel(glyph, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        ic.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        RoundedPanel icBox = new RoundedPanel(10, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_NEN_TRANG);
        icBox.setLayout(new BorderLayout());
        icBox.setPreferredSize(new Dimension(38, 38));
        icBox.add(ic, BorderLayout.CENTER);
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icBox);

        JLabel t = new JLabel(title);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        value.setFont(new Font(FONT, Font.BOLD, 22));
        value.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel s = new JLabel(sub);
        s.setFont(new Font(FONT, Font.PLAIN, 11));
        s.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 8, 0);
        fl.setAlignOnBaseline(true);
        JPanel valRow = new JPanel(fl);
        valRow.setOpaque(false);
        valRow.add(value);
        valRow.add(s);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        t.setAlignmentX(LEFT_ALIGNMENT);
        valRow.setAlignmentX(LEFT_ALIGNMENT);
        body.add(t);
        body.add(valRow);

        // card.add(iconWrap, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void refreshStats() {
        PhieuXuatKhoController.Statistics stats = controller.statistics();
        lblTongPhieu.setText(String.valueOf(stats.getTotal()));
        lblDaXuat.setText(String.valueOf(stats.getExported()));
        lblNhap.setText(String.valueOf(stats.getDrafts()));
        lblGiaTri.setText(rutGon(stats.getValue()));
    }

    private static String rutGon(long v) {
        if (v >= 1_000_000) {
            String s = String.format(Locale.US, "%.2f", v / 1_000_000.0);
            s = s.replaceAll("\\.?0+$", "");
            return s.replace('.', ',') + " tr";
        }
        return MONEY.format(v) + " đ";
    }

    // =====================================================================
    // Khung danh sách: bộ lọc + bảng
    // =====================================================================
    private JComponent createListCard() {
        RoundedPanel box = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        txtSearch = new SearchField("Tìm theo mã phiếu xuất...");
        txtSearch.setPreferredSize(new Dimension(300, 34));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(() -> reload(true)));

        btnDate = new RoundButton("Ngày xuất: Tất cả", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnDate.setFont(new Font(FONT, Font.PLAIN, 12));
        btnDate.setPreferredSize(new Dimension(250, 34));
        btnDate.addActionListener(e -> showDateFilter());

        cboTrangThai = new JComboBox<>(new String[] {
                "Trạng thái: Tất cả", "Trạng thái: Nháp", "Trạng thái: Đã xuất", "Trạng thái: Đã hủy" });
        cboTrangThai.setFont(new Font(FONT, Font.PLAIN, 12));
        cboTrangThai.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        cboTrangThai.setPreferredSize(new Dimension(180, 34));
        cboTrangThai.addActionListener(e -> reload(true));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setOpaque(false);
        filterLeft.add(txtSearch);
        filterLeft.add(btnDate);
        filterLeft.add(cboTrangThai);



        btnTai.addActionListener(e -> taiLaiTuDB());

        btnXem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu xuất trong danh sách để xem.",
                        "Chưa chọn phiếu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < pageRows.size()) {
                xemPhieu(pageRows.get(modelRow));
            }
        });

        btnSua.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu xuất trong danh sách để sửa.",
                        "Chưa chọn phiếu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < pageRows.size()) {
                suaPhieu(pageRows.get(modelRow));
            }
        });
        btnExport.addActionListener(e -> xuatDanhSach());

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRight.setOpaque(false);
        filterRight.add(btnTai);
        filterRight.add(btnXem);
        filterRight.add(btnSua);
        filterRight.add(btnExport);

        JPanel filter = new JPanel(new BorderLayout());
        filter.setOpaque(false);
        filter.setBorder(new EmptyBorder(14, 16, 14, 16));
        filter.add(filterLeft, BorderLayout.WEST);
        filter.add(filterRight, BorderLayout.EAST);

        // ---- bảng ----
        String[] cols = { "Mã phiếu xuất", "Ngày xuất", "Nhân viên", "Lý do xuất",
                "Số lượng mặt hàng", "Tổng giá trị", "Trạng thái" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(42);
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

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(4, 5));


        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        int[] widths = { 90, 100, 140, 190, 120, 110, 110 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // ---- thông tin bên dưới bảng ----
        lblShowing = new JLabel();
        lblShowing.setFont(new Font(FONT, Font.PLAIN, 12));
        lblShowing.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel note = new JLabel("Giá trị tính theo giá nhập hiện tại · Chỉ phiếu Đã xuất được trừ tồn kho");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JPanel footLeft = new JPanel();
        footLeft.setOpaque(false);
        footLeft.setLayout(new BoxLayout(footLeft, BoxLayout.Y_AXIS));
        lblShowing.setAlignmentX(LEFT_ALIGNMENT);
        note.setAlignmentX(LEFT_ALIGNMENT);
        footLeft.add(lblShowing);
        footLeft.add(Box.createVerticalStrut(2));
        footLeft.add(note);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(10, 16, 10, 12)));
        foot.add(footLeft, BorderLayout.WEST);

        box.add(filter, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        box.add(foot, BorderLayout.SOUTH);
        return box;
    }

    // =====================================================================
    // Nạp dữ liệu, lọc, hiển thị bảng
    // =====================================================================
    private void applyFilter() {
        int ttIdx = cboTrangThai.getSelectedIndex(); // 0 = tất cả, 1 = Nháp, 2 = Đã xuất, 3 = Đã hủy
        TrangThai[] map = { null, TrangThai.NHAP, TrangThai.DA_XUAT, TrangThai.DA_HUY };
        filtered = controller.loc(new Filter(txtSearch.getText(), map[ttIdx], tuNgay, denNgay));
    }

    /**
     * Đọc lại toàn bộ phiếu từ CSDL SALALA rồi hiển thị. Báo lỗi ở chân bảng nếu
     * không đọc được.
     */
    private void taiLaiTuDB() {
        boolean ok = controller.reload();
        reload(true);
        if (!ok) {
            lblShowing.setText("Không đọc được dữ liệu từ CSDL SALALA: " + controller.getLastError());
            lblShowing.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        }
    }

    /**
     * Áp bộ lọc lên dữ liệu đã nạp (không truy vấn CSDL), cập nhật thống kê và
     * bảng.
     */
    private void reload(boolean resetPage) {
        if (model == null)
            return;
        applyFilter();
        model.setRowCount(0);
        pageRows.clear();
        for (int i = 0; i < filtered.size(); i++) {
            Phieu p = filtered.get(i);
            pageRows.add(p);
            model.addRow(new Object[] { p.so, p.ngay.format(DF), p.nhanVien, p.lyDo,
                    p.dong.size(), PhieuXuatKhoController.tongTien(p), p.tt.text });
        }
        lblShowing.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblShowing.setText(filtered.isEmpty()
                ? "Không có phiếu xuất kho nào"
                : "Hiển thị tất cả " + filtered.size() + " phiếu xuất kho");
        refreshStats();
    }

    private void showDateFilter() {
        JTextField from = new JTextField(tuNgay == null ? "" : tuNgay.format(DF), 10);
        JTextField to = new JTextField(denNgay == null ? "" : denNgay.format(DF), 10);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Từ ngày (dd/MM/yyyy)"));
        p.add(from);
        p.add(new JLabel("Đến ngày (dd/MM/yyyy)"));
        p.add(to);
        p.add(new JLabel(""));
        p.add(new JLabel("Để trống = không giới hạn"));
        int ok = JOptionPane.showConfirmDialog(this, p, "Lọc theo ngày xuất",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION)
            return;
        try {
            LocalDate f = PhieuXuatKhoController.parseDate(from.getText(), DF);
            LocalDate t = PhieuXuatKhoController.parseDate(to.getText(), DF);
            String rangeError = PhieuXuatKhoController.validateDateRange(f, t);
            if (rangeError != null) {
                JOptionPane.showMessageDialog(this, rangeError, "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            tuNgay = f;
            denNgay = t;
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (định dạng dd/MM/yyyy).", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text;
        if (tuNgay == null && denNgay == null)
            text = "Ngày xuất: Tất cả";
        else if (tuNgay != null && denNgay != null)
            text = "Ngày xuất: " + tuNgay.format(DF) + " – " + denNgay.format(DF);
        else if (tuNgay != null)
            text = "Ngày xuất: từ " + tuNgay.format(DF);
        else
            text = "Ngày xuất: đến " + denNgay.format(DF);
        btnDate.setText(text);
        reload(true);
    }

    // =====================================================================
    // Chức năng chính
    // =====================================================================
    private void lapPhieu() {
        Phieu p = openEditor(null);
        if (p == null)
            return;
        if (!controller.add(p)) {
            JOptionPane.showMessageDialog(this, "Không lưu được phiếu xuất:\n" + controller.getLastError(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }
        taiLaiTuDB();
    }

    private void xemPhieu(Phieu p) {
        openDetail(p);
    }

    private void suaPhieu(Phieu p) {
        if (p.tt != TrangThai.NHAP) {
            JOptionPane.showMessageDialog(this, "Chỉ sửa được phiếu đang ở trạng thái Nháp.", "Không thể sửa",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Phieu np = openEditor(p);
        if (np == null)
            return;
        if (!controller.update(p, np)) {
            JOptionPane.showMessageDialog(this, "Không lưu được thay đổi:\n" + controller.getLastError(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            taiLaiTuDB();
            return;
        }
        taiLaiTuDB();
    }

    /** Xác nhận xuất kho -> trừ tồn trong CSDL. Trả về true nếu thành công. */
    private boolean xacNhan(Phieu p, Component parent) {
        if (p.tt != TrangThai.NHAP)
            return false;
        if (p.dong.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Phiếu chưa có vật tư nào.", "Không thể xác nhận",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        java.util.List<String> loi = controller.kiemTraTon(p);
        if (!loi.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không được xuất quá số lượng tồn kho:\n" + String.join("\n", loi),
                    "Không đủ tồn kho", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        int ok = JOptionPane.showConfirmDialog(parent,
                "Xác nhận xuất kho phiếu " + p.so + " và trừ "
                        + PhieuXuatKhoController.tongSoLuong(p) + " đơn vị khỏi tồn kho?",
                "Xác nhận xuất kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION)
            return false;

        if (!controller.confirm(p)) {
            JOptionPane.showMessageDialog(parent, "Không xác nhận được phiếu xuất:\n" + controller.getLastError(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            taiLaiTuDB();
            return false;
        }
        taiLaiTuDB();
        JOptionPane.showMessageDialog(parent, "Đã xuất kho và cập nhật tồn kho.", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    private boolean huy(Phieu p, Component parent) {
        if (p.tt != TrangThai.NHAP)
            return false;
        int ok = JOptionPane.showConfirmDialog(parent,
                "Hủy phiếu xuất " + p.so + "? Thao tác này không thể hoàn tác.",
                "Hủy phiếu xuất", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION)
            return false;
        if (!controller.cancel(p)) {
            JOptionPane.showMessageDialog(parent, "Không hủy được phiếu xuất:\n" + controller.getLastError(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            taiLaiTuDB();
            return false;
        }
        taiLaiTuDB();
        return true;
    }

    // =====================================================================
    // Xuất danh sách (CSV) & in phiếu
    // =====================================================================
    private void xuatDanhSach() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh-sach-phieu-xuat-kho.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        String csv = PhieuXuatKhoController.toCsv(filtered);
        try {
            Files.write(fc.getSelectedFile().toPath(), csv.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " phiếu ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatIn(Phieu p, Window owner) {
        final String text = buildText(p);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(10, 12, 10, 12));

        JDialog d = new JDialog(owner, "Phiếu " + p.so, Dialog.ModalityType.APPLICATION_MODAL);
        JButton btnPrint = new JButton("In");
        JButton btnSave = new JButton("Lưu file .txt");
        JButton btnClose = new JButton("Đóng");
        btnPrint.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(d, "Không in được: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnSave.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(p.so + ".txt"));
            if (fc.showSaveDialog(d) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(fc.getSelectedFile().toPath(), text.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(d, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnClose.addActionListener(e -> d.dispose());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bar.add(btnPrint);
        bar.add(btnSave);
        bar.add(btnClose);
        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(area), BorderLayout.CENTER);
        d.add(bar, BorderLayout.SOUTH);
        d.setSize(680, 480);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    private String buildText(Phieu p) {
        return PhieuXuatKhoController.buildText(p, MONEY);
    }

    // =====================================================================
    // Hộp thoại xem chi tiết phiếu
    // =====================================================================
    private void openDetail(Phieu p) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog d = new JDialog(owner, "Chi tiết phiếu xuất " + p.so, Dialog.ModalityType.APPLICATION_MODAL);

        // ---- thông tin ----
        Badge badge = new Badge();
        badge.setText(p.tt.text);
        applyBadge(badge, p.tt);
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 10));
        grid.setOpaque(false);
        grid.add(field("Mã phiếu xuất", valueLabel(p.so, RangBuocGiaoDien.MAU_CHU_DAO)));
        grid.add(field("Ngày xuất", valueLabel(p.ngay.format(DF), RangBuocGiaoDien.MAU_CHU_CHINH)));
        grid.add(field("Nhân viên lập", valueLabel(p.nhanVien, RangBuocGiaoDien.MAU_CHU_CHINH)));
        grid.add(field("Lý do xuất", valueLabel(p.lyDo, RangBuocGiaoDien.MAU_CHU_CHINH)));
        grid.add(field("Ghi chú", valueLabel(p.ghiChu == null || p.ghiChu.isEmpty() ? "—" : p.ghiChu, RangBuocGiaoDien.MAU_CHU_CHINH)));
        JPanel stWrap = new JPanel(new BorderLayout());
        stWrap.setOpaque(false);
        JLabel stCap = new JLabel("Trạng thái");
        stCap.setFont(new Font(FONT, Font.PLAIN, 11));
        stCap.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        stWrap.add(stCap, BorderLayout.NORTH);
        stWrap.add(badgeWrap, BorderLayout.CENTER);
        grid.add(stWrap);

        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(new EmptyBorder(14, 18, 10, 18));
        info.add(grid, BorderLayout.CENTER);

        // ---- bảng ----
        DefaultTableModel m = new DefaultTableModel(
                new String[] { "TÊN VẬT TƯ", "ĐƠN VỊ", "SL", "GIÁ NHẬP", "THÀNH TIỀN" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Dong dg : p.dong)
            m.addRow(new Object[] { dg.ten, dg.donVi, dg.sl, dg.donGia,
                    PhieuXuatKhoController.thanhTien(dg) });
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setShowVerticalLines(false);
        t.setGridColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
        t.setFillsViewportHeight(true);
        t.setFocusable(false);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 32));
        t.getTableHeader().setDefaultRenderer(new HeaderRenderer(2, 3, 4));
        t.getColumnModel().getColumn(0).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.LEFT, false));
        t.getColumnModel().getColumn(1).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_PHU, Font.PLAIN, SwingConstants.LEFT, false));
        t.getColumnModel().getColumn(2).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.RIGHT, false));
        t.getColumnModel().getColumn(3).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.RIGHT, true));
        t.getColumnModel().getColumn(4).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.RIGHT, true));
        t.getColumnModel().getColumn(0).setPreferredWidth(240);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);

        JLabel lblTc = new JLabel("TỔNG CỘNG");
        lblTc.setFont(new Font(FONT, Font.BOLD, 12));
        lblTc.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        JLabel lblSum = new JLabel("SL: " + PhieuXuatKhoController.tongSoLuong(p) + "      "
                + MONEY.format(PhieuXuatKhoController.tongTien(p)) + " đ");
        lblSum.setFont(new Font(FONT, Font.BOLD, 13));
        lblSum.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        JPanel total = new JPanel(new BorderLayout());
        total.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        total.setBorder(new EmptyBorder(9, 12, 9, 14));
        total.add(lblTc, BorderLayout.WEST);
        total.add(lblSum, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout());
        center.add(sp, BorderLayout.CENTER);
        center.add(total, BorderLayout.SOUTH);

        // ---- nút ----
        boolean nhap = p.tt == TrangThai.NHAP;
        RoundButton btnXn = new RoundButton("Xác nhận & Trừ tồn", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_TRANG);
        RoundButton btnIn = new RoundButton("Xuất / In phiếu", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        RoundButton btnHuy = new RoundButton("Hủy xuất", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        RoundButton btnDong = new RoundButton("Đóng", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnXn.setEnabled(nhap);
        btnHuy.setEnabled(nhap);
        btnXn.addActionListener(e -> {
            if (xacNhan(p, d))
                d.dispose();
        });
        btnHuy.addActionListener(e -> {
            if (huy(p, d))
                d.dispose();
        });
        btnIn.addActionListener(e -> xuatIn(p, d));
        btnDong.addActionListener(e -> d.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBorder(new EmptyBorder(12, 10, 12, 10));
        actions.add(btnXn);
        actions.add(btnIn);
        actions.add(btnHuy);
        actions.add(btnDong);

        JLabel note = new JLabel(
                "Lưu ý: Tồn kho chỉ bị trừ sau khi xác nhận. Không được xuất quá số lượng tồn hiện tại.");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JPanel noteBox = new JPanel(new BorderLayout());
        noteBox.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        noteBox.setBorder(new EmptyBorder(9, 18, 9, 18));
        noteBox.add(note, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(actions, BorderLayout.NORTH);
        south.add(noteBox, BorderLayout.SOUTH);

        d.setLayout(new BorderLayout());
        d.add(info, BorderLayout.NORTH);
        d.add(center, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(720, 560);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JLabel valueLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, Font.BOLD, 12));
        l.setForeground(color);
        return l;
    }

    private JComponent field(String caption, JLabel value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel c = new JLabel(caption);
        c.setFont(new Font(FONT, Font.PLAIN, 11));
        c.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        c.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c);
        p.add(Box.createVerticalStrut(2));
        p.add(value);
        return p;
    }

    private static void applyBadge(Badge b, TrangThai tt) {
        switch (tt) {

        }
    }

    // =====================================================================
    // Hộp thoại lập / sửa phiếu xuất
    // =====================================================================
    /**
     * @param src null = lập mới; khác null = sửa. Trả về phiếu đã nhập, hoặc null
     *            nếu hủy.
     */
    private Phieu openEditor(Phieu src) {
        // Danh mục vật tư + tồn kho và danh sách nhân viên đọc trực tiếp từ CSDL
        final java.util.List<CatalogItem> catalog = controller.layDanhMucVatTu();
        if (catalog.isEmpty()) {
            String err = controller.getLastError();
            JOptionPane.showMessageDialog(this,
                    err == null || err.isEmpty()
                            ? "Chưa có vật tư nào trong CSDL để lập phiếu xuất."
                            : "Không đọc được danh mục vật tư từ CSDL SALALA:\n" + err,
                    "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        final java.util.List<String[]> dsNhanVien = controller.layDanhSachNhanVien();
        if (src == null && dsNhanVien.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có nhân viên nào trong CSDL để lập phiếu.",
                    "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        final Phieu[] result = { null };

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                src == null ? "Lập phiếu xuất kho" : "Sửa phiếu xuất " + src.so,
                Dialog.ModalityType.APPLICATION_MODAL);

        // ---- thông tin chung ----
        JTextField txtMa = new JTextField(src == null ? controller.previewNextCode() : src.so);
        txtMa.setEditable(false);
        JTextField txtNgay = new JTextField((src == null ? LocalDate.now() : src.ngay).format(DF));

        JComboBox<String> cboNV = new JComboBox<>();
        if (src == null) {
            int chon = 0;
            for (int i = 0; i < dsNhanVien.size(); i++) {
                String[] nv = dsNhanVien.get(i);
                cboNV.addItem(nv[0] + " - " + nv[1]);
                if (maNhanVienHienTai != null ? maNhanVienHienTai.equals(nv[0])
                        : (tenNhanVienHienTai != null && tenNhanVienHienTai.equals(nv[1]))) {
                    chon = i;
                }
            }
            cboNV.setSelectedIndex(chon);
        } else { // người lập phiếu không đổi khi sửa
            cboNV.addItem((src.maNhanVien == null ? "" : src.maNhanVien + " - ") + src.nhanVien);
            cboNV.setEnabled(false);
        }

        JComboBox<String> cboLyDo = new JComboBox<>(LY_DO);
        cboLyDo.setEditable(true);
        if (src != null)
            cboLyDo.setSelectedItem(src.lyDo);
        JTextField txtGhiChu = new JTextField(src == null ? "" : src.ghiChu);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 6, 14));
        addFormRow(form, 0, "Mã phiếu xuất", txtMa);
        addFormRow(form, 1, "Ngày xuất (dd/MM/yyyy)", txtNgay);
        addFormRow(form, 2, "Nhân viên lập", cboNV);
        addFormRow(form, 3, "Lý do xuất", cboLyDo);
        addFormRow(form, 4, "Ghi chú", txtGhiChu);

        // ---- bảng vật tư của phiếu (chỉ sửa được SL) ----
        DefaultTableModel m = new DefaultTableModel(
                new String[] { "Mã VT", "Tên vật tư", "Đơn vị", "SL", "Giá nhập", "Thành tiền" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : (c >= 4 ? Long.class : String.class);
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(28);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.getTableHeader().setReorderingAllowed(false);
        t.getColumnModel().getColumn(0).setPreferredWidth(70);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(60);
        t.getColumnModel().getColumn(3).setPreferredWidth(60);
        t.getColumnModel().getColumn(4).setPreferredWidth(100);
        t.getColumnModel().getColumn(5).setPreferredWidth(110);

        JLabel lblTong = new JLabel();
        lblTong.setFont(new Font(FONT, Font.BOLD, 13));
        lblTong.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        Runnable capNhatTong = () -> {
            java.util.List<Dong> lines = linesFromModel(m);
            int sl = PhieuXuatKhoController.tongSoLuongDong(lines);
            long tien = PhieuXuatKhoController.tongTienDong(lines);
            lblTong.setText("Tổng SL: " + sl + "     Tổng tiền: " + MONEY.format(tien) + " đ");
        };

        final boolean[] busy = { false };
        m.addTableModelListener(e -> {
            if (busy[0])
                return;
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                int r = e.getFirstRow();
                if (r >= 0 && r < m.getRowCount()) {
                    busy[0] = true;
                    try {
                        int sl = ((Number) m.getValueAt(r, 3)).intValue();
                        long gia = ((Number) m.getValueAt(r, 4)).longValue();
                        int ton = controller.tonKho((String) m.getValueAt(r, 0));
                        PhieuXuatKhoController.QuantityResult quantity = controller.normalizeEditedQuantity(sl, ton);
                        if (quantity.exceededStock()) {
                            JOptionPane.showMessageDialog(d,
                                    "Không được xuất quá tồn kho. Tồn hiện tại: " + ton, "Vượt tồn kho",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        sl = quantity.getQuantity();
                        m.setValueAt(sl, r, 3);
                        m.setValueAt(PhieuXuatKhoController.thanhTien(new Dong("", "", "", sl, gia)), r, 5);
                    } finally {
                        busy[0] = false;
                    }
                }
            }
            capNhatTong.run();
        });

        if (src != null) {
            for (Dong dg : src.dong) {
                m.addRow(new Object[] { dg.ma, dg.ten, dg.donVi, dg.sl, dg.donGia,
                        PhieuXuatKhoController.thanhTien(dg) });
            }
        }
        capNhatTong.run();

        // ---- thanh thêm vật tư ----
        JComboBox<String> cboVT = new JComboBox<>();
        for (CatalogItem item : catalog) {
            cboVT.addItem(item.getMa() + " - " + item.getTen() + "  (tồn: " + item.getTon() + ")");
        }
        JSpinner spSL = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        JButton btnThem = new JButton("Thêm vào phiếu");
        JButton btnXoa = new JButton("Xóa dòng chọn");
        btnThem.addActionListener(e -> {
            int i = cboVT.getSelectedIndex();
            if (i < 0)
                return;
            if (t.isEditing())
                t.getCellEditor().stopCellEditing();
            CatalogItem item = catalog.get(i);
            String ma = item.getMa();
            int ton = controller.tonKho(ma); // tồn mới nhất trong CSDL
            int sl = (Integer) spSL.getValue();
            java.util.List<Dong> lines = linesFromModel(m);
            PhieuXuatKhoController.LineAdditionResult addition = controller.mergeLine(lines,
                    controller.taoDong(item, sl), ton);
            if (!addition.isSuccessful()) {
                JOptionPane.showMessageDialog(d,
                        "Không được xuất quá số lượng tồn hiện tại.\nTồn kho: " + ton
                                + (addition.getAlreadyAdded() > 0
                                        ? " (đã thêm " + addition.getAlreadyAdded() + " vào phiếu)"
                                        : ""),
                        "Vượt tồn kho", JOptionPane.WARNING_MESSAGE);
                return;
            }
            replaceModelRows(m, lines);
        });
        btnXoa.addActionListener(e -> {
            if (t.isEditing())
                t.getCellEditor().stopCellEditing();
            int r = t.getSelectedRow();
            if (r >= 0)
                m.removeRow(r);
        });

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addBar.add(cboVT);
        addBar.add(new JLabel("SL"));
        addBar.add(spSL);
        addBar.add(btnThem);
        addBar.add(btnXoa);

        JPanel mid = new JPanel(new BorderLayout());
        mid.setBorder(new EmptyBorder(0, 14, 0, 14));
        mid.add(addBar, BorderLayout.NORTH);
        mid.add(new JScrollPane(t), BorderLayout.CENTER);

        // ---- Lưu / Hủy ----
        JButton btnLuu = new JButton(src == null ? "Lưu phiếu nháp" : "Lưu thay đổi");
        JButton btnDong = new JButton("Hủy");
        btnDong.addActionListener(e -> d.dispose());
        btnLuu.addActionListener(e -> {
            if (t.isEditing())
                t.getCellEditor().stopCellEditing();

            LocalDate ngay;
            try {
                ngay = PhieuXuatKhoController.parseDate(txtNgay.getText(), DF);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(d, "Ngày xuất không hợp lệ (định dạng dd/MM/yyyy).", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object lyObj = cboLyDo.getEditor().getItem();
            String lyDo = lyObj == null ? "" : lyObj.toString().trim();
            String ghiChu = txtGhiChu.getText().trim();
            java.util.List<Dong> lines = linesFromModel(m);
            String validationError = controller.validate(
                    new PhieuXuatKhoController.Draft(ngay, lyDo, ghiChu, lines));
            if (validationError != null) {
                JOptionPane.showMessageDialog(d, validationError, "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Phieu p = new Phieu();
            p.dong = lines;
            if (src == null) {
                String[] nv = dsNhanVien.get(cboNV.getSelectedIndex());
                p.so = txtMa.getText();
                p.maNhanVien = nv[0];
                p.nhanVien = nv[1];
                p.tt = TrangThai.NHAP;
            } else {
                p.so = src.so;
                p.maNhanVien = src.maNhanVien;
                p.nhanVien = src.nhanVien;
                p.tt = src.tt;
            }
            p.ngay = ngay;
            p.lyDo = lyDo;
            p.ghiChu = ghiChu;
            result[0] = p;
            d.dispose();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(6, 14, 10, 14));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.add(btnLuu);
        btns.add(btnDong);
        bottom.add(lblTong, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);

        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.NORTH);
        d.add(mid, BorderLayout.CENTER);
        d.add(bottom, BorderLayout.SOUTH);
        d.setSize(780, 600);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        return result[0];
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(3, 0, 3, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);
        form.add(comp, gc);
    }

    private static java.util.List<Dong> linesFromModel(DefaultTableModel model) {
        java.util.List<Dong> lines = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            lines.add(new Dong((String) model.getValueAt(row, 0), (String) model.getValueAt(row, 1),
                    (String) model.getValueAt(row, 2), ((Number) model.getValueAt(row, 3)).intValue(),
                    ((Number) model.getValueAt(row, 4)).longValue()));
        }
        return lines;
    }

    private static void replaceModelRows(DefaultTableModel model, java.util.List<Dong> lines) {
        model.setRowCount(0);
        for (Dong line : lines) {
            model.addRow(new Object[] { line.ma, line.ten, line.donVi, line.sl,
                    line.donGia, PhieuXuatKhoController.thanhTien(line) });
        }
    }

    // =====================================================================
    // Renderers & component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final java.util.Set<Integer> right = new java.util.HashSet<>();

        HeaderRenderer(int... rightCols) {
            for (int c : rightCols)
                right.add(c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(right.contains(c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 11));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class Cell extends DefaultTableCellRenderer {
        private final Color color;
        private final int style, align;
        private final boolean money;

        Cell(Color color, int style, int align, boolean money) {
            this.color = color;
            this.style = style;
            this.align = align;
            this.money = money;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(align);
            setFont(new Font(FONT, style, 12));
            setForeground(color);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (money && v instanceof Number)
                setText(MONEY.format(((Number) v).longValue()) + " đ");
            return this;
        }
    }

    /** Cột trạng thái: nhãn bo tròn có chấm màu. */
    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Badge badge = new Badge();

        StatusRenderer() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.insets = new Insets(0, 8, 0, 0);
            panel.add(badge, gc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();

            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    private static class Badge extends JLabel {

        Badge() {
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 11));
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }

        void setColors(Color bg, Color fg) {
            this.bg = bg;
            setForeground(fg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút bo góc. */
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
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setForeground(b ? fg : RangBuocGiaoDien.MAU_CHU_PHU);
            setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
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

    /** Panel bo góc có viền. */
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

    /** Ô tìm kiếm có icon kính lúp và chữ gợi ý. */
    private static class SearchField extends JTextField {
        private final String hint;

        SearchField(String hint) {
            this.hint = hint;
            setFont(new Font(FONT, Font.PLAIN, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 32, 0, 8)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(12, cy - 6, 8, 8));
            g2.drawLine(19, cy + 1, 23, cy + 5);
            if (getText().isEmpty()) {
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 32, cy + (fm.getAscent() - fm.getDescent()) / 2);
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