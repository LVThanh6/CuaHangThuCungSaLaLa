package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Giao diện Danh sách khách hàng:
 * Hiển thị thẻ thống kê, thanh tìm kiếm, bộ lọc, bảng dữ liệu và phân trang
 * Theo chuẩn thiết kế hệ thống SALALA Pet Shop từ Figma.
 */
public class DanhSachKhachHangGUI extends JPanel {

    // =========================================================================
    // 1. KHAI BÁO CÁC BIẾN GIAO DIỆN LÊN ĐẦU FILE
    // =========================================================================
    private JPanel panelTongThe;
    private JPanel panelPhanTren;
    private JPanel panelTieuDe;
    private JLabel lblDuongDan;
    private JLabel lblTieuDe;
    private JLabel lblMoTa;
    private JButton btnThemKhachHang;

    // Các thẻ thống kê số liệu
    private JPanel panelThongKe;
    private JPanel theTongKhachHang;
    private JPanel theKhachHangMoi;
    private JPanel theTongThuCung;
    private JPanel theThanhVienVang;

    // Thành phần bảng dữ liệu và bộ lọc
    private QuanLyTable panelQuanLyTable;
    private String[] danhSachCot;
    private Object[][] duLieuKhachHang;
    private String[] danhSachBoLoc;

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public DanhSachKhachHangGUI() {
        khoiTaoGiaoDien();
    }

    // =========================================================================
    // 3. CÁC HÀM XÂY DỰNG GIAO DIỆN
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // Khởi tạo khối phía trên (Breadcrumb + Tiêu đề + Nút thêm + 4 thẻ thống kê)
        khoiTaoPhanTren();
        add(panelPhanTren, BorderLayout.NORTH);

        // Khởi tạo khối bảng dữ liệu bên dưới
        khoiTaoBangDuLieu();
        add(panelQuanLyTable, BorderLayout.CENTER);
    }

    private void khoiTaoPhanTren() {
        panelPhanTren = new JPanel();
        panelPhanTren.setLayout(new BoxLayout(panelPhanTren, BoxLayout.Y_AXIS));
        panelPhanTren.setOpaque(false);

        // --- Hàng 1: Breadcrumb đường dẫn & Tiêu đề + Nút thêm ---
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);

        JPanel panelTrai = new JPanel();
        panelTrai.setLayout(new BoxLayout(panelTrai, BoxLayout.Y_AXIS));
        panelTrai.setOpaque(false);

        lblDuongDan = new JLabel("Khách hàng / Danh sách khách hàng");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        lblTieuDe = new JLabel("Danh sách khách hàng");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblMoTa = new JLabel("Quản lý thông tin liên hệ, thú cưng, hạng thành viên và lịch sử chi tiêu.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Nút "+ Thêm khách hàng"
        btnThemKhachHang = new JButton("+ Thêm khách hàng");
        btnThemKhachHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemKhachHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemKhachHang.setForeground(Color.WHITE);
        btnThemKhachHang.setFocusPainted(false);
        btnThemKhachHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemKhachHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnThemKhachHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- Hàng 2: 4 Thẻ thống kê số liệu ---
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        theTongKhachHang = taoTheThongKe("👤", "Tổng khách hàng", "96", "Toàn bộ hồ sơ");
        theKhachHangMoi = taoTheThongKe("➕", "Khách hàng mới", "12", "Trong tháng 10");
        theTongThuCung = taoTheThongKe("🐾", "Tổng thú cưng", "128", "1,3 thú cưng / khách");
        theThanhVienVang = taoTheThongKe("👑", "Thành viên Vàng trở lên", "29", "30,2% khách hàng");

        panelThongKe.add(theTongKhachHang);
        panelThongKe.add(theKhachHangMoi);
        panelThongKe.add(theTongThuCung);
        panelThongKe.add(theThanhVienVang);

        panelPhanTren.add(panelThongKe);
    }

    private JPanel taoTheThongKe(String bieuTuong, String tieuDe, String giaTri, String moTaPhu) {
        JPanel the = new JPanel(new BorderLayout(14, 0));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        // Khối Icon bên trái
        JLabel lblIcon = new JLabel(bieuTuong);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(46, 46));
        lblIcon.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1));

        // Khối thông tin bên phải
        JPanel panelThongTin = new JPanel();
        panelThongTin.setLayout(new BoxLayout(panelThongTin, BoxLayout.Y_AXIS));
        panelThongTin.setOpaque(false);

        JLabel lblTenChiSo = new JLabel(tieuDe);
        lblTenChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTenChiSo.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel panelSoLieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelSoLieu.setOpaque(false);

        JLabel lblSoChinh = new JLabel(giaTri);
        lblSoChinh.setFont(RangBuocGiaoDien.fontDam(20));
        lblSoChinh.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblGhiChu = new JLabel(moTaPhu);
        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        panelSoLieu.add(lblSoChinh);
        panelSoLieu.add(lblGhiChu);

        panelThongTin.add(lblTenChiSo);
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 4)));
        panelThongTin.add(panelSoLieu);

        the.add(lblIcon, BorderLayout.WEST);
        the.add(panelThongTin, BorderLayout.CENTER);

        return the;
    }

    private void khoiTaoBangDuLieu() {
        danhSachCot = new String[] {
                "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
                "Địa chỉ", "Số thú cưng", "Hạng thành viên", "Tổng chi tiêu", "Thao tác"
        };

        duLieuKhachHang = new Object[][] {
                { "KH001", "Nguyễn Hoàng Nam", "0912 345 678", "nam.nguyen@gmail.com", "Q. Bình Thạnh, TP.HCM", "2",
                        "● Vàng", "18.650.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH002", "Trần Thị Mai", "0945 678 901", "mai.tran@outlook.com", "TP. Thủ Đức, TP.HCM", "2", "● Bạc",
                        "9.280.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH003", "Phạm Quốc Hưng", "0986 789 012", "hung.pham@gmail.com", "Q. 3, TP.HCM", "1", "● Kim cương",
                        "42.150.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH004", "Lê Minh Anh", "0908 234 567", "minhanh.le@gmail.com", "Q. Phú Nhuận, TP.HCM", "3", "● Vàng",
                        "24.860.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH005", "Võ Thanh Hà", "0933 456 789", "ha.vo@yahoo.com", "Q. Gò Vấp, TP.HCM", "1", "● Đồng",
                        "2.450.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH006", "Đặng Minh Tú", "0977 890 123", "tu.dang@gmail.com", "Q. Tân Bình, TP.HCM", "2", "● Bạc",
                        "7.920.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH007", "Bùi Ngọc Lan", "0902 567 890", "lan.bui@gmail.com", "Q. 7, TP.HCM", "1", "● Đồng",
                        "3.180.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" },
                { "KH008", "Hoàng Gia Bảo", "0907 890 123", "bao.hoang@gmail.com", "Q. 10, TP.HCM", "4", "● Vàng",
                        "21.600.000 ₫", "👁 Xem  ✏ Sửa  🗑 Xóa" }
        };

        danhSachBoLoc = new String[] { "Hạng thành viên: Tất cả" };

        panelQuanLyTable = new QuanLyTable(
                danhSachCot,
                duLieuKhachHang,
                "Tìm theo tên, mã khách hàng, số điện thoại...",
                danhSachBoLoc);

        // Tùy chỉnh padding viền khớp chuẩn giao diện
        panelQuanLyTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
}
