package dao;


import model.TaiKhoan;

/**
 * Lớp Session quản lý phiên làm việc của người dùng hiện tại sau khi đăng nhập thành công.
 * Cung cấp thông tin (Tài khoản, Tên, Vai trò, Mã đối tượng QL/NV/BS) cho toàn bộ 
 * các chức năng khác trong hệ thống SALALA Pet Shop.
 */
public class DangNhap_DAO {
    private static TaiKhoan taiKhoanHienTai;
    private static String tenNguoiDung;
    private static String maDoiTuong; // Mã nhân viên, mã bác sĩ hoặc mã quản lý
    private static String vaiTro;

    /**
     * Thiết lập thông tin phiên đăng nhập
     * @param tk Tài khoản vừa đăng nhập thành công
     * @param ten Tên hiển thị của người dùng (Họ và tên thực tế)
     * @param maDT Mã đối tượng tham chiếu trong CSDL (Ví dụ: NV001, BS002, QL01)
     */
    public static void dangNhap(TaiKhoan tk, String ten, String maDT) {
        taiKhoanHienTai = tk;
        tenNguoiDung = ten;
        maDoiTuong = maDT;
        vaiTro = tk != null ? tk.getVaiTro() : "";
    }

    /**
     * Xóa dữ liệu phiên làm việc khi người dùng đăng xuất
     */
    public static void dangXuat() {
        taiKhoanHienTai = null;
        tenNguoiDung = null;
        maDoiTuong = null;
        vaiTro = null;
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa
     */
    public static boolean isDaDangNhap() {
        return taiKhoanHienTai != null;
    }

    // ==========================================
    // GETTERS ĐỂ CÁC MÀN HÌNH KHÁC TRUY XUẤT
    // ==========================================
    
    public static TaiKhoan getTaiKhoanHienTai() {
        return taiKhoanHienTai;
    }

    public static String getTenNguoiDung() {
        return tenNguoiDung != null ? tenNguoiDung : "Người dùng";
    }

    public static String getMaDoiTuong() {
        return maDoiTuong != null ? maDoiTuong : "";
    }

    public static String getVaiTro() {
        return vaiTro != null ? vaiTro : "";
    }
}