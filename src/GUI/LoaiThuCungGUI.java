package GUI;

import GUI.HoSoThuCungGUI.NutBoTron;
import GUI.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.HoSoThuCungGUI.PanelBoTron;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Class LoaiThuCungGUI: Giao diện "Loại thú cưng" (Tab Thú cưng ➔ Loại thú cưng).
 * Chỉ dựng giao diện, chưa xử lý nghiệp vụ.
 * Database chưa có bảng riêng cho loại, nên màn hình thống kê theo cột ThuCung.loai
 * (số lượng, giới tính đực/cái) và số lượt khám lấy từ HoSoSucKhoe.
 * Dùng lại các thành phần PanelBoTron, NutBoTron, OTimKiemGoiY của HoSoThuCungGUI.
 */
public class LoaiThuCungGUI extends JPanel {

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
    private final String[] tieuDeCot = { "STT", "Tên loại", "Số thú cưng", "Đực / Cái", "Tỷ lệ",
            "Số lượt khám", "Thao tác" };
    private final Object[][] duLieuMau = {
            { "1", "Chó", "72", "40 / 32", "56,3%", "120", "" },
            { "2", "Mèo", "48", "22 / 26", "37,5%", "96", "" },
            { "3", "Thỏ", "5", "2 / 3", "3,9%", "9", "" },
            { "4", "Hamster", "3", "1 / 2", "2,3%", "4", "" }
    };
    private final String[] dsSapXep = { "Sắp xếp: Số lượng giảm dần", "Số lượng tăng dần", "Tên loại A-Z" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBoTron nutThemLoai;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongLoai;
    private JLabel nhanLoaiPhoBien;
    private JLabel nhanSoTongThuCung;
    private JLabel nhanSoLuotKham;

    // --- Phần thẻ chứa bảng ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboSapXep;
    private JButton nutChinhSua;
    private JButton nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangLoaiThuCung;
    private JScrollPane thanhCuonBang;

    // --- Phần phân trang ---
    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;
    private JLabel nhanSoDongTrang;
    private JComboBox<String> cboSoDongTrang;
    private NutBoTron nutTrangTruoc;
    private NutBoTron nutTrang1;
    private NutBoTron nutTrangSau;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public LoaiThuCungGUI() {
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

        nhanDuongDan = new JLabel("Thú cưng / Loại thú cưng");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Loại thú cưng");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Thống kê các loài thú cưng đang được chăm sóc tại cửa hàng và phòng khám.");
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

        nutThemLoai = new NutBoTron("+  Thêm loại", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutThemLoai.setPreferredSize(new Dimension(132, 40));

        JPanel panelNut = new JPanel(new GridBagLayout());
        panelNut.setOpaque(false);
        panelNut.add(nutThemLoai);

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

        nhanSoTongLoai = new JLabel("4");
        nhanLoaiPhoBien = new JLabel("Chó");
        nhanSoTongThuCung = new JLabel("128");
        nhanSoLuotKham = new JLabel("229");

        panelThongKe.add(taoTheThongKe("L", "Tổng số loại", nhanSoTongLoai, "Loại đang quản lý"));
        panelThongKe.add(taoTheThongKe("PB", "Loại phổ biến nhất", nhanLoaiPhoBien, "72 thú cưng"));
        panelThongKe.add(taoTheThongKe("TC", "Tổng thú cưng", nhanSoTongThuCung, "Thuộc mọi loại"));
        panelThongKe.add(taoTheThongKe("K", "Tổng lượt khám", nhanSoLuotKham, "Theo hồ sơ sức khỏe"));
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

        txtTimKiem = new OTimKiemGoiY("Tìm theo tên loại thú cưng...");
        txtTimKiem.setPreferredSize(new Dimension(400, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(400, CHIEU_CAO_O_NHAP));

        cboSapXep = new JComboBox<>(dsSapXep);
        cboSapXep.setFont(RangBuocGiaoDien.fontThuong(13));
        cboSapXep.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboSapXep.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboSapXep.setPreferredSize(new Dimension(240, CHIEU_CAO_O_NHAP));
        cboSapXep.setMaximumSize(new Dimension(240, CHIEU_CAO_O_NHAP));

        nutChinhSua = taoNutLienKet("Chỉnh sửa", RangBuocGiaoDien.MAU_CHU_DAO);
        nutXoa = taoNutLienKet("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);

        nutXuatDanhSach = new NutBoTron("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(154, CHIEU_CAO_O_NHAP));
        nutXuatDanhSach.setMaximumSize(new Dimension(154, CHIEU_CAO_O_NHAP));

        panelCongCu.add(txtTimKiem);
        panelCongCu.add(Box.createHorizontalStrut(12));
        panelCongCu.add(cboSapXep);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutChinhSua);
        panelCongCu.add(Box.createHorizontalStrut(14));
        panelCongCu.add(nutXoa);
        panelCongCu.add(Box.createHorizontalStrut(40));
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
        bangLoaiThuCung = new TableMau(tieuDeCot, duLieuMau);

        int cotThaoTac = tieuDeCot.length - 1;
        int[] doRongCot = { 60, 200, 130, 130, 110, 130, 140 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangLoaiThuCung.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
        bangLoaiThuCung.getColumnModel().getColumn(cotThaoTac).setCellRenderer(new OThaoTac(bangLoaiThuCung));

        thanhCuonBang = new JScrollPane(bangLoaiThuCung);
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

        nhanThongTinTrang = new JLabel("Hiển thị 1–4 trong 4 loại thú cưng");
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
        nutTrangSau = taoNutTrang(">", false);

        JPanel panelBenPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 11));
        panelBenPhai.setOpaque(false);
        panelBenPhai.add(nhanSoDongTrang);
        panelBenPhai.add(cboSoDongTrang);
        panelBenPhai.add(Box.createHorizontalStrut(10));
        panelBenPhai.add(nutTrangTruoc);
        panelBenPhai.add(nutTrang1);
        panelBenPhai.add(nutTrangSau);

        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
        panelPhanTrang.add(panelBenPhai, BorderLayout.EAST);
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TẠO THÀNH PHẦN
    // =========================================================================
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
        nut.setPreferredSize(new Dimension(34, 34));
        return nut;
    }

    // =========================================================================
    // GETTERS (để nhóm làm chức năng truy cập khi cần)
    // =========================================================================
    public TableMau getBangLoaiThuCung() {
        return bangLoaiThuCung;
    }

    public OTimKiemGoiY getTxtTimKiem() {
        return txtTimKiem;
    }

    public JButton getNutThemLoai() {
        return nutThemLoai;
    }

    // =========================================================================
    // LỚP PHỤ TRỢ: Renderer cột "Thao tác" (Sửa | Xóa)
    // =========================================================================
    private static class OThaoTac extends JPanel implements TableCellRenderer {
        private final JTable bang;

        OThaoTac(JTable bang) {
            super(new FlowLayout(FlowLayout.LEFT, 14, 0));
            this.bang = bang;
            setBorder(new EmptyBorder(12, 12, 0, 0));
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
            JFrame frame = new JFrame("Kiểm thử độc lập - LoaiThuCungGUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LoaiThuCungGUI());
            GiaoDienChinhGUI.caiDatKichThuocCuaSo(frame, false);
            frame.setVisible(true);
        });
    }
}
