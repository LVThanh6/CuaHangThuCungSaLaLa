package entity;

/**
 * Lớp đại diện tài khoản trong package entity (kế thừa TaiKhoan_entity).
 * Đảm bảo tương thích hoàn toàn cho các mã nguồn sử dụng kiểu TaiKhoan hoặc TaiKhoan_entity.
 */
public class TaiKhoan extends TaiKhoan_entity {
    private static final long serialVersionUID = 1L;

    public TaiKhoan() {
        super();
    }

    public TaiKhoan(String maTK, String tenDangNhap, String matKhau, String vaiTro, boolean trangThai, String maQuanLy) {
        super(maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy);
    }
}

