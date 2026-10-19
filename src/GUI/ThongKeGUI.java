package GUI;

import dao.ThongKe_DAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Thống kê - Báo cáo hoạt động và doanh thu SALALA Pet Shop.
 * Tích hợp đa dạng biểu đồ trực quan (Cột, Đường/Miền, Donut/Tròn, Phân bố thú cưng),
 * và hỗ trợ xuất báo cáo chuyên nghiệp định dạng PDF và Excel/CSV.
 */
public class ThongKeGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    private ThongKe_DAO thongKeDAO;
    private JComboBox<String> cbKhoangThoiGian;
    private JLabel lblTongDoanhThuSo;
    private JLabel lblTongHoaDonSo;
    private JLabel lblKhachMoiSo;
    private JLabel lblLuotChamSocSo;

    // Thành phần biểu đồ tương tác
    private BieuDoPanel bieuDoPanel;
    private JButton[] btnLoaiBieuDo;

    private JPanel panelCoCauContainer;
    private TableMau bangTopDichVu;
    private TableMau bangDoanhThuNhanVien;

    private final DecimalFormat dfTien = new DecimalFormat("#,### ₫");

    public ThongKeGUI() {
        thongKeDAO = new ThongKe_DAO();
        khoiTaoGiaoDien();
        taiDuLieuThongKe();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        add(taoPhanTren(), BorderLayout.NORTH);
        add(taoPhanNoiDung(), BorderLayout.CENTER);
    }

    // ==========================================
    // 1. PHẦN TRÊN: TIÊU ĐỀ + BỘ LỌC + KPI
    // ==========================================
    private JPanel taoPhanTren() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Hàng 1: Breadcrumb + Tiêu đề + Nút Xuất PDF & Excel
        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);

        JPanel benTrai = new JPanel();
        benTrai.setLayout(new BoxLayout(benTrai, BoxLayout.Y_AXIS));
        benTrai.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("Thống kê / Báo cáo doanh thu & phân tích hoạt động");
        lblBreadcrumb.setFont(RangBuocGiaoDien.fontThuong(12));
        lblBreadcrumb.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JLabel lblTitle = new JLabel("Thống Kê & Báo Cáo Tổng Hợp");
        lblTitle.setFont(RangBuocGiaoDien.fontDam(22));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblSub = new JLabel("Dữ liệu tự động cập nhật từ hệ thống bán hàng, dịch vụ khám chữa bệnh SALALA.");
        lblSub.setFont(RangBuocGiaoDien.fontThuong(12));
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        benTrai.add(lblBreadcrumb);
        benTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        benTrai.add(lblTitle);
        benTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        benTrai.add(lblSub);
        hangTieuDe.add(benTrai, BorderLayout.WEST);

        // Bên phải: Chọn kỳ báo cáo & 2 Nút Xuất (PDF + Excel)
        JPanel benPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        benPhai.setOpaque(false);

        cbKhoangThoiGian = new JComboBox<>(new String[] {
                "Tháng này (Tháng 10/2026)",
                "Hôm nay",
                "Tuần này",
                "Quý 3/2026",
                "Cả năm 2026"
        });
        cbKhoangThoiGian.setFont(RangBuocGiaoDien.fontThuong(12));
        cbKhoangThoiGian.setPreferredSize(new Dimension(195, 36));
        cbKhoangThoiGian.setBackground(Color.WHITE);
        cbKhoangThoiGian.addActionListener(e -> taiDuLieuThongKe());

        // Nút Xuất PDF nổi bật
        JButton btnXuatPDF = new JButton("📄 Xuất báo cáo PDF");
        btnXuatPDF.setFont(RangBuocGiaoDien.fontDam(12));
        btnXuatPDF.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnXuatPDF.setForeground(Color.WHITE);
        btnXuatPDF.setFocusPainted(false);
        btnXuatPDF.setBorderPainted(false);
        btnXuatPDF.setPreferredSize(new Dimension(160, 36));
        btnXuatPDF.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXuatPDF.addActionListener(e -> xuLyXuatPDF());

        // Nút Xuất Excel phụ
        JButton btnXuatExcel = new JButton("📥 Xuất Excel/CSV");
        btnXuatExcel.setFont(RangBuocGiaoDien.fontVua(12));
        btnXuatExcel.setBackground(Color.WHITE);
        btnXuatExcel.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        btnXuatExcel.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        btnXuatExcel.setFocusPainted(false);
        btnXuatExcel.setPreferredSize(new Dimension(140, 36));
        btnXuatExcel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXuatExcel.addActionListener(e -> xuLyXuatExcel());

        benPhai.add(cbKhoangThoiGian);
        benPhai.add(btnXuatPDF);
        benPhai.add(btnXuatExcel);
        hangTieuDe.add(benPhai, BorderLayout.EAST);

        p.add(hangTieuDe);
        p.add(Box.createRigidArea(new Dimension(0, 14)));

        // Hàng 2: 4 Thẻ chỉ số KPI chính
        JPanel panelCards = new JPanel(new GridLayout(1, 4, 14, 0));
        panelCards.setOpaque(false);

        lblTongDoanhThuSo = new JLabel("0 ₫");
        lblTongHoaDonSo = new JLabel("0");
        lblKhachMoiSo = new JLabel("0");
        lblLuotChamSocSo = new JLabel("0");

        panelCards.add(taoTheKPI("💰", "Tổng doanh thu", lblTongDoanhThuSo, "+18,5% so với kỳ trước", true));
        panelCards.add(taoTheKPI("🧾", "Tổng số hóa đơn", lblTongHoaDonSo, "Tỷ lệ thanh toán 91%", false));
        panelCards.add(taoTheKPI("👥", "Tổng khách hàng", lblKhachMoiSo, "Hồ sơ đăng ký hệ thống", false));
        panelCards.add(taoTheKPI("🐾", "Tổng số thú cưng", lblLuotChamSocSo, "Đang chăm sóc & điều trị", false));

        p.add(panelCards);
        return p;
    }

    private JPanel taoTheKPI(String icon, String tieuDe, JLabel lblGiaTri, String ghiChu, boolean noiBat) {
        JPanel the = new JPanel(new BorderLayout(12, 0));
        the.setBackground(Color.WHITE);
        the.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        lblIcon.setPreferredSize(new Dimension(44, 44));
        lblIcon.setBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 1, true));

        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblGiaTri.setFont(RangBuocGiaoDien.fontDam(18));
        lblGiaTri.setForeground(noiBat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblPhu = new JLabel(ghiChu);
        lblPhu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblPhu.setForeground(noiBat ? new Color(22, 163, 74) : RangBuocGiaoDien.MAU_CHU_GOI_Y);

        thongTin.add(lblTieuDe);
        thongTin.add(Box.createRigidArea(new Dimension(0, 2)));
        thongTin.add(lblGiaTri);
        thongTin.add(Box.createRigidArea(new Dimension(0, 2)));
        thongTin.add(lblPhu);

        the.add(lblIcon, BorderLayout.WEST);
        the.add(thongTin, BorderLayout.CENTER);
        return the;
    }

    // ==========================================
    // 2. PHẦN NỘI DUNG: BIỂU ĐỒ ĐA DẠNG + BẢNG SỐ LIỆU
    // ==========================================
    private JPanel taoPhanNoiDung() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);

        // Cột trái: Khung Biểu đồ đa dạng + Bảng Top dịch vụ
        JPanel cotTrai = new JPanel(new BorderLayout(0, 14));
        cotTrai.setOpaque(false);

        cotTrai.add(taoKhungBieuDo(), BorderLayout.NORTH);
        cotTrai.add(taoBangTopDichVu(), BorderLayout.CENTER);

        // Cột phải: Cơ cấu chi tiết + Bảng Doanh số nhân viên
        JPanel cotPhai = new JPanel(new BorderLayout(0, 14));
        cotPhai.setOpaque(false);

        panelCoCauContainer = new JPanel();
        panelCoCauContainer.setLayout(new BoxLayout(panelCoCauContainer, BoxLayout.Y_AXIS));
        panelCoCauContainer.setBackground(Color.WHITE);
        panelCoCauContainer.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        cotPhai.add(panelCoCauContainer, BorderLayout.NORTH);
        cotPhai.add(taoBangDoanhThuNhanVien(), BorderLayout.CENTER);

        p.add(cotTrai);
        p.add(cotPhai);
        return p;
    }

    /**
     * Khung chứa biểu đồ tương tác với thanh chuyển đổi 4 loại biểu đồ
     */
    private JPanel taoKhungBieuDo() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 16, 14, 16)));

        // Thanh công cụ chuyển đổi biểu đồ phía trên
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Biểu đồ phân tích kinh doanh");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(14));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        toolbar.add(lblTieuDe, BorderLayout.WEST);

        // Bộ nút bấm chuyển đổi 4 loại biểu đồ
        JPanel pNutChuyen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pNutChuyen.setOpaque(false);

        String[] tenLoai = { "📊 Cột (12 Tháng)", "📈 Xu hướng", "🍩 Tròn (Cơ cấu)", "🐾 Phân bố thú cưng" };
        int[] maLoai = { BieuDoPanel.BIEU_DO_COT, BieuDoPanel.BIEU_DO_DUONG, BieuDoPanel.BIEU_DO_TRON, BieuDoPanel.BIEU_DO_PHAN_BO };

        btnLoaiBieuDo = new JButton[tenLoai.length];
        for (int i = 0; i < tenLoai.length; i++) {
            final int index = i;
            final int type = maLoai[i];
            JButton b = new JButton(tenLoai[i]);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setMargin(new Insets(4, 8, 4, 8));

            if (i == 0) {
                b.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
                b.setForeground(Color.WHITE);
                b.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 1, true));
            } else {
                b.setBackground(new Color(248, 250, 252));
                b.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                b.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            }

            b.addActionListener(e -> {
                bieuDoPanel.setLoaiBieuDo(type);
                capNhatTrangThaiNutChon(index);
            });
            btnLoaiBieuDo[i] = b;
            pNutChuyen.add(b);
        }

        toolbar.add(pNutChuyen, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Khối vẽ biểu đồ BieuDoPanel
        bieuDoPanel = new BieuDoPanel();
        card.add(bieuDoPanel, BorderLayout.CENTER);

        return card;
    }

    private void capNhatTrangThaiNutChon(int activeIndex) {
        for (int i = 0; i < btnLoaiBieuDo.length; i++) {
            if (i == activeIndex) {
                btnLoaiBieuDo[i].setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnLoaiBieuDo[i].setForeground(Color.WHITE);
                btnLoaiBieuDo[i].setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 1, true));
            } else {
                btnLoaiBieuDo[i].setBackground(new Color(248, 250, 252));
                btnLoaiBieuDo[i].setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                btnLoaiBieuDo[i].setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            }
        }
    }

    private JPanel taoBangTopDichVu() {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(Color.WHITE);
        the.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel lblTieuDe = new JLabel("Top 5 Dịch vụ & Sản phẩm phổ biến nhất");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(13));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        the.add(lblTieuDe, BorderLayout.NORTH);

        String[] cot = { "Tên dịch vụ / sản phẩm", "Số lượt", "Doanh thu" };
        bangTopDichVu = new TableMau(cot, new Object[][]{});

        JScrollPane scroll = new JScrollPane(bangTopDichVu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        the.add(scroll, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoBangDoanhThuNhanVien() {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(Color.WHITE);
        the.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel lblTieuDe = new JLabel("Doanh số phục vụ theo nhân viên & Bác sĩ");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(13));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        the.add(lblTieuDe, BorderLayout.NORTH);

        String[] cot = { "Mã NV", "Họ tên", "Vai trò", "Hóa đơn", "Doanh số" };
        bangDoanhThuNhanVien = new TableMau(cot, new Object[][]{});

        JScrollPane scroll = new JScrollPane(bangDoanhThuNhanVien);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        the.add(scroll, BorderLayout.CENTER);
        return the;
    }

    // ==========================================
    // TẢI DỮ LIỆU & LÀM MỚI BIỂU ĐỒ
    // ==========================================
    public void taiDuLieuThongKe() {
        String ky = (String) cbKhoangThoiGian.getSelectedItem();

        // 1. Cập nhật thẻ KPI
        double doanhThu = thongKeDAO.getTongDoanhThu(ky);
        int soHD = thongKeDAO.getTongSoHoaDon();
        int soKH = thongKeDAO.getTongSoKhachHang();
        int soTC = thongKeDAO.getTongSoThuCung();

        lblTongDoanhThuSo.setText(dfTien.format(doanhThu));
        lblTongHoaDonSo.setText(soHD + " đơn");
        lblKhachMoiSo.setText(soKH + " khách");
        lblLuotChamSocSo.setText(soTC + " bé");

        // 2. Tải dữ liệu biểu đồ
        Map<String, Double> doanhThuThang = thongKeDAO.getDoanhThuCacThang();
        Map<String, Long> coCau = thongKeDAO.getCoCauDoanhThu();
        Map<String, Integer> phanBoThuCung = thongKeDAO.getPhanBoThuCung();

        if (bieuDoPanel != null) {
            bieuDoPanel.capNhatDuLieu(doanhThuThang, coCau, phanBoThuCung);
        }

        // 3. Cập nhật khối thanh tỷ lệ cơ cấu doanh thu bên phải
        panelCoCauContainer.removeAll();
        JLabel lblTieuDe = new JLabel("Chi tiết cơ cấu doanh thu theo nhóm");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(13));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        panelCoCauContainer.add(lblTieuDe);
        panelCoCauContainer.add(Box.createRigidArea(new Dimension(0, 10)));

        long tongCoCau = 0;
        for (long val : coCau.values()) tongCoCau += val;
        if (tongCoCau == 0) tongCoCau = (long) doanhThu;

        Color[] mauNhom = {
                new Color(13, 148, 136),
                new Color(14, 165, 233),
                new Color(245, 158, 11),
                new Color(139, 92, 246)
        };
        int idx = 0;
        for (Map.Entry<String, Long> entry : coCau.entrySet()) {
            Color m = mauNhom[idx % mauNhom.length];
            panelCoCauContainer.add(taoThanhTyLe(entry.getKey(), entry.getValue(), tongCoCau, m));
            panelCoCauContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            idx++;
        }
        panelCoCauContainer.revalidate();
        panelCoCauContainer.repaint();

        // 4. Cập nhật bảng Top Dịch vụ
        List<Object[]> topDV = thongKeDAO.getTopDichVuSanPham();
        Object[][] duLieuDV = new Object[topDV.size()][3];
        for (int i = 0; i < topDV.size(); i++) {
            duLieuDV[i] = topDV.get(i);
        }
        bangTopDichVu.datLaiDuLieu(duLieuDV, new String[]{ "Tên dịch vụ / sản phẩm", "Số lượt", "Doanh thu" });

        // 5. Cập nhật bảng Doanh số nhân viên
        List<Object[]> dsNV = thongKeDAO.getDoanhSoNhanVien();
        Object[][] duLieuNV = new Object[dsNV.size()][5];
        for (int i = 0; i < dsNV.size(); i++) {
            duLieuNV[i] = dsNV.get(i);
        }
        bangDoanhThuNhanVien.datLaiDuLieu(duLieuNV, new String[]{ "Mã NV", "Họ tên", "Vai trò", "Hóa đơn", "Doanh số" });
    }

    private JPanel taoThanhTyLe(String ten, long giaTri, long tong, Color mau) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        int phanTram = (tong > 0) ? (int) Math.round((double) giaTri * 100 / tong) : 0;

        JPanel hangTren = new JPanel(new BorderLayout());
        hangTren.setOpaque(false);
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblTien = new JLabel(dfTien.format(giaTri) + " (" + phanTram + "%)");
        lblTien.setFont(RangBuocGiaoDien.fontDam(12));
        lblTien.setForeground(mau);

        hangTren.add(lblTen, BorderLayout.WEST);
        hangTren.add(lblTien, BorderLayout.EAST);
        p.add(hangTren);
        p.add(Box.createRigidArea(new Dimension(0, 4)));

        JComponent thanhTienTrinh = new JComponent() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                RangBuocGiaoDien.batKhuRangCua(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                int wPhanTram = (int) (w * (phanTram / 100.0));
                g2.setColor(mau);
                g2.fillRoundRect(0, 0, Math.max(wPhanTram, 6), h, 6, 6);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 7);
            }
        };
        p.add(thanhTienTrinh);
        return p;
    }

    // ==========================================
    // 3. XUẤT BÁO CÁO PDF CHUYÊN NGHIỆP
    // ==========================================
    private void xuLyXuatPDF() {
        JFileChooser chooser = new JFileChooser();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
        chooser.setSelectedFile(new File("BaoCao_ThongKe_SALALA_" + sdf.format(new Date()) + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                f = new File(f.getParentFile(), f.getName() + ".pdf");
            }

            // Chụp hình ảnh biểu đồ hiện tại độ phân giải cao
            BufferedImage imgChart = bieuDoPanel.xuatHinhAnh(525, 185);

            String ky = (String) cbKhoangThoiGian.getSelectedItem();
            List<Object[]> topDV = thongKeDAO.getTopDichVuSanPham();
            List<Object[]> dsNV = thongKeDAO.getDoanhSoNhanVien();

            boolean ok = BaoCaoPDF_Exporter.xuatBaoCaoPDF(
                    f,
                    ky,
                    "Quản lý cửa hàng SALALA",
                    lblTongDoanhThuSo.getText(),
                    lblTongHoaDonSo.getText(),
                    lblKhachMoiSo.getText(),
                    lblLuotChamSocSo.getText(),
                    topDV,
                    dsNV,
                    imgChart
            );

            if (ok) {
                int opt = JOptionPane.showConfirmDialog(this,
                        "Xuất báo cáo PDF thành công!\nFile đã được lưu tại:\n" + f.getAbsolutePath() + "\n\nBạn có muốn mở file báo cáo ngay bây giờ không?",
                        "Xuất PDF thành công",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (opt == JOptionPane.YES_OPTION) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(f);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Không thể mở file tự động: " + ex.getMessage(), "Lưu ý", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tạo file báo cáo PDF!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 4. XUẤT BÁO CÁO EXCEL / CSV
    // ==========================================
    private void xuLyXuatExcel() {
        JFileChooser chooser = new JFileChooser();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
        chooser.setSelectedFile(new File("BaoCao_ThongKe_SALALA_" + sdf.format(new Date()) + ".csv"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()), StandardCharsets.UTF_8)) {
                writer.write('\uFEFF'); // UTF-8 BOM
                writer.write("BÁO CÁO THỐNG KÊ DOANH THU & HOẠT ĐỘNG SALALA PET SHOP\n");
                writer.write("Kỳ báo cáo:," + cbKhoangThoiGian.getSelectedItem() + "\n");
                writer.write("Thời gian xuất:," + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "\n\n");

                writer.write("1. CHỈ SỐ KPI CHÍNH\n");
                writer.write("Tổng doanh thu:," + lblTongDoanhThuSo.getText() + "\n");
                writer.write("Tổng số hóa đơn:," + lblTongHoaDonSo.getText() + "\n");
                writer.write("Tổng số khách hàng:," + lblKhachMoiSo.getText() + "\n");
                writer.write("Tổng số thú cưng:," + lblLuotChamSocSo.getText() + "\n\n");

                writer.write("2. TOP 5 DỊCH VỤ & SẢN PHẨM PHỔ BIẾN\n");
                writer.write("STT,Tên dịch vụ / sản phẩm,Số lượt,Doanh thu\n");
                for (int r = 0; r < bangTopDichVu.getRowCount(); r++) {
                    writer.write((r + 1) + ",\"" + bangTopDichVu.getValueAt(r, 0) + "\",\""
                            + bangTopDichVu.getValueAt(r, 1) + "\",\""
                            + bangTopDichVu.getValueAt(r, 2) + "\"\n");
                }

                writer.write("\n3. DOANH SỐ PHỤC VỤ THEO NHÂN VIÊN & BÁC SĨ\n");
                writer.write("Mã NV,Họ tên,Vai trò,Số hóa đơn,Doanh số\n");
                for (int r = 0; r < bangDoanhThuNhanVien.getRowCount(); r++) {
                    writer.write("\"" + bangDoanhThuNhanVien.getValueAt(r, 0) + "\",\""
                            + bangDoanhThuNhanVien.getValueAt(r, 1) + "\",\""
                            + bangDoanhThuNhanVien.getValueAt(r, 2) + "\",\""
                            + bangDoanhThuNhanVien.getValueAt(r, 3) + "\",\""
                            + bangDoanhThuNhanVien.getValueAt(r, 4) + "\"\n");
                }

                JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu báo cáo Excel/CSV thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử ThongKeGUI SALALA");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 750);
            frame.setLocationRelativeTo(null);
            frame.add(new ThongKeGUI());
            frame.setVisible(true);
        });
    }
}
