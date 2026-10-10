package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 * Class HeaderGUI: Thanh tiêu đề và điều hướng của SALALA Pet Shop.
 * Đọc thông tin người dùng và hiển thị thanh header, phân quyền danh sách tab theo vai trò.
 */
public class HeaderGUI extends JPanel {

    public interface TabSelectionListener {
        void onTabSelected(String tenTab, String menuCon);
    }

    // --- Thông tin người dùng ---
    private String tenNguoiDung;
    private String chucVu;
    private String chuCaiAvatar;

    // --- Cấu trúc Tab & Menu con ---
    private String[] danhSachTabs;
    private Map<String, String[]> menuConMap;
    private String tabHienTai;

    // --- Giao diện ---
    private List<JButton> danhSachNutTab;
    private JPanel panelTabContainer;
    private TabSelectionListener tabSelectionListener;
    private Consumer<String> onTabChanged;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public HeaderGUI() {
        this("Lê Văn Thành", "Quản lý");
    }

    public HeaderGUI(String tenNguoiDung, String chucVu) {
        this.tenNguoiDung = (tenNguoiDung != null && !tenNguoiDung.trim().isEmpty()) ? tenNguoiDung : "Người dùng";
        this.chucVu = (chucVu != null && !chucVu.trim().isEmpty()) ? chucVu : "Quản lý";
        this.danhSachNutTab = new ArrayList<>();
        
        khoiTaoMenuConMacDinh();
        thietLapTheoVaiTro(this.chucVu);
        khoiTaoGiaoDien();
    }

    public HeaderGUI(String tenNguoiDung, String chucVu, TabSelectionListener listener) {
        this(tenNguoiDung, chucVu);
        this.tabSelectionListener = listener;
    }

    // ==========================================
    // KHỞI TẠO MENU CON VÀ PHÂN QUYỀN VAI TRÒ
    // ==========================================
    private void khoiTaoMenuConMacDinh() {
        menuConMap = new HashMap<>();
        menuConMap.put("Nhân viên", new String[]{"Danh sách nhân viên", "Danh sách bác sĩ", "Thống kê"});
        menuConMap.put("Thú cưng", new String[]{"Hồ sơ thú cưng", "Loại thú cưng", "Lịch sử khám"});
        menuConMap.put("Khách hàng", new String[]{"Danh sách khách hàng", "Thẻ thành viên"});
        menuConMap.put("Hóa đơn", new String[]{"Tạo hóa đơn mới", "Danh sách hóa đơn"});
        menuConMap.put("Vật tư", new String[]{"Kho thuốc", "Dụng cụ y tế"});
        menuConMap.put("Lịch hẹn", new String[]{"Lịch hôm nay", "Đặt lịch mới"});
        menuConMap.put("Dịch vụ", new String[]{"Bảng giá dịch vụ", "Gói chăm sóc"});
    }

    /**
     * Tự động điều chỉnh danh sách Tabs hiển thị dựa theo vai trò (chức vụ) người dùng.
     * @param vaiTro Vai trò người dùng (ví dụ: "Quản lý", "Nhân viên", "Bác sĩ")
     */
    public void thietLapTheoVaiTro(String vaiTro) {
        this.chucVu = vaiTro;
        
        // Cắt chữ cái viết tắt đại diện cho Avatar
        capNhatChuCaiAvatar();

        // Phân quyền Tab hiển thị theo vai trò
        if ("Bác sĩ".equalsIgnoreCase(vaiTro) || "Bác sĩ thú y".equalsIgnoreCase(vaiTro)) {
            this.danhSachTabs = new String[]{"Thú cưng", "Khách hàng", "Vật tư", "Lịch hẹn", "Dịch vụ"};
        } else if ("Nhân viên".equalsIgnoreCase(vaiTro) || "Thu ngân".equalsIgnoreCase(vaiTro)) {
            this.danhSachTabs = new String[]{"Thú cưng", "Khách hàng", "Hóa đơn", "Lịch hẹn", "Dịch vụ"};
        } else {
            // Mặc định là Quản lý / Toàn quyền
            this.danhSachTabs = new String[]{"Nhân viên", "Thú cưng", "Khách hàng", "Hóa đơn", "Vật tư", "Lịch hẹn", "Dịch vụ"};
        }

        if (danhSachTabs.length > 0) {
            this.tabHienTai = danhSachTabs[0];
        }
    }

    private void capNhatChuCaiAvatar() {
        String[] parts = tenNguoiDung.trim().split("\\s+");
        if (parts.length >= 2) {
            chuCaiAvatar = (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            chuCaiAvatar = parts[0].substring(0, 1).toUpperCase();
        } else {
            chuCaiAvatar = "SL";
        }
    }

    // ==========================================
    // KHỞI TẠO GIAO DIỆN HEADER
    // ==========================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        add(taoHangThongTinTren(), BorderLayout.NORTH);
        add(taoHangDieuHuongTab(), BorderLayout.CENTER);
    }

