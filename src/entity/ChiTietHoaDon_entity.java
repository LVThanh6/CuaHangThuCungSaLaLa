package entity;

public class ChiTietHoaDon_entity {
	private String maCTHD;
	private HoaDon_entity maHD;
	private DichVu_entity maDV;
	private VatTu_entity maVT;
	private String maDVCode;
	private String maVTCode;
	private NhanVien_entity nhanVienThucHien;
	private BacSi_entity bacSiThucHien;
	private int soLuong;
	private double donGia;
	public ChiTietHoaDon_entity() {
		super();
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
	public String getMaCTHD() {
		return maCTHD;
	}
	public HoaDon_entity getMaHD() {
		return maHD;
	}
	public void setMaHD(HoaDon_entity maHD) {
		this.maHD = maHD;
	}
	public DichVu_entity getMaDV() {
		return maDV;
	}
	public void setMaDV(DichVu_entity maDV) {
		this.maDV = maDV;
		this.maDVCode = maDV == null ? null : maDV.getMaDV();
		if (maDV != null) {
			this.maVT = null;
			this.maVTCode = null;
		}
	}
	public String getMaDVString() {
		if (maDV != null) {
			return maDV.getMaDV();
		}
		return maDVCode;
	}
	public void setMaDVCode(String maDVCode) {
		this.maDVCode = maDVCode;
		if (maDVCode != null && !maDVCode.isBlank()) {
			this.maDV = null;
		}
	}
	public VatTu_entity getMaVT() {
		return maVT;
	}
	public void setMaVT(VatTu_entity maVT) {
		this.maVT = maVT;
		this.maVTCode = maVT == null ? null : maVT.getMaVT();
		if (maVT != null) {
			this.maDV = null;
			this.maDVCode = null;
		}
	}
	public String getMaVTString() {
		if (maVT != null) {
			return maVT.getMaVT();
		}
		return maVTCode;
	}
	public void setMaVTCode(String maVTCode) {
		this.maVTCode = maVTCode;
		if (maVTCode != null && !maVTCode.isBlank()) {
			this.maVT = null;
		}
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
	
	
}

