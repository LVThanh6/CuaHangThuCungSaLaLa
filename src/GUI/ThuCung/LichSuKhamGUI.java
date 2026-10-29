package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import GUI.ThuCung.HoSoThuCungGUI.NutBoTron;
import GUI.ThuCung.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.ThuCung.HoSoThuCungGUI.PanelBoTron;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Class LichSuKhamGUI: Giao diện "Lịch sử khám" (Tab Thú cưng ➔ Lịch sử khám).
 * Chỉ dựng giao diện, chưa xử lý nghiệp vụ.
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

    // --- Dữ liệu hiển thị mẫu (chỉ để dựng giao diện) ---
    private final String[] tieuDeCot = { "Mã hồ sơ", "Thú cưng", "Chủ nuôi", "Ngày khám", "Triệu chứng",
            "Chẩn đoán", "Cân nặng (kg)", "Bác sĩ phụ trách" };
    private final Object[][] duLieuMau = {
            { "HS001", "Mimi (TC001)", "Nguyễn Thị Hoa", "06/06/2025 09:00", "Ngứa, rụng lông", "Viêm da dị ứng", "2.8", "Nguyễn Hữu Tài" },
            { "HS002", "Lucky (TC002)", "Trần Văn Hùng", "11/06/2025 10:00", "Tiêu chảy, mệt mỏi", "Rối loạn tiêu hóa", "3.6", "Trần Thị Uyên" },
            { "HS003", "Bông (TC003)", "Lê Thị Kim", "16/06/2025 11:00", "Ho, chảy nước mũi", "Viêm đường hô hấp trên", "4.4", "Lê Minh Vinh" },
            { "HS004", "Milo (TC004)", "Phạm Văn Lâm", "21/06/2025 12:00", "Đi khập khiễng, đau chân", "Trật khớp nhẹ", "5.2", "Phạm Thị Xuân" },
            { "HS005", "Kitty (TC005)", "Võ Thị My", "26/06/2025 13:00", "Nôn mửa, bỏ ăn", "Viêm dạ dày", "6.0", "Võ Thanh Tùng" },
            { "HS006", "Boss (TC006)", "Đặng Văn Nghĩa", "01/07/2025 14:00", "Ngứa, rụng lông", "Viêm da dị ứng", "6.8", "Đặng Thị Yến" },
            { "HS007", "Susu (TC007)", "Bùi Thị Oanh", "06/07/2025 15:00", "Tiêu chảy, mệt mỏi", "Rối loạn tiêu hóa", "7.6", "Bùi Văn Khánh" },
            { "HS008", "Nâu (TC008)", "Hồ Văn Phong", "11/07/2025 16:00", "Ho, chảy nước mũi", "Viêm đường hô hấp trên", "8.4", "Hồ Thị Diệp" }
    };
    private final String[] dsBacSiLoc = { "Bác sĩ: Tất cả", "Nguyễn Hữu Tài", "Trần Thị Uyên", "Lê Minh Vinh" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

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
    private JButton nutXemChiTiet;
    private JButton nutChinhSua;
    private JButton nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangLichSuKham;
    private JScrollPane thanhCuonBang;

    // --- Phần phân trang ---
    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;
    private JLabel nhanSoDongTrang;
    private JComboBox<String> cboSoDongTrang;
    private NutBoTron nutTrangTruoc;
    private NutBoTron nutTrang1;
    private NutBoTron nutTrang2;
    private NutBoTron nutTrang3;
    private NutBoTron nutTrangRutGon;
    private NutBoTron nutTrangCuoi;
    private NutBoTron nutTrangSau;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public LichSuKhamGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
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

    // =========================================================================
    // PHẦN 2: 4 THẺ THỐNG KÊ
    // =========================================================================
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

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã hồ sơ, thú cưng, chẩn đoán...");
        txtTimKiem.setPreferredSize(new Dimension(340, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(340, CHIEU_CAO_O_NHAP));

        cboBacSi = taoOChon(dsBacSiLoc, 180);
        cboThoiGian = taoOChon(dsThoiGianLoc, 180);

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
        bangLichSuKham = new TableMau(tieuDeCot, duLieuMau);
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

    private void khoiTaoPhanTrang() {
        panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setPreferredSize(new Dimension(0, CHIEU_CAO_THANH_PHAN_TRANG));
        panelPhanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(0, 20, 0, 20)));

        nhanThongTinTrang = new JLabel("Hiển thị 1–8 trong 256 lượt khám");
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
        nutTrang3 = taoNutTrang("3", false);
        nutTrangRutGon = taoNutTrang("...", false);
        nutTrangCuoi = taoNutTrang("32", false);
        nutTrangSau = taoNutTrang(">", false);

        JPanel panelBenPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 11));
        panelBenPhai.setOpaque(false);
        panelBenPhai.add(nhanSoDongTrang);
        panelBenPhai.add(cboSoDongTrang);
        panelBenPhai.add(Box.createHorizontalStrut(10));
        panelBenPhai.add(nutTrangTruoc);
        panelBenPhai.add(nutTrang1);
        panelBenPhai.add(nutTrang2);
        panelBenPhai.add(nutTrang3);
        panelBenPhai.add(nutTrangRutGon);
        panelBenPhai.add(nutTrangCuoi);
        panelBenPhai.add(nutTrangSau);

        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
        panelPhanTrang.add(panelBenPhai, BorderLayout.EAST);
    }

    // =========================================================================
    // CÀI ĐẶT SỰ KIỆN CHO CỤM NÚT THAO TÁC
    // =========================================================================
    private void caiDatSuKien() {
        nutXemChiTiet.addActionListener(e -> xemDongDaChon());
        nutChinhSua.addActionListener(e -> chinhSuaDongDaChon());
        nutXoa.addActionListener(e -> xoaDongDaChon());
        capNhatTrangThaiNutHanhDong();
    }

    private void capNhatTrangThaiNutHanhDong() {
        boolean coMotDongDuocChon = bangLichSuKham.getSelectedRowCount() == 1;
        nutXemChiTiet.setEnabled(coMotDongDuocChon);
        nutChinhSua.setEnabled(coMotDongDuocChon);
        nutXoa.setEnabled(coMotDongDuocChon);
    }

    private int layDongDaChon() {
        if (bangLichSuKham.getSelectedRowCount() != 1) {
            return -1;
        }
        int dong = bangLichSuKham.getSelectedRow();
        return dong < 0 ? -1 : bangLichSuKham.convertRowIndexToModel(dong);
    }

    private void xemDongDaChon() {
        int dong = layDongDaChon();
        if (dong < 0) {
            return;
        }

        StringBuilder chiTiet = new StringBuilder();
        for (int cot = 0; cot < bangLichSuKham.getColumnCount(); cot++) {
            if (cot > 0) {
                chiTiet.append('\n');
            }
            chiTiet.append(bangLichSuKham.getColumnName(cot)).append(": ")
                    .append(String.valueOf(bangLichSuKham.getModel().getValueAt(dong, cot)));
        }
        JOptionPane.showMessageDialog(this, chiTiet.toString(), "Chi tiết lượt khám",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void chinhSuaDongDaChon() {
        int dong = layDongDaChon();
        if (dong < 0) {
            return;
        }
        String maHoSo = String.valueOf(bangLichSuKham.getModel().getValueAt(dong, 0));
        JOptionPane.showMessageDialog(this, "Mở form cập nhật kết quả khám cho hồ sơ: " + maHoSo,
                "Chỉnh sửa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xoaDongDaChon() {
        int dong = layDongDaChon();
        if (dong < 0) {
            return;
        }
        String maHoSo = String.valueOf(bangLichSuKham.getModel().getValueAt(dong, 0));
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hồ sơ khám \"" + maHoSo + "\" không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon == JOptionPane.YES_OPTION) {
            ((DefaultTableModel) bangLichSuKham.getModel()).removeRow(dong);
            bangLichSuKham.clearSelection();
            capNhatTrangThaiNutHanhDong();
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

    // =========================================================================
    // MAIN CHẠY THỬ ĐỘC LẬP
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử độc lập - LichSuKhamGUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LichSuKhamGUI());
            GiaoDienChinhGUI.caiDatKichThuocCuaSo(frame, false);
            frame.setVisible(true);
        });
    }
}
