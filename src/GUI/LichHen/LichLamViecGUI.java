package GUI.LichHen;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import dao.LichLamViec_DAO;
import entity.LichLamViec_entity;
import entity.NhanSuLichLamViec_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LichLamViecGUI extends JPanel {

    private enum CheDoXem {
        NGAY("Ngày"),
        TUAN("Tuần"),
        THANG("Tháng");

        private final String ten;

        CheDoXem(String ten) {
            this.ten = ten;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    private static final String[] TRANG_THAI_DANH_DAU = {
            LichLamViec_entity.TRANG_THAI_CHUA_LAM,
            LichLamViec_entity.TRANG_THAI_DANG_LAM,
            LichLamViec_entity.TRANG_THAI_HOAN_THANH,
            LichLamViec_entity.TRANG_THAI_VANG_MAT,
            LichLamViec_entity.TRANG_THAI_DI_TRE,
            LichLamViec_entity.TRANG_THAI_VE_SOM,
            LichLamViec_entity.TRANG_THAI_DA_HUY
    };

    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DINH_DANG_THU =
            DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("vi-VN"));

    private final LichLamViec_DAO dao = new LichLamViec_DAO();
    private final List<LichLamViec_entity> danhSachLich = new ArrayList<>();
    private final Map<LocalDate, Integer> soLichTheoNgay = new HashMap<>();
    private final Timer timerChotNgay = new Timer(0, e -> xuLyChotNgay());

    private LocalDate ngayDangChon = LocalDate.now();
    private CheDoXem cheDoXem = CheDoXem.NGAY;

    private JLabel lblThangNam;
    private JLabel lblTieuDeLich;
    private JLabel lblThongBao;
    private JLabel lblTongQuan;
    private JPanel gridLich;
    private TableMau bangLich;
    private JComboBox<CheDoXem> cbCheDoXem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnTrangThai;
    private JButton btnThem;

    public LichLamViecGUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(taoThanhTieuDe(), BorderLayout.NORTH);
        add(taoNoiDungChinh(), BorderLayout.CENTER);

        timerChotNgay.setRepeats(false);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (isDisplayable()) {
                    lapLichChotNgay();
                } else {
                    timerChotNgay.stop();
                }
            }
        });

        taiDuLieu();
    }

    private JPanel taoThanhTieuDe() {
        JPanel pnl = new JPanel(new BorderLayout(12, 8));
        pnl.setOpaque(false);

        JPanel pnlMoTa = new JPanel();
        pnlMoTa.setLayout(new BoxLayout(pnlMoTa, BoxLayout.Y_AXIS));
        pnlMoTa.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Lịch làm việc");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(22));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel("Theo dõi ca làm, quản lý lịch và cập nhật chấm công");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pnlMoTa.add(lblTieuDe);
        pnlMoTa.add(Box.createVerticalStrut(4));
        pnlMoTa.add(lblMoTa);

        JPanel pnlDieuHuong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlDieuHuong.setOpaque(false);

        cbCheDoXem = new JComboBox<>(CheDoXem.values());
        cbCheDoXem.setSelectedItem(cheDoXem);
        cbCheDoXem.setFont(RangBuocGiaoDien.fontDam(12));
        cbCheDoXem.addActionListener(e -> {
            cheDoXem = (CheDoXem) cbCheDoXem.getSelectedItem();
            taiDuLieu();
        });

        JButton btnTruoc = taoNutDieuHuong("◀");
        btnTruoc.setToolTipText("Khoảng thời gian trước");
        btnTruoc.addActionListener(e -> diChuyenKhoang(-1));

        JButton btnSau = taoNutDieuHuong("▶");
        btnSau.setToolTipText("Khoảng thời gian tiếp theo");
        btnSau.addActionListener(e -> diChuyenKhoang(1));

        JButton btnHomNay = new JButton("Hôm nay");
        btnHomNay.setFont(RangBuocGiaoDien.fontDam(12));
        btnHomNay.setFocusPainted(false);
        btnHomNay.addActionListener(e -> {
            ngayDangChon = LocalDate.now();
            taiDuLieu();
        });

        pnlDieuHuong.add(new JLabel("Xem theo:"));
        pnlDieuHuong.add(cbCheDoXem);
        pnlDieuHuong.add(btnTruoc);
        pnlDieuHuong.add(btnSau);
        pnlDieuHuong.add(btnHomNay);

        pnl.add(pnlMoTa, BorderLayout.WEST);
        pnl.add(pnlDieuHuong, BorderLayout.EAST);
        return pnl;
    }

    private JSplitPane taoNoiDungChinh() {
        JPanel pnlLich = taoLichThang();
        JPanel pnlBang = taoBangLich();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlLich, pnlBang);
        splitPane.setDividerLocation(315);
        splitPane.setResizeWeight(0);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JPanel taoLichThang() {
        JPanel pnl = taoThe();
        pnl.setLayout(new BorderLayout(0, 12));

        JPanel pnlThang = new JPanel(new BorderLayout(6, 0));
        pnlThang.setOpaque(false);

        JButton btnThangTruoc = taoNutDieuHuong("◀");
        btnThangTruoc.addActionListener(e -> doiThang(-1));
        JButton btnThangSau = taoNutDieuHuong("▶");
        btnThangSau.addActionListener(e -> doiThang(1));

        lblThangNam = new JLabel("", SwingConstants.CENTER);
        lblThangNam.setFont(RangBuocGiaoDien.fontDam(16));
        lblThangNam.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        pnlThang.add(btnThangTruoc, BorderLayout.WEST);
        pnlThang.add(lblThangNam, BorderLayout.CENTER);
        pnlThang.add(btnThangSau, BorderLayout.EAST);

        gridLich = new JPanel(new GridLayout(0, 7, 4, 5));
        gridLich.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Số ca trong ngày · Chọn ngày để xem lịch");
        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pnl.add(pnlThang, BorderLayout.NORTH);
        pnl.add(gridLich, BorderLayout.CENTER);
        pnl.add(lblGhiChu, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel taoBangLich() {
        JPanel pnl = taoThe();
        pnl.setLayout(new BorderLayout(0, 12));

        JPanel pnlDau = new JPanel(new BorderLayout(8, 8));
        pnlDau.setOpaque(false);

        JPanel pnlThongTin = new JPanel();
        pnlThongTin.setLayout(new BoxLayout(pnlThongTin, BoxLayout.Y_AXIS));
        pnlThongTin.setOpaque(false);

        lblTieuDeLich = new JLabel();
        lblTieuDeLich.setFont(RangBuocGiaoDien.fontDam(17));
        lblTieuDeLich.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

        lblTongQuan = new JLabel(" ");
        lblTongQuan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTongQuan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pnlThongTin.add(lblTieuDeLich);
        pnlThongTin.add(Box.createVerticalStrut(4));
        pnlThongTin.add(lblTongQuan);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlNut.setOpaque(false);

        btnThem = new JButton("+ Thêm lịch");
        btnThem.setFont(RangBuocGiaoDien.fontDam(12));
        btnThem.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThem.setForeground(Color.WHITE);
        btnThem.setFocusPainted(false);
        btnThem.addActionListener(e -> themLich());

        btnSua = new JButton("Sửa lịch");
        btnSua.setFont(RangBuocGiaoDien.fontDam(12));
        btnSua.addActionListener(e -> suaLich());

        btnTrangThai = new JButton("Trạng thái");
        btnTrangThai.setFont(RangBuocGiaoDien.fontDam(12));
        btnTrangThai.addActionListener(e -> capNhatTrangThai());

        btnXoa = new JButton("Xóa");
        btnXoa.setFont(RangBuocGiaoDien.fontDam(12));
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.addActionListener(e -> xoaLich());

        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnTrangThai);
        pnlNut.add(btnXoa);

        pnlDau.add(pnlThongTin, BorderLayout.WEST);
        pnlDau.add(pnlNut, BorderLayout.EAST);

        String[] cot = {"Ngày", "Bắt đầu", "Kết thúc", "Mã NS", "Nhân sự", "Vai trò", "Trạng thái"};
        bangLich = new TableMau(cot, new Object[0][cot.length]);
        bangLich.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangLich.setAutoCreateRowSorter(true);
        bangLich.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatTrangThaiNut();
            }
        });
        bangLich.getColumnModel().getColumn(0).setPreferredWidth(95);
        bangLich.getColumnModel().getColumn(1).setPreferredWidth(75);
        bangLich.getColumnModel().getColumn(2).setPreferredWidth(75);
        bangLich.getColumnModel().getColumn(3).setPreferredWidth(75);
        bangLich.getColumnModel().getColumn(4).setPreferredWidth(170);
        bangLich.getColumnModel().getColumn(5).setPreferredWidth(110);
        bangLich.getColumnModel().getColumn(6).setPreferredWidth(110);
        bangLich.getColumnModel().getColumn(6).setCellRenderer(new RendererTrangThai());

        JScrollPane scroll = new JScrollPane(bangLich);
        scroll.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        scroll.getViewport().setBackground(Color.WHITE);

        lblThongBao = new JLabel(" ");
        lblThongBao.setFont(RangBuocGiaoDien.fontThuong(12));
        lblThongBao.setBorder(new EmptyBorder(2, 2, 0, 2));

        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        pnl.add(lblThongBao, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel taoThe() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return pnl;
    }

    private boolean taiDuLieu() {
        KhoangNgay khoang = layKhoangXem();
        try {
            danhSachLich.clear();
            danhSachLich.addAll(dao.getLichTrongKhoang(khoang.tuNgay, khoang.denNgayExclusive));

            YearMonth thang = YearMonth.from(ngayDangChon);
            soLichTheoNgay.clear();
            soLichTheoNgay.putAll(dao.getSoLichTheoNgay(thang.atDay(1), thang.plusMonths(1).atDay(1)));

            veLichThang();
            veBangLich();
            hienThiThongBao(null, false);
            return true;
        } catch (SQLException | IllegalArgumentException e) {
            danhSachLich.clear();
            soLichTheoNgay.clear();
            veLichThang();
            veBangLich();
            hienThiThongBao("Không tải được lịch làm việc: " + e.getMessage(), true);
            return false;
        }
    }

    private void veLichThang() {
        YearMonth thang = YearMonth.from(ngayDangChon);
        lblThangNam.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());
        gridLich.removeAll();

        String[] tenThu = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String thu : tenThu) {
            JLabel lbl = new JLabel(thu, SwingConstants.CENTER);
            lbl.setFont(RangBuocGiaoDien.fontDam(11));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            gridLich.add(lbl);
        }

        int lechDauThang = thang.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < lechDauThang; i++) {
            gridLich.add(new JLabel(""));
        }

        for (int ngay = 1; ngay <= thang.lengthOfMonth(); ngay++) {
            LocalDate ngayLich = thang.atDay(ngay);
            int soCa = soLichTheoNgay.getOrDefault(ngayLich, 0);
            JButton btnNgay = new JButton(taoNhanNgay(ngay, soCa));
            btnNgay.setFont(RangBuocGiaoDien.fontDam(12));
            btnNgay.setHorizontalAlignment(SwingConstants.CENTER);
            btnNgay.setFocusPainted(false);
            btnNgay.setMargin(new Insets(2, 0, 2, 0));
            btnNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnNgay.setToolTipText(soCa + " ca làm việc");

            if (ngayLich.equals(ngayDangChon)) {
                btnNgay.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnNgay.setForeground(Color.WHITE);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 2, true));
            } else if (ngayLich.equals(LocalDate.now())) {
                btnNgay.setBackground(new Color(220, 244, 238));
                btnNgay.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_CHU_DAO, 1, true));
            } else {
                btnNgay.setBackground(Color.WHITE);
                btnNgay.setForeground(ngayLich.isBefore(LocalDate.now())
                        ? Color.GRAY : RangBuocGiaoDien.MAU_CHU_CHINH);
                btnNgay.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
            }
            btnNgay.addActionListener(e -> {
                ngayDangChon = ngayLich;
                taiDuLieu();
            });
            gridLich.add(btnNgay);
        }

        int tongONgay = lechDauThang + thang.lengthOfMonth();
        int soOTrong = (7 - tongONgay % 7) % 7;
        for (int i = 0; i < soOTrong; i++) {
            gridLich.add(new JLabel(""));
        }

        gridLich.revalidate();
        gridLich.repaint();
    }

    private String taoNhanNgay(int ngay, int soCa) {
        LocalDate ngayHienThi = LocalDate.of(ngayDangChon.getYear(), ngayDangChon.getMonthValue(), ngay);
        String mauSoCa = ngayHienThi.equals(ngayDangChon) ? "#ffffff" : "#64748b";
        String dongSoCa = soCa == 0 ? "&nbsp;" : soCa + " ca";
        return "<html><center><b>" + ngay + "</b><br><span style='font-size:9px;color:"
                + mauSoCa + "'>" + dongSoCa + "</span></center></html>";
    }

    private void veBangLich() {
        bangLich.xoaTatCaDuLieu();
        DateTimeFormatter ngayHienThi = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LichLamViec_entity lich : danhSachLich) {
            bangLich.themDong(new Object[]{
                    lich.getNgay() != null ? lich.getNgay().format(ngayHienThi) : "",
                    dinhDangGio(lich.getGioBatDau()),
                    dinhDangGio(lich.getGioKetThuc()),
                    lich.getMaNhanSu(),
                    lich.getTenNhanSu(),
                    lich.getVaiTro(),
                    lich.getTrangThai()
            });
        }

        KhoangNgay khoang = layKhoangXem();
        lblTieuDeLich.setText(tieuDeKhoang(khoang));
        int soCa = danhSachLich.size();
        int soHoanThanh = 0;
        int soChuaLam = 0;
        for (LichLamViec_entity lich : danhSachLich) {
            if (LichLamViec_entity.TRANG_THAI_HOAN_THANH.equals(lich.getTrangThai())) {
                soHoanThanh++;
            } else if (LichLamViec_entity.TRANG_THAI_CHUA_LAM.equals(lich.getTrangThai())) {
                soChuaLam++;
            }
        }
        lblTongQuan.setText(soCa + " ca làm  ·  " + soChuaLam + " chưa làm  ·  " + soHoanThanh + " hoàn thành");
        capNhatTrangThaiNut();
    }

    private String tieuDeKhoang(KhoangNgay khoang) {
        switch (cheDoXem) {
            case NGAY:
                return ngayDangChon.format(DINH_DANG_THU) + ", " + ngayDangChon.format(DINH_DANG_NGAY);
            case TUAN:
                LocalDate ngayCuoiTuan = khoang.denNgayExclusive.minusDays(1);
                return "Tuần " + khoang.tuNgay.format(DINH_DANG_NGAY)
                        + " – " + ngayCuoiTuan.format(DINH_DANG_NGAY);
            case THANG:
                return "Lịch tháng " + ngayDangChon.getMonthValue() + " / " + ngayDangChon.getYear();
            default:
                return "";
        }
    }

    private String dinhDangGio(LocalTime gio) {
        return gio == null ? "" : gio.format(DINH_DANG_GIO);
    }

    private KhoangNgay layKhoangXem() {
        switch (cheDoXem) {
            case NGAY:
                return new KhoangNgay(ngayDangChon, ngayDangChon.plusDays(1));
            case TUAN:
                LocalDate dauTuan = ngayDangChon.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new KhoangNgay(dauTuan, dauTuan.plusWeeks(1));
            case THANG:
                YearMonth thang = YearMonth.from(ngayDangChon);
                return new KhoangNgay(thang.atDay(1), thang.plusMonths(1).atDay(1));
            default:
                throw new IllegalStateException("Chế độ xem lịch không hợp lệ.");
        }
    }

    private void doiThang(int soThang) {
        ngayDangChon = ngayDangChon.plusMonths(soThang);
        taiDuLieu();
    }

    private void diChuyenKhoang(int buoc) {
        switch (cheDoXem) {
            case NGAY:
                ngayDangChon = ngayDangChon.plusDays(buoc);
                break;
            case TUAN:
                ngayDangChon = ngayDangChon.plusWeeks(buoc);
                break;
            case THANG:
                ngayDangChon = ngayDangChon.plusMonths(buoc);
                break;
            default:
                throw new IllegalStateException("Chế độ xem lịch không hợp lệ.");
        }
        taiDuLieu();
    }

    private void capNhatTrangThaiNut() {
        LichLamViec_entity lich = layLichDangChon(false);
        if (btnThem == null || btnSua == null || btnXoa == null || btnTrangThai == null) {
            return;
        }

        LocalDateTime bayGio = LocalDateTime.now();
        btnThem.setEnabled(!ngayDangChon.isBefore(bayGio.toLocalDate()));
        boolean coLich = lich != null;
        boolean coTheSua = coLich && lich.coTheSuaLichTai(bayGio);
        btnSua.setEnabled(coTheSua);
        btnXoa.setEnabled(coTheSua);
        btnTrangThai.setEnabled(coLich && lich.coTheCapNhatTrangThaiTrongNgay(LocalDate.now()));
    }

    private LichLamViec_entity layLichDangChon(boolean thongBaoNeuTrong) {
        int dong = bangLich.getSelectedRow();
        if (dong < 0) {
            if (thongBaoNeuTrong) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch làm việc.");
            }
            return null;
        }
        return danhSachLich.get(bangLich.convertRowIndexToModel(dong));
    }

    private void themLich() {
        if (ngayDangChon.isBefore(LocalDate.now())) {
            hienThiThongBao("Không thể thêm lịch vào ngày đã qua.", true);
            return;
        }
        hienThiFormLich(null);
    }

    private void suaLich() {
        LichLamViec_entity lich = layLichDangChon(true);
        if (lich == null) {
            return;
        }
        if (!lich.coTheSuaLichTai(LocalDateTime.now())) {
            hienThiThongBao("Chỉ được sửa hoặc xóa lịch trước giờ bắt đầu.", true);
            capNhatTrangThaiNut();
            return;
        }
        hienThiFormLich(lich);
    }

    private void xoaLich() {
        LichLamViec_entity lich = layLichDangChon(true);
        if (lich == null) {
            return;
        }
        if (!lich.coTheSuaLichTai(LocalDateTime.now())) {
            hienThiThongBao("Chỉ được xóa lịch trước giờ bắt đầu.", true);
            capNhatTrangThaiNut();
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Xóa ca " + dinhDangGio(lich.getGioBatDau()) + "–"
                        + dinhDangGio(lich.getGioKetThuc()) + " của " + lich.getTenNhanSu() + "?",
                "Xác nhận xóa lịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.xoaLich(lich.getMaLLV());
            if (taiDuLieu()) {
                hienThiThongBao("Đã xóa lịch làm việc.", false);
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            hienThiThongBao("Không thể xóa lịch: " + e.getMessage(), true);
        }
    }

    private void capNhatTrangThai() {
        LichLamViec_entity lich = layLichDangChon(true);
        if (lich == null) {
            return;
        }
        if (!lich.coTheCapNhatTrangThaiTrongNgay(LocalDate.now())) {
            hienThiThongBao("Chỉ có thể cập nhật trạng thái trong ngày diễn ra lịch.", true);
            return;
        }

        JComboBox<String> cbTrangThai = new JComboBox<>(TRANG_THAI_DANH_DAU);
        cbTrangThai.setSelectedItem(lich.getTrangThai());
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.add(new JLabel("Chọn trạng thái chấm công cho ca làm:"), BorderLayout.NORTH);
        pnl.add(cbTrangThai, BorderLayout.CENTER);

        int luaChon = JOptionPane.showConfirmDialog(
                this, pnl, "Cập nhật trạng thái", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (luaChon != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            dao.capNhatTrangThai(lich.getMaLLV(), (String) cbTrangThai.getSelectedItem());
            if (taiDuLieu()) {
                hienThiThongBao("Đã cập nhật trạng thái lịch.", false);
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            hienThiThongBao("Không thể cập nhật trạng thái: " + e.getMessage(), true);
        }
    }

    private void hienThiFormLich(LichLamViec_entity lichHienTai) {
        List<NhanSuLichLamViec_entity> nhanSu;
        try {
            nhanSu = dao.getDanhSachNhanSu();
        } catch (SQLException e) {
            hienThiThongBao("Không tải được danh sách nhân sự: " + e.getMessage(), true);
            return;
        }
        if (nhanSu.isEmpty()) {
            hienThiThongBao("Chưa có nhân viên hoặc bác sĩ để xếp lịch.", true);
            return;
        }

        Window cuaSoCha = SwingUtilities.getWindowAncestor(this);
        Frame frameCha = cuaSoCha instanceof Frame ? (Frame) cuaSoCha : null;
        boolean themMoi = lichHienTai == null;
        JDialog dialog = new JDialog(frameCha, themMoi ? "Thêm ca làm việc" : "Sửa ca làm việc", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(new EmptyBorder(18, 22, 6, 22));
        pnlForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<NhanSuLichLamViec_entity> cbNhanSu =
                new JComboBox<>(nhanSu.toArray(new NhanSuLichLamViec_entity[0]));
        cbNhanSu.setRenderer(new RendererNhanSu());
        if (lichHienTai != null) {
            for (NhanSuLichLamViec_entity nhanSuItem : nhanSu) {
                if (nhanSuItem.getMaNhanSu().equals(lichHienTai.getMaNhanSu())
                        && nhanSuItem.getLoaiNhanSu() == lichHienTai.getLoaiNhanSu()) {
                    cbNhanSu.setSelectedItem(nhanSuItem);
                    break;
                }
            }
        }

        LocalDate ngayBanDau = lichHienTai != null ? lichHienTai.getNgay() : ngayDangChon;
        Date ngayBatDauNhoNhat = doiSangDate(LocalDate.now());
        SpinnerDateModel dateModel = new SpinnerDateModel(
                doiSangDate(ngayBanDau), ngayBatDauNhoNhat, null, Calendar.DAY_OF_MONTH
        );
        JSpinner spNgay = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spNgay, "dd/MM/yyyy");
        spNgay.setEditor(dateEditor);
        spNgay.setPreferredSize(new Dimension(180, 34));

        JTextField txtGioBatDau = taoONhapGio(
                lichHienTai != null ? dinhDangGio(lichHienTai.getGioBatDau()) : "08:00"
        );
        JTextField txtGioKetThuc = taoONhapGio(
                lichHienTai != null ? dinhDangGio(lichHienTai.getGioKetThuc()) : "17:00"
        );

        themTruong(pnlForm, gbc, 0, "Nhân sự:", cbNhanSu);
        themTruong(pnlForm, gbc, 1, "Ngày làm:", spNgay);
        themTruong(pnlForm, gbc, 2, "Giờ bắt đầu:", txtGioBatDau);
        themTruong(pnlForm, gbc, 3, "Giờ kết thúc:", txtGioKetThuc);

        JLabel lblGhiChu = new JLabel("<html>Lịch chỉ sửa/xóa được trước giờ bắt đầu. "
                + "Trạng thái chấm công cập nhật riêng trong ngày làm.</html>");
        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(11));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        pnlForm.add(lblGhiChu, gbc);

        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dialog.dispose());
        JButton btnLuu = new JButton(themMoi ? "Tạo lịch" : "Lưu thay đổi");
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFocusPainted(false);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        pnlNut.setBackground(Color.WHITE);
        pnlNut.setBorder(new EmptyBorder(0, 16, 8, 16));
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);

        btnLuu.addActionListener(e -> {
            NhanSuLichLamViec_entity nhanSuChon =
                    (NhanSuLichLamViec_entity) cbNhanSu.getSelectedItem();
            if (nhanSuChon == null) {
                hienThiLoiForm(dialog, "Vui lòng chọn nhân viên hoặc bác sĩ.");
                return;
            }

            try {
                LocalDate ngay = doiSangLocalDate((Date) spNgay.getValue());
                LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText().trim(), DINH_DANG_GIO);
                LocalTime gioKetThuc = LocalTime.parse(txtGioKetThuc.getText().trim(), DINH_DANG_GIO);
                if (!gioKetThuc.isAfter(gioBatDau)) {
                    hienThiLoiForm(dialog, "Giờ kết thúc phải sau giờ bắt đầu.");
                    return;
                }

                String maLich = themMoi ? dao.phatSinhMaLLV() : lichHienTai.getMaLLV();
                LichLamViec_entity lich = new LichLamViec_entity(
                        maLich,
                        nhanSuChon.getMaNhanSu(),
                        nhanSuChon.getTenNhanSu(),
                        nhanSuChon.getVaiTro(),
                        ngay,
                        gioBatDau,
                        gioKetThuc,
                        themMoi ? LichLamViec_entity.TRANG_THAI_CHUA_LAM : lichHienTai.getTrangThai(),
                        nhanSuChon.getLoaiNhanSu()
                );

                if (themMoi) {
                    if (!dao.themLich(lich)) {
                        throw new IllegalStateException("Không thể tạo lịch làm việc.");
                    }
                } else {
                    if (!dao.capNhatLich(lich)) {
                        throw new IllegalStateException("Không thể cập nhật lịch làm việc.");
                    }
                }

                dialog.dispose();
                ngayDangChon = ngay;
                if (taiDuLieu()) {
                    hienThiThongBao(themMoi ? "Đã tạo ca làm việc." : "Đã lưu thay đổi lịch.", false);
                }
            } catch (DateTimeParseException ex) {
                hienThiLoiForm(dialog, "Giờ phải đúng định dạng 24 giờ, ví dụ 08:30 hoặc 14:00.");
            } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
                hienThiLoiForm(dialog, ex.getMessage());
            }
        });

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlNut, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(450, 320));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTextField taoONhapGio(String giaTri) {
        JTextField txt = new JTextField(giaTri, 12);
        txt.setToolTipText("Nhập theo định dạng 24 giờ, ví dụ 08:30");
        return txt;
    }

    private void themTruong(JPanel pnl, GridBagConstraints gbc, int hang, String nhan, JComponent truong) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = hang;
        gbc.weightx = 0;
        pnl.add(new JLabel(nhan), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        pnl.add(truong, gbc);
    }

    private void hienThiLoiForm(Component cha, String loi) {
        JOptionPane.showMessageDialog(
                cha,
                loi == null || loi.trim().isEmpty() ? "Không thể lưu lịch làm việc." : loi,
                "Không thể lưu lịch",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private Date doiSangDate(LocalDate ngay) {
        return Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate doiSangLocalDate(Date ngay) {
        return ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void lapLichChotNgay() {
        if (!isDisplayable()) {
            return;
        }
        LocalDateTime bayGio = LocalDateTime.now();
        LocalDateTime mocNgayMai = bayGio.toLocalDate().plusDays(1).atStartOfDay();
        long millis = Math.max(1000L, java.time.Duration.between(bayGio, mocNgayMai).toMillis());
        timerChotNgay.setInitialDelay((int) Math.min(millis, Integer.MAX_VALUE));
        timerChotNgay.restart();
    }

    private void xuLyChotNgay() {
        if (!isDisplayable()) {
            return;
        }
        try {
            dao.capNhatTrangThaiTuDongQuaNgay();
            taiDuLieu();
        } catch (SQLException e) {
            hienThiThongBao("Không thể tự động chốt trạng thái lịch: " + e.getMessage(), true);
        } finally {
            lapLichChotNgay();
        }
    }

    private void hienThiThongBao(String thongBao, boolean loi) {
        if (lblThongBao == null) {
            return;
        }
        lblThongBao.setText(thongBao == null ? " " : thongBao);
        lblThongBao.setForeground(loi
                ? RangBuocGiaoDien.MAU_CANH_BAO_DO
                : RangBuocGiaoDien.MAU_CHU_DAO);
    }

    private JButton taoNutDieuHuong(String chu) {
        JButton btn = new JButton(chu);
        btn.setPreferredSize(new Dimension(34, 30));
        btn.setFont(RangBuocGiaoDien.fontDam(12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        return btn;
    }

    private static class KhoangNgay {
        private final LocalDate tuNgay;
        private final LocalDate denNgayExclusive;

        private KhoangNgay(LocalDate tuNgay, LocalDate denNgayExclusive) {
            this.tuNgay = tuNgay;
            this.denNgayExclusive = denNgayExclusive;
        }
    }

    private static class RendererNhanSu extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof NhanSuLichLamViec_entity) {
                setText(value.toString());
            }
            return this;
        }
    }

    private static class RendererTrangThai extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            if (!isSelected) {
                String trangThai = value == null ? "" : value.toString();
                if (LichLamViec_entity.TRANG_THAI_HOAN_THANH.equals(trangThai)) {
                    lbl.setForeground(new Color(21, 128, 61));
                } else if (LichLamViec_entity.TRANG_THAI_VANG_MAT.equals(trangThai)
                        || LichLamViec_entity.TRANG_THAI_DA_HUY.equals(trangThai)) {
                    lbl.setForeground(new Color(185, 28, 28));
                } else if (LichLamViec_entity.TRANG_THAI_DANG_LAM.equals(trangThai)
                        || LichLamViec_entity.TRANG_THAI_DI_TRE.equals(trangThai)
                        || LichLamViec_entity.TRANG_THAI_VE_SOM.equals(trangThai)) {
                    lbl.setForeground(new Color(180, 83, 9));
                } else {
                    lbl.setForeground(new Color(71, 85, 105));
                }
            }
            return lbl;
        }
    }
}
