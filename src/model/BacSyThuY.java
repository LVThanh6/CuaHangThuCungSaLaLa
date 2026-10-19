package model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

/**
 * Model đại diện cho bảng BacSyThuY trong CSDL SALALA.
 */
public class BacSyThuY implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maBacSi;
    private String maTK;
    private String tenChuyenNganh;
    private String hoTen;
    private String gioiTinh;
    private String sdt;
    private String email;
    private Date ngaySinh;

    public BacSyThuY() {
    }

    public BacSyThuY(String maBacSi, String maTK, String tenChuyenNganh, String hoTen, String gioiTinh, String sdt, String email, Date ngaySinh) {
        this.maBacSi = maBacSi;
        this.maTK = maTK;
        this.tenChuyenNganh = tenChuyenNganh;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt;
        this.email = email;
        this.ngaySinh = ngaySinh;
    }

    public String getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getTenChuyenNganh() {
        return tenChuyenNganh;
    }

    public void setTenChuyenNganh(String tenChuyenNganh) {
        this.tenChuyenNganh = tenChuyenNganh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BacSyThuY bacSyThuY = (BacSyThuY) o;
        return Objects.equals(maBacSi, bacSyThuY.maBacSi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBacSi);
    }

    @Override
    public String toString() {
        return hoTen + " (" + maBacSi + " - " + tenChuyenNganh + ")";
    }
}
