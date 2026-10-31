package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity biểu diễn một hóa đơn theo các cột của bảng danh sách hóa đơn
 * trong DSHoaDon_gui.
 *
 * Lưu ý:
 * - tien được lưu bằng BigDecimal để tránh sai số khi tính toán tiền.
 * - ngayLap dùng LocalDate, không lưu ngày dưới dạng chuỗi hiển thị dd/MM/yyyy.
 * - tenKhachHang và tenNhanVien là tên hiển thị lấy từ JOIN khi truy vấn.
 *   Khi làm việc trực tiếp với CSDL, có thể bổ sung maKhachHang/maNhanVien
 *   theo đúng tên cột khóa ngoại trong database SALALA.
 */
public class HoaDon_entity{
    private String maHD;
    private LocalDate ngayLap;
    private String trangThai;
    private KhachHang maKH;
    private NhanVien_entity maNhanVien;
    private KhuyenMai_Entity maKM;
    private Thue_entity maThue;
    private ThanhToan_entity maThanhToan ;
	public HoaDon_entity() {
		super();
	}
	public HoaDon_entity(String maHD, LocalDate ngayLap, String trangThai, KhachHang maKH, NhanVien_entity maNhanVien,
			KhuyenMai_Entity maKM, Thue_entity maThue, ThanhToan_entity maThanhToan) {
		super();
		this.maHD = maHD;
		this.ngayLap = ngayLap;
		this.trangThai = trangThai;
		this.maKH = maKH;
		this.maNhanVien = maNhanVien;
		this.maKM = maKM;
		this.maThue = maThue;
		this.maThanhToan = maThanhToan;
	}
	public String getMaHD() {
		return maHD;
	}
	public KhachHang getMaKH() {
		return maKH;
	}
	public NhanVien_entity getMaNhanVien() {
		return maNhanVien;
	}
	public KhuyenMai_Entity getMaKM() {
		return maKM;
	}
	public Thue_entity getMaThue() {
		return maThue;
	}
	public ThanhToan_entity getMaThanhToan() {
		return maThanhToan;
	}
	public LocalDate getNgayLap() {
		return ngayLap;
	}
	public void setNgayLap(LocalDate ngayLap) {
		this.ngayLap = ngayLap;
	}
	public String getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
	public void setMaKH(KhachHang maKH) {
		this.maKH = maKH;
	}
	public void setMaNhanVien(NhanVien_entity maNhanVien) {
		this.maNhanVien = maNhanVien;
	}
	public void setMaKM(KhuyenMai_Entity maKM) {
		this.maKM = maKM;
	}
	public void setMaThue(Thue_entity maThue) {
		this.maThue = maThue;
	}
	public void setMaThanhToan(ThanhToan_entity maThanhToan) {
		this.maThanhToan = maThanhToan;
	}

    
                  

}