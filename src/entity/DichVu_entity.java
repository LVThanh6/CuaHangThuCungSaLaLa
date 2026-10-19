
package entity;

import java.time.LocalTime;

public class DichVu_entity {
    private String maDV;
    private String tenDV;
    private String moTa;
    private double giaDV;
    private LocalTime thoiGian;
    private String trangThai;

    public DichVu_entity() {
        super();
    }

    public DichVu_entity(String maDV, String tenDV, String moTa,
                         double giaDV, LocalTime thoiGian,
                         String trangThai) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.moTa = moTa;
        this.giaDV = giaDV;
        this.thoiGian = thoiGian;
        this.trangThai = trangThai;
    }

    public String getMaDV() {
        return maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public double getGiaDV() {
        return giaDV;
    }

    public void setGiaDV(double giaDV) {
        this.giaDV = giaDV;
    }

    public LocalTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
