package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.BacSi_entity;
import entity.ChiTietHoaDon_entity;
import entity.DichVu_entity;
import entity.HoaDon_entity;
import entity.NhanVien_entity;
import entity.VatTu_entity;

/**
 * DAO Chi Tiết Hóa Đơn (Đã tối ưu hóa bằng JOIN, triệt tiêu việc gọi BacSi_DAO.getAll() trong vòng lặp).
 */
public class ChiTietHoaDon_DAO {

    private static final String SELECT_BASE =
            "SELECT ct.maCTHD, ct.maHD, ct.maDV, ct.maVT, ct.maCTDT, ct.maNhanVien, ct.maBacSi, ct.soLuong, ct.donGia, " +
            "dv.tenDV, dv.moTa AS moTaDV, dv.giaDV, dv.thoiGian, dv.trangThai AS trangThaiDV, " +
            "vt.tenVT, vt.soLuongTon, vt.trangThai AS trangThaiVT, vt.giaBanCoBan, vt.giaNhap, vt.maThuoc, " +
            "nv.maTK AS maTKNV, nv.hoTen AS hoTenNV, nv.chucVu, nv.sdt AS sdtNV, nv.diaChi AS diaChiNV, nv.ngaySinh AS ngaySinhNV, nv.gioiTinh AS gioiTinhNV, nv.luongCoBan, " +
            "bs.maTK AS maTKBS, bs.hoTen AS hoTenBS, bs.gioiTinh AS gioiTinhBS, bs.sdt AS sdtBS, bs.email AS emailBS, bs.ngaySinh AS ngaySinhBS, bs.luongThang " +
            "FROM ChiTietHoaDon ct " +
            "LEFT JOIN DichVu dv ON dv.maDV = ct.maDV " +
            "LEFT JOIN VatTu vt ON vt.maVT = ct.maVT " +
            "LEFT JOIN NhanVien nv ON nv.maNhanVien = ct.maNhanVien " +
            "LEFT JOIN BacSyThuY bs ON bs.maBacSi = ct.maBacSi ";

    // 1. Lấy toàn bộ danh sách chi tiết hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getAllChiTietHoaDon() {
        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsCTHD;
        }

        String sql = SELECT_BASE + "ORDER BY ct.maHD DESC, ct.maCTHD ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsCTHD.add(mapRowToEntity(rs, null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    // 2. Thêm chi tiết hóa đơn
    public boolean createChiTietHoaDon(ChiTietHoaDon_entity ct) {
        if (!duLieuHopLe(ct)) {
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

            if (ct.getMaDV() != null) {
                ps.setString(3, ct.getMaDV().getMaDV());
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
                ps.setString(4, ct.getMaVT().getMaVT());
            }

            if (ct.getMaCTDT() != null) {
                ps.setString(5, ct.getMaCTDT().getMaCTDT());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }

            if (ct.getNhanVienThucHien() != null) {
                ps.setString(6, ct.getNhanVienThucHien().getMaNhanVien());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            if (ct.getBacSiThucHien() != null) {
                ps.setString(7, ct.getBacSiThucHien().getMaBacSi());
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
        if (maCTHD == null || maCTHD.isBlank()) {
            return false;
        }

        String sql = "DELETE FROM ChiTietHoaDon WHERE maCTHD = ?";
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTHD.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 4. Cập nhật chi tiết hóa đơn
    public boolean updateChiTietHoaDon(ChiTietHoaDon_entity ct) {
        if (!duLieuHopLe(ct)) {
            return false;
        }

        String sql = """
            UPDATE ChiTietHoaDon
            SET maHD = ?, maDV = ?, maVT = ?, maCTDT = ?, maNhanVien = ?, maBacSi = ?,
                soLuong = ?, donGia = ?
            WHERE maCTHD = ?
            """;

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
                ps.setString(4, ct.getMaCTDT().getMaCTDT());
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 5. Tìm chi tiết hóa đơn theo mã
    public ChiTietHoaDon_entity getChiTietHoaDonTheoMa(String maCTHD) {
        if (maCTHD == null || maCTHD.isBlank()) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + "WHERE ct.maCTHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTHD.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs, null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 6. Lấy danh sách chi tiết theo mã hóa đơn (CỰC NHANH - 1 query)
    public ArrayList<ChiTietHoaDon_entity> getChiTietHoaDonTheoMaHD(String maHD) {
        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();
        if (maHD == null || maHD.isBlank()) return dsCTHD;
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return dsCTHD;
        }

        // Tạo sẵn đối tượng HoaDon tối thiểu để gán vào các dòng chi tiết mà không cần query lại
        HoaDon_entity stubHD = new HoaDon_entity(maHD.trim(), null, null, null, null, null, null, null);

        String sql = SELECT_BASE + "WHERE ct.maHD = ? ORDER BY ct.maCTHD ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsCTHD.add(mapRowToEntity(rs, stubHD));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    public BigDecimal getSoLuongThuocDaCap(String maCTDT) throws SQLException {
        if (maCTDT == null || maCTDT.isBlank()) return BigDecimal.ZERO;
        String sql = "SELECT COALESCE(SUM(soLuong), 0) FROM ChiTietHoaDon WHERE maCTDT = ?";
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTDT.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private ChiTietHoaDon_entity mapRowToEntity(ResultSet rs, HoaDon_entity existingHD) throws SQLException {
        String maHD = rs.getString("maHD");
        HoaDon_entity hd = existingHD;
        if (hd == null && maHD != null) {
            hd = new HoaDon_entity(maHD, null, null, null, null, null, null, null);
        }

        String maDV = rs.getString("maDV");
        DichVu_entity dv = null;
        if (maDV != null) {
            Time tg = rs.getTime("thoiGian");
            LocalTime thoiGian = tg != null ? tg.toLocalTime() : LocalTime.of(0, 30);
            dv = new DichVu_entity(
                    maDV,
                    rs.getString("tenDV"),
                    rs.getString("moTaDV"),
                    rs.getDouble("giaDV"),
                    thoiGian,
                    rs.getString("trangThaiDV")
            );
        }

        String maVT = rs.getString("maVT");
        VatTu_entity vt = null;
        if (maVT != null) {
            vt = new VatTu_entity(
                    maVT,
                    null,
                    rs.getString("tenVT"),
                    rs.getInt("soLuongTon"),
                    rs.getString("trangThaiVT"),
                    rs.getDouble("giaBanCoBan"),
                    rs.getDouble("giaNhap"),
                    rs.getString("maThuoc")
            );
        }

        String maNhanVien = rs.getString("maNhanVien");
        NhanVien_entity nv = null;
        if (maNhanVien != null) {
            Date ngaySinhNV = rs.getDate("ngaySinhNV");
            nv = new NhanVien_entity(
                    maNhanVien,
                    rs.getString("maTKNV") != null ? new entity.TaiKhoan(rs.getString("maTKNV")) : null,
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

        String maBacSi = rs.getString("maBacSi");
        BacSi_entity bs = null;
        if (maBacSi != null) {
            Date ngaySinhBS = rs.getDate("ngaySinhBS");
            bs = new BacSi_entity(
                    maBacSi,
                    rs.getString("maTKBS"),
                    rs.getString("hoTenBS"),
                    rs.getString("gioiTinhBS"),
                    rs.getString("sdtBS"),
                    null,
                    rs.getString("emailBS"),
                    ngaySinhBS != null ? ngaySinhBS.toLocalDate() : null,
                    rs.getDouble("luongThang"),
                    0,
                    null
            );
        }

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

    private boolean duLieuHopLe(ChiTietHoaDon_entity ct) {
        if (ct == null
                || ct.getMaCTHD() == null
                || ct.getMaCTHD().isBlank()
                || ct.getMaHD() == null
                || ct.getSoLuong() <= 0
                || ct.getDonGia() < 0) {
            return false;
        }

        boolean coDichVu = ct.getMaDV() != null;
        boolean coVatTu = ct.getMaVT() != null;

        if (ct.getMaCTDT() != null && !coVatTu) {
            return false;
        }
        return coDichVu ^ coVatTu;
    }
}
