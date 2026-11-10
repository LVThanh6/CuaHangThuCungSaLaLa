package GUI.KhachHang;

import Service.KhachHang.KhachHangService;
import Service.KhachHang.KhachHangService.ChiTietKhachHang;
import Service.KhachHang.KhachHangService.ThongKeKhachHang;
import Service.KhachHang.KhachHangService.KetQuaXoaKhachHang;
import Service.KhachHang.KhachHangService.KetQuaXuLyKhachHang;
import Service.KhachHang.KhachHangService.KetQuaXuatFile;
import entity.KhachHang.KhachHang;
import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.QuanLyTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Giao diện Danh sách khách hàng:
 * Hiển thị thẻ thống kê, thanh tìm kiếm, bộ lọc và toàn bộ bảng dữ liệu.
 * Tích hợp toàn bộ chức năng Thêm, Xem chi tiết, Chỉnh sửa, Xóa, Lọc, Xuất CSV
 * từ CSDL SALALA.
 */
public class DanhSachKhachHangGUI extends JPanel {

    // =========================================================================
    // 1. KHAI BÁO CÁC BIẾN GIAO DIỆN LÊN ĐẦU FILE
    // =========================================================================
    private JPanel panelTongThe;
    private JPanel panelPhanTren;
    private JPanel panelTieuDe;
    private JLabel lblDuongDan;
    private JLabel lblTieuDe;
    private JLabel lblMoTa;
    private JButton btnThemKhachHang;
    private JButton btnXemKhachHang;
    private JButton btnSuaKhachHang;
    private JButton btnXoaKhachHang;
    // Nút xuất danh sách khách hàng ra file CSV (gắn vào thanh công cụ của QuanLyTable)
    private JButton btnXuatDanhSach;

    // Các thẻ thống kê số liệu
    private JPanel panelThongKe;
    private JPanel theTongKhachHang;
    private JPanel theKhachHangMoi;
    private JPanel theTongThuCung;
    private JPanel theThanhVienVang;

    private JLabel lblValTongKH;
    private JLabel lblValKhachMoi;
    private JLabel lblValTongTC;
    private JLabel lblValVang;
    private JLabel lblDescTC;

    // Thành phần bảng dữ liệu và bộ lọc
    private QuanLyTable panelQuanLyTable;
    private String[] danhSachCot;
    private Object[][] duLieuKhachHang;
    private String[] danhSachBoLoc;

    private final KhachHangService service = new KhachHangService();

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public DanhSachKhachHangGUI() {
        khoiTaoGiaoDien();
        caiDatCacChucNang();
        taiDuLieuTuDatabase();
    }

    // =========================================================================
    // 3. CÁC HÀM XÂY DỰNG GIAO DIỆN
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // Khởi tạo khối phía trên (Breadcrumb + Tiêu đề + Nút thêm + 4 thẻ thống kê)
        khoiTaoPhanTren();
        add(panelPhanTren, BorderLayout.NORTH);

