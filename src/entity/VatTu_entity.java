package entity;

import java.util.Objects;

public class VatTu_entity {
	private String maVT;
    private NhaCungCap_entity maNCC;
    private String tenVT;
    private int soLuongTon;
    private String trangThai;
    private double giaBanCoBan;
    private double giaNhap;
    private Thuoc_entity maThuoc;

	public VatTu_entity() {
		super();
	}

	public VatTu_entity(String maVT) {
		this.maVT = maVT;
	}

	public VatTu_entity(String maVT, NhaCungCap_entity maNCC, String tenVT, int soLuongTon, String trangThai,
			double giaBanCoBan, double giaNhap) {
		this(maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, (Thuoc_entity) null);
	}

	public VatTu_entity(String maVT, NhaCungCap_entity maNCC, String tenVT, int soLuongTon, String trangThai,
			double giaBanCoBan, double giaNhap, Thuoc_entity maThuoc) {
		super();
		this.maVT = maVT;
		this.maNCC = maNCC;
		this.tenVT = tenVT;
		this.soLuongTon = soLuongTon;
		this.trangThai = trangThai;
		this.giaBanCoBan = giaBanCoBan;
		this.giaNhap = giaNhap;
		this.maThuoc = maThuoc;
	}

	public VatTu_entity(String maVT, NhaCungCap_entity maNCC, String tenVT, int soLuongTon, String trangThai,
			double giaBanCoBan, double giaNhap, String maThuoc) {
		this(maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap,
				maThuoc != null ? new Thuoc_entity(maThuoc) : null);
	}

	public String getMaVT() {
		return maVT;
	}

	public void setMaVT(String maVT) {
		this.maVT = maVT;
	}
	
	public NhaCungCap_entity getMaNCC() {
		return maNCC;
	}

	public void setMaNCC(NhaCungCap_entity maNCC) {
		this.maNCC = maNCC;
	}

	public void setMaNCC(String maNCC) {
		this.maNCC = maNCC != null ? new NhaCungCap_entity(maNCC, "", "", "", "") : null;
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
	public Thuoc_entity getMaThuoc() {
		return maThuoc;
	}
	public void setMaThuoc(Thuoc_entity maThuoc) {
		this.maThuoc = maThuoc;
	}
	public void setMaThuoc(String maThuoc) {
		this.maThuoc = maThuoc != null ? new Thuoc_entity(maThuoc) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VatTu_entity that = (VatTu_entity) o;
		return Objects.equals(maVT, that.maVT);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maVT);
	}

	@Override
	public String toString() {
		return tenVT != null ? tenVT + " (" + maVT + ")" : (maVT != null ? maVT : "");
	}
}
