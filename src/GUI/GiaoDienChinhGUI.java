package GUI;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

import GUI.BacSi.BacSiGUI;
import GUI.Chung.HeaderGUI;
import GUI.Chung.RangBuocGiaoDien;
import GUI.DichVu.DichVuGUI;
import GUI.DichVu.KhuyenMaiGUI;
import GUI.DichVu.ThueGUI;
import GUI.HoaDon.DSHoaDon_gui;
import GUI.HoaDon.LapHoaDon_gui;
import GUI.KhachHang.DanhSachKhachHangGUI;
import GUI.KhachHang.QuanLyHangThanhVienGUI;

// IMPORT GUI LỊCH & LỊCH HẸN
import GUI.LichHen.DatLichMoiGUI;
import GUI.LichHen.XemLichGUI;
import GUI.LichHen.LichLamViecGUI; 

import GUI.NhanVien.NhanVienGUI;
import GUI.ThongKe.ThongKeGUI;
import GUI.ThuCung.DonThuocGUI;
import GUI.ThuCung.HoSoThuCungGUI;
import GUI.ThuCung.LichSuKhamGUI;
import GUI.TaiKhoan.DangNhapGUI;
import GUI.TaiKhoan.QuanLyTaiKhoanGUI;
import dao.DangNhap_DAO;

// Các GUI thuộc tab Vật tư
import GUI.VatTu.VatTuGUI;
import GUI.VatTu.NhaCungCapGUI;
import GUI.VatTu.PhieuNhapKhoGUI;
import GUI.VatTu.PhieuXuatKhoGUI;

public class GiaoDienChinhGUI extends JPanel {

    // --- CÁC THÀNH PHẦN GIAO DIỆN CHÍNH ---
    private HeaderGUI headerGUI; 
    private JPanel panelNoiDung; 
    private JLabel nhanNoiDung;
    private String tabHienTai;

    // --- KHAI BÁO CÁC ĐỐI TƯỢNG DÙNG CHUNG CHO TAB VẬT TƯ ---
    private VatTuGUI vatTuGUI;
    private PhieuNhapKhoGUI phieuNhapGUI;
    private PhieuXuatKhoGUI phieuXuatGUI;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public GiaoDienChinhGUI() {
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

        // --- KHỞI TẠO DỮ LIỆU LIÊN KẾT CHO TAB VẬT TƯ ---
        vatTuGUI = new VatTuGUI();
        
        phieuNhapGUI = new PhieuNhapKhoGUI(
                vatTuGUI::getDanhSachVatTu,
                vatTuGUI::tangTon,
                () -> {
                    try {
                        if (headerGUI != null) {
                            headerGUI.chonMenuCon("Vật tư", "Phiếu xuất kho");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        phieuXuatGUI = new PhieuXuatKhoGUI(
                vatTuGUI::getDanhSachVatTu,
                vatTuGUI::tangTon, 
                vatTuGUI::getTonKho);

        // 1. NHÚNG PHẦN HEADER VÀO NORTH
        headerGUI = new HeaderGUI(tenNguoiDung, chucVu);
        add(headerGUI, BorderLayout.NORTH);

        // 2. NHÚNG PHẦN MAIN (NỘI DUNG) VÀO CENTER
        panelNoiDung = new JPanel(new BorderLayout());
        panelNoiDung.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        panelNoiDung.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);
        panelNoiDung.setPreferredSize(new Dimension(RangBuocGiaoDien.MAN_HINH_RONG_MAC_DINH, RangBuocGiaoDien.MAN_HINH_CAO_MAC_DINH - 100));
        add(panelNoiDung, BorderLayout.CENTER);

        // 3. LẮNG NGHE SỰ KIỆN CHUYỂN TAB TỪ HEADER
        headerGUI.setTabSelectionListener((tenTab, menuCon) -> {
            if (menuCon != null && !menuCon.isEmpty()) {
                try {
                    xuLyChonMenuCon(tenTab, menuCon);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                xuLyChonTab(tenTab);
            }
        });

        if (headerGUI.getTabHienTai() != null) {
            xuLyChonTab(headerGUI.getTabHienTai());
        }
    }

    // ==========================================
    // CƠ CHẾ NHÚNG GIAO DIỆN (SET NỘI DUNG)
    // ==========================================
    public void setNoiDung(Component comp, boolean cuonTrang) {
        if (panelNoiDung == null) return;
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
    
    public void setNoiDung(Component comp) { setNoiDung(comp, false); }
    public void setNoiDung(Object view) { if (view instanceof Component) setNoiDung((Component) view); }

    public void hienThiNoiDungMacDinh(String thongBao) {
        if (panelNoiDung == null) return;
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

        // Tự động mở menu con đầu tiên mà user được phân quyền trong tab này
        String[] menuCons = (headerGUI != null) ? headerGUI.getMenuConCuaTab(tenTab) : null;
        if (menuCons != null && menuCons.length > 0) {
            try {
                xuLyChonMenuCon(tenTab, menuCons[0]);
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        hienThiNoiDungMacDinh("Đang xem tab: " + tenTab);

        switch (tenTab) {
            case "Nhân viên": hienThiTabNhanVien(); break;
            case "Bác sĩ": hienThiTabBacSi(); break;
            case "Thú cưng": hienThiTabThuCung(); break;
            case "Khách hàng": hienThiTabKhachHang(); break;
            case "Hóa đơn": hienThiTabHoaDon(); break;
            case "Vật tư": hienThiTabVatTu(); break;
            case "Lịch": hienThiTabLich(); break; 
            case "Dịch vụ": hienThiTabDichVu(); break;
            case "Thống kê": hienThiThongKe(); break;
            case "Quản lý tài khoản": hienThiQuanLyTaiKhoan(); break;
        }
    }

    private void xuLyChonMenuCon(String tenTab, String tenMenuCon) throws SQLException {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem: " + tenTab + " ➔ " + tenMenuCon);

        switch (tenMenuCon) {
            case "Danh sách nhân viên": hienThiDanhSachNhanVien(); break;
            case "Danh sách bác sĩ": hienThiDanhSachBacSi(); break;
            case "Thống kê": hienThiThongKe(); break;
            case "Quản lý tài khoản": hienThiQuanLyTaiKhoan(); break;
            case "Hồ sơ thú cưng": hienThiHoSoThuCung(); break;
            case "Đơn Thuốc": hienThiDonThuoc(); break;
            case "Lịch sử khám": hienThiLichSuKham(); break;
            case "Danh sách khách hàng": hienThiDanhSachKhachHang(); break;
            case "Quản lý hạng thành viên":
            case "Thẻ thành viên": hienThiTheThanhVien(); break;
            case "Tạo hóa đơn mới": hienThiTaoHoaDonMoi(); break;
            case "Danh sách hóa đơn": hienThiDanhSachHoaDon(); break;
            case "Vật tư": hienThiVatTu(); break;
            case "Nhà cung cấp": hienThiNhaCungCap(); break;
            case "Phiếu nhập kho": hienThiPhieuNhapKho(); break;
            case "Phiếu xuất kho": hienThiPhieuXuatKho(); break;
            case "Bảng giá dịch vụ": hienThiBangGiaDichVu(); break;
            case "Khuyến mãi": hienThiKhuyenMai(); break;
            case "Thuế": hienThiThue(); break;
            
            // Tab Lịch & Lịch làm việc
            case "Lịch hẹn (Khám)": hienThiXemLich(); break; 
            case "Đặt lịch mới": hienThiDatLichMoi(); break;
            case "Lịch làm việc": hienThiLichLamViec(); break; 
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
    public void hienThiTabVatTu() { hienThiVatTu(); }
    public void hienThiTabLich() { hienThiXemLich(); } 
    public void hienThiTabDichVu() { hienThiBangGiaDichVu(); }

    public void hienThiDanhSachNhanVien() { setNoiDung(new NhanVienGUI()); }
    public void hienThiDanhSachBacSi() { setNoiDung(new BacSiGUI()); }
    public void hienThiThongKe() { setNoiDung(new ThongKeGUI()); }
    public void hienThiQuanLyTaiKhoan() { setNoiDung(new QuanLyTaiKhoanGUI()); }
    public void hienThiHoSoThuCung() { setNoiDung(new HoSoThuCungGUI()); }
    public void hienThiDonThuoc() { setNoiDung(new DonThuocGUI()); }
    public void hienThiLichSuKham() { setNoiDung(new LichSuKhamGUI()); }
    public void hienThiVatTu() { setNoiDung(vatTuGUI); }
    public void hienThiNhaCungCap() { setNoiDung(new NhaCungCapGUI()); }
    public void hienThiPhieuNhapKho() { setNoiDung(phieuNhapGUI); }
    public void hienThiPhieuXuatKho() { setNoiDung(phieuXuatGUI); }
    
    public void hienThiDanhSachKhachHang() {
        try { setNoiDung(new DanhSachKhachHangGUI()); } 
        catch (Throwable t) { hienThiNoiDungMacDinh("Lỗi hiển thị Khách hàng: " + t.getMessage()); }
    }
    public void hienThiTheThanhVien() { hienThiQuanLyHangThanhVien(); }
    public void hienThiQuanLyHangThanhVien() {
        try { setNoiDung(new QuanLyHangThanhVienGUI()); } 
        catch (Throwable t) { hienThiNoiDungMacDinh("Lỗi hiển thị Hạng thành viên: " + t.getMessage()); }
    }

    public void hienThiTaoHoaDonMoi() throws SQLException { setNoiDung(new LapHoaDon_gui()); }
    public void hienThiDanhSachHoaDon() {
        setNoiDung(new DSHoaDon_gui(() -> {
            try { hienThiTaoHoaDonMoi(); } catch (SQLException e) { e.printStackTrace(); }
        }));
    }
    
    public void hienThiXemLich() { setNoiDung(new XemLichGUI()); }
    public void hienThiDatLichMoi() { setNoiDung(new DatLichMoiGUI()); }
    public void hienThiLichLamViec() { setNoiDung(new LichLamViecGUI()); }
    
    public void hienThiBangGiaDichVu() { setNoiDung(new DichVuGUI()); }
    public void hienThiKhuyenMai() { setNoiDung(new KhuyenMaiGUI()); }
    public void hienThiThue() { setNoiDung(new ThueGUI()); }

    // ==========================================
    // TIỆN ÍCH ĐĂNG XUẤT
    // ==========================================
    public static void xuLyDangXuat(JFrame frameChinh) {
        dao.DangNhap_DAO.dangXuat();
        if (frameChinh != null) frameChinh.dispose();
        DangNhapGUI login = new DangNhapGUI();
        login.setVisible(true);
    }

    public static void caiDatKichThuocCuaSo(JFrame frame, boolean moFullManHinh) {
        frame.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
        if (moFullManHinh) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            frame.setPreferredSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);
            frame.pack();
            frame.setLocationRelativeTo(null);
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

            // 2. Chạy ứng dụng: Gọi giao diện Đăng Nhập đầu tiên
            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}
