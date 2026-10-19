package GUI;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import dao.ThongKe_DAO;

/**
 * Class ThongKeGUI: Màn hình Thống kê theo mẫu Figma.
 *
 * - Hàng KPI (6 thẻ) phía trên.
 * - Vùng biểu đồ có thể chuyển giữa 1 khung / 2 khung / 4 khung.
 * - MỖI KHUNG tự chọn "Chỉ số" và "Loại biểu đồ" (14 loại) độc lập với nhau.
 *
 * Các KPI và dữ liệu biểu đồ được tải trực tiếp từ cơ sở dữ liệu SALALA.
 */
public class ThongKeGUI extends JPanel {

    // ==========================================
    // HẰNG SỐ: LOẠI BIỂU ĐỒ
    // ==========================================
    private static final String L_COT_DUNG = "Biểu đồ cột đứng";
    private static final String L_COT_NGANG = "Biểu đồ cột ngang";
    private static final String L_COT_NHOM = "Biểu đồ cột nhóm";
    private static final String L_COT_CHONG = "Biểu đồ cột chồng";
    private static final String L_DUONG = "Biểu đồ đường";
    private static final String L_VUNG = "Biểu đồ vùng";
    private static final String L_VUNG_CHONG = "Biểu đồ vùng chồng";
    private static final String L_TRON = "Biểu đồ tròn";
    private static final String L_VANH = "Biểu đồ vành khuyên";
    private static final String L_PHAN_TAN = "Biểu đồ phân tán";
    private static final String L_BONG_BONG = "Biểu đồ bong bóng";
    private static final String L_KET_HOP = "Biểu đồ kết hợp cột và đường";
    private static final String L_RADAR = "Biểu đồ radar";
    private static final String L_BANG = "Biểu đồ dạng bảng dữ liệu";

    private static final String[] DANH_SACH_LOAI = {
            L_COT_DUNG, L_COT_NGANG, L_COT_NHOM, L_COT_CHONG, L_DUONG, L_VUNG, L_VUNG_CHONG,
            L_TRON, L_VANH, L_PHAN_TAN, L_BONG_BONG, L_KET_HOP, L_RADAR, L_BANG
    };

    // Bảng màu các chuỗi dữ liệu (xanh đậm - mint - xám tím - cam - xanh dương -
    // chàm)
    private static final Color[] PALETTE = {
            RangBuocGiaoDien.MAU_CHU_DAO,
            new Color(94, 234, 212),
            new Color(160, 164, 188),
            new Color(245, 158, 11),
            new Color(14, 165, 233),
            new Color(99, 102, 241)
    };

    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    // ==========================================
    // THÀNH PHẦN GIAO DIỆN
    // ==========================================
    private JLabel lblMoTa;
    private JLabel lblTrangThai;
    private JLabel[] lblKpiGiaTri = new JLabel[6];
    private JLabel[] lblKpiGhiChu = new JLabel[6];

    private JPanel vungBieuDo;
    private final KhungBieuDo[] cacKhung = new KhungBieuDo[4];
    private int soKhungHienTai = 4;
    private int maYeuCauDuLieu;

