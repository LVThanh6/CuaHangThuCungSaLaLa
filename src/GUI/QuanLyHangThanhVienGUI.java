package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Giao diện Quản lý hạng thành viên:
 * Hiển thị các tiêu chuẩn hạng thành viên, quyền lợi, ưu đãi và phân nhóm khách hàng
 * Theo chuẩn thiết kế hệ thống SALALA Pet Shop từ Figma.
 */
public class QuanLyHangThanhVienGUI extends JPanel {

    // =========================================================================
    // 1. KHAI BÁO CÁC BIẾN GIAO DIỆN LÊN ĐẦU FILE
    // =========================================================================
    private JPanel panelTongThe;
    private JPanel panelPhanTren;
    private JPanel panelTieuDe;
    private JLabel lblDuongDan;
    private JLabel lblTieuDe;
    private JLabel lblMoTa;
    private JButton btnThemHang;

    // Các thẻ thống kê nhanh phía trên
    private JPanel panelThongKe;
    private JPanel theTongKhachHang;
    private JPanel theNangHangThangNay;
    private JPanel theGiamGiaDaApDung;
    private JPanel theChiTieuThanhVien;

    // Khu vực hiển thị 4 thẻ hạng thành viên
    private JPanel panelDanhSachHang;
    private JScrollPane scrollDanhSachHang;
    private JPanel theHangDong;
    private JPanel theHangBac;
    private JPanel theHangVang;
    private JPanel theHangKimCuong;

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public QuanLyHangThanhVienGUI() {
        khoiTaoGiaoDien();
    }

    // =========================================================================
    // 3. CÁC HÀM XÂY DỰNG GIAO DIỆN
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // 1. Phần phía trên: Breadcrumb + Tiêu đề + Nút thêm + 4 thẻ thống kê
        khoiTaoPhanTren();
        add(panelPhanTren, BorderLayout.NORTH);

