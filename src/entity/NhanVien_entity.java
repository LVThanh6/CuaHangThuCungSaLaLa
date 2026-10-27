package entity;

import java.time.LocalDate;

public class NhanVien_entity {
	private String maNhanVien;
	private String maTK;
	private String hoTen;
	private String chucVu;
	private String sdt;
	private String diaChi;
	private LocalDate ngaySinh;
	private String gioiTinh;
	private double luongCoBan;
	private Boolean trangThaiTK; // Bổ sung thuộc tính trạng thái tài khoản

	public NhanVien_entity() {
		super();
	}

	// Constructor 9 tham số (dùng cho các trường hợp thêm/sửa không can thiệp trạng thái tk)
	public NhanVien_entity(String maNhanVien, String maTK, String hoTen, String chucVu, String sdt, String diaChi,
			LocalDate ngaySinh, String gioiTinh, double luongCoBan) {
		super();
		this.maNhanVien = maNhanVien;
		this.maTK = maTK;
		this.hoTen = hoTen;
		this.chucVu = chucVu;
		this.sdt = sdt;
		this.diaChi = diaChi;
		this.ngaySinh = ngaySinh;
		this.gioiTinh = gioiTinh;
		this.luongCoBan = luongCoBan;
	}

	// Constructor 10 tham số (dùng trong DAO để lấy trọn vẹn dữ liệu từ CSDL)
	// Lương cơ bản do chức vụ quyết định (bảng LuongChucVu), Quản lý = 0 (chưa tính lương)
	public NhanVien_entity(String maNhanVien, String maTK, String hoTen, String chucVu, String sdt, String diaChi,
			LocalDate ngaySinh, String gioiTinh, double luongCoBan, Boolean trangThaiTK) {
		super();
		this.maNhanVien = maNhanVien;
		this.maTK = maTK;
		this.hoTen = hoTen;
		this.chucVu = chucVu;
		this.sdt = sdt;
		this.diaChi = diaChi;
		this.ngaySinh = ngaySinh;
		this.gioiTinh = gioiTinh;
		this.luongCoBan = luongCoBan;
		this.trangThaiTK = trangThaiTK;
	}

	public String getMaNhanVien() {
		return maNhanVien;
	}
	
	public String getMaTK() {
		return maTK;
	}
	public void setMaTK(String maTK) {
		this.maTK = maTK;
	}
	public String getHoTen() {
		return hoTen;
	}
	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}
	public String getChucVu() {
		return chucVu;
	}
	public void setChucVu(String chucVu) {
		this.chucVu = chucVu;
	}
	public String getSdt() {
		return sdt;
	}
	public void setSdt(String sdt) {
		this.sdt = sdt;
	}
	public String getDiaChi() {
		return diaChi;
	}
	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}
	public LocalDate getNgaySinh() {
		return ngaySinh;
	}
	public void setNgaySinh(LocalDate ngaySinh) {
		this.ngaySinh = ngaySinh;
	}
	public String getGioiTinh() {
		return gioiTinh;
	}
	public void setGioiTinh(String gioiTinh) {
		this.gioiTinh = gioiTinh;
	}
	public double getLuongCoBan() {
		return luongCoBan;
	}
	public void setLuongCoBan(double luongCoBan) {
		this.luongCoBan = luongCoBan;
	}
	public Boolean getTrangThaiTK() {
		return trangThaiTK;
	}
	public void setTrangThaiTK(Boolean trangThaiTK) {
		this.trangThaiTK = trangThaiTK;
	}
}