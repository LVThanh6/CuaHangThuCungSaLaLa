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
import javax.swing.table.TableCellRenderer;

/**
 * Class DonThuocGUI: Giao diện "Đơn thuốc" (thay thế giao diện Loại thú cưng).
 * Chỉ dựng giao diện, chưa xử lý nghiệp vụ.
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

    // --- Dữ liệu hiển thị mẫu (chỉ để dựng giao diện) ---
    private final String[] tieuDeCot = { "Mã đơn thuốc", "Thú cưng", "Mã hồ sơ", "Bác sĩ kê đơn", "Ngày kê đơn",
            "Số loại thuốc", "Ghi chú", "Trạng thái", "Thao tác" };
    private final Object[][] duLieuMau = {
            { "DT001", "Mimi (TC001)", "HS001", "Nguyễn Hữu Tài", "06/06/2025 09:00", "2", "Theo dõi ăn uống", "Chờ lấy thuốc", "" },
            { "DT002", "Lucky (TC002)", "HS002", "Trần Thị Uyên", "11/06/2025 10:00", "1", "Cho uống đủ liều", "Hoàn thành", "" },
            { "DT003", "Bông (TC003)", "HS003", "Lê Minh Vinh", "16/06/2025 11:00", "3", "Tái khám sau 7 ngày", "Đã cấp thuốc", "" },
            { "DT004", "Milo (TC004)", "HS004", "Phạm Thị Xuân", "21/06/2025 12:00", "2", "Theo dõi ăn uống", "Chờ lấy thuốc", "" },
            { "DT005", "Kitty (TC005)", "HS005", "Võ Thanh Tùng", "26/06/2025 13:00", "1", "Cho uống đủ liều", "Hoàn thành", "" },
            { "DT006", "Boss (TC006)", "HS006", "Đặng Thị Yến", "01/07/2025 14:00", "2", "Tái khám sau 7 ngày", "Đã cấp thuốc", "" },
            { "DT007", "Susu (TC007)", "HS007", "Bùi Văn Khánh", "06/07/2025 15:00", "1", "Theo dõi ăn uống", "Chờ lấy thuốc", "" },
            { "DT008", "Nâu (TC008)", "HS008", "Hồ Thị Diệp", "11/07/2025 16:00", "4", "Cho uống đủ liều", "Hoàn thành", "" }
    };
    private final String[] dsTrangThaiLoc = { "Trạng thái: Tất cả", "Đã cấp thuốc", "Chờ lấy thuốc", "Hoàn thành" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

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
    private JLabel nhanSoHoanThanh;

    // --- Phần thẻ chứa bảng ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboThoiGian;
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

        nhanSoTongDon = new JLabel("15");
        nhanSoDaCapThuoc = new JLabel("5");
        nhanSoChoLayThuoc = new JLabel("5");
        nhanSoHoanThanh = new JLabel("5");

        panelThongKe.add(taoTheThongKe("ĐT", "Tổng đơn thuốc", nhanSoTongDon, "Đơn đã kê"));
        panelThongKe.add(taoTheThongKe("CT", "Đã cấp thuốc", nhanSoDaCapThuoc, "Đã giao thuốc"));
        panelThongKe.add(taoTheThongKe("CL", "Chờ lấy thuốc", nhanSoChoLayThuoc, "Cần xử lý"));
        panelThongKe.add(taoTheThongKe("HT", "Hoàn thành", nhanSoHoanThanh, "Đã kết thúc"));
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

        nutXemChiTiet = taoNutLienKet("Xem chi tiết", RangBuocGiaoDien.MAU_CHU_DAO);
        nutChinhSua = taoNutLienKet("Chỉnh sửa", RangBuocGiaoDien.MAU_CHU_DAO);
        nutXoa = taoNutLienKet("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);

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
        bangDonThuoc = new TableMau(tieuDeCot, duLieuMau);

        int cotTrangThai = tieuDeCot.length - 2;
        int cotThaoTac = tieuDeCot.length - 1;
        int[] doRongCot = { 100, 120, 80, 150, 140, 100, 160, 130, 140 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangDonThuoc.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
        bangDonThuoc.getColumnModel().getColumn(cotTrangThai).setCellRenderer(new OTrangThai(bangDonThuoc));
        bangDonThuoc.getColumnModel().getColumn(cotThaoTac).setCellRenderer(new OThaoTac(bangDonThuoc));

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
                case "Hoàn thành":
                    mauNen = RangBuocGiaoDien.MAU_THANH_CONG_NEN;
                    mauChu = RangBuocGiaoDien.MAU_THANH_CONG_CHU;
                    break;
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
    // LỚP PHỤ TRỢ: Renderer cột "Thao tác" (Xem | Sửa | Xóa)
    // =========================================================================
    private static class OThaoTac extends JPanel implements TableCellRenderer {
        private final JTable bang;

        OThaoTac(JTable bang) {
            super(new FlowLayout(FlowLayout.LEFT, 14, 0));
            this.bang = bang;
            setBorder(new EmptyBorder(12, 12, 0, 0));
            add(taoNhan("Xem", RangBuocGiaoDien.MAU_CHU_DAO));
            add(taoNhan("Sửa", RangBuocGiaoDien.MAU_THONG_TIN_CHU));
            add(taoNhan("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU));
        }

        private JLabel taoNhan(String chu, Color mau) {
            JLabel nhan = new JLabel(chu);
            nhan.setFont(RangBuocGiaoDien.fontDam(12));
            nhan.setForeground(mau);
            return nhan;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(bang.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            }
            return this;
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
