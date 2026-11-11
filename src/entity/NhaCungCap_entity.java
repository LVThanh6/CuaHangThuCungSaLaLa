package entity;

import java.util.Objects;

public class NhaCungCap_entity {
	private String maNCC;
	private String tenNCC;
	private String diaChi;
	private String sdt;
	private String email;

	public NhaCungCap_entity() {
		super();
	}

	public NhaCungCap_entity(String maNCC) {
		this.maNCC = maNCC;
	}

	public NhaCungCap_entity(String maNCC, String tenNCC, String diaChi, String sdt, String email) {
		super();
		this.maNCC = maNCC;
		this.tenNCC = tenNCC;
		this.diaChi = diaChi;
		this.sdt = sdt;
		this.email = email;
	}

	public String getMaNCC() {
		return maNCC;
	}

	public void setMaNCC(String maNCC) {
		this.maNCC = maNCC;
	}

	public String getTenNCC() {
		return tenNCC;
	}

	public void setTenNCC(String tenNCC) {
		this.tenNCC = tenNCC;
	}

	public String getDiaChi() {
		return diaChi;
	}

	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}

	public String getSdt() {
		return sdt;
	}

	public void setSdt(String sdt) {
		this.sdt = sdt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NhaCungCap_entity that = (NhaCungCap_entity) o;
		return Objects.equals(maNCC, that.maNCC);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maNCC);
	}

	@Override
	public String toString() {
		return tenNCC != null ? tenNCC + " (" + maNCC + ")" : (maNCC != null ? maNCC : "");
	}
}
