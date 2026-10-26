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
 * Class DonThuocGUI: Giao diện "Đơn thuốc" (Tab Thú cưng ➔ Đơn thuốc).
 * Chuẩn thiết kế mới:
 * - Loại bỏ hoàn toàn cột "Thao tác" khỏi bảng.
 * - Toolbar tích hợp cụm nút: "+ Kê đơn thuốc", "Xem chi tiết", "Chỉnh sửa", "Xóa", "Xuất Excel".
 * - Các nút Xem/Sửa/Xóa mặc định bị disable, chỉ bật sáng khi click chọn 1 dòng trên bảng.
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

    // --- Dữ liệu hiển thị mẫu (8 cột, KHÔNG có cột Thao tác) ---
    private final String[] tieuDeCot = { "Mã đơn thuốc", "Thú cưng", "Mã hồ sơ", "Bác sĩ kê đơn", "Ngày kê đơn",
            "Số loại thuốc", "Ghi chú", "Trạng thái" };
    private final Object[][] duLieuMau = {
            { "DT001", "Mimi (TC001)", "HS001", "Nguyễn Hữu Tài", "06/06/2025 09:00", "2", "Theo dõi ăn uống", "Chờ lấy thuốc" },
            { "DT002", "Lucky (TC002)", "HS002", "Trần Thị Uyên", "11/06/2025 10:00", "1", "Cho uống đủ liều", "Hoàn thành" },
            { "DT003", "Bông (TC003)", "HS003", "Lê Minh Vinh", "16/06/2025 11:00", "3", "Tái khám sau 7 ngày", "Đã cấp thuốc" },
            { "DT004", "Milo (TC004)", "HS004", "Phạm Thị Xuân", "21/06/2025 12:00", "2", "Theo dõi ăn uống", "Chờ lấy thuốc" },
            { "DT005", "Kitty (TC005)", "HS005", "Võ Thanh Tùng", "26/06/2025 13:00", "1", "Cho uống đủ liều", "Hoàn thành" },
            { "DT006", "Boss (TC006)", "HS006", "Đặng Thị Yến", "01/07/2025 14:00", "2", "Tái khám sau 7 ngày", "Đã cấp thuốc" },
            { "DT007", "Susu (TC007)", "HS007", "Bùi Văn Khánh", "06/07/2025 15:00", "1", "Theo dõi ăn uống", "Chờ lấy thuốc" },
            { "DT008", "Nâu (TC008)", "HS008", "Hồ Thị Diệp", "11/07/2025 16:00", "4", "Cho uống đủ liều", "Hoàn thành" }
    };
    private final String[] dsTrangThaiLoc = { "Trạng thái: Tất cả", "Đã cấp thuốc", "Chờ lấy thuốc", "Hoàn thành" };
    private final String[] dsThoiGianLoc = { "Thời gian: Tất cả", "Hôm nay", "7 ngày qua", "Tháng này" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongDon;
    private JLabel nhanSoDaCapThuoc;
    private JLabel nhanSoChoLayThuoc;
    private JLabel nhanSoHoanThanh;

    // --- Phần thẻ chứa bảng & Cụm nút thao tác ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboThoiGian;
    private NutBoTron nutKeDonThuoc;
    private NutBoTron nutXemChiTiet;
    private NutBoTron nutChinhSua;
    private NutBoTron nutXoa;
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

        nhanTieuDe = new JLabel("Danh sách đơn thuốc");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Theo dõi lịch sử kê đơn thuốc, bác sĩ phụ trách và tình trạng phát thuốc.");
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

        nhanSoTongDon = new JLabel("142");
        nhanSoDaCapThuoc = new JLabel("98");
        nhanSoChoLayThuoc = new JLabel("12");
        nhanSoHoanThanh = new JLabel("32");

        panelThongKe.add(taoTheThongKe("DT", "Tổng số đơn", nhanSoTongDon, "Tất cả đơn thuốc"));
        panelThongKe.add(taoTheThongKe("ĐC", "Đã cấp thuốc", nhanSoDaCapThuoc, "Đã giao cho khách"));
        panelThongKe.add(taoTheThongKe("CL", "Chờ lấy thuốc", nhanSoChoLayThuoc, "Cần chuẩn bị gấp"));
        panelThongKe.add(taoTheThongKe("HT", "Hoàn thành", nhanSoHoanThanh, "Đã xong điều trị"));
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
        txtTimKiem.setPreferredSize(new Dimension(280, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(280, CHIEU_CAO_O_NHAP));

        cboTrangThai = taoOChon(dsTrangThaiLoc, 150);
        cboThoiGian = taoOChon(dsThoiGianLoc, 140);

        // CỤM NÚT THAO TÁC ĐỘC LẬP
        nutKeDonThuoc = new NutBoTron("+ Kê đơn thuốc", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutKeDonThuoc.setPreferredSize(new Dimension(135, CHIEU_CAO_O_NHAP));

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
        panelCongCu.add(cboTrangThai);
        panelCongCu.add(Box.createHorizontalStrut(10));
        panelCongCu.add(cboThoiGian);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutKeDonThuoc);
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
        bangDonThuoc = new TableMau(tieuDeCot, duLieuMau);
        bangDonThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int cotTrangThai = tieuDeCot.length - 1; // Cột số 7
        int[] doRongCot = { 110, 130, 90, 160, 150, 100, 180, 140 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangDonThuoc.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
        bangDonThuoc.getColumnModel().getColumn(cotTrangThai).setCellRenderer(new OTrangThai(bangDonThuoc));

        // Lắng nghe chọn dòng: chỉ bật nút khi người dùng click chọn 1 dòng
        bangDonThuoc.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangDonThuoc.getSelectedRow() != -1;
                nutXemChiTiet.setEnabled(coChon);
                nutChinhSua.setEnabled(coChon);
                nutXoa.setEnabled(coChon);
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

        nhanThongTinTrang = new JLabel("Hiển thị 1–8 trong 142 đơn thuốc");
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
        nutTrangCuoi = taoNutTrang("18", false);
        nutTrangSau = taoNutTrang(">", false);

        JPanel panelBenPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 11));
        panelBenPhai.setOpaque(false);
        panelBenPhai.add(nhanSoDongTrang);
        panelBenPhai.add(cboSoDongTrang);
        panelBenPhai.add(Box.createHorizontalStrut(10));
        panelBenPhai.add(nutTrangTruoc);
        panelBenPhai.add(nutTrang1);
        panelBenPhai.add(nutTrang2);
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
        nutKeDonThuoc.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Mở form lập đơn thuốc mới cho thú cưng.", "Kê đơn thuốc",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        nutXemChiTiet.addActionListener(e -> hienThiDialogChiTietDonThuoc());

        nutChinhSua.addActionListener(e -> {
            int row = bangDonThuoc.getSelectedRow();
            if (row != -1) {
                String maDT = bangDonThuoc.getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(this, "Mở form chỉnh sửa đơn thuốc: " + maDT,
                        "Chỉnh sửa", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        nutXoa.addActionListener(e -> {
            int row = bangDonThuoc.getSelectedRow();
            if (row != -1) {
                String maDT = bangDonThuoc.getValueAt(row, 0).toString();
                int hoi = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa đơn thuốc \"" + maDT + "\" không?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (hoi == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Đã xóa đơn thuốc thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        nutXuatDanhSach.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu danh sách đơn thuốc sang Excel thành công!",
                    "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void hienThiDialogChiTietDonThuoc() {
        int row = bangDonThuoc.getSelectedRow();
        if (row == -1) return;

        String maDT = bangDonThuoc.getValueAt(row, 0).toString();
        String thuCung = bangDonThuoc.getValueAt(row, 1).toString();
        String maHS = bangDonThuoc.getValueAt(row, 2).toString();
        String bacSi = bangDonThuoc.getValueAt(row, 3).toString();
        String ngayKe = bangDonThuoc.getValueAt(row, 4).toString();
        String soThuoc = bangDonThuoc.getValueAt(row, 5).toString();
        String ghiChu = bangDonThuoc.getValueAt(row, 6).toString();
        String trangThai = bangDonThuoc.getValueAt(row, 7).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết đơn thuốc", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel pContent = new JPanel(new BorderLayout(0, 16));
        pContent.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        pContent.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitle = new JLabel("CHI TIẾT ĐƠN THUỐC: " + maDT);
        lblTitle.setFont(RangBuocGiaoDien.fontDam(18));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        PanelBoTron card = new PanelBoTron(new GridLayout(8, 2, 8, 10), 10, Color.WHITE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        themDongThongTin(card, "Mã đơn thuốc:", maDT);
        themDongThongTin(card, "Thú cưng:", thuCung);
        themDongThongTin(card, "Mã hồ sơ:", maHS);
        themDongThongTin(card, "Bác sĩ kê đơn:", bacSi);
        themDongThongTin(card, "Ngày kê đơn:", ngayKe);
        themDongThongTin(card, "Số loại thuốc:", soThuoc);
        themDongThongTin(card, "Ghi chú điều trị:", ghiChu);
        themDongThongTin(card, "Trạng thái:", trangThai);

        NutBoTron btnDong = new NutBoTron("Đóng", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        btnDong.setPreferredSize(new Dimension(100, 36));
        btnDong.addActionListener(e -> dialog.dispose());

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pBtn.setOpaque(false);
        pBtn.add(btnDong);

        pContent.add(lblTitle, BorderLayout.NORTH);
        pContent.add(card, BorderLayout.CENTER);
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
    // LỚP PHỤ TRỢ: Renderer cột "Trạng thái" (dạng pill badge)
    // =========================================================================
    private static class OTrangThai extends JPanel implements TableCellRenderer {
        private final JTable bang;
        private String chu = "";
        private Color mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
        private Color mauChu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;

        OTrangThai(JTable bang) {
            this.bang = bang;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            this.chu = value != null ? value.toString() : "";
            if (isSelected) {
                setBackground(bang.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            }
            if ("Đã cấp thuốc".equals(chu)) {
                mauNen = RangBuocGiaoDien.MAU_THONG_TIN_NEN;
                mauChu = RangBuocGiaoDien.MAU_THONG_TIN_CHU;
            } else if ("Chờ lấy thuốc".equals(chu)) {
                mauNen = RangBuocGiaoDien.MAU_CANH_BAO_NEN;
                mauChu = RangBuocGiaoDien.MAU_CANH_BAO_CHU;
            } else if ("Hoàn thành".equals(chu)) {
                mauNen = RangBuocGiaoDien.MAU_THANH_CONG_NEN;
                mauChu = RangBuocGiaoDien.MAU_THANH_CONG_CHU;
            } else {
                mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
                mauChu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
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
