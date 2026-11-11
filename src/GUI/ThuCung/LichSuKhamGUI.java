package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import GUI.ThuCung.HoSoThuCungGUI.NutBoTron;
import GUI.ThuCung.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.ThuCung.HoSoThuCungGUI.PanelBoTron;
import Service.ThuCung.LichSuKhamService;
import entity.BacSi.BacSi_entity;
import entity.ThuCung.HoSoSucKhoe_entity;
import entity.ThuCung.ThuCung_entity;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Class LichSuKhamGUI: Giao diện "Lịch sử khám" (Tab Thú cưng ➔ Lịch sử khám).
 * Quản lý hồ sơ sức khỏe và lịch sử khám thông qua service.
 * Các trường hiển thị dựa theo bảng HoSoSucKhoe (kết hợp ThuCung, KhachHang, BacSyThuY).
 * Dùng lại các thành phần PanelBoTron, NutBoTron, OTimKiemGoiY của HoSoThuCungGUI.
 */
public class LichSuKhamGUI extends JPanel {

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

    private final String[] tieuDeCot = { "Mã hồ sơ", "Thú cưng", "Chủ nuôi", "Ngày khám", "Triệu chứng",
            "Chẩn đoán", "Cân nặng (kg)", "Bác sĩ phụ trách" };
    private final String[] dsBacSiLoc = { "Bác sĩ: Tất cả" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final LichSuKhamService service = new LichSuKhamService();
    private List<HoSoSucKhoe_entity> danhSachHoSo = new ArrayList<>();
    private List<HoSoSucKhoe_entity> danhSachDangHienThi = new ArrayList<>();
    private LocalDate tuNgay;
    private LocalDate denNgay;

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBoTron nutThemLuotKham;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongLuotKham;
    private JLabel nhanSoKhamHomNay;
    private JLabel nhanSoKhamThangNay;
    private JLabel nhanSoBacSi;

    // --- Phần thẻ chứa bảng ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboBacSi;
    private JComboBox<String> cboThoiGian;
    private JButton nutChonKhoangNgay;
    private JButton nutXemChiTiet;
    private JButton nutChinhSua;
    private JButton nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangLichSuKham;
    private JScrollPane thanhCuonBang;

    // --- Thông tin dưới bảng ---
    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public LichSuKhamGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        loadDataToTable();
    }

    //Hàm gốc tổng hợp và sắp xếp bố cục chính
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

    //Tạo đường dẫn trang, tiêu đề "Lịch sử khám" và nút "+ Thêm lượt khám".
    private void khoiTaoTieuDe() {
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);
        panelTieuDe.setBorder(new EmptyBorder(0, 0, KHOANG_CACH_THE, 0));

