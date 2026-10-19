
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon_entity;
import entity.HoaDon_entity;
import entity.DichVu_entity;
import entity.VatTu_entity;

public class ChiTietHoaDon_DAO {

    private final HoaDon_Dao hdDAO = new HoaDon_Dao();
    private final DichVu_DAO dvDAO = new DichVu_DAO();
    private final VatTu_DAO vtDAO = new VatTu_DAO();

    // 1. Lấy toàn bộ danh sách chi tiết hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getAllChiTietHoaDon() {
        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, soLuong, donGia
            FROM ChiTietHoaDon
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsCTHD;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maHD = rs.getString("maHD");
                    String maDV = rs.getString("maDV");
                    String maVT = rs.getString("maVT");

                    HoaDon_entity hd =
                        hdDAO.getHDTheoMa(maHD);

                    DichVu_entity dv = maDV != null
                        ? dvDAO.getDichVuTheoMa(maDV)
                        : null;

                    VatTu_entity vt = maVT != null
                        ? vtDAO.getVatTuTheoMa(maVT)
                        : null;

                    ChiTietHoaDon_entity cthd =
                        new ChiTietHoaDon_entity(
                            rs.getString("maCTHD"),
                            hd,
                            dv,
                            vt,
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                        );

                    dsCTHD.add(cthd);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    // 2. Thêm chi tiết hóa đơn
    public boolean createChiTietHoaDon(
            ChiTietHoaDon_entity ct) {

        if (!duLieuHopLe(ct)) {
            return false;
        }

        String sql = """
            INSERT INTO ChiTietHoaDon
                (maCTHD, maHD, maDV, maVT, soLuong, donGia)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ct.getMaCTHD());
                ps.setString(2, ct.getMaHD().getMaHD());

                // Mỗi dòng chỉ có dịch vụ hoặc vật tư
                if (ct.getMaDV() != null) {
                    ps.setString(3, ct.getMaDV().getMaDV());
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                    ps.setString(4, ct.getMaVT().getMaVT());
                }

                ps.setInt(5, ct.getSoLuong());
                ps.setDouble(6, ct.getDonGia());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 3. Xóa chi tiết hóa đơn theo mã
    public boolean deleteChiTietHoaDon(String maCTHD) {
        String sql =
            "DELETE FROM ChiTietHoaDon WHERE maCTHD = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || maCTHD == null
                    || maCTHD.isBlank()) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCTHD);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 4. Cập nhật chi tiết hóa đơn
    public boolean updateChiTietHoaDon(
            ChiTietHoaDon_entity ct) {

        if (!duLieuHopLe(ct)) {
            return false;
        }

        String sql = """
            UPDATE ChiTietHoaDon
            SET maHD = ?, maDV = ?, maVT = ?,
                soLuong = ?, donGia = ?
            WHERE maCTHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ct.getMaHD().getMaHD());

                if (ct.getMaDV() != null) {
                    ps.setString(2, ct.getMaDV().getMaDV());
                    ps.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                    ps.setString(3, ct.getMaVT().getMaVT());
                }

                ps.setInt(4, ct.getSoLuong());
                ps.setDouble(5, ct.getDonGia());
                ps.setString(6, ct.getMaCTHD());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 5. Tìm chi tiết hóa đơn theo mã
    public ChiTietHoaDon_entity getChiTietHoaDonTheoMa(
            String maCTHD) {

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, soLuong, donGia
            FROM ChiTietHoaDon
            WHERE maCTHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCTHD);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return taoChiTietHoaDon(rs);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 6. Lấy danh sách chi tiết theo mã hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getChiTietHoaDonTheoMaHD(
            String maHD) {

        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, soLuong, donGia
            FROM ChiTietHoaDon
            WHERE maHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsCTHD;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dsCTHD.add(taoChiTietHoaDon(rs));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    // 7. Chuyển một dòng ResultSet thành đối tượng entity
    private ChiTietHoaDon_entity taoChiTietHoaDon(
            ResultSet rs) throws SQLException {

        String maHD = rs.getString("maHD");
        String maDV = rs.getString("maDV");
        String maVT = rs.getString("maVT");

        HoaDon_entity hd = hdDAO.getHDTheoMa(maHD);

        DichVu_entity dv = maDV != null
            ? dvDAO.getDichVuTheoMa(maDV)
            : null;

        VatTu_entity vt = maVT != null
            ? vtDAO.getVatTuTheoMa(maVT)
            : null;

        return new ChiTietHoaDon_entity(
            rs.getString("maCTHD"),
            hd,
            dv,
            vt,
            rs.getInt("soLuong"),
            rs.getDouble("donGia")
        );
    }

    // 8. Kiểm tra dữ liệu trước khi thêm hoặc cập nhật
    private boolean duLieuHopLe(ChiTietHoaDon_entity ct) {
        if (ct == null
                || ct.getMaCTHD() == null
                || ct.getMaCTHD().isBlank()
                || ct.getMaHD() == null
                || ct.getSoLuong() <= 0
                || ct.getDonGia() < 0) {
            return false;
        }

        // Phải có đúng một trong hai: dịch vụ hoặc vật tư
        boolean coDichVu = ct.getMaDV() != null;
        boolean coVatTu = ct.getMaVT() != null;

        return coDichVu ^ coVatTu;
    }
}
