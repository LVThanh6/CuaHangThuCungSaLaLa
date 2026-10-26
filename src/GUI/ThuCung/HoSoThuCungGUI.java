package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Class HoSoThuCungGUI: Giao diện "Danh sách thú cưng" (Tab Thú cưng ➔ Hồ sơ thú cưng).
 * Chuẩn thiết kế mới:
 * - Loại bỏ hoàn toàn cột "Thao tác" khỏi bảng.
 * - Toolbar trên bảng tích hợp cụm nút: "+ Thêm thú cưng", "Xem chi tiết", "Chỉnh sửa", "Xóa", "Xuất Excel".
 * - Các nút Xem/Sửa/Xóa mặc định bị disable, chỉ bật khi chọn dòng trên bảng qua ListSelectionListener.
 */
public class HoSoThuCungGUI extends JPanel {

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
    private final String[] tieuDeCot = { "Mã thú cưng", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh",
            "Chủ nuôi", "SĐT chủ", "Đặc điểm nhận diện" };
    private final Object[][] duLieuMau = {
            { "TC001", "Mimi", "Mèo", "Đực", "12/05/2022", "Nguyễn Thị Hoa", "0904000001", "Lông trắng, mắt xanh" },
            { "TC002", "Lucky", "Chó", "Cái", "20/08/2023", "Trần Văn Hùng", "0904000002", "Lông vàng, đuôi ngắn" },
            { "TC003", "Bông", "Mèo", "Đực", "03/02/2023", "Lê Thị Kim", "0904000003", "Đốm đen ở tai trái" },
            { "TC004", "Milo", "Thỏ", "Cái", "15/11/2023", "Phạm Văn Lâm", "0904000004", "Lông xám, khoang trắng ở ngực" },
            { "TC005", "Kitty", "Hamster", "Đực", "22/06/2022", "Võ Thị My", "0904000005", "Lông trắng, mắt xanh" },
            { "TC006", "Boss", "Chó", "Cái", "08/09/2023", "Đặng Văn Nghĩa", "0904000006", "Lông vàng, đuôi ngắn" },
            { "TC007", "Susu", "Mèo", "Đực", "14/03/2024", "Bùi Thị Oanh", "0904000007", "Đốm đen ở tai trái" },
            { "TC008", "Nâu", "Chó", "Cái", "10/01/2023", "Hồ Văn Phong", "0904000008", "Lông xám, khoang trắng ở ngực" }
    };
    private final String[] dsLoaiLoc = { "Loài: Tất cả", "Chó", "Mèo", "Thỏ", "Hamster" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongThuCung;
    private JLabel nhanSoCho;
    private JLabel nhanSoMeo;
    private JLabel nhanSoLoaiKhac;

    // --- Phần thẻ chứa bảng & Cụm nút thao tác độc lập ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboLoai;
    private NutBoTron nutThemThuCung;
    private NutBoTron nutXemHoSo;
    private NutBoTron nutChinhSua;
    private NutBoTron nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangThuCung;
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
    public HoSoThuCungGUI() {
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

        nhanDuongDan = new JLabel("Thú cưng / Danh sách thú cưng");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Danh sách thú cưng");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Quản lý hồ sơ thú cưng, thông tin chủ nuôi và tình trạng sức khỏe.");
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

        nhanSoTongThuCung = new JLabel("128");
        nhanSoCho = new JLabel("72");
        nhanSoMeo = new JLabel("48");
        nhanSoLoaiKhac = new JLabel("8");

        panelThongKe.add(taoTheThongKe("TC", "Tổng số thú cưng", nhanSoTongThuCung, "Hồ sơ đã đăng ký"));
        panelThongKe.add(taoTheThongKe("C", "Chó", nhanSoCho, "56,3% tổng hồ sơ"));
        panelThongKe.add(taoTheThongKe("M", "Mèo", nhanSoMeo, "37,5% tổng hồ sơ"));
        panelThongKe.add(taoTheThongKe("K", "Loài khác", nhanSoLoaiKhac, "Thỏ và thú cưng khác"));
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

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã thú cưng, tên thú cưng, chủ nuôi...");
        txtTimKiem.setPreferredSize(new Dimension(300, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(300, CHIEU_CAO_O_NHAP));

        cboLoai = new JComboBox<>(dsLoaiLoc);
        cboLoai.setFont(RangBuocGiaoDien.fontThuong(13));
        cboLoai.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboLoai.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboLoai.setPreferredSize(new Dimension(170, CHIEU_CAO_O_NHAP));
        cboLoai.setMaximumSize(new Dimension(170, CHIEU_CAO_O_NHAP));

        // CỤM NÚT THAO TÁC ĐỘC LẬP THEO QUY TẮC CHUẨN
        nutThemThuCung = new NutBoTron("+ Thêm mới", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutThemThuCung.setPreferredSize(new Dimension(125, CHIEU_CAO_O_NHAP));

        nutXemHoSo = new NutBoTron("Xem chi tiết", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXemHoSo.setPreferredSize(new Dimension(110, CHIEU_CAO_O_NHAP));

        nutChinhSua = new NutBoTron("Chỉnh sửa", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutChinhSua.setPreferredSize(new Dimension(100, CHIEU_CAO_O_NHAP));

        nutXoa = new NutBoTron("Xóa", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_NGUY_HIEM_NEN, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_NGUY_HIEM_CHU, 8);
        nutXoa.setPreferredSize(new Dimension(80, CHIEU_CAO_O_NHAP));

        nutXuatDanhSach = new NutBoTron("Xuất Excel", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(110, CHIEU_CAO_O_NHAP));

        // Mặc định các nút tác động theo dòng bị disable
        nutXemHoSo.setEnabled(false);
        nutChinhSua.setEnabled(false);
        nutXoa.setEnabled(false);

        panelCongCu.add(txtTimKiem);
        panelCongCu.add(Box.createHorizontalStrut(12));
        panelCongCu.add(cboLoai);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutThemThuCung);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXemHoSo);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutChinhSua);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXoa);
        panelCongCu.add(Box.createHorizontalStrut(8));
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
        bangThuCung = new TableMau(tieuDeCot, duLieuMau);
        bangThuCung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] doRongCot = { 110, 130, 90, 90, 110, 170, 120, 280 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangThuCung.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }

