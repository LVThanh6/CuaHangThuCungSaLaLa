package GUI.DichVu;

import GUI.Chung.RangBuocGiaoDien;
import entity.DichVu.Thue_entity;
import Service.DichVu.ThueService;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class ThueGUI extends JPanel {

    private final ThueService thueService = new ThueService();
    private JTable bangThue;
    private DefaultTableModel moHinhBang;
    private JButton btnSua, btnXoa;
    private List<Thue_entity> danhSach;

    public ThueGUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoBang(), BorderLayout.CENTER);

        taiLai();
    }

    private JPanel taoTieuDe() {
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblPhu = new JLabel("Dịch vụ / Thuế");
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel lblTieuDe = new JLabel("Quản lý Thuế");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        trai.add(lblPhu);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTieuDe);

        JButton btnThem = new JButton("Thêm thuế");
        dinhDangNut(btnThem, true);
        btnThem.addActionListener(suKien -> moBieuMau(null));

        btnSua = new JButton("Chỉnh sửa");
        dinhDangNut(btnSua, false);
        btnSua.setEnabled(false);
        btnSua.addActionListener(suKien -> {
            int chiSoDong = bangThue.getSelectedRow();
            if (chiSoDong >= 0 && danhSach != null && chiSoDong < danhSach.size()) {
                moBieuMau(danhSach.get(chiSoDong));
            }
        });

        btnXoa = new JButton("Xóa");
        dinhDangNut(btnXoa, false);
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(suKien -> {
            int chiSoDong = bangThue.getSelectedRow();
            if (chiSoDong >= 0 && danhSach != null && chiSoDong < danhSach.size()) {
                Thue_entity thue = danhSach.get(chiSoDong);
                int kq = JOptionPane.showConfirmDialog(this,
                        "Xóa loại thuế " + thue.getTenThue() + " (" + thue.getMaThue() + ")?", "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION);
                if (kq == JOptionPane.YES_OPTION) {
                    if (thueService.xoa(thue.getMaThue())) {
                        JOptionPane.showMessageDialog(this, "Xóa thành công!");
                        taiLai();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể xóa thuế (có thể đang được sử dụng).", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(btnSua);
        phai.add(btnXoa);
        phai.add(btnThem);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(trai, BorderLayout.WEST);
        panel.add(phai, BorderLayout.EAST);
        return panel;
    }

    private JComponent taoBang() {
        String[] cot = { "Mã thuế", "Tên loại thuế", "Mức thuế (%)" };
        moHinhBang = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int chiSoDong, int chiSoCot) {
                return false;
            }
        };
        bangThue = new JTable(moHinhBang);
        bangThue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bangThue.setRowHeight(32);
        bangThue.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        bangThue.getSelectionModel().addListSelectionListener(suKien -> {
            boolean coChon = bangThue.getSelectedRow() >= 0;
            btnSua.setEnabled(coChon);
            btnXoa.setEnabled(coChon);
        });

        JScrollPane cuonBang = new JScrollPane(bangThue);
        cuonBang.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        cuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        return cuonBang;
    }

    private void taiLai() {
        moHinhBang.setRowCount(0);
        danhSach = thueService.layTatCa();
        if (danhSach != null) {
            for (Thue_entity thue : danhSach) {
                moHinhBang.addRow(new Object[] {
                        thue.getMaThue(),
                        thue.getTenThue(),
                        thue.getMucThue() + "%"
                });
            }
        }
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    private void moBieuMau(Thue_entity goc) {
        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog hopThoai = new JDialog(cha, goc == null ? "Thêm loại thuế" : "Chỉnh sửa thuế",
                Dialog.ModalityType.APPLICATION_MODAL);
        hopThoai.setSize(400, 280);
        hopThoai.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        GridBagConstraints rangBuoc = new GridBagConstraints();
        rangBuoc.fill = GridBagConstraints.HORIZONTAL;
        rangBuoc.weightx = 1;
        rangBuoc.insets = new Insets(0, 0, 8, 0);

        JTextField txtMa = new JTextField(goc == null ? "TX" + System.currentTimeMillis() % 100000 : goc.getMaThue());
        if (goc != null)
            txtMa.setEditable(false);
        JTextField txtTen = new JTextField(goc == null ? "" : khongNull(goc.getTenThue()));
        JTextField txtMuc = new JTextField(goc == null ? "10" : String.valueOf(goc.getMucThue()));

        String[] nhan = { "Mã thuế", "Tên loại thuế", "Mức thuế (%)" };
        JComponent[] truong = { txtMa, txtTen, txtMuc };

        for (int chiSo = 0; chiSo < nhan.length; chiSo++) {
            rangBuoc.gridy = chiSo * 2;
            panel.add(new JLabel(nhan[chiSo]), rangBuoc);
            rangBuoc.gridy = chiSo * 2 + 1;
            rangBuoc.insets = new Insets(0, 0, 12, 0);
            panel.add(truong[chiSo], rangBuoc);
        }

        JButton btnLuu = new JButton("Lưu");
        dinhDangNut(btnLuu, true);
        btnLuu.addActionListener(suKien -> {
            try {
                boolean daLuu = thueService.luu(
                        txtMa.getText(), txtTen.getText(), txtMuc.getText(), goc == null);

                if (daLuu) {
                    JOptionPane.showMessageDialog(hopThoai, "Lưu thành công!");
                    hopThoai.dispose();
                    taiLai(); // Load lại dữ liệu mới ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(hopThoai, "Không thể lưu thuế vào CSDL.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException loi) {
                JOptionPane.showMessageDialog(hopThoai, loi.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelNut.setOpaque(false);
        panelNut.add(btnLuu);

        hopThoai.add(panel, BorderLayout.CENTER);
        hopThoai.add(panelNut, BorderLayout.SOUTH);
        hopThoai.setVisible(true);
    }

    private void dinhDangNut(JButton nutBam, boolean chinh) {
        nutBam.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nutBam.setForeground(chinh ? RangBuocGiaoDien.MAU_CHU_CHINH : RangBuocGiaoDien.MAU_CHU_CHINH);
        nutBam.setBackground(chinh ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_NEN_THE);
        nutBam.setFocusPainted(false);
        nutBam.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(chinh ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(8, 14, 8, 14)));
        nutBam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static String khongNull(String chuoi) {
        return chuoi == null ? "" : chuoi;
    }
}