package entity;

import java.util.Objects;

/**
 * Entity "Chi tiết phiếu xuất kho" - SALALA Pet Shop.
 * Bảng: ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia)  -- khóa chính (maPhieu, maVT).
 * Đơn giá = giá nhập tại thời điểm lập phiếu.
 * tenVT, donVi không nằm trong bảng ChiTietPhieuXuat; được lấy bằng JOIN với VatTu để hiển thị.
 */
public class ChiTietPhieuXuatKho_entity {

    private String maPhieu;
    private String maVT;
    private String tenVT = "";   // chỉ để hiển thị (JOIN VatTu)
    private String donVi = "";   // chỉ để hiển thị (JOIN VatTu)
    private int soLuong;
    private long donGia;

    public ChiTietPhieuXuatKho_entity() {}

    public ChiTietPhieuXuatKho_entity(String maPhieu, String maVT, int soLuong, long donGia) {
        this.maPhieu = maPhieu;
        this.maVT = maVT;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietPhieuXuatKho_entity(String maPhieu, String maVT, String tenVT, String donVi,
                                      int soLuong, long donGia) {
        this(maPhieu, maVT, soLuong, donGia);
        this.tenVT = tenVT;
        this.donVi = donVi;
    }

    // ===== Getter / Setter =====
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getMaVT() { return maVT; }
    public void setMaVT(String maVT) { this.maVT = maVT; }

    public String getTenVT() { return tenVT; }
    public void setTenVT(String tenVT) { this.tenVT = tenVT; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }

    /** Thành tiền = số lượng x đơn giá (ép long trước khi nhân để tránh tràn int). */
    public long getThanhTien() { return (long) soLuong * donGia; }

    // ===== Chuyển đổi với lớp lồng PhieuXuatKho_entity.ChiTiet =====
    public PhieuXuatKho_entity.ChiTiet toChiTiet() {
        return new PhieuXuatKho_entity.ChiTiet(maVT, tenVT, donVi, soLuong, donGia);
    }

    public static ChiTietPhieuXuatKho_entity fromChiTiet(String maPhieu, PhieuXuatKho_entity.ChiTiet d) {
        return new ChiTietPhieuXuatKho_entity(maPhieu, d.getMaVT(), d.getTenVT(), d.getDonVi(),
                d.getSoLuong(), d.getDonGia());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietPhieuXuatKho_entity)) return false;
        ChiTietPhieuXuatKho_entity x = (ChiTietPhieuXuatKho_entity) o;
        return Objects.equals(maPhieu, x.maPhieu) && Objects.equals(maVT, x.maVT);
    }

    @Override public int hashCode() { return Objects.hash(maPhieu, maVT); }

    @Override public String toString() {
        return maPhieu + " - " + maVT + " x" + soLuong;
    }
}