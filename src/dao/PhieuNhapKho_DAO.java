package dao;

import connectDB.ConnectDB;
import entity.PhieuNhapKho_entity;
import entity.PhieuNhapKho_entity.ChiTiet;
import entity.PhieuNhapKho_entity.TrangThai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO "Phiếu nhập kho" - làm việc trực tiếp với CSDL SALALA:
 *   PhieuNhapKho(maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
 *   ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong)
 *   VatTu(maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, maThuoc)
 *   NhanVien(maNhanVien, hoTen, ...), NhaCungCap(maNCC, tenNCC, ...), Thuoc(maThuoc, donViTinh)
 * Kết nối dùng chung qua ConnectDB.getInstance().getConnection().
 */
public class PhieuNhapKho_DAO {

    private String lastError = null;

    /** Thông báo lỗi của thao tác gần nhất; null nếu thành công. */
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

    // ===================================================================
    // Truy vấn
    // ===================================================================

    /** Tất cả phiếu (kèm chi tiết), mới nhất trước. */
    public List<PhieuNhapKho_entity> getAll() {
        lastError = null;
        List<PhieuNhapKho_entity> ds = new ArrayList<>();
        Map<String, PhieuNhapKho_entity> theoMa = new HashMap<>();

        String sqlPhieu = "SELECT p.maPNK, p.ngayNhap, p.ghiChu, p.trangThai, "
                + "p.maNhanVien, nv.hoTen, p.maNCC, n.tenNCC "
                + "FROM PhieuNhapKho p "
                + "JOIN NhanVien nv ON nv.maNhanVien = p.maNhanVien "
                + "JOIN NhaCungCap n ON n.maNCC = p.maNCC "
                + "ORDER BY p.ngayNhap DESC, p.maPNK DESC";
        String sqlCT = "SELECT c.maPNK, c.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuNhapKho c "
                + "JOIN VatTu v ON v.maVT = c.maVT "
                + "LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc "
                + "ORDER BY c.maPNK, c.maCTPNK";
        try {
            Connection c = conn();
            try (PreparedStatement ps = c.prepareStatement(sqlPhieu); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuNhapKho_entity p = mapPhieu(rs);
                    ds.add(p);
                    theoMa.put(p.getSoPhieu(), p);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(sqlCT); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuNhapKho_entity p = theoMa.get(rs.getString("maPNK"));
                    if (p != null) p.getChiTiet().add(mapChiTiet(rs));
                }
            }
        } catch (SQLException e) {
            setError(e);
        }
        return ds;
    }

    /** Một phiếu theo số phiếu (kèm chi tiết), không có -> null. */
    public PhieuNhapKho_entity getBySo(String soPhieu) {
        lastError = null;
        String sql = "SELECT p.maPNK, p.ngayNhap, p.ghiChu, p.trangThai, "
                + "p.maNhanVien, nv.hoTen, p.maNCC, n.tenNCC "
                + "FROM PhieuNhapKho p "
                + "JOIN NhanVien nv ON nv.maNhanVien = p.maNhanVien "
                + "JOIN NhaCungCap n ON n.maNCC = p.maNCC "
                + "WHERE p.maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuNhapKho_entity p = mapPhieu(rs);
                    p.setChiTiet(getChiTiet(soPhieu));
                    return p;
                }
            }
        } catch (SQLException e) {
            setError(e);
        }
        return null;
    }

    /** Các dòng vật tư của phiếu (join VatTu / Thuoc để lấy tên, đơn vị). */
    public List<ChiTiet> getChiTiet(String soPhieu) {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = "SELECT c.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuNhapKho c "
                + "JOIN VatTu v ON c.maVT = v.maVT "
                + "LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc "
                + "WHERE c.maPNK = ? ORDER BY c.maCTPNK";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapChiTiet(rs));
            }
        } catch (SQLException e) {
            setError(e);
        }
        return ds;
    }

    /** Danh mục vật tư để chọn khi lập phiếu: mỗi phần tử {maVT, tenVT, donVi, giaNhap(Long)}. */
    public List<Object[]> getDanhMucVatTu() {
        lastError = null;
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT v.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, v.giaNhap "
                + "FROM VatTu v LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc ORDER BY v.maVT";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{rs.getString("maVT"), rs.getString("tenVT"),
                        rs.getString("donVi"), rs.getLong("giaNhap")});
            }
        } catch (SQLException e) {
            setError(e);
        }
        return ds;
    }

    /**
     * Tìm nhân viên theo mã hoặc họ tên. key == null -> lấy nhân viên quản lý kho đầu tiên
     * (không có thì lấy nhân viên đầu tiên). Trả về {maNhanVien, hoTen} hoặc null.
     */
    public Object[] getNhanVien(String key) {
        lastError = null;
        try {
            Connection c = conn();
            if (key != null && !key.trim().isEmpty()) {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT TOP 1 maNhanVien, hoTen FROM NhanVien WHERE maNhanVien = ? OR hoTen = ?")) {
                    ps.setString(1, key.trim());
                    ps.setString(2, key.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return new Object[]{rs.getString(1), rs.getString(2)};
                    }
                }
                return null;
            }
            String sql = "SELECT TOP 1 maNhanVien, hoTen FROM NhanVien "
                    + "ORDER BY CASE WHEN chucVu = N'Nhân viên quản lý kho' THEN 0 ELSE 1 END, maNhanVien";
            try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Object[]{rs.getString(1), rs.getString(2)};
            }
        } catch (SQLException e) {
            setError(e);
        }
        return null;
    }

    /** Sinh mã phiếu kế tiếp, dạng PNK001, PNK002... */
    public String taoSoPhieuMoi() {
        lastError = null;
        int max = 0;
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT maPNK FROM PhieuNhapKho WHERE maPNK LIKE 'PNK%'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    max = Math.max(max, Integer.parseInt(rs.getString(1).substring(3)));
                } catch (NumberFormatException ignored) { }
            }
        } catch (SQLException e) {
            setError(e);
        }
        return String.format("PNK%03d", max + 1);
    }

    // ===================================================================
    // Thêm / sửa
    // ===================================================================

    /** Lập phiếu mới (header + chi tiết, trong 1 transaction). Cần maNhanVien và maNCC trên entity. */
    public boolean insert(PhieuNhapKho_entity p) {
        lastError = null;
        if (p.getMaNhanVien() == null || p.getMaNCC() == null) {
            lastError = "Thiếu mã nhân viên hoặc mã nhà cung cấp.";
            return false;
        }
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "INSERT INTO PhieuNhapKho(maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getSoPhieu());
                ps.setTimestamp(2, toTimestamp(p.getNgayLap()));
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getTrangThai().getText());
                ps.setString(5, p.getMaNhanVien());
                ps.setString(6, p.getMaNCC());
                ps.executeUpdate();
            }
            insertChiTiet(c, p);
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

    /** Sửa phiếu (chỉ khi Chờ xác nhận): cập nhật header và thay toàn bộ chi tiết. */
    public boolean update(PhieuNhapKho_entity p) {
        lastError = null;
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            // Đổi ngày nhưng giữ nguyên giờ đã lưu
            String sql = "UPDATE PhieuNhapKho SET ngayNhap = DATEADD(DAY, DATEDIFF(DAY, ngayNhap, ?), ngayNhap), "
                    + "maNCC = ?, ghiChu = ? WHERE maPNK = ? AND trangThai = ?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDate(1, java.sql.Date.valueOf(p.getNgayLap()));
                ps.setString(2, p.getMaNCC());
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getSoPhieu());
                ps.setString(5, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) {
                c.rollback();
                lastError = "Phiếu không còn ở trạng thái Chờ xác nhận.";
                return false;
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuNhapKho WHERE maPNK = ?")) {
                ps.setString(1, p.getSoPhieu());
                ps.executeUpdate();
            }
            insertChiTiet(c, p);
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

    // ===================================================================
    // Xác nhận (cộng tồn) / hủy
    // ===================================================================

    /**
     * Xác nhận phiếu: chuyển trạng thái sang Đã xác nhận và cộng số lượng vào tồn kho VatTu
     * (đồng thời cập nhật trangThai vật tư theo ngưỡng của hệ thống), trong 1 transaction.
     */
    public boolean xacNhan(String soPhieu) {
        lastError = null;
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            int n;
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE PhieuNhapKho SET trangThai = ? WHERE maPNK = ? AND trangThai = ?")) {
                ps.setString(1, TrangThai.DA_XAC_NHAN.getText());
                ps.setString(2, soPhieu);
                ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) {
                c.rollback();
                lastError = "Phiếu không còn ở trạng thái Chờ xác nhận.";
                return false;
            }

            String upd = "UPDATE v SET v.soLuongTon = v.soLuongTon + s.sl, "
                    + "v.trangThai = CASE WHEN v.soLuongTon + s.sl <= 0 THEN N'Hết tồn' "
                    + "                   WHEN v.soLuongTon + s.sl <= 30 THEN N'Sắp hết' "
                    + "                   ELSE N'Còn hàng' END "
                    + "FROM VatTu v JOIN (SELECT maVT, SUM(soLuong) AS sl FROM ChiTietPhieuNhapKho "
                    + "                   WHERE maPNK = ? GROUP BY maVT) s ON s.maVT = v.maVT";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, soPhieu);
                ps.executeUpdate();
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

    /** Hủy phiếu (chỉ khi Chờ xác nhận); tồn kho không đổi. */
    public boolean huy(String soPhieu) {
        lastError = null;
        String sql = "UPDATE PhieuNhapKho SET trangThai = ? WHERE maPNK = ? AND trangThai = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, TrangThai.DA_HUY.getText());
            ps.setString(2, soPhieu);
            ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
            boolean ok = ps.executeUpdate() > 0;
            if (!ok) lastError = "Phiếu không còn ở trạng thái Chờ xác nhận.";
            return ok;
        } catch (SQLException e) {
            setError(e);
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================

    private void insertChiTiet(Connection c, PhieuNhapKho_entity p) throws SQLException {
        int so = ChiTietPhieuNhapKho_DAO.maxChiTietSo(c);
        String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTiet d : p.getChiTiet()) {
                ps.setString(1, ChiTietPhieuNhapKho_DAO.maChiTiet(++so));
                ps.setString(2, p.getSoPhieu());
                ps.setString(3, d.getMaVT());
                ps.setLong(4, d.getDonGia());
                ps.setInt(5, d.getSoLuong());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Hôm nay -> giờ hiện tại; ngày khác -> 00:00. */
    private static Timestamp toTimestamp(LocalDate d) {
        return Timestamp.valueOf(d.equals(LocalDate.now()) ? LocalDateTime.now() : d.atStartOfDay());
    }

    private PhieuNhapKho_entity mapPhieu(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("ngayNhap");
        PhieuNhapKho_entity p = new PhieuNhapKho_entity(
                rs.getString("maPNK"),
                ts == null ? LocalDate.now() : ts.toLocalDateTime().toLocalDate(),
                rs.getString("hoTen"),
                rs.getString("tenNCC"),
                rs.getString("ghiChu"),
                TrangThai.fromText(rs.getString("trangThai")));
        p.setMaNhanVien(rs.getString("maNhanVien"));
        p.setMaNCC(rs.getString("maNCC"));
        return p;
    }

    private ChiTiet mapChiTiet(ResultSet rs) throws SQLException {
        return new ChiTiet(rs.getString("maVT"), rs.getString("tenVT"), rs.getString("donVi"),
                rs.getInt("soLuong"), rs.getLong("donGia"));
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}