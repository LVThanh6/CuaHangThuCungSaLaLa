package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel lập hóa đơn bán hàng cho SALALA Pet Shop.
 * Panel độc lập, có thể nhúng vào vùng CENTER của giao diện chính.
 *
 * Ví dụ:
 *   panelNoiDung.removeAll();
 *   panelNoiDung.add(new LapHoaDonPanel(), BorderLayout.CENTER);
 *   panelNoiDung.revalidate();
 *   panelNoiDung.repaint();
 *
 * Khu vực sản phẩm để trống để tích hợp dữ liệu từ Database/DAO sau.
 */
public class LapHoaDonPanel extends JPanel {

    private final Color mauNen = RangBuocGiaoDien.MAU_NEN_TRANG;
    private final Color mauThe = RangBuocGiaoDien.MAU_NEN_THE;
    private final Color mauVien = RangBuocGiaoDien.MAU_DUONG_VIEN;
    private final Color mauChu = RangBuocGiaoDien.MAU_CHU_CHINH;
    private final Color mauChuPhu = RangBuocGiaoDien.MAU_CHU_PHU;
    private final Color mauChuDao = RangBuocGiaoDien.MAU_CHU_DAO;
    private final Color mauChuDaoNhat = RangBuocGiaoDien.MAU_CHU_DAO_NHAT;

    private JTextField txtMaHoaDon;
    private JTextField txtNgayLap;
    private JTextField txtNhanVien;
    private JTextField txtTimSanPham;
    private JTextField txtKhachDua;
    private JTextField txtGhiChu;
    private JComboBox<String> cboKhuyenMai;
    private JComboBox<String> cboPhuongThuc;
    private JLabel lblTamTinh;
    private JLabel lblGiamGia;
    private JLabel lblThue;
    private JLabel lblTongPhaiThu;
    private JLabel lblTienThoi;
    private JLabel lblKhachHang;
    private JLabel lblThuCung;
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;

    private String phuongThucThanhToan = "Tiền mặt";

    public LapHoaDonPanel() {
        setLayout(new BorderLayout());
        setBackground(mauNen);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
    }

    private JPanel taoTieuDe() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel breadcrumb = nhan("Hóa đơn  /  Lập hóa đơn mới", 11, mauChuPhu, false);
        JLabel title = nhan("Lập Hóa đơn Bán hàng", 20, mauChu, true);
        JLabel subtitle = nhan("Tạo hóa đơn, chọn khách hàng và thú cưng, thêm sản phẩm/dịch vụ rồi thanh toán.", 11, mauChuPhu, false);

