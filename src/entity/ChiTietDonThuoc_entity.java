package entity;

import java.math.BigDecimal;

public class ChiTietDonThuoc_entity {
    private String maCTDT;
    private String maDonThuoc;
    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;
    private BigDecimal soLuong;
    private BigDecimal lieuLuong;

    public ChiTietDonThuoc_entity() {
    }

    public ChiTietDonThuoc_entity(String maCTDT, String maDonThuoc, String maThuoc,
            BigDecimal soLuong, BigDecimal lieuLuong) {
        this.maCTDT = maCTDT;
        this.maDonThuoc = maDonThuoc;
        this.maThuoc = maThuoc;
        this.soLuong = soLuong;
        this.lieuLuong = lieuLuong;
    }

    public String getMaCTDT() { return maCTDT; }
    public void setMaCTDT(String maCTDT) { this.maCTDT = maCTDT; }
    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }
    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }
    public BigDecimal getLieuLuong() { return lieuLuong; }
    public void setLieuLuong(BigDecimal lieuLuong) { this.lieuLuong = lieuLuong; }
}
