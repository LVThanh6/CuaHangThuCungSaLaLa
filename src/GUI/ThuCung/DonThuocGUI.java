package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import GUI.ThuCung.HoSoThuCungGUI.NutBoTron;
import GUI.ThuCung.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.ThuCung.HoSoThuCungGUI.PanelBoTron;
import dao.BacSi_DAO;
import dao.ChiTietDonThuoc_DAO;
import dao.DonThuoc_DAO;
import dao.HoSoSucKhoe_DAO;
import dao.ThuCung_DAO;
import entity.BacSi_entity;
import entity.ChiTietDonThuoc_entity;
import entity.DonThuoc_entity;
import entity.HoSoSucKhoe_entity;
import entity.ThuCung_entity;
import entity.Thuoc_entity;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DonThuocGUI extends JPanel {

    private static final int KHOANG_CACH_LE = 20;
    private static final int KHOANG_CACH_THE = 16;
    private static final int BAN_KINH_THE = 12;
    private static final int CHIEU_CAO_THE_THONG_KE = 80;
    private static final int CHIEU_CAO_O_NHAP = 38;
    private static final int CHIEU_CAO_THANH_PHAN_TRANG = 56;
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final String[] tieuDeCot = { "Mã đơn thuốc", "Thú cưng", "Mã hồ sơ", "Bác sĩ kê đơn", "Ngày kê đơn",
            "Số loại thuốc", "Ghi chú", "Trạng thái" };
    private final String[] dsTrangThaiLoc = { "Trạng thái: Tất cả", "Chờ lấy thuốc", "Đã cấp thuốc", "Hoàn thành" };
    
    // Cập nhật bộ lọc thời gian: Thêm Tuần này, Tháng 1-12 và tùy chỉnh
    private final String[] dsThoiGianLoc = { 
        "Thời gian: Tất cả", "Hôm nay", "Tuần này", 
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12",
        "Tùy chỉnh khoảng ngày..."
    };
    
    
    private final DonThuoc_DAO donThuocDAO = new DonThuoc_DAO();
    private final ChiTietDonThuoc_DAO chiTietDAO = new ChiTietDonThuoc_DAO();
    private final HoSoSucKhoe_DAO hoSoSucKhoeDAO = new HoSoSucKhoe_DAO();
    private final ThuCung_DAO thuCungDAO = new ThuCung_DAO();
    private final BacSi_DAO bacSiDAO = new BacSi_DAO();
    
    // --- DỮ LIỆU & PHÂN TRANG ---
    private List<DonThuoc_entity> danhSachDonThuoc = new ArrayList<>();
    private List<DonThuoc_entity> daLoc = new ArrayList<>();
    
    private LocalDate tuNgay;
    private LocalDate denNgay;

    private JPanel panelTieuDe, panelThongKe;
    private JLabel nhanSoTongDon, nhanSoDaCapThuoc, nhanSoChoLayThuoc, nhanTongSoThuoc;
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboTrangThai, cboThoiGian;
    
    // Khai báo các nút thao tác
    private JButton nutChonKhoangNgay, nutXemChiTiet, nutChinhSua, nutXoa, nutXuatThuoc;
    private NutBoTron nutKeDonThuoc, nutXuatDanhSach;
    private TableMau bangDonThuoc;
    private JScrollPane thanhCuonBang;

    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;

    public DonThuocGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        loadDataToTable();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        khoiTaoTieuDe();
        khoiTaoThongKe();
        khoiTaoTheBang();

        add(panelTieuDe, BorderLayout.NORTH);
        JPanel panelThan = new JPanel(new BorderLayout(0, KHOANG_CACH_THE));
        panelThan.setOpaque(false);
        panelThan.add(panelThongKe, BorderLayout.NORTH);
        panelThan.add(panelTheBang, BorderLayout.CENTER);
        add(panelThan, BorderLayout.CENTER);
    }

    private void khoiTaoTieuDe() {
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);
        panelTieuDe.setBorder(new EmptyBorder(0, 0, KHOANG_CACH_THE, 0));

        JPanel panelChu = new JPanel(); panelChu.setOpaque(false); panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        JLabel nhanDuongDan = new JLabel("Thú cưng / Đơn thuốc"); nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12)); nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel nhanTieuDe = new JLabel("Đơn thuốc"); nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26)); nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel nhanMoTa = new JLabel("Quản lý đơn thuốc do bác sĩ kê cho thú cưng sau mỗi lượt khám."); nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12)); nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelChu.add(nhanDuongDan); panelChu.add(Box.createVerticalStrut(4)); panelChu.add(nhanTieuDe); panelChu.add(Box.createVerticalStrut(2)); panelChu.add(nhanMoTa);

        nutKeDonThuoc = new NutBoTron("+  Kê đơn thuốc", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutKeDonThuoc.setPreferredSize(new Dimension(146, 40));
        JPanel panelNut = new JPanel(new GridBagLayout()); panelNut.setOpaque(false); panelNut.add(nutKeDonThuoc);

        panelTieuDe.add(panelChu, BorderLayout.WEST); panelTieuDe.add(panelNut, BorderLayout.EAST);
    }

    private void khoiTaoThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH_THE, 0));
        panelThongKe.setOpaque(false); panelThongKe.setPreferredSize(new Dimension(0, CHIEU_CAO_THE_THONG_KE));

        nhanSoTongDon = new JLabel("0"); nhanSoDaCapThuoc = new JLabel("0");
        nhanSoChoLayThuoc = new JLabel("0"); nhanTongSoThuoc = new JLabel("0");

        panelThongKe.add(taoTheThongKe("ĐT", "Tổng đơn thuốc", nhanSoTongDon, "Đơn đã kê"));
        panelThongKe.add(taoTheThongKe("CT", "Hoàn thành / Cấp", nhanSoDaCapThuoc, "Đã giao thuốc"));
        panelThongKe.add(taoTheThongKe("CL", "Chờ lấy thuốc", nhanSoChoLayThuoc, "Cần xử lý"));
        panelThongKe.add(taoTheThongKe("TH", "Tổng loại thuốc", nhanTongSoThuoc, "Trong các đơn thuốc"));
    }

    private PanelBoTron taoTheThongKe(String bieuTuong, String tieuDe, JLabel nhanSo, String moTa) {
        PanelBoTron the = new PanelBoTron(new BorderLayout(14, 0), BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.setBorder(new EmptyBorder(0, 18, 0, 12));
        PanelBoTron khungBieuTuong = new PanelBoTron(new GridBagLayout(), 10, RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        khungBieuTuong.setPreferredSize(new Dimension(40, 40));
        JLabel nhanBieuTuong = new JLabel(bieuTuong); nhanBieuTuong.setFont(RangBuocGiaoDien.fontDam(13)); nhanBieuTuong.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        khungBieuTuong.add(nhanBieuTuong);
        JPanel boBieuTuong = new JPanel(new GridBagLayout()); boBieuTuong.setOpaque(false); boBieuTuong.add(khungBieuTuong);
        
        JLabel nhanTieuDeThe = new JLabel(tieuDe); nhanTieuDeThe.setFont(RangBuocGiaoDien.fontThuong(12)); nhanTieuDeThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        nhanSo.setFont(RangBuocGiaoDien.fontDam(26)); nhanSo.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel nhanMoTaThe = new JLabel(moTa); nhanMoTaThe.setFont(RangBuocGiaoDien.fontThuong(12)); nhanMoTaThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        
        JPanel dongSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); dongSo.setOpaque(false); dongSo.add(nhanSo); dongSo.add(nhanMoTaThe);
        JPanel panelChu = new JPanel(); panelChu.setOpaque(false); panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        nhanTieuDeThe.setAlignmentX(Component.LEFT_ALIGNMENT); dongSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelChu.add(Box.createVerticalGlue()); panelChu.add(nhanTieuDeThe); panelChu.add(dongSo); panelChu.add(Box.createVerticalGlue());
        
        the.add(boBieuTuong, BorderLayout.WEST); the.add(panelChu, BorderLayout.CENTER);
        return the;
    }

    private void khoiTaoTheBang() {
        panelTheBang = new PanelBoTron(new BorderLayout(), BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelTheBang.setBorder(new EmptyBorder(1, 1, 1, 1));
        khoiTaoThanhCongCu(); khoiTaoBang(); khoiTaoPhanTrang();
        panelTheBang.add(panelCongCu, BorderLayout.NORTH); panelTheBang.add(thanhCuonBang, BorderLayout.CENTER); panelTheBang.add(panelPhanTrang, BorderLayout.SOUTH);
    }

    private void khoiTaoThanhCongCu() {
        panelCongCu = new JPanel(); panelCongCu.setOpaque(false); panelCongCu.setLayout(new BoxLayout(panelCongCu, BoxLayout.X_AXIS)); panelCongCu.setBorder(new EmptyBorder(16, 20, 16, 20));

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã đơn, thú cưng, bác sĩ...");
        txtTimKiem.setPreferredSize(new Dimension(280, CHIEU_CAO_O_NHAP)); txtTimKiem.setMaximumSize(new Dimension(280, CHIEU_CAO_O_NHAP));

        cboTrangThai = taoOChon(dsTrangThaiLoc, 160); 
        cboThoiGian = taoOChon(dsThoiGianLoc, 160);
        nutChonKhoangNgay = taoNutLienKet("Khoảng ngày", RangBuocGiaoDien.MAU_CHU_DAO);
        
        nutXemChiTiet = taoNutLienKet("Xem chi tiết", RangBuocGiaoDien.MAU_CHU_DAO);
        nutChinhSua = taoNutLienKet("Chỉnh sửa", RangBuocGiaoDien.MAU_CHU_DAO);
        nutXoa = taoNutLienKet("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
        nutXuatThuoc = taoNutLienKet("Xác nhận xuất thuốc", RangBuocGiaoDien.MAU_THANH_CONG_CHU);
        
        nutXemChiTiet.setEnabled(false); nutChinhSua.setEnabled(false); nutXoa.setEnabled(false); nutXuatThuoc.setEnabled(false);

        nutXuatDanhSach = new NutBoTron("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(140, CHIEU_CAO_O_NHAP)); nutXuatDanhSach.setMaximumSize(new Dimension(140, CHIEU_CAO_O_NHAP));

        panelCongCu.add(txtTimKiem); panelCongCu.add(Box.createHorizontalStrut(12)); panelCongCu.add(cboTrangThai); panelCongCu.add(Box.createHorizontalStrut(12)); panelCongCu.add(cboThoiGian);
        panelCongCu.add(Box.createHorizontalStrut(8)); panelCongCu.add(nutChonKhoangNgay); panelCongCu.add(Box.createHorizontalGlue());
        
        panelCongCu.add(nutXemChiTiet); panelCongCu.add(Box.createHorizontalStrut(14)); 
        panelCongCu.add(nutChinhSua); panelCongCu.add(Box.createHorizontalStrut(14)); 
        panelCongCu.add(nutXuatThuoc); panelCongCu.add(Box.createHorizontalStrut(14)); // Đã thêm nút Xuất thuốc vào giao diện
        panelCongCu.add(nutXoa); panelCongCu.add(Box.createHorizontalStrut(24)); 
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
         bangDonThuoc = new TableMau(tieuDeCot, new Object[0][tieuDeCot.length]);
         bangDonThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         int cotTrangThai = tieuDeCot.length - 1;
         int[] doRongCot = { 100, 120, 80, 150, 140, 100, 160, 130 };
         for (int i = 0; i < doRongCot.length; i++) bangDonThuoc.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
         bangDonThuoc.getColumnModel().getColumn(cotTrangThai).setCellRenderer(new OTrangThai(bangDonThuoc));
         bangDonThuoc.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) capNhatTrangThaiNutHanhDong(); });
         thanhCuonBang = new JScrollPane(bangDonThuoc);
         thanhCuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
         thanhCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
         thanhCuonBang.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void khoiTaoPhanTrang() {
        panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setPreferredSize(new Dimension(0, CHIEU_CAO_THANH_PHAN_TRANG));
        panelPhanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(0, 20, 0, 20)));
        nhanThongTinTrang = new JLabel();
        nhanThongTinTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
    }

    private void caiDatSuKien() {
        nutKeDonThuoc.addActionListener(e -> moFormDonThuoc(null));
        nutXemChiTiet.addActionListener(e -> xemDongDaChon());
        nutChinhSua.addActionListener(e -> chinhSuaDongDaChon());
        nutXoa.addActionListener(e -> xoaDongDaChon());
        nutXuatDanhSach.addActionListener(e -> xuatDanhSach());
        nutChonKhoangNgay.addActionListener(e -> chonKhoangNgay());
        cboTrangThai.addActionListener(e -> locDuLieu());
        cboThoiGian.addActionListener(e -> locDuLieu());
        
        // Sự kiện xuất thuốc & tự động tạo hóa đơn
        nutXuatThuoc.addActionListener(e -> xuatThuocVaTaoHoaDon());
        
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { locDuLieu(); }
        });
        bangDonThuoc.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangDonThuoc.getSelectedRowCount() == 1) xemDongDaChon();
            }
        });
    }

    public void loadDataToTable() {
        try {
            danhSachDonThuoc = donThuocDAO.getAllDonThuoc();
            locDuLieu(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDuLieu() {
        String keyword = txtTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        String status = String.valueOf(cboTrangThai.getSelectedItem());
        String time = String.valueOf(cboThoiGian.getSelectedItem());
        
        LocalDate tu = null;
        LocalDate den = null;
        LocalDate homNay = LocalDate.now();
        
        if ("Tùy chỉnh khoảng ngày...".equals(time)) {
            tu = tuNgay;
            den = denNgay;
        } else {
            tuNgay = null; denNgay = null; 
            if ("Hôm nay".equals(time)) { 
                tu = homNay; den = homNay; 
            } else if ("Tuần này".equals(time)) {
                tu = homNay.with(java.time.DayOfWeek.MONDAY);
                den = homNay.with(java.time.DayOfWeek.SUNDAY);
            } else if (time.startsWith("Tháng ")) {
                try {
                    int month = Integer.parseInt(time.replace("Tháng ", ""));
                    tu = LocalDate.of(homNay.getYear(), month, 1);
                    den = tu.with(TemporalAdjusters.lastDayOfMonth());
                } catch (Exception e) {
                    tu = null; den = null;
                }
            }
        }

        daLoc.clear();
        for (DonThuoc_entity don : danhSachDonThuoc) {
            LocalDate ngay = don.getNgayKeDon() == null ? null : don.getNgayKeDon().toLocalDate();
            
            boolean matchedKeyword = keyword.isEmpty() 
                    || value(don.getMaDonThuoc()).toLowerCase(Locale.ROOT).contains(keyword) 
                    || value(don.getMaHSSK()).toLowerCase(Locale.ROOT).contains(keyword) 
                    || value(don.getTenThuCung()).toLowerCase(Locale.ROOT).contains(keyword) 
                    || value(don.getTenBacSi()).toLowerCase(Locale.ROOT).contains(keyword);
                    
            boolean matchedStatus = status == null || status.startsWith("Trạng thái:") || status.equals(don.getTrangThai());
            
            boolean matchedDate = true;
            if (tu != null || den != null) {
                if (ngay == null) {
                    matchedDate = false;
                } else {
                    matchedDate = (tu == null || !ngay.isBefore(tu)) && (den == null || !ngay.isAfter(den));
                }
            }
            
            if (matchedKeyword && matchedStatus && matchedDate) {
                daLoc.add(don);
            }
        }
        
        capNhatBang();
        capNhatThongKe(); 
    }

    private void capNhatBang() {
        int tong = daLoc.size();
        DefaultTableModel model = (DefaultTableModel) bangDonThuoc.getModel();
        model.setRowCount(0);
        
        for (int i = 0; i < tong; i++) {
            DonThuoc_entity don = daLoc.get(i);
            model.addRow(new Object[] { don.getMaDonThuoc(), value(don.getTenThuCung()) + " (" + don.getMaTC() + ")", don.getMaHSSK(), don.getTenBacSi(), don.getNgayKeDon() == null ? "" : DINH_DANG_NGAY_GIO.format(don.getNgayKeDon()), don.getSoLoaiThuoc(), value(don.getGhiChu()), don.getTrangThai() });
        }
        bangDonThuoc.clearSelection(); capNhatTrangThaiNutHanhDong();
        nhanThongTinTrang.setText(tong == 0 ? "Không có đơn thuốc phù hợp" : "Hiển thị tất cả " + tong + " đơn thuốc");
    }

    private void capNhatThongKe() {
        int daCap = 0, choLay = 0, loaiThuoc = 0;
        for (DonThuoc_entity don : daLoc) {
            if ("Đã cấp thuốc".equals(don.getTrangThai()) || "Hoàn thành".equals(don.getTrangThai())) daCap++;
            else if ("Chờ lấy thuốc".equals(don.getTrangThai())) choLay++;
            loaiThuoc += don.getSoLoaiThuoc();
        }
        nhanSoTongDon.setText(String.valueOf(daLoc.size())); nhanSoDaCapThuoc.setText(String.valueOf(daCap));
        nhanSoChoLayThuoc.setText(String.valueOf(choLay)); nhanTongSoThuoc.setText(String.valueOf(loaiThuoc));
    }

    private String value(String text) { return text == null ? "" : text; }

    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8)); form.add(new JLabel("Từ ngày (dd/MM/yyyy):")); form.add(tu); form.add(new JLabel("Đến ngày (dd/MM/yyyy):")); form.add(den);
        
        if (JOptionPane.showConfirmDialog(this, form, "Lọc theo khoảng ngày", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            LocalDate tempTu = tu.getText().trim().isEmpty() ? null : LocalDate.parse(tu.getText().trim(), DINH_DANG_NGAY);
            LocalDate tempDen = den.getText().trim().isEmpty() ? null : LocalDate.parse(den.getText().trim(), DINH_DANG_NGAY);
            if (tempTu != null && tempDen != null && tempTu.isAfter(tempDen)) throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
            
            tuNgay = tempTu; 
            denNgay = tempDen;
            cboThoiGian.setSelectedItem("Tùy chỉnh khoảng ngày..."); 
        } catch (DateTimeParseException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Ngày nhập không hợp lệ: " + e.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String layMaDonDaChon() {
        if (bangDonThuoc.getSelectedRowCount() != 1) return null;
        int row = bangDonThuoc.getSelectedRow();
        return row < 0 ? null : String.valueOf(bangDonThuoc.getModel().getValueAt(bangDonThuoc.convertRowIndexToModel(row), 0));
    }

    private void moFormDonThuoc(DonThuoc_entity donHienTai) {
        try {
            List<HoSoSucKhoe_entity> hoSoList = hoSoSucKhoeDAO.getAllHoSoSucKhoe();
            List<ThuCung_entity> thuCungList = thuCungDAO.getAllThuCung();
            List<BacSi_entity> bacSiList = bacSiDAO.getAll();
            List<Thuoc_entity> thuocList = chiTietDAO.getAllThuoc();
            if (hoSoList.isEmpty() || thuCungList.isEmpty() || bacSiList.isEmpty()) { JOptionPane.showMessageDialog(this, "Thiếu dữ liệu gốc.", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
            
            JTextField txtMa = new JTextField(donHienTai == null ? donThuocDAO.getMaTiepTheo() : donHienTai.getMaDonThuoc()); txtMa.setEditable(false);
            JComboBox<HoSoSucKhoe_entity> cboHoSo = new JComboBox<>(hoSoList.toArray(new HoSoSucKhoe_entity[0]));
            cboHoSo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object item, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    if (item instanceof HoSoSucKhoe_entity) setText(((HoSoSucKhoe_entity) item).getMaHSSK() + " - " + value(((HoSoSucKhoe_entity) item).getTenThuCung()));
                    return this;
                }
            });
            if (donHienTai != null) {
                for (int i = 0; i < cboHoSo.getItemCount(); i++) if (donHienTai.getMaHSSK().equals(cboHoSo.getItemAt(i).getMaHSSK())) { cboHoSo.setSelectedIndex(i); break; }
            }
            
            JTextField txtNgay = new JTextField(donHienTai == null ? DINH_DANG_NGAY_GIO.format(LocalDateTime.now().withSecond(0).withNano(0)) : DINH_DANG_NGAY_GIO.format(donHienTai.getNgayKeDon()));
            JTextField txtGhiChu = new JTextField(donHienTai == null ? "" : value(donHienTai.getGhiChu()));
            
            // Xử lý cứng trạng thái dựa theo việc tạo mới hay cập nhật
            JComboBox<String> cboTrangThaiForm = new JComboBox<>(new String[] { "Chờ lấy thuốc", "Đã cấp thuốc", "Chờ thanh toán", "Hoàn thành", "Đã hủy" });
            if (donHienTai != null) {
                cboTrangThaiForm.setSelectedItem(donHienTai.getTrangThai());
                cboTrangThaiForm.setEnabled(true);
            } else {
                cboTrangThaiForm.setSelectedItem("Chờ lấy thuốc"); // Ràng buộc mặc định cho đơn thuốc mới kê
                cboTrangThaiForm.setEnabled(false); 
            }

            DefaultTableModel chiTietModel = new DefaultTableModel(new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Liều lượng" }, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            if (donHienTai != null) {
                for (ChiTietDonThuoc_entity item : chiTietDAO.getChiTietTheoMaDon(donHienTai.getMaDonThuoc())) chiTietModel.addRow(new Object[] { item.getMaThuoc(), item.getTenThuoc(), item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
            }
            JTable bangChiTiet = new JTable(chiTietModel); bangChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JButton nutThemThuoc = new JButton("Thêm thuốc"); nutThemThuoc.addActionListener(e -> themChiTietThuoc(chiTietModel, thuocList));
            JButton nutXoaThuoc = new JButton("Xóa thuốc"); nutXoaThuoc.addActionListener(e -> { int r = bangChiTiet.getSelectedRow(); if (r >= 0) chiTietModel.removeRow(r); });
            JPanel panelThuocNut = new JPanel(new FlowLayout(FlowLayout.LEFT)); panelThuocNut.add(nutThemThuoc); panelThuocNut.add(nutXoaThuoc);
            JPanel panelThuoc = new JPanel(new BorderLayout(0, 6)); panelThuoc.add(new JScrollPane(bangChiTiet), BorderLayout.CENTER); panelThuoc.add(panelThuocNut, BorderLayout.SOUTH); panelThuoc.setPreferredSize(new Dimension(560, 190));

            JPanel header = new JPanel(new GridLayout(0, 2, 8, 8)); header.add(new JLabel("Mã đơn thuốc:")); header.add(txtMa); header.add(new JLabel("Hồ sơ khám:")); header.add(cboHoSo); header.add(new JLabel("Ngày kê đơn:")); header.add(txtNgay); header.add(new JLabel("Ghi chú:")); header.add(txtGhiChu); header.add(new JLabel("Trạng thái:")); header.add(cboTrangThaiForm);
            JPanel form = new JPanel(new BorderLayout(0, 8)); form.add(header, BorderLayout.NORTH); form.add(panelThuoc, BorderLayout.CENTER);
            
            if (JOptionPane.showConfirmDialog(this, form, donHienTai == null ? "Kê đơn thuốc" : "Cập nhật", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            
            LocalDateTime ngay = LocalDateTime.parse(txtNgay.getText().trim(), DINH_DANG_NGAY_GIO);
            HoSoSucKhoe_entity hs = (HoSoSucKhoe_entity) cboHoSo.getSelectedItem();
            List<ChiTietDonThuoc_entity> chiTiet = new ArrayList<>();
            for (int i = 0; i < chiTietModel.getRowCount(); i++) chiTiet.add(new ChiTietDonThuoc_entity(null, txtMa.getText(), String.valueOf(chiTietModel.getValueAt(i, 0)), new BigDecimal(String.valueOf(chiTietModel.getValueAt(i, 3))), new BigDecimal(String.valueOf(chiTietModel.getValueAt(i, 4)))));
            if (chiTiet.isEmpty()) throw new IllegalArgumentException("Cần ít nhất một thuốc.");
            
            DonThuoc_entity don = new DonThuoc_entity(txtMa.getText(), hs.getMaTC(), hs.getMaBacSi(), hs.getMaHSSK(), ngay, txtGhiChu.getText().trim(), String.valueOf(cboTrangThaiForm.getSelectedItem()));
            if (donHienTai == null ? donThuocDAO.createDonThuoc(don, chiTiet) : donThuocDAO.updateDonThuoc(don, chiTiet)) {
                JOptionPane.showMessageDialog(this, "Lưu thành công."); loadDataToTable();
            } else JOptionPane.showMessageDialog(this, "Lưu thất bại.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private void themChiTietThuoc(DefaultTableModel model, List<Thuoc_entity> ds) {
        if (ds.isEmpty()) return;
        JComboBox<Thuoc_entity> cboThuoc = new JComboBox<>(ds.toArray(new Thuoc_entity[0]));
        JTextField txtSL = new JTextField("1"), txtLieu = new JTextField("1");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8)); form.add(new JLabel("Thuốc:")); form.add(cboThuoc); form.add(new JLabel("Số lượng:")); form.add(txtSL); form.add(new JLabel("Liều lượng:")); form.add(txtLieu);
        if (JOptionPane.showConfirmDialog(this, form, "Thêm thuốc", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Thuoc_entity thuoc = (Thuoc_entity) cboThuoc.getSelectedItem(); BigDecimal sl = new BigDecimal(txtSL.getText().trim()), lieu = new BigDecimal(txtLieu.getText().trim());
            for (int i = 0; i < model.getRowCount(); i++) if (thuoc.getMaThuoc().equals(model.getValueAt(i, 0))) throw new IllegalArgumentException("Đã có trong đơn.");
            model.addRow(new Object[] { thuoc.getMaThuoc(), thuoc.getTenThuoc(), thuoc.getDonViTinh(), sl, lieu });
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE); }
    }

    private void capNhatTrangThaiNutHanhDong() {
        boolean ch = bangDonThuoc.getSelectedRowCount() == 1; 
        nutXemChiTiet.setEnabled(ch); nutChinhSua.setEnabled(ch); nutXoa.setEnabled(ch);
        
        // Điều kiện kích hoạt nút Xuất Thuốc (Chỉ bật khi Đơn đang ở trạng thái "Chờ lấy thuốc")
        if (ch) {
            int row = bangDonThuoc.getSelectedRow();
            String trangThai = String.valueOf(bangDonThuoc.getModel().getValueAt(row, 7));
            nutXuatThuoc.setEnabled(trangThai.equals("Chờ lấy thuốc"));
        } else {
            nutXuatThuoc.setEnabled(false);
        }
    }

    private void xemDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        try {
            DonThuoc_entity don = donThuocDAO.getDonThuocTheoMa(ma); if (don == null) return;
            DefaultTableModel model = new DefaultTableModel(new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Liều lượng" }, 0);
            for (ChiTietDonThuoc_entity item : donThuocDAO.getChiTietDonThuoc(ma)) model.addRow(new Object[] { item.getMaThuoc(), item.getTenThuoc(), item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
            JPanel panel = new JPanel(new BorderLayout(0, 8)); panel.add(new JLabel("Đơn " + don.getMaDonThuoc() + " | " + value(don.getTenThuCung()) + " | " + don.getTrangThai()), BorderLayout.NORTH); panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER); panel.setPreferredSize(new Dimension(620, 280));
            JOptionPane.showMessageDialog(this, panel, "Chi tiết", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private void chinhSuaDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        try {
            DonThuoc_entity don = donThuocDAO.getDonThuocTheoMa(ma); if (don != null) moFormDonThuoc(don);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private void xoaDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa đơn thuốc \"" + ma + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                if (donThuocDAO.deleteDonThuoc(ma)) { JOptionPane.showMessageDialog(this, "Đã xóa."); loadDataToTable(); } 
                else JOptionPane.showMessageDialog(this, "Lỗi xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // Nghiệp vụ XUẤT THUỐC VÀ TẠO HÓA ĐƠN tự động
    private void xuatThuocVaTaoHoaDon() {
        String maDon = layMaDonDaChon(); 
        if (maDon == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận xuất thuốc cho đơn \"" + maDon + "\"?\nThao tác này sẽ đổi trạng thái thành 'Hoàn thành' và tự động tạo 1 Hóa Đơn có thú cưng và chi tiết tiền thuốc.", 
            "Xác nhận xuất thuốc", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        java.sql.Connection con = null;
        try {
            // 1. Lấy thông tin Đơn Thuốc và Chi Tiết đơn thuốc
            DonThuoc_entity don = donThuocDAO.getDonThuocTheoMa(maDon);
            if (don == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin đơn thuốc " + maDon, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<ChiTietDonThuoc_entity> chiTietList = donThuocDAO.getChiTietDonThuoc(maDon);
            if (chiTietList == null || chiTietList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Đơn thuốc này không có chi tiết thuốc nào!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Lấy thông tin Khách Hàng (Chủ nuôi) từ Thú Cưng
            ThuCung_entity tc = thuCungDAO.getThuCungTheoMa(don.getMaTC());
            if (tc == null || tc.getMaKH() == null) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy thông tin Thú cưng / Khách hàng để tạo hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Lấy Nhân Viên đang thực hiện thao tác
            entity.NhanVien_entity nv = GUI.HoaDon.LapHoaDon_gui.layNhanVienDangNhapHienTai();
            if (nv == null) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không xác định được nhân viên đang đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dao.HoaDon_Dao hdDAO = new dao.HoaDon_Dao();
            dao.ChiTietHoaDon_DAO cthdDAO = new dao.ChiTietHoaDon_DAO();
            dao.VatTu_DAO vtDAO = new dao.VatTu_DAO();

            // Mở kết nối transaction
            con = connectDB.ConnectDB.getCon();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false);

            // 4. Tạo Hóa Đơn mới trạng thái Chờ thanh toán và GẮN THÚ CƯNG
            int soHD = hdDAO.laySoHoaDonLonNhat() + 1;
            String maHD = String.format("HD%03d", soHD);

            entity.HoaDon_entity hd = new entity.HoaDon_entity(
                    maHD,
                    LocalDate.now(),
                    "Chờ thanh toán",
                    tc.getMaKH(),
                    nv,
                    null, null, null
            );
            hd.setMaTC(tc.getMaTC()); // GẮN MÃ THÚ CƯNG CHO HÓA ĐƠN

            if (!hdDAO.themHD(con, hd)) {
                throw new java.sql.SQLException("Không thể lưu thông tin Hóa Đơn " + maHD);
            }

            // 5. Thêm Chi Tiết Hóa Đơn dựa trên Chi Tiết Đơn Thuốc (tìm đúng Vật Tư liên kết qua maThuoc)
            int ctIndex = 1;
            double tongTienThuoc = 0.0;
            for (ChiTietDonThuoc_entity ctDon : chiTietList) {
                // Tìm vật tư kho liên kết với mã thuốc (ví dụ maThuoc TH005 -> maVT VT010)
                entity.VatTu_entity vt = vtDAO.getVatTuTheoMaThuoc(ctDon.getMaThuoc());
                if (vt == null) {
                    vt = vtDAO.getVatTuTheoMa(ctDon.getMaThuoc());
                }

                if (vt == null) {
                    throw new java.sql.SQLException("Không tìm thấy mặt hàng trong kho cho thuốc: " 
                            + ctDon.getTenThuoc() + " (Mã: " + ctDon.getMaThuoc() + ")");
                }

                double donGia = vt.getGiaBanCoBan();
                int soLuong = ctDon.getSoLuong().intValue();
                if (soLuong <= 0) soLuong = 1;

                String maCTHD = "CT" + maHD + String.format("%02d", ctIndex++);
                entity.ChiTietHoaDon_entity cthd = new entity.ChiTietHoaDon_entity(
                        maCTHD,
                        hd,
                        null, // Không có dịch vụ
                        vt,   // Thuốc đóng vai trò vật tư kho
                        nv,   // Nhân viên xử lý
                        null, // Không truyền mã Bác Sĩ vì xuất thuốc do thu ngân/nhân viên làm
                        soLuong,
                        donGia
                );
                cthd.setMaCTDT(ctDon.getMaCTDT()); // GẮN MÃ CHI TIẾT ĐƠN THUỐC ĐỂ KHỚP RÀNG BUỘC CSDL

                if (!cthdDAO.createChiTietHoaDon(con, cthd)) {
                    throw new java.sql.SQLException("Không thể lưu chi tiết hóa đơn " + maCTHD + " cho thuốc: " + vt.getTenVT());
                }
                tongTienThuoc += donGia * soLuong;
            }

            // 6. Đổi trạng thái Đơn Thuốc thành "Hoàn thành"
            if (!donThuocDAO.capNhatTrangThai(maDon, "Hoàn thành")) {
                throw new java.sql.SQLException("Không thể cập nhật trạng thái đơn thuốc sang 'Hoàn thành'!");
            }

            // Commit toàn bộ giao dịch
            con.commit();

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0 ₫");
            JOptionPane.showMessageDialog(this,
                    "Đã xuất đơn thuốc thành công!\n"
                    + "Hóa đơn [" + maHD + "] đã được tạo tự động cho thú cưng " + tc.getTenTC() + ".\n"
                    + "Tổng tiền thuốc: " + df.format(tongTienThuoc),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            loadDataToTable();

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (java.sql.SQLException rEx) {
                    rEx.printStackTrace();
                }
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi quá trình xuất thuốc: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (java.sql.SQLException cEx) {
                    cEx.printStackTrace();
                }
            }
        }
    }

    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new java.io.File("don-thuoc.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile(); if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff'); DefaultTableModel m = (DefaultTableModel) bangDonThuoc.getModel();
            for (int r = -1; r < m.getRowCount(); r++) {
                for (int c = 0; c < m.getColumnCount(); c++) { if (c > 0) writer.write(','); writer.write("\"" + (r < 0 ? bangDonThuoc.getColumnName(c) : String.valueOf(m.getValueAt(r, c))).replace("\"", "\"\"") + "\""); }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Xuất thành công.");
        } catch (IOException e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private JComboBox<String> taoOChon(String[] ds, int rong) { JComboBox<String> o = new JComboBox<>(ds); o.setFont(RangBuocGiaoDien.fontThuong(13)); o.setBackground(RangBuocGiaoDien.MAU_NEN_THE); o.setPreferredSize(new Dimension(rong, CHIEU_CAO_O_NHAP)); return o; }
    private JButton taoNutLienKet(String chu, Color mau) { JButton nut = new JButton(chu); nut.setFont(RangBuocGiaoDien.fontThuong(12)); nut.setForeground(mau); nut.setContentAreaFilled(false); nut.setBorderPainted(false); nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return nut; }
    private static class OTrangThai extends JPanel implements TableCellRenderer {
        private final JTable bang; private String chu = ""; private Color mauNen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN, mauChu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
        OTrangThai(JTable bang) { this.bang = bang; }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            chu = v == null ? "" : v.toString();
            if (chu.equals("Chờ lấy thuốc") || chu.equals("Chờ thanh toán")) { mauNen = RangBuocGiaoDien.MAU_CANH_BAO_NEN; mauChu = RangBuocGiaoDien.MAU_CANH_BAO_CHU; }
            else if (chu.equals("Đã cấp thuốc") || chu.equals("Hoàn thành")) { mauNen = RangBuocGiaoDien.MAU_THONG_TIN_NEN; mauChu = RangBuocGiaoDien.MAU_THONG_TIN_CHU; }
            else if (chu.equals("Đã hủy")) { mauNen = RangBuocGiaoDien.MAU_NGUY_HIEM_NEN; mauChu = RangBuocGiaoDien.MAU_NGUY_HIEM_CHU; }
            setBackground(s ? bang.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : RangBuocGiaoDien.MAU_TIEU_DE_BANG)); return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setFont(RangBuocGiaoDien.fontDam(12)); FontMetrics fm = g2.getFontMetrics();
            int h = 24, w = fm.stringWidth(chu) + 28, x = 12, y = (getHeight() - h) / 2;
            g2.setColor(mauNen); g2.fillRoundRect(x, y, w, h, h, h); g2.setColor(mauChu); g2.fillOval(x + 9, y + h / 2 - 3, 6, 6);
            g2.drawString(chu, x + 20, y + (h - fm.getHeight()) / 2 + fm.getAscent()); g2.dispose();
        }
    }
}