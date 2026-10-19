
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.ThuCung_entity;
import entity.KhachHang;

public class ThuCung_DAO {

    private KhachHang_DAO khDAO = new KhachHang_DAO();

    /**
     * Lấy toàn bộ danh sách thú cưng.
     * Lấy maKH từ bảng ThuCung,
     * sau đó dùng KhachHang_DAO lấy đối tượng khách hàng.
     */
    public ArrayList<ThuCung_entity> getAllThuCung() {
        ArrayList<ThuCung_entity> dsTC = new ArrayList<>();

        String sql = "SELECT maTC, maKH, tenTC, loai, gioiTinh, "
                   + "ngaySinh, chuHo, dacDiemNhanDien "
                   + "FROM ThuCung";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsTC;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maKH = rs.getString("maKH");

                    // Lấy đối tượng khách hàng theo mã
                    KhachHang kh = maKH != null
                            ? khDAO.getKhachHangTheoMa(maKH)
                            : null;

                    Date ngay = rs.getDate("ngaySinh");
                    java.time.LocalDate ngaySinh =
                            ngay != null ? ngay.toLocalDate() : null;

                    ThuCung_entity tc = new ThuCung_entity(
                            rs.getString("maTC"),
                            kh,
                            rs.getString("tenTC"),
                            rs.getString("loai"),
                            rs.getString("gioiTinh"),
                            ngaySinh,
                            rs.getString("chuHo"),
                            rs.getString("dacDiemNhanDien")
                    );

                    dsTC.add(tc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsTC;
    }

    /**
     * Thêm thú cưng mới.
     * Lấy maKH từ đối tượng KhachHang.
     */
    public boolean createThuCung(ThuCung_entity tc) {
        String sql = "INSERT INTO ThuCung "
                   + "(maTC, maKH, tenTC, loai, gioiTinh, "
                   + "ngaySinh, chuHo, dacDiemNhanDien) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || tc.getMaKH() == null) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa thú cưng theo mã
     */
    public boolean deleteThuCung(String maTC) {
        String sql = "DELETE FROM ThuCung WHERE maTC = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maTC);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin thú cưng
     */
    public boolean updateThuCung(ThuCung_entity tc) {
        String sql = "UPDATE ThuCung SET "
                   + "maKH = ?, tenTC = ?, loai = ?, gioiTinh = ?, "
                   + "ngaySinh = ?, chuHo = ?, dacDiemNhanDien = ? "
                   + "WHERE maTC = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || tc.getMaKH() == null) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm thú cưng theo mã chính xác
     */
    public ThuCung_entity getThuCungTheoMa(String maTC) {
        String sql = "SELECT maTC, maKH, tenTC, loai, gioiTinh, "
                   + "ngaySinh, chuHo, dacDiemNhanDien "
                   + "FROM ThuCung WHERE maTC = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maTC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maKH = rs.getString("maKH");

                        KhachHang kh = maKH != null
                                ? khDAO.getKhachHangTheoMa(maKH)
                                : null;

                        Date ngay = rs.getDate("ngaySinh");
                        java.time.LocalDate ngaySinh =
                                ngay != null ? ngay.toLocalDate() : null;

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

        String sql = "SELECT maTC, maKH, tenTC, loai, gioiTinh, "
                   + "ngaySinh, chuHo, dacDiemNhanDien "
                   + "FROM ThuCung WHERE maKH = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsTC;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKH);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // Lấy đối tượng khách hàng qua KhachHang_DAO
                        KhachHang kh = khDAO.getKhachHangTheoMa(
                                rs.getString("maKH")
                        );

                        Date ngay = rs.getDate("ngaySinh");
                        java.time.LocalDate ngaySinh =
                                ngay != null ? ngay.toLocalDate() : null;

                        ThuCung_entity tc = new ThuCung_entity(
                                rs.getString("maTC"),
                                kh,
                                rs.getString("tenTC"),
                                rs.getString("loai"),
                                rs.getString("gioiTinh"),
                                ngaySinh,
                                rs.getString("chuHo"),
                                rs.getString("dacDiemNhanDien")
                        );

                        dsTC.add(tc);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsTC;
    }
}
