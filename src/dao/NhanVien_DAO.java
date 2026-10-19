
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.NhanVien_entity;

public class NhanVien_DAO {

    public String phatSinhMaNhanVien() {
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maNhanVien, 3, "
                   + "LEN(maNhanVien)))), 0) + 1 "
                   + "FROM NhanVien WHERE maNhanVien LIKE 'NV%'";
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format("NV%03d", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo mã nhân viên: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách nhân viên
     */
    public ArrayList<NhanVien_entity> getAllNhanVien() {
        ArrayList<NhanVien_entity> dsNV = new ArrayList<>();

        String sql = "SELECT maNhanVien, maTK, hoTen, chucVu, "
                   + "sdt, diaChi, ngaySinh, gioiTinh, luongCoBan "
                   + "FROM NhanVien";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsNV;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maNhanVien = rs.getString("maNhanVien");
                    String maTK = rs.getString("maTK");
                    String hoTen = rs.getString("hoTen");
                    String chucVu = rs.getString("chucVu");
                    String sdt = rs.getString("sdt");
                    String diaChi = rs.getString("diaChi");

                    Date ngay = rs.getDate("ngaySinh");
                    java.time.LocalDate ngaySinh =
                            ngay != null ? ngay.toLocalDate() : null;

                    String gioiTinh = rs.getString("gioiTinh");
                    double luongCoBan = rs.getDouble("luongCoBan");

                    NhanVien_entity nv = new NhanVien_entity(
                            maNhanVien, maTK, hoTen, chucVu,
                            sdt, diaChi, ngaySinh, gioiTinh, luongCoBan
                    );

                    dsNV.add(nv);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsNV;
    }

    /**
     * Thêm một nhân viên mới
     */
    public boolean createNhanVien(NhanVien_entity nv) {
        String sql = "INSERT INTO NhanVien "
                   + "(maNhanVien, maTK, hoTen, chucVu, sdt, "
                   + "diaChi, ngaySinh, gioiTinh, luongCoBan) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nv.getMaNhanVien());
                ps.setString(2, nv.getMaTK());
                ps.setString(3, nv.getHoTen());
                ps.setString(4, nv.getChucVu());
                ps.setString(5, nv.getSdt());
                ps.setString(6, nv.getDiaChi());

                if (nv.getNgaySinh() != null) {
                    ps.setDate(7, Date.valueOf(nv.getNgaySinh()));
                } else {
                    ps.setNull(7, java.sql.Types.DATE);
                }

                ps.setString(8, nv.getGioiTinh());
                ps.setDouble(9, nv.getLuongCoBan());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa nhân viên theo mã
     */
    public boolean deleteNhanVien(String maNhanVien) {
        String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNhanVien);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin nhân viên theo mã
     */
    public boolean updateNhanVien(NhanVien_entity nv) {
        String sql = "UPDATE NhanVien SET "
                   + "maTK = ?, hoTen = ?, chucVu = ?, sdt = ?, "
                   + "diaChi = ?, ngaySinh = ?, gioiTinh = ?, "
                   + "luongCoBan = ? "
                   + "WHERE maNhanVien = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nv.getMaTK());
                ps.setString(2, nv.getHoTen());
                ps.setString(3, nv.getChucVu());
                ps.setString(4, nv.getSdt());
                ps.setString(5, nv.getDiaChi());

                if (nv.getNgaySinh() != null) {
                    ps.setDate(6, Date.valueOf(nv.getNgaySinh()));
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }

                ps.setString(7, nv.getGioiTinh());
                ps.setDouble(8, nv.getLuongCoBan());
                ps.setString(9, nv.getMaNhanVien());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm nhân viên theo mã chính xác
     */
    public NhanVien_entity getNhanVienTheoMa(String maNhanVien) {
        String sql = "SELECT maNhanVien, maTK, hoTen, chucVu, "
                   + "sdt, diaChi, ngaySinh, gioiTinh, luongCoBan "
                   + "FROM NhanVien WHERE maNhanVien = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNhanVien);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Date ngay = rs.getDate("ngaySinh");

                        java.time.LocalDate ngaySinh =
                                ngay != null ? ngay.toLocalDate() : null;

                        return new NhanVien_entity(
                                rs.getString("maNhanVien"),
                                rs.getString("maTK"),
                                rs.getString("hoTen"),
                                rs.getString("chucVu"),
                                rs.getString("sdt"),
                                rs.getString("diaChi"),
                                ngaySinh,
                                rs.getString("gioiTinh"),
                                rs.getDouble("luongCoBan")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
