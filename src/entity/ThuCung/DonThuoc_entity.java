package entity.ThuCung;

import entity.BacSi.BacSi_entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class DonThuoc_entity {
    private String maDonThuoc;
    private ThuCung_entity maTC;
    private BacSi_entity maBacSi;
    private HoSoSucKhoe_entity maHSSK;
    private LocalDateTime ngayKeDon;
    private String ghiChu;
    private String trangThai;
    private String tenThuCung;
    private String tenBacSi;
    private int soLoaiThuoc;

    public DonThuoc_entity() {
    }

    public DonThuoc_entity(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc;
    }

    public DonThuoc_entity(String maDonThuoc, ThuCung_entity maTC, BacSi_entity maBacSi, HoSoSucKhoe_entity maHSSK,
            LocalDateTime ngayKeDon, String ghiChu, String trangThai) {
        this.maDonThuoc = maDonThuoc;
        this.maTC = maTC;
        this.maBacSi = maBacSi;
        this.maHSSK = maHSSK;
        this.ngayKeDon = ngayKeDon;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public DonThuoc_entity(String maDonThuoc, ThuCung_entity maTC, BacSi_entity maBacSi, String maHSSK,
            LocalDateTime ngayKeDon, String ghiChu, String trangThai) {
        this(maDonThuoc, maTC, maBacSi,
                maHSSK != null ? new HoSoSucKhoe_entity(maHSSK) : null,
                ngayKeDon, ghiChu, trangThai);
    }

    public DonThuoc_entity(String maDonThuoc, String maTC, String maBacSi, String maHSSK,
            LocalDateTime ngayKeDon, String ghiChu, String trangThai) {
        this(maDonThuoc,
                maTC != null ? new ThuCung_entity(maTC) : null,
                maBacSi != null ? new BacSi_entity(maBacSi) : null,
                maHSSK != null ? new HoSoSucKhoe_entity(maHSSK) : null,
                ngayKeDon, ghiChu, trangThai);
    }

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc;
    }

    public ThuCung_entity getMaTC() {
        return maTC;
    }

    public void setMaTC(ThuCung_entity maTC) {
        this.maTC = maTC;
    }

    public void setMaTC(String maTC) {
        this.maTC = maTC != null ? new ThuCung_entity(maTC) : null;
    }

    public BacSi_entity getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(BacSi_entity maBacSi) {
        this.maBacSi = maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi != null ? new BacSi_entity(maBacSi) : null;
    }

    public HoSoSucKhoe_entity getMaHSSK() {
        return maHSSK;
    }

    public void setMaHSSK(HoSoSucKhoe_entity maHSSK) {
        this.maHSSK = maHSSK;
    }

    public void setMaHSSK(String maHSSK) {
        this.maHSSK = maHSSK != null ? new HoSoSucKhoe_entity(maHSSK) : null;
    }

    public LocalDateTime getNgayKeDon() {
        return ngayKeDon;
    }

    public void setNgayKeDon(LocalDateTime ngayKeDon) {
        this.ngayKeDon = ngayKeDon;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenBacSi() {
        return tenBacSi;
    }

    public void setTenBacSi(String tenBacSi) {
        this.tenBacSi = tenBacSi;
    }

    public int getSoLoaiThuoc() {
        return soLoaiThuoc;
    }

    public void setSoLoaiThuoc(int soLoaiThuoc) {
        this.soLoaiThuoc = soLoaiThuoc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DonThuoc_entity that = (DonThuoc_entity) o;
        return Objects.equals(maDonThuoc, that.maDonThuoc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonThuoc);
    }

    @Override
    public String toString() {
        return "DonThuoc{" +
                "maDonThuoc='" + maDonThuoc + '\'' +
                ", ngayKeDon=" + ngayKeDon +
                '}';
    }
}
