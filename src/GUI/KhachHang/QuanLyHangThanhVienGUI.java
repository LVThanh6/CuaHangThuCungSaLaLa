package GUI.KhachHang;

import controller.HangThanhVienController;
import controller.HangThanhVienController.Dashboard;
import controller.HangThanhVienController.KetQuaCapNhatChinhSach;
import controller.HangThanhVienController.KetQuaChuyenHang;
import controller.HangThanhVienController.KetQuaPhanHang;
import controller.HangThanhVienController.ThongTinDongThanhVien;
import controller.HangThanhVienController.TierCardModel;
import controller.HangThanhVienController.TierPolicyFormData;
import controller.KhachHangController.CustomerDetails;
import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Giao diện Quản lý hạng thành viên:
 * Kết nối trực tiếp bảng QuanLyHangThanhVien trong CSDL SALALA.
 * Hiển thị 4 nhóm hạng: Đồng, Bạc, Vàng, Kim Cương kèm thống kê thực tế,
 * chính sách ưu đãi, quyền lợi đặc quyền và danh sách thành viên.
 */
public class QuanLyHangThanhVienGUI extends JPanel {

    // =========================================================================
    // 1. KHAI BÁO CÁC BIẾN GIAO DIỆN LÊN ĐẦU FILE
    // =========================================================================
    private JPanel panelTongThe;
    private JPanel panelPhanTren;
    private JPanel panelTieuDe;
    private JLabel lblDuongDan;
    private JLabel lblTieuDe;
    private JLabel lblMoTa;
    private JButton btnThemHang;

    // Các thẻ thống kê nhanh phía trên
    private JPanel panelThongKe;
    private JPanel theTongKhachHang;
    private JPanel theNangHangThangNay;
    private JPanel theGiamGiaDaApDung;
    private JPanel theChiTieuThanhVien;

    private JLabel lblValTongKH;
    private JLabel lblValNangHang;
    private JLabel lblValGiamGia;
    private JLabel lblValChiTieu;

    // Khu vực hiển thị 4 thẻ hạng thành viên
    private JPanel panelDanhSachHang;
    private JScrollPane scrollDanhSachHang;

    private final HangThanhVienController controller = new HangThanhVienController();

