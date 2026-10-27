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
	private String loaiLuong;
	private double luongTheoGio;
	private double luongThang;
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
		this.loaiLuong = "THEO_GIO";
		this.luongTheoGio = luongCoBan;
		this.luongThang = 0;
	}

	// Constructor 10 tham số (dùng trong DAO để lấy trọn vẹn dữ liệu từ CSDL)
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
		this.loaiLuong = "THEO_GIO";
		this.luongTheoGio = luongCoBan;
		this.luongThang = 0;
		this.trangThaiTK = trangThaiTK;
	}

	public NhanVien_entity(String maNhanVien, String maTK, String hoTen, String chucVu, String sdt, String diaChi,
			LocalDate ngaySinh, String gioiTinh, double luongCoBan, String loaiLuong, double luongTheoGio,
			double luongThang, Boolean trangThaiTK) {
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
		this.loaiLuong = loaiLuong;
		this.luongTheoGio = luongTheoGio;
		this.luongThang = luongThang;
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
	public String getLoaiLuong() {
		return loaiLuong == null ? "THEO_GIO" : loaiLuong;
	}
	public void setLoaiLuong(String loaiLuong) {
		this.loaiLuong = loaiLuong;
	}
	public double getLuongTheoGio() {
		return luongTheoGio;
	}
	public void setLuongTheoGio(double luongTheoGio) {
		this.luongTheoGio = luongTheoGio;
	}
	public double getLuongThang() {
		return luongThang;
	}
	public void setLuongThang(double luongThang) {
		this.luongThang = luongThang;
	}
	public Boolean getTrangThaiTK() {
		return trangThaiTK;
	}
	public void setTrangThaiTK(Boolean trangThaiTK) {
		this.trangThaiTK = trangThaiTK;
	}
}