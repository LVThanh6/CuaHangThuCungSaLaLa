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

    // --- CÁC THÀNH PHẦN GIAO DIỆN CHÍNH ---
    private HeaderGUI headerGUI; // Phần 1: Thanh Header
    private JPanel panelNoiDung; // Phần 2: Vùng Main chứa nội dung tab bên trong
    private JLabel nhanNoiDung;
    private String tabHienTai;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public GiaoDienChinhGUI() {
        this("Lê Văn Thành", "Quản lý");
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
     * Mặc định component sẽ co giãn 100% diện tích của tab (phù hợp khi mở full màn
     * hình hoặc co về kích thước tối thiểu).
     * 
     * @param comp Giao diện cần hiển thị
     */
    public void setNoiDung(Component comp) {
        setNoiDung(comp, false);
    }

    /**
     * Nhúng một Component vào vùng nội dung chính với tùy chọn cuộn trang tự động.
     * 
     * @param comp      Giao diện cần hiển thị
     * @param cuonTrang Bật thanh cuộn nếu giao diện bên trong có kích thước cố định
     *                  lớn hơn màn hình
     */
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

    /**
     * Hiển thị thông báo giữ chỗ mặc định khi tab chưa nhúng giao diện cụ thể.
     */
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
            case "Loại thú cưng":
                hienThiLoaiThuCung();
                break;
            case "Lịch sử khám":
                hienThiLichSuKham();
                break;

            // Tab Khách hàng
            case "Danh sách khách hàng":
                hienThiDanhSachKhachHang();
                break;
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

            // Tab Vật tư
            case "Kho thuốc":
                hienThiKhoThuoc();
                break;
            case "Dụng cụ y tế":
                hienThiDungCuYTe();
                break;

            // Tab Lịch hẹn
            case "Lịch hôm nay":
                hienThiLichHomNay();
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
    // CÁC HÀM RỖNG ĐỂ BẠN NHÚNG GIAO DIỆN (PANEL / VIEW) CHO TỪNG TAB
    // Bạn chỉ cần gọi: setNoiDung(new TenClassGUI());
    // =========================================================================

    /** Tab 1: Nhân viên */
    public void hienThiTabNhanVien() {
        // TODO: Nhúng class giao diện Nhân viên vào đây
        // Ví dụ: setNoiDung(new NhanVienGUI());
    }

    /** Tab 2: Thú cưng */
    public void hienThiTabThuCung() {
        // TODO: Nhúng class giao diện Thú cưng vào đây
        // Ví dụ: setNoiDung(new ThuCungGUI());
    }

    /** Tab 3: Khách hàng */
    public void hienThiTabKhachHang() {
        // TODO: Nhúng class giao diện Khách hàng vào đây
        // Ví dụ: setNoiDung(new KhachHangGUI());
    }

    /** Tab 4: Hóa đơn */
    public void hienThiTabHoaDon() {
        // TODO: Nhúng class giao diện Hóa đơn vào đây
        // Ví dụ: setNoiDung(new HoaDonGUI());
    }

    /** Tab 5: Vật tư */
    public void hienThiTabVatTu() {
        // TODO: Nhúng class giao diện Vật tư vào đây
        // Ví dụ: setNoiDung(new VatTuGUI());
    }

    /** Tab 6: Lịch hẹn */
    public void hienThiTabLichHen() {
        // TODO: Nhúng class giao diện Lịch hẹn vào đây
        // Ví dụ: setNoiDung(new LichHenGUI());
    }

    /** Tab 7: Dịch vụ */
    public void hienThiTabDichVu() {
        // TODO: Nhúng class giao diện Dịch vụ vào đây
        // Ví dụ: setNoiDung(new DichVuGUI());
    }

    // =========================================================================
    // CÁC HÀM RỖNG CHO MENU CON (SUB-MENU) CỦA TỪNG TAB
    // =========================================================================

    // --- Tab Nhân viên ---
    public void hienThiDanhSachNhanVien() {
        // TODO: Nhúng class giao diện Danh sách nhân viên vào đây
    }

    public void hienThiDanhSachBacSi() {
        // TODO: Nhúng class giao diện Danh sách bác sĩ vào đây
    }

    public void hienThiThongKe() {
        // TODO: Nhúng class ThongKeGUI vào đây
        // Ví dụ (khi ThongKeGUI kế thừa JPanel/Component):
        // setNoiDung(new ThongKeGUI());
    }

    // --- Tab Thú cưng ---
    public void hienThiHoSoThuCung() {
        // TODO: Nhúng class giao diện Hồ sơ thú cưng vào đây
    }

    public void hienThiLoaiThuCung() {
        // TODO: Nhúng class giao diện Loại thú cưng vào đây
    }

    public void hienThiLichSuKham() {
        // TODO: Nhúng class giao diện Lịch sử khám vào đây
    }

    // --- Tab Khách hàng ---
    public void hienThiDanhSachKhachHang() {
        // TODO: Nhúng class giao diện Danh sách khách hàng vào đây
    }

    public void hienThiTheThanhVien() {
        // TODO: Nhúng class giao diện Thẻ thành viên vào đây
    }

    // --- Tab Hóa đơn ---
    public void hienThiTaoHoaDonMoi() {
        // TODO: Nhúng class giao diện Tạo hóa đơn mới vào đây
    }

    public void hienThiDanhSachHoaDon() {
        // TODO: Nhúng class giao diện Danh sách hóa đơn vào đây
    }

    // --- Tab Vật tư ---
    public void hienThiKhoThuoc() {
        // TODO: Nhúng class giao diện Kho thuốc vào đây
    }

    public void hienThiDungCuYTe() {
        // TODO: Nhúng class giao diện Dụng cụ y tế vào đây
    }

    // --- Tab Lịch hẹn ---
    public void hienThiLichHomNay() {
        // TODO: Nhúng class giao diện Lịch hôm nay vào đây
    }

    public void hienThiDatLichMoi() {
        // TODO: Nhúng class giao diện Đặt lịch mới vào đây
    }

    // --- Tab Dịch vụ ---
    public void hienThiBangGiaDichVu() {
        // TODO: Nhúng class giao diện Bảng giá dịch vụ vào đây
    }

    public void hienThiGoiChamSoc() {
        // TODO: Nhúng class giao diện Gói chăm sóc vào đây
    }

    // ==========================================
    // TIỆN ÍCH CÀI ĐẶT CỬA SỔ CHUẨN
    // ==========================================
    /**
     * Cài đặt kích thước chuẩn cho JFrame chứa GiaoDienChinhGUI
     */
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

    // ==========================================
    // MAIN KHỞI ĐỘNG CHƯƠNG TRÌNH
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ thống Quản lý SALALA Pet Shop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Khởi tạo giao diện chính với thông tin người dùng và vai trò
            GiaoDienChinhGUI giaoDienChinh = new GiaoDienChinhGUI("Lê Văn Thành", "Quản lý");
            frame.setContentPane(giaoDienChinh);

            // Cấu hình kích thước chuẩn (có thể mở full màn hình hoặc co về kích thước tối
            // thiểu)
            caiDatKichThuocCuaSo(frame, false);

            frame.setVisible(true);
        });
    }
}
