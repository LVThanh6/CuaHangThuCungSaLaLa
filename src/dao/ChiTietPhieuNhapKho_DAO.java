package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuNhapKho_entity;
import entity.PhieuNhapKho_entity.TrangThai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Chi tiết phiếu nhập kho".
 *
 * Giả định CSDL (SQL Server):
 *   VatTu(maVT, tenVT, donVi, giaNhap, soLuongTon)
 *   PhieuNhapKho(soPhieu, ngayLap, nhanVien, nhaCungCap, ghiChu, trangThai)
 *   ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia)  -- PK (soPhieu, maVT)
 *
 * Quy tắc: chỉ được thêm / sửa / xóa dòng chi tiết khi phiếu đang "Chờ xác nhận".
 * Các thao tác ghi KHÔNG tự cộng tồn kho; việc cộng tồn do PhieuNhapKho_DAO.xacNhan() đảm nhiệm.
 */
public class ChiTietPhieuNhapKho_DAO {

    private static final String SELECT_BASE =
            "SELECT c.soPhieu, c.maVT, v.tenVT, v.donVi, c.soLuong, c.donGia "
            + "FROM ChiTietPhieuNhap c JOIN VatTu v ON c.maVT = v.maVT ";

    /** Điều kiện: phiếu tồn tại và đang Chờ xác nhận (tham số: soPhieu, trangThai). */
    private static final String PHIEU_CHO_XAC_NHAN =
            "EXISTS (SELECT 1 FROM PhieuNhapKho WHERE soPhieu = ? AND trangThai = ?)";

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================

    /** Tất cả dòng chi tiết của một phiếu. */
    public List<ChiTietPhieuNhapKho_entity> getBySoPhieu(String soPhieu) {
        List<ChiTietPhieuNhapKho_entity> ds = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.soPhieu = ? ORDER BY c.maVT";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một dòng chi tiết theo (soPhieu, maVT); không có -> null. */
    public ChiTietPhieuNhapKho_entity getOne(String soPhieu, String maVT) {
        String sql = SELECT_BASE + "WHERE c.soPhieu = ? AND c.maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Kiểm tra vật tư đã có trong phiếu chưa. */
    public boolean exists(String soPhieu, String maVT) {
        String sql = "SELECT 1 FROM ChiTietPhieuNhap WHERE soPhieu = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Tổng số lượng của phiếu. */
    public int tongSoLuong(String soPhieu) {
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietPhieuNhap WHERE soPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng tiền của phiếu (soLuong x donGia). */
    public long tongTien(String soPhieu) {
        String sql = "SELECT COALESCE(SUM(CAST(soLuong AS BIGINT) * donGia), 0) "
                + "FROM ChiTietPhieuNhap WHERE soPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================================================================
    // Thêm / sửa / xóa
    // ===================================================================

    /** Thêm một dòng vật tư vào phiếu (chỉ khi phiếu Chờ xác nhận và vật tư chưa có trong phiếu). */
    public boolean insert(ChiTietPhieuNhapKho_entity ct) {
        String sql = "INSERT INTO ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia) "
                + "SELECT ?, ?, ?, ? WHERE " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, ct.getSoPhieu());
            ps.setString(2, ct.getMaVT());
            ps.setInt(3, ct.getSoLuong());
            ps.setLong(4, ct.getDonGia());
            ps.setString(5, ct.getSoPhieu());
            ps.setString(6, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // ví dụ: trùng khóa (vật tư đã có trong phiếu)
            return false;
        }
    }

    /** Thêm nhiều dòng cùng lúc trong 1 transaction (chỉ khi phiếu Chờ xác nhận). */
    public boolean insertAll(String soPhieu, List<ChiTietPhieuNhapKho_entity> ds) {
        if (ds == null || ds.isEmpty()) return true;
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            if (!isChoXacNhan(c, soPhieu)) {
                c.rollback();
                return false;
            }
            String sql = "INSERT INTO ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia) VALUES (?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (ChiTietPhieuNhapKho_entity d : ds) {
                    ps.setString(1, soPhieu);
                    ps.setString(2, d.getMaVT());
                    ps.setInt(3, d.getSoLuong());
                    ps.setLong(4, d.getDonGia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
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

    /** Sửa số lượng / đơn giá của một dòng (chỉ khi phiếu Chờ xác nhận). */
    public boolean update(ChiTietPhieuNhapKho_entity ct) {
        String sql = "UPDATE ChiTietPhieuNhap SET soLuong = ?, donGia = ? "
                + "WHERE soPhieu = ? AND maVT = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setLong(2, ct.getDonGia());
            ps.setString(3, ct.getSoPhieu());
            ps.setString(4, ct.getMaVT());
            ps.setString(5, ct.getSoPhieu());
            ps.setString(6, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa một dòng vật tư khỏi phiếu (chỉ khi phiếu Chờ xác nhận). */
    public boolean delete(String soPhieu, String maVT) {
        String sql = "DELETE FROM ChiTietPhieuNhap WHERE soPhieu = ? AND maVT = ? AND "
                + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            ps.setString(3, soPhieu);
            ps.setString(4, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa toàn bộ chi tiết của một phiếu (chỉ khi phiếu Chờ xác nhận). */
    public boolean deleteBySoPhieu(String soPhieu) {
        String sql = "DELETE FROM ChiTietPhieuNhap WHERE soPhieu = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, soPhieu);
            ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() >= 0 && isChoXacNhanQuiet(soPhieu);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================

    private ChiTietPhieuNhapKho_entity map(ResultSet rs) throws SQLException {
        return new ChiTietPhieuNhapKho_entity(
                rs.getString("soPhieu"),
                rs.getString("maVT"),
                rs.getString("tenVT"),
                rs.getString("donVi"),
                rs.getInt("soLuong"),
                rs.getLong("donGia"));
    }

    private boolean isChoXacNhan(Connection c, String soPhieu) throws SQLException {
        String sql = "SELECT 1 FROM PhieuNhapKho WHERE soPhieu = ? AND trangThai = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, TrangThai.CHO_XAC_NHAN.getText());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isChoXacNhanQuiet(String soPhieu) {
        try {
            return isChoXacNhan(conn(), soPhieu);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}