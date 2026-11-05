package entity;

import java.util.Objects;

public class ChiTietHoaDon_entity {
	private String maCTHD;
	private HoaDon_entity maHD;
	private DichVu_entity maDV;
	private VatTu_entity maVT;
	private ChiTietDonThuoc_entity maCTDT;
	private NhanVien_entity nhanVienThucHien;
	private BacSi_entity bacSiThucHien;
	private int soLuong;
	private double donGia;

	public ChiTietHoaDon_entity() {
		super();
	}

	public ChiTietHoaDon_entity(String maCTHD) {
		this.maCTHD = maCTHD;
	}

	public ChiTietHoaDon_entity(String maCTHD, HoaDon_entity maHD, DichVu_entity maDV, VatTu_entity maVT, int soLuong,
			double donGia) {
		super();
		this.maCTHD = maCTHD;
		this.maHD = maHD;
		this.maDV = maDV;
		this.maVT = maVT;
		this.soLuong = soLuong;
		this.donGia = donGia;
	}

	public ChiTietHoaDon_entity(String maCTHD, HoaDon_entity maHD, DichVu_entity maDV, VatTu_entity maVT,
			NhanVien_entity nhanVienThucHien, BacSi_entity bacSiThucHien, int soLuong, double donGia) {
		super();
		this.maCTHD = maCTHD;
		this.maHD = maHD;
		this.maDV = maDV;
		this.maVT = maVT;
		this.nhanVienThucHien = nhanVienThucHien;
		this.bacSiThucHien = bacSiThucHien;
		this.soLuong = soLuong;
		this.donGia = donGia;
	}

	public ChiTietHoaDon_entity(String maCTHD, HoaDon_entity maHD, DichVu_entity maDV, VatTu_entity maVT,
			ChiTietDonThuoc_entity maCTDT, NhanVien_entity nhanVienThucHien, BacSi_entity bacSiThucHien,
			int soLuong, double donGia) {
		super();
		this.maCTHD = maCTHD;
		this.maHD = maHD;
		this.maDV = maDV;
		this.maVT = maVT;
		this.maCTDT = maCTDT;
		this.nhanVienThucHien = nhanVienThucHien;
		this.bacSiThucHien = bacSiThucHien;
		this.soLuong = soLuong;
		this.donGia = donGia;
	}

	public String getMaCTHD() {
		return maCTHD;
	}

	public void setMaCTHD(String maCTHD) {
		this.maCTHD = maCTHD;
	}

	public HoaDon_entity getMaHD() {
		return maHD;
	}

	public void setMaHD(HoaDon_entity maHD) {
		this.maHD = maHD;
	}

	public void setMaHD(String maHD) {
		this.maHD = maHD != null ? new HoaDon_entity(maHD) : null;
	}

	public DichVu_entity getMaDV() {
		return maDV;
	}

	public void setMaDV(DichVu_entity maDV) {
		this.maDV = maDV;
	}

	public void setMaDV(String maDV) {
		this.maDV = maDV != null ? new DichVu_entity(maDV, "", "", 0, null, "") : null;
	}

	public VatTu_entity getMaVT() {
		return maVT;
	}

	public void setMaVT(VatTu_entity maVT) {
		this.maVT = maVT;
	}

	public void setMaVT(String maVT) {
		this.maVT = maVT != null ? new VatTu_entity(maVT) : null;
	}

	public ChiTietDonThuoc_entity getMaCTDT() {
		return maCTDT;
	}

	public void setMaCTDT(ChiTietDonThuoc_entity maCTDT) {
		this.maCTDT = maCTDT;
	}

	public void setMaCTDT(String maCTDT) {
		this.maCTDT = maCTDT != null ? new ChiTietDonThuoc_entity(maCTDT) : null;
	}

	public NhanVien_entity getNhanVienThucHien() {
		return nhanVienThucHien;
	}

	public void setNhanVienThucHien(NhanVien_entity nhanVienThucHien) {
		this.nhanVienThucHien = nhanVienThucHien;
	}

	public BacSi_entity getBacSiThucHien() {
		return bacSiThucHien;
	}

	public void setBacSiThucHien(BacSi_entity bacSiThucHien) {
		this.bacSiThucHien = bacSiThucHien;
	}

	public String getMaNhanVienThucHien() {
		return nhanVienThucHien == null ? null : nhanVienThucHien.getMaNhanVien();
	}

	public String getMaBacSiThucHien() {
		return bacSiThucHien == null ? null : bacSiThucHien.getMaBacSi();
	}

	public int getSoLuong() {
		return soLuong;
	}

	public void setSoLuong(int soLuong) {
		this.soLuong = soLuong;
	}

	public double getDonGia() {
		return donGia;
	}

	public void setDonGia(double donGia) {
		this.donGia = donGia;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChiTietHoaDon_entity that = (ChiTietHoaDon_entity) o;
		return Objects.equals(maCTHD, that.maCTHD);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maCTHD);
	}

	@Override
	public String toString() {
		return "ChiTietHoaDon{" +
				"maCTHD='" + maCTHD + '\'' +
				", soLuong=" + soLuong +
				", donGia=" + donGia +
				'}';
	}
}
