package controller.ThuCung;

import dao.BacSi.BacSi_DAO;
import dao.ThuCung.HoSoSucKhoe_DAO;
import dao.ThuCung.ThuCung_DAO;
import entity.BacSi.BacSi_entity;
import entity.ThuCung.HoSoSucKhoe_entity;
import entity.ThuCung.ThuCung_entity;

import java.math.BigDecimal;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LichSuKhamController {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final HoSoSucKhoe_DAO hoSoDao;
    private final ThuCung_DAO thuCungDao;
    private final BacSi_DAO bacSiDao;

    public LichSuKhamController() {
        this(new HoSoSucKhoe_DAO(), new ThuCung_DAO(), new BacSi_DAO());
    }

    LichSuKhamController(HoSoSucKhoe_DAO hoSoDao, ThuCung_DAO thuCungDao, BacSi_DAO bacSiDao) {
        this.hoSoDao = hoSoDao;
        this.thuCungDao = thuCungDao;
        this.bacSiDao = bacSiDao;
    }

    public DuLieuManHinh taiDuLieu() throws SQLException {
        return new DuLieuManHinh(hoSoDao.getAllHoSoSucKhoe(), bacSiDao.getAll());
    }

    public List<ThuCung_entity> taiThuCung() {
        return thuCungDao.getAllThuCung();
    }

    public List<BacSi_entity> taiBacSi() throws SQLException {
        return bacSiDao.getAll();
    }

    public String maTiepTheo() throws SQLException {
        return hoSoDao.getMaTiepTheo();
    }

    public HoSoSucKhoe_entity timTheoMa(String maHoSo) throws SQLException {
        return hoSoDao.getHoSoSucKhoeTheoMa(maHoSo);
    }

    public boolean xoa(String maHoSo) throws SQLException {
        return hoSoDao.deleteHoSoSucKhoe(maHoSo);
    }

    public boolean luu(ThongTinHoSo thongTin) throws SQLException {
        if (thongTin.thuCung == null || thongTin.bacSi == null
                || thongTin.trieuChung.trim().isEmpty() || thongTin.chuanDoan.trim().isEmpty()
                || thongTin.trieuChung.trim().length() > 500
                || thongTin.chuanDoan.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "Chọn thú cưng/bác sĩ và nhập triệu chứng, chẩn đoán (tối đa 500 ký tự).");
        }
        BigDecimal canNang = null;
        if (!thongTin.canNang.trim().isEmpty()) {
            try {
                canNang = new BigDecimal(thongTin.canNang.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cân nặng phải là số hợp lệ.", e);
            }
            if (canNang.signum() <= 0) {
                throw new IllegalArgumentException("Cân nặng phải lớn hơn 0.");
            }
        }
        LocalDateTime ngayKham;
        try {
            ngayKham = LocalDateTime.parse(thongTin.ngayKham.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày khám không hợp lệ, hãy nhập dd/MM/yyyy HH:mm.", e);
        }
        HoSoSucKhoe_entity hoSo = new HoSoSucKhoe_entity(thongTin.maHoSo,
                thongTin.thuCung.getMaTC(), thongTin.bacSi.getMaBacSi(), ngayKham,
                thongTin.trieuChung.trim(), thongTin.chuanDoan.trim(), canNang);
        return thongTin.maHoSoMoi ? hoSoDao.createHoSoSucKhoe(hoSo) : hoSoDao.updateHoSoSucKhoe(hoSo);
    }

    public List<HoSoSucKhoe_entity> loc(List<HoSoSucKhoe_entity> danhSach, String tuKhoa,
            String bacSiLuaChon, String thoiGianLuaChon, LocalDate tuNgayTuyChinh,
            LocalDate denNgayTuyChinh, LocalDate ngayHienTai) {
        LocalDate tuNgay = tuNgayTuyChinh;
        LocalDate denNgay = denNgayTuyChinh;
        if ("Hôm nay".equals(thoiGianLuaChon)) {
            tuNgay = ngayHienTai;
            denNgay = ngayHienTai;
        } else if ("7 ngày qua".equals(thoiGianLuaChon)) {
            tuNgay = ngayHienTai.minusDays(6);
            denNgay = ngayHienTai;
        } else if ("Tháng này".equals(thoiGianLuaChon)) {
            tuNgay = ngayHienTai.withDayOfMonth(1);
            denNgay = ngayHienTai;
        }
        String keyword = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<HoSoSucKhoe_entity> ketQua = new ArrayList<>();
        for (HoSoSucKhoe_entity hoSo : danhSach) {
            LocalDate ngay = hoSo.getNgayKham() == null ? null : hoSo.getNgayKham().toLocalDate();
            boolean matchedKeyword = keyword.isEmpty()
                    || giaTri(hoSo.getMaHSSK()).toLowerCase(Locale.ROOT).contains(keyword)
                    || giaTri(hoSo.getMaTC() == null ? null : hoSo.getMaTC().getMaTC()).toLowerCase(Locale.ROOT)
                            .contains(keyword)
                    || giaTri(hoSo.getTenThuCung()).toLowerCase(Locale.ROOT).contains(keyword)
                    || giaTri(hoSo.getChuHo()).toLowerCase(Locale.ROOT).contains(keyword)
                    || giaTri(hoSo.getTrieuChung()).toLowerCase(Locale.ROOT).contains(keyword)
                    || giaTri(hoSo.getChuanDoan()).toLowerCase(Locale.ROOT).contains(keyword)
                    || giaTri(hoSo.getTenBacSi()).toLowerCase(Locale.ROOT).contains(keyword);
            String maBacSi = hoSo.getMaBacSi() == null ? "" : hoSo.getMaBacSi().getMaBacSi();
            boolean matchedDoctor = bacSiLuaChon == null || bacSiLuaChon.startsWith("Bác sĩ:")
                    || bacSiLuaChon.endsWith("(" + maBacSi + ")");
            boolean matchedDate = (tuNgay == null || (ngay != null && !ngay.isBefore(tuNgay)))
                    && (denNgay == null || (ngay != null && !ngay.isAfter(denNgay)));
            if (matchedKeyword && matchedDoctor && matchedDate) {
                ketQua.add(hoSo);
            }
        }
        return ketQua;
    }

    public ThongKe thongKe(List<HoSoSucKhoe_entity> danhSach, LocalDate ngayHienTai) {
        int ngay = 0;
        int thang = 0;
        Set<String> bacSi = new HashSet<>();
        for (HoSoSucKhoe_entity hoSo : danhSach) {
            if (hoSo.getNgayKham() != null) {
                LocalDate ngayKham = hoSo.getNgayKham().toLocalDate();
                if (ngayKham.equals(ngayHienTai))
                    ngay++;
                if (ngayKham.getYear() == ngayHienTai.getYear()
                        && ngayKham.getMonth() == ngayHienTai.getMonth())
                    thang++;
            }
            if (hoSo.getMaBacSi() != null)
                bacSi.add(hoSo.getMaBacSi().getMaBacSi());
        }
        return new ThongKe(danhSach.size(), ngay, thang, bacSi.size());
    }

    public void xuatDanhSach(File file, List<HoSoSucKhoe_entity> danhSach) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            ghiDong(writer, "Mã hồ sơ", "Thú cưng", "Chủ nuôi", "Ngày khám", "Triệu chứng",
                    "Chẩn đoán", "Cân nặng (kg)", "Bác sĩ phụ trách");
            for (HoSoSucKhoe_entity hoSo : danhSach) {
                ghiDong(writer, giaTri(hoSo.getMaHSSK()),
                        giaTri(hoSo.getTenThuCung()) + " ("
                                + (hoSo.getMaTC() == null ? "" : hoSo.getMaTC().getMaTC()) + ")",
                        giaTri(hoSo.getChuHo()),
                        hoSo.getNgayKham() == null ? "" : DATE_TIME_FORMAT.format(hoSo.getNgayKham()),
                        giaTri(hoSo.getTrieuChung()), giaTri(hoSo.getChuanDoan()),
                        hoSo.getCanNang() == null ? "" : hoSo.getCanNang().toPlainString(),
                        giaTri(hoSo.getTenBacSi()));
            }
        }
    }

    private void ghiDong(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                writer.write(',');
            writer.write("\"" + values[i].replace("\"", "\"\"") + "\"");
        }
        writer.newLine();
    }

    public KhoangNgay khoangNgay(String tuNgayText, String denNgayText) {
        LocalDate tu = parseNgay(tuNgayText);
        LocalDate den = parseNgay(denNgayText);
        if (tu != null && den != null && tu.isAfter(den)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }
        return new KhoangNgay(tu, den);
    }

    private LocalDate parseNgay(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy.", e);
        }
    }

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }

    public static final class DuLieuManHinh {
        public final List<HoSoSucKhoe_entity> hoSo;
        public final List<BacSi_entity> bacSi;

        public DuLieuManHinh(List<HoSoSucKhoe_entity> hoSo, List<BacSi_entity> bacSi) {
            this.hoSo = hoSo;
            this.bacSi = bacSi;
        }
    }

    public static final class ThongTinHoSo {
        public final String maHoSo;
        public final boolean maHoSoMoi;
        public final ThuCung_entity thuCung;
        public final BacSi_entity bacSi;
        public final String ngayKham;
        public final String trieuChung;
        public final String chuanDoan;
        public final String canNang;

        public ThongTinHoSo(String maHoSo, boolean maHoSoMoi, ThuCung_entity thuCung,
                BacSi_entity bacSi, String ngayKham, String trieuChung, String chuanDoan, String canNang) {
            this.maHoSo = maHoSo;
            this.maHoSoMoi = maHoSoMoi;
            this.thuCung = thuCung;
            this.bacSi = bacSi;
            this.ngayKham = ngayKham;
            this.trieuChung = trieuChung;
            this.chuanDoan = chuanDoan;
            this.canNang = canNang;
        }
    }

    public static final class ThongKe {
        public final int tongLuot;
        public final int homNay;
        public final int thangNay;
        public final int soBacSi;

        public ThongKe(int tongLuot, int homNay, int thangNay, int soBacSi) {
            this.tongLuot = tongLuot;
            this.homNay = homNay;
            this.thangNay = thangNay;
            this.soBacSi = soBacSi;
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
}
