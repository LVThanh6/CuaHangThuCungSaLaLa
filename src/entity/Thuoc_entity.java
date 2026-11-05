package entity;

import java.util.Objects;

public class Thuoc_entity {
    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;
    private String huongDanSuDung;

    public Thuoc_entity() {
    }

    public Thuoc_entity(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public Thuoc_entity(String maThuoc, String tenThuoc, String donViTinh, String huongDanSuDung) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.donViTinh = donViTinh;
        this.huongDanSuDung = huongDanSuDung;
    }

    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public String getHuongDanSuDung() { return huongDanSuDung; }
    public void setHuongDanSuDung(String huongDanSuDung) { this.huongDanSuDung = huongDanSuDung; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Thuoc_entity that = (Thuoc_entity) o;
        return Objects.equals(maThuoc, that.maThuoc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maThuoc);
    }

    @Override
    public String toString() {
        return tenThuoc != null ? tenThuoc + " (" + maThuoc + ")" : maThuoc;
    }
}
