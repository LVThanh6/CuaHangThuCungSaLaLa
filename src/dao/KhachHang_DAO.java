package dao;

import connectDB.ConnectDB;
import entity.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class KhachHang_DAO {
    private static final String SELECT_CUSTOMERS =
            "SELECT k.maKH, k.hoTen, k.sdt, k.email, k.diaChi, "
                    + "COALESCE(p.soThuCung, 0) AS soThuCung, "
                    + "CASE "
                    + "WHEN COALESCE(s.tongChiTieu, 0) >= 30000000 THEN N'Kim cương' "
                    + "WHEN COALESCE(s.tongChiTieu, 0) >= 15000000 THEN N'Vàng' "
                    + "WHEN COALESCE(s.tongChiTieu, 0) >= 5000000 THEN N'Bạc' "
                    + "ELSE N'Đồng' END AS hangThanhVien, "
                    + "COALESCE(s.tongChiTieu, 0) AS tongChiTieu "
                    + "FROM KhachHang k "
                    + "LEFT JOIN (SELECT maKH, COUNT(*) AS soThuCung "
                    + "FROM ThuCung GROUP BY maKH) p ON p.maKH = k.maKH "
                    + "LEFT JOIN (SELECT h.maKH, SUM(c.soLuong * c.donGia) AS tongChiTieu "
                    + "FROM HoaDon h JOIN ChiTietHoaDon c ON c.maHD = h.maHD "
                    + "GROUP BY h.maKH) s ON s.maKH = k.maKH ";

    public ArrayList<KhachHang> getAlltbKhachHang() {
        return timKiemTongHop(null);
    }

    public ArrayList<KhachHang> getAllKhachHangDayDu() {
        return getAlltbKhachHang();
    }

    public KhachHang getKhachHangTheoMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_CUSTOMERS + "WHERE k.maKH = ?")) {
            statement.setString(1, maKH.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapKhachHang(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm khách hàng: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<KhachHang> timKiemTongHop(String tuKhoa) {
        ArrayList<KhachHang> customers = new ArrayList<>();
        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return customers;
        }

        String sql = SELECT_CUSTOMERS;
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql += "WHERE k.maKH LIKE ? OR k.hoTen LIKE ? OR k.sdt LIKE ? ";
        }
        sql += "ORDER BY k.maKH";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                String pattern = "%" + tuKhoa.trim() + "%";
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapKhachHang(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách khách hàng: " + e.getMessage());
        }
        return customers;
    }

    private KhachHang mapKhachHang(ResultSet resultSet) throws SQLException {
        return new KhachHang(
                resultSet.getString("maKH"),
                resultSet.getString("hoTen"),
                resultSet.getString("sdt"),
                resultSet.getString("email"),
                resultSet.getString("diaChi"),
                resultSet.getInt("soThuCung"),
                resultSet.getString("hangThanhVien"),
                resultSet.getDouble("tongChiTieu"));
    }
}