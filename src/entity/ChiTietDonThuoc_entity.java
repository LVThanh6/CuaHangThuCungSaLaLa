package entity;

import java.math.BigDecimal;
import java.util.Objects;

public class ChiTietDonThuoc_entity {
    private String maCTDT;
    private DonThuoc_entity maDonThuoc;
    private Thuoc_entity maThuoc;
    private String tenThuoc;
    private String donViTinh;
    private BigDecimal soLuong;
    private BigDecimal lieuLuong;

    public ChiTietDonThuoc_entity() {
    }

    public ChiTietDonThuoc_entity(String maCTDT) {
        this.maCTDT = maCTDT;
    }

    public ChiTietDonThuoc_entity(String maCTDT, DonThuoc_entity maDonThuoc, Thuoc_entity maThuoc,
            BigDecimal soLuong, BigDecimal lieuLuong) {
        this.maCTDT = maCTDT;
        this.maDonThuoc = maDonThuoc;
        this.maThuoc = maThuoc;
        this.soLuong = soLuong;
        this.lieuLuong = lieuLuong;
    }

    public ChiTietDonThuoc_entity(String maCTDT, String maDonThuoc, String maThuoc,
            BigDecimal soLuong, BigDecimal lieuLuong) {
        this(maCTDT,
             maDonThuoc != null ? new DonThuoc_entity(maDonThuoc) : null,
             maThuoc != null ? new Thuoc_entity(maThuoc) : null,
             soLuong, lieuLuong);
    }

    public String getMaCTDT() { return maCTDT; }
    public void setMaCTDT(String maCTDT) { this.maCTDT = maCTDT; }

    public DonThuoc_entity getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(DonThuoc_entity maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc != null ? new DonThuoc_entity(maDonThuoc) : null;
    }

    public Thuoc_entity getMaThuoc() { return maThuoc; }
    public void setMaThuoc(Thuoc_entity maThuoc) { this.maThuoc = maThuoc; }
    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc != null ? new Thuoc_entity(maThuoc) : null;
    }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }

    public BigDecimal getLieuLuong() { return lieuLuong; }
    public void setLieuLuong(BigDecimal lieuLuong) { this.lieuLuong = lieuLuong; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietDonThuoc_entity that = (ChiTietDonThuoc_entity) o;
        return Objects.equals(maCTDT, that.maCTDT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maCTDT);
    }

    @Override
    public String toString() {
        return "ChiTietDonThuoc{" +
                "maCTDT='" + maCTDT + '\'' +
                ", soLuong=" + soLuong +
                '}';
    }
}
