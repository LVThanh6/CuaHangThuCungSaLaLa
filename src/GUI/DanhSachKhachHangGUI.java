package GUI;

import dao.KhachHang_DAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Danh sách khách hàng:
 * Hiển thị thẻ thống kê, thanh tìm kiếm, bộ lọc, bảng dữ liệu và phân trang.
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

    // Đối tượng DAO xử lý dữ liệu từ database SALALA
    private KhachHang_DAO khachHangDAO;

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public DanhSachKhachHangGUI() {
        khachHangDAO = new KhachHang_DAO();
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

        // Nút "Thêm khách hàng"
        btnThemKhachHang = new JButton("Thêm khách hàng");
        btnThemKhachHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemKhachHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemKhachHang.setFocusPainted(false);
        btnThemKhachHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemKhachHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnThemKhachHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // --- Hàng 2: 4 Thẻ thống kê số liệu ---
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        lblValTongKH = new JLabel("15");
        lblValKhachMoi = new JLabel("5");
        lblValTongTC = new JLabel("15");
        lblValVang = new JLabel("3");
        lblDescTC = new JLabel("1.0 thú cưng / khách");

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
        danhSachCot = new String[] {
                "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
                "Địa chỉ", "Số thú cưng", "Hạng thành viên", "Tổng chi tiêu", "Thao tác"
        };

        duLieuKhachHang = new Object[][] {};
        danhSachBoLoc = new String[] { "Hạng thành viên: Tất cả" };

        panelQuanLyTable = new QuanLyTable(
                danhSachCot,
                duLieuKhachHang,
                "Tìm theo tên, mã khách hàng, số điện thoại...",
                danhSachBoLoc);

        panelQuanLyTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    // =========================================================================
    // 4. CÀI ĐẶT CÁC SỰ KIỆN & CHỨC NĂNG NGHIỆP VỤ
    // =========================================================================
    private void caiDatCacChucNang() {
        // 1. Sự kiện nút "+ Thêm khách hàng"
        btnThemKhachHang.addActionListener(e -> hienThiDialogThemKhachHang());

        // 2. Cài đặt Bộ lọc theo Hạng thành viên
        panelQuanLyTable.caiDatBoLoc(0,
                new String[] { "Hạng thành viên: Tất cả", "Đồng", "Bạc", "Vàng", "Kim cương" },
                luaChon -> {
                    if (luaChon == null || luaChon.equals("Hạng thành viên: Tất cả")) {
                        panelQuanLyTable.getRowSorter().setRowFilter(null);
                    } else {
                        panelQuanLyTable.getRowSorter().setRowFilter(RowFilter.regexFilter(luaChon, 6));
                    }
                });

        // 3. Sự kiện Click trên bảng dữ liệu (Double click xem chi tiết, Click cột Thao
        // tác, Right-click popup)
        JTable table = panelQuanLyTable.getBangDuLieu();

        // Menu chuột phải
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemXem = new JMenuItem("Xem chi tiết khách hàng");
        JMenuItem itemSua = new JMenuItem("Chỉnh sửa thông tin");
        JMenuItem itemXoa = new JMenuItem("Xóa khách hàng");

        itemXem.setFont(RangBuocGiaoDien.fontThuong(13));
        itemSua.setFont(RangBuocGiaoDien.fontThuong(13));
        itemXoa.setFont(RangBuocGiaoDien.fontThuong(13));
        itemXoa.setForeground(new Color(220, 38, 38));

        itemXem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                String maKH = table.getModel().getValueAt(modelRow, 0).toString();
                hienThiDialogXemChiTiet(maKH);
            }
        });

        itemSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                String maKH = table.getModel().getValueAt(modelRow, 0).toString();
                hienThiDialogSuaKhachHang(maKH);
            }
        });

        itemXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                String maKH = table.getModel().getValueAt(modelRow, 0).toString();
                String hoTen = table.getModel().getValueAt(modelRow, 1).toString();
                xuLyXoaKhachHang(maKH, hoTen);
            }
        });

        popupMenu.add(itemXem);
        popupMenu.add(itemSua);
        popupMenu.addSeparator();
        popupMenu.add(itemXoa);

        table.setComponentPopupMenu(popupMenu);

        // Click & Right-click trên bảng
        table.addMouseListener(new MouseAdapter() {
            private void chonDong(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    chonDong(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    chonDong(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    int modelRow = table.convertRowIndexToModel(row);
                    String maKH = table.getModel().getValueAt(modelRow, 0).toString();

                    // Click đúp vào dòng -> Xem chi tiết
                    if (e.getClickCount() == 2) {
                        hienThiDialogXemChiTiet(maKH);
                    }
                    // Click chuột vào cột số 8 (Cột Thao tác) -> Hiện menu lựa chọn
                    else if (col == 8) {
                        popupMenu.show(table, e.getX(), e.getY());
                    }
                }
            }
        });

        // Thiết lập Renderer cho cột Hạng thành viên (6), Chi tiêu (7), Thao tác (8)
        try {
            table.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc,
                        int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                    lbl.setFont(RangBuocGiaoDien.fontDam(12));
                    String valStr = val != null ? val.toString() : "";
                    if (valStr.contains("Kim cương")) {
                        lbl.setForeground(new Color(2, 132, 199));
                    } else if (valStr.contains("Vàng")) {
                        lbl.setForeground(new Color(217, 119, 6));
                    } else if (valStr.contains("Bạc")) {
                        lbl.setForeground(new Color(100, 116, 139));
                    } else {
                        lbl.setForeground(new Color(180, 83, 9));
                    }
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    if (!isSel) {
                        lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return lbl;
                }
            });

            table.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc,
                        int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                    lbl.setFont(RangBuocGiaoDien.fontDam(13));
                    lbl.setForeground(new Color(14, 116, 101));
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 16));
                    if (!isSel) {
                        lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return lbl;
                }
            });

            table.getColumnModel().getColumn(8).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc,
                        int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                    lbl.setFont(RangBuocGiaoDien.fontVua(12));
                    lbl.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (!isSel) {
                        lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return lbl;
                }
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Tải dữ liệu thực tế từ cơ sở dữ liệu SALALA qua KhachHang_DAO
     */
    public void taiDuLieuTuDatabase() {
        try {
            ArrayList<KhachHang> ds = khachHangDAO.getAllKhachHangDayDu();
            if (ds != null && !ds.isEmpty()) {
                duLieuKhachHang = new Object[ds.size()][9];
                for (int i = 0; i < ds.size(); i++) {
                    KhachHang kh = ds.get(i);
                    duLieuKhachHang[i][0] = kh.getMaKH();
                    duLieuKhachHang[i][1] = kh.getHoTen();
                    duLieuKhachHang[i][2] = kh.getSdt();
                    duLieuKhachHang[i][3] = kh.getEmail();
                    duLieuKhachHang[i][4] = kh.getDiaChi() != null ? kh.getDiaChi() : "";
                    duLieuKhachHang[i][5] = String.valueOf(kh.getSoThuCung());
                    duLieuKhachHang[i][6] = (kh.getHangThanhVien() != null ? kh.getHangThanhVien() : "Đồng");
                    duLieuKhachHang[i][7] = String.format("%,.0f ₫", kh.getTongChiTieu()).replace(',', '.');
                    duLieuKhachHang[i][8] = "Xem | Sửa | Xóa";
                }
                if (panelQuanLyTable != null) {
                    panelQuanLyTable.capNhatDuLieu(duLieuKhachHang);
                }
            }
            capNhatThongKe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật các thẻ thống kê từ CSDL
     */
    public void capNhatThongKe() {
        int tongKH = khachHangDAO.getTongSoKhachHang();
        int tongTC = khachHangDAO.getTongSoThuCung();
        int soVang = khachHangDAO.getSoLuongThanhVienVangTroLen();
        int soMoi = khachHangDAO.getSoLuongKhachHangMoi();

        if (lblValTongKH != null)
            lblValTongKH.setText(String.valueOf(tongKH));
        if (lblValKhachMoi != null)
            lblValKhachMoi.setText(String.valueOf(soMoi));
        if (lblValTongTC != null)
            lblValTongTC.setText(String.valueOf(tongTC));
        if (lblValVang != null)
            lblValVang.setText(String.valueOf(soVang));
        if (lblDescTC != null && tongKH > 0) {
            lblDescTC.setText(String.format("%.1f thú cưng / khách", (double) tongTC / tongKH));
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
        String maGoiY = khachHangDAO.phatSinhMaKHMoi();

        JTextField txtMaKH = taoTextField(maGoiY);
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
        btnHuy.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        btnHuy.setPreferredSize(new Dimension(100, 38));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = new JButton("Lưu khách hàng");
        btnLuu.setFont(RangBuocGiaoDien.fontDam(13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setFocusPainted(false);
        btnLuu.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        btnLuu.setPreferredSize(new Dimension(140, 38));

        btnLuu.addActionListener(e -> {
            String ma = txtMaKH.getText().trim();
            String ten = txtHoTen.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();

            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mã khách hàng!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                txtMaKH.requestFocus();
                return;
            }
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập họ và tên khách hàng!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                txtHoTen.requestFocus();
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(dialog, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!",
                        "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                txtSDT.requestFocus();
                return;
            }
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                JOptionPane.showMessageDialog(dialog, "Email không đúng định dạng (ví dụ: ten@gmail.com)!",
                        "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return;
            }

            KhachHang khMoi = new KhachHang(ma, ten, sdt, email, diaChi);
            boolean thanhCong = khachHangDAO.createKhachHang(khMoi);
            if (thanhCong) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                taiDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Thêm thất bại! Vui lòng kiểm tra xem Mã khách hàng đã tồn tại chưa.", "Lỗi",
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
        KhachHang kh = khachHangDAO.getKhachHangTheoMa(maKH);
        if (kh == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin khách hàng!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Hồ Sơ Chi Tiết Khách Hàng - " + maKH, true);
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

        panelInfo.add(new JLabel("Mã KH: " + kh.getMaKH()));
        panelInfo.add(new JLabel("Họ tên: " + kh.getHoTen()));
        panelInfo.add(new JLabel("Điện thoại: " + kh.getSdt()));
        panelInfo.add(new JLabel("Email: " + kh.getEmail()));
        panelInfo.add(new JLabel("Địa chỉ: " + (kh.getDiaChi() != null ? kh.getDiaChi() : "Chưa cập nhật")));
        panelBody.add(panelInfo);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        // 2. Danh sách thú cưng của khách
        List<Object[]> dsThuCung = khachHangDAO.getDanhSachThuCungCuaKhachHang(maKH);
        String[] cotThuCung = { "Mã TC", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh", "Đặc điểm" };
        Object[][] dataTC = new Object[dsThuCung.size()][6];
        for (int i = 0; i < dsThuCung.size(); i++) {
            dataTC[i] = dsThuCung.get(i);
        }

        JTable tableTC = new JTable(new DefaultTableModel(dataTC, cotThuCung) {
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
                " Thú cưng sở hữu (" + dsThuCung.size() + ") ", 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollTC);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        // 3. Lịch sử hóa đơn của khách
        List<Object[]> dsHoaDon = khachHangDAO.getDanhSachHoaDonCuaKhachHang(maKH);
        String[] cotHoaDon = { "Mã HĐ", "Ngày lập", "Trạng thái", "Số tiền (₫)", "Hình thức" };
        Object[][] dataHD = new Object[dsHoaDon.size()][5];
        for (int i = 0; i < dsHoaDon.size(); i++) {
            Object[] row = dsHoaDon.get(i);
            dataHD[i][0] = row[0];
            dataHD[i][1] = row[1];
            dataHD[i][2] = row[2];
            dataHD[i][3] = String.format("%,.0f ₫", (Double) row[3]).replace(',', '.');
            dataHD[i][4] = row[4];
        }

        JTable tableHD = new JTable(new DefaultTableModel(dataHD, cotHoaDon) {
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
                " Lịch sử hóa đơn giao dịch (" + dsHoaDon.size() + ") ", 0, 0, RangBuocGiaoDien.fontDam(13),
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
        KhachHang kh = khachHangDAO.getKhachHangTheoMa(maKH);
        if (kh == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cập Nhật Thông Tin Khách Hàng",
                true);
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblHeader = new JLabel("Chỉnh sửa thông tin - " + maKH);
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
            String ten = txtHoTen.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();

            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Họ và tên không được để trống!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(dialog, "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                JOptionPane.showMessageDialog(dialog, "Email không đúng định dạng!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            KhachHang khCapNhat = new KhachHang(maKH, ten, sdt, email, diaChi);
            boolean ok = khachHangDAO.updateKhachHang(khCapNhat);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin khách hàng thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                taiDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                "Bạn có chắc chắn muốn xóa khách hàng:\n" + hoTen + " (Mã: " + maKH + ") không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (chon == JOptionPane.YES_OPTION) {
            // Kiểm tra ràng buộc trước khi xóa
            if (khachHangDAO.kiemTraRangBuoc(maKH)) {
                JOptionPane.showMessageDialog(this,
                        "Khách hàng " + maKH + " đang có dữ liệu thú cưng hoặc hóa đơn liên quan trong hệ thống.\n" +
                                "Để đảm bảo tính toàn vẹn dữ liệu kế toán, không thể xóa khách hàng này!",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = khachHangDAO.deleteKhachHang(maKH);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa khách hàng " + maKH + " thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    public KhachHang_DAO getKhachHangDAO() {
        return khachHangDAO;
    }
}
