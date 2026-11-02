package entity;

public class NhanSuLichLamViec_entity {
    private final String maNhanSu;
    private final String tenNhanSu;
    private final String vaiTro;
    private final LichLamViec_entity.LoaiNhanSu loaiNhanSu;

    public NhanSuLichLamViec_entity(
            String maNhanSu,
            String tenNhanSu,
            String vaiTro,
            LichLamViec_entity.LoaiNhanSu loaiNhanSu) {
        this.maNhanSu = maNhanSu;
        this.tenNhanSu = tenNhanSu;
        this.vaiTro = vaiTro;
        this.loaiNhanSu = loaiNhanSu;
    }

    public String getMaNhanSu() {
        return maNhanSu;
    }

    public String getTenNhanSu() {
        return tenNhanSu;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public LichLamViec_entity.LoaiNhanSu getLoaiNhanSu() {
        return loaiNhanSu;
    }

    @Override
    public String toString() {
        return maNhanSu + " · " + tenNhanSu + " — " + vaiTro;
    }
}
