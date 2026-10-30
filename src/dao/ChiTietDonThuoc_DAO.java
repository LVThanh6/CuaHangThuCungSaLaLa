package dao;

import connectDB.ConnectDB;
import entity.ChiTietDonThuoc_entity;
import entity.Thuoc_entity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDonThuoc_DAO {

    public List<ChiTietDonThuoc_entity> getChiTietTheoMaDon(String maDonThuoc) throws SQLException {
        List<ChiTietDonThuoc_entity> danhSach = new ArrayList<>();
        String sql = "SELECT c.maCTDT, c.maDonThuoc, c.maThuoc, t.tenThuoc, t.donViTinh, c.soLuong, c.lieuLuong "
                + "FROM ChiTietDonThuoc c JOIN Thuoc t ON t.maThuoc = c.maThuoc "
                + "WHERE c.maDonThuoc = ? ORDER BY c.maCTDT";
        try (Connection con = ketNoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDonThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietDonThuoc_entity chiTiet = new ChiTietDonThuoc_entity(rs.getString("maCTDT"),
                            rs.getString("maDonThuoc"), rs.getString("maThuoc"),
                            rs.getBigDecimal("soLuong"), rs.getBigDecimal("lieuLuong"));
                    chiTiet.setTenThuoc(rs.getString("tenThuoc"));
                    chiTiet.setDonViTinh(rs.getString("donViTinh"));
                    danhSach.add(chiTiet);
                }
            }
        }
        return danhSach;
    }

    public List<Thuoc_entity> getAllThuoc() throws SQLException {
        List<Thuoc_entity> danhSach = new ArrayList<>();
        String sql = "SELECT maThuoc, tenThuoc, donViTinh, huongDanSuDung FROM Thuoc ORDER BY tenThuoc";
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new Thuoc_entity(rs.getString("maThuoc"), rs.getString("tenThuoc"),
                        rs.getString("donViTinh"), rs.getString("huongDanSuDung")));
            }
        }
        return danhSach;
    }

    String getMaChiTietTiepTheo(Connection con) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maCTDT, 5, 18) AS INT)), 0) "
                + "FROM ChiTietDonThuoc WHERE maCTDT LIKE 'CTDT%'";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("CTDT%03d", rs.getInt(1) + 1);
            }
        }
        throw new SQLException("Không thể phát sinh mã chi tiết đơn thuốc.");
    }

    private Connection ketNoi() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        return con;
    }
}
