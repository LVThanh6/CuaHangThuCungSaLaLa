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
import javax.swing.border.LineBorder;

/**
 * Class LichSuKhamGUI: Giao diện "Lịch sử khám" (Tab Thú cưng ➔ Lịch sử khám).
 * Chuẩn thiết kế mới:
 * - Loại bỏ hoàn toàn cột "Thao tác" khỏi bảng.
 * - Xóa bỏ 3 cột hiển thị: "Triệu chứng", "Chẩn đoán", "Bác sĩ phụ trách" khỏi bảng giúp bảng thoáng đãng.
 * - Cụm nút thao tác trên toolbar: "+ Thêm lượt khám", "Xem chi tiết", "Chỉnh sửa", "Xóa", "Xuất Excel".
 * - Khi chọn dòng và bấm "Xem chi tiết", mở JDialog hiển thị đầy đủ thông tin (đặc biệt là 3 thông tin vừa được ẩn).
 * - Các nút Xem/Sửa/Xóa mặc định bị disable, chỉ bật khi chọn 1 dòng trên bảng.
 */
public class LichSuKhamGUI extends JPanel {

    // =========================================================================
    // KHAI BÁO TOÀN BỘ BIẾN & DỮ LIỆU
    // =========================================================================

    // --- Hằng số kích thước ---
    private static final int KHOANG_CACH_LE = 20;
    private static final int KHOANG_CACH_THE = 16;
    private static final int BAN_KINH_THE = 12;
    private static final int CHIEU_CAO_THE_THONG_KE = 80;
    private static final int CHIEU_CAO_O_NHAP = 38;
    private static final int CHIEU_CAO_THANH_PHAN_TRANG = 56;

    // Lớp nội bộ lưu trữ dữ liệu hồ sơ khám đầy đủ
    public static class BanGhiKham {
        public String maHS, thuCung, chuNuoi, ngayKham, trieuChung, chanDoan, canNang, bacSi;

        public BanGhiKham(String maHS, String thuCung, String chuNuoi, String ngayKham,
                          String trieuChung, String chanDoan, String canNang, String bacSi) {
            this.maHS = maHS;
            this.thuCung = thuCung;
            this.chuNuoi = chuNuoi;
            this.ngayKham = ngayKham;
            this.trieuChung = trieuChung;
            this.chanDoan = chanDoan;
            this.canNang = canNang;
            this.bacSi = bacSi;
        }
    }

    private final BanGhiKham[] danhSachBanGhi = {
            new BanGhiKham("HS001", "Mimi (TC001)", "Nguyễn Thị Hoa", "06/06/2025 09:00", "Ngứa, rụng lông", "Viêm da dị ứng", "2.8", "BS. Nguyễn Hữu Tài"),
            new BanGhiKham("HS002", "Lucky (TC002)", "Trần Văn Hùng", "11/06/2025 10:00", "Tiêu chảy, mệt mỏi", "Rối loạn tiêu hóa", "3.6", "BS. Trần Thị Uyên"),
            new BanGhiKham("HS003", "Bông (TC003)", "Lê Thị Kim", "16/06/2025 11:00", "Ho, chảy nước mũi", "Viêm đường hô hấp trên", "4.4", "BS. Lê Minh Vinh"),
            new BanGhiKham("HS004", "Milo (TC004)", "Phạm Văn Lâm", "21/06/2025 12:00", "Đi khập khiễng, đau chân", "Trật khớp nhẹ", "5.2", "BS. Phạm Thị Xuân"),
            new BanGhiKham("HS005", "Kitty (TC005)", "Võ Thị My", "26/06/2025 13:00", "Nôn mửa, bỏ ăn", "Viêm dạ dày", "6.0", "BS. Võ Thanh Tùng"),
            new BanGhiKham("HS006", "Boss (TC006)", "Đặng Văn Nghĩa", "01/07/2025 14:00", "Ngứa, rụng lông", "Viêm da dị ứng", "6.8", "BS. Đặng Thị Yến"),
            new BanGhiKham("HS007", "Susu (TC007)", "Bùi Thị Oanh", "06/07/2025 15:00", "Tiêu chảy, mệt mỏi", "Rối loạn tiêu hóa", "7.6", "BS. Bùi Văn Khánh"),
            new BanGhiKham("HS008", "Nâu (TC008)", "Hồ Văn Phong", "11/07/2025 16:00", "Ho, chảy nước mũi", "Viêm đường hô hấp trên", "8.4", "BS. Hồ Thị Diệp")
    };

