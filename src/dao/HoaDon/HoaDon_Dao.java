package dao.HoaDon;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.HoaDon.HoaDon_entity;
import entity.KhachHang.KhachHang;
import entity.DichVu.KhuyenMai_Entity;
import entity.NhanVien.NhanVien_entity;
import entity.HoaDon.ThanhToan_entity;
import entity.DichVu.Thue_entity;

/**
 * DAO Hóa Đơn (Đã tối ưu hóa bằng JOIN, triệt tiêu hoàn toàn N+1 Query).
 */
public class HoaDon_Dao {

    private static final String SELECT_BASE =
            "SELECT h.maHD, h.ngayLap, h.trangThai, h.maTC, " +
            "k.maKH, k.hoTen AS hoTenKH, k.sdt AS sdtKH, k.email AS emailKH, k.diaChi AS diaChiKH, " +
            "nv.maNhanVien, nv.maTK AS maTKNV, nv.hoTen AS hoTenNV, nv.chucVu, nv.sdt AS sdtNV, nv.diaChi AS diaChiNV, nv.ngaySinh AS ngaySinhNV, nv.gioiTinh AS gioiTinhNV, nv.luongCoBan, " +
            "km.maKM, km.tenKM, km.mucKM, km.ngayBatDau, km.ngayKetThuc, km.dieuKien, " +
            "th.maThue, th.tenThue, th.mucThue, " +
            "tt.maThanhToan, tt.soTien, tt.thoiGian, tt.phuongThuc, tt.trangThai AS trangThaiTT " +
            "FROM HoaDon h " +
            "LEFT JOIN KhachHang k ON k.maKH = h.maKH " +
            "LEFT JOIN NhanVien nv ON nv.maNhanVien = h.maNhanVien " +
            "LEFT JOIN KhuyenMai km ON km.maKM = h.maKM " +
            "LEFT JOIN Thue th ON th.maThue = h.maThue " +
            "LEFT JOIN ThanhToan tt ON tt.maThanhToan = h.maThanhToan ";

    /**
     * Lấy toàn bộ danh sách hóa đơn bằng 1 query duy nhất.
     */
    public ArrayList<HoaDon_entity> getDSHD() {
        ArrayList<HoaDon_entity> dsHD = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsHD;
        }

