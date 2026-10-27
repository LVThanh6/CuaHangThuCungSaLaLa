package GUI;

import java.awt.*;
import javax.swing.*;

import GUI.BacSi.BacSiGUI;
import GUI.Chung.HeaderGUI;
import GUI.Chung.RangBuocGiaoDien;
import GUI.DichVu.DichVu;
import GUI.HoaDon.DSHoaDon_gui;
import GUI.HoaDon.LapHoaDon_gui;
import GUI.KhachHang.DanhSachKhachHangGUI;
import GUI.KhachHang.QuanLyHangThanhVienGUI;
import GUI.LichHen.DatLichMoiGUI;
import GUI.LichHen.XemLichGUI;
import GUI.NhanVien.NhanVienGUI;
import GUI.ThongKe.ThongKeGUI;
import GUI.ThuCung.DonThuocGUI;
import GUI.ThuCung.HoSoThuCungGUI;
import GUI.ThuCung.LichSuKhamGUI;
import GUI.VatTu.VatTu;
import GUI.TaiKhoan.DangNhapGUI;
import dao.DangNhap_DAO;

/**
 * Class GiaoDienChinhGUI: Khung giao diện chính của hệ thống SALALA Pet Shop.
 * Bao gồm 2 phần:
 * 1. Header (HeaderGUI): Hiển thị logo, thông tin và phân quyền tabs theo vai
 * trò người dùng (BorderLayout.NORTH).
 * 2. Main (panelNoiDung): Vùng nội dung chính hiển thị các tab, chừa sẵn các
 * hàm rỗng để nhúng class khác (BorderLayout.CENTER).
 */
public class GiaoDienChinhGUI extends JPanel {

    // --- CÁC THÀNH PHẦN GIAO DIỆN CHÍNH ---
    private HeaderGUI headerGUI; // Phần 1: Thanh Header
    private JPanel panelNoiDung; // Phần 2: Vùng Main chứa nội dung tab bên trong
    private JLabel nhanNoiDung;
    private String tabHienTai;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public GiaoDienChinhGUI() {
        // Tự động lấy thông tin từ phiên đăng nhập hiện tại nếu có
        this(dao.DangNhap_DAO.isDaDangNhap() ? dao.DangNhap_DAO.getTenNguoiDung() : "Lê Văn Thành", 
             dao.DangNhap_DAO.isDaDangNhap() ? dao.DangNhap_DAO.getVaiTro() : "Quản lý");
    }

    public GiaoDienChinhGUI(String tenNguoiDung, String chucVu) {
        khoiTaoGiaoDien(tenNguoiDung, chucVu);
    }

