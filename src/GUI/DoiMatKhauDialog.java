package GUI;

import dao.TaiKhoan_DAO;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Dialog hỗ trợ người dùng tự đổi mật khẩu cá nhân.
 */
public class DoiMatKhauDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JPasswordField txtMatKhauCu;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhanMatKhau;
    private JLabel lblLoi;
    private final TaiKhoan taiKhoan;
    private final TaiKhoan_DAO taiKhoanDAO;

    public DoiMatKhauDialog(Frame parent, TaiKhoan taiKhoan) {
        super(parent, "Đổi mật khẩu tài khoản", true);
        this.taiKhoan = taiKhoan;
        this.taiKhoanDAO = new TaiKhoan_DAO();

        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setSize(420, 380);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel lblTieuDe = new JLabel("Đổi mật khẩu cá nhân");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(18));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        p.add(lblTieuDe);

        JLabel lblSub = new JLabel("Tài khoản: " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : ""));
        lblSub.setFont(RangBuocGiaoDien.fontThuong(12));
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        p.add(lblSub);
        p.add(Box.createVerticalStrut(18));

        // 1. Mật khẩu hiện tại
        p.add(taoNhan("Mật khẩu hiện tại:"));
        p.add(Box.createVerticalStrut(4));
        txtMatKhauCu = taoInputPass();
        p.add(txtMatKhauCu);
        p.add(Box.createVerticalStrut(12));

        // 2. Mật khẩu mới
        p.add(taoNhan("Mật khẩu mới (tối thiểu 6 ký tự):"));
        p.add(Box.createVerticalStrut(4));
        txtMatKhauMoi = taoInputPass();
        p.add(txtMatKhauMoi);
        p.add(Box.createVerticalStrut(12));

        // 3. Xác nhận mật khẩu mới
        p.add(taoNhan("Xác nhận mật khẩu mới:"));
        p.add(Box.createVerticalStrut(4));
        txtXacNhanMatKhau = taoInputPass();
        p.add(txtXacNhanMatKhau);
        p.add(Box.createVerticalStrut(8));

        lblLoi = new JLabel(" ");
        lblLoi.setFont(RangBuocGiaoDien.fontVua(11));
        lblLoi.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        p.add(lblLoi);
        p.add(Box.createVerticalStrut(14));

        // Nút bấm
        JPanel pNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pNut.setOpaque(false);

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.setFont(RangBuocGiaoDien.fontVua(13));
        btnHuy.setPreferredSize(new Dimension(90, 34));
        btnHuy.setBackground(new Color(241, 245, 249));
        btnHuy.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dispose());
        pNut.add(btnHuy);

        JButton btnXacNhan = new JButton("Cập nhật");
        btnXacNhan.setFont(RangBuocGiaoDien.fontDam(13));
        btnXacNhan.setPreferredSize(new Dimension(100, 34));
        btnXacNhan.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> xuLyDoiMatKhau());
        pNut.add(btnXacNhan);

        p.add(pNut);
        add(p, BorderLayout.CENTER);
    }

    private JLabel taoNhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(RangBuocGiaoDien.fontDam(12));
        l.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        return l;
    }

    private JPasswordField taoInputPass() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(340, 36));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return pf;
    }

    private void xuLyDoiMatKhau() {
        if (taiKhoan == null) {
            lblLoi.setText("Không tìm thấy thông tin tài khoản hợp lệ!");
            return;
        }

        String passCu = new String(txtMatKhauCu.getPassword()).trim();
        String passMoi = new String(txtMatKhauMoi.getPassword()).trim();
        String passXacNhan = new String(txtXacNhanMatKhau.getPassword()).trim();

        if (passCu.isEmpty()) {
            lblLoi.setText("Vui lòng nhập mật khẩu hiện tại!");
            txtMatKhauCu.requestFocus();
            return;
        }
        if (passMoi.isEmpty()) {
            lblLoi.setText("Vui lòng nhập mật khẩu mới!");
            txtMatKhauMoi.requestFocus();
            return;
        }
        if (passMoi.length() < 6) {
            lblLoi.setText("Mật khẩu mới phải có ít nhất 6 ký tự!");
            txtMatKhauMoi.requestFocus();
            return;
        }
        if (!passMoi.equals(passXacNhan)) {
            lblLoi.setText("Mật khẩu xác nhận không khớp!");
            txtXacNhanMatKhau.requestFocus();
            return;
        }

        // Kiểm tra mật khẩu cũ có đúng không
        String hashedOld = TaiKhoan_DAO.hashPassword(passCu);
        boolean khopOld = hashedOld.equalsIgnoreCase(taiKhoan.getMatKhau()) || passCu.equals(taiKhoan.getMatKhau());
        if (!khopOld) {
            lblLoi.setText("Mật khẩu hiện tại không chính xác!");
            txtMatKhauCu.requestFocus();
            return;
        }

        // Đổi mật khẩu trong CSDL
        boolean ok = taiKhoanDAO.doiMatKhau(taiKhoan.getMaTK(), passMoi);
        if (ok) {
            taiKhoan.setMatKhau(TaiKhoan_DAO.hashPassword(passMoi));
            JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            lblLoi.setText("Không thể cập nhật mật khẩu vào CSDL!");
        }
    }
}
