package controller;

import dao.TaiKhoan.DangNhap_DAO;
import dao.TaiKhoan.TaiKhoan_DAO;
import entity.TaiKhoan.TaiKhoan;

public class DangNhapController {
    private final TaiKhoan_DAO taiKhoanDao;

    public DangNhapController() {
        this(new TaiKhoan_DAO());
    }

    DangNhapController(TaiKhoan_DAO taiKhoanDao) {
        this.taiKhoanDao = taiKhoanDao;
    }

    public KetQuaDangNhap dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return KetQuaDangNhap.thatBai("Vui lòng nhập tên đăng nhập!", null, null, null);
        }
        if (matKhau == null || matKhau.trim().isEmpty()) {
            return KetQuaDangNhap.thatBai("Vui lòng nhập mật khẩu!", null, null, null);
        }
        TaiKhoan_DAO.KetQuaDangNhap result = taiKhoanDao.dangNhap(tenDangNhap.trim(), matKhau);
        if (!result.isThanhCong()) {
            return KetQuaDangNhap.thatBai(result.getThongBao(), null, null, null);
        }
        TaiKhoan taiKhoan = result.getTaiKhoan();
        DangNhap_DAO.dangNhap(taiKhoan, result.getTenNguoiDung(), result.getMaDoiTuong());
        return KetQuaDangNhap.thanhCong(taiKhoan, result.getTenNguoiDung(), result.getMaDoiTuong());
    }

    public static final class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongBao;
        private final TaiKhoan taiKhoan;
        private final String tenNguoiDung, maDoiTuong;

        private KetQuaDangNhap(boolean thanhCong, String thongBao, TaiKhoan taiKhoan,
                String tenNguoiDung, String maDoiTuong) {
            this.thanhCong = thanhCong; this.thongBao = thongBao; this.taiKhoan = taiKhoan;
            this.tenNguoiDung = tenNguoiDung; this.maDoiTuong = maDoiTuong;
        }
        static KetQuaDangNhap thanhCong(TaiKhoan tk, String ten, String ma) {
            return new KetQuaDangNhap(true, null, tk, ten, ma);
        }
        static KetQuaDangNhap thatBai(String message, TaiKhoan tk, String ten, String ma) {
            return new KetQuaDangNhap(false, message, tk, ten, ma);
        }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public TaiKhoan getTaiKhoan() { return taiKhoan; }
        public String getTenNguoiDung() { return tenNguoiDung; }
        public String getMaDoiTuong() { return maDoiTuong; }
    }
}
