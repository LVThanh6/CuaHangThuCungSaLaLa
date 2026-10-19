package entity;

public class Thue_entity {
	private String maThue;
	private String tenThue;
	private double mucThue;
	public Thue_entity() {
		super();
	}
	public Thue_entity(String maThue, String tenThue, double mucThue) {
		super();
		this.maThue = maThue;
		this.tenThue = tenThue;
		this.mucThue = mucThue;
	}
	public String getMaThue() {
		return maThue;
	}
	
	public String getTenThue() {
		return tenThue;
	}
	public void setTenThue(String tenThue) {
		this.tenThue = tenThue;
	}
	public double getMucThue() {
		return mucThue;
	}
	public void setMucThue(double mucThue) {
		this.mucThue = mucThue;
	}
	
	
}
