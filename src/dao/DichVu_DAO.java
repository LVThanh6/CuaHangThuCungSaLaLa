package dao;

import connectDB.ConnectDB;
import entity.DichVu_entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DichVu_DAO {
    public ArrayList<DichVu_entity> getAllDichVu() {
        ArrayList<DichVu_entity> services = new ArrayList<>();
        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return services;
        }

        String sql = "SELECT maDV, tenDV, moTa, giaDV, thoiGian, trangThai "
                + "FROM DichVu ORDER BY maDV";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(mapDichVu(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách dịch vụ: " + e.getMessage());
        }
        return services;
    }

    public DichVu_entity getDichVuTheoMa(String maDV) {
        if (maDV == null || maDV.trim().isEmpty()) {
            return null;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return null;
        }

        String sql = "SELECT maDV, tenDV, moTa, giaDV, thoiGian, trangThai "
                + "FROM DichVu WHERE maDV = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDV.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapDichVu(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm dịch vụ: " + e.getMessage());
            return null;
        }
    }

    private DichVu_entity mapDichVu(ResultSet resultSet) throws SQLException {
        java.sql.Time sqlTime = resultSet.getTime("thoiGian");
        return new DichVu_entity(
                resultSet.getString("maDV"),
                resultSet.getString("tenDV"),
                resultSet.getString("moTa"),
                resultSet.getDouble("giaDV"),
                sqlTime == null ? null : sqlTime.toLocalTime(),
                resultSet.getString("trangThai"));
    }
}