package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import GUI.ThuCung.HoSoThuCungGUI.NutBoTron;
import GUI.ThuCung.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.ThuCung.HoSoThuCungGUI.PanelBoTron;
import dao.BacSi_DAO;
import dao.ChiTietDonThuoc_DAO;
import dao.DonThuoc_DAO;
import dao.HoSoSucKhoe_DAO;
import dao.ThuCung_DAO;
import entity.BacSi_entity;
import entity.ChiTietDonThuoc_entity;
import entity.DonThuoc_entity;
import entity.HoSoSucKhoe_entity;
import entity.ThuCung_entity;
import entity.Thuoc_entity;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Class DonThuocGUI: Giao diện "Đơn thuốc" (thay thế giao diện Loại thú cưng).
 * Quản lý đơn thuốc và các chi tiết thuốc thông qua DAO.
 * Các trường hiển thị dựa theo bảng DonThuoc (kết hợp ThuCung, BacSyThuY, ChiTietDonThuoc).
 * Dùng lại các thành phần PanelBoTron, NutBoTron, OTimKiemGoiY của HoSoThuCungGUI.
 */
public class DonThuocGUI extends JPanel {

    // =========================================================================
    // KHAI BÁO TOÀN BỘ BIẾN
    // =========================================================================

    // --- Hằng số kích thước ---
    private static final int KHOANG_CACH_LE = 20;
    private static final int KHOANG_CACH_THE = 16;
    private static final int BAN_KINH_THE = 12;
    private static final int CHIEU_CAO_THE_THONG_KE = 80;
    private static final int CHIEU_CAO_O_NHAP = 38;
    private static final int CHIEU_CAO_THANH_PHAN_TRANG = 56;
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final String[] tieuDeCot = { "Mã đơn thuốc", "Thú cưng", "Mã hồ sơ", "Bác sĩ kê đơn", "Ngày kê đơn",
            "Số loại thuốc", "Ghi chú", "Trạng thái" };
    private final String[] dsTrangThaiLoc = { "Trạng thái: Tất cả", "Chờ lấy thuốc", "Đã cấp thuốc" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };
    private final DonThuoc_DAO donThuocDAO = new DonThuoc_DAO();
    private final ChiTietDonThuoc_DAO chiTietDAO = new ChiTietDonThuoc_DAO();
    private final HoSoSucKhoe_DAO hoSoSucKhoeDAO = new HoSoSucKhoe_DAO();
    private final ThuCung_DAO thuCungDAO = new ThuCung_DAO();
    private final BacSi_DAO bacSiDAO = new BacSi_DAO();
    private List<DonThuoc_entity> danhSachDonThuoc = new ArrayList<>();
    private LocalDate tuNgay;
    private LocalDate denNgay;

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBoTron nutKeDonThuoc;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongDon;
    private JLabel nhanSoDaCapThuoc;
    private JLabel nhanSoChoLayThuoc;
    private JLabel nhanTongSoThuoc;

    // --- Phần thẻ chứa bảng ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboThoiGian;
    private JButton nutChonKhoangNgay;
    private JButton nutXemChiTiet;
    private JButton nutChinhSua;
    private JButton nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangDonThuoc;
    private JScrollPane thanhCuonBang;

    // --- Phần phân trang ---
    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;
    private JLabel nhanSoDongTrang;
    private JComboBox<String> cboSoDongTrang;
    private NutBoTron nutTrangTruoc;
    private NutBoTron nutTrang1;
    private NutBoTron nutTrang2;
    private NutBoTron nutTrangRutGon;
    private NutBoTron nutTrangCuoi;
    private NutBoTron nutTrangSau;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public DonThuocGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        loadDataToTable();
    }

    // =========================================================================
    // KHỞI TẠO BỐ CỤC CHÍNH
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        khoiTaoTieuDe();
        khoiTaoThongKe();
        khoiTaoTheBang();

        add(panelTieuDe, BorderLayout.NORTH);

        JPanel panelThan = new JPanel(new BorderLayout(0, KHOANG_CACH_THE));
        panelThan.setOpaque(false);
        panelThan.add(panelThongKe, BorderLayout.NORTH);
        panelThan.add(panelTheBang, BorderLayout.CENTER);
        add(panelThan, BorderLayout.CENTER);
    }

    // =========================================================================
    // PHẦN 1: TIÊU ĐỀ TRANG
    // =========================================================================
    private void khoiTaoTieuDe() {
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);
        panelTieuDe.setBorder(new EmptyBorder(0, 0, KHOANG_CACH_THE, 0));

        nhanDuongDan = new JLabel("Thú cưng / Đơn thuốc");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Đơn thuốc");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Quản lý đơn thuốc do bác sĩ kê cho thú cưng sau mỗi lượt khám.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel panelChu = new JPanel();
        panelChu.setOpaque(false);
        panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        panelChu.add(nhanDuongDan);
        panelChu.add(Box.createVerticalStrut(4));
        panelChu.add(nhanTieuDe);
        panelChu.add(Box.createVerticalStrut(2));
        panelChu.add(nhanMoTa);

        nutKeDonThuoc = new NutBoTron("+  Kê đơn thuốc", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutKeDonThuoc.setPreferredSize(new Dimension(146, 40));

        JPanel panelNut = new JPanel(new GridBagLayout());
        panelNut.setOpaque(false);
        panelNut.add(nutKeDonThuoc);

        panelTieuDe.add(panelChu, BorderLayout.WEST);
        panelTieuDe.add(panelNut, BorderLayout.EAST);
    }

    // =========================================================================
    // PHẦN 2: 4 THẺ THỐNG KÊ
    // =========================================================================
    private void khoiTaoThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH_THE, 0));
        panelThongKe.setOpaque(false);
        panelThongKe.setPreferredSize(new Dimension(0, CHIEU_CAO_THE_THONG_KE));

        nhanSoTongDon = new JLabel("0");
        nhanSoDaCapThuoc = new JLabel("0");
        nhanSoChoLayThuoc = new JLabel("0");
        nhanTongSoThuoc = new JLabel("0");

        panelThongKe.add(taoTheThongKe("ĐT", "Tổng đơn thuốc", nhanSoTongDon, "Đơn đã kê"));
        panelThongKe.add(taoTheThongKe("CT", "Đã cấp thuốc", nhanSoDaCapThuoc, "Đã giao thuốc"));
        panelThongKe.add(taoTheThongKe("CL", "Chờ lấy thuốc", nhanSoChoLayThuoc, "Cần xử lý"));
        panelThongKe.add(taoTheThongKe("TH", "Tổng loại thuốc", nhanTongSoThuoc, "Trong các đơn thuốc"));
    }

    private PanelBoTron taoTheThongKe(String bieuTuong, String tieuDe, JLabel nhanSo, String moTa) {
        PanelBoTron the = new PanelBoTron(new BorderLayout(14, 0), BAN_KINH_THE,
                RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.setBorder(new EmptyBorder(0, 18, 0, 12));

        PanelBoTron khungBieuTuong = new PanelBoTron(new GridBagLayout(), 10,
                RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        khungBieuTuong.setPreferredSize(new Dimension(40, 40));
        JLabel nhanBieuTuong = new JLabel(bieuTuong);
        nhanBieuTuong.setFont(RangBuocGiaoDien.fontDam(13));
        nhanBieuTuong.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        khungBieuTuong.add(nhanBieuTuong);

        JPanel boBieuTuong = new JPanel(new GridBagLayout());
        boBieuTuong.setOpaque(false);
        boBieuTuong.add(khungBieuTuong);

        JLabel nhanTieuDeThe = new JLabel(tieuDe);
        nhanTieuDeThe.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanTieuDeThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanSo.setFont(RangBuocGiaoDien.fontDam(26));
        nhanSo.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel nhanMoTaThe = new JLabel(moTa);
        nhanMoTaThe.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTaThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel dongSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dongSo.setOpaque(false);
        dongSo.add(nhanSo);
        dongSo.add(nhanMoTaThe);

        JPanel panelChu = new JPanel();
        panelChu.setOpaque(false);
        panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        nhanTieuDeThe.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelChu.add(Box.createVerticalGlue());
        panelChu.add(nhanTieuDeThe);
        panelChu.add(dongSo);
        panelChu.add(Box.createVerticalGlue());

        the.add(boBieuTuong, BorderLayout.WEST);
        the.add(panelChu, BorderLayout.CENTER);
        return the;
    }

    // =========================================================================
    // PHẦN 3: THẺ CHỨA THANH CÔNG CỤ + BẢNG + PHÂN TRANG
    // =========================================================================
    private void khoiTaoTheBang() {
        panelTheBang = new PanelBoTron(new BorderLayout(), BAN_KINH_THE,
                RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelTheBang.setBorder(new EmptyBorder(1, 1, 1, 1));

        khoiTaoThanhCongCu();
        khoiTaoBang();
        khoiTaoPhanTrang();

        panelTheBang.add(panelCongCu, BorderLayout.NORTH);
        panelTheBang.add(thanhCuonBang, BorderLayout.CENTER);
        panelTheBang.add(panelPhanTrang, BorderLayout.SOUTH);
    }

    private void khoiTaoThanhCongCu() {
        panelCongCu = new JPanel();
        panelCongCu.setOpaque(false);
        panelCongCu.setLayout(new BoxLayout(panelCongCu, BoxLayout.X_AXIS));
        panelCongCu.setBorder(new EmptyBorder(16, 20, 16, 20));

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã đơn, thú cưng, bác sĩ...");
        txtTimKiem.setPreferredSize(new Dimension(340, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(340, CHIEU_CAO_O_NHAP));

        cboTrangThai = taoOChon(dsTrangThaiLoc, 180);
        cboThoiGian = taoOChon(dsThoiGianLoc, 180);
        nutChonKhoangNgay = taoNutLienKet("Khoảng ngày", RangBuocGiaoDien.MAU_CHU_DAO);

         nutXemChiTiet = taoNutLienKet("Xem chi tiết", RangBuocGiaoDien.MAU_CHU_DAO);
         nutChinhSua = taoNutLienKet("Chỉnh sửa", RangBuocGiaoDien.MAU_CHU_DAO);
         nutXoa = taoNutLienKet("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);

         nutXemChiTiet.setEnabled(false);
         nutChinhSua.setEnabled(false);
         nutXoa.setEnabled(false);

        nutXuatDanhSach = new NutBoTron("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(154, CHIEU_CAO_O_NHAP));
        nutXuatDanhSach.setMaximumSize(new Dimension(154, CHIEU_CAO_O_NHAP));

        panelCongCu.add(txtTimKiem);
        panelCongCu.add(Box.createHorizontalStrut(12));
        panelCongCu.add(cboTrangThai);
        panelCongCu.add(Box.createHorizontalStrut(12));
        panelCongCu.add(cboThoiGian);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutChonKhoangNgay);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutXemChiTiet);
        panelCongCu.add(Box.createHorizontalStrut(14));
        panelCongCu.add(nutChinhSua);
        panelCongCu.add(Box.createHorizontalStrut(14));
        panelCongCu.add(nutXoa);
        panelCongCu.add(Box.createHorizontalStrut(24));
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
         bangDonThuoc = new TableMau(tieuDeCot, new Object[0][tieuDeCot.length]);
         bangDonThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

         int cotTrangThai = tieuDeCot.length - 1;
         int[] doRongCot = { 100, 120, 80, 150, 140, 100, 160, 130 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangDonThuoc.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
         }
         bangDonThuoc.getColumnModel().getColumn(cotTrangThai).setCellRenderer(new OTrangThai(bangDonThuoc));

         bangDonThuoc.getSelectionModel().addListSelectionListener(e -> {
             if (!e.getValueIsAdjusting()) {
                 capNhatTrangThaiNutHanhDong();
             }
         });
        thanhCuonBang = new JScrollPane(bangDonThuoc);
        thanhCuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        thanhCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        thanhCuonBang.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void khoiTaoPhanTrang() {
        panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setPreferredSize(new Dimension(0, CHIEU_CAO_THANH_PHAN_TRANG));
        panelPhanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(0, 20, 0, 20)));

        nhanThongTinTrang = new JLabel("Hiển thị 1–8 trong 15 đơn thuốc");
        nhanThongTinTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanSoDongTrang = new JLabel("Số dòng / trang");
        nhanSoDongTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanSoDongTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        cboSoDongTrang = new JComboBox<>(dsSoDongTrang);
        cboSoDongTrang.setFont(RangBuocGiaoDien.fontThuong(13));
        cboSoDongTrang.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboSoDongTrang.setPreferredSize(new Dimension(64, 34));

        nutTrangTruoc = taoNutTrang("<", false);
        nutTrang1 = taoNutTrang("1", true);
        nutTrang2 = taoNutTrang("2", false);
        nutTrangRutGon = taoNutTrang("...", false);
        nutTrangCuoi = taoNutTrang("2", false);
        nutTrangSau = taoNutTrang(">", false);

        JPanel panelBenPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 11));
        panelBenPhai.setOpaque(false);
        panelBenPhai.add(nhanSoDongTrang);
        panelBenPhai.add(cboSoDongTrang);
        panelBenPhai.add(Box.createHorizontalStrut(10));
        panelBenPhai.add(nutTrangTruoc);
        panelBenPhai.add(nutTrang1);
        panelBenPhai.add(nutTrang2);
        panelBenPhai.add(nutTrangSau);

        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
        panelPhanTrang.add(panelBenPhai, BorderLayout.EAST);
    }

    // =========================================================================
    // CÀI ĐẶT SỰ KIỆN CHO CỤM NÚT THAO TÁC
    // =========================================================================
    private void caiDatSuKien() {
        nutKeDonThuoc.addActionListener(e -> moFormDonThuoc(null));
        nutXemChiTiet.addActionListener(e -> xemDongDaChon());
        nutChinhSua.addActionListener(e -> chinhSuaDongDaChon());
        nutXoa.addActionListener(e -> xoaDongDaChon());
        nutXuatDanhSach.addActionListener(e -> xuatDanhSach());
        nutChonKhoangNgay.addActionListener(e -> chonKhoangNgay());
        cboTrangThai.addActionListener(e -> locDuLieu());
        cboThoiGian.addActionListener(e -> locDuLieu());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
        });
        bangDonThuoc.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangDonThuoc.getSelectedRowCount() == 1) {
                    xemDongDaChon();
                }
            }
        });
        capNhatTrangThaiNutHanhDong();
    }

    public void loadDataToTable() {
        try {
            List<DonThuoc_entity> ketQua = donThuocDAO.getAllDonThuoc();
            danhSachDonThuoc = new ArrayList<>();
            for (DonThuoc_entity don : ketQua) {
                if ("Chờ lấy thuốc".equals(don.getTrangThai()) || "Đã cấp thuốc".equals(don.getTrangThai())) {
                    danhSachDonThuoc.add(don);
                }
            }
            locDuLieu();
            capNhatThongKe();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách đơn thuốc: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDuLieu() {
        String keyword = txtTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        String status = String.valueOf(cboTrangThai.getSelectedItem());
        String time = String.valueOf(cboThoiGian.getSelectedItem());
        LocalDate tu = tuNgay;
        LocalDate den = denNgay;
        LocalDate homNay = LocalDate.now();
        if ("Hôm nay".equals(time)) {
            tu = homNay;
            den = homNay;
        } else if ("7 ngày qua".equals(time)) {
            tu = homNay.minusDays(6);
            den = homNay;
        } else if ("Tháng này".equals(time)) {
            tu = homNay.withDayOfMonth(1);
            den = homNay;
        }

        DefaultTableModel model = (DefaultTableModel) bangDonThuoc.getModel();
        model.setRowCount(0);
        for (DonThuoc_entity don : danhSachDonThuoc) {
            LocalDate ngay = don.getNgayKeDon() == null ? null : don.getNgayKeDon().toLocalDate();
            boolean matchedKeyword = keyword.isEmpty()
                    || value(don.getMaDonThuoc()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getMaHSSK()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getTenThuCung()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getTenBacSi()).toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchedStatus = status == null || status.startsWith("Trạng thái:")
                    || status.equals(don.getTrangThai());
            boolean matchedDate = (tu == null || (ngay != null && !ngay.isBefore(tu)))
                    && (den == null || (ngay != null && !ngay.isAfter(den)));
            if (matchedKeyword && matchedStatus && matchedDate) {
                model.addRow(new Object[] { don.getMaDonThuoc(),
                        value(don.getTenThuCung()) + " (" + don.getMaTC() + ")", don.getMaHSSK(),
                        don.getTenBacSi(), don.getNgayKeDon() == null ? "" : DINH_DANG_NGAY_GIO.format(don.getNgayKeDon()),
                        don.getSoLoaiThuoc(), value(don.getGhiChu()), don.getTrangThai() });
            }
        }
        bangDonThuoc.clearSelection();
        capNhatTrangThaiNutHanhDong();
        nhanThongTinTrang.setText("Hiển thị " + model.getRowCount() + " trong " + model.getRowCount() + " đơn thuốc");
    }

    private void capNhatThongKe() {
        int daCap = 0;
        int choLay = 0;
        int loaiThuoc = 0;
        for (DonThuoc_entity don : danhSachDonThuoc) {
            if ("Đã cấp thuốc".equals(don.getTrangThai())) {
                daCap++;
            } else if ("Chờ lấy thuốc".equals(don.getTrangThai())) {
                choLay++;
            }
            loaiThuoc += don.getSoLoaiThuoc();
        }
        nhanSoTongDon.setText(String.valueOf(danhSachDonThuoc.size()));
        nhanSoDaCapThuoc.setText(String.valueOf(daCap));
        nhanSoChoLayThuoc.setText(String.valueOf(choLay));
        nhanTongSoThuoc.setText(String.valueOf(loaiThuoc));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        form.add(tu);
        form.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        form.add(den);
        if (JOptionPane.showConfirmDialog(this, form, "Lọc theo khoảng ngày",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            tuNgay = tu.getText().trim().isEmpty() ? null : LocalDate.parse(tu.getText().trim(), DINH_DANG_NGAY);
            denNgay = den.getText().trim().isEmpty() ? null : LocalDate.parse(den.getText().trim(), DINH_DANG_NGAY);
            if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
                throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            }
            cboThoiGian.setSelectedIndex(0);
            locDuLieu();
        } catch (DateTimeParseException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Khoảng ngày không hợp lệ: " + e.getMessage(),
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String layMaDonDaChon() {
        int row = layDongDaChon();
        return row < 0 ? null : String.valueOf(bangDonThuoc.getModel().getValueAt(row, 0));
    }

    private void moFormDonThuoc(DonThuoc_entity donHienTai) {
        try {
            List<HoSoSucKhoe_entity> hoSoList = hoSoSucKhoeDAO.getAllHoSoSucKhoe();
            List<ThuCung_entity> thuCungList = thuCungDAO.getAllThuCung();
            List<BacSi_entity> bacSiList = bacSiDAO.getAll();
            List<Thuoc_entity> thuocList = chiTietDAO.getAllThuoc();
            if (hoSoList.isEmpty() || thuCungList.isEmpty() || bacSiList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cần có hồ sơ khám, thú cưng và bác sĩ trước khi kê đơn.",
                        "Thiếu dữ liệu liên quan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JTextField txtMa = new JTextField(donHienTai == null
                    ? donThuocDAO.getMaTiepTheo() : donHienTai.getMaDonThuoc());
            txtMa.setEditable(false);
            JComboBox<HoSoSucKhoe_entity> cboHoSo = new JComboBox<>(hoSoList.toArray(new HoSoSucKhoe_entity[0]));
            cboHoSo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object item, int index,
                        boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    if (item instanceof HoSoSucKhoe_entity) {
                        HoSoSucKhoe_entity hs = (HoSoSucKhoe_entity) item;
                        setText(hs.getMaHSSK() + " - " + value(hs.getTenThuCung()) + " - " + value(hs.getTenBacSi()));
                    }
                    return this;
                }
            });
            if (donHienTai != null) {
                for (int i = 0; i < cboHoSo.getItemCount(); i++) {
                    if (donHienTai.getMaHSSK().equals(cboHoSo.getItemAt(i).getMaHSSK())) {
                        cboHoSo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            JTextField txtNgay = new JTextField(donHienTai == null
                    ? DINH_DANG_NGAY_GIO.format(LocalDateTime.now().withSecond(0).withNano(0))
                    : DINH_DANG_NGAY_GIO.format(donHienTai.getNgayKeDon()));
            JTextField txtGhiChu = new JTextField(donHienTai == null ? "" : value(donHienTai.getGhiChu()));
            JComboBox<String> cboTrangThaiForm = new JComboBox<>(new String[] { "Chờ lấy thuốc", "Đã cấp thuốc" });
            if (donHienTai != null) {
                cboTrangThaiForm.setSelectedItem(donHienTai.getTrangThai());
            }

            DefaultTableModel chiTietModel = new DefaultTableModel(
                    new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Liều lượng" }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            if (donHienTai != null) {
                for (ChiTietDonThuoc_entity item : chiTietDAO.getChiTietTheoMaDon(donHienTai.getMaDonThuoc())) {
                    chiTietModel.addRow(new Object[] { item.getMaThuoc(), item.getTenThuoc(),
                            item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
                }
            }
            JTable bangChiTiet = new JTable(chiTietModel);
            bangChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JButton nutThemThuoc = new JButton("Thêm thuốc");
            JButton nutXoaThuoc = new JButton("Xóa thuốc");
            nutThemThuoc.addActionListener(e -> themChiTietThuoc(chiTietModel, thuocList));
            nutXoaThuoc.addActionListener(e -> {
                int row = bangChiTiet.getSelectedRow();
                if (row >= 0) {
                    chiTietModel.removeRow(row);
                }
            });
            JPanel panelThuocNut = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelThuocNut.add(nutThemThuoc);
            panelThuocNut.add(nutXoaThuoc);
            JPanel panelThuoc = new JPanel(new BorderLayout(0, 6));
            panelThuoc.add(new JScrollPane(bangChiTiet), BorderLayout.CENTER);
            panelThuoc.add(panelThuocNut, BorderLayout.SOUTH);
            panelThuoc.setPreferredSize(new Dimension(560, 190));

            JPanel header = new JPanel(new GridLayout(0, 2, 8, 8));
            header.add(new JLabel("Mã đơn thuốc:")); header.add(txtMa);
            header.add(new JLabel("Hồ sơ khám:")); header.add(cboHoSo);
            header.add(new JLabel("Ngày kê đơn (dd/MM/yyyy HH:mm):")); header.add(txtNgay);
            header.add(new JLabel("Ghi chú:")); header.add(txtGhiChu);
            header.add(new JLabel("Trạng thái:")); header.add(cboTrangThaiForm);
            JPanel form = new JPanel(new BorderLayout(0, 8));
            form.add(header, BorderLayout.NORTH);
            form.add(panelThuoc, BorderLayout.CENTER);
            String title = donHienTai == null ? "Kê đơn thuốc" : "Cập nhật đơn thuốc";
            if (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            LocalDateTime ngay = LocalDateTime.parse(txtNgay.getText().trim(), DINH_DANG_NGAY_GIO);
            HoSoSucKhoe_entity hs = (HoSoSucKhoe_entity) cboHoSo.getSelectedItem();
            if (hs == null || txtGhiChu.getText().trim().length() > 500) {
                throw new IllegalArgumentException("Chọn hồ sơ khám và giới hạn ghi chú không quá 500 ký tự.");
            }
            List<ChiTietDonThuoc_entity> chiTiet = new ArrayList<>();
            for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                chiTiet.add(new ChiTietDonThuoc_entity(null, txtMa.getText(),
                        String.valueOf(chiTietModel.getValueAt(i, 0)),
                        new BigDecimal(String.valueOf(chiTietModel.getValueAt(i, 3))),
                        new BigDecimal(String.valueOf(chiTietModel.getValueAt(i, 4)))));
            }
            if (chiTiet.isEmpty()) {
                throw new IllegalArgumentException("Đơn thuốc phải có ít nhất một loại thuốc.");
            }
            DonThuoc_entity don = new DonThuoc_entity(txtMa.getText(), hs.getMaTC(), hs.getMaBacSi(),
                    hs.getMaHSSK(), ngay, txtGhiChu.getText().trim(),
                    String.valueOf(cboTrangThaiForm.getSelectedItem()));
            boolean saved = donHienTai == null
                    ? donThuocDAO.createDonThuoc(don, chiTiet)
                    : donThuocDAO.updateDonThuoc(don, chiTiet);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Đã lưu đơn thuốc và chi tiết thuốc.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu đơn thuốc.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày kê đơn không hợp lệ, hãy nhập dd/MM/yyyy HH:mm.",
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý đơn thuốc: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themChiTietThuoc(DefaultTableModel model, List<Thuoc_entity> danhSachThuoc) {
        if (danhSachThuoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có danh mục thuốc trong hệ thống.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<Thuoc_entity> cboThuoc = new JComboBox<>(danhSachThuoc.toArray(new Thuoc_entity[0]));
        JTextField txtSoLuong = new JTextField("1");
        JTextField txtLieuLuong = new JTextField("1");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Thuốc:")); form.add(cboThuoc);
        form.add(new JLabel("Số lượng:")); form.add(txtSoLuong);
        form.add(new JLabel("Liều lượng:")); form.add(txtLieuLuong);
        if (JOptionPane.showConfirmDialog(this, form, "Thêm thuốc vào đơn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Thuoc_entity thuoc = (Thuoc_entity) cboThuoc.getSelectedItem();
            BigDecimal soLuong = new BigDecimal(txtSoLuong.getText().trim());
            BigDecimal lieuLuong = new BigDecimal(txtLieuLuong.getText().trim());
            if (thuoc == null || soLuong.signum() <= 0 || lieuLuong.signum() <= 0) {
                throw new NumberFormatException("Số lượng và liều lượng phải lớn hơn 0.");
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                if (thuoc.getMaThuoc().equals(model.getValueAt(i, 0))) {
                    throw new IllegalArgumentException("Thuốc này đã có trong đơn.");
                }
            }
            model.addRow(new Object[] { thuoc.getMaThuoc(), thuoc.getTenThuoc(),
                    thuoc.getDonViTinh(), soLuong, lieuLuong });
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Thông tin thuốc không hợp lệ: " + e.getMessage(),
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void capNhatTrangThaiNutHanhDong() {
        boolean coMotDongDuocChon = bangDonThuoc.getSelectedRowCount() == 1;
        nutXemChiTiet.setEnabled(coMotDongDuocChon);
        nutChinhSua.setEnabled(coMotDongDuocChon);
        nutXoa.setEnabled(coMotDongDuocChon);
    }

    private int layDongDaChon() {
        if (bangDonThuoc.getSelectedRowCount() != 1) {
            return -1;
        }
        int dong = bangDonThuoc.getSelectedRow();
        return dong < 0 ? -1 : bangDonThuoc.convertRowIndexToModel(dong);
    }

    private void xemDongDaChon() {
        String maDon = layMaDonDaChon();
        if (maDon == null) {
            return;
        }
        try {
            DonThuoc_entity don = donThuocDAO.getDonThuocTheoMa(maDon);
            if (don == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn thuốc " + maDon + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<ChiTietDonThuoc_entity> items = donThuocDAO.getChiTietDonThuoc(maDon);
            DefaultTableModel model = new DefaultTableModel(
                    new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Liều lượng" }, 0);
            for (ChiTietDonThuoc_entity item : items) {
                model.addRow(new Object[] { item.getMaThuoc(), item.getTenThuoc(),
                        item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
            }
            JTable bangChiTiet = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.add(new JLabel("Đơn " + don.getMaDonThuoc() + " | " + value(don.getTenThuCung())
                    + " | " + value(don.getTenBacSi()) + " | "
                    + (don.getNgayKeDon() == null ? "" : DINH_DANG_NGAY_GIO.format(don.getNgayKeDon()))
                    + " | " + don.getTrangThai()), BorderLayout.NORTH);
            panel.add(new JScrollPane(bangChiTiet), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(620, 280));
            JOptionPane.showMessageDialog(this, panel, "Chi tiết đơn thuốc",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết đơn thuốc: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chinhSuaDongDaChon() {
        String maDon = layMaDonDaChon();
        if (maDon == null) {
            return;
        }
        try {
            DonThuoc_entity don = donThuocDAO.getDonThuocTheoMa(maDon);
            if (don == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn thuốc " + maDon + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            moFormDonThuoc(don);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải đơn thuốc để sửa: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaDongDaChon() {
        String maDon = layMaDonDaChon();
        if (maDon == null) {
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn thuốc \"" + maDon + "\" không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon == JOptionPane.YES_OPTION) {
            try {
                if (donThuocDAO.deleteDonThuoc(maDon)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa đơn thuốc.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy đơn thuốc để xóa.",
                            "Xóa thất bại", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Không thể xóa đơn thuốc: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("danh-sach-don-thuoc.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại, ghi đè?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            DefaultTableModel model = (DefaultTableModel) bangDonThuoc.getModel();
            for (int r = -1; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) writer.write(',');
                    String text = r < 0 ? bangDonThuoc.getColumnName(c) : String.valueOf(model.getValueAt(r, c));
                    writer.write("\"" + text.replace("\"", "\"\"") + "\"");
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Đã xuất tệp: " + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể xuất danh sách: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TẠO THÀNH PHẦN
    // =========================================================================
    private JComboBox<String> taoOChon(String[] danhSach, int doRong) {
        JComboBox<String> o = new JComboBox<>(danhSach);
        o.setFont(RangBuocGiaoDien.fontThuong(13));
        o.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        o.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        o.setPreferredSize(new Dimension(doRong, CHIEU_CAO_O_NHAP));
        o.setMaximumSize(new Dimension(doRong, CHIEU_CAO_O_NHAP));
        return o;
    }

    private JButton taoNutLienKet(String chu, Color mauChu) {
        JButton nut = new JButton(chu);
        nut.setFont(RangBuocGiaoDien.fontThuong(12));
        nut.setForeground(mauChu);
        nut.setContentAreaFilled(false);
        nut.setBorderPainted(false);
        nut.setFocusPainted(false);
        nut.setMargin(new Insets(0, 0, 0, 0));
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return nut;
    }

    private NutBoTron taoNutTrang(String chu, boolean dangChon) {
        NutBoTron nut;
        if (dangChon) {
            nut = new NutBoTron(chu, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT,
                    null, Color.WHITE, 8);
        } else {
            nut = new NutBoTron(chu, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DONG_RE_CHUOT,
                    RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        }
        nut.setFont(RangBuocGiaoDien.fontThuong(12));
        nut.setPreferredSize(new Dimension(chu.length() > 2 ? 40 : 34, 34));
        return nut;
    }

    // =========================================================================
    // GETTERS (để nhóm làm chức năng truy cập khi cần)
    // =========================================================================
    public TableMau getBangDonThuoc() {
        return bangDonThuoc;
    }

    public OTimKiemGoiY getTxtTimKiem() {
        return txtTimKiem;
    }

    public JComboBox<String> getCboTrangThai() {
        return cboTrangThai;
    }

    public JComboBox<String> getCboThoiGian() {
        return cboThoiGian;
    }

    public JButton getNutKeDonThuoc() {
        return nutKeDonThuoc;
    }

    // =========================================================================
    // LỚP PHỤ TRỢ: Renderer cột "Trạng thái" (huy hiệu màu)
    // =========================================================================
    private static class OTrangThai extends JPanel implements TableCellRenderer {
        private final JTable bang;
        private String chu = "";
        private Color mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
        private Color mauChu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;

        OTrangThai(JTable bang) {
            this.bang = bang;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            chu = value == null ? "" : value.toString();
            switch (chu) {
                case "Chờ lấy thuốc":
                    mauNen = RangBuocGiaoDien.MAU_CANH_BAO_NEN;
                    mauChu = RangBuocGiaoDien.MAU_CANH_BAO_CHU;
                    break;
                case "Đã cấp thuốc":
                    mauNen = RangBuocGiaoDien.MAU_THONG_TIN_NEN;
                    mauChu = RangBuocGiaoDien.MAU_THONG_TIN_CHU;
                    break;
                default:
                    mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
                    mauChu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
                    break;
            }
            if (isSelected) {
                setBackground(bang.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setFont(RangBuocGiaoDien.fontDam(12));
            FontMetrics doDo = g2.getFontMetrics();
            int rong = doDo.stringWidth(chu) + 28;
            int cao = 24;
            int x = 12;
            int y = (getHeight() - cao) / 2;
            g2.setColor(mauNen);
            g2.fillRoundRect(x, y, rong, cao, cao, cao);
            g2.setColor(mauChu);
            g2.fillOval(x + 9, y + cao / 2 - 3, 6, 6);
            g2.drawString(chu, x + 20, y + (cao - doDo.getHeight()) / 2 + doDo.getAscent());
            g2.dispose();
        }
    }

    // =========================================================================
    // MAIN CHẠY THỬ ĐỘC LẬP
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử độc lập - DonThuocGUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new DonThuocGUI());
            GiaoDienChinhGUI.caiDatKichThuocCuaSo(frame, false);
            frame.setVisible(true);
        });
    }
}
