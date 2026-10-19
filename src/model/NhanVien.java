package model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

/**
 * Model đại diện cho bảng NhanVien trong CSDL SALALA.
 */
public class NhanVien implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maNhanVien;
    private String maTK;
    private String hoTen;
    private String chucVu;
    private String sdt;
    private String diaChi;
    private Date ngaySinh;
    private String gioiTinh;
    private double luongCoBan;

    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String maTK, String hoTen, String chucVu, String sdt, String diaChi, Date ngaySinh, String gioiTinh, double luongCoBan) {
        this.maNhanVien = maNhanVien;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.luongCoBan = luongCoBan;
    }

    public NhanVien(String maNhanVien, String hoTen, String chucVu, String sdt, String diaChi, Date ngaySinh, String gioiTinh, double luongCoBan) {
        this(maNhanVien, null, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan);
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
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
        NhanVien nhanVien = (NhanVien) o;
        return Objects.equals(maNhanVien, nhanVien.maNhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", sdt='" + sdt + '\'' +
                ", luongCoBan=" + luongCoBan +
                '}';
    }
}
