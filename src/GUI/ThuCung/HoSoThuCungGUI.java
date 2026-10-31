package GUI.ThuCung;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;
import GUI.GiaoDienChinhGUI;
import dao.KhachHang_DAO;
import dao.ThuCung_DAO;
import entity.KhachHang;
import entity.ThuCung_entity;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Class HoSoThuCungGUI: Giao diện "Danh sách thú cưng" (Tab Thú cưng ➔ Hồ sơ thú cưng).
 * Quản lý hồ sơ thú cưng, sử dụng DAO để đọc và ghi dữ liệu.
 */
public class HoSoThuCungGUI extends JPanel {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    // =========================================================================
    // KHAI BÁO TOÀN BỘ BIẾN
    // =========================================================================

    // --- Hằng số kích thước ---
    private static final int KHOANG_CACH_LE = 20;
    private static final int KHOANG_CACH_THE = 16;
    private static final int BAN_KINH_THE = 12;
    private static final int CHIEU_CAO_THE_THONG_KE = 80;
    private static final int CHIEU_CAO_O_NHAP = 38;
    private static final int CHIEU_CAO_THANH_PHAN_TRANG = 56;

    private final String[] tieuDeCot = { "Mã thú cưng", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh",
            "Chủ nuôi", "SĐT chủ", "Đặc điểm nhận diện" };
    private final String[] dsLoaiLoc = { "Loài: Tất cả", "Chó", "Mèo", "Thỏ", "Hamster" };
    private final String[] dsSoDongTrang = { "8", "16", "32" };
    private final ThuCung_DAO thuCungDAO = new ThuCung_DAO();
    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();
    private ArrayList<ThuCung_entity> danhSachThuCung = new ArrayList<>();

    // --- Phần tiêu đề trang ---
    private JPanel panelTieuDe;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBoTron nutThemThuCung;

    // --- Phần thẻ thống kê ---
    private JPanel panelThongKe;
    private JLabel nhanSoTongThuCung;
    private JLabel nhanSoCho;
    private JLabel nhanSoMeo;
    private JLabel nhanSoLoaiKhac;

    // --- Phần thẻ chứa bảng ---
    private PanelBoTron panelTheBang;
    private JPanel panelCongCu;
    private OTimKiemGoiY txtTimKiem;
    private JComboBox<String> cboLoai;
    private JButton nutXemHoSo;
    private JButton nutChinhSua;
    private JButton nutXoa;
    private NutBoTron nutXuatDanhSach;
    private TableMau bangThuCung;
    private JScrollPane thanhCuonBang;

