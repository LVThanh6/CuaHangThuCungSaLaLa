package Service.ThuCung;

import connectDB.ConnectDB;
import dao.BacSi.BacSi_DAO;
import dao.HoaDon.ChiTietHoaDon_DAO;
import dao.HoaDon.HoaDon_Dao;
import dao.ThuCung.ChiTietDonThuoc_DAO;
import dao.ThuCung.DonThuoc_DAO;
import dao.ThuCung.HoSoSucKhoe_DAO;
import dao.ThuCung.ThuCung_DAO;
import dao.VatTu.VatTu_DAO;
import entity.BacSi.BacSi_entity;
import entity.HoaDon.ChiTietHoaDon_entity;
import entity.HoaDon.HoaDon_entity;
import entity.NhanVien.NhanVien_entity;
import entity.ThuCung.ChiTietDonThuoc_entity;
import entity.ThuCung.DonThuoc_entity;
import entity.ThuCung.HoSoSucKhoe_entity;
import entity.ThuCung.ThuCung_entity;
import entity.ThuCung.Thuoc_entity;
import entity.VatTu.VatTu_entity;

import java.math.BigDecimal;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonThuocService {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final DonThuoc_DAO donThuocDao;
    private final ChiTietDonThuoc_DAO chiTietDao;
    private final HoSoSucKhoe_DAO hoSoDao;
    private final ThuCung_DAO thuCungDao;
    private final BacSi_DAO bacSiDao;

    public DonThuocService() {
        this(new DonThuoc_DAO(), new ChiTietDonThuoc_DAO(), new HoSoSucKhoe_DAO(),
                new ThuCung_DAO(), new BacSi_DAO());
    }

    DonThuocService(DonThuoc_DAO donThuocDao, ChiTietDonThuoc_DAO chiTietDao,
            HoSoSucKhoe_DAO hoSoDao, ThuCung_DAO thuCungDao, BacSi_DAO bacSiDao) {
        this.donThuocDao = donThuocDao;
        this.chiTietDao = chiTietDao;
        this.hoSoDao = hoSoDao;
        this.thuCungDao = thuCungDao;
        this.bacSiDao = bacSiDao;
    }

    public List<DonThuoc_entity> taiDanhSach() throws SQLException {
        return donThuocDao.getAllDonThuoc();
    }

    public DuLieuForm taiDuLieuForm() throws SQLException {
        return new DuLieuForm(hoSoDao.getAllHoSoSucKhoe(), thuCungDao.getAllThuCung(),
                bacSiDao.getAll(), chiTietDao.getAllThuoc());
    }

    public String maTiepTheo() throws SQLException {
        return donThuocDao.getMaTiepTheo();
    }

    public List<ChiTietDonThuoc_entity> taiChiTiet(String maDonThuoc) throws SQLException {
        return chiTietDao.getChiTietTheoMaDon(maDonThuoc);
    }

    public DonThuoc_entity timTheoMa(String maDonThuoc) throws SQLException {
        return donThuocDao.getDonThuocTheoMa(maDonThuoc);
    }

    public List<ChiTietDonThuoc_entity> taiChiTietDon(String maDonThuoc) throws SQLException {
        return donThuocDao.getChiTietDonThuoc(maDonThuoc);
    }

    public boolean xoa(String maDonThuoc) throws SQLException {
        return donThuocDao.deleteDonThuoc(maDonThuoc);
    }

    public boolean luu(ThongTinDonThuoc thongTin) throws SQLException {
        if (thongTin.hoSo == null) {
            throw new IllegalArgumentException("Vui lòng chọn hồ sơ khám.");
        }
        LocalDateTime ngayKeDon;
        try {
            ngayKeDon = LocalDateTime.parse(thongTin.ngayKeDon.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày kê đơn không hợp lệ (dd/MM/yyyy HH:mm).", e);
        }
        if (thongTin.chiTiet == null || thongTin.chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một thuốc.");
        }
        DonThuoc_entity don = new DonThuoc_entity(thongTin.maDonThuoc,
                thongTin.hoSo.getMaTC(), thongTin.hoSo.getMaBacSi(), thongTin.hoSo.getMaHSSK(),
                ngayKeDon, thongTin.ghiChu.trim(), thongTin.trangThai);
        return thongTin.donMoi
                ? donThuocDao.createDonThuoc(don, thongTin.chiTiet)
                : donThuocDao.updateDonThuoc(don, thongTin.chiTiet);
    }

    public ChiTietDonThuoc_entity taoChiTiet(Thuoc_entity thuoc, String soLuong, String lieuLuong,
            String maDonThuoc, List<ChiTietDonThuoc_entity> chiTietHienTai) {
        if (thuoc == null) {
            throw new IllegalArgumentException("Vui lòng chọn thuốc.");
        }
        BigDecimal soLuongValue;
        BigDecimal lieuLuongValue;
        try {
            soLuongValue = new BigDecimal(soLuong.trim());
            lieuLuongValue = new BigDecimal(lieuLuong.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng và liều lượng phải là số hợp lệ.", e);
        }
        if (soLuongValue.signum() <= 0 || lieuLuongValue.signum() <= 0) {
            throw new IllegalArgumentException("Số lượng và liều lượng phải lớn hơn 0.");
        }
        for (ChiTietDonThuoc_entity chiTiet : chiTietHienTai) {
            if (chiTiet.getMaThuoc() != null
                    && thuoc.getMaThuoc().equals(chiTiet.getMaThuoc().getMaThuoc())) {
                throw new IllegalArgumentException("Đã có trong đơn.");
            }
        }
        return new ChiTietDonThuoc_entity(null, maDonThuoc, thuoc.getMaThuoc(),
                soLuongValue, lieuLuongValue);
    }

    public List<DonThuoc_entity> loc(List<DonThuoc_entity> danhSach, String tuKhoa,
            String trangThai, String khoangThoiGian, LocalDate tuNgay, LocalDate denNgay,
            LocalDate homNay) {
        LocalDate tu = tuNgay;
        LocalDate den = denNgay;
        if (!"Tùy chỉnh khoảng ngày...".equals(khoangThoiGian)) {
            tu = null;
            den = null;
            if ("Hôm nay".equals(khoangThoiGian)) {
                tu = homNay;
                den = homNay;
            } else if ("Tuần này".equals(khoangThoiGian)) {
                tu = homNay.with(java.time.DayOfWeek.MONDAY);
                den = homNay.with(java.time.DayOfWeek.SUNDAY);
            } else if (khoangThoiGian != null && khoangThoiGian.startsWith("Tháng ")) {
                int month = Integer.parseInt(khoangThoiGian.substring("Tháng ".length()));
                tu = LocalDate.of(homNay.getYear(), month, 1);
                den = tu.with(TemporalAdjusters.lastDayOfMonth());
            }
        }
        String keyword = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<DonThuoc_entity> ketQua = new ArrayList<>();
        for (DonThuoc_entity don : danhSach) {
            LocalDate ngay = don.getNgayKeDon() == null ? null : don.getNgayKeDon().toLocalDate();
            boolean matchedKeyword = keyword.isEmpty()
                    || value(don.getMaDonThuoc()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getMaHSSK() == null ? null : don.getMaHSSK().getMaHSSK()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getTenThuCung()).toLowerCase(Locale.ROOT).contains(keyword)
                    || value(don.getTenBacSi()).toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchedStatus = trangThai == null || trangThai.startsWith("Trạng thái:")
                    || trangThai.equals(don.getTrangThai());
            boolean matchedDate = (tu == null || (ngay != null && !ngay.isBefore(tu)))
                    && (den == null || (ngay != null && !ngay.isAfter(den)));
            if (matchedKeyword && matchedStatus && matchedDate) ketQua.add(don);
        }
        return ketQua;
    }

    public ThongKe thongKe(List<DonThuoc_entity> danhSach) {
        int daCap = 0;
        int choLay = 0;
        int loaiThuoc = 0;
        for (DonThuoc_entity don : danhSach) {
            if ("Đã cấp thuốc".equals(don.getTrangThai()) || "Hoàn thành".equals(don.getTrangThai())) daCap++;
            else if ("Chờ lấy thuốc".equals(don.getTrangThai())) choLay++;
            loaiThuoc += don.getSoLoaiThuoc();
        }
        return new ThongKe(danhSach.size(), daCap, choLay, loaiThuoc);
    }

    public void xuatDanhSach(File file, List<DonThuoc_entity> danhSach) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            ghiDong(writer, "Mã đơn thuốc", "Thú cưng", "Mã hồ sơ", "Bác sĩ kê đơn",
                    "Ngày kê đơn", "Số loại thuốc", "Ghi chú", "Trạng thái");
            for (DonThuoc_entity don : danhSach) {
                ghiDong(writer, value(don.getMaDonThuoc()),
                        value(don.getTenThuCung()) + " ("
                                + (don.getMaTC() == null ? "" : don.getMaTC().getMaTC()) + ")",
                        don.getMaHSSK() == null ? "" : value(don.getMaHSSK().getMaHSSK()),
                        String.valueOf(don.getTenBacSi()),
                        don.getNgayKeDon() == null ? "" : DATE_TIME_FORMAT.format(don.getNgayKeDon()),
                        String.valueOf(don.getSoLoaiThuoc()), value(don.getGhiChu()),
                        String.valueOf(don.getTrangThai()));
            }
        }
    }

    private void ghiDong(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(',');
            writer.write("\"" + values[i].replace("\"", "\"\"") + "\"");
        }
        writer.newLine();
    }

    public KhoangNgay khoangNgay(String tuNgayText, String denNgayText) {
        LocalDate tu = parseNgay(tuNgayText);
        LocalDate den = parseNgay(denNgayText);
        if (tu != null && den != null && tu.isAfter(den)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        return new KhoangNgay(tu, den);
    }

    private LocalDate parseNgay(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy.", e);
        }
    }

    public KetQuaXuatThuoc xuatVaTaoHoaDon(String maDon, NhanVien_entity nhanVien) throws SQLException {
        DonThuoc_entity don = donThuocDao.getDonThuocTheoMa(maDon);
        if (don == null) throw new IllegalArgumentException("Không tìm thấy thông tin đơn thuốc " + maDon);
        List<ChiTietDonThuoc_entity> chiTietList = donThuocDao.getChiTietDonThuoc(maDon);
        if (chiTietList == null || chiTietList.isEmpty()) {
            throw new IllegalArgumentException("Đơn thuốc này không có chi tiết thuốc nào!");
        }
        ThuCung_entity thuCung = thuCungDao.getThuCungTheoMa(
                don.getMaTC() == null ? null : don.getMaTC().getMaTC());
        if (thuCung == null || thuCung.getMaKH() == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin Thú cưng / Khách hàng để tạo hóa đơn!");
        }
        if (nhanVien == null) throw new IllegalArgumentException("Không xác định được nhân viên đang đăng nhập!");

        HoaDon_Dao hoaDonDao = new HoaDon_Dao();
        ChiTietHoaDon_DAO chiTietHoaDonDao = new ChiTietHoaDon_DAO();
        VatTu_DAO vatTuDao = new VatTu_DAO();
        Connection connection = ConnectDB.getCon();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu!");
        try (Connection con = connection) {
            try {
                con.setAutoCommit(false);
                String maHoaDon = String.format("HD%03d", hoaDonDao.laySoHoaDonLonNhat() + 1);
                HoaDon_entity hoaDon = new HoaDon_entity(maHoaDon, LocalDate.now(),
                        "Chờ thanh toán", thuCung.getMaKH(), nhanVien, null, null, null);
                hoaDon.setMaTC(thuCung.getMaTC());
                if (!hoaDonDao.themHD(con, hoaDon)) {
                    throw new SQLException("Không thể lưu thông tin Hóa Đơn " + maHoaDon);
                }
                int soDong = 1;
                double tongTien = 0;
                for (ChiTietDonThuoc_entity chiTietDon : chiTietList) {
                    String maThuoc = chiTietDon.getMaThuoc() == null
                            ? null : chiTietDon.getMaThuoc().getMaThuoc();
                    VatTu_entity vatTu = vatTuDao.getVatTuTheoMaThuoc(maThuoc);
                    if (vatTu == null) vatTu = vatTuDao.getVatTuTheoMa(maThuoc);
                    if (vatTu == null) {
                        throw new SQLException("Không tìm thấy mặt hàng trong kho cho thuốc: "
                                + chiTietDon.getTenThuoc() + " (Mã: " + maThuoc + ")");
                    }
                    double donGia = vatTu.getGiaBanCoBan();
                    int soLuong = chiTietDon.getSoLuong().intValue();
                    if (soLuong <= 0) soLuong = 1;
                    ChiTietHoaDon_entity dongHoaDon = new ChiTietHoaDon_entity(soDong++, hoaDon,
                            null, vatTu, nhanVien, null, soLuong, donGia);
                    dongHoaDon.setMaCTDT(chiTietDon.getMaCTDT());
                    if (!chiTietHoaDonDao.createChiTietHoaDon(con, dongHoaDon)) {
                        throw new SQLException("Không thể lưu dòng chi tiết hóa đơn cho thuốc: " + vatTu.getTenVT());
                    }
                    tongTien += donGia * soLuong;
                }
                if (!donThuocDao.capNhatTrangThai(maDon, "Hoàn thành")) {
                    throw new SQLException("Không thể cập nhật trạng thái đơn thuốc sang 'Hoàn thành'!");
                }
                con.commit();
                return new KetQuaXuatThuoc(maHoaDon, thuCung.getTenTC(), tongTien);
            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    public static final class DuLieuForm {
        public final List<HoSoSucKhoe_entity> hoSo;
        public final List<ThuCung_entity> thuCung;
        public final List<BacSi_entity> bacSi;
        public final List<Thuoc_entity> thuoc;
        public DuLieuForm(List<HoSoSucKhoe_entity> hoSo, List<ThuCung_entity> thuCung,
                List<BacSi_entity> bacSi, List<Thuoc_entity> thuoc) {
            this.hoSo = hoSo;
            this.thuCung = thuCung;
            this.bacSi = bacSi;
            this.thuoc = thuoc;
        }
    }

    public static final class ThongTinDonThuoc {
        public final String maDonThuoc;
        public final boolean donMoi;
        public final HoSoSucKhoe_entity hoSo;
        public final String ngayKeDon;
        public final String ghiChu;
        public final String trangThai;
        public final List<ChiTietDonThuoc_entity> chiTiet;
        public ThongTinDonThuoc(String maDonThuoc, boolean donMoi, HoSoSucKhoe_entity hoSo,
                String ngayKeDon, String ghiChu, String trangThai, List<ChiTietDonThuoc_entity> chiTiet) {
            this.maDonThuoc = maDonThuoc;
            this.donMoi = donMoi;
            this.hoSo = hoSo;
            this.ngayKeDon = ngayKeDon;
            this.ghiChu = ghiChu;
            this.trangThai = trangThai;
            this.chiTiet = chiTiet;
        }
    }

    public static final class ThongKe {
        public final int tong;
        public final int daCap;
        public final int choLay;
        public final int loaiThuoc;
        public ThongKe(int tong, int daCap, int choLay, int loaiThuoc) {
            this.tong = tong;
            this.daCap = daCap;
            this.choLay = choLay;
            this.loaiThuoc = loaiThuoc;
        }
    }

    public static final class KhoangNgay {
        public final LocalDate tuNgay;
        public final LocalDate denNgay;
        private KhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
        }
    }

    public static final class KetQuaXuatThuoc {
        public final String maHoaDon;
        public final String tenThuCung;
        public final double tongTien;
        public KetQuaXuatThuoc(String maHoaDon, String tenThuCung, double tongTien) {
            this.maHoaDon = maHoaDon;
            this.tenThuCung = tenThuCung;
            this.tongTien = tongTien;
        }
    }
}