    public ThongKeGUI() {
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        add(taoThanhTieuDe(), BorderLayout.NORTH);

        // 4 khung được tạo sẵn và giữ nguyên cấu hình khi chuyển qua lại 1/2/4 khung
        cacKhung[0] = new KhungBieuDo(0, L_VUNG);
        cacKhung[1] = new KhungBieuDo(2, L_COT_NGANG);
        cacKhung[2] = new KhungBieuDo(3, L_COT_CHONG);
        cacKhung[3] = new KhungBieuDo(4, L_COT_NGANG);

        TrangCuon trang = new TrangCuon();
        trang.setOpaque(false);
        trang.add(taoHangKPI(), BorderLayout.NORTH);

        vungBieuDo = new JPanel();
        vungBieuDo.setOpaque(false);
        trang.add(vungBieuDo, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(trang);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        datSoKhung(4);
        capNhatMoTaThoiGian(2);
    }

    // ==========================================
    // A. THANH TIÊU ĐỀ + CHỌN KHOẢNG THỜI GIAN + CHỌN SỐ KHUNG
    // ==========================================
    private JComponent taoThanhTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel trai = new JPanel();
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        trai.setOpaque(false);

        JLabel lblDuongDan = new JLabel("Tổng quan / Thống kê");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblTieuDe = new JLabel("Thống kê");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblMoTa = new JLabel(" ");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTrangThai.setForeground(new Color(185, 28, 28));

        trai.add(lblDuongDan);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTieuDe);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblMoTa);
        trai.add(lblTrangThai);
        panel.add(trai, BorderLayout.WEST);

        // Nhóm nút bên phải: [1 khung | 2 khung | 4 khung] [Hôm nay | Tuần này | Tháng
        // này | Năm nay]
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        phai.setOpaque(false);

        final int[] soKhungTheoViTri = { 1, 2, 4 };
        phai.add(taoNhomToggle(new String[] { "1 khung", "2 khung", "4 khung" }, 2,
                idx -> datSoKhung(soKhungTheoViTri[idx])));
        phai.add(taoNhomToggle(new String[] { "Hôm nay", "Tuần này", "Tháng này", "Năm nay" }, 2,
                this::capNhatMoTaThoiGian));

        JPanel phaiCanGiua = new JPanel(new GridBagLayout());
        phaiCanGiua.setOpaque(false);
        phaiCanGiua.add(phai);
        panel.add(phaiCanGiua, BorderLayout.EAST);

        return panel;
    }

    /**
     * Tạo nhóm nút toggle bo viền (giống bộ chọn "Hôm nay / Tuần này / ..." trong
     * Figma).
     */
    private JPanel taoNhomToggle(String[] ten, int chon, IntConsumer khiChon) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));

        ButtonGroup nhom = new ButtonGroup();
        for (int i = 0; i < ten.length; i++) {
            final int idx = i;
            JToggleButton b = new JToggleButton(ten[i]);
            b.setFont(RangBuocGiaoDien.fontVua(12));
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(7, 14, 7, 14));
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addItemListener(e -> kieuToggle(b));
            b.addActionListener(e -> khiChon.accept(idx));
            nhom.add(b);
            p.add(b);
            b.setSelected(i == chon);
            kieuToggle(b);
        }
        return p;
    }

    private static void kieuToggle(JToggleButton b) {
        if (b.isSelected()) {
            b.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        }
    }

    /** 0 = Hôm nay, 1 = Tuần này, 2 = Tháng này, 3 = Năm nay. */
    private void capNhatMoTaThoiGian(int chon) {
        LocalDate hn = LocalDate.now();
        LocalDate tu, den;
        switch (chon) {
            case 0:
                tu = hn;
                den = hn;
                break;
            case 1:
                tu = hn.with(DayOfWeek.MONDAY);
                den = tu.plusDays(6);
                break;
            case 2:
                tu = hn.withDayOfMonth(1);
                den = hn.with(TemporalAdjusters.lastDayOfMonth());
                break;
            default:
                tu = hn.withDayOfYear(1);
                den = hn.with(TemporalAdjusters.lastDayOfYear());
        }
        taiLaiDuLieu(tu, den);
    }

    private void taiLaiDuLieu(LocalDate tu, LocalDate den) {
        final int maYeuCau = ++maYeuCauDuLieu;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblTrangThai.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTrangThai.setToolTipText(null);
        lblTrangThai.setText("Đang tải số liệu: " + f.format(tu) + " – " + f.format(den));
        datKPI(4, "—", "Đang tải dữ liệu...");
        datKPI(5, "—", "Đang tải dữ liệu...");

        new SwingWorker<ThongKe_DAO.DuLieuThongKe, Void>() {
            @Override
            protected ThongKe_DAO.DuLieuThongKe doInBackground() throws Exception {
                return new ThongKe_DAO().layDuLieuThongKe(tu, den);
            }

            @Override
            protected void done() {
                if (maYeuCau != maYeuCauDuLieu || isCancelled())
                    return;
                try {
                    capNhatDuLieu(get());
                    lblMoTa.setText("Tổng quan hoạt động SALALA · "
                            + f.format(tu) + " – " + f.format(den));
                    lblTrangThai.setText(" ");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    hienLoiTaiDuLieu("Đã gián đoạn khi tải dữ liệu thống kê.");
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable nguyenNhan = e.getCause() == null ? e : e.getCause();
                    hienLoiTaiDuLieu("Không thể tải dữ liệu thống kê: " + nguyenNhan.getMessage());
                }
            }
        }.execute();
    }

    private void capNhatDuLieu(ThongKe_DAO.DuLieuThongKe duLieu) {
        NumberFormat dinhDangTien = NumberFormat.getNumberInstance(VN);
        dinhDangTien.setMaximumFractionDigits(0);
        datKPI(0, String.valueOf(duLieu.soNhanVien), "Toàn hệ thống");
        datKPI(1, String.valueOf(duLieu.soBacSi), "Toàn hệ thống");
        datKPI(2, String.valueOf(duLieu.soKhachHang), "Toàn hệ thống");
        datKPI(3, String.valueOf(duLieu.soThuCung), "Toàn hệ thống");
        datKPI(4, dinhDangTien.format(duLieu.doanhThu) + " đ", "Trong khoảng thời gian đã chọn");
        datKPI(5, String.valueOf(duLieu.soHoaDon), "Trong khoảng thời gian đã chọn");

        List<ChiSo> cacChiSo = new ArrayList<>();
        for (ThongKe_DAO.ChiSoThongKe chiSo : duLieu.chiSo) {
            cacChiSo.add(new ChiSo(chiSo));
        }
        for (KhungBieuDo khung : cacKhung) {
            if (khung != null)
                khung.datDanhSachChiSo(cacChiSo);
        }
    }

    private void hienLoiTaiDuLieu(String loi) {
        lblTrangThai.setForeground(new Color(185, 28, 28));
        lblTrangThai.setText("Không thể tải dữ liệu thống kê. Kiểm tra kết nối cơ sở dữ liệu.");
        lblTrangThai.setToolTipText(loi);
    }

    // ==========================================
    // B. HÀNG KPI (6 THẺ)
    // ==========================================
    private JComponent taoHangKPI() {
        String[][] kpi = {
                { "Tổng số nhân viên", "—", "Toàn hệ thống" },
                { "Tổng số bác sĩ", "—", "Toàn hệ thống" },
                { "Tổng số khách hàng", "—", "Toàn hệ thống" },
                { "Tổng số thú cưng", "—", "Toàn hệ thống" },
                { "Doanh thu", "—", "Trong khoảng thời gian đã chọn" },
                { "Số hóa đơn", "—", "Trong khoảng thời gian đã chọn" }
        };

        JPanel hang = new JPanel(new GridLayout(1, 6, 12, 0));
        hang.setOpaque(false);
        for (int i = 0; i < kpi.length; i++) {
            lblKpiGiaTri[i] = new JLabel(kpi[i][1]);
            lblKpiGhiChu[i] = new JLabel(kpi[i][2]);
            hang.add(taoTheKPI(kpi[i][0], lblKpiGiaTri[i], lblKpiGhiChu[i]));
        }
        return hang;
    }

    private JPanel taoTheKPI(String tieuDe, JLabel lblGiaTri, JLabel lblGhiChu) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblGiaTri.setFont(RangBuocGiaoDien.fontDam(22));
        lblGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        the.add(lblTieuDe);
        the.add(Box.createVerticalStrut(6));
        the.add(lblGiaTri);
        the.add(Box.createVerticalStrut(4));
        the.add(lblGhiChu);
        return the;
    }

    /** Cập nhật 1 thẻ KPI (chiSo từ 0 đến 5) khi có dữ liệu thật. */
    public void datKPI(int viTri, String giaTri, String ghiChu) {
        if (viTri < 0 || viTri >= lblKpiGiaTri.length)
            return;
        lblKpiGiaTri[viTri].setText(giaTri);
        lblKpiGhiChu[viTri].setText(ghiChu);
    }

    // ==========================================
    // C. VÙNG BIỂU ĐỒ: 1 / 2 / 4 KHUNG
    // ==========================================
    private void datSoKhung(int n) {
        soKhungHienTai = n;
        int cot = (n == 1) ? 1 : 2;
        int hang = (n == 4) ? 2 : 1;
        int chieuCaoVungBieuDo = (n == 1) ? 440 : (n == 2 ? 350 : 500);

        vungBieuDo.removeAll();
        vungBieuDo.setLayout(new GridLayout(hang, cot, 16, 16));
        for (int i = 0; i < n; i++) {
            vungBieuDo.add(cacKhung[i]);
        }
        vungBieuDo.setPreferredSize(new Dimension(0, chieuCaoVungBieuDo));
        vungBieuDo.revalidate();
        vungBieuDo.repaint();
    }

    public int getSoKhungHienTai() {
        return soKhungHienTai;
    }

    /**
     * Trang cuộn: bám theo chiều rộng viewport; kéo giãn chiều cao khi cửa sổ lớn
     * hơn nội dung.
     */
    private static class TrangCuon extends JPanel implements Scrollable {
        TrangCuon() {
            super(new BorderLayout(0, 14));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return Math.max(16, r.height - 32);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container pa = getParent();
            return (pa instanceof JViewport) && pa.getHeight() > getPreferredSize().height;
        }
    }

    // ==========================================
    // D. MỘT KHUNG BIỂU ĐỒ (tự chọn chỉ số + loại biểu đồ)
    // ==========================================
    private static class KhungBieuDo extends JPanel {
        private final JComboBox<ChiSo> cbChiSo = new JComboBox<>();
        private final JComboBox<String> cbLoai = new JComboBox<>(DANH_SACH_LOAI);
        private final JLabel lblTieuDe = new JLabel();
        private final JLabel lblDonVi = new JLabel();
        private final JPanel vungVe = new JPanel(new BorderLayout());
        private final BieuDoCanvas canvas = new BieuDoCanvas();

        private boolean hienNhan = true, hienLuoi = true, hienChuGiai = true;
        private final int viTriMacDinh;

        KhungBieuDo(int chiSoMacDinh, String loaiMacDinh) {
            viTriMacDinh = chiSoMacDinh;
            setLayout(new BorderLayout(0, 8));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(14, 18, 12, 18)));

            // --- Dòng 1: tiêu đề (trái) + đơn vị (phải)
            lblTieuDe.setFont(RangBuocGiaoDien.fontDam(15));
            lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lblDonVi.setFont(RangBuocGiaoDien.fontThuong(11));
            lblDonVi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            JPanel hang1 = new JPanel(new BorderLayout());
            hang1.setOpaque(false);
            hang1.add(lblTieuDe, BorderLayout.WEST);
            hang1.add(lblDonVi, BorderLayout.EAST);

            // --- Dòng 2: chọn chỉ số, loại biểu đồ, tùy chọn hiển thị
            cbChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
            cbLoai.setFont(RangBuocGiaoDien.fontThuong(12));
            cbChiSo.setToolTipText("Chỉ số hiển thị trong khung này");
            cbLoai.setToolTipText("Loại biểu đồ của khung này");

            JPanel haiCombo = new JPanel(new GridLayout(1, 2, 6, 0));
            haiCombo.setOpaque(false);
            haiCombo.add(cbChiSo);
            haiCombo.add(cbLoai);

            JButton btnTuyChon = new JButton("Tùy chọn");
            btnTuyChon.setFont(RangBuocGiaoDien.fontThuong(12));
            btnTuyChon.setFocusPainted(false);
            btnTuyChon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPopupMenu menu = new JPopupMenu();
            menu.add(taoMucCheck("Hiển thị nhãn dữ liệu", hienNhan, v -> {
                hienNhan = v;
                capNhat();
            }));
            menu.add(taoMucCheck("Hiển thị đường lưới", hienLuoi, v -> {
                hienLuoi = v;
                capNhat();
            }));
            menu.add(taoMucCheck("Hiển thị chú giải", hienChuGiai, v -> {
                hienChuGiai = v;
                capNhat();
            }));
            btnTuyChon.addActionListener(e -> menu.show(btnTuyChon, 0, btnTuyChon.getHeight()));

            JPanel hang2 = new JPanel(new BorderLayout(6, 0));
            hang2.setOpaque(false);
            hang2.add(haiCombo, BorderLayout.CENTER);
            hang2.add(btnTuyChon, BorderLayout.EAST);

            JPanel dau = new JPanel();
            dau.setLayout(new BoxLayout(dau, BoxLayout.Y_AXIS));
            dau.setOpaque(false);
            dau.add(hang1);
            dau.add(Box.createVerticalStrut(8));
            dau.add(hang2);
            add(dau, BorderLayout.NORTH);

            // --- Vùng vẽ
            vungVe.setOpaque(false);
            add(vungVe, BorderLayout.CENTER);

            cbLoai.setSelectedItem(loaiMacDinh);
            cbChiSo.addActionListener(e -> capNhat());
            cbLoai.addActionListener(e -> capNhat());
            capNhat();
        }

        private JCheckBoxMenuItem taoMucCheck(String ten, boolean chon, Consumer<Boolean> khiDoi) {
            JCheckBoxMenuItem m = new JCheckBoxMenuItem(ten, chon);
            m.addActionListener(e -> khiDoi.accept(m.isSelected()));
            return m;
        }

        void datDanhSachChiSo(List<ChiSo> danhSach) {
            ChiSo dangChon = (ChiSo) cbChiSo.getSelectedItem();
            String tenDangChon = dangChon == null ? null : dangChon.ten;
            cbChiSo.removeAllItems();
            int viTriChon = Math.min(viTriMacDinh, Math.max(0, danhSach.size() - 1));
            for (int i = 0; i < danhSach.size(); i++) {
                ChiSo chiSo = danhSach.get(i);
                cbChiSo.addItem(chiSo);
                if (chiSo.ten.equals(tenDangChon))
                    viTriChon = i;
            }
            if (!danhSach.isEmpty())
                cbChiSo.setSelectedIndex(viTriChon);
            capNhat();
        }

        /** Vẽ lại khung theo chỉ số / loại biểu đồ / tùy chọn hiện tại. */
        void capNhat() {
            ChiSo cs = (ChiSo) cbChiSo.getSelectedItem();
            String loai = (String) cbLoai.getSelectedItem();
            if (cs == null || loai == null)
                return;

            lblTieuDe.setText(cs.tieuDe);
            lblDonVi.setText(cs.donVi);

            vungVe.removeAll();
            if (L_BANG.equals(loai)) {
                vungVe.add(taoBang(cs), BorderLayout.CENTER);
            } else {
                canvas.datCauHinh(cs, loai, hienNhan, hienLuoi, hienChuGiai);
                vungVe.add(canvas, BorderLayout.CENTER);
            }
            vungVe.revalidate();
            vungVe.repaint();
        }

        private JComponent taoBang(ChiSo cs) {
            String[] cot = new String[cs.chuoi.length + 1];
            cot[0] = "Danh mục / Thời gian";
            System.arraycopy(cs.chuoi, 0, cot, 1, cs.chuoi.length);

            DefaultTableModel model = new DefaultTableModel(cot, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (int i = 0; i < cs.nhan.length; i++) {
                Object[] dong = new Object[cot.length];
                dong[0] = cs.nhan[i];
                for (int s = 0; s < cs.chuoi.length; s++) {
                    dong[s + 1] = fmt(cs.giaTri[s][i]) + cs.hauTo;
                }
                model.addRow(dong);
            }
            JTable bang = new JTable(model);
            bang.setFont(RangBuocGiaoDien.fontThuong(13));
            bang.setRowHeight(28);
            bang.getTableHeader().setFont(RangBuocGiaoDien.fontDam(12));
            bang.setShowVerticalLines(false);

            JScrollPane sp = new JScrollPane(bang);
            sp.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
            return sp;
        }
    }

    // ==========================================
    // E. MÔ HÌNH DỮ LIỆU MỘT CHỈ SỐ
    // ==========================================
    private static class ChiSo {
        final String ten; // tên hiển thị trong combobox
        final String tieuDe; // tiêu đề khung: "Biểu đồ ..."
        final String donVi; // góc phải trên của khung
        final String hauTo; // hậu tố cho nhãn dữ liệu (vd: "%")
        final boolean coTong; // có hiển thị tổng chuỗi ở chú giải không
        final String[] nhan; // nhãn trục danh mục
        final String[] chuoi; // tên các chuỗi dữ liệu
        final double[][] giaTri; // [chuỗi][danh mục]

        ChiSo(String ten, String donVi, String hauTo, boolean coTong,
                String[] nhan, String[] chuoi, double[][] giaTri) {
            this.ten = ten;
            this.tieuDe = "Biểu đồ " + Character.toLowerCase(ten.charAt(0)) + ten.substring(1);
            this.donVi = donVi;
            this.hauTo = hauTo;
            this.coTong = coTong;
            this.nhan = nhan;
            this.chuoi = chuoi;
            this.giaTri = giaTri;
        }

        ChiSo(ThongKe_DAO.ChiSoThongKe duLieu) {
            this(duLieu.ten, duLieu.donVi, duLieu.hauTo, true,
                    duLieu.nhan.toArray(new String[0]),
                    duLieu.chuoi.toArray(new String[0]),
                    chuyenGiaTri(duLieu.giaTri));
        }

        private static double[][] chuyenGiaTri(List<List<Double>> giaTri) {
            double[][] ketQua = new double[giaTri.size()][];
            for (int i = 0; i < giaTri.size(); i++) {
                ketQua[i] = new double[giaTri.get(i).size()];
                for (int j = 0; j < giaTri.get(i).size(); j++) {
                    ketQua[i][j] = giaTri.get(i).get(j);
                }
            }
            return ketQua;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    // ==========================================
    // F. ENGINE VẼ BIỂU ĐỒ (GRAPHICS2D) - DÙNG CHUNG CHO MỌI KHUNG
    // ==========================================
    private static class BieuDoCanvas extends JPanel {
        private ChiSo cs;
        private String loai = L_COT_DUNG;
        private boolean nhan = true, luoi = true, chuGiai = true;

        // Hình học vùng vẽ hiện tại (dùng cho các biểu đồ tọa độ)
        private Rectangle p;
        private double slot, tran;
        private int n, soChuoi;

        BieuDoCanvas() {
            setOpaque(false);
        }

        void datCauHinh(ChiSo cs, String loai, boolean nhan, boolean luoi, boolean chuGiai) {
            this.cs = cs;
            this.loai = loai;
            this.nhan = nhan;
            this.luoi = luoi;
            this.chuGiai = chuGiai;
            repaint();
        }

        private boolean laTron() {
            return L_TRON.equals(loai) || L_VANH.equals(loai);
        }

        /**
         * Số chuỗi được vẽ: cột đứng / vùng / tròn / vành khuyên chỉ lấy chuỗi đầu
         * tiên.
         */
        private int soChuoiHienThi() {
            boolean mot = L_COT_DUNG.equals(loai) || L_VUNG.equals(loai) || laTron();
            return mot ? 1 : cs.chuoi.length;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (cs == null)
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setFont(RangBuocGiaoDien.fontThuong(11));

            int w = getWidth(), h = getHeight();
            int hChuGiai = chuGiai ? 26 : 0;
            Rectangle vung = new Rectangle(0, 0, w, h - hChuGiai);
            if (vung.width < 80 || vung.height < 60) {
                g2.dispose();
                return;
            }

            if (laTron())
                veTron(g2, vung);
            else if (L_RADAR.equals(loai))
                veRadar(g2, vung);
            else if (L_COT_NGANG.equals(loai))
                veCotNgang(g2, vung);
            else
                veTruc(g2, vung);

            if (chuGiai)
                veChuGiai(g2, w, h - hChuGiai);
            g2.dispose();
        }

        // ---------- Biểu đồ trục tọa độ đứng ----------
        private void veTruc(Graphics2D g2, Rectangle v) {
            n = cs.nhan.length;
            soChuoi = soChuoiHienThi();
            boolean chong = L_COT_CHONG.equals(loai) || L_VUNG_CHONG.equals(loai);

            double max = 0;
            for (int i = 0; i < n; i++) {
                double tong = 0;
                for (int s = 0; s < soChuoi; s++) {
                    max = Math.max(max, cs.giaTri[s][i]);
                    tong += cs.giaTri[s][i];
                }
                if (chong)
                    max = Math.max(max, tong);
            }
            tran = niceMax(max);
            p = new Rectangle(v.x + 46, v.y + 24, v.width - 46 - 16, v.height - 24 - 26);
            if (p.width <= 0 || p.height <= 0)
                return;
            slot = p.width / (double) n;
            int day = p.y + p.height;

            FontMetrics fm = g2.getFontMetrics();
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int y = day - (int) Math.round(p.height * i / 4.0);
                if (i > 0 && luoi) {
                    g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
                    g2.drawLine(p.x, y, p.x + p.width, y);
                }
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                String t = fmt(tran * i / 4.0);
                g2.drawString(t, p.x - 8 - fm.stringWidth(t), y + fm.getAscent() / 2 - 1);
            }
            g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
            g2.drawLine(p.x, day, p.x + p.width, day);

            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            for (int i = 0; i < n; i++) {
                veGiua(g2, cs.nhan[i], cx(i), day + 16);
            }

            switch (loai) {
                case L_COT_DUNG:
                case L_COT_NHOM:
                    veCotNhom(g2);
                    break;
                case L_COT_CHONG:
                    veCotChong(g2);
                    break;
                case L_DUONG:
                    for (int s = 0; s < soChuoi; s++) {
                        veDuongChuoi(g2, cs.giaTri[s], mau(s), nhan);
                    }
                    break;
                case L_VUNG:
                    veVung(g2, false);
                    break;
                case L_VUNG_CHONG:
                    veVung(g2, true);
                    break;
                case L_PHAN_TAN:
                    vePhanTan(g2);
                    break;
                case L_BONG_BONG:
                    veBongBong(g2);
                    break;
                case L_KET_HOP:
                    veKetHop(g2);
                    break;
                default:
                    veCotNhom(g2);
            }
        }

        private double cx(int i) {
            return p.x + slot * (i + 0.5);
        }

        private int yv(double giaTri) {
            return p.y + p.height - (int) Math.round(giaTri / tran * p.height);
        }

        private void veCotNhom(Graphics2D g2) {
            int day = p.y + p.height;
            double nhom = Math.min(slot * 0.7, soChuoi * 52.0);
            double bw = nhom / soChuoi;
            for (int i = 0; i < n; i++) {
                for (int s = 0; s < soChuoi; s++) {
                    double v = cs.giaTri[s][i];
                    double x = cx(i) - nhom / 2 + s * bw;
                    int y = yv(v);
                    g2.setColor(mau(s));
                    g2.fill(new Rectangle2D.Double(x + 1, y, bw - 2, day - y));
                    if (nhan) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                        veGiua(g2, fmt(v) + cs.hauTo, x + bw / 2, y - 5);
                    }
                }
            }
        }

        private void veCotChong(Graphics2D g2) {
            double bw = Math.min(slot * 0.5, 64);
            for (int i = 0; i < n; i++) {
                double luyKe = 0;
                for (int s = 0; s < soChuoi; s++) {
                    int y0 = yv(luyKe);
                    luyKe += cs.giaTri[s][i];
                    int y1 = yv(luyKe);
                    g2.setColor(mau(s));
                    g2.fill(new Rectangle2D.Double(cx(i) - bw / 2, y1, bw, y0 - y1));
                }
                if (nhan) {
                    g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                    veGiua(g2, fmt(luyKe) + cs.hauTo, cx(i), yv(luyKe) - 5);
                }
            }
        }

        private void veDuongChuoi(Graphics2D g2, double[] vals, Color c, boolean ganNhan) {
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(c);
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine((int) cx(i), yv(vals[i]), (int) cx(i + 1), yv(vals[i + 1]));
            }
            for (int i = 0; i < n; i++) {
                int x = (int) cx(i), y = yv(vals[i]);
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(c);
                g2.drawOval(x - 5, y - 5, 10, 10);
                if (ganNhan) {
                    g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                    veGiua(g2, fmt(vals[i]) + cs.hauTo, x, y - 10);
                }
            }
            g2.setStroke(new BasicStroke(1f));
        }

        private void veVung(Graphics2D g2, boolean chong) {
            double[][] cum = new double[soChuoi][n];
            for (int i = 0; i < n; i++) {
                double run = 0;
                for (int s = 0; s < soChuoi; s++) {
                    run = chong ? run + cs.giaTri[s][i] : cs.giaTri[s][i];
                    cum[s][i] = run;
                }
            }
            int day = p.y + p.height;
            for (int s = soChuoi - 1; s >= 0; s--) {
                Path2D.Double vungTo = new Path2D.Double();
                vungTo.moveTo(cx(0), yv(cum[s][0]));
                for (int i = 1; i < n; i++)
                    vungTo.lineTo(cx(i), yv(cum[s][i]));
                for (int i = n - 1; i >= 0; i--) {
                    double yDuoi = (chong && s > 0) ? yv(cum[s - 1][i]) : day;
                    vungTo.lineTo(cx(i), yDuoi);
                }
                vungTo.closePath();
                g2.setColor(alpha(mau(s), chong ? 190 : 45));
                g2.fill(vungTo);

                g2.setColor(mau(s));
                g2.setStroke(new BasicStroke(2f));
                for (int i = 0; i < n - 1; i++) {
                    g2.drawLine((int) cx(i), yv(cum[s][i]), (int) cx(i + 1), yv(cum[s][i + 1]));
                }
                if (!chong) {
                    for (int i = 0; i < n; i++) {
                        g2.fillOval((int) cx(i) - 3, yv(cum[s][i]) - 3, 6, 6);
                    }
                }
            }
            if (nhan) {
                g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                for (int i = 0; i < n; i++) {
                    veGiua(g2, fmt(cum[soChuoi - 1][i]) + cs.hauTo, cx(i), yv(cum[soChuoi - 1][i]) - 10);
                }
            }
            g2.setStroke(new BasicStroke(1f));
        }

        private void vePhanTan(Graphics2D g2) {
            for (int s = 0; s < soChuoi; s++) {
                double lech = (s - (soChuoi - 1) / 2.0) * 14;
                for (int i = 0; i < n; i++) {
                    double x = cx(i) + lech;
                    int y = yv(cs.giaTri[s][i]);
                    g2.setColor(alpha(mau(s), 170));
                    g2.fill(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
                    g2.setColor(mau(s));
                    g2.draw(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
                    if (nhan) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                        veGiua(g2, fmt(cs.giaTri[s][i]) + cs.hauTo, x, y - 10);
                    }
                }
            }
        }

        private void veBongBong(Graphics2D g2) {
            double maxKichThuoc = 1;
            for (double[] hang : cs.giaTri)
                for (double x : hang)
                    maxKichThuoc = Math.max(maxKichThuoc, x);
            for (int s = 0; s < soChuoi; s++) {
                double lech = (s - (soChuoi - 1) / 2.0) * 18;
                for (int i = 0; i < n; i++) {
                    // Kích thước bong bóng: theo chuỗi kế tiếp (nếu có), ngược lại theo chính giá
                    // trị
                    double kt = soChuoi > 1 ? cs.giaTri[(s + 1) % soChuoi][i] : cs.giaTri[s][i];
                    double d = 16 + 40 * (kt / maxKichThuoc);
                    double x = cx(i) + lech;
                    int y = yv(cs.giaTri[s][i]);
                    g2.setColor(alpha(mau(s), 120));
                    g2.fill(new Ellipse2D.Double(x - d / 2, y - d / 2, d, d));
                    g2.setColor(mau(s));
                    g2.draw(new Ellipse2D.Double(x - d / 2, y - d / 2, d, d));
                    if (nhan) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                        veGiua(g2, fmt(cs.giaTri[s][i]) + cs.hauTo, x, y + 4);
                    }
                }
            }
        }

        /**
         * Cột = chuỗi đầu tiên; đường = các chuỗi còn lại (nếu chỉ có 1 chuỗi thì đường
         * nối đỉnh các cột).
         */
        private void veKetHop(Graphics2D g2) {
            int day = p.y + p.height;
            double bw = Math.min(slot * 0.5, 56);
            for (int i = 0; i < n; i++) {
                int y = yv(cs.giaTri[0][i]);
                g2.setColor(mau(0));
                g2.fill(new Rectangle2D.Double(cx(i) - bw / 2, y, bw, day - y));
            }
            if (soChuoi > 1) {
                for (int s = 1; s < soChuoi; s++)
                    veDuongChuoi(g2, cs.giaTri[s], mau(s), nhan);
            } else {
                veDuongChuoi(g2, cs.giaTri[0], mau(1), nhan);
            }
        }

        // ---------- Cột ngang ----------
        private void veCotNgang(Graphics2D g2, Rectangle v) {
            n = cs.nhan.length;
            soChuoi = soChuoiHienThi();
            double max = 0;
            for (int s = 0; s < soChuoi; s++)
                for (double x : cs.giaTri[s])
                    max = Math.max(max, x);
            tran = niceMax(max);

            FontMetrics fm = g2.getFontMetrics();
            int lw = 0;
            for (String t : cs.nhan)
                lw = Math.max(lw, fm.stringWidth(t));
            lw += 8;
            p = new Rectangle(v.x + lw + 10, v.y + 8, v.width - lw - 10 - 44, v.height - 8 - 26);
            if (p.width <= 0 || p.height <= 0)
                return;
            double slotY = p.height / (double) n;
            int day = p.y + p.height;

            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int x = p.x + (int) Math.round(p.width * i / 4.0);
                if (i > 0 && luoi) {
                    g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
                    g2.drawLine(x, p.y, x, day);
                }
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                veGiua(g2, fmt(tran * i / 4.0), x, day + 16);
            }
            g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
            g2.drawLine(p.x, p.y, p.x, day);

            double nhom = Math.min(slotY * 0.7, soChuoi * 28.0);
            double bh = nhom / soChuoi;
            for (int i = 0; i < n; i++) {
                double cy = p.y + slotY * (i + 0.5);
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                String t = cs.nhan[i];
                g2.drawString(t, p.x - 8 - fm.stringWidth(t), (float) cy + 4);
                for (int s = 0; s < soChuoi; s++) {
                    double w = cs.giaTri[s][i] / tran * p.width;
                    double y = cy - nhom / 2 + s * bh;
                    g2.setColor(mau(s));
                    g2.fill(new Rectangle2D.Double(p.x, y + 1, w, bh - 2));
                    if (nhan) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                        g2.drawString(fmt(cs.giaTri[s][i]) + cs.hauTo, (float) (p.x + w + 8), (float) (y + bh / 2 + 4));
                    }
                }
            }
        }

        // ---------- Tròn / Vành khuyên ----------
        private void veTron(Graphics2D g2, Rectangle v) {
            int soDM = cs.nhan.length;
            double[] gt = cs.giaTri[0];
            double tong = 0;
            for (double x : gt)
                tong += x;
            if (tong <= 0)
                return;

            boolean vanh = L_VANH.equals(loai);
            double r = Math.min(v.width, v.height) / 2.0 - 10;
            if (r < 20)
                return;
            double cxm = v.x + v.width / 2.0, cym = v.y + v.height / 2.0;
            double ri = r * 0.58;
            double bat = 90;

            for (int i = 0; i < soDM; i++) {
                double goc = -360.0 * gt[i] / tong;
                Shape hinh = new Arc2D.Double(cxm - r, cym - r, 2 * r, 2 * r, bat, goc, Arc2D.PIE);
                if (vanh) {
                    Area a = new Area(hinh);
                    a.subtract(new Area(new Ellipse2D.Double(cxm - ri, cym - ri, 2 * ri, 2 * ri)));
                    hinh = a;
                }
                g2.setColor(mau(i));
                g2.fill(hinh);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(hinh);

                double phanTram = gt[i] * 100.0 / tong;
                if (nhan && phanTram >= 6) {
                    double giua = Math.toRadians(bat + goc / 2);
                    double rr = vanh ? (r + ri) / 2 : r * 0.65;
                    g2.setColor(Color.WHITE);
                    veGiua(g2, Math.round(phanTram) + "%", cxm + rr * Math.cos(giua), cym - rr * Math.sin(giua) + 4);
                }
                bat += goc;
            }
            g2.setStroke(new BasicStroke(1f));

            if (vanh) {
                Font cu = g2.getFont();
                g2.setFont(RangBuocGiaoDien.fontDam(16));
                g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                veGiua(g2, fmt(tong) + cs.hauTo, cxm, cym + 2);
                g2.setFont(RangBuocGiaoDien.fontThuong(11));
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                veGiua(g2, "Tổng", cxm, cym + 18);
                g2.setFont(cu);
            }
        }

        // ---------- Radar ----------
        private void veRadar(Graphics2D g2, Rectangle v) {
            int soTruc = cs.nhan.length;
            int soC = cs.chuoi.length;
            double max = 0;
            for (double[] hang : cs.giaTri)
                for (double x : hang)
                    max = Math.max(max, x);
            double tr = niceMax(max);

            double cxm = v.x + v.width / 2.0, cym = v.y + v.height / 2.0;
            double r = Math.min(v.width, v.height) / 2.0 - 32;
            if (r < 20)
                return;

            g2.setStroke(new BasicStroke(1f));
            for (int vong = 1; vong <= 4; vong++) {
                if (vong < 4 && !luoi)
                    continue;
                Path2D.Double da = new Path2D.Double();
                for (int i = 0; i < soTruc; i++) {
                    double ang = -Math.PI / 2 + 2 * Math.PI * i / soTruc;
                    double x = cxm + r * vong / 4 * Math.cos(ang);
                    double y = cym + r * vong / 4 * Math.sin(ang);
                    if (i == 0)
                        da.moveTo(x, y);
                    else
                        da.lineTo(x, y);
                }
                da.closePath();
                g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
                g2.draw(da);
            }

            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < soTruc; i++) {
                double ang = -Math.PI / 2 + 2 * Math.PI * i / soTruc;
                double c = Math.cos(ang), s = Math.sin(ang);
                g2.setColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
                g2.draw(new java.awt.geom.Line2D.Double(cxm, cym, cxm + r * c, cym + r * s));

                double lx = cxm + (r + 12) * c, ly = cym + (r + 12) * s + 4;
                int tw = fm.stringWidth(cs.nhan[i]);
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                if (Math.abs(c) < 0.3)
                    g2.drawString(cs.nhan[i], (float) (lx - tw / 2.0), (float) ly);
                else if (c > 0)
                    g2.drawString(cs.nhan[i], (float) lx, (float) ly);
                else
                    g2.drawString(cs.nhan[i], (float) (lx - tw), (float) ly);
            }

            for (int s = 0; s < soC; s++) {
                Path2D.Double da = new Path2D.Double();
                for (int i = 0; i < soTruc; i++) {
                    double ang = -Math.PI / 2 + 2 * Math.PI * i / soTruc;
                    double rr = r * cs.giaTri[s][i] / tr;
                    double x = cxm + rr * Math.cos(ang), y = cym + rr * Math.sin(ang);
                    if (i == 0)
                        da.moveTo(x, y);
                    else
                        da.lineTo(x, y);
                }
                da.closePath();
                g2.setColor(alpha(mau(s), 70));
                g2.fill(da);
                g2.setColor(mau(s));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(da);

                for (int i = 0; i < soTruc; i++) {
                    double ang = -Math.PI / 2 + 2 * Math.PI * i / soTruc;
                    double rr = r * cs.giaTri[s][i] / tr;
                    double x = cxm + rr * Math.cos(ang), y = cym + rr * Math.sin(ang);
                    g2.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
                    if (nhan) {
                        g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
                        veGiua(g2, fmt(cs.giaTri[s][i]) + cs.hauTo, x, y - 7);
                        g2.setColor(mau(s));
                    }
                }
            }
            g2.setStroke(new BasicStroke(1f));
        }

        // ---------- Chú giải ----------
        private void veChuGiai(Graphics2D g2, int w, int yTren) {
            int soMuc = laTron() ? cs.nhan.length : soChuoiHienThi();
            FontMetrics fm = g2.getFontMetrics();
            int x = 16, y = yTren + 16;
            for (int i = 0; i < soMuc; i++) {
                String text;
                if (laTron()) {
                    text = cs.nhan[i];
                } else {
                    text = cs.chuoi[i];
                    if (cs.coTong) {
                        double tong = 0;
                        for (double d : cs.giaTri[i])
                            tong += d;
                        text += ": " + fmt(tong) + " " + cs.donVi.toLowerCase(VN);
                    }
                }
                int tw = fm.stringWidth(text);
                if (x + 14 + tw > w - 8)
                    break;
                g2.setColor(mau(i));
                g2.fill(new RoundRectangle2D.Double(x, y - 9, 9, 9, 3, 3));
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                g2.drawString(text, x + 14, y);
                x += 14 + tw + 18;
            }
        }
    }

    // ==========================================
    // TIỆN ÍCH CHUNG
    // ==========================================
    private static Color mau(int i) {
        return PALETTE[i % PALETTE.length];
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static void veGiua(Graphics2D g2, String t, double cx, double yBaseline) {
        g2.drawString(t, (float) (cx - g2.getFontMetrics().stringWidth(t) / 2.0), (float) yBaseline);
    }

    private static String fmt(double v) {
        if (v == Math.rint(v))
            return String.valueOf((long) v);
        return String.format(VN, "%.1f", v);
    }

    /**
     * Làm tròn trần trục giá trị sao cho chia 4 khoảng đều và đẹp (0, 15, 30, 45,
     * 60...).
     */
    private static double niceMax(double max) {
        if (max <= 0)
            return 4;
        double raw = max / 4.0;
        double e = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / e;
        double[] buoc = { 1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10 };
        double b = 10;
        for (double x : buoc) {
            if (f <= x + 1e-9) {
                b = x;
                break;
            }
        }
        return b * e * 4;
    }
}