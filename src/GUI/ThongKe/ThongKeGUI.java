package GUI.ThongKe;

import GUI.Chung.RangBuocGiaoDien;
import dao.ThongKe_DAO;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ThongKeGUI extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Locale VIET_NAM = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] LOAI_BIEU_DO = { "Biểu đồ cột", "Biểu đồ đường", "Biểu đồ điểm", "Biểu đồ quạt" };

    private final JLabel lblMoTa = new JLabel(" ");
    private final JLabel lblTrangThai = new JLabel(" ");
    private final JLabel[] lblTieuDeKpi = new JLabel[5];
    private final JLabel[] lblGiaTriKpi = new JLabel[5];
    private final JLabel[] lblGhiChuKpi = new JLabel[5];
    private final JSpinner ngayBatDau = taoBoChonNgay(LocalDate.now().withDayOfMonth(1));
    private final JSpinner ngayKetThuc = taoBoChonNgay(LocalDate.now());
    private final JComboBox<String> cboKhoangNhanh = new JComboBox<>(
            new String[] { "Tùy chọn", "Hôm nay", "7 ngày qua", "30 ngày qua", "Tháng này", "Năm nay" });
    private static final String[] NHOM_CHI_SO = {
            "Doanh thu",
            "Hàng hóa",
            "Dịch vụ",
            "Tồn kho"
    };
    private final JComboBox<String> cboNhomChiSo = new JComboBox<>(NHOM_CHI_SO);
    private final JComboBox<ChiSo> cboChiSo = new JComboBox<>();
    private final List<ChiSo> tatCaChiSo = new java.util.ArrayList<>();
    private final JLabel lblGhiChuChiSo = new JLabel(" ");
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new Object[] { "Thời gian / Danh mục", "Số liệu" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangDuLieu = new JTable(modelBang);
    private int maYeuCauDuLieu;
    private ThongKe_DAO.DuLieuThongKe duLieuThongKe;

    public ThongKeGUI() {
        khoiTaoGiaoDien();
        taiLaiDuLieu(docNgay(ngayBatDau), docNgay(ngayKetThuc));
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 14));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 20, 20, 20));
        add(taoThanhTieuDe(), BorderLayout.NORTH);

        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setOpaque(false);
        noiDung.add(taoHangKpi(), BorderLayout.NORTH);
        noiDung.add(taoBangThongKe(), BorderLayout.CENTER);
        add(noiDung, BorderLayout.CENTER);
    }

    private JComponent taoThanhTieuDe() {
        JPanel tong = new JPanel(new BorderLayout(12, 8));
        tong.setOpaque(false);
        tong.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel benTrai = new JPanel();
        benTrai.setOpaque(false);
        benTrai.setLayout(new BoxLayout(benTrai, BoxLayout.Y_AXIS));
        JLabel duongDan = new JLabel("Tổng quan / Thống kê");
        duongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        duongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JLabel tieuDe = new JLabel("Thống kê bán hàng");
        tieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        tieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTrangThai.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTrangThai.setForeground(new Color(185, 28, 28));
        benTrai.add(duongDan);
        benTrai.add(Box.createVerticalStrut(4));
        benTrai.add(tieuDe);
        benTrai.add(Box.createVerticalStrut(4));
        benTrai.add(lblMoTa);
        benTrai.add(lblTrangThai);
        tong.add(benTrai, BorderLayout.WEST);

        JPanel dieuKhien = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dieuKhien.setOpaque(false);
        dieuKhien.add(new JLabel("Từ"));
        dieuKhien.add(ngayBatDau);
        dieuKhien.add(new JLabel("Đến"));
        dieuKhien.add(ngayKetThuc);

        cboKhoangNhanh.setFont(RangBuocGiaoDien.fontThuong(12));
        cboKhoangNhanh.addActionListener(e -> apDungKhoangNhanh());
        dieuKhien.add(cboKhoangNhanh);

        JButton btnApDung = new JButton("Thống kê");
        btnApDung.setFont(RangBuocGiaoDien.fontDam(12));
        btnApDung.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApDung.addActionListener(e -> apDungKhoangDaChon());
        dieuKhien.add(btnApDung);

        JButton btnTaoBieuDo = new JButton("+ Tạo biểu đồ");
        btnTaoBieuDo.setFont(RangBuocGiaoDien.fontDam(12));
        btnTaoBieuDo.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnTaoBieuDo.setForeground(Color.BLACK);
        btnTaoBieuDo.setFocusPainted(false);
        btnTaoBieuDo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaoBieuDo.addActionListener(e -> moDialogTaoBieuDo());
        dieuKhien.add(btnTaoBieuDo);
        tong.add(dieuKhien, BorderLayout.EAST);
        return tong;
    }

    private JComponent taoHangKpi() {
        String[][] thongTin = {
                { "Doanh thu", "—", "Hàng hóa + dịch vụ" },
                { "Hóa đơn", "—", "Số hóa đơn trong kỳ" },
                { "Số lượng bán", "—", "Sản phẩm đã bán" },
                { "Doanh thu hàng hóa", "—", "Vật tư bán ra" },
                { "Doanh thu dịch vụ", "—", "Dịch vụ đã thực hiện" }
        };

        JPanel hang = new JPanel(new GridLayout(1, thongTin.length, 12, 0));
        hang.setOpaque(false);
        for (int i = 0; i < thongTin.length; i++) {
            lblTieuDeKpi[i] = new JLabel(thongTin[i][0]);
            lblGiaTriKpi[i] = new JLabel(thongTin[i][1]);
            lblGhiChuKpi[i] = new JLabel(thongTin[i][2]);
            hang.add(taoTheKpi(lblTieuDeKpi[i], lblGiaTriKpi[i], lblGhiChuKpi[i]));
        }
        return hang;
    }

    private JPanel taoTheKpi(JLabel nhan, JLabel giaTri, JLabel ghiChu) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        nhan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        giaTri.setFont(RangBuocGiaoDien.fontDam(20));
        giaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        ghiChu.setFont(RangBuocGiaoDien.fontThuong(11));
        ghiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        the.add(nhan);
        the.add(Box.createVerticalStrut(6));
        the.add(giaTri);
        the.add(Box.createVerticalStrut(4));
        the.add(ghiChu);
        return the;
    }

    private JComponent taoBangThongKe() {
        JPanel tong = new JPanel(new BorderLayout(0, 8));
        tong.setBackground(Color.WHITE);
        tong.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel chonChiSo = new JPanel(new GridLayout(2, 2, 10, 8));
        chonChiSo.setOpaque(false);
        JLabel nhom = new JLabel("Nhóm báo cáo");
        nhom.setFont(RangBuocGiaoDien.fontDam(13));
        nhom.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboNhomChiSo.setFont(RangBuocGiaoDien.fontThuong(13));
        cboNhomChiSo.addActionListener(e -> capNhatDanhSachChiSo(null));
        JLabel nhan = new JLabel("Chỉ số trong nhóm");
        nhan.setFont(RangBuocGiaoDien.fontDam(13));
        nhan.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboChiSo.setFont(RangBuocGiaoDien.fontThuong(13));
        cboChiSo.addActionListener(e -> capNhatBangChiSoChon());
        lblGhiChuChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChuChiSo.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chonChiSo.add(nhom);
        chonChiSo.add(cboNhomChiSo);
        chonChiSo.add(nhan);
        chonChiSo.add(cboChiSo);
        JPanel dauBang = new JPanel(new BorderLayout(0, 6));
        dauBang.setOpaque(false);
        dauBang.add(chonChiSo, BorderLayout.NORTH);
        dauBang.add(lblGhiChuChiSo, BorderLayout.SOUTH);
        tong.add(dauBang, BorderLayout.NORTH);

        bangDuLieu.setFont(RangBuocGiaoDien.fontThuong(13));
        bangDuLieu.setRowHeight(30);
        bangDuLieu.setShowGrid(true);
        bangDuLieu.setGridColor(new Color(226, 232, 240));
        bangDuLieu.getTableHeader().setFont(RangBuocGiaoDien.fontDam(13));
        bangDuLieu.getTableHeader().setBackground(new Color(241, 245, 249));
        bangDuLieu.getTableHeader().setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        DefaultTableCellRenderer canPhai = new DefaultTableCellRenderer();
        canPhai.setHorizontalAlignment(SwingConstants.RIGHT);
        bangDuLieu.getColumnModel().getColumn(1).setCellRenderer(canPhai);
        JScrollPane cuon = new JScrollPane(bangDuLieu);
        cuon.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        tong.add(cuon, BorderLayout.CENTER);
        return tong;
    }

    private void apDungKhoangNhanh() {
        int chon = cboKhoangNhanh.getSelectedIndex();
        if (chon == 0) {
            return;
        }
        LocalDate homNay = LocalDate.now();
        LocalDate tu;
        LocalDate den = homNay;
        switch (chon) {
            case 1:
                tu = homNay;
                break;
            case 2:
                tu = homNay.minusDays(6);
                break;
            case 3:
                tu = homNay.minusDays(29);
                break;
            case 4:
                tu = homNay.withDayOfMonth(1);
                break;
            default:
                tu = homNay.withDayOfYear(1);
                break;
        }
        datNgay(ngayBatDau, tu);
        datNgay(ngayKetThuc, den);
        apDungKhoangDaChon();
    }

    private void apDungKhoangDaChon() {
        try {
            taiLaiDuLieu(docNgay(ngayBatDau), docNgay(ngayKetThuc));
        } catch (IllegalArgumentException ex) {
            hienLoi(ex.getMessage());
        }
    }

    private void taiLaiDuLieu(LocalDate tu, LocalDate den) {
        if (tu == null || den == null || tu.isAfter(den)) {
            hienLoi("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
            return;
        }
        int yeuCau = ++maYeuCauDuLieu;
        lblTrangThai.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTrangThai.setToolTipText(null);
        lblTrangThai.setText("Đang tải dữ liệu thống kê...");
        new SwingWorker<ThongKe_DAO.DuLieuThongKe, Void>() {
            @Override
            protected ThongKe_DAO.DuLieuThongKe doInBackground() throws Exception {
                return new ThongKe_DAO().layDuLieuThongKe(tu, den);
            }

            @Override
            protected void done() {
                if (yeuCau != maYeuCauDuLieu || isCancelled()) {
                    return;
                }
                try {
                    capNhatDuLieu(get());
                    lblMoTa.setText("Từ " + DINH_DANG_NGAY.format(tu) + " đến " + DINH_DANG_NGAY.format(den)
                            + " · Dữ liệu " + ThongKe_DAO.moTaNhomThoiGian(tu, den) + ".");
                    lblTrangThai.setText(" ");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hienLoi("Đã gián đoạn khi tải dữ liệu thống kê.");
                } catch (ExecutionException ex) {
                    Throwable nguyenNhan = ex.getCause() == null ? ex : ex.getCause();
                    hienLoi("Không thể tải dữ liệu thống kê: " + nguyenNhan.getMessage());
                }
            }
        }.execute();
    }

    private void capNhatDuLieu(ThongKe_DAO.DuLieuThongKe duLieu) {
        duLieuThongKe = duLieu;

        ChiSo dangChon = (ChiSo) cboChiSo.getSelectedItem();
        String maDangChon = dangChon == null ? null : dangChon.ma;
        tatCaChiSo.clear();
        for (ThongKe_DAO.ChiSoThongKe chiSo : duLieu.chiSo) {
            tatCaChiSo.add(new ChiSo(chiSo));
        }
        capNhatDanhSachChiSo(maDangChon);
        capNhatKpiTheoNhom();
    }

    private void capNhatDanhSachChiSo(String maCanChon) {
        cboChiSo.removeAllItems();
        String nhomChon = (String) cboNhomChiSo.getSelectedItem();
        for (ChiSo chiSo : tatCaChiSo) {
            if (nhomChon.equals(nhomCuaChiSo(chiSo.ma))) {
                cboChiSo.addItem(chiSo);
            }
        }
        if (cboChiSo.getItemCount() == 0) {
            capNhatBangChiSoChon();
            return;
        }
        int viTri = 0;
        if (maCanChon != null) {
            for (int i = 0; i < cboChiSo.getItemCount(); i++) {
                if (cboChiSo.getItemAt(i).ma.equals(maCanChon)) {
                    viTri = i;
                    break;
                }
            }
        }
        cboChiSo.setSelectedIndex(viTri);
        capNhatKpiTheoNhom();
    }

    private void capNhatKpiTheoNhom() {
        if (duLieuThongKe == null) {
            return;
        }
        NumberFormat dinhDang = NumberFormat.getNumberInstance(VIET_NAM);
        dinhDang.setMaximumFractionDigits(0);
        double[] giaTri = {
                duLieuThongKe.doanhThuTong,
                duLieuThongKe.soHoaDon,
                duLieuThongKe.soLuongBan,
                duLieuThongKe.doanhThuHangHoa,
                duLieuThongKe.doanhThuDichVu
        };
        String[] hauTo = { "đ", "", "", "đ", "đ" };
        for (int i = 0; i < giaTri.length; i++) {
            lblGiaTriKpi[i].setText(dinhDang.format(giaTri[i]) + " " + hauTo[i]);
            lblGhiChuKpi[i].setText("Trong khoảng thời gian đã chọn");
        }
    }

    private static String nhomCuaChiSo(String ma) {
        switch (ma) {
            case ThongKe_DAO.MA_TOP_HANG_HOA:
            case ThongKe_DAO.MA_SO_LUONG_BAN:
            case ThongKe_DAO.MA_DOANH_THU_HANG_HOA:
            case ThongKe_DAO.MA_TOP_TON_KHO:
                return NHOM_CHI_SO[1];
            case ThongKe_DAO.MA_TOP_DICH_VU:
            case ThongKe_DAO.MA_DOANH_THU_DICH_VU:
                return NHOM_CHI_SO[2];
            case ThongKe_DAO.MA_TON_KHO:
                return NHOM_CHI_SO[3];
            default:
                return NHOM_CHI_SO[0];
        }
    }

    private void capNhatBangChiSoChon() {
        ChiSo chiSo = (ChiSo) cboChiSo.getSelectedItem();
        modelBang.setRowCount(0);
        if (chiSo == null || chiSo.nhan.length == 0 || chiSo.giaTri.length == 0) {
            lblGhiChuChiSo.setText("Chưa có dữ liệu cho chỉ số này.");
            return;
        }
        lblGhiChuChiSo.setText(chiSo.ghiChu);
        boolean theoThoiGian = chiSo.ma.equals(ThongKe_DAO.MA_DOANH_THU_TONG)
                || chiSo.ma.equals(ThongKe_DAO.MA_SO_HOA_DON)
                || chiSo.ma.equals(ThongKe_DAO.MA_SO_LUONG_BAN);
        modelBang.setColumnIdentifiers(new Object[] {
                theoThoiGian ? "Thời gian" : "Danh mục", "Số liệu" });
        NumberFormat dinhDang = NumberFormat.getNumberInstance(VIET_NAM);
        dinhDang.setMaximumFractionDigits(0);
        for (int i = 0; i < chiSo.nhan.length; i++) {
            String giaTri = dinhDang.format(chiSo.giaTri[0][i]);
            if (!chiSo.hauTo.isEmpty()) {
                giaTri += " " + chiSo.hauTo;
            }
            modelBang.addRow(new Object[] { chiSo.nhan[i], giaTri });
        }
        modelBang.addRow(new Object[] { "Tổng cộng", dinhDang.format(chiSo.tong) + " " + chiSo.hauTo });
    }

    private void hienLoi(String loi) {
        lblTrangThai.setForeground(new Color(185, 28, 28));
        lblTrangThai.setText("Không thể tải dữ liệu thống kê.");
        lblTrangThai.setToolTipText(loi);
    }

    private void moDialogTaoBieuDo() {
        if (cboChiSo.getItemCount() == 0) {
            hienLoi("Chưa có dữ liệu thống kê để tạo biểu đồ.");
            return;
        }
        LocalDate tu;
        LocalDate den;
        try {
            tu = docNgay(ngayBatDau);
            den = docNgay(ngayKetThuc);
        } catch (IllegalArgumentException ex) {
            hienLoi(ex.getMessage());
            return;
        }
        List<ChiSo> dsChiSo = new java.util.ArrayList<>();
        for (int i = 0; i < cboChiSo.getItemCount(); i++) {
            dsChiSo.add(cboChiSo.getItemAt(i));
        }
        Window cha = SwingUtilities.getWindowAncestor(this);
        DialogTaoBieuDo dialog = new DialogTaoBieuDo(
                cha, dsChiSo, (ChiSo) cboChiSo.getSelectedItem(), tu, den);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static JSpinner taoBoChonNgay(LocalDate ngay) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(
                Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null,
                java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(112, 30));
        return spinner;
    }

    private static LocalDate docNgay(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException ex) {
            throw new IllegalArgumentException("Ngày nhập không hợp lệ. Hãy dùng định dạng dd/MM/yyyy.", ex);
        }
        Date ngay = (Date) spinner.getValue();
        return ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static void datNgay(JSpinner spinner, LocalDate ngay) {
        spinner.setValue(Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static class ChiSo {
        private final String ma;
        private final String ten;
        private final String donVi;
        private final String hauTo;
        private final String ghiChu;
        private final String[] nhan;
        private final String[] chuoi;
        private final double[][] giaTri;
        private final double tong;

        private ChiSo(ThongKe_DAO.ChiSoThongKe duLieu) {
            ma = duLieu.ma;
            ten = duLieu.ten;
            donVi = duLieu.donVi;
            hauTo = duLieu.hauTo;
            ghiChu = duLieu.ghiChu;
            tong = duLieu.tong;
            nhan = duLieu.nhan.toArray(new String[0]);
            chuoi = duLieu.chuoi.toArray(new String[0]);
            giaTri = new double[duLieu.giaTri.size()][];
            for (int i = 0; i < giaTri.length; i++) {
                giaTri[i] = new double[duLieu.giaTri.get(i).size()];
                for (int j = 0; j < giaTri[i].length; j++) {
                    giaTri[i][j] = duLieu.giaTri.get(i).get(j);
                }
            }
            if (chuoi.length != giaTri.length) {
                throw new IllegalArgumentException("Chỉ số '" + ten
                        + "' có số chuỗi và số dòng giá trị không khớp.");
            }
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    private static class DialogTaoBieuDo extends JDialog {
        private static final long serialVersionUID = 1L;

        private final JComboBox<ChiSo> cboChiSo = new JComboBox<>();
        private final JComboBox<String> cboLoai = new JComboBox<>(LOAI_BIEU_DO);
        private final JSpinner ngayBatDau;
        private final JSpinner ngayKetThuc;
        private final JLabel lblTrangThai = new JLabel(" ");
        private final JButton btnTao = new JButton("Tạo biểu đồ");

        private DialogTaoBieuDo(Window cha, List<ChiSo> danhSachChiSo, ChiSo chiSoChon,
                LocalDate tu, LocalDate den) {
            super(cha, "Tạo biểu đồ thống kê", ModalityType.APPLICATION_MODAL);
            ngayBatDau = taoBoChonNgay(tu);
            ngayKetThuc = taoBoChonNgay(den);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(510, 340);
            setResizable(false);

            JPanel noiDung = new JPanel();
            noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
            noiDung.setBackground(Color.WHITE);
            noiDung.setBorder(new EmptyBorder(18, 20, 16, 20));
            noiDung.add(taoHang("Chỉ số thống kê:", cboChiSo));
            noiDung.add(Box.createVerticalStrut(12));
            noiDung.add(taoHang("Loại biểu đồ:", cboLoai));
            noiDung.add(Box.createVerticalStrut(12));
            noiDung.add(taoHang("Từ ngày:", ngayBatDau));
            noiDung.add(Box.createVerticalStrut(8));
            noiDung.add(taoHang("Đến ngày:", ngayKetThuc));
            noiDung.add(Box.createVerticalStrut(12));
            lblTrangThai.setForeground(new Color(185, 28, 28));
            noiDung.add(lblTrangThai);
            add(noiDung, BorderLayout.CENTER);

            JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            nut.setBackground(new Color(248, 250, 252));
            JButton btnHuy = new JButton("Hủy");
            btnHuy.addActionListener(e -> dispose());
            btnTao.addActionListener(e -> taoBieuDo());
            nut.add(btnHuy);
            nut.add(btnTao);
            add(nut, BorderLayout.SOUTH);

            for (ChiSo chiSo : danhSachChiSo) {
                cboChiSo.addItem(chiSo);
            }
            if (chiSoChon != null) {
                for (int i = 0; i < cboChiSo.getItemCount(); i++) {
                    if (cboChiSo.getItemAt(i).ma.equals(chiSoChon.ma)) {
                        cboChiSo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        private JPanel taoHang(String nhan, Component noiDung) {
            JPanel hang = new JPanel(new BorderLayout(12, 0));
            hang.setOpaque(false);
            JLabel tieuDe = new JLabel(nhan);
            tieuDe.setFont(RangBuocGiaoDien.fontVua(12));
            tieuDe.setPreferredSize(new Dimension(112, 30));
            hang.add(tieuDe, BorderLayout.WEST);
            hang.add(noiDung, BorderLayout.CENTER);
            return hang;
        }

        private void taoBieuDo() {
            LocalDate tu;
            LocalDate den;
            try {
                tu = docNgay(ngayBatDau);
                den = docNgay(ngayKetThuc);
            } catch (IllegalArgumentException ex) {
                lblTrangThai.setText(ex.getMessage());
                return;
            }
            if (tu.isAfter(den)) {
                lblTrangThai.setText("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
                return;
            }
            ChiSo chiSoChon = (ChiSo) cboChiSo.getSelectedItem();
            if (chiSoChon == null) {
                lblTrangThai.setText("Chưa có chỉ số để tạo biểu đồ.");
                return;
            }
            btnTao.setEnabled(false);
            lblTrangThai.setText("Đang nạp dữ liệu cho khoảng thời gian đã chọn...");
            String maChiSo = chiSoChon.ma;
            int loai = loaiBieuDo(cboLoai.getSelectedIndex());
            new SwingWorker<ThongKe_DAO.DuLieuThongKe, Void>() {
                @Override
                protected ThongKe_DAO.DuLieuThongKe doInBackground() throws Exception {
                    return new ThongKe_DAO().layDuLieuThongKe(tu, den);
                }

                @Override
                protected void done() {
                    try {
                        ThongKe_DAO.DuLieuThongKe duLieu = get();
                        ChiSo chiSo = null;
                        for (ThongKe_DAO.ChiSoThongKe ketQua : duLieu.chiSo) {
                            if (ketQua.ma.equals(maChiSo)) {
                                chiSo = new ChiSo(ketQua);
                                break;
                            }
                        }
                        if (chiSo == null) {
                            throw new IllegalStateException("Không tìm thấy chỉ số thống kê đã chọn.");
                        }
                        CuaSoBieuDo cuaSo = new CuaSoBieuDo(chiSo, loai, tu, den);
                        cuaSo.setVisible(true);
                        dispose();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        hienLoi("Đã gián đoạn khi nạp dữ liệu biểu đồ.");
                    } catch (ExecutionException ex) {
                        Throwable nguyenNhan = ex instanceof ExecutionException && ex.getCause() != null
                                ? ex.getCause()
                                : ex;
                        hienLoi("Không thể tạo biểu đồ: " + nguyenNhan.getMessage());
                    } catch (RuntimeException ex) {
                        hienLoi("Không thể tạo biểu đồ: " + ex.getMessage());
                    }
                }
            }.execute();
        }

        private void hienLoi(String loi) {
            lblTrangThai.setText(loi);
            btnTao.setEnabled(true);
        }

        private static int loaiBieuDo(int index) {
            switch (index) {
                case 1:
                    return BieuDoPanel.BIEU_DO_DUONG;
                case 2:
                    return BieuDoPanel.BIEU_DO_DIEM;
                case 3:
                    return BieuDoPanel.BIEU_DO_QUAT;
                default:
                    return BieuDoPanel.BIEU_DO_COT;
            }
        }
    }

    private static class CuaSoBieuDo extends JFrame {
        private static final long serialVersionUID = 1L;

        private final BieuDoPanel bieuDo;

        private CuaSoBieuDo(ChiSo chiSo, int loai, LocalDate tu, LocalDate den) {
            super("Biểu đồ: " + chiSo.ten);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(900, 600);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            bieuDo = new BieuDoPanel();
            bieuDo.datDuLieu(chiSo.ten + " · " + DINH_DANG_NGAY.format(tu) + " – " + DINH_DANG_NGAY.format(den),
                    chiSo.nhan, chiSo.chuoi, chiSo.giaTri, chiSo.donVi);
            bieuDo.setLoaiBieuDo(loai);
            add(bieuDo, BorderLayout.CENTER);
            JButton btnIn = new JButton("In biểu đồ");
            btnIn.addActionListener(e -> inBieuDo());
            JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            thanhCongCu.setBackground(Color.WHITE);
            thanhCongCu.add(btnIn);
            add(thanhCongCu, BorderLayout.SOUTH);
        }

        private void inBieuDo() {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(getTitle());
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) {
                    return java.awt.print.Printable.NO_SUCH_PAGE;
                }
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double scale = Math.min(pageFormat.getImageableWidth() / bieuDo.getWidth(),
                        pageFormat.getImageableHeight() / bieuDo.getHeight());
                g2.scale(scale, scale);
                bieuDo.printAll(g2);
                g2.dispose();
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            if (job.printDialog()) {
                try {
                    job.print();
                } catch (PrinterException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Không thể in biểu đồ: " + ex.getMessage(), "Lỗi in",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
