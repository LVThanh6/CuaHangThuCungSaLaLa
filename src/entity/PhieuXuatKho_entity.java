package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity "Phiếu xuất kho" - SALALA Pet Shop.
 * Bảng: PhieuXuatKho (header) + ChiTietPhieuXuat (các dòng vật tư).
 * Đơn giá của dòng = giá nhập tại thời điểm lập phiếu.
 */
public class PhieuXuatKho_entity {

    // ===== Trạng thái =====
    public enum TrangThai {
        NHAP("Nháp"), DA_XUAT("Đã xuất"), DA_HUY("Đã hủy");

        private final String text;
        TrangThai(String text) { this.text = text; }
        public String getText() { return text; }

        /** Chuyển chuỗi lưu trong CSDL về enum. */
        public static TrangThai fromText(String s) {
            for (TrangThai t : values()) if (t.text.equals(s)) return t;
            return NHAP;
        }
    }

    // ===== Chi tiết phiếu (1 dòng vật tư) =====
    public static class ChiTiet {
        private String maVT, tenVT, donVi;
        private int soLuong;
        private long donGia; // giá nhập

        public ChiTiet() {}
        public ChiTiet(String maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this.maVT = maVT; this.tenVT = tenVT; this.donVi = donVi;
            this.soLuong = soLuong; this.donGia = donGia;
        }

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

        public long getThanhTien() { return soLuong * donGia; }
    }

    // ===== Thuộc tính =====
    private String maPhieu;          // PX0001
    private LocalDate ngayXuat;
    private String nhanVien;
    private String lyDo;
    private String ghiChu = "";
    private TrangThai trangThai = TrangThai.NHAP;
    private List<ChiTiet> chiTiet = new ArrayList<>();

    public PhieuXuatKho_entity() {}

    public PhieuXuatKho_entity(String maPhieu) { this.maPhieu = maPhieu; }

    public PhieuXuatKho_entity(String maPhieu, LocalDate ngayXuat, String nhanVien,
                               String lyDo, String ghiChu, TrangThai trangThai) {
        this.maPhieu = maPhieu; this.ngayXuat = ngayXuat; this.nhanVien = nhanVien;
        this.lyDo = lyDo; this.ghiChu = ghiChu; this.trangThai = trangThai;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public LocalDate getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(LocalDate ngayXuat) { this.ngayXuat = ngayXuat; }
    public String getNhanVien() { return nhanVien; }
    public void setNhanVien(String nhanVien) { this.nhanVien = nhanVien; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }
    public List<ChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTiet> chiTiet) { this.chiTiet = chiTiet; }

    public int tongSL() {
        int t = 0;
        for (ChiTiet d : chiTiet) t += d.getSoLuong();
        return t;
    }

    public long tongTien() {
        long t = 0;
        for (ChiTiet d : chiTiet) t += d.getThanhTien();
        return t;
    }

    @Override public boolean equals(Object o) {
        return o instanceof PhieuXuatKho_entity
                && Objects.equals(maPhieu, ((PhieuXuatKho_entity) o).maPhieu);
    }
    @Override public int hashCode() { return Objects.hash(maPhieu); }

    @Override public String toString() { return maPhieu + "  ·  " + trangThai.getText(); }
}