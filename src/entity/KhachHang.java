package entity;

import java.util.Objects;

/**
 * Entity KhachHang - đại diện cho bảng KhachHang trong CSDL SALALA.
 */
public class KhachHang {
    private String maKH;
    private String hoTen;
    private String sdt;
    private String email;
    private String diaChi;

    // Các thuộc tính thống kê mở rộng hiển thị trên GUI
    private int soThuCung;
    private String hangThanhVien;
    private double tongChiTieu;

    public KhachHang() {
    }

    public KhachHang(String maKH) {
        this.maKH = maKH;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String email, String diaChi) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String email, String diaChi, int soThuCung,
            String hangThanhVien, double tongChiTieu) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.soThuCung = soThuCung;
        this.hangThanhVien = hangThanhVien;
        this.tongChiTieu = tongChiTieu;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getSoThuCung() {
        return soThuCung;
    }

    public void setSoThuCung(int soThuCung) {
        this.soThuCung = soThuCung;
    }

    public String getHangThanhVien() {
        return hangThanhVien;
    }

    public void setHangThanhVien(String hangThanhVien) {
        this.hangThanhVien = hangThanhVien;
    }

    public double getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(maKH, khachHang.maKH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKH);
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKH='" + maKH + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", soThuCung=" + soThuCung +
                ", hangThanhVien='" + hangThanhVien + '\'' +
                ", tongChiTieu=" + tongChiTieu +
                '}';
    }
}
