package dao.ThuCung;

import connectDB.ConnectDB;
import entity.ThuCung.HoSoSucKhoe_entity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class HoSoSucKhoe_DAO {

    private static final String SELECT_COLUMNS = "SELECT h.maHSSK, h.maTC, h.maBacSi, h.ngayKham, "
            + "h.trieuChung, h.chuanDoan, h.canNang, tc.tenTC, tc.chuHo, bs.hoTen "
            + "FROM HoSoSucKhoe h "
            + "LEFT JOIN ThuCung tc ON tc.maTC = h.maTC "
            + "LEFT JOIN BacSyThuY bs ON bs.maBacSi = h.maBacSi ";

    public List<HoSoSucKhoe_entity> getAllHoSoSucKhoe() throws SQLException {
        List<HoSoSucKhoe_entity> danhSach = new ArrayList<>();
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(SELECT_COLUMNS + "ORDER BY h.ngayKham DESC, h.maHSSK");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(mapHoSo(rs));
            }
        }
        return danhSach;
    }

    public List<HoSoSucKhoe_entity> getHoSoSucKhoeTheoMaTC(String maTC) throws SQLException {
        List<HoSoSucKhoe_entity> danhSach = new ArrayList<>();
        if (maTC == null || maTC.isBlank()) return danhSach;

        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(SELECT_COLUMNS + "WHERE h.maTC = ? ORDER BY h.ngayKham DESC, h.maHSSK")) {
            ps.setString(1, maTC.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapHoSo(rs));
                }
            }
        }
        return danhSach;
    }

    public HoSoSucKhoe_entity getHoSoSucKhoeTheoMa(String maHSSK) throws SQLException {
        if (maHSSK == null || maHSSK.isBlank()) return null;
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(SELECT_COLUMNS + "WHERE h.maHSSK = ?")) {
            ps.setString(1, maHSSK.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapHoSo(rs) : null;
            }
        }
    }

    public String getMaTiepTheo() throws SQLException {
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement("SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maHSSK, 3, 18) AS INT)), 0) "
                     + "FROM HoSoSucKhoe WHERE maHSSK LIKE 'HS%'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("HS%03d", rs.getInt(1) + 1);
            }
        }
        throw new SQLException("Không thể phát sinh mã hồ sơ sức khỏe.");
    }

    public boolean createHoSoSucKhoe(HoSoSucKhoe_entity hoSo) throws SQLException {
        if (hoSo == null) return false;
        String sql = "INSERT INTO HoSoSucKhoe (maHSSK, maTC, maBacSi, ngayKham, trieuChung, chuanDoan, canNang) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ketNoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            ganDuLieu(ps, hoSo);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateHoSoSucKhoe(HoSoSucKhoe_entity hoSo) throws SQLException {
        if (hoSo == null) return false;
        String sql = "UPDATE HoSoSucKhoe SET maTC = ?, maBacSi = ?, ngayKham = ?, trieuChung = ?, "
                + "chuanDoan = ?, canNang = ? WHERE maHSSK = ?";
        try (Connection con = ketNoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoSo.getMaTC() != null ? hoSo.getMaTC().getMaTC() : null);
            ps.setString(2, hoSo.getMaBacSi() != null ? hoSo.getMaBacSi().getMaBacSi() : null);
            ps.setTimestamp(3, hoSo.getNgayKham() == null ? null : Timestamp.valueOf(hoSo.getNgayKham()));
            ps.setString(4, hoSo.getTrieuChung());
            ps.setString(5, hoSo.getChuanDoan());
            if (hoSo.getCanNang() == null) {
                ps.setNull(6, Types.DECIMAL);
            } else {
                ps.setBigDecimal(6, hoSo.getCanNang());
            }
            ps.setString(7, hoSo.getMaHSSK());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteHoSoSucKhoe(String maHSSK) throws SQLException {
        if (maHSSK == null || maHSSK.isBlank()) return false;
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement("DELETE FROM HoSoSucKhoe WHERE maHSSK = ?")) {
            ps.setString(1, maHSSK.trim());
            return ps.executeUpdate() == 1;
        }
    }

    private void ganDuLieu(PreparedStatement ps, HoSoSucKhoe_entity hoSo) throws SQLException {
        ps.setString(1, hoSo.getMaHSSK());
        ps.setString(2, hoSo.getMaTC() != null ? hoSo.getMaTC().getMaTC() : null);
        ps.setString(3, hoSo.getMaBacSi() != null ? hoSo.getMaBacSi().getMaBacSi() : null);
        ps.setTimestamp(4, hoSo.getNgayKham() == null ? null : Timestamp.valueOf(hoSo.getNgayKham()));
        ps.setString(5, hoSo.getTrieuChung());
        ps.setString(6, hoSo.getChuanDoan());
        if (hoSo.getCanNang() == null) {
            ps.setNull(7, Types.DECIMAL);
        } else {
            ps.setBigDecimal(7, hoSo.getCanNang());
        }
    }

    private HoSoSucKhoe_entity mapHoSo(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("ngayKham");
        HoSoSucKhoe_entity hoSo = new HoSoSucKhoe_entity(rs.getString("maHSSK"), rs.getString("maTC"),
                rs.getString("maBacSi"), timestamp == null ? null : timestamp.toLocalDateTime(),
                rs.getString("trieuChung"), rs.getString("chuanDoan"), rs.getBigDecimal("canNang"));
        hoSo.setTenThuCung(rs.getString("tenTC"));
        hoSo.setChuHo(rs.getString("chuHo"));
        hoSo.setTenBacSi(rs.getString("hoTen"));
        return hoSo;
    }

    private Connection ketNoi() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        return con;
    }
}
