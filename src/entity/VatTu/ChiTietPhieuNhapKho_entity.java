package entity.VatTu;

import java.util.Objects;

/**
 * Entity "Chi tiết phiếu nhập kho" - SALALA Pet Shop.
 * Bảng: ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong)
 * (tenVT lấy từ VatTu, donVi lấy từ Thuoc.donViTinh để hiển thị).
 *
 * Khóa chính: maCTPNK (do DAO sinh: CTPNK01, CTPNK02, ...).
 * equals/hashCode theo (maPNK, maVT) vì một phiếu không nên có 2 dòng cùng vật
 * tư.
 */
public class ChiTietPhieuNhapKho_entity {

    private String maCTPNK; // khóa chính của dòng chi tiết
    private PhieuNhapKho_entity maPNK;
    private VatTu_entity maVT;
    private String tenVT; // từ VatTu (chỉ để hiển thị)
    private String donVi; // từ VatTu (chỉ để hiển thị)
    private int soLuong;
    private long donGia;

    public ChiTietPhieuNhapKho_entity() {
    }

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity maPNK, VatTu_entity maVT) {
        this.maPNK = maPNK;
        this.maVT = maVT;
    }

    public ChiTietPhieuNhapKho_entity(String maPNK, String maVT) {
        this(maPNK != null ? new PhieuNhapKho_entity(maPNK) : null,
                maVT != null ? new VatTu_entity(maVT) : null);
    }

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity maPNK, VatTu_entity maVT, int soLuong, long donGia) {
        this.maPNK = maPNK;
        this.maVT = maVT;
        setSoLuong(soLuong);
        setDonGia(donGia);
    }

    public ChiTietPhieuNhapKho_entity(String maPNK, String maVT, int soLuong, long donGia) {
        this(maPNK != null ? new PhieuNhapKho_entity(maPNK) : null,
                maVT != null ? new VatTu_entity(maVT) : null,
                soLuong, donGia);
    }

    public ChiTietPhieuNhapKho_entity(PhieuNhapKho_entity maPNK, VatTu_entity maVT, String tenVT, String donVi,
            int soLuong, long donGia) {
        this(maPNK, maVT, soLuong, donGia);
        this.tenVT = tenVT;
        this.donVi = donVi;
    }

    public ChiTietPhieuNhapKho_entity(String maPNK, String maVT, String tenVT, String donVi,
            int soLuong, long donGia) {
        this(maPNK != null ? new PhieuNhapKho_entity(maPNK) : null,
                maVT != null ? new VatTu_entity(maVT) : null,
                tenVT, donVi, soLuong, donGia);
    }

    // ===== Getter / Setter =====
    public String getMaCTPNK() {
        return maCTPNK;
    }

    public void setMaCTPNK(String maCTPNK) {
        this.maCTPNK = maCTPNK;
    }

    public PhieuNhapKho_entity getMaPNK() {
        return maPNK;
    }

    public void setMaPNK(PhieuNhapKho_entity maPNK) {
        this.maPNK = maPNK;
    }

    public void setMaPNK(String maPNK) {
        this.maPNK = maPNK != null ? new PhieuNhapKho_entity(maPNK) : null;
    }

    public VatTu_entity getMaVT() {
        return maVT;
    }

    public void setMaVT(VatTu_entity maVT) {
        this.maVT = maVT;
    }

    public void setMaVT(String maVT) {
        this.maVT = maVT != null ? new VatTu_entity(maVT) : null;
    }

    public String getTenVT() {
        return tenVT;
    }

    public void setTenVT(String tenVT) {
        this.tenVT = tenVT;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0)
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        this.soLuong = soLuong;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        if (donGia < 0)
            throw new IllegalArgumentException("Đơn giá không được âm.");
        this.donGia = donGia;
    }

    /** Thành tiền = số lượng x đơn giá. */
    public long getThanhTien() {
        return (long) soLuong * donGia;
    }

    // ===== Chuyển đổi với lớp ChiTiet lồng trong PhieuNhapKho_entity =====
    public PhieuNhapKho_entity.ChiTiet toChiTiet() {
        return new PhieuNhapKho_entity.ChiTiet(maVT, tenVT, donVi, soLuong, donGia);
    }

    public static ChiTietPhieuNhapKho_entity fromChiTiet(String maPNK, PhieuNhapKho_entity.ChiTiet d) {
        ChiTietPhieuNhapKho_entity e = new ChiTietPhieuNhapKho_entity();
        e.setMaPNK(maPNK);
        e.maVT = d.getMaVT();
        e.tenVT = d.getTenVT();
        e.donVi = d.getDonVi();
        e.soLuong = d.getSoLuong();
        e.donGia = d.getDonGia();
        return e;
    }

    // ===== equals / hashCode theo khóa chính (maPNK, maVT) =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChiTietPhieuNhapKho_entity))
            return false;
        ChiTietPhieuNhapKho_entity c = (ChiTietPhieuNhapKho_entity) o;
        return Objects.equals(maPNK, c.maPNK) && Objects.equals(maVT, c.maVT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPNK, maVT);
    }

    @Override
    public String toString() {
        return (maPNK != null ? maPNK.getMaPNK() : "") + " | " + (maVT != null ? maVT.getMaVT() : "")
                + " | " + (tenVT == null ? "" : tenVT)
                + " | SL: " + soLuong + " | Đơn giá: " + donGia;
    }
}