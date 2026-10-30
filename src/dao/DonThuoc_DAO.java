package dao;

import connectDB.ConnectDB;
import entity.ChiTietDonThuoc_entity;
import entity.DonThuoc_entity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DonThuoc_DAO {

    private final ChiTietDonThuoc_DAO chiTietDAO = new ChiTietDonThuoc_DAO();
    private static final String SELECT_COLUMNS = "SELECT d.maDonThuoc, d.maTC, d.maBacSi, d.maHSSK, d.ngayKeDon, "
            + "d.ghiChu, d.trangThai, tc.tenTC, bs.hoTen, "
            + "(SELECT COUNT(DISTINCT c.maThuoc) FROM ChiTietDonThuoc c WHERE c.maDonThuoc = d.maDonThuoc) AS soLoaiThuoc "
            + "FROM DonThuoc d JOIN ThuCung tc ON tc.maTC = d.maTC "
            + "JOIN BacSyThuY bs ON bs.maBacSi = d.maBacSi ";

    public List<DonThuoc_entity> getAllDonThuoc() throws SQLException {
        List<DonThuoc_entity> danhSach = new java.util.ArrayList<>();
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(SELECT_COLUMNS + "ORDER BY d.ngayKeDon DESC, d.maDonThuoc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(mapDonThuoc(rs));
            }
        }
        return danhSach;
    }

    public DonThuoc_entity getDonThuocTheoMa(String maDonThuoc) throws SQLException {
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(SELECT_COLUMNS + "WHERE d.maDonThuoc = ?")) {
            ps.setString(1, maDonThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapDonThuoc(rs) : null;
            }
        }
    }

    public String getMaTiepTheo() throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maDonThuoc, 3, 18) AS INT)), 0) "
                + "FROM DonThuoc WHERE maDonThuoc LIKE 'DT%'";
        try (Connection con = ketNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("DT%03d", rs.getInt(1) + 1);
            }
        }
        throw new SQLException("Không thể phát sinh mã đơn thuốc.");
    }

    public boolean createDonThuoc(DonThuoc_entity donThuoc, List<ChiTietDonThuoc_entity> chiTiet)
            throws SQLException {
        validateDonThuoc(donThuoc, chiTiet);
        String sql = "INSERT INTO DonThuoc (maDonThuoc, maTC, maBacSi, maHSSK, ngayKeDon, ghiChu, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ganDuLieu(ps, donThuoc);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }
                chenChiTiet(con, donThuoc.getMaDonThuoc(), chiTiet);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean updateDonThuoc(DonThuoc_entity donThuoc, List<ChiTietDonThuoc_entity> chiTiet)
            throws SQLException {
        validateDonThuoc(donThuoc, chiTiet);
        String sql = "UPDATE DonThuoc SET maTC = ?, maBacSi = ?, maHSSK = ?, ngayKeDon = ?, ghiChu = ?, "
                + "trangThai = ? WHERE maDonThuoc = ?";
        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int capNhat;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, donThuoc.getMaTC());
                    ps.setString(2, donThuoc.getMaBacSi());
                    ps.setString(3, donThuoc.getMaHSSK());
                    ps.setTimestamp(4, Timestamp.valueOf(donThuoc.getNgayKeDon()));
                    ps.setString(5, donThuoc.getGhiChu());
                    ps.setString(6, donThuoc.getTrangThai());
                    ps.setString(7, donThuoc.getMaDonThuoc());
                    capNhat = ps.executeUpdate();
                }
                if (capNhat != 1) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDonThuoc WHERE maDonThuoc = ?")) {
                    ps.setString(1, donThuoc.getMaDonThuoc());
                    ps.executeUpdate();
                }
                chenChiTiet(con, donThuoc.getMaDonThuoc(), chiTiet);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean deleteDonThuoc(String maDonThuoc) throws SQLException {
        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDonThuoc WHERE maDonThuoc = ?")) {
                    ps.setString(1, maDonThuoc);
                    ps.executeUpdate();
                }
                int deleted;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM DonThuoc WHERE maDonThuoc = ?")) {
                    ps.setString(1, maDonThuoc);
                    deleted = ps.executeUpdate();
                }
                con.commit();
                return deleted == 1;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }

    public List<ChiTietDonThuoc_entity> getChiTietDonThuoc(String maDonThuoc) throws SQLException {
        return chiTietDAO.getChiTietTheoMaDon(maDonThuoc);
    }

    private void chenChiTiet(Connection con, String maDonThuoc, List<ChiTietDonThuoc_entity> chiTiet)
            throws SQLException {
        if (chiTiet == null || chiTiet.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO ChiTietDonThuoc (maCTDT, maDonThuoc, maThuoc, soLuong, lieuLuong) "
                + "VALUES (?, ?, ?, ?, ?)";
        int nextId = Integer.parseInt(chiTietDAO.getMaChiTietTiepTheo(con).substring(4));
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ChiTietDonThuoc_entity item : chiTiet) {
                ps.setString(1, String.format("CTDT%03d", nextId++));
                ps.setString(2, maDonThuoc);
                ps.setString(3, item.getMaThuoc());
                ps.setBigDecimal(4, item.getSoLuong());
                ps.setBigDecimal(5, item.getLieuLuong());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void ganDuLieu(PreparedStatement ps, DonThuoc_entity donThuoc) throws SQLException {
        ps.setString(1, donThuoc.getMaDonThuoc());
        ps.setString(2, donThuoc.getMaTC());
        ps.setString(3, donThuoc.getMaBacSi());
        ps.setString(4, donThuoc.getMaHSSK());
        ps.setTimestamp(5, Timestamp.valueOf(donThuoc.getNgayKeDon()));
        ps.setString(6, donThuoc.getGhiChu());
        ps.setString(7, donThuoc.getTrangThai());
    }

    private void validateDonThuoc(DonThuoc_entity donThuoc, List<ChiTietDonThuoc_entity> chiTiet) {
        if (donThuoc == null || donThuoc.getMaDonThuoc() == null
                || donThuoc.getMaTC() == null || donThuoc.getMaBacSi() == null
                || donThuoc.getMaHSSK() == null || donThuoc.getNgayKeDon() == null
                || !("Chờ lấy thuốc".equals(donThuoc.getTrangThai())
                        || "Đã cấp thuốc".equals(donThuoc.getTrangThai()))) {
            throw new IllegalArgumentException("Đơn thuốc phải có ngày kê và trạng thái hợp lệ.");
        }
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Đơn thuốc phải có ít nhất một loại thuốc.");
        }
        for (ChiTietDonThuoc_entity item : chiTiet) {
            if (item == null || item.getMaThuoc() == null
                    || item.getSoLuong() == null || item.getLieuLuong() == null
                    || item.getSoLuong().signum() <= 0 || item.getLieuLuong().signum() <= 0) {
                throw new IllegalArgumentException("Chi tiết thuốc phải có thuốc, số lượng và liều lượng hợp lệ.");
            }
        }
    }

    private DonThuoc_entity mapDonThuoc(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("ngayKeDon");
        DonThuoc_entity donThuoc = new DonThuoc_entity(rs.getString("maDonThuoc"), rs.getString("maTC"),
                rs.getString("maBacSi"), rs.getString("maHSSK"),
                timestamp == null ? null : timestamp.toLocalDateTime(),
                rs.getString("ghiChu"), rs.getString("trangThai"));
        donThuoc.setTenThuCung(rs.getString("tenTC"));
        donThuoc.setTenBacSi(rs.getString("hoTen"));
        donThuoc.setSoLoaiThuoc(rs.getInt("soLoaiThuoc"));
        return donThuoc;
    }

    private Connection ketNoi() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        return con;
    }
}
