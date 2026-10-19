package entity;

public class VatTu_entity {
	private String maVT;
    private NhaCungCap_entity maNCC;
    private String tenVT ;
    private int soLuongTon;
    private String trangThai ;
    private double giaBanCoBan;
    private double giaNhap ;
	public VatTu_entity() {
		super();
	}
	public VatTu_entity(String maVT, NhaCungCap_entity maNCC, String tenVT, int soLuongTon, String trangThai,
			double giaBanCoBan, double giaNhap) {
		super();
		this.maVT = maVT;
		this.maNCC = maNCC;
		this.tenVT = tenVT;
		this.soLuongTon = soLuongTon;
		this.trangThai = trangThai;
		this.giaBanCoBan = giaBanCoBan;
		this.giaNhap = giaNhap;
	}
	public String getMaVT() {
		return maVT;
	}
	
	public NhaCungCap_entity getMaNCC() {
		return maNCC;
	}
	
	public String getTenVT() {
		return tenVT;
	}
	public void setTenVT(String tenVT) {
		this.tenVT = tenVT;
	}
	public int getSoLuongTon() {
		return soLuongTon;
	}
	public void setSoLuongTon(int soLuongTon) {
		this.soLuongTon = soLuongTon;
	}
	public String getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
	public double getGiaBanCoBan() {
		return giaBanCoBan;
	}
	public void setGiaBanCoBan(double giaBanCoBan) {
		this.giaBanCoBan = giaBanCoBan;
	}
	public double getGiaNhap() {
		return giaNhap;
	}
	public void setGiaNhap(double giaNhap) {
		this.giaNhap = giaNhap;
	}
    
    
}