    // --- Tiêu đề cột hiển thị trên bảng: ĐÃ LƯỢC BỎ Triệu chứng, Chẩn đoán, Bác sĩ, Thao tác ---
    private final String[] tieuDeCot = { "Mã hồ sơ", "Thú cưng", "Chủ nuôi", "Ngày khám", "Cân nặng (kg)" };

    private final String[] dsBacSiLoc = { "Bác sĩ: Tất cả", "Nguyễn Hữu Tài", "Trần Thị Uyên", "Lê Minh Vinh" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongLuotKham;
    private JLabel nhanSoKhamHomNay;
    private JLabel nhanSoKhamThangNay;
    private JLabel nhanSoBacSi;

    // --- Phần thẻ chứa bảng & Cụm nút thao tác ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboBacSi;
    private JComboBox<String> cboThoiGian;
    private NutBoTron nutThemLuotKham;
    private NutBoTron nutXemChiTiet;
    private NutBoTron nutChinhSua;
    private NutBoTron nutXoa;
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

        nhanTieuDe = new JLabel("Lịch sử khám bệnh");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Theo dõi lịch sử khám bệnh, chẩn đoán và phác đồ điều trị của từng thú cưng.");
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

        panelTieuDe.add(panelChu, BorderLayout.WEST);
    }

