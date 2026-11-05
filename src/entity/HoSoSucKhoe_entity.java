package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class HoSoSucKhoe_entity {
    private String maHSSK;
    private ThuCung_entity maTC;
    private BacSi_entity maBacSi;
    private LocalDateTime ngayKham;
    private String trieuChung;
    private String chuanDoan;
    private BigDecimal canNang;
    private String tenThuCung;
    private String chuHo;
    private String tenBacSi;

    public HoSoSucKhoe_entity() {
    }

    public HoSoSucKhoe_entity(String maHSSK) {
        this.maHSSK = maHSSK;
    }

    public HoSoSucKhoe_entity(String maHSSK, ThuCung_entity maTC, BacSi_entity maBacSi, LocalDateTime ngayKham,
            String trieuChung, String chuanDoan, BigDecimal canNang) {
        this.maHSSK = maHSSK;
        this.maTC = maTC;
        this.maBacSi = maBacSi;
        this.ngayKham = ngayKham;
        this.trieuChung = trieuChung;
        this.chuanDoan = chuanDoan;
        this.canNang = canNang;
    }

    public HoSoSucKhoe_entity(String maHSSK, String maTC, String maBacSi, LocalDateTime ngayKham,
            String trieuChung, String chuanDoan, BigDecimal canNang) {
        this(maHSSK, maTC != null ? new ThuCung_entity(maTC) : null,
             maBacSi != null ? new BacSi_entity(maBacSi) : null,
             ngayKham, trieuChung, chuanDoan, canNang);
    }

    public String getMaHSSK() { return maHSSK; }
    public void setMaHSSK(String maHSSK) { this.maHSSK = maHSSK; }

    public ThuCung_entity getMaTC() { return maTC; }
    public void setMaTC(ThuCung_entity maTC) { this.maTC = maTC; }
    public void setMaTC(String maTC) { this.maTC = maTC != null ? new ThuCung_entity(maTC) : null; }

    public BacSi_entity getMaBacSi() { return maBacSi; }
    public void setMaBacSi(BacSi_entity maBacSi) { this.maBacSi = maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi != null ? new BacSi_entity(maBacSi) : null; }

    public LocalDateTime getNgayKham() { return ngayKham; }
    public void setNgayKham(LocalDateTime ngayKham) { this.ngayKham = ngayKham; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public String getChuanDoan() { return chuanDoan; }
    public void setChuanDoan(String chuanDoan) { this.chuanDoan = chuanDoan; }

    public BigDecimal getCanNang() { return canNang; }
    public void setCanNang(BigDecimal canNang) { this.canNang = canNang; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }

    public String getChuHo() { return chuHo; }
    public void setChuHo(String chuHo) { this.chuHo = chuHo; }

    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoSoSucKhoe_entity that = (HoSoSucKhoe_entity) o;
        return Objects.equals(maHSSK, that.maHSSK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHSSK);
    }

    @Override
    public String toString() {
        return "HoSoSucKhoe{" +
                "maHSSK='" + maHSSK + '\'' +
                ", ngayKham=" + ngayKham +
                '}';
    }
}
