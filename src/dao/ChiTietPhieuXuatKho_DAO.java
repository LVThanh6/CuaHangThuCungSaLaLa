package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuXuatKho_entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Chi tiết phiếu xuất kho".
 *
 * Giả định CSDL (SQL Server):
 *   VatTu(maVT, tenVT, donVi, giaNhap, soLuongTon)
 *   ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia)   -- khóa chính (maPhieu, maVT)
 *
 * Các hàm có tham số Connection dùng để chạy chung transaction với PhieuXuatKho_DAO.
 * Các hàm không có tham số Connection tự mở kết nối và tự commit.
 */
public class ChiTietPhieuXuatKho_DAO {

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================
    /** Các dòng vật tư của một phiếu (JOIN VatTu để lấy tên, đơn vị). */
    public List<ChiTietPhieuXuatKho_entity> getByMaPhieu(String maPhieu) {
        List<ChiTietPhieuXuatKho_entity> ds = new ArrayList<>();
        String sql = "SELECT c.maPhieu, c.maVT, v.tenVT, v.donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuXuat c JOIN VatTu v ON c.maVT = v.maVT "
                + "WHERE c.maPhieu = ? ORDER BY c.maVT";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một dòng chi tiết theo (maPhieu, maVT); không có -> null. */
    public ChiTietPhieuXuatKho_entity getById(String maPhieu, String maVT) {
        String sql = "SELECT c.maPhieu, c.maVT, v.tenVT, v.donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuXuat c JOIN VatTu v ON c.maVT = v.maVT "
                + "WHERE c.maPhieu = ? AND c.maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Kiểm tra vật tư đã có trong phiếu chưa. */
    public boolean tonTai(String maPhieu, String maVT) {
        String sql = "SELECT 1 FROM ChiTietPhieuXuat WHERE maPhieu = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Tổng số lượng xuất của phiếu. */
    public int tongSoLuong(String maPhieu) {
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietPhieuXuat WHERE maPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng tiền (soLuong x donGia) của phiếu. */
    public long tongTien(String maPhieu) {
        String sql = "SELECT COALESCE(SUM(CAST(soLuong AS BIGINT) * donGia), 0) "
                + "FROM ChiTietPhieuXuat WHERE maPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================================================================
    // Thêm / sửa / xóa (tự quản lý kết nối)
    // ===================================================================
    /** Thêm một dòng chi tiết. Trả về false nếu trùng khóa hoặc lỗi. */
    public boolean insert(ChiTietPhieuXuatKho_entity ct) {
        try (Connection c = null == null ? conn() : null) {
            return insert(c, ct);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật số lượng / đơn giá của một dòng. */
    public boolean update(ChiTietPhieuXuatKho_entity ct) {
        String sql = "UPDATE ChiTietPhieuXuat SET soLuong = ?, donGia = ? WHERE maPhieu = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setLong(2, ct.getDonGia());
            ps.setString(3, ct.getMaPhieu() != null ? ct.getMaPhieu().getMaPhieu() : null);
            ps.setString(4, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa một dòng vật tư khỏi phiếu. */
    public boolean delete(String maPhieu, String maVT) {
        String sql = "DELETE FROM ChiTietPhieuXuat WHERE maPhieu = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ps.setString(2, maVT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa toàn bộ chi tiết của một phiếu. */
    public boolean deleteByMaPhieu(String maPhieu) {
        try {
            deleteByMaPhieu(conn(), maPhieu);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thay toàn bộ chi tiết của phiếu bằng danh sách mới (1 transaction).
     * Lưu ý: chỉ nên gọi khi phiếu còn ở trạng thái Nháp.
     */
    public boolean replaceAll(String maPhieu, List<ChiTietPhieuXuatKho_entity> ds) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            deleteByMaPhieu(c, maPhieu);
            insertAll(c, maPhieu, ds);
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    // ===================================================================
    // Hàm dùng chung transaction (truyền Connection từ bên ngoài)
    // ===================================================================
    /** Thêm một dòng bằng connection đang dùng; ném SQLException để caller rollback. */
    public boolean insert(Connection c, ChiTietPhieuXuatKho_entity ct) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ct.getMaPhieu() != null ? ct.getMaPhieu().getMaPhieu() : null);
            ps.setString(2, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
            ps.setInt(3, ct.getSoLuong());
            ps.setLong(4, ct.getDonGia());
            return ps.executeUpdate() > 0;
        }
    }

    /** Thêm nhiều dòng bằng batch; maPhieu của từng dòng được gán theo tham số maPhieu. */
    public void insertAll(Connection c, String maPhieu, List<ChiTietPhieuXuatKho_entity> ds) throws SQLException {
        if (ds == null || ds.isEmpty()) return;
        String sql = "INSERT INTO ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTietPhieuXuatKho_entity ct : ds) {
                ps.setString(1, maPhieu);
                ps.setString(2, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
                ps.setInt(3, ct.getSoLuong());
                ps.setLong(4, ct.getDonGia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Xóa toàn bộ chi tiết của phiếu bằng connection đang dùng. */
    public void deleteByMaPhieu(Connection c, String maPhieu) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuXuat WHERE maPhieu = ?")) {
            ps.setString(1, maPhieu);
            ps.executeUpdate();
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================
    private ChiTietPhieuXuatKho_entity mapRow(ResultSet rs) throws SQLException {
        return new ChiTietPhieuXuatKho_entity(
                rs.getString("maPhieu"),
                rs.getString("maVT"),
                rs.getString("tenVT"),
                rs.getString("donVi"),
                rs.getInt("soLuong"),
                rs.getLong("donGia"));
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}