package entity;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Lớp đại diện quản lý cửa hàng trong package entity (kế thừa QuanLyCuaHang_entity).
 */
public class QuanLyCuaHang extends QuanLyCuaHang_entity {
    private static final long serialVersionUID = 1L;

    public QuanLyCuaHang() {
        super();
    }

    public QuanLyCuaHang(String maQuanLy, String maTK, String hoTenQuanLy, String sdt, String diaChi, LocalDate ngaySinh, String gioiTinh, double luongCoBan) {
        super(maQuanLy, maTK, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan);
    }

    public QuanLyCuaHang(String maQuanLy, String maTK, String hoTenQuanLy, String sdt, String diaChi, Date ngaySinh, String gioiTinh, double luongCoBan) {
        super(maQuanLy, maTK, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan);
    }
}

