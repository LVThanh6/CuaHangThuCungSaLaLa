package GUI.Chung;

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

import dao.DangNhap_DAO;
import GUI.GiaoDienChinhGUI;
import GUI.TaiKhoan.ThongTinTaiKhoanDialog;

public class HeaderGUI extends JPanel {

    public interface TabSelectionListener {
        void onTabSelected(String tenTab, String menuCon);
    }

    public enum VaiTroHeThong {
        ADMIN("Admin (Toàn quyền)"),
        QUAN_LY("Quản lý"),
        BAC_SI("Bác sĩ thú y"),
        NHAN_VIEN_BAN_HANG("Nhân viên bán hàng"),
        NHAN_VIEN_DICH_VU("Nhân viên dịch vụ"),
        NHAN_VIEN_KHO("Nhân viên quản lý kho");

        private final String tenHienThi;

        VaiTroHeThong(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    private String tenNguoiDung;
    private String chucVu;
    private String chuCaiAvatar;
    private VaiTroHeThong vaiTroHeThong;

    private String[] danhSachTabs;
    private Map<String, String[]> menuConMap;
    private String tabHienTai;

    private List<JButton> danhSachNutTab;
    private JPanel panelTabContainer;
    private TabSelectionListener tabSelectionListener;
    private Consumer<String> onTabChanged;

    public HeaderGUI() {
        this(DangNhap_DAO.isDaDangNhap() ? DangNhap_DAO.getTenNguoiDung() : "Người dùng",
             DangNhap_DAO.isDaDangNhap() ? DangNhap_DAO.getVaiTro() : "Quản lý");
    }

    public HeaderGUI(String tenNguoiDung, String chucVu) {
        this.tenNguoiDung = (tenNguoiDung != null && !tenNguoiDung.trim().isEmpty()) ? tenNguoiDung : "Người dùng";
        this.chucVu = (chucVu != null && !chucVu.trim().isEmpty()) ? chucVu : "Quản lý";
        this.danhSachNutTab = new ArrayList<>();

        thietLapTheoVaiTro(this.chucVu);
        khoiTaoGiaoDien();
    }

    public HeaderGUI(String tenNguoiDung, String chucVu, TabSelectionListener listener) {
        this(tenNguoiDung, chucVu);
        this.tabSelectionListener = listener;
    }

    /**
     * Nhận diện và chuẩn hóa vai trò thành 1 trong 6 vai trò người dùng trong hệ thống
     */
    public static VaiTroHeThong xacDinhVaiTro(String vaiTro) {
        if (DangNhap_DAO.isDaDangNhap()) {
            String username = DangNhap_DAO.getTaiKhoanHienTai() != null ? DangNhap_DAO.getTaiKhoanHienTai().getTenDangNhap() : "";
            if ("admin".equalsIgnoreCase(username)) {
                return VaiTroHeThong.ADMIN;
            }
        }
        if (vaiTro == null || vaiTro.trim().isEmpty()) {
            return VaiTroHeThong.QUAN_LY;
        }
        String vt = vaiTro.trim().toLowerCase();
        if (vt.contains("admin") || vt.contains("quản trị") || vt.contains("quantri")) {
            return VaiTroHeThong.ADMIN;
        }
        if (vt.contains("bác sĩ") || vt.contains("bác sỹ") || vt.contains("bac si") || vt.contains("bacsi")) {
            return VaiTroHeThong.BAC_SI;
        }
        if (vt.contains("bán hàng") || vt.contains("ban hang") || vt.contains("thu ngân") || vt.contains("thungan") || vt.contains("banhang")) {
            return VaiTroHeThong.NHAN_VIEN_BAN_HANG;
        }
        if (vt.contains("dịch vụ") || vt.contains("dich vu") || vt.contains("dichvu")) {
            return VaiTroHeThong.NHAN_VIEN_DICH_VU;
        }
        if (vt.contains("kho")) {
            return VaiTroHeThong.NHAN_VIEN_KHO;
        }
        if (vt.contains("quản lý") || vt.contains("quan ly") || vt.contains("quanly") || vt.contains("ql")) {
            return VaiTroHeThong.QUAN_LY;
        }
        return VaiTroHeThong.QUAN_LY;
    }

    /**
     * Phân quyền Tabs và Menu con cho 6 vai trò người dùng
     */
    public void thietLapTheoVaiTro(String vaiTro) {
        this.chucVu = vaiTro;
        capNhatChuCaiAvatar();
        this.vaiTroHeThong = xacDinhVaiTro(vaiTro);
        menuConMap = new HashMap<>();

        switch (this.vaiTroHeThong) {
            case ADMIN:
                // 1. ADMIN: Toàn quyền tất cả chức năng hệ thống
                this.danhSachTabs = new String[] { "Nhân viên", "Bác sĩ", "Thú cưng", "Khách hàng", "Hóa đơn", "Vật tư", "Lịch", "Dịch vụ" };
                menuConMap.put("Nhân viên", new String[] { "Danh sách nhân viên", "Thống kê", "Quản lý tài khoản" });
                menuConMap.put("Bác sĩ", new String[] { "Danh sách bác sĩ" });
                menuConMap.put("Thú cưng", new String[] { "Hồ sơ thú cưng", "Đơn Thuốc", "Lịch sử khám" });
                menuConMap.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                menuConMap.put("Hóa đơn", new String[] { "Tạo hóa đơn mới", "Danh sách hóa đơn" });
                menuConMap.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp", "Phiếu nhập kho", "Phiếu xuất kho" });
                menuConMap.put("Lịch", new String[] { "Lịch hẹn (Khám)", "Đặt lịch mới", "Lịch làm việc" });
                menuConMap.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                break;

            case QUAN_LY:
                // 2. QUẢN LÝ: Các chức năng liên quan đến quản lý điều hành, nhân sự, dịch vụ, khách hàng, lịch làm việc, thống kê
                this.danhSachTabs = new String[] { "Nhân viên", "Bác sĩ", "Khách hàng", "Lịch", "Dịch vụ", "Hóa đơn", "Vật tư" };
                menuConMap.put("Nhân viên", new String[] { "Danh sách nhân viên", "Thống kê" });
                menuConMap.put("Bác sĩ", new String[] { "Danh sách bác sĩ" });
                menuConMap.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                menuConMap.put("Lịch", new String[] { "Lịch làm việc", "Lịch hẹn (Khám)" });
                menuConMap.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                menuConMap.put("Hóa đơn", new String[] { "Danh sách hóa đơn" });
                menuConMap.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp" });
                break;

            case BAC_SI:
                // 3. BÁC SĨ: Các chức năng liên quan đến khám chữa bệnh và kê thuốc
                this.danhSachTabs = new String[] { "Thú cưng", "Lịch", "Khách hàng" };
                menuConMap.put("Thú cưng", new String[] { "Hồ sơ thú cưng", "Đơn Thuốc", "Lịch sử khám" });
                menuConMap.put("Lịch", new String[] { "Lịch hẹn (Khám)", "Lịch làm việc" });
                menuConMap.put("Khách hàng", new String[] { "Danh sách khách hàng" });
                break;

            case NHAN_VIEN_BAN_HANG:
                // 4. NHÂN VIÊN BÁN HÀNG: Liên quan đến vật tư, đơn thuốc, hóa đơn cùng dịch vụ
                this.danhSachTabs = new String[] { "Hóa đơn", "Thú cưng", "Vật tư", "Dịch vụ", "Khách hàng" };
                menuConMap.put("Hóa đơn", new String[] { "Tạo hóa đơn mới", "Danh sách hóa đơn" });
                menuConMap.put("Thú cưng", new String[] { "Đơn Thuốc" });
                menuConMap.put("Vật tư", new String[] { "Vật tư" });
                menuConMap.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                menuConMap.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                break;

            case NHAN_VIEN_DICH_VU:
                // 5. NHÂN VIÊN DỊCH VỤ: Liên quan đến lịch hẹn của dịch vụ và chức năng các gói dịch vụ
                this.danhSachTabs = new String[] { "Lịch", "Dịch vụ", "Thú cưng", "Khách hàng" };
                menuConMap.put("Lịch", new String[] { "Đặt lịch mới", "Lịch hẹn (Khám)", "Lịch làm việc" });
                menuConMap.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi" });
                menuConMap.put("Thú cưng", new String[] { "Hồ sơ thú cưng" });
                menuConMap.put("Khách hàng", new String[] { "Danh sách khách hàng" });
                break;

            case NHAN_VIEN_KHO:
                // 6. NHÂN VIÊN KHO: Phụ trách quản lý kho và vật tư
                this.danhSachTabs = new String[] { "Vật tư" };
                menuConMap.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp", "Phiếu nhập kho", "Phiếu xuất kho" });
                break;
        }

        if (danhSachTabs.length > 0) {
            this.tabHienTai = danhSachTabs[0];
        }
    }

    public String[] getMenuConCuaTab(String tenTab) {
        if (menuConMap == null) return null;
        return menuConMap.get(tenTab);
    }

    public String[] getDanhSachTabs() {
        return danhSachTabs;
    }

    public VaiTroHeThong getVaiTroHeThong() {
        return vaiTroHeThong;
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

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        add(taoHangThongTinTren(), BorderLayout.NORTH);
        add(taoHangDieuHuongTab(), BorderLayout.CENTER);
    }

    private JPanel taoHangThongTinTren() {
        JPanel hangTren = new JPanel(new BorderLayout());
        hangTren.setBackground(Color.WHITE);
        hangTren.setBorder(new EmptyBorder(12, 24, 12, 24));

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

        JLabel lblChucVu = new JLabel(vaiTroHeThong != null ? vaiTroHeThong.getTenHienThi() : chucVu);
        lblChucVu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblChucVu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(2));
        thongTin.add(lblChucVu);

        khoiNguoiDung.add(avatarNguoiDung);
        khoiNguoiDung.add(thongTin);
        panelPhai.add(khoiNguoiDung);

        JPopupMenu userMenu = new JPopupMenu();
        userMenu.setBackground(Color.WHITE);
        userMenu.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));

        JMenuItem itemThongTin = new JMenuItem("Thông tin tài khoản");
        itemThongTin.setFont(RangBuocGiaoDien.fontThuong(13));
        itemThongTin.setBackground(Color.WHITE);
        itemThongTin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemThongTin.addActionListener(e -> {
            Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
            ThongTinTaiKhoanDialog dlg = new ThongTinTaiKhoanDialog(frame, DangNhap_DAO.getTaiKhoanHienTai(), DangNhap_DAO.getTenNguoiDung());
            dlg.setVisible(true);
        });
        
        JMenuItem itemDangXuat = new JMenuItem("Đăng xuất");
        itemDangXuat.setFont(RangBuocGiaoDien.fontDam(13));
        itemDangXuat.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        itemDangXuat.setBackground(Color.WHITE);
        itemDangXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        itemDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?", "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                JFrame frameChinh = (JFrame) SwingUtilities.getWindowAncestor(this);
                GiaoDienChinhGUI.xuLyDangXuat(frameChinh);
            }
        });

        userMenu.add(itemThongTin);
        userMenu.addSeparator();
        userMenu.add(itemDangXuat);

        khoiNguoiDung.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userMenu.show(khoiNguoiDung, khoiNguoiDung.getWidth() - userMenu.getPreferredSize().width, khoiNguoiDung.getHeight());
            }
        });

        hangTren.add(panelPhai, BorderLayout.EAST);
        return hangTren;
    }

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

            JPopupMenu popupMenu = taoPopupMenu(tenTab);
            nut.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (popupMenu != null && popupMenu.getSubElements().length > 0) {
                        popupMenu.show(nut, 0, nut.getHeight());
                    }
                }
            });

            nut.addActionListener(e -> setTabHienTai(tenTab));
            danhSachNutTab.add(nut);
            panelTabContainer.add(nut);
        }
        panelTabContainer.revalidate();
        panelTabContainer.repaint();
    }

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

            item.addActionListener(e -> chonMenuCon(tenTab, con));
            menu.add(item);
        }
        return menu;
    }

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
        thietLapTheoVaiTro(chucVu);
        removeAll();
        khoiTaoGiaoDien();
        revalidate();
        repaint();
    }

    public String getTabHienTai() { return tabHienTai; }
    public void setTabSelectionListener(TabSelectionListener tabSelectionListener) { this.tabSelectionListener = tabSelectionListener; }
}
