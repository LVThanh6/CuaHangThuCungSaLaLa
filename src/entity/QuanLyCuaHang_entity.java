package entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

/**
 * Entity đại diện cho bảng QuanLyCuaHang trong CSDL SALALA.
 */
public class QuanLyCuaHang_entity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maQuanLy;
    private TaiKhoan_entity maTK;
    private String hoTenQuanLy;
    private String sdt;
    private String diaChi;
    private Date ngaySinh;
    private String gioiTinh;
    private double luongCoBan;

    public QuanLyCuaHang_entity() {
    }

    public QuanLyCuaHang_entity(String maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public QuanLyCuaHang_entity(String maQuanLy, TaiKhoan_entity maTK, String hoTenQuanLy, String sdt, String diaChi, Date ngaySinh,
            String gioiTinh, double luongCoBan) {
        this.maQuanLy = maQuanLy;
        this.maTK = maTK;
        this.hoTenQuanLy = hoTenQuanLy;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.luongCoBan = luongCoBan;
    }

    public QuanLyCuaHang_entity(String maQuanLy, String maTK, String hoTenQuanLy, String sdt, String diaChi, Date ngaySinh,
            String gioiTinh, double luongCoBan) {
        this(maQuanLy, maTK != null ? new TaiKhoan_entity(maTK) : null, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh,
                luongCoBan);
    }

    public String getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(String maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public TaiKhoan_entity getMaTK() {
        return maTK;
    }

    public void setMaTK(TaiKhoan_entity maTK) {
        this.maTK = maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK != null ? new TaiKhoan_entity(maTK) : null;
    }

    public String getHoTenQuanLy() {
        return hoTenQuanLy;
    }

    public void setHoTenQuanLy(String hoTenQuanLy) {
        this.hoTenQuanLy = hoTenQuanLy;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public double getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(double luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        QuanLyCuaHang_entity that = (QuanLyCuaHang_entity) o;
        return Objects.equals(maQuanLy, that.maQuanLy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maQuanLy);
    }

    @Override
    public String toString() {
        return "QuanLyCuaHang{" +
                "maQuanLy='" + maQuanLy + '\'' +
                ", hoTenQuanLy='" + hoTenQuanLy + '\'' +
                '}';
    }
}
