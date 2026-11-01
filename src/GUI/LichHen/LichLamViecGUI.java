package GUI.LichHen;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import dao.LichLamViec_DAO;
import entity.LichLamViec_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class LichLamViecGUI extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel panelThang;
    private JPanel panelNgay;
    private TableMau bangTimeline;
    private LocalDate ngayDangChon = LocalDate.now();
    private LichLamViec_DAO dao = new LichLamViec_DAO();

    public LichLamViecGUI() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG); //[cite: 21]

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        panelThang = taoGiaoDienLichThang();
        panelNgay = taoGiaoDienTimelineNgay();

        mainPanel.add(panelThang, "THANG");
        mainPanel.add(panelNgay, "NGAY");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "THANG");
    }

    // 1. GIAO DIỆN LỊCH THÁNG
    private JPanel taoGiaoDienLichThang() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTieuDe = new JLabel("Lịch Làm Việc Tổng Quan");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24)); //[cite: 21]
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH); //[cite: 21]
        pnl.add(lblTieuDe, BorderLayout.NORTH);

        // Khung lưới hiển thị lịch cơ bản (Demo khung, logic tính ngày bạn tự loop)
        JPanel gridLich = new JPanel(new GridLayout(0, 7, 10, 10));
        gridLich.setOpaque(false);
        
        String[] thu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        for (String t : thu) {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setFont(RangBuocGiaoDien.fontDam(14));
            gridLich.add(l);
        }

        // Demo các nút ngày trong tháng
        for (int i = 1; i <= 30; i++) {
            JButton btnNgay = new JButton(String.valueOf(i));
            btnNgay.setFont(RangBuocGiaoDien.fontDam(16));
            btnNgay.setBackground(Color.WHITE);
            int currentDay = i;
            btnNgay.addActionListener(e -> {
                ngayDangChon = LocalDate.now().withDayOfMonth(currentDay);
                loadDuLieuNgay();
                cardLayout.show(mainPanel, "NGAY");
            });
            gridLich.add(btnNgay);
        }
        
        pnl.add(gridLich, BorderLayout.CENTER);
        return pnl;
    }

    // 2. GIAO DIỆN TIMELINE THEO NGÀY
    private JPanel taoGiaoDienTimelineNgay() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header điều hướng quay lại
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        JButton btnQuayLai = new JButton("⬅ Quay lại lịch tháng");
        btnQuayLai.setFont(RangBuocGiaoDien.fontDam(14)); //[cite: 21]
        btnQuayLai.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnQuayLai.setContentAreaFilled(false);
        btnQuayLai.setBorderPainted(false);
        btnQuayLai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuayLai.addActionListener(e -> cardLayout.show(mainPanel, "THANG"));
        
        JPanel pnlHanhDong = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlHanhDong.setOpaque(false);
        JButton btnThem = new JButton("+ Thêm Lịch");
        btnThem.setBackground(RangBuocGiaoDien.MAU_CHU_DAO); //[cite: 21]
        btnThem.setForeground(Color.WHITE);
        JButton btnSua = new JButton("Chỉnh sửa");
        
        btnSua.addActionListener(e -> kiemTraTruocKhiSua());

        pnlHanhDong.add(btnThem);
        pnlHanhDong.add(btnSua);

        pnlTop.add(btnQuayLai, BorderLayout.WEST);
        pnlTop.add(pnlHanhDong, BorderLayout.EAST);

        // Bảng dữ liệu dùng TableMau[cite: 22]
        String[] cot = {"Mã LLV", "Nhân sự", "Vai trò", "Bắt đầu", "Kết thúc", "Trạng thái"};
        bangTimeline = new TableMau(cot, new Object[][]{});
        JScrollPane sp = new JScrollPane(bangTimeline);
        sp.getViewport().setBackground(Color.WHITE);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(sp, BorderLayout.CENTER);
        return pnl;
    }

    // 3. LOGIC XỬ LÝ
    private void loadDuLieuNgay() {
        bangTimeline.xoaTatCaDuLieu(); // Phương thức từ TableMau[cite: 22]
        List<LichLamViec_entity> ds = dao.getLichTheoNgay(ngayDangChon);
        for (LichLamViec_entity l : ds) {
            bangTimeline.themDong(new Object[]{
                    l.getMaLLV(), l.getTenNhanSu(), l.getVaiTro(), 
                    l.getGioBatDau(), l.getGioKetThuc(), l.getTrangThai()
            });
        }
    }

    /**
     * Logic kiểm tra Khóa cứng lịch làm việc quá hạn (Đúng yêu cầu đề bài)
     */
    private void kiemTraTruocKhiSua() {
        int r = bangTimeline.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch làm việc cần sửa!");
            return;
        }

        // Kiểm tra xem lịch là của ngày hôm qua hay hôm nay
        if (ngayDangChon.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Lịch của ngày cũ đã bị khóa cứng, không thể chỉnh sửa!", "Khóa chỉnh sửa", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalTime gioBatDau = (LocalTime) bangTimeline.getValueAt(r, 3);
        LocalDateTime thoiDiemLich = LocalDateTime.of(ngayDangChon, gioBatDau);

        // Miễn là chưa đến giờ bắt đầu thì được sửa
        if (LocalDateTime.now().isAfter(thoiDiemLich)) {
            JOptionPane.showMessageDialog(this, "Đã quá thời gian bắt đầu ca làm việc. Bạn chỉ có thể cập nhật trạng thái!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            // Ở đây bạn có thể mở form thu gọn chỉ cho phép đổi trạng thái thay vì đổi giờ
        } else {
            // Mở JDialog Form sửa lịch bình thường...
            JOptionPane.showMessageDialog(this, "Mở form sửa lịch làm việc...");
        }
    }
}