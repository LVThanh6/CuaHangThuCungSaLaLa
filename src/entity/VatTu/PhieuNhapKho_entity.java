package entity.VatTu;

import entity.NhanVien.NhanVien_entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity "Phiếu nhập kho" - SALALA Pet Shop.
 * Bảng: PhieuNhapKho (header) + ChiTietPhieuNhap (các dòng vật tư).
 */
public class PhieuNhapKho_entity {

    // ===== Trạng thái =====
    public enum TrangThai {
        CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");

        private final String text;
        TrangThai(String text) { this.text = text; }
        public String getText() { return text; }

        /** Chuyển chuỗi lưu trong CSDL về enum. */
        public static TrangThai fromText(String s) {
            for (TrangThai t : values()) if (t.text.equals(s)) return t;
            return CHO_XAC_NHAN;
        }
    }

    // ===== Chi tiết phiếu (1 dòng vật tư) =====
    public static class ChiTiet {
        private VatTu_entity maVT;
        private String tenVT, donVi;
        private int soLuong;
        private long donGia;

        public ChiTiet() {}
        public ChiTiet(VatTu_entity maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this.maVT = maVT; this.tenVT = tenVT; this.donVi = donVi;
            this.soLuong = soLuong; this.donGia = donGia;
        }
        public ChiTiet(String maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this(maVT != null ? new VatTu_entity(maVT) : null, tenVT, donVi, soLuong, donGia);
        }

        public VatTu_entity getMaVT() { return maVT; }
        public void setMaVT(VatTu_entity maVT) { this.maVT = maVT; }
        public void setMaVT(String maVT) { this.maVT = maVT != null ? new VatTu_entity(maVT) : null; }

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
    private String soPhieu;          // PN-yyyy-NNNN
    private LocalDate ngayLap;
    private NhanVien_entity nhanVien;
    private NhaCungCap_entity nhaCungCap;
    private String ghiChu = "";
    private TrangThai trangThai = TrangThai.CHO_XAC_NHAN;
    private List<ChiTiet> chiTiet = new ArrayList<>();

    public PhieuNhapKho_entity() {}

    public PhieuNhapKho_entity(String soPhieu) { this.soPhieu = soPhieu; }

    public PhieuNhapKho_entity(String soPhieu, LocalDate ngayLap, NhanVien_entity nhanVien,
                               NhaCungCap_entity nhaCungCap, String ghiChu, TrangThai trangThai) {
        this.soPhieu = soPhieu; this.ngayLap = ngayLap; this.nhanVien = nhanVien;
        this.nhaCungCap = nhaCungCap; this.ghiChu = ghiChu; this.trangThai = trangThai;
    }

    public PhieuNhapKho_entity(String soPhieu, LocalDate ngayLap, String nhanVien,
                               String nhaCungCap, String ghiChu, TrangThai trangThai) {
        this(soPhieu, ngayLap,
             nhanVien != null ? new NhanVien_entity(nhanVien) : null,
             nhaCungCap != null ? new NhaCungCap_entity(nhaCungCap) : null,
             ghiChu, trangThai);
    }

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }

    public NhanVien_entity getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien_entity nhanVien) { this.nhanVien = nhanVien; }
    public void setNhanVien(String nhanVien) {
        this.nhanVien = nhanVien != null ? new NhanVien_entity(nhanVien) : null;
    }

    public NhaCungCap_entity getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap_entity nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap != null ? new NhaCungCap_entity(nhaCungCap) : null;
    }

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
        return o instanceof PhieuNhapKho_entity
                && Objects.equals(soPhieu, ((PhieuNhapKho_entity) o).soPhieu);
    }
    @Override public int hashCode() { return Objects.hash(soPhieu); }

    @Override public String toString() { return soPhieu + "  ·  " + (trangThai != null ? trangThai.getText() : ""); }
}
