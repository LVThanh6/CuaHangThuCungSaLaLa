package dao;

import connectDB.ConnectDB;
import entity.NhaCungCap_entity;

import java.sql.*;
import java.util.ArrayList;

/**
 * DAO bảng NhaCungCap - dùng chung kết nối của ConnectDB (SQL Server "SALALA").
 *
 * Lưu ý: ConnectDB trả về connection proxy, con.close() không đóng kết nối thật,
 * nên dùng try-with-resources vẫn an toàn.
 *
 * Các phương thức giữ nguyên chữ ký cũ (không ném exception). Khi lỗi, nội dung lỗi
 * dễ hiểu được lưu ở getLastError() để giao diện hiển thị.
 */
public class NhaCungCap_DAO {

    private String lastError = null;

    /** Thông báo lỗi của thao tác gần nhất; null nếu thành công. */
    public String getLastError() {
        return lastError;
    }

    private Connection getConnection() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Khong ket noi duoc CSDL SALALA. Kiem tra SQL Server service, "
                    + "mat khau (-Dsalala.db.password=... hoac bien moi truong SALALA_DB_PASSWORD) va file mssql-jdbc.jar.");
        }
        return con;
    }

    private void setError(SQLException e) {
        e.printStackTrace();
        switch (e.getErrorCode()) {
            case 547:  // vi phạm khóa ngoại
                lastError = "Nha cung cap dang duoc su dung o bang VatTu hoac PhieuNhapKho nen khong the xoa.";
                break;
            case 2627: // trùng khóa chính
            case 2601:
                lastError = "Ma nha cung cap da ton tai.";
                break;
            default:
                lastError = e.getMessage();
        }
    }

    // Lấy toàn bộ danh sách nhà cung cấp
    public ArrayList<NhaCungCap_entity> getAllNhaCungCap() {
        lastError = null;
        ArrayList<NhaCungCap_entity> ds = new ArrayList<>();
        String sql = "SELECT maNCC, tenNCC, diaChi, sdt, email FROM NhaCungCap ORDER BY maNCC";

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(new NhaCungCap_entity(
                        rs.getString("maNCC"),
                        rs.getString("tenNCC"),
                        rs.getString("diaChi"),
                        rs.getString("sdt"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            setError(e);
        }
        return ds;
    }

    // Thêm nhà cung cấp
    public boolean createNhaCungCap(NhaCungCap_entity ncc) {
        lastError = null;
        String sql = "INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, sdt, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, ncc.getMaNCC());
            pstmt.setString(2, ncc.getTenNCC());
            pstmt.setString(3, nz(ncc.getDiaChi()));   // cột NOT NULL
            pstmt.setString(4, nz(ncc.getSdt()));      // cột NOT NULL
            pstmt.setString(5, nz(ncc.getEmail()));    // cột NOT NULL

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setError(e);
        }
        return false;
    }

    // Cập nhật nhà cung cấp
    public boolean updateNhaCungCap(NhaCungCap_entity ncc) {
        lastError = null;
        String sql = "UPDATE NhaCungCap SET tenNCC = ?, diaChi = ?, sdt = ?, email = ? WHERE maNCC = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, ncc.getTenNCC());
            pstmt.setString(2, nz(ncc.getDiaChi()));
            pstmt.setString(3, nz(ncc.getSdt()));
            pstmt.setString(4, nz(ncc.getEmail()));
            pstmt.setString(5, ncc.getMaNCC());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setError(e);
        }
        return false;
    }

    // Xóa nhà cung cấp
    public boolean deleteNhaCungCap(String maNCC) {
        lastError = null;
        String sql = "DELETE FROM NhaCungCap WHERE maNCC = ?";
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, maNCC);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setError(e);
        }
        return false;
    }

    /** Sinh mã mới NCC001, NCC002... theo mã lớn nhất trong CSDL; trả về null nếu lỗi. */
    public String taoMaMoi() {
        lastError = null;
        int max = 0;
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT maNCC FROM NhaCungCap")) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.startsWith("NCC")) {
                    try {
                        max = Math.max(max, Integer.parseInt(ma.substring(3)));
                    } catch (NumberFormatException ignored) { }
                }
            }
        } catch (SQLException e) {
            setError(e);
            return null;
        }
        return String.format("NCC%03d", max + 1);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}