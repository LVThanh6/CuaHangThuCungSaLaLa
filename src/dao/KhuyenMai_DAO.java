
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.KhuyenMai_Entity;

public class KhuyenMai_DAO {

    /**
     * Lấy toàn bộ danh sách khuyến mãi
     */
    public ArrayList<KhuyenMai_Entity> getAllKhuyenMai() {
        ArrayList<KhuyenMai_Entity> dsKM = new ArrayList<>();

        String sql = "SELECT maKM, tenKM, mucKM, ngayBatDau, "
                   + "ngayKetThuc, dieuKien FROM KhuyenMai";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsKM;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM");
                    double mucKM = rs.getDouble("mucKM");

                    Date ngayBD = rs.getDate("ngayBatDau");
                    Date ngayKT = rs.getDate("ngayKetThuc");

                    java.time.LocalDate ngayBatDau =
                            ngayBD != null ? ngayBD.toLocalDate() : null;

                    java.time.LocalDate ngayKetThuc =
                            ngayKT != null ? ngayKT.toLocalDate() : null;

                    String dieuKien = rs.getString("dieuKien");

                    KhuyenMai_Entity km = new KhuyenMai_Entity(
                            maKM, tenKM, mucKM,
                            ngayBatDau, ngayKetThuc, dieuKien
                    );

                    dsKM.add(km);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsKM;
    }

    /**
     * Thêm một khuyến mãi mới
     */
    public boolean createKhuyenMai(KhuyenMai_Entity km) {
        String sql = "INSERT INTO KhuyenMai "
                   + "(maKM, tenKM, mucKM, ngayBatDau, ngayKetThuc, dieuKien) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, km.getMaKM());
                ps.setString(2, km.getTenKM());
                ps.setDouble(3, km.getMucKM());

                if (km.getNgayBatDau() != null) {
                    ps.setDate(4, Date.valueOf(km.getNgayBatDau()));
                } else {
                    ps.setNull(4, java.sql.Types.DATE);
                }

                if (km.getNgayKetThuc() != null) {
                    ps.setDate(5, Date.valueOf(km.getNgayKetThuc()));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }

                ps.setString(6, km.getDieuKien());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa khuyến mãi theo mã
     */
    public boolean deleteKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKM);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin khuyến mãi theo mã
     */
    public boolean updateKhuyenMai(KhuyenMai_Entity km) {
        String sql = "UPDATE KhuyenMai "
                   + "SET tenKM = ?, mucKM = ?, ngayBatDau = ?, "
                   + "ngayKetThuc = ?, dieuKien = ? "
                   + "WHERE maKM = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, km.getTenKM());
                ps.setDouble(2, km.getMucKM());

                if (km.getNgayBatDau() != null) {
                    ps.setDate(3, Date.valueOf(km.getNgayBatDau()));
                } else {
                    ps.setNull(3, java.sql.Types.DATE);
                }

                if (km.getNgayKetThuc() != null) {
                    ps.setDate(4, Date.valueOf(km.getNgayKetThuc()));
                } else {
                    ps.setNull(4, java.sql.Types.DATE);
                }

                ps.setString(5, km.getDieuKien());
                ps.setString(6, km.getMaKM());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm khuyến mãi theo mã chính xác
     */
    public KhuyenMai_Entity getKhuyenMaiTheoMa(String maKM) {
        String sql = "SELECT maKM, tenKM, mucKM, ngayBatDau, "
                   + "ngayKetThuc, dieuKien "
                   + "FROM KhuyenMai WHERE maKM = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKM);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Date ngayBD = rs.getDate("ngayBatDau");
                        Date ngayKT = rs.getDate("ngayKetThuc");

                        java.time.LocalDate ngayBatDau =
                                ngayBD != null ? ngayBD.toLocalDate() : null;

                        java.time.LocalDate ngayKetThuc =
                                ngayKT != null ? ngayKT.toLocalDate() : null;

                        return new KhuyenMai_Entity(
                                rs.getString("maKM"),
                                rs.getString("tenKM"),
                                rs.getDouble("mucKM"),
                                ngayBatDau,
                                ngayKetThuc,
                                rs.getString("dieuKien")
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
