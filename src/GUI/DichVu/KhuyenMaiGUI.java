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

    private static final String PHONG_CHU = "Segoe UI";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int COT_TRANG_THAI = 7;

    /** Dùng ở màn hình lập hóa đơn để bỏ qua khuyến mãi đang tạm dừng. */
    public static boolean isTamDung(String maKM) {
        return KhuyenMaiService.laKhuyenMaiTamDung(maKM);
    }

    private enum CheDo {
        THEM, SUA, NHAN_BAN
    }

    // ===== Dữ liệu =====
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
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
    private JTable bangKhuyenMai;
    private DefaultTableModel moHinhBang;

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
        JLabel lblPhu = new JLabel("Dịch vụ / Khuyến mãi");
        lblPhu.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        lblPhu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel lblTieuDe = new JLabel("Khuyến mãi");
        lblTieuDe.setFont(new Font(PHONG_CHU, Font.BOLD, 22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel lblMoTa = new JLabel(
                "Thiết lập ưu đãi theo thời gian, điều kiện áp dụng và theo dõi hiệu lực chương trình.");
        lblMoTa.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblPhu.setAlignmentX(LEFT_ALIGNMENT);
        lblTieuDe.setAlignmentX(LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(LEFT_ALIGNMENT);
        trai.add(lblPhu);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblTieuDe);
        trai.add(Box.createVerticalStrut(2));
        trai.add(lblMoTa);

        RoundButton btnThem = new RoundButton("Thêm khuyến mãi", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
        btnThem.addActionListener(suKien -> moBieuMau(null, CheDo.THEM));
        JPanel phai = new JPanel(new GridBagLayout());
        phai.setOpaque(false);
        phai.add(btnThem);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(trai, BorderLayout.WEST);
        panel.add(phai, BorderLayout.EAST);
        return panel;
    }

    // =====================================================================
    // 4 thẻ thống kê
    // =====================================================================
    private JComponent taoTheThongKe() {
        lblTong = new JLabel();
        lblDang = new JLabel();
        lblSap = new JLabel();
        lblTamHet = new JLabel();

        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theThongKe("%", "Tổng khuyến mãi", lblTong, "Tất cả chương trình"));
        hangThe.add(theThongKe("✓", "Đang áp dụng", lblDang, "Có thể dùng tại quầy"));
        hangThe.add(theThongKe("◷", "Sắp diễn ra", lblSap, "Trong 30 ngày tới"));
        hangThe.add(theThongKe("‖", "Tạm dừng / hết hạn", lblTamHet, "Không tự động áp dụng"));
        return hangThe;
    }

    private JComponent theThongKe(String bieuTuong, String tieuDe, JLabel giaTri, String phu) {
        RoundedPanel the = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.setLayout(new BorderLayout(12, 0));
        the.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel nhanBieuTuong = new JLabel(bieuTuong, SwingConstants.CENTER);
        nhanBieuTuong.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        nhanBieuTuong.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        RoundedPanel hopBieuTuong = new RoundedPanel(10, RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        hopBieuTuong.setLayout(new BorderLayout());
        hopBieuTuong.setPreferredSize(new Dimension(38, 38));
        hopBieuTuong.add(nhanBieuTuong, BorderLayout.CENTER);
        JPanel khungBieuTuong = new JPanel(new GridBagLayout());
        khungBieuTuong.setOpaque(false);
        khungBieuTuong.add(hopBieuTuong);

        JLabel nhanTieuDe = new JLabel(tieuDe);
        nhanTieuDe.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        giaTri.setFont(new Font(PHONG_CHU, Font.BOLD, 22));
        giaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel nhanPhu = new JLabel(phu);
        nhanPhu.setFont(new Font(PHONG_CHU, Font.PLAIN, 11));
        nhanPhu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        FlowLayout bocCuc = new FlowLayout(FlowLayout.LEFT, 8, 0);
        bocCuc.setAlignOnBaseline(true);
        JPanel hang = new JPanel(bocCuc);
        hang.setOpaque(false);
        hang.add(giaTri);
        hang.add(nhanPhu);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        nhanTieuDe.setAlignmentX(LEFT_ALIGNMENT);
        hang.setAlignmentX(LEFT_ALIGNMENT);
        noiDung.add(nhanTieuDe);
        noiDung.add(hang);

        // card.add(iconWrap, BorderLayout.WEST);
        the.add(noiDung, BorderLayout.CENTER);
        return the;
    }

    private void capNhatThongKe() {
        KhuyenMaiService.Summary tomTat = khuyenMaiService.tomTat(tatCa);
        lblTong.setText(String.valueOf(tomTat.tong));
        lblDang.setText(String.valueOf(tomTat.soDangApDung));
        lblSap.setText(String.valueOf(tomTat.soSapDienRa));
        lblTamHet.setText(tomTat.soTamDung + " / " + tomTat.soHetHan);
    }

    // =====================================================================
    // Khung danh sách: bộ lọc + bảng
    // =====================================================================
    private JComponent taoKhungDanhSach() {
        RoundedPanel khung = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        khung.setLayout(new BorderLayout());

        // ---- thanh lọc ----
        txtTim = new SearchField("Tìm kiếm mã khuyến mãi...");
        txtTim.setPreferredSize(new Dimension(300, 34));
        txtTim.getDocument().addDocumentListener(new SimpleDocListener(() -> hienThi(true)));

        cboTrangThai = new JComboBox<>(new String[] {
                "Trạng thái: Tất cả", "Trạng thái: Đang áp dụng", "Trạng thái: Sắp diễn ra",
                "Trạng thái: Tạm dừng", "Trạng thái: Hết hạn" });
        cboTrangThai.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        cboTrangThai.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboTrangThai.setPreferredSize(new Dimension(190, 34));
        cboTrangThai.addActionListener(suKien -> hienThi(true));

        btnThoiGian = new RoundButton("Thời gian: Tất cả", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnThoiGian.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        btnThoiGian.setPreferredSize(new Dimension(230, 34));
        btnThoiGian.addActionListener(suKien -> chonThoiGian());

        JPanel locTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locTrai.setOpaque(false);
        locTrai.add(txtTim);
        locTrai.add(cboTrangThai);
        locTrai.add(btnThoiGian);

        RoundButton btnSua = new RoundButton("Sửa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        RoundButton btnNhanBan = new RoundButton("Nhân bản", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnTamNgung = new RoundButton("Tạm ngưng", RangBuocGiaoDien.MAU_CANH_BAO_DO, RangBuocGiaoDien.MAU_CANH_BAO_DO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnXuat = new RoundButton("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);

        btnSua.addActionListener(suKien -> {
            int dongChon = bangKhuyenMai.getSelectedRow();
            if (dongChon < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trong danh sách để sửa.", "Chưa chọn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int dongMoHinh = bangKhuyenMai.convertRowIndexToModel(dongChon);
            if (dongMoHinh >= 0 && dongMoHinh < dongHienTai.size())
                moBieuMau(dongHienTai.get(dongMoHinh), CheDo.SUA);
        });

        btnNhanBan.addActionListener(suKien -> {
            int dongChon = bangKhuyenMai.getSelectedRow();
            if (dongChon < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trong danh sách để nhân bản.",
                        "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int dongMoHinh = bangKhuyenMai.convertRowIndexToModel(dongChon);
            if (dongMoHinh >= 0 && dongMoHinh < dongHienTai.size())
                moBieuMau(dongHienTai.get(dongMoHinh), CheDo.NHAN_BAN);
        });

        btnTamNgung.addActionListener(suKien -> {
            int dongChon = bangKhuyenMai.getSelectedRow();
            if (dongChon < 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một khuyến mãi trong danh sách để tạm ngưng/kích hoạt.", "Chưa chọn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int dongMoHinh = bangKhuyenMai.convertRowIndexToModel(dongChon);
            if (dongMoHinh >= 0 && dongMoHinh < dongHienTai.size())
                doiTamDung(dongHienTai.get(dongMoHinh));
        });

        btnXuat.addActionListener(suKien -> xuatDanhSach());

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
        moHinhBang = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int chiSoDong, int chiSoCot) {
                return false;
            }
        };
        bangKhuyenMai = new JTable(moHinhBang);
        bangKhuyenMai.setRowHeight(42);
        bangKhuyenMai.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        bangKhuyenMai.setShowVerticalLines(false);
        bangKhuyenMai.setShowHorizontalLines(true);
        bangKhuyenMai.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        bangKhuyenMai.setIntercellSpacing(new Dimension(0, 1));
        bangKhuyenMai.setSelectionBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        bangKhuyenMai.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        bangKhuyenMai.setFillsViewportHeight(true);
        bangKhuyenMai.setFocusable(false);
        bangKhuyenMai.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader tieuDeBang = bangKhuyenMai.getTableHeader();
        tieuDeBang.setReorderingAllowed(false);
        tieuDeBang.setPreferredSize(new Dimension(0, 36));
        tieuDeBang.setDefaultRenderer(new HeaderRenderer(2));

        bangKhuyenMai.getColumnModel().getColumn(0).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(1).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(2).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.RIGHT));
        bangKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(5).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(6).setCellRenderer(new Cell(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        bangKhuyenMai.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        int[] rong = { 95, 160, 95, 80, 95, 95, 210, 110 };
        for (int chiSo = 0; chiSo < rong.length; chiSo++) {
            bangKhuyenMai.getColumnModel().getColumn(chiSo).setPreferredWidth(rong[chiSo]);
        }

        JScrollPane cuonBang = new JScrollPane(bangKhuyenMai);
        cuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        cuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cuonBang.getVerticalScrollBar().setUnitIncrement(16);

        // ---- chân bảng ----
        lblHienThi = new JLabel();
        lblHienThi.setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
        lblHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel ghiChu = new JLabel(
                "Trạng thái được cập nhật tự động theo thời gian; chương trình tạm dừng cần kích hoạt thủ công");
        ghiChu.setFont(new Font(PHONG_CHU, Font.PLAIN, 11));
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

        khung.add(loc, BorderLayout.NORTH);
        khung.add(cuonBang, BorderLayout.CENTER);
        khung.add(chan, BorderLayout.SOUTH);
        return khung;
    }

    // =====================================================================
    // Nạp dữ liệu và lọc
    // =====================================================================
    /** Đọc lại dữ liệu từ CSDL rồi hiển thị. */
    private void taiLai() {
        List<KhuyenMai_Entity> danhSach = khuyenMaiService.taiDanhSach();
        tatCa = new ArrayList<>(danhSach);
        hienThi(false);
    }

    private boolean laPhanTram(KhuyenMai_Entity km) {
        return khuyenMaiService.laPhanTram(km);
    }

    private static String soThuong(double duLieu) {
        return KhuyenMaiService.dinhDangSo(duLieu);
    }

    private String hienMuc(KhuyenMai_Entity km) {
        return khuyenMaiService.dinhDangMuc(km);
    }

    private void apDungLoc() {
        locRa = khuyenMaiService.loc(tatCa, txtTim.getText(), cboTrangThai.getSelectedIndex(), tuNgay, denNgay);
    }

    /** Lọc lại, cập nhật thống kê và hiển thị toàn bộ kết quả. */
    private void hienThi(boolean veTrang1) {
        if (moHinhBang == null)
            return;
        dangNap = true;
        apDungLoc();
        moHinhBang.setRowCount(0);
        dongHienTai.clear();
        for (int chiSo = 0; chiSo < locRa.size(); chiSo++) {
            KhuyenMai_Entity km = locRa.get(chiSo);
            dongHienTai.add(km);
            moHinhBang.addRow(new Object[] {
                    km.getMaKM(),
                    km.getTenKM(),
                    hienMuc(km),
                    laPhanTram(km) ? "Phần trăm" : "Số tiền",
                    km.getNgayBatDau() != null ? DINH_DANG_NGAY.format(km.getNgayBatDau()) : "",
                    km.getNgayKetThuc() != null ? DINH_DANG_NGAY.format(km.getNgayKetThuc()) : "",
                    khongNull(km.getDieuKien()),
                    khuyenMaiService.trangThai(km).nhan,
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
        JTextField tu = new JTextField(tuNgay == null ? "" : tuNgay.format(DINH_DANG_NGAY), 10);
        JTextField den = new JTextField(denNgay == null ? "" : denNgay.format(DINH_DANG_NGAY), 10);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Từ ngày (dd/MM/yyyy)"));
        panel.add(tu);
        panel.add(new JLabel("Đến ngày (dd/MM/yyyy)"));
        panel.add(den);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Để trống = không giới hạn"));
        int luaChon = JOptionPane.showConfirmDialog(this, panel, "Lọc theo thời gian hiệu lực",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (luaChon != JOptionPane.OK_OPTION)
            return;
        try {
            KhuyenMaiService.DateRange khoangNgay = khuyenMaiService.phanTichKhoangNgay(tu.getText(), den.getText());
            tuNgay = khoangNgay.tuNgay;
            denNgay = khoangNgay.denNgay;
        } catch (IllegalArgumentException loi) {
            JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String chuoiHienThi;
        if (tuNgay == null && denNgay == null)
            chuoiHienThi = "Thời gian: Tất cả";
        else if (tuNgay != null && denNgay != null)
            chuoiHienThi = "Thời gian: " + tuNgay.format(DINH_DANG_NGAY) + " – " + denNgay.format(DINH_DANG_NGAY);
        else if (tuNgay != null)
            chuoiHienThi = "Thời gian: từ " + tuNgay.format(DINH_DANG_NGAY);
        else
            chuoiHienThi = "Thời gian: đến " + denNgay.format(DINH_DANG_NGAY);
        btnThoiGian.setText(chuoiHienThi);
        hienThi(true);
    }

    // =====================================================================
    // Thao tác trên dòng
    // =====================================================================

    private void doiTamDung(KhuyenMai_Entity km) {
        Status trangThai = khuyenMaiService.trangThai(km);
        if (trangThai == Status.HET_HAN)
            return;
        if (trangThai == Status.TAM_DUNG) {
            khuyenMaiService.tiepTuc(km.getMaKM());
        } else {
            int kq = JOptionPane.showConfirmDialog(this,
                    "Tạm dừng khuyến mãi " + km.getTenKM() + " (" + km.getMaKM() + ")?\n"
                            + "Chương trình sẽ không được áp dụng cho đến khi kích hoạt lại.",
                    "Tạm dừng khuyến mãi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (kq != JOptionPane.YES_OPTION)
                return;
            khuyenMaiService.tamDung(km.getMaKM());
        }
        hienThi(false);
    }

    // =====================================================================
    // Xuất danh sách CSV
    // =====================================================================
    private void xuatDanhSach() {
        JFileChooser hopChonTep = new JFileChooser();
        hopChonTep.setSelectedFile(new File("danh-sach-khuyen-mai.csv"));
        if (hopChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        try {
            Files.write(hopChonTep.getSelectedFile().toPath(), khuyenMaiService.taoCsv(locRa).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + locRa.size() + " khuyến mãi ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException loi) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + loi.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Form thêm / sửa / nhân bản
    // =====================================================================
    private void moBieuMau(KhuyenMai_Entity mau, CheDo cheDo) {
        final boolean sua = cheDo == CheDo.SUA;
        String tieuDe = sua ? "Chỉnh sửa khuyến mãi"
                : (cheDo == CheDo.NHAN_BAN ? "Nhân bản khuyến mãi" : "Thêm khuyến mãi");

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog hopThoai = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        hopThoai.setSize(460, 540);
        hopThoai.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        GridBagConstraints rangBuoc = new GridBagConstraints();
        rangBuoc.fill = GridBagConstraints.HORIZONTAL;
        rangBuoc.weightx = 1;

        JTextField txtMa = new JTextField(sua ? mau.getMaKM() : "KM" + System.currentTimeMillis() % 100000);
        if (sua)
            txtMa.setEditable(false);
        JTextField txtTen = new JTextField(mau == null ? ""
                : khongNull(mau.getTenKM()) + (cheDo == CheDo.NHAN_BAN ? " (bản sao)" : ""));
        JComboBox<String> cboLoai = new JComboBox<>(new String[] { "Phần trăm", "Số tiền" });
        if (mau != null && !laPhanTram(mau))
            cboLoai.setSelectedIndex(1);
        JTextField txtMuc = new JTextField(mau == null ? "10" : soThuong(mau.getMucKM()));
        JTextField txtBatDau = new JTextField(mau == null || mau.getNgayBatDau() == null
                ? DINH_DANG_NGAY.format(LocalDate.now())
                : DINH_DANG_NGAY.format(mau.getNgayBatDau()));
        JTextField txtKetThuc = new JTextField(mau == null || mau.getNgayKetThuc() == null
                ? DINH_DANG_NGAY.format(LocalDate.now().plusMonths(3))
                : DINH_DANG_NGAY.format(mau.getNgayKetThuc()));
        JTextField txtDieuKien = new JTextField(mau == null ? "" : khongNull(mau.getDieuKien()));

        String[] nhan = { "Mã KM", "Tên khuyến mãi", "Loại giảm", "Mức giảm (% hoặc số tiền VNĐ)",
                "Ngày bắt đầu (dd/MM/yyyy)", "Ngày kết thúc (dd/MM/yyyy)", "Điều kiện áp dụng" };
        JComponent[] truong = { txtMa, txtTen, cboLoai, txtMuc, txtBatDau, txtKetThuc, txtDieuKien };
        for (int chiSo = 0; chiSo < nhan.length; chiSo++) {
            rangBuoc.gridy = chiSo * 2;
            rangBuoc.insets = new Insets(0, 0, 4, 0);
            panel.add(new JLabel(nhan[chiSo]), rangBuoc);
            rangBuoc.gridy = chiSo * 2 + 1;
            rangBuoc.insets = new Insets(0, 0, 10, 0);
            panel.add(truong[chiSo], rangBuoc);
        }

        RoundButton btnLuu = new RoundButton("Lưu", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_THE);
        RoundButton btnHuy = new RoundButton("Hủy", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH);
        btnHuy.addActionListener(suKien -> hopThoai.dispose());
        btnLuu.addActionListener(suKien -> {
            try {
                boolean phanTram = cboLoai.getSelectedIndex() == 0;
                boolean daLuu = khuyenMaiService.luu(txtMa.getText(), txtTen.getText(), phanTram, txtMuc.getText(),
                        txtBatDau.getText(), txtKetThuc.getText(), txtDieuKien.getText(), tatCa, sua);
                if (daLuu) {
                    JOptionPane.showMessageDialog(hopThoai, "Lưu thành công!");
                    hopThoai.dispose();
                    taiLai(); // nạp lại dữ liệu mới ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(hopThoai, "Không thể lưu khuyến mãi vào CSDL.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException loi) {
                JOptionPane.showMessageDialog(hopThoai, loi.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnHuy);
        panelPhai.add(btnLuu);

        JPanel panelNut = new JPanel(new BorderLayout());
        panelNut.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        panelNut.setBorder(new EmptyBorder(0, 20, 14, 20));
        panelNut.add(panelPhai, BorderLayout.EAST);

        if (sua) { // vẫn giữ chức năng xóa của bản cũ, đặt trong form chỉnh sửa
            RoundButton btnXoa = new RoundButton("Xóa", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_NGUY_HIEM_CHAM, RangBuocGiaoDien.MAU_CANH_BAO_DO);
            btnXoa.addActionListener(suKien -> {
                int kq = JOptionPane.showConfirmDialog(hopThoai,
                        "Xóa khuyến mãi " + mau.getTenKM() + " (" + mau.getMaKM() + ")?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (kq != JOptionPane.YES_OPTION)
                    return;
                if (khuyenMaiService.xoa(mau.getMaKM())) {
                    khuyenMaiService.tiepTuc(mau.getMaKM());
                    JOptionPane.showMessageDialog(hopThoai, "Xóa thành công!");
                    hopThoai.dispose();
                    taiLai();
                } else {
                    JOptionPane.showMessageDialog(hopThoai, "Không thể xóa khuyến mãi (có thể đang được sử dụng).",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            panelNut.add(btnXoa, BorderLayout.WEST);
        }

        hopThoai.setLayout(new BorderLayout());
        hopThoai.add(panel, BorderLayout.CENTER);
        hopThoai.add(panelNut, BorderLayout.SOUTH);
        hopThoai.setVisible(true);
    }

    private static String khongNull(String chuoi) {
        return chuoi == null ? "" : chuoi;
    }

    // =====================================================================
    // Renderers & component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final Set<Integer> phai = new HashSet<>();

        HeaderRenderer(int... cotPhai) {
            for (int chiSoCot : cotPhai)
                phai.add(chiSoCot);
        }

        @Override
        public Component getTableCellRendererComponent(JTable bangNguon, Object duLieu, boolean dangChon, boolean coTieuDiem, int dong, int chiSoCot) {
            super.getTableCellRendererComponent(bangNguon, duLieu, false, false, dong, chiSoCot);
            setHorizontalAlignment(phai.contains(chiSoCot) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(PHONG_CHU, Font.BOLD, 11));
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
        public Component getTableCellRendererComponent(JTable bangNguon, Object duLieu, boolean dangChon, boolean coTieuDiem, int dong, int chiSoCot) {
            super.getTableCellRendererComponent(bangNguon, duLieu, dangChon, false, dong, chiSoCot);
            setHorizontalAlignment(canh);
            setFont(new Font(PHONG_CHU, kieu, 12));
            setForeground(mau);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }
    }

    /** Cột trạng thái: nhãn bo tròn có chấm màu. */
    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel khungTrangThai = new JPanel(new GridBagLayout());
        private final Badge nhanTrangThai = new Badge();

        StatusRenderer() {
            GridBagConstraints rangBuoc = new GridBagConstraints();
            rangBuoc.anchor = GridBagConstraints.WEST;
            rangBuoc.weightx = 1;
            rangBuoc.insets = new Insets(0, 8, 0, 0);
            khungTrangThai.add(nhanTrangThai, rangBuoc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable bangNguon, Object duLieu, boolean dangChon, boolean coTieuDiem, int dong, int chiSoCot) {
            String chuoiTrangThai = duLieu == null ? "" : duLieu.toString();
            nhanTrangThai.setText(chuoiTrangThai);
            if (Status.DANG_AP_DUNG.nhan.equals(chuoiTrangThai))
                nhanTrangThai.datMau(RangBuocGiaoDien.MAU_THANH_CONG_NEN, RangBuocGiaoDien.MAU_THANH_CONG_CHU);
            else if (Status.SAP_DIEN_RA.nhan.equals(chuoiTrangThai))
                nhanTrangThai.datMau(RangBuocGiaoDien.MAU_THONG_TIN_NEN, RangBuocGiaoDien.MAU_THONG_TIN_CHU);
            else if (Status.TAM_DUNG.nhan.equals(chuoiTrangThai))
                nhanTrangThai.datMau(RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO);
            else
                nhanTrangThai.datMau(RangBuocGiaoDien.MAU_TRUNG_TINH_NEN, RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            khungTrangThai.setOpaque(true);
            khungTrangThai.setBackground(dangChon ? bangNguon.getSelectionBackground() : bangNguon.getBackground());
            return khungTrangThai;
        }
    }

    private static class Badge extends JLabel {
        private Color mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;

        Badge() {
            setOpaque(false);
            setFont(new Font(PHONG_CHU, Font.BOLD, 11));
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }

        void datMau(Color mauNen, Color mauChu) {
            this.mauNen = mauNen;
            setForeground(mauChu);
        }

        @Override
        protected void paintComponent(Graphics doHoa) {
            Graphics2D doHoa2D = (Graphics2D) doHoa.create();
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            doHoa2D.setColor(mauNen);
            doHoa2D.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            doHoa2D.dispose();
            super.paintComponent(doHoa);
        }
    }

    /** Nút bo góc. */
    private static class RoundButton extends JButton {
        private final Color mauTo, mauVien, mauChu;

        RoundButton(String noiDung, Color mauTo, Color mauVien, Color mauChu) {
            super(noiDung);
            this.mauTo = mauTo;
            this.mauVien = mauVien;
            this.mauChu = mauChu;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font(PHONG_CHU, Font.BOLD, 12));
            setForeground(mauChu);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        @Override
        public void setEnabled(boolean bat) {
            super.setEnabled(bat);
            setForeground(bat ? mauChu : RangBuocGiaoDien.MAU_CHU_GOI_Y);
            setCursor(Cursor.getPredefinedCursor(bat ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics doHoa) {
            Graphics2D doHoa2D = (Graphics2D) doHoa.create();
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean kichHoat = isEnabled();
            doHoa2D.setColor(kichHoat ? mauTo : RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
            doHoa2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            doHoa2D.setColor(kichHoat ? mauVien : RangBuocGiaoDien.MAU_DUONG_VIEN);
            doHoa2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            doHoa2D.dispose();
            super.paintComponent(doHoa);
        }
    }

    /** Panel bo góc có viền. */
    private static class RoundedPanel extends JPanel {
        private final int banKinhGoc;
        private final Color mauTo, mauVien;

        RoundedPanel(int banKinhGoc, Color mauTo, Color mauVien) {
            this.banKinhGoc = banKinhGoc;
            this.mauTo = mauTo;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics doHoa) {
            Graphics2D doHoa2D = (Graphics2D) doHoa.create();
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            doHoa2D.setColor(mauTo);
            doHoa2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinhGoc, banKinhGoc);
            doHoa2D.setColor(mauVien);
            doHoa2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinhGoc, banKinhGoc);
            doHoa2D.dispose();
            super.paintComponent(doHoa);
        }
    }

    /** Ô tìm kiếm có icon kính lúp và chữ gợi ý. */
    private static class SearchField extends JTextField {
        private final String goiY;

        SearchField(String goiY) {
            this.goiY = goiY;
            setFont(new Font(PHONG_CHU, Font.PLAIN, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 32, 0, 8)));
        }

        @Override
        protected void paintComponent(Graphics doHoa) {
            super.paintComponent(doHoa);
            Graphics2D doHoa2D = (Graphics2D) doHoa.create();
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int tamY = getHeight() / 2;
            doHoa2D.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            doHoa2D.setStroke(new BasicStroke(1.4f));
            doHoa2D.draw(new Ellipse2D.Double(12, tamY - 6, 8, 8));
            doHoa2D.drawLine(19, tamY + 1, 23, tamY + 5);
            if (getText().isEmpty()) {
                doHoa2D.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                doHoa2D.setFont(getFont());
                FontMetrics soDoChu = doHoa2D.getFontMetrics();
                doHoa2D.drawString(goiY, 32, tamY + (soDoChu.getAscent() - soDoChu.getDescent()) / 2);
            }
            doHoa2D.dispose();
        }
    }

    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable tacVu;

        SimpleDocListener(Runnable tacVu) {
            this.tacVu = tacVu;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent suKien) {
            tacVu.run();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent suKien) {
            tacVu.run();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent suKien) {
            tacVu.run();
        }
    }
}