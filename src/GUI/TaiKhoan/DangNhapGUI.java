package GUI.TaiKhoan;

import Service.TaiKhoan.DangNhapService;
import entity.TaiKhoan.TaiKhoan;

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
 * Hỗ trợ xác thực bảo mật SHA-256 khớp CSDL SALALA, lưu Session và phân quyền 6
 * vai trò người dùng
 */
public class DangNhapGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JCheckBox chkHienMatKhau;
    private JButton btnDangNhap;
    private JLabel lblThongBaoLoi;

    private final DangNhapService controller = new DangNhapService();

    public DangNhapGUI() {
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setTitle("Đăng nhập - SALALA Pet Shop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(850, 520));
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
                // Nền chuyển sắc nhẹ nhàng
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(13, 148, 136),
                        getWidth(), getHeight(), new Color(15, 118, 110));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(60, 40, 60, 40));

        // Tên thương hiệu dạng văn bản (thay cho logo đồ họa)
        JLabel logoIcon = new JLabel("[SALALA]");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoIcon.setForeground(Color.WHITE);
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
                "Quản lý hồ sơ thú cưng, khám chữa bệnh & kê thuốc",
                "Kiểm soát kho, vật tư, phiếu nhập xuất & nhà cung cấp",
                "Quản lý bán hàng, dịch vụ, bảng giá & hóa đơn thanh toán",
                "Xếp lịch làm việc & tiếp nhận đặt lịch hẹn linh hoạt",
                "Phân quyền chuẩn xác 6 nhóm người dùng hệ thống",
                "Báo cáo doanh thu & biểu đồ hiệu suất hoạt động"
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
        form.setBorder(new EmptyBorder(40, 48, 30, 48));

        JLabel lblTieuDe = new JLabel("Đăng nhập hệ thống");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPhu = new JLabel("Vui lòng nhập tài khoản để truy cập giao diện theo phân quyền");
        lblPhu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblPhu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblPhu.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lblTieuDe);
        form.add(Box.createVerticalStrut(4));
        form.add(lblPhu);
        form.add(Box.createVerticalStrut(20));

        // 1. Ô Tên đăng nhập
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(RangBuocGiaoDien.fontDam(12));
        lblUser.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblUser);
        form.add(Box.createVerticalStrut(6));

        txtTenDangNhap = new JTextField("admin");
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtTenDangNhap.setPreferredSize(new Dimension(300, 38));
        txtTenDangNhap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTenDangNhap.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        txtTenDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtTenDangNhap);
        form.add(Box.createVerticalStrut(14));

        // 2. Ô Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(RangBuocGiaoDien.fontDam(12));
        lblPass.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblPass);
        form.add(Box.createVerticalStrut(6));

        txtMatKhau = new JPasswordField(DangNhapService.MAT_KHAU_MAC_DINH);
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtMatKhau.setPreferredSize(new Dimension(300, 38));
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMatKhau.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtMatKhau);
        form.add(Box.createVerticalStrut(6));

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
        form.add(Box.createVerticalStrut(6));

        // 4. Nhãn thông báo lỗi
        lblThongBaoLoi = new JLabel(" ");
        lblThongBaoLoi.setFont(RangBuocGiaoDien.fontVua(12));
        lblThongBaoLoi.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        lblThongBaoLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblThongBaoLoi);
        form.add(Box.createVerticalStrut(8));

        // 5. Nút Đăng nhập
        btnDangNhap = new JButton("ĐĂNG NHẬP");
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnDangNhap.setPreferredSize(new Dimension(300, 42));
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setBackground(new Color(13, 148, 136));
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        form.add(btnDangNhap);
        form.add(Box.createVerticalStrut(16));

        form.add(Box.createVerticalGlue());

        return form;
    }

    private JButton taoNutChonNhanh(String username, String label) {
        JButton b = new JButton("<html><div style='text-align:center; white-space:nowrap;'><b>" + label
                + "</b><br><span style='font-size:10px; color:#0f766e;'>" + username + "</span></div></html>");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(new Color(13, 148, 136));
        b.setBackground(new Color(240, 253, 250));
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(204, 251, 241), 1, true),
                new EmptyBorder(6, 4, 6, 4)));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(90, 52));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            txtTenDangNhap.setText(username);
            txtMatKhau.setText(DangNhapService.MAT_KHAU_MAC_DINH);
            lblThongBaoLoi.setText(" ");
        });
        return b;
    }

    // ==========================================
    // XỬ LÝ ĐĂNG NHẬP & LƯU SESSION (nghiệp vụ nằm ở DangNhapService)
    // ==========================================
    private void xuLyDangNhap() {
        String username = txtTenDangNhap.getText().trim();
        String password = new String(txtMatKhau.getPassword()).trim();

        btnDangNhap.setEnabled(false);
        btnDangNhap.setText("Đang kiểm tra...");

        SwingUtilities.invokeLater(() -> {
            DangNhapService.KetQuaDangNhap result = controller.dangNhap(username, password);
            if (result.isThanhCong()) {
                lblThongBaoLoi.setText(" ");
                TaiKhoan tk = result.getTaiKhoan();

                // Khuyến nghị đổi mật khẩu nếu đang dùng mật khẩu mặc định
                if (controller.nenKhuyenNghiDoiMatKhau(password)) {
                    int luaChon = JOptionPane.showConfirmDialog(this,
                            "Bạn đang đăng nhập bằng mật khẩu mặc định (" + DangNhapService.MAT_KHAU_MAC_DINH
                                    + ").\nĐể đảm bảo an toàn, hệ thống khuyến nghị bạn nên đổi mật khẩu ngay bây giờ.\nBạn có muốn đổi mật khẩu không?",
                            "Cảnh báo bảo mật",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (luaChon == JOptionPane.YES_OPTION) {
                        ThongTinTaiKhoanGUI.DoiMatKhauDialog doiMkDialog = new ThongTinTaiKhoanGUI.DoiMatKhauDialog(
                                this, tk);
                        doiMkDialog.setVisible(true);
                    }
                }

                String tenHienThi = result.getTenNguoiDung();
                String vaiTro = tk.getVaiTro();

                JFrame frameMain = new JFrame("Hệ thống Quản lý SALALA Pet Shop");
                frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                GiaoDienChinhGUI giaoDienChinh = new GiaoDienChinhGUI(tenHienThi, vaiTro);
                frameMain.setContentPane(giaoDienChinh);
                GiaoDienChinhGUI.caiDatKichThuocCuaSo(frameMain, true);
                frameMain.setVisible(true);
                this.dispose();
            } else {
                hienThiLoi(result.getThongBao());
                if (result.getTruongLoi() == DangNhapService.TruongNhap.TEN_DANG_NHAP)
                    txtTenDangNhap.requestFocus();
                else if (result.getTruongLoi() == DangNhapService.TruongNhap.MAT_KHAU)
                    txtMatKhau.requestFocus();
            }
            btnDangNhap.setEnabled(true);
            btnDangNhap.setText("ĐĂNG NHẬP");
        });
    }

    private void hienThiLoi(String msg) {
        lblThongBaoLoi.setText(msg);
    }

    // ==========================================
    // MAIN ENTRY POINT CHO TOÀN BỘ ỨNG DỤNG
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (java.awt.AWTError | ClassNotFoundException | InstantiationException
                | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, "Không thể thiết lập giao diện hệ thống: " + ex.getMessage(),
                    "Lỗi giao diện", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            if (!new DangNhapService().ketNoiCoSoDuLieu()) {
                JOptionPane.showMessageDialog(null, "Không thể kết nối cơ sở dữ liệu SALALA.",
                        "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }

            DangNhapGUI login = new DangNhapGUI();
            login.setVisible(true);
        });
    }
}