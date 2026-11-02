package GUI.LichHen;

import GUI.Chung.RangBuocGiaoDien;
import dao.DichVu_DAO;
import dao.HoSoSucKhoe_DAO;
import dao.KhachHang_DAO;
import dao.LichHen_DAO;
import dao.NhanVien_DAO;
import dao.ThuCung_DAO;
import entity.DichVu_entity;
import entity.HoSoSucKhoe_entity;
import entity.KhachHang;
import entity.LichHen_entity;
import entity.NhanVien_entity;
import entity.ThuCung_entity;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.JTextComponent;

/**
 * Class DatLichMoiGUI: Giao diện "Đặt lịch mới" (tạo lịch khám) của hệ thống SALALA Pet Shop.
 * Bố cục gồm:
 * 1. Phần đầu trang: đường dẫn, tiêu đề, mô tả và nút "Xem lịch".
 * 2. Cột trái (3 thẻ nhập liệu): Khách hàng - Thú cưng - Lịch khám.
 * 3. Cột phải: thẻ "Tóm tắt lịch hẹn" + nút "Đặt lịch khám" / "Nhập lại".
 */
public class DatLichMoiGUI extends JPanel {

    // =========================================================================
    // 1. HẰNG SỐ KÍCH THƯỚC & DỮ LIỆU MẪU CHO COMBO
    // =========================================================================
    private static final int BAN_KINH_THE = 12;
    private static final int BAN_KINH_O = 8;
    private static final int CHIEU_CAO_O = 38;
    private static final int LE_TRANG = 20;
    private static final int KHOANG_CACH = 16;
    private static final int KHOANG_CACH_TRUONG = 14;
    private static final int RONG_COT_PHAI = 330;
    private static final int SO_COT_LUOI = 3;

