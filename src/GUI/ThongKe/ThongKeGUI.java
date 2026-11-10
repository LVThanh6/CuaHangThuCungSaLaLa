package GUI.ThongKe;

import GUI.Chung.RangBuocGiaoDien;
import Service.ThongKe.ThongKeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    private final JLabel[] lblTieuDeKpi = new JLabel[6];
    private final JLabel[] lblGiaTriKpi = new JLabel[6];
    private final JLabel[] lblGhiChuKpi = new JLabel[6];
    private final JSpinner ngayBatDau = taoBoChonNgay(LocalDate.now().withDayOfMonth(1));
    private final JSpinner ngayKetThuc = taoBoChonNgay(LocalDate.now());
    private final JComboBox<String> cboKhoangNhanh = new JComboBox<>(
            new String[] { "Tùy chọn", "Hôm nay", "7 ngày qua", "30 ngày qua", "Tháng này", "Năm nay" });
    private final JComboBox<ThongKeService.DanhMuc> cboNhomChiSo = new JComboBox<>(
            ThongKeService.DanhMuc.values());
    private final JComboBox<ChiSo> cboChiSo = new JComboBox<>();
    private final List<ChiSo> tatCaChiSo = new java.util.ArrayList<>();
    private final JLabel lblGhiChuChiSo = new JLabel(" ");
    private final JTextField txtTimNhanVien = new JTextField(22);
    private final JPanel panelTimNhanVien = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new Object[] { "Thời gian / Danh mục", "Số liệu" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangDuLieu = new JTable(modelBang);
    private final ThongKeService thongKeService = new ThongKeService();
    private int maYeuCauDuLieu;
    private ThongKeService.DuLieuBaoCao duLieuThongKe;

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
        cboKhoangNhanh.addActionListener(suKien -> apDungKhoangNhanh());
        dieuKhien.add(cboKhoangNhanh);

        JButton btnApDung = new JButton("Thống kê");
        btnApDung.setFont(RangBuocGiaoDien.fontDam(12));
        btnApDung.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApDung.addActionListener(suKien -> apDungKhoangDaChon());
        dieuKhien.add(btnApDung);

        JButton btnTaoBieuDo = new JButton("Tạo biểu đồ");
        btnTaoBieuDo.setFont(RangBuocGiaoDien.fontDam(12));
        btnTaoBieuDo.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnTaoBieuDo.setForeground(Color.BLACK);
        btnTaoBieuDo.setFocusPainted(false);
        btnTaoBieuDo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaoBieuDo.addActionListener(suKien -> moDialogTaoBieuDo());
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
                { "Doanh thu dịch vụ", "—", "Dịch vụ đã thực hiện" },
                { "Tiền lương", "—", "Nhân viên + bác sĩ" }
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
        cboNhomChiSo.addActionListener(suKien -> capNhatDanhSachChiSo(null));
        JLabel nhan = new JLabel("Chỉ số trong nhóm");
        nhan.setFont(RangBuocGiaoDien.fontDam(13));
        nhan.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboChiSo.setFont(RangBuocGiaoDien.fontThuong(13));
        cboChiSo.addActionListener(suKien -> capNhatBangChiSoChon());
        lblGhiChuChiSo.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChuChiSo.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chonChiSo.add(nhom);
        chonChiSo.add(cboNhomChiSo);
        chonChiSo.add(nhan);
        chonChiSo.add(cboChiSo);
        // Ô tìm kiếm nhân viên
        panelTimNhanVien.setOpaque(false);
        JLabel nhanTim = new JLabel("Tìm nhân viên");
        nhanTim.setFont(RangBuocGiaoDien.fontDam(13));
        nhanTim.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        txtTimNhanVien.setFont(RangBuocGiaoDien.fontThuong(13));
        txtTimNhanVien.setToolTipText("Nhập tên, số điện thoại hoặc mã nhân viên");
        txtTimNhanVien.addActionListener(suKien -> capNhatBangChiSoChon());
        JButton btnTim = new JButton("Tìm kiếm");
        btnTim.setFont(RangBuocGiaoDien.fontDam(12));
        btnTim.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTim.addActionListener(suKien -> capNhatBangChiSoChon());
        JButton btnXoaLoc = new JButton("Xóa lọc");
        btnXoaLoc.setFont(RangBuocGiaoDien.fontThuong(12));
        btnXoaLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXoaLoc.addActionListener(suKien -> {
            txtTimNhanVien.setText("");
            capNhatBangChiSoChon();
        });
        JLabel goiY = new JLabel("(theo tên, SĐT hoặc mã nhân viên)");
        goiY.setFont(RangBuocGiaoDien.fontThuong(12));
        goiY.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        panelTimNhanVien.add(nhanTim);
        panelTimNhanVien.add(txtTimNhanVien);
        panelTimNhanVien.add(btnTim);
        panelTimNhanVien.add(btnXoaLoc);
        panelTimNhanVien.add(goiY);
        panelTimNhanVien.setVisible(false);

        JPanel phanChon = new JPanel(new BorderLayout(0, 8));
        phanChon.setOpaque(false);
        phanChon.add(chonChiSo, BorderLayout.NORTH);
        phanChon.add(panelTimNhanVien, BorderLayout.CENTER);

        JPanel dauBang = new JPanel(new BorderLayout(0, 6));
        dauBang.setOpaque(false);
        dauBang.add(phanChon, BorderLayout.NORTH);
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
        ThongKeService.KhoangThoiGianNhanh khoangNhanhChon;
        switch (chon) {
            case 1:
                khoangNhanhChon = ThongKeService.KhoangThoiGianNhanh.HOM_NAY;
                break;
            case 2:
                khoangNhanhChon = ThongKeService.KhoangThoiGianNhanh.BAY_NGAY_QUA;
                break;
            case 3:
                khoangNhanhChon = ThongKeService.KhoangThoiGianNhanh.BA_MUOI_NGAY_QUA;
                break;
            case 4:
                khoangNhanhChon = ThongKeService.KhoangThoiGianNhanh.THANG_NAY;
                break;
            default:
                khoangNhanhChon = ThongKeService.KhoangThoiGianNhanh.NAM_NAY;
                break;
        }
        ThongKeService.KhoangNgay khoangNgay = ThongKeService.layKhoangNhanh(khoangNhanhChon, LocalDate.now());
        datNgay(ngayBatDau, khoangNgay.getBatDau());
        datNgay(ngayKetThuc, khoangNgay.getKetThuc());
        apDungKhoangDaChon();
    }

    private void apDungKhoangDaChon() {
        try {
            taiLaiDuLieu(docNgay(ngayBatDau), docNgay(ngayKetThuc));
        } catch (IllegalArgumentException loi) {
            hienLoi(loi.getMessage());
        }
    }

    private void taiLaiDuLieu(LocalDate tu, LocalDate den) {
        String thongBaoLoiKhoangNgay = ThongKeService.loiKhoangNgay(tu, den);
        if (thongBaoLoiKhoangNgay != null) {
            hienLoi(thongBaoLoiKhoangNgay);
            return;
        }
        int yeuCau = ++maYeuCauDuLieu;
        lblTrangThai.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTrangThai.setToolTipText(null);
        lblTrangThai.setText("Đang tải dữ liệu thống kê...");
        new SwingWorker<ThongKeService.KetQuaTaiDuLieu, Void>() {
            @Override
            protected ThongKeService.KetQuaTaiDuLieu doInBackground() {
                return thongKeService.taiDuLieu(tu, den);
            }

            @Override
            protected void done() {
                if (yeuCau != maYeuCauDuLieu || isCancelled())
                    return;
                try {
                    ThongKeService.KetQuaTaiDuLieu ketQua = get();
                    if (!ketQua.isThanhCong()) {
                        hienLoi("Không thể tải dữ liệu thống kê: " + ketQua.getThongBaoLoi());
                        return;
                    }
                    capNhatDuLieu(ketQua.getBaoCao());
                    lblMoTa.setText("Từ " + DINH_DANG_NGAY.format(tu) + " đến " + DINH_DANG_NGAY.format(den)
                            + " · Dữ liệu " + ThongKeService.moTaNhomThoiGian(tu, den) + ".");
                    lblTrangThai.setText(" ");
                } catch (InterruptedException loi) {
                    Thread.currentThread().interrupt();
                    hienLoi("Đã gián đoạn khi tải dữ liệu thống kê.");
                } catch (ExecutionException loi) {
                    Throwable nguyenNhan = loi.getCause() == null ? loi : loi.getCause();
                    hienLoi("Không thể tải dữ liệu thống kê: " + nguyenNhan.getMessage());
                }
            }
        }.execute();
    }

    private void capNhatDuLieu(ThongKeService.DuLieuBaoCao duLieu) {
        duLieuThongKe = duLieu;

        ChiSo dangChon = (ChiSo) cboChiSo.getSelectedItem();
        String maDangChon = dangChon == null ? null : dangChon.ma;
        tatCaChiSo.clear();
        for (ThongKeService.ChiSoBaoCao chiSoBaoCao : duLieu.getDanhSachChiSo()) {
            tatCaChiSo.add(new ChiSo(chiSoBaoCao));
        }
        capNhatDanhSachChiSo(maDangChon);
        capNhatKpiTheoNhom();
    }

    private void capNhatDanhSachChiSo(String maCanChon) {
        cboChiSo.removeAllItems();
        ThongKeService.DanhMuc nhomChon = (ThongKeService.DanhMuc) cboNhomChiSo.getSelectedItem();
        for (ChiSo chiSo : tatCaChiSo) {
            if (nhomChon == ThongKeService.layDanhMucCho(chiSo.ma, chiSo.ten)) {
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
        if (duLieuThongKe == null)
            return;
        NumberFormat dinhDang = NumberFormat.getNumberInstance(VIET_NAM);
        dinhDang.setMaximumFractionDigits(0);
        double[] giaTri = {
                duLieuThongKe.getTongDoanhThu(),
                duLieuThongKe.getSoHoaDon(),
                duLieuThongKe.getSoLuongBan(),
                duLieuThongKe.getDoanhThuHangHoa(),
                duLieuThongKe.getDoanhThuDichVu(),
                duLieuThongKe.getTongLuong()
        };
        String[] hauTo = { "đ", "", "", "đ", "đ", "đ" };
        for (int i = 0; i < giaTri.length; i++) {
            lblGiaTriKpi[i].setText(dinhDang.format(giaTri[i]) + " " + hauTo[i]);
            lblGhiChuKpi[i].setText("Trong khoảng thời gian đã chọn");
        }
    }

    private void capNhatBangChiSoChon() {
        ChiSo chiSo = (ChiSo) cboChiSo.getSelectedItem();
        modelBang.setRowCount(0);

        boolean laNhomNhanSu = cboNhomChiSo.getSelectedItem() == ThongKeService.DanhMuc.NHAN_SU;
        panelTimNhanVien.setVisible(laNhomNhanSu);
        String tuKhoa = laNhomNhanSu ? txtTimNhanVien.getText().trim() : "";

        if (chiSo == null || chiSo.nhan.length == 0 || chiSo.giaTri.length == 0) {
            lblGhiChuChiSo.setText("Chưa có dữ liệu cho chỉ số này.");
            return;
        }
        lblGhiChuChiSo.setText(chiSo.ghiChu);
        boolean theoThoiGian = ThongKeService.laChuoiThoiGian(chiSo.ma);
        modelBang.setColumnIdentifiers(new Object[] {
                theoThoiGian ? "Thời gian" : "Danh mục", "Số liệu" });
        NumberFormat dinhDang = NumberFormat.getNumberInstance(VIET_NAM);
        dinhDang.setMaximumFractionDigits(0);
        String hauTo = chiSo.hauTo.isEmpty() ? "" : " " + chiSo.hauTo;
        double tongHienThi = 0;
        int soDong = 0;
        for (int i = 0; i < chiSo.nhan.length; i++) {
            if (!tuKhoa.isEmpty() && !thongKeService.khopTuKhoaNhanVien(tuKhoa, chiSo.nhan[i])) {
                continue;
            }
            modelBang.addRow(new Object[] { chiSo.nhan[i], dinhDang.format(chiSo.giaTri[0][i]) + hauTo });
            tongHienThi += chiSo.giaTri[0][i];
            soDong++;
        }
        if (tuKhoa.isEmpty()) {
            modelBang.addRow(new Object[] { "Tổng cộng", dinhDang.format(chiSo.tong) + hauTo });
        } else if (soDong == 0) {
            lblGhiChuChiSo.setText("Không tìm thấy nhân viên nào khớp với \"" + tuKhoa + "\".");
        } else {
            modelBang.addRow(
                    new Object[] { "Tổng cộng (" + soDong + " kết quả)", dinhDang.format(tongHienThi) + hauTo });
        }
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
        } catch (IllegalArgumentException loi) {
            hienLoi(loi.getMessage());
            return;
        }
        List<ChiSo> dsChiSo = new java.util.ArrayList<>();
        for (int i = 0; i < cboChiSo.getItemCount(); i++) {
            dsChiSo.add(cboChiSo.getItemAt(i));
        }
        Window cha = SwingUtilities.getWindowAncestor(this);
        DialogTaoBieuDo dialog = new DialogTaoBieuDo(
                cha, dsChiSo, (ChiSo) cboChiSo.getSelectedItem(), tu, den, thongKeService);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static JSpinner taoBoChonNgay(LocalDate ngay) {
        JSpinner conXoay = new JSpinner(new SpinnerDateModel(
                Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null,
                java.util.Calendar.DAY_OF_MONTH));
        conXoay.setEditor(new JSpinner.DateEditor(conXoay, "dd/MM/yyyy"));
        conXoay.setPreferredSize(new Dimension(112, 30));
        return conXoay;
    }

    private static LocalDate docNgay(JSpinner conXoay) {
        try {
            conXoay.commitEdit();
        } catch (java.text.ParseException loi) {
            throw new IllegalArgumentException("Ngày nhập không hợp lệ. Hãy dùng định dạng dd/MM/yyyy.", loi);
        }
        Date ngay = (Date) conXoay.getValue();
        return ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static void datNgay(JSpinner conXoay, LocalDate ngay) {
        conXoay.setValue(Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()));
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

        private ChiSo(ThongKeService.ChiSoBaoCao duLieu) {
            ma = duLieu.getMa();
            ten = duLieu.getTen();
            donVi = duLieu.getDonVi();
            hauTo = duLieu.getHauTo();
            ghiChu = duLieu.getMoTa();
            tong = duLieu.getTong();
            nhan = duLieu.getNhan().toArray(new String[0]);
            chuoi = duLieu.getChuoi().toArray(new String[0]);
            giaTri = new double[duLieu.getGiaTri().size()][];
            for (int i = 0; i < giaTri.length; i++) {
                giaTri[i] = new double[duLieu.getGiaTri().get(i).size()];
                for (int j = 0; j < giaTri[i].length; j++) {
                    giaTri[i][j] = duLieu.getGiaTri().get(i).get(j);
                }
            }
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    private class DialogTaoBieuDo extends JDialog {
        private static final long serialVersionUID = 1L;

        private final JComboBox<ChiSo> cboChiSo = new JComboBox<>();
        private final JComboBox<String> cboLoai = new JComboBox<>(LOAI_BIEU_DO);
        private final JSpinner ngayBatDau;
        private final JSpinner ngayKetThuc;
        private final JLabel lblTrangThai = new JLabel(" ");
        private final JButton btnTao = new JButton("Tạo biểu đồ");
        private final ThongKeService thongKeService;

        private DialogTaoBieuDo(Window cha, List<ChiSo> danhSachChiSo, ChiSo chiSoChon,
                LocalDate tu, LocalDate den, ThongKeService thongKeService) {
            super(cha, "Tạo biểu đồ thống kê", ModalityType.APPLICATION_MODAL);
            this.thongKeService = thongKeService;
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
            btnHuy.addActionListener(suKien -> dispose());
            btnTao.addActionListener(suKien -> taoBieuDo());
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
            } catch (IllegalArgumentException loi) {
                lblTrangThai.setText(loi.getMessage());
                return;
            }
            String thongBaoLoiKhoangNgay = ThongKeService.loiKhoangNgay(tu, den);
            if (thongBaoLoiKhoangNgay != null) {
                lblTrangThai.setText(thongBaoLoiKhoangNgay);
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
            new SwingWorker<ThongKeService.KetQuaTaiDuLieu, Void>() {
                @Override
                protected ThongKeService.KetQuaTaiDuLieu doInBackground() {
                    return thongKeService.taiDuLieu(tu, den);
                }

                @Override
                protected void done() {
                    try {
                        ThongKeService.KetQuaTaiDuLieu ketQua = get();
                        if (!ketQua.isThanhCong()) {
                            throw new IllegalStateException(ketQua.getThongBaoLoi());
                        }
                        ChiSo chiSo = null;
                        for (ThongKeService.ChiSoBaoCao ketQuaBaoCao : ketQua.getBaoCao().getDanhSachChiSo()) {
                            if (ketQuaBaoCao.getMa().equals(maChiSo)) {
                                chiSo = new ChiSo(ketQuaBaoCao);
                                break;
                            }
                        }
                        if (chiSo == null) {
                            throw new IllegalStateException("Không tìm thấy chỉ số thống kê đã chọn.");
                        }
                        CuaSoBieuDo cuaSo = new CuaSoBieuDo(getOwner(), chiSo, loai, tu, den);
                        cuaSo.setVisible(true);
                        dispose();
                    } catch (InterruptedException loi) {
                        Thread.currentThread().interrupt();
                        hienLoi("Đã gián đoạn khi nạp dữ liệu biểu đồ.");
                    } catch (ExecutionException loi) {
                        Throwable nguyenNhan = loi instanceof ExecutionException && loi.getCause() != null
                                ? loi.getCause()
                                : loi;
                        hienLoi("Không thể tạo biểu đồ: " + nguyenNhan.getMessage());
                    } catch (RuntimeException loi) {
                        hienLoi("Không thể tạo biểu đồ: " + loi.getMessage());
                    }
                }
            }.execute();
        }

        private void hienLoi(String loi) {
            lblTrangThai.setText(loi);
            btnTao.setEnabled(true);
        }

        private int loaiBieuDo(int chiSo) {
            switch (chiSo) {
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

    private static class CuaSoBieuDo extends JDialog {
        private static final long serialVersionUID = 1L;
        private final BieuDoPanel bieuDo;

        private CuaSoBieuDo(Window theCha, ChiSo chiSo, int loai, LocalDate tu, LocalDate den) {
            super(theCha, "Biểu đồ: " + chiSo.ten, ModalityType.MODELESS);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(900, 600);
            setLocationRelativeTo(theCha);
            setLayout(new BorderLayout());

            bieuDo = new BieuDoPanel();
            bieuDo.datDuLieu(chiSo.ten + " · " + DINH_DANG_NGAY.format(tu) + " – " + DINH_DANG_NGAY.format(den),
                    chiSo.nhan, chiSo.chuoi, chiSo.giaTri, chiSo.donVi);
            bieuDo.setLoaiBieuDo(loai);
            add(bieuDo, BorderLayout.CENTER);

            JButton btnIn = new JButton("In biểu đồ");
            btnIn.addActionListener(suKien -> inBieuDo());
            JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            thanhCongCu.setBackground(Color.WHITE);
            thanhCongCu.add(btnIn);
            add(thanhCongCu, BorderLayout.SOUTH);
        }

        private void inBieuDo() {
            PrinterJob tienTrinhIn = PrinterJob.getPrinterJob();
            tienTrinhIn.setJobName(getTitle());
            tienTrinhIn.setPrintable((graphics, dinhDangTrang, chiSoTrang) -> {
                if (chiSoTrang > 0)
                    return java.awt.print.Printable.NO_SUCH_PAGE;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
                g2.translate(dinhDangTrang.getImageableX(), dinhDangTrang.getImageableY());
                double tyLe = Math.min(dinhDangTrang.getImageableWidth() / bieuDo.getWidth(),
                        dinhDangTrang.getImageableHeight() / bieuDo.getHeight());
                g2.scale(tyLe, tyLe);
                bieuDo.printAll(g2);
                g2.dispose();
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            if (tienTrinhIn.printDialog()) {
                try {
                    tienTrinhIn.print();
                } catch (PrinterException loi) {
                    JOptionPane.showMessageDialog(this,
                            "Không thể in biểu đồ: " + loi.getMessage(), "Lỗi in",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}

/**
 * Lớp Giao diện hỗ trợ vẽ Biểu đồ
 */
class BieuDoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int BIEU_DO_COT = 1;
    public static final int BIEU_DO_DUONG = 2;
    public static final int BIEU_DO_DIEM = 3;
    public static final int BIEU_DO_QUAT = 4;

    private static final Color[] BGC_MAU = {
            new Color(13, 148, 136), new Color(14, 165, 233),
            new Color(245, 158, 11), new Color(139, 92, 246),
            new Color(236, 72, 153), new Color(16, 185, 129)
    };

    private final DecimalFormat dinhDangSo = new DecimalFormat("#,##0.##");
    private String tieuDe = "";
    private String donVi = "";
    private String[] nhan = new String[0];
    private String[] chuoi = new String[0];
    private double[][] giaTri = new double[0][];
    private int loaiBieuDo = BIEU_DO_COT;
    private boolean hienNhan = true;
    private boolean hienLuoi = true;
    private boolean hienChuGiai = true;

    public BieuDoPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(640, 400));
        setToolTipText("");
    }

    public void datDuLieu(String tieuDe, String[] nhan, String[] chuoi, double[][] giaTri, String donVi) {
        if (nhan == null || chuoi == null || giaTri == null || giaTri.length != chuoi.length) {
            throw new IllegalArgumentException("Dữ liệu biểu đồ phải có một dòng giá trị cho mỗi chuỗi.");
        }
        for (double[] hang : giaTri) {
            if (hang == null || hang.length != nhan.length) {
                throw new IllegalArgumentException("Mỗi chuỗi biểu đồ phải có một giá trị cho mỗi nhãn.");
            }
        }
        this.tieuDe = tieuDe == null ? "" : tieuDe;
        this.nhan = Arrays.copyOf(nhan, nhan.length);
        this.chuoi = Arrays.copyOf(chuoi, chuoi.length);
        this.giaTri = new double[giaTri.length][];
        for (int i = 0; i < giaTri.length; i++) {
            this.giaTri[i] = Arrays.copyOf(giaTri[i], giaTri[i].length);
        }
        this.donVi = donVi == null ? "" : donVi;
        repaint();
    }

    public void datTuyChon(boolean hienNhan, boolean hienLuoi, boolean hienChuGiai) {
        this.hienNhan = hienNhan;
        this.hienLuoi = hienLuoi;
        this.hienChuGiai = hienChuGiai;
        repaint();
    }

    public void setLoaiBieuDo(int loai) {
        if (loai < BIEU_DO_COT || loai > BIEU_DO_QUAT) {
            throw new IllegalArgumentException("Loại biểu đồ không được hỗ trợ.");
        }
        this.loaiBieuDo = loai;
        repaint();
    }

    public int getLoaiBieuDo() {
        return loaiBieuDo;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!tieuDe.isEmpty()) {
            g2.setFont(RangBuocGiaoDien.fontDam(14));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            g2.drawString(tieuDe, 20, 24);
        }
        if (nhan.length == 0 || giaTri.length == 0) {
            g2.setFont(RangBuocGiaoDien.fontThuong(13));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString("Không có dữ liệu trong khoảng thời gian đã chọn.", 20, 54);
            g2.dispose();
            return;
        }

        if (loaiBieuDo == BIEU_DO_QUAT)
            veBieuDoQuat(g2);
        else
            veBieuDoTheoTruc(g2);
        g2.dispose();
    }

    private void veBieuDoTheoTruc(Graphics2D g2) {
        int le = 76, phai = 24, tren = tieuDe.isEmpty() ? 24 : 44;
        int day = hienChuGiai ? getHeight() - 62 : getHeight() - 34;
        int rong = getWidth() - le - phai, cao = day - tren;
        if (rong <= 0 || cao <= 0)
            return;

        double lonNhat = 0;
        for (double[] hang : giaTri) {
            for (double mangGiaTri : hang)
                lonNhat = Math.max(lonNhat, mangGiaTri);
        }
        double tran = mucTran(lonNhat);
        FontMetrics thuocDoChu = g2.getFontMetrics(RangBuocGiaoDien.fontThuong(11));
        g2.setFont(RangBuocGiaoDien.fontThuong(11));
        for (int i = 0; i <= 4; i++) {
            int toaDoY = day - cao * i / 4;
            if (i > 0 && hienLuoi) {
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(le, toaDoY, le + rong, toaDoY);
            }
            String chuoiChu = dinhDangSo.format(tran * i / 4);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString(chuoiChu, le - thuocDoChu.stringWidth(chuoiChu) - 8, toaDoY + thuocDoChu.getAscent() / 2);
        }
        g2.setColor(new Color(203, 213, 225));
        g2.drawLine(le, day, le + rong, day);

        int soNhan = nhan.length, soChuoi = giaTri.length;
        double khoangCach = rong / (double) soNhan;
        for (int s = 0; s < soChuoi; s++) {
            g2.setColor(BGC_MAU[s % BGC_MAU.length]);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < soNhan; i++) {
                int toaDoX = (int) Math.round(le + khoangCach * (i + 0.5));
                int toaDoY = day - (int) Math.round(giaTri[s][i] / tran * cao);
                if (loaiBieuDo == BIEU_DO_COT) {
                    double rongNhom = Math.min(khoangCach * 0.72, 72);
                    double rongCotChinh = rongNhom / soChuoi;
                    double viTriXCot = toaDoX - rongNhom / 2 + s * rongCotChinh;
                    g2.fillRoundRect((int) viTriXCot, toaDoY, Math.max(1, (int) rongCotChinh - 2),
                            Math.max(1, day - toaDoY), 5, 5);
                } else if (loaiBieuDo == BIEU_DO_DUONG && i > 0) {
                    int toaDoXTruoc = (int) Math.round(le + khoangCach * (i - 0.5));
                    int toaDoYTruoc = day - (int) Math.round(giaTri[s][i - 1] / tran * cao);
                    g2.drawLine(toaDoXTruoc, toaDoYTruoc, toaDoX, toaDoY);
                }
                if (loaiBieuDo == BIEU_DO_DIEM || loaiBieuDo == BIEU_DO_DUONG) {
                    g2.fill(new Ellipse2D.Double(toaDoX - 4, toaDoY - 4, 8, 8));
                }
                if (hienNhan) {
                    String chuoiNhan = dinhDangSo.format(giaTri[s][i]) + (donVi.isEmpty() ? "" : " " + donVi);
                    g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                    g2.drawString(chuoiNhan, toaDoX - g2.getFontMetrics().stringWidth(chuoiNhan) / 2, toaDoY - 8);
                    g2.setColor(BGC_MAU[s % BGC_MAU.length]);
                }
            }
        }

        g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
        int soNhanToiDa = Math.max(1, rong / 90);
        int buocNhay = Math.max(1, (int) Math.ceil(soNhan / (double) soNhanToiDa));
        for (int i = 0; i < soNhan; i += buocNhay) {
            String chuoiNhan = nhan[i];
            int toaDoX = (int) Math.round(le + khoangCach * (i + 0.5));
            g2.drawString(chuoiNhan, toaDoX - g2.getFontMetrics().stringWidth(chuoiNhan) / 2, day + 18);
        }
        if (hienChuGiai)
            veChuGiai(g2, getHeight() - 16);
    }

    private void veBieuDoQuat(Graphics2D g2) {
        double[] mangGiaTri = giaTri[0];
        double tongGiaTri = 0;
        for (double giaTriHienTai : mangGiaTri)
            tongGiaTri += Math.max(0, giaTriHienTai);
        if (tongGiaTri <= 0) {
            g2.setFont(RangBuocGiaoDien.fontThuong(13));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString("Không có giá trị dương để vẽ biểu đồ quạt.", 20, 54);
            return;
        }

        int kichThuoc = Math.max(1, Math.min(getHeight() - 100, getWidth() / 2 - 80));
        int toaDoX = 24, toaDoY = Math.max(42, (getHeight() - kichThuoc) / 2);
        double gocBatDau = 90;
        for (int i = 0; i < mangGiaTri.length; i++) {
            double giaTriHienTai = Math.max(0, mangGiaTri[i]);
            if (giaTriHienTai == 0)
                continue;
            double gocChuyen = giaTriHienTai / tongGiaTri * 360;
            g2.setColor(BGC_MAU[i % BGC_MAU.length]);
            g2.fill(new Arc2D.Double(toaDoX, toaDoY, kichThuoc, kichThuoc, gocBatDau, gocChuyen, Arc2D.PIE));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(toaDoX, toaDoY, kichThuoc, kichThuoc, gocBatDau, gocChuyen, Arc2D.PIE));
            gocBatDau += gocChuyen;
        }

        int toaDoXChuGiai = toaDoX + kichThuoc + 22, toaDoYChuGiai = Math.max(52, toaDoY + 16);
        int chieuRongConLai = Math.max(1, getWidth() - toaDoXChuGiai - 12);
        int soHangToiDa = Math.max(1, (getHeight() - 88) / 24);
        int soCot = (nhan.length + soHangToiDa - 1) / soHangToiDa;
        int rongCot = Math.max(1, chieuRongConLai / soCot);
        g2.setFont(RangBuocGiaoDien.fontThuong(12));
        for (int i = 0; i < nhan.length; i++) {
            int cot = i / soHangToiDa, hang = i % soHangToiDa;
            int viTriX = toaDoXChuGiai + cot * rongCot, viTriY = toaDoYChuGiai + hang * 24;
            g2.setColor(BGC_MAU[i % BGC_MAU.length]);
            g2.fillRoundRect(viTriX, viTriY - 10, 12, 12, 4, 4);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            String chuoiChu = nhan[i] + ": " + dinhDangSo.format(mangGiaTri[i]) + " (" +
                    String.format("%.1f%%", Math.max(0, mangGiaTri[i]) * 100 / tongGiaTri) + ")";
            int rongChuToiDa = Math.max(1, rongCot - 22);
            FontMetrics thuocDoChu = g2.getFontMetrics();
            while (!chuoiChu.isEmpty() && thuocDoChu.stringWidth(chuoiChu) > rongChuToiDa)
                chuoiChu = chuoiChu.substring(0, chuoiChu.length() - 1);
            g2.drawString(chuoiChu, viTriX + 20, viTriY);
        }
    }

    private void veChuGiai(Graphics2D g2, int toaDoY) {
        g2.setFont(RangBuocGiaoDien.fontThuong(11));
        int toaDoX = 20;
        for (int i = 0; i < chuoi.length; i++) {
            g2.setColor(BGC_MAU[i % BGC_MAU.length]);
            g2.fillRoundRect(toaDoX, toaDoY - 9, 10, 10, 3, 3);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.drawString(chuoi[i], toaDoX + 15, toaDoY);
            toaDoX += 28 + g2.getFontMetrics().stringWidth(chuoi[i]);
        }
    }

    private static double mucTran(double lonNhat) {
        if (lonNhat <= 0)
            return 1;
        double tho = lonNhat / 4;
        double coSo = Math.pow(10, Math.floor(Math.log10(tho)));
        double phanDu = tho / coSo;
        double buocNhay = phanDu <= 1 ? 1 : phanDu <= 2 ? 2 : phanDu <= 5 ? 5 : 10;
        return buocNhay * coSo * 4;
    }

    public BufferedImage xuatHinhAnh(int rongAnh, int caoAnh) {
        BufferedImage hinhAnh = new BufferedImage(rongAnh, caoAnh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = hinhAnh.createGraphics();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, rongAnh, caoAnh);
        setSize(rongAnh, caoAnh);
        paint(g2);
        g2.dispose();
        return hinhAnh;
    }
}