    // 1. HÀNG TRÊN: LOGO THƯƠNG HIỆU & THÔNG TIN NGƯỜI DÙNG
    private JPanel taoHangThongTinTren() {
        JPanel hangTren = new JPanel(new BorderLayout());
        hangTren.setBackground(Color.WHITE);
        hangTren.setBorder(new EmptyBorder(12, 24, 12, 24));

        // --- Logo + Tên thương hiệu ---
        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelLogo.setOpaque(false);

        JComponent bieuTuongLogo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                RangBuocGiaoDien.batKhuRangCua(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
                g2.fillRoundRect(0, 0, 32, 32, 10, 10);

                g2.setColor(Color.WHITE);
                g2.fillOval(8, 8, 6, 6);
                g2.fillOval(18, 8, 6, 6);
                g2.fillOval(13, 17, 7, 7);
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(32, 32); }
        };

        JLabel lblTenThuongHieu = new JLabel("SALALA Pet Shop");
        lblTenThuongHieu.setFont(RangBuocGiaoDien.fontDam(18));
        lblTenThuongHieu.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        panelLogo.add(bieuTuongLogo);
        panelLogo.add(lblTenThuongHieu);
        hangTren.add(panelLogo, BorderLayout.WEST);

        // --- Khối thông tin người dùng bên phải ---
        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panelPhai.setOpaque(false);

        JPanel khoiNguoiDung = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        khoiNguoiDung.setOpaque(false);
        khoiNguoiDung.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JComponent avatarNguoiDung = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                RangBuocGiaoDien.batKhuRangCua(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(220, 244, 238));
                g2.fillOval(0, 0, 36, 36);

