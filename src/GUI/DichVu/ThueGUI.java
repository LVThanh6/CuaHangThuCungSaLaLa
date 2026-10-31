package GUI.DichVu;

import GUI.Chung.RangBuocGiaoDien;
import dao.Thue_DAO;
import entity.Thue_entity;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class ThueGUI extends JPanel {

    private static final Color XANH = new Color(0x0F6B5C);
    private static final Color NEN = new Color(0xF8FAFA);
    private static final Color VIEN = new Color(0xE5E7EB);
    private static final Color CHU = new Color(0x111827);
    private static final Color XAM = new Color(0x6B7280);
    private static final Color DO = new Color(0xDC2626);

    private final Thue_DAO dao = new Thue_DAO();
    private JTable table;
    private DefaultTableModel model;
    private JButton btnSua, btnXoa;
    private List<Thue_entity> danhSach;

    public ThueGUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(NEN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoBang(), BorderLayout.CENTER);

        taiLai();
    }

    private JPanel taoTieuDe() {
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblSub = new JLabel("Dịch vụ / Thuế");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(XAM);
        JLabel lblTitle = new JLabel("Quản lý Thuế");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(CHU);
        trai.add(lblSub);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTitle);

        JButton btnThem = new JButton("+ Thêm thuế");
        dinhDangNut(btnThem, true);
        btnThem.addActionListener(e -> moForm(null));

        btnSua = new JButton("Chỉnh sửa");
        dinhDangNut(btnSua, false);
        btnSua.setEnabled(false);
        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && danhSach != null && row < danhSach.size()) {
                moForm(danhSach.get(row));
            }
        });

        btnXoa = new JButton("Xóa");
        dinhDangNut(btnXoa, false);
        btnXoa.setForeground(DO);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && danhSach != null && row < danhSach.size()) {
                Thue_entity thue = danhSach.get(row);
                int kq = JOptionPane.showConfirmDialog(this, "Xóa loại thuế " + thue.getTenThue() + " (" + thue.getMaThue() + ")?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (kq == JOptionPane.YES_OPTION) {
                    if (dao.deleteThue(thue.getMaThue())) {
                        JOptionPane.showMessageDialog(this, "Xóa thành công!");
                        taiLai();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể xóa thuế (có thể đang được sử dụng).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(btnSua);
        phai.add(btnXoa);
        phai.add(btnThem);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(trai, BorderLayout.WEST);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    private JComponent taoBang() {
        String[] cot = { "Mã thuế", "Tên loại thuế", "Mức thuế (%)" };
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean coChon = table.getSelectedRow() >= 0;
            btnSua.setEnabled(coChon);
            btnXoa.setEnabled(coChon);
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(VIEN));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private void taiLai() {
        model.setRowCount(0);
        danhSach = dao.getAllThue();
        if (danhSach != null) {
            for (Thue_entity t : danhSach) {
                model.addRow(new Object[] {
                        t.getMaThue(),
                        t.getTenThue(),
                        t.getMucThue() + "%"
                });
            }
        }
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    private void moForm(Thue_entity goc) {
        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, goc == null ? "Thêm loại thuế" : "Chỉnh sửa thuế", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 280);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 8, 0);

        JTextField txtMa = new JTextField(goc == null ? "TX" + System.currentTimeMillis() % 100000 : goc.getMaThue());
        if (goc != null) txtMa.setEditable(false);
        JTextField txtTen = new JTextField(goc == null ? "" : nz(goc.getTenThue()));
        JTextField txtMuc = new JTextField(goc == null ? "10" : String.valueOf(goc.getMucThue()));

        String[] nhan = { "Mã thuế", "Tên loại thuế", "Mức thuế (%)" };
        JComponent[] truong = { txtMa, txtTen, txtMuc };

        for (int i = 0; i < nhan.length; i++) {
            gc.gridy = i * 2;
            p.add(new JLabel(nhan[i]), gc);
            gc.gridy = i * 2 + 1;
            gc.insets = new Insets(0, 0, 12, 0);
            p.add(truong[i], gc);
        }

        JButton btnLuu = new JButton("Lưu");
        dinhDangNut(btnLuu, true);
        btnLuu.addActionListener(e -> {
            try {
                String ma = txtMa.getText().trim();
                String ten = txtTen.getText().trim();
                double muc = Double.parseDouble(txtMuc.getText().trim().replace(",", "."));

                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Tên thuế không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Thue_entity thue = new Thue_entity(ma, ten, muc);
                boolean ok;
                if (goc == null) {
                    ok = dao.createThue(thue);
                } else {
                    ok = dao.updateThue(thue);
                }

                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Lưu thành công!");
                    dlg.dispose();
                    taiLai(); // Load lại dữ liệu mới ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(dlg, "Không thể lưu thuế vào CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Mức thuế phải là số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtn.setOpaque(false);
        pBtn.add(btnLuu);

        dlg.add(p, BorderLayout.CENTER);
        dlg.add(pBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void dinhDangNut(JButton b, boolean chinh) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(chinh ? Color.WHITE : CHU);
        b.setBackground(chinh ? XANH : Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(chinh ? XANH : VIEN),
                new EmptyBorder(8, 14, 8, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