        // Khởi tạo khối bảng dữ liệu bên dưới
        khoiTaoBangDuLieu();
        add(panelQuanLyTable, BorderLayout.CENTER);
    }

    private void khoiTaoPhanTren() {
        panelPhanTren = new JPanel();
        panelPhanTren.setLayout(new BoxLayout(panelPhanTren, BoxLayout.Y_AXIS));
        panelPhanTren.setOpaque(false);

        // --- Hàng 1: Breadcrumb đường dẫn & Tiêu đề + Nút thêm ---
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);

        JPanel panelTrai = new JPanel();
        panelTrai.setLayout(new BoxLayout(panelTrai, BoxLayout.Y_AXIS));
        panelTrai.setOpaque(false);

        lblDuongDan = new JLabel("Khách hàng / Danh sách khách hàng");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        lblTieuDe = new JLabel("Danh sách khách hàng");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblMoTa = new JLabel(
                "Quản lý thông tin liên hệ, thú cưng, hạng thành viên và lịch sử chi tiêu từ CSDL SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Các nút thao tác dòng được chọn
        btnThemKhachHang = new JButton("Thêm khách hàng");
        btnThemKhachHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemKhachHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemKhachHang.setForeground(Color.WHITE);
        btnThemKhachHang.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnThemKhachHang.setRolloverEnabled(false);
        btnThemKhachHang.setFocusPainted(false);
        btnThemKhachHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemKhachHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnXemKhachHang = taoNutThaoTac("Xem chi tiết", false);
        btnSuaKhachHang = taoNutThaoTac("Chỉnh sửa", false);
        btnXoaKhachHang = taoNutThaoTac("Xóa", false);
        btnXoaKhachHang.setForeground(new Color(220, 38, 38));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnXemKhachHang);
        panelPhai.add(btnSuaKhachHang);
        panelPhai.add(btnXoaKhachHang);
        panelPhai.add(btnThemKhachHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- Hàng 2: 4 Thẻ thống kê số liệu ---
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        lblValTongKH = new JLabel("0");
        lblValKhachMoi = new JLabel("0");
        lblValTongTC = new JLabel("0");
        lblValVang = new JLabel("0");
        lblDescTC = new JLabel("0.0 thú cưng / khách");

        theTongKhachHang = taoTheThongKe("Tổng khách hàng", lblValTongKH, new JLabel("Toàn bộ hồ sơ"));
        theKhachHangMoi = taoTheThongKe("Khách hàng mới", lblValKhachMoi, new JLabel("Gần đây"));
        theTongThuCung = taoTheThongKe("Tổng thú cưng", lblValTongTC, lblDescTC);
        theThanhVienVang = taoTheThongKe("Thành viên Vàng trở lên", lblValVang, new JLabel("Khách hàng thân thiết"));

        panelThongKe.add(theTongKhachHang);
        panelThongKe.add(theKhachHangMoi);
        panelThongKe.add(theTongThuCung);
        panelThongKe.add(theThanhVienVang);

        panelPhanTren.add(panelThongKe);
    }

    private JButton taoNutThaoTac(String text, boolean noiBat) {
        JButton button = new JButton(text);
        button.setFont(RangBuocGiaoDien.fontThuong(12));
        button.setForeground(noiBat ? Color.WHITE : RangBuocGiaoDien.MAU_CHU_DAO);
        button.setBackground(noiBat ? RangBuocGiaoDien.MAU_CHU_DAO : Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel lblSoChinh, JLabel lblGhiChu) {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        // Khối thông tin số liệu
        JPanel panelThongTin = new JPanel();
        panelThongTin.setLayout(new BoxLayout(panelThongTin, BoxLayout.Y_AXIS));
        panelThongTin.setOpaque(false);

        JLabel lblTenChiSo = new JLabel(tieuDe);
        lblTenChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTenChiSo.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel panelSoLieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelSoLieu.setOpaque(false);

        lblSoChinh.setFont(RangBuocGiaoDien.fontDam(22));
        lblSoChinh.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        panelSoLieu.add(lblSoChinh);
        panelSoLieu.add(lblGhiChu);

        panelThongTin.add(lblTenChiSo);
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 6)));
        panelThongTin.add(panelSoLieu);

        the.add(panelThongTin, BorderLayout.CENTER);

        return the;
    }

    private void khoiTaoBangDuLieu() {
        danhSachCot = service.layDanhSachCot();
        duLieuKhachHang = new Object[][] {};
        danhSachBoLoc = new String[] { service.layLuaChonLocMacDinh() };

        panelQuanLyTable = new QuanLyTable(
                danhSachCot,
                duLieuKhachHang,
                "Tìm theo tên, mã khách hàng, số điện thoại...",
                danhSachBoLoc);

        panelQuanLyTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Tạo nút "Xuất danh sách" và gắn vào thanh công cụ của QuanLyTable
        btnXuatDanhSach = taoNutXuatDanhSach();
        panelQuanLyTable.datNutCongCuPhai(btnXuatDanhSach);
    }

    // =========================================================================
    // 4. CÀI ĐẶT CÁC SỰ KIỆN & CHỨC NĂNG NGHIỆP VỤ
    // =========================================================================
    private void caiDatCacChucNang() {
        // 1. Sự kiện nút "+ Thêm khách hàng"
        btnThemKhachHang.addActionListener(e -> hienThiDialogThemKhachHang());

        // 2. Cài đặt Bộ lọc theo Hạng thành viên
        panelQuanLyTable.caiDatBoLoc(0,
                service.layDanhSachTuyChonLoc(),
                luaChon -> {
                    String regex = service.taoRegexLoc(luaChon);
                    if (regex == null) {
                        panelQuanLyTable.getRowSorter().setRowFilter(null);
                    } else {
                        panelQuanLyTable.getRowSorter().setRowFilter(
                                RowFilter.regexFilter(regex, service.layViTriCotHangThanhVien()));
                    }
                });

        // 3. Các nút thao tác luôn dùng dòng đang được chọn
        JTable table = panelQuanLyTable.getBangDuLieu();
        ListSelectionListener capNhatNut = e -> capNhatTrangThaiNut(table);
        table.getSelectionModel().addListSelectionListener(capNhatNut);
        btnXemKhachHang.addActionListener(e -> thucHienTrenKhachHang(table, 0));
        btnSuaKhachHang.addActionListener(e -> thucHienTrenKhachHang(table, 1));
        btnXoaKhachHang.addActionListener(e -> thucHienTrenKhachHang(table, 2));
        capNhatTrangThaiNut(table);

        // 4. Nút xuất danh sách khách hàng ra file CSV
        btnXuatDanhSach.addActionListener(e -> xuLyXuatDanhSach());

        // Thiết lập renderer cho cột Hạng thành viên
        table.getColumnModel().getColumn(service.layViTriCotHangThanhVien())
                .setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel,
                            boolean hasFoc,
                            int r, int c) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                        lbl.setFont(RangBuocGiaoDien.fontDam(12));
                        String valStr = val != null ? val.toString() : "";
                        lbl.setForeground(KhachHangService.layMauHang(valStr));
                        lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                        if (!isSel) {
                            lbl.setBackground(KhachHangService.layMauNenDong(r));
                        }
                        return lbl;
                    }
                });
    }

    private void capNhatTrangThaiNut(JTable table) {
        boolean coDong = service.coTheThaoTacDuyNhat(table.getSelectedRowCount());
        btnXemKhachHang.setEnabled(coDong);
        btnSuaKhachHang.setEnabled(coDong);
        btnXoaKhachHang.setEnabled(coDong);
    }

    /**
     * Tạo nút "Xuất danh sách" với giao diện thống nhất cùng phong cách toàn bộ ứng dụng.
     * Nút sẽ được gắn vào thanh công cụ của QuanLyTable qua datNutCongCuPhai().
     */
    private JButton taoNutXuatDanhSach() {
        JButton btn = new JButton("Xuất danh sách");
        btn.setPreferredSize(new Dimension(145, 38));
        btn.setFont(RangBuocGiaoDien.fontDam(12));
        btn.setForeground(new Color(30, 41, 59));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Xuất danh sách":
     * 1. Mở hộp thoại chọn vị trí lưu file (JFileChooser).
     * 2. Gọi service.xuatDanhSachKhachHangRaCSV() để thực thi logic xuất.
     * 3. Hiển thị thông báo kết quả thành công / lỗi cho người dùng.
     */
    private void xuLyXuatDanhSach() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu danh sách khách hàng");
        fileChooser.setSelectedFile(new java.io.File(service.layTenFileCSVMacDinh()));

        int luaChon = fileChooser.showSaveDialog(this);
        if (luaChon != JFileChooser.APPROVE_OPTION) {
            return; // Người dùng huỷ, không làm gì cả
        }

        java.io.File file = fileChooser.getSelectedFile();
        // Tự động thêm đuôi .csv nếu người dùng quên
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new java.io.File(file.getAbsolutePath() + ".csv");
        }

        KetQuaXuatFile ketQua = service.xuatDanhSachKhachHangRaCSV(file, panelQuanLyTable.getBangDuLieu());

        if (ketQua.isThanhCong()) {
            JOptionPane.showMessageDialog(this,
                    ketQua.getThongBao(),
                    "Xuất file thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    ketQua.getThongBao(),
                    "Lỗi xuất file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void thucHienTrenKhachHang(JTable table, int thaoTac) {
        int row = table.getSelectedRow();
        if (!service.coTheThaoTacDuyNhat(table.getSelectedRowCount()) || row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String maKH = String.valueOf(table.getModel().getValueAt(modelRow, service.layViTriCotMaKhachHang()));
        if (thaoTac == 0) {
            hienThiDialogXemChiTiet(maKH);
        } else if (thaoTac == 1) {
            hienThiDialogSuaKhachHang(maKH);
        } else {
            String hoTen = String.valueOf(table.getModel().getValueAt(modelRow, service.layViTriCotHoTen()));
            xuLyXoaKhachHang(maKH, hoTen);
        }
    }

    /**
     * Tải dữ liệu thực tế từ cơ sở dữ liệu SALALA qua KhachHangService
     */
    public void taiDuLieuTuDatabase() {
        duLieuKhachHang = service.taiDuLieuBang();
        if (panelQuanLyTable != null) {
            panelQuanLyTable.capNhatDuLieu(duLieuKhachHang);
        }
        capNhatThongKe();
    }

    /**
     * Cập nhật các thẻ thống kê từ CSDL
     */
    public void capNhatThongKe() {
        ThongKeKhachHang statistics = service.layThongKe();
        if (lblValTongKH != null)
            lblValTongKH.setText(statistics.layChuoiTongKhachHang());
        if (lblValKhachMoi != null)
            lblValKhachMoi.setText(statistics.layChuoiKhachHangMoi());
        if (lblValTongTC != null)
            lblValTongTC.setText(statistics.layChuoiTongThuCung());
        if (lblValVang != null)
            lblValVang.setText(statistics.layChuoiThanhVienVang());
        if (lblDescTC != null) {
            lblDescTC.setText(statistics.layChuoiMoTaThuCung());
        }
    }

    // =========================================================================
    // 5. CÁC HỘP THOẠI CHỨC NĂNG (DIALOGS)
    // =========================================================================

    /**
     * Hộp thoại thêm khách hàng mới
     */
    private void hienThiDialogThemKhachHang() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Khách Hàng Mới", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Tiêu đề form
        JLabel lblHeader = new JLabel("Thông tin khách hàng mới");
        lblHeader.setFont(RangBuocGiaoDien.fontDam(18));
        lblHeader.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelContent.add(lblHeader);

        JLabel lblSub = new JLabel("Nhập đầy đủ thông tin để tạo hồ sơ thành viên SALALA");
        lblSub.setFont(RangBuocGiaoDien.fontThuong(12));
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelContent.add(lblSub);
        panelContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // Tự động phát sinh mã khách hàng
        String maGoiY = service.phatSinhMaMoi();

        JTextField txtMaKH = taoTextField(maGoiY);
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(241, 245, 249));
        JTextField txtHoTen = taoTextField("");
        JTextField txtSDT = taoTextField("");
        JTextField txtEmail = taoTextField("");
        JTextField txtDiaChi = taoTextField("");

        panelContent.add(taoHangNhapLieu("Mã khách hàng (*):", txtMaKH));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Họ và tên (*):", txtHoTen));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Số điện thoại (*):", txtSDT));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Email (*):", txtEmail));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Địa chỉ:", txtDiaChi));

        dialog.add(panelContent, BorderLayout.CENTER);

        // Khối nút bấm phía dưới
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        panelButtons.setBackground(new Color(248, 250, 252));
        panelButtons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.setFont(RangBuocGiaoDien.fontVua(13));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        btnHuy.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnHuy.setRolloverEnabled(false);
        btnHuy.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btnHuy.setPreferredSize(new Dimension(100, 38));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = new JButton("Lưu khách hàng");
        btnLuu.setFont(RangBuocGiaoDien.fontDam(13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLuu.setRolloverEnabled(false);
        btnLuu.setFocusPainted(false);
        btnLuu.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        btnLuu.setPreferredSize(new Dimension(140, 38));

        btnLuu.addActionListener(e -> {
            KetQuaXuLyKhachHang ketQua = service.xuLyThemKhachHang(
                    txtMaKH.getText(), txtHoTen.getText(), txtSDT.getText(),
                    txtEmail.getText(), txtDiaChi.getText());
            if (ketQua.isThanhCong()) {
                JOptionPane.showMessageDialog(this, ketQua.getThongBao(), "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                taiDuLieuTuDatabase();
            } else if (ketQua.isLoiNhapLieu()) {
                JOptionPane.showMessageDialog(dialog, ketQua.getThongBao(), "Lỗi định dạng",
                        JOptionPane.WARNING_MESSAGE);
                switch (ketQua.getTruongLoi()) {
                    case MA_KHACH_HANG:
                        txtMaKH.requestFocus();
                        break;
                    case HO_TEN:
                        txtHoTen.requestFocus();
                        break;
                    case SO_DIEN_THOAI:
                        txtSDT.requestFocus();
                        break;
                    case EMAIL:
                        txtEmail.requestFocus();
                        break;
                    default:
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(dialog, ketQua.getThongBao(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panelButtons.add(btnHuy);
        panelButtons.add(btnLuu);
        dialog.add(panelButtons, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại xem chi tiết khách hàng (kèm thú cưng và lịch sử hóa đơn)
     */
    private void hienThiDialogXemChiTiet(String maKH) {
        ChiTietKhachHang details = service.layChiTiet(maKH);
        if (details == null) {
            JOptionPane.showMessageDialog(this, service.layThongDiepKhongTimThayKhachHang(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                details.layTieuDeDialogChiTiet(), true);
        dialog.setSize(680, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelBody = new JPanel();
        panelBody.setLayout(new BoxLayout(panelBody, BoxLayout.Y_AXIS));
        panelBody.setBackground(Color.WHITE);
        panelBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Thẻ thông tin cá nhân
        JPanel panelInfo = new JPanel(new GridLayout(3, 2, 12, 8));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                " Thông tin cá nhân ", 0, 0, RangBuocGiaoDien.fontDam(13), RangBuocGiaoDien.MAU_CHU_DAO));

        panelInfo.add(new JLabel(details.layThongTinMaKH()));
        panelInfo.add(new JLabel(details.layThongTinHoTen()));
        panelInfo.add(new JLabel(details.layThongTinSDT()));
        panelInfo.add(new JLabel(details.layThongTinEmail()));
        panelInfo.add(new JLabel(details.layThongTinDiaChi()));
        panelBody.add(panelInfo);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        // 2. Danh sách thú cưng của khách
        JTable tableTC = new JTable(
                new DefaultTableModel(details.layDuLieuBangThuCung(), ChiTietKhachHang.COT_THU_CUNG) {
                    @Override
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                });
        tableTC.setRowHeight(30);
        JScrollPane scrollTC = new JScrollPane(tableTC);
        scrollTC.setPreferredSize(new Dimension(0, 120));
        scrollTC.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                details.layTieuDeThuCung(), 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollTC);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        // 3. Lịch sử hóa đơn của khách
        JTable tableHD = new JTable(new DefaultTableModel(details.layDuLieuBangHoaDon(), ChiTietKhachHang.COT_HOA_DON) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableHD.setRowHeight(30);
        JScrollPane scrollHD = new JScrollPane(tableHD);
        scrollHD.setPreferredSize(new Dimension(0, 130));
        scrollHD.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                details.layTieuDeHoaDon(), 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollHD);

        dialog.add(panelBody, BorderLayout.CENTER);

        // Nút đóng
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        panelBottom.setBackground(new Color(248, 250, 252));
        JButton btnDong = new JButton("Đóng");
        btnDong.setPreferredSize(new Dimension(100, 36));
        btnDong.setFont(RangBuocGiaoDien.fontVua(13));
        btnDong.addActionListener(e -> dialog.dispose());
        panelBottom.add(btnDong);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại chỉnh sửa thông tin khách hàng
     */
    private void hienThiDialogSuaKhachHang(String maKH) {
        KhachHang kh = service.layKhachHangTheoMa(maKH);
        if (kh == null) {
            JOptionPane.showMessageDialog(this, service.layThongDiepKhongTimThayKhachHang(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Cập Nhật Thông Tin Khách Hàng", true);
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblHeader = new JLabel(service.layTieuDeDialogSua(maKH));
        lblHeader.setFont(RangBuocGiaoDien.fontDam(18));
        lblHeader.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelContent.add(lblHeader);
        panelContent.add(Box.createRigidArea(new Dimension(0, 16)));

        JTextField txtMaKH = taoTextField(kh.getMaKH());
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(241, 245, 249));

        JTextField txtHoTen = taoTextField(kh.getHoTen());
        JTextField txtSDT = taoTextField(kh.getSdt());
        JTextField txtEmail = taoTextField(kh.getEmail());
        JTextField txtDiaChi = taoTextField(kh.getDiaChi() != null ? kh.getDiaChi() : "");

        panelContent.add(taoHangNhapLieu("Mã khách hàng:", txtMaKH));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Họ và tên (*):", txtHoTen));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Số điện thoại (*):", txtSDT));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Email (*):", txtEmail));
        panelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContent.add(taoHangNhapLieu("Địa chỉ:", txtDiaChi));

        dialog.add(panelContent, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        panelButtons.setBackground(new Color(248, 250, 252));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(90, 36));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnCapNhat = new JButton("Lưu thay đổi");
        btnCapNhat.setPreferredSize(new Dimension(130, 36));
        btnCapNhat.setFont(RangBuocGiaoDien.fontDam(13));
        btnCapNhat.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnCapNhat.setForeground(Color.WHITE);

        btnCapNhat.addActionListener(e -> {
            KetQuaXuLyKhachHang ketQua = service.xuLyCapNhatKhachHang(
                    maKH, txtHoTen.getText(), txtSDT.getText(),
                    txtEmail.getText(), txtDiaChi.getText());
            if (ketQua.isThanhCong()) {
                JOptionPane.showMessageDialog(this, ketQua.getThongBao(), "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                taiDuLieuTuDatabase();
            } else if (ketQua.isLoiNhapLieu()) {
                JOptionPane.showMessageDialog(dialog, ketQua.getThongBao(), "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                switch (ketQua.getTruongLoi()) {
                    case MA_KHACH_HANG:
                        txtMaKH.requestFocus();
                        break;
                    case HO_TEN:
                        txtHoTen.requestFocus();
                        break;
                    case SO_DIEN_THOAI:
                        txtSDT.requestFocus();
                        break;
                    case EMAIL:
                        txtEmail.requestFocus();
                        break;
                    default:
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(dialog, ketQua.getThongBao(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panelButtons.add(btnHuy);
        panelButtons.add(btnCapNhat);
        dialog.add(panelButtons, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Xử lý xóa khách hàng
     */
    private void xuLyXoaKhachHang(String maKH, String hoTen) {
        int chon = JOptionPane.showConfirmDialog(this,
                service.taoThongDiepXacNhanXoa(hoTen, maKH),
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (chon == JOptionPane.YES_OPTION) {
            KetQuaXoaKhachHang ketQua = service.xuLyXoaKhachHang(maKH);
            if (ketQua.isBiRangBuoc()) {
                JOptionPane.showMessageDialog(this, ketQua.getThongBao(),
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ketQua.isThanhCong()) {
                JOptionPane.showMessageDialog(this, ketQua.getThongBao(), "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(this, ketQua.getThongBao(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Tiện ích tạo dòng nhập liệu
    private JPanel taoHangNhapLieu(String nhan, JTextField txt) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(nhan);
        lbl.setPreferredSize(new Dimension(140, 36));
        lbl.setFont(RangBuocGiaoDien.fontVua(13));
        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        p.add(lbl, BorderLayout.WEST);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    private JTextField taoTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setPreferredSize(new Dimension(0, 36));
        txt.setFont(RangBuocGiaoDien.fontThuong(13));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        return txt;
    }

}
