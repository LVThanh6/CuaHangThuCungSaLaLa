package entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity đại diện cho bảng TaiKhoan trong CSDL SALALA.
 */
public class TaiKhoan_entity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maTK;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private boolean trangThai;
    private QuanLyCuaHang_entity maQuanLy;

    public TaiKhoan_entity() {
    }

    public TaiKhoan_entity(String maTK) {
        this.maTK = maTK;
    }

    public TaiKhoan_entity(String maTK, String tenDangNhap, String matKhau, String vaiTro, boolean trangThai,
            QuanLyCuaHang_entity maQuanLy) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.maQuanLy = maQuanLy;
    }

    public TaiKhoan_entity(String maTK, String tenDangNhap, String matKhau, String vaiTro, boolean trangThai,
            String maQuanLy) {
        this(maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy != null ? new QuanLyCuaHang_entity(maQuanLy) : null);
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public QuanLyCuaHang_entity getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(QuanLyCuaHang_entity maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public void setMaQuanLy(String maQuanLy) {
        this.maQuanLy = maQuanLy != null ? new QuanLyCuaHang_entity(maQuanLy) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TaiKhoan_entity taiKhoan = (TaiKhoan_entity) o;
        return Objects.equals(maTK, taiKhoan.maTK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTK);
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTK='" + maTK + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", vaiTro='" + vaiTro + '\'' +
                ", trangThai=" + trangThai +
                '}';
    }
}
