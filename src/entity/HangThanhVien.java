package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity HangThanhVien - đại diện cho các hạng thành viên của SALALA Pet Shop
 * (Đồng, Bạc, Vàng, Kim Cương) dùng cho QuanLyHangThanhVienGUI.
 */
public class HangThanhVien {
    private String maHang;
    private String tenHang;
    private double chiTieuToiThieu;
    private double phanTramGiamGia;
    private int soLuongThanhVien;
    private double tongChiTieu;
    private List<String> danhSachQuyenLoi;

    public HangThanhVien() {
        this.danhSachQuyenLoi = new ArrayList<>();
    }

    public HangThanhVien(String maHang, String tenHang, double chiTieuToiThieu, double phanTramGiamGia) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.phanTramGiamGia = phanTramGiamGia;
        this.danhSachQuyenLoi = new ArrayList<>();
    }

    public HangThanhVien(String maHang, String tenHang, double chiTieuToiThieu, double phanTramGiamGia,
            int soLuongThanhVien, double tongChiTieu, List<String> danhSachQuyenLoi) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.phanTramGiamGia = phanTramGiamGia;
        this.soLuongThanhVien = soLuongThanhVien;
        this.tongChiTieu = tongChiTieu;
        this.danhSachQuyenLoi = danhSachQuyenLoi != null ? danhSachQuyenLoi : new ArrayList<>();
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public double getChiTieuToiThieu() {
        return chiTieuToiThieu;
    }

    public void setChiTieuToiThieu(double chiTieuToiThieu) {
        this.chiTieuToiThieu = chiTieuToiThieu;
    }

    public double getPhanTramGiamGia() {
        return phanTramGiamGia;
    }

    public void setPhanTramGiamGia(double phanTramGiamGia) {
        this.phanTramGiamGia = phanTramGiamGia;
    }

    public int getSoLuongThanhVien() {
        return soLuongThanhVien;
    }

    public void setSoLuongThanhVien(int soLuongThanhVien) {
        this.soLuongThanhVien = soLuongThanhVien;
    }

    public double getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public List<String> getDanhSachQuyenLoi() {
        return danhSachQuyenLoi;
    }

    public void setDanhSachQuyenLoi(List<String> danhSachQuyenLoi) {
        this.danhSachQuyenLoi = danhSachQuyenLoi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HangThanhVien that = (HangThanhVien) o;
        return Objects.equals(maHang, that.maHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHang);
    }

    @Override
    public String toString() {
        return "HangThanhVien{" +
                "maHang='" + maHang + '\'' +
                ", tenHang='" + tenHang + '\'' +
                ", chiTieuToiThieu=" + chiTieuToiThieu +
                ", phanTramGiamGia=" + phanTramGiamGia +
                ", soLuongThanhVien=" + soLuongThanhVien +
                '}';
    }
}
