package dao;

import connectDB.ConnectDB;
import entity.PhieuNhapKho_entity;
import entity.PhieuNhapKho_entity.ChiTiet;
import entity.PhieuNhapKho_entity.TrangThai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "Phiếu nhập kho".
 *
 * Giả định CSDL (SQL Server):
 *   VatTu(maVT, tenVT, donVi, giaNhap, soLuongTon)
 *   PhieuNhapKho(soPhieu, ngayLap, nhanVien, nhaCungCap, ghiChu, trangThai)
 *   ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia)
 * Lớp ConnectDB cần có: ConnectDB.getInstance().getConnection().
 */
public class PhieuNhapKho_DAO {

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================
    /** Tất cả phiếu (kèm chi tiết), mới nhất trước. */
    public List<PhieuNhapKho_entity> getAll() {
        List<PhieuNhapKho_entity> ds = new ArrayList<>();
        String sql = "SELECT * FROM PhieuNhapKho ORDER BY ngayLap DESC, soPhieu DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(mapPhieu(rs));
            for (PhieuNhapKho_entity p : ds) p.setChiTiet(getChiTiet(p.getSoPhieu()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Một phiếu theo số phiếu (kèm chi tiết), không có -> null. */
    public PhieuNhapKho_entity getBySo(String soPhieu) {
        String sql = "SELECT * FROM PhieuNhapKho WHERE soPhieu = ?";
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
            e.printStackTrace();
        }
        return null;
    }

    /** Các dòng vật tư của phiếu (join VatTu để lấy tên, đơn vị). */
    public List<ChiTiet> getChiTiet(String soPhieu) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = "SELECT c.maVT, v.tenVT, v.donVi, c.soLuong, c.donGia "
                + "FROM ChiTietPhieuNhap c JOIN VatTu v ON c.maVT = v.maVT "
                + "WHERE c.soPhieu = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, soPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTiet(rs.getString("maVT"), rs.getString("tenVT"),
                            rs.getString("donVi"), rs.getInt("soLuong"), rs.getLong("donGia")));
                }
            }
        }
        return ds;
    }

    /** Sinh số phiếu kế tiếp, dạng PN-yyyy-NNNN. */
    public String taoSoPhieuMoi() {
        int year = LocalDate.now().getYear();
        String prefix = "PN-" + year + "-";
        String sql = "SELECT MAX(soPhieu) FROM PhieuNhapKho WHERE soPhieu LIKE ?";
        int next = 1;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    next = Integer.parseInt(rs.getString(1).substring(prefix.length())) + 1;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return String.format("%s%04d", prefix, next);
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
            String sql = "INSERT INTO PhieuNhapKho(soPhieu, ngayLap, nhanVien, nhaCungCap, ghiChu, trangThai) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getSoPhieu());
                ps.setDate(2, Date.valueOf(p.getNgayLap()));
                ps.setString(3, p.getNhanVien());
                ps.setString(4, p.getNhaCungCap());
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

    /** Sửa phiếu (chỉ khi đang Chờ xác nhận): cập nhật header và thay toàn bộ chi tiết. */
    public boolean update(PhieuNhapKho_entity p) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            String sql = "UPDATE PhieuNhapKho SET ngayLap=?, nhaCungCap=?, ghiChu=? "
                    + "WHERE soPhieu=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(p.getNgayLap()));
                ps.setString(2, p.getNhaCungCap());
                ps.setString(3, p.getGhiChu());
                ps.setString(4, p.getSoPhieu());
                ps.setString(5, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietPhieuNhap WHERE soPhieu=?")) {
                ps.setString(1, p.getSoPhieu());
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
     * Xác nhận phiếu: chuyển trạng thái sang Đã xác nhận và cộng số lượng vào tồn kho
     * (cùng 1 transaction). Chỉ thực hiện khi phiếu đang Chờ xác nhận.
     */
    public boolean xacNhan(String soPhieu) {
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            String sql = "UPDATE PhieuNhapKho SET trangThai=? WHERE soPhieu=? AND trangThai=?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, TrangThai.DA_XAC_NHAN.getText());
                ps.setString(2, soPhieu);
                ps.setString(3, TrangThai.CHO_XAC_NHAN.getText());
                n = ps.executeUpdate();
            }
            if (n == 0) { c.rollback(); return false; }

            String upd = "UPDATE VatTu SET soLuongTon = soLuongTon + "
                    + "(SELECT SUM(soLuong) FROM ChiTietPhieuNhap WHERE soPhieu=? AND maVT=VatTu.maVT) "
                    + "WHERE maVT IN (SELECT maVT FROM ChiTietPhieuNhap WHERE soPhieu=?)";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, soPhieu);
                ps.setString(2, soPhieu);
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
    public boolean huy(String soPhieu) {
        String sql = "UPDATE PhieuNhapKho SET trangThai=? WHERE soPhieu=? AND trangThai=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, TrangThai.DA_HUY.getText());
            ps.setString(2, soPhieu);
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
    private void insertChiTiet(Connection c, PhieuNhapKho_entity p) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuNhap(soPhieu, maVT, soLuong, donGia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ChiTiet d : p.getChiTiet()) {
                ps.setString(1, p.getSoPhieu());
                ps.setString(2, d.getMaVT());
                ps.setInt(3, d.getSoLuong());
                ps.setLong(4, d.getDonGia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private PhieuNhapKho_entity mapPhieu(ResultSet rs) throws SQLException {
        return new PhieuNhapKho_entity(
                rs.getString("soPhieu"),
                rs.getDate("ngayLap").toLocalDate(),
                rs.getString("nhanVien"),
                rs.getString("nhaCungCap"),
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