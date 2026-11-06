package dao.VatTu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.VatTu.NhaCungCap_entity;
import entity.VatTu.VatTu_entity;

/**
 * DAO Vật Tư (Đã tối ưu hóa bằng JOIN, triệt tiêu N+1 Query sang NhaCungCap).
 */
public class VatTu_DAO {

    private static final String SELECT_BASE =
            "SELECT v.maVT, v.maNCC, v.tenVT, v.soLuongTon, v.trangThai, v.giaBanCoBan, v.giaNhap, v.maThuoc, " +
            "ncc.tenNCC, ncc.diaChi, ncc.sdt, ncc.email " +
            "FROM VatTu v " +
            "LEFT JOIN NhaCungCap ncc ON ncc.maNCC = v.maNCC ";

    /**
     * Lấy toàn bộ danh sách vật tư kèm thông tin nhà cung cấp bằng 1 query JOIN duy nhất.
     */
    public ArrayList<VatTu_entity> getAllVatTu() {
        ArrayList<VatTu_entity> dsVT = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsVT;
        }

        String sql = SELECT_BASE + "ORDER BY v.maVT ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsVT.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsVT;
    }

    /**
     * Thêm vật tư mới
     */
    public boolean createVatTu(VatTu_entity vt) {
        if (vt == null || vt.getMaVT() == null || vt.getMaNCC() == null) {
            return false;
        }

        String sql = "INSERT INTO VatTu "
                   + "(maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, maThuoc) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vt.getMaVT());
            ps.setString(2, vt.getMaNCC().getMaNCC());
            ps.setString(3, vt.getTenVT());
            ps.setInt(4, vt.getSoLuongTon());
            ps.setString(5, vt.getTrangThai());
            ps.setDouble(6, vt.getGiaBanCoBan());
            ps.setDouble(7, vt.getGiaNhap());
            ps.setString(8, vt.getMaThuoc() != null ? vt.getMaThuoc().getMaThuoc() : null);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa vật tư theo mã
     */
    public boolean deleteVatTu(String maVT) {
        if (maVT == null || maVT.isBlank()) return false;
        String sql = "DELETE FROM VatTu WHERE maVT = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVT.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin vật tư
     */
    public boolean updateVatTu(VatTu_entity vt) {
        if (vt == null || vt.getMaVT() == null || vt.getMaNCC() == null) {
            return false;
        }

        String sql = "UPDATE VatTu SET "
                   + "maNCC = ?, tenVT = ?, soLuongTon = ?, trangThai = ?, "
                   + "giaBanCoBan = ?, giaNhap = ?, maThuoc = ? "
                   + "WHERE maVT = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vt.getMaNCC().getMaNCC());
            ps.setString(2, vt.getTenVT());
            ps.setInt(3, vt.getSoLuongTon());
            ps.setString(4, vt.getTrangThai());
            ps.setDouble(5, vt.getGiaBanCoBan());
            ps.setDouble(6, vt.getGiaNhap());
            ps.setString(7, vt.getMaThuoc() != null ? vt.getMaThuoc().getMaThuoc() : null);
            ps.setString(8, vt.getMaVT());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm vật tư theo mã
     */
    public VatTu_entity getVatTuTheoMa(String maVT) {
        if (maVT == null || maVT.isBlank()) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE v.maVT = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVT.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Tìm vật tư theo mã thuốc liên kết
     */
    public VatTu_entity getVatTuTheoMaThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE v.maThuoc = ?";
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return null;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuoc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sinh mã vật tư mới theo cấu trúc VT + số thứ tự 3 chữ số (VT001, VT002, ...).
     * Lấy số lớn nhất hiện có rồi cộng thêm 1.
     */
    public String taoMaVatTuMoi() {
        int max = 0;
        Connection con = ConnectDB.getCon();
        if (con != null) {
            try (PreparedStatement ps = con.prepareStatement("SELECT maVT FROM VatTu");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString(1);
                    if (ma != null && ma.trim().matches("(?i)VT\\d+")) {
                        int so = Integer.parseInt(ma.trim().substring(2));
                        if (so > max) max = so;
                    }
                }
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return String.format("VT%03d", max + 1);
    }

    /**
     * Lấy danh sách nhà cung cấp {maNCC, tenNCC} để đổ vào ComboBox ở form thêm/sửa vật tư.
     */
    public ArrayList<String[]> getDanhSachNCC() {
        ArrayList<String[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return ds;
        }

        String sql = "SELECT maNCC, tenNCC FROM NhaCungCap ORDER BY maNCC ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new String[] { rs.getString("maNCC"), rs.getString("tenNCC") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    private VatTu_entity mapRowToEntity(ResultSet rs) throws SQLException {
        String maNCC = rs.getString("maNCC");
        NhaCungCap_entity ncc = null;
        if (maNCC != null) {
            ncc = new NhaCungCap_entity(
                    maNCC,
                    rs.getString("tenNCC"),
                    rs.getString("diaChi"),
                    rs.getString("sdt"),
                    rs.getString("email")
            );
        }

        return new VatTu_entity(
                rs.getString("maVT"),
                ncc,
                rs.getString("tenVT"),
                rs.getInt("soLuongTon"),
                rs.getString("trangThai"),
                rs.getDouble("giaBanCoBan"),
                rs.getDouble("giaNhap"),
                layMaThuoc(rs)
        );
    }

    private String layMaThuoc(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if ("maThuoc".equalsIgnoreCase(metadata.getColumnLabel(i))) {
                return rs.getString(i);
            }
        }
        return null;
    }
}