    // ==========================================
    // KHỞI TẠO BỐ CỤC CHÍNH (HEADER + MAIN)
    // ==========================================
    private void khoiTaoGiaoDien(String tenNguoiDung, String chucVu) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
        setPreferredSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);

        // 1. NHÚNG PHẦN HEADER VÀO NORTH
        headerGUI = new HeaderGUI(tenNguoiDung, chucVu);
        add(headerGUI, BorderLayout.NORTH);

        // 2. NHÚNG PHẦN MAIN (NỘI DUNG) VÀO CENTER
        panelNoiDung = new JPanel(new BorderLayout());
        panelNoiDung.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        panelNoiDung.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);
        panelNoiDung.setPreferredSize(
                new Dimension(RangBuocGiaoDien.MAN_HINH_RONG_MAC_DINH, RangBuocGiaoDien.MAN_HINH_CAO_MAC_DINH - 100));
        add(panelNoiDung, BorderLayout.CENTER);

        // 3. LẮNG NGHE SỰ KIỆN CHUYỂN TAB TỪ HEADER
        headerGUI.setTabSelectionListener((tenTab, menuCon) -> {
            if (menuCon != null && !menuCon.isEmpty()) {
                xuLyChonMenuCon(tenTab, menuCon);
            } else {
                xuLyChonTab(tenTab);
            }
        });

        // Hiển thị tab mặc định ban đầu
        if (headerGUI.getTabHienTai() != null) {
            xuLyChonTab(headerGUI.getTabHienTai());
        }
    }

    // ==========================================
    // CƠ CHẾ NHÚNG GIAO DIỆN (SET NỘI DUNG)
    // ==========================================
    /**
     * Nhúng một Component (JPanel, JScrollPane, ...) vào vùng nội dung chính.
     * Mặc định component sẽ co giãn 100% diện tích của tab.
     */
    public void setNoiDung(Component comp) {
        setNoiDung(comp, false);
    }

    public void setNoiDung(Component comp, boolean cuonTrang) {
        if (panelNoiDung == null)
            return;
        panelNoiDung.removeAll();
        if (comp != null) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);
            }

            if (cuonTrang && !(comp instanceof JScrollPane)) {
                JScrollPane scrollPane = new JScrollPane(comp);
                scrollPane.setBorder(null);
                scrollPane.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
                panelNoiDung.add(scrollPane, BorderLayout.CENTER);
            } else {
                panelNoiDung.add(comp, BorderLayout.CENTER);
            }
        } else {
            hienThiNoiDungMacDinh("Đang xem tab: " + tabHienTai);
        }
        panelNoiDung.revalidate();
        panelNoiDung.repaint();
    }

    public void setNoiDung(Object view) {
        if (view instanceof Component) {
            setNoiDung((Component) view);
        }
    }

    public void hienThiNoiDungMacDinh(String thongBao) {
        if (panelNoiDung == null)
            return;
        if (nhanNoiDung == null) {
            nhanNoiDung = new JLabel();
            nhanNoiDung.setHorizontalAlignment(SwingConstants.CENTER);
            nhanNoiDung.setFont(RangBuocGiaoDien.fontDam(18));
            nhanNoiDung.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        }
        nhanNoiDung.setText(thongBao);

        JPanel vungMacDinh = new JPanel(new GridBagLayout());
        vungMacDinh.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        vungMacDinh.add(nhanNoiDung);

        panelNoiDung.removeAll();
        panelNoiDung.add(vungMacDinh, BorderLayout.CENTER);
        panelNoiDung.revalidate();
        panelNoiDung.repaint();
    }

    // ==========================================
    // ĐIỀU HƯỚNG SỰ KIỆN CHỌN TAB & MENU CON
    // ==========================================
    private void xuLyChonTab(String tenTab) {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem tab: " + tenTab);

        switch (tenTab) {
            case "Nhân viên":
                hienThiTabNhanVien();
                break;
            case "Bác sĩ":
                hienThiTabBacSi();
                break;
            case "Thú cưng":
                hienThiTabThuCung();
                break;
            case "Khách hàng":
                hienThiTabKhachHang();
                break;
            case "Hóa đơn":
                hienThiTabHoaDon();
                break;
            case "Vật tư":
                hienThiTabVatTu();
                break;
            case "Lịch hẹn":
                hienThiTabLichHen();
                break;
            case "Dịch vụ":
                hienThiTabDichVu();
                break;
            case "Thống kê":
                hienThiThongKe();
                break;
        }
    }

    private void xuLyChonMenuCon(String tenTab, String tenMenuCon) {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem: " + tenTab + " ➔ " + tenMenuCon);

        switch (tenMenuCon) {
            // Tab Nhân viên
            case "Danh sách nhân viên":
                hienThiDanhSachNhanVien();
                break;
            case "Danh sách bác sĩ":
                hienThiDanhSachBacSi();
                break;
            case "Thống kê":
                hienThiThongKe();
                break;

            // Tab Thú cưng
            case "Hồ sơ thú cưng":
                hienThiHoSoThuCung();
                break;
            case "Đơn Thuốc":
                hienThiDonThuoc();
                break;
            case "Lịch sử khám":
                hienThiLichSuKham();
                break;

            // Tab Khách hàng
            case "Danh sách khách hàng":
                hienThiDanhSachKhachHang();
                break;
            case "Quản lý hạng thành viên":
            case "Thẻ thành viên":
                hienThiTheThanhVien();
                break;

            // Tab Hóa đơn
            case "Tạo hóa đơn mới":
                hienThiTaoHoaDonMoi();
                break;
            case "Danh sách hóa đơn":
                hienThiDanhSachHoaDon();
                break;

            // Tab Lịch hẹn
            case "Xem lịch":
                hienThiXemLich();
                break;
            case "Đặt lịch mới":
                hienThiDatLichMoi();
                break;

            // Tab Dịch vụ
            case "Bảng giá dịch vụ":
                hienThiBangGiaDichVu();
                break;
            case "Gói chăm sóc":
                hienThiGoiChamSoc();
                break;
        }
    }

    // =========================================================================
    // CÁC HÀM CHO TAB CHÍNH VÀ MENU CON
    // =========================================================================

    public void hienThiTabNhanVien() { hienThiDanhSachNhanVien(); }
    public void hienThiTabBacSi() { hienThiDanhSachBacSi(); }
    public void hienThiTabThuCung() { hienThiHoSoThuCung(); }
    public void hienThiTabKhachHang() { hienThiDanhSachKhachHang(); }
    public void hienThiTabHoaDon() { hienThiDanhSachHoaDon(); }
    public void hienThiTabVatTu() { setNoiDung(new VatTu()); }
    public void hienThiTabLichHen() { hienThiXemLich(); }
    public void hienThiTabDichVu() { hienThiBangGiaDichVu(); }

    public void hienThiDanhSachNhanVien() { setNoiDung(new NhanVienGUI()); }
    public void hienThiDanhSachBacSi() { setNoiDung(new BacSiGUI()); }
    public void hienThiThongKe() { setNoiDung(new ThongKeGUI()); }
    public void hienThiHoSoThuCung() { setNoiDung(new HoSoThuCungGUI()); }
    public void hienThiDonThuoc() { setNoiDung(new DonThuocGUI()); }
    public void hienThiLichSuKham() { setNoiDung(new LichSuKhamGUI()); }
    
    public void hienThiDanhSachKhachHang() {
        try {
            setNoiDung(new DanhSachKhachHangGUI());
        } catch (Throwable t) {
            t.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi hiển thị Khách hàng: " + t.getMessage());
        }
    }

    public void hienThiTheThanhVien() { hienThiQuanLyHangThanhVien(); }

    public void hienThiQuanLyHangThanhVien() {
        try {
            setNoiDung(new QuanLyHangThanhVienGUI());
        } catch (Throwable t) {
            t.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi hiển thị Hạng thành viên: " + t.getMessage());
        }
    }

    public void hienThiTaoHoaDonMoi() { setNoiDung(new LapHoaDon_gui()); }
    public void hienThiDanhSachHoaDon() { setNoiDung(new DSHoaDon_gui()); }
    public void hienThiXemLich() { setNoiDung(new XemLichGUI()); }
    public void hienThiDatLichMoi() { setNoiDung(new DatLichMoiGUI()); }
    public void hienThiBangGiaDichVu() { setNoiDung(new DichVu()); }
    public void hienThiGoiChamSoc() { /* Bổ sung sau */ }


    // ==========================================
    // TIỆN ÍCH ĐĂNG XUẤT
    // ==========================================
    /**
     * Gọi hàm này khi người dùng bấm nút Đăng xuất trên HeaderGUI.
     * Xóa dao.DangNhap_DAO, đóng frame chính và mở lại form đăng nhập.
     * @param frameChinh JFrame chứa giao diện hiện tại
     */
    public static void xuLyDangXuat(JFrame frameChinh) {
        dao.DangNhap_DAO.dangXuat(); // Hủy thông tin phiên làm việc
        
        if (frameChinh != null) {
            frameChinh.dispose(); // Đóng giao diện chính
        }
        
        // Mở lại màn hình đăng nhập
        DangNhapGUI login = new DangNhapGUI();
        login.setVisible(true);
    }

    // ==========================================
    // TIỆN ÍCH CÀI ĐẶT CỬA SỔ CHUẨN
    // ==========================================
    public static void caiDatKichThuocCuaSo(JFrame frame, boolean moFullManHinh) {
        frame.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
        frame.setPreferredSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);
        frame.setSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);
        frame.setLocationRelativeTo(null);
        if (moFullManHinh) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public HeaderGUI getHeaderGUI() { return headerGUI; }
    public void setHeaderGUI(HeaderGUI headerGUI) { this.headerGUI = headerGUI; }
    public JPanel getPanelNoiDung() { return panelNoiDung; }
    public void setPanelNoiDung(JPanel panelNoiDung) { this.panelNoiDung = panelNoiDung; }
    public String getTabHienTai() { return tabHienTai; }
    public JLabel getNhanNoiDung() { return nhanNoiDung; }
    public void setNhanNoiDung(JLabel nhanNoiDung) { this.nhanNoiDung = nhanNoiDung; }

    public void setTabHienTai(String tenTab) {
        if (headerGUI != null) {
            headerGUI.setTabHienTai(tenTab);
        } else {
            xuLyChonTab(tenTab);
        }
    }

    // ==========================================
    // MAIN KHỞI ĐỘNG CHƯƠNG TRÌNH
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            // 1. Khởi tạo kết nối CSDL SALALA
            try {
                connectDB.ConnectDB.getInstance().connect();
            } catch (Exception ignored) {}

            // 2. Chạy ứng dụng: Gọi giao diện Đăng Nhập đầu tiên thay vì giao diện chính
            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}