package dao.VatTu;

import connectDB.ConnectDB;
import entity.VatTu.PhieuNhapKho_entity;
import entity.VatTu.PhieuNhapKho_entity.ChiTiet;
import entity.VatTu.PhieuNhapKho_entity.TrangThai;

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
 * DAO "Phiếu nhập kho" - khớp với SALALA_Database.sql:
 *
 *   PhieuNhapKho(maPNK, ngayNhap DATETIME2, ghiChu, trangThai, maNhanVien, maNCC)
 *   ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong)
 *   VatTu(maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, maThuoc)
 *   Thuoc(maThuoc, tenThuoc, donViTinh, ...)   -- đơn vị của vật tư lấy từ đây (nếu vật tư là thuốc)
 *   NhaCungCap(maNCC, tenNCC, ...), NhanVien(maNhanVien, hoTen, ...)
 *
 * Mã phiếu: PNK001, PNK002, ... ; mã dòng chi tiết: CTPNK01, CTPNK02, ... (giống dữ liệu mẫu).
 */
public class PhieuNhapKho_DAO {

    private static final String PREFIX_PNK = "PNK";
    private static final String PREFIX_CT = "CTPNK";

    /** Truy vấn chi tiết dùng chung: tên vật tư + đơn vị (Thuoc.donViTinh, rỗng nếu không phải thuốc). */
    private static final String SELECT_CHI_TIET =
            "SELECT c.maPNK, c.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, c.soLuong, c.donGia "
            + "FROM ChiTietPhieuNhapKho c "
            + "JOIN VatTu v ON c.maVT = v.maVT "
            + "LEFT JOIN Thuoc t ON v.maThuoc = t.maThuoc ";

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn phiếu
    // ===================================================================