    // --- Phần phân trang ---
    private JPanel panelPhanTrang;
    private JLabel nhanThongTinTrang;
    private JLabel nhanSoDongTrang;
    private JComboBox<String> cboSoDongTrang;
    private NutBoTron nutTrangTruoc;
    private NutBoTron nutTrang1;
    private NutBoTron nutTrang2;
    private NutBoTron nutTrang3;
    private NutBoTron nutTrangRutGon;
    private NutBoTron nutTrangCuoi;
    private NutBoTron nutTrangSau;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public HoSoThuCungGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        loadDataToTable();
    }

    // =========================================================================
    // KHỞI TẠO BỐ CỤC CHÍNH
    // =========================================================================
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE, KHOANG_CACH_LE));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        khoiTaoTieuDe();
        khoiTaoThongKe();
        khoiTaoTheBang();

        add(panelTieuDe, BorderLayout.NORTH);

        JPanel panelThan = new JPanel(new BorderLayout(0, KHOANG_CACH_THE));
        panelThan.setOpaque(false);
        panelThan.add(panelThongKe, BorderLayout.NORTH);
        panelThan.add(panelTheBang, BorderLayout.CENTER);
        add(panelThan, BorderLayout.CENTER);
    }

    // =========================================================================
    // PHẦN 1: TIÊU ĐỀ TRANG
    // =========================================================================
    private void khoiTaoTieuDe() {
        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setOpaque(false);
        panelTieuDe.setBorder(new EmptyBorder(0, 0, KHOANG_CACH_THE, 0));

        nhanDuongDan = new JLabel("Thú cưng / Danh sách thú cưng");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Danh sách thú cưng");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Quản lý hồ sơ thú cưng, thông tin chủ nuôi và tình trạng sức khỏe.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel panelChu = new JPanel();
        panelChu.setOpaque(false);
        panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        panelChu.add(nhanDuongDan);
        panelChu.add(Box.createVerticalStrut(4));
        panelChu.add(nhanTieuDe);
        panelChu.add(Box.createVerticalStrut(2));
        panelChu.add(nhanMoTa);

        nutThemThuCung = new NutBoTron("+  Thêm thú cưng", RangBuocGiaoDien.MAU_CHU_DAO,
                RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT, null, Color.WHITE, 8);
        nutThemThuCung.setPreferredSize(new Dimension(152, 40));

        JPanel panelNut = new JPanel(new GridBagLayout());
        panelNut.setOpaque(false);
        panelNut.add(nutThemThuCung);

        panelTieuDe.add(panelChu, BorderLayout.WEST);
        panelTieuDe.add(panelNut, BorderLayout.EAST);
    }

    // =========================================================================
    // PHẦN 2: 4 THẺ THỐNG KÊ
    // =========================================================================
    private void khoiTaoThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH_THE, 0));
        panelThongKe.setOpaque(false);
        panelThongKe.setPreferredSize(new Dimension(0, CHIEU_CAO_THE_THONG_KE));

        nhanSoTongThuCung = new JLabel("128");
        nhanSoCho = new JLabel("72");
        nhanSoMeo = new JLabel("48");
        nhanSoLoaiKhac = new JLabel("8");

        panelThongKe.add(taoTheThongKe("TC", "Tổng số thú cưng", nhanSoTongThuCung, "Hồ sơ đã đăng ký"));
        panelThongKe.add(taoTheThongKe("C", "Chó", nhanSoCho, "56,3% tổng hồ sơ"));
        panelThongKe.add(taoTheThongKe("M", "Mèo", nhanSoMeo, "37,5% tổng hồ sơ"));
        panelThongKe.add(taoTheThongKe("K", "Loài khác", nhanSoLoaiKhac, "Thỏ và thú cưng khác"));
    }

    private PanelBoTron taoTheThongKe(String bieuTuong, String tieuDe, JLabel nhanSo, String moTa) {
        PanelBoTron the = new PanelBoTron(new BorderLayout(14, 0), BAN_KINH_THE,
                RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.setBorder(new EmptyBorder(0, 18, 0, 12));

        PanelBoTron khungBieuTuong = new PanelBoTron(new GridBagLayout(), 10,
                RangBuocGiaoDien.MAU_CHU_DAO_NHAT, RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        khungBieuTuong.setPreferredSize(new Dimension(40, 40));
        JLabel nhanBieuTuong = new JLabel(bieuTuong);
        nhanBieuTuong.setFont(RangBuocGiaoDien.fontDam(13));
        nhanBieuTuong.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        khungBieuTuong.add(nhanBieuTuong);

        JPanel boBieuTuong = new JPanel(new GridBagLayout());
        boBieuTuong.setOpaque(false);
        boBieuTuong.add(khungBieuTuong);

        JLabel nhanTieuDeThe = new JLabel(tieuDe);
        nhanTieuDeThe.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanTieuDeThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanSo.setFont(RangBuocGiaoDien.fontDam(26));
        nhanSo.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel nhanMoTaThe = new JLabel(moTa);
        nhanMoTaThe.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTaThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel dongSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dongSo.setOpaque(false);
        dongSo.add(nhanSo);
        dongSo.add(nhanMoTaThe);

        JPanel panelChu = new JPanel();
        panelChu.setOpaque(false);
        panelChu.setLayout(new BoxLayout(panelChu, BoxLayout.Y_AXIS));
        nhanTieuDeThe.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelChu.add(Box.createVerticalGlue());
        panelChu.add(nhanTieuDeThe);
        panelChu.add(dongSo);
        panelChu.add(Box.createVerticalGlue());

        the.add(boBieuTuong, BorderLayout.WEST);
        the.add(panelChu, BorderLayout.CENTER);
        return the;
    }

    // =========================================================================
    // PHẦN 3: THẺ CHỨA THANH CÔNG CỤ + BẢNG + PHÂN TRANG
    // =========================================================================
    private void khoiTaoTheBang() {
        panelTheBang = new PanelBoTron(new BorderLayout(), BAN_KINH_THE,
                RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelTheBang.setBorder(new EmptyBorder(1, 1, 1, 1));

        khoiTaoThanhCongCu();
        khoiTaoBang();
        khoiTaoPhanTrang();

        panelTheBang.add(panelCongCu, BorderLayout.NORTH);
        panelTheBang.add(thanhCuonBang, BorderLayout.CENTER);
        panelTheBang.add(panelPhanTrang, BorderLayout.SOUTH);
    }

    private void khoiTaoThanhCongCu() {
        panelCongCu = new JPanel();
        panelCongCu.setOpaque(false);
        panelCongCu.setLayout(new BoxLayout(panelCongCu, BoxLayout.X_AXIS));
        panelCongCu.setBorder(new EmptyBorder(16, 20, 16, 20));

        txtTimKiem = new OTimKiemGoiY("Tìm theo mã thú cưng, tên thú cưng, chủ nuôi...");
        txtTimKiem.setPreferredSize(new Dimension(400, CHIEU_CAO_O_NHAP));
        txtTimKiem.setMaximumSize(new Dimension(400, CHIEU_CAO_O_NHAP));

        cboLoai = new JComboBox<>(dsLoaiLoc);
        cboLoai.setFont(RangBuocGiaoDien.fontThuong(13));
        cboLoai.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboLoai.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        cboLoai.setPreferredSize(new Dimension(210, CHIEU_CAO_O_NHAP));
        cboLoai.setMaximumSize(new Dimension(210, CHIEU_CAO_O_NHAP));

        nutXemHoSo = taoNutLienKet("Xem chi tiết", RangBuocGiaoDien.MAU_CHU_DAO);
        nutChinhSua = taoNutLienKet("Chỉnh sửa", RangBuocGiaoDien.MAU_CHU_DAO);
        nutXoa = taoNutLienKet("Xóa", RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);

        nutXemHoSo.setEnabled(false);
        nutChinhSua.setEnabled(false);
        nutXoa.setEnabled(false);

        nutXuatDanhSach = new NutBoTron("Xuất danh sách", RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DONG_RE_CHUOT, RangBuocGiaoDien.MAU_DUONG_VIEN,
                RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        nutXuatDanhSach.setPreferredSize(new Dimension(154, CHIEU_CAO_O_NHAP));
        nutXuatDanhSach.setMaximumSize(new Dimension(154, CHIEU_CAO_O_NHAP));

        panelCongCu.add(txtTimKiem);
        panelCongCu.add(Box.createHorizontalStrut(12));
        panelCongCu.add(cboLoai);
        panelCongCu.add(Box.createHorizontalGlue());
        panelCongCu.add(nutXemHoSo);
        panelCongCu.add(Box.createHorizontalStrut(14));
        panelCongCu.add(nutChinhSua);
        panelCongCu.add(Box.createHorizontalStrut(14));
        panelCongCu.add(nutXoa);
        panelCongCu.add(Box.createHorizontalStrut(40));
        panelCongCu.add(nutXuatDanhSach);
    }

    private void khoiTaoBang() {
        bangThuCung = new TableMau(tieuDeCot, new Object[0][tieuDeCot.length]);
        bangThuCung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] doRongCot = { 100, 130, 90, 90, 110, 170, 120, 250 };
        for (int i = 0; i < doRongCot.length; i++) {
            bangThuCung.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
        bangThuCung.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatTrangThaiNutHanhDong();
            }
        });

        thanhCuonBang = new JScrollPane(bangThuCung);
        thanhCuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        thanhCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        thanhCuonBang.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void khoiTaoPhanTrang() {
        panelPhanTrang = new JPanel(new BorderLayout());
        panelPhanTrang.setOpaque(false);
        panelPhanTrang.setPreferredSize(new Dimension(0, CHIEU_CAO_THANH_PHAN_TRANG));
        panelPhanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(0, 20, 0, 20)));

        nhanThongTinTrang = new JLabel("Hiển thị 1–8 trong 128 thú cưng");
        nhanThongTinTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanSoDongTrang = new JLabel("Số dòng / trang");
        nhanSoDongTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanSoDongTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        cboSoDongTrang = new JComboBox<>(dsSoDongTrang);
        cboSoDongTrang.setFont(RangBuocGiaoDien.fontThuong(13));
        cboSoDongTrang.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        cboSoDongTrang.setPreferredSize(new Dimension(64, 34));

        nutTrangTruoc = taoNutTrang("<", false);
        nutTrang1 = taoNutTrang("1", true);
        nutTrang2 = taoNutTrang("2", false);
        nutTrang3 = taoNutTrang("3", false);
        nutTrangRutGon = taoNutTrang("...", false);
        nutTrangCuoi = taoNutTrang("16", false);
        nutTrangSau = taoNutTrang(">", false);

        JPanel panelBenPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 11));
        panelBenPhai.setOpaque(false);
        panelBenPhai.add(nhanSoDongTrang);
        panelBenPhai.add(cboSoDongTrang);
        panelBenPhai.add(Box.createHorizontalStrut(10));
        panelBenPhai.add(nutTrangTruoc);
        panelBenPhai.add(nutTrang1);
        panelBenPhai.add(nutTrang2);
        panelBenPhai.add(nutTrang3);
        panelBenPhai.add(nutTrangRutGon);
        panelBenPhai.add(nutTrangCuoi);
        panelBenPhai.add(nutTrangSau);

        panelPhanTrang.add(nhanThongTinTrang, BorderLayout.WEST);
        panelPhanTrang.add(panelBenPhai, BorderLayout.EAST);
    }

    // =========================================================================
    // CÀI ĐẶT SỰ KIỆN CHO CỤM NÚT THAO TÁC
    // =========================================================================
    private void caiDatSuKien() {
        nutThemThuCung.addActionListener(e -> moFormThemThuCung());
        nutXemHoSo.addActionListener(e -> xemDongDaChon());
        nutChinhSua.addActionListener(e -> chinhSuaDongDaChon());
        nutXoa.addActionListener(e -> xoaDongDaChon());
        nutXuatDanhSach.addActionListener(e -> xuatDanhSach());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                locDuLieu();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                locDuLieu();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                locDuLieu();
            }
        });
        cboLoai.addActionListener(e -> locDuLieu());
        bangThuCung.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bangThuCung.getSelectedRowCount() == 1) {
                    xemDongDaChon();
                }
            }
        });
        capNhatTrangThaiNutHanhDong();
    }

    /**
     * Nạp danh sách thú cưng từ DAO và áp dụng bộ lọc đang chọn.
     */
    public void loadDataToTable() {
        try {
            ArrayList<ThuCung_entity> ketQua = thuCungDAO.getAllThuCung();
            danhSachThuCung = ketQua != null ? ketQua : new ArrayList<>();
            locDuLieu();
            capNhatThongKe();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách thú cưng: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDuLieu() {
        if (bangThuCung == null) {
            return;
        }
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        String loaiChon = String.valueOf(cboLoai.getSelectedItem());
        DefaultTableModel model = (DefaultTableModel) bangThuCung.getModel();
        model.setRowCount(0);

        for (ThuCung_entity tc : danhSachThuCung) {
            if (tc == null) {
                continue;
            }
            String ma = giaTri(tc.getMaTC());
            String ten = giaTri(tc.getTenTC());
            String loai = giaTri(tc.getLoai());
            String chuHo = giaTri(tc.getChuHo());
            String sdt = tc.getMaKH() == null ? "" : giaTri(tc.getMaKH().getSdt());
            String ngaySinh = tc.getNgaySinh() == null ? "" : DINH_DANG_NGAY.format(tc.getNgaySinh());

            boolean khopTuKhoa = tuKhoa.isEmpty()
                    || ma.toLowerCase(Locale.ROOT).contains(tuKhoa)
                    || loai.toLowerCase(Locale.ROOT).contains(tuKhoa)
                    || ten.toLowerCase(Locale.ROOT).contains(tuKhoa)
                    || chuHo.toLowerCase(Locale.ROOT).contains(tuKhoa)
                    || sdt.toLowerCase(Locale.ROOT).contains(tuKhoa);
            boolean khopLoai = loaiChon == null || loaiChon.startsWith("Loài:")
                    || loai.equalsIgnoreCase(loaiChon);
            if (khopTuKhoa && khopLoai) {
                model.addRow(new Object[] { ma, ten, loai, giaTri(tc.getGioiTinh()), ngaySinh,
                        chuHo, sdt, giaTri(tc.getDacDiemNhanDien()) });
            }
        }
        bangThuCung.clearSelection();
        capNhatTrangThaiNutHanhDong();
        int soDong = model.getRowCount();
        nhanThongTinTrang.setText("Hiển thị " + soDong + " trong " + soDong + " thú cưng");
    }

    private void capNhatThongKe() {
        int cho = 0;
        int meo = 0;
        for (ThuCung_entity tc : danhSachThuCung) {
            if (tc != null && "Chó".equalsIgnoreCase(tc.getLoai())) {
                cho++;
            } else if (tc != null && "Mèo".equalsIgnoreCase(tc.getLoai())) {
                meo++;
            }
        }
        nhanSoTongThuCung.setText(String.valueOf(danhSachThuCung.size()));
        nhanSoCho.setText(String.valueOf(cho));
        nhanSoMeo.setText(String.valueOf(meo));
        nhanSoLoaiKhac.setText(String.valueOf(danhSachThuCung.size() - cho - meo));
    }

    private String giaTri(String value) {
        return value == null ? "" : value;
    }

    private void capNhatTrangThaiNutHanhDong() {
        boolean coMotDongDuocChon = bangThuCung.getSelectedRowCount() == 1;
        nutXemHoSo.setEnabled(coMotDongDuocChon);
        nutChinhSua.setEnabled(coMotDongDuocChon);
        nutXoa.setEnabled(coMotDongDuocChon);
    }

    private int layDongDaChon() {
        if (bangThuCung.getSelectedRowCount() != 1) {
            return -1;
        }
        int dong = bangThuCung.getSelectedRow();
        return dong < 0 ? -1 : bangThuCung.convertRowIndexToModel(dong);
    }

    private void xemDongDaChon() {
        String maThuCung = layMaThuCungDaChon();
        if (maThuCung == null) {
            return;
        }
        try {
            ThuCung_entity tc = thuCungDAO.getThuCungTheoMa(maThuCung);
            if (tc == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thú cưng " + maThuCung + ".",
                        "Không tìm thấy dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            hienThiFormThuCung(tc, true, false);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết thú cưng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chinhSuaDongDaChon() {
        String maThuCung = layMaThuCungDaChon();
        if (maThuCung == null) {
            return;
        }
        try {
            ThuCung_entity tc = thuCungDAO.getThuCungTheoMa(maThuCung);
            if (tc == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thú cưng " + maThuCung + ".",
                        "Không tìm thấy dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            hienThiFormThuCung(tc, false, true);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin thú cưng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaDongDaChon() {
        String maThuCung = layMaThuCungDaChon();
        if (maThuCung == null) {
            return;
        }
        int dong = layDongDaChon();
        String tenThuCung = String.valueOf(bangThuCung.getModel().getValueAt(dong, 1));
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hồ sơ thú cưng \"" + tenThuCung + "\" (" + maThuCung + ") không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon == JOptionPane.YES_OPTION) {
            try {
                if (thuCungDAO.deleteThuCung(maThuCung)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thú cưng thành công.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Không xóa được thú cưng. Vui lòng kiểm tra dữ liệu liên quan.",
                            "Xóa thất bại", JOptionPane.WARNING_MESSAGE);
                }
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thú cưng: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String layMaThuCungDaChon() {
        int dong = layDongDaChon();
        return dong < 0 ? null : String.valueOf(bangThuCung.getModel().getValueAt(dong, 0));
    }

    private void moFormThemThuCung() {
        hienThiFormThuCung(null, false, true);
    }

    private void hienThiFormThuCung(ThuCung_entity thuCung, boolean chiDoc, boolean luuDuoc) {
        try {
            ArrayList<KhachHang> danhSachKhachHang = khachHangDAO.getAlltbKhachHang();
            if (danhSachKhachHang == null || danhSachKhachHang.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cần có khách hàng trước khi tạo hồ sơ thú cưng.",
                        "Chưa có khách hàng", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JTextField txtMa = new JTextField(thuCung == null ? phatSinhMaThuCung() : giaTri(thuCung.getMaTC()));
            txtMa.setEditable(false);
            JTextField txtTen = new JTextField(thuCung == null ? "" : giaTri(thuCung.getTenTC()));
            JComboBox<String> cboLoaiNhap = new JComboBox<>(new String[] { "Chó", "Mèo", "Thỏ", "Hamster", "Khác" });
            cboLoaiNhap.setEditable(true);
            if (thuCung != null) {
                cboLoaiNhap.setSelectedItem(giaTri(thuCung.getLoai()));
            }
            JComboBox<String> cboGioiTinh = new JComboBox<>(new String[] { "", "Đực", "Cái" });
            if (thuCung != null) {
                cboGioiTinh.setSelectedItem(giaTri(thuCung.getGioiTinh()));
            }
            JTextField txtNgaySinh = new JTextField(thuCung == null || thuCung.getNgaySinh() == null
                    ? "" : DINH_DANG_NGAY.format(thuCung.getNgaySinh()));
            JComboBox<KhachHang> cboKhachHang = new JComboBox<>(danhSachKhachHang.toArray(new KhachHang[0]));
            cboKhachHang.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof KhachHang) {
                        KhachHang kh = (KhachHang) value;
                        setText(giaTri(kh.getHoTen()) + " (" + giaTri(kh.getMaKH()) + ")");
                    }
                    return this;
                }
            });
            if (thuCung != null && thuCung.getMaKH() != null) {
                for (int i = 0; i < cboKhachHang.getItemCount(); i++) {
                    if (thuCung.getMaKH().getMaKH().equals(cboKhachHang.getItemAt(i).getMaKH())) {
                        cboKhachHang.setSelectedIndex(i);
                        break;
                    }
                }
            }
            JTextField txtChuHo = new JTextField(thuCung == null ? "" : giaTri(thuCung.getChuHo()));
            JTextField txtDacDiem = new JTextField(thuCung == null ? "" : giaTri(thuCung.getDacDiemNhanDien()));

            cboKhachHang.addActionListener(e -> {
                KhachHang kh = (KhachHang) cboKhachHang.getSelectedItem();
                if (kh != null && (thuCung == null || txtChuHo.getText().trim().isEmpty())) {
                    txtChuHo.setText(giaTri(kh.getHoTen()));
                }
            });
            if (thuCung == null) {
                KhachHang khMacDinh = (KhachHang) cboKhachHang.getSelectedItem();
                if (khMacDinh != null) {
                    txtChuHo.setText(giaTri(khMacDinh.getHoTen()));
                }
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Mã thú cưng:")); form.add(txtMa);
            form.add(new JLabel("Tên thú cưng:")); form.add(txtTen);
            form.add(new JLabel("Loài:")); form.add(cboLoaiNhap);
            form.add(new JLabel("Giới tính:")); form.add(cboGioiTinh);
            form.add(new JLabel("Ngày sinh (dd/MM/yyyy):")); form.add(txtNgaySinh);
            form.add(new JLabel("Khách hàng:")); form.add(cboKhachHang);
            form.add(new JLabel("Chủ nuôi:")); form.add(txtChuHo);
            form.add(new JLabel("Đặc điểm nhận diện:")); form.add(txtDacDiem);

            for (Component component : new Component[] { txtTen, cboLoaiNhap, cboGioiTinh,
                    txtNgaySinh, cboKhachHang, txtChuHo, txtDacDiem }) {
                component.setEnabled(!chiDoc);
            }
            String tieuDe = chiDoc ? "Chi tiết hồ sơ thú cưng"
                    : thuCung == null ? "Thêm thú cưng" : "Cập nhật thú cưng";
            int luaChon = JOptionPane.showConfirmDialog(this, form, tieuDe,
                    luuDuoc ? JOptionPane.OK_CANCEL_OPTION : JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (chiDoc || luaChon != JOptionPane.OK_OPTION) {
                return;
            }

            String ten = txtTen.getText().trim();
            String loai = String.valueOf(cboLoaiNhap.getSelectedItem()).trim();
            String chuHo = txtChuHo.getText().trim();
            KhachHang kh = (KhachHang) cboKhachHang.getSelectedItem();
            if (ten.isEmpty() || loai.isEmpty() || chuHo.isEmpty() || kh == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên, loài, chủ nuôi và chọn khách hàng.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate ngaySinh = null;
            if (!txtNgaySinh.getText().trim().isEmpty()) {
                try {
                    ngaySinh = LocalDate.parse(txtNgaySinh.getText().trim(), DINH_DANG_NGAY);
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ. Hãy nhập theo dd/MM/yyyy.",
                            "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            ThuCung_entity duLieu = new ThuCung_entity(txtMa.getText(), kh, ten, loai,
                    String.valueOf(cboGioiTinh.getSelectedItem()), ngaySinh, chuHo, txtDacDiem.getText().trim());
            boolean thanhCong = thuCung == null
                    ? thuCungDAO.createThuCung(duLieu)
                    : thuCungDAO.updateThuCung(duLieu);
            if (thanhCong) {
                JOptionPane.showMessageDialog(this,
                        thuCung == null ? "Đã thêm thú cưng thành công." : "Đã cập nhật thú cưng thành công.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu thông tin thú cưng.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý hồ sơ thú cưng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String phatSinhMaThuCung() {
        int maLonNhat = 0;
        Pattern pattern = Pattern.compile("^TC(\\d+)$", Pattern.CASE_INSENSITIVE);
        for (ThuCung_entity tc : danhSachThuCung) {
            Matcher matcher = pattern.matcher(giaTri(tc.getMaTC()));
            if (matcher.matches()) {
                try {
                    maLonNhat = Math.max(maLonNhat, Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Mã thú cưng không hợp lệ: " + tc.getMaTC(), e);
                }
            }
        }
        return String.format(Locale.ROOT, "TC%03d", maLonNhat + 1);
    }

    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất danh sách thú cưng");
        chooser.setSelectedFile(new java.io.File("danh-sach-thu-cung.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "Tệp đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận ghi đè",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            for (int cot = 0; cot < bangThuCung.getColumnCount(); cot++) {
                if (cot > 0) {
                    writer.write(',');
                }
                writer.write(baoVeCsv(bangThuCung.getColumnName(cot)));
            }
            writer.newLine();
            DefaultTableModel model = (DefaultTableModel) bangThuCung.getModel();
            for (int dong = 0; dong < model.getRowCount(); dong++) {
                for (int cot = 0; cot < model.getColumnCount(); cot++) {
                    if (cot > 0) {
                        writer.write(',');
                    }
                    writer.write(baoVeCsv(String.valueOf(model.getValueAt(dong, cot))));
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Đã xuất danh sách ra:\n" + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể xuất danh sách: " + e.getMessage(),
                    "Lỗi xuất tệp", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String baoVeCsv(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TẠO THÀNH PHẦN
    // =========================================================================
    private JButton taoNutLienKet(String chu, Color mauChu) {
        JButton nut = new JButton(chu);
        nut.setFont(RangBuocGiaoDien.fontThuong(12));
        nut.setForeground(mauChu);
        nut.setContentAreaFilled(false);
        nut.setBorderPainted(false);
        nut.setFocusPainted(false);
        nut.setMargin(new Insets(0, 0, 0, 0));
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return nut;
    }

    private NutBoTron taoNutTrang(String chu, boolean dangChon) {
        NutBoTron nut;
        if (dangChon) {
            nut = new NutBoTron(chu, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT,
                    null, Color.WHITE, 8);
        } else {
            nut = new NutBoTron(chu, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DONG_RE_CHUOT,
                    RangBuocGiaoDien.MAU_DUONG_VIEN, RangBuocGiaoDien.MAU_CHU_CHINH, 8);
        }
        nut.setFont(RangBuocGiaoDien.fontThuong(12));
        nut.setPreferredSize(new Dimension(chu.length() > 2 ? 40 : 34, 34));
        return nut;
    }

    // =========================================================================
    // GETTERS (để nhóm làm chức năng truy cập khi cần)
    // =========================================================================
    public TableMau getBangThuCung() {
        return bangThuCung;
    }

    public OTimKiemGoiY getTxtTimKiem() {
        return txtTimKiem;
    }

    public JComboBox<String> getCboLoai() {
        return cboLoai;
    }

    public JButton getNutThemThuCung() {
        return nutThemThuCung;
    }

    // =========================================================================
    // CÁC LỚP PHỤ TRỢ GIAO DIỆN
    // =========================================================================

    /** Panel nền bo góc, có đường viền. */
    public static class PanelBoTron extends JPanel {
        private final int banKinh;
        private final Color mauNen;
        private final Color mauVien;

        public PanelBoTron(LayoutManager boCuc, int banKinh, Color mauNen, Color mauVien) {
            super(boCuc);
            this.banKinh = banKinh;
            this.mauNen = mauNen;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(mauNen);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút bo góc, có hiệu ứng rê chuột. */
    public static class NutBoTron extends JButton {
        private final Color mauNen;
        private final Color mauReChuot;
        private final Color mauVien;
        private final int banKinh;
        private boolean dangReChuot = false;

        public NutBoTron(String chu, Color mauNen, Color mauReChuot, Color mauVien, Color mauChu, int banKinh) {
            super(chu);
            this.mauNen = mauNen;
            this.mauReChuot = mauReChuot;
            this.mauVien = mauVien;
            this.banKinh = banKinh;
            setForeground(mauChu);
            setFont(RangBuocGiaoDien.fontDam(13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    dangReChuot = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dangReChuot = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(dangReChuot ? mauReChuot : mauNen);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, banKinh, banKinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Ô nhập có chữ gợi ý (placeholder). */
    public static class OTimKiemGoiY extends JTextField {
        private final String goiY;

        public OTimKiemGoiY(String goiY) {
            this.goiY = goiY;
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 12, 0, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                g2.setFont(getFont());
                Insets le = getInsets();
                int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                g2.drawString(goiY, le.left, y);
                g2.dispose();
            }
        }
    }

    // =========================================================================
    // MAIN CHẠY THỬ ĐỘC LẬP
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử độc lập - HoSoThuCungGUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new HoSoThuCungGUI());
            GiaoDienChinhGUI.caiDatKichThuocCuaSo(frame, false);
            frame.setVisible(true);
        });
    }
}
