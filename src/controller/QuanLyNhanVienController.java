package controller;

import dao.NhanVien.NhanVien_DAO;
import entity.NhanVien.NhanVien_entity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class QuanLyNhanVienController {
    private final NhanVien_DAO nhanVienDao;

    public QuanLyNhanVienController() {
        this(new NhanVien_DAO());
    }

    QuanLyNhanVienController(NhanVien_DAO nhanVienDao) {
        this.nhanVienDao = nhanVienDao;
    }

    public List<NhanVien_entity> taiDanhSach() throws SQLException {
        return new ArrayList<>(nhanVienDao.getAll());
    }

    public String taoMaMoi() throws SQLException {
        return nhanVienDao.getMaTiepTheo();
    }

    public NhanVien_entity timTheoMa(String maNhanVien) throws SQLException {
        return nhanVienDao.getNhanVienTheoMa(maNhanVien);
    }

    public KetQuaLuu validateAndCreate(ThongTinNhanVien input) throws SQLException {
        KetQuaLuu validation = validate(input);
        if (!validation.isThanhCong()) return validation;
        NhanVien_entity nhanVien = new NhanVien_entity(
                input.maNhanVien, null, input.hoTen, input.chucVu, input.soDienThoai,
                input.diaChi, input.ngaySinh, input.gioiTinh,
                NhanVienController.luongTheoChucVu(input.chucVu));
        boolean saved = nhanVienDao.them(nhanVien);
        return new KetQuaLuu(saved, saved ? null : "Không thể lưu nhân viên vào cơ sở dữ liệu.");
    }

    public KetQuaLuu validateAndCreate(String maNhanVien, String hoTen, String chucVu,
            String soDienThoai, String gioiTinh, String ngaySinhText, String diaChi) throws SQLException {
        if (hoTen.trim().isEmpty()) return new KetQuaLuu(false, "Họ và tên nhân viên không được để trống!");
        if (!soDienThoai.trim().matches("^0\\d{9}$")) return new KetQuaLuu(false, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        LocalDate birth;
        try {
            birth = LocalDate.parse(ngaySinhText.trim());
        } catch (DateTimeParseException ex) {
            return new KetQuaLuu(false, "Ngày sinh phải theo định dạng yyyy-MM-dd!");
        }
        return validateAndCreate(new ThongTinNhanVien(maNhanVien, null, hoTen.trim(), chucVu,
                soDienThoai.trim(), gioiTinh, birth, diaChi.trim()));
    }

    public KetQuaLuu validateAndUpdate(ThongTinNhanVien input) throws SQLException {
        if (input.hoTen.isEmpty()) return new KetQuaLuu(false, "Họ và tên nhân viên không được để trống!");
        NhanVien_entity nhanVien = input.nhanVienHienTai;
        nhanVien.setHoTen(input.hoTen);
        nhanVien.setChucVu(input.chucVu);
        nhanVien.setSdt(input.soDienThoai);
        nhanVien.setGioiTinh(input.gioiTinh);
        nhanVien.setNgaySinh(input.ngaySinh);
        nhanVien.setDiaChi(input.diaChi);
        nhanVien.setLuongCoBan(NhanVienController.luongTheoChucVu(input.chucVu));
        boolean saved = nhanVienDao.capNhat(nhanVien);
        return new KetQuaLuu(saved, saved ? null : "Không thể cập nhật nhân viên trong cơ sở dữ liệu.");
    }

    public KetQuaLuu validateAndUpdate(NhanVien_entity current, String hoTen, String chucVu,
            String soDienThoai, String gioiTinh, String ngaySinhText, String diaChi) throws SQLException {
        if (hoTen.trim().isEmpty()) return new KetQuaLuu(false, "Họ và tên nhân viên không được để trống!");
        LocalDate birth;
        try {
            birth = LocalDate.parse(ngaySinhText.trim());
        } catch (DateTimeParseException ex) {
            return new KetQuaLuu(false, "Ngày sinh phải theo định dạng yyyy-MM-dd!");
        }
        return validateAndUpdate(new ThongTinNhanVien(current.getMaNhanVien(), current, hoTen.trim(),
                chucVu, soDienThoai.trim(), gioiTinh, birth, diaChi.trim()));
    }

    public boolean xoa(String maNhanVien) throws SQLException {
        return nhanVienDao.xoa(maNhanVien);
    }

    private KetQuaLuu validate(ThongTinNhanVien input) {
        if (input.hoTen.isEmpty()) return new KetQuaLuu(false, "Họ và tên nhân viên không được để trống!");
        if (!input.soDienThoai.matches("^0\\d{9}$")) return new KetQuaLuu(false, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        if (input.ngaySinh == null) return new KetQuaLuu(false, "Ngày sinh phải theo định dạng yyyy-MM-dd!");
        return new KetQuaLuu(true, null);
    }

    public static final class ThongTinNhanVien {
        public final String maNhanVien, hoTen, chucVu, soDienThoai, gioiTinh, diaChi;
        public final LocalDate ngaySinh;
        public final NhanVien_entity nhanVienHienTai;

        public ThongTinNhanVien(String maNhanVien, NhanVien_entity nhanVienHienTai,
                String hoTen, String chucVu, String soDienThoai, String gioiTinh,
                LocalDate ngaySinh, String diaChi) {
            this.maNhanVien = maNhanVien;
            this.nhanVienHienTai = nhanVienHienTai;
            this.hoTen = hoTen;
            this.chucVu = chucVu;
            this.soDienThoai = soDienThoai;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.diaChi = diaChi;
        }
    }

    public static final class KetQuaLuu {
        private final boolean thanhCong;
        private final String thongBao;
        private KetQuaLuu(boolean thanhCong, String thongBao) {
            this.thanhCong = thanhCong; this.thongBao = thongBao;
        }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
    }
}
