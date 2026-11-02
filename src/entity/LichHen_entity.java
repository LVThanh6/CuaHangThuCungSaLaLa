package entity;

import java.time.LocalDateTime;

public class LichHen_entity {
    private String maLichHen;
    
    // Sử dụng Object Composition để lấy nguyên đối tượng chứa khóa ngoại
    private ThuCung_entity thuCung;
    private KhachHang khachHang;
    private NhanVien_entity nhanVien;
    private DichVu_entity dichVu; // Có thể null theo thiết kế CSDL
    
    private LocalDateTime ngayGio;
    private String lyDo;
    private String trangThai;
    private String ghiChu;
    private String loaiHen;

    public LichHen_entity() {
        super();
    }

    public LichHen_entity(String maLichHen, ThuCung_entity thuCung, KhachHang khachHang, 
                          NhanVien_entity nhanVien, DichVu_entity dichVu, LocalDateTime ngayGio, 
                          String lyDo, String trangThai, String ghiChu, String loaiHen) {
        super();
        this.maLichHen = maLichHen;
        this.thuCung = thuCung;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.dichVu = dichVu;
        this.ngayGio = ngayGio;
        this.lyDo = lyDo;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.loaiHen = loaiHen;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public ThuCung_entity getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung_entity thuCung) {
        this.thuCung = thuCung;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien_entity getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien_entity nhanVien) {
        this.nhanVien = nhanVien;
    }

    public DichVu_entity getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu_entity dichVu) {
        this.dichVu = dichVu;
    }

    public LocalDateTime getNgayGio() {
        return ngayGio;
    }

    public void setNgayGio(LocalDateTime ngayGio) {
        this.ngayGio = ngayGio;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getLoaiHen() {
        return loaiHen;
    }

    public void setLoaiHen(String loaiHen) {
        this.loaiHen = loaiHen;
    }
}