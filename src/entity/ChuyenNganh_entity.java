package entity;

import java.util.Objects;

public class ChuyenNganh_entity {
    private String maChuyenNganh;
    private String tenChuyenNganh;

    public ChuyenNganh_entity() {
    }

    public ChuyenNganh_entity(String maChuyenNganh) {
        this.maChuyenNganh = maChuyenNganh;
    }

    public ChuyenNganh_entity(String maChuyenNganh, String tenChuyenNganh) {
        this.maChuyenNganh = maChuyenNganh;
        this.tenChuyenNganh = tenChuyenNganh;
    }

    public String getMaChuyenNganh() {
        return maChuyenNganh;
    }

    public void setMaChuyenNganh(String maChuyenNganh) {
        this.maChuyenNganh = maChuyenNganh;
    }

    public String getTenChuyenNganh() {
        return tenChuyenNganh;
    }

    public void setTenChuyenNganh(String tenChuyenNganh) {
        this.tenChuyenNganh = tenChuyenNganh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChuyenNganh_entity that = (ChuyenNganh_entity) o;
        return Objects.equals(maChuyenNganh, that.maChuyenNganh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChuyenNganh);
    }

    @Override
    public String toString() {
        return tenChuyenNganh != null ? tenChuyenNganh : maChuyenNganh;
    }
}
