package dao;

import connectDB.ConnectDB;
import model.BacSyThuY;

import java.sql.*;
import java.util.ArrayList;

/**
 * Lớp DAO quản lý truy vấn dữ liệu Bác sĩ thú y (bảng BacSyThuY) trong CSDL SALALA.
 */
public class BacSy_DAO {

    public BacSy_DAO() {
    }

    /**
     * Lấy toàn bộ danh sách bác sĩ thú y từ CSDL
     */
    public ArrayList<BacSyThuY> getAllBacSy() {
        ArrayList<BacSyThuY> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();

        if (con != null) {
            String sql = "SELECT maBacSi, maTK, tenChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh FROM BacSyThuY ORDER BY maBacSi ASC";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ds.add(new BacSyThuY(
                            rs.getString("maBacSi"),
                            rs.getString("maTK"),
                            rs.getString("tenChuyenNganh"),
                            rs.getString("hoTen"),
                            rs.getString("gioiTinh"),
                            rs.getString("sdt"),
                            rs.getString("email"),
                            rs.getDate("ngaySinh")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("Lỗi truy vấn danh sách bác sĩ: " + e.getMessage());
            }
        }
        return ds;
    }

    /**
     * Lấy bác sĩ theo mã
     */
    public BacSyThuY getBacSyTheoMa(String maBS) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || maBS == null) return null;

        String sql = "SELECT maBacSi, maTK, tenChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh FROM BacSyThuY WHERE maBacSi = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maBS);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new BacSyThuY(
                            rs.getString("maBacSi"),
                            rs.getString("maTK"),
                            rs.getString("tenChuyenNganh"),
                            rs.getString("hoTen"),
                            rs.getString("gioiTinh"),
                            rs.getString("sdt"),
                            rs.getString("email"),
                            rs.getDate("ngaySinh")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm bác sĩ: " + e.getMessage());
        }
        return null;
    }
}
