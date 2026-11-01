package dao;

import connectDB.ConnectDB;
import entity.LichLamViec_entity;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LichLamViec_DAO {

    /**
     * Chạy tự động để cập nhật trạng thái "Hoàn thành" nếu lịch đã qua ngày
     */
    public void capNhatTrangThaiTuDongQuaNgay() {
        String sql = "UPDATE LichLamViec SET trangThai = N'Hoàn thành' " +
                     "WHERE ngay < CAST(GETDATE() AS DATE) AND trangThai NOT IN (N'Hoàn thành', N'Đã hủy')";
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách lịch làm việc theo một ngày cụ thể (Kết hợp cả Nhân viên và Bác sĩ)
     */
    public List<LichLamViec_entity> getLichTheoNgay(LocalDate ngay) {
        // Gọi hàm tự động cập nhật trước khi lấy dữ liệu
        capNhatTrangThaiTuDongQuaNgay(); 
        
        List<LichLamViec_entity> ds = new ArrayList<>();
        String sql = "SELECT l.maLLV, l.maNhanVien AS maNhanSu, nv.hoTen AS ten, nv.chucVu AS vaiTro, " +
                     "l.ngay, l.gioBatDau, l.gioKetThuc, l.trangThai " +
                     "FROM LichLamViec l JOIN NhanVien nv ON l.maNhanVien = nv.maNhanVien " +
                     "WHERE l.ngay = ? " +
                     "UNION ALL " + // Giả lập lấy thêm Bác sĩ (Nếu CSDL cho phép maNhanVien chứa mã BS)
                     "SELECT l.maLLV, l.maNhanVien AS maNhanSu, bs.hoTen AS ten, N'Bác sĩ' AS vaiTro, " +
                     "l.ngay, l.gioBatDau, l.gioKetThuc, l.trangThai " +
                     "FROM LichLamViec l JOIN BacSyThuY bs ON l.maNhanVien = bs.maBacSi " +
                     "WHERE l.ngay = ? " +
                     "ORDER BY gioBatDau ASC";
                     
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return ds;
            ps.setDate(1, Date.valueOf(ngay));
            ps.setDate(2, Date.valueOf(ngay));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Time tB = rs.getTime("gioBatDau");
                Time tK = rs.getTime("gioKetThuc");
                ds.add(new LichLamViec_entity(
                        rs.getString("maLLV"), rs.getString("maNhanSu"), rs.getString("ten"), rs.getString("vaiTro"),
                        rs.getDate("ngay").toLocalDate(),
                        tB != null ? tB.toLocalTime() : null,
                        tK != null ? tK.toLocalTime() : null,
                        rs.getString("trangThai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Các hàm thêm, sửa, xóa cơ bản
    public boolean themLich(LichLamViec_entity llv) { /* INSERT INTO ... */ return true; }
    public boolean xoaLich(String maLLV) { /* DELETE FROM ... */ return true; }
    public boolean capNhatLich(LichLamViec_entity llv) { /* UPDATE ... */ return true; }
}