package entity.ThuCung;

import entity.KhachHang.KhachHang;

import java.time.LocalDate;
import java.util.Objects;

public class ThuCung_entity {
	private String maTC;
	private KhachHang maKH;
	private String tenTC;
	private String loai;
	private String gioiTinh;
	private LocalDate ngaySinh;
	private String chuHo;
	private String dacDiemNhanDien;

	public ThuCung_entity() {
		super();
	}

	public ThuCung_entity(String maTC) {
		this.maTC = maTC;
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

	public void setMaTC(String maTC) {
		this.maTC = maTC;
	}

	public KhachHang getMaKH() {
		return maKH;
	}

	public void setMaKH(KhachHang maKH) {
		this.maKH = maKH;
	}

	public void setMaKH(String maKH) {
		this.maKH = maKH != null ? new KhachHang(maKH) : null;
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ThuCung_entity that = (ThuCung_entity) o;
		return Objects.equals(maTC, that.maTC);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maTC);
	}

	@Override
	public String toString() {
		return tenTC != null ? tenTC + " (" + maTC + ")" : (maTC != null ? maTC : "");
	}
}
