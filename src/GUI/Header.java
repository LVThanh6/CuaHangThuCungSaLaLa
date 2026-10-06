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
 * Thanh tiêu đề và điều hướng của SALALA Pet Shop (Chỉ đổi tab khi click, hover chỉ sổ menu).
 */
public class Header extends JPanel {

    public interface TabSelectionListener {
        void onTabSelected(String tenTab, String menuCon);
    }

    // --- Khai báo biến thành viên (Không gán giá trị sẵn) ---
    private String tenNguoiDung;
    private String chucVu;
    private String chuCaiAvatar;
    
    private String[] danhSachTabs;
    private Map<String, String[]> menuConMap; 
    private String tabHienTai;

    private List<JButton> danhSachNutTab;
    private TabSelectionListener tabSelectionListener;
    private Consumer<String> onTabChanged;
    private JLabel nhanNoiDung;

    // --- CONSTRUCTOR ---
    public Header() {
        // Gán giá trị khởi tạo tại constructor
        this.tenNguoiDung = "Lê Văn Thành";
        this.chucVu = "Quản lý";
        this.danhSachTabs = new String[]{"Nhân viên", "Thú cưng", "Khách hàng", "Hóa đơn", "Vật tư", "Lịch hẹn", "Dịch vụ"};
        this.tabHienTai = danhSachTabs[0];
        this.danhSachNutTab = new ArrayList<>();

        // Khởi tạo dữ liệu menu con tương ứng cho từng tab
        this.menuConMap = new HashMap<>();
        this.menuConMap.put("Nhân viên", new String[]{"Danh sách nhân viên", "Danh sách bác sĩ", "Thống kê"});
        this.menuConMap.put("Thú cưng", new String[]{"Hồ sơ thú cưng", "Loại thú cưng", "Lịch sử khám"});
        this.menuConMap.put("Khách hàng", new String[]{"Danh sách khách hàng", "Thẻ thành viên"});
        this.menuConMap.put("Hóa đơn", new String[]{"Tạo hóa đơn mới", "Danh sách hóa đơn"});
        this.menuConMap.put("Vật tư", new String[]{"Kho thuốc", "Dụng cụ y tế"});
        this.menuConMap.put("Lịch hẹn", new String[]{"Lịch hôm nay", "Đặt lịch mới"});
        this.menuConMap.put("Dịch vụ", new String[]{"Bảng giá dịch vụ", "Gói chăm sóc"});

        khoiTaoGiaoDien();
    }

    // 1. THÔNG TIN NGƯỜI DÙNG & HÀNG TRÊN
    private void thongTinNguoiDung() {
        String[] parts = tenNguoiDung.trim().split("\\s+");
        if (parts.length >= 2) {
            chuCaiAvatar = (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        } else {
            chuCaiAvatar = parts[0].substring(0, 1).toUpperCase();
        }

        JPanel hangTren = new JPanel(new BorderLayout());
        hangTren.setBackground(Color.WHITE);
        hangTren.setBorder(new EmptyBorder(12, 24, 12, 24));

        // --- Logo + Tên thương hiệu ---
        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelLogo.setOpaque(false);

        JComponent bieuTuongLogo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                GiaoDienMau.batKhuRangCua(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(GiaoDienMau.MAU_CHU_DAO);
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
        lblTenThuongHieu.setFont(GiaoDienMau.fontDam(18));
        lblTenThuongHieu.setForeground(GiaoDienMau.MAU_CHU_CHINH);

        panelLogo.add(bieuTuongLogo);
        panelLogo.add(lblTenThuongHieu);
        hangTren.add(panelLogo, BorderLayout.WEST);

        // --- Phần bên phải ---
        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panelPhai.setOpaque(false);

       

        // Khối người dùng
        JPanel khoiNguoiDung = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        khoiNguoiDung.setOpaque(false);
        khoiNguoiDung.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JComponent avatarNguoiDung = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                GiaoDienMau.batKhuRangCua(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(220, 244, 238));
                g2.fillOval(0, 0, 36, 36);

                g2.setColor(GiaoDienMau.MAU_CHU_DAO);
                g2.setFont(GiaoDienMau.fontDam(13));
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
        lblTen.setFont(GiaoDienMau.fontDam(13));
        lblTen.setForeground(GiaoDienMau.MAU_CHU_CHINH);

        JLabel lblChucVu = new JLabel(chucVu);
        lblChucVu.setFont(GiaoDienMau.fontThuong(11));
        lblChucVu.setForeground(GiaoDienMau.MAU_CHU_PHU);

        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(2));
        thongTin.add(lblChucVu);

        khoiNguoiDung.add(avatarNguoiDung);
        khoiNguoiDung.add(thongTin);
        panelPhai.add(khoiNguoiDung);

        hangTren.add(panelPhai, BorderLayout.EAST);
        add(hangTren, BorderLayout.NORTH);
    }

    // 2. THANH ĐIỀU HƯỚNG & XỬ LÝ SỔ MENU KHI HOVER, CHỈ ĐỔI TAB KHI CLICK
    private void thanhDieuHuong() {
        JPanel hangTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        hangTab.setBackground(Color.WHITE);
        hangTab.setBorder(new EmptyBorder(0, 20, 8, 20));
        danhSachNutTab.clear();

        for (String tenTab : danhSachTabs) {
            JButton nut = new JButton(tenTab + " ▾") {
                @Override
                protected void paintComponent(Graphics g) {
                    GiaoDienMau.batKhuRangCua(g);
                    Graphics2D g2 = (Graphics2D) g;
                    int w = getWidth(), h = getHeight();
                    boolean dangChon = tenTab.equals(tabHienTai);

                    if (dangChon) {
                        g2.setColor(GiaoDienMau.MAU_CHU_DAO_NHAT);
                        g2.fillRoundRect(0, 0, w, h, 8, 8);
                        g2.setColor(GiaoDienMau.MAU_VIEN_CHU_DAO);
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(241, 245, 249));
                        g2.fillRoundRect(0, 0, w, h, 8, 8);
                    }
                    super.paintComponent(g);
                }
            };

            nut.setFont(GiaoDienMau.fontVua(13));
            nut.setForeground(tenTab.equals(tabHienTai) ? GiaoDienMau.MAU_CHU_DAO : GiaoDienMau.MAU_CHU_CHINH);
            nut.setFocusPainted(false);
            nut.setBorderPainted(false);
            nut.setContentAreaFilled(false);
            nut.setOpaque(false);
            nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nut.setMargin(new Insets(6, 14, 6, 14));

            // Tạo Popup Menu tương ứng cho tab
            JPopupMenu popupMenu = taoPopupMenu(tenTab);

            // Sự kiện rê chuột vào (`mouseEntered`): Chỉ hiện popup sổ xuống, KHÔNG gọi setTabHienTai()
            nut.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (popupMenu != null && popupMenu.getSubElements().length > 0) {
                        popupMenu.show(nut, 0, nut.getHeight());
                    }
                }
            });

            // Sự kiện Click: Lúc này mới chính thức đổi tab
            nut.addActionListener(e -> setTabHienTai(tenTab));

            danhSachNutTab.add(nut);
            hangTab.add(nut);
        }

        add(hangTab, BorderLayout.CENTER);
    }

    // Hàm phụ trợ tạo Popup Menu sổ xuống
    private JPopupMenu taoPopupMenu(String tenTab) {
        String[] cacCon = menuConMap.get(tenTab);
        if (cacCon == null || cacCon.length == 0) return null;

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new LineBorder(GiaoDienMau.MAU_DUONG_VIEN, 1));

        for (String con : cacCon) {
            JMenuItem item = new JMenuItem(con);
            item.setFont(GiaoDienMau.fontThuong(13));
            item.setBackground(Color.WHITE);
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Khi click vào mục con trong menu -> đổi tab và nhận menu con tương ứng
            item.addActionListener(e -> {
                setTabHienTai(tenTab);
                if (tabSelectionListener != null) {
                    tabSelectionListener.onTabSelected(tenTab, con);
                }
            });
            menu.add(item);
        }
        return menu;
    }

    // 3. NỘI DUNG CHÍNH
    private void noiDungChinh() {
        if (nhanNoiDung == null) {
            nhanNoiDung = new JLabel();
            nhanNoiDung.setHorizontalAlignment(SwingConstants.CENTER);
            nhanNoiDung.setFont(GiaoDienMau.fontDam(18));
            nhanNoiDung.setForeground(GiaoDienMau.MAU_CHU_CHINH);

            JPanel vungNoiDung = new JPanel(new GridBagLayout());
            vungNoiDung.setBackground(GiaoDienMau.MAU_NEN_TRANG);
            vungNoiDung.add(nhanNoiDung);
            add(vungNoiDung, BorderLayout.SOUTH);
        }

        nhanNoiDung.setText("Đang xem tab: " + tabHienTai);
        revalidate();
        repaint();
    }

    // KHỞI TẠO GIAO DIỆN
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, GiaoDienMau.MAU_DUONG_VIEN));

        thongTinNguoiDung();
        thanhDieuHuong();
        noiDungChinh();
    }

    // SET TAB HIỆN TẠI
    public void setTabHienTai(String tenTab) {
        this.tabHienTai = tenTab;

        for (JButton nut : danhSachNutTab) {
            boolean dangChon = nut.getText().startsWith(tenTab);
            nut.setForeground(dangChon ? GiaoDienMau.MAU_CHU_DAO : GiaoDienMau.MAU_CHU_CHINH);
            nut.repaint();
        }

        noiDungChinh();

        if (tabSelectionListener != null) {
            tabSelectionListener.onTabSelected(tenTab, null);
        }
        if (onTabChanged != null) {
            onTabChanged.accept(tenTab);
        }
    }

    // LẤY TAB HIỆN TẠI
    public String getTabHienTai() {
        return tabHienTai;
    }

    // ĐĂNG KÝ LISTENER
    public void setTabSelectionListener(TabSelectionListener listener) {
        this.tabSelectionListener = listener;
    }

    public void setOnTabChanged(Consumer<String> onTabChanged) {
        this.onTabChanged = onTabChanged;
    }

    // CẬP NHẬT THÔNG TIN NGƯỜI DÙNG
    public void setThongTinNguoiDung(String ten, String chucVu, String avatarText) {
        this.tenNguoiDung = ten;
        this.chucVu = chucVu;
        this.chuCaiAvatar = avatarText;

        removeAll();
        khoiTaoGiaoDien();
        revalidate();
        repaint();
    }

    // MAIN TEST
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử độc lập - Thanh Header SALALA Pet Shop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 450);
            frame.setLocationRelativeTo(null);

            Header thanhHeader = new Header();

            JPanel khungChinh = new JPanel(new BorderLayout());
            khungChinh.setBackground(GiaoDienMau.MAU_NEN_TRANG);
            khungChinh.add(thanhHeader, BorderLayout.NORTH);

            JPanel vungNoiDungTest = new JPanel(new GridBagLayout());
            vungNoiDungTest.setBackground(GiaoDienMau.MAU_NEN_TRANG);

            JLabel nhanThongBao = new JLabel("Đang xem tab: " + thanhHeader.getTabHienTai());
            nhanThongBao.setFont(GiaoDienMau.fontDam(18));
            nhanThongBao.setForeground(GiaoDienMau.MAU_CHU_CHINH);

            vungNoiDungTest.add(nhanThongBao);
            khungChinh.add(vungNoiDungTest, BorderLayout.CENTER);

            thanhHeader.setOnTabChanged(tab -> {
                nhanThongBao.setText("Đang xem tab: " + tab);
            });

            thanhHeader.setTabSelectionListener((tab, menuCon) -> {
                if (menuCon != null) {
                    nhanThongBao.setText("Đang xem: " + tab + " -> " + menuCon);
                }
            });

            frame.setContentPane(khungChinh);
            frame.setVisible(true);
        });
    }

	public String getTenNguoiDung() {
		return tenNguoiDung;
	}

	public void setTenNguoiDung(String tenNguoiDung) {
		this.tenNguoiDung = tenNguoiDung;
	}

	public String getChucVu() {
		return chucVu;
	}

	public void setChucVu(String chucVu) {
		this.chucVu = chucVu;
	}

	public String getChuCaiAvatar() {
		return chuCaiAvatar;
	}

	public void setChuCaiAvatar(String chuCaiAvatar) {
		this.chuCaiAvatar = chuCaiAvatar;
	}

	public String[] getDanhSachTabs() {
		return danhSachTabs;
	}

	public void setDanhSachTabs(String[] danhSachTabs) {
		this.danhSachTabs = danhSachTabs;
	}

	public Map<String, String[]> getMenuConMap() {
		return menuConMap;
	}

	public void setMenuConMap(Map<String, String[]> menuConMap) {
		this.menuConMap = menuConMap;
	}

	public List<JButton> getDanhSachNutTab() {
		return danhSachNutTab;
	}

	public void setDanhSachNutTab(List<JButton> danhSachNutTab) {
		this.danhSachNutTab = danhSachNutTab;
	}

	public JLabel getNhanNoiDung() {
		return nhanNoiDung;
	}

	public void setNhanNoiDung(JLabel nhanNoiDung) {
		this.nhanNoiDung = nhanNoiDung;
	}

	public TabSelectionListener getTabSelectionListener() {
		return tabSelectionListener;
	}

	public Consumer<String> getOnTabChanged() {
		return onTabChanged;
	}
}
