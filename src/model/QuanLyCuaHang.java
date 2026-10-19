package model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

/**
 * Model đại diện cho bảng QuanLyCuaHang trong CSDL SALALA.
 */
public class QuanLyCuaHang implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maQuanLy;
    private String maTK;
    private String hoTenQuanLy;
    private String sdt;
    private String diaChi;
    private Date ngaySinh;
    private String gioiTinh;
    private double luongCoBan;

    public QuanLyCuaHang() {
    }

    public QuanLyCuaHang(String maQuanLy, String maTK, String hoTenQuanLy, String sdt, String diaChi, Date ngaySinh, String gioiTinh, double luongCoBan) {
        this.maQuanLy = maQuanLy;
        this.maTK = maTK;
        this.hoTenQuanLy = hoTenQuanLy;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.luongCoBan = luongCoBan;
    }

    public String getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(String maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuanLyCuaHang that = (QuanLyCuaHang) o;
        return Objects.equals(maQuanLy, that.maQuanLy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maQuanLy);
    }

    @Override
    public String toString() {
        return hoTenQuanLy + " (" + maQuanLy + ")";
    }
}
