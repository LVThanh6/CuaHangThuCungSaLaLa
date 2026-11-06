package controller;

import dao.NhanVien.NhanVien_DAO;
import entity.NhanVien.NhanVien_entity;

import java.sql.SQLException;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NhanVienController {
    private static final String[] CHUC_VU = {
            "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Quản lý"
    };
    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final NhanVien_DAO nhanVienDao;

    public NhanVienController() {
        this(new NhanVien_DAO());
    }

    NhanVienController(NhanVien_DAO nhanVienDao) {
        this.nhanVienDao = nhanVienDao;
    }

    public List<NhanVien_entity> taiDanhSach() throws SQLException {
        return nhanVienDao.getAll();
    }

    public String taoMaMoi() throws SQLException {
        return nhanVienDao.getMaTiepTheo();
    }

    public NhanVien_entity timTheoMa(String maNhanVien) throws SQLException {
        return nhanVienDao.getNhanVienTheoMa(maNhanVien);
    }

    public boolean doiTrangThai(String maNhanVien, boolean hoatDong) throws SQLException {
        return nhanVienDao.doiTrangThaiTaiKhoan(maNhanVien, hoatDong);
    }

    public KetQuaXoa xoa(String maNhanVien) throws SQLException {
        try {
            return new KetQuaXoa(nhanVienDao.xoa(maNhanVien), false);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) return new KetQuaXoa(false, true);
            throw ex;
        }
    }

    public KetQuaLuu validateAndSave(ThongTinNhanVien input) throws SQLException {
        if (input.hoTen.isEmpty()) return new KetQuaLuu(false, "Vui lòng nhập họ và tên.");
        if (!input.soDienThoai.matches("0\\d{9}")) return new KetQuaLuu(false, "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.");
        if (!input.ngaySinhText.isEmpty() && input.ngaySinh == null) return new KetQuaLuu(false, "Ngày sinh không hợp lệ (dd/MM/yyyy).");
        if (input.ngaySinh != null && input.ngaySinh.isAfter(LocalDate.now())) return new KetQuaLuu(false, "Ngày sinh không được ở tương lai.");
        if (input.luongCoBan < 0) return new KetQuaLuu(false, "Lương cơ bản không được âm.");

        NhanVien_entity nhanVien = new NhanVien_entity(
                input.maNhanVien,
                input.nhanVienHienTai == null ? null : input.nhanVienHienTai.getMaTK(),
                input.hoTen,
                input.chucVu,
                input.soDienThoai,
                input.diaChi,
                input.ngaySinh,
                input.gioiTinh,
                input.luongCoBan,
                input.nhanVienHienTai == null ? null : input.nhanVienHienTai.getTrangThaiTK());
        boolean saved = input.nhanVienHienTai == null ? nhanVienDao.them(nhanVien) : nhanVienDao.capNhat(nhanVien);
        if (saved && input.capNhatTrangThaiTaiKhoan) {
            saved = nhanVienDao.doiTrangThaiTaiKhoan(input.maNhanVien, input.trangThaiTaiKhoan);
        }
        return new KetQuaLuu(saved, saved ? null : "Không thể lưu thông tin nhân viên vào CSDL.");
    }

    public KetQuaLuu validateAndSave(String maNhanVien, NhanVien_entity current, String hoTen,
            String soDienThoai, String diaChi, String ngaySinhText, String gioiTinh,
            String chucVu, String luongText, boolean capNhatTrangThai, boolean trangThaiTaiKhoan)
            throws SQLException {
        if (hoTen.trim().isEmpty()) return new KetQuaLuu(false, "Vui lòng nhập họ và tên.");
        String normalizedPhone = soDienThoai.replaceAll("\\s", "");
        if (!normalizedPhone.matches("0\\d{9}")) return new KetQuaLuu(false, "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.");
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
            return new KetQuaLuu(false, "Lương cơ bản không hợp lệ.");
        }
        return validateAndSave(new ThongTinNhanVien(maNhanVien, current, hoTen.trim(),
                chucVu, normalizedPhone, diaChi.trim(), birthText, birth, gioiTinh,
                salary, capNhatTrangThai, trangThaiTaiKhoan));
    }

    public List<NhanVien_entity> locDanhSach(List<NhanVien_entity> danhSach, String tuKhoa,
            String chucVu, String trangThai, boolean coSapXep, boolean tangDan) {
        String query = boDau(tuKhoa.trim());
        String querySo = query.replace(" ", "");
        List<NhanVien_entity> ketQua = new ArrayList<>();
        for (NhanVien_entity nhanVien : danhSach) {
            if (chucVu != null && !chucVu.isEmpty() && !chucVu.equals(nhanVien.getChucVu())) continue;
            if (trangThai != null && !trangThai.isEmpty()
                    && !trangThai.equals(trangThai(nhanVien))) continue;
            if (!query.isEmpty()
                    && !boDau(nhanVien.getHoTen()).contains(query)
                    && !boDau(nhanVien.getMaNhanVien()).contains(query)
                    && !boDau(nhanVien.getDiaChi() == null ? "" : nhanVien.getDiaChi()).contains(query)
                    && !(nhanVien.getSdt() == null ? "" : nhanVien.getSdt()).replace(" ", "").contains(querySo)) continue;
            ketQua.add(nhanVien);
        }
        if (coSapXep) {
            Collator collator = Collator.getInstance(VI);
            ketQua.sort((a, b) -> {
                int result = collator.compare(tenGoi(a.getHoTen()), tenGoi(b.getHoTen()));
                if (result == 0) result = collator.compare(a.getHoTen(), b.getHoTen());
                return tangDan ? result : -result;
            });
        }
        return ketQua;
    }

    public ThongKe thongKe(List<NhanVien_entity> danhSach) {
        int dangLam = 0, ngung = 0, chuaTaiKhoan = 0;
        for (NhanVien_entity nhanVien : danhSach) {
            if (nhanVien.getTrangThaiTK() == null) chuaTaiKhoan++;
            else if (nhanVien.getTrangThaiTK()) dangLam++;
            else ngung++;
        }
        return new ThongKe(danhSach.size(), dangLam, ngung, chuaTaiKhoan);
    }

    public String taoNoiDungCsv(List<NhanVien_entity> danhSach) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Mã nhân viên,Họ và tên,Chức vụ,Số điện thoại,Địa chỉ,Ngày sinh,Giới tính,Trạng thái tài khoản\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        for (NhanVien_entity nhanVien : danhSach) {
            csv.append(csv(nhanVien.getMaNhanVien())).append(',')
                    .append(csv(nz(nhanVien.getHoTen()))).append(',')
                    .append(csv(nz(nhanVien.getChucVu()))).append(',')
                    .append(csv("'" + nz(nhanVien.getSdt()).replace(" ", ""))).append(',')
                    .append(csv(nz(nhanVien.getDiaChi()))).append(',')
                    .append(csv(nhanVien.getNgaySinh() == null ? "" : formatter.format(nhanVien.getNgaySinh()))).append(',')
                    .append(csv(nz(nhanVien.getGioiTinh()))).append(',')
                    .append(csv(trangThaiNhan(nhanVien))).append('\n');
        }
        return csv.toString();
    }

    private static String trangThaiNhan(NhanVien_entity nhanVien) {
        if (nhanVien.getTrangThaiTK() == null) return "Chưa có tài khoản";
        return nhanVien.getTrangThaiTK() ? "Đang làm việc" : "Ngừng hoạt động";
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String nz(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public double tinhTongLuongThang(NhanVien_entity nhanVien, LocalDate homNay) throws SQLException {
        if (nhanVien == null) return Double.NaN;
        LocalDate dauThang = homNay.withDayOfMonth(1);
        String chucVu = nhanVien.getChucVu() == null ? "" : nhanVien.getChucVu();
        double luongCoBan;
        if ("Nhân viên bán hàng".equals(chucVu) || "Nhân viên dịch vụ".equals(chucVu)) {
            luongCoBan = nhanVienDao.getSoGioLamViecCuaNhanVien(nhanVien.getMaNhanVien(), dauThang, homNay)
                    * nhanVien.getLuongCoBan();
        } else {
            luongCoBan = nhanVien.getLuongCoBan() * homNay.getDayOfMonth() / homNay.lengthOfMonth();
        }
        double hoaHong = "Nhân viên dịch vụ".equals(chucVu)
                ? nhanVienDao.getDoanhThuDichVuCuaNhanVien(nhanVien.getMaNhanVien(), dauThang, homNay) * 0.05
                : 0;
        return luongCoBan + hoaHong;
    }

    public static double luongTheoChucVu(String chucVu) {
        if ("Nhân viên dịch vụ".equals(chucVu)) return 60000;
        if ("Nhân viên quản lý kho".equals(chucVu)) return 13500000;
        if ("Quản lý".equals(chucVu)) return 15000000;
        return 45000;
    }

    private String trangThai(NhanVien_entity nhanVien) {
        if (nhanVien.getTrangThaiTK() == null) return "CHUA_TK";
        return nhanVien.getTrangThaiTK() ? "DANG_LAM" : "NGUNG";
    }

    private static String boDau(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(VI), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replace('đ', 'd');
    }

    private static String tenGoi(String ten) {
        int index = ten.lastIndexOf(' ');
        return index < 0 ? ten : ten.substring(index + 1);
    }

    public static final class ThongTinNhanVien {
        public final String maNhanVien, hoTen, chucVu, soDienThoai, diaChi, ngaySinhText, gioiTinh;
        public final LocalDate ngaySinh;
        public final double luongCoBan;
        public final NhanVien_entity nhanVienHienTai;
        public final boolean capNhatTrangThaiTaiKhoan, trangThaiTaiKhoan;

        public ThongTinNhanVien(String maNhanVien, NhanVien_entity nhanVienHienTai, String hoTen,
                String chucVu, String soDienThoai, String diaChi, String ngaySinhText,
                LocalDate ngaySinh, String gioiTinh, double luongCoBan,
                boolean capNhatTrangThaiTaiKhoan, boolean trangThaiTaiKhoan) {
            this.maNhanVien = maNhanVien; this.nhanVienHienTai = nhanVienHienTai; this.hoTen = hoTen;
            this.chucVu = chucVu; this.soDienThoai = soDienThoai; this.diaChi = diaChi;
            this.ngaySinhText = ngaySinhText; this.ngaySinh = ngaySinh; this.gioiTinh = gioiTinh;
            this.luongCoBan = luongCoBan; this.capNhatTrangThaiTaiKhoan = capNhatTrangThaiTaiKhoan;
            this.trangThaiTaiKhoan = trangThaiTaiKhoan;
        }
    }

    public static final class KetQuaLuu {
        private final boolean thanhCong;
        private final String thongBaoLoi;
        private KetQuaLuu(boolean thanhCong, String thongBaoLoi) {
            this.thanhCong = thanhCong; this.thongBaoLoi = thongBaoLoi;
        }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongBaoLoi() { return thongBaoLoi; }
    }

    public static final class ThongKe {
        public final int tongSo, dangLam, ngung, chuaTaiKhoan;
        private ThongKe(int tongSo, int dangLam, int ngung, int chuaTaiKhoan) {
            this.tongSo = tongSo; this.dangLam = dangLam; this.ngung = ngung; this.chuaTaiKhoan = chuaTaiKhoan;
        }
    }

    public static final class KetQuaXoa {
        public final boolean thanhCong, coDuLieuLienQuan;
        private KetQuaXoa(boolean thanhCong, boolean coDuLieuLienQuan) {
            this.thanhCong = thanhCong; this.coDuLieuLienQuan = coDuLieuLienQuan;
        }
    }
}
