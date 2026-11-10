package Service.TaiKhoan;

import connectDB.ConnectDB;
import dao.TaiKhoan.DangNhap_DAO;
import dao.TaiKhoan.TaiKhoan_DAO;
import entity.TaiKhoan.TaiKhoan;

/**
 * Service xử lý nghiệp vụ đăng nhập: kiểm tra đầu vào, xác thực (SHA-256 qua TaiKhoan_DAO),
 * lưu Session (DangNhap_DAO) và các quy tắc liên quan (mật khẩu mặc định, kết nối CSDL).
 * Không chứa bất kỳ thành phần giao diện nào.
 */
public class DangNhapService {

    /** Mật khẩu mặc định khi cấp tài khoản mới - nên khuyến nghị người dùng đổi. */
    public static final String MAT_KHAU_MAC_DINH = "123456";

    /** Ô nhập liệu GUI nên đưa con trỏ về khi đăng nhập lỗi. */
    public enum TruongNhap { TEN_DANG_NHAP, MAT_KHAU, KHONG }

    public static class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongBao;
        private final TaiKhoan taiKhoan;
        private final String tenNguoiDung;
        private final String maDoiTuong;
        private final TruongNhap truongLoi;

        public KetQuaDangNhap(boolean thanhCong, String thongBao, TaiKhoan taiKhoan,
                              String tenNguoiDung, String maDoiTuong, TruongNhap truongLoi) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.taiKhoan = taiKhoan;
            this.tenNguoiDung = tenNguoiDung;
            this.maDoiTuong = maDoiTuong;
            this.truongLoi = truongLoi;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public TaiKhoan getTaiKhoan() { return taiKhoan; }
        public String getTenNguoiDung() { return tenNguoiDung; }
        public String getMaDoiTuong() { return maDoiTuong; }
        public TruongNhap getTruongLoi() { return truongLoi; }
    }

    private final TaiKhoan_DAO taiKhoanDAO = new TaiKhoan_DAO();

    /**
     * Xác thực đăng nhập. Nếu thành công sẽ lưu Session vào DangNhap_DAO
     * (tài khoản, họ tên, mã đối tượng QL/NV/BS, vai trò).
     */
    public KetQuaDangNhap dangNhap(String tenDangNhap, String matKhau) {
        String user = tenDangNhap == null ? "" : tenDangNhap.trim();
        String pass = matKhau == null ? "" : matKhau.trim();

        if (user.isEmpty()) {
            return loi("Vui lòng nhập tên đăng nhập!", TruongNhap.TEN_DANG_NHAP);
        }
        if (pass.isEmpty()) {
            return loi("Vui lòng nhập mật khẩu!", TruongNhap.MAT_KHAU);
        }

        TaiKhoan_DAO.KetQuaDangNhap kq = taiKhoanDAO.dangNhap(user, pass);
        if (!kq.isThanhCong()) {
            return loi(kq.getThongBao(), TruongNhap.KHONG);
        }

        DangNhap_DAO.dangNhap(kq.getTaiKhoan(), kq.getTenNguoiDung(), kq.getMaDoiTuong());
        return new KetQuaDangNhap(true, kq.getThongBao(), kq.getTaiKhoan(),
                kq.getTenNguoiDung(), kq.getMaDoiTuong(), TruongNhap.KHONG);
    }

    /** Đang dùng mật khẩu mặc định => giao diện nên khuyến nghị đổi mật khẩu. */
    public boolean nenKhuyenNghiDoiMatKhau(String matKhau) {
        return matKhau != null && MAT_KHAU_MAC_DINH.equals(matKhau.trim());
    }

    /** Mở kết nối CSDL khi khởi động ứng dụng. @return true nếu kết nối thành công. */
    public boolean ketNoiCoSoDuLieu() {
        ConnectDB.getInstance().connect();
        return ConnectDB.getInstance().getConnection() != null;
    }

    /** Xóa Session khi đăng xuất. */
    public void dangXuat() {
        DangNhap_DAO.dangXuat();
    }

    private KetQuaDangNhap loi(String thongBao, TruongNhap truong) {
        return new KetQuaDangNhap(false, thongBao, null, null, null, truong);
    }
}