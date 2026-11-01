package GUI.LichHen;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import dao.LichLamViec_DAO;
import entity.LichLamViec_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LichLamViecGUI extends JPanel {
    // Các Panel chia màn hình
    private JPanel panelTrai_Lich;
    private JPanel panelPhai_Timeline;
    
    // UI Lịch tháng
    private JLabel lblThangNam;
    private JPanel gridLich;
    
    // UI Timeline
    private TableMau bangTimeline;
    private JLabel lblTieuDeNgay;
    
    // Biến trạng thái
    private int thangHienTai = LocalDate.now().getMonthValue();
    private int namHienTai = LocalDate.now().getYear();
    private LocalDate ngayDangChon = LocalDate.now(); // Mặc định là hôm nay
    
    // Dữ liệu
    private LichLamViec_DAO dao = new LichLamViec_DAO();
    private List<LichLamViec_entity> danhSachLichNgay;

    public LichLamViecGUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Khởi tạo 2 nửa màn hình
        panelTrai_Lich = taoGiaoDienLichThang();
        panelPhai_Timeline = taoGiaoDienTimelineNgay();

        // Sử dụng JSplitPane để chia đôi màn hình (Trái: Lịch | Phải: Timeline)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTrai_Lich, panelPhai_Timeline);
        splitPane.setDividerLocation(420); // Chiều rộng cố định cho bộ Lịch
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        add(splitPane, BorderLayout.CENTER);
        
        // Render dữ liệu mặc định ban đầu (Của ngày hôm nay)
        capNhatLichThang();
        loadDuLieuNgay();
    }

    // ==============================================================
    // 1. KHỐI GIAO DIỆN BÊN TRÁI: LỊCH THÁNG
    // ==============================================================
    private JPanel taoGiaoDienLichThang() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Header điều hướng Tháng/Năm
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        lblThangNam = new JLabel("", SwingConstants.CENTER);
        lblThangNam.setFont(RangBuocGiaoDien.fontDam(18));
        lblThangNam.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        
        JButton btnHomNay = new JButton("Hôm nay");
        btnHomNay.setFont(RangBuocGiaoDien.fontDam(12));
        btnHomNay.setBackground(new Color(220, 244, 238));
        btnHomNay.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnHomNay.setFocusPainted(false);
        
        JButton btnPrev = taoNutDieuHuong("◀");
        JButton btnNext = taoNutDieuHuong("▶");
        
        // Sự kiện đổi tháng
        btnPrev.addActionListener(e -> doiThang(-1));
        btnNext.addActionListener(e -> doiThang(1));
        btnHomNay.addActionListener(e -> {
            thangHienTai = LocalDate.now().getMonthValue();
            namHienTai = LocalDate.now().getYear();
            ngayDangChon = LocalDate.now();
            capNhatLichThang();
            loadDuLieuNgay();
        });

        JPanel pnlDieuHuong = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.add(btnPrev);
        pnlDieuHuong.add(lblThangNam);
        pnlDieuHuong.add(btnNext);

        pnlTop.add(btnHomNay, BorderLayout.WEST);
        pnlTop.add(pnlDieuHuong, BorderLayout.CENTER);

        // Khung Grid chứa các ngày
        gridLich = new JPanel(new GridLayout(0, 7, 8, 8));
        gridLich.setOpaque(false);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(gridLich, BorderLayout.CENTER);
        
        return pnl;
    }

    private void doiThang(int delta) {
        thangHienTai += delta;
        if (thangHienTai < 1) {
            thangHienTai = 12;
            namHienTai--;
        } else if (thangHienTai > 12) {
            thangHienTai = 1;
            namHienTai++;
        }
        capNhatLichThang(); // Render lại lịch tháng mới
    }

    private void capNhatLichThang() {
        gridLich.removeAll();
        lblThangNam.setText("Tháng " + thangHienTai + " / " + namHienTai);
        
        String[] thu = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String t : thu) {
            JLabel lbl = new JLabel(t, SwingConstants.CENTER);
            lbl.setFont(RangBuocGiaoDien.fontDam(14));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            gridLich.add(lbl);
        }
        
        YearMonth ym = YearMonth.of(namHienTai, thangHienTai);
        LocalDate firstDay = ym.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue(); 
        int daysInMonth = ym.lengthOfMonth();
        
        // Ô trống đầu tháng
        for (int i = 1; i < dayOfWeek; i++) gridLich.add(new JLabel("")); 
        
        // Nút bấm các ngày trong tháng
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate dateOfBtn = ym.atDay(i);
            JButton btnNgay = new JButton(String.valueOf(i));
            btnNgay.setFont(RangBuocGiaoDien.fontDam(14));
            btnNgay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnNgay.setFocusPainted(false);
            
            // XỬ LÝ MÀU SẮC ĐÁNH DẤU TRẠNG THÁI NGÀY
            if (dateOfBtn.equals(ngayDangChon)) {
                // Đang được chọn (Màu xanh đậm)
                btnNgay.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnNgay.setForeground(Color.WHITE);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 2, true));
            } else if (dateOfBtn.equals(LocalDate.now())) {
                // Hôm nay nhưng không được chọn (Xanh nhạt)
                btnNgay.setBackground(new Color(220, 244, 238));
                btnNgay.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 1, true));
            } else if (dateOfBtn.isBefore(LocalDate.now())) {
                // Quá khứ (Xám)
                btnNgay.setBackground(new Color(245, 245, 245));
                btnNgay.setForeground(Color.GRAY);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            } else {
                // Tương lai (Trắng)
                btnNgay.setBackground(Color.WHITE);
                btnNgay.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            }
            
            // SỰ KIỆN KHI CLICK VÀO 1 NGÀY BẤT KỲ
            btnNgay.addActionListener(e -> {
                ngayDangChon = dateOfBtn; // Cập nhật ngày
                capNhatLichThang(); // Vẽ lại lịch để bôi đậm ô vừa chọn
                loadDuLieuNgay();   // Tải timeline bên phải lập tức
            });
            gridLich.add(btnNgay);
        }
        
        // Điền ô trống cuối tháng cho đủ lưới
        int remainingCells = 42 - (dayOfWeek - 1 + daysInMonth);
        for (int i = 0; i < remainingCells; i++) gridLich.add(new JLabel(""));
        
        gridLich.revalidate();
        gridLich.repaint();
    }

    // ==============================================================
    // 2. KHỐI GIAO DIỆN BÊN PHẢI: TIMELINE LỊCH TRÌNH
    // ==============================================================
    private JPanel taoGiaoDienTimelineNgay() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        lblTieuDeNgay = new JLabel("Timeline Ngày: ");
        lblTieuDeNgay.setFont(RangBuocGiaoDien.fontDam(18));
        lblTieuDeNgay.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        
        JPanel pnlHanhDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlHanhDong.setOpaque(false);
        
        JButton btnThem = new JButton("+ Thêm Lịch");
        btnThem.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThem.setForeground(Color.WHITE);
        btnThem.setFont(RangBuocGiaoDien.fontDam(13));
        btnThem.addActionListener(e -> themLichLamViec());

        JButton btnSua = new JButton("Sửa / Đổi TT");
        btnSua.setFont(RangBuocGiaoDien.fontDam(13));
        btnSua.setBackground(Color.WHITE);
        btnSua.addActionListener(e -> kiemTraTruocKhiThaoTac(false));

        JButton btnXoa = new JButton("Xóa");
        btnXoa.setFont(RangBuocGiaoDien.fontDam(13));
        btnXoa.setBackground(Color.WHITE);
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.addActionListener(e -> kiemTraTruocKhiThaoTac(true));

        pnlHanhDong.add(btnThem);
        pnlHanhDong.add(btnSua);
        pnlHanhDong.add(btnXoa);

        pnlTop.add(lblTieuDeNgay, BorderLayout.WEST);
        pnlTop.add(pnlHanhDong, BorderLayout.EAST);

        // Bảng dữ liệu 
        String[] cot = {"Mã LLV", "Mã NS", "Tên Nhân Sự", "Vai trò", "Bắt đầu", "Kết thúc", "Trạng thái"};
        bangTimeline = new TableMau(cot, new Object[][]{});
        
        JScrollPane sp = new JScrollPane(bangTimeline);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(sp, BorderLayout.CENTER);
        
        return pnl;
    }

    private void loadDuLieuNgay() {
        // Cập nhật tiêu đề bên phải
        lblTieuDeNgay.setText("Timeline Ngày: " + ngayDangChon.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        bangTimeline.xoaTatCaDuLieu(); 
        danhSachLichNgay = dao.getLichTheoNgay(ngayDangChon); // DAO tự động lọc trạng thái
        
        for (LichLamViec_entity l : danhSachLichNgay) {
            bangTimeline.themDong(new Object[]{
                    l.getMaLLV(), l.getMaNhanSu(), l.getTenNhanSu(), l.getVaiTro(), 
                    l.getGioBatDau(), l.getGioKetThuc(), l.getTrangThai()
            });
        }
    }

    // ==============================================================
    // 3. LOGIC XỬ LÝ QUYỀN HẠN (KHÓA CỨNG, KHÓA GIỜ)
    // ==============================================================
    private void themLichLamViec() {
        if (ngayDangChon.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Không thể thêm lịch làm việc vào ngày cũ!", "Từ chối", JOptionPane.ERROR_MESSAGE);
            return;
        }
        hienThiFormThongTin(null);
    }

    private void kiemTraTruocKhiThaoTac(boolean isXoa) {
        int r = bangTimeline.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 lịch làm việc trên Timeline để thao tác!");
            return;
        }

        // 1. Quá khứ -> Khóa cứng 100%
        if (ngayDangChon.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Lịch của ngày cũ đã bị khóa vĩnh viễn. Không thể sửa/xóa!", "Khóa chức năng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LichLamViec_entity llvChon = danhSachLichNgay.get(r);
        LocalDateTime thoiDiemBatDau = LocalDateTime.of(ngayDangChon, llvChon.getGioBatDau());

        // 2. Hôm nay nhưng đã quá giờ bắt đầu ca
        if (ngayDangChon.isEqual(LocalDate.now()) && LocalDateTime.now().isAfter(thoiDiemBatDau)) {
            if (isXoa) {
                JOptionPane.showMessageDialog(this, "Không thể xóa ca làm việc đã hoặc đang diễn ra!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } else {
                hienThiFormDoiTrangThai(llvChon); // Chặn Form Full, chỉ mở Form Mini cập nhật trạng thái
            }
        } 
        // 3. Chưa tới giờ -> Cho phép Sửa Full hoặc Xóa
        else {
            if (isXoa) {
                if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn xóa lịch này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    dao.xoaLich(llvChon.getMaLLV());
                    loadDuLieuNgay();
                }
            } else {
                hienThiFormThongTin(llvChon); 
            }
        }
    }

    // ==============================================================
    // 4. DIALOG THÊM / SỬA / CẬP NHẬT
    // ==============================================================
    private void hienThiFormDoiTrangThai(LichLamViec_entity llv) {
        String[] options = {"Đang làm", "Hoàn thành", "Vắng mặt", "Đi trễ", "Về sớm"};
        String input = (String) JOptionPane.showInputDialog(this, 
                "Ca làm việc này đã trôi qua giờ bắt đầu.\nBạn chỉ có thể cập nhật trạng thái:", 
                "Đánh dấu trạng thái", 
                JOptionPane.QUESTION_MESSAGE, null, options, llv.getTrangThai());
                
        if (input != null && !input.equals(llv.getTrangThai())) {
            if(dao.capNhatTrangThaiKhoa(llv.getMaLLV(), input)) {
                loadDuLieuNgay();
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
            }
        }
    }

    private void hienThiFormThongTin(LichLamViec_entity llvHienTai) {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ancestor, llvHienTai == null ? "Thêm Lịch Mới" : "Sửa Lịch Làm Việc", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnl = new JPanel(new GridLayout(4, 2, 10, 20));
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));
        pnl.setBackground(Color.WHITE);

        JTextField txtMaNS = new JTextField(llvHienTai != null ? llvHienTai.getMaNhanSu() : "");
        JTextField txtGioBD = new JTextField(llvHienTai != null ? llvHienTai.getGioBatDau().toString() : "08:00");
        JTextField txtGioKT = new JTextField(llvHienTai != null ? llvHienTai.getGioKetThuc().toString() : "17:00");
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Chưa làm", "Hoàn thành", "Vắng mặt"});
        if (llvHienTai != null) cbTrangThai.setSelectedItem(llvHienTai.getTrangThai());

        pnl.add(new JLabel("Mã NV / BS:"));
        pnl.add(txtMaNS);
        pnl.add(new JLabel("Bắt Đầu (HH:mm):"));
        pnl.add(txtGioBD);
        pnl.add(new JLabel("Kết Thúc (HH:mm):"));
        pnl.add(txtGioKT);
        pnl.add(new JLabel("Trạng Thái:"));
        pnl.add(cbTrangThai);

        JButton btnLuu = new JButton("Lưu Dữ Liệu");
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(RangBuocGiaoDien.fontDam(14));
        
        btnLuu.addActionListener(e -> {
            try {
                if (txtMaNS.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền mã nhân sự!");
                    return;
                }
                
                LichLamViec_entity capNhat = new LichLamViec_entity(
                    llvHienTai == null ? dao.phatSinhMaLLV() : llvHienTai.getMaLLV(), 
                    txtMaNS.getText().trim().toUpperCase(), 
                    "", "", ngayDangChon, 
                    LocalTime.parse(txtGioBD.getText()), 
                    LocalTime.parse(txtGioKT.getText()), 
                    cbTrangThai.getSelectedItem().toString()
                );

                if (llvHienTai == null) {
                    dao.themLich(capNhat);
                } else {
                    dao.capNhatLich(capNhat);
                }
                
                loadDuLieuNgay();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Xử lý thành công!");
                
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Giờ phải đúng định dạng 24h (VD: 08:30, 14:00)", "Sai định dạng", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnlBtn = new JPanel();
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnLuu);

        dialog.add(pnl, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JButton taoNutDieuHuong(String text) {
        JButton btn = new JButton(text);
        btn.setFont(RangBuocGiaoDien.fontDam(14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        return btn;
    }
}