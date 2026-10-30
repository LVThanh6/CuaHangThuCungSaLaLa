package GUI.KhachHang;

import dao.HangThanhVien_DAO;
import dao.KhachHang_DAO;
import entity.HangThanhVien;
import entity.KhachHang;
import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Quản lý hạng thành viên:
 * Kết nối trực tiếp bảng QuanLyHangThanhVien trong CSDL SALALA.
 * Hiển thị 4 nhóm hạng: Đồng, Bạc, Vàng, Kim Cương kèm thống kê thực tế,
 * chính sách ưu đãi, quyền lợi đặc quyền và danh sách thành viên.
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

    // Đối tượng DAO xử lý dữ liệu từ database SALALA
    private HangThanhVien_DAO hangThanhVienDAO;
    private KhachHang_DAO khachHangDAO;

    private SimpleDateFormat sdfNgay = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public QuanLyHangThanhVienGUI() {
        hangThanhVienDAO = new HangThanhVien_DAO();
        khachHangDAO = new KhachHang_DAO();
        khoiTaoGiaoDien();
        caiDatSuKien();
        capNhatThongKe();
    }

    // =========================================================================
    // 3. CÁC HÀM XÂY DỰNG GIAO DIỆN
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // 1. Phần phía trên: Breadcrumb + Tiêu đề + Nút thêm hạng + 4 thẻ thống kê
        khoiTaoPhanTren();
        add(panelPhanTren, BorderLayout.NORTH);

        // 2. Phần lưới 4 thẻ hạng thành viên
        panelDanhSachHang = new JPanel(new GridLayout(1, 4, 16, 0));
        panelDanhSachHang.setOpaque(false);

        scrollDanhSachHang = new JScrollPane(panelDanhSachHang);
        scrollDanhSachHang.setBorder(BorderFactory.createEmptyBorder());
        scrollDanhSachHang.setOpaque(false);
        scrollDanhSachHang.getViewport().setOpaque(false);
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

        lblMoTa = new JLabel("Thiết lập chính sách ưu đãi, mức giảm giá và quản lý thành viên từ CSDL SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Nút "Thêm hạng" - Nút phẳng tiêu chuẩn, nền màu chủ đạo, chữ trắng, không
        // icon, không animation
        btnThemHang = new JButton("Thêm hạng");
        btnThemHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemHang.setForeground(Color.WHITE);
        btnThemHang.setUI(new BasicButtonUI());
        btnThemHang.setRolloverEnabled(false);
        btnThemHang.setFocusPainted(false);
        btnThemHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnThemHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // Hàng 2: 4 Thẻ thống kê số liệu thực tế từ database
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        lblValTongKH = new JLabel("0");
        lblValNangHang = new JLabel("0");
        lblValGiamGia = new JLabel("0 ₫");
        lblValChiTieu = new JLabel("0 ₫");

        theTongKhachHang = taoTheThongKe("Tổng khách hàng", lblValTongKH, "Đã phân hạng");
        theNangHangThangNay = taoTheThongKe("Thành viên nâng hạng", lblValNangHang, "Gần đây");
        theGiamGiaDaApDung = taoTheThongKe("Giảm giá đã áp dụng", lblValGiamGia, "Toàn bộ hóa đơn");
        theChiTieuThanhVien = taoTheThongKe("Chi tiêu thành viên", lblValChiTieu, "Tổng doanh thu");

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

    /**
     * Nạp dữ liệu thực tế từ database SALALA và cập nhật giao diện
     */
    public void capNhatThongKe() {
        // 1. Thống kê số liệu trên các thẻ đầu trang
        int tongKH = hangThanhVienDAO.getTongSoThanhVien();
        int soNangHang = hangThanhVienDAO.getSoLuongNangHangGanDay();
        double tongChiTieu = hangThanhVienDAO.getTongChiTieuThanhVien();
        double tongGiamGia = hangThanhVienDAO.getTongGiamGiaDaApDung();

        String strChiTieu = tongChiTieu >= 1_000_000
                ? String.format("%.1f tr", tongChiTieu / 1_000_000)
                : String.format("%,.0f ₫", tongChiTieu);
        String strGiamGia = tongGiamGia >= 1_000_000
                ? String.format("%.1f tr", tongGiamGia / 1_000_000)
                : String.format("%,.0f ₫", tongGiamGia);

        if (lblValTongKH != null)
            lblValTongKH.setText(String.valueOf(tongKH));
        if (lblValNangHang != null)
            lblValNangHang.setText(String.valueOf(soNangHang));
        if (lblValGiamGia != null)
            lblValGiamGia.setText(strGiamGia);
        if (lblValChiTieu != null)
            lblValChiTieu.setText(strChiTieu);

        // 2. Tải và vẽ 4 thẻ hạng thành viên
        panelDanhSachHang.removeAll();
        List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();

        Color[] mauTops = {
                new Color(245, 158, 11), // Đồng: Amber
                new Color(100, 116, 139), // Bạc: Slate
                new Color(217, 119, 6), // Vàng: Amber đậm / Gold
                new Color(2, 132, 199) // Kim cương: Sky blue
        };
        Color[] mauNens = {
                new Color(254, 243, 199),
                new Color(241, 245, 249),
                new Color(254, 240, 138),
                new Color(224, 242, 254)
        };

        for (int i = 0; i < dsHang.size(); i++) {
            HangThanhVien h = dsHang.get(i);
            int soLuong = h.getSoLuongThanhVien();
            double tiLe = tongKH > 0 ? (soLuong * 100.0 / tongKH) : 0.0;

            String dieuKien = formatDieuKienChiTieu(h.getChiTieuToiThieu(), h.getChiTieuToiDa());
            String strGiam = String.format("%.0f%%", h.getMucGiamGia());
            String strSoKhach = soLuong + " khách hàng";
            String strTiLe = String.format("%.1f%% tổng khách", tiLe);

            List<String> qlList = h.getDanhSachQuyenLoi();
            String[] arrQL = qlList.toArray(new String[0]);

            Color mauTop = i < mauTops.length ? mauTops[i] : new Color(79, 70, 229);
            Color mauNen = i < mauNens.length ? mauNens[i] : new Color(243, 244, 246);

            JPanel the = taoTheHang(h.getTenHang(), dieuKien, strGiam, strSoKhach, strTiLe, arrQL, mauTop, mauNen);
            panelDanhSachHang.add(the);
        }

        panelDanhSachHang.revalidate();
        panelDanhSachHang.repaint();
    }

    private String formatDieuKienChiTieu(double min, Double max) {
        if (min <= 0 && max != null) {
            return "Tổng chi tiêu dưới " + String.format("%,.0f ₫", max).replace(',', '.');
        } else if (max == null) {
            return "Tổng chi tiêu từ " + String.format("%,.0f ₫", min).replace(',', '.');
        } else {
            return "Từ " + String.format("%,.0f ₫", min).replace(',', '.') + " đến dưới "
                    + String.format("%,.0f ₫", max).replace(',', '.');
        }
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

        // 5. Danh sách các quyền lợi cụ thể từ database
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
        btnXemDanhSach.setUI(new BasicButtonUI());
        btnXemDanhSach.setRolloverEnabled(false);
        btnXemDanhSach.setFocusPainted(false);
        btnXemDanhSach.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
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
        btnThemHang.addActionListener(e -> hienThiDialogThemHang());
    }

    /**
     * Hộp thoại xem danh sách thành viên thực tế thuộc hạng cụ thể (Đồng / Bạc /
     * Vàng / Kim cương)
     */
    private void hienThiDialogDanhSachTheoHang(String tenHang) {
        List<HangThanhVien> dsThanhVien = hangThanhVienDAO.getDanhSachThanhVienTheoHang(tenHang);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Danh Sách Thành Viên - Hạng " + tenHang + " (" + dsThanhVien.size() + " thành viên)", true);
        dialog.setSize(950, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new BorderLayout(12, 0));
        panelTop.setBackground(Color.WHITE);
        panelTop.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Nhóm khách hàng đạt Hạng " + tenHang + " (Từ bảng QuanLyHangThanhVien)");
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

        // Bảng dữ liệu từ CSDL
        String[] cot = { "Mã hạng", "Mã KH", "Họ và tên", "Số điện thoại", "Email", "Số thú cưng", "Tổng chi tiêu",
                "Điểm tích lũy", "Ngày cập nhật" };
        Object[][] duLieu = new Object[dsThanhVien.size()][9];
        for (int i = 0; i < dsThanhVien.size(); i++) {
            HangThanhVien h = dsThanhVien.get(i);
            duLieu[i][0] = h.getMaHang();
            duLieu[i][1] = h.getMaKH();
            duLieu[i][2] = h.getHoTenKhachHang();
            duLieu[i][3] = h.getSdtKhachHang();
            duLieu[i][4] = h.getEmailKhachHang();
            duLieu[i][5] = h.getSoThuCung() + " bé";
            duLieu[i][6] = String.format("%,.0f ₫", h.getTongChiTieu()).replace(',', '.');
            duLieu[i][7] = h.getDiemTichLuy() + " điểm";
            duLieu[i][8] = h.getNgayCapNhatHang() != null ? sdfNgay.format(h.getNgayCapNhatHang()) : "-";
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

        // Căn phải cho chi tiêu và điểm
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

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
                        String maKH = table.getModel().getValueAt(modelRow, 1).toString();
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

        // Thanh điều khiển phía dưới
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBackground(new Color(248, 250, 252));
        panelBottom.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblHuongDan = new JLabel(
                "Lưu ý: Nhấp đúp vào bất kỳ khách hàng nào để xem chi tiết thú cưng & lịch sử hóa đơn");
        lblHuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelBottom.add(lblHuongDan, BorderLayout.WEST);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtn.setOpaque(false);

        JButton btnChuyenHang = new JButton("Chuyển hạng thành viên");
        btnChuyenHang.setFont(RangBuocGiaoDien.fontVua(13));
        btnChuyenHang.setBackground(Color.WHITE);
        btnChuyenHang.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnChuyenHang.setUI(new BasicButtonUI());
        btnChuyenHang.setRolloverEnabled(false);
        btnChuyenHang.setFocusPainted(false);
        btnChuyenHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        btnChuyenHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnChuyenHang.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn khách hàng cần chuyển hạng trong bảng!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String maHang = table.getModel().getValueAt(modelRow, 0).toString();
            String maKH = table.getModel().getValueAt(modelRow, 1).toString();
            String hoTen = table.getModel().getValueAt(modelRow, 2).toString();

            hienThiDialogChuyenHang(dialog, maHang, maKH, hoTen, tenHang);
        });

        JButton btnDong = new JButton("Đóng");
        btnDong.setPreferredSize(new Dimension(90, 36));
        btnDong.setFont(RangBuocGiaoDien.fontVua(13));
        btnDong.setUI(new BasicButtonUI());
        btnDong.setRolloverEnabled(false);
        btnDong.addActionListener(e -> dialog.dispose());

        pBtn.add(btnChuyenHang);
        pBtn.add(btnDong);
        panelBottom.add(pBtn, BorderLayout.EAST);

        dialog.add(panelBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Hộp thoại chuyển nhanh hạng cho một thành viên
     */
    private void hienThiDialogChuyenHang(JDialog parent, String maHang, String maKH, String hoTen, String hangHienTai) {
        String[] cacHang = { "Đồng", "Bạc", "Vàng", "Kim cương" };
        JComboBox<String> cbo = new JComboBox<>(cacHang);
        cbo.setSelectedItem(hangHienTai);

        JPanel panel = new JPanel(new GridLayout(3, 1, 6, 8));
        panel.add(new JLabel("Khách hàng: " + hoTen + " (" + maKH + ")"));
        panel.add(new JLabel("Chọn hạng thành viên mới:"));
        panel.add(cbo);

        int opt = JOptionPane.showConfirmDialog(parent, panel, "Chuyển hạng thành viên", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String hangMoi = (String) cbo.getSelectedItem();
            if (hangMoi != null && !hangMoi.equals(hangHienTai)) {
                // Lấy thông số tiêu chuẩn của hạng mới
                List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
                HangThanhVien targetHang = null;
                for (HangThanhVien h : dsHang) {
                    if (h.getTenHang().equalsIgnoreCase(hangMoi)) {
                        targetHang = h;
                        break;
                    }
                }

                if (targetHang != null) {
                    // Cập nhật hạng trong QuanLyHangThanhVien
                    HangThanhVien updateObj = new HangThanhVien();
                    updateObj.setMaHang(maHang);
                    updateObj.setMaKH(maKH);
                    updateObj.setTenHang(hangMoi);
                    updateObj.setChiTieuToiThieu(targetHang.getChiTieuToiThieu());
                    updateObj.setChiTieuToiDa(targetHang.getChiTieuToiDa());
                    updateObj.setMucGiamGia(targetHang.getMucGiamGia());
                    updateObj.setDiemTichLuy1Van(targetHang.getDiemTichLuy1Van());
                    updateObj.setQuyenLoi(targetHang.getQuyenLoi());

                    // Giữ nguyên tổng chi tiêu và tính lại điểm
                    HangThanhVien current = hangThanhVienDAO.getHangThanhVienTheoMaKH(maKH);
                    double spending = current != null ? current.getTongChiTieu() : 0.0;
                    int diem = (int) Math.floor(spending / 10000.0 * targetHang.getDiemTichLuy1Van());
                    updateObj.setTongChiTieu(spending);
                    updateObj.setDiemTichLuy(diem);
                    updateObj.setNgayCapNhatHang(new java.sql.Timestamp(System.currentTimeMillis()));
                    updateObj.setTrangThai("Đang áp dụng");

                    boolean ok = hangThanhVienDAO.capNhatHangThanhVien(updateObj);
                    if (ok) {
                        JOptionPane.showMessageDialog(parent,
                                "Chuyển khách hàng " + hoTen + " sang hạng " + hangMoi + " thành công!",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        parent.dispose();
                        capNhatThongKe();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Không thể cập nhật hạng thành viên. Vui lòng kiểm tra kết nối!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Hộp thoại xem chi tiết hồ sơ khách hàng ngay từ màn hình Hạng thành viên
     */
    private void hienThiDialogXemChiTietKhachHang(String maKH) {
        KhachHang kh = khachHangDAO.getKhachHangTheoMa(maKH);
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

        List<Object[]> dsThuCung = khachHangDAO.getDanhSachThuCungCuaKhachHang(maKH);
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

        List<Object[]> dsHoaDon = khachHangDAO.getDanhSachHoaDonCuaKhachHang(maKH);
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
        bDong.setUI(new BasicButtonUI());
        bDong.setRolloverEnabled(false);
        bDong.addActionListener(e -> dialog.dispose());
        pBottom.add(bDong);
        dialog.add(pBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại thêm / cấu hình chính sách hạng thành viên và phân hạng khách hàng
     */
    private void hienThiDialogThemHang() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm / Tùy Chỉnh Hạng Thành Viên",
                true);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(RangBuocGiaoDien.fontDam(13));

        // TAB 1: Cấu hình chính sách hạng (Đồng, Bạc, Vàng, Kim Cương)
        JPanel tabChinhSach = new JPanel();
        tabChinhSach.setLayout(new BoxLayout(tabChinhSach, BoxLayout.Y_AXIS));
        tabChinhSach.setBackground(Color.WHITE);
        tabChinhSach.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] dsTenHang = { "Đồng", "Bạc", "Vàng", "Kim cương" };
        JComboBox<String> cboTenHang = new JComboBox<>(dsTenHang);
        cboTenHang.setPreferredSize(new Dimension(0, 36));
        cboTenHang.setFont(RangBuocGiaoDien.fontVua(13));

        JTextField txtChiTieuMin = new JTextField();
        JTextField txtChiTieuMax = new JTextField();
        JTextField txtGiamGia = new JTextField();
        JTextField txtDiemTichLuy1Van = new JTextField();
        JTextArea txtQuyenLoi = new JTextArea(4, 20);
        txtQuyenLoi.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        txtQuyenLoi.setFont(RangBuocGiaoDien.fontThuong(12));

        // Hàm nạp thông tin hạng vào form khi chọn ComboBox
        Runnable napThongTinHang = () -> {
            String selected = (String) cboTenHang.getSelectedItem();
            List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
            for (HangThanhVien h : dsHang) {
                if (h.getTenHang().equalsIgnoreCase(selected)) {
                    txtChiTieuMin.setText(String.format("%.0f", h.getChiTieuToiThieu()));
                    txtChiTieuMax
                            .setText(h.getChiTieuToiDa() != null ? String.format("%.0f", h.getChiTieuToiDa()) : "");
                    txtGiamGia.setText(String.format("%.1f", h.getMucGiamGia()));
                    txtDiemTichLuy1Van.setText(String.format("%.1f", h.getDiemTichLuy1Van()));
                    txtQuyenLoi.setText(h.getQuyenLoi() != null ? h.getQuyenLoi() : "");
                    break;
                }
            }
        };
        napThongTinHang.run();
        cboTenHang.addActionListener(e -> napThongTinHang.run());

        tabChinhSach.add(taoHangNhapLieu("Hạng thành viên:", cboTenHang));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Chi tiêu tối thiểu (₫):", txtChiTieuMin));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Chi tiêu tối đa (₫):", txtChiTieuMax));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Mức giảm giá (%):", txtGiamGia));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Điểm / 10.000 ₫:", txtDiemTichLuy1Van));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel pQL = new JPanel(new BorderLayout(8, 0));
        pQL.setOpaque(false);
        JLabel lblQL = new JLabel("Quyền lợi áp dụng:");
        lblQL.setPreferredSize(new Dimension(150, 24));
        lblQL.setFont(RangBuocGiaoDien.fontVua(13));
        pQL.add(lblQL, BorderLayout.NORTH);
        pQL.add(new JScrollPane(txtQuyenLoi), BorderLayout.CENTER);
        tabChinhSach.add(pQL);

        tabbedPane.addTab("Cấu hình chính sách hạng", tabChinhSach);

        // TAB 2: Phân hạng / gán hạng cho khách hàng
        JPanel tabPhanHang = new JPanel();
        tabPhanHang.setLayout(new BoxLayout(tabPhanHang, BoxLayout.Y_AXIS));
        tabPhanHang.setBackground(Color.WHITE);
        tabPhanHang.setBorder(new EmptyBorder(16, 20, 16, 20));

        ArrayList<KhachHang> dsKH = khachHangDAO.getAlltbKhachHang();
        JComboBox<String> cboKhachHang = new JComboBox<>();
        cboKhachHang.setPreferredSize(new Dimension(0, 36));
        cboKhachHang.setFont(RangBuocGiaoDien.fontThuong(13));
        for (KhachHang kh : dsKH) {
            cboKhachHang.addItem(kh.getMaKH() + " - " + kh.getHoTen());
        }

        JComboBox<String> cboHangMoi = new JComboBox<>(dsTenHang);
        cboHangMoi.setPreferredSize(new Dimension(0, 36));
        cboHangMoi.setFont(RangBuocGiaoDien.fontVua(13));

        JLabel lblThongBaoKH = new JLabel("Gán trực tiếp hạng thành viên cho khách hàng đã đăng ký trong CSDL.");
        lblThongBaoKH.setFont(RangBuocGiaoDien.fontThuong(12));
        lblThongBaoKH.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        tabPhanHang.add(lblThongBaoKH);
        tabPhanHang.add(Box.createRigidArea(new Dimension(0, 14)));
        tabPhanHang.add(taoHangNhapLieu("Khách hàng:", cboKhachHang));
        tabPhanHang.add(Box.createRigidArea(new Dimension(0, 14)));
        tabPhanHang.add(taoHangNhapLieu("Hạng thành viên:", cboHangMoi));
        tabPhanHang.add(Box.createVerticalGlue());

        tabbedPane.addTab("Phân hạng khách hàng", tabPhanHang);

        dialog.add(tabbedPane, BorderLayout.CENTER);

        // Nút lưu / hủy
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panelBottom.setBackground(new Color(248, 250, 252));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(90, 36));
        btnHuy.setFont(RangBuocGiaoDien.fontVua(13));
        btnHuy.setUI(new BasicButtonUI());
        btnHuy.setRolloverEnabled(false);
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = new JButton("Lưu cấu hình");
        btnLuu.setPreferredSize(new Dimension(140, 36));
        btnLuu.setFont(RangBuocGiaoDien.fontDam(13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setUI(new BasicButtonUI());
        btnLuu.setRolloverEnabled(false);
        btnLuu.setFocusPainted(false);

        btnLuu.addActionListener(e -> {
            int selectedTab = tabbedPane.getSelectedIndex();
            if (selectedTab == 0) {
                // Lưu chính sách hạng
                String ten = (String) cboTenHang.getSelectedItem();
                String minStr = txtChiTieuMin.getText().trim();
                String maxStr = txtChiTieuMax.getText().trim();
                String giamStr = txtGiamGia.getText().trim();
                String diemStr = txtDiemTichLuy1Van.getText().trim();
                String ql = txtQuyenLoi.getText().trim();

                double minSpend = 0;
                try {
                    minSpend = Double.parseDouble(minStr.replace(".", "").replace(",", ""));
                    if (minSpend < 0)
                        throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Chi tiêu tối thiểu phải là số >= 0!", "Lỗi định dạng",
                            JOptionPane.WARNING_MESSAGE);
                    txtChiTieuMin.requestFocus();
                    return;
                }

                Double maxSpend = null;
                if (!maxStr.isEmpty()) {
                    try {
                        maxSpend = Double.parseDouble(maxStr.replace(".", "").replace(",", ""));
                        if (maxSpend <= minSpend) {
                            JOptionPane.showMessageDialog(dialog, "Chi tiêu tối đa phải lớn hơn chi tiêu tối thiểu!",
                                    "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                            txtChiTieuMax.requestFocus();
                            return;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Chi tiêu tối đa phải là số hợp lệ!", "Lỗi định dạng",
                                JOptionPane.WARNING_MESSAGE);
                        txtChiTieuMax.requestFocus();
                        return;
                    }
                }

                double giamGia = 0;
                try {
                    giamGia = Double.parseDouble(giamStr);
                    if (giamGia < 0 || giamGia > 100)
                        throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Mức giảm giá phải từ 0% đến 100%!", "Lỗi định dạng",
                            JOptionPane.WARNING_MESSAGE);
                    txtGiamGia.requestFocus();
                    return;
                }

                double diem1Van = 1.0;
                try {
                    diem1Van = Double.parseDouble(diemStr);
                    if (diem1Van < 0)
                        throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Điểm tích lũy trên 1 vạn phải là số >= 0!", "Lỗi định dạng",
                            JOptionPane.WARNING_MESSAGE);
                    txtDiemTichLuy1Van.requestFocus();
                    return;
                }

                boolean ok = hangThanhVienDAO.capNhatChinhSachHang(ten, minSpend, maxSpend, giamGia, diem1Van, ql);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Đã lưu chính sách hạng \"" + ten + "\" thành công vào database SALALA!\n" +
                                    "- Chi tiêu: từ " + String.format("%,.0f ₫", minSpend)
                                    + (maxSpend != null ? " đến " + String.format("%,.0f ₫", maxSpend) : " trở lên")
                                    + "\n" +
                                    "- Giảm giá: " + giamGia + "% | Điểm/10.000₫: " + diem1Van,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    capNhatThongKe();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Không thể cập nhật chính sách hạng. Vui lòng thử lại!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Phân hạng khách hàng
                if (cboKhachHang.getItemCount() == 0) {
                    JOptionPane.showMessageDialog(dialog, "Không có khách hàng nào trong hệ thống!", "Thông báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String khSelected = (String) cboKhachHang.getSelectedItem();
                String maKH = khSelected.split(" - ")[0].trim();
                String tenHangMoi = (String) cboHangMoi.getSelectedItem();

                List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
                HangThanhVien targetHang = null;
                for (HangThanhVien h : dsHang) {
                    if (h.getTenHang().equalsIgnoreCase(tenHangMoi)) {
                        targetHang = h;
                        break;
                    }
                }

                if (targetHang != null) {
                    HangThanhVien existing = hangThanhVienDAO.getHangThanhVienTheoMaKH(maKH);
                    boolean ok;
                    if (existing != null) {
                        existing.setTenHang(tenHangMoi);
                        existing.setChiTieuToiThieu(targetHang.getChiTieuToiThieu());
                        existing.setChiTieuToiDa(targetHang.getChiTieuToiDa());
                        existing.setMucGiamGia(targetHang.getMucGiamGia());
                        existing.setDiemTichLuy1Van(targetHang.getDiemTichLuy1Van());
                        existing.setQuyenLoi(targetHang.getQuyenLoi());
                        existing.setDiemTichLuy((int) Math
                                .floor(existing.getTongChiTieu() / 10000.0 * targetHang.getDiemTichLuy1Van()));
                        existing.setNgayCapNhatHang(new java.sql.Timestamp(System.currentTimeMillis()));
                        ok = hangThanhVienDAO.capNhatHangThanhVien(existing);
                    } else {
                        String newMaHang = hangThanhVienDAO.phatSinhMaHangMoi();
                        HangThanhVien newHtv = new HangThanhVien(
                                newMaHang, maKH, tenHangMoi,
                                targetHang.getChiTieuToiThieu(), targetHang.getChiTieuToiDa(),
                                targetHang.getMucGiamGia(), targetHang.getDiemTichLuy1Van(),
                                targetHang.getQuyenLoi(), 0.0, 0,
                                new java.sql.Timestamp(System.currentTimeMillis()), "Đang áp dụng");
                        ok = hangThanhVienDAO.themHangThanhVien(newHtv);
                    }

                    if (ok) {
                        JOptionPane.showMessageDialog(this,
                                "Phân khách hàng " + khSelected + " sang hạng " + tenHangMoi + " thành công!",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        capNhatThongKe();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Không thể lưu phân hạng. Vui lòng thử lại!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panelBottom.add(btnHuy);
        panelBottom.add(btnLuu);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel taoHangNhapLieu(String nhan, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(nhan);
        lbl.setPreferredSize(new Dimension(150, 36));
        lbl.setFont(RangBuocGiaoDien.fontVua(13));
        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        comp.setPreferredSize(new Dimension(0, 36));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        }
        p.add(lbl, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    public HangThanhVien_DAO getHangThanhVienDAO() {
        return hangThanhVienDAO;
    }
}