        // 2. Phần lưới danh sách các hạng thành viên
        khoiTaoDanhSachHang();
        add(scrollDanhSachHang, BorderLayout.CENTER);
    }

    private void khoiTaoPhanTren() {
        panelPhanTren = new JPanel();
        panelPhanTren.setLayout(new BoxLayout(panelPhanTren, BoxLayout.Y_AXIS));
        panelPhanTren.setOpaque(false);

        // Hàng 1: Breadcrumb đường dẫn & Tiêu đề + Nút thêm
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);

        JPanel panelTrai = new JPanel();
        panelTrai.setLayout(new BoxLayout(panelTrai, BoxLayout.Y_AXIS));
        panelTrai.setOpaque(false);

        lblDuongDan = new JLabel("Khách hàng / Quản lý hạng thành viên");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        lblTieuDe = new JLabel("Quản lý hạng thành viên");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblMoTa = new JLabel("Thiết lập điều kiện, mức giảm giá và quyền lợi dành cho từng nhóm khách hàng.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Nút "+ Thêm hạng"
        btnThemHang = new JButton("+ Thêm hạng");
        btnThemHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemHang.setForeground(Color.WHITE);
        btnThemHang.setFocusPainted(false);
        btnThemHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnThemHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // Hàng 2: 4 Thẻ thống kê số liệu
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        theTongKhachHang = taoTheThongKe("👥", "Tổng khách hàng", "96", "Đã phân hạng");
        theNangHangThangNay = taoTheThongKe("📈", "Nâng hạng tháng này", "14", "+4 so với tháng trước");
        theGiamGiaDaApDung = taoTheThongKe("🏷️", "Giảm giá đã áp dụng", "28,6 tr", "Trong tháng 10");
        theChiTieuThanhVien = taoTheThongKe("🧾", "Chi tiêu thành viên", "186 tr", "72% tổng doanh thu");

        panelThongKe.add(theTongKhachHang);
        panelThongKe.add(theNangHangThangNay);
        panelThongKe.add(theGiamGiaDaApDung);
        panelThongKe.add(theChiTieuThanhVien);

        panelPhanTren.add(panelThongKe);
    }

    private JPanel taoTheThongKe(String bieuTuong, String tieuDe, String giaTri, String moTaPhu) {
        JPanel the = new JPanel(new BorderLayout(14, 0));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // Icon bên trái
        JLabel lblIcon = new JLabel(bieuTuong);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(46, 46));
        lblIcon.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1));

        // Thông tin số liệu
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

    private void khoiTaoDanhSachHang() {
        panelDanhSachHang = new JPanel(new GridLayout(1, 4, 16, 0));
        panelDanhSachHang.setOpaque(false);

        // Quyền lợi cụ thể cho từng hạng theo Figma
        String[] qlDong = {
            "Giảm 2% hóa đơn dịch vụ", 
            "Tích 1 điểm / 10.000 ₫", 
            "Nhắc lịch tiêm và tái khám", 
            "Ưu đãi sinh nhật thú cưng"
        };
        String[] qlBac = {
            "Giảm 5% hóa đơn dịch vụ", 
            "Tích 1,2 điểm / 10.000 ₫", 
            "01 lần vệ sinh tai miễn phí / quý", 
            "Ưu tiên khung giờ khám"
        };
        String[] qlVang = {
            "Giảm 10% hóa đơn dịch vụ", 
            "Tích 1,5 điểm / 10.000 ₫", 
            "01 lần khám tổng quát / năm", 
            "Miễn phí giao hàng nội thành"
        };
        String[] qlKimCuong = {
            "Giảm 15% hóa đơn dịch vụ", 
            "Tích 2 điểm / 10.000 ₫", 
            "02 lần khám tổng quát / năm", 
            "Hotline bác sĩ ưu tiên 24/7"
        };

        // 4 Thẻ hạng thành viên tương ứng
        theHangDong = taoTheHang(
            "🥉", "Đồng", "Tổng chi tiêu dưới 5.000.000 ₫", 
            "2%", "42 khách hàng", "43,8% tổng khách", 
            qlDong, new Color(245, 158, 11), new Color(254, 243, 199)
        );

        theHangBac = taoTheHang(
            "🥈", "Bạc", "Từ 5.000.000 ₫ đến dưới 15.000.000 ₫", 
            "5%", "25 khách hàng", "26,0% tổng khách", 
            qlBac, new Color(100, 116, 139), new Color(241, 245, 249)
        );

        theHangVang = taoTheHang(
            "👑", "Vàng", "Từ 15.000.000 ₫ đến dưới 30.000.000 ₫", 
            "10%", "21 khách hàng", "21,9% tổng khách", 
            qlVang, new Color(217, 119, 6), new Color(254, 240, 138)
        );

        theHangKimCuong = taoTheHang(
            "💎", "Kim cương", "Tổng chi tiêu từ 30.000.000 ₫", 
            "15%", "8 khách hàng", "8,3% tổng khách", 
            qlKimCuong, new Color(2, 132, 199), new Color(224, 242, 254)
        );

        panelDanhSachHang.add(theHangDong);
        panelDanhSachHang.add(theHangBac);
        panelDanhSachHang.add(theHangVang);
        panelDanhSachHang.add(theHangKimCuong);

        scrollDanhSachHang = new JScrollPane(panelDanhSachHang);
        scrollDanhSachHang.setBorder(BorderFactory.createEmptyBorder());
        scrollDanhSachHang.setOpaque(false);
        scrollDanhSachHang.getViewport().setOpaque(false);
    }

    private JPanel taoTheHang(
        String bieuTuong, String tenHang, String dieuKienChiTieu, 
        String mucGiamGia, String soKhachHang, String tiLeKhachHang, 
        String[] danhSachQuyenLoi, Color mauVienTop, Color mauNenNhe
    ) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));

        // 1. Thanh viền màu ở đỉnh thẻ
        JPanel thanhMauTop = new JPanel();
        thanhMauTop.setBackground(mauVienTop);
        thanhMauTop.setPreferredSize(new Dimension(0, 4));
        thanhMauTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        the.add(thanhMauTop);

        // 2. Khối Header của thẻ (Icon + Tên hạng + Badge Đang áp dụng)
        JPanel panelHeaderThe = new JPanel(new BorderLayout());
        panelHeaderThe.setBackground(mauNenNhe);
        panelHeaderThe.setBorder(new EmptyBorder(16, 16, 10, 16));

        JLabel lblTenHang = new JLabel(bieuTuong + "  " + tenHang);
        lblTenHang.setFont(RangBuocGiaoDien.fontDam(18));
        lblTenHang.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblHuyHieu = new JLabel("● Đang áp dụng");
        lblHuyHieu.setFont(RangBuocGiaoDien.fontDam(10));
        lblHuyHieu.setForeground(new Color(217, 119, 6));
        lblHuyHieu.setBackground(Color.WHITE);
        lblHuyHieu.setOpaque(true);
        lblHuyHieu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(254, 215, 170), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));

        panelHeaderThe.add(lblTenHang, BorderLayout.WEST);
        panelHeaderThe.add(lblHuyHieu, BorderLayout.EAST);
        the.add(panelHeaderThe);

        // 3. Khối điều kiện chi tiêu
        JPanel panelDieuKien = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelDieuKien.setBackground(mauNenNhe);
        panelDieuKien.setBorder(new EmptyBorder(0, 16, 14, 16));

        JLabel lblDieuKien = new JLabel(dieuKienChiTieu);
        lblDieuKien.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDieuKien.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelDieuKien.add(lblDieuKien);
        the.add(panelDieuKien);

        // 4. Khối 2 cột: Mức giảm giá & Số lượng khách hàng
        JPanel panelThongSo = new JPanel(new GridLayout(1, 2, 8, 0));
        panelThongSo.setOpaque(false);
        panelThongSo.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Cột trái: Mức giảm giá
        JPanel cotTrai = new JPanel();
        cotTrai.setLayout(new BoxLayout(cotTrai, BoxLayout.Y_AXIS));
        cotTrai.setOpaque(false);

        JLabel lblMucGiamGiaTieuDe = new JLabel("Mức giảm giá");
        lblMucGiamGiaTieuDe.setFont(RangBuocGiaoDien.fontThuong(11));
        lblMucGiamGiaTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JLabel lblMucGiamGiaSo = new JLabel(mucGiamGia);
        lblMucGiamGiaSo.setFont(RangBuocGiaoDien.fontDam(26));
        lblMucGiamGiaSo.setForeground(new Color(194, 65, 12));

        JLabel lblMucGiamGiaGhiChu = new JLabel("trên dịch vụ đủ điều kiện");
        lblMucGiamGiaGhiChu.setFont(RangBuocGiaoDien.fontThuong(10));
        lblMucGiamGiaGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        cotTrai.add(lblMucGiamGiaTieuDe);
        cotTrai.add(Box.createRigidArea(new Dimension(0, 2)));
        cotTrai.add(lblMucGiamGiaSo);
        cotTrai.add(Box.createRigidArea(new Dimension(0, 2)));
        cotTrai.add(lblMucGiamGiaGhiChu);

        // Cột phải: Số lượng khách hàng
        JPanel cotPhai = new JPanel();
        cotPhai.setLayout(new BoxLayout(cotPhai, BoxLayout.Y_AXIS));
        cotPhai.setOpaque(false);
        cotPhai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
            new EmptyBorder(0, 12, 0, 0)
        ));

        JLabel lblSoKhachTieuDe = new JLabel("Số lượng khách hàng");
        lblSoKhachTieuDe.setFont(RangBuocGiaoDien.fontThuong(11));
        lblSoKhachTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JLabel lblSoKhachGiaTri = new JLabel(soKhachHang);
        lblSoKhachGiaTri.setFont(RangBuocGiaoDien.fontDam(15));
        lblSoKhachGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblSoKhachTiLe = new JLabel(tiLeKhachHang);
        lblSoKhachTiLe.setFont(RangBuocGiaoDien.fontThuong(10));
        lblSoKhachTiLe.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        cotPhai.add(lblSoKhachTieuDe);
        cotPhai.add(Box.createRigidArea(new Dimension(0, 4)));
        cotPhai.add(lblSoKhachGiaTri);
        cotPhai.add(Box.createRigidArea(new Dimension(0, 4)));
        cotPhai.add(lblSoKhachTiLe);

        panelThongSo.add(cotTrai);
        panelThongSo.add(cotPhai);
        the.add(panelThongSo);

        // Đường kẻ phân cách
        JSeparator duongKe = new JSeparator();
        duongKe.setForeground(RangBuocGiaoDien.MAU_DUONG_VIEN);
        duongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        the.add(duongKe);

        // 5. Khối quyền lợi thành viên
        JPanel panelQuyenLoi = new JPanel();
        panelQuyenLoi.setLayout(new BoxLayout(panelQuyenLoi, BoxLayout.Y_AXIS));
        panelQuyenLoi.setOpaque(false);
        panelQuyenLoi.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTieuDeQuyenLoi = new JLabel("QUYỀN LỢI THÀNH VIÊN");
        lblTieuDeQuyenLoi.setFont(RangBuocGiaoDien.fontDam(11));
        lblTieuDeQuyenLoi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelQuyenLoi.add(lblTieuDeQuyenLoi);
        panelQuyenLoi.add(Box.createRigidArea(new Dimension(0, 10)));

        for (String ql : danhSachQuyenLoi) {
            JLabel lblItem = new JLabel("✓  " + ql);
            lblItem.setFont(RangBuocGiaoDien.fontThuong(12));
            lblItem.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            panelQuyenLoi.add(lblItem);
            panelQuyenLoi.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        the.add(panelQuyenLoi);

        the.add(Box.createVerticalGlue());

        // 6. Khối nút Thao tác phía dưới (Chỉnh sửa + Xóa)
        JPanel panelThaoTac = new JPanel(new GridLayout(1, 2, 10, 0));
        panelThaoTac.setOpaque(false);
        panelThaoTac.setBorder(new EmptyBorder(12, 16, 16, 16));

        JButton btnSua = new JButton("✏ Chỉnh sửa");
        btnSua.setFont(RangBuocGiaoDien.fontDam(12));
        btnSua.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnSua.setBackground(Color.WHITE);
        btnSua.setFocusPainted(false);
        btnSua.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btnSua.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnXoa = new JButton("🗑 Xóa");
        btnXoa.setFont(RangBuocGiaoDien.fontDam(12));
        btnXoa.setForeground(RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
        btnXoa.setBackground(Color.WHITE);
        btnXoa.setFocusPainted(false);
        btnXoa.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panelThaoTac.add(btnSua);
        panelThaoTac.add(btnXoa);
        the.add(panelThaoTac);

        return the;
    }
}
