package GUI.Chung;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import controller.Chung.NavigationController;
import GUI.GiaoDienChinhGUI;
import GUI.TaiKhoan.ThongTinTaiKhoanGUI;

public class HeaderGUI extends JPanel {

    public interface TabSelectionListener {
        void onTabSelected(String tenTab, String menuCon);
    }

    private String tenNguoiDung;
    private String chucVu;
    private String chuCaiAvatar;
    private NavigationController.VaiTroHeThong vaiTroHeThong;

    private String[] danhSachTabs;
    private NavigationController.MenuQuyen menuQuyen;
    private final NavigationController controller = new NavigationController();
    private String tabHienTai;

    private List<JButton> danhSachNutTab;
    private JPanel panelTabContainer;
    private TabSelectionListener tabSelectionListener;
    private Consumer<String> onTabChanged;

    public HeaderGUI() {
        this(
                new NavigationController().getTenNguoiDungHienTai(),
                new NavigationController().getVaiTroNguoiDungHienTai());
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

    public void thietLapTheoVaiTro(String vaiTro) {
        this.chucVu = vaiTro;
        capNhatChuCaiAvatar();
        menuQuyen = controller.layMenuQuyen(vaiTro);
        vaiTroHeThong = menuQuyen.getVaiTro();
        danhSachTabs = menuQuyen.getDanhSachTabs();

        if (danhSachTabs.length > 0) {
            this.tabHienTai = danhSachTabs[0];
        }
    }

    public String[] getMenuConCuaTab(String tenTab) {
        return menuQuyen == null ? null : menuQuyen.getMenuCon(tenTab);
    }

    public String[] getDanhSachTabs() {
        return danhSachTabs;
    }

    public NavigationController.VaiTroHeThong getVaiTroHeThong() {
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

        JLabel lblTen = new JLabel(tenNguoiDung);
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
            ThongTinTaiKhoanGUI.ThongTinTaiKhoanDialog dlg = new ThongTinTaiKhoanGUI.ThongTinTaiKhoanDialog(
                    frame, controller.getTaiKhoanHienTai(), controller.getTenNguoiDungHienTai());
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
            JButton nut = new JButton(tenTab) {
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
        String[] cacCon = getMenuConCuaTab(tenTab);
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