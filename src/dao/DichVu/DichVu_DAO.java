package dao.DichVu;

import connectDB.ConnectDB;
import entity.DichVu.DichVu_entity;

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

    public boolean createDichVu(DichVu_entity dv) {
        if (dv == null || dv.getMaDV() == null || dv.getTenDV() == null) {
            return false;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return false;
        }

        String sql = "INSERT INTO DichVu (maDV, tenDV, moTa, giaDV, thoiGian, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dv.getMaDV().trim());
            statement.setString(2, dv.getTenDV().trim());
            statement.setString(3, dv.getMoTa());
            statement.setDouble(4, dv.getGiaDV());
            statement.setTime(5, dv.getThoiGian() == null ? java.sql.Time.valueOf("00:30:00") : java.sql.Time.valueOf(dv.getThoiGian()));
            statement.setString(6, dv.getTrangThai() == null ? "Đang áp dụng" : dv.getTrangThai().trim());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm dịch vụ: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDichVu(DichVu_entity dv) {
        if (dv == null || dv.getMaDV() == null) {
            return false;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return false;
        }

        String sql = "UPDATE DichVu SET tenDV = ?, moTa = ?, giaDV = ?, thoiGian = ?, trangThai = ? "
                + "WHERE maDV = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dv.getTenDV().trim());
            statement.setString(2, dv.getMoTa());
            statement.setDouble(3, dv.getGiaDV());
            statement.setTime(4, dv.getThoiGian() == null ? java.sql.Time.valueOf("00:30:00") : java.sql.Time.valueOf(dv.getThoiGian()));
            statement.setString(5, dv.getTrangThai() == null ? "Đang áp dụng" : dv.getTrangThai().trim());
            statement.setString(6, dv.getMaDV().trim());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật dịch vụ: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDichVu(String maDV) {
        if (maDV == null || maDV.trim().isEmpty()) {
            return false;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return false;
        }

        String sql = "DELETE FROM DichVu WHERE maDV = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDV.trim());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa dịch vụ: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTrangThai(String maDV, String trangThai) {
        if (maDV == null || maDV.trim().isEmpty() || trangThai == null) {
            return false;
        }

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return false;
        }

        String sql = "UPDATE DichVu SET trangThai = ? WHERE maDV = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trangThai.trim());
            statement.setString(2, maDV.trim());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái dịch vụ: " + e.getMessage());
            return false;
        }
    }

    public String getMaTiepTheo() {
        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            return "DV001";
        }

        String sql = "SELECT TOP 1 maDV FROM DichVu ORDER BY maDV DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String ma = resultSet.getString("maDV");
                if (ma != null && ma.toUpperCase().startsWith("DV")) {
                    try {
                        int stt = Integer.parseInt(ma.substring(2).trim());
                        return String.format("DV%03d", stt + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sinh mã dịch vụ: " + e.getMessage());
        }
        return "DV001";
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