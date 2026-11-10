package GUI.DichVu;

import GUI.Chung.RangBuocGiaoDien;
import Service.DichVu.KhuyenMaiService;
import Service.DichVu.KhuyenMaiService.Status;
import entity.DichVu.KhuyenMai_Entity;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Giao diện "Khuyến mãi" - SALALA Pet Shop (Dịch vụ / Khuyến mãi).
 * Gồm: tiêu đề, 4 thẻ thống kê, bộ lọc (tìm kiếm, trạng thái, thời gian), bảng,
 * thao tác Sửa / Nhân bản / Tạm dừng - Kích hoạt, xuất danh sách CSV.
 *
 * Lưu ý về dữ liệu:
 * - Trạng thái (Đang áp dụng / Sắp diễn ra / Hết hạn) được tính tự động từ ngày
 * bắt đầu - kết thúc.
 * - "Tạm dừng" hiện chỉ lưu trong bộ nhớ (TAM_DUNG); muốn lưu bền vững cần thêm
 * cột vào CSDL/entity.
 * - "Loại giảm": mức giảm <= 100 được coi là Phần trăm, > 100 là Số tiền (VNĐ).
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class KhuyenMaiGUI extends JPanel {

    // ===== Màu sắc =====

    private static final String FONT = "Segoe UI";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int COT_TRANG_THAI = 7;

    /** Dùng ở màn hình lập hóa đơn để bỏ qua khuyến mãi đang tạm dừng. */
    public static boolean isTamDung(String maKM) {
        return KhuyenMaiService.isPausedPromotion(maKM);
    }

    private enum CheDo {
        THEM, SUA, NHAN_BAN
    }

    // ===== Dữ liệu =====
    private final KhuyenMaiService service = new KhuyenMaiService();
    private List<KhuyenMai_Entity> tatCa = new ArrayList<>();
    private List<KhuyenMai_Entity> locRa = new ArrayList<>();
    private final List<KhuyenMai_Entity> dongHienTai = new ArrayList<>();
    private LocalDate tuNgay, denNgay;
    private boolean dangNap = false;

    // ===== Thành phần =====
    private JLabel lblTong, lblDang, lblSap, lblTamHet, lblHienThi;
    private SearchField txtTim;
    private RoundButton btnThoiGian;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel model;

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
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
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel lblTitle = new JLabel("Khuyến mãi");
        lblTitle.setFont(new Font(FONT, Font.BOLD, 22));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel lblMoTa = new JLabel(
                "Thiết lập ưu đãi theo thời gian, điều kiện áp dụng và theo dõi hiệu lực chương trình.");
        lblMoTa.setFont(new Font(FONT, Font.PLAIN, 12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(LEFT_ALIGNMENT);
        trai.add(lblSub);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblTitle);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblMoTa);

        RoundButton btnThem = new RoundButton("Thêm khuyến mãi", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
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
        RoundedPanel card = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel ic = new JLabel(bieuTuong, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        ic.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        RoundedPanel icBox = new RoundedPanel(10, RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        icBox.setLayout(new BorderLayout());
        icBox.setPreferredSize(new Dimension(38, 38));
        icBox.add(ic, BorderLayout.CENTER);
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icBox);

        JLabel t = new JLabel(tieuDe);
        t.setFont(new Font(FONT, Font.PLAIN, 12));
        t.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        giaTri.setFont(new Font(FONT, Font.BOLD, 22));
        giaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel s = new JLabel(phu);
        s.setFont(new Font(FONT, Font.PLAIN, 11));
        s.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

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

        // card.add(iconWrap, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void capNhatThongKe() {
        KhuyenMaiService.Summary summary = service.summarize(tatCa);
        lblTong.setText(String.valueOf(summary.total));
        lblDang.setText(String.valueOf(summary.active));
        lblSap.setText(String.valueOf(summary.upcoming));
        lblTamHet.setText(summary.paused + " / " + summary.expired);
    }

    // =====================================================================
    // Khung danh sách: bộ lọc + bảng
    // =====================================================================
    private JComponent taoKhungDanhSach() {
        RoundedPanel box = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        box.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        txtTim = new SearchField("Tìm kiếm mã khuyến mãi...");
        txtTim.setPreferredSize(new Dimension(300, 34));
        txtTim.getDocument().addDocumentListener(new SimpleDocListener(() -> hienThi(true)));

        cboTrangThai = new JComboBox<>(new String[] {
                "Trạng thái: Tất cả", "Trạng thái: Đang áp dụng", "Trạng thái: Sắp diễn ra",
                "Trạng thái: Tạm dừng", "Trạng thái: Hết hạn" });
        cboTrangThai.setFont(new Font(FONT, Font.PLAIN, 12));
        cboTrangThai.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboTrangThai.setPreferredSize(new Dimension(190, 34));
        cboTrangThai.addActionListener(e -> hienThi(true));

        btnThoiGian = new RoundButton("Thời gian: Tất cả", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnThoiGian.setFont(new Font(FONT, Font.PLAIN, 12));
        btnThoiGian.setPreferredSize(new Dimension(230, 34));
        btnThoiGian.addActionListener(e -> chonThoiGian());

        JPanel locTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locTrai.setOpaque(false);
        locTrai.add(txtTim);
        locTrai.add(cboTrangThai);
        locTrai.add(btnThoiGian);

        RoundButton btnSua = new RoundButton("Sửa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        RoundButton btnNhanBan = new RoundButton("Nhân bản", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnTamNgung = new RoundButton("Tạm ngưng", RangBuocGiaoDien.MAU_CANH_BAO_DO, RangBuocGiaoDien.MAU_CANH_BAO_DO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnXuat = new RoundButton("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);

        btnSua.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trong danh sách để sửa.", "Chưa chọn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int mr = table.convertRowIndexToModel(sel);
            if (mr >= 0 && mr < dongHienTai.size())
                moForm(dongHienTai.get(mr), CheDo.SUA);
        });

        btnNhanBan.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trong danh sách để nhân bản.",
                        "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int mr = table.convertRowIndexToModel(sel);
            if (mr >= 0 && mr < dongHienTai.size())
                moForm(dongHienTai.get(mr), CheDo.NHAN_BAN);
        });

        btnTamNgung.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một khuyến mãi trong danh sách để tạm ngưng/kích hoạt.", "Chưa chọn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int mr = table.convertRowIndexToModel(sel);
            if (mr >= 0 && mr < dongHienTai.size())
                doiTamDung(dongHienTai.get(mr));
        });

        btnXuat.addActionListener(e -> xuatDanhSach());

        JPanel locPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        locPhai.setOpaque(false);
        locPhai.add(btnSua);
        locPhai.add(btnNhanBan);
        locPhai.add(btnTamNgung);
        locPhai.add(btnXuat);

        JPanel loc = new JPanel(new BorderLayout());
        loc.setOpaque(false);
        loc.setBorder(new EmptyBorder(14, 16, 14, 16));
        loc.add(locTrai, BorderLayout.WEST);
        loc.add(locPhai, BorderLayout.EAST);

        // ---- bảng ----
        String[] cot = { "Mã khuyến mãi", "Tên khuyến mãi", "Mức giảm", "Loại giảm", "Ngày bắt đầu",
                "Ngày kết thúc", "Điều kiện", "Trạng thái" };
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
        table.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        table.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(2));

        table.getColumnModel().getColumn(0).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.RIGHT));
        table.getColumnModel().getColumn(3).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(5).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(6).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        int[] rong = { 95, 160, 95, 80, 95, 95, 210, 110 };
        for (int i = 0; i < rong.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // ---- chân bảng ----
        lblHienThi = new JLabel();
        lblHienThi.setFont(new Font(FONT, Font.PLAIN, 12));
        lblHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel ghiChu = new JLabel(
                "Trạng thái được cập nhật tự động theo thời gian; chương trình tạm dừng cần kích hoạt thủ công");
        ghiChu.setFont(new Font(FONT, Font.PLAIN, 11));
        ghiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JPanel chanTrai = new JPanel();
        chanTrai.setOpaque(false);
        chanTrai.setLayout(new BoxLayout(chanTrai, BoxLayout.Y_AXIS));
        lblHienThi.setAlignmentX(LEFT_ALIGNMENT);
        ghiChu.setAlignmentX(LEFT_ALIGNMENT);
        chanTrai.add(lblHienThi);
        chanTrai.add(Box.createVerticalStrut(2));
        chanTrai.add(ghiChu);

        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(10, 16, 10, 12)));
        chan.add(chanTrai, BorderLayout.WEST);

        box.add(loc, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        box.add(chan, BorderLayout.SOUTH);
        return box;
    }

    // =====================================================================
    // Nạp dữ liệu và lọc
    // =====================================================================
    /** Đọc lại dữ liệu từ CSDL rồi hiển thị. */
    private void taiLai() {
        List<KhuyenMai_Entity> ds = service.load();
        tatCa = new ArrayList<>(ds);
        hienThi(false);
    }

    private boolean laPhanTram(KhuyenMai_Entity km) {
        return service.isPercentage(km);
    }

    private static String soThuong(double v) {
        return KhuyenMaiService.formatNumber(v);
    }

    private String hienMuc(KhuyenMai_Entity km) {
        return service.formatAmount(km);
    }

    private void apDungLoc() {
        locRa = service.filter(tatCa, txtTim.getText(), cboTrangThai.getSelectedIndex(), tuNgay, denNgay);
    }

    /** Lọc lại, cập nhật thống kê và hiển thị toàn bộ kết quả. */
    private void hienThi(boolean veTrang1) {
        if (model == null)
            return;
        dangNap = true;
        apDungLoc();
        model.setRowCount(0);
        dongHienTai.clear();
        for (int i = 0; i < locRa.size(); i++) {
            KhuyenMai_Entity km = locRa.get(i);
            dongHienTai.add(km);
            model.addRow(new Object[] {
                    km.getMaKM(),
                    km.getTenKM(),
                    hienMuc(km),
                    laPhanTram(km) ? "Phần trăm" : "Số tiền",
                    km.getNgayBatDau() != null ? DF.format(km.getNgayBatDau()) : "",
                    km.getNgayKetThuc() != null ? DF.format(km.getNgayKetThuc()) : "",
                    nz(km.getDieuKien()),
                    service.status(km).label,
                    ""
            });
        }
        lblHienThi.setText(locRa.isEmpty()
                ? "Không có khuyến mãi nào"
                : "Hiển thị tất cả " + locRa.size() + " khuyến mãi");
        capNhatThongKe();
        dangNap = false;
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
        if (ok != JOptionPane.OK_OPTION)
            return;
        try {
            KhuyenMaiService.DateRange range = service.parseDateRange(tu.getText(), den.getText());
            tuNgay = range.from;
            denNgay = range.to;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text;
        if (tuNgay == null && denNgay == null)
            text = "Thời gian: Tất cả";
        else if (tuNgay != null && denNgay != null)
            text = "Thời gian: " + tuNgay.format(DF) + " – " + denNgay.format(DF);
        else if (tuNgay != null)
            text = "Thời gian: từ " + tuNgay.format(DF);
        else
            text = "Thời gian: đến " + denNgay.format(DF);
        btnThoiGian.setText(text);
        hienThi(true);
    }

    // =====================================================================
    // Thao tác trên dòng
    // =====================================================================

    private void doiTamDung(KhuyenMai_Entity km) {
        Status status = service.status(km);
        if (status == Status.EXPIRED)
            return;
        if (status == Status.PAUSED) {
            service.resume(km.getMaKM());
        } else {
            int kq = JOptionPane.showConfirmDialog(this,
                    "Tạm dừng khuyến mãi " + km.getTenKM() + " (" + km.getMaKM() + ")?\n"
                            + "Chương trình sẽ không được áp dụng cho đến khi kích hoạt lại.",
                    "Tạm dừng khuyến mãi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (kq != JOptionPane.YES_OPTION)
                return;
            service.pause(km.getMaKM());
        }
        hienThi(false);
    }

    // =====================================================================
    // Xuất danh sách CSV
    // =====================================================================
    private void xuatDanhSach() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh-sach-khuyen-mai.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        try {
            Files.write(fc.getSelectedFile().toPath(), service.toCsv(locRa).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + locRa.size() + " khuyến mãi ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Form thêm / sửa / nhân bản
    // =====================================================================
    private void moForm(KhuyenMai_Entity mau, CheDo cheDo) {
        final boolean sua = cheDo == CheDo.SUA;
        String tieuDe = sua ? "Chỉnh sửa khuyến mãi"
                : (cheDo == CheDo.NHAN_BAN ? "Nhân bản khuyến mãi" : "Thêm khuyến mãi");

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 540);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField txtMa = new JTextField(sua ? mau.getMaKM() : "KM" + System.currentTimeMillis() % 100000);
        if (sua)
            txtMa.setEditable(false);
        JTextField txtTen = new JTextField(mau == null ? ""
                : nz(mau.getTenKM()) + (cheDo == CheDo.NHAN_BAN ? " (bản sao)" : ""));
        JComboBox<String> cboLoai = new JComboBox<>(new String[] { "Phần trăm", "Số tiền" });
        if (mau != null && !laPhanTram(mau))
            cboLoai.setSelectedIndex(1);
        JTextField txtMuc = new JTextField(mau == null ? "10" : soThuong(mau.getMucKM()));
        JTextField txtBatDau = new JTextField(mau == null || mau.getNgayBatDau() == null
                ? DF.format(LocalDate.now())
                : DF.format(mau.getNgayBatDau()));
        JTextField txtKetThuc = new JTextField(mau == null || mau.getNgayKetThuc() == null
                ? DF.format(LocalDate.now().plusMonths(3))
                : DF.format(mau.getNgayKetThuc()));
        JTextField txtDieuKien = new JTextField(mau == null ? "" : nz(mau.getDieuKien()));

        String[] nhan = { "Mã KM", "Tên khuyến mãi", "Loại giảm", "Mức giảm (% hoặc số tiền VNĐ)",
                "Ngày bắt đầu (dd/MM/yyyy)", "Ngày kết thúc (dd/MM/yyyy)", "Điều kiện áp dụng" };
        JComponent[] truong = { txtMa, txtTen, cboLoai, txtMuc, txtBatDau, txtKetThuc, txtDieuKien };
        for (int i = 0; i < nhan.length; i++) {
            gc.gridy = i * 2;
            gc.insets = new Insets(0, 0, 4, 0);
            p.add(new JLabel(nhan[i]), gc);
            gc.gridy = i * 2 + 1;
            gc.insets = new Insets(0, 0, 10, 0);
            p.add(truong[i], gc);
        }

        RoundButton btnLuu = new RoundButton("Lưu", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnHuy = new RoundButton("Hủy", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            try {
                boolean phanTram = cboLoai.getSelectedIndex() == 0;
                boolean ok = service.save(txtMa.getText(), txtTen.getText(), phanTram, txtMuc.getText(),
                        txtBatDau.getText(), txtKetThuc.getText(), txtDieuKien.getText(), tatCa, sua);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Lưu thành công!");
                    dlg.dispose();
                    taiLai(); // nạp lại dữ liệu mới ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(dlg, "Không thể lưu khuyến mãi vào CSDL.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pPhai.setOpaque(false);
        pPhai.add(btnHuy);
        pPhai.add(btnLuu);

        JPanel pBtn = new JPanel(new BorderLayout());
        pBtn.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        pBtn.setBorder(new EmptyBorder(0, 20, 14, 20));
        pBtn.add(pPhai, BorderLayout.EAST);

        if (sua) { // vẫn giữ chức năng xóa của bản cũ, đặt trong form chỉnh sửa
            RoundButton btnXoa = new RoundButton("Xóa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_NGUY_HIEM_CHAM, RangBuocGiaoDien.MAU_CANH_BAO_DO);
            btnXoa.addActionListener(e -> {
                int kq = JOptionPane.showConfirmDialog(dlg,
                        "Xóa khuyến mãi " + mau.getTenKM() + " (" + mau.getMaKM() + ")?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (kq != JOptionPane.YES_OPTION)
                    return;
                if (service.delete(mau.getMaKM())) {
                    service.resume(mau.getMaKM());
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
            for (int c : cotPhai)
                phai.add(c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(phai.contains(c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 11));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class Cell extends DefaultTableCellRenderer {
        private final Color mau;
        private final int kieu, canh;

        Cell(Color mau, int kieu, int canh) {
            this.mau = mau;
            this.kieu = kieu;
            this.canh = canh;
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
            badge.setText(s);
            if (Status.ACTIVE.label.equals(s))
                badge.setColors(RangBuocGiaoDien.MAU_THANH_CONG_NEN, RangBuocGiaoDien.MAU_THANH_CONG_CHU);
            else if (Status.UPCOMING.label.equals(s))
                badge.setColors(RangBuocGiaoDien.MAU_THONG_TIN_NEN, RangBuocGiaoDien.MAU_THONG_TIN_CHU);
            else if (Status.PAUSED.label.equals(s))
                badge.setColors(RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO);
            else
                badge.setColors(RangBuocGiaoDien.MAU_TRUNG_TINH_NEN, RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    private static class Badge extends JLabel {
        private Color bg = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;

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
            setForeground(b ? fg : RangBuocGiaoDien.MAU_CHU_GOI_Y);
            setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean en = isEnabled();
            g2.setColor(en ? fill : RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
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
            this.fill = fill;
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
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
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