    // =========================================================================
    // PHẦN 2: 4 THẺ THỐNG KÊ
    // =========================================================================
    private void khoiTaoThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH_THE, 0));
        panelThongKe.setOpaque(false);
        panelThongKe.setPreferredSize(new Dimension(0, CHIEU_CAO_THE_THONG_KE));

        nhanSoTongLuotKham = new JLabel("256");
        nhanSoKhamHomNay = new JLabel("14");
        nhanSoKhamThangNay = new JLabel("86");
        nhanSoBacSi = new JLabel("8");

        panelThongKe.add(taoTheThongKe("LS", "Tổng lượt khám", nhanSoTongLuotKham, "Tất cả hồ sơ"));
        panelThongKe.add(taoTheThongKe("HN", "Khám hôm nay", nhanSoKhamHomNay, "Lượt trong ngày"));
        panelThongKe.add(taoTheThongKe("TN", "Khám tháng này", nhanSoKhamThangNay, "+12% so với tháng trước"));
        panelThongKe.add(taoTheThongKe("BS", "Bác sĩ phụ trách", nhanSoBacSi, "Đang trực khám"));
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

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã hồ sơ, thú cưng, chủ nuôi...");
        txtTimKiem.setPreferredSize(new Dimension(280, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(280, CHIEU_CAO_O_NHAP));

        cboBacSi = taoOChon(dsBacSiLoc, 160);
        cboThoiGian = taoOChon(dsThoiGianLoc, 140);

        // CỤM NÚT THAO TÁC ĐỘC LẬP
        nutThemLuotKham = new NutBoTron("+ Thêm lượt khám", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutThemLuotKham.setPreferredSize(new Dimension(140, CHIEU_CAO_O_NHAP));

        nutXemChiTiet = new NutBoTron("Xem chi tiết", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXemChiTiet.setPreferredSize(new Dimension(110, CHIEU_CAO_O_NHAP));

        nutChinhSua = new NutBoTron("Chỉnh sửa", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutChinhSua.setPreferredSize(new Dimension(100, CHIEU_CAO_O_NHAP));

        nutXoa = new NutBoTron("Xóa", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_NGUY_HIEM_NEN, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_NGUY_HIEM_CHU, 8);
        nutXoa.setPreferredSize(new Dimension(75, CHIEU_CAO_O_NHAP));

        nutXuatDanhSach = new NutBoTron("Xuất Excel", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(110, CHIEU_CAO_O_NHAP));

        // Mặc định disable các nút tác động theo dòng
        nutXemChiTiet.setEnabled(false);
        nutChinhSua.setEnabled(false);
        nutXoa.setEnabled(false);

        panelCongCu.add(txtTimKiem);
        panelCongCu.add(Box.createHorizontalStrut(10));
        panelCongCu.add(cboBacSi);
        panelCongCu.add(Box.createHorizontalStrut(10));
        panelCongCu.add(cboThoiGian);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutThemLuotKham);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXemChiTiet);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutChinhSua);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXoa);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
        Object[][] duLieuBang = new Object[danhSachBanGhi.length][5];
        for (int i = 0; i < danhSachBanGhi.length; i++) {
            BanGhiKham g = danhSachBanGhi[i];
            duLieuBang[i][0] = g.maHS;
            duLieuBang[i][1] = g.thuCung;
            duLieuBang[i][2] = g.chuNuoi;
            duLieuBang[i][3] = g.ngayKham;
            duLieuBang[i][4] = g.canNang + " kg";
        }

        bangLichSuKham = new TableMau(tieuDeCot, duLieuBang);
        bangLichSuKham.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] doRongCot = { 130, 200, 240, 200, 160 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangLichSuKham.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }

        // Lắng nghe chọn dòng: chỉ bật Xem/Sửa/Xóa khi người dùng click chọn 1 dòng
        bangLichSuKham.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangLichSuKham.getSelectedRow() != -1;
                nutXemChiTiet.setEnabled(coChon);
                nutChinhSua.setEnabled(coChon);
                nutXoa.setEnabled(coChon);
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
    // CÀI ĐẶT SỰ KIỆN CHO CỤM NÚT THAO TÁC & DIALOG CHI TIẾT
    // =========================================================================
    private void caiDatSuKien() {
        nutThemLuotKham.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Mở form ghi nhận lượt khám bệnh mới.", "Thêm lượt khám",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Bấm nút Xem chi tiết -> Mở JDialog hiển thị 3 thông tin vừa được ẩn khỏi bảng
        nutXemChiTiet.addActionListener(e -> hienThiDialogChiTietKham());

        nutChinhSua.addActionListener(e -> {
            int row = bangLichSuKham.getSelectedRow();
            if (row != -1) {
                String maHS = bangLichSuKham.getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(this, "Mở form cập nhật kết quả khám cho hồ sơ: " + maHS,
                        "Chỉnh sửa", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        nutXoa.addActionListener(e -> {
            int row = bangLichSuKham.getSelectedRow();
            if (row != -1) {
                String maHS = bangLichSuKham.getValueAt(row, 0).toString();
                int hoi = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa hồ sơ khám \"" + maHS + "\" không?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (hoi == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Đã xóa lượt khám thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        nutXuatDanhSach.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu lịch sử khám bệnh sang Excel thành công!",
                    "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Dialog chi tiết lượt khám: Hiển thị đầy đủ thông tin, đặc biệt là 3 thông tin
     * được ẩn khỏi bảng: Triệu chứng, Chẩn đoán, và Bác sĩ phụ trách.
     */
    private void hienThiDialogChiTietKham() {
        int row = bangLichSuKham.getSelectedRow();
        if (row == -1 || row >= danhSachBanGhi.length) return;

        BanGhiKham bg = danhSachBanGhi[row];

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết lượt khám bệnh - SALALA", true);
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);

        JPanel pContent = new JPanel(new BorderLayout(0, 16));
        pContent.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        pContent.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header dialog
        JPanel pHead = new JPanel(new BorderLayout());
        pHead.setOpaque(false);

        JLabel lblTitle = new JLabel("CHI TIẾT LƯỢT KHÁM BỆNH");
        lblTitle.setFont(RangBuocGiaoDien.fontDam(18));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        JLabel lblMa = new JLabel("Mã hồ sơ: " + bg.maHS);
        lblMa.setFont(RangBuocGiaoDien.fontDam(13));
        lblMa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pHead.add(lblTitle, BorderLayout.WEST);
        pHead.add(lblMa, BorderLayout.EAST);

        // Body chứa 2 thẻ thông tin
        JPanel pBody = new JPanel();
        pBody.setLayout(new BoxLayout(pBody, BoxLayout.Y_AXIS));
        pBody.setOpaque(false);

        // Thẻ 1: Thông tin cơ bản
        PanelBoTron card1 = new PanelBoTron(new GridLayout(4, 2, 8, 10), 10, Color.WHITE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card1.setBorder(new EmptyBorder(14, 18, 14, 18));
        themDongThongTin(card1, "Thú cưng:", bg.thuCung);
        themDongThongTin(card1, "Chủ nuôi:", bg.chuNuoi);
        themDongThongTin(card1, "Thời gian khám:", bg.ngayKham);
        themDongThongTin(card1, "Cân nặng:", bg.canNang + " kg");

        // Thẻ 2: 3 Thông tin trọng tâm được đưa từ bảng vào Dialog
        PanelBoTron card2 = new PanelBoTron(new BorderLayout(0, 10), 10, Color.WHITE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card2.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel pChuyenKhoa = new JPanel(new GridLayout(3, 1, 0, 10));
        pChuyenKhoa.setOpaque(false);

        // Bác sĩ phụ trách
        JPanel pBS = taoDongThongTinNoiBat("Bác sĩ phụ trách:", bg.bacSi, RangBuocGiaoDien.MAU_CHU_DAO);
        // Triệu chứng
        JPanel pTC = taoDongThongTinNoiBat("Triệu chứng lâm sàng:", bg.trieuChung, RangBuocGiaoDien.MAU_CHU_CHINH);
        // Chẩn đoán
        JPanel pCD = taoDongThongTinNoiBat("Chẩn đoán y khoa:", bg.chanDoan, RangBuocGiaoDien.MAU_CANH_BAO_CHU);

        pChuyenKhoa.add(pBS);
        pChuyenKhoa.add(pTC);
        pChuyenKhoa.add(pCD);
        card2.add(pChuyenKhoa, BorderLayout.CENTER);

        pBody.add(card1);
        pBody.add(Box.createVerticalStrut(12));
        pBody.add(card2);

        // Footer nút Đóng
        NutBoTron btnDong = new NutBoTron("Đóng", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        btnDong.setPreferredSize(new Dimension(100, 36));
        btnDong.addActionListener(e -> dialog.dispose());

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pBtn.setOpaque(false);
        pBtn.add(btnDong);

        pContent.add(pHead, BorderLayout.NORTH);
        pContent.add(pBody, BorderLayout.CENTER);
        pContent.add(pBtn, BorderLayout.SOUTH);

        dialog.setContentPane(pContent);
        dialog.setVisible(true);
    }

    private void themDongThongTin(JPanel panel, String tieuDe, String giaTri) {
        JLabel lblT = new JLabel(tieuDe);
        lblT.setFont(RangBuocGiaoDien.fontThuong(12));
        lblT.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblV = new JLabel(giaTri);
        lblV.setFont(RangBuocGiaoDien.fontDam(13));
        lblV.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        panel.add(lblT);
        panel.add(lblV);
    }

    private JPanel taoDongThongTinNoiBat(String tieuDe, String noiDung, Color mauNoiBat) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);

        JLabel lblT = new JLabel(tieuDe);
        lblT.setFont(RangBuocGiaoDien.fontThuong(12));
        lblT.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblV = new JLabel(noiDung);
        lblV.setFont(RangBuocGiaoDien.fontDam(13));
        lblV.setForeground(mauNoiBat);

        p.add(lblT, BorderLayout.NORTH);
        p.add(lblV, BorderLayout.CENTER);
        return p;
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
    // GETTERS
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
