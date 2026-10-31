package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Giao diện "Quản lý xuất kho" - SALALA Pet Shop.
 * Chức năng: danh sách phiếu xuất (tìm kiếm, lọc ngày, lọc trạng thái, phân trang),
 *            lập phiếu xuất, xem / sửa phiếu, xác nhận & trừ tồn kho, hủy phiếu,
 *            xuất / in phiếu, xuất danh sách ra CSV.
 * Quy tắc: chỉ phiếu "Đã xuất" mới trừ tồn kho; không được xuất quá số lượng tồn hiện tại.
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class PhieuXuatKho extends JPanel {

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
    private static final Color ORANGE      = new Color(0xC2410C);
    private static final Color GREEN_BG    = new Color(0xDCFCE7);
    private static final Color RED_BG      = new Color(0xFEE2E2);
    private static final Color GRAY_BG     = new Color(0xF3F4F6);
    private static final Color GRAY_FG     = new Color(0x4B5563);
    private static final Color NOTE_BG     = new Color(0xFFFBEB);

    private static final String FONT = "Segoe UI";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    private static final String[] LY_DO = {
            "Sử dụng cho điều trị", "Bán hàng cho khách", "Chăm sóc thú cưng", "Vật tư hư hỏng"
    };

    // Thông tin vật tư dùng cho dữ liệu mẫu: {mã, tên, đơn vị, giá nhập}
    private static final Object[][] VT_MAU = {
            {"VT-001", "Amoxicillin 250mg", "Ống", 45000L},
            {"VT-002", "Rabisin Vắc-xin Dại", "Lọ", 320000L},
            {"VT-003", "Prednisolone 5mg", "Viên", 8500L},
            {"VT-004", "Fronil Spot-on Dog", "Tuýp", 78000L},
            {"VT-005", "Bông y tế vô trùng", "Gói", 12000L},
            {"VT-006", "Kim tiêm 5ml", "Hộp", 35000L}
    };

    // =====================================================================
    // Mô hình dữ liệu
    // =====================================================================
    enum TrangThai {
        NHAP("Nháp"), DA_XUAT("Đã xuất"), DA_HUY("Đã hủy");
        final String text;
        TrangThai(String text) { this.text = text; }
    }

    static class Dong {
        String ma, ten, donVi;
        int sl;
        long donGia; // giá nhập
        Dong(String ma, String ten, String donVi, int sl, long donGia) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.sl = sl; this.donGia = donGia;
        }
        long thanhTien() { return sl * donGia; }
    }

    static class Phieu {
        String so, nhanVien, lyDo, ghiChu = "";
        LocalDate ngay;
        TrangThai tt = TrangThai.NHAP;
        java.util.List<Dong> dong = new ArrayList<>();

        int tongSL() { int t = 0; for (Dong d : dong) t += d.sl; return t; }
        long tongTien() { long t = 0; for (Dong d : dong) t += d.thanhTien(); return t; }
    }

    // ===== Phụ thuộc bên ngoài =====
    private final Supplier<java.util.List<Object[]>> catalogSupplier; // {ma, ten, donVi, giaNhap}
    private final BiConsumer<String, Integer> stockUpdater;           // (maVT, soLuongThayDoi) - truyền số âm để trừ
    private final Function<String, Integer> tonKho;                   // tồn hiện tại theo mã vật tư

    // ===== Dữ liệu & trạng thái màn hình =====
    private final java.util.List<Phieu> all = new ArrayList<>();      // mới nhất ở đầu
    private java.util.List<Phieu> filtered = new ArrayList<>();
    private final java.util.List<Phieu> pageRows = new ArrayList<>();
    private int page = 1;
    private int pageSize = 8;
    private LocalDate tuNgay, denNgay;
    private int seq = 12;
    private String nhanVien = "Lê Minh";

    // ===== Thành phần =====
    private JLabel lblTongPhieu, lblDaXuat, lblNhap, lblGiaTri, lblShowing;
    private SearchField txtSearch;
    private RoundButton btnDate;
    private JComboBox<String> cboTrangThai;
    private JComboBox<Integer> cboPageSize;
    private JPanel pager;
    private JTable table;
    private DefaultTableModel model;
    private boolean loading = false;

    /**
     * @param catalogSupplier danh sách vật tư hiện có: mỗi phần tử {ma, ten, donVi, giaNhap}
     * @param stockUpdater    được gọi khi xác nhận phiếu: (mã vật tư, -số lượng xuất)
     * @param tonKho          trả về tồn kho hiện tại của một mã vật tư
     */
    public PhieuXuatKho(Supplier<java.util.List<Object[]>> catalogSupplier,
                        BiConsumer<String, Integer> stockUpdater,
                        Function<String, Integer> tonKho) {
        this.catalogSupplier = catalogSupplier;
        this.stockUpdater = stockUpdater;
        this.tonKho = tonKho;

        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setOpaque(true);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(createHeader(), BorderLayout.NORTH);
        top.add(createStatCards(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(createListCard(), BorderLayout.CENTER);

        loadSampleData();
        reload(true);
    }

    public void setNhanVien(String ten) { this.nhanVien = ten; }

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
        bc.setForeground(TEXT_GRAY);
        JLabel t = new JLabel("Quản lý xuất kho");
        t.setFont(new Font(FONT, Font.BOLD, 22));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel("Theo dõi phiếu xuất vật tư, lý do sử dụng và giá trị xuất kho.");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(TEXT_GRAY);
        bc.setAlignmentX(LEFT_ALIGNMENT);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(bc);
        left.add(Box.createVerticalStrut(2));
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        RoundButton btnLap = new RoundButton("+  Lập phiếu xuất kho", PRIMARY, PRIMARY, WHITE);
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
        RoundedPanel card = new RoundedPanel(12, WHITE, BORDER);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel ic = new JLabel(glyph, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        ic.setForeground(PRIMARY);
        RoundedPanel icBox = new RoundedPanel(10, PRIMARY_BG, PRIMARY_BG);
        icBox.setLayout(new BorderLayout());
        icBox.setPreferredSize(new Dimension(38, 38));
        icBox.add(ic, BorderLayout.CENTER);
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icBox);

        JLabel t = new JLabel(title);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setForeground(TEXT_GRAY);
        value.setFont(new Font(FONT, Font.BOLD, 22));
        value.setForeground(TEXT_DARK);
        JLabel s = new JLabel(sub);
        s.setFont(new Font(FONT, Font.PLAIN, 11));
        s.setForeground(TEXT_GRAY);

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

        card.add(iconWrap, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void refreshStats() {
        int nDaXuat = 0, nNhap = 0;
        long giaTri = 0;
        for (Phieu p : all) {
            if (p.tt == TrangThai.DA_XUAT) { nDaXuat++; giaTri += p.tongTien(); }
            else if (p.tt == TrangThai.NHAP) nNhap++;
        }
        lblTongPhieu.setText(String.valueOf(all.size()));
        lblDaXuat.setText(String.valueOf(nDaXuat));
        lblNhap.setText(String.valueOf(nNhap));
        lblGiaTri.setText(rutGon(giaTri));
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
    // Khung danh sách: bộ lọc + bảng + phân trang
    // =====================================================================
    private JComponent createListCard() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        txtSearch = new SearchField("Tìm theo mã phiếu xuất...");
        txtSearch.setPreferredSize(new Dimension(300, 34));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(() -> reload(true)));

        btnDate = new RoundButton("Ngày xuất: Tất cả", WHITE, BORDER, TEXT_DARK);
        btnDate.setFont(new Font(FONT, Font.PLAIN, 12));
        btnDate.setPreferredSize(new Dimension(250, 34));
        btnDate.addActionListener(e -> showDateFilter());

        cboTrangThai = new JComboBox<>(new String[]{
                "Trạng thái: Tất cả", "Trạng thái: Nháp", "Trạng thái: Đã xuất", "Trạng thái: Đã hủy"});
        cboTrangThai.setFont(new Font(FONT, Font.PLAIN, 12));
        cboTrangThai.setBackground(WHITE);
        cboTrangThai.setPreferredSize(new Dimension(180, 34));
        cboTrangThai.addActionListener(e -> reload(true));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setOpaque(false);
        filterLeft.add(txtSearch);
        filterLeft.add(btnDate);
        filterLeft.add(cboTrangThai);

        RoundButton btnExport = new RoundButton("⤓  Xuất danh sách", WHITE, BORDER, TEXT_DARK);
        btnExport.addActionListener(e -> xuatDanhSach());
        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filterRight.setOpaque(false);
        filterRight.add(btnExport);

        JPanel filter = new JPanel(new BorderLayout());
        filter.setOpaque(false);
        filter.setBorder(new EmptyBorder(14, 16, 14, 16));
        filter.add(filterLeft, BorderLayout.WEST);
        filter.add(filterRight, BorderLayout.EAST);

        // ---- bảng ----
        String[] cols = {"Mã phiếu xuất", "Ngày xuất", "Nhân viên", "Lý do xuất",
                "Số lượng mặt hàng", "Tổng giá trị", "Trạng thái", "Thao tác"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(42);
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

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(4, 5));

        table.getColumnModel().getColumn(0).setCellRenderer(new Cell(PRIMARY, Font.BOLD, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(1).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(2).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(3).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(4).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.RIGHT, false));
        table.getColumnModel().getColumn(5).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.RIGHT, true));
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        int[] widths = {90, 100, 140, 190, 120, 110, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onTableClick(e); }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int c = table.columnAtPoint(e.getPoint());
                int r = table.rowAtPoint(e.getPoint());
                boolean hand = c == 7 && r >= 0;
                table.setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // ---- chân bảng: thông tin + phân trang ----
        lblShowing = new JLabel();
        lblShowing.setFont(new Font(FONT, Font.PLAIN, 12));
        lblShowing.setForeground(TEXT_GRAY);
        JLabel note = new JLabel("Giá trị tính theo giá nhập · Chỉ phiếu Đã xuất được trừ tồn kho");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(TEXT_GRAY);
        JPanel footLeft = new JPanel();
        footLeft.setOpaque(false);
        footLeft.setLayout(new BoxLayout(footLeft, BoxLayout.Y_AXIS));
        lblShowing.setAlignmentX(LEFT_ALIGNMENT);
        note.setAlignmentX(LEFT_ALIGNMENT);
        footLeft.add(lblShowing);
        footLeft.add(Box.createVerticalStrut(2));
        footLeft.add(note);

        JLabel lblRows = new JLabel("Số dòng / trang");
        lblRows.setFont(new Font(FONT, Font.PLAIN, 12));
        lblRows.setForeground(TEXT_GRAY);
        cboPageSize = new JComboBox<>(new Integer[]{8, 16, 32});
        cboPageSize.setFont(new Font(FONT, Font.PLAIN, 12));
        cboPageSize.setBackground(WHITE);
        cboPageSize.addActionListener(e -> {
            if (loading) return;
            pageSize = (Integer) cboPageSize.getSelectedItem();
            reload(true);
        });
        pager = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pager.setOpaque(false);

        JPanel footRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footRight.setOpaque(false);
        footRight.add(lblRows);
        footRight.add(cboPageSize);
        footRight.add(pager);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 16, 10, 12)));
        foot.add(footLeft, BorderLayout.WEST);
        foot.add(footRight, BorderLayout.EAST);

        box.add(filter, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        box.add(foot, BorderLayout.SOUTH);
        return box;
    }

    // =====================================================================
    // Dữ liệu mẫu
    // =====================================================================
    private static Dong dong(String ma, int sl) {
        for (Object[] v : VT_MAU) {
            if (v[0].equals(ma)) return new Dong(ma, (String) v[1], (String) v[2], sl, (Long) v[3]);
        }
        throw new IllegalArgumentException(ma);
    }

    private static Phieu mau(String so, LocalDate ngay, String nv, String lyDo, TrangThai tt, Dong... ds) {
        Phieu p = new Phieu();
        p.so = so; p.ngay = ngay; p.nhanVien = nv; p.lyDo = lyDo; p.tt = tt;
        for (Dong d : ds) p.dong.add(d);
        return p;
    }

    private void loadSampleData() {
        all.add(mau("PX0012", LocalDate.of(2026, 10, 5), "Lê Minh", "Sử dụng cho điều trị", TrangThai.NHAP,
                dong("VT-001", 4), dong("VT-004", 1), dong("VT-006", 4)));
        all.add(mau("PX0011", LocalDate.of(2026, 10, 5), "Võ Hoàng Nam", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", 6), dong("VT-005", 5)));
        all.add(mau("PX0010", LocalDate.of(2026, 10, 4), "Phạm Ngọc Mai", "Chăm sóc thú cưng", TrangThai.DA_XUAT,
                dong("VT-001", 2), dong("VT-003", 8)));
        all.add(mau("PX0009", LocalDate.of(2026, 10, 4), "Lê Minh", "Sử dụng cho điều trị", TrangThai.DA_XUAT,
                dong("VT-001", 5), dong("VT-002", 1), dong("VT-003", 2), dong("VT-004", 1)));
        all.add(mau("PX0008", LocalDate.of(2026, 10, 3), "Trần Quốc Huy", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", 1), dong("VT-002", 1), dong("VT-005", 5)));
        all.add(mau("PX0007", LocalDate.of(2026, 10, 3), "Võ Hoàng Nam", "Vật tư hư hỏng", TrangThai.DA_HUY,
                new Dong("VT-005", "Bông y tế vô trùng", "Gói", 5, 13000)));
        all.add(mau("PX0006", LocalDate.of(2026, 10, 2), "Đặng Thanh Trúc", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", 1), dong("VT-006", 5)));
        all.add(mau("PX0005", LocalDate.of(2026, 10, 2), "Lê Minh", "Sử dụng cho điều trị", TrangThai.DA_XUAT,
                dong("VT-001", 4), dong("VT-003", 4), dong("VT-005", 7)));
        all.add(mau("PX0004", LocalDate.of(2026, 10, 1), "Phạm Ngọc Mai", "Chăm sóc thú cưng", TrangThai.DA_XUAT,
                dong("VT-004", 2)));
        all.add(mau("PX0003", LocalDate.of(2026, 10, 1), "Trần Quốc Huy", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-006", 3)));
        all.add(mau("PX0002", LocalDate.of(2026, 9, 30), "Lê Minh", "Sử dụng cho điều trị", TrangThai.NHAP,
                dong("VT-002", 2)));
        all.add(mau("PX0001", LocalDate.of(2026, 9, 30), "Đặng Thanh Trúc", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-005", 10)));
    }

    // =====================================================================
    // Lọc, phân trang, nạp bảng
    // =====================================================================
    private void applyFilter() {
        String kw = txtSearch.getText().trim().toLowerCase();
        int ttIdx = cboTrangThai.getSelectedIndex(); // 0 = tất cả, 1 = Nháp, 2 = Đã xuất, 3 = Đã hủy
        TrangThai[] map = {null, TrangThai.NHAP, TrangThai.DA_XUAT, TrangThai.DA_HUY};
        filtered = new ArrayList<>();
        for (Phieu p : all) {
            if (!kw.isEmpty() && !p.so.toLowerCase().contains(kw)) continue;
            if (ttIdx > 0 && p.tt != map[ttIdx]) continue;
            if (tuNgay != null && p.ngay.isBefore(tuNgay)) continue;
            if (denNgay != null && p.ngay.isAfter(denNgay)) continue;
            filtered.add(p);
        }
    }

    /** Tính lại bộ lọc, thống kê và hiển thị trang hiện tại. */
    private void reload(boolean resetPage) {
        if (model == null) return;
        loading = true;
        applyFilter();
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) pageSize));
        if (resetPage) page = 1;
        if (page > totalPages) page = totalPages;

        model.setRowCount(0);
        pageRows.clear();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, filtered.size());
        for (int i = from; i < to; i++) {
            Phieu p = filtered.get(i);
            pageRows.add(p);
            model.addRow(new Object[]{p.so, p.ngay.format(DF), p.nhanVien, p.lyDo,
                    p.dong.size(), p.tongTien(), p.tt.text, ""});
        }
        lblShowing.setText(filtered.isEmpty()
                ? "Không có phiếu xuất kho nào"
                : "Hiển thị " + (from + 1) + "–" + to + " trong " + filtered.size() + " phiếu xuất kho");
        refreshStats();
        rebuildPager(totalPages);
        loading = false;
    }

    private void rebuildPager(int totalPages) {
        pager.removeAll();
        pager.add(pageButton("‹", page > 1, false, () -> { page--; reload(false); }));
        int start = Math.max(1, Math.min(page - 2, totalPages - 4));
        int end = Math.min(totalPages, start + 4);
        for (int i = start; i <= end; i++) {
            final int n = i;
            pager.add(pageButton(String.valueOf(i), true, i == page, () -> { page = n; reload(false); }));
        }
        if (end < totalPages) {
            JLabel dots = new JLabel("…");
            dots.setForeground(TEXT_GRAY);
            pager.add(dots);
        }
        pager.add(pageButton("›", page < totalPages, false, () -> { page++; reload(false); }));
        pager.revalidate();
        pager.repaint();
    }

    private RoundButton pageButton(String text, boolean enabled, boolean selected, Runnable action) {
        RoundButton b = selected
                ? new RoundButton(text, PRIMARY, PRIMARY, WHITE)
                : new RoundButton(text, WHITE, BORDER, TEXT_DARK);
        b.setBorder(new EmptyBorder(6, 11, 6, 11));
        b.setEnabled(enabled);
        b.addActionListener(e -> action.run());
        return b;
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
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            LocalDate f = from.getText().trim().isEmpty() ? null : LocalDate.parse(from.getText().trim(), DF);
            LocalDate t = to.getText().trim().isEmpty() ? null : LocalDate.parse(to.getText().trim(), DF);
            if (f != null && t != null && f.isAfter(t)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
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
        if (tuNgay == null && denNgay == null) text = "Ngày xuất: Tất cả";
        else if (tuNgay != null && denNgay != null) text = "Ngày xuất: " + tuNgay.format(DF) + " – " + denNgay.format(DF);
        else if (tuNgay != null) text = "Ngày xuất: từ " + tuNgay.format(DF);
        else text = "Ngày xuất: đến " + denNgay.format(DF);
        btnDate.setText(text);
        reload(true);
    }

    private void onTableClick(MouseEvent e) {
        int r = table.rowAtPoint(e.getPoint());
        int c = table.columnAtPoint(e.getPoint());
        if (r < 0 || c != 7 || r >= pageRows.size()) return;
        int relX = e.getX() - table.getCellRect(r, c, false).x;
        Phieu p = pageRows.get(r);
        if (relX < 58) xemPhieu(p);
        else if (relX < 104) suaPhieu(p);
    }

    // =====================================================================
    // Chức năng chính
    // =====================================================================
    private void lapPhieu() {
        Phieu p = openEditor(null);
        if (p != null) {
            all.add(0, p);
            reload(true);
        }
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
        if (np != null) {
            p.ngay = np.ngay;
            p.lyDo = np.lyDo;
            p.ghiChu = np.ghiChu;
            p.dong = np.dong;
            reload(false);
        }
    }

    /** Danh sách dòng vượt quá tồn kho hiện tại (rỗng = hợp lệ). */
    private java.util.List<String> kiemTraTon(Phieu p) {
        java.util.List<String> loi = new ArrayList<>();
        for (Dong d : p.dong) {
            int ton = tonKho.apply(d.ma);
            if (d.sl > ton) loi.add("• " + d.ten + ": cần xuất " + d.sl + ", tồn hiện tại " + ton);
        }
        return loi;
    }

    /** Xác nhận xuất kho -> trừ tồn. Trả về true nếu thành công. */
    private boolean xacNhan(Phieu p, Component parent) {
        if (p.tt != TrangThai.NHAP) return false;
        if (p.dong.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Phiếu chưa có vật tư nào.", "Không thể xác nhận",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        java.util.List<String> loi = kiemTraTon(p);
        if (!loi.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Không được xuất quá số lượng tồn kho:\n" + String.join("\n", loi),
                    "Không đủ tồn kho", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        int ok = JOptionPane.showConfirmDialog(parent,
                "Xác nhận xuất kho phiếu " + p.so + " và trừ " + p.tongSL() + " đơn vị khỏi tồn kho?",
                "Xác nhận xuất kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return false;

        for (Dong d : p.dong) stockUpdater.accept(d.ma, -d.sl);
        p.tt = TrangThai.DA_XUAT;
        reload(false);
        JOptionPane.showMessageDialog(parent, "Đã xuất kho và cập nhật tồn kho.", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    private boolean huy(Phieu p, Component parent) {
        if (p.tt != TrangThai.NHAP) return false;
        int ok = JOptionPane.showConfirmDialog(parent,
                "Hủy phiếu xuất " + p.so + "? Thao tác này không thể hoàn tác.",
                "Hủy phiếu xuất", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return false;
        p.tt = TrangThai.DA_HUY;
        reload(false);
        return true;
    }

    // =====================================================================
    // Xuất danh sách (CSV) & in phiếu
    // =====================================================================
    private void xuatDanhSach() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh-sach-phieu-xuat-kho.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Mã phiếu xuất,Ngày xuất,Nhân viên,Lý do xuất,Số lượng mặt hàng,Tổng giá trị,Trạng thái\r\n");
        for (Phieu p : filtered) {
            sb.append(csv(p.so)).append(',')
              .append(csv(p.ngay.format(DF))).append(',')
              .append(csv(p.nhanVien)).append(',')
              .append(csv(p.lyDo)).append(',')
              .append(p.dong.size()).append(',')
              .append(p.tongTien()).append(',')
              .append(csv(p.tt.text)).append("\r\n");
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " phiếu ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
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
        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(78) + "\n";
        sb.append("SALALA PET SHOP\n");
        sb.append("PHIẾU XUẤT KHO\n");
        sb.append(line);
        sb.append("Mã phiếu     : ").append(p.so).append('\n');
        sb.append("Ngày xuất    : ").append(p.ngay.format(DF)).append('\n');
        sb.append("Nhân viên    : ").append(p.nhanVien).append('\n');
        sb.append("Lý do xuất   : ").append(p.lyDo).append('\n');
        sb.append("Ghi chú      : ").append(p.ghiChu == null ? "" : p.ghiChu).append('\n');
        sb.append("Trạng thái   : ").append(p.tt.text).append('\n');
        sb.append(line);
        sb.append(String.format("%-28s %-7s %5s %14s %17s%n", "Tên vật tư", "ĐVT", "SL", "Giá nhập", "Thành tiền"));
        sb.append(line);
        for (Dong d : p.dong) {
            sb.append(String.format("%-28s %-7s %5d %14s %17s%n", d.ten, d.donVi, d.sl,
                    MONEY.format(d.donGia) + " đ", MONEY.format(d.thanhTien()) + " đ"));
        }
        sb.append(line);
        sb.append(String.format("%-28s %-7s %5d %14s %17s%n", "TỔNG CỘNG", "", p.tongSL(), "",
                MONEY.format(p.tongTien()) + " đ"));
        return sb.toString();
    }

    // =====================================================================
    // Hộp thoại xem chi tiết phiếu
    // =====================================================================
    private void openDetail(Phieu p) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog d = new JDialog(owner, "Chi tiết phiếu xuất " + p.so, Dialog.ModalityType.APPLICATION_MODAL);

        // ---- thông tin ----
        Badge badge = new Badge();
        badge.setText("● " + p.tt.text);
        applyBadge(badge, p.tt);
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 10));
        grid.setOpaque(false);
        grid.add(field("Mã phiếu xuất", valueLabel(p.so, PRIMARY)));
        grid.add(field("Ngày xuất", valueLabel(p.ngay.format(DF), TEXT_DARK)));
        grid.add(field("Nhân viên lập", valueLabel(p.nhanVien, TEXT_DARK)));
        grid.add(field("Lý do xuất", valueLabel(p.lyDo, TEXT_DARK)));
        grid.add(field("Ghi chú", valueLabel(p.ghiChu == null || p.ghiChu.isEmpty() ? "—" : p.ghiChu, TEXT_DARK)));
        JPanel stWrap = new JPanel(new BorderLayout());
        stWrap.setOpaque(false);
        JLabel stCap = new JLabel("Trạng thái");
        stCap.setFont(new Font(FONT, Font.PLAIN, 11));
        stCap.setForeground(TEXT_GRAY);
        stWrap.add(stCap, BorderLayout.NORTH);
        stWrap.add(badgeWrap, BorderLayout.CENTER);
        grid.add(stWrap);

        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(new EmptyBorder(14, 18, 10, 18));
        info.add(grid, BorderLayout.CENTER);

        // ---- bảng ----
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"TÊN VẬT TƯ", "ĐƠN VỊ", "SL", "GIÁ NHẬP", "THÀNH TIỀN"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Dong dg : p.dong) m.addRow(new Object[]{dg.ten, dg.donVi, dg.sl, dg.donGia, dg.thanhTien()});
        JTable t = new JTable(m);
        t.setRowHeight(32);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xF0F1F3));
        t.setFillsViewportHeight(true);
        t.setFocusable(false);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 32));
        t.getTableHeader().setDefaultRenderer(new HeaderRenderer(2, 3, 4));
        t.getColumnModel().getColumn(0).setCellRenderer(new Cell(TEXT_DARK, Font.BOLD, SwingConstants.LEFT, false));
        t.getColumnModel().getColumn(1).setCellRenderer(new Cell(TEXT_GRAY, Font.PLAIN, SwingConstants.LEFT, false));
        t.getColumnModel().getColumn(2).setCellRenderer(new Cell(PRIMARY, Font.BOLD, SwingConstants.RIGHT, false));
        t.getColumnModel().getColumn(3).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.RIGHT, true));
        t.getColumnModel().getColumn(4).setCellRenderer(new Cell(TEXT_DARK, Font.BOLD, SwingConstants.RIGHT, true));
        t.getColumnModel().getColumn(0).setPreferredWidth(240);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        sp.getViewport().setBackground(WHITE);

        JLabel lblTc = new JLabel("TỔNG CỘNG");
        lblTc.setFont(new Font(FONT, Font.BOLD, 12));
        lblTc.setForeground(PRIMARY);
        JLabel lblSum = new JLabel("SL: " + p.tongSL() + "      " + MONEY.format(p.tongTien()) + " đ");
        lblSum.setFont(new Font(FONT, Font.BOLD, 13));
        lblSum.setForeground(PRIMARY);
        JPanel total = new JPanel(new BorderLayout());
        total.setBackground(new Color(0xE6F7F3));
        total.setBorder(new EmptyBorder(9, 12, 9, 14));
        total.add(lblTc, BorderLayout.WEST);
        total.add(lblSum, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout());
        center.add(sp, BorderLayout.CENTER);
        center.add(total, BorderLayout.SOUTH);

        // ---- nút ----
        boolean nhap = p.tt == TrangThai.NHAP;
        RoundButton btnXn = new RoundButton("Xác nhận & Trừ tồn", PRIMARY, PRIMARY, WHITE);
        RoundButton btnIn = new RoundButton("Xuất / In phiếu", WHITE, BORDER, TEXT_DARK);
        RoundButton btnHuy = new RoundButton("Hủy xuất", WHITE, new Color(0xFCA5A5), RED);
        RoundButton btnDong = new RoundButton("Đóng", WHITE, BORDER, TEXT_DARK);
        btnXn.setEnabled(nhap);
        btnHuy.setEnabled(nhap);
        btnXn.addActionListener(e -> { if (xacNhan(p, d)) d.dispose(); });
        btnHuy.addActionListener(e -> { if (huy(p, d)) d.dispose(); });
        btnIn.addActionListener(e -> xuatIn(p, d));
        btnDong.addActionListener(e -> d.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBorder(new EmptyBorder(12, 10, 12, 10));
        actions.add(btnXn);
        actions.add(btnIn);
        actions.add(btnHuy);
        actions.add(btnDong);

        JLabel note = new JLabel("ⓘ  Tồn kho chỉ bị trừ sau khi xác nhận. Không được xuất quá số lượng tồn hiện tại.");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(ORANGE);
        JPanel noteBox = new JPanel(new BorderLayout());
        noteBox.setBackground(NOTE_BG);
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
        c.setForeground(TEXT_GRAY);
        c.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c);
        p.add(Box.createVerticalStrut(2));
        p.add(value);
        return p;
    }

    private static void applyBadge(Badge b, TrangThai tt) {
        switch (tt) {
            case DA_XUAT: b.setColors(GREEN_BG, GREEN); break;
            case DA_HUY:  b.setColors(RED_BG, RED); break;
            default:      b.setColors(GRAY_BG, GRAY_FG); break;
        }
    }

    // =====================================================================
    // Hộp thoại lập / sửa phiếu xuất
    // =====================================================================
    /** @param src null = lập mới; khác null = sửa. Trả về phiếu đã nhập, hoặc null nếu hủy. */
    private Phieu openEditor(Phieu src) {
        final java.util.List<Object[]> catalog = catalogSupplier.get();
        final Phieu[] result = {null};

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                src == null ? "Lập phiếu xuất kho" : "Sửa phiếu xuất " + src.so,
                Dialog.ModalityType.APPLICATION_MODAL);

        // ---- thông tin chung ----
        JTextField txtMa = new JTextField(src == null ? String.format("PX%04d", seq + 1) : src.so);
        txtMa.setEditable(false);
        JTextField txtNgay = new JTextField((src == null ? LocalDate.now() : src.ngay).format(DF));
        JTextField txtNV = new JTextField(src == null ? nhanVien : src.nhanVien);
        txtNV.setEditable(false);
        JComboBox<String> cboLyDo = new JComboBox<>(LY_DO);
        cboLyDo.setEditable(true);
        if (src != null) cboLyDo.setSelectedItem(src.lyDo);
        JTextField txtGhiChu = new JTextField(src == null ? "" : src.ghiChu);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 6, 14));
        addFormRow(form, 0, "Mã phiếu xuất", txtMa);
        addFormRow(form, 1, "Ngày xuất (dd/MM/yyyy)", txtNgay);
        addFormRow(form, 2, "Nhân viên lập", txtNV);
        addFormRow(form, 3, "Lý do xuất", cboLyDo);
        addFormRow(form, 4, "Ghi chú", txtGhiChu);

        // ---- bảng vật tư của phiếu (chỉ sửa được SL) ----
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Mã VT", "Tên vật tư", "Đơn vị", "SL", "Giá nhập", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
            @Override public Class<?> getColumnClass(int c) {
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
        lblTong.setForeground(PRIMARY);
        Runnable capNhatTong = () -> {
            int sl = 0; long tien = 0;
            for (int r = 0; r < m.getRowCount(); r++) {
                sl += ((Number) m.getValueAt(r, 3)).intValue();
                tien += ((Number) m.getValueAt(r, 5)).longValue();
            }
            lblTong.setText("Tổng SL: " + sl + "     Tổng tiền: " + MONEY.format(tien) + " đ");
        };

        final boolean[] busy = {false};
        m.addTableModelListener(e -> {
            if (busy[0]) return;
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                int r = e.getFirstRow();
                if (r >= 0 && r < m.getRowCount()) {
                    busy[0] = true;
                    try {
                        int sl = ((Number) m.getValueAt(r, 3)).intValue();
                        long gia = ((Number) m.getValueAt(r, 4)).longValue();
                        int ton = tonKho.apply((String) m.getValueAt(r, 0));
                        if (sl > ton) {
                            JOptionPane.showMessageDialog(d,
                                    "Không được xuất quá tồn kho. Tồn hiện tại: " + ton, "Vượt tồn kho",
                                    JOptionPane.WARNING_MESSAGE);
                            sl = ton;
                        }
                        if (sl < 1) sl = 1;
                        m.setValueAt(sl, r, 3);
                        m.setValueAt(sl * gia, r, 5);
                    } finally {
                        busy[0] = false;
                    }
                }
            }
            capNhatTong.run();
        });

        if (src != null) {
            for (Dong dg : src.dong) {
                m.addRow(new Object[]{dg.ma, dg.ten, dg.donVi, dg.sl, dg.donGia, dg.thanhTien()});
            }
        }
        capNhatTong.run();

        // ---- thanh thêm vật tư ----
        JComboBox<String> cboVT = new JComboBox<>();
        for (Object[] r : catalog) {
            cboVT.addItem(r[0] + " - " + r[1] + "  (tồn: " + tonKho.apply((String) r[0]) + ")");
        }
        JSpinner spSL = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        JButton btnThem = new JButton("Thêm vào phiếu");
        JButton btnXoa = new JButton("Xóa dòng chọn");
        btnThem.addActionListener(e -> {
            int i = cboVT.getSelectedIndex();
            if (i < 0) return;
            Object[] vt = catalog.get(i);
            String ma = (String) vt[0];
            int ton = tonKho.apply(ma);
            int sl = (Integer) spSL.getValue();
            int daCo = 0, rowFound = -1;
            for (int r = 0; r < m.getRowCount(); r++) {
                if (ma.equals(m.getValueAt(r, 0))) {
                    rowFound = r;
                    daCo = ((Number) m.getValueAt(r, 3)).intValue();
                }
            }
            if (daCo + sl > ton) {
                JOptionPane.showMessageDialog(d,
                        "Không được xuất quá số lượng tồn hiện tại.\nTồn kho: " + ton
                                + (daCo > 0 ? " (đã thêm " + daCo + " vào phiếu)" : ""),
                        "Vượt tồn kho", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long gia = ((Number) vt[3]).longValue();
            if (rowFound >= 0) {
                m.setValueAt(daCo + sl, rowFound, 3);
            } else {
                m.addRow(new Object[]{ma, vt[1], vt[2], sl, gia, sl * gia});
            }
        });
        btnXoa.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            int r = t.getSelectedRow();
            if (r >= 0) m.removeRow(r);
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
            if (t.isEditing()) t.getCellEditor().stopCellEditing();

            LocalDate ngay;
            try {
                ngay = LocalDate.parse(txtNgay.getText().trim(), DF);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(d, "Ngày xuất không hợp lệ (định dạng dd/MM/yyyy).", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object lyObj = cboLyDo.getEditor().getItem();
            String lyDo = lyObj == null ? "" : lyObj.toString().trim();
            if (lyDo.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Vui lòng nhập lý do xuất.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (m.getRowCount() == 0) {
                JOptionPane.showMessageDialog(d, "Phiếu cần có ít nhất một vật tư.", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Phieu p = new Phieu();
            for (int r = 0; r < m.getRowCount(); r++) {
                String ma = (String) m.getValueAt(r, 0);
                int sl = ((Number) m.getValueAt(r, 3)).intValue();
                long gia = ((Number) m.getValueAt(r, 4)).longValue();
                int ton = tonKho.apply(ma);
                if (sl <= 0) {
                    JOptionPane.showMessageDialog(d, "Số lượng phải > 0 (dòng " + (r + 1) + ").", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (sl > ton) {
                    JOptionPane.showMessageDialog(d, "Dòng " + (r + 1) + " vượt tồn kho (tồn: " + ton + ").", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                p.dong.add(new Dong(ma, (String) m.getValueAt(r, 1), (String) m.getValueAt(r, 2), sl, gia));
            }
            if (src == null) {
                seq++;
                p.so = String.format("PX%04d", seq);
                p.nhanVien = nhanVien;
                p.tt = TrangThai.NHAP;
            } else {
                p.so = src.so;
                p.nhanVien = src.nhanVien;
                p.tt = src.tt;
            }
            p.ngay = ngay;
            p.lyDo = lyDo;
            p.ghiChu = txtGhiChu.getText().trim();
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

    // =====================================================================
    // Renderers & component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final java.util.Set<Integer> right = new java.util.HashSet<>();
        HeaderRenderer(int... rightCols) {
            for (int c : rightCols) right.add(c);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(right.contains(c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 11));
            setForeground(TEXT_GRAY);
            setBackground(new Color(0xF9FAFB));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class Cell extends DefaultTableCellRenderer {
        private final Color color;
        private final int style, align;
        private final boolean money;
        Cell(Color color, int style, int align, boolean money) {
            this.color = color; this.style = style; this.align = align; this.money = money;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(align);
            setFont(new Font(FONT, style, 12));
            setForeground(color);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (money && v instanceof Number) setText(MONEY.format(((Number) v).longValue()) + " đ");
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
            badge.setText("● " + s);
            if ("Đã xuất".equals(s)) badge.setColors(GREEN_BG, GREEN);
            else if ("Đã hủy".equals(s)) badge.setColors(RED_BG, RED);
            else badge.setColors(GRAY_BG, GRAY_FG);
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    /** Cột thao tác: "Xem" và "Sửa" (vị trí khớp với onTableClick: Xem 12-58px, Sửa 58-104px). */
    private static class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ActionRenderer() {
            panel.setBorder(new EmptyBorder(11, 12, 0, 0));
            panel.add(link("Xem"));
            panel.add(link("Sửa"));
        }
        private static JLabel link(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font(FONT, Font.BOLD, 12));
            l.setForeground(PRIMARY);
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

    private static class Badge extends JLabel {
        private Color bg = GRAY_BG;
        Badge() {
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 11));
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }
        void setColors(Color bg, Color fg) { this.bg = bg; setForeground(fg); }
        @Override protected void paintComponent(Graphics g) {
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
        @Override public void setEnabled(boolean b) {
            super.setEnabled(b);
            setForeground(b ? fg : new Color(0x9CA3AF));
            setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean en = isEnabled();
            g2.setColor(en ? fill : GRAY_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(en ? line : BORDER);
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
                    new EmptyBorder(0, 32, 0, 8)));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            g2.setColor(TEXT_GRAY);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new Ellipse2D.Double(12, cy - 6, 8, 8));
            g2.drawLine(19, cy + 1, 23, cy + 5);
            if (getText().isEmpty()) {
                g2.setColor(new Color(0x9CA3AF));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 32, cy + (fm.getAscent() - fm.getDescent()) / 2);
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