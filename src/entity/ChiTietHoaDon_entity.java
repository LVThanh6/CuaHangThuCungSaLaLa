package entity;

public class ChiTietHoaDon_entity {
	private String maCTHD;
	private HoaDon_entity maHD;
	private DichVu_entity maDV;
	private VatTu_entity maVT;
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
	public String getMaCTHD() {
		return maCTHD;
	}
	public HoaDon_entity getMaHD() {
		return maHD;
	}
	public DichVu_entity getMaDV() {
		return maDV;
	}
	public VatTu_entity getMaVT() {
		return maVT;
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

