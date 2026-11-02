package dao;

import connectDB.ConnectDB;
import entity.LichLamViec_entity;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LichLamViec_DAO {

    /**
     * Tự động quét và cập nhật trạng thái lịch của các ngày trước đó thành "Hoàn thành" 
     * nếu chưa bị đánh dấu Hủy hoặc Vắng mặt.
     */
    public void capNhatTrangThaiTuDongQuaNgay() {
        String sql = "UPDATE LichLamViec SET trangThai = N'Hoàn thành' " +
                     "WHERE ngay < CAST(GETDATE() AS DATE) " +
                     "AND trangThai NOT IN (N'Hoàn thành', N'Đã hủy', N'Vắng mặt')";
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi auto-update qua ngày: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ lịch làm việc của cả Nhân Viên và Bác Sĩ trong 1 ngày
     */
    public List<LichLamViec_entity> getLichTheoNgay(LocalDate ngay) {
        capNhatTrangThaiTuDongQuaNgay(); // Kích hoạt auto-update trước khi lấy dữ liệu

        List<LichLamViec_entity> ds = new ArrayList<>();
        // Sử dụng UNION ALL để lấy chung Lịch của Nhân viên và Bác sĩ
        String sql = "SELECT l.maLLV, l.maNhanVien AS maNhanSu, nv.hoTen AS ten, nv.chucVu AS vaiTro, " +
                     "l.ngay, l.gioBatDau, l.gioKetThuc, l.trangThai " +
                     "FROM LichLamViec l JOIN NhanVien nv ON l.maNhanVien = nv.maNhanVien " +
                     "WHERE l.ngay = ? " +
                     "UNION ALL " +
                     "SELECT l.maLLV, l.maNhanVien AS maNhanSu, bs.hoTen AS ten, N'Bác sĩ' AS vaiTro, " +
                     "l.ngay, l.gioBatDau, l.gioKetThuc, l.trangThai " +
                     "FROM LichLamViec l JOIN BacSyThuY bs ON l.maNhanVien = bs.maBacSi " +
                     "WHERE l.ngay = ? " +
                     "ORDER BY l.gioBatDau ASC, l.maLLV ASC";

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

    public boolean themLich(LichLamViec_entity llv) {
        String sql = "INSERT INTO LichLamViec (maLLV, maNhanVien, ngay, gioBatDau, gioKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, llv.getMaLLV());
            ps.setString(2, llv.getMaNhanSu());
            ps.setDate(3, Date.valueOf(llv.getNgay()));
            ps.setTime(4, Time.valueOf(llv.getGioBatDau()));
            ps.setTime(5, Time.valueOf(llv.getGioKetThuc()));
            ps.setString(6, llv.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatLich(LichLamViec_entity llv) {
        String sql = "UPDATE LichLamViec SET maNhanVien = ?, gioBatDau = ?, gioKetThuc = ?, trangThai = ? WHERE maLLV = ?";
        try (Connection con = ConnectDB.getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, llv.getMaNhanSu());
            ps.setTime(2, Time.valueOf(llv.getGioBatDau()));
            ps.setTime(3, Time.valueOf(llv.getGioKetThuc()));
            ps.setString(4, llv.getTrangThai());
            ps.setString(5, llv.getMaLLV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThaiKhoa(String maLLV, String trangThai) {
        String sql = "UPDATE LichLamViec SET trangThai = ? WHERE maLLV = ?";
        try (Connection con = ConnectDB.getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maLLV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaLich(String maLLV) {
        String sql = "DELETE FROM LichLamViec WHERE maLLV = ?";
        try (Connection con = ConnectDB.getCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLLV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String phatSinhMaLLV() {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maLLV, 4, LEN(maLLV)) AS INT)), 0) FROM LichLamViec WHERE maLLV LIKE 'LLV%'";
        try (Connection con = ConnectDB.getCon();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return String.format("LLV%04d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "LLV0001";
    }
}