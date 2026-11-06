package entity;

/** Một dòng chi tiết phiếu nhập (soPhieu = maPNK). */
public class ChiTietPhieuNhapKho_entity {
    private final String soPhieu, maVT, tenVT, donVi;
    private final int soLuong;
    private final long donGia;

    public ChiTietPhieuNhapKho_entity(String soPhieu, String maVT, String tenVT, String donVi,
                                      int soLuong, long donGia) {
        this.soPhieu = soPhieu; this.maVT = maVT; this.tenVT = tenVT; this.donVi = donVi;
        this.soLuong = soLuong; this.donGia = donGia;
    }
    public String getSoPhieu() { return soPhieu; }
    public String getMaVT() { return maVT; }
    public String getTenVT() { return tenVT; }
    public String getDonVi() { return donVi; }
    public int getSoLuong() { return soLuong; }
    public long getDonGia() { return donGia; }
    public long thanhTien() { return (long) soLuong * donGia; }
}