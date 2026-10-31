package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.BacSi_entity;

public class BacSi_DAO {

    /**
     * Lấy toàn bộ danh sách bác sĩ kèm trạng thái tài khoản và tổng số ca khám tháng hiện tại
     */
    public List<BacSi_entity> getAll() throws SQLException {
        List<BacSi_entity> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT bs.maBacSi, bs.maTK, bs.hoTen, bs.gioiTinh, bs.sdt, cn.tenChuyenNganh, bs.email, bs.ngaySinh, bs.luongThang, " +
                     "tk.trangThai AS trangThaiTK, " +
                     "(SELECT COUNT(*) FROM HoSoSucKhoe h WHERE h.maBacSi = bs.maBacSi " +
                     " AND MONTH(h.ngayKham) = MONTH(GETDATE()) AND YEAR(h.ngayKham) = YEAR(GETDATE())) AS soCaKham " +
                     "FROM BacSyThuY bs " +
                     "JOIN ChuyenNganh cn ON cn.maChuyenNganh = bs.maChuyenNganh " +
                     "LEFT JOIN TaiKhoan tk ON bs.maTK = tk.maTK " +
                     "ORDER BY bs.maBacSi ASC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date nsSQL = rs.getDate("ngaySinh");
                LocalDate ngaySinh = nsSQL != null ? nsSQL.toLocalDate() : null;

                Boolean trangThaiTK = null;
                if (rs.getObject("trangThaiTK") != null) {
                    trangThaiTK = rs.getBoolean("trangThaiTK");
                }

                BacSi_entity bs = new BacSi_entity(
                    rs.getString("maBacSi"),
                    rs.getString("maTK"),
                    rs.getString("hoTen"),
                    rs.getString("gioiTinh"),
                    rs.getString("sdt"),
                    rs.getString("tenChuyenNganh"),
                    rs.getString("email"),
                    ngaySinh,
                    rs.getDouble("luongThang"),
                    rs.getInt("soCaKham"),
                    trangThaiTK
                );
                ds.add(bs);
            }
        }
        return ds;
    }

    /**
     * Lấy tổng số nhân viên cửa hàng (cho thẻ thông tin "Thuộc N nhân viên")
     */
    public int getTongNhanVien() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        String sql = "SELECT COUNT(*) FROM NhanVien";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Phát sinh tự động mã bác sĩ tiếp theo (ví dụ: BS016)
     */
    public String getMaTiepTheo() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return "BS001";

        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(maBacSi, 3, LEN(maBacSi)) AS INT)), 0) FROM BacSyThuY WHERE maBacSi LIKE 'BS%'";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("BS%03d", max + 1);
            }
        }
        return "BS001";
    }

    /**
     * Thêm bác sĩ mới vào CSDL
     */
    public boolean them(BacSi_entity bs) throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        ChuyenNganh_DAO cnDao = new ChuyenNganh_DAO();
        String maCN = cnDao.getMaTheoTen(bs.getChuyenNganh());
        if (maCN == null) {
            cnDao.them(bs.getChuyenNganh());
            maCN = cnDao.getMaTheoTen(bs.getChuyenNganh());
        }

        String sql = "INSERT INTO BacSyThuY (maBacSi, maTK, maChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh, luongThang) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bs.getMaBacSi());
            if (bs.getMaTK() != null && !bs.getMaTK().isBlank()) {
                ps.setString(2, bs.getMaTK());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, maCN);
            ps.setString(4, bs.getHoTen());
            ps.setString(5, bs.getGioiTinh());
            ps.setString(6, bs.getSdt());
            ps.setString(7, bs.getEmail());
            if (bs.getNgaySinh() != null) {
                ps.setDate(8, Date.valueOf(bs.getNgaySinh()));
            } else {
                ps.setNull(8, Types.DATE);
            }
            ps.setDouble(9, bs.getLuongThang());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin bác sĩ
     */
    public boolean capNhat(BacSi_entity bs) throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        ChuyenNganh_DAO cnDao = new ChuyenNganh_DAO();
        String maCN = cnDao.getMaTheoTen(bs.getChuyenNganh());
        if (maCN == null) {
            cnDao.them(bs.getChuyenNganh());
            maCN = cnDao.getMaTheoTen(bs.getChuyenNganh());
        }

        String sql = "UPDATE BacSyThuY SET maChuyenNganh = ?, hoTen = ?, gioiTinh = ?, " +
                     "sdt = ?, email = ?, ngaySinh = ?, luongThang = ? WHERE maBacSi = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCN);
            ps.setString(2, bs.getHoTen());
            ps.setString(3, bs.getGioiTinh());
            ps.setString(4, bs.getSdt());
            ps.setString(5, bs.getEmail());
            if (bs.getNgaySinh() != null) {
                ps.setDate(6, Date.valueOf(bs.getNgaySinh()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setDouble(7, bs.getLuongThang());
            ps.setString(8, bs.getMaBacSi());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa bác sĩ theo mã
     */
    public boolean xoa(String maBacSi) throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM BacSyThuY WHERE maBacSi = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBacSi);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi trạng thái tài khoản của bác sĩ (Hoạt động / Tạm nghỉ)
     */
    public boolean doiTrangThaiTaiKhoan(String maBacSi, boolean trangThai) throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maTK = (SELECT maTK FROM BacSyThuY WHERE maBacSi = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setString(2, maBacSi);
            return ps.executeUpdate() > 0;
        }
    }
}