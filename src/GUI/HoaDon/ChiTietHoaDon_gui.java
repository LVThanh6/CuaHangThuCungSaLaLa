package GUI.HoaDon;

import GUI.Chung.LamMoNen;

import GUI.Chung.NenAnhPanel;

import GUI.Chung.TamNenNoiDung;

import controller.HoaDon.ChiTietHoaDonController;

import entity.HoaDon.ChiTietHoaDon_entity;

import entity.HoaDon.HoaDon_entity;

import entity.KhachHang.KhachHang;

import entity.NhanVien.NhanVien_entity;

import entity.HoaDon.ThanhToan_entity;

import entity.ThuCung.ThuCung_entity;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import javax.swing.border.LineBorder;

import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.table.DefaultTableModel;

import java.awt.*;

import java.awt.event.ActionEvent;

import java.awt.print.Printable;

import java.awt.print.PrinterException;

import java.awt.print.PrinterJob;

import java.awt.image.BufferedImage;

import java.text.DecimalFormat;

import java.text.DecimalFormatSymbols;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * 
 * Màn hình chi tiết hóa đơn.
 * Mở bằng: ChiTietHoaDon_gui.hienThi(this, maHD);
 * 
 */

public class ChiTietHoaDon_gui extends JPanel {
    private final ChiTietHoaDonController controller;

    private static final Color XANH = Color.decode("#0F766E");

    private static final Color NEN = new Color(248, 250, 252);

    private static final Color VIEN = new Color(226, 232, 240);

    private static final Color CHU = new Color(30, 41, 59);

    private static final Color XAM = new Color(100, 116, 139);

    private static final DateTimeFormatter NGAY =

            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat TIEN;

    static {

        DecimalFormatSymbols sym = new DecimalFormatSymbols();

        sym.setGroupingSeparator('.');

        sym.setDecimalSeparator(',');

        TIEN = new DecimalFormat("#,##0", sym);

    }

    // Dữ liệu lấy từ các DAO

    private final HoaDon_entity hd;

    private final ArrayList<ChiTietHoaDon_entity> dong;

    private final ArrayList<ThuCung_entity> thuCung;

    private final String hangThanhVien;
    private final DSHoaDon_gui dsHoaDon;

    private final JPanel noiDung = new TrangCuon();

    private final JButton btnIn = new JButton("In/xuất hóa đơn");

    private boolean dangXuLyIn;

    private JPanel pnlHangThanhToan;

    // Các số liệu tổng hợp

    private double mucKM;

    private double mucThue;

    private double tongTien;

    private double tienKM;

    private double tienThue;

    private double thanhTien;

    private double daThanhToan;

    private double conPhaiThu;

    // ==================== MỞ MÀN HÌNH ====================

    /** Mở chi tiết hóa đơn trong một cửa sổ riêng. */

    public static void hienThi(Component cha, String maHD, DSHoaDon_gui dsHoaDon) {

        ChiTietHoaDonController controller = new ChiTietHoaDonController();
        ChiTietHoaDonController.DuLieuChiTiet duLieu = controller.taiDuLieu(maHD);
        HoaDon_entity hd = duLieu == null ? null : duLieu.getHoaDon();

        if (hd == null) {

            JOptionPane.showMessageDialog(

                    cha,

                    "Không tìm thấy dữ liệu của hóa đơn " + maHD + ".",

                    "Lỗi",

                    JOptionPane.ERROR_MESSAGE

            );

            return;

        }

        Window chu = cha == null

                ? null
                : SwingUtilities.getWindowAncestor(cha);

        JDialog dlg = new JDialog(chu,

                "Chi tiết hóa đơn " + maHD,

                Dialog.ModalityType.APPLICATION_MODAL);

        ChiTietHoaDon_gui chiTiet = new ChiTietHoaDon_gui(controller, duLieu, dsHoaDon);

        // Cửa sổ chi tiết cũng dùng ảnh nền + tấm nền kem mờ giống màn hình chính
        NenAnhPanel nen = new NenAnhPanel();
        if (nen.coAnh()) {
            TamNenNoiDung tam = new TamNenNoiDung(new BorderLayout());
            tam.add(chiTiet, BorderLayout.CENTER);
            nen.add(tam, BorderLayout.CENTER);
            LamMoNen.apDung(chiTiet);
            dlg.setContentPane(nen);
        } else {
            dlg.setContentPane(chiTiet);
        }

        // Nhấn ESC để đóng

        dlg.getRootPane().registerKeyboardAction(

                (ActionEvent e) -> dlg.dispose(),

                KeyStroke.getKeyStroke("ESCAPE"),

                JComponent.WHEN_IN_FOCUSED_WINDOW

        );

        dlg.setSize(1000, 720);

        dlg.setMinimumSize(new Dimension(820, 560));

        dlg.setLocationRelativeTo(cha);

        dlg.setVisible(true);

    }

