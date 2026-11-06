package controller;

import dao.BacSi.BacSi_DAO;
import dao.BacSi.ChuyenNganh_DAO;
import dao.LichHen.LichLamViec_DAO;
import entity.BacSi.BacSi_entity;
import entity.BacSi.ChuyenNganh_entity;
import entity.LichHen.LichLamViec_entity;

import java.sql.SQLException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BacSiController {
    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final BacSi_DAO bacSiDao;
    private final ChuyenNganh_DAO chuyenNganhDao;
    private final LichLamViec_DAO lichLamViecDao;

    public BacSiController() {
        this(new BacSi_DAO(), new ChuyenNganh_DAO(), new LichLamViec_DAO());
    }

    BacSiController(BacSi_DAO bacSiDao, ChuyenNganh_DAO chuyenNganhDao, LichLamViec_DAO lichLamViecDao) {
        this.bacSiDao = bacSiDao;
        this.chuyenNganhDao = chuyenNganhDao;
        this.lichLamViecDao = lichLamViecDao;
    }

    public DuLieuDanhSach taiDanhSach() throws SQLException {
        return new DuLieuDanhSach(bacSiDao.getAll(), bacSiDao.getTongNhanVien());
    }

    public String taoMaMoi() throws SQLException {
        return bacSiDao.getMaTiepTheo();
    }

    public List<ChuyenNganh_entity> taiChuyenNganh() throws SQLException {
        return chuyenNganhDao.getAll();
    }

    public boolean themChuyenNganh(String ten) throws SQLException {
        return ten != null && !ten.trim().isEmpty() && chuyenNganhDao.them(ten.trim());
    }

    public boolean doiTrangThai(String maBacSi, boolean hoatDong) throws SQLException {
        return bacSiDao.doiTrangThaiTaiKhoan(maBacSi, hoatDong);
    }

    public KetQuaXoa xoa(String maBacSi) throws SQLException {
        try {
            return new KetQuaXoa(bacSiDao.xoa(maBacSi), false);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) return new KetQuaXoa(false, true);
            throw ex;
        }
    }

    public List<BacSi_entity> locDanhSach(List<BacSi_entity> danhSach, String tuKhoa,
            String tenChuyenNganh, String trangThai, int cotSapXep, boolean tangDan) {
        String query = boDau(tuKhoa.trim());
        String querySo = query.replace(" ", "");
        List<BacSi_entity> ketQua = new ArrayList<>();
        for (BacSi_entity bacSi : danhSach) {
            String specialty = bacSi.getChuyenNganh() == null ? null : bacSi.getChuyenNganh().getTenChuyenNganh();
            if (tenChuyenNganh != null && !tenChuyenNganh.equals(specialty)) continue;
            if (trangThai != null && !trangThai.equals(trangThai(bacSi))) continue;
            if (!query.isEmpty()
                    && !boDau(bacSi.getHoTen() == null ? "" : bacSi.getHoTen()).contains(query)
                    && !boDau(bacSi.getMaBacSi() == null ? "" : bacSi.getMaBacSi()).contains(query)
                    && !(bacSi.getSdt() == null ? "" : bacSi.getSdt()).replace(" ", "").contains(querySo)) continue;
            ketQua.add(bacSi);
        }
        if (cotSapXep >= 0) {
            Collator collator = Collator.getInstance(VI);
            ketQua.sort((a, b) -> {
                int result;
                if (cotSapXep == 6) {
                    result = Integer.compare(a.getSoCaKham(), b.getSoCaKham());
                } else {
                    result = collator.compare(tenGoi(a.getHoTen()), tenGoi(b.getHoTen()));
                    if (result == 0) result = collator.compare(a.getHoTen(), b.getHoTen());
                }
                return tangDan ? result : -result;
            });
        }
        return ketQua;
    }

    public ThongKe thongKe(List<BacSi_entity> danhSach) {
        int dangLam = 0, tamNghi = 0, soCa = 0;
        for (BacSi_entity bacSi : danhSach) {
            if (bacSi.getTrangThaiTK() == null) continue;
            if (bacSi.getTrangThaiTK()) dangLam++;
            else tamNghi++;
            soCa += bacSi.getSoCaKham();
        }
        return new ThongKe(danhSach.size(), dangLam, tamNghi, soCa);
    }

    public String taoNoiDungCsv(List<BacSi_entity> danhSach) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Mã bác sĩ,Họ tên,Giới tính,Số điện thoại,Chuyên ngành,Email,Số ca khám,Trạng thái\n");
        for (BacSi_entity bacSi : danhSach) {
            csv.append(csv(bacSi.getMaBacSi())).append(',')
                    .append(csv(nz(bacSi.getHoTen()))).append(',')
                    .append(csv(nz(bacSi.getGioiTinh()))).append(',')
                    .append(csv("'" + nz(bacSi.getSdt()).replace(" ", ""))).append(',')
                    .append(csv(nz(bacSi.getChuyenNganh()))).append(',')
                    .append(csv(nz(bacSi.getEmail()))).append(',')
                    .append(bacSi.getSoCaKham()).append(',')
                    .append(csv(trangThaiNhan(bacSi))).append('\n');
        }
        return csv.toString();
    }

    private static String trangThaiNhan(BacSi_entity bacSi) {
        if (bacSi.getTrangThaiTK() == null) return "Chưa có tài khoản";
        return bacSi.getTrangThaiTK() ? "Đang làm việc" : "Tạm nghỉ";
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String nz(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String trangThai(BacSi_entity bacSi) {
        if (bacSi.getTrangThaiTK() == null) return "CHUA_TK";
        return bacSi.getTrangThaiTK() ? "DANG_LAM" : "TAM_NGHI";
    }

    private static String boDau(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(VI), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replace('đ', 'd');
    }

    private static String tenGoi(String ten) {
        if (ten == null) return "";
        int index = ten.lastIndexOf(' ');
        return index < 0 ? ten : ten.substring(index + 1);
    }

    public KetQuaLuu validateAndSave(ThongTinBacSi thongTin) throws SQLException {
        String loi = validate(thongTin);
        if (loi != null) {
            return new KetQuaLuu(false, loi);
        }
        BacSi_entity entity = new BacSi_entity(
                thongTin.maBacSi,
                thongTin.bacSiHienTai == null ? null : thongTin.bacSiHienTai.getMaTK(),
                thongTin.hoTen,
                thongTin.gioiTinh,
                thongTin.soDienThoai,
                thongTin.tenChuyenNganh == null ? null : new ChuyenNganh_entity(null, thongTin.tenChuyenNganh),
                thongTin.email,
                thongTin.ngaySinh,
                thongTin.luongThang,
                thongTin.bacSiHienTai == null ? 0 : thongTin.bacSiHienTai.getSoCaKham(),
                thongTin.bacSiHienTai == null ? null : thongTin.bacSiHienTai.getTrangThaiTK());

        boolean saved = thongTin.bacSiHienTai == null ? bacSiDao.them(entity) : bacSiDao.capNhat(entity);
        if (saved && thongTin.capNhatTrangThaiTaiKhoan) {
            saved = bacSiDao.doiTrangThaiTaiKhoan(thongTin.maBacSi, thongTin.trangThaiTaiKhoan);
        }
        return new KetQuaLuu(saved, saved ? null : "Không thể lưu thông tin bác sĩ vào CSDL.");
    }

    public KetQuaLuu validateAndSave(String maBacSi, BacSi_entity current, String hoTen,
            String soDienThoai, String email, String ngaySinhText, String gioiTinh,
            String tenChuyenNganh, String luongText, boolean capNhatTrangThai,
            boolean trangThaiTaiKhoan) throws SQLException {
        String normalizedName = hoTen.trim();
        String normalizedPhone = soDienThoai.replaceAll("\\s", "");
        String normalizedEmail = email.trim();
        if (normalizedName.isEmpty()) return new KetQuaLuu(false, "Vui lòng nhập họ và tên.");
        if (!normalizedPhone.matches("0\\d{9}")) return new KetQuaLuu(false, "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.");
        if (!normalizedEmail.isEmpty() && !normalizedEmail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return new KetQuaLuu(false, "Email không hợp lệ.");
        }
        String birthText = ngaySinhText.trim();
        LocalDate birth = null;
        if (!birthText.isEmpty()) {
            try {
                birth = LocalDate.parse(birthText, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                return new KetQuaLuu(false, "Ngày sinh không hợp lệ (dd/MM/yyyy).");
            }
        }
        if (birth != null && birth.isAfter(LocalDate.now())) {
            return new KetQuaLuu(false, "Ngày sinh không được ở tương lai.");
        }
        double salary;
        try {
            salary = Double.parseDouble(luongText.replaceAll("[.,\\s]", ""));
        } catch (NumberFormatException ex) {
            return new KetQuaLuu(false, "Lương tháng không hợp lệ (nhập số).");
        }
        ThongTinBacSi input = new ThongTinBacSi(maBacSi, current, normalizedName,
                normalizedPhone, normalizedEmail, birthText, birth, gioiTinh,
                tenChuyenNganh, salary, capNhatTrangThai, trangThaiTaiKhoan);
        return validateAndSave(input);
    }

    private String validate(ThongTinBacSi input) {
        if (input.hoTen.isEmpty()) return "Vui lòng nhập họ và tên.";
        if (!input.soDienThoai.matches("0\\d{9}")) return "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.";
        if (!input.email.isEmpty() && !input.email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return "Email không hợp lệ.";
        if (input.ngaySinhText.isEmpty() == false && input.ngaySinh == null) return "Ngày sinh không hợp lệ (dd/MM/yyyy).";
        if (input.ngaySinh != null && input.ngaySinh.isAfter(LocalDate.now())) return "Ngày sinh không được ở tương lai.";
        if (input.luongThang < 0) return "Lương tháng không được âm.";
        return null;
    }

    public List<LichLamViec_entity> taiLichBacSi(String maBacSi, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        List<LichLamViec_entity> lichTheoKhoang = lichLamViecDao.getLichTrongKhoang(tuNgay, denNgay);
        List<LichLamViec_entity> ketQua = new ArrayList<>();
        for (LichLamViec_entity lich : lichTheoKhoang) {
            if (lich.getLoaiNhanSu() == LichLamViec_entity.LoaiNhanSu.BAC_SI
                    && maBacSi.equals(lich.getMaNhanSu())) {
                ketQua.add(lich);
            }
        }
        return ketQua;
    }

    public static final class DuLieuDanhSach {
        private final List<BacSi_entity> bacSi;
        private final int tongNhanVien;

        private DuLieuDanhSach(List<BacSi_entity> bacSi, int tongNhanVien) {
            this.bacSi = new ArrayList<>(bacSi);
            this.tongNhanVien = tongNhanVien;
        }

        public List<BacSi_entity> getBacSi() { return new ArrayList<>(bacSi); }
        public int getTongNhanVien() { return tongNhanVien; }
    }

    public static final class ThongKe {
        public final int tongSo, dangLam, tamNghi, soCa;
        private ThongKe(int tongSo, int dangLam, int tamNghi, int soCa) {
            this.tongSo = tongSo; this.dangLam = dangLam; this.tamNghi = tamNghi; this.soCa = soCa;
        }
    }

    public static final class KetQuaXoa {
        public final boolean thanhCong, coDuLieuLienQuan;
        private KetQuaXoa(boolean thanhCong, boolean coDuLieuLienQuan) {
            this.thanhCong = thanhCong; this.coDuLieuLienQuan = coDuLieuLienQuan;
        }
    }

    public static final class ThongTinBacSi {
        public final String maBacSi;
        public final BacSi_entity bacSiHienTai;
        public final String hoTen;
        public final String soDienThoai;
        public final String email;
        public final String ngaySinhText;
        public final LocalDate ngaySinh;
        public final String gioiTinh;
        public final String tenChuyenNganh;
        public final double luongThang;
        public final boolean capNhatTrangThaiTaiKhoan;
        public final boolean trangThaiTaiKhoan;

        public ThongTinBacSi(String maBacSi, BacSi_entity bacSiHienTai, String hoTen, String soDienThoai,
                String email, String ngaySinhText, LocalDate ngaySinh, String gioiTinh, String tenChuyenNganh,
                double luongThang, boolean capNhatTrangThaiTaiKhoan, boolean trangThaiTaiKhoan) {
            this.maBacSi = maBacSi;
            this.bacSiHienTai = bacSiHienTai;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.ngaySinhText = ngaySinhText;
            this.ngaySinh = ngaySinh;
            this.gioiTinh = gioiTinh;
            this.tenChuyenNganh = tenChuyenNganh;
            this.luongThang = luongThang;
            this.capNhatTrangThaiTaiKhoan = capNhatTrangThaiTaiKhoan;
            this.trangThaiTaiKhoan = trangThaiTaiKhoan;
        }
    }

    public static final class KetQuaLuu {
        private final boolean thanhCong;
        private final String thongBaoLoi;

        private KetQuaLuu(boolean thanhCong, String thongBaoLoi) {
            this.thanhCong = thanhCong;
            this.thongBaoLoi = thongBaoLoi;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBaoLoi() { return thongBaoLoi; }
    }
}
