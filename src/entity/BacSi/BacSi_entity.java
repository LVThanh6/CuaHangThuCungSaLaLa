package entity.BacSi;

import entity.TaiKhoan.TaiKhoan;

import java.time.LocalDate;
import java.util.Objects;

public class BacSi_entity {
    private String maBacSi;
    private TaiKhoan maTK;
    private String hoTen;
    private String gioiTinh;
    private String sdt;
    private ChuyenNganh_entity chuyenNganh;
    private String email;
    private LocalDate ngaySinh;
    private double luongThang;
    private int soCaKham;
    private Boolean trangThaiTK; // true: Đang làm việc, false: Tạm nghỉ, null: Chưa có tài khoản

    public BacSi_entity() {
        super();
    }

    public BacSi_entity(String maBacSi) {
        this.maBacSi = maBacSi;
    }

    public BacSi_entity(String maBacSi, TaiKhoan maTK, String hoTen, String gioiTinh, String sdt,
            ChuyenNganh_entity chuyenNganh, String email, LocalDate ngaySinh) {
        this(maBacSi, maTK, hoTen, gioiTinh, sdt, chuyenNganh, email, ngaySinh, 0, 0, null);
    }

    public BacSi_entity(String maBacSi, TaiKhoan maTK, String hoTen, String gioiTinh, String sdt,
            ChuyenNganh_entity chuyenNganh, String email, LocalDate ngaySinh, int soCaKham, Boolean trangThaiTK) {
        this(maBacSi, maTK, hoTen, gioiTinh, sdt, chuyenNganh, email, ngaySinh, 0, soCaKham, trangThaiTK);
    }

    public BacSi_entity(String maBacSi, TaiKhoan maTK, String hoTen, String gioiTinh, String sdt,
            ChuyenNganh_entity chuyenNganh, String email, LocalDate ngaySinh, double luongThang,
            int soCaKham, Boolean trangThaiTK) {
        super();
        this.maBacSi = maBacSi;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt;
        this.chuyenNganh = chuyenNganh;
        this.email = email;
        this.ngaySinh = ngaySinh;
        this.luongThang = luongThang;
        this.soCaKham = soCaKham;
        this.trangThaiTK = trangThaiTK;
    }

    public BacSi_entity(String maBacSi, String maTK, String hoTen, String gioiTinh, String sdt,
            String chuyenNganh, String email, LocalDate ngaySinh, double luongThang,
            int soCaKham, Boolean trangThaiTK) {
        this(maBacSi, maTK != null ? new TaiKhoan(maTK) : null, hoTen, gioiTinh, sdt,
                chuyenNganh != null ? new ChuyenNganh_entity(chuyenNganh, chuyenNganh) : null,
                email, ngaySinh, luongThang, soCaKham, trangThaiTK);
    }

    public String getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi;
    }

    public TaiKhoan getMaTK() {
        return maTK;
    }

    public void setMaTK(TaiKhoan maTK) {
        this.maTK = maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK != null ? new TaiKhoan(maTK) : null;
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

    public ChuyenNganh_entity getChuyenNganh() {
        return chuyenNganh;
    }

    public void setChuyenNganh(ChuyenNganh_entity chuyenNganh) {
        this.chuyenNganh = chuyenNganh;
    }

    public void setChuyenNganh(String chuyenNganh) {
        this.chuyenNganh = chuyenNganh != null ? new ChuyenNganh_entity(chuyenNganh, chuyenNganh) : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public double getLuongThang() {
        return luongThang;
    }

    public void setLuongThang(double luongThang) {
        this.luongThang = luongThang;
    }

    public int getSoCaKham() {
        return soCaKham;
    }

    public void setSoCaKham(int soCaKham) {
        this.soCaKham = soCaKham;
    }

    public Boolean getTrangThaiTK() {
        return trangThaiTK;
    }

    public void setTrangThaiTK(Boolean trangThaiTK) {
        this.trangThaiTK = trangThaiTK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BacSi_entity that = (BacSi_entity) o;
        return Objects.equals(maBacSi, that.maBacSi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBacSi);
    }

    @Override
    public String toString() {
        return hoTen != null ? hoTen + " (" + maBacSi + ")" : (maBacSi != null ? maBacSi : "");
    }
}
