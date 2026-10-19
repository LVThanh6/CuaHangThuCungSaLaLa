package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Lớp PanelQuanLyMau: Tích hợp TableMau cùng Thanh tìm kiếm, Bộ lọc, Nút xuất file và Phân trang chuẩn giao diện SALALA.
 */
public class QuanLyTable extends JPanel {

    private TableMau bangDuLieu;
    private JTextField txtTimKiem;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public QuanLyTable(String[] tenCot, Object[][] duLieu, String placeholderTimKiem, String[] danhSachBoLoc) {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. THANH CÔNG CỤ PHÍA TRÊN (Tìm kiếm + Bộ lọc + Xuất danh sách)
        JPanel panelThanhCongCu = new JPanel(new BorderLayout(12, 0));
        panelThanhCongCu.setOpaque(false);

        // Bên trái: Ô tìm kiếm và các bộ lọc ComboBox
        JPanel panelTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTrai.setOpaque(false);

        // Ô tìm kiếm
        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(310, 38));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        
        // Hiệu ứng Placeholder cho ô tìm kiếm
        txtTimKiem.setText(placeholderTimKiem);
        txtTimKiem.setForeground(new Color(148, 163, 184));
        txtTimKiem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().equals(placeholderTimKiem)) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(new Color(30, 41, 59));
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().isEmpty()) {
                    txtTimKiem.setText(placeholderTimKiem);
                    txtTimKiem.setForeground(new Color(148, 163, 184));
                }
            }
        });
        panelTrai.add(txtTimKiem);

        // Các ComboBox bộ lọc truyền vào
        if (danhSachBoLoc != null) {
            for (String boLocTitle : danhSachBoLoc) {
                JComboBox<String> cb = new JComboBox<>(new String[]{boLocTitle, "Tất cả", "Lựa chọn 1", "Lựa chọn 2"});
                cb.setPreferredSize(new Dimension(170, 38));
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                cb.setBackground(Color.WHITE);
                panelTrai.add(cb);
            }
        }
        panelThanhCongCu.add(panelTrai, BorderLayout.CENTER);

        // Bên phải: Nút Xuất danh sách
        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        JButton btnXuat = new JButton("📥 Xuất danh sách");
        btnXuat.setPreferredSize(new Dimension(145, 38));
        btnXuat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXuat.setForeground(new Color(30, 41, 59));
        btnXuat.setBackground(Color.WHITE);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        btnXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXuat.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đang xuất dữ liệu ra Excel..."));
        panelPhai.add(btnXuat);
        panelThanhCongCu.add(panelPhai, BorderLayout.EAST);

        add(panelThanhCongCu, BorderLayout.NORTH);

        // 2. KHUNG CHỨA BẢNG DỮ LIỆU Ở GIỮA
        bangDuLieu = new TableMau(tenCot, duLieu);
        model = (DefaultTableModel) bangDuLieu.getModel();
        
        // Hỗ trợ lọc tìm kiếm tự động trên bảng
        rowSorter = new TableRowSorter<>(model);
        bangDuLieu.setRowSorter(rowSorter);

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtTimKiem.getText();
                if (text.equals(placeholderTimKiem) || text.trim().isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bangDuLieu);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Tạo Card bọc ngoài bảng
        JPanel panelCardBang = new JPanel(new BorderLayout());
        panelCardBang.setBackground(Color.WHITE);
        panelCardBang.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panelCardBang.add(scrollPane, BorderLayout.CENTER);

        // 3. THANH PHÂN TRANG PHÍA DƯỚI
        JPanel panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblThongTinTrang = new JLabel("Hiển thị 1–8 trong 24 bản ghi");
        lblThongTinTrang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongTinTrang.setForeground(new Color(100, 116, 139));
        panelPhanTrang.add(lblThongTinTrang, BorderLayout.WEST);

        JPanel panelNutTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelNutTrang.setOpaque(false);
        
        JLabel lblSoDong = new JLabel("Số dòng / trang");
        lblSoDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoDong.setForeground(new Color(100, 116, 139));
        panelNutTrang.add(lblSoDong);

        JComboBox<String> cbSoDong = new JComboBox<>(new String[]{"8", "10", "15", "20"});
        cbSoDong.setPreferredSize(new Dimension(60, 32));
        panelNutTrang.add(cbSoDong);

        panelNutTrang.add(taoNutPhanTrang("‹", false));
        panelNutTrang.add(taoNutPhanTrang("1", true)); // Trang đang chọn
        panelNutTrang.add(taoNutPhanTrang("2", false));
        panelNutTrang.add(taoNutPhanTrang("3", false));
        panelNutTrang.add(taoNutPhanTrang("›", false));

        panelPhanTrang.add(panelNutTrang, BorderLayout.EAST);
        panelCardBang.add(panelPhanTrang, BorderLayout.SOUTH);

        add(panelCardBang, BorderLayout.CENTER);
    }

    // Hàm phụ trợ tạo nút phân trang phong cách hiện đại
    private JButton taoNutPhanTrang(String text, boolean dangChon) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (dangChon) {
            btn.setBackground(new Color(14, 116, 101)); // Màu chủ đạo SALALA
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(14, 116, 101), 1));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(30, 41, 59));
            btn.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        }
        return btn;
    }

    // --- TÍCH HỢP CLASS TableMau ---
    public static class TableMau extends JTable {
        private DefaultTableModel model;

        public TableMau(String[] tenCot, Object[][] duLieu) {
            super(new DefaultTableModel(duLieu, tenCot) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            this.model = (DefaultTableModel) getModel();
            khoiTaoGiaoDien();
        }

        private void khoiTaoGiaoDien() {
            setRowHeight(42);
            setShowVerticalLines(false);
            setShowHorizontalLines(true);
            setGridColor(new Color(226, 232, 240));
            setSelectionBackground(new Color(230, 244, 241));
            setSelectionForeground(new Color(30, 41, 59));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(30, 41, 59));
            setBackground(Color.WHITE);
            setFillsViewportHeight(true);

            JTableHeader header = getTableHeader();
            header.setPreferredSize(new Dimension(0, 40));
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.setForeground(new Color(100, 116, 139));
            header.setBackground(new Color(248, 250, 252));
            header.setReorderingAllowed(false);

            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                        if (!isSelected) {
                            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                        }
                    }
                    return c;
                }
            });
        }
    }

    // --- HÀM MAIN CHẠY THỬ KIỂM THỬ ĐỘC LẬP ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Quản Lý Nhân Viên - SALALA Pet Shop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1250, 620);
            frame.setLocationRelativeTo(null);

            // Dữ liệu mẫu chuẩn
            String[] tieuDe = {"Mã nhân viên", "Họ và tên", "Vai trò", "Liên hệ", "Ngày vào làm", "Trạng thái", "Thao tác"};
            Object[][] duLieu = {
                {"NV001", "Lê Minh", "Bác sĩ thú y", "0901 234 567", "12/03/2022", "Đang làm việc", "👁 ✏ ..."},
                {"NV002", "Nguyễn Thu Hà", "Quản lý cửa hàng", "0912 345 678", "01/06/2022", "Đang làm việc", "👁 ✏ ..."},
                {"NV003", "Trần Quốc Huy", "Nhân viên bán hàng", "0933 456 789", "15/08/2023", "Đang làm việc", "👁 ✏ ..."},
                {"NV004", "Phạm Ngọc Mai", "Chăm sóc thú cưng", "0944 567 890", "02/10/2023", "Đang làm việc", "👁 ✏ ..."},
                {"NV005", "Võ Hoàng Nam", "Quản lý kho", "0965 678 901", "08/01/2024", "Tạm nghỉ", "👁 ✏ ..."},
                {"NV006", "Đặng Thanh Trúc", "Lễ tân", "0976 789 012", "20/02/2024", "Đang làm việc", "👁 ✏ ..."}
            };

            // Tiêu đề các bộ lọc thả xuống
            String[] danhSachBoLoc = {"Vai trò: Tất cả", "Trạng thái: Tất cả"};

            // Khởi tạo Panel quản lý hoàn chỉnh
            QuanLyTable panel = new QuanLyTable(
                    tieuDe, 
                    duLieu, 
                    "Tìm theo tên, mã nhân viên, số điện thoại...", 
                    danhSachBoLoc
            );

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}