                g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
                g2.setFont(RangBuocGiaoDien.fontDam(13));
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(chuCaiAvatar);
                int textH = fm.getAscent();
                g2.drawString(chuCaiAvatar, (36 - textW) / 2, (36 + textH) / 2 - 2);
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(36, 36); }
        };

        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTen = new JLabel(tenNguoiDung + " ▾");
        lblTen.setFont(RangBuocGiaoDien.fontDam(13));
        lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblChucVu = new JLabel(chucVu);
        lblChucVu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblChucVu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(2));
        thongTin.add(lblChucVu);

        khoiNguoiDung.add(avatarNguoiDung);
        khoiNguoiDung.add(thongTin);
        panelPhai.add(khoiNguoiDung);

        hangTren.add(panelPhai, BorderLayout.EAST);
        return hangTren;
    }

    // 2. HÀNG ĐIỀU HƯỚNG TAB
    private JPanel taoHangDieuHuongTab() {
        panelTabContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelTabContainer.setBackground(Color.WHITE);
        panelTabContainer.setBorder(new EmptyBorder(0, 20, 8, 20));
        veLaiCacNutTab();
        return panelTabContainer;
    }

    private void veLaiCacNutTab() {
        if (panelTabContainer == null) return;
        panelTabContainer.removeAll();
        danhSachNutTab.clear();

        for (String tenTab : danhSachTabs) {
            JButton nut = new JButton(tenTab + " ▾") {
                @Override
                protected void paintComponent(Graphics g) {
                    RangBuocGiaoDien.batKhuRangCua(g);
                    Graphics2D g2 = (Graphics2D) g;
                    int w = getWidth(), h = getHeight();
                    boolean dangChon = tenTab.equals(tabHienTai);

                    if (dangChon) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
                        g2.fillRoundRect(0, 0, w, h, 8, 8);
                        g2.setColor(RangBuocGiaoDien.MAU_VIEN_CHU_DAO);
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(241, 245, 249));
                        g2.fillRoundRect(0, 0, w, h, 8, 8);
                    }
                    super.paintComponent(g);
                }
            };

            nut.setFont(RangBuocGiaoDien.fontVua(13));
            nut.setForeground(tenTab.equals(tabHienTai) ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_CHINH);
            nut.setFocusPainted(false);
            nut.setBorderPainted(false);
            nut.setContentAreaFilled(false);
            nut.setOpaque(false);
            nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nut.setMargin(new Insets(6, 14, 6, 14));

            // Popup menu khi rê chuột
            JPopupMenu popupMenu = taoPopupMenu(tenTab);
            nut.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (popupMenu != null && popupMenu.getSubElements().length > 0) {
                        popupMenu.show(nut, 0, nut.getHeight());
                    }
                }
            });

            // Sự kiện Click chọn tab
            nut.addActionListener(e -> setTabHienTai(tenTab));

            danhSachNutTab.add(nut);
            panelTabContainer.add(nut);
        }
        panelTabContainer.revalidate();
        panelTabContainer.repaint();
    }

    // Popup Menu sổ xuống cho từng tab
    private JPopupMenu taoPopupMenu(String tenTab) {
        String[] cacCon = menuConMap.get(tenTab);
        if (cacCon == null || cacCon.length == 0) return null;

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));

        for (String con : cacCon) {
            JMenuItem item = new JMenuItem(con);
            item.setFont(RangBuocGiaoDien.fontThuong(13));
            item.setBackground(Color.WHITE);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            item.addActionListener(e -> {
                chonMenuCon(tenTab, con);
            });
            menu.add(item);
        }
        return menu;
    }

    // ==========================================
    // CHỌN TAB VÀ MENU CON
    // ==========================================
    public void setTabHienTai(String tenTab) {
        this.tabHienTai = tenTab;
        capNhatMauNutTab();

        if (tabSelectionListener != null) {
            tabSelectionListener.onTabSelected(tenTab, null);
        }
        if (onTabChanged != null) {
            onTabChanged.accept(tenTab);
        }
    }

    public void chonMenuCon(String tenTab, String tenMenuCon) {
        this.tabHienTai = tenTab;
        capNhatMauNutTab();

        if (tabSelectionListener != null) {
            tabSelectionListener.onTabSelected(tenTab, tenMenuCon);
        }
        if (onTabChanged != null) {
            onTabChanged.accept(tenTab);
        }
    }

    private void capNhatMauNutTab() {
        for (JButton nut : danhSachNutTab) {
            boolean dangChon = nut.getText().startsWith(tabHienTai);
            nut.setForeground(dangChon ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_CHINH);
            nut.repaint();
        }
    }

    // ==========================================
    // CẬP NHẬT THÔNG TIN NGƯỜI DÙNG & VAI TRÒ
    // ==========================================
    /**
     * Cập nhật thông tin người dùng và tự động làm mới giao diện Header theo vai trò mới
     */
    public void setThongTinNguoiDung(String ten, String chucVu) {
        this.tenNguoiDung = ten;
        thietLapTheoVaiTro(chucVu);

        removeAll();
        khoiTaoGiaoDien();
        revalidate();
        repaint();
    }

    public void setThongTinNguoiDung(String ten, String chucVu, String avatarText) {
        this.tenNguoiDung = ten;
        this.chucVu = chucVu;
        this.chuCaiAvatar = avatarText;

        removeAll();
        khoiTaoGiaoDien();
        revalidate();
        repaint();
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
        capNhatChuCaiAvatar();
        repaint();
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        setThongTinNguoiDung(this.tenNguoiDung, chucVu);
    }

    public String getChuCaiAvatar() {
        return chuCaiAvatar;
    }

    public void setChuCaiAvatar(String chuCaiAvatar) {
        this.chuCaiAvatar = chuCaiAvatar;
        repaint();
    }

    public String[] getDanhSachTabs() {
        return danhSachTabs;
    }

    public void setDanhSachTabs(String[] danhSachTabs) {
        this.danhSachTabs = danhSachTabs;
        veLaiCacNutTab();
    }

    public Map<String, String[]> getMenuConMap() {
        return menuConMap;
    }

    public void setMenuConMap(Map<String, String[]> menuConMap) {
        this.menuConMap = menuConMap;
    }

    public String getTabHienTai() {
        return tabHienTai;
    }

    public List<JButton> getDanhSachNutTab() {
        return danhSachNutTab;
    }

    public TabSelectionListener getTabSelectionListener() {
        return tabSelectionListener;
    }

    public void setTabSelectionListener(TabSelectionListener tabSelectionListener) {
        this.tabSelectionListener = tabSelectionListener;
    }

    public Consumer<String> getOnTabChanged() {
        return onTabChanged;
    }

    public void setOnTabChanged(Consumer<String> onTabChanged) {
        this.onTabChanged = onTabChanged;
    }

    // ==========================================
    // MAIN TEST ĐỘC LẬP CHO HEADER
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử HeaderGUI theo vai trò người dùng");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 300);
            frame.setLocationRelativeTo(null);

            // Test với vai trò Quản lý
            HeaderGUI header = new HeaderGUI("Lê Văn Thành", "Quản lý");
            header.setTabSelectionListener((tab, menuCon) -> {
                System.out.println("Đã chọn: " + tab + (menuCon != null ? " -> " + menuCon : ""));
            });

            frame.setContentPane(header);
            frame.setVisible(true);
        });
    }
}