        p.add(breadcrumb);
        p.add(Box.createVerticalStrut(3));
        p.add(title);
        p.add(Box.createVerticalStrut(3));
        p.add(subtitle);
        p.setBorder(new EmptyBorder(0, 0, 12, 0));
        return p;
    }

    private JPanel taoNoiDung() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setOpaque(false);

        JPanel benTrai = new JPanel(new BorderLayout(0, 10));
        benTrai.setOpaque(false);
        benTrai.add(taoThongTinHoaDon(), BorderLayout.NORTH);

        JPanel khuVucDuoi = new JPanel(new BorderLayout(0, 10));
        khuVucDuoi.setOpaque(false);
        khuVucDuoi.add(taoChonKhachHangThuCung(), BorderLayout.NORTH);
        khuVucDuoi.add(taoKhuVucSanPham(), BorderLayout.CENTER);
        khuVucDuoi.add(taoBangChiTiet(), BorderLayout.SOUTH);
        benTrai.add(khuVucDuoi, BorderLayout.CENTER);

        JPanel thanhToan = taoThanhToan();
        thanhToan.setPreferredSize(new Dimension(265, 100));

        wrapper.add(benTrai, BorderLayout.CENTER);
        wrapper.add(thanhToan, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel taoThongTinHoaDon() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 0));
        p.setOpaque(false);

        txtMaHoaDon = new JTextField("Tự động tạo khi lưu");
        txtNgayLap = new JTextField(java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNhanVien = new JTextField();

        p.add(taoTheTruong("Mã hóa đơn (tự động)", txtMaHoaDon));
        p.add(taoTheTruong("Ngày lập", txtNgayLap));
        p.add(taoTheTruong("Nhân viên lập", txtNhanVien));
        return p;
    }

    private JPanel taoChonKhachHangThuCung() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setOpaque(false);

        JPanel khach = taoThe();
        khach.setLayout(new BorderLayout(0, 8));
        JPanel tieuDeKhach = new JPanel(new BorderLayout());
        tieuDeKhach.setOpaque(false);
        tieuDeKhach.add(nhan("Khách hàng", 11, mauChuPhu, true), BorderLayout.WEST);
        JLabel daChonKH = nhan("Chưa chọn", 9, mauChuDao, true);
        tieuDeKhach.add(daChonKH, BorderLayout.EAST);
        lblKhachHang = nhan("Chưa chọn khách hàng", 12, mauChu, true);
        JButton chonKhach = nutPhu("Chọn khách hàng");
        chonKhach.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Kết nối nút này với màn hình chọn khách hàng hoặc DAO khách hàng.",
                "Chọn khách hàng", JOptionPane.INFORMATION_MESSAGE));
        JPanel dongKhach = new JPanel(new BorderLayout(8, 0));
        dongKhach.setOpaque(true);
        dongKhach.setBackground(mauChuDaoNhat);
        dongKhach.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1, true),
                new EmptyBorder(8, 9, 8, 9)));
        dongKhach.add(lblKhachHang, BorderLayout.CENTER);
        dongKhach.add(chonKhach, BorderLayout.EAST);
        khach.add(tieuDeKhach, BorderLayout.NORTH);
        khach.add(dongKhach, BorderLayout.CENTER);

        JPanel thuCung = taoThe();
        thuCung.setLayout(new BorderLayout(0, 8));
        JPanel tieuDePet = new JPanel(new BorderLayout());
        tieuDePet.setOpaque(false);
        tieuDePet.add(nhan("Thú cưng", 11, mauChuPhu, true), BorderLayout.WEST);
        tieuDePet.add(nhan("Chưa chọn", 9, mauChuDao, true), BorderLayout.EAST);
        lblThuCung = nhan("Chưa chọn thú cưng", 12, mauChu, true);
        JButton chonThuCung = nutPhu("Chọn thú cưng");
        chonThuCung.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Kết nối nút này với màn hình chọn thú cưng hoặc DAO thú cưng.",
                "Chọn thú cưng", JOptionPane.INFORMATION_MESSAGE));
        JPanel dongPet = new JPanel(new BorderLayout(8, 0));
        dongPet.setOpaque(true);
        dongPet.setBackground(mauChuDaoNhat);
        dongPet.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1, true),
                new EmptyBorder(8, 9, 8, 9)));
        dongPet.add(lblThuCung, BorderLayout.CENTER);
        dongPet.add(chonThuCung, BorderLayout.EAST);
        thuCung.add(tieuDePet, BorderLayout.NORTH);
        thuCung.add(dongPet, BorderLayout.CENTER);

        p.add(khach);
        p.add(thuCung);
        return p;
    }

    private JPanel taoKhuVucSanPham() {
        JPanel p = taoThe();
        p.setLayout(new BorderLayout(0, 8));

        JPanel tren = new JPanel(new BorderLayout(8, 0));
        tren.setOpaque(false);
        tren.add(nhan("Tra cứu & Thêm sản phẩm / Dịch vụ", 13, mauChu, true), BorderLayout.WEST);
        tren.add(nhan("Dữ liệu sẽ được nạp từ cơ sở dữ liệu", 10, mauChuPhu, false), BorderLayout.EAST);
        p.add(tren, BorderLayout.NORTH);

        txtTimSanPham = new JTextField();
        txtTimSanPham.setFont(RangBuocGiaoDien.fontThuong(12));
        txtTimSanPham.setForeground(mauChuPhu);
        txtTimSanPham.setToolTipText("Nhập tên hoặc mã sản phẩm/dịch vụ để tìm trong cơ sở dữ liệu");
        txtTimSanPham.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true),
                new EmptyBorder(9, 10, 9, 10)));
        p.add(txtTimSanPham, BorderLayout.CENTER);

        JPanel trang = new JPanel(new BorderLayout());
        trang.setOpaque(true);
        trang.setBackground(new Color(250, 252, 254));
        trang.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel icon = nhan("＋", 24, mauChuDao, true);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        trang.add(icon, BorderLayout.WEST);
        JPanel thongBao = new JPanel();
        thongBao.setOpaque(false);
        thongBao.setLayout(new BoxLayout(thongBao, BoxLayout.Y_AXIS));
        thongBao.add(nhan("Chưa có sản phẩm/dịch vụ", 12, mauChu, true));
        thongBao.add(Box.createVerticalStrut(4));
        thongBao.add(nhan("Khu vực này sẽ hiển thị kết quả truy vấn từ Database.", 10, mauChuPhu, false));
        trang.add(thongBao, BorderLayout.CENTER);
        p.add(trang, BorderLayout.SOUTH);
        return p;
    }

    private JPanel taoBangChiTiet() {
        JPanel p = taoThe();
        p.setLayout(new BorderLayout(0, 8));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(nhan("Chi tiết hóa đơn", 13, mauChu, true), BorderLayout.WEST);
        JButton themDong = nutPhu("+  Thêm dòng");
        themDong.addActionListener(e -> themDongTrong());
        heading.add(themDong, BorderLayout.EAST);
        p.add(heading, BorderLayout.NORTH);

        String[] cot = {"Tên sản phẩm / Dịch vụ", "SL", "Đơn giá (đ)", "Thành tiền (đ)", "Xóa"};
        modelChiTiet = new DefaultTableModel(cot, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setFont(RangBuocGiaoDien.fontThuong(11));
        tblChiTiet.setForeground(mauChu);
        tblChiTiet.setBackground(Color.WHITE);
        tblChiTiet.setRowHeight(31);
        tblChiTiet.setShowVerticalLines(false);
        tblChiTiet.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        tblChiTiet.setFillsViewportHeight(true);
        tblChiTiet.getTableHeader().setFont(RangBuocGiaoDien.fontDam(10));
        tblChiTiet.getTableHeader().setForeground(mauChuPhu);
        tblChiTiet.getTableHeader().setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
        tblChiTiet.getTableHeader().setPreferredSize(new Dimension(10, 30));
        tblChiTiet.getTableHeader().setReorderingAllowed(false);
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(45);

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(new LineBorder(mauVien, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(500, 190));
        p.add(scroll, BorderLayout.CENTER);

        JPanel tong = new JPanel(new BorderLayout());
        tong.setOpaque(false);
        tong.setBorder(new EmptyBorder(5, 0, 0, 0));
        tong.add(nhan("Tổng cộng (0 dòng)", 10, mauChuPhu, false), BorderLayout.WEST);
        tong.add(nhan("0 đ", 12, mauChu, true), BorderLayout.EAST);
        p.add(tong, BorderLayout.SOUTH);
        return p;
    }

    private JPanel taoThanhToan() {
        JPanel p = taoThe();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true), new EmptyBorder(14, 13, 14, 13)));
        p.add(nhan("Thanh toán & Xuất hóa đơn", 14, mauChu, true));
        p.add(Box.createVerticalStrut(15));

        p.add(nhanTien("Tạm tính", "0 đ", false));
        p.add(Box.createVerticalStrut(9));

        p.add(nhan("Chương trình khuyến mãi", 10, mauChuPhu, false));
        p.add(Box.createVerticalStrut(5));
        cboKhuyenMai = new JComboBox<>(new String[]{"Không áp dụng khuyến mãi"});
        cboKhuyenMai.setFont(RangBuocGiaoDien.fontThuong(11));
        cboKhuyenMai.setBackground(Color.WHITE);
        cboKhuyenMai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(cboKhuyenMai);
        p.add(Box.createVerticalStrut(8));

        p.add(nhanTien("Giảm giá", "0 đ", false));
        p.add(Box.createVerticalStrut(8));
        p.add(nhanTien("Thuế VAT", "0 đ", false));
        p.add(Box.createVerticalStrut(10));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(10));

        JPanel tong = new JPanel(new BorderLayout(4, 0));
        tong.setOpaque(false);
        tong.add(nhan("Tổng phải thu", 13, mauChu, true), BorderLayout.WEST);
        lblTongPhaiThu = nhan("0 đ", 20, mauChuDao, true);
        tong.add(lblTongPhaiThu, BorderLayout.EAST);
        p.add(tong);
        p.add(Box.createVerticalStrut(17));

        p.add(nhan("Phương thức thanh toán", 11, mauChuPhu, false));
        p.add(Box.createVerticalStrut(7));
        JPanel phuongThuc = new JPanel(new GridLayout(1, 3, 5, 0));
        phuongThuc.setOpaque(false);
        JButton tienMat = nutPhuongThuc("Tiền mặt", true);
        JButton chuyenKhoan = nutPhuongThuc("Chuyển khoản", false);
        JButton theATM = nutPhuongThuc("Thẻ ATM", false);
        tienMat.addActionListener(e -> chonPhuongThuc("Tiền mặt", tienMat, chuyenKhoan, theATM));
        chuyenKhoan.addActionListener(e -> chonPhuongThuc("Chuyển khoản", tienMat, chuyenKhoan, theATM));
        theATM.addActionListener(e -> chonPhuongThuc("Thẻ ATM", tienMat, chuyenKhoan, theATM));
        phuongThuc.add(tienMat);
        phuongThuc.add(chuyenKhoan);
        phuongThuc.add(theATM);
        p.add(phuongThuc);
        p.add(Box.createVerticalStrut(15));

        p.add(nhan("Khách đưa (đ)", 11, mauChuPhu, false));
        p.add(Box.createVerticalStrut(5));
        txtKhachDua = new JTextField();
        txtKhachDua.setFont(RangBuocGiaoDien.fontDam(13));
        txtKhachDua.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1, true),
                new EmptyBorder(9, 8, 9, 8)));
        txtKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(txtKhachDua);
        p.add(Box.createVerticalStrut(8));

        p.add(nhanTien("Tiền thối lại", "0 đ", true));
        p.add(Box.createVerticalStrut(13));

        p.add(nhan("Ghi chú hóa đơn", 11, mauChuPhu, false));
        p.add(Box.createVerticalStrut(5));
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(RangBuocGiaoDien.fontThuong(11));
        txtGhiChu.setToolTipText("Nhập ghi chú cho hóa đơn này...");
        txtGhiChu.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true),
                new EmptyBorder(9, 8, 9, 8)));
        txtGhiChu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(txtGhiChu);
        p.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new GridLayout(1, 2, 7, 0));
        actions.setOpaque(false);
        JButton luu = nutPhu("Lưu nháp");
        JButton thanhToan = nutChinh("Thanh toán");
        luu.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Giao diện đã sẵn sàng. Hãy kết nối nút Lưu nháp với DAO hóa đơn.",
                "Lưu nháp", JOptionPane.INFORMATION_MESSAGE));
        thanhToan.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Hãy kết nối bước kiểm tra dữ liệu và lưu thanh toán với Database.",
                "Thanh toán", JOptionPane.INFORMATION_MESSAGE));
        actions.add(luu);
        actions.add(thanhToan);
        p.add(actions);

        return p;
    }

    private JPanel nhanTien(String ten, String giaTri, boolean nhanManh) {
        JPanel dong = new JPanel(new BorderLayout(4, 0));
        dong.setOpaque(false);
        dong.add(nhan(ten, 11, mauChuPhu, false), BorderLayout.WEST);
        JLabel value = nhan(giaTri, nhanManh ? 12 : 11, nhanManh ? mauChuDao : mauChu, true);
        dong.add(value, BorderLayout.EAST);
        // Trả lại label giá trị để có thể cập nhật sau khi nối dữ liệu.
        if ("Tạm tính".equals(ten)) lblTamTinh = value;
        else if ("Giảm giá".equals(ten)) lblGiamGia = value;
        else if ("Thuế VAT".equals(ten)) lblThue = value;
        else if ("Tiền thối lại".equals(ten)) lblTienThoi = value;
        return dong;
    }

    private void chonPhuongThuc(String ten, JButton a, JButton b, JButton c) {
        phuongThucThanhToan = ten;
        datTrangThaiNut(a, "Tiền mặt".equals(ten));
        datTrangThaiNut(b, "Chuyển khoản".equals(ten));
        datTrangThaiNut(c, "Thẻ ATM".equals(ten));
    }

    private void datTrangThaiNut(JButton b, boolean selected) {
        b.setBackground(selected ? mauChuDaoNhat : Color.WHITE);
        b.setForeground(selected ? mauChuDao : mauChuPhu);
        b.setBorder(new LineBorder(selected ? mauChuDao : mauVien, 1, true));
    }

    private JButton nutPhuongThuc(String text, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(RangBuocGiaoDien.fontVua(10));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(selected ? mauChuDaoNhat : Color.WHITE);
        b.setForeground(selected ? mauChuDao : mauChuPhu);
        b.setBorder(new LineBorder(selected ? mauChuDao : mauVien, 1, true));
        b.setMargin(new Insets(7, 3, 7, 3));
        return b;
    }

    private JPanel taoTheTruong(String tieuDe, JTextField field) {
        JPanel p = taoThe();
        p.setLayout(new BorderLayout(0, 5));
        p.add(nhan(tieuDe, 10, mauChuPhu, true), BorderLayout.NORTH);
        field.setFont(RangBuocGiaoDien.fontDam(11));
        field.setForeground(mauChu);
        field.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        p.add(field, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(100, 60));
        return p;
    }

    private JPanel taoThe() {
        JPanel p = new JPanel();
        p.setBackground(mauThe);
        p.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true),
                new EmptyBorder(10, 11, 10, 11)));
        return p;
    }

    private JLabel nhan(String text, int size, Color color, boolean dam) {
        JLabel l = new JLabel(text);
        l.setFont(dam ? RangBuocGiaoDien.fontDam(size) : RangBuocGiaoDien.fontThuong(size));
        l.setForeground(color);
        return l;
    }

    private JButton nutPhu(String text) {
        JButton b = new JButton(text);
        b.setFont(RangBuocGiaoDien.fontDam(10));
        b.setForeground(mauChuDao);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(mauVien, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton nutChinh(String text) {
        JButton b = new JButton(text);
        b.setFont(RangBuocGiaoDien.fontDam(11));
        b.setForeground(Color.WHITE);
        b.setBackground(mauChuDao);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 10, 9, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void themDongTrong() {
        modelChiTiet.addRow(new Object[]{"", 1, "0", "0", "Xóa"});
    }

    /** Getter để lấy phương thức thanh toán khi tích hợp xử lý nghiệp vụ. */
    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public JTable getBangChiTietHoaDon() {
        return tblChiTiet;
    }

    public JTextField getOTimSanPham() {
        return txtTimSanPham;
    }
}
