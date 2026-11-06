package controller;

import dao.TaiKhoan.TaiKhoan_DAO;
import entity.TaiKhoan.TaiKhoan;

public class DoiMatKhauController {
    private final TaiKhoan_DAO taiKhoanDao;

    public DoiMatKhauController() {
        this(new TaiKhoan_DAO());
    }

    DoiMatKhauController(TaiKhoan_DAO taiKhoanDao) {
        this.taiKhoanDao = taiKhoanDao;
    }

    public KetQuaDoiMatKhau doiMatKhau(TaiKhoan taiKhoan, String matKhauCu,
            String matKhauMoi, String xacNhanMatKhau) {
        if (taiKhoan == null) return new KetQuaDoiMatKhau(false, "Không tìm thấy thông tin tài khoản hợp lệ!", TruongNhap.TAI_KHOAN);
        if (matKhauCu.isEmpty()) return new KetQuaDoiMatKhau(false, "Vui lòng nhập mật khẩu hiện tại!", TruongNhap.MAT_KHAU_CU);
        if (matKhauMoi.isEmpty()) return new KetQuaDoiMatKhau(false, "Vui lòng nhập mật khẩu mới!", TruongNhap.MAT_KHAU_MOI);
        if (matKhauMoi.length() < 6) return new KetQuaDoiMatKhau(false, "Mật khẩu mới phải có ít nhất 6 ký tự!", TruongNhap.MAT_KHAU_MOI);
        if (!matKhauMoi.equals(xacNhanMatKhau)) return new KetQuaDoiMatKhau(false, "Mật khẩu xác nhận không khớp!", TruongNhap.XAC_NHAN);

        String oldHash = TaiKhoan_DAO.hashPassword(matKhauCu);
        if (!oldHash.equalsIgnoreCase(taiKhoan.getMatKhau()) && !matKhauCu.equals(taiKhoan.getMatKhau())) {
            return new KetQuaDoiMatKhau(false, "Mật khẩu hiện tại không chính xác!", TruongNhap.MAT_KHAU_CU);
        }
        if (!taiKhoanDao.doiMatKhau(taiKhoan.getMaTK(), matKhauMoi)) {
            return new KetQuaDoiMatKhau(false, "Không thể cập nhật mật khẩu vào CSDL!", TruongNhap.KHONG_CO);
        }
        taiKhoan.setMatKhau(TaiKhoan_DAO.hashPassword(matKhauMoi));
        return new KetQuaDoiMatKhau(true, null, TruongNhap.KHONG_CO);
    }

    public enum TruongNhap { TAI_KHOAN, MAT_KHAU_CU, MAT_KHAU_MOI, XAC_NHAN, KHONG_CO }

    public static final class KetQuaDoiMatKhau {
        private final boolean thanhCong;
        private final String thongBao;
        private final TruongNhap truongNhap;
        private KetQuaDoiMatKhau(boolean thanhCong, String thongBao, TruongNhap truongNhap) {
            this.thanhCong = thanhCong; this.thongBao = thongBao; this.truongNhap = truongNhap;
        }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public TruongNhap getTruongNhap() { return truongNhap; }
    }
}