    // ==================== KHỞI TẠO ====================

    public ChiTietHoaDon_gui(ChiTietHoaDonController controller,
            ChiTietHoaDonController.DuLieuChiTiet duLieu,
            DSHoaDon_gui dsHoaDon) {

        this.controller = controller;

        this.hd = duLieu.getHoaDon();

        this.dong = duLieu.getCacDong();

        this.thuCung = duLieu.getThuCung();
        this.dsHoaDon = dsHoaDon;

        this.hangThanhVien = duLieu.getHangThanhVien();

        apDungTongTien(controller.tinhTongTien(hd, this.dong));

        setLayout(new BorderLayout());

        setBackground(NEN);

        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));

        noiDung.setBackground(NEN);

        noiDung.setBorder(new EmptyBorder(14, 18, 18, 18));

        them(taoBreadcrumb());

        noiDung.add(Box.createVerticalStrut(6));

        them(taoTieuDe());

        noiDung.add(Box.createVerticalStrut(14));

        them(taoHangKhachHangThuCung());

        noiDung.add(Box.createVerticalStrut(12));

        them(taoTheDichVu());

        noiDung.add(Box.createVerticalStrut(12));

        them(taoHangThanhToan());

        JScrollPane scroll = new JScrollPane(noiDung);

        scroll.setBorder(null);

        scroll.getViewport().setBackground(NEN);

        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(scroll, BorderLayout.CENTER);

    }

    private void them(JComponent c) {

        c.setAlignmentX(Component.LEFT_ALIGNMENT);

        noiDung.add(c);

    }

    // ==================== TÍNH TIỀN ====================

    private void apDungTongTien(ChiTietHoaDonController.TongTien tong) {
        mucKM = tong.getMucKhuyenMai();
        mucThue = tong.getMucThue();
        tongTien = tong.getTongTien();
        tienKM = tong.getTienKhuyenMai();
        tienThue = tong.getTienThue();
        thanhTien = tong.getThanhTien();
        daThanhToan = tong.getDaThanhToan();
        conPhaiThu = tong.getConPhaiThu();
    }

    // ==================== TIÊU ĐỀ ====================

    private JLabel taoBreadcrumb() {

        JLabel lbl = new JLabel(

                "Hóa đơn / Danh sách hóa đơn / " + hd.getMaHD());

        lbl.setFont(new Font("Arial", Font.PLAIN, 11));

        lbl.setForeground(XAM);

        return lbl;

    }

    private JPanel taoTieuDe() {

        JPanel tieuDe = new JPanel(new BorderLayout());

        tieuDe.setOpaque(false);

        JPanel trai = new JPanel();

        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        trai.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Chi tiết hóa đơn " + hd.getMaHD());

        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 23));

        lblTieuDe.setForeground(CHU);

        String ngay = hd.getNgayLap() == null

                ? "—"
                : hd.getNgayLap().format(NGAY);

        NhanVien_entity nv = hd.getMaNhanVien();

        JLabel lblMoTa = new JLabel(

                "Ngày lập: " + ngay

                        + " · Nhân viên: "

                        + giaTri(nv == null ? null : nv.getHoTen()));

        lblMoTa.setFont(new Font("Arial", Font.PLAIN, 12));

        lblMoTa.setForeground(XAM);

        trai.add(lblTieuDe);

        trai.add(Box.createVerticalStrut(4));

        trai.add(lblMoTa);

        btnIn.setFont(new Font("Arial", Font.BOLD, 12));

        btnIn.setForeground(Color.WHITE);

        btnIn.setBackground(XANH);

        btnIn.setOpaque(true);

        btnIn.setContentAreaFilled(true);

        btnIn.setBorderPainted(false);

        btnIn.setFocusPainted(false);

        btnIn.setBorder(new EmptyBorder(10, 18, 10, 18));

        btnIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnIn.setEnabled(true);

        btnIn.setToolTipText("In hóa đơn (có thể in lại bất kỳ lúc nào)");

        btnIn.addActionListener(e -> inHoaDon());

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        phai.setOpaque(false);

        phai.add(btnIn);

        tieuDe.add(trai, BorderLayout.CENTER);

        tieuDe.add(phai, BorderLayout.EAST);

        return tieuDe;

    }

    // ==================== KHÁCH HÀNG + THÚ CƯNG ====================

    private JPanel taoHangKhachHangThuCung() {

        JPanel hang = new JPanel(new GridLayout(1, 2, 12, 0));

        hang.setOpaque(false);

        hang.add(taoTheKhachHang());

        hang.add(taoTheThuCung());

        return hang;

    }

    private JPanel taoTheKhachHang() {

        JPanel the = taoThe("Thông tin khách hàng", null);

        KhachHang kh = hd.getMaKH();

        ArrayList<String[]> cap = new ArrayList<>();

        cap.add(new String[] { "Khách hàng",

                kh == null ? "—"

                        : giaTri(kh.getMaKH()) + " — "

                                + giaTri(kh.getHoTen()) });

        cap.add(new String[] { "Số điện thoại",

                giaTri(kh == null ? null : kh.getSdt()) });

        cap.add(new String[] { "Email",

                giaTri(kh == null ? null : kh.getEmail()) });

        cap.add(new String[] { "Địa chỉ",

                giaTri(kh == null ? null : kh.getDiaChi()) });

        if (hangThanhVien != null) {

            cap.add(new String[] { "Hạng thành viên", hangThanhVien });

        }

        the.add(taoDanhSachDong(cap.toArray(new String[0][])),

                BorderLayout.CENTER);

        return the;

    }

    private JPanel taoTheThuCung() {

        JPanel the = taoThe("Thú cưng", null);

        if (hd.getMaTC() == null) {

            JLabel lbl = new JLabel("Hóa đơn vật tư thông thường không gắn với thú cưng.");

            lbl.setFont(new Font("Arial", Font.PLAIN, 12));

            lbl.setForeground(XAM);

            the.add(lbl, BorderLayout.CENTER);

            return the;

        }

        if (thuCung.isEmpty()) {

            JLabel lbl = new JLabel(

                    "Không tìm thấy hồ sơ thú cưng của hóa đơn.");

            lbl.setFont(new Font("Arial", Font.PLAIN, 12));

            lbl.setForeground(XAM);

            the.add(lbl, BorderLayout.CENTER);

            return the;

        }

        ThuCung_entity tc = thuCung.get(0);

        String ngaySinh = tc.getNgaySinh() == null

                ? "—"
                : tc.getNgaySinh().format(NGAY);

        JPanel noi = new JPanel();

        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));

        noi.setOpaque(false);

        noi.add(taoDanhSachDong(new String[][] {

                { "Hồ sơ", giaTri(tc.getMaTC()) + " — "

                        + giaTri(tc.getTenTC()) },

                { "Loài", giaTri(tc.getLoai()) },

                { "Giới tính", giaTri(tc.getGioiTinh()) },

                { "Ngày sinh", ngaySinh },

                { "Đặc điểm", giaTri(tc.getDacDiemNhanDien()) }

        }));

        the.add(noi, BorderLayout.CENTER);

        return the;

    }

    // ==================== DỊCH VỤ, VẬT TƯ VÀ THUỐC ====================

    private JPanel taoTheDichVu() {

        JPanel the = taoThe("Dịch vụ, vật tư và thuốc",

                dong.size() + " khoản mục · Đơn vị tiền tệ: VNĐ");

        String[] cot = {

                "Mã", "Dịch vụ / Vật tư / Thuốc", "Loại",

                "Số lượng", "Đơn giá", "Khuyến mãi", "Tổng tiền"

        };

        DefaultTableModel model = new DefaultTableModel(cot, 0) {

            @Override

            public boolean isCellEditable(int row, int column) {

                return false;

            }

        };

        for (ChiTietHoaDon_entity c : dong) {
            Object[] duLieuDong = controller.taoDongChiTietHienThi(c, mucKM);

            // Định dạng các cột tiền theo cách trình bày của GUI.
            duLieuDong[4] = tien((double) duLieuDong[4]);
            duLieuDong[5] = tien((double) duLieuDong[5]);
            duLieuDong[6] = tien((double) duLieuDong[6]);

            model.addRow(duLieuDong);
        }

        JTable bang = new JTable(model);

        bang.setRowHeight(36);

        bang.setFont(new Font("Arial", Font.PLAIN, 12));

        bang.setForeground(CHU);

        bang.setShowVerticalLines(false);

        bang.setShowHorizontalLines(true);

        bang.setGridColor(new Color(241, 245, 249));

        bang.setIntercellSpacing(new Dimension(0, 1));

        bang.setRowSelectionAllowed(false);

        bang.setFocusable(false);

        bang.getTableHeader().setReorderingAllowed(false);

        bang.getTableHeader().setPreferredSize(new Dimension(0, 36));

        int[] rong = { 80, 260, 100, 80, 110, 110, 120 };

        for (int i = 0; i < cot.length; i++) {

            boolean canPhai = i >= 3;

            bang.getColumnModel().getColumn(i)

                    .setPreferredWidth(rong[i]);

            // Tiêu đề cột

            bang.getColumnModel().getColumn(i).setHeaderRenderer(

                    (t, v, sel, foc, r, c) -> {

                        JLabel l = new JLabel(

                                v == null ? "" : v.toString());

                        l.setOpaque(true);

                        l.setBackground(new Color(241, 245, 249));

                        l.setForeground(new Color(71, 85, 105));

                        l.setFont(new Font("Arial", Font.BOLD, 11));

                        l.setBorder(new EmptyBorder(0, 10, 0, 10));

                        l.setHorizontalAlignment(canPhai

                                ? SwingConstants.RIGHT

                                : SwingConstants.LEFT);

                        return l;

                    });

            // Nội dung cột

            Color mau = i == 0 ? XANH : CHU;

            boolean dam = i == 0 || i == 6;

            bang.getColumnModel().getColumn(i).setCellRenderer(

                    new OTable(canPhai

                            ? SwingConstants.RIGHT

                            : SwingConstants.LEFT, mau, dam));

        }

        JPanel khuBang = new JPanel(new BorderLayout());

        khuBang.setOpaque(false);

        khuBang.add(bang.getTableHeader(), BorderLayout.NORTH);

        khuBang.add(bang, BorderLayout.CENTER);

        the.add(khuBang, BorderLayout.CENTER);

        return the;

    }

    // ==================== THANH TOÁN + TỔNG TIỀN ====================

    private JPanel taoHangThanhToan() {

        pnlHangThanhToan = new JPanel(new GridBagLayout());

        pnlHangThanhToan.setOpaque(false);

        napTheThanhToanVaTongTien();

        return pnlHangThanhToan;

    }

    private void napTheThanhToanVaTongTien() {

        if (pnlHangThanhToan == null)
            return;

        pnlHangThanhToan.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;

        gbc.weighty = 1;

        gbc.gridx = 0;

        gbc.weightx = 0.6;

        gbc.insets = new Insets(0, 0, 0, 0);

        pnlHangThanhToan.add(taoTheThanhToan(), gbc);

        gbc.gridx = 1;

        gbc.weightx = 0.4;

        gbc.insets = new Insets(0, 12, 0, 0);

        pnlHangThanhToan.add(taoTheTongTien(), gbc);

        pnlHangThanhToan.revalidate();

        pnlHangThanhToan.repaint();

    }

    private JPanel taoTheThanhToan() {

        JPanel the = taoThe("Thanh toán", null);

        ThanhToan_entity tt = hd.getMaThanhToan();

        String trangThai = controller.layTrangThaiThanhToan(hd);

        boolean daTT = controller.laHoaDonDaThanhToan(hd);

        Chip chip = daTT

                ? new Chip("Đã thanh toán",

                        new Color(21, 128, 61), new Color(220, 252, 231))

                : new Chip((trangThai == null

                        ? "Chưa thanh toán"
                        : trangThai),

                        new Color(180, 83, 9), new Color(254, 243, 199));

        JLabel lblNgay = new JLabel(

                tt == null || tt.getThoiGian() == null

                        ? ""
                        : tt.getThoiGian().format(NGAY));

        lblNgay.setFont(new Font("Arial", Font.PLAIN, 12));

        lblNgay.setForeground(XAM);

        JPanel hangChip = new JPanel(

                new FlowLayout(FlowLayout.LEFT, 8, 0));

        hangChip.setOpaque(false);

        hangChip.add(chip);

        hangChip.add(lblNgay);

        hangChip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cacDong = taoDanhSachDong(new String[][] {

                { "Phương thức thanh toán",

                        giaTri(tt == null ? null : tt.getPhuongThuc()) },

                { "Mã giao dịch",

                        giaTri(tt == null ? null : tt.getMaThanhToan()) }

        });

        cacDong.setAlignmentX(Component.LEFT_ALIGNMENT);

        String ghiChu;

        if (conPhaiThu > 0) {

            ghiChu = "Còn phải thu " + tien(conPhaiThu)

                    + ". Vui lòng nhắc khách hàng thanh toán.";

        } else if (daThanhToan > 0) {

            ghiChu = "Đã nhận đủ tiền.";

        } else {

            ghiChu = "";

        }

        JLabel lblGhiChu = new JLabel(ghiChu);

        lblGhiChu.setFont(new Font("Arial", Font.PLAIN, 12));

        lblGhiChu.setForeground(XAM);

        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel noi = new JPanel();

        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));

        noi.setOpaque(false);

        noi.add(hangChip);

        noi.add(Box.createVerticalStrut(10));

        noi.add(cacDong);

        noi.add(Box.createVerticalStrut(8));

        noi.add(lblGhiChu);

        the.add(noi, BorderLayout.CENTER);

        return the;

    }

    private JPanel taoTheTongTien() {

        JPanel the = new JPanel(new BorderLayout());

        the.setBackground(Color.WHITE);

        the.setBorder(BorderFactory.createCompoundBorder(

                new LineBorder(VIEN, 1, true),

                new EmptyBorder(14, 16, 14, 16)

        ));

        JPanel noi = new JPanel();

        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));

        noi.setOpaque(false);

        noi.add(taoDong("Tổng tiền", tien(tongTien)));

        noi.add(taoDong(

                "Khuyến mãi (" + phanTram(mucKM) + "%)",

                tienKM > 0 ? "-" + tien(tienKM) : tien(0)));

        noi.add(taoDong(

                "Thuế (" + phanTram(mucThue) + "%)",

                tienThue > 0 ? "+" + tien(tienThue) : tien(0)));

        noi.add(Box.createVerticalStrut(6));

        JSeparator sep = new JSeparator();

        sep.setForeground(VIEN);

        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        noi.add(sep);

        noi.add(Box.createVerticalStrut(8));

        // Dòng "Thành tiền" nổi bật

        JPanel dongThanhTien = new JPanel(new BorderLayout(12, 0));

        dongThanhTien.setOpaque(false);

        JLabel lblNhan = new JLabel("Thành tiền");

        lblNhan.setFont(new Font("Arial", Font.BOLD, 14));

        lblNhan.setForeground(CHU);

        JLabel lblTien = new JLabel(tien(thanhTien));

        lblTien.setFont(new Font("Arial", Font.BOLD, 24));

        lblTien.setForeground(XANH);

        dongThanhTien.add(lblNhan, BorderLayout.WEST);

        dongThanhTien.add(lblTien, BorderLayout.EAST);

        noi.add(dongThanhTien);

        noi.add(Box.createVerticalStrut(8));

        noi.add(taoDong("Đã thanh toán", tien(daThanhToan)));

        noi.add(taoDong("Còn phải thu", tien(conPhaiThu)));

        the.add(noi, BorderLayout.CENTER);

        return the;

    }

    // ==================== HÀM DỰNG GIAO DIỆN CHUNG ====================

    /** Thẻ trắng viền nhẹ, có tiêu đề (và dòng phụ bên phải nếu có). */

    private JPanel taoThe(String tieuDe, String phu) {

        JPanel the = new JPanel(new BorderLayout(0, 10));

        the.setBackground(Color.WHITE);

        the.setBorder(BorderFactory.createCompoundBorder(

                new LineBorder(VIEN, 1, true),

                new EmptyBorder(14, 16, 14, 16)

        ));

        JLabel lbl = new JLabel(tieuDe);

        lbl.setFont(new Font("Arial", Font.BOLD, 14));

        lbl.setForeground(CHU);

        JPanel dau = new JPanel(new BorderLayout());

        dau.setOpaque(false);

        dau.add(lbl, BorderLayout.WEST);

        if (phu != null) {

            JLabel lblPhu = new JLabel(phu);

            lblPhu.setFont(new Font("Arial", Font.PLAIN, 11));

            lblPhu.setForeground(XAM);

            dau.add(lblPhu, BorderLayout.EAST);

        }

        the.add(dau, BorderLayout.NORTH);

        return the;

    }

    /** Nhiều dòng "nhãn ..... giá trị". */

    private JPanel taoDanhSachDong(String[][] cap) {

        JPanel p = new JPanel();

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.setOpaque(false);

        for (String[] c : cap) {

            p.add(taoDong(c[0], c[1]));

        }

        return p;

    }

    private JPanel taoDong(String nhan, String giaTri) {

        JPanel dong = new JPanel(new BorderLayout(12, 0));

        dong.setOpaque(false);

        dong.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel lblNhan = new JLabel(nhan);

        lblNhan.setFont(new Font("Arial", Font.PLAIN, 12));

        lblNhan.setForeground(XAM);

        JLabel lblGiaTri = new JLabel(giaTri);

        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 12));

        lblGiaTri.setForeground(CHU);

        lblGiaTri.setHorizontalAlignment(SwingConstants.RIGHT);

        dong.add(lblNhan, BorderLayout.WEST);

        dong.add(lblGiaTri, BorderLayout.CENTER);

        return dong;

    }

    // ==================== IN / XUẤT HÓA ĐƠN ====================

    private boolean isHoaDonDaThanhToan() {
        return controller.laHoaDonDaThanhToan(hd);
    }

    private void inHoaDon() {
        if (dangXuLyIn) {
            return;
        }

        if (isHoaDonDaThanhToan()) {
            batDauIn(null);
            return;
        }

        String[] ptOptions = { "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng" };
        JComboBox<String> cboPT = new JComboBox<>(ptOptions);
        Object[] thongBao = {
                "Hóa đơn " + hd.getMaHD() + " chưa thanh toán.",
                "Tổng số tiền cần thu: " + tien(thanhTien),
                "Xác nhận khách đã thanh toán và in hóa đơn?",
                "Phương thức thanh toán:", cboPT
        };
        int opt = JOptionPane.showConfirmDialog(
                this, thongBao, "Hóa đơn chưa thanh toán",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String pt = (String) cboPT.getSelectedItem();
            batDauIn(pt != null ? pt : "Tiền mặt");
        }
    }

    private void batDauIn(String phuongThucThanhToan) {
        dangXuLyIn = true;
        btnIn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return phuongThucThanhToan == null
                        || controller.xacNhanThanhToan(hd, phuongThucThanhToan, thanhTien);
            }

            @Override
            protected void done() {
                boolean daKhoiDongIn = false;
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(
                                ChiTietHoaDon_gui.this,
                                "Không thể lưu trạng thái thanh toán vào CSDL!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (phuongThucThanhToan != null) {
                        capNhatHienThiSauThanhToan();
                    }
                    daKhoiDongIn = hienHopThoaiIn();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(
                            ChiTietHoaDon_gui.this,
                            "Đã dừng xử lý in hóa đơn.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (java.util.concurrent.ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                            ChiTietHoaDon_gui.this,
                            "Không thể xác nhận thanh toán: " + ex.getCause().getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (!daKhoiDongIn) {
                        ketThucXuLyIn();
                    }
                }
            }
        };
        worker.execute();
    }

    private boolean hienHopThoaiIn() {
        try {
            Dimension kichThuoc = noiDung.getSize();
            if (kichThuoc.width <= 0 || kichThuoc.height <= 0) {
                kichThuoc = noiDung.getPreferredSize();
            }
            if (kichThuoc.width <= 0 || kichThuoc.height <= 0) {
                throw new PrinterException("Không xác định được kích thước nội dung hóa đơn.");
            }

            BufferedImage anhHoaDon = new BufferedImage(
                    kichThuoc.width, kichThuoc.height, BufferedImage.TYPE_INT_RGB);
            Dimension kichThuocBanDau = noiDung.getSize();
            Graphics2D anhG2 = anhHoaDon.createGraphics();
            try {
                anhG2.setColor(Color.WHITE);
                anhG2.fillRect(0, 0, kichThuoc.width, kichThuoc.height);
                noiDung.setSize(kichThuoc);
                noiDung.doLayout();
                noiDung.printAll(anhG2);
            } finally {
                anhG2.dispose();
                noiDung.setSize(kichThuocBanDau);
                noiDung.doLayout();
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Hoa don " + hd.getMaHD());
            job.setPrintable((g, pf, trang) -> {
                if (trang > 0) {
                    return Printable.NO_SUCH_PAGE;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    double tiLe = Math.min(
                            pf.getImageableWidth() / anhHoaDon.getWidth(),
                            pf.getImageableHeight() / anhHoaDon.getHeight());
                    g2.translate(pf.getImageableX(), pf.getImageableY());
                    g2.scale(tiLe, tiLe);
                    g2.drawImage(anhHoaDon, 0, 0, null);
                    return Printable.PAGE_EXISTS;
                } finally {
                    g2.dispose();
                }
            });

            if (!job.printDialog()) {
                return false;
            }

            btnIn.setText("Đang in...");
            SwingWorker<Void, Void> printWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws PrinterException {
                    job.print();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        JOptionPane.showMessageDialog(
                                ChiTietHoaDon_gui.this,
                                "Đã dừng in hóa đơn.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        JOptionPane.showMessageDialog(
                                ChiTietHoaDon_gui.this,
                                "Không thể in hóa đơn: " + cause.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        ketThucXuLyIn();
                    }
                }
            };
            printWorker.execute();
            return true;
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    this, "Không thể chuẩn bị hóa đơn để in: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void ketThucXuLyIn() {
        dangXuLyIn = false;
        btnIn.setText("In/xuất hóa đơn");
        btnIn.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
    }

    private void capNhatHienThiSauThanhToan() {
        apDungTongTien(controller.tinhTongTien(hd, dong));
        napTheThanhToanVaTongTien();
        noiDung.validate();
        if (dsHoaDon != null) {
            dsHoaDon.taiDuLieu();
        }
    }

    // ==================== HÀM TIỆN ÍCH ====================

    private static String tien(double v) {

        return TIEN.format(Math.round(v)) + " ₫";

    }

    private static String phanTram(double v) {

        return v == Math.rint(v)

                ? String.valueOf((long) v)

                : String.valueOf(v);

    }

    private static String giaTri(String s) {

        return s == null || s.isBlank() ? "—" : s;

    }

    // ==================== CÁC LỚP PHỤ ====================

    /** Panel luôn rộng bằng khung nhìn (không có thanh cuộn ngang). */

    private static class TrangCuon extends JPanel implements Scrollable {

        @Override

        public Dimension getPreferredScrollableViewportSize() {

            return getPreferredSize();

        }

        @Override

        public int getScrollableUnitIncrement(

                Rectangle r, int huong, int chieu) {

            return 20;

        }

        @Override

        public int getScrollableBlockIncrement(

                Rectangle r, int huong, int chieu) {

            return huong == SwingConstants.VERTICAL

                    ? r.height - 40
                    : r.width;

        }

        @Override

        public boolean getScrollableTracksViewportWidth() {

            return true;

        }

        @Override

        public boolean getScrollableTracksViewportHeight() {

            return false;

        }

    }

    /** Ô trong bảng: chọn căn lề, màu chữ, in đậm. */

    private static class OTable extends DefaultTableCellRenderer {

        private final Color mau;

        private final boolean dam;

        OTable(int canLe, Color mau, boolean dam) {

            this.mau = mau;

            this.dam = dam;

            setHorizontalAlignment(canLe);

        }

        @Override

        public Component getTableCellRendererComponent(

                JTable t, Object value, boolean selected,

                boolean focus, int row, int column) {

            super.getTableCellRendererComponent(

                    t, value, false, false, row, column);

            setForeground(mau);

            setFont(new Font("Arial",

                    dam ? Font.BOLD : Font.PLAIN, 12));

            setBorder(new EmptyBorder(0, 10, 0, 10));

            return this;

        }

    }

    /** Nhãn trạng thái bo tròn. */

    private static class Chip extends JLabel {

        private final Color nen;

        Chip(String text, Color chu, Color nen) {

            super(text);

            this.nen = nen;

            setForeground(chu);

            setFont(new Font("Arial", Font.PLAIN, 11));

            setOpaque(false);

            setBorder(new EmptyBorder(3, 10, 3, 10));

        }

        @Override

        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,

                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(nen);

            g2.fillRoundRect(0, 0, getWidth(), getHeight(),

                    getHeight(), getHeight());

            g2.dispose();

            super.paintComponent(g);

        }

    }

}