package entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Entity HangThanhVien - tương ứng bảng QuanLyHangThanhVien trong CSDL SALALA.
 */
public class HangThanhVien {
    private String maHang;            // VARCHAR(20) PK
    private String maKH;              // VARCHAR(20) FK -> KhachHang(maKH)
    private String tenHang;           // NVARCHAR(50) (Đồng, Bạc, Vàng, Kim cương)
    private double chiTieuToiThieu;   // DECIMAL(18,2)
    private Double chiTieuToiDa;      // DECIMAL(18,2) NULLABLE
    private double mucGiamGia;        // DECIMAL(5,2)
    private double diemTichLuy1Van;   // DECIMAL(4,1)
    private String quyenLoi;          // NVARCHAR(500)
    private double tongChiTieu;       // DECIMAL(18,2)
    private int diemTichLuy;          // INT
    private Timestamp ngayCapNhatHang;// DATETIME2
    private String trangThai;         // NVARCHAR(50)

    // Các trường mở rộng phục vụ hiển thị trên giao diện và báo cáo thống kê
    private String hoTenKhachHang;
    private String sdtKhachHang;
    private String emailKhachHang;
    private int soThuCung;
    private int soLuongThanhVien;     // Dùng khi hiển thị 4 Card nhóm hạng
    private List<String> danhSachQuyenLoi;

    // Constructors
    public HangThanhVien() {
        this.trangThai = "Đang áp dụng";
        this.danhSachQuyenLoi = new ArrayList<>();
    }

    /**
     * Constructor đầy đủ theo các cột trong bảng QuanLyHangThanhVien
     */
    public HangThanhVien(String maHang, String maKH, String tenHang, double chiTieuToiThieu, Double chiTieuToiDa,
                        double mucGiamGia, double diemTichLuy1Van, String quyenLoi, double tongChiTieu,
                        int diemTichLuy, Timestamp ngayCapNhatHang, String trangThai) {
        this.maHang = maHang;
        this.maKH = maKH;
        this.tenHang = tenHang;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.chiTieuToiDa = chiTieuToiDa;
        this.mucGiamGia = mucGiamGia;
        this.diemTichLuy1Van = diemTichLuy1Van;
        setQuyenLoi(quyenLoi);
        this.tongChiTieu = tongChiTieu;
        this.diemTichLuy = diemTichLuy;
        this.ngayCapNhatHang = ngayCapNhatHang != null ? ngayCapNhatHang : new Timestamp(System.currentTimeMillis());
        this.trangThai = trangThai != null ? trangThai : "Đang áp dụng";
    }

    /**
     * Constructor phục vụ hiển thị 4 Card nhóm hạng (Đồng, Bạc, Vàng, Kim cương)
     */
    public HangThanhVien(String tenHang, double chiTieuToiThieu, Double chiTieuToiDa, double mucGiamGia,
                        double diemTichLuy1Van, String quyenLoi, int soLuongThanhVien, double tongChiTieu) {
        this.tenHang = tenHang;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.chiTieuToiDa = chiTieuToiDa;
        this.mucGiamGia = mucGiamGia;
        this.diemTichLuy1Van = diemTichLuy1Van;
        setQuyenLoi(quyenLoi);
        this.soLuongThanhVien = soLuongThanhVien;
        this.tongChiTieu = tongChiTieu;
        this.trangThai = "Đang áp dụng";
    }

    // Getters and Setters
    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
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

    public Double getChiTieuToiDa() {
        return chiTieuToiDa;
    }

    public void setChiTieuToiDa(Double chiTieuToiDa) {
        this.chiTieuToiDa = chiTieuToiDa;
    }

    public double getMucGiamGia() {
        return mucGiamGia;
    }

    public void setMucGiamGia(double mucGiamGia) {
        this.mucGiamGia = mucGiamGia;
    }

    // Tương thích với các gọi hàm cũ
    public double getPhanTramGiamGia() {
        return mucGiamGia;
    }

    public void setPhanTramGiamGia(double phanTramGiamGia) {
        this.mucGiamGia = phanTramGiamGia;
    }

    public double getDiemTichLuy1Van() {
        return diemTichLuy1Van;
    }

    public void setDiemTichLuy1Van(double diemTichLuy1Van) {
        this.diemTichLuy1Van = diemTichLuy1Van;
    }

    public String getQuyenLoi() {
        return quyenLoi;
    }

    public void setQuyenLoi(String quyenLoi) {
        this.quyenLoi = quyenLoi;
        this.danhSachQuyenLoi = new ArrayList<>();
        if (quyenLoi != null && !quyenLoi.trim().isEmpty()) {
            String[] lines = quyenLoi.split("\r?\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    this.danhSachQuyenLoi.add(line.trim());
                }
            }
        }
    }

    public double getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public Timestamp getNgayCapNhatHang() {
        return ngayCapNhatHang;
    }

    public void setNgayCapNhatHang(Timestamp ngayCapNhatHang) {
        this.ngayCapNhatHang = ngayCapNhatHang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getHoTenKhachHang() {
        return hoTenKhachHang;
    }

    public void setHoTenKhachHang(String hoTenKhachHang) {
        this.hoTenKhachHang = hoTenKhachHang;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }

    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
    }

    public String getEmailKhachHang() {
        return emailKhachHang;
    }

    public void setEmailKhachHang(String emailKhachHang) {
        this.emailKhachHang = emailKhachHang;
    }

    public int getSoThuCung() {
        return soThuCung;
    }

    public void setSoThuCung(int soThuCung) {
        this.soThuCung = soThuCung;
    }

    public int getSoLuongThanhVien() {
        return soLuongThanhVien;
    }

    public void setSoLuongThanhVien(int soLuongThanhVien) {
        this.soLuongThanhVien = soLuongThanhVien;
    }

    public List<String> getDanhSachQuyenLoi() {
        if (danhSachQuyenLoi == null || danhSachQuyenLoi.isEmpty()) {
            if (quyenLoi != null && !quyenLoi.trim().isEmpty()) {
                setQuyenLoi(quyenLoi);
            } else {
                danhSachQuyenLoi = new ArrayList<>();
            }
        }
        return danhSachQuyenLoi;
    }

    public void setDanhSachQuyenLoi(List<String> danhSachQuyenLoi) {
        this.danhSachQuyenLoi = danhSachQuyenLoi;
        if (danhSachQuyenLoi != null) {
            this.quyenLoi = String.join("\n", danhSachQuyenLoi);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
                ", maKH='" + maKH + '\'' +
                ", tenHang='" + tenHang + '\'' +
                ", mucGiamGia=" + mucGiamGia +
                ", tongChiTieu=" + tongChiTieu +
                ", diemTichLuy=" + diemTichLuy +
                '}';
    }
}