    /** Tất cả phiếu (kèm chi tiết), mới nhất trước. Dùng 2 truy vấn, không N+1. */
    public List<PhieuNhapKho_entity> getAll() {
        List<PhieuNhapKho_entity> ds = new ArrayList<>();
        String sql = "SELECT maPNK, ngayNhap, maNhanVien, maNCC, ghiChu, trangThai "
                + "FROM PhieuNhapKho ORDER BY ngayNhap DESC, maPNK DESC";
        try {
            Connection c = conn();
            try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapPhieu(rs));
            }
            Map<String, List<ChiTiet>> theoPhieu = new HashMap<>();
            try (PreparedStatement ps = c.prepareStatement(SELECT_CHI_TIET + "ORDER BY c.maCTPNK");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    theoPhieu.computeIfAbsent(rs.getString("maPNK"), k -> new ArrayList<>())
                            .add(mapChiTiet(rs));
                }
            }
            for (PhieuNhapKho_entity p : ds) {
                List<ChiTiet> l = theoPhieu.get(p.getMaPNK());
                p.setChiTiet(l != null ? l : new ArrayList<>());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một phiếu theo mã (kèm chi tiết); không có -> null. */
    public PhieuNhapKho_entity getByMa(String maPNK) {
        String sql = "SELECT maPNK, ngayNhap, maNhanVien, maNCC, ghiChu, trangThai "
                + "FROM PhieuNhapKho WHERE maPNK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPNK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuNhapKho_entity p = mapPhieu(rs);
                    p.setChiTiet(getChiTiet(maPNK));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Các dòng vật tư của phiếu. */
    public List<ChiTiet> getChiTiet(String maPNK) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                SELECT_CHI_TIET + "WHERE c.maPNK = ? ORDER BY c.maCTPNK")) {
            ps.setString(1, maPNK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapChiTiet(rs));
            }
        }
        return ds;
    }

    /** Sinh mã phiếu kế tiếp: PNK + số thứ tự 3 chữ số (PNK003, PNK004, ...). */
    public String taoMaPhieuMoi() {
        String sql = "SELECT MAX(TRY_CAST(SUBSTRING(maPNK, " + (PREFIX_PNK.length() + 1) + ", 17) AS INT)) "
                + "FROM PhieuNhapKho WHERE maPNK LIKE '" + PREFIX_PNK + "%'";
        int next = 1;
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) next = rs.getInt(1) + 1;   // MAX = NULL -> getInt = 0 -> next = 1
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%s%03d", PREFIX_PNK, next);
    }

    // ===================================================================
    // Danh mục phục vụ form (lấy từ CSDL, không hard-code)
    // ===================================================================

    /** Nhà cung cấp: mỗi phần tử {maNCC, tenNCC}. */
    public List<String[]> getNhaCungCapOptions() {
        return queryPairs("SELECT maNCC, tenNCC FROM NhaCungCap ORDER BY tenNCC");
    }

    /** Nhân viên: mỗi phần tử {maNhanVien, hoTen}. */
    public List<String[]> getNhanVienOptions() {
        return queryPairs("SELECT maNhanVien, hoTen FROM NhanVien ORDER BY maNhanVien");
    }

    /** Danh mục vật tư để chọn khi lập phiếu: {maVT, tenVT, donVi, giaNhap}. */
    public List<Object[]> getVatTuCatalog() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT v.maVT, v.tenVT, COALESCE(t.donViTinh, N'') AS donVi, v.giaNhap "
                + "FROM VatTu v LEFT JOIN Thuoc t ON v.maThuoc = t.maThuoc ORDER BY v.maVT";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{rs.getString("maVT"), rs.getString("tenVT"),
                        rs.getString("donVi"), rs.getLong("giaNhap")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    private List<String[]> queryPairs(String sql) {
        List<String[]> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(new String[]{rs.getString(1), rs.getString(2)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ===================================================================
    // Thêm / sửa
    // ===================================================================

    /** Lập phiếu mới (header + chi tiết, trong 1 transaction). */
    public boolean insert(PhieuNhapKho_entity p) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "INSERT INTO PhieuNhapKho(maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getMaPNK());
                ps.setTimestamp(2, toTimestamp(p.getNgayNhap()));
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getTrangThai().getText());
                ps.setString(5, p.getNhanVien() != null ? p.getNhanVien().getMaNhanVien() : null);
                ps.setString(6, p.getNhaCungCap() != null ? p.getNhaCungCap().getMaNCC() : null);
                ps.executeUpdate();
            }
            insertChiTiet(c, p);
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

    /** Sửa phiếu (chỉ khi đang Chờ xác nhận): cập nhật header và thay toàn bộ chi tiết. */
    public boolean update(PhieuNhapKho_entity p) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "UPDATE PhieuNhapKho SET ngayNhap=?, maNCC=?, ghiChu=? "
                    + "WHERE maPNK=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setTimestamp(1, toTimestamp(p.getNgayNhap()));
                ps.setString(2, p.getNhaCungCap() != null ? p.getNhaCungCap().getMaNCC() : null);
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getMaPNK());
                ps.setString(5, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuNhapKho WHERE maPNK=?")) {
                ps.setString(1, p.getMaPNK());
                ps.executeUpdate();
            }
            insertChiTiet(c, p);
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
    // Xác nhận (cộng tồn) / hủy
    // ===================================================================

    /**
     * Xác nhận phiếu: chuyển trạng thái sang Đã xác nhận và cộng số lượng vào VatTu.soLuongTon
     * (cùng 1 transaction). Chỉ thực hiện khi phiếu đang Chờ xác nhận.
     */
    public boolean xacNhan(String maPNK) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            String sql = "UPDATE PhieuNhapKho SET trangThai=? WHERE maPNK=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, TrangThai.DA_XAC_NHAN.getText());
                ps.setString(2, maPNK);
                ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            String upd = "UPDATE VatTu SET soLuongTon = soLuongTon + "
                    + "(SELECT SUM(soLuong) FROM ChiTietPhieuNhapKho WHERE maPNK=? AND maVT=VatTu.maVT) "
                    + "WHERE maVT IN (SELECT maVT FROM ChiTietPhieuNhapKho WHERE maPNK=?)";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, maPNK);
                ps.setString(2, maPNK);
                ps.executeUpdate();
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

    /** Hủy phiếu (chỉ khi Chờ xác nhận); tồn kho không đổi. */
    public boolean huy(String maPNK) {
        String sql = "UPDATE PhieuNhapKho SET trangThai=? WHERE maPNK=? AND trangThai=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, TrangThai.DA_HUY.getText());
            ps.setString(2, maPNK);
            ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================

    /** Chèn các dòng chi tiết của phiếu; sinh maCTPNK liên tục từ số lớn nhất hiện có. */
    private void insertChiTiet(Connection c, PhieuNhapKho_entity p) throws SQLException {
        if (p.getChiTiet() == null || p.getChiTiet().isEmpty()) return;
        int next = nextChiTietSeq(c);
        String sql = "INSERT INTO ChiTietPhieuNhapKho(maCTPNK, maPNK, maVT, donGia, soLuong) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTiet d : p.getChiTiet()) {
                ps.setString(1, String.format("%s%02d", PREFIX_CT, next++));
                ps.setString(2, p.getMaPNK());
                ps.setString(3, d.getMaVT() != null ? d.getMaVT().getMaVT() : null);
                ps.setLong(4, d.getDonGia());
                ps.setInt(5, d.getSoLuong());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Số thứ tự kế tiếp cho maCTPNK (toàn bảng, vì maCTPNK là khóa chính). */
    static int nextChiTietSeq(Connection c) throws SQLException {
        String sql = "SELECT MAX(TRY_CAST(SUBSTRING(maCTPNK, " + (PREFIX_CT.length() + 1) + ", 15) AS INT)) "
                + "FROM ChiTietPhieuNhapKho WHERE maCTPNK LIKE '" + PREFIX_CT + "%'";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) + 1 : 1;
        }
    }

    /** Hôm nay -> giữ giờ hiện tại; ngày khác -> 00:00. */
    private static Timestamp toTimestamp(LocalDate d) {
        LocalDate day = d != null ? d : LocalDate.now();
        LocalDateTime t = day.equals(LocalDate.now()) ? LocalDateTime.now() : day.atStartOfDay();
        return Timestamp.valueOf(t);
    }

    private PhieuNhapKho_entity mapPhieu(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("ngayNhap");
        return new PhieuNhapKho_entity(
                rs.getString("maPNK"),
                ts != null ? ts.toLocalDateTime().toLocalDate() : LocalDate.now(),
                rs.getString("maNhanVien"),
                rs.getString("maNCC"),
                rs.getString("ghiChu"),
                TrangThai.fromText(rs.getString("trangThai")));
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