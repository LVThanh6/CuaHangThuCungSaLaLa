package dao.VatTu;

import connectDB.ConnectDB;
import entity.VatTu.ChiTietPhieuXuatKho_entity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Chi tiết phiếu xuất kho".
 *
 * Bảng thật trong CSDL SALALA (chỉ đọc/ghi các bảng của phiếu xuất kho):
 *   ChiTietPhieuXuatKho(maCTPXK, maPXK, maVT, soLuong)   -- khóa chính maCTPXK
 * Bảng chỉ đọc để hiển thị:
 *   VatTu(maVT, tenVT, giaNhap, maThuoc, ...)   -> tên vật tư, đơn giá (= giaNhap hiện tại)
 *   Thuoc(maThuoc, donViTinh)                   -> đơn vị (vật tư không phải thuốc: "-")
 *
 * Các hàm có tham số Connection dùng để chạy chung transaction với PhieuXuatKho_DAO.
 * Các hàm không có tham số Connection tự dùng kết nối chung của ConnectDB.
 */
public class ChiTietPhieuXuatKho_DAO {

    /** Đơn vị hiển thị khi vật tư không gắn với thuốc (VatTu không có cột đơn vị). */
    static final String DON_VI_MAC_DINH = "-";

    private static final String SELECT_CHI_TIET =
            "SELECT c.maCTPXK, c.maPXK, c.maVT, v.tenVT, "
                    + "COALESCE(t.donViTinh, N'" + DON_VI_MAC_DINH + "') AS donVi, c.soLuong, v.giaNhap "
                    + "FROM ChiTietPhieuXuatKho c "
                    + "JOIN VatTu v ON v.maVT = c.maVT "
                    + "LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc ";

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================
    /** Các dòng vật tư của một phiếu (JOIN VatTu, Thuoc để lấy tên, đơn vị, giá nhập). */
    public List<ChiTietPhieuXuatKho_entity> getByMaPhieu(String maPXK) {
        List<ChiTietPhieuXuatKho_entity> ds = new ArrayList<>();
        String sql = SELECT_CHI_TIET + "WHERE c.maPXK = ? ORDER BY c.maCTPXK";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một dòng chi tiết theo (maPXK, maVT); không có -> null. */
    public ChiTietPhieuXuatKho_entity getById(String maPXK, String maVT) {
        String sql = SELECT_CHI_TIET + "WHERE c.maPXK = ? AND c.maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
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
    public boolean tonTai(String maPXK, String maVT) {
        String sql = "SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPXK = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
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
    public int tongSoLuong(String maPXK) {
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietPhieuXuatKho WHERE maPXK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1).intValue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng tiền (soLuong x giaNhap hiện tại của vật tư) của phiếu. */
    public long tongTien(String maPXK) {
        String sql = "SELECT COALESCE(SUM(c.soLuong * v.giaNhap), 0) "
                + "FROM ChiTietPhieuXuatKho c JOIN VatTu v ON v.maVT = c.maVT WHERE c.maPXK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1).longValue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================================================================
    // Thêm / sửa / xóa (tự quản lý kết nối)
    // ===================================================================
    /** Thêm một dòng chi tiết (tự sinh maCTPXK). Trả về false nếu lỗi. */
    public boolean insert(ChiTietPhieuXuatKho_entity ct) {
        try {
            return insert(conn(), ct);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật số lượng của một dòng (đơn giá không lưu trong bảng nên không cập nhật). */
    public boolean update(ChiTietPhieuXuatKho_entity ct) {
        String sql = "UPDATE ChiTietPhieuXuatKho SET soLuong = ? WHERE maPXK = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, ct.getSoLuong());
            ps.setString(2, ct.getMaPhieu() != null ? ct.getMaPhieu().getMaPhieu() : null);
            ps.setString(3, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa một dòng vật tư khỏi phiếu. */
    public boolean delete(String maPXK, String maVT) {
        String sql = "DELETE FROM ChiTietPhieuXuatKho WHERE maPXK = ? AND maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
            ps.setString(2, maVT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa toàn bộ chi tiết của một phiếu. */
    public boolean deleteByMaPhieu(String maPXK) {
        try {
            deleteByMaPhieu(conn(), maPXK);
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
    public boolean replaceAll(String maPXK, List<ChiTietPhieuXuatKho_entity> ds) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            deleteByMaPhieu(c, maPXK);
            insertAll(c, maPXK, ds);
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
        String sql = "INSERT INTO ChiTietPhieuXuatKho(maCTPXK, maPXK, maVT, soLuong) VALUES (?,?,?,?)";
        String maPXK = ct.getMaPhieu() != null ? ct.getMaPhieu().getMaPhieu() : null;
        String ma = ct.getMaCTPXK();
        if (ma == null || ma.isEmpty()) {
            ma = taoMaCT(c, maPXK, demDong(c, maPXK) + 1);
        }
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, maPXK);
            ps.setString(3, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
            ps.setInt(4, ct.getSoLuong());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) ct.setMaCTPXK(ma);
            return ok;
        }
    }

    /** Thêm nhiều dòng bằng batch; maPXK của từng dòng được gán theo tham số maPXK. */
    public void insertAll(Connection c, String maPXK, List<ChiTietPhieuXuatKho_entity> ds) throws SQLException {
        if (ds == null || ds.isEmpty()) return;
        String sql = "INSERT INTO ChiTietPhieuXuatKho(maCTPXK, maPXK, maVT, soLuong) VALUES (?,?,?,?)";
        int dong = demDong(c, maPXK);   // số dòng đã có; dòng mới đánh số tiếp theo
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTietPhieuXuatKho_entity ct : ds) {
                ps.setString(1, taoMaCT(c, maPXK, ++dong));
                ps.setString(2, maPXK);
                ps.setString(3, ct.getMaVT() != null ? ct.getMaVT().getMaVT() : null);
                ps.setInt(4, ct.getSoLuong());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Xóa toàn bộ chi tiết của phiếu bằng connection đang dùng. */
    public void deleteByMaPhieu(Connection c, String maPXK) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuXuatKho WHERE maPXK = ?")) {
            ps.setString(1, maPXK);
            ps.executeUpdate();
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================
    /** Độ dài tối đa của cột maCTPXK (VARCHAR(20)). */
    private static final int MA_CT_TOI_DA = 20;

    /** Số dòng chi tiết hiện có của phiếu. */
    private int demDong(Connection c, String maPXK) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM ChiTietPhieuXuatKho WHERE maPXK = ?")) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Mã nhân viên lập phiếu (đọc cùng connection/transaction). */
    private String layMaNhanVien(Connection c, String maPXK) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT maNhanVien FROM PhieuXuatKho WHERE maPXK = ?")) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        throw new SQLException("Không tìm thấy phiếu xuất " + maPXK + " để tạo mã chi tiết.");
    }

    /**
     * Mã chi tiết = số dòng (2 chữ số) + mã phiếu + mã nhân viên lập phiếu.
     * Ví dụ: dòng 1 của PXK003 do NV011 lập -> "01PXK003NV011".
     */
    private String taoMaCT(Connection c, String maPXK, int soDong) throws SQLException {
        String ma = String.format("%02d", soDong) + maPXK + layMaNhanVien(c, maPXK);
        if (ma.length() > MA_CT_TOI_DA) {
            throw new SQLException("Mã chi tiết '" + ma + "' dài quá " + MA_CT_TOI_DA + " ký tự (cột maCTPXK).");
        }
        return ma;
    }

    private ChiTietPhieuXuatKho_entity mapRow(ResultSet rs) throws SQLException {
        ChiTietPhieuXuatKho_entity ct = new ChiTietPhieuXuatKho_entity(
                rs.getString("maPXK"),
                rs.getString("maVT"),
                rs.getString("tenVT"),
                rs.getString("donVi"),
                toInt(rs.getBigDecimal("soLuong")),
                toLong(rs.getBigDecimal("giaNhap")));
        ct.setMaCTPXK(rs.getString("maCTPXK"));
        return ct;
    }

    static int toInt(BigDecimal v) { return v == null ? 0 : v.intValue(); }

    static long toLong(BigDecimal v) { return v == null ? 0L : v.longValue(); }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}