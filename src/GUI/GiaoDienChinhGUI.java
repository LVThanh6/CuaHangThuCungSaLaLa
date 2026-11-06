package GUI;


import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;


import GUI.BacSi.BacSiGUI;
import GUI.Chung.HeaderGUI;
import GUI.Chung.LamMoNen;
import GUI.Chung.NenAnhPanel;
import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TamNenNoiDung;
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
import GUI.TaiKhoan.DangNhapGUI;
import GUI.ThongKe.ThongKeGUI;
import GUI.ThuCung.DonThuocGUI;
import GUI.ThuCung.HoSoThuCungGUI;
import GUI.ThuCung.LichSuKhamGUI;

import GUI.TaiKhoan.DangNhapGUI;
import GUI.TaiKhoan.QuanLyTaiKhoanGUI;
import controller.NavigationController;

// Các GUI thuộc tab Vật tư
import GUI.VatTu.VatTuGUI;

import GUI.VatTu.NhaCungCapGUI;
import GUI.VatTu.PhieuNhapKhoGUI;
import GUI.VatTu.PhieuXuatKhoGUI;
import GUI.VatTu.VatTuGUI;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class GiaoDienChinhGUI extends NenAnhPanel {
    private static final NavigationController dieuHuongController = new NavigationController();

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
        this(
                dieuHuongController.isDaDangNhap() ? dieuHuongController.getTenNguoiDungHienTai() : "Lê Văn Thành",
                dieuHuongController.isDaDangNhap() ? dieuHuongController.getVaiTroNguoiDungHienTai() : "Quản lý");
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
        if (coAnhNen()) {
            // Header trong suốt, phía sau là ảnh nền đã làm mờ (kính mờ)
            LamMoNen.apDungChoHeader(headerGUI);
            setThanhMo(headerGUI);
        }
        add(headerGUI, BorderLayout.NORTH);

        // 2. NHÚNG PHẦN MAIN (NỘI DUNG) VÀO CENTER
        if (coAnhNen()) {
            // Tấm nền kem bán trong suốt + lề để ảnh nền hiện quanh mép
            panelNoiDung = new TamNenNoiDung(new BorderLayout());
        } else {
            panelNoiDung = new JPanel(new BorderLayout());
            panelNoiDung.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        }
        panelNoiDung.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);
        panelNoiDung.setPreferredSize(new Dimension(RangBuocGiaoDien.MAN_HINH_RONG_MAC_DINH, RangBuocGiaoDien.MAN_HINH_CAO_MAC_DINH - 100));
        add(panelNoiDung, BorderLayout.CENTER);

        // 3. LẮNG NGHE SỰ KIỆN CHUYỂN TAB TỪ HEADER
        headerGUI.setTabSelectionListener((tenTab, menuCon) -> {
            if (menuCon != null && !menuCon.isEmpty()) {
                xuLyChonMenuCon(tenTab, menuCon);
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
                if (coAnhNen()) {
                    scrollPane.setOpaque(false);
                    scrollPane.getViewport().setOpaque(false);
                    scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
                }
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
                panelNoiDung.add(scrollPane, BorderLayout.CENTER);
            } else {
                panelNoiDung.add(comp, BorderLayout.CENTER);
            }
        } else {
            hienThiNoiDungMacDinh("Đang xem tab: " + tabHienTai);
        }
        // Làm mờ nền các màn hình con để ảnh nền hiện lên
        if (comp != null && coAnhNen()) {
            LamMoNen.apDung(comp);
        }
        panelNoiDung.revalidate();
        panelNoiDung.repaint();
    }
    
    /** Đã nạp được ảnh nền cho vùng nội dung hay chưa. */
    private boolean coAnhNen() {
        return coAnh();
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
        vungMacDinh.setOpaque(!coAnhNen());
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

        String[] menuCons = (headerGUI != null) ? headerGUI.getMenuConCuaTab(tenTab) : null;
        if (menuCons != null && menuCons.length > 0) {
            xuLyChonMenuCon(tenTab, menuCons[0]);
            return;
        }

        hienThiNoiDungMacDinh("Đang xem tab: " + tenTab);
        hienThiManHinh(dieuHuongController.layManHinhChoTab(tenTab));
    }

    private void xuLyChonMenuCon(String tenTab, String tenMenuCon) {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem: " + tenTab + " ➔ " + tenMenuCon);

        hienThiManHinh(dieuHuongController.layManHinhChoMenuCon(tenMenuCon));
    }

    private void hienThiManHinh(NavigationController.ManHinh manHinh) {
        switch (manHinh) {
            case NHAN_VIEN: hienThiDanhSachNhanVien(); break;
            case BAC_SI: hienThiDanhSachBacSi(); break;
            case THONG_KE: hienThiThongKe(); break;
            case QUAN_LY_TAI_KHOAN: hienThiQuanLyTaiKhoan(); break;
            case HO_SO_THU_CUNG: hienThiHoSoThuCung(); break;
            case DON_THUOC: hienThiDonThuoc(); break;
            case LICH_SU_KHAM: hienThiLichSuKham(); break;
            case KHACH_HANG: hienThiDanhSachKhachHang(); break;
            case HANG_THANH_VIEN: hienThiTheThanhVien(); break;
            case TAO_HOA_DON:
                try {
                    hienThiTaoHoaDonMoi();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(
                            this, "Không thể mở màn hình lập hóa đơn: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case DANH_SACH_HOA_DON: hienThiDanhSachHoaDon(); break;
            case VAT_TU: hienThiVatTu(); break;
            case NHA_CUNG_CAP: hienThiNhaCungCap(); break;
            case PHIEU_NHAP_KHO: hienThiPhieuNhapKho(); break;
            case PHIEU_XUAT_KHO: hienThiPhieuXuatKho(); break;
            case DICH_VU: hienThiBangGiaDichVu(); break;
            case KHUYEN_MAI: hienThiKhuyenMai(); break;
            case THUE: hienThiThue(); break;
            case LICH_HEN: hienThiXemLich(); break;
            case DAT_LICH: hienThiDatLichMoi(); break;
            case LICH_LAM_VIEC: hienThiLichLamViec(); break;
            case MAC_DINH: default: break;
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
        setNoiDung(new DanhSachKhachHangGUI());
    }
    public void hienThiTheThanhVien() { hienThiQuanLyHangThanhVien(); }
    public void hienThiQuanLyHangThanhVien() {
        setNoiDung(new QuanLyHangThanhVienGUI());
    }

    public void hienThiTaoHoaDonMoi() throws SQLException { setNoiDung(new LapHoaDon_gui()); }
    public void hienThiDanhSachHoaDon() {
        setNoiDung(new DSHoaDon_gui(() -> {
            try {
                hienThiTaoHoaDonMoi();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        this, "Không thể mở màn hình lập hóa đơn: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
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
        dieuHuongController.dangXuat();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // 1. Khởi tạo kết nối CSDL SALALA
            try {
                connectDB.ConnectDB.getInstance().connect();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Không thể kết nối cơ sở dữ liệu SALALA: " + e.getMessage(),
                        "Lỗi kết nối",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Chạy ứng dụng: Gọi giao diện Đăng Nhập đầu tiên
            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}