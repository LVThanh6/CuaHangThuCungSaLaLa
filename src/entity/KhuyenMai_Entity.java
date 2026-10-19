package entity;

import java.time.LocalDate;

public class KhuyenMai_Entity {
	private String maKM;
	private String tenKM;
	private double mucKM;
	private LocalDate ngayBatDau;
	private LocalDate ngayKetThuc;
	private String dieuKien;
	public KhuyenMai_Entity() {
		super();
	}
	public KhuyenMai_Entity(String maKM, String tenKM, double mucKM, LocalDate ngayBatDau, LocalDate ngayKetThuc,
			String dieuKien) {
		super();
		this.maKM = maKM;
		this.tenKM = tenKM;
		this.mucKM = mucKM;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
		this.dieuKien = dieuKien;
	}
	public String getMaKM() {
		return maKM;
	}
	
	public String getTenKM() {
		return tenKM;
	}
	public void setTenKM(String tenKM) {
		this.tenKM = tenKM;
	}
	public double getMucKM() {
		return mucKM;
	}
	public void setMucKM(double mucKM) {
		this.mucKM = mucKM;
	}
	public LocalDate getNgayBatDau() {
		return ngayBatDau;
	}
	public void setNgayBatDau(LocalDate ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}
	public LocalDate getNgayKetThuc() {
		return ngayKetThuc;
	}
	public void setNgayKetThuc(LocalDate ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}
	public String getDieuKien() {
		return dieuKien;
	}
	public void setDieuKien(String dieuKien) {
		this.dieuKien = dieuKien;
	}
	
	
}
