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
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel panelThang;
    private JPanel panelNgay;
    
    // Components
    private TableMau bangTimeline;
    private JLabel lblThangNam;
    private JLabel lblTieuDeNgay;
    private JPanel gridLich;
    
    // State Variables
    private int thangHienTai = LocalDate.now().getMonthValue();
    private int namHienTai = LocalDate.now().getYear();
    private LocalDate ngayDangChon = LocalDate.now();
    
    private LichLamViec_DAO dao = new LichLamViec_DAO();
    private List<LichLamViec_entity> danhSachLichNgay;

    public LichLamViecGUI() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        panelThang = taoGiaoDienLichThang();
        panelNgay = taoGiaoDienTimelineNgay();

        mainPanel.add(panelThang, "THANG");
        mainPanel.add(panelNgay, "NGAY");

        add(mainPanel, BorderLayout.CENTER);
        
        capNhatLichThang();
        cardLayout.show(mainPanel, "THANG");
    }

    // ==============================================================
    // 1. GIAO DIỆN LỊCH THÁNG (VIEW DẠNG LƯỚI)
    // ==============================================================
    private JPanel taoGiaoDienLichThang() {
        JPanel pnl = new JPanel(new BorderLayout(0, 20));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        JLabel lblTieuDe = new JLabel("Lịch Làm Việc Tổng Quan");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        
        JPanel pnlDieuHuong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlDieuHuong.setOpaque(false);
        
        JButton btnPrev = taoNutTrang("<");
        JButton btnNext = taoNutTrang(">");
        JButton btnHomNay = new JButton("Hôm nay");
        btnHomNay.setFont(RangBuocGiaoDien.fontDam(14));
        btnHomNay.setBackground(Color.WHITE);
        btnHomNay.setFocusPainted(false);
        
        lblThangNam = new JLabel("", SwingConstants.CENTER);
        lblThangNam.setFont(RangBuocGiaoDien.fontDam(18));
        lblThangNam.setPreferredSize(new Dimension(160, 30));
        
        btnPrev.addActionListener(e -> doiThang(-1));
        btnNext.addActionListener(e -> doiThang(1));
        btnHomNay.addActionListener(e -> {
            thangHienTai = LocalDate.now().getMonthValue();
            namHienTai = LocalDate.now().getYear();
            capNhatLichThang();
        });

        pnlDieuHuong.add(btnHomNay);
        pnlDieuHuong.add(btnPrev);
        pnlDieuHuong.add(lblThangNam);
        pnlDieuHuong.add(btnNext);
        
        pnlTop.add(lblTieuDe, BorderLayout.WEST);
        pnlTop.add(pnlDieuHuong, BorderLayout.EAST);

        gridLich = new JPanel(new GridLayout(0, 7, 10, 10));
        gridLich.setOpaque(false);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(gridLich, BorderLayout.CENTER);
        
        return pnl;
    }

    private JButton taoNutTrang(String text) {
        JButton btn = new JButton(text);
        btn.setFont(RangBuocGiaoDien.fontDam(14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        return btn;
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
        capNhatLichThang();
    }

    private void capNhatLichThang() {
        gridLich.removeAll();
        lblThangNam.setText("Tháng " + thangHienTai + " / " + namHienTai);
        
        String[] thu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
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
        
        for (int i = 1; i < dayOfWeek; i++) {
            gridLich.add(new JLabel("")); 
        }
        
        for (int i = 1; i <= daysInMonth; i++) {
            int currentDay = i;
            JButton btnNgay = new JButton(String.valueOf(i));
            btnNgay.setFont(RangBuocGiaoDien.fontDam(18));
            btnNgay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnNgay.setFocusPainted(false);
            
            LocalDate dateOfBtn = ym.atDay(i);
            
            if (dateOfBtn.equals(LocalDate.now())) {
                btnNgay.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnNgay.setForeground(Color.WHITE);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 2, true));
            } else if (dateOfBtn.isBefore(LocalDate.now())) {
                btnNgay.setBackground(new Color(241, 245, 249)); // Quá khứ
                btnNgay.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            } else {
                btnNgay.setBackground(Color.WHITE); // Tương lai
                btnNgay.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            }
            
            btnNgay.addActionListener(e -> {
                ngayDangChon = dateOfBtn;
                loadDuLieuNgay();
                lblTieuDeNgay.setText("Timeline Lịch Ngày: " + ngayDangChon.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cardLayout.show(mainPanel, "NGAY");
            });
            gridLich.add(btnNgay);
        }
        
        int remainingCells = 42 - (dayOfWeek - 1 + daysInMonth);
        for (int i = 0; i < remainingCells; i++) {
            gridLich.add(new JLabel(""));
        }
        
        gridLich.revalidate();
        gridLich.repaint();
    }

    // ==============================================================
    // 2. GIAO DIỆN TIMELINE NGÀY (VIEW TABLEMAU)
    // ==============================================================
    private JPanel taoGiaoDienTimelineNgay() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        JButton btnQuayLai = taoNutTrang("⬅ Quay lại Lịch Tháng");
        btnQuayLai.addActionListener(e -> {
            capNhatLichThang(); 
            cardLayout.show(mainPanel, "THANG");
        });
        
        lblTieuDeNgay = new JLabel("Timeline Lịch Ngày: ");
        lblTieuDeNgay.setFont(RangBuocGiaoDien.fontDam(18));
        lblTieuDeNgay.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        
        JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTrai.setOpaque(false);
        pnlTrai.add(btnQuayLai);
        pnlTrai.add(lblTieuDeNgay);
        
        JPanel pnlHanhDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHanhDong.setOpaque(false);
        
        JButton btnThem = new JButton("+ Thêm Lịch");
        btnThem.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThem.setForeground(Color.WHITE);
        btnThem.setFont(RangBuocGiaoDien.fontDam(13));
        btnThem.addActionListener(e -> themLichLamViec());

        JButton btnSua = new JButton("Sửa / Đổi Trạng Thái");
        btnSua.setFont(RangBuocGiaoDien.fontDam(13));
        btnSua.setBackground(Color.WHITE);
        btnSua.addActionListener(e -> kiemTraTruocKhiThaoTac(false));

        JButton btnXoa = new JButton("Xóa Lịch");
        btnXoa.setFont(RangBuocGiaoDien.fontDam(13));
        btnXoa.setBackground(Color.WHITE);
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.addActionListener(e -> kiemTraTruocKhiThaoTac(true));

        pnlHanhDong.add(btnThem);
        pnlHanhDong.add(btnSua);
        pnlHanhDong.add(btnXoa);

        pnlTop.add(pnlTrai, BorderLayout.WEST);
        pnlTop.add(pnlHanhDong, BorderLayout.EAST);

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
        bangTimeline.xoaTatCaDuLieu(); 
        danhSachLichNgay = dao.getLichTheoNgay(ngayDangChon); 
        
        for (LichLamViec_entity l : danhSachLichNgay) {
            bangTimeline.themDong(new Object[]{
                    l.getMaLLV(), l.getMaNhanSu(), l.getTenNhanSu(), l.getVaiTro(), 
                    l.getGioBatDau(), l.getGioKetThuc(), l.getTrangThai()
            });
        }
    }

    // ==============================================================
    // 3. LOGIC XỬ LÝ (KIỂM TRA THỜI GIAN KHÓA CỨNG)
    // ==============================================================
    
    private void themLichLamViec() {
        // NGHIỆP VỤ: Quá khứ không được phép thêm lịch
        if (ngayDangChon.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày cũ đã bị khóa! Không thể thêm lịch.", "Từ chối", JOptionPane.ERROR_MESSAGE);
            return;
        }
        hienThiFormThongTin(null);
    }

    private void kiemTraTruocKhiThaoTac(boolean isXoa) {
        int r = bangTimeline.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 lịch làm việc để thao tác!");
            return;
        }

        // NGHIỆP VỤ 1: Ngày hôm qua -> Khóa cứng 100% không cho sửa/xóa
        if (ngayDangChon.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Lịch của ngày cũ đã khóa cứng. Không thể tác động!", "Khóa chỉnh sửa", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LichLamViec_entity llvChon = danhSachLichNgay.get(r);
        LocalDateTime thoiDiemBatDau = LocalDateTime.of(ngayDangChon, llvChon.getGioBatDau());

        // NGHIỆP VỤ 2: Hôm nay + Đã quá giờ bắt đầu
        if (ngayDangChon.isEqual(LocalDate.now()) && LocalDateTime.now().isAfter(thoiDiemBatDau)) {
            if (isXoa) {
                JOptionPane.showMessageDialog(this, "Không thể xóa ca làm việc đã hoặc đang diễn ra!", "Từ chối", JOptionPane.WARNING_MESSAGE);
            } else {
                // CHỈ CHO ĐỔI TRẠNG THÁI (Khóa đổi giờ)
                hienThiFormDoiTrangThai(llvChon);
            }
        } 
        // NGHIỆP VỤ 3: Hôm nay (Chưa tới giờ) HOẶC Ngày mai trở đi -> Cho phép Full chức năng
        else {
            if (isXoa) {
                if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa lịch này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    dao.xoaLich(llvChon.getMaLLV());
                    loadDuLieuNgay();
                }
            } else {
                hienThiFormThongTin(llvChon); // Mở Full Edit
            }
        }
    }

    // ==============================================================
    // 4. CÁC FORM THAO TÁC JDIALOG
    // ==============================================================
    
    // FORM ĐỔI TRẠNG THÁI NHANH (Khi đã qua giờ bắt đầu)
    private void hienThiFormDoiTrangThai(LichLamViec_entity llv) {
        String[] options = {"Đang làm", "Hoàn thành", "Vắng mặt", "Đi trễ", "Về sớm"};
        String input = (String) JOptionPane.showInputDialog(this, 
                "Ca làm việc này ĐÃ BẮT ĐẦU.\nBạn chỉ có thể cập nhật trạng thái:", 
                "Cập nhật trạng thái", 
                JOptionPane.QUESTION_MESSAGE, null, options, llv.getTrangThai());
                
        if (input != null && !input.equals(llv.getTrangThai())) {
            if(dao.capNhatTrangThaiKhoa(llv.getMaLLV(), input)) {
                loadDuLieuNgay();
                JOptionPane.showMessageDialog(this, "Đổi trạng thái thành công!");
            }
        }
    }

    // FORM THÊM / SỬA FULL (Khi chưa tới giờ)
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

        pnl.add(new JLabel("Mã Nhân Sự (NV/BS):"));
        pnl.add(txtMaNS);
        pnl.add(new JLabel("Giờ Bắt Đầu (HH:mm):"));
        pnl.add(txtGioBD);
        pnl.add(new JLabel("Giờ Kết Thúc (HH:mm):"));
        pnl.add(txtGioKT);
        pnl.add(new JLabel("Trạng Thái:"));
        pnl.add(cbTrangThai);

        JButton btnLuu = new JButton("Lưu Thông Tin");
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(RangBuocGiaoDien.fontDam(14));
        
        btnLuu.addActionListener(e -> {
            try {
                if (txtMaNS.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Mã nhân sự không được trống!");
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
                JOptionPane.showMessageDialog(dialog, "Lỗi: Giờ phải theo chuẩn (HH:mm). VD: 08:30", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi không xác định: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnlBtn = new JPanel();
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnLuu);

        dialog.add(pnl, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}