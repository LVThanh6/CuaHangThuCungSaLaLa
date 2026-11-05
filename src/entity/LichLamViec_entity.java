package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class LichLamViec_entity {
    public enum LoaiNhanSu {
        NHAN_VIEN,
        BAC_SI
    }

    public static final String TRANG_THAI_CHUA_LAM = "Chưa làm";
    public static final String TRANG_THAI_DANG_LAM = "Đang làm";
    public static final String TRANG_THAI_HOAN_THANH = "Hoàn thành";
    public static final String TRANG_THAI_VANG_MAT = "Vắng mặt";
    public static final String TRANG_THAI_DI_TRE = "Đi trễ";
    public static final String TRANG_THAI_VE_SOM = "Về sớm";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";
    public static final Set<String> TRANG_THAI_CHO_PHEP = Set.of(
            TRANG_THAI_CHUA_LAM,
            TRANG_THAI_DANG_LAM,
            TRANG_THAI_HOAN_THANH,
            TRANG_THAI_VANG_MAT,
            TRANG_THAI_DI_TRE,
            TRANG_THAI_VE_SOM,
            TRANG_THAI_DA_HUY
    );

    private String maLLV;
    private NhanVien_entity maNhanVien;
    private BacSi_entity maBacSi;
    private String maNhanSu;
    private LoaiNhanSu loaiNhanSu = LoaiNhanSu.NHAN_VIEN;
    private String tenNhanSu; // Tên lấy từ phép JOIN (Không lưu CSDL)
    private String vaiTro;    // Chức vụ lấy từ phép JOIN (Không lưu CSDL)
    private LocalDate ngay;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String trangThai;

    public LichLamViec_entity() {
    }

    public LichLamViec_entity(String maLLV) {
        this.maLLV = maLLV;
    }

    public LichLamViec_entity(String maLLV, NhanVien_entity maNhanVien, BacSi_entity maBacSi,
                              LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai) {
        this.maLLV = maLLV;
        this.maNhanVien = maNhanVien;
        this.maBacSi = maBacSi;
        if (maNhanVien != null) {
            this.maNhanSu = maNhanVien.getMaNhanVien();
            this.loaiNhanSu = LoaiNhanSu.NHAN_VIEN;
            this.tenNhanSu = maNhanVien.getHoTen();
            this.vaiTro = maNhanVien.getChucVu();
        } else if (maBacSi != null) {
            this.maNhanSu = maBacSi.getMaBacSi();
            this.loaiNhanSu = LoaiNhanSu.BAC_SI;
            this.tenNhanSu = maBacSi.getHoTen();
            this.vaiTro = "Bác sĩ";
        }
        this.ngay = ngay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
    }

    public LichLamViec_entity(String maLLV, String maNhanSu, String tenNhanSu, String vaiTro, LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai) {
        this(maLLV, maNhanSu, tenNhanSu, vaiTro, ngay, gioBatDau, gioKetThuc, trangThai,
                vaiTro != null && "Bác sĩ".equalsIgnoreCase(vaiTro)
                        ? LoaiNhanSu.BAC_SI : LoaiNhanSu.NHAN_VIEN);
    }

    public LichLamViec_entity(String maLLV, String maNhanSu, String tenNhanSu, String vaiTro,
                              LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc,
                              String trangThai, LoaiNhanSu loaiNhanSu) {
        this.maLLV = maLLV;
        this.maNhanSu = maNhanSu;
        this.loaiNhanSu = loaiNhanSu != null ? loaiNhanSu : LoaiNhanSu.NHAN_VIEN;
        this.tenNhanSu = tenNhanSu;
        this.vaiTro = vaiTro;
        this.ngay = ngay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
        if (this.loaiNhanSu == LoaiNhanSu.NHAN_VIEN && maNhanSu != null) {
            this.maNhanVien = new NhanVien_entity(maNhanSu);
            this.maNhanVien.setHoTen(tenNhanSu);
            this.maNhanVien.setChucVu(vaiTro);
        } else if (this.loaiNhanSu == LoaiNhanSu.BAC_SI && maNhanSu != null) {
            this.maBacSi = new BacSi_entity(maNhanSu);
            this.maBacSi.setHoTen(tenNhanSu);
        }
    }

    public String getMaLLV() { return maLLV; }
    public void setMaLLV(String maLLV) { this.maLLV = maLLV; }

    public NhanVien_entity getMaNhanVien() {
        if (maNhanVien == null && maNhanSu != null && loaiNhanSu == LoaiNhanSu.NHAN_VIEN) {
            maNhanVien = new NhanVien_entity(maNhanSu);
            maNhanVien.setHoTen(tenNhanSu);
            maNhanVien.setChucVu(vaiTro);
        }
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien_entity maNhanVien) {
        this.maNhanVien = maNhanVien;
        if (maNhanVien != null) {
            this.maNhanSu = maNhanVien.getMaNhanVien();
            this.loaiNhanSu = LoaiNhanSu.NHAN_VIEN;
            if (maNhanVien.getHoTen() != null) this.tenNhanSu = maNhanVien.getHoTen();
            if (maNhanVien.getChucVu() != null) this.vaiTro = maNhanVien.getChucVu();
        }
    }

    public void setMaNhanVien(String maNV) {
        this.setMaNhanVien(maNV != null ? new NhanVien_entity(maNV) : null);
    }

    public BacSi_entity getMaBacSi() {
        if (maBacSi == null && maNhanSu != null && loaiNhanSu == LoaiNhanSu.BAC_SI) {
            maBacSi = new BacSi_entity(maNhanSu);
            maBacSi.setHoTen(tenNhanSu);
        }
        return maBacSi;
    }

    public void setMaBacSi(BacSi_entity maBacSi) {
        this.maBacSi = maBacSi;
        if (maBacSi != null) {
            this.maNhanSu = maBacSi.getMaBacSi();
            this.loaiNhanSu = LoaiNhanSu.BAC_SI;
            if (maBacSi.getHoTen() != null) this.tenNhanSu = maBacSi.getHoTen();
            this.vaiTro = "Bác sĩ";
        }
    }

    public void setMaBacSi(String maBS) {
        this.setMaBacSi(maBS != null ? new BacSi_entity(maBS) : null);
    }

    public String getMaNhanSu() { return maNhanSu; }
    public void setMaNhanSu(String maNhanSu) {
        this.maNhanSu = maNhanSu;
        if (this.loaiNhanSu == LoaiNhanSu.NHAN_VIEN) {
            this.maNhanVien = maNhanSu != null ? new NhanVien_entity(maNhanSu) : null;
        } else {
            this.maBacSi = maNhanSu != null ? new BacSi_entity(maNhanSu) : null;
        }
    }

    public LoaiNhanSu getLoaiNhanSu() { return loaiNhanSu; }
    public void setLoaiNhanSu(LoaiNhanSu loaiNhanSu) {
        this.loaiNhanSu = loaiNhanSu != null ? loaiNhanSu : LoaiNhanSu.NHAN_VIEN;
    }

    public String getTenNhanSu() { return tenNhanSu; }
    public void setTenNhanSu(String tenNhanSu) { this.tenNhanSu = tenNhanSu; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean coTheSuaLichTai(LocalDateTime thoiDiem) {
        if (ngay == null || gioBatDau == null || thoiDiem == null) {
            return false;
        }
        return ngay.isAfter(thoiDiem.toLocalDate())
                || (ngay.isEqual(thoiDiem.toLocalDate())
                && gioBatDau.isAfter(thoiDiem.toLocalTime()));
    }

    public boolean coTheCapNhatTrangThaiTrongNgay(LocalDate homNay) {
        return ngay != null && ngay.equals(homNay);
    }

    public boolean daKhoaVinhVienTai(LocalDate homNay) {
        return ngay != null && ngay.isBefore(homNay);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LichLamViec_entity that = (LichLamViec_entity) o;
        return Objects.equals(maLLV, that.maLLV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maLLV);
    }

    @Override
    public String toString() {
        return "LichLamViec{" +
                "maLLV='" + maLLV + '\'' +
                ", ngay=" + ngay +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
