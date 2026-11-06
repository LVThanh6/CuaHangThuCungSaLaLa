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
 * DAO "Chi tiết phiếu nhập kho" - làm việc trực tiếp với CSDL SALALA:
 *   ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong)   -- PK maCTPNK
 *   PhieuNhapKho(maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
 *   VatTu(maVT, ..., tenVT, ..., maThuoc)  +  Thuoc(maThuoc, donViTinh)
 *
 * Quy tắc: chỉ thêm / sửa / xóa dòng chi tiết khi phiếu đang "Chờ xác nhận".
 * Các thao tác ghi KHÔNG cộng tồn kho; việc đó do PhieuNhapKho_DAO.xacNhan() đảm nhiệm.
 * Mỗi (maPNK, maVT) chỉ có một dòng.
 */
public class ChiTietPhieuNhapKho_DAO {

    private static final String SELECT_BASE =
            "SELECT c.maPNK, c.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, c.soLuong, c.donGia "
            + "FROM ChiTietPhieuNhapKho c "
            + "JOIN VatTu v ON c.maVT = v.maVT "
            + "LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc ";

    /** Điều kiện: phiếu tồn tại và đang Chờ xác nhận (tham số: maPNK, trangThai). */
    private static final String PHIEU_CHO_XAC_NHAN =
            "EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPNK = ? AND trangThai = ?)";

    private String lastError = null;

    public String getLastError() { return lastError; }

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    private void setError(SQLException e) {
        e.printStackTrace();
        lastError = e.getMessage();
    }

    // ---- sinh mã chi tiết CTPNK01, CTPNK02... (dùng chung với PhieuNhapKho_DAO) ----
    static int maxChiTietSo(Connection c) throws SQLException {
        int max = 0;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT maCTPNK FROM ChiTietPhieuNhapKho WHERE maCTPNK LIKE 'CTPNK%'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    max = Math.max(max, Integer.parseInt(rs.getString(1).substring(5)));
                } catch (NumberFormatException ignored) { }
            }
        }
        return max;
    }

    static String maChiTiet(int so) { return String.format("CTPNK%02d", so); }

    // ===================================================================
    // Truy vấn
    // ===================================================================

    /** Tất cả dòng chi tiết của một phiếu. */
    public List<ChiTietPhieuNhapKho_entity> getBySoPhieu(String soPhieu) {
        lastError = null;
        List<ChiTietPhieuNhapKho_entity> ds = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.maPNK = ? ORDER BY c.maCTPNK";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        } catch (SQLException e) {
            setError(e);
        }
        return ds;
    }

    /** Một dòng chi tiết theo (soPhieu, maVT); không có -> null. */
    public ChiTietPhieuNhapKho_entity getOne(String soPhieu, String maVT) {
        lastError = null;
        String sql = SELECT_BASE + "WHERE c.maPNK = ? AND c.maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            setError(e);
        }
        return null;
    }

    /** Kiểm tra vật tư đã có trong phiếu chưa. */
    public boolean exists(String soPhieu, String maVT) {
        lastError = null;
        String sql = "SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    /** Tổng số lượng của phiếu. */
    public int tongSoLuong(String soPhieu) {
        lastError = null;
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietPhieuNhapKho WHERE maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            setError(e);
        }
        return 0;
    }

    /** Tổng tiền của phiếu (soLuong x donGia). */
    public long tongTien(String soPhieu) {
        lastError = null;
        String sql = "SELECT COALESCE(SUM(soLuong * donGia), 0) FROM ChiTietPhieuNhapKho WHERE maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            setError(e);
        }
        return 0;
    }

    // ===================================================================
    // Thêm / sửa / xóa
    // ===================================================================

    /** Thêm một dòng vật tư vào phiếu (chỉ khi phiếu Chờ xác nhận và vật tư chưa có trong phiếu). */
    public boolean insert(ChiTietPhieuNhapKho_entity ct) {
        lastError = null;
        String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) "
                + "SELECT ?, ?, ?, ?, ? WHERE " + PHIEU_CHO_XAC_NHAN
                + " AND NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND maVT = ?)";
        try {
            Connection c = conn();
            String ma = maChiTiet(maxChiTietSo(c) + 1);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, ct.getSoPhieu());
                ps.setString(3, ct.getMaVT());
                ps.setLong(4, ct.getDonGia());
                ps.setInt(5, ct.getSoLuong());
                ps.setString(6, ct.getSoPhieu());
                ps.setString(7, TrangThai.CHO_XAC_NHAN.getText());
                ps.setString(8, ct.getSoPhieu());
                ps.setString(9, ct.getMaVT());
                boolean ok = ps.executeUpdate() > 0;
                if (!ok) lastError = "Phiếu không ở trạng thái Chờ xác nhận hoặc vật tư đã có trong phiếu.";
                return ok;
            }
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    /** Thêm nhiều dòng cùng lúc trong 1 transaction (chỉ khi phiếu Chờ xác nhận). */
    public boolean insertAll(String soPhieu, List<ChiTietPhieuNhapKho_entity> ds) {
        lastError = null;
        if (ds == null || ds.isEmpty()) return true;
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            if (!isChoXacNhan(c, soPhieu)) {
                c.rollback();
                lastError = "Phiếu không ở trạng thái Chờ xác nhận.";
                return false;
            }
            int so = maxChiTietSo(c);
            String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (ChiTietPhieuNhapKho_entity d : ds) {
                    ps.setString(1, maChiTiet(++so));
                    ps.setString(2, soPhieu);
                    ps.setString(3, d.getMaVT());
                    ps.setLong(4, d.getDonGia());
                    ps.setInt(5, d.getSoLuong());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            setError(e);
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    /** Sửa số lượng / đơn giá của một dòng (chỉ khi phiếu Chờ xác nhận). */
    public boolean update(ChiTietPhieuNhapKho_entity ct) {
        lastError = null;
        String sql = "UPDATE ChiTietPhieuNhapKho SET soLuong = ?, donGia = ? "
                + "WHERE maPNK = ? AND maVT = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setLong(2, ct.getDonGia());
            ps.setString(3, ct.getSoPhieu());
            ps.setString(4, ct.getMaVT());
            ps.setString(5, ct.getSoPhieu());
            ps.setString(6, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    /** Xóa một dòng vật tư khỏi phiếu (chỉ khi phiếu Chờ xác nhận). */
    public boolean delete(String soPhieu, String maVT) {
        lastError = null;
        String sql = "DELETE FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND maVT = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, maVT);
            ps.setString(3, soPhieu);
            ps.setString(4, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    /** Xóa toàn bộ chi tiết của một phiếu (chỉ khi phiếu Chờ xác nhận). */
    public boolean deleteBySoPhieu(String soPhieu) {
        lastError = null;
        String sql = "DELETE FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, soPhieu);
            ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
            ps.executeUpdate();
            return isChoXacNhan(conn(), soPhieu);
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================

    private ChiTietPhieuNhapKho_entity map(ResultSet rs) throws SQLException {
        return new ChiTietPhieuNhapKho_entity(
                rs.getString("maPNK"),
                rs.getString("maVT"),
                rs.getString("tenVT"),
                rs.getString("donVi"),
                rs.getInt("soLuong"),
                rs.getLong("donGia"));
    }

    private boolean isChoXacNhan(Connection c, String soPhieu) throws SQLException {
        String sql = "SELECT 1 FROM PhieuNhapKho WHERE maPNK = ? AND trangThai = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            ps.setString(2, TrangThai.CHO_XAC_NHAN.getText());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}