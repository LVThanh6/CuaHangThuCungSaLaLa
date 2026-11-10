package entity.NhanVien;

import entity.TaiKhoan.TaiKhoan;

import java.time.LocalDate;
import java.util.Objects;

public class NhanVien_entity {
	private String maNhanVien;
	private TaiKhoan maTK;
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

	public NhanVien_entity(String maNhanVien) {
		this.maNhanVien = maNhanVien;
	}

	// Constructor 9 tham số
	public NhanVien_entity(String maNhanVien, TaiKhoan maTK, String hoTen, String chucVu, String sdt, String diaChi,
			LocalDate ngaySinh, String gioiTinh, double luongCoBan) {
		this(maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan, null);
	}

	// Constructor 10 tham số
	public NhanVien_entity(String maNhanVien, TaiKhoan maTK, String hoTen, String chucVu, String sdt, String diaChi,
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

	public void setMaNhanVien(String maNhanVien) {
		this.maNhanVien = maNhanVien;
	}

	public TaiKhoan getMaTK() {
		return maTK;
	}

	public void setMaTK(TaiKhoan maTK) {
		this.maTK = maTK;
	}

	public void setMaTK(String maTK) {
		this.maTK = maTK != null ? new TaiKhoan(maTK) : null;
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NhanVien_entity that = (NhanVien_entity) o;
		return Objects.equals(maNhanVien, that.maNhanVien);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maNhanVien);
	}

	@Override
	public String toString() {
		return hoTen != null ? hoTen + " (" + maNhanVien + ")" : (maNhanVien != null ? maNhanVien : "");
	}
}
