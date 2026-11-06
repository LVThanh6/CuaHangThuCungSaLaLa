package dao;

import connectDB.ConnectDB;
import entity.PhieuXuatKho_entity;
import entity.PhieuXuatKho_entity.ChiTiet;
import entity.PhieuXuatKho_entity.TrangThai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Phiếu xuất kho".
 *
 * Giả định CSDL (SQL Server):
 *   VatTu(maVT, tenVT, donVi, giaNhap, soLuongTon)
 *   PhieuXuatKho(maPhieu, ngayXuat, nhanVien, lyDo, ghiChu, trangThai)
 *   ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia)   -- donGia = giá nhập
 * Lớp ConnectDB cần có: ConnectDB.getInstance().getConnection().
 */
public class PhieuXuatKho_DAO {

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================
    /** Tất cả phiếu (kèm chi tiết), mới nhất trước. GUI tự lọc / phân trang trong bộ nhớ. */
    public List<PhieuXuatKho_entity> getAll() {
        List<PhieuXuatKho_entity> ds = new ArrayList<>();
        String sql = "SELECT * FROM PhieuXuatKho ORDER BY ngayXuat DESC, maPhieu DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(mapPhieu(rs));
            for (PhieuXuatKho_entity p : ds) p.setChiTiet(getChiTiet(p.getMaPhieu()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một phiếu theo mã (kèm chi tiết), không có -> null. */
    public PhieuXuatKho_entity getByMa(String maPhieu) {
        String sql = "SELECT * FROM PhieuXuatKho WHERE maPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuXuatKho_entity p = mapPhieu(rs);
                    p.setChiTiet(getChiTiet(maPhieu));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Các dòng vật tư của phiếu (join VatTu để lấy tên, đơn vị). */
    public List<ChiTiet> getChiTiet(String maPhieu) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = "SELECT c.maVT, v.tenVT, v.donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuXuat c JOIN VatTu v ON c.maVT = v.maVT "
                + "WHERE c.maPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTiet(rs.getString("maVT"), rs.getString("tenVT"),
                            rs.getString("donVi"), rs.getInt("soLuong"), rs.getLong("donGia")));
                }
            }
        }
        return ds;
    }

    /** Tồn kho hiện tại của một vật tư (không có -> 0). */
    public int getTonKho(String maVT) {
        String sql = "SELECT soLuongTon FROM VatTu WHERE maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Sinh mã phiếu kế tiếp, dạng PX0001. */
    public String taoMaPhieuMoi() {
        String sql = "SELECT MAX(maPhieu) FROM PhieuXuatKho WHERE maPhieu LIKE 'PX%'";
        int next = 1;
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) {
                next = Integer.parseInt(rs.getString(1).substring(2)) + 1;
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return String.format("PX%04d", next);
    }

    // ===================================================================
    // Thêm / sửa
    // ===================================================================
    /** Lập phiếu mới (trạng thái Nháp; header + chi tiết trong 1 transaction). */
    public boolean insert(PhieuXuatKho_entity p) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "INSERT INTO PhieuXuatKho(maPhieu, ngayXuat, nhanVien, lyDo, ghiChu, trangThai) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getMaPhieu());
                ps.setDate(2, Date.valueOf(p.getNgayXuat()));
                ps.setString(3, p.getNhanVien() != null ? p.getNhanVien().getMaNhanVien() : null);
                ps.setString(4, p.getLyDo());
                ps.setString(5, p.getGhiChu());
                ps.setString(6, p.getTrangThai().getText());
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

    /** Sửa phiếu (chỉ khi Nháp): cập nhật header và thay toàn bộ chi tiết. */
    public boolean update(PhieuXuatKho_entity p) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "UPDATE PhieuXuatKho SET ngayXuat=?, lyDo=?, ghiChu=? "
                    + "WHERE maPhieu=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(p.getNgayXuat()));
                ps.setString(2, p.getLyDo());
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getMaPhieu());
                ps.setString(5, TrangThai.NHAP.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuXuat WHERE maPhieu=?")) {
                ps.setString(1, p.getMaPhieu());
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
    // Xác nhận (trừ tồn) / hủy
    // ===================================================================
    /**
     * Xác nhận xuất kho: trừ tồn từng vật tư và chuyển phiếu sang Đã xuất (1 transaction).
     * Nếu bất kỳ vật tư nào không đủ tồn thì rollback toàn bộ và trả về false.
     */
    public boolean xacNhan(String maPhieu) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            String sql = "UPDATE PhieuXuatKho SET trangThai=? WHERE maPhieu=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, TrangThai.DA_XUAT.getText());
                ps.setString(2, maPhieu);
                ps.setString(3, TrangThai.NHAP.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            // Điều kiện soLuongTon >= soLuong đảm bảo không xuất quá tồn (kể cả khi có người khác thao tác song song)
            String upd = "UPDATE VatTu SET soLuongTon = soLuongTon - ? WHERE maVT = ? AND soLuongTon >= ?";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                for (ChiTiet d : getChiTietTrongTx(c, maPhieu)) {
                    ps.setInt(1, d.getSoLuong());
                    ps.setString(2, d.getMaVT() != null ? d.getMaVT().getMaVT() : null);
                    ps.setInt(3, d.getSoLuong());
                    if (ps.executeUpdate() == 0) { c.rollback(); return false; }
                }
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

    /** Hủy phiếu (chỉ khi Nháp); tồn kho không đổi. */
    public boolean huy(String maPhieu) {
        String sql = "UPDATE PhieuXuatKho SET trangThai=? WHERE maPhieu=? AND trangThai=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, TrangThai.DA_HUY.getText());
            ps.setString(2, maPhieu);
            ps.setString(3, TrangThai.NHAP.getText());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================
    private void insertChiTiet(Connection c, PhieuXuatKho_entity p) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuXuat(maPhieu, maVT, soLuong, donGia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTiet d : p.getChiTiet()) {
                ps.setString(1, p.getMaPhieu());
                ps.setString(2, d.getMaVT() != null ? d.getMaVT().getMaVT() : null);
                ps.setInt(3, d.getSoLuong());
                ps.setLong(4, d.getDonGia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Đọc chi tiết bằng đúng connection đang trong transaction. */
    private List<ChiTiet> getChiTietTrongTx(Connection c, String maPhieu) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = "SELECT maVT, soLuong FROM ChiTietPhieuXuat WHERE maPhieu = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTiet d = new ChiTiet();
                    d.setMaVT(rs.getString("maVT"));
                    d.setSoLuong(rs.getInt("soLuong"));
                    ds.add(d);
                }
            }
        }
        return ds;
    }

    private PhieuXuatKho_entity mapPhieu(ResultSet rs) throws SQLException {
        return new PhieuXuatKho_entity(
                rs.getString("maPhieu"),
                rs.getDate("ngayXuat").toLocalDate(),
                rs.getString("nhanVien"),
                rs.getString("lyDo"),
                rs.getString("ghiChu"),
                TrangThai.fromText(rs.getString("trangThai")));
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}