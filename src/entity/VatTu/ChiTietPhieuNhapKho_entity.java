package entity.VatTu;

import java.util.Objects;

/**
 * Entity "Chi tiết phiếu nhập kho" - SALALA Pet Shop.
 * Bảng: ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia)
 * (tenVT, donVi lấy từ bảng VatTu để hiển thị).
 *
 * Khóa chính: (soPhieu, maVT).
 */
public class ChiTietPhieuNhapKho_entity {

    private PhieuNhapKho_entity soPhieu;
    private VatTu_entity maVT;
    private String tenVT;   // từ VatTu (chỉ để hiển thị)
    private String donVi;   // từ VatTu (chỉ để hiển thị)
    private int soLuong;
    private long donGia;

    public ChiTietPhieuNhapKho_entity() {}

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity soPhieu, VatTu_entity maVT) {
        this.soPhieu = soPhieu;
        this.maVT = maVT;
    }

    public ChiTietPhieuNhapKho_entity(String soPhieu, String maVT) {
        this(soPhieu != null ? new PhieuNhapKho_entity(soPhieu) : null,
             maVT != null ? new VatTu_entity(maVT) : null);
    }

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity soPhieu, VatTu_entity maVT, int soLuong, long donGia) {
        this.soPhieu = soPhieu;
        this.maVT = maVT;
        setSoLuong(soLuong);
        setDonGia(donGia);
    }

    public ChiTietPhieuNhapKho_entity(String soPhieu, String maVT, int soLuong, long donGia) {
        this(soPhieu != null ? new PhieuNhapKho_entity(soPhieu) : null,
             maVT != null ? new VatTu_entity(maVT) : null,
             soLuong, donGia);
    }

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity soPhieu, VatTu_entity maVT, String tenVT, String donVi,
                                   int soLuong, long donGia) {
        this(soPhieu, maVT, soLuong, donGia);
        this.tenVT = tenVT;
        this.donVi = donVi;
    }

    public ChiTietPhieuNhapKho_entity(String soPhieu, String maVT, String tenVT, String donVi,
                                   int soLuong, long donGia) {
        this(soPhieu != null ? new PhieuNhapKho_entity(soPhieu) : null,
             maVT != null ? new VatTu_entity(maVT) : null,
             tenVT, donVi, soLuong, donGia);
    }

    // ===== Getter / Setter =====
    public PhieuNhapKho_entity getSoPhieu() { return soPhieu; }
    public void setSoPhieu(PhieuNhapKho_entity soPhieu) { this.soPhieu = soPhieu; }
    public void setSoPhieu(String soPhieu) {
        this.soPhieu = soPhieu != null ? new PhieuNhapKho_entity(soPhieu) : null;
    }

    public VatTu_entity getMaVT() { return maVT; }
    public void setMaVT(VatTu_entity maVT) { this.maVT = maVT; }
    public void setMaVT(String maVT) {
        this.maVT = maVT != null ? new VatTu_entity(maVT) : null;
    }

    public String getTenVT() { return tenVT; }
    public void setTenVT(String tenVT) { this.tenVT = tenVT; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        this.soLuong = soLuong;
    }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) {
        if (donGia < 0) throw new IllegalArgumentException("Đơn giá không được âm.");
        this.donGia = donGia;
    }

    /** Thành tiền = số lượng x đơn giá. */
    public long getThanhTien() { return (long) soLuong * donGia; }

    // ===== Chuyển đổi với lớp ChiTiet lồng trong PhieuNhapKho_entity =====
    public PhieuNhapKho_entity.ChiTiet toChiTiet() {
        return new PhieuNhapKho_entity.ChiTiet(maVT, tenVT, donVi, soLuong, donGia);
    }

    public static ChiTietPhieuNhapKho_entity fromChiTiet(String soPhieu, PhieuNhapKho_entity.ChiTiet d) {
        ChiTietPhieuNhapKho_entity e = new ChiTietPhieuNhapKho_entity();
        e.setSoPhieu(soPhieu);
        e.maVT = d.getMaVT();
        e.tenVT = d.getTenVT();
        e.donVi = d.getDonVi();
        e.soLuong = d.getSoLuong();
        e.donGia = d.getDonGia();
        return e;
    }

    // ===== equals / hashCode theo khóa chính (soPhieu, maVT) =====
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietPhieuNhapKho_entity)) return false;
        ChiTietPhieuNhapKho_entity c = (ChiTietPhieuNhapKho_entity) o;
        return Objects.equals(soPhieu, c.soPhieu) && Objects.equals(maVT, c.maVT);
    }

    @Override public int hashCode() { return Objects.hash(soPhieu, maVT); }

    @Override public String toString() {
        return (soPhieu != null ? soPhieu.getSoPhieu() : "") + " | " + (maVT != null ? maVT.getMaVT() : "")
                + " | " + (tenVT == null ? "" : tenVT)
                + " | SL: " + soLuong + " | Đơn giá: " + donGia;
    }
}
