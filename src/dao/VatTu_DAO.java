package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.VatTu_entity;
import entity.NhaCungCap_entity;

public class VatTu_DAO {

    private NhaCungCap_DAO nccDAO = new NhaCungCap_DAO();

    /**
     * Lấy toàn bộ danh sách vật tư
     * Đồng thời lấy thông tin nhà cung cấp theo maNCC
     */
    public ArrayList<VatTu_entity> getAllVatTu() {
        ArrayList<VatTu_entity> dsVT = new ArrayList<>();

        String sql = "SELECT * FROM VatTu";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsVT;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maNCC = rs.getString("maNCC");

                    // Lấy đối tượng nhà cung cấp theo mã
                    NhaCungCap_entity ncc =
                            maNCC != null
                            ? nccDAO.getNhaCungCapTheoMa(maNCC)
                            : null;

                    VatTu_entity vt = new VatTu_entity(
                        rs.getString("maVT"),
                        ncc,
                        rs.getString("tenVT"),
                        rs.getInt("soLuongTon"),
                        rs.getString("trangThai"),
                        rs.getDouble("giaBanCoBan"),
                        rs.getDouble("giaNhap"),
                        layMaThuoc(rs)
                    );

                    dsVT.add(vt);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsVT;
    }

    /**
     * Thêm vật tư mới
     * Lấy maNCC từ đối tượng NhaCungCap_entity
     */
    public boolean createVatTu(VatTu_entity vt) {
        String sql = "INSERT INTO VatTu "
                   + "(maVT, maNCC, tenVT, soLuongTon, "
                   + "trangThai, giaBanCoBan, giaNhap, maThuoc) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || vt.getMaNCC() == null) {
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
                ps.setString(8, vt.getMaThuoc());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa vật tư theo mã
     */
    public boolean deleteVatTu(String maVT) {
        String sql = "DELETE FROM VatTu WHERE maVT = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maVT);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin vật tư
     */
    public boolean updateVatTu(VatTu_entity vt) {
        String sql = "UPDATE VatTu SET "
                   + "maNCC = ?, tenVT = ?, soLuongTon = ?, "
                   + "trangThai = ?, giaBanCoBan = ?, giaNhap = ?, maThuoc = ? "
                   + "WHERE maVT = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || vt.getMaNCC() == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, vt.getMaNCC().getMaNCC());
                ps.setString(2, vt.getTenVT());
                ps.setInt(3, vt.getSoLuongTon());
                ps.setString(4, vt.getTrangThai());
                ps.setDouble(5, vt.getGiaBanCoBan());
                ps.setDouble(6, vt.getGiaNhap());
                ps.setString(7, vt.getMaThuoc());
                ps.setString(8, vt.getMaVT());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm vật tư theo mã
     */
    public VatTu_entity getVatTuTheoMa(String maVT) {
        String sql = "SELECT * FROM VatTu WHERE maVT = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maVT);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maNCC = rs.getString("maNCC");

                        // Lấy đối tượng nhà cung cấp từ DAO
                        NhaCungCap_entity ncc =
                                maNCC != null
                                ? nccDAO.getNhaCungCapTheoMa(maNCC)
                                : null;

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
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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
