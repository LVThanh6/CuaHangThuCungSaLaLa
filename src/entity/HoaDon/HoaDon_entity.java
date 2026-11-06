package entity.HoaDon;

import entity.DichVu.KhuyenMai_Entity;
import entity.DichVu.Thue_entity;
import entity.KhachHang.KhachHang;
import entity.NhanVien.NhanVien_entity;
import entity.ThuCung.ThuCung_entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity biểu diễn một hóa đơn theo các cột của bảng danh sách hóa đơn
 * trong DSHoaDon_gui.
 */
public class HoaDon_entity {
    private String maHD;
    private LocalDate ngayLap;
    private String trangThai;
    private KhachHang maKH;
    private ThuCung_entity maTC;
    private NhanVien_entity maNhanVien;
    private KhuyenMai_Entity maKM;
    private Thue_entity maThue;
    private ThanhToan_entity maThanhToan;

    public HoaDon_entity() {
        super();
    }

    public HoaDon_entity(String maHD) {
        this.maHD = maHD;
    }

    public HoaDon_entity(String maHD, LocalDate ngayLap, String trangThai, KhachHang maKH, NhanVien_entity maNhanVien,
            KhuyenMai_Entity maKM, Thue_entity maThue, ThanhToan_entity maThanhToan) {
        this(maHD, ngayLap, trangThai, maKH, null, maNhanVien, maKM, maThue, maThanhToan);
    }

    public HoaDon_entity(String maHD, LocalDate ngayLap, String trangThai, KhachHang maKH, ThuCung_entity maTC,
            NhanVien_entity maNhanVien, KhuyenMai_Entity maKM, Thue_entity maThue, ThanhToan_entity maThanhToan) {
        super();
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.maKH = maKH;
        this.maTC = maTC;
        this.maNhanVien = maNhanVien;
        this.maKM = maKM;
        this.maThue = maThue;
        this.maThanhToan = maThanhToan;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH != null ? new KhachHang(maKH) : null;
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

    public NhanVien_entity getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien_entity maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public void setMaNhanVien(String maNV) {
        this.maNhanVien = maNV != null ? new NhanVien_entity(maNV) : null;
    }

    public KhuyenMai_Entity getMaKM() {
        return maKM;
    }

    public void setMaKM(KhuyenMai_Entity maKM) {
        this.maKM = maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM != null ? new KhuyenMai_Entity(maKM, "", 0, null, null, "") : null;
    }

    public Thue_entity getMaThue() {
        return maThue;
    }

    public void setMaThue(Thue_entity maThue) {
        this.maThue = maThue;
    }

    public void setMaThue(String maThue) {
        this.maThue = maThue != null ? new Thue_entity(maThue, "", 0) : null;
    }

    public ThanhToan_entity getMaThanhToan() {
        return maThanhToan;
    }

    public void setMaThanhToan(ThanhToan_entity maThanhToan) {
        this.maThanhToan = maThanhToan;
    }

    public void setMaThanhToan(String maTT) {
        this.maThanhToan = maTT != null ? new ThanhToan_entity(maTT, 0, null, "", "") : null;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoaDon_entity that = (HoaDon_entity) o;
        return Objects.equals(maHD, that.maHD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHD);
    }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHD='" + maHD + '\'' +
                ", ngayLap=" + ngayLap +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
