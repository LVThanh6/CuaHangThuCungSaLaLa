package entity;

import java.time.LocalDateTime;

public class DonThuoc_entity {
    private String maDonThuoc;
    private String maTC;
    private String maBacSi;
    private String maHSSK;
    private LocalDateTime ngayKeDon;
    private String ghiChu;
    private String trangThai;
    private String tenThuCung;
    private String tenBacSi;
    private int soLoaiThuoc;

    public DonThuoc_entity() {
    }

    public DonThuoc_entity(String maDonThuoc, String maTC, String maBacSi, String maHSSK,
            LocalDateTime ngayKeDon, String ghiChu, String trangThai) {
        this.maDonThuoc = maDonThuoc;
        this.maTC = maTC;
        this.maBacSi = maBacSi;
        this.maHSSK = maHSSK;
        this.ngayKeDon = ngayKeDon;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public String getMaTC() { return maTC; }
    public void setMaTC(String maTC) { this.maTC = maTC; }
    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    public String getMaHSSK() { return maHSSK; }
    public void setMaHSSK(String maHSSK) { this.maHSSK = maHSSK; }
    public LocalDateTime getNgayKeDon() { return ngayKeDon; }
    public void setNgayKeDon(LocalDateTime ngayKeDon) { this.ngayKeDon = ngayKeDon; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    public int getSoLoaiThuoc() { return soLoaiThuoc; }
    public void setSoLoaiThuoc(int soLoaiThuoc) { this.soLoaiThuoc = soLoaiThuoc; }
}