        // Lắng nghe chọn dòng: chỉ bật Xem/Sửa/Xóa khi có dòng được chọn
        bangThuCung.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangThuCung.getSelectedRow() != -1;
                nutXemHoSo.setEnabled(coChon);
                nutChinhSua.setEnabled(coChon);
                nutXoa.setEnabled(coChon);
            }
        });

        thanhCuonBang = new JScrollPane(bangThuCung);
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

        nhanThongTinTrang = new JLabel("Hiển thị 1–8 trong 128 thú cưng");
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
        nutTrangCuoi = taoNutTrang("16", false);
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
        nutThemThuCung.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Mở form thêm mới hồ sơ thú cưng.", "Thêm thú cưng",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        nutXemHoSo.addActionListener(e -> hienThiDialogChiTietThuCung());

        nutChinhSua.addActionListener(e -> {
            int row = bangThuCung.getSelectedRow();
            if (row != -1) {
                String maTC = bangThuCung.getValueAt(row, 0).toString();
                String tenTC = bangThuCung.getValueAt(row, 1).toString();
                JOptionPane.showMessageDialog(this, "Mở form chỉnh sửa thông tin cho thú cưng: " + tenTC + " (" + maTC + ")",
                        "Chỉnh sửa", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        nutXoa.addActionListener(e -> {
            int row = bangThuCung.getSelectedRow();
            if (row != -1) {
                String tenTC = bangThuCung.getValueAt(row, 1).toString();
                int hoi = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa hồ sơ thú cưng \"" + tenTC + "\" không?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (hoi == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ thú cưng thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        nutXuatDanhSach.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu danh sách thú cưng sang file Excel thành công!",
                    "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void hienThiDialogChiTietThuCung() {
        int row = bangThuCung.getSelectedRow();
        if (row == -1) return;

        String ma = bangThuCung.getValueAt(row, 0).toString();
        String ten = bangThuCung.getValueAt(row, 1).toString();
        String loai = bangThuCung.getValueAt(row, 2).toString();
        String gioiTinh = bangThuCung.getValueAt(row, 3).toString();
        String ngaySinh = bangThuCung.getValueAt(row, 4).toString();
        String chuNuoi = bangThuCung.getValueAt(row, 5).toString();
        String sdt = bangThuCung.getValueAt(row, 6).toString();
        String dacDiem = bangThuCung.getValueAt(row, 7).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hồ sơ chi tiết thú cưng", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel pContent = new JPanel(new BorderLayout(0, 16));
        pContent.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        pContent.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitle = new JLabel("HỒ SƠ THÚ CƯNG: " + ten.toUpperCase());
        lblTitle.setFont(RangBuocGiaoDien.fontDam(18));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        PanelBoTron card = new PanelBoTron(new GridLayout(8, 2, 8, 10), 10, Color.WHITE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        themDongThongTin(card, "Mã thú cưng:", ma);
        themDongThongTin(card, "Tên thú cưng:", ten);
        themDongThongTin(card, "Loài:", loai);
        themDongThongTin(card, "Giới tính:", gioiTinh);
        themDongThongTin(card, "Ngày sinh:", ngaySinh);
        themDongThongTin(card, "Chủ nuôi:", chuNuoi);
        themDongThongTin(card, "Số điện thoại:", sdt);
        themDongThongTin(card, "Đặc điểm nhận diện:", dacDiem);

        NutBoTron btnDong = new NutBoTron("Đóng", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        btnDong.setPreferredSize(new Dimension(100, 36));
        btnDong.addActionListener(e -> dialog.dispose());

        JPanel pBtn = new FlowLayout(FlowLayout.RIGHT, 0, 0) == null ? new JPanel() : new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
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
    public TableMau getBangThuCung() {
        return bangThuCung;
    }

    public OTimKiemGoiY getTxtTimKiem() {
        return txtTimKiem;
    }

    public JComboBox<String> getCboLoai() {
        return cboLoai;
    }

    public JButton getNutThemThuCung() {
        return nutThemThuCung;
    }

    // =========================================================================
    // CÁC LỚP PHỤ TRỢ GIAO DIỆN
    // =========================================================================

    /** Panel nền bo góc, có đường viền. */
    public static class PanelBoTron extends JPanel {
        private final int banKinh;
        private final Color mauNen;
        private final Color mauVien;

        public PanelBoTron(LayoutManager boCuc, int banKinh, Color mauNen, Color mauVien) {
            super(boCuc);
            this.banKinh = banKinh;
            this.mauNen = mauNen;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(mauNen);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút bo góc, có hiệu ứng rê chuột và hỗ trợ trạng thái disabled mượt mà. */
    public static class NutBoTron extends JButton {
        private final Color mauNen;
        private final Color mauReChuot;
        private final Color mauVien;
        private final int banKinh;
        private boolean dangReChuot = false;

        public NutBoTron(String chu, Color mauNen, Color mauReChuot, Color mauVien, Color mauChu, int banKinh) {
            super(chu);
            this.mauNen = mauNen;
            this.mauReChuot = mauReChuot;
            this.mauVien = mauVien;
            this.banKinh = banKinh;
            setForeground(mauChu);
            setFont(RangBuocGiaoDien.fontDam(13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        dangReChuot = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dangReChuot = false;
                    repaint();
                }
            });
        }

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setCursor(b ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            if (!isEnabled()) {
                g2.setColor(RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
                g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            } else {
                g2.setColor(dangReChuot ? mauReChuot : mauNen);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
                if (mauVien != null) {
                    g2.setColor(mauVien);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Ô nhập có chữ gợi ý (placeholder). */
    public static class OTimKiemGoiY extends JTextField {
        private final String goiY;

        public OTimKiemGoiY(String goiY) {
            this.goiY = goiY;
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 12, 0, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                g2.setFont(getFont());
                Insets le = getInsets();
                int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                g2.drawString(goiY, le.left, y);
                g2.dispose();
            }
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
            JFrame frame = new JFrame("Kiểm thử độc lập - HoSoThuCungGUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new HoSoThuCungGUI());
            GiaoDienChinhGUI.caiDatKichThuocCuaSo(frame, false);
            frame.setVisible(true);
        });
    }
}
