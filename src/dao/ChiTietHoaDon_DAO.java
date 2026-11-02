
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.BacSi_entity;
import entity.ChiTietHoaDon_entity;
import entity.HoaDon_entity;
import entity.DichVu_entity;
import entity.NhanVien_entity;
import entity.VatTu_entity;

public class ChiTietHoaDon_DAO {

    private final HoaDon_Dao hdDAO = new HoaDon_Dao();
    private final DichVu_DAO dvDAO = new DichVu_DAO();
    private final VatTu_DAO vtDAO = new VatTu_DAO();

    // 1. Lấy toàn bộ danh sách chi tiết hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getAllChiTietHoaDon() {
        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();

        String sql = """
            SELECT *
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
                    String maNhanVien = rs.getString("maNhanVien");
                    String maBacSi = rs.getString("maBacSi");

                    HoaDon_entity hd =
                        hdDAO.getHDTheoMa(maHD);

                    DichVu_entity dv = maDV != null
                        ? dvDAO.getDichVuTheoMa(maDV)
                        : null;

                    VatTu_entity vt = maVT != null
                        ? vtDAO.getVatTuTheoMa(maVT)
                        : null;

                    NhanVien_entity nv = maNhanVien != null ? new NhanVien_DAO().getNhanVienTheoMa(maNhanVien) : null;
                    BacSi_entity bs = maBacSi != null ? new BacSi_DAO().getAll().stream().filter(x -> x.getMaBacSi().equals(maBacSi)).findFirst().orElse(null) : null;

                    ChiTietHoaDon_entity cthd =
                        new ChiTietHoaDon_entity(
                            rs.getString("maCTHD"),
                            hd,
                            dv,
                            vt,
                            nv,
                            bs,
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                        );
                    cthd.setMaCTDT(layMaCTDT(rs));

                    dsCTHD.add(cthd);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    // 2. Thêm chi tiết hóa đơn
    public boolean createChiTietHoaDon(ChiTietHoaDon_entity ct) {
        if (!duLieuHopLe(ct)) {
            System.out.println("Lỗi: Dữ liệu chi tiết hóa đơn không hợp lệ!");
            return false;
        }

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try {
            return createChiTietHoaDon(con, ct);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean createChiTietHoaDon(Connection con, ChiTietHoaDon_entity ct) throws SQLException {
        if (!duLieuHopLe(ct)) {
            return false;
        }
        String sql = """
            INSERT INTO ChiTietHoaDon
                (maCTHD, maHD, maDV, maVT, maCTDT, maNhanVien, maBacSi, soLuong, donGia)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ct.getMaCTHD());
                ps.setString(2, ct.getMaHD().getMaHD());

                // Dịch vụ hoặc vật tư
                if (ct.getMaDV() != null) {
                    ps.setString(3, ct.getMaDV().getMaDV());
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                    ps.setString(4, ct.getMaVT().getMaVT());
                }

                if (ct.getMaCTDT() != null) {
                    ps.setString(5, ct.getMaCTDT());
                } else {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                }

                // Nhân viên hoặc bác sĩ thực hiện
                if (ct.getNhanVienThucHien() != null) {
                    ps.setString(6,
                        ct.getNhanVienThucHien().getMaNhanVien());
                } else {
                    ps.setNull(6, java.sql.Types.VARCHAR);
                }

                if (ct.getBacSiThucHien() != null) {
                    ps.setString(7,
                        ct.getBacSiThucHien().getMaBacSi());
                } else {
                    ps.setNull(7, java.sql.Types.VARCHAR);
                }

                ps.setInt(8, ct.getSoLuong());
                ps.setDouble(9, ct.getDonGia());
                return ps.executeUpdate() > 0;
        }
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
            SET maHD = ?, maDV = ?, maVT = ?, maCTDT = ?, maNhanVien = ?, maBacSi = ?,
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

                if (ct.getMaCTDT() != null) {
                    ps.setString(4, ct.getMaCTDT());
                } else {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                }

                if (ct.getNhanVienThucHien() != null) {
                    ps.setString(5, ct.getNhanVienThucHien().getMaNhanVien());
                    ps.setNull(6, java.sql.Types.VARCHAR);
                } else if (ct.getBacSiThucHien() != null) {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.setString(6, ct.getBacSiThucHien().getMaBacSi());
                } else {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.setNull(6, java.sql.Types.VARCHAR);
                }
                ps.setInt(7, ct.getSoLuong());
                ps.setDouble(8, ct.getDonGia());
                ps.setString(9, ct.getMaCTHD());

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
            SELECT *
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
            SELECT *
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
        String maNhanVien = rs.getString("maNhanVien");
        String maBacSi = rs.getString("maBacSi");

        HoaDon_entity hd = hdDAO.getHDTheoMa(maHD);

        DichVu_entity dv = maDV != null
            ? dvDAO.getDichVuTheoMa(maDV)
            : null;

        VatTu_entity vt = maVT != null
            ? vtDAO.getVatTuTheoMa(maVT)
            : null;

        NhanVien_entity nv = maNhanVien != null ? new NhanVien_DAO().getNhanVienTheoMa(maNhanVien) : null;
        BacSi_entity bs = maBacSi != null ? new BacSi_DAO().getAll().stream().filter(x -> x.getMaBacSi().equals(maBacSi)).findFirst().orElse(null) : null;

        ChiTietHoaDon_entity chiTiet = new ChiTietHoaDon_entity(
            rs.getString("maCTHD"),
            hd,
            dv,
            vt,
            nv,
            bs,
            rs.getInt("soLuong"),
            rs.getDouble("donGia")
        );
        chiTiet.setMaCTDT(layMaCTDT(rs));
        return chiTiet;
    }

    private String layMaCTDT(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if ("maCTDT".equalsIgnoreCase(metadata.getColumnLabel(i))) {
                return rs.getString(i);
            }
        }
        return null;
    }

    public BigDecimal getSoLuongThuocDaCap(String maCTDT) throws SQLException {
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietHoaDon WHERE maCTDT = ?";
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTDT);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
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

        if (ct.getMaCTDT() != null && !coVatTu) {
            return false;
        }
        return coDichVu ^ coVatTu;
    }
}
