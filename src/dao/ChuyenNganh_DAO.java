package dao;

import connectDB.ConnectDB;
import entity.ChuyenNganh_entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChuyenNganh_DAO {

    public List<ChuyenNganh_entity> getAll() throws SQLException {
        List<ChuyenNganh_entity> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT maChuyenNganh, tenChuyenNganh FROM ChuyenNganh ORDER BY tenChuyenNganh";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new ChuyenNganh_entity(
                    rs.getString("maChuyenNganh"),
                    rs.getString("tenChuyenNganh")
                ));
            }
        }
        return ds;
    }

    public String getMaTiepTheo() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return "CN001";

        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(maChuyenNganh, 3, LEN(maChuyenNganh)) AS INT)), 0) FROM ChuyenNganh WHERE maChuyenNganh LIKE 'CN%'";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("CN%03d", max + 1);
            }
        }
        return "CN001";
    }

    public boolean them(String tenChuyenNganh) throws SQLException {
        if (tenChuyenNganh == null || tenChuyenNganh.isBlank()) return false;
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String ma = getMaTiepTheo();
        String sql = "INSERT INTO ChuyenNganh (maChuyenNganh, tenChuyenNganh) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, tenChuyenNganh.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public String getMaTheoTen(String tenChuyenNganh) throws SQLException {
        if (tenChuyenNganh == null) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT maChuyenNganh FROM ChuyenNganh WHERE tenChuyenNganh = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenChuyenNganh.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }
}
