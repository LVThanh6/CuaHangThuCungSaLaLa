package entity;

import java.time.LocalDate;

public class ThuCung_entity {
	private String maTC  ;
	private KhachHang maKH ;
	private String tenTC ;
	private String loai;
	private String gioiTinh ;
	private LocalDate ngaySinh;
	private String chuHo;
	private String dacDiemNhanDien;
	public ThuCung_entity() {
		super();
	}
	public ThuCung_entity(String maTC, KhachHang maKH, String tenTC, String loai, String gioiTinh, LocalDate ngaySinh,
			String chuHo, String dacDiemNhanDien) {
		super();
		this.maTC = maTC;
		this.maKH = maKH;
		this.tenTC = tenTC;
		this.loai = loai;
		this.gioiTinh = gioiTinh;
		this.ngaySinh = ngaySinh;
		this.chuHo = chuHo;
		this.dacDiemNhanDien = dacDiemNhanDien;
	}
	public String getMaTC() {
		return maTC;
	}
	
	public KhachHang getMaKH() {
		return maKH;
	}
	
	public String getTenTC() {
		return tenTC;
	}
	public void setTenTC(String tenTC) {
		this.tenTC = tenTC;
	}
	public String getLoai() {
		return loai;
	}
	public void setLoai(String loai) {
		this.loai = loai;
	}
	public String getGioiTinh() {
		return gioiTinh;
	}
	public void setGioiTinh(String gioiTinh) {
		this.gioiTinh = gioiTinh;
	}
	public LocalDate getNgaySinh() {
		return ngaySinh;
	}
	public void setNgaySinh(LocalDate ngaySinh) {
		this.ngaySinh = ngaySinh;
	}
	public String getChuHo() {
		return chuHo;
	}
	public void setChuHo(String chuHo) {
		this.chuHo = chuHo;
	}
	public String getDacDiemNhanDien() {
		return dacDiemNhanDien;
	}
	public void setDacDiemNhanDien(String dacDiemNhanDien) {
		this.dacDiemNhanDien = dacDiemNhanDien;
	}
	
	
}
