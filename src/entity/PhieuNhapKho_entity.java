package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu nhập kho (ánh xạ bảng PhieuNhapKho + ChiTietPhieuNhapKho của CSDL SALALA).
 *  soPhieu  = maPNK       nhanVien   = NhanVien.hoTen   (maNhanVien lưu riêng)
 *  ngayLap  = ngayNhap    nhaCungCap = NhaCungCap.tenNCC (maNCC lưu riêng)
 */
public class PhieuNhapKho_entity {

    public enum TrangThai {
        CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");

        private final String text;
        TrangThai(String text) { this.text = text; }
        public String getText() { return text; }

        /** Chuyển chuỗi trong CSDL thành enum. Dữ liệu cũ "Hoàn thành" coi như đã xác nhận. */
        public static TrangThai fromText(String s) {
            if (s != null) {
                String t = s.trim();
                for (TrangThai tt : values()) if (tt.text.equalsIgnoreCase(t)) return tt;
                if (t.equalsIgnoreCase("Hoàn thành")) return DA_XAC_NHAN;
            }
            return CHO_XAC_NHAN;
        }
    }

    /** Một dòng vật tư của phiếu. */
    public static class ChiTiet {
        private final String maVT, tenVT, donVi;
        private final int soLuong;
        private final long donGia;

        public ChiTiet(String maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this.maVT = maVT; this.tenVT = tenVT; this.donVi = donVi;
            this.soLuong = soLuong; this.donGia = donGia;
        }
        public String getMaVT() { return maVT; }
        public String getTenVT() { return tenVT; }
        public String getDonVi() { return donVi; }
        public int getSoLuong() { return soLuong; }
        public long getDonGia() { return donGia; }
        public long thanhTien() { return (long) soLuong * donGia; }
    }

    private String soPhieu, maNhanVien, nhanVien, maNCC, nhaCungCap, ghiChu;
    private LocalDate ngayLap;
    private TrangThai trangThai;
    private List<ChiTiet> chiTiet = new ArrayList<>();

    public PhieuNhapKho_entity(String soPhieu, LocalDate ngayLap, String nhanVien,
                               String nhaCungCap, String ghiChu, TrangThai trangThai) {
        this.soPhieu = soPhieu; this.ngayLap = ngayLap; this.nhanVien = nhanVien;
        this.nhaCungCap = nhaCungCap; this.ghiChu = ghiChu; this.trangThai = trangThai;
    }

    public String getSoPhieu() { return soPhieu; }
    public LocalDate getNgayLap() { return ngayLap; }
    public String getNhanVien() { return nhanVien; }
    public String getNhaCungCap() { return nhaCungCap; }
    public String getGhiChu() { return ghiChu == null ? "" : ghiChu; }
    public TrangThai getTrangThai() { return trangThai; }
    public String getMaNhanVien() { return maNhanVien; }
    public String getMaNCC() { return maNCC; }
    public List<ChiTiet> getChiTiet() { return chiTiet; }

    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }
    public void setChiTiet(List<ChiTiet> chiTiet) { this.chiTiet = chiTiet == null ? new ArrayList<>() : chiTiet; }

    public int tongSoLuong() { int t = 0; for (ChiTiet d : chiTiet) t += d.getSoLuong(); return t; }
    public long tongTien() { long t = 0; for (ChiTiet d : chiTiet) t += d.thanhTien(); return t; }
}