    // =========================================================================
    // 2. HÀM KHỞI TẠO (CONSTRUCTOR)
    // =========================================================================
    public QuanLyHangThanhVienGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        capNhatThongKe();
    }

    // =========================================================================
    // 3. CÁC HÀM XÂY DỰNG GIAO DIỆN
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // 1. Phần phía trên: Breadcrumb + Tiêu đề + Nút thêm hạng + 4 thẻ thống kê
        khoiTaoPhanTren();
        add(panelPhanTren, BorderLayout.NORTH);

        // 2. Phần lưới 4 thẻ hạng thành viên
        panelDanhSachHang = new JPanel(new GridLayout(1, 4, 16, 0));
        panelDanhSachHang.setOpaque(false);

        scrollDanhSachHang = new JScrollPane(panelDanhSachHang);
        scrollDanhSachHang.setBorder(BorderFactory.createEmptyBorder());
        scrollDanhSachHang.setOpaque(false);
        scrollDanhSachHang.getViewport().setOpaque(false);
        add(scrollDanhSachHang, BorderLayout.CENTER);
    }

    private void khoiTaoPhanTren() {
        panelPhanTren = new JPanel();
        panelPhanTren.setLayout(new BoxLayout(panelPhanTren, BoxLayout.Y_AXIS));
        panelPhanTren.setOpaque(false);

        // Hàng 1: Breadcrumb đường dẫn & Tiêu đề + Nút thêm
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);

        JPanel panelTrai = new JPanel();
        panelTrai.setLayout(new BoxLayout(panelTrai, BoxLayout.Y_AXIS));
        panelTrai.setOpaque(false);

        lblDuongDan = new JLabel("Khách hàng / Quản lý hạng thành viên");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        lblTieuDe = new JLabel("Quản lý hạng thành viên");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblMoTa = new JLabel("Thiết lập chính sách ưu đãi, mức giảm giá và quản lý thành viên từ CSDL SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(13));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        panelTrai.add(lblDuongDan);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblTieuDe);
        panelTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTrai.add(lblMoTa);
        panelTieuDe.add(panelTrai, BorderLayout.WEST);

        // Nút "Thêm hạng" - Nút phẳng tiêu chuẩn, nền màu chủ đạo, chữ trắng, không icon, không animation
        btnThemHang = new JButton("Thêm hạng");
        btnThemHang.setFont(RangBuocGiaoDien.fontDam(13));
        btnThemHang.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThemHang.setForeground(Color.WHITE);
        btnThemHang.setUI(new BasicButtonUI());
        btnThemHang.setRolloverEnabled(false);
        btnThemHang.setFocusPainted(false);
        btnThemHang.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThemHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panelPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelPhai.setOpaque(false);
        panelPhai.add(btnThemHang);
        panelTieuDe.add(panelPhai, BorderLayout.EAST);

        panelPhanTren.add(panelTieuDe);
        panelPhanTren.add(Box.createRigidArea(new Dimension(0, 16)));

        // Hàng 2: 4 Thẻ thống kê số liệu thực tế từ database
        panelThongKe = new JPanel(new GridLayout(1, 4, 16, 0));
        panelThongKe.setOpaque(false);

        lblValTongKH = new JLabel("0");
        lblValNangHang = new JLabel("0");
        lblValGiamGia = new JLabel("0 ₫");
        lblValChiTieu = new JLabel("0 ₫");

        theTongKhachHang = taoTheThongKe("Tổng khách hàng", lblValTongKH, "Đã phân hạng");
        theNangHangThangNay = taoTheThongKe("Thành viên nâng hạng", lblValNangHang, "Gần đây");
        theGiamGiaDaApDung = taoTheThongKe("Giảm giá đã áp dụng", lblValGiamGia, "Toàn bộ hóa đơn");
        theChiTieuThanhVien = taoTheThongKe("Chi tiêu thành viên", lblValChiTieu, "Tổng doanh thu");

        panelThongKe.add(theTongKhachHang);
        panelThongKe.add(theNangHangThangNay);
        panelThongKe.add(theGiamGiaDaApDung);
        panelThongKe.add(theChiTieuThanhVien);

        panelPhanTren.add(panelThongKe);
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel lblSoChinh, String moTaPhu) {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

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

        JLabel lblGhiChu = new JLabel(moTaPhu);
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

    /**
     * Nạp dữ liệu thực tế từ database SALALA và cập nhật giao diện
     */
    public void capNhatThongKe() {
        Dashboard dashboard = controller.layThongKeTongQuan();

        if (lblValTongKH != null)
            lblValTongKH.setText(dashboard.layChuoiTongThanhVien());
        if (lblValNangHang != null)
            lblValNangHang.setText(dashboard.layChuoiSoNangHangGanDay());
        if (lblValGiamGia != null)
            lblValGiamGia.setText(dashboard.layGiamGiaDinhDang());
        if (lblValChiTieu != null)
            lblValChiTieu.setText(dashboard.layChiTieuDinhDang());

        panelDanhSachHang.removeAll();
        List<TierCardModel> cards = controller.layDanhSachTheHang(dashboard);

        for (TierCardModel card : cards) {
            JPanel the = taoTheHang(card);
            panelDanhSachHang.add(the);
        }

        panelDanhSachHang.revalidate();
        panelDanhSachHang.repaint();
    }

    private JPanel taoTheHang(TierCardModel card) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        the.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 1. Thanh viền màu ở đỉnh thẻ
        JPanel thanhMauTop = new JPanel();
        thanhMauTop.setBackground(card.mauVienTop);
        thanhMauTop.setPreferredSize(new Dimension(0, 4));
        thanhMauTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        the.add(thanhMauTop);

        // 2. Khối Header của thẻ (Tên hạng + Badge Đang áp dụng)
        JPanel panelHeaderThe = new JPanel(new BorderLayout());
        panelHeaderThe.setBackground(card.mauNenNhe);
        panelHeaderThe.setBorder(new EmptyBorder(16, 16, 10, 16));

        JLabel lblTenHang = new JLabel(card.tenHang);
        lblTenHang.setFont(RangBuocGiaoDien.fontDam(18));
        lblTenHang.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblHuyHieu = new JLabel("Đang áp dụng");
        lblHuyHieu.setFont(RangBuocGiaoDien.fontDam(10));
        lblHuyHieu.setForeground(new Color(217, 119, 6));
        lblHuyHieu.setBackground(Color.WHITE);
        lblHuyHieu.setOpaque(true);
        lblHuyHieu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 215, 170), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        panelHeaderThe.add(lblTenHang, BorderLayout.WEST);
        panelHeaderThe.add(lblHuyHieu, BorderLayout.EAST);
        the.add(panelHeaderThe);

        // 3. Khối điều kiện chi tiêu
        JPanel panelDieuKien = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelDieuKien.setBackground(card.mauNenNhe);
        panelDieuKien.setBorder(new EmptyBorder(0, 16, 14, 16));

        JLabel lblDieuKien = new JLabel(card.dieuKienChiTieu);
        lblDieuKien.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDieuKien.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelDieuKien.add(lblDieuKien);
        the.add(panelDieuKien);

        // 4. Khối 2 cột: Mức giảm giá & Số lượng khách hàng
        JPanel panelThongSo = new JPanel(new GridLayout(1, 2, 8, 0));
        panelThongSo.setOpaque(false);
        panelThongSo.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Cột trái: Mức giảm giá
        JPanel colGiamGia = new JPanel();
        colGiamGia.setLayout(new BoxLayout(colGiamGia, BoxLayout.Y_AXIS));
        colGiamGia.setBackground(Color.WHITE);
        colGiamGia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel lblTieuDeGiamGia = new JLabel("Giảm giá");
        lblTieuDeGiamGia.setFont(RangBuocGiaoDien.fontThuong(11));
        lblTieuDeGiamGia.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblPhanTram = new JLabel(card.mucGiamGia);
        lblPhanTram.setFont(RangBuocGiaoDien.fontDam(20));
        lblPhanTram.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        colGiamGia.add(lblTieuDeGiamGia);
        colGiamGia.add(Box.createRigidArea(new Dimension(0, 4)));
        colGiamGia.add(lblPhanTram);
        panelThongSo.add(colGiamGia);

        // Cột phải: Số lượng khách hàng
        JPanel colKhachHang = new JPanel();
        colKhachHang.setLayout(new BoxLayout(colKhachHang, BoxLayout.Y_AXIS));
        colKhachHang.setBackground(Color.WHITE);
        colKhachHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel lblSoKhach = new JLabel(card.soKhachHang);
        lblSoKhach.setFont(RangBuocGiaoDien.fontDam(14));
        lblSoKhach.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblTiLe = new JLabel(card.tiLeKhachHang);
        lblTiLe.setFont(RangBuocGiaoDien.fontThuong(11));
        lblTiLe.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        colKhachHang.add(lblSoKhach);
        colKhachHang.add(Box.createRigidArea(new Dimension(0, 4)));
        colKhachHang.add(lblTiLe);
        panelThongSo.add(colKhachHang);

        the.add(panelThongSo);

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.add(sep);

        // 5. Danh sách các quyền lợi cụ thể từ database
        JPanel panelQuyenLoi = new JPanel();
        panelQuyenLoi.setLayout(new BoxLayout(panelQuyenLoi, BoxLayout.Y_AXIS));
        panelQuyenLoi.setOpaque(false);
        panelQuyenLoi.setBorder(new EmptyBorder(14, 16, 12, 16));

        JLabel lblTieuDeQuyenLoi = new JLabel("QUYỀN LỢI ĐẶC QUYỀN");
        lblTieuDeQuyenLoi.setFont(RangBuocGiaoDien.fontDam(11));
        lblTieuDeQuyenLoi.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelQuyenLoi.add(lblTieuDeQuyenLoi);
        panelQuyenLoi.add(Box.createRigidArea(new Dimension(0, 8)));

        for (String ql : card.danhSachQuyenLoi) {
            JPanel dongQuyenLoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            dongQuyenLoi.setOpaque(false);

            JLabel lblTick = new JLabel("-");
            lblTick.setFont(RangBuocGiaoDien.fontDam(12));
            lblTick.setForeground(new Color(16, 185, 129));

            JLabel lblText = new JLabel(ql);
            lblText.setFont(RangBuocGiaoDien.fontThuong(12));
            lblText.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

            dongQuyenLoi.add(lblTick);
            dongQuyenLoi.add(lblText);
            panelQuyenLoi.add(dongQuyenLoi);
        }

        the.add(panelQuyenLoi);

        // 6. Nút "Xem danh sách khách hàng" trực tiếp dưới chân thẻ
        JPanel panelBottomThe = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panelBottomThe.setOpaque(false);

        JButton btnXemDanhSach = new JButton("Xem danh sách khách hàng");
        btnXemDanhSach.setFont(RangBuocGiaoDien.fontVua(12));
        btnXemDanhSach.setBackground(Color.WHITE);
        btnXemDanhSach.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnXemDanhSach.setUI(new BasicButtonUI());
        btnXemDanhSach.setRolloverEnabled(false);
        btnXemDanhSach.setFocusPainted(false);
        btnXemDanhSach.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btnXemDanhSach.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXemDanhSach.addActionListener(e -> hienThiDialogDanhSachTheoHang(card.tenHang));

        panelBottomThe.add(btnXemDanhSach);
        the.add(panelBottomThe);

        // Click trên cả thẻ để mở danh sách
        the.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hienThiDialogDanhSachTheoHang(card.tenHang);
            }
        });

        the.add(Box.createVerticalGlue());
        return the;
    }

    // =========================================================================
    // 4. CÀI ĐẶT CÁC SỰ KIỆN CHỨC NĂNG
    // =========================================================================
    private void caiDatSuKien() {
        btnThemHang.addActionListener(e -> hienThiDialogThemHang());
    }

    /**
     * Hộp thoại xem danh sách thành viên thực tế thuộc hạng cụ thể (Đồng / Bạc /
     * Vàng / Kim cương)
     */
    private void hienThiDialogDanhSachTheoHang(String tenHang) {
        Object[][] duLieu = controller.taiDuLieuBangThanhVien(tenHang);
        String[] cot = controller.layDanhSachCotThanhVien();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                controller.layTieuDeDialogDanhSachThanhVien(tenHang, duLieu.length), true);
        dialog.setSize(950, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new BorderLayout(12, 0));
        panelTop.setBackground(Color.WHITE);
        panelTop.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(controller.layTieuDeNhomKhachHang(tenHang));
        lblTitle.setFont(RangBuocGiaoDien.fontDam(16));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelTop.add(lblTitle, BorderLayout.WEST);

        // Ô tìm kiếm trong nhóm
        JTextField txtLoc = new JTextField();
        txtLoc.setPreferredSize(new Dimension(240, 34));
        txtLoc.setFont(RangBuocGiaoDien.fontThuong(12));
        txtLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtLoc.setToolTipText("Tìm kiếm theo tên, mã hoặc SĐT");

        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pSearch.setOpaque(false);
        pSearch.add(new JLabel("Tìm:"));
        pSearch.add(txtLoc);
        panelTop.add(pSearch, BorderLayout.EAST);

        dialog.add(panelTop, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(duLieu, cot) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(RangBuocGiaoDien.fontThuong(13));
        table.getTableHeader().setFont(RangBuocGiaoDien.fontDam(12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Căn phải cho chi tiêu và điểm
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(controller.layViTriCotChiTieuThanhVien()).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(controller.layViTriCotDiemTichLuy()).setCellRenderer(rightRenderer);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Click đúp vào dòng để xem chi tiết khách hàng và thú cưng
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        ThongTinDongThanhVien tt = controller.trichXuatThongTinDong(table.getModel(), modelRow);
                        hienThiDialogXemChiTietKhachHang(tt.getMaKH());
                    }
                }
            }
        });

        txtLoc.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String regex = controller.taoRegexTimKiem(txtLoc.getText());
                sorter.setRowFilter(regex == null ? null : RowFilter.regexFilter(regex));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 0, 20));
        scroll.getViewport().setBackground(Color.WHITE);
        dialog.add(scroll, BorderLayout.CENTER);

        // Thanh điều khiển phía dưới
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBackground(new Color(248, 250, 252));
        panelBottom.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblHuongDan = new JLabel(
                "Lưu ý: Nhấp đúp vào bất kỳ khách hàng nào để xem chi tiết thú cưng & lịch sử hóa đơn");
        lblHuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        panelBottom.add(lblHuongDan, BorderLayout.WEST);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtn.setOpaque(false);

        JButton btnChuyenHang = new JButton("Chuyển hạng thành viên");
        btnChuyenHang.setFont(RangBuocGiaoDien.fontVua(13));
        btnChuyenHang.setBackground(Color.WHITE);
        btnChuyenHang.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnChuyenHang.setUI(new BasicButtonUI());
        btnChuyenHang.setRolloverEnabled(false);
        btnChuyenHang.setFocusPainted(false);
        btnChuyenHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        btnChuyenHang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnChuyenHang.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, controller.layThongBaoChuaChonKhachHangChuyenHang(),
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            ThongTinDongThanhVien tt = controller.trichXuatThongTinDong(table.getModel(), modelRow);

            hienThiDialogChuyenHang(dialog, tt.getMaHang(), tt.getMaKH(), tt.getHoTen(), tenHang);
        });

        JButton btnDong = new JButton("Đóng");
        btnDong.setPreferredSize(new Dimension(90, 36));
        btnDong.setFont(RangBuocGiaoDien.fontVua(13));
        btnDong.setUI(new BasicButtonUI());
        btnDong.setRolloverEnabled(false);
        btnDong.addActionListener(e -> dialog.dispose());

        pBtn.add(btnChuyenHang);
        pBtn.add(btnDong);
        panelBottom.add(pBtn, BorderLayout.EAST);

        dialog.add(panelBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Hộp thoại chuyển nhanh hạng cho một thành viên
     */
    private void hienThiDialogChuyenHang(JDialog parent, String maHang, String maKH, String hoTen, String hangHienTai) {
        String[] cacHang = controller.layDanhSachTenHang();
        JComboBox<String> cbo = new JComboBox<>(cacHang);
        cbo.setSelectedItem(hangHienTai);

        JPanel panel = new JPanel(new GridLayout(3, 1, 6, 8));
        panel.add(new JLabel(controller.layNhanKhachHangChuyenHang(hoTen, maKH)));
        panel.add(new JLabel("Chọn hạng thành viên mới:"));
        panel.add(cbo);

        int opt = JOptionPane.showConfirmDialog(parent, panel, "Chuyển hạng thành viên", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String hangMoi = (String) cbo.getSelectedItem();
            KetQuaChuyenHang ketQua = controller.xuLyChuyenHang(maHang, maKH, hoTen, hangHienTai, hangMoi);
            if (ketQua.isCoThayDoi()) {
                if (ketQua.isThanhCong()) {
                    JOptionPane.showMessageDialog(parent, ketQua.getThongBao(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    parent.dispose();
                    capNhatThongKe();
                } else {
                    JOptionPane.showMessageDialog(parent, ketQua.getThongBao(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Hộp thoại xem chi tiết hồ sơ khách hàng ngay từ màn hình Hạng thành viên
     */
    private void hienThiDialogXemChiTietKhachHang(String maKH) {
        CustomerDetails details = controller.layChiTietKhachHang(maKH);
        if (details == null)
            return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                details.layTieuDeDialogChiTiet(), true);
        dialog.setSize(680, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelBody = new JPanel();
        panelBody.setLayout(new BoxLayout(panelBody, BoxLayout.Y_AXIS));
        panelBody.setBackground(Color.WHITE);
        panelBody.setBorder(new EmptyBorder(20, 24, 20, 24));

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

        JTable tableTC = new JTable(new DefaultTableModel(details.layDuLieuBangThuCung(), CustomerDetails.COT_THU_CUNG) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableTC.setRowHeight(30);
        JScrollPane scrollTC = new JScrollPane(tableTC);
        scrollTC.setPreferredSize(new Dimension(0, 110));
        scrollTC.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                details.layTieuDeThuCung(), 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollTC);
        panelBody.add(Box.createRigidArea(new Dimension(0, 14)));

        JTable tableHD = new JTable(new DefaultTableModel(details.layDuLieuBangHoaDon(), CustomerDetails.COT_HOA_DON) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableHD.setRowHeight(30);
        JScrollPane scrollHD = new JScrollPane(tableHD);
        scrollHD.setPreferredSize(new Dimension(0, 120));
        scrollHD.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                details.layTieuDeHoaDon(), 0, 0, RangBuocGiaoDien.fontDam(13),
                RangBuocGiaoDien.MAU_CHU_DAO));
        panelBody.add(scrollHD);

        dialog.add(panelBody, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        pBottom.setBackground(new Color(248, 250, 252));
        JButton bDong = new JButton("Đóng");
        bDong.setPreferredSize(new Dimension(100, 36));
        bDong.setFont(RangBuocGiaoDien.fontVua(13));
        bDong.setUI(new BasicButtonUI());
        bDong.setRolloverEnabled(false);
        bDong.addActionListener(e -> dialog.dispose());
        pBottom.add(bDong);
        dialog.add(pBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Hộp thoại thêm / cấu hình chính sách hạng thành viên và phân hạng khách hàng
     */
    private void hienThiDialogThemHang() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm / Tùy Chỉnh Hạng Thành Viên",
                true);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(RangBuocGiaoDien.fontDam(13));

        // TAB 1: Cấu hình chính sách hạng (Đồng, Bạc, Vàng, Kim Cương)
        JPanel tabChinhSach = new JPanel();
        tabChinhSach.setLayout(new BoxLayout(tabChinhSach, BoxLayout.Y_AXIS));
        tabChinhSach.setBackground(Color.WHITE);
        tabChinhSach.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] dsTenHang = controller.layDanhSachTenHang();
        JComboBox<String> cboTenHang = new JComboBox<>(dsTenHang);
        cboTenHang.setPreferredSize(new Dimension(0, 36));
        cboTenHang.setFont(RangBuocGiaoDien.fontVua(13));

        JTextField txtChiTieuMin = new JTextField();
        JTextField txtChiTieuMax = new JTextField();
        JTextField txtGiamGia = new JTextField();
        JTextField txtDiemTichLuy1Van = new JTextField();
        JTextArea txtQuyenLoi = new JTextArea(4, 20);
        txtQuyenLoi.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
        txtQuyenLoi.setFont(RangBuocGiaoDien.fontThuong(12));

        // Hàm nạp thông tin hạng vào form khi chọn ComboBox
        Runnable napThongTinHang = () -> {
            String selected = (String) cboTenHang.getSelectedItem();
            TierPolicyFormData form = controller.layDuLieuFormChinhSach(selected);
            txtChiTieuMin.setText(form.chiTieuMin);
            txtChiTieuMax.setText(form.chiTieuMax);
            txtGiamGia.setText(form.mucGiamGia);
            txtDiemTichLuy1Van.setText(form.diemTichLuy1Van);
            txtQuyenLoi.setText(form.quyenLoi);
        };
        napThongTinHang.run();
        cboTenHang.addActionListener(e -> napThongTinHang.run());

        tabChinhSach.add(taoHangNhapLieu("Hạng thành viên:", cboTenHang));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Chi tiêu tối thiểu (₫):", txtChiTieuMin));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Chi tiêu tối đa (₫):", txtChiTieuMax));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Mức giảm giá (%):", txtGiamGia));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));
        tabChinhSach.add(taoHangNhapLieu("Điểm / 10.000 ₫:", txtDiemTichLuy1Van));
        tabChinhSach.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel pQL = new JPanel(new BorderLayout(8, 0));
        pQL.setOpaque(false);
        JLabel lblQL = new JLabel("Quyền lợi áp dụng:");
        lblQL.setPreferredSize(new Dimension(150, 24));
        lblQL.setFont(RangBuocGiaoDien.fontVua(13));
        pQL.add(lblQL, BorderLayout.NORTH);
        pQL.add(new JScrollPane(txtQuyenLoi), BorderLayout.CENTER);
        tabChinhSach.add(pQL);

        tabbedPane.addTab("Cấu hình chính sách hạng", tabChinhSach);

        // TAB 2: Phân hạng / gán hạng cho khách hàng
        JPanel tabPhanHang = new JPanel();
        tabPhanHang.setLayout(new BoxLayout(tabPhanHang, BoxLayout.Y_AXIS));
        tabPhanHang.setBackground(Color.WHITE);
        tabPhanHang.setBorder(new EmptyBorder(16, 20, 16, 20));

        List<String> dsKH = controller.layDanhSachHienThiKhachHang();
        JComboBox<String> cboKhachHang = new JComboBox<>();
        cboKhachHang.setPreferredSize(new Dimension(0, 36));
        cboKhachHang.setFont(RangBuocGiaoDien.fontThuong(13));
        for (String khInfo : dsKH) {
            cboKhachHang.addItem(khInfo);
        }

        JComboBox<String> cboHangMoi = new JComboBox<>(dsTenHang);
        cboHangMoi.setPreferredSize(new Dimension(0, 36));
        cboHangMoi.setFont(RangBuocGiaoDien.fontVua(13));

        JLabel lblThongBaoKH = new JLabel("Gán trực tiếp hạng thành viên cho khách hàng đã đăng ký trong CSDL.");
        lblThongBaoKH.setFont(RangBuocGiaoDien.fontThuong(12));
        lblThongBaoKH.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        tabPhanHang.add(lblThongBaoKH);
        tabPhanHang.add(Box.createRigidArea(new Dimension(0, 14)));
        tabPhanHang.add(taoHangNhapLieu("Khách hàng:", cboKhachHang));
        tabPhanHang.add(Box.createRigidArea(new Dimension(0, 14)));
        tabPhanHang.add(taoHangNhapLieu("Hạng thành viên:", cboHangMoi));
        tabPhanHang.add(Box.createVerticalGlue());

        tabbedPane.addTab("Phân hạng khách hàng", tabPhanHang);

        dialog.add(tabbedPane, BorderLayout.CENTER);

        // Nút lưu / hủy
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panelBottom.setBackground(new Color(248, 250, 252));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(90, 36));
        btnHuy.setFont(RangBuocGiaoDien.fontVua(13));
        btnHuy.setUI(new BasicButtonUI());
        btnHuy.setRolloverEnabled(false);
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = new JButton("Lưu cấu hình");
        btnLuu.setPreferredSize(new Dimension(140, 36));
        btnLuu.setFont(RangBuocGiaoDien.fontDam(13));
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setUI(new BasicButtonUI());
        btnLuu.setRolloverEnabled(false);
        btnLuu.setFocusPainted(false);

        btnLuu.addActionListener(e -> {
            int selectedTab = tabbedPane.getSelectedIndex();
            if (selectedTab == 0) {
                // Lưu chính sách hạng
                String ten = (String) cboTenHang.getSelectedItem();
                String minStr = txtChiTieuMin.getText();
                String maxStr = txtChiTieuMax.getText();
                String giamStr = txtGiamGia.getText();
                String diemStr = txtDiemTichLuy1Van.getText();
                String ql = txtQuyenLoi.getText();

                KetQuaCapNhatChinhSach kq = controller.xuLyCapNhatChinhSach(
                        ten, minStr, maxStr, giamStr, diemStr, ql);
                if (kq.isThanhCong()) {
                    JOptionPane.showMessageDialog(this, kq.getThongBao(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    capNhatThongKe();
                    dialog.dispose();
                } else if (kq.isLoiNhapLieu()) {
                    JOptionPane.showMessageDialog(dialog, kq.getThongBao(), "Lỗi định dạng",
                            JOptionPane.WARNING_MESSAGE);
                    switch (kq.getTruongLoi()) {
                        case MINIMUM: txtChiTieuMin.requestFocus(); break;
                        case MAXIMUM: txtChiTieuMax.requestFocus(); break;
                        case DISCOUNT: txtGiamGia.requestFocus(); break;
                        case POINTS: txtDiemTichLuy1Van.requestFocus(); break;
                        default: break;
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, kq.getThongBao(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Phân hạng khách hàng
                String khSelected = (String) cboKhachHang.getSelectedItem();
                String tenHangMoi = (String) cboHangMoi.getSelectedItem();

                KetQuaPhanHang kq = controller.xuLyPhanHangKhachHang(
                        cboKhachHang.getItemCount(), khSelected, tenHangMoi);
                if (kq.isDanhSachRong()) {
                    JOptionPane.showMessageDialog(dialog, kq.getThongBao(), "Thông báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (kq.isThanhCong()) {
                    JOptionPane.showMessageDialog(this, kq.getThongBao(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    capNhatThongKe();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, kq.getThongBao(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelBottom.add(btnHuy);
        panelBottom.add(btnLuu);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel taoHangNhapLieu(String nhan, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(nhan);
        lbl.setPreferredSize(new Dimension(150, 36));
        lbl.setFont(RangBuocGiaoDien.fontVua(13));
        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        comp.setPreferredSize(new Dimension(0, 36));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        }
        p.add(lbl, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

}