    private static final String[] DANH_SACH_THU_CUNG = { "-- Chọn thú cưng --" };
    private static final String[] DANH_SACH_NHAN_VIEN = { "-- Chọn nhân viên --" };
    private static final String[] DANH_SACH_NOI_DUNG_KHAM = {
            "-- Chọn nội dung khám --", "Khám tổng quát", "Khám định kỳ", "Tiêm phòng", "Tái khám",
            "Kiểm tra da và lông", "Kiểm tra răng miệng", "Tư vấn dinh dưỡng"
    };
    private static final String[] DANH_SACH_GIO_KHAM = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    };

    private static final String GIA_TRI_TRONG = "—";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final LichHen_DAO lichHenDAO = new LichHen_DAO();
    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();
    private final ThuCung_DAO thuCungDAO = new ThuCung_DAO();
    private final NhanVien_DAO nhanVienDAO = new NhanVien_DAO();
    private final DichVu_DAO dichVuDAO = new DichVu_DAO();
    private final HoSoSucKhoe_DAO hoSoSucKhoeDAO = new HoSoSucKhoe_DAO();
    private final Map<String, ThuCung_entity> thuCungTheoNhan = new HashMap<>();
    private final Map<String, NhanVien_entity> nhanVienTheoNhan = new HashMap<>();
    private final Map<String, DichVu_entity> dichVuTheoNhan = new HashMap<>();
    private final Map<String, String> canNangGanNhatTheoThuCung = new HashMap<>();
    private KhachHang khachHangDuocChon;
    private String gioKhamDuocChon;
    private Runnable xuLySauKhiDatLich;

    // =========================================================================
    // 2. CÁC THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    // --- Khung cuộn & bố cục chung ---
    private JScrollPane khungCuonTrang;
    private PanelCuon panelNoiDung;
    private JPanel panelCotTrai;
    private JPanel panelCotPhai;

    // --- Phần đầu trang ---
    private JPanel panelDauTrang;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBo nutXemLich;

    // --- Thẻ 1: Thông tin khách hàng ---
    private TheBoGoc theKhachHang;
    private TruongVanBan txtSoDienThoai;
    private NutBo nutTimKhach;
    private TruongVanBan txtTenKhachHang;
    private TruongVanBan txtEmailKhachHang;
    private TruongVanBan txtHangThanhVien;
    private TruongVanBan txtDiaChiKhachHang;

    // --- Thẻ 2: Thông tin thú cưng ---
    private TheBoGoc theThuCung;
    private ComboHienDai cboThuCung;
    private TruongVanBan txtLoaiThuCung;
    private TruongVanBan txtGiongThuCung;
    private TruongVanBan txtGioiTinhThuCung;
    private TruongVanBan txtNgaySinhThuCung;
    private TruongVanBan txtCanNangThuCung;

    // --- Thẻ 3: Thông tin lịch khám ---
    private TheBoGoc theLichKham;
    private TruongVanBan txtNgayKham;
    private ComboHienDai cboBacSi;
    private ComboHienDai cboNoiDungKham;
    private JPanel panelGioKham;
    private NutBo[] nutGioKham;
    private VungVanBan vungGhiChu;

    // --- Cột phải: Tóm tắt lịch hẹn ---
    private TheBoGoc theTomTat;
    private JLabel nhanTieuDeTomTat;
    private JLabel nhanMaLichHen;
    private NhanTrangThai nhanTrangThai;
    private JLabel nhanTomTatChuNuoi;
    private JLabel nhanTomTatThuCung;
    private JLabel nhanTomTatBacSi;
    private JLabel nhanTomTatThoiGian;
    private JLabel nhanTomTatNoiDung;
    private TheBoGoc hopLuuY;
    private JLabel nhanLuuY;
    private NutBo nutDatLich;
    private NutBo nutNhapLai;

    // =========================================================================
    // 3. KHỞI TẠO
    // =========================================================================
    public DatLichMoiGUI() {
        khoiTaoGiaoDien();
        taiDuLieuCombobox();
        caiDatSuKien();
        txtNgayKham.setText(DINH_DANG_NGAY.format(LocalDate.now()));
        nhanMaLichHen.setText(lichHenDAO.phatSinhMaLichHenMoi());
        capNhatTomTat();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);

        // Nội dung nằm trong khung cuộn để không bị cắt khi màn hình nhỏ
        panelNoiDung = new PanelCuon(new BorderLayout(0, KHOANG_CACH));
        panelNoiDung.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        panelNoiDung.setBorder(BorderFactory.createEmptyBorder(16, LE_TRANG, 24, LE_TRANG));

        panelNoiDung.add(taoPhanDauTrang(), BorderLayout.NORTH);

        JPanel thanCuaTrang = new JPanel(new BorderLayout(KHOANG_CACH, 0));
        thanCuaTrang.setOpaque(false);
        thanCuaTrang.add(taoCotTrai(), BorderLayout.CENTER);
        thanCuaTrang.add(taoCotPhai(), BorderLayout.EAST);
        panelNoiDung.add(thanCuaTrang, BorderLayout.CENTER);

        khungCuonTrang = new JScrollPane(panelNoiDung);
        khungCuonTrang.setBorder(null);
        khungCuonTrang.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        khungCuonTrang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        khungCuonTrang.getVerticalScrollBar().setUnitIncrement(16);
        add(khungCuonTrang, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // PHẦN ĐẦU TRANG
    // -------------------------------------------------------------------------
    private JPanel taoPhanDauTrang() {
        panelDauTrang = new JPanel(new BorderLayout(0, 8));
        panelDauTrang.setOpaque(false);

        nhanDuongDan = new JLabel("Lịch hẹn / Đặt lịch mới");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Đặt lịch mới");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Tạo lịch khám cho thú cưng · Tìm khách hàng, chọn thú cưng, bác sĩ và khung giờ phù hợp.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel khoiTieuDe = new JPanel(new BorderLayout(0, 2));
        khoiTieuDe.setOpaque(false);
        khoiTieuDe.add(nhanTieuDe, BorderLayout.NORTH);
        khoiTieuDe.add(nhanMoTa, BorderLayout.CENTER);

        nutXemLich = new NutBo("Xem lịch", NutBo.KIEU_VIEN);
        nutXemLich.setFont(RangBuocGiaoDien.fontDam(13));
        nutXemLich.datBieuTuong(new BieuTuong(BieuTuong.LICH, 18, RangBuocGiaoDien.MAU_CHU_DAO));
        nutXemLich.setPreferredSize(new Dimension(125, 40));

        JPanel khoiNut = new JPanel(new GridBagLayout());
        khoiNut.setOpaque(false);
        khoiNut.add(nutXemLich);

        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);
        hangTieuDe.add(khoiTieuDe, BorderLayout.WEST);
        hangTieuDe.add(khoiNut, BorderLayout.EAST);

        panelDauTrang.add(nhanDuongDan, BorderLayout.NORTH);
        panelDauTrang.add(hangTieuDe, BorderLayout.CENTER);
        return panelDauTrang;
    }

    // -------------------------------------------------------------------------
    // CỘT TRÁI: 3 THẺ NHẬP LIỆU
    // -------------------------------------------------------------------------
    private JPanel taoCotTrai() {
        panelCotTrai = new JPanel(new GridBagLayout());
        panelCotTrai.setOpaque(false);

        themVaoCot(panelCotTrai, taoTheKhachHang(), 0, 0);
        themVaoCot(panelCotTrai, taoTheThuCung(), 1, KHOANG_CACH);
        themVaoCot(panelCotTrai, taoTheLichKham(), 2, KHOANG_CACH);
        themDemCuoiCot(panelCotTrai, 3);
        return panelCotTrai;
    }

    /** Thẻ 1: Thông tin khách hàng (chủ nuôi). */
    private TheBoGoc taoTheKhachHang() {
        JPanel luoi = new JPanel(new GridBagLayout());
        luoi.setOpaque(false);

        // Số điện thoại + nút Tìm
        txtSoDienThoai = new TruongVanBan("Nhập số điện thoại");
        TheBoGoc oSoDienThoai = taoOVanBan(txtSoDienThoai, false);
        nutTimKhach = new NutBo("Tìm", NutBo.KIEU_CHINH);
        nutTimKhach.setFont(RangBuocGiaoDien.fontDam(13));
        nutTimKhach.setPreferredSize(new Dimension(64, CHIEU_CAO_O));
        JPanel hangSoDienThoai = new JPanel(new BorderLayout(8, 0));
        hangSoDienThoai.setOpaque(false);
        hangSoDienThoai.add(oSoDienThoai, BorderLayout.CENTER);
        hangSoDienThoai.add(nutTimKhach, BorderLayout.EAST);

        txtTenKhachHang = new TruongVanBan("Tự động hiển thị");
        txtEmailKhachHang = new TruongVanBan("Tự động hiển thị");
        txtHangThanhVien = new TruongVanBan("Tự động hiển thị");
        txtDiaChiKhachHang = new TruongVanBan("Tự động hiển thị");

        themVaoLuoi(luoi, taoNhomTruong("Số điện thoại", true, hangSoDienThoai), 0, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Họ và tên chủ nuôi", false, taoOVanBan(txtTenKhachHang, true)), 1, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Email", false, taoOVanBan(txtEmailKhachHang, true)), 2, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Hạng thành viên", false, taoOVanBan(txtHangThanhVien, true)), 0, 1, 1);
        themVaoLuoi(luoi, taoNhomTruong("Địa chỉ", false, taoOVanBan(txtDiaChiKhachHang, true)), 1, 1, 2);

        theKhachHang = taoTheNhapLieu("Thông tin khách hàng", "Tìm khách hàng theo số điện thoại để tự động điền thông tin chủ nuôi.", luoi);
        return theKhachHang;
    }

    /** Thẻ 2: Thông tin thú cưng. */
    private TheBoGoc taoTheThuCung() {
        JPanel luoi = new JPanel(new GridBagLayout());
        luoi.setOpaque(false);

        cboThuCung = new ComboHienDai(DANH_SACH_THU_CUNG, 10, CHIEU_CAO_O);
        txtLoaiThuCung = new TruongVanBan("Tự động hiển thị");
        txtGiongThuCung = new TruongVanBan("Chưa lưu trong hồ sơ thú cưng");
        txtGioiTinhThuCung = new TruongVanBan("Tự động hiển thị");
        txtNgaySinhThuCung = new TruongVanBan("Tự động hiển thị");
        txtCanNangThuCung = new TruongVanBan("Tự động hiển thị");

        themVaoLuoi(luoi, taoNhomTruong("Thú cưng", true, cboThuCung), 0, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Loại thú cưng", false, taoOVanBan(txtLoaiThuCung, true)), 1, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Đặc điểm nhận diện", false, taoOVanBan(txtGiongThuCung, true)), 2, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Giới tính", false, taoOVanBan(txtGioiTinhThuCung, true)), 0, 1, 1);
        themVaoLuoi(luoi, taoNhomTruong("Ngày sinh", false, taoOVanBan(txtNgaySinhThuCung, true)), 1, 1, 1);
        themVaoLuoi(luoi, taoNhomTruong("Cân nặng (kg)", false, taoOVanBan(txtCanNangThuCung, true)), 2, 1, 1);

        theThuCung = taoTheNhapLieu("Thông tin thú cưng", "Chọn thú cưng thuộc khách hàng để khám.", luoi);
        return theThuCung;
    }

    /** Thẻ 3: Thông tin lịch khám. */
    private TheBoGoc taoTheLichKham() {
        JPanel luoi = new JPanel(new GridBagLayout());
        luoi.setOpaque(false);

        // Ngày khám (ô nhập + biểu tượng lịch)
        txtNgayKham = new TruongVanBan("dd/MM/yyyy");
        TheBoGoc oNgayKham = taoOVanBan(txtNgayKham, false);
        oNgayKham.add(new JLabel(new BieuTuong(BieuTuong.LICH, 16, RangBuocGiaoDien.MAU_CHU_PHU)), BorderLayout.EAST);

        cboBacSi = new ComboHienDai(DANH_SACH_NHAN_VIEN, 10, CHIEU_CAO_O);
        cboNoiDungKham = new ComboHienDai(DANH_SACH_NOI_DUNG_KHAM, 10, CHIEU_CAO_O);

        // Lưới chọn giờ khám
        panelGioKham = new JPanel(new GridLayout(2, 8, 8, 8));
        panelGioKham.setOpaque(false);
        nutGioKham = new NutBo[DANH_SACH_GIO_KHAM.length];
        ActionListener chonGio = e -> {
            for (NutBo nut : nutGioKham) {
                nut.datChon(nut == e.getSource());
            }
            gioKhamDuocChon = ((NutBo) e.getSource()).getText();
            capNhatTomTat();
        };
        for (int i = 0; i < DANH_SACH_GIO_KHAM.length; i++) {
            nutGioKham[i] = new NutBo(DANH_SACH_GIO_KHAM[i], NutBo.KIEU_VIEN);
            nutGioKham[i].setFont(RangBuocGiaoDien.fontVua(13));
            nutGioKham[i].setPreferredSize(new Dimension(64, 34));
            nutGioKham[i].addActionListener(chonGio);
            panelGioKham.add(nutGioKham[i]);
        }

        // Ghi chú
        vungGhiChu = new VungVanBan("Nhập triệu chứng, yêu cầu đặc biệt hoặc ghi chú cho bác sĩ...");
        TheBoGoc oGhiChu = taoOVungVanBan(vungGhiChu, 86);

        themVaoLuoi(luoi, taoNhomTruong("Ngày khám", true, oNgayKham), 0, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Nhân viên phụ trách", true, cboBacSi), 1, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Nội dung khám", true, cboNoiDungKham), 2, 0, 1);
        themVaoLuoi(luoi, taoNhomTruong("Giờ khám", true, panelGioKham), 0, 1, 3);
        themVaoLuoi(luoi, taoNhomTruong("Ghi chú", false, oGhiChu), 0, 2, 3);

        theLichKham = taoTheNhapLieu("Thông tin lịch khám", "Chọn ngày, nhân viên phụ trách, nội dung và khung giờ khám.", luoi);
        return theLichKham;
    }

    // -------------------------------------------------------------------------
    // CỘT PHẢI: TÓM TẮT LỊCH HẸN
    // -------------------------------------------------------------------------
    private JPanel taoCotPhai() {
        panelCotPhai = new JPanel(new BorderLayout());
        panelCotPhai.setOpaque(false);
        panelCotPhai.setPreferredSize(new Dimension(RONG_COT_PHAI, 10));

        theTomTat = new TheBoGoc(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        theTomTat.setLayout(new GridBagLayout());
        theTomTat.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));

        nhanTieuDeTomTat = new JLabel("Tóm tắt lịch hẹn");
        nhanTieuDeTomTat.setFont(RangBuocGiaoDien.fontDam(15));
        nhanTieuDeTomTat.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMaLichHen = taoNhanGiaTri("Tự động tạo");
        nhanMaLichHen.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        nhanTrangThai = new NhanTrangThai("Chờ khám");
        nhanTomTatChuNuoi = taoNhanGiaTri(GIA_TRI_TRONG);
        nhanTomTatThuCung = taoNhanGiaTri(GIA_TRI_TRONG);
        nhanTomTatBacSi = taoNhanGiaTri(GIA_TRI_TRONG);
        nhanTomTatThoiGian = taoNhanGiaTri(GIA_TRI_TRONG);
        nhanTomTatNoiDung = taoNhanGiaTri(GIA_TRI_TRONG);

        nhanLuuY = new JLabel("<html>Lịch hẹn mới sẽ ở trạng thái <b>Chờ khám</b><br>"
                + "cho tới khi thú cưng được tiếp nhận.</html>");
        nhanLuuY.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanLuuY.setForeground(RangBuocGiaoDien.MAU_THONG_TIN_CHU);
        hopLuuY = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_THONG_TIN_NEN, RangBuocGiaoDien.MAU_THONG_TIN_NEN);
        hopLuuY.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        hopLuuY.add(nhanLuuY, BorderLayout.CENTER);

        nutDatLich = new NutBo("Đặt lịch khám", NutBo.KIEU_CHINH);
        nutDatLich.setFont(RangBuocGiaoDien.fontDam(14));
        nutDatLich.setPreferredSize(new Dimension(10, 44));
        nutNhapLai = new NutBo("Nhập lại", NutBo.KIEU_VIEN);
        nutNhapLai.setFont(RangBuocGiaoDien.fontDam(13));
        nutNhapLai.setPreferredSize(new Dimension(10, 42));

        JPanel khoiNut = new JPanel(new GridLayout(2, 1, 0, 10));
        khoiNut.setOpaque(false);
        khoiNut.add(nutDatLich);
        khoiNut.add(nutNhapLai);

        themVaoCot(theTomTat, nhanTieuDeTomTat, 0, 0);
        themVaoCot(theTomTat, taoDongTomTat("Mã lịch hẹn", nhanMaLichHen), 1, 12);
        themVaoCot(theTomTat, taoDongTomTat("Trạng thái", nhanTrangThai), 2, 0);
        themVaoCot(theTomTat, taoDongTomTat("Chủ nuôi", nhanTomTatChuNuoi), 3, 0);
        themVaoCot(theTomTat, taoDongTomTat("Thú cưng", nhanTomTatThuCung), 4, 0);
        themVaoCot(theTomTat, taoDongTomTat("Nhân viên", nhanTomTatBacSi), 5, 0);
        themVaoCot(theTomTat, taoDongTomTat("Thời gian", nhanTomTatThoiGian), 6, 0);
        themVaoCot(theTomTat, taoDongTomTat("Nội dung khám", nhanTomTatNoiDung), 7, 0);
        themVaoCot(theTomTat, hopLuuY, 8, 16);
        themVaoCot(theTomTat, khoiNut, 9, 16);
        themDemCuoiCot(theTomTat, 10);

        panelCotPhai.add(theTomTat, BorderLayout.NORTH);
        return panelCotPhai;
    }

    // =========================================================================
    // 4. HÀM DỰNG THÀNH PHẦN DÙNG CHUNG
    // =========================================================================
    /** Thẻ trắng bo góc có tiêu đề + mô tả + phần nội dung. */
    private TheBoGoc taoTheNhapLieu(String tieuDe, String moTa, JComponent noiDung) {
        TheBoGoc the = new TheBoGoc(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        the.setLayout(new BorderLayout(0, 16));
        the.setBorder(BorderFactory.createEmptyBorder(18, 20, 6, 20));

        JLabel nhanTieuDeThe = new JLabel(tieuDe);
        nhanTieuDeThe.setFont(RangBuocGiaoDien.fontDam(15));
        nhanTieuDeThe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel nhanMoTaThe = new JLabel(moTa);
        nhanMoTaThe.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTaThe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel khoiDau = new JPanel(new BorderLayout(0, 2));
        khoiDau.setOpaque(false);
        khoiDau.add(nhanTieuDeThe, BorderLayout.NORTH);
        khoiDau.add(nhanMoTaThe, BorderLayout.CENTER);

        the.add(khoiDau, BorderLayout.NORTH);
        the.add(noiDung, BorderLayout.CENTER);
        return the;
    }

    /** Nhóm "nhãn + ô nhập"; dấu * đỏ cho trường bắt buộc. */
    private JPanel taoNhomTruong(String nhan, boolean batBuoc, JComponent oNhap) {
        JLabel nhanTruong = new JLabel(batBuoc
                ? "<html>" + nhan + " <font color='#B91C1C'>*</font></html>"
                : nhan);
        nhanTruong.setFont(RangBuocGiaoDien.fontDam(12));
        nhanTruong.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JPanel nhom = new JPanel(new BorderLayout(0, 6));
        nhom.setOpaque(false);
        nhom.add(nhanTruong, BorderLayout.NORTH);
        nhom.add(oNhap, BorderLayout.CENTER);
        return nhom;
    }

    /** Ô nhập một dòng bo góc. chiDoc = true: nền xám nhạt, dùng cho trường tự động điền. */
    private TheBoGoc taoOVanBan(TruongVanBan truong, boolean chiDoc) {
        TheBoGoc o = new TheBoGoc(BAN_KINH_O,
                chiDoc ? RangBuocGiaoDien.MAU_NEN_TRANG : RangBuocGiaoDien.MAU_NEN_THE,
                RangBuocGiaoDien.MAU_DUONG_VIEN);
        o.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        o.setPreferredSize(new Dimension(10, CHIEU_CAO_O));
        o.add(truong, BorderLayout.CENTER);
        if (chiDoc) {
            truong.setEditable(false);
            truong.setFocusable(false);
        } else {
            o.theoDoiTieuDiem(truong);
        }
        return o;
    }

    /** Ô nhập nhiều dòng bo góc (ghi chú). */
    private TheBoGoc taoOVungVanBan(VungVanBan vung, int chieuCao) {
        TheBoGoc o = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        o.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
        o.setPreferredSize(new Dimension(10, chieuCao));

        JScrollPane cuon = new JScrollPane(vung);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        o.add(cuon, BorderLayout.CENTER);
        o.theoDoiTieuDiem(vung);
        return o;
    }

    private JLabel taoNhanGiaTri(String chu) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(RangBuocGiaoDien.fontDam(13));
        nhan.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        nhan.setHorizontalAlignment(SwingConstants.RIGHT);
        return nhan;
    }

    /** Một dòng trong thẻ tóm tắt: nhãn xám bên trái, giá trị bên phải, có đường kẻ dưới. */
    private JPanel taoDongTomTat(String nhan, JComponent giaTri) {
        JLabel nhanTrai = new JLabel(nhan);
        nhanTrai.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanTrai.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel dong = new JPanel(new BorderLayout(12, 0));
        dong.setOpaque(false);
        dong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, RangBuocGiaoDien.MAU_LUOI_BANG),
                BorderFactory.createEmptyBorder(11, 0, 11, 0)));
        dong.add(nhanTrai, BorderLayout.WEST);
        dong.add(giaTri, BorderLayout.CENTER);
        return dong;
    }

    /** Thêm một hàng vào cột dọc (GridBagLayout 1 cột), khoangTren là khoảng cách tới hàng trước. */
    private void themVaoCot(Container cot, Component thanhPhan, int hang, int khoangTren) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = hang;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTH;
        gc.insets = new Insets(khoangTren, 0, 0, 0);
        cot.add(thanhPhan, gc);
    }

    /** Hàng đệm cuối cột để đẩy các thẻ lên trên. */
    private void themDemCuoiCot(Container cot, int hang) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = hang;
        gc.weighty = 1;
        JPanel dem = new JPanel();
        dem.setOpaque(false);
        cot.add(dem, gc);
    }

    /** Thêm nhóm trường vào lưới 3 cột đều nhau. */
    private void themVaoLuoi(JPanel luoi, JComponent nhom, int cot, int hang, int soCotChiem) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = cot;
        gc.gridy = hang;
        gc.gridwidth = soCotChiem;
        gc.weightx = soCotChiem;
        // Ép chiều rộng ưu tiên nhỏ để các cột chia đều theo trọng số (không phụ thuộc độ dài nội dung)
        nhom.setPreferredSize(new Dimension(10, nhom.getPreferredSize().height));
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        boolean laCotCuoi = (cot + soCotChiem) >= SO_COT_LUOI;
        gc.insets = new Insets(0, 0, KHOANG_CACH_TRUONG, laCotCuoi ? 0 : KHOANG_CACH);
        luoi.add(nhom, gc);
    }

    // =========================================================================
    // 5. NGHIỆP VỤ ĐẶT LỊCH
    // =========================================================================
    private void taiDuLieuCombobox() {
        try {
            cboBacSi.removeAllItems();
            cboBacSi.addItem("-- Chọn nhân viên --");
            nhanVienTheoNhan.clear();
            for (NhanVien_entity nhanVien : nhanVienDAO.getAll()) {
                String nhan = nhanVien.getHoTen() + " (" + nhanVien.getMaNhanVien() + ")";
                cboBacSi.addItem(nhan);
                nhanVienTheoNhan.put(nhan, nhanVien);
            }
        } catch (Exception ex) {
            hienThiLoi("Không thể tải danh sách nhân viên phụ trách.", ex);
        }

        cboNoiDungKham.removeAllItems();
        cboNoiDungKham.addItem(DANH_SACH_NOI_DUNG_KHAM[0]);
        dichVuTheoNhan.clear();
        try {
            for (DichVu_entity dichVu : dichVuDAO.getAllDichVu()) {
                if (dichVu == null || dichVu.getTenDV() == null
                        || laDichVuNgungHoatDong(dichVu.getTrangThai())) {
                    continue;
                }
                String nhan = dichVu.getTenDV() + " (" + dichVu.getMaDV() + ")";
                cboNoiDungKham.addItem(nhan);
                dichVuTheoNhan.put(nhan, dichVu);
            }
        } catch (Exception ex) {
            hienThiLoi("Không thể tải danh sách dịch vụ khám.", ex);
        }
        if (dichVuTheoNhan.isEmpty()) {
            for (int i = 1; i < DANH_SACH_NOI_DUNG_KHAM.length; i++) {
                cboNoiDungKham.addItem(DANH_SACH_NOI_DUNG_KHAM[i]);
            }
        }
    }

    private boolean laDichVuNgungHoatDong(String trangThai) {
        if (trangThai == null) {
            return false;
        }
        String trangThaiChuanHoa = trangThai.trim().toLowerCase();
        return trangThaiChuanHoa.contains("ngưng")
                || trangThaiChuanHoa.contains("ngừng")
                || trangThaiChuanHoa.contains("không hoạt động");
    }

    private void caiDatSuKien() {
        nutTimKhach.addActionListener(e -> timKhachHangTheoSoDienThoai());
        nutDatLich.addActionListener(e -> datLichKham());
        nutNhapLai.addActionListener(e -> nhapLaiForm());
        cboThuCung.addActionListener(e -> capNhatThongTinThuCung());
        cboBacSi.addActionListener(e -> capNhatTomTat());
        cboNoiDungKham.addActionListener(e -> capNhatTomTat());
        txtNgayKham.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                capNhatTomTat();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                capNhatTomTat();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                capNhatTomTat();
            }
        });
        nutXemLich.addActionListener(e -> moManHinhXemLich());
    }

    private void timKhachHangTheoSoDienThoai() {
        String soDienThoai = txtSoDienThoai.getText().trim();
        String soDienThoaiSo = soDienThoai.replaceAll("[^0-9]", "");
        if (soDienThoaiSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại khách hàng.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtSoDienThoai.requestFocusInWindow();
            return;
        }

        try {
            List<KhachHang> khachHangTimThay = khachHangDAO.timKiemTongHop(soDienThoaiSo);
            khachHangDuocChon = null;
            for (KhachHang khachHang : khachHangTimThay) {
                if (khachHang.getSdt() != null
                        && khachHang.getSdt().replaceAll("[^0-9]", "").equals(soDienThoaiSo)) {
                    khachHangDuocChon = khachHang;
                    break;
                }
            }
            if (khachHangDuocChon == null) {
                lamTrongThongTinKhachHang();
                lamTrongThongTinThuCung();
                xoaDanhSachThuCung();
                capNhatTomTat();
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng có số điện thoại này.",
                        "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            txtTenKhachHang.setText(giaTriHoacMacDinh(khachHangDuocChon.getHoTen()));
            txtEmailKhachHang.setText(giaTriHoacMacDinh(khachHangDuocChon.getEmail()));
            txtHangThanhVien.setText(giaTriHoacMacDinh(khachHangDuocChon.getHangThanhVien()));
            txtDiaChiKhachHang.setText(giaTriHoacMacDinh(khachHangDuocChon.getDiaChi()));
            taiThuCungCuaKhachHang();
            capNhatTomTat();
        } catch (Exception ex) {
            hienThiLoi("Không thể tìm khách hàng theo số điện thoại.", ex);
        }
    }

    private void taiThuCungCuaKhachHang() {
        thuCungTheoNhan.clear();
        cboThuCung.removeAllItems();
        cboThuCung.addItem(DANH_SACH_THU_CUNG[0]);
        canNangGanNhatTheoThuCung.clear();
        ngayKhamGanNhat.clear();

        try {
            for (ThuCung_entity thuCung : thuCungDAO.getThuCungTheoMaKH(khachHangDuocChon.getMaKH())) {
                String nhan = thuCung.getTenTC() + " (" + thuCung.getMaTC() + ")";
                cboThuCung.addItem(nhan);
                thuCungTheoNhan.put(nhan, thuCung);
            }
            for (HoSoSucKhoe_entity hoSo : hoSoSucKhoeDAO.getAllHoSoSucKhoe()) {
                if (hoSo.getCanNang() == null || hoSo.getNgayKham() == null) {
                    continue;
                }
                String maThuCung = hoSo.getMaTC();
                LocalDateTime ngayGanNhat = ngayKhamGanNhat.get(maThuCung);
                if (ngayGanNhat == null || hoSo.getNgayKham().isAfter(ngayGanNhat)) {
                    ngayKhamGanNhat.put(maThuCung, hoSo.getNgayKham());
                    canNangGanNhatTheoThuCung.put(maThuCung,
                            hoSo.getCanNang().stripTrailingZeros().toPlainString());
                }
            }
            if (thuCungTheoNhan.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khách hàng này chưa có hồ sơ thú cưng.",
                        "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            hienThiLoi("Không thể tải thú cưng hoặc cân nặng gần nhất.", ex);
        }
    }

    private final Map<String, LocalDateTime> ngayKhamGanNhat = new HashMap<>();

    private void capNhatThongTinThuCung() {
        String nhan = (String) cboThuCung.getSelectedItem();
        ThuCung_entity thuCung = thuCungTheoNhan.get(nhan);
        if (thuCung == null) {
            lamTrongThongTinThuCung();
            capNhatTomTat();
            return;
        }
        txtLoaiThuCung.setText(giaTriHoacMacDinh(thuCung.getLoai()));
        txtGiongThuCung.setText(giaTriHoacMacDinh(thuCung.getDacDiemNhanDien()));
        txtGioiTinhThuCung.setText(giaTriHoacMacDinh(thuCung.getGioiTinh()));
        txtNgaySinhThuCung.setText(thuCung.getNgaySinh() == null
                ? GIA_TRI_TRONG
                : DINH_DANG_NGAY.format(thuCung.getNgaySinh()));
        txtCanNangThuCung.setText(canNangGanNhatTheoThuCung.getOrDefault(thuCung.getMaTC(), "Chưa có dữ liệu"));
        capNhatTomTat();
    }

    private void datLichKham() {
        if (khachHangDuocChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và xác nhận khách hàng trước khi đặt lịch.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nhanThuCung = (String) cboThuCung.getSelectedItem();
        ThuCung_entity thuCung = thuCungTheoNhan.get(nhanThuCung);
        String nhanVienChon = (String) cboBacSi.getSelectedItem();
        NhanVien_entity nhanVien = nhanVienTheoNhan.get(nhanVienChon);
        String noiDung = (String) cboNoiDungKham.getSelectedItem();
        DichVu_entity dichVu = dichVuTheoNhan.get(noiDung);

        if (thuCung == null || nhanVien == null || noiDung == null
                || noiDung.startsWith("--") || gioKhamDuocChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thú cưng, nhân viên, nội dung khám và giờ khám.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String lyDo = dichVu == null ? noiDung : dichVu.getTenDV();
        if (lyDo.length() > 255) {
            JOptionPane.showMessageDialog(this, "Nội dung khám không được vượt quá 255 ký tự.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate ngay = LocalDate.parse(txtNgayKham.getText().trim(), DINH_DANG_NGAY);
            LocalDateTime ngayGio = LocalDateTime.of(ngay, LocalTime.parse(gioKhamDuocChon));
            if (ngayGio.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Thời gian đặt lịch phải ở hiện tại hoặc trong tương lai.",
                        "Thời gian không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String ghiChu = vungGhiChu.getText().trim();
            if (ghiChu.length() > 500) {
                JOptionPane.showMessageDialog(this, "Ghi chú không được vượt quá 500 ký tự.",
                        "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maLichHen = lichHenDAO.phatSinhMaLichHenMoi();
            LichHen_entity lichHen = new LichHen_entity(maLichHen, thuCung, khachHangDuocChon,
                    nhanVien, dichVu, ngayGio, lyDo, "Chờ khám", ghiChu, "Khám bệnh");
            if (!lichHenDAO.createLichHen(lichHen)) {
                JOptionPane.showMessageDialog(this, "Không thể lưu lịch hẹn. Vui lòng kiểm tra dữ liệu và kết nối cơ sở dữ liệu.",
                        "Lỗi đặt lịch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Đặt lịch khám thành công. Mã lịch: " + maLichHen,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (xuLySauKhiDatLich != null) {
                xuLySauKhiDatLich.run();
            }
            nhapLaiForm();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày khám không đúng định dạng dd/MM/yyyy.",
                    "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtNgayKham.requestFocusInWindow();
        } catch (Exception ex) {
            hienThiLoi("Không thể tạo lịch hẹn.", ex);
        }
    }

    private void nhapLaiForm() {
        khachHangDuocChon = null;
        txtSoDienThoai.setText("");
        lamTrongThongTinKhachHang();
        xoaDanhSachThuCung();
        cboBacSi.setSelectedIndex(0);
        cboNoiDungKham.setSelectedIndex(0);
        txtNgayKham.setText(DINH_DANG_NGAY.format(LocalDate.now()));
        vungGhiChu.setText("");
        gioKhamDuocChon = null;
        for (NutBo nut : nutGioKham) {
            nut.datChon(false);
        }
        nhanMaLichHen.setText(lichHenDAO.phatSinhMaLichHenMoi());
        capNhatTomTat();
    }

    private void lamTrongThongTinKhachHang() {
        txtTenKhachHang.setText("");
        txtEmailKhachHang.setText("");
        txtHangThanhVien.setText("");
        txtDiaChiKhachHang.setText("");
    }

    private void lamTrongThongTinThuCung() {
        txtLoaiThuCung.setText("");
        txtGiongThuCung.setText("");
        txtGioiTinhThuCung.setText("");
        txtNgaySinhThuCung.setText("");
        txtCanNangThuCung.setText("");
    }

    private void xoaDanhSachThuCung() {
        thuCungTheoNhan.clear();
        canNangGanNhatTheoThuCung.clear();
        ngayKhamGanNhat.clear();
        cboThuCung.removeAllItems();
        cboThuCung.addItem(DANH_SACH_THU_CUNG[0]);
    }

    private void capNhatTomTat() {
        nhanTomTatChuNuoi.setText(khachHangDuocChon == null
                ? GIA_TRI_TRONG : giaTriHoacMacDinh(khachHangDuocChon.getHoTen()));
        ThuCung_entity thuCung = thuCungTheoNhan.get(cboThuCung == null
                ? null : (String) cboThuCung.getSelectedItem());
        nhanTomTatThuCung.setText(thuCung == null ? GIA_TRI_TRONG : thuCung.getTenTC());
        NhanVien_entity nhanVien = nhanVienTheoNhan.get(cboBacSi == null
                ? null : (String) cboBacSi.getSelectedItem());
        nhanTomTatBacSi.setText(nhanVien == null ? GIA_TRI_TRONG : nhanVien.getHoTen());
        nhanTomTatNoiDung.setText(cboNoiDungKham == null || cboNoiDungKham.getSelectedIndex() <= 0
                ? GIA_TRI_TRONG : dichVuTheoNhan.containsKey((String) cboNoiDungKham.getSelectedItem())
                        ? dichVuTheoNhan.get((String) cboNoiDungKham.getSelectedItem()).getTenDV()
                        : (String) cboNoiDungKham.getSelectedItem());

        String thoiGian = GIA_TRI_TRONG;
        if (txtNgayKham != null && gioKhamDuocChon != null) {
            try {
                LocalDate ngay = LocalDate.parse(txtNgayKham.getText().trim(), DINH_DANG_NGAY);
                thoiGian = DINH_DANG_NGAY_GIO.format(LocalDateTime.of(ngay, LocalTime.parse(gioKhamDuocChon)));
            } catch (DateTimeParseException ignored) {
                thoiGian = txtNgayKham.getText().trim() + " " + gioKhamDuocChon;
            }
        }
        nhanTomTatThoiGian.setText(thoiGian);
    }

    private String giaTriHoacMacDinh(String giaTri) {
        return giaTri == null || giaTri.trim().isEmpty() ? GIA_TRI_TRONG : giaTri;
    }

    private void moManHinhXemLich() {
        java.awt.Window cuaSo = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (cuaSo instanceof javax.swing.JDialog) {
            cuaSo.dispose();
            return;
        }
        if (cuaSo instanceof javax.swing.JFrame) {
            ((javax.swing.JFrame) cuaSo).setContentPane(new XemLichGUI());
            cuaSo.revalidate();
            cuaSo.repaint();
        }
    }

    private void hienThiLoi(String thongDiep, Exception ex) {
        String chiTiet = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        JOptionPane.showMessageDialog(this, thongDiep + "\n" + chiTiet,
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void setXuLySauKhiDatLich(Runnable xuLySauKhiDatLich) {
        this.xuLySauKhiDatLich = xuLySauKhiDatLich;
    }

    // =========================================================================
    // 6. GETTERS
    // =========================================================================
    public JButton getNutXemLich() {
        return nutXemLich;
    }

    public JButton getNutTimKhach() {
        return nutTimKhach;
    }

    public JButton getNutDatLich() {
        return nutDatLich;
    }

    public JButton getNutNhapLai() {
        return nutNhapLai;
    }

    public JTextField getTxtSoDienThoai() {
        return txtSoDienThoai;
    }

    public JTextField getTxtNgayKham() {
        return txtNgayKham;
    }

    public JComboBox<String> getCboThuCung() {
        return cboThuCung;
    }

    public JComboBox<String> getCboBacSi() {
        return cboBacSi;
    }

    public JComboBox<String> getCboNoiDungKham() {
        return cboNoiDungKham;
    }

    public JTextArea getVungGhiChu() {
        return vungGhiChu;
    }

    // =========================================================================
    // 6. CÁC LỚP CON HỖ TRỢ VẼ GIAO DIỆN
    // =========================================================================

    /** Panel nền của khung cuộn: luôn co giãn theo chiều rộng khung nhìn. */
    private static class PanelCuon extends JPanel implements Scrollable {
        PanelCuon(java.awt.LayoutManager boCuc) {
            super(boCuc);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 32, 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container cha = getParent();
            return (cha instanceof JViewport) && cha.getHeight() > getPreferredSize().height;
        }
    }

    // -------------------------------------------------------------------------
    // Thẻ bo góc (dùng cho thẻ nhập liệu, ô nhập, hộp lưu ý)
    // -------------------------------------------------------------------------
    private static class TheBoGoc extends JPanel {
        private int banKinh;
        private Color mauNen;
        private Color mauVien;

        TheBoGoc(int banKinh, Color mauNen, Color mauVien) {
            super(new BorderLayout());
            this.banKinh = banKinh;
            this.mauNen = mauNen;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        /** Đổi màu viền sang màu chủ đạo khi ô nhập đang được focus. */
        void theoDoiTieuDiem(JTextComponent truong) {
            final Color mauVienGoc = mauVien;
            truong.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    mauVien = RangBuocGiaoDien.MAU_CHU_DAO;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    mauVien = mauVienGoc;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    banKinh * 2, banKinh * 2);
            g2.setColor(mauNen);
            g2.fill(hinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.draw(hinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // -------------------------------------------------------------------------
    // Ô nhập một dòng / nhiều dòng có chữ gợi ý (placeholder)
    // -------------------------------------------------------------------------
    private static class TruongVanBan extends JTextField {
        private String goiY;

        TruongVanBan(String goiY) {
            this.goiY = goiY;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setCaretColor(RangBuocGiaoDien.MAU_CHU_DAO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && goiY != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setFont(getFont());
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(goiY, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }

    private static class VungVanBan extends JTextArea {
        private String goiY;

        VungVanBan(String goiY) {
            this.goiY = goiY;
            setOpaque(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(BorderFactory.createEmptyBorder());
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setCaretColor(RangBuocGiaoDien.MAU_CHU_DAO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && goiY != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setFont(getFont());
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(goiY, getInsets().left, getInsets().top + fm.getAscent());
                g2.dispose();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Huy hiệu trạng thái (thẻ tóm tắt) - cùng kiểu với bảng ở màn Xem lịch
    // -------------------------------------------------------------------------
    private static class NhanTrangThai extends JComponent {
        private String trangThai;

        NhanTrangThai(String trangThai) {
            this.trangThai = trangThai;
            setPreferredSize(new Dimension(110, 24));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);

            Color nen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
            Color chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            Color cham = RangBuocGiaoDien.MAU_TRUNG_TINH_CHAM;
            if ("Chờ khám".equals(trangThai)) {
                nen = RangBuocGiaoDien.MAU_CANH_BAO_NEN;
                chu = RangBuocGiaoDien.MAU_CANH_BAO_CHU;
                cham = RangBuocGiaoDien.MAU_CANH_BAO_CHAM;
            } else if ("Đang khám".equals(trangThai)) {
                nen = RangBuocGiaoDien.MAU_THONG_TIN_NEN;
                chu = RangBuocGiaoDien.MAU_THONG_TIN_CHU;
                cham = RangBuocGiaoDien.MAU_THONG_TIN_CHAM;
            } else if ("Đã khám".equals(trangThai)) {
                nen = RangBuocGiaoDien.MAU_THANH_CONG_NEN;
                chu = RangBuocGiaoDien.MAU_THANH_CONG_CHU;
                cham = RangBuocGiaoDien.MAU_THANH_CONG_CHAM;
            } else if ("Đã hủy".equals(trangThai)) {
                nen = RangBuocGiaoDien.MAU_NGUY_HIEM_NEN;
                chu = RangBuocGiaoDien.MAU_NGUY_HIEM_CHU;
                cham = RangBuocGiaoDien.MAU_NGUY_HIEM_CHAM;
            }

            g2.setFont(RangBuocGiaoDien.fontDam(11));
            FontMetrics fm = g2.getFontMetrics();
            int cao = 22;
            int rong = 10 + 6 + 6 + fm.stringWidth(trangThai) + 10;
            int x = getWidth() - rong; // Căn phải theo cột giá trị của thẻ tóm tắt
            int y = (getHeight() - cao) / 2;

            g2.setColor(nen);
            g2.fill(new RoundRectangle2D.Double(x, y, rong, cao, cao, cao));
            g2.setColor(cham);
            g2.fill(new Ellipse2D.Double(x + 10, y + (cao - 6) / 2.0, 6, 6));
            g2.setColor(chu);
            g2.drawString(trangThai, x + 22, y + (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Nút bấm bo góc: 3 kiểu (chính - nền teal, viền - nền trắng, phẳng - không viền)
    // -------------------------------------------------------------------------
    private static class NutBo extends JButton {
        static final int KIEU_CHINH = 0;
        static final int KIEU_VIEN = 1;
        static final int KIEU_PHANG = 2;

        private int kieu;
        private boolean chon;
        private Icon bieuTuongNut;

        NutBo(String chu, int kieu) {
            super(chu);
            this.kieu = kieu;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(RangBuocGiaoDien.fontVua(13));
        }

        void datChon(boolean chon) {
            this.chon = chon;
            repaint();
        }

        void datBieuTuong(Icon bieuTuongNut) {
            this.bieuTuongNut = bieuTuongNut;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);

            boolean reChuot = getModel().isRollover();
            boolean noi = (kieu == KIEU_CHINH) || chon;
            Color nen;
            Color vien;
            Color chu;
            if (noi) {
                nen = reChuot ? RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT : RangBuocGiaoDien.MAU_CHU_DAO;
                vien = nen;
                chu = Color.WHITE;
            } else if (kieu == KIEU_VIEN) {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : RangBuocGiaoDien.MAU_NEN_THE;
                vien = RangBuocGiaoDien.MAU_DUONG_VIEN;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            } else {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : null;
                vien = null;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            }

            int rong = getWidth();
            int cao = getHeight();
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, rong - 1, cao - 1, 12, 12);
            if (nen != null) {
                g2.setColor(nen);
                g2.fill(hinh);
            }
            if (vien != null) {
                g2.setColor(vien);
                g2.draw(hinh);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth(getText());
            int rongIcon = (bieuTuongNut != null) ? bieuTuongNut.getIconWidth() + 8 : 0;
            int x = (rong - rongChu - rongIcon) / 2;
            if (bieuTuongNut != null) {
                bieuTuongNut.paintIcon(this, g2, x, (cao - bieuTuongNut.getIconHeight()) / 2);
                x += rongIcon;
            }
            g2.setColor(chu);
            g2.drawString(getText(), x, (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Combo box hiện đại: bo góc, mũi tên chevron, danh sách thả xuống đồng bộ màu
    // -------------------------------------------------------------------------
    private static class ComboHienDai extends JComboBox<String> {

        ComboHienDai(String[] mucChon, int rong, int cao) {
            super(mucChon);
            setUI(new GiaoDienCombo());
            setOpaque(false);
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            setBorder(BorderFactory.createEmptyBorder());
            setPreferredSize(new Dimension(rong, cao));
            setMaximumRowCount(8);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel nhan = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    nhan.setFont(RangBuocGiaoDien.fontThuong(13));
                    nhan.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    nhan.setForeground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
                    nhan.setBackground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_NEN_THE);
                    return nhan;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    BAN_KINH_O * 2, BAN_KINH_O * 2);
            g2.setColor(RangBuocGiaoDien.MAU_NEN_THE);
            g2.fill(hinh);
            g2.setColor((isPopupVisible() || hasFocus()) ? RangBuocGiaoDien.MAU_CHU_DAO
                    : RangBuocGiaoDien.MAU_DUONG_VIEN);
            g2.draw(hinh);
            g2.dispose();
            RangBuocGiaoDien.batKhuRangCua(g);
            super.paintComponent(g);
        }
    }

    private static class GiaoDienCombo extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton nut = new JButton(new BieuTuong(BieuTuong.MUI_TEN_XUONG, 14, RangBuocGiaoDien.MAU_CHU_PHU));
            nut.setBorder(BorderFactory.createEmptyBorder());
            nut.setContentAreaFilled(false);
            nut.setFocusPainted(false);
            nut.setOpaque(false);
            return nut;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Không tô nền: phần nền bo góc đã được vẽ trong ComboHienDai
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Object giaTri = comboBox.getSelectedItem();
            if (giaTri == null) {
                return;
            }
            boolean laGoiY = comboBox.getSelectedIndex() == 0 && giaTri.toString().startsWith("--");
            g.setFont(comboBox.getFont());
            g.setColor(laGoiY ? RangBuocGiaoDien.MAU_CHU_GOI_Y : RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(giaTri.toString(), bounds.x + 12,
                    bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
            return popup;
        }
    }

    // -------------------------------------------------------------------------
    // Bộ biểu tượng vẽ bằng Graphics2D (lưới 24 x 24)
    // -------------------------------------------------------------------------
    private static class BieuTuong implements Icon {
        static final int LICH = 0;
        static final int MUI_TEN_XUONG = 1;

        private int loai;
        private int kichThuoc;
        private Color mau;

        BieuTuong(int loai, int kichThuoc, Color mau) {
            this.loai = loai;
            this.kichThuoc = kichThuoc;
            this.mau = mau;
        }

        @Override
        public int getIconWidth() {
            return kichThuoc;
        }

        @Override
        public int getIconHeight() {
            return kichThuoc;
        }

        private void veDuongGap(Graphics2D g2, double... toaDo) {
            Path2D.Double duong = new Path2D.Double();
            duong.moveTo(toaDo[0], toaDo[1]);
            for (int i = 2; i < toaDo.length; i += 2) {
                duong.lineTo(toaDo[i], toaDo[i + 1]);
            }
            g2.draw(duong);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double tyLe = kichThuoc / 24.0;
            g2.scale(tyLe, tyLe);
            g2.setColor(mau);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (loai) {
                case LICH:
                    g2.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 4, 4));
                    veDuongGap(g2, 3.5, 10, 20.5, 10);
                    veDuongGap(g2, 8, 3, 8, 7);
                    veDuongGap(g2, 16, 3, 16, 7);
                    g2.fill(new Ellipse2D.Double(7.2, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(11.1, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(15, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(7.2, 16.6, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(11.1, 16.6, 1.8, 1.8));
                    break;
                case MUI_TEN_XUONG:
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    veDuongGap(g2, 6, 9, 12, 15, 18, 9);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }
}
