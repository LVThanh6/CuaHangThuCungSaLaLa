package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class LichLamViec_entity {
    private String maLLV;
    private String maNhanSu; // Dùng chung cho Nhân viên / Bác sĩ
    private String tenNhanSu; // Tên hiển thị (Không lưu CSDL)
    private String vaiTro;    // Vai trò hiển thị (Không lưu CSDL)
    private LocalDate ngay;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String trangThai;

    public LichLamViec_entity() {}

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

    // Getters and Setters...
    public String getMaLLV() { return maLLV; }
    public void setMaLLV(String maLLV) { this.maLLV = maLLV; }
    public String getMaNhanSu() { return maNhanSu; }
    public void setMaNhanSu(String maNhanSu) { this.maNhanSu = maNhanSu; }
    public String getTenNhanSu() { return tenNhanSu; }
    public String getVaiTro() { return vaiTro; }
    public LocalDate getNgay() { return ngay; }
    public LocalTime getGioBatDau() { return gioBatDau; }
    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}