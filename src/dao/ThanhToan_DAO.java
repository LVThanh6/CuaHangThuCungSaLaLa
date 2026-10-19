
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.ThanhToan_entity;

public class ThanhToan_DAO {

    /**
     * Lấy toàn bộ danh sách thanh toán từ bảng ThanhToan
     */
    public ArrayList<ThanhToan_entity> getAllThanhToan() {
        ArrayList<ThanhToan_entity> dsThanhToan = new ArrayList<>();

        String sql = "SELECT maThanhToan, soTien, thoiGian, "
                   + "phuongThuc, trangThai FROM ThanhToan";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsThanhToan;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maThanhToan = rs.getString("maThanhToan");
                    double soTien = rs.getDouble("soTien");

                    Date ngay = rs.getDate("thoiGian");
                    java.time.LocalDate thoiGian =
                            ngay != null ? ngay.toLocalDate() : null;

                    String phuongThuc = rs.getString("phuongThuc");
                    String trangThai = rs.getString("trangThai");

                    ThanhToan_entity tt = new ThanhToan_entity(
                            maThanhToan,
                            soTien,
                            thoiGian,
                            phuongThuc,
                            trangThai
                    );

                    dsThanhToan.add(tt);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsThanhToan;
    }

    /**
     * Thêm một thanh toán mới
     */
    public boolean createThanhToan(ThanhToan_entity tt) {
        String sql = "INSERT INTO ThanhToan "
                   + "(maThanhToan, soTien, thoiGian, phuongThuc, trangThai) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tt.getMaThanhToan());
                ps.setDouble(2, tt.getSoTien());

                if (tt.getThoiGian() != null) {
                    ps.setDate(3, Date.valueOf(tt.getThoiGian()));
                } else {
                    ps.setNull(3, java.sql.Types.DATE);
                }

                ps.setString(4, tt.getPhuongThuc());
                ps.setString(5, tt.getTrangThai());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa một thanh toán theo mã
     */
    public boolean deleteThanhToan(String maThanhToan) {
        String sql = "DELETE FROM ThanhToan WHERE maThanhToan = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maThanhToan);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin thanh toán theo mã
     */
    public boolean updateThanhToan(ThanhToan_entity tt) {
        String sql = "UPDATE ThanhToan "
                   + "SET soTien = ?, thoiGian = ?, "
                   + "phuongThuc = ?, trangThai = ? "
                   + "WHERE maThanhToan = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, tt.getSoTien());

                if (tt.getThoiGian() != null) {
                    ps.setDate(2, Date.valueOf(tt.getThoiGian()));
                } else {
                    ps.setNull(2, java.sql.Types.DATE);
                }

                ps.setString(3, tt.getPhuongThuc());
                ps.setString(4, tt.getTrangThai());
                ps.setString(5, tt.getMaThanhToan());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm một thanh toán theo mã chính xác
     */
    public ThanhToan_entity getThanhToanTheoMa(String maThanhToan) {
        String sql = "SELECT maThanhToan, soTien, thoiGian, "
                   + "phuongThuc, trangThai "
                   + "FROM ThanhToan WHERE maThanhToan = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maThanhToan);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double soTien = rs.getDouble("soTien");

                        Date ngay = rs.getDate("thoiGian");
                        java.time.LocalDate thoiGian =
                                ngay != null ? ngay.toLocalDate() : null;

                        String phuongThuc = rs.getString("phuongThuc");
                        String trangThai = rs.getString("trangThai");

                        return new ThanhToan_entity(
                                rs.getString("maThanhToan"),
                                soTien,
                                thoiGian,
                                phuongThuc,
                                trangThai
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
