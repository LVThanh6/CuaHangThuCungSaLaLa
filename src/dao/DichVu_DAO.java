package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.DichVu_entity;

public class DichVu_DAO {

    // 1. Lấy danh sách tất cả dịch vụ
    public ArrayList<DichVu_entity> getAllDichVu() {
        ArrayList<DichVu_entity> dsDichVu = new ArrayList<>();

        String sql = "SELECT maDV, tenDV, moTa, giaDV, thoiGian, trangThai "
                   + "FROM DichVu";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return dsDichVu;
            }
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
            	java.sql.Time sqlTime = rs.getTime("thoiGian");

            	DichVu_entity dv = new DichVu_entity(
            			rs.getString("maDV"),
            			rs.getString("tenDV"),
            			rs.getString("moTa"),
            			rs.getDouble("giaDV"),
            			sqlTime != null ? sqlTime.toLocalTime() : null,
            			rs.getString("trangThai")
    );

    dsDichVu.add(dv);
}


            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsDichVu;
    }

    // 2. Thêm dịch vụ
    public boolean createDichVu(DichVu_entity dv) {
        String sql = "INSERT INTO DichVu "
                   + "(maDV, tenDV, moTa, giaDV, thoiGian, trangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, dv.getMaDV());
            stmt.setString(2, dv.getTenDV());
            stmt.setString(3, dv.getMoTa());
            stmt.setDouble(4, dv.getGiaDV());

            if (dv.getThoiGian() != null) {
                stmt.setTime(5, Time.valueOf(dv.getThoiGian()));
            } else {
                stmt.setNull(5, java.sql.Types.TIME);
            }

            stmt.setString(6, dv.getTrangThai());

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 3. Xóa dịch vụ theo mã
    public boolean deleteDichVu(String maDV) {
        String sql = "DELETE FROM DichVu WHERE maDV = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, maDV);

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 4. Cập nhật dịch vụ
    public boolean updateDichVu(DichVu_entity dv) {
        String sql = "UPDATE DichVu "
                   + "SET tenDV = ?, moTa = ?, giaDV = ?, "
                   + "thoiGian = ?, trangThai = ? "
                   + "WHERE maDV = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, dv.getTenDV());
            stmt.setString(2, dv.getMoTa());
            stmt.setDouble(3, dv.getGiaDV());

            if (dv.getThoiGian() != null) {
                stmt.setTime(4, Time.valueOf(dv.getThoiGian()));
            } else {
                stmt.setNull(4, java.sql.Types.TIME);
            }

            stmt.setString(5, dv.getTrangThai());
            stmt.setString(6, dv.getMaDV());

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 5. Tìm dịch vụ theo mã
    public DichVu_entity getDichVuTheoMa(String maDV) {
        String sql = "SELECT maDV, tenDV, moTa, giaDV, thoiGian, trangThai "
                   + "FROM DichVu WHERE maDV = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return null;
            }
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, maDV);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	Time sqlTime = rs.getTime("thoiGian");

            	DichVu_entity dv = new DichVu_entity(
            	    rs.getString("maDV"),
            	    rs.getString("tenDV"),
            	    rs.getString("moTa"),
            	    rs.getDouble("giaDV"),
            	    sqlTime != null ? sqlTime.toLocalTime() : null,
            	    rs.getString("trangThai")
            	);

                rs.close();
                stmt.close();

                return dv;
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}