package GUI;

import dao.KhachHang_DAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class DanhSachKhachHangGUI extends JPanel {
    private static final String[] COLUMNS = {
            "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
            "Địa chỉ", "Số thú cưng", "Hạng thành viên", "Tổng chi tiêu"
    };

    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();
    private final JPanel panelBang = new JPanel(new BorderLayout());

    public DanhSachKhachHangGUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel panelTieuDe = new JPanel(new BorderLayout(12, 0));
        panelTieuDe.setOpaque(false);
        JPanel panelNhan = new JPanel();
        panelNhan.setLayout(new BoxLayout(panelNhan, BoxLayout.Y_AXIS));
        panelNhan.setOpaque(false);

        JLabel lblDuongDan = new JLabel("Khách hàng / Danh sách khách hàng");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        JLabel lblTieuDe = new JLabel("Danh sách khách hàng");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelNhan.add(lblDuongDan);
        panelNhan.add(Box.createRigidArea(new Dimension(0, 4)));
        panelNhan.add(lblTieuDe);
        panelTieuDe.add(panelNhan, BorderLayout.WEST);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieu());
        panelTieuDe.add(btnLamMoi, BorderLayout.EAST);

        add(panelTieuDe, BorderLayout.NORTH);
        add(panelBang, BorderLayout.CENTER);
        taiDuLieu();
    }

    private void taiDuLieu() {
        ArrayList<KhachHang> khachHangs = khachHangDAO.getAllKhachHangDayDu();
        Object[][] rows = new Object[khachHangs.size()][COLUMNS.length];
        for (int i = 0; i < khachHangs.size(); i++) {
            KhachHang khachHang = khachHangs.get(i);
            rows[i] = new Object[] {
                    khachHang.getMaKH(),
                    khachHang.getHoTen(),
                    khachHang.getSdt(),
                    khachHang.getEmail(),
                    khachHang.getDiaChi(),
                    khachHang.getSoThuCung(),
                    khachHang.getHangThanhVien(),
                    String.format("%,.0f ₫", khachHang.getTongChiTieu())
            };
        }

        panelBang.removeAll();
        panelBang.add(new QuanLyTable(
                COLUMNS,
                rows,
                "Tìm theo tên, mã khách hàng, số điện thoại...",
                new String[] { "Hạng thành viên: Tất cả" }), BorderLayout.CENTER);
        panelBang.revalidate();
        panelBang.repaint();
    }
}
