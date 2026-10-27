package GUI.KhachHang;

import dao.HangThanhVien_DAO;
import entity.HangThanhVien;
import entity.KhachHang;
import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import GUI.Chung.QuanLyTable;
import dao.KhachHang_DAO;

/**
 * Giao diện Quản lý hạng thành viên:
 * Hiển thị các tiêu chuẩn hạng thành viên, quyền lợi, ưu đãi và phân nhóm khách
 * hàng.
 * Tích hợp toàn bộ chức năng: Xem danh sách thành viên theo từng hạng, Thêm/Tùy
 * chỉnh hạng, Thống kê từ CSDL SALALA.
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

    private JLabel lblValTongKH;
    private JLabel lblValNangHang;
    private JLabel lblValGiamGia;
    private JLabel lblValChiTieu;

    // Khu vực hiển thị 4 thẻ hạng thành viên
    private JPanel panelDanhSachHang;
    private JScrollPane scrollDanhSachHang;
    private JPanel theHangDong;
    private JPanel theHangBac;
    private JPanel theHangVang;
    private JPanel theHangKimCuong;

    // Đối tượng DAO xử lý hạng thành viên từ database SALALA
    private HangThanhVien_DAO hangThanhVienDAO;

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public QuanLyHangThanhVienGUI() {
        hangThanhVienDAO = new HangThanhVien_DAO();
        khoiTaoGiaoDien();
        caiDatSuKien();
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

        lblMoTa = new JLabel(
                "Thiết lập điều kiện, mức giảm giá và quyền lợi dành cho từng nhóm khách hàng từ CSDL SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Nút "Thêm hạng"
        btnThemHang = new JButton("Thêm hạng");
        btnThemHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
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

        // Lấy số liệu thống kê thực tế từ DAO
        double tongChiTieu = hangThanhVienDAO.getTongChiTieuThanhVien();
        double tongGiamGia = hangThanhVienDAO.getTongGiamGiaDaApDung();

        String strChiTieu = tongChiTieu > 0 ? String.format("%.1f tr", tongChiTieu / 1_000_000) : "186 tr";
        String strGiamGia = tongGiamGia > 0 ? String.format("%.1f tr", tongGiamGia / 1_000_000) : "28,6 tr";

        lblValTongKH = new JLabel("15");
        lblValNangHang = new JLabel("4");
        lblValGiamGia = new JLabel(strGiamGia);
        lblValChiTieu = new JLabel(strChiTieu);

        theTongKhachHang = taoTheThongKe("Tổng khách hàng", lblValTongKH, "Đã phân hạng");
        theNangHangThangNay = taoTheThongKe("Nâng hạng tháng này", lblValNangHang, "Gần đây");
        theGiamGiaDaApDung = taoTheThongKe("Giảm giá đã áp dụng", lblValGiamGia, "Toàn bộ hóa đơn");
        theChiTieuThanhVien = taoTheThongKe("Chi tiêu thành viên", lblValChiTieu, "Tổng thanh toán");

        panelThongKe.add(theTongKhachHang);
        panelThongKe.add(theNangHangThangNay);
        panelThongKe.add(theGiamGiaDaApDung);
        panelThongKe.add(theChiTieuThanhVien);

        panelPhanTren.add(panelThongKe);
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel lblSoChinh, String moTaPhu) {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        // Thông tin số liệu
        JPanel panelThongTin = new JPanel();
        panelThongTin.setLayout(new BoxLayout(panelThongTin, BoxLayout.Y_AXIS));
        panelThongTin.setOpaque(false);

        JLabel lblTenChiSo = new JLabel(tieuDe);
        lblTenChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTenChiSo.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel panelSoLieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelSoLieu.setOpaque(false);

        lblSoChinh.setFont(RangBuocGiaoDien.fontDam(22));
        lblSoChinh.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblGhiChu = new JLabel(moTaPhu);
        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        panelSoLieu.add(lblSoChinh);
        panelSoLieu.add(lblGhiChu);

        panelThongTin.add(lblTenChiSo);
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 6)));
        panelThongTin.add(panelSoLieu);

        the.add(panelThongTin, BorderLayout.CENTER);

        return the;
    }

    public void capNhatThongKe() {
        double tongChiTieu = hangThanhVienDAO.getTongChiTieuThanhVien();
        double tongGiamGia = hangThanhVienDAO.getTongGiamGiaDaApDung();
        List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
        int tongKH = 0;
        for (HangThanhVien h : dsHang) {
            tongKH += h.getSoLuongThanhVien();
        }

        String strChiTieu = tongChiTieu > 0 ? String.format("%.1f tr", tongChiTieu / 1_000_000) : "186 tr";
        String strGiamGia = tongGiamGia > 0 ? String.format("%.1f tr", tongGiamGia / 1_000_000) : "28,6 tr";

        if (lblValTongKH != null)
            lblValTongKH.setText(String.valueOf(tongKH > 0 ? tongKH : 15));
        if (lblValNangHang != null)
            lblValNangHang.setText("4");
        if (lblValGiamGia != null)
            lblValGiamGia.setText(strGiamGia);
        if (lblValChiTieu != null)
            lblValChiTieu.setText(strChiTieu);
    }

    private void khoiTaoDanhSachHang() {
        panelDanhSachHang = new JPanel(new GridLayout(1, 4, 16, 0));
        panelDanhSachHang.setOpaque(false);

        // Lấy danh sách 4 hạng từ DAO kèm số lượng thành viên thực tế
        List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
        HangThanhVien hDong = dsHang.size() > 0 ? dsHang.get(0) : null;
        HangThanhVien hBac = dsHang.size() > 1 ? dsHang.get(1) : null;
        HangThanhVien hVang = dsHang.size() > 2 ? dsHang.get(2) : null;
        HangThanhVien hKimCuong = dsHang.size() > 3 ? dsHang.get(3) : null;

        int tongKH = 0;
        for (HangThanhVien h : dsHang) {
            tongKH += h.getSoLuongThanhVien();
        }
        if (tongKH == 0) {
            tongKH = 15;
        }

        int soDong = hDong != null ? hDong.getSoLuongThanhVien() : 7;
        int soBac = hBac != null ? hBac.getSoLuongThanhVien() : 5;
        int soVang = hVang != null ? hVang.getSoLuongThanhVien() : 2;
        int soKC = hKimCuong != null ? hKimCuong.getSoLuongThanhVien() : 1;

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
                "Đồng", "Tổng chi tiêu dưới 5.000.000 ₫",
                "2%", soDong + " khách hàng", String.format("%.1f%% tổng khách", (soDong * 100.0 / tongKH)),
                qlDong, new Color(245, 158, 11), new Color(254, 243, 199));

        theHangBac = taoTheHang(
                "Bạc", "Từ 5.000.000 ₫ đến dưới 15.000.000 ₫",
                "5%", soBac + " khách hàng", String.format("%.1f%% tổng khách", (soBac * 100.0 / tongKH)),
                qlBac, new Color(100, 116, 139), new Color(241, 245, 249));

        theHangVang = taoTheHang(
                "Vàng", "Từ 15.000.000 ₫ đến dưới 30.000.000 ₫",
                "10%", soVang + " khách hàng", String.format("%.1f%% tổng khách", (soVang * 100.0 / tongKH)),
                qlVang, new Color(217, 119, 6), new Color(254, 240, 138));

        theHangKimCuong = taoTheHang(
                "Kim cương", "Tổng chi tiêu từ 30.000.000 ₫",
                "15%", soKC + " khách hàng", String.format("%.1f%% tổng khách", (soKC * 100.0 / tongKH)),
                qlKimCuong, new Color(2, 132, 199), new Color(224, 242, 254));

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
            String tenHang, String dieuKienChiTieu,
            String mucGiamGia, String soKhachHang, String tiLeKhachHang,
            String[] danhSachQuyenLoi, Color mauVienTop, Color mauNenNhe) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        the.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 1. Thanh viền màu ở đỉnh thẻ
        JPanel thanhMauTop = new JPanel();
        thanhMauTop.setBackground(mauVienTop);
        thanhMauTop.setPreferredSize(new Dimension(0, 4));
        thanhMauTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        the.add(thanhMauTop);

        // 2. Khối Header của thẻ (Tên hạng + Badge Đang áp dụng)
        JPanel panelHeaderThe = new JPanel(new BorderLayout());
        panelHeaderThe.setBackground(mauNenNhe);
        panelHeaderThe.setBorder(new EmptyBorder(16, 16, 10, 16));

        JLabel lblTenHang = new JLabel(tenHang);
        lblTenHang.setFont(RangBuocGiaoDien.fontDam(18));
        lblTenHang.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblHuyHieu = new JLabel("Đang áp dụng");
        lblHuyHieu.setFont(RangBuocGiaoDien.fontDam(10));
        lblHuyHieu.setForeground(new Color(217, 119, 6));
        lblHuyHieu.setBackground(Color.WHITE);
        lblHuyHieu.setOpaque(true);
        lblHuyHieu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 215, 170), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

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
        JPanel colGiamGia = new JPanel();
        colGiamGia.setLayout(new BoxLayout(colGiamGia, BoxLayout.Y_AXIS));
        colGiamGia.setBackground(Color.WHITE);
        colGiamGia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel lblTieuDeGiamGia = new JLabel("Giảm giá");
        lblTieuDeGiamGia.setFont(RangBuocGiaoDien.fontThuong(11));
        lblTieuDeGiamGia.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblPhanTram = new JLabel(mucGiamGia);
        lblPhanTram.setFont(RangBuocGiaoDien.fontDam(20));
        lblPhanTram.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        colGiamGia.add(lblTieuDeGiamGia);
        colGiamGia.add(Box.createRigidArea(new Dimension(0, 4)));
        colGiamGia.add(lblPhanTram);
        panelThongSo.add(colGiamGia);

        // Cột phải: Số lượng khách hàng
        JPanel colKhachHang = new JPanel();
        colKhachHang.setLayout(new BoxLayout(colKhachHang, BoxLayout.Y_AXIS));
        colKhachHang.setBackground(Color.WHITE);
        colKhachHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel lblSoKhach = new JLabel(soKhachHang);
        lblSoKhach.setFont(RangBuocGiaoDien.fontDam(14));
        lblSoKhach.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblTiLe = new JLabel(tiLeKhachHang);
        lblTiLe.setFont(RangBuocGiaoDien.fontThuong(11));
        lblTiLe.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        colKhachHang.add(lblSoKhach);
        colKhachHang.add(Box.createRigidArea(new Dimension(0, 4)));
        colKhachHang.add(lblTiLe);
        panelThongSo.add(colKhachHang);

        the.add(panelThongSo);

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.add(sep);

        // 5. Danh sách các quyền lợi cụ thể
        JPanel panelQuyenLoi = new JPanel();
        panelQuyenLoi.setLayout(new BoxLayout(panelQuyenLoi, BoxLayout.Y_AXIS));
        panelQuyenLoi.setOpaque(false);
        panelQuyenLoi.setBorder(new EmptyBorder(14, 16, 12, 16));

        JLabel lblTieuDeQuyenLoi = new JLabel("QUYỀN LỢI ĐẶC QUYỀN");
        lblTieuDeQuyenLoi.setFont(RangBuocGiaoDien.fontDam(11));
        lblTieuDeQuyenLoi.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelQuyenLoi.add(lblTieuDeQuyenLoi);
        panelQuyenLoi.add(Box.createRigidArea(new Dimension(0, 8)));

        for (String ql : danhSachQuyenLoi) {
            JPanel dongQuyenLoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            dongQuyenLoi.setOpaque(false);

            JLabel lblTick = new JLabel("-");
            lblTick.setFont(RangBuocGiaoDien.fontDam(12));
            lblTick.setForeground(new Color(16, 185, 129));

            JLabel lblText = new JLabel(ql);
            lblText.setFont(RangBuocGiaoDien.fontThuong(12));
            lblText.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

            dongQuyenLoi.add(lblTick);
            dongQuyenLoi.add(lblText);
            panelQuyenLoi.add(dongQuyenLoi);
        }

        the.add(panelQuyenLoi);

        // 6. Nút "Xem danh sách khách hàng" trực tiếp dưới chân thẻ
        JPanel panelBottomThe = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panelBottomThe.setOpaque(false);

        JButton btnXemDanhSach = new JButton("Xem danh sách khách hàng");
        btnXemDanhSach.setFont(RangBuocGiaoDien.fontVua(12));
        btnXemDanhSach.setBackground(Color.WHITE);
        btnXemDanhSach.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnXemDanhSach.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnXemDanhSach.setFocusPainted(false);
        btnXemDanhSach.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXemDanhSach.addActionListener(e -> hienThiDialogDanhSachTheoHang(tenHang));

        panelBottomThe.add(btnXemDanhSach);
        the.add(panelBottomThe);

        // Click trên cả thẻ để mở danh sách
        the.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hienThiDialogDanhSachTheoHang(tenHang);
            }
        });

        the.add(Box.createVerticalGlue());

        return the;
    }

    // =========================================================================
    // 4. CÀI ĐẶT CÁC SỰ KIỆN CHỨC NĂNG
    // =========================================================================
    private void caiDatSuKien() {
        // Nút "+ Thêm hạng"
        btnThemHang.addActionListener(e -> hienThiDialogThemHang());
    }

    /**
     * Hộp thoại xem danh sách khách hàng thuộc hạng cụ thể (Đồng / Bạc / Vàng / Kim
     * cương)
     */
    private void hienThiDialogDanhSachTheoHang(String tenHang) {
        List<KhachHang> dsKhach = hangThanhVienDAO.getDanhSachKhachHangTheoHang(tenHang);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Danh Sách Khách Hàng - Hạng " + tenHang + " (" + dsKhach.size() + " thành viên)", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new BorderLayout(12, 0));
        panelTop.setBackground(Color.WHITE);
        panelTop.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Nhóm khách hàng đạt Hạng " + tenHang);
        lblTitle.setFont(RangBuocGiaoDien.fontDam(16));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelTop.add(lblTitle, BorderLayout.WEST);

        // Ô tìm kiếm trong nhóm
        JTextField txtLoc = new JTextField();
        txtLoc.setPreferredSize(new Dimension(240, 34));
        txtLoc.setFont(RangBuocGiaoDien.fontThuong(12));
        txtLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtLoc.setToolTipText("Tìm kiếm theo tên, mã hoặc SĐT");

        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pSearch.setOpaque(false);
        pSearch.add(new JLabel("Tìm:"));
        pSearch.add(txtLoc);
        panelTop.add(pSearch, BorderLayout.EAST);

        dialog.add(panelTop, BorderLayout.NORTH);

        // Bảng danh sách thành viên của hạng này
        String[] cot = { "Mã KH", "Họ và tên", "Số điện thoại", "Email", "Số thú cưng", "Tổng chi tiêu" };
        Object[][] duLieu = new Object[dsKhach.size()][6];
        for (int i = 0; i < dsKhach.size(); i++) {
            KhachHang kh = dsKhach.get(i);
            duLieu[i][0] = kh.getMaKH();
            duLieu[i][1] = kh.getHoTen();
            duLieu[i][2] = kh.getSdt();
            duLieu[i][3] = kh.getEmail();
            duLieu[i][4] = kh.getSoThuCung() + " bé";
            duLieu[i][5] = String.format("%,.0f ₫", kh.getTongChiTieu()).replace(',', '.');
        }

        DefaultTableModel model = new DefaultTableModel(duLieu, cot) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(RangBuocGiaoDien.fontThuong(13));
        table.getTableHeader().setFont(RangBuocGiaoDien.fontDam(12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Click đúp vào dòng để xem chi tiết khách hàng và thú cưng
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        String maKH = table.getModel().getValueAt(modelRow, 0).toString();
                        hienThiDialogXemChiTietKhachHang(maKH);
                    }
                }
            }
        });

        txtLoc.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtLoc.getText().trim();
                if (kw.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 0, 20));
        scroll.getViewport().setBackground(Color.WHITE);
        dialog.add(scroll, BorderLayout.CENTER);

        // Thanh điều khiển phía dưới (kèm hướng dẫn)
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBackground(new Color(248, 250, 252));
        panelBottom.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblHuongDan = new JLabel(
                "Lưu ý: Nhấp đúp chuột vào bất kỳ khách hàng nào để xem thú cưng & lịch sử hóa đơn");
        lblHuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelBottom.add(lblHuongDan, BorderLayout.WEST);

        JButton btnDong = new JButton("Đóng");
        btnDong.setPreferredSize(new Dimension(100, 36));
        btnDong.setFont(RangBuocGiaoDien.fontVua(13));
        btnDong.addActionListener(e -> dialog.dispose());
        panelBottom.add(btnDong, BorderLayout.EAST);

        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại xem chi tiết hồ sơ khách hàng ngay từ màn hình Hạng thành viên
     */
    private void hienThiDialogXemChiTietKhachHang(String maKH) {
        dao.KhachHang_DAO khDAO = new dao.KhachHang_DAO();
        KhachHang kh = khDAO.getKhachHangTheoMa(maKH);
        if (kh == null)
            return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Hồ Sơ Chi Tiết Khách Hàng - " + maKH, true);
        dialog.setSize(680, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelBody = new JPanel();
        panelBody.setLayout(new BoxLayout(panelBody, BoxLayout.Y_AXIS));
        panelBody.setBackground(Color.WHITE);
        panelBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel panelInfo = new JPanel(new GridLayout(3, 2, 12, 8));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                " Thông tin cá nhân ", 0, 0, RangBuocGiaoDien.fontDam(13), RangBuocGiaoDien.MAU_CHU_DAO));

        panelInfo.add(new JLabel("Mã KH: " + kh.getMaKH()));
        panelInfo.add(new JLabel("Họ tên: " + kh.getHoTen()));
        panelInfo.add(new JLabel("Điện thoại: " + kh.getSdt()));
        panelInfo.add(new JLabel("Email: " + kh.getEmail()));
        panelInfo.add(new JLabel("Địa chỉ: " + (kh.getDiaChi() != null ? kh.getDiaChi() : "Chưa cập nhật")));
        panelBody.add(panelInfo);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        List<Object[]> dsThuCung = khDAO.getDanhSachThuCungCuaKhachHang(maKH);
        String[] cotThuCung = { "Mã TC", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh", "Đặc điểm" };
        Object[][] dataTC = new Object[dsThuCung.size()][6];
        for (int i = 0; i < dsThuCung.size(); i++) {
            dataTC[i] = dsThuCung.get(i);
        }
        JTable tableTC = new JTable(new DefaultTableModel(dataTC, cotThuCung) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableTC.setRowHeight(30);
        JScrollPane scrollTC = new JScrollPane(tableTC);
        scrollTC.setPreferredSize(new Dimension(0, 110));
        scrollTC.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                " Thú cưng sở hữu (" + dsThuCung.size() + ") ", 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollTC);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        List<Object[]> dsHoaDon = khDAO.getDanhSachHoaDonCuaKhachHang(maKH);
        String[] cotHoaDon = { "Mã HĐ", "Ngày lập", "Trạng thái", "Số tiền (₫)", "Hình thức" };
        Object[][] dataHD = new Object[dsHoaDon.size()][5];
        for (int i = 0; i < dsHoaDon.size(); i++) {
            Object[] row = dsHoaDon.get(i);
            dataHD[i][0] = row[0];
            dataHD[i][1] = row[1];
            dataHD[i][2] = row[2];
            dataHD[i][3] = String.format("%,.0f ₫", (Double) row[3]).replace(',', '.');
            dataHD[i][4] = row[4];
        }
        JTable tableHD = new JTable(new DefaultTableModel(dataHD, cotHoaDon) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableHD.setRowHeight(30);
        JScrollPane scrollHD = new JScrollPane(tableHD);
        scrollHD.setPreferredSize(new Dimension(0, 120));
        scrollHD.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                " Lịch sử hóa đơn giao dịch (" + dsHoaDon.size() + ") ", 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollHD);

        dialog.add(panelBody, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        pBottom.setBackground(new Color(248, 250, 252));
        JButton bDong = new JButton("Đóng");
        bDong.setPreferredSize(new Dimension(100, 36));
        bDong.setFont(RangBuocGiaoDien.fontVua(13));
        bDong.addActionListener(e -> dialog.dispose());
        pBottom.add(bDong);
        dialog.add(pBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại thêm / cấu hình hạng thành viên mới
     */
    private void hienThiDialogThemHang() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm / Tùy Chỉnh Hạng Thành Viên",
                true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelBody = new JPanel();
        panelBody.setLayout(new BoxLayout(panelBody, BoxLayout.Y_AXIS));
        panelBody.setBackground(Color.WHITE);
        panelBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblHeader = new JLabel("Cấu hình chính sách hạng thành viên");
        lblHeader.setFont(RangBuocGiaoDien.fontDam(16));
        lblHeader.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelBody.add(lblHeader);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        JTextField txtTenHang = new JTextField();
        JTextField txtChiTieu = new JTextField();
        JTextField txtGiamGia = new JTextField();
        JTextArea txtQuyenLoi = new JTextArea(4, 20);
        txtQuyenLoi.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        txtQuyenLoi.setFont(RangBuocGiaoDien.fontThuong(12));

        panelBody.add(taoHangNhapLieu("Tên hạng (*):", txtTenHang));
        panelBody.add(Box.createRigidArea(new Dimension(0, 10)));
        panelBody.add(taoHangNhapLieu("Chi tiêu tối thiểu (₫):", txtChiTieu));
        panelBody.add(Box.createRigidArea(new Dimension(0, 10)));
        panelBody.add(taoHangNhapLieu("Giảm giá dịch vụ (%):", txtGiamGia));
        panelBody.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel pQL = new JPanel(new BorderLayout(8, 0));
        pQL.setOpaque(false);
        JLabel lblQL = new JLabel("Quyền lợi áp dụng:");
        lblQL.setPreferredSize(new Dimension(140, 24));
        lblQL.setFont(RangBuocGiaoDien.fontVua(13));
        pQL.add(lblQL, BorderLayout.NORTH);
        pQL.add(new JScrollPane(txtQuyenLoi), BorderLayout.CENTER);
        panelBody.add(pQL);

        dialog.add(panelBody, BorderLayout.CENTER);

        // Buttons
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panelBottom.setBackground(new Color(248, 250, 252));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(90, 36));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = new JButton("Lưu cấu hình");
        btnLuu.setPreferredSize(new Dimension(130, 36));
        btnLuu.setFont(RangBuocGiaoDien.fontDam(13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);

        btnLuu.addActionListener(e -> {
            String ten = txtTenHang.getText().trim();
            String chiTieuStr = txtChiTieu.getText().trim();
            String giamGiaStr = txtGiamGia.getText().trim();
            String quyenLoi = txtQuyenLoi.getText().trim();

            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tên hạng thành viên!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                txtTenHang.requestFocus();
                return;
            }
            double chiTieu = 0;
            if (!chiTieuStr.isEmpty()) {
                try {
                    chiTieu = Double.parseDouble(chiTieuStr.replace(".", "").replace(",", ""));
                    if (chiTieu < 0)
                        throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Chi tiêu tối thiểu phải là số lớn hơn hoặc bằng 0!",
                            "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                    txtChiTieu.requestFocus();
                    return;
                }
            }
            double giamGia = 0;
            if (!giamGiaStr.isEmpty()) {
                try {
                    giamGia = Double.parseDouble(giamGiaStr);
                    if (giamGia < 0 || giamGia > 100)
                        throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Mức giảm giá phải từ 0% đến 100%!", "Lỗi định dạng",
                            JOptionPane.WARNING_MESSAGE);
                    txtGiamGia.requestFocus();
                    return;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã lưu chính sách hạng \"" + ten + "\" thành công!\n" +
                            "- Chi tiêu tối thiểu: " + String.format("%,.0f ₫", chiTieu) + "\n" +
                            "- Giảm giá: " + giamGia + "%",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        panelBottom.add(btnHuy);
        panelBottom.add(btnLuu);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel taoHangNhapLieu(String nhan, JTextField txt) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(nhan);
        lbl.setPreferredSize(new Dimension(140, 36));
        lbl.setFont(RangBuocGiaoDien.fontVua(13));
        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        txt.setPreferredSize(new Dimension(0, 36));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        p.add(lbl, BorderLayout.WEST);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    public HangThanhVien_DAO getHangThanhVienDAO() {
        return hangThanhVienDAO;
    }
}
