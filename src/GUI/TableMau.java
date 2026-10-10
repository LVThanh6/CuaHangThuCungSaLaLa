package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Lớp TableMau: Component bảng dữ liệu hiện đại, sạch sẽ, 
 * dễ dàng truyền biến và nhúng vào các chức năng khác nhau.
 */
public class TableMau extends JTable {
    
    private DefaultTableModel model;

    // Constructor khởi tạo với tiêu đề cột và dữ liệu mẫu
    public TableMau(String[] tenCot, Object[][] duLieu) {
        super(new DefaultTableModel(duLieu, tenCot) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Mặc định khóa không cho chỉnh sửa trực tiếp trên bảng
            }
        });
        this.model = (DefaultTableModel) getModel();
        khoiTaoGiaoDien();
    }

    // Constructor khởi tạo bảng trống
    public TableMau() {
        super(new DefaultTableModel());
        this.model = (DefaultTableModel) getModel();
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        // Cấu hình giao diện tổng thể cho bảng
        setRowHeight(42);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(new Color(226, 232, 240)); // Màu đường lưới xám nhạt
        setSelectionBackground(new Color(230, 244, 241)); // Màu khi chọn dòng
        setSelectionForeground(new Color(30, 41, 59));
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setForeground(new Color(30, 41, 59));
        setBackground(Color.WHITE);
        setFillsViewportHeight(true);

        // Cấu hình phần Tiêu đề (Header) của bảng
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(new Color(100, 116, 139));
        header.setBackground(new Color(248, 250, 252));
        header.setReorderingAllowed(false); // Không cho kéo dịch chuyển cột

        // Tùy chỉnh renderer mặc định để tạo hiệu ứng dòng xen kẽ và khoảng đệm (padding)
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); // Tạo khoảng cách lề cho chữ trong ô
                    if (!isSelected) {
                        // Màu xen kẽ giữa các dòng (Dòng chẵn trắng, dòng lẻ xám nhẹ)
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                }
                return c;
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC TIỆN ÍCH ĐỂ TRUYỀN DỮ LIỆU ---

    /**
     * Thêm một dòng dữ liệu mới vào bảng
     */
    public void themDong(Object[] duLieuDong) {
        model.addRow(duLieuDong);
    }

    /**
     * Xóa toàn bộ dữ liệu hiện có trong bảng
     */
    public void xoaTatCaDuLieu() {
        model.setRowCount(0);
    }

    /**
     * Cập nhật lại danh sách tiêu đề cột và dữ liệu mới
     */
    public void datLaiDuLieu(Object[][] duLieuMoi, String[] tenCotMoi) {
        model.setDataVector(duLieuMoi, tenCotMoi);
    }
    
    // --- HÀM MAIN CHẠY THỬ ĐỘC LẬP ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử độc lập - TableMau");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.setLocationRelativeTo(null);

            // 1. Chuẩn bị dữ liệu mẫu
            String[] tieuDe = {"Mã NV", "Họ và tên", "Chức vụ", "Trạng thái"};
            Object[][] duLieu = {
                {"NV001", "Lê Văn Thành", "Quản lý", "Đang làm việc"},
                {"NV002", "Nguyễn Văn A", "Nhân viên", "Tạm nghỉ"},
                {"NV003", "Trần Thị B", "Lễ tân", "Đang làm việc"}
            };

            // 2. Khởi tạo TableMau
            TableMau bangNhanVien = new TableMau(tieuDe, duLieu);

            // 3. Bọc bảng vào JScrollPane để hiển thị Tiêu đề (Header) và thanh cuộn
            JScrollPane thanhCuon = new JScrollPane(bangNhanVien);
            thanhCuon.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            thanhCuon.getViewport().setBackground(Color.WHITE);

            // 4. Đưa lên JFrame và hiển thị
            frame.add(thanhCuon, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}