        nhanDuongDan = new JLabel("Thú cưng / Lịch sử khám");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Lịch sử khám");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Theo dõi các lượt khám, chẩn đoán và bác sĩ phụ trách của từng thú cưng.");
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

        nutThemLuotKham = new NutBoTron("+  Thêm lượt khám", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutThemLuotKham.setPreferredSize(new Dimension(160, 40));

        JPanel panelNut = new JPanel(new GridBagLayout());
        panelNut.setOpaque(false);
        panelNut.add(nutThemLuotKham);

        panelTieuDe.add(panelChu, BorderLayout.WEST);
        panelTieuDe.add(panelNut, BorderLayout.EAST);
    }

    //khoiTaoThongKe() & taoTheThongKe(): Dựng 4 thẻ hiển thị chỉ số (Tổng lượt khám, Khám hôm nay, Khám tháng này, Bác sĩ thú y).
    private void khoiTaoThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH_THE, 0));
        panelThongKe.setOpaque(false);
        panelThongKe.setPreferredSize(new Dimension(0, CHIEU_CAO_THE_THONG_KE));

        nhanSoTongLuotKham = new JLabel("256");
        nhanSoKhamHomNay = new JLabel("6");
        nhanSoKhamThangNay = new JLabel("48");
        nhanSoBacSi = new JLabel("15");

        panelThongKe.add(taoTheThongKe("TK", "Tổng lượt khám", nhanSoTongLuotKham, "Hồ sơ sức khỏe"));
        panelThongKe.add(taoTheThongKe("HN", "Khám hôm nay", nhanSoKhamHomNay, "Lượt trong ngày"));
        panelThongKe.add(taoTheThongKe("TN", "Khám tháng này", nhanSoKhamThangNay, "Lượt trong tháng"));
        panelThongKe.add(taoTheThongKe("BS", "Bác sĩ thú y", nhanSoBacSi, "Đang tham gia khám"));
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

    //Tạo khung bo tròn bọc thanh công cụ, bảng dữ liệu và thanh phân trang.
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

    //Tạo ô tìm kiếm, combobox lọc bác sĩ/thời gian, nút "Khoảng ngày", và nhóm nút thao tác (Xem chi tiết, Chỉnh sửa, Xóa, Xuất danh sách).
    private void khoiTaoThanhCongCu() {
        panelCongCu = new JPanel();
        panelCongCu.setOpaque(false);
        panelCongCu.setLayout(new BoxLayout(panelCongCu, BoxLayout.X_AXIS));
        panelCongCu.setBorder(new EmptyBorder(16, 20, 16, 20));

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã hồ sơ, thú cưng, chẩn đoán...");
        txtTimKiem.setPreferredSize(new Dimension(340, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(340, CHIEU_CAO_O_NHAP));

        cboBacSi = taoOChon(dsBacSiLoc, 180);
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
        panelCongCu.add(cboBacSi);
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
    
    //Định nghĩa các cột, thiết lập chiều rộng cột và sự kiện chọn dòng của bảng
    private void khoiTaoBang() {
        bangLichSuKham = new TableMau(tieuDeCot, new Object[0][tieuDeCot.length]);
        bangLichSuKham.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] doRongCot = { 80, 120, 140, 140, 170, 170, 100, 150 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangLichSuKham.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
        bangLichSuKham.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatTrangThaiNutHanhDong();
            }
        });

        thanhCuonBang = new JScrollPane(bangLichSuKham);
        thanhCuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        thanhCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        thanhCuonBang.getVerticalScrollBar().setUnitIncrement(16);
    }

    //Hiển thị số lượng cuối bảng cuối bảng
    private void khoiTaoPhanTrang() {
        panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setPreferredSize(new Dimension(0, CHIEU_CAO_THANH_PHAN_TRANG));
        panelPhanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(0, 20, 0, 20)));

        nhanThongTinTrang = new JLabel();
        nhanThongTinTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
    }

    // CÀI ĐẶT SỰ KIỆN CHO CỤM NÚT THAO TÁC
    private void caiDatSuKien() {
        nutThemLuotKham.addActionListener(e -> moFormHoSo(null));
        nutXemChiTiet.addActionListener(e -> xemDongDaChon());
        nutChinhSua.addActionListener(e -> chinhSuaDongDaChon());
        nutXoa.addActionListener(e -> xoaDongDaChon());
        nutXuatDanhSach.addActionListener(e -> xuatDanhSach());
        nutChonKhoangNgay.addActionListener(e -> chonKhoangNgay());
        cboBacSi.addActionListener(e -> locDuLieu());
        cboThoiGian.addActionListener(e -> locDuLieu());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
        });
        bangLichSuKham.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangLichSuKham.getSelectedRowCount() == 1) {
                    xemDongDaChon();
                }
            }
        });
        capNhatTrangThaiNutHanhDong();
    }

    //Gọi service nạp danh sách hồ sơ sức khỏe và nạp danh sách bác sĩ vào combobox
    public void loadDataToTable() {
        try {
            LichSuKhamService.DuLieuManHinh duLieu = service.taiDuLieu();
            danhSachHoSo = duLieu.hoSo;
            List<BacSi_entity> bacSi = duLieu.bacSi;
            Object selected = cboBacSi.getSelectedItem();
            cboBacSi.removeAllItems();
            cboBacSi.addItem("Bác sĩ: Tất cả");
            for (BacSi_entity item : bacSi) {
                cboBacSi.addItem(item.getHoTen() + " (" + item.getMaBacSi() + ")");
            }
            if (selected != null && selected.toString().startsWith("Bác sĩ:")) {
                cboBacSi.setSelectedIndex(0);
            } else if (selected != null) {
                cboBacSi.setSelectedItem(selected);
            }
            locDuLieu();
            capNhatThongKe();
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch sử khám: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //tìm theo mã, cboBacSi, cboThoiGian.
    private void locDuLieu() {
        if (bangLichSuKham == null) return;
        String doctor = String.valueOf(cboBacSi.getSelectedItem());
        String time = String.valueOf(cboThoiGian.getSelectedItem());
        LocalDate today = LocalDate.now();
        danhSachDangHienThi =
                service.loc(danhSachHoSo, txtTimKiem.getText(), doctor, time, tuNgay, denNgay, today);
        DefaultTableModel model = (DefaultTableModel) bangLichSuKham.getModel();
        model.setRowCount(0);
        for (HoSoSucKhoe_entity hs : danhSachDangHienThi) {
            model.addRow(new Object[] { hs.getMaHSSK(),
                    value(hs.getTenThuCung()) + " (" + (hs.getMaTC() != null ? hs.getMaTC().getMaTC() : "") + ")", value(hs.getChuHo()),
                    hs.getNgayKham() == null ? "" : DINH_DANG_NGAY_GIO.format(hs.getNgayKham()),
                    value(hs.getTrieuChung()), value(hs.getChuanDoan()),
                    hs.getCanNang() == null ? "" : hs.getCanNang().toPlainString(), value(hs.getTenBacSi()) });
        }
        bangLichSuKham.clearSelection();
        capNhatTrangThaiNutHanhDong();
        nhanThongTinTrang.setText("Hiển thị tất cả " + model.getRowCount() + " lượt khám");
    }

    //Cập nhật số liệu thống kê ở 4 thẻ trên cùng dựa theo danh sách hồ sơ hiện tại và ngày hiện tại.
    private void capNhatThongKe() {
        LichSuKhamService.ThongKe thongKe = service.thongKe(danhSachHoSo, LocalDate.now());
        nhanSoTongLuotKham.setText(String.valueOf(thongKe.tongLuot));
        nhanSoKhamHomNay.setText(String.valueOf(thongKe.homNay));
        nhanSoKhamThangNay.setText(String.valueOf(thongKe.thangNay));
        nhanSoBacSi.setText(String.valueOf(thongKe.soBacSi));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    //Mở popup cho phép người dùng nhập khoảng ngày khám để lọc danh sách. Nếu nhập sai định dạng ngày, hiển thị cảnh báo.
    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Từ ngày (dd/MM/yyyy):")); form.add(tu);
        form.add(new JLabel("Đến ngày (dd/MM/yyyy):")); form.add(den);
        if (JOptionPane.showConfirmDialog(this, form, "Lọc theo khoảng ngày",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            LichSuKhamService.KhoangNgay khoang = service.khoangNgay(tu.getText(), den.getText());
            tuNgay = khoang.tuNgay;
            denNgay = khoang.denNgay;
            cboThoiGian.setSelectedIndex(0);
            locDuLieu();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Khoảng ngày không hợp lệ: " + e.getMessage(),
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String layMaHoSoDaChon() {
        int row = layDongDaChon();
        return row < 0 ? null : String.valueOf(bangLichSuKham.getModel().getValueAt(row, 0));
    }

    //Mở Form popup (JOptionPane) chứa thông tin thú cưng, bác sĩ, ngày khám, triệu chứng, chẩn đoán, cân nặng để thêm mới hoặc cập nhật hồ sơ sức khỏe.
    private void moFormHoSo(HoSoSucKhoe_entity hoSoHienTai) {
        try {
            List<ThuCung_entity> thuCungList = service.taiThuCung();
            List<BacSi_entity> bacSiList = service.taiBacSi();
            
            JTextField txtMa = new JTextField(hoSoHienTai == null
                    ? service.maTiepTheo() : hoSoHienTai.getMaHSSK());
            txtMa.setEditable(false);
            JComboBox<ThuCung_entity> cboThuCung = new JComboBox<>(thuCungList.toArray(new ThuCung_entity[0]));
            cboThuCung.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object item, int index,
                        boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    if (item instanceof ThuCung_entity) {
                        ThuCung_entity tc = (ThuCung_entity) item;
                        setText(value(tc.getTenTC()) + " (" + tc.getMaTC() + ")");
                    }
                    return this;
                }
            });
            JComboBox<BacSi_entity> cboBacSiForm = new JComboBox<>(bacSiList.toArray(new BacSi_entity[0]));
            cboBacSiForm.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object item, int index,
                        boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    if (item instanceof BacSi_entity) {
                        BacSi_entity bs = (BacSi_entity) item;
                        setText(value(bs.getHoTen()) + " (" + bs.getMaBacSi() + ")");
                    }
                    return this;
                }
            });
            if (hoSoHienTai != null) {
                for (int i = 0; i < cboThuCung.getItemCount(); i++) {
                    if (hoSoHienTai.getMaTC() != null && hoSoHienTai.getMaTC().equals(cboThuCung.getItemAt(i))) cboThuCung.setSelectedIndex(i);
                }
                for (int i = 0; i < cboBacSiForm.getItemCount(); i++) {
                    if (hoSoHienTai.getMaBacSi() != null && hoSoHienTai.getMaBacSi().equals(cboBacSiForm.getItemAt(i))) cboBacSiForm.setSelectedIndex(i);
                }
            }
            JTextField txtNgay = new JTextField(hoSoHienTai == null
                    ? DINH_DANG_NGAY_GIO.format(LocalDateTime.now().withSecond(0).withNano(0))
                    : DINH_DANG_NGAY_GIO.format(hoSoHienTai.getNgayKham()));
            JTextField txtTrieuChung = new JTextField(hoSoHienTai == null ? "" : value(hoSoHienTai.getTrieuChung()));
            JTextField txtChuanDoan = new JTextField(hoSoHienTai == null ? "" : value(hoSoHienTai.getChuanDoan()));
            JTextField txtCanNang = new JTextField(hoSoHienTai == null || hoSoHienTai.getCanNang() == null
                    ? "" : hoSoHienTai.getCanNang().toPlainString());
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Mã hồ sơ:")); form.add(txtMa);
            form.add(new JLabel("Thú cưng:")); form.add(cboThuCung);
            form.add(new JLabel("Bác sĩ:")); form.add(cboBacSiForm);
            form.add(new JLabel("Ngày khám (dd/MM/yyyy HH:mm):")); form.add(txtNgay);
            form.add(new JLabel("Triệu chứng:")); form.add(txtTrieuChung);
            form.add(new JLabel("Chẩn đoán:")); form.add(txtChuanDoan);
            form.add(new JLabel("Cân nặng (kg):")); form.add(txtCanNang);
            String title = hoSoHienTai == null ? "Thêm lượt khám" : "Cập nhật hồ sơ khám";
            if (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            ThuCung_entity tc = (ThuCung_entity) cboThuCung.getSelectedItem();
            BacSi_entity bs = (BacSi_entity) cboBacSiForm.getSelectedItem();
            boolean saved = service.luu(new LichSuKhamService.ThongTinHoSo(
                    txtMa.getText(), hoSoHienTai == null, tc, bs, txtNgay.getText(),
                    txtTrieuChung.getText(), txtChuanDoan.getText(), txtCanNang.getText()));
            if (saved) {
                JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ khám.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu hồ sơ khám.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý hồ sơ khám: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý hồ sơ khám: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //(tự động bật/tắt nút Xem, Sửa, Xóa khi người dùng chọn dòng trên bảng).
    private void capNhatTrangThaiNutHanhDong() {
        boolean coMotDongDuocChon = bangLichSuKham.getSelectedRowCount() == 1;
        nutXemChiTiet.setEnabled(coMotDongDuocChon);
        nutChinhSua.setEnabled(coMotDongDuocChon);
        nutXoa.setEnabled(coMotDongDuocChon);
    }

    //ChonDongDaChon(): Lấy chỉ số dòng đã chọn trong bảng, trả về -1 nếu không có dòng nào được chọn.
    private int layDongDaChon() {
        if (bangLichSuKham.getSelectedRowCount() != 1) {
            return -1;
        }
        int dong = bangLichSuKham.getSelectedRow();
        return dong < 0 ? -1 : bangLichSuKham.convertRowIndexToModel(dong);
    }

    //Xem chi tiết 
    private void xemDongDaChon() {
        String maHoSo = layMaHoSoDaChon();
        if (maHoSo == null) {
            return;
        }
        try {
            HoSoSucKhoe_entity hs = service.timTheoMa(maHoSo);
            if (hs == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hồ sơ " + maHoSo + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String chiTiet = "Mã hồ sơ: " + hs.getMaHSSK()
                    + "\nThú cưng: " + value(hs.getTenThuCung()) + " (" + (hs.getMaTC() != null ? hs.getMaTC().getMaTC() : "") + ")"
                    + "\nChủ nuôi: " + value(hs.getChuHo())
                    + "\nBác sĩ: " + value(hs.getTenBacSi()) + " (" + (hs.getMaBacSi() != null ? hs.getMaBacSi().getMaBacSi() : "") + ")"
                    + "\nNgày khám: " + (hs.getNgayKham() == null ? "" : DINH_DANG_NGAY_GIO.format(hs.getNgayKham()))
                    + "\nTriệu chứng: " + value(hs.getTrieuChung())
                    + "\nChẩn đoán: " + value(hs.getChuanDoan())
                    + "\nCân nặng (kg): " + (hs.getCanNang() == null ? "" : hs.getCanNang().toPlainString());
            JOptionPane.showMessageDialog(this, chiTiet, "Chi tiết lượt khám",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết hồ sơ: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Chỉnh sửa hồ sơ sức khỏe của dòng được chọn, hiển thị thông tin trong một hộp thoại JOptionPane để người dùng có thể cập nhật.
    private void chinhSuaDongDaChon() {
        String maHoSo = layMaHoSoDaChon();
        if (maHoSo == null) {
            return;
        }
        try {
            HoSoSucKhoe_entity hs = service.timTheoMa(maHoSo);
            if (hs == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hồ sơ " + maHoSo + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            moFormHoSo(hs);
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải hồ sơ để sửa: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //xóa hồ sơ sức khỏe của dòng được chọn, hiển thị hộp thoại xác nhận trước khi xóa. Nếu xóa thành công, nạp lại danh sách hồ sơ.
    private void xoaDongDaChon() {
        String maHoSo = layMaHoSoDaChon();
        if (maHoSo == null) {
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hồ sơ khám \"" + maHoSo + "\" không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon == JOptionPane.YES_OPTION) {
            try {
                if (service.xoa(maHoSo)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ khám.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hồ sơ để xóa.",
                            "Xóa thất bại", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException | RuntimeException e) {
                JOptionPane.showMessageDialog(this, "Không thể xóa hồ sơ khám: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //Xuất danh sách hồ sơ sức khỏe hiện tại ra tệp CSV, hiển thị hộp thoại chọn tệp và cảnh báo nếu tệp đã tồn tại.
    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("lich-su-kham.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại, ghi đè?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.xuatDanhSach(file, danhSachDangHienThi);
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

    // =========================================================================
    // GETTERS (để nhóm làm chức năng truy cập khi cần)
    // =========================================================================
    public TableMau getBangLichSuKham() {
        return bangLichSuKham;
    }

    public OTimKiemGoiY getTxtTimKiem() {
        return txtTimKiem;
    }

    public JComboBox<String> getCboBacSi() {
        return cboBacSi;
    }

    public JComboBox<String> getCboThoiGian() {
        return cboThoiGian;
    }

    public JButton getNutThemLuotKham() {
        return nutThemLuotKham;
    }

    
}
