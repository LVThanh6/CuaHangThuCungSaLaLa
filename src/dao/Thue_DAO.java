
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.Thue_entity;

public class Thue_DAO {

    /**
     * Lấy toàn bộ danh sách thuế từ bảng Thue
     */
    public ArrayList<Thue_entity> getAllThue() {
        ArrayList<Thue_entity> dsThue = new ArrayList<>();

        String sql = "SELECT maThue, tenThue, mucThue FROM Thue";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsThue;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maThue = rs.getString("maThue");
                    String tenThue = rs.getString("tenThue");
                    double mucThue = rs.getDouble("mucThue");

                    Thue_entity thue = new Thue_entity(
                            maThue, tenThue, mucThue
                    );

                    dsThue.add(thue);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsThue;
    }

    /**
     * Thêm một loại thuế mới
     */
    public boolean createThue(Thue_entity thue) {
        String sql = "INSERT INTO Thue (maThue, tenThue, mucThue) "
                   + "VALUES (?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, thue.getMaThue());
                ps.setString(2, thue.getTenThue());
                ps.setDouble(3, thue.getMucThue());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa một loại thuế theo mã
     */
    public boolean deleteThue(String maThue) {
        String sql = "DELETE FROM Thue WHERE maThue = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maThue);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin thuế theo mã
     */
    public boolean updateThue(Thue_entity thue) {
        String sql = "UPDATE Thue "
                   + "SET tenThue = ?, mucThue = ? "
                   + "WHERE maThue = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, thue.getTenThue());
                ps.setDouble(2, thue.getMucThue());
                ps.setString(3, thue.getMaThue());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm một loại thuế theo mã chính xác
     */
    public Thue_entity getThueTheoMa(String maThue) {
        String sql = "SELECT maThue, tenThue, mucThue "
                   + "FROM Thue WHERE maThue = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maThue);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ma = rs.getString("maThue");
                        String ten = rs.getString("tenThue");
                        double muc = rs.getDouble("mucThue");

                        return new Thue_entity(ma, ten, muc);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

