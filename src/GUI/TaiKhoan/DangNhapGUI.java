package GUI.TaiKhoan;

import dao.DangNhap_DAO;
import dao.TaiKhoan_DAO;
import model.TaiKhoan;

import GUI.Chung.RangBuocGiaoDien;
import GUI.GiaoDienChinhGUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Giao diện Đăng nhập hệ thống SALALA Pet Shop.
 * Tích hợp xác thực bảo mật chuẩn SHA-256 khớp với CSDL SALALA và lưu Session.
 */
public class DangNhapGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JCheckBox chkHienMatKhau;
    private JButton btnDangNhap;
    private JLabel lblThongBaoLoi;

    private final TaiKhoan_DAO taiKhoanDAO;

    public DangNhapGUI() {
        this.taiKhoanDAO = new TaiKhoan_DAO();
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setTitle("Đăng nhập - SALALA Pet Shop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 540);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setLayout(new GridLayout(1, 2));

        // 1. CỘT TRÁI: BANNER THƯƠNG HIỆU
        getContentPane().add(taoPanelBanner());

        // 2. CỘT PHẢI: FORM ĐĂNG NHẬP
        getContentPane().add(taoPanelForm());

        // Gán phím Enter để đăng nhập nhanh
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    xuLyDangNhap();
                }
            }
        };
        txtTenDangNhap.addKeyListener(enterListener);
        txtMatKhau.addKeyListener(enterListener);
    }

    // ==========================================
    // CỘT TRÁI: BANNER
    // ==========================================
    private JPanel taoPanelBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                RangBuocGiaoDien.batKhuRangCua(g2);
                // Gradient nền chuyển sắc nhẹ nhàng
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(13, 148, 136),
                        getWidth(), getHeight(), new Color(15, 118, 110));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Họa tiết trang trí vòng tròn chìm
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillOval(-50, -50, 220, 220);
                g2.fillOval(getWidth() - 120, getHeight() - 140, 200, 200);
            }
        };
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(60, 40, 60, 40));

        // Biểu tượng Logo
        JComponent logoIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, 54, 54, 16, 16);

                g2.setColor(new Color(13, 148, 136));
                g2.fillOval(14, 14, 10, 10);
                g2.fillOval(30, 14, 10, 10);
                g2.fillOval(22, 28, 11, 11);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(54, 54);
            }
        };
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTenThuongHieu = new JLabel("SALALA Pet Shop");
        lblTenThuongHieu.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTenThuongHieu.setForeground(Color.WHITE);
        lblTenThuongHieu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSlogan = new JLabel("Hệ thống Quản lý Cửa hàng & Phòng khám Thú y");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSlogan.setForeground(new Color(204, 251, 241));
        lblSlogan.setAlignmentX(Component.LEFT_ALIGNMENT);

        banner.add(logoIcon);
        banner.add(Box.createVerticalStrut(18));
        banner.add(lblTenThuongHieu);
        banner.add(Box.createVerticalStrut(6));
        banner.add(lblSlogan);
        banner.add(Box.createVerticalStrut(40));

        // Các tính năng nổi bật
        String[] tinhNang = {
                "🐾 Quản lý hồ sơ thú cưng & khám chữa bệnh",
                "📦 Kiểm soát vật tư, thuốc & hóa đơn xuất nhập",
                "🔒 Bảo mật đăng nhập phân quyền đa cấp SHA-256",
                "📊 Báo cáo doanh thu & hiệu suất hoạt động"
        };
        for (String tn : tinhNang) {
            JLabel lblTN = new JLabel(tn);
            lblTN.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTN.setForeground(new Color(240, 253, 250));
            lblTN.setAlignmentX(Component.LEFT_ALIGNMENT);
            banner.add(lblTN);
            banner.add(Box.createVerticalStrut(12));
        }

        banner.add(Box.createVerticalGlue());

        JLabel lblBaoMat = new JLabel("© 2026 SALALA Pet Shop - All Rights Reserved");
        lblBaoMat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBaoMat.setForeground(new Color(153, 246, 228));
        lblBaoMat.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.add(lblBaoMat);

        return banner;
    }

    // ==========================================
    // CỘT PHẢI: FORM ĐĂNG NHẬP
    // ==========================================
    private JPanel taoPanelForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(50, 48, 40, 48));

        JLabel lblTieuDe = new JLabel("Đăng nhập hệ thống");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPhu = new JLabel("Vui lòng nhập tài khoản để truy cập giao diện quản trị");
        lblPhu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblPhu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblPhu.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lblTieuDe);
        form.add(Box.createVerticalStrut(4));
        form.add(lblPhu);
        form.add(Box.createVerticalStrut(24));

        // 1. Ô Tên đăng nhập
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(RangBuocGiaoDien.fontDam(12));
        lblUser.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblUser);
        form.add(Box.createVerticalStrut(6));

        txtTenDangNhap = new JTextField("quanly01");
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtTenDangNhap.setPreferredSize(new Dimension(300, 38));
        txtTenDangNhap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTenDangNhap.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        txtTenDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtTenDangNhap);
        form.add(Box.createVerticalStrut(16));

        // 2. Ô Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(RangBuocGiaoDien.fontDam(12));
        lblPass.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblPass);
        form.add(Box.createVerticalStrut(6));

        txtMatKhau = new JPasswordField("123456");
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtMatKhau.setPreferredSize(new Dimension(300, 38));
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMatKhau.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtMatKhau);
        form.add(Box.createVerticalStrut(8));

        // 3. Checkbox Hiện mật khẩu
        chkHienMatKhau = new JCheckBox("Hiển thị mật khẩu");
        chkHienMatKhau.setFont(RangBuocGiaoDien.fontThuong(11));
        chkHienMatKhau.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setFocusPainted(false);
        chkHienMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkHienMatKhau.addActionListener(e -> {
            txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? (char) 0 : '•');
        });
        form.add(chkHienMatKhau);
        form.add(Box.createVerticalStrut(10));

        // 4. Nhãn thông báo lỗi
        lblThongBaoLoi = new JLabel(" ");
        lblThongBaoLoi.setFont(RangBuocGiaoDien.fontVua(12));
        lblThongBaoLoi.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        lblThongBaoLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblThongBaoLoi);
        form.add(Box.createVerticalStrut(12));

        // 5. Nút Đăng nhập
        btnDangNhap = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                RangBuocGiaoDien.batKhuRangCua(g2);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(15, 118, 110));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(20, 184, 166));
                } else {
                    g2.setColor(new Color(13, 148, 136));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnDangNhap.setPreferredSize(new Dimension(300, 42));
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setContentAreaFilled(false);
        btnDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        form.add(btnDangNhap);
        form.add(Box.createVerticalStrut(20));

        // 6. Gợi ý tài khoản mẫu để test nhanh
        JPanel panelGoiY = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panelGoiY.setOpaque(false);
        panelGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGoiY = new JLabel("💡 Mẹo thử nhanh (pass: 123456):");
        lblGoiY.setFont(RangBuocGiaoDien.fontThuong(11));
        lblGoiY.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelGoiY.add(lblGoiY);

        JButton btnTestQL = taoNutChonNhanh("quanly01");
        JButton btnTestNV = taoNutChonNhanh("nhanvien01");
        JButton btnTestBS = taoNutChonNhanh("bacsi01");

        panelGoiY.add(btnTestQL);
        panelGoiY.add(btnTestNV);
        panelGoiY.add(btnTestBS);

        form.add(panelGoiY);
        form.add(Box.createVerticalGlue());

        return form;
    }

    private JButton taoNutChonNhanh(String username) {
        JButton b = new JButton(username);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(new Color(13, 148, 136));
        b.setBackground(new Color(240, 253, 250));
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(204, 251, 241), 1, true),
                new EmptyBorder(2, 6, 2, 6)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            txtTenDangNhap.setText(username);
            txtMatKhau.setText("123456");
            lblThongBaoLoi.setText(" ");
        });
        return b;
    }

    // ==========================================
    // XỬ LÝ ĐĂNG NHẬP & LƯU SESSION
    // ==========================================
    private void xuLyDangNhap() {
        String username = txtTenDangNhap.getText().trim();
        String password = new String(txtMatKhau.getPassword()).trim();

        if (username.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên đăng nhập!");
            txtTenDangNhap.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            hienThiLoi("Vui lòng nhập mật khẩu!");
            txtMatKhau.requestFocus();
            return;
        }

        btnDangNhap.setEnabled(false);
        btnDangNhap.setText("Đang kiểm tra...");

        SwingUtilities.invokeLater(() -> {
            try {
                TaiKhoan_DAO.KetQuaDangNhap kq = taiKhoanDAO.dangNhap(username, password);

                if (kq.isThanhCong()) {
                    lblThongBaoLoi.setText(" ");
                    TaiKhoan tk = kq.getTaiKhoan();
                    String tenHienThi = kq.getTenNguoiDung();
                    String vaiTro = tk.getVaiTro();

                    // ⭐ LƯU THÔNG TIN PHIÊN LÀM VIỆC VÀO SESSION
                    DangNhap_DAO.dangNhap(tk, tenHienThi, kq.getMaDoiTuong());

                    // Mở giao diện chính với thông tin tài khoản đã đăng nhập
                    JFrame frameMain = new JFrame("Hệ thống Quản lý SALALA Pet Shop");
                    frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    GiaoDienChinhGUI giaoDienChinh = new GiaoDienChinhGUI(tenHienThi, vaiTro);
                    frameMain.setContentPane(giaoDienChinh);
                    GiaoDienChinhGUI.caiDatKichThuocCuaSo(frameMain, true);
                    frameMain.setVisible(true);

                    // Đóng cửa sổ đăng nhập
                    this.dispose();
                } else {
                    hienThiLoi(kq.getThongBao());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                hienThiLoi("Lỗi hệ thống: " + ex.getMessage());
            } finally {
                btnDangNhap.setEnabled(true);
                btnDangNhap.setText("ĐĂNG NHẬP");
            }
        });
    }

    private void hienThiLoi(String msg) {
        lblThongBaoLoi.setText("⚠ " + msg);
    }

    // ==========================================
    // MAIN ENTRY POINT CHO TOÀN BỘ ỨNG DỤNG
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}
