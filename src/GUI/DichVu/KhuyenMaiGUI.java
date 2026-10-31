package GUI.DichVu;

import dao.KhuyenMai_DAO;
import entity.KhuyenMai_Entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Giao diện "Khuyến mãi" - SALALA Pet Shop (Dịch vụ / Khuyến mãi).
 * Gồm: tiêu đề, 4 thẻ thống kê, bộ lọc (tìm kiếm, trạng thái, thời gian), bảng có phân trang,
 * thao tác Sửa / Nhân bản / Tạm dừng - Kích hoạt, xuất danh sách CSV.
 *
 * Lưu ý về dữ liệu:
 *  - Trạng thái (Đang áp dụng / Sắp diễn ra / Hết hạn) được tính tự động từ ngày bắt đầu - kết thúc.
 *  - "Tạm dừng" hiện chỉ lưu trong bộ nhớ (TAM_DUNG); muốn lưu bền vững cần thêm cột vào CSDL/entity.
 *  - "Loại giảm": mức giảm <= 100 được coi là Phần trăm, > 100 là Số tiền (VNĐ).
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class KhuyenMaiGUI extends JPanel {

    // ===== Màu sắc =====
    private static final Color XANH    = new Color(0x0F6B5C);
    private static final Color XANH_BG = new Color(0xDDF3EE);
    private static final Color NEN     = new Color(0xF8FAFA);
    private static final Color VIEN    = new Color(0xE5E7EB);
    private static final Color CHU     = new Color(0x111827);
    private static final Color XAM     = new Color(0x6B7280);
    private static final Color DO      = new Color(0xDC2626);

    private static final Color LUC_BG  = new Color(0xDCFCE7);
    private static final Color LUC     = new Color(0x047857);
    private static final Color LAM_BG  = new Color(0xDBEAFE);
    private static final Color LAM     = new Color(0x1D4ED8);
    private static final Color TIM_BG  = new Color(0xEDE9FE);
    private static final Color TIM     = new Color(0x6D28D9);
    private static final Color XAM_BG  = new Color(0xF3F4F6);
    private static final Color XAM_DAM = new Color(0x4B5563);

    private static final String FONT = "Segoe UI";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    private static final int COT_TRANG_THAI = 7;
    private static final int COT_THAO_TAC = 8;

    /** Mã các khuyến mãi đang tạm dừng (chỉ lưu trong bộ nhớ). */
    private static final Set<String> TAM_DUNG = new HashSet<>();

    /** Dùng ở màn hình lập hóa đơn để bỏ qua khuyến mãi đang tạm dừng. */
    public static boolean isTamDung(String maKM) {
        return TAM_DUNG.contains(maKM);
    }

    private enum TT {
        DANG_AP_DUNG("Đang áp dụng"), SAP_DIEN_RA("Sắp diễn ra"), TAM_DUNG("Tạm dừng"), HET_HAN("Hết hạn");
        final String text;
        TT(String text) { this.text = text; }
    }

    private enum CheDo { THEM, SUA, NHAN_BAN }

    // ===== Dữ liệu =====
    private final KhuyenMai_DAO dao = new KhuyenMai_DAO();
    private List<KhuyenMai_Entity> tatCa = new ArrayList<>();
    private List<KhuyenMai_Entity> locRa = new ArrayList<>();
    private final List<KhuyenMai_Entity> dongHienTai = new ArrayList<>();
    private int trang = 1;
    private int soDong = 8;
    private LocalDate tuNgay, denNgay;
    private boolean dangNap = false;

    // ===== Thành phần =====
    private JLabel lblTong, lblDang, lblSap, lblTamHet, lblHienThi;
    private SearchField txtTim;
    private RoundButton btnThoiGian;
    private JComboBox<String> cboTrangThai;
    private JComboBox<Integer> cboSoDong;
    private JPanel phanTrang;
    private JTable table;
    private DefaultTableModel model;

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(NEN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel tren = new JPanel(new BorderLayout(0, 14));
        tren.setOpaque(false);
        tren.add(taoTieuDe(), BorderLayout.NORTH);
        tren.add(taoTheThongKe(), BorderLayout.CENTER);

        add(tren, BorderLayout.NORTH);
        add(taoKhungDanhSach(), BorderLayout.CENTER);

        taiLai();
    }

    // =====================================================================
    // Tiêu đề
    // =====================================================================
    private JPanel taoTieuDe() {
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblSub = new JLabel("Dịch vụ / Khuyến mãi");
        lblSub.setFont(new Font(FONT, Font.PLAIN, 12));
        lblSub.setForeground(XAM);
        JLabel lblTitle = new JLabel("Khuyến mãi");
        lblTitle.setFont(new Font(FONT, Font.BOLD, 22));
        lblTitle.setForeground(CHU);
        JLabel lblMoTa = new JLabel("Thiết lập ưu đãi theo thời gian, điều kiện áp dụng và theo dõi hiệu lực chương trình.");
        lblMoTa.setFont(new Font(FONT, Font.PLAIN, 12));
        lblMoTa.setForeground(XAM);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(LEFT_ALIGNMENT);
        trai.add(lblSub);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblTitle);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblMoTa);

        RoundButton btnThem = new RoundButton("+  Thêm khuyến mãi", XANH, XANH, Color.WHITE);
        btnThem.addActionListener(e -> moForm(null, CheDo.THEM));
        JPanel phai = new JPanel(new GridBagLayout());
        phai.setOpaque(false);
        phai.add(btnThem);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(trai, BorderLayout.WEST);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // 4 thẻ thống kê
    // =====================================================================
    private JComponent taoTheThongKe() {
        lblTong = new JLabel();
        lblDang = new JLabel();
        lblSap = new JLabel();
        lblTamHet = new JLabel();

        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(theThongKe("%", "Tổng khuyến mãi", lblTong, "Tất cả chương trình"));
        row.add(theThongKe("✓", "Đang áp dụng", lblDang, "Có thể dùng tại quầy"));
        row.add(theThongKe("◷", "Sắp diễn ra", lblSap, "Trong 30 ngày tới"));
        row.add(theThongKe("‖", "Tạm dừng / hết hạn", lblTamHet, "Không tự động áp dụng"));
        return row;
    }

    private JComponent theThongKe(String bieuTuong, String tieuDe, JLabel giaTri, String phu) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, VIEN);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel ic = new JLabel(bieuTuong, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        ic.setForeground(XANH);
        RoundedPanel icBox = new RoundedPanel(10, XANH_BG, XANH_BG);
        icBox.setLayout(new BorderLayout());
        icBox.setPreferredSize(new Dimension(38, 38));
        icBox.add(ic, BorderLayout.CENTER);
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icBox);

        JLabel t = new JLabel(tieuDe);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setForeground(XAM);
        giaTri.setFont(new Font(FONT, Font.BOLD, 22));
        giaTri.setForeground(CHU);
        JLabel s = new JLabel(phu);
        s.setFont(new Font(FONT, Font.PLAIN, 11));
        s.setForeground(XAM);

        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 8, 0);
        fl.setAlignOnBaseline(true);
        JPanel hang = new JPanel(fl);
        hang.setOpaque(false);
        hang.add(giaTri);
        hang.add(s);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        t.setAlignmentX(LEFT_ALIGNMENT);
        hang.setAlignmentX(LEFT_ALIGNMENT);
        body.add(t);
        body.add(hang);

        card.add(iconWrap, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void capNhatThongKe() {
        LocalDate now = LocalDate.now();
        int dang = 0, sap = 0, tam = 0, het = 0;
        for (KhuyenMai_Entity km : tatCa) {
            switch (trangThai(km)) {
                case DANG_AP_DUNG: dang++; break;
                case SAP_DIEN_RA:
                    if (km.getNgayBatDau() == null || !km.getNgayBatDau().isAfter(now.plusDays(30))) sap++;
                    break;
                case TAM_DUNG: tam++; break;
                default: het++; break;
            }
        }
        lblTong.setText(String.valueOf(tatCa.size()));
        lblDang.setText(String.valueOf(dang));
        lblSap.setText(String.valueOf(sap));
        lblTamHet.setText(tam + " / " + het);
    }

    // =====================================================================
    // Khung danh sách: bộ lọc + bảng + phân trang
    // =====================================================================
    private JComponent taoKhungDanhSach() {
        RoundedPanel box = new RoundedPanel(12, Color.WHITE, VIEN);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        txtTim = new SearchField("Tìm kiếm mã khuyến mãi...");
        txtTim.setPreferredSize(new Dimension(300, 34));
        txtTim.getDocument().addDocumentListener(new SimpleDocListener(() -> hienThi(true)));

        cboTrangThai = new JComboBox<>(new String[]{
                "Trạng thái: Tất cả", "Trạng thái: Đang áp dụng", "Trạng thái: Sắp diễn ra",
                "Trạng thái: Tạm dừng", "Trạng thái: Hết hạn"});
        cboTrangThai.setFont(new Font(FONT, Font.PLAIN, 12));
        cboTrangThai.setBackground(Color.WHITE);
        cboTrangThai.setPreferredSize(new Dimension(190, 34));
        cboTrangThai.addActionListener(e -> hienThi(true));

        btnThoiGian = new RoundButton("Thời gian: Tất cả", Color.WHITE, VIEN, CHU);
        btnThoiGian.setFont(new Font(FONT, Font.PLAIN, 12));
        btnThoiGian.setPreferredSize(new Dimension(230, 34));
        btnThoiGian.addActionListener(e -> chonThoiGian());

        JPanel locTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locTrai.setOpaque(false);
        locTrai.add(txtTim);
        locTrai.add(cboTrangThai);
        locTrai.add(btnThoiGian);

        RoundButton btnXuat = new RoundButton("⤓  Xuất danh sách", Color.WHITE, VIEN, CHU);
        btnXuat.addActionListener(e -> xuatDanhSach());
        JPanel locPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        locPhai.setOpaque(false);
        locPhai.add(btnXuat);

        JPanel loc = new JPanel(new BorderLayout());
        loc.setOpaque(false);
        loc.setBorder(new EmptyBorder(14, 16, 14, 16));
        loc.add(locTrai, BorderLayout.WEST);
        loc.add(locPhai, BorderLayout.EAST);

        // ---- bảng ----
        String[] cot = {"Mã khuyến mãi", "Tên khuyến mãi", "Mức giảm", "Loại giảm", "Ngày bắt đầu",
                "Ngày kết thúc", "Điều kiện", "Trạng thái", "Thao tác"};
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font(FONT, Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xF0F1F3));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xE8F5F2));
        table.setSelectionForeground(CHU);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(2));

        table.getColumnModel().getColumn(0).setCellRenderer(new Cell(XANH, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new Cell(CHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2).setCellRenderer(new Cell(CHU, Font.BOLD, SwingConstants.RIGHT));
        table.getColumnModel().getColumn(3).setCellRenderer(new Cell(CHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4).setCellRenderer(new Cell(CHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(5).setCellRenderer(new Cell(CHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(6).setCellRenderer(new Cell(CHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        int[] rong = {95, 160, 95, 80, 95, 95, 210, 110, 230};
        for (int i = 0; i < rong.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }
        table.getColumnModel().getColumn(COT_THAO_TAC).setMinWidth(230); // đủ chỗ cho 3 thao tác

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { khiClickBang(e); }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean tay = table.columnAtPoint(e.getPoint()) == COT_THAO_TAC && table.rowAtPoint(e.getPoint()) >= 0;
                table.setCursor(Cursor.getPredefinedCursor(tay ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // ---- chân bảng ----
        lblHienThi = new JLabel();
        lblHienThi.setFont(new Font(FONT, Font.PLAIN, 12));
        lblHienThi.setForeground(XAM);
        JLabel ghiChu = new JLabel("Trạng thái được cập nhật tự động theo thời gian; chương trình tạm dừng cần kích hoạt thủ công");
        ghiChu.setFont(new Font(FONT, Font.PLAIN, 11));
        ghiChu.setForeground(XAM);
        JPanel chanTrai = new JPanel();
        chanTrai.setOpaque(false);
        chanTrai.setLayout(new BoxLayout(chanTrai, BoxLayout.Y_AXIS));
        lblHienThi.setAlignmentX(LEFT_ALIGNMENT);
        ghiChu.setAlignmentX(LEFT_ALIGNMENT);
        chanTrai.add(lblHienThi);
        chanTrai.add(Box.createVerticalStrut(2));
        chanTrai.add(ghiChu);

        JLabel lblSoDong = new JLabel("Số dòng / trang");
        lblSoDong.setFont(new Font(FONT, Font.PLAIN, 12));
        lblSoDong.setForeground(XAM);
        cboSoDong = new JComboBox<>(new Integer[]{8, 16, 32});
        cboSoDong.setFont(new Font(FONT, Font.PLAIN, 12));
        cboSoDong.setBackground(Color.WHITE);
        cboSoDong.addActionListener(e -> {
            if (dangNap) return;
            soDong = (Integer) cboSoDong.getSelectedItem();
            hienThi(true);
        });
        phanTrang = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        phanTrang.setOpaque(false);

        JPanel chanPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chanPhai.setOpaque(false);
        chanPhai.add(lblSoDong);
        chanPhai.add(cboSoDong);
        chanPhai.add(phanTrang);

        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN),
                new EmptyBorder(10, 16, 10, 12)));
        chan.add(chanTrai, BorderLayout.WEST);
        chan.add(chanPhai, BorderLayout.EAST);

        box.add(loc, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        box.add(chan, BorderLayout.SOUTH);
        return box;
    }

    // =====================================================================
    // Nạp dữ liệu, lọc, phân trang
    // =====================================================================
    /** Đọc lại dữ liệu từ CSDL rồi hiển thị. */
    private void taiLai() {
        List<KhuyenMai_Entity> ds = dao.getAllKhuyenMai();
        tatCa = ds == null ? new ArrayList<>() : new ArrayList<>(ds);
        hienThi(false);
    }

    private TT trangThai(KhuyenMai_Entity km) {
        LocalDate now = LocalDate.now();
        if (km.getNgayKetThuc() != null && km.getNgayKetThuc().isBefore(now)) return TT.HET_HAN;
        if (TAM_DUNG.contains(km.getMaKM())) return TT.TAM_DUNG;
        if (km.getNgayBatDau() != null && km.getNgayBatDau().isAfter(now)) return TT.SAP_DIEN_RA;
        return TT.DANG_AP_DUNG;
    }

    private static boolean laPhanTram(KhuyenMai_Entity km) {
        return km.getMucKM() <= 100;
    }

    private static String soThuong(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static String hienMuc(KhuyenMai_Entity km) {
        double m = km.getMucKM();
        return m <= 100 ? soThuong(m) + "%" : MONEY.format((long) m) + " ₫";
    }

    private void apDungLoc() {
        String kw = txtTim.getText().trim().toLowerCase();
        int idx = cboTrangThai.getSelectedIndex(); // 0 tất cả, 1 đang áp dụng, 2 sắp diễn ra, 3 tạm dừng, 4 hết hạn
        TT[] map = {null, TT.DANG_AP_DUNG, TT.SAP_DIEN_RA, TT.TAM_DUNG, TT.HET_HAN};
        locRa = new ArrayList<>();
        for (KhuyenMai_Entity km : tatCa) {
            if (!kw.isEmpty()
                    && !nz(km.getMaKM()).toLowerCase().contains(kw)
                    && !nz(km.getTenKM()).toLowerCase().contains(kw)) continue;
            if (idx > 0 && trangThai(km) != map[idx]) continue;
            if (tuNgay != null && km.getNgayKetThuc() != null && km.getNgayKetThuc().isBefore(tuNgay)) continue;
            if (denNgay != null && km.getNgayBatDau() != null && km.getNgayBatDau().isAfter(denNgay)) continue;
            locRa.add(km);
        }
    }

    /** Lọc lại, cập nhật thống kê và hiển thị trang hiện tại. */
    private void hienThi(boolean veTrang1) {
        if (model == null) return;
        dangNap = true;
        apDungLoc();
        int tongTrang = Math.max(1, (int) Math.ceil(locRa.size() / (double) soDong));
        if (veTrang1) trang = 1;
        if (trang > tongTrang) trang = tongTrang;

        model.setRowCount(0);
        dongHienTai.clear();
        int tu = (trang - 1) * soDong;
        int den = Math.min(tu + soDong, locRa.size());
        for (int i = tu; i < den; i++) {
            KhuyenMai_Entity km = locRa.get(i);
            dongHienTai.add(km);
            model.addRow(new Object[]{
                    km.getMaKM(),
                    km.getTenKM(),
                    hienMuc(km),
                    laPhanTram(km) ? "Phần trăm" : "Số tiền",
                    km.getNgayBatDau() != null ? DF.format(km.getNgayBatDau()) : "",
                    km.getNgayKetThuc() != null ? DF.format(km.getNgayKetThuc()) : "",
                    nz(km.getDieuKien()),
                    trangThai(km).text,
                    ""
            });
        }
        lblHienThi.setText(locRa.isEmpty()
                ? "Không có khuyến mãi nào"
                : "Hiển thị " + (tu + 1) + "–" + den + " trong " + locRa.size() + " khuyến mãi");
        capNhatThongKe();
        dungPhanTrang(tongTrang);
        dangNap = false;
    }

    private void dungPhanTrang(int tongTrang) {
        phanTrang.removeAll();
        phanTrang.add(nutTrang("‹", trang > 1, false, () -> { trang--; hienThi(false); }));
        int dau = Math.max(1, Math.min(trang - 2, tongTrang - 4));
        int cuoi = Math.min(tongTrang, dau + 4);
        for (int i = dau; i <= cuoi; i++) {
            final int n = i;
            phanTrang.add(nutTrang(String.valueOf(i), true, i == trang, () -> { trang = n; hienThi(false); }));
        }
        if (cuoi < tongTrang) {
            JLabel cham = new JLabel("…");
            cham.setForeground(XAM);
            phanTrang.add(cham);
        }
        phanTrang.add(nutTrang("›", trang < tongTrang, false, () -> { trang++; hienThi(false); }));
        phanTrang.revalidate();
        phanTrang.repaint();
    }

    private RoundButton nutTrang(String text, boolean choPhep, boolean chon, Runnable hanhDong) {
        RoundButton b = chon
                ? new RoundButton(text, XANH, XANH, Color.WHITE)
                : new RoundButton(text, Color.WHITE, VIEN, CHU);
        b.setBorder(new EmptyBorder(6, 11, 6, 11));
        b.setEnabled(choPhep);
        b.addActionListener(e -> hanhDong.run());
        return b;
    }

    private void chonThoiGian() {
        JTextField tu = new JTextField(tuNgay == null ? "" : tuNgay.format(DF), 10);
        JTextField den = new JTextField(denNgay == null ? "" : denNgay.format(DF), 10);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Từ ngày (dd/MM/yyyy)"));
        p.add(tu);
        p.add(new JLabel("Đến ngày (dd/MM/yyyy)"));
        p.add(den);
        p.add(new JLabel(""));
        p.add(new JLabel("Để trống = không giới hạn"));
        int ok = JOptionPane.showConfirmDialog(this, p, "Lọc theo thời gian hiệu lực",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            LocalDate f = tu.getText().trim().isEmpty() ? null : LocalDate.parse(tu.getText().trim(), DF);
            LocalDate t = den.getText().trim().isEmpty() ? null : LocalDate.parse(den.getText().trim(), DF);
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
        if (tuNgay == null && denNgay == null) text = "Thời gian: Tất cả";
        else if (tuNgay != null && denNgay != null) text = "Thời gian: " + tuNgay.format(DF) + " – " + denNgay.format(DF);
        else if (tuNgay != null) text = "Thời gian: từ " + tuNgay.format(DF);
        else text = "Thời gian: đến " + denNgay.format(DF);
        btnThoiGian.setText(text);
        hienThi(true);
    }

    // =====================================================================
    // Thao tác trên dòng
    // =====================================================================
    /** Vị trí các thao tác trong ô (khớp với ActionRenderer): Sửa 12-64, Nhân bản 64-140, Tạm dừng/Kích hoạt 140-224. */
    private void khiClickBang(MouseEvent e) {
        int r = table.rowAtPoint(e.getPoint());
        int c = table.columnAtPoint(e.getPoint());
        if (r < 0 || c != COT_THAO_TAC || r >= dongHienTai.size()) return;
        int x = e.getX() - table.getCellRect(r, c, false).x;
        KhuyenMai_Entity km = dongHienTai.get(r);
        if (x >= 12 && x < 64) {
            moForm(km, CheDo.SUA);
        } else if (x >= 64 && x < 140) {
            moForm(km, CheDo.NHAN_BAN);
        } else if (x >= 140 && x < 224) {
            doiTamDung(km);
        }
    }

    private void doiTamDung(KhuyenMai_Entity km) {
        TT tt = trangThai(km);
        if (tt == TT.HET_HAN) return;
        if (tt == TT.TAM_DUNG) {
            TAM_DUNG.remove(km.getMaKM());
        } else {
            int kq = JOptionPane.showConfirmDialog(this,
                    "Tạm dừng khuyến mãi " + km.getTenKM() + " (" + km.getMaKM() + ")?\n"
                            + "Chương trình sẽ không được áp dụng cho đến khi kích hoạt lại.",
                    "Tạm dừng khuyến mãi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (kq != JOptionPane.YES_OPTION) return;
            TAM_DUNG.add(km.getMaKM());
        }
        hienThi(false);
    }

    // =====================================================================
    // Xuất danh sách CSV
    // =====================================================================
    private void xuatDanhSach() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh-sach-khuyen-mai.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Mã khuyến mãi,Tên khuyến mãi,Mức giảm,Loại giảm,Ngày bắt đầu,Ngày kết thúc,Điều kiện,Trạng thái\r\n");
        for (KhuyenMai_Entity km : locRa) {
            sb.append(csv(km.getMaKM())).append(',')
              .append(csv(km.getTenKM())).append(',')
              .append(csv(hienMuc(km))).append(',')
              .append(csv(laPhanTram(km) ? "Phần trăm" : "Số tiền")).append(',')
              .append(csv(km.getNgayBatDau() != null ? DF.format(km.getNgayBatDau()) : "")).append(',')
              .append(csv(km.getNgayKetThuc() != null ? DF.format(km.getNgayKetThuc()) : "")).append(',')
              .append(csv(nz(km.getDieuKien()))).append(',')
              .append(csv(trangThai(km).text)).append("\r\n");
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + locRa.size() + " khuyến mãi ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }

    // =====================================================================
    // Form thêm / sửa / nhân bản
    // =====================================================================
    private void moForm(KhuyenMai_Entity mau, CheDo cheDo) {
        final boolean sua = cheDo == CheDo.SUA;
        String tieuDe = sua ? "Chỉnh sửa khuyến mãi" : (cheDo == CheDo.NHAN_BAN ? "Nhân bản khuyến mãi" : "Thêm khuyến mãi");

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 540);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField txtMa = new JTextField(sua ? mau.getMaKM() : "KM" + System.currentTimeMillis() % 100000);
        if (sua) txtMa.setEditable(false);
        JTextField txtTen = new JTextField(mau == null ? ""
                : nz(mau.getTenKM()) + (cheDo == CheDo.NHAN_BAN ? " (bản sao)" : ""));
        JComboBox<String> cboLoai = new JComboBox<>(new String[]{"Phần trăm", "Số tiền"});
        if (mau != null && !laPhanTram(mau)) cboLoai.setSelectedIndex(1);
        JTextField txtMuc = new JTextField(mau == null ? "10" : soThuong(mau.getMucKM()));
        JTextField txtBatDau = new JTextField(mau == null || mau.getNgayBatDau() == null
                ? DF.format(LocalDate.now()) : DF.format(mau.getNgayBatDau()));
        JTextField txtKetThuc = new JTextField(mau == null || mau.getNgayKetThuc() == null
                ? DF.format(LocalDate.now().plusMonths(3)) : DF.format(mau.getNgayKetThuc()));
        JTextField txtDieuKien = new JTextField(mau == null ? "" : nz(mau.getDieuKien()));

        String[] nhan = {"Mã KM", "Tên khuyến mãi", "Loại giảm", "Mức giảm (% hoặc số tiền VNĐ)",
                "Ngày bắt đầu (dd/MM/yyyy)", "Ngày kết thúc (dd/MM/yyyy)", "Điều kiện áp dụng"};
        JComponent[] truong = {txtMa, txtTen, cboLoai, txtMuc, txtBatDau, txtKetThuc, txtDieuKien};
        for (int i = 0; i < nhan.length; i++) {
            gc.gridy = i * 2;
            gc.insets = new Insets(0, 0, 4, 0);
            p.add(new JLabel(nhan[i]), gc);
            gc.gridy = i * 2 + 1;
            gc.insets = new Insets(0, 0, 10, 0);
            p.add(truong[i], gc);
        }

        RoundButton btnLuu = new RoundButton("Lưu", XANH, XANH, Color.WHITE);
        RoundButton btnHuy = new RoundButton("Hủy", Color.WHITE, VIEN, CHU);
        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            try {
                String ma = txtMa.getText().trim();
                String ten = txtTen.getText().trim();
                boolean phanTram = cboLoai.getSelectedIndex() == 0;
                String mucStr = txtMuc.getText().trim();
                double muc = phanTram
                        ? Double.parseDouble(mucStr.replace(",", "."))
                        : Double.parseDouble(mucStr.replace(".", "").replace(",", ""));
                LocalDate bd = LocalDate.parse(txtBatDau.getText().trim(), DF);
                LocalDate kt = LocalDate.parse(txtKetThuc.getText().trim(), DF);
                String dk = txtDieuKien.getText().trim();

                if (ma.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Mã khuyến mãi không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Tên khuyến mãi không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (phanTram && (muc <= 0 || muc > 100)) {
                    JOptionPane.showMessageDialog(dlg, "Mức giảm theo phần trăm phải trong khoảng (0, 100].", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!phanTram && muc <= 100) {
                    JOptionPane.showMessageDialog(dlg, "Mức giảm theo số tiền phải lớn hơn 100 ₫.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (kt.isBefore(bd)) {
                    JOptionPane.showMessageDialog(dlg, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!sua) {
                    for (KhuyenMai_Entity k : tatCa) {
                        if (ma.equalsIgnoreCase(k.getMaKM())) {
                            JOptionPane.showMessageDialog(dlg, "Mã khuyến mãi đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                KhuyenMai_Entity km = new KhuyenMai_Entity(ma, ten, muc, bd, kt, dk);
                boolean ok = sua ? dao.updateKhuyenMai(km) : dao.createKhuyenMai(km);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Lưu thành công!");
                    dlg.dispose();
                    taiLai(); // nạp lại dữ liệu mới ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(dlg, "Không thể lưu khuyến mãi vào CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dlg, "Dữ liệu nhập không hợp lệ (kiểm tra định dạng số và ngày dd/MM/yyyy).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pPhai.setOpaque(false);
        pPhai.add(btnHuy);
        pPhai.add(btnLuu);

        JPanel pBtn = new JPanel(new BorderLayout());
        pBtn.setBackground(Color.WHITE);
        pBtn.setBorder(new EmptyBorder(0, 20, 14, 20));
        pBtn.add(pPhai, BorderLayout.EAST);

        if (sua) { // vẫn giữ chức năng xóa của bản cũ, đặt trong form chỉnh sửa
            RoundButton btnXoa = new RoundButton("Xóa", Color.WHITE, new Color(0xFCA5A5), DO);
            btnXoa.addActionListener(e -> {
                int kq = JOptionPane.showConfirmDialog(dlg,
                        "Xóa khuyến mãi " + mau.getTenKM() + " (" + mau.getMaKM() + ")?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (kq != JOptionPane.YES_OPTION) return;
                if (dao.deleteKhuyenMai(mau.getMaKM())) {
                    TAM_DUNG.remove(mau.getMaKM());
                    JOptionPane.showMessageDialog(dlg, "Xóa thành công!");
                    dlg.dispose();
                    taiLai();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Không thể xóa khuyến mãi (có thể đang được sử dụng).",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            pBtn.add(btnXoa, BorderLayout.WEST);
        }

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(pBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // =====================================================================
    // Renderers & component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final Set<Integer> phai = new HashSet<>();
        HeaderRenderer(int... cotPhai) {
            for (int c : cotPhai) phai.add(c);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(phai.contains(c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 11));
            setForeground(XAM);
            setBackground(new Color(0xF9FAFB));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, VIEN),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class Cell extends DefaultTableCellRenderer {
        private final Color mau;
        private final int kieu, canh;
        Cell(Color mau, int kieu, int canh) {
            this.mau = mau; this.kieu = kieu; this.canh = canh;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(canh);
            setFont(new Font(FONT, kieu, 12));
            setForeground(mau);
            setBorder(new EmptyBorder(0, 12, 0, 12));
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
            if (TT.DANG_AP_DUNG.text.equals(s)) badge.setColors(LUC_BG, LUC);
            else if (TT.SAP_DIEN_RA.text.equals(s)) badge.setColors(LAM_BG, LAM);
            else if (TT.TAM_DUNG.text.equals(s)) badge.setColors(TIM_BG, TIM);
            else badge.setColors(XAM_BG, XAM_DAM);
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    /** Cột thao tác: Sửa | Nhân bản | Tạm dừng (hoặc Kích hoạt). Vị trí khớp với khiClickBang. */
    private static class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        private final JLabel lblSua = link("✎ Sửa", XANH, 52);
        private final JLabel lblNhanBan = link("❐ Nhân bản", XANH, 76);
        private final JLabel lblDoi = link("", DO, 84);

        ActionRenderer() {
            panel.setBorder(new EmptyBorder(11, 12, 0, 0));
            panel.add(lblSua);
            panel.add(lblNhanBan);
            panel.add(lblDoi);
        }

        private static JLabel link(String text, Color color, int width) {
            JLabel l = new JLabel(text);
            l.setFont(new Font(FONT, Font.PLAIN, 12));
            l.setForeground(color);
            l.setPreferredSize(new Dimension(width, 20));
            return l;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String tt = String.valueOf(t.getValueAt(r, COT_TRANG_THAI));
            if (TT.HET_HAN.text.equals(tt)) {
                lblDoi.setVisible(false);
            } else if (TT.TAM_DUNG.text.equals(tt)) {
                lblDoi.setVisible(true);
                lblDoi.setText("▶ Kích hoạt");
                lblDoi.setForeground(XANH);
            } else {
                lblDoi.setVisible(true);
                lblDoi.setText("‖ Tạm dừng");
                lblDoi.setForeground(DO);
            }
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    private static class Badge extends JLabel {
        private Color bg = XAM_BG;
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
            g2.setColor(en ? fill : XAM_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(en ? line : VIEN);
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
                    BorderFactory.createLineBorder(VIEN, 1, true),
                    new EmptyBorder(0, 32, 0, 8)));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight() / 2;
            g2.setColor(XAM);
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