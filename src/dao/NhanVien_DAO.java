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

    public String getMaTiepTheo() throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maNhanVien, 3, "
                   + "LEN(maNhanVien)))), 0) + 1 "
                   + "FROM NhanVien WHERE maNhanVien LIKE 'NV%'";
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("NV%03d", rs.getInt(1));
            }
        }
        return null;
    }

    public ArrayList<NhanVien_entity> getAll() throws SQLException {
        ArrayList<NhanVien_entity> dsNV = new ArrayList<>();

        // Sử dụng LEFT JOIN để lấy trạng thái từ bảng TaiKhoan
        String sql = "SELECT nv.maNhanVien, nv.maTK, nv.hoTen, nv.chucVu, "
                   + "nv.sdt, nv.diaChi, nv.ngaySinh, nv.gioiTinh, nv.luongCoBan, tk.trangThai "
                   + "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON nv.maTK = tk.maTK";

        Connection con = ConnectDB.getCon();
        if (con == null) return dsNV;

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

                Boolean trangThaiTK = null;
                if (maTK != null) {
                    Object ttObj = rs.getObject("trangThai");
                    if (ttObj != null) {
                        trangThaiTK = rs.getBoolean("trangThai");
                    }
                }

                NhanVien_entity nv = new NhanVien_entity(
                        maNhanVien, maTK, hoTen, chucVu,
                        sdt, diaChi, ngaySinh, gioiTinh, luongCoBan, trangThaiTK
                );

                dsNV.add(nv);
            }
        }
        return dsNV;
    }

    public boolean them(NhanVien_entity nv) throws SQLException {
        String sql = "INSERT INTO NhanVien "
                   + "(maNhanVien, maTK, hoTen, chucVu, sdt, "
                   + "diaChi, ngaySinh, gioiTinh, luongCoBan) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getCon();
        if (con == null) return false;

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
    }

    public boolean xoa(String maNhanVien) throws SQLException {
        String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(NhanVien_entity nv) throws SQLException {
        String sql = "UPDATE NhanVien SET "
                   + "maTK = ?, hoTen = ?, chucVu = ?, sdt = ?, "
                   + "diaChi = ?, ngaySinh = ?, gioiTinh = ?, "
                   + "luongCoBan = ? "
                   + "WHERE maNhanVien = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) return false;

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
    }

    public boolean doiTrangThaiTaiKhoan(String maNhanVien, boolean hoatDong) throws SQLException {
        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maTK = (SELECT maTK FROM NhanVien WHERE maNhanVien = ?)";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, hoatDong);
            ps.setString(2, maNhanVien);
            return ps.executeUpdate() > 0;
        }
    }

    public NhanVien_entity getNhanVienTheoMa(String maNhanVien) throws SQLException {
        String sql = "SELECT nv.maNhanVien, nv.maTK, nv.hoTen, nv.chucVu, "
                   + "nv.sdt, nv.diaChi, nv.ngaySinh, nv.gioiTinh, nv.luongCoBan, tk.trangThai "
                   + "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON nv.maTK = tk.maTK "
                   + "WHERE nv.maNhanVien = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date ngay = rs.getDate("ngaySinh");
                    java.time.LocalDate ngaySinh =
                            ngay != null ? ngay.toLocalDate() : null;

                    Boolean trangThaiTK = null;
                    if (rs.getString("maTK") != null) {
                        Object ttObj = rs.getObject("trangThai");
                        if (ttObj != null) {
                            trangThaiTK = rs.getBoolean("trangThai");
                        }
                    }

                    return new NhanVien_entity(
                            rs.getString("maNhanVien"),
                            rs.getString("maTK"),
                            rs.getString("hoTen"),
                            rs.getString("chucVu"),
                            rs.getString("sdt"),
                            rs.getString("diaChi"),
                            ngaySinh,
                            rs.getString("gioiTinh"),
                            rs.getDouble("luongCoBan"),
                            trangThaiTK
                    );
                }
            }
        }
        return null;
    }
}