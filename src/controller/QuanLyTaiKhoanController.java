package controller;

import dao.TaiKhoan.TaiKhoan_DAO;
import dao.TaiKhoan.TaiKhoan_DAO.TaiKhoanChiTiet;
import entity.TaiKhoan.TaiKhoan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuanLyTaiKhoanController {
    private final TaiKhoan_DAO taiKhoanDao;

    public QuanLyTaiKhoanController() {
        this(new TaiKhoan_DAO());
    }

    QuanLyTaiKhoanController(TaiKhoan_DAO taiKhoanDao) {
        this.taiKhoanDao = taiKhoanDao;
    }

    public DuLieuDanhSach taiDanhSach() {
        List<TaiKhoanChiTiet> records = taiKhoanDao.getAllTaiKhoanChiTiet();
        List<TaiKhoanItem> danhSach = new ArrayList<>();
        int hoatDong = 0, biKhoa = 0, quanLy = 0, nhanVien = 0, bacSi = 0;
        for (TaiKhoanChiTiet record : records) {
            TaiKhoanItem item = new TaiKhoanItem(record);
            danhSach.add(item);
            if (item.isTrangThai()) hoatDong++;
            else biKhoa++;
            String role = item.getVaiTro();
            if (role != null && (role.equalsIgnoreCase("Quản lý") || role.equalsIgnoreCase("Admin"))) quanLy++;
            else if (role != null && role.toLowerCase().contains("bác sĩ")) bacSi++;
            else nhanVien++;
        }
        return new DuLieuDanhSach(danhSach, hoatDong, biKhoa, quanLy, nhanVien, bacSi);
    }

    public String taoMaMoi() {
        return taiKhoanDao.phatSinhMaTK();
    }

    public DanhMucTaoTaiKhoan taiDanhMucTaoTaiKhoan() {
        return new DanhMucTaoTaiKhoan(
                chuyenDoi(taiKhoanDao.getNhanVienChuaCoTaiKhoan(), LoaiDoiTuong.NHAN_VIEN),
                chuyenDoi(taiKhoanDao.getBacSiChuaCoTaiKhoan(), LoaiDoiTuong.BAC_SI),
                chuyenDoiQuanLy(taiKhoanDao.getDanhSachQuanLy()));
    }

    public List<QuanLyOption> taiDanhSachQuanLy() {
        return chuyenDoiQuanLy(taiKhoanDao.getDanhSachQuanLy());
    }

    public String vaiTroGoiY(DoiTuongOption doiTuong) {
        return doiTuong.loai == LoaiDoiTuong.BAC_SI ? "Bác sĩ" : doiTuong.vaiTro;
    }

    public KetQuaTaoTaiKhoan taoTaiKhoan(ThongTinTaiKhoanMoi input) {
        String username = input.tenDangNhap.trim();
        if (username.isEmpty()) return new KetQuaTaoTaiKhoan(false, "Vui lòng nhập tên đăng nhập!");
        if (taiKhoanDao.isTenDangNhapTonTai(username, null)) {
            return new KetQuaTaoTaiKhoan(false, "Tên đăng nhập '" + username + "' đã tồn tại! Vui lòng chọn tên khác.");
        }
        String password = input.matKhau.trim().isEmpty() ? "123456" : input.matKhau.trim();
        TaiKhoan taiKhoan = new TaiKhoan(input.maTaiKhoan, username, password, input.vaiTro,
                input.trangThai, input.maQuanLy);
        boolean saved = taiKhoanDao.themTaiKhoan(taiKhoan,
                input.doiTuongLienKet == null ? null : input.doiTuongLienKet.loai.toDaoValue(),
                input.doiTuongLienKet == null ? null : input.doiTuongLienKet.ma);
        return new KetQuaTaoTaiKhoan(saved, saved ? null : "Không thể lưu tài khoản vào CSDL!");
    }

    public boolean capNhatTaiKhoan(String maTaiKhoan, String username, String role,
            boolean trangThai, String maQuanLy) {
        return taiKhoanDao.capNhatTaiKhoan(new TaiKhoan(maTaiKhoan, username, null, role, trangThai, maQuanLy));
    }

    public boolean datTrangThai(String maTaiKhoan, boolean trangThai) {
        return taiKhoanDao.datTrangThaiTaiKhoan(maTaiKhoan, trangThai);
    }

    public KetQuaMatKhau datLaiMatKhau(String maTaiKhoan, String matKhauMoi) {
        if (matKhauMoi == null) return new KetQuaMatKhau(false, "CANCELLED");
        String password = matKhauMoi.trim();
        if (password.isEmpty()) return new KetQuaMatKhau(false, "Mật khẩu không được để trống!");
        if (password.length() < 6) return new KetQuaMatKhau(false, "Mật khẩu phải từ 6 ký tự trở lên!");
        boolean saved = taiKhoanDao.doiMatKhau(maTaiKhoan, password);
        return new KetQuaMatKhau(saved, saved ? null : "Không thể cập nhật mật khẩu!");
    }

    public boolean xoaTaiKhoan(String maTaiKhoan) {
        return taiKhoanDao.xoaTaiKhoan(maTaiKhoan);
    }

    private List<DoiTuongOption> chuyenDoi(List<Map<String, String>> raw, LoaiDoiTuong loai) {
        List<DoiTuongOption> options = new ArrayList<>();
        for (Map<String, String> item : raw) {
            options.add(new DoiTuongOption(loai, item.get("ma"), item.get("ten"), item.get("vaiTro")));
        }
        return options;
    }

    private List<QuanLyOption> chuyenDoiQuanLy(List<Map<String, String>> raw) {
        List<QuanLyOption> options = new ArrayList<>();
        for (Map<String, String> item : raw) {
            options.add(new QuanLyOption(item.get("maQuanLy"), item.get("hoTen")));
        }
        return options;
    }

    public enum LoaiDoiTuong {
        NHAN_VIEN("NhanVien"), BAC_SI("BacSy");
        private final String daoValue;
        LoaiDoiTuong(String daoValue) { this.daoValue = daoValue; }
        String toDaoValue() { return daoValue; }
    }

    public static final class DoiTuongOption {
        public final LoaiDoiTuong loai;
        public final String ma, ten, vaiTro;
        private DoiTuongOption(LoaiDoiTuong loai, String ma, String ten, String vaiTro) {
            this.loai = loai; this.ma = ma; this.ten = ten; this.vaiTro = vaiTro;
        }
    }

    public static final class QuanLyOption {
        public final String ma, hoTen;
        private QuanLyOption(String ma, String hoTen) { this.ma = ma; this.hoTen = hoTen; }
    }

    public static final class DanhMucTaoTaiKhoan {
        public final List<DoiTuongOption> nhanVien, bacSi;
        public final List<QuanLyOption> quanLy;
        private DanhMucTaoTaiKhoan(List<DoiTuongOption> nv, List<DoiTuongOption> bs, List<QuanLyOption> ql) {
            nhanVien = nv; bacSi = bs; quanLy = ql;
        }
    }

    public static final class DuLieuDanhSach {
        public final List<TaiKhoanItem> taiKhoan;
        public final int hoatDong, biKhoa, quanLy, nhanVien, bacSi;
        private DuLieuDanhSach(List<TaiKhoanItem> taiKhoan, int hoatDong, int biKhoa, int quanLy, int nhanVien, int bacSi) {
            this.taiKhoan = taiKhoan; this.hoatDong = hoatDong; this.biKhoa = biKhoa;
            this.quanLy = quanLy; this.nhanVien = nhanVien; this.bacSi = bacSi;
        }
    }

    public static final class TaiKhoanItem {
        private final TaiKhoanChiTiet source;
        private TaiKhoanItem(TaiKhoanChiTiet source) { this.source = source; }
        public String getMaTK() { return source.getMaTK(); }
        public String getTenDangNhap() { return source.getTenDangNhap(); }
        public String getVaiTro() { return source.getVaiTro(); }
        public boolean isTrangThai() { return source.isTrangThai(); }
        public String getMaQuanLy() { return source.getMaQuanLy(); }
        public String getTenQuanLy() { return source.getTenQuanLy(); }
        public String getHoTenNguoiDung() { return source.getHoTenNguoiDung(); }
        public String getMaDoiTuong() { return source.getMaDoiTuong(); }
        public String getSdtNguoiDung() { return source.getSdtNguoiDung(); }
    }

    public static final class ThongTinTaiKhoanMoi {
        public final String maTaiKhoan, tenDangNhap, matKhau, vaiTro, maQuanLy;
        public final boolean trangThai;
        public final DoiTuongOption doiTuongLienKet;
        public ThongTinTaiKhoanMoi(String maTaiKhoan, String tenDangNhap, String matKhau, String vaiTro,
                boolean trangThai, String maQuanLy, DoiTuongOption doiTuongLienKet) {
            this.maTaiKhoan = maTaiKhoan; this.tenDangNhap = tenDangNhap; this.matKhau = matKhau;
            this.vaiTro = vaiTro; this.trangThai = trangThai; this.maQuanLy = maQuanLy;
            this.doiTuongLienKet = doiTuongLienKet;
        }
    }

    public static final class KetQuaTaoTaiKhoan {
        public final boolean thanhCong;
        public final String thongBao;
        private KetQuaTaoTaiKhoan(boolean thanhCong, String thongBao) {
            this.thanhCong = thanhCong; this.thongBao = thongBao;
        }
    }

    public static final class KetQuaMatKhau {
        public final boolean thanhCong;
        public final String thongBao;
        private KetQuaMatKhau(boolean thanhCong, String thongBao) {
            this.thanhCong = thanhCong; this.thongBao = thongBao;
        }
    }
}
