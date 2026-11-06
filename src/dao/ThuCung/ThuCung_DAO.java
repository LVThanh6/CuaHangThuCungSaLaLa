package dao.ThuCung;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.KhachHang.KhachHang;
import entity.ThuCung.ThuCung_entity;

/**
 * DAO Thú Cưng (Đã tối ưu hóa bằng JOIN, triệt tiêu N+1 Query).
 */
public class ThuCung_DAO {

    private static final String SELECT_BASE =
            "SELECT tc.maTC, tc.maKH, tc.tenTC, tc.loai, tc.gioiTinh, tc.ngaySinh, tc.chuHo, tc.dacDiemNhanDien, " +
            "kh.hoTen, kh.sdt, kh.email, kh.diaChi " +
            "FROM ThuCung tc " +
            "LEFT JOIN KhachHang kh ON kh.maKH = tc.maKH ";

    /**
     * Lấy toàn bộ danh sách thú cưng kèm thông tin khách hàng bằng 1 query JOIN.
     */
    public ArrayList<ThuCung_entity> getAllThuCung() {
        ArrayList<ThuCung_entity> dsTC = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsTC;
        }

        String sql = SELECT_BASE + "ORDER BY tc.maTC ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsTC.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsTC;
    }

    /**
     * Tìm thú cưng theo mã chính xác
     */
    public ThuCung_entity getThuCungTheoMa(String maTC) {
        if (maTC == null || maTC.isBlank()) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE tc.maTC = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTC.trim());
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
     * Lấy danh sách thú cưng thuộc một khách hàng
     */
    public ArrayList<ThuCung_entity> getThuCungTheoMaKH(String maKH) {
        ArrayList<ThuCung_entity> dsTC = new ArrayList<>();
        if (maKH == null || maKH.isBlank()) return dsTC;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsTC;
        }

        String sql = SELECT_BASE + "WHERE tc.maKH = ? ORDER BY tc.maTC ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsTC.add(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsTC;
    }

    /**
     * Thêm thú cưng mới.
     */
    public boolean createThuCung(ThuCung_entity tc) {
        if (tc == null || tc.getMaTC() == null || tc.getMaKH() == null) {
            return false;
        }

        String sql = "INSERT INTO ThuCung "
                   + "(maTC, maKH, tenTC, loai, gioiTinh, ngaySinh, chuHo, dacDiemNhanDien) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getMaTC());
            ps.setString(2, tc.getMaKH().getMaKH());
            ps.setString(3, tc.getTenTC());
            ps.setString(4, tc.getLoai());
            ps.setString(5, tc.getGioiTinh());

            if (tc.getNgaySinh() != null) {
                ps.setDate(6, Date.valueOf(tc.getNgaySinh()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }

            ps.setString(7, tc.getChuHo());
            ps.setString(8, tc.getDacDiemNhanDien());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa thú cưng theo mã
     */
    public boolean deleteThuCung(String maTC) {
        if (maTC == null || maTC.isBlank()) return false;
        String sql = "DELETE FROM ThuCung WHERE maTC = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTC.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin thú cưng
     */
    public boolean updateThuCung(ThuCung_entity tc) {
        if (tc == null || tc.getMaTC() == null || tc.getMaKH() == null) {
            return false;
        }

        String sql = "UPDATE ThuCung SET "
                   + "maKH = ?, tenTC = ?, loai = ?, gioiTinh = ?, "
                   + "ngaySinh = ?, chuHo = ?, dacDiemNhanDien = ? "
                   + "WHERE maTC = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getMaKH().getMaKH());
            ps.setString(2, tc.getTenTC());
            ps.setString(3, tc.getLoai());
            ps.setString(4, tc.getGioiTinh());

            if (tc.getNgaySinh() != null) {
                ps.setDate(5, Date.valueOf(tc.getNgaySinh()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.setString(6, tc.getChuHo());
            ps.setString(7, tc.getDacDiemNhanDien());
            ps.setString(8, tc.getMaTC());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ThuCung_entity mapRowToEntity(ResultSet rs) throws SQLException {
        String maKH = rs.getString("maKH");
        KhachHang kh = null;
        if (maKH != null) {
            kh = new KhachHang(
                    maKH,
                    rs.getString("hoTen"),
                    rs.getString("sdt"),
                    rs.getString("email"),
                    rs.getString("diaChi")
            );
        }

        Date ngay = rs.getDate("ngaySinh");
        java.time.LocalDate ngaySinh = ngay != null ? ngay.toLocalDate() : null;

        return new ThuCung_entity(
                rs.getString("maTC"),
                kh,
                rs.getString("tenTC"),
                rs.getString("loai"),
                rs.getString("gioiTinh"),
                ngaySinh,
                rs.getString("chuHo"),
                rs.getString("dacDiemNhanDien")
        );
    }
}
