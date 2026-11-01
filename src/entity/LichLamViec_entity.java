package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class LichLamViec_entity {
    private String maLLV;
    private String maNhanSu; // Dùng chung cho maNhanVien hoặc maBacSi
    private String tenNhanSu; // Tên lấy từ phép JOIN (Không lưu CSDL)
    private String vaiTro;    // Chức vụ lấy từ phép JOIN (Không lưu CSDL)
    private LocalDate ngay;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String trangThai;

    public LichLamViec_entity() {
    }

    public LichLamViec_entity(String maLLV, String maNhanSu, String tenNhanSu, String vaiTro, LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai) {
        this.maLLV = maLLV;
        this.maNhanSu = maNhanSu;
        this.tenNhanSu = tenNhanSu;
        this.vaiTro = vaiTro;
        this.ngay = ngay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
    }

    public String getMaLLV() { return maLLV; }
    public void setMaLLV(String maLLV) { this.maLLV = maLLV; }
    public String getMaNhanSu() { return maNhanSu; }
    public void setMaNhanSu(String maNhanSu) { this.maNhanSu = maNhanSu; }
    public String getTenNhanSu() { return tenNhanSu; }
    public void setTenNhanSu(String tenNhanSu) { this.tenNhanSu = tenNhanSu; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }
    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }
    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}