        String sql = SELECT_BASE + "ORDER BY h.ngayLap DESC, h.maHD DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsHD.add(mapRowToHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    /**
     * Tìm hóa đơn theo mã chính xác bằng 1 query JOIN
     */
    public HoaDon_entity getHDTheoMa(String maHD) {
        if (maHD == null || maHD.isBlank()) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE h.maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToHoaDon(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm hóa đơn mới
     */
    public boolean themHD(HoaDon_entity hd) {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }
        try {
            return themHD(con, hd);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean themHD(Connection con, HoaDon_entity hd) throws SQLException {
        if (con == null || hd == null) {
            return false;
        }

        boolean coCotMaTC = kiemTraCotTonTai(con, "HoaDon", "maTC");
        String sql;
        if (coCotMaTC) {
            sql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, maKH, maNhanVien, maKM, maThue, maThanhToan, maTC) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, maKH, maNhanVien, maKM, maThue, maThanhToan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2,
                    hd.getNgayLap() != null
                            ? Timestamp.valueOf(hd.getNgayLap().atStartOfDay())
                            : new Timestamp(System.currentTimeMillis()));
            ps.setString(3, hd.getTrangThai() != null ? hd.getTrangThai() : "Chờ thanh toán");
            ps.setString(4, hd.getMaKH() != null ? hd.getMaKH().getMaKH() : null);
            ps.setString(5, hd.getMaNhanVien() != null ? hd.getMaNhanVien().getMaNhanVien() : null);
            ps.setString(6, hd.getMaKM() != null ? hd.getMaKM().getMaKM() : null);
            ps.setString(7, hd.getMaThue() != null ? hd.getMaThue().getMaThue() : null);
            ps.setString(8, hd.getMaThanhToan() != null ? hd.getMaThanhToan().getMaThanhToan() : null);

            if (coCotMaTC) {
                if (hd.getMaTC() != null && hd.getMaTC().getMaTC() != null
                        && !hd.getMaTC().getMaTC().isBlank()) {
                    ps.setString(9, hd.getMaTC().getMaTC());
                } else {
                    ps.setNull(9, java.sql.Types.VARCHAR);
                }
            }

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa hóa đơn và các dòng chi tiết liên quan
     */
    public boolean xoaHD(String maHD) {
        if (maHD == null || maHD.isBlank()) {
            return false;
        }

        String sqlXoaChiTiet = "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
        String sqlXoaHoaDon = "DELETE FROM HoaDon WHERE maHD = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try {
            boolean autoCommitCu = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(sqlXoaChiTiet)) {
                    ps.setString(1, maHD);
                    ps.executeUpdate();
                }

                int soDongXoa;
                try (PreparedStatement ps = con.prepareStatement(sqlXoaHoaDon)) {
                    ps.setString(1, maHD);
                    soDongXoa = ps.executeUpdate();
                }

                if (soDongXoa == 0) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommitCu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xác nhận đã thanh toán hóa đơn
     */
    public boolean xacNhanThanhToan(String maHD, String phuongThuc, double soTien) {
        if (maHD == null || maHD.isBlank()) {
            return false;
        }

        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try {
            boolean autoCommitCu = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                String sqlCheck = "SELECT maThanhToan FROM HoaDon WHERE maHD = ?";
                String maTT = null;
                try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                    ps.setString(1, maHD);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maTT = rs.getString("maThanhToan");
                        }
                    }
                }

                if (maTT == null || maTT.isBlank()) {
                    maTT = "TT" + System.currentTimeMillis();
                    if (maTT.length() > 20) {
                        maTT = maTT.substring(0, 20);
                    }

                    String sqlInsertTT = "INSERT INTO ThanhToan (maThanhToan, soTien, thoiGian, phuongThuc, trangThai) "
                            + "VALUES (?, ?, CURRENT_TIMESTAMP, ?, N'Đã thanh toán')";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertTT)) {
                        ps.setString(1, maTT);
                        ps.setDouble(2, Math.max(0, soTien));
                        ps.setString(3, phuongThuc != null ? phuongThuc : "Tiền mặt");
                        ps.executeUpdate();
                    }

                    String sqlUpdateHD = "UPDATE HoaDon SET trangThai = N'Đã thanh toán', maThanhToan = ? WHERE maHD = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateHD)) {
                        ps.setString(1, maTT);
                        ps.setString(2, maHD);
                        ps.executeUpdate();
                    }
                } else {
                    String sqlUpdateTT = "UPDATE ThanhToan SET trangThai = N'Đã thanh toán', soTien = ?, "
                            + "phuongThuc = ?, thoiGian = CURRENT_TIMESTAMP WHERE maThanhToan = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateTT)) {
                        ps.setDouble(1, Math.max(0, soTien));
                        ps.setString(2, phuongThuc != null ? phuongThuc : "Tiền mặt");
                        ps.setString(3, maTT);
                        ps.executeUpdate();
                    }

                    String sqlUpdateHD = "UPDATE HoaDon SET trangThai = N'Đã thanh toán' WHERE maHD = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateHD)) {
                        ps.setString(1, maHD);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommitCu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy số thứ tự hóa đơn lớn nhất hiện tại
     */
    public int laySoHoaDonLonNhat() {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maHD, 3, LEN(maHD)) AS INT)), 0) FROM HoaDon WHERE maHD LIKE 'HD%'";
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private HoaDon_entity mapRowToHoaDon(ResultSet rs) throws SQLException {
        String maHD = rs.getString("maHD");
        Timestamp ts = rs.getTimestamp("ngayLap");
        java.time.LocalDate ngayLap = ts != null ? ts.toLocalDateTime().toLocalDate() : null;
        String trangThai = rs.getString("trangThai");

        String maKH = rs.getString("maKH");
        KhachHang kh = null;
        if (maKH != null) {
            kh = new KhachHang(
                    maKH,
                    rs.getString("hoTenKH"),
                    rs.getString("sdtKH"),
                    rs.getString("emailKH"),
                    rs.getString("diaChiKH")
            );
        }

        String maNV = rs.getString("maNhanVien");
        NhanVien_entity nv = null;
        if (maNV != null) {
            Date ngaySinhNV = rs.getDate("ngaySinhNV");
            nv = new NhanVien_entity(
                    maNV,
                    rs.getString("maTKNV") != null ? new entity.TaiKhoan.TaiKhoan_entity(rs.getString("maTKNV")) : null,
                    rs.getString("hoTenNV"),
                    rs.getString("chucVu"),
                    rs.getString("sdtNV"),
                    rs.getString("diaChiNV"),
                    ngaySinhNV != null ? ngaySinhNV.toLocalDate() : null,
                    rs.getString("gioiTinhNV"),
                    rs.getDouble("luongCoBan"),
                    null
            );
        }

        String maKM = rs.getString("maKM");
        KhuyenMai_Entity km = null;
        if (maKM != null) {
            Date ngayBD = rs.getDate("ngayBatDau");
            Date ngayKT = rs.getDate("ngayKetThuc");
            km = new KhuyenMai_Entity(
                    maKM,
                    rs.getString("tenKM"),
                    rs.getDouble("mucKM"),
                    ngayBD != null ? ngayBD.toLocalDate() : null,
                    ngayKT != null ? ngayKT.toLocalDate() : null,
                    rs.getString("dieuKien")
            );
        }

        String maThue = rs.getString("maThue");
        Thue_entity thue = null;
        if (maThue != null) {
            thue = new Thue_entity(
                    maThue,
                    rs.getString("tenThue"),
                    rs.getDouble("mucThue")
            );
        }

        String maTT = rs.getString("maThanhToan");
        ThanhToan_entity tt = null;
        if (maTT != null) {
            Date thoiGianTT = rs.getDate("thoiGian");
            tt = new ThanhToan_entity(
                    maTT,
                    rs.getDouble("soTien"),
                    thoiGianTT != null ? thoiGianTT.toLocalDate() : null,
                    rs.getString("phuongThuc"),
                    rs.getString("trangThaiTT")
            );
        }

        HoaDon_entity hd = new HoaDon_entity(maHD, ngayLap, trangThai, kh, nv, km, thue, tt);
        hd.setMaTC(layMaTC(rs));
        return hd;
    }

    private String layMaTC(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if ("maTC".equalsIgnoreCase(metadata.getColumnLabel(i))) {
                return rs.getString(i);
            }
        }
        return null;
    }

    private boolean kiemTraCotTonTai(Connection con, String tenBang, String tenCot) {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenBang);
            ps.setString(2, tenCot);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
