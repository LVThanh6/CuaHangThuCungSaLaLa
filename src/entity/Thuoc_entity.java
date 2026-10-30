package entity;

public class Thuoc_entity {
    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;
    private String huongDanSuDung;

    public Thuoc_entity(String maThuoc, String tenThuoc, String donViTinh, String huongDanSuDung) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.donViTinh = donViTinh;
        this.huongDanSuDung = huongDanSuDung;
    }

    public String getMaThuoc() { return maThuoc; }
    public String getTenThuoc() { return tenThuoc; }
    public String getDonViTinh() { return donViTinh; }
    public String getHuongDanSuDung() { return huongDanSuDung; }

    @Override
    public String toString() {
        return tenThuoc + " (" + maThuoc + ")";
    }
}
