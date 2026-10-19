package GUI;

import java.awt.*;
import javax.swing.*;

/**
 * Class GiaoDienChinhGUI: Khung giao diện chính của hệ thống SALALA Pet Shop.
 * Bao gồm 2 phần:
 * 1. Header (HeaderGUI): Hiển thị logo, thông tin và phân quyền tabs theo vai
 * trò người dùng (BorderLayout.NORTH).
 * 2. Main (panelNoiDung): Vùng nội dung chính hiển thị các tab, chừa sẵn các
 * hàm rỗng để nhúng class khác (BorderLayout.CENTER).
 */
public class GiaoDienChinhGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    private HeaderGUI headerGUI;
    private JPanel panelNoiDung;
    private JLabel nhanNoiDung;
    private String tabHienTai;
    private model.TaiKhoan taiKhoan;

    public GiaoDienChinhGUI() {
        this(null, "Lê Văn Thành", "Quản lý");
    }

    public GiaoDienChinhGUI(String tenNguoiDung, String chucVu) {
        this(null, tenNguoiDung, chucVu);
    }

    public GiaoDienChinhGUI(model.TaiKhoan taiKhoan, String tenNguoiDung, String chucVu) {
        this.taiKhoan = taiKhoan;
        khoiTaoGiaoDien(tenNguoiDung, chucVu);
    }

    private void khoiTaoGiaoDien(String tenNguoiDung, String chucVu) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
        setPreferredSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);

        headerGUI = new HeaderGUI(taiKhoan, tenNguoiDung, chucVu);
        headerGUI.setLogoutListener(() -> dangXuat());
        add(headerGUI, BorderLayout.NORTH);

        panelNoiDung = new JPanel(new BorderLayout());
        panelNoiDung.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        panelNoiDung.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);
        panelNoiDung.setPreferredSize(
                new Dimension(RangBuocGiaoDien.MAN_HINH_RONG_MAC_DINH, RangBuocGiaoDien.MAN_HINH_CAO_MAC_DINH - 100));
        add(panelNoiDung, BorderLayout.CENTER);

        headerGUI.setTabSelectionListener((tenTab, menuCon) -> {
            if (menuCon != null && !menuCon.isEmpty()) {
                xuLyChonMenuCon(tenTab, menuCon);
            } else {
                xuLyChonTab(tenTab);
            }
        });

        try {
            if (headerGUI.getTabHienTai() != null) {
                xuLyChonTab(headerGUI.getTabHienTai());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải tab ban đầu: " + e.getMessage());
            e.printStackTrace();
            hienThiNoiDungMacDinh("Chào mừng đến với Hệ thống Quản lý SALALA Pet Shop");
        }
    }

    public void setNoiDung(Component comp) {
        setNoiDung(comp, false);
    }

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

    public void setNoiDung(Object view) {
        if (view instanceof Component) {
            setNoiDung((Component) view);
        }
    }

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

    private void xuLyChonTab(String tenTab) {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem tab: " + tenTab);

        try {
            switch (tenTab) {
                case "Nhân viên":
                    hienThiTabNhanVien();
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
        } catch (Exception e) {
            System.err.println("Lỗi tải tab " + tenTab + ": " + e.getMessage());
            e.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi khi mở tab " + tenTab + ": " + e.getMessage());
        }
    }

    private void xuLyChonMenuCon(String tenTab, String tenMenuCon) {
        this.tabHienTai = tenTab;
        hienThiNoiDungMacDinh("Đang xem: " + tenTab + " ➔ " + tenMenuCon);

        try {
            switch (tenMenuCon) {
                case "Danh sách nhân viên":
                    hienThiDanhSachNhanVien();
                    break;
                case "Quản lý tài khoản":
                case "Tài khoản":
                    hienThiQuanLyTaiKhoan();
                    break;
                case "Danh sách bác sĩ":
                    hienThiDanhSachBacSi();
                    break;
                case "Thống kê":
                    hienThiThongKe();
                    break;
                case "Hồ sơ thú cưng":
                    hienThiHoSoThuCung();
                    break;
                case "Loại thú cưng":
                    hienThiLoaiThuCung();
                    break;
                case "Lịch sử khám":
                    hienThiLichSuKham();
                    break;
                case "Danh sách khách hàng":
                    hienThiDanhSachKhachHang();
                    break;
                case "Quản lý hạng thành viên":
                case "Quản lý thẻ thành viên":
                case "Thẻ thành viên":
                    hienThiTheThanhVien();
                    break;
                case "Tạo hóa đơn mới":
                    hienThiTaoHoaDonMoi();
                    break;
                case "Danh sách hóa đơn":
                    hienThiDanhSachHoaDon();
                    break;
                case "Kho thuốc":
                    hienThiKhoThuoc();
                    break;
                case "Dụng cụ y tế":
                    hienThiDungCuYTe();
                    break;
                case "Lịch hôm nay":
                    hienThiLichHomNay();
                    break;
                case "Đặt lịch mới":
                    hienThiDatLichMoi();
                    break;
                case "Bảng giá dịch vụ":
                    hienThiBangGiaDichVu();
                    break;
                case "Gói chăm sóc":
                    hienThiGoiChamSoc();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải menu con " + tenMenuCon + ": " + e.getMessage());
            e.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi khi mở: " + tenMenuCon + " (" + e.getMessage() + ")");
        }
    }

    public void hienThiTabNhanVien() {
        setNoiDung(new QuanLyNhanVienGUI());
    }

    public void hienThiTabThuCung() {
        hienThiNoiDungMacDinh("Tab Thú cưng chưa được nhúng giao diện cụ thể.");
    }

    public void hienThiTabKhachHang() {
        try {
            setNoiDung(new DanhSachKhachHangGUI());
        } catch (Throwable t) {
            t.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi hiển thị Khách hàng: " + t.getMessage());
        }
    }

    public void hienThiTabHoaDon() {
        setNoiDung(new DSHoaDon_gui());
    }

    public void hienThiTabVatTu() {
        hienThiNoiDungMacDinh("Tab Vật tư chưa được nhúng giao diện cụ thể.");
    }

    public void hienThiTabLichHen() {
        hienThiNoiDungMacDinh("Tab Lịch hẹn chưa được nhúng giao diện cụ thể.");
    }

    public void hienThiTabDichVu() {
        hienThiNoiDungMacDinh("Tab Dịch vụ chưa được nhúng giao diện cụ thể.");
    }

    public void hienThiDanhSachNhanVien() {
        setNoiDung(new QuanLyNhanVienGUI());
    }

    public void hienThiQuanLyTaiKhoan() {
        setNoiDung(new QuanLyTaiKhoanGUI());
    }

    public void hienThiDanhSachBacSi() {
        setNoiDung(QuanLyTable.taoBangBacSi());
    }

    public void hienThiThongKe() {
        setNoiDung(new ThongKeGUI());
    }

    public void hienThiHoSoThuCung() {
        hienThiNoiDungMacDinh("Hồ sơ thú cưng chưa được triển khai.");
    }

    public void hienThiLoaiThuCung() {
        hienThiNoiDungMacDinh("Loại thú cưng chưa được triển khai.");
    }

    public void hienThiLichSuKham() {
        hienThiNoiDungMacDinh("Lịch sử khám chưa được triển khai.");
    }

    public void hienThiDanhSachKhachHang() {
        try {
            setNoiDung(new DanhSachKhachHangGUI());
        } catch (Throwable t) {
            t.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi hiển thị Khách hàng: " + t.getMessage());
        }
    }

    public void hienThiTheThanhVien() {
        hienThiQuanLyHangThanhVien();
    }

    public void hienThiQuanLyHangThanhVien() {
        try {
            setNoiDung(new QuanLyHangThanhVienGUI());
        } catch (Throwable t) {
            t.printStackTrace();
            hienThiNoiDungMacDinh("Lỗi hiển thị Hạng thành viên: " + t.getMessage());
        }
    }

    public void hienThiTaoHoaDonMoi() {
        setNoiDung(new LapHoaDon_gui());
    }

    public void hienThiDanhSachHoaDon() {
        setNoiDung(new DSHoaDon_gui());
    }

    public void hienThiKhoThuoc() {
        hienThiNoiDungMacDinh("Kho thuốc chưa được triển khai.");
    }

    public void hienThiDungCuYTe() {
        hienThiNoiDungMacDinh("Dụng cụ y tế chưa được triển khai.");
    }

    public void hienThiLichHomNay() {
        hienThiNoiDungMacDinh("Lịch hôm nay chưa được triển khai.");
    }

    public void hienThiDatLichMoi() {
        hienThiNoiDungMacDinh("Đặt lịch mới chưa được triển khai.");
    }

    public void hienThiBangGiaDichVu() {
        hienThiNoiDungMacDinh("Bảng giá dịch vụ chưa được triển khai.");
    }

    public void hienThiGoiChamSoc() {
        hienThiNoiDungMacDinh("Gói chăm sóc chưa được triển khai.");
    }

    public static void caiDatKichThuocCuaSo(JFrame frame, boolean moFullManHinh) {
        frame.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
        frame.setPreferredSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);
        frame.setSize(RangBuocGiaoDien.KICH_THUOC_MAC_DINH);
        frame.setLocationRelativeTo(null);
        if (moFullManHinh) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public HeaderGUI getHeaderGUI() {
        return headerGUI;
    }

    public void setHeaderGUI(HeaderGUI headerGUI) {
        this.headerGUI = headerGUI;
    }

    public JPanel getPanelNoiDung() {
        return panelNoiDung;
    }

    public void setPanelNoiDung(JPanel panelNoiDung) {
        this.panelNoiDung = panelNoiDung;
    }

    public String getTabHienTai() {
        return tabHienTai;
    }

    public void setTabHienTai(String tenTab) {
        if (headerGUI != null) {
            headerGUI.setTabHienTai(tenTab);
        } else {
            xuLyChonTab(tenTab);
        }
    }

    public JLabel getNhanNoiDung() {
        return nhanNoiDung;
    }

    public void setNhanNoiDung(JLabel nhanNoiDung) {
        this.nhanNoiDung = nhanNoiDung;
    }

    public model.TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(model.TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        if (headerGUI != null) {
            headerGUI.setTaiKhoan(taiKhoan);
        }
    }

    public void dangXuat() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.dispose();
        }
        SwingUtilities.invokeLater(() -> new DangNhapGUI().setVisible(true));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {
                connectDB.ConnectDB.getInstance().connect();
            } catch (Exception ignored) {
            }

            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}

