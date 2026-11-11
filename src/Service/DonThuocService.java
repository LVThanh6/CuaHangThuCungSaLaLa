package Service;

import connectDB.ConnectDB;
import dao.BacSi_DAO;
import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_Dao;
import dao.ChiTietDonThuoc_DAO;
import dao.DonThuoc_DAO;
import dao.HoSoSucKhoe_DAO;
import dao.ThuCung_DAO;
import dao.VatTu_DAO;
import entity.BacSi_entity;
import entity.ChiTietHoaDon_entity;
import entity.HoaDon_entity;
import entity.NhanVien_entity;
import entity.ChiTietDonThuoc_entity;
import entity.DonThuoc_entity;
import entity.HoSoSucKhoe_entity;
import entity.ThuCung_entity;
import entity.Thuoc_entity;
import entity.VatTu_entity;

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

    //Lấy toàn bộ danh sách đơn thuốc.
    public List<DonThuoc_entity> taiDanhSach() throws SQLException {
        return donThuocDao.getAllDonThuoc();
    }

    //Lấy dữ liệu mồi (Hồ sơ, Thú cưng, Bác sĩ, Thuốc) để đổ vào form kê đơn.
    public DuLieuForm taiDuLieuForm() throws SQLException {
        return new DuLieuForm(hoSoDao.getAllHoSoSucKhoe(), thuCungDao.getAllThuCung(),
                bacSiDao.getAll(), chiTietDao.getAllThuoc());
    }

    //Tự động phát sinh mã đơn thuốc mới.
    public String maTiepTheo() throws SQLException {
        return donThuocDao.getMaTiepTheo();
    }
    
    public List<ChiTietDonThuoc_entity> taiChiTiet(String maDonThuoc) throws SQLException {
        return chiTietDao.getChiTietTheoMaDon(maDonThuoc);
    }

    //Tìm theo mã
    public DonThuoc_entity timTheoMa(String maDonThuoc) throws SQLException {
        return donThuocDao.getDonThuocTheoMa(maDonThuoc);
    }

    public List<ChiTietDonThuoc_entity> taiChiTietDon(String maDonThuoc) throws SQLException {
        return donThuocDao.getChiTietDonThuoc(maDonThuoc);
    }

    //Xóa đơn thuốc theo mã
    public boolean xoa(String maDonThuoc) throws SQLException {
        return donThuocDao.deleteDonThuoc(maDonThuoc);
    }

    //Kiểm tra hợp lệ (ngày giờ, phải có ít nhất 1 thuốc) và thực hiện Thêm mới hoặc Cập nhật đơn thuốc.
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

    //Tạo chi tiết đơn thuốc mới, kiểm tra hợp lệ (thuốc không trùng, số lượng và liều lượng phải là số dương).
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

    // Lọc danh sách đơn thuốc theo từ khóa, trạng thái và khoảng ngày.
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

    //Đếm số lượng đơn thuốc theo trạng thái (đã cấp, chờ lấy) và đếm tổng loại thuốc.
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

    //Xuất danh sách đơn thuốc ra file CSV.
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

    //Ghi một dòng vào file CSV, tự động thêm dấu ngoặc kép và xử lý dấu phẩy trong dữ liệu.
    private void ghiDong(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(',');
            writer.write("\"" + values[i].replace("\"", "\"\"") + "\"");
        }
        writer.newLine();
    }

    //Chuyển đổi chuỗi ngày tháng sang đối tượng LocalDate, kiểm tra hợp lệ và trả về khoảng ngày.
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

    //Hàm nghiệp vụ liên kết. Đổi trạng thái đơn thuốc thành "Hoàn thành", tự động trừ kho vật tư và sinh ra một Hóa Đơn mới kèm theo chi tiết tiền thuốc.
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
