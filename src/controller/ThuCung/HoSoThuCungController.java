package controller.ThuCung;

import dao.KhachHang.KhachHang_DAO;
import dao.ThuCung.ThuCung_DAO;
import entity.KhachHang.KhachHang;
import entity.ThuCung.ThuCung_entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HoSoThuCungController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final ThuCung_DAO thuCungDao;
    private final KhachHang_DAO khachHangDao;

    public HoSoThuCungController() {
        this(new ThuCung_DAO(), new KhachHang_DAO());
    }

    HoSoThuCungController(ThuCung_DAO thuCungDao, KhachHang_DAO khachHangDao) {
        this.thuCungDao = thuCungDao;
        this.khachHangDao = khachHangDao;
    }

    public List<ThuCung_entity> taiDanhSach() {
        return thuCungDao.getAllThuCung();
    }

    public List<KhachHang> taiKhachHang() {
        return khachHangDao.getAlltbKhachHang();
    }

    public ThuCung_entity timTheoMa(String maThuCung) {
        return thuCungDao.getThuCungTheoMa(maThuCung);
    }

    public boolean xoa(String maThuCung) {
        return thuCungDao.deleteThuCung(maThuCung);
    }

    public boolean luu(ThongTinThuCung thongTin) {
        if (thongTin == null || thongTin.khachHang == null
                || thongTin.ten.trim().isEmpty() || thongTin.loai.trim().isEmpty()
                || thongTin.chuHo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đủ tên, loài, chủ nuôi.");
        }
        LocalDate ngaySinh = null;
        if (!thongTin.ngaySinh.trim().isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(thongTin.ngaySinh.trim(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Ngày sinh phải dạng dd/MM/yyyy.", e);
            }
        }
        ThuCung_entity thuCung = new ThuCung_entity(
                thongTin.maThuCung,
                thongTin.khachHang,
                thongTin.ten.trim(),
                thongTin.loai.trim(),
                thongTin.gioiTinh,
                ngaySinh,
                thongTin.chuHo.trim(),
                thongTin.dacDiem.trim());
        return thongTin.maThuCungMoi ? thuCungDao.createThuCung(thuCung) : thuCungDao.updateThuCung(thuCung);
    }

    public String phatSinhMa(List<ThuCung_entity> danhSach) {
        int maLonNhat = 0;
        Pattern pattern = Pattern.compile("^TC(\\d+)$", Pattern.CASE_INSENSITIVE);
        for (ThuCung_entity thuCung : danhSach) {
            Matcher matcher = pattern.matcher(thuCung.getMaTC() == null ? "" : thuCung.getMaTC());
            if (matcher.matches()) {
                maLonNhat = Math.max(maLonNhat, Integer.parseInt(matcher.group(1)));
            }
        }
        return String.format(Locale.ROOT, "TC%03d", maLonNhat + 1);
    }

    public List<ThuCung_entity> loc(List<ThuCung_entity> danhSach, String tuKhoa, String loai) {
        String keyword = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<ThuCung_entity> ketQua = new ArrayList<>();
        for (ThuCung_entity thuCung : danhSach) {
            if (thuCung == null) {
                continue;
            }
            String ma = giaTri(thuCung.getMaTC());
            String ten = giaTri(thuCung.getTenTC());
            String loaiThuCung = giaTri(thuCung.getLoai());
            String chuHo = giaTri(thuCung.getChuHo());
            String dienThoai = thuCung.getMaKH() == null ? "" : giaTri(thuCung.getMaKH().getSdt());
            boolean khopTuKhoa = keyword.isEmpty()
                    || ma.toLowerCase(Locale.ROOT).contains(keyword)
                    || loaiThuCung.toLowerCase(Locale.ROOT).contains(keyword)
                    || ten.toLowerCase(Locale.ROOT).contains(keyword)
                    || chuHo.toLowerCase(Locale.ROOT).contains(keyword)
                    || dienThoai.toLowerCase(Locale.ROOT).contains(keyword);
            boolean khopLoai = loai == null || loai.startsWith("Loài:")
                    || loaiThuCung.equalsIgnoreCase(loai);
            if (khopTuKhoa && khopLoai) {
                ketQua.add(thuCung);
            }
        }
        return ketQua;
    }

    public ThongKe thongKe(List<ThuCung_entity> danhSach) {
        int cho = 0;
        int meo = 0;
        for (ThuCung_entity thuCung : danhSach) {
            if (thuCung != null && "Chó".equalsIgnoreCase(thuCung.getLoai())) {
                cho++;
            } else if (thuCung != null && "Mèo".equalsIgnoreCase(thuCung.getLoai())) {
                meo++;
            }
        }
        return new ThongKe(danhSach.size(), cho, meo);
    }

    public void xuatDanhSach(File file, List<ThuCung_entity> danhSach) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            ghiDong(writer, "Mã thú cưng", "Tên thú cưng", "Loài", "Giới tính",
                    "Ngày sinh", "Chủ nuôi", "SĐT chủ", "Đặc điểm nhận diện");
            for (ThuCung_entity thuCung : danhSach) {
                ghiDong(writer, giaTri(thuCung.getMaTC()), giaTri(thuCung.getTenTC()),
                        giaTri(thuCung.getLoai()), giaTri(thuCung.getGioiTinh()),
                        thuCung.getNgaySinh() == null ? "" : DATE_FORMAT.format(thuCung.getNgaySinh()),
                        giaTri(thuCung.getChuHo()),
                        thuCung.getMaKH() == null ? "" : giaTri(thuCung.getMaKH().getSdt()),
                        giaTri(thuCung.getDacDiemNhanDien()));
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

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }

    public static final class ThongTinThuCung {
        public final String maThuCung;
        public final boolean maThuCungMoi;
        public final KhachHang khachHang;
        public final String ten;
        public final String loai;
        public final String gioiTinh;
        public final String ngaySinh;
        public final String chuHo;
        public final String dacDiem;

        public ThongTinThuCung(String maThuCung, boolean maThuCungMoi, KhachHang khachHang,
                String ten, String loai, String gioiTinh, String ngaySinh, String chuHo, String dacDiem) {
            this.maThuCung = maThuCung;
            this.maThuCungMoi = maThuCungMoi;
            this.khachHang = khachHang;
            this.ten = ten;
            this.loai = loai;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.chuHo = chuHo;
            this.dacDiem = dacDiem;
        }
    }

    public static final class ThongKe {
        public final int tong;
        public final int cho;
        public final int meo;
        public ThongKe(int tong, int cho, int meo) {
            this.tong = tong;
            this.cho = cho;
            this.meo = meo;
        }
    }
}
