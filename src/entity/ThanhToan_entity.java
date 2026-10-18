package entity;

import java.time.LocalDate;

public class ThanhToan_entity {
	private String maThanhToan;
	private double soTien;
	private LocalDate thoiGian;
    private String phuongThuc;
    private String trangThai;
	public ThanhToan_entity() {
		super();
	}
	public ThanhToan_entity(String maThanhToan, double soTien, LocalDate thoiGian, String phuongThuc,
			String trangThai) {
		super();
		this.maThanhToan = maThanhToan;
		this.soTien = soTien;
		this.thoiGian = thoiGian;
		this.phuongThuc = phuongThuc;
		this.trangThai = trangThai;
	}
	public String getMaThanhToan() {
		return maThanhToan;
	}
	public double getSoTien() {
		return soTien;
	}
	public void setSoTien(double soTien) {
		this.soTien = soTien;
	}
	public LocalDate getThoiGian() {
		return thoiGian;
	}
	public void setThoiGian(LocalDate thoiGian) {
		this.thoiGian = thoiGian;
	}
	public String getPhuongThuc() {
		return phuongThuc;
	}
	public void setPhuongThuc(String phuongThuc) {
		this.phuongThuc = phuongThuc;
	}
	public String getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
    
    
}
