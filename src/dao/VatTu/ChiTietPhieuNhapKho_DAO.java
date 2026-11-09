package dao.VatTu;

import connectDB.ConnectDB;
import entity.VatTu.ChiTietPhieuNhapKho_entity;
import entity.VatTu.PhieuNhapKho_entity.TrangThai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Chi tiết phiếu nhập kho" - khớp với SALALA_Database.sql:
 *
 * ChiTietPhieuNhapKho(maCTPNK PK, maPNK FK, maVT FK, donGia, soLuong)
 * PhieuNhapKho(maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
 * VatTu(maVT, ..., tenVT, ..., maThuoc) + Thuoc(maThuoc, ..., donViTinh)
 *
 * Quy tắc: chỉ được thêm / sửa / xóa dòng chi tiết khi phiếu đang "Chờ xác
 * nhận".
 * Các thao tác ghi KHÔNG tự cộng tồn kho; việc cộng tồn do
 * PhieuNhapKho_DAO.xacNhan() đảm nhiệm.
 */
public class ChiTietPhieuNhapKho_DAO {

    private static final String SELECT_BASE = "SELECT c.maCTPNK, c.maPNK, c.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, c.soLuong, c.donGia "
            + "FROM ChiTietPhieuNhapKho c "
            + "JOIN VatTu v ON c.maVT = v.maVT "
            + "LEFT JOIN Thuoc t ON v.maThuoc = t.maThuoc ";

    /**
     * Điều kiện: phiếu tồn tại và đang Chờ xác nhận (tham số: maPNK, trangThai).
     */
    private static final String PHIEU_CHO_XAC_NHAN = "EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPNK = ? AND trangThai = ?)";

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null)
            throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================

    /** Tất cả dòng chi tiết của một phiếu. */
    public List<ChiTietPhieuNhapKho_entity> getByMaPNK(String maPNK) {
        List<ChiTietPhieuNhapKho_entity> ds = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.maPNK = ? ORDER BY c.maCTPNK";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một dòng chi tiết theo (maPNK, maVT); không có -> null. */
    public ChiTietPhieuNhapKho_entity getOne(String maPNK, String maVT) {
        String sql = SELECT_BASE + "WHERE c.maPNK = ? AND c.maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            ps.setString(2, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Kiểm tra vật tư đã có trong phiếu chưa. */
    public boolean exists(String maPNK, String maVT) {
        String sql = "SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
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
    public int tongSoLuong(String maPNK) {
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietPhieuNhapKho WHERE maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng tiền của phiếu (soLuong x donGia). */
    public long tongTien(String maPNK) {
        String sql = "SELECT COALESCE(SUM(soLuong * donGia), 0) FROM ChiTietPhieuNhapKho WHERE maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================================================================
    // Thêm / sửa / xóa
    // ===================================================================

    /**
     * Thêm một dòng vật tư vào phiếu (chỉ khi phiếu Chờ xác nhận và vật tư chưa có
     * trong phiếu).
     * Mã dòng (maCTPNK) được sinh tự động và gán lại vào entity.
     */
    public boolean insert(ChiTietPhieuNhapKho_entity ct) {
        String maPNK = ct.getMaPNK() != null ? ct.getMaPNK().getMaPNK() : null;
        String maVT = ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null;
        if (exists(maPNK, maVT))
            return false; // một vật tư chỉ xuất hiện 1 lần trong phiếu

        String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) "
                + "SELECT ?, ?, ?, ?, ? WHERE " + PHIEU_CHO_XAC_NHAN;
        try {
            Connection c = conn();
            String maCT = String.format("CTPNK%02d", PhieuNhapKho_DAO.nextChiTietSeq(c));
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maCT);
                ps.setString(2, maPNK);
                ps.setString(3, maVT);
                ps.setLong(4, ct.getDonGia());
                ps.setInt(5, ct.getSoLuong());
                ps.setString(6, maPNK);
                ps.setString(7, TrangThai.CHO_XAC_NHAN.getText());
                boolean ok = ps.executeUpdate() > 0;
                if (ok)
                    ct.setMaCTPNK(maCT);
                return ok;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm nhiều dòng cùng lúc trong 1 transaction (chỉ khi phiếu Chờ xác nhận).
     */
    public boolean insertAll(String maPNK, List<ChiTietPhieuNhapKho_entity> ds) {
        if (ds == null || ds.isEmpty())
            return true;
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            if (!isChoXacNhan(c, maPNK)) {
                c.rollback();
                return false;
            }
            int next = PhieuNhapKho_DAO.nextChiTietSeq(c);
            String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (ChiTietPhieuNhapKho_entity d : ds) {
                    ps.setString(1, String.format("CTPNK%02d", next++));
                    ps.setString(2, maPNK);
                    ps.setString(3, d.getMaVT() != null ? d.getMaVT().getMaVT() : null);
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
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    /** Sửa số lượng / đơn giá của một dòng (chỉ khi phiếu Chờ xác nhận). */
    public boolean update(ChiTietPhieuNhapKho_entity ct) {
        String maPNK = ct.getMaPNK() != null ? ct.getMaPNK().getMaPNK() : null;
        String sql = "UPDATE ChiTietPhieuNhapKho SET soLuong = ?, donGia = ? "
                + "WHERE maPNK = ? AND maVT = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setLong(2, ct.getDonGia());
            ps.setString(3, maPNK);
            ps.setString(4, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
            ps.setString(5, maPNK);
            ps.setString(6, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa một dòng vật tư khỏi phiếu (chỉ khi phiếu Chờ xác nhận). */
    public boolean delete(String maPNK, String maVT) {
        String sql = "DELETE FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND maVT = ? AND "
                + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            ps.setString(2, maVT);
            ps.setString(3, maPNK);
            ps.setString(4, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa toàn bộ chi tiết của một phiếu (chỉ khi phiếu Chờ xác nhận).
     * Trả về true khi phiếu đang Chờ xác nhận (kể cả phiếu chưa có dòng nào), false
     * nếu bị từ chối.
     */
    public boolean deleteByMaPNK(String maPNK) {
        String sql = "DELETE FROM ChiTietPhieuNhapKho WHERE maPNK = ? AND " + PHIEU_CHO_XAC_NHAN;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            ps.setString(2, maPNK);
            ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
            ps.executeUpdate();
            return isChoXacNhanQuiet(maPNK);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================

    private ChiTietPhieuNhapKho_entity map(ResultSet rs) throws SQLException {
        ChiTietPhieuNhapKho_entity e = new ChiTietPhieuNhapKho_entity(
                rs.getString("maPNK"),
                rs.getString("maVT"),
                rs.getString("tenVT"),
                rs.getString("donVi"),
                rs.getInt("soLuong"),
                rs.getLong("donGia"));
        e.setMaCTPNK(rs.getString("maCTPNK"));
        return e;
    }

    private boolean isChoXacNhan(Connection c, String maPNK) throws SQLException {
        String sql = "SELECT 1 FROM PhieuNhapKho WHERE maPNK = ? AND trangThai = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPNK);
            ps.setString(2, TrangThai.CHO_XAC_NHAN.getText());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isChoXacNhanQuiet(String maPNK) {
        try {
            return isChoXacNhan(conn(), maPNK);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void rollback(Connection c) {
        try {
            if (c != null)
                c.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommit(Connection c) {
        try {
            if (c != null)
                c.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}