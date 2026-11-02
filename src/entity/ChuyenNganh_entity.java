package entity;

public class ChuyenNganh_entity {
    private String maChuyenNganh;
    private String tenChuyenNganh;

    public ChuyenNganh_entity() {
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
    public String toString() {
        return tenChuyenNganh;
    }
}
