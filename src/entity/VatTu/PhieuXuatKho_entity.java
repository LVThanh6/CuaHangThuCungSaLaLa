package entity.VatTu;

import entity.NhanVien.NhanVien_entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity "Phiếu xuất kho" - SALALA Pet Shop.
 * Bảng thật trong CSDL SALALA:
 * PhieuXuatKho(maPXK, ngayXuat DATETIME2, ghiChu, trangThai, maNhanVien)
 * ChiTietPhieuXuatKho(maCTPXK, maPXK, maVT, soLuong)
 * Lưu ý: CSDL KHÔNG có cột lý do xuất và đơn giá.
 * - Lý do xuất được lưu chung trong cột ghiChu theo dạng "Lý do: ... | Ghi chú:
 * ..."
 * (xem ghepGhiChu / tachGhiChu).
 * - Đơn giá = VatTu.giaNhap hiện tại (JOIN khi đọc).
 */
public class PhieuXuatKho_entity {

    // ===== Trạng thái =====
    /**
     * text : chữ hiển thị trên giao diện.
     * dbText : giá trị lưu trong cột PhieuXuatKho.trangThai.
     * Dữ liệu mẫu của SALALA dùng "Hoàn thành" cho phiếu đã xuất nên DA_XUAT lưu là
     * "Hoàn thành".
     */
    public enum TrangThai {
        NHAP("Nháp", "Nháp"), DA_XUAT("Đã xuất", "Hoàn thành"), DA_HUY("Đã hủy", "Đã hủy");

        private final String text, dbText;

        TrangThai(String text, String dbText) {
            this.text = text;
            this.dbText = dbText;
        }

        public String getText() {
            return text;
        }

        public String getDbText() {
            return dbText;
        }

        /** Chuyển chuỗi lưu trong CSDL (hoặc chữ hiển thị) về enum. */
        public static TrangThai fromText(String s) {
            if (s != null) {
                String v = s.trim();
                for (TrangThai t : values())
                    if (t.dbText.equalsIgnoreCase(v) || t.text.equalsIgnoreCase(v))
                        return t;
            }
            return NHAP;
        }
    }

    // ===== Chi tiết phiếu (1 dòng vật tư) =====
    public static class ChiTiet {
        private VatTu_entity maVT;
        private String tenVT, donVi;
        private int soLuong;
        private long donGia; // giá nhập hiện tại của VatTu (KHÔNG lưu trong ChiTietPhieuXuatKho)

        public ChiTiet() {
        }

        public ChiTiet(VatTu_entity maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this.maVT = maVT;
            this.tenVT = tenVT;
            this.donVi = donVi;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        public ChiTiet(String maVT, String tenVT, String donVi, int soLuong, long donGia) {
            this(maVT != null ? new VatTu_entity(maVT) : null, tenVT, donVi, soLuong, donGia);
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
            this.soLuong = soLuong;
        }

        public long getDonGia() {
            return donGia;
        }

        public void setDonGia(long donGia) {
            this.donGia = donGia;
        }

        public long getThanhTien() {
            return (long) soLuong * donGia;
        }
    }

    // ===== Thuộc tính =====
    private String maPhieu; // PXK001
    private LocalDate ngayXuat;
    private NhanVien_entity nhanVien;
    private String tenNhanVien = ""; // chỉ để hiển thị (JOIN NhanVien.hoTen)
    private String lyDo;
    private String ghiChu = "";
    private TrangThai trangThai = TrangThai.NHAP;
    private List<ChiTiet> chiTiet = new ArrayList<>();

    public PhieuXuatKho_entity() {
    }

    public PhieuXuatKho_entity(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public PhieuXuatKho_entity(String maPhieu, LocalDate ngayXuat, NhanVien_entity nhanVien,
            String lyDo, String ghiChu, TrangThai trangThai) {
        this.maPhieu = maPhieu;
        this.ngayXuat = ngayXuat;
        this.nhanVien = nhanVien;
        this.lyDo = lyDo;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public PhieuXuatKho_entity(String maPhieu, LocalDate ngayXuat, String nhanVien,
            String lyDo, String ghiChu, TrangThai trangThai) {
        this(maPhieu, ngayXuat,
                nhanVien != null ? new NhanVien_entity(nhanVien) : null,
                lyDo, ghiChu, trangThai);
    }

    public String getMaPhieu() {
        return maPhieu;
    }

    public void setMaPhieu(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public LocalDate getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    public NhanVien_entity getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien_entity nhanVien) {
        this.nhanVien = nhanVien;
    }

    public void setNhanVien(String nhanVien) {
        this.nhanVien = nhanVien != null ? new NhanVien_entity(nhanVien) : null;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    }

    public List<ChiTiet> getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(List<ChiTiet> chiTiet) {
        this.chiTiet = chiTiet;
    }

    // ===== Ghép / tách "lý do" và "ghi chú" vào cột ghiChu =====
    public static final int GHI_CHU_TOI_DA = 500;
    private static final String TIEN_TO_LY_DO = "Lý do: ";
    private static final String NGAN_CACH = " | Ghi chú: ";

    /** Ghép lý do + ghi chú thành chuỗi lưu vào cột ghiChu. */
    public static String ghepGhiChu(String lyDo, String ghiChu) {
        String ld = lyDo == null ? "" : lyDo.trim();
        String gc = ghiChu == null ? "" : ghiChu.trim();
        if (ld.isEmpty())
            return gc;
        return TIEN_TO_LY_DO + ld + (gc.isEmpty() ? "" : NGAN_CACH + gc);
    }

    /**
     * Tách chuỗi trong cột ghiChu thành {lyDo, ghiChu}. Dữ liệu cũ (không có tiền
     * tố) coi toàn bộ là lý do.
     */
    public static String[] tachGhiChu(String raw) {
        String v = raw == null ? "" : raw.trim();
        if (v.startsWith(TIEN_TO_LY_DO)) {
            String rest = v.substring(TIEN_TO_LY_DO.length());
            int i = rest.indexOf(NGAN_CACH);
            if (i < 0)
                return new String[] { rest.trim(), "" };
            return new String[] { rest.substring(0, i).trim(), rest.substring(i + NGAN_CACH.length()).trim() };
        }
        return new String[] { v, "" };
    }

    public int tongSL() {
        int t = 0;
        for (ChiTiet d : chiTiet)
            t += d.getSoLuong();
        return t;
    }

    public long tongTien() {
        long t = 0;
        for (ChiTiet d : chiTiet)
            t += d.getThanhTien();
        return t;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PhieuXuatKho_entity
                && Objects.equals(maPhieu, ((PhieuXuatKho_entity) o).maPhieu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieu);
    }

    @Override
    public String toString() {
        return maPhieu + "  ·  " + (trangThai != null ? trangThai.getText() : "");
    }
}