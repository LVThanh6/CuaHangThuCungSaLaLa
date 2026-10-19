package entity;

import java.time.LocalDate;

public class BacSi_entity {
    private String maBacSi;
    private String maTK;
    private String hoTen;
    private String gioiTinh;
    private String sdt;
    private String chuyenNganh;
    private String email;
    private LocalDate ngaySinh;
    private int soCaKham;
    private Boolean trangThaiTK; // true: Đang làm việc, false: Tạm nghỉ, null: Chưa có tài khoản

    public BacSi_entity() {
        super();
    }

    public BacSi_entity(String maBacSi, String maTK, String hoTen, String gioiTinh, String sdt,
                        String chuyenNganh, String email, LocalDate ngaySinh) {
        this(maBacSi, maTK, hoTen, gioiTinh, sdt, chuyenNganh, email, ngaySinh, 0, null);
    }

    public BacSi_entity(String maBacSi, String maTK, String hoTen, String gioiTinh, String sdt,
                        String chuyenNganh, String email, LocalDate ngaySinh, int soCaKham, Boolean trangThaiTK) {
        super();
        this.maBacSi = maBacSi;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt;
        this.chuyenNganh = chuyenNganh;
        this.email = email;
        this.ngaySinh = ngaySinh;
        this.soCaKham = soCaKham;
        this.trangThaiTK = trangThaiTK;
    }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getMaTK() { return maTK; }
    public void setMaTK(String maTK) { this.maTK = maTK; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getChuyenNganh() { return chuyenNganh; }
    public void setChuyenNganh(String chuyenNganh) { this.chuyenNganh = chuyenNganh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public int getSoCaKham() { return soCaKham; }
    public void setSoCaKham(int soCaKham) { this.soCaKham = soCaKham; }

    public Boolean getTrangThaiTK() { return trangThaiTK; }
    public void setTrangThaiTK(Boolean trangThaiTK) { this.trangThaiTK = trangThaiTK; }
}