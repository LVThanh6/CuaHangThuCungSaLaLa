package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import GUI.ThuCung.HoSoThuCungGUI.NutBoTron;
import GUI.ThuCung.HoSoThuCungGUI.OTimKiemGoiY;
import GUI.ThuCung.HoSoThuCungGUI.PanelBoTron;
import Service.ThuCung.DonThuocService;
import entity.BacSi.BacSi_entity;
import entity.ThuCung.ChiTietDonThuoc_entity;
import entity.ThuCung.DonThuoc_entity;
import entity.ThuCung.HoSoSucKhoe_entity;
import entity.ThuCung.ThuCung_entity;
import entity.ThuCung.Thuoc_entity;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    
    private final DonThuocService service = new DonThuocService();
    
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

    //Hàm tổng quản lý việc gắn các thành phần UI vào màn hình
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

    //Tạo đường dẫn, tiêu đề trang và nút "+ Kê đơn thuốc".
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

    //khoiTaoThongKe() & taoTheThongKe(): Dựng 4 thẻ thống kê số lượng đơn (Tổng đơn, Hoàn thành, Chờ lấy, Tổng loại thuốc).
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

    //Tạo khung bo tròn bao bọc thanh công cụ và bảng.
    private void khoiTaoTheBang() {
        panelTheBang = new PanelBoTron(new BorderLayout(), BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelTheBang.setBorder(new EmptyBorder(1, 1, 1, 1));
        khoiTaoThanhCongCu(); khoiTaoBang(); khoiTaoPhanTrang();
        panelTheBang.add(panelCongCu, BorderLayout.NORTH); panelTheBang.add(thanhCuonBang, BorderLayout.CENTER); panelTheBang.add(panelPhanTrang, BorderLayout.SOUTH);
    }
    
    //Tạo ô tìm kiếm, combobox lọc trạng thái/thời gian và các nút chức năng (Xem chi tiết, Chỉnh sửa, Xuất thuốc, Xóa, Xuất danh sách).
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

    //Thiết lập các cột, độ rộng cột và gắn bộ dựng giao diện dòng trạng thái.
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

    //Vẽ thanh thông tin tổng số lượng đơn thuốc ở phía dưới cùng
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

    //Gắn các Listener cho nút bấm, combobox và ô tìm kiếm)
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

    //Lấy dữ liệu đơn thuốc từ CSDL lên
    public void loadDataToTable() {
        try {
            danhSachDonThuoc = service.taiDanhSach();
            locDuLieu(); 
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Lọc danh sách theo từ khóa, trạng thái và mốc/khoảng thời gian.
    private void locDuLieu() {
        String time = String.valueOf(cboThoiGian.getSelectedItem());
        if (!"Tùy chỉnh khoảng ngày...".equals(time)) {
            tuNgay = null;
            denNgay = null;
        }
        daLoc = service.loc(danhSachDonThuoc, txtTimKiem.getText(),
                String.valueOf(cboTrangThai.getSelectedItem()), time, tuNgay, denNgay, LocalDate.now());
        
        capNhatBang();
        capNhatThongKe(); 
    }
    
    //capNhatBang() & capNhatThongKe(): Đổ dữ liệu đã lọc vào bảng và cập nhật con số ở các thẻ thống kê.
    private void capNhatBang() {
        int tong = daLoc.size();
        DefaultTableModel model = (DefaultTableModel) bangDonThuoc.getModel();
        model.setRowCount(0);
        
        for (int i = 0; i < tong; i++) {
            DonThuoc_entity don = daLoc.get(i);
            model.addRow(new Object[] { don.getMaDonThuoc(),
                    value(don.getTenThuCung()) + " (" + (don.getMaTC() != null ? don.getMaTC().getMaTC() : "") + ")",
                    don.getMaHSSK() != null ? don.getMaHSSK().getMaHSSK() : "",
                    don.getTenBacSi(), don.getNgayKeDon() == null ? "" : DINH_DANG_NGAY_GIO.format(don.getNgayKeDon()),
                    don.getSoLoaiThuoc(), value(don.getGhiChu()), don.getTrangThai() });
        }
        bangDonThuoc.clearSelection(); capNhatTrangThaiNutHanhDong();
        nhanThongTinTrang.setText(tong == 0 ? "Không có đơn thuốc phù hợp" : "Hiển thị tất cả " + tong + " đơn thuốc");
    }

    private void capNhatThongKe() {
        DonThuocService.ThongKe thongKe = service.thongKe(daLoc);
        nhanSoTongDon.setText(String.valueOf(thongKe.tong));
        nhanSoDaCapThuoc.setText(String.valueOf(thongKe.daCap));
        nhanSoChoLayThuoc.setText(String.valueOf(thongKe.choLay));
        nhanTongSoThuoc.setText(String.valueOf(thongKe.loaiThuoc));
    }
    
    

    private String value(String text) { 
    	return text == null ? "" : text; 
    }

    //Mở hộp thoại chọn khoảng ngày tùy chỉnh (Từ ngày... Đến ngày...).
    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8)); form.add(new JLabel("Từ ngày (dd/MM/yyyy):")); form.add(tu); form.add(new JLabel("Đến ngày (dd/MM/yyyy):")); form.add(den);
        
        if (JOptionPane.showConfirmDialog(this, form, "Lọc theo khoảng ngày", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            DonThuocService.KhoangNgay khoang = service.khoangNgay(tu.getText(), den.getText());
            tuNgay = khoang.tuNgay;
            denNgay = khoang.denNgay;
            cboThoiGian.setSelectedItem("Tùy chỉnh khoảng ngày..."); 
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Ngày nhập không hợp lệ: " + e.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String layMaDonDaChon() {
        if (bangDonThuoc.getSelectedRowCount() != 1) return null;
        int row = bangDonThuoc.getSelectedRow();
        return row < 0 ? null : String.valueOf(bangDonThuoc.getModel().getValueAt(bangDonThuoc.convertRowIndexToModel(row), 0));
    }

    //Mở Form popup để kê đơn thuốc mới hoặc chỉnh sửa đơn cũ.
    private void moFormDonThuoc(DonThuoc_entity donHienTai) {
        try {
            DonThuocService.DuLieuForm duLieu = service.taiDuLieuForm();
            List<HoSoSucKhoe_entity> hoSoList = duLieu.hoSo;
            List<ThuCung_entity> thuCungList = duLieu.thuCung;
            List<BacSi_entity> bacSiList = duLieu.bacSi;
            List<Thuoc_entity> thuocList = duLieu.thuoc;
            
            JTextField txtMa = new JTextField(donHienTai == null ? service.maTiepTheo() : donHienTai.getMaDonThuoc()); txtMa.setEditable(false);
            JComboBox<HoSoSucKhoe_entity> cboHoSo = new JComboBox<>(hoSoList.toArray(new HoSoSucKhoe_entity[0]));
            cboHoSo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object item, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    if (item instanceof HoSoSucKhoe_entity) setText(((HoSoSucKhoe_entity) item).getMaHSSK() + " - " + value(((HoSoSucKhoe_entity) item).getTenThuCung()));
                    return this;
                }
            });
            if (donHienTai != null) {
                for (int i = 0; i < cboHoSo.getItemCount(); i++) {
                    if (donHienTai.getMaHSSK() != null
                            && donHienTai.getMaHSSK().getMaHSSK().equals(cboHoSo.getItemAt(i).getMaHSSK())) {
                        cboHoSo.setSelectedIndex(i);
                        break;
                    }
                }
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
                for (ChiTietDonThuoc_entity item : service.taiChiTiet(donHienTai.getMaDonThuoc())) chiTietModel.addRow(new Object[] { item.getMaThuoc() != null ? item.getMaThuoc().getMaThuoc() : "", item.getTenThuoc(), item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
            }
            JTable bangChiTiet = new JTable(chiTietModel); bangChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JButton nutThemThuoc = new JButton("Thêm thuốc"); nutThemThuoc.addActionListener(e -> themChiTietThuoc(chiTietModel, thuocList));
            JButton nutXoaThuoc = new JButton("Xóa thuốc"); nutXoaThuoc.addActionListener(e -> { int r = bangChiTiet.getSelectedRow(); if (r >= 0) chiTietModel.removeRow(r); });
            JPanel panelThuocNut = new JPanel(new FlowLayout(FlowLayout.LEFT)); panelThuocNut.add(nutThemThuoc); panelThuocNut.add(nutXoaThuoc);
            JPanel panelThuoc = new JPanel(new BorderLayout(0, 6)); panelThuoc.add(new JScrollPane(bangChiTiet), BorderLayout.CENTER); panelThuoc.add(panelThuocNut, BorderLayout.SOUTH); panelThuoc.setPreferredSize(new Dimension(560, 190));

            JPanel header = new JPanel(new GridLayout(0, 2, 8, 8)); header.add(new JLabel("Mã đơn thuốc:")); header.add(txtMa); header.add(new JLabel("Hồ sơ khám:")); header.add(cboHoSo); header.add(new JLabel("Ngày kê đơn:")); header.add(txtNgay); header.add(new JLabel("Ghi chú:")); header.add(txtGhiChu); header.add(new JLabel("Trạng thái:")); header.add(cboTrangThaiForm);
            JPanel form = new JPanel(new BorderLayout(0, 8)); form.add(header, BorderLayout.NORTH); form.add(panelThuoc, BorderLayout.CENTER);
            
            if (JOptionPane.showConfirmDialog(this, form, donHienTai == null ? "Kê đơn thuốc" : "Cập nhật", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            
            HoSoSucKhoe_entity hs = (HoSoSucKhoe_entity) cboHoSo.getSelectedItem();
            List<ChiTietDonThuoc_entity> chiTiet = new ArrayList<>();
            for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                Thuoc_entity thuoc = null;
                String maThuoc = String.valueOf(chiTietModel.getValueAt(i, 0));
                for (Thuoc_entity item : thuocList) {
                    if (item.getMaThuoc().equals(maThuoc)) {
                        thuoc = item;
                        break;
                    }
                }
                chiTiet.add(service.taoChiTiet(thuoc,
                        String.valueOf(chiTietModel.getValueAt(i, 3)),
                        String.valueOf(chiTietModel.getValueAt(i, 4)), txtMa.getText(),
                        new ArrayList<ChiTietDonThuoc_entity>()));
            }
            if (service.luu(new DonThuocService.ThongTinDonThuoc(
                    txtMa.getText(), donHienTai == null, hs, txtNgay.getText(),
                    txtGhiChu.getText(), String.valueOf(cboTrangThaiForm.getSelectedItem()), chiTiet))) {
                JOptionPane.showMessageDialog(this, "Lưu thành công."); loadDataToTable();
            } else JOptionPane.showMessageDialog(this, "Lưu thất bại.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    //Mở popup chọn thuốc, nhập số lượng, liều lượng để thêm vào bảng chi tiết của đơn thuốc.
    private void themChiTietThuoc(DefaultTableModel model, List<Thuoc_entity> ds) {
        if (ds.isEmpty()) return;
        JComboBox<Thuoc_entity> cboThuoc = new JComboBox<>(ds.toArray(new Thuoc_entity[0]));
        JTextField txtSL = new JTextField("1"), txtLieu = new JTextField("1");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8)); form.add(new JLabel("Thuốc:")); form.add(cboThuoc); form.add(new JLabel("Số lượng:")); form.add(txtSL); form.add(new JLabel("Liều lượng:")); form.add(txtLieu);
        if (JOptionPane.showConfirmDialog(this, form, "Thêm thuốc", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Thuoc_entity thuoc = (Thuoc_entity) cboThuoc.getSelectedItem();
            List<ChiTietDonThuoc_entity> dangCo = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String maThuoc = String.valueOf(model.getValueAt(i, 0));
                for (Thuoc_entity item : ds) {
                    if (item.getMaThuoc().equals(maThuoc)) {
                        dangCo.add(service.taoChiTiet(item,
                                String.valueOf(model.getValueAt(i, 3)),
                                String.valueOf(model.getValueAt(i, 4)), "", new ArrayList<ChiTietDonThuoc_entity>()));
                        break;
                    }
                }
            }
            ChiTietDonThuoc_entity chiTiet = service.taoChiTiet(
                    thuoc, txtSL.getText(), txtLieu.getText(), "", dangCo);
            model.addRow(new Object[] { thuoc.getMaThuoc(), thuoc.getTenThuoc(), thuoc.getDonViTinh(), chiTiet.getSoLuong(), chiTiet.getLieuLuong() });
        } catch (IllegalArgumentException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE); }
    }

    //(bật/tắt các nút Xem/Sửa/Xóa/Xuất thuốc tùy theo trạng thái đơn đang chọn).
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

    //Xem chi tiết đơn thuốc đã chọn (popup hiển thị thông tin chung + bảng chi tiết thuốc).
    private void xemDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        try {
            DonThuoc_entity don = service.timTheoMa(ma); if (don == null) return;
            DefaultTableModel model = new DefaultTableModel(new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Số lượng", "Liều lượng" }, 0);
            for (ChiTietDonThuoc_entity item : service.taiChiTietDon(ma)) model.addRow(new Object[] { item.getMaThuoc() != null ? item.getMaThuoc().getMaThuoc() : "", item.getTenThuoc(), item.getDonViTinh(), item.getSoLuong(), item.getLieuLuong() });
            JPanel panel = new JPanel(new BorderLayout(0, 8)); panel.add(new JLabel("Đơn " + don.getMaDonThuoc() + " | " + value(don.getTenThuCung()) + " | " + don.getTrangThai()), BorderLayout.NORTH); panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER); panel.setPreferredSize(new Dimension(620, 280));
            JOptionPane.showMessageDialog(this, panel, "Chi tiết", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | RuntimeException e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    //Chỉnh sửa đơn thuốc đã chọn 
    private void chinhSuaDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        try {
            DonThuoc_entity don = service.timTheoMa(ma); if (don != null) moFormDonThuoc(don);
        } catch (SQLException | RuntimeException e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    //Xóa đơn thuốc đã chọn (có xác nhận trước khi xóa).
    private void xoaDongDaChon() {
        String ma = layMaDonDaChon(); if (ma == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa đơn thuốc \"" + ma + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                if (service.xoa(ma)) { JOptionPane.showMessageDialog(this, "Đã xóa."); loadDataToTable(); }
                else JOptionPane.showMessageDialog(this, "Lỗi xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException | RuntimeException e) { JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
        }
    }

    //Nghiệp vụ XUẤT THUỐC VÀ TẠO HÓA ĐƠN tự động
    //Điểm nối giữa quản lý đơn thuốc và quản lý hóa đơn (chuyển trạng thái đơn + tự động sinh hóa đơn).
    private void xuatThuocVaTaoHoaDon() {
        String maDon = layMaDonDaChon();
        if (maDon == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xuất thuốc cho đơn \"" + maDon + "\"?\nThao tác này sẽ đổi trạng thái thành 'Hoàn thành' và tự động tạo 1 Hóa Đơn có thú cưng và chi tiết tiền thuốc.",
                "Xác nhận xuất thuốc", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            entity.NhanVien.NhanVien_entity nhanVien = GUI.HoaDon.LapHoaDon_gui.layNhanVienDangNhapHienTai();
            DonThuocService.KetQuaXuatThuoc ketQua = service.xuatVaTaoHoaDon(maDon, nhanVien);
            java.text.DecimalFormat dinhDangTien = new java.text.DecimalFormat("#,##0 ₫");
            JOptionPane.showMessageDialog(this,
                    "Đã xuất đơn thuốc thành công!\nHóa đơn [" + ketQua.maHoaDon
                            + "] đã được tạo tự động cho thú cưng " + ketQua.tenThuCung
                            + ".\nTổng tiền thuốc: " + dinhDangTien.format(ketQua.tongTien),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadDataToTable();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể xuất thuốc", JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi quá trình xuất thuốc: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Xuất danh sách đơn thuốc ra file CSV.
    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new java.io.File("don-thuoc.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile(); if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        try {
            service.xuatDanhSach(file, daLoc);
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