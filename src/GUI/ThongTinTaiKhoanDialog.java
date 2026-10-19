package GUI;

import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Dialog hiển thị hồ sơ thông tin tài khoản người dùng đang đăng nhập.
 */
public class ThongTinTaiKhoanDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public ThongTinTaiKhoanDialog(Frame parent, TaiKhoan tk, String tenHienThi) {
        super(parent, "Thông tin tài khoản", true);
        setSize(440, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 28, 20, 28));

        // Khối Avatar lớn
        JPanel pAvatar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pAvatar.setOpaque(false);

        String initials = "SL";
        if (tenHienThi != null && !tenHienThi.trim().isEmpty()) {
            String[] parts = tenHienThi.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            } else {
                initials = parts[0].substring(0, 1).toUpperCase();
            }
        }
        final String avatarText = initials;

        JComponent avatarComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setColor(new Color(204, 251, 241));
                g2.fillOval(0, 0, 72, 72);

                g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(avatarText);
                int textH = fm.getAscent();
                g2.drawString(avatarText, (72 - textW) / 2, (72 + textH) / 2 - 4);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(72, 72);
            }
        };
        pAvatar.add(avatarComp);
        p.add(pAvatar);

        // Tên & Vai trò
        JLabel lblTen = new JLabel(tenHienThi != null ? tenHienThi : "Người dùng");
        lblTen.setFont(RangBuocGiaoDien.fontDam(18));
        lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTen);

        JLabel lblVaiTro = new JLabel(tk != null ? tk.getVaiTro() : "");
        lblVaiTro.setFont(RangBuocGiaoDien.fontVua(13));
        lblVaiTro.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblVaiTro.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblVaiTro);
        p.add(Box.createVerticalStrut(20));

        // Danh sách thông tin chi tiết
        JPanel pThongTin = new JPanel(new GridLayout(4, 2, 8, 10));
        pThongTin.setOpaque(false);
        pThongTin.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        themDongThongTin(pThongTin, "Mã tài khoản:", tk != null ? tk.getMaTK() : "---");
        themDongThongTin(pThongTin, "Tên đăng nhập:", tk != null ? tk.getTenDangNhap() : "---");
        themDongThongTin(pThongTin, "Trạng thái:", (tk != null && tk.isTrangThai()) ? "Hoạt động" : "Đã khóa");
        themDongThongTin(pThongTin, "Mã quản lý:", (tk != null && tk.getMaQuanLy() != null) ? tk.getMaQuanLy() : "Toàn quyền");

        p.add(pThongTin);
        p.add(Box.createVerticalStrut(20));

        // Nút Đổi mật khẩu & Đóng
        JPanel pNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        pNut.setOpaque(false);

        JButton btnDoiMatKhau = new JButton("🔑 Đổi mật khẩu");
        btnDoiMatKhau.setFont(RangBuocGiaoDien.fontVua(12));
        btnDoiMatKhau.setPreferredSize(new Dimension(130, 34));
        btnDoiMatKhau.setBackground(new Color(240, 253, 250));
        btnDoiMatKhau.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnDoiMatKhau.setBorder(new LineBorder(new Color(204, 251, 241), 1));
        btnDoiMatKhau.setFocusPainted(false);
        btnDoiMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDoiMatKhau.addActionListener(e -> {
            dispose();
            new DoiMatKhauDialog(parent, tk).setVisible(true);
        });
        pNut.add(btnDoiMatKhau);

        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(RangBuocGiaoDien.fontDam(12));
        btnDong.setPreferredSize(new Dimension(90, 34));
        btnDong.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnDong.setForeground(Color.WHITE);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> dispose());
        pNut.add(btnDong);

        p.add(pNut);
        add(p, BorderLayout.CENTER);
    }

    private void themDongThongTin(JPanel panel, String tieuDe, String giaTri) {
        JLabel lTieuDe = new JLabel(tieuDe);
        lTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lGiaTri = new JLabel(giaTri);
        lGiaTri.setFont(RangBuocGiaoDien.fontDam(12));
        lGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        panel.add(lTieuDe);
        panel.add(lGiaTri);
    }
}
