package dao.LichHen;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.DichVu.DichVu_entity;
import entity.KhachHang.KhachHang;
import entity.LichHen.LichHen_entity;
import entity.NhanVien.NhanVien_entity;
import entity.ThuCung.ThuCung_entity;

/**
 * DAO Lịch Hẹn (Đã tối ưu hóa truy vấn JOIN tránh N+1 Query gây lag hệ thống).
 */
public class LichHen_DAO {

    private static final String SELECT_BASE =
            "SELECT lh.maLichHen, lh.maTC, lh.maKH, lh.maNhanVien, lh.maDV, lh.ngayGio, lh.lyDo, lh.trangThai, lh.ghiChu, lh.loaiHen, " +
            "tc.tenTC, tc.loai AS loaiTC, tc.gioiTinh AS gioiTinhTC, tc.ngaySinh AS ngaySinhTC, tc.chuHo AS chuHoTC, tc.dacDiemNhanDien, " +
            "kh.hoTen AS hoTenKH, kh.sdt AS sdtKH, kh.email AS emailKH, kh.diaChi AS diaChiKH, " +
            "nv.maTK AS maTKNV, nv.hoTen AS hoTenNV, nv.chucVu AS chucVuNV, nv.sdt AS sdtNV, nv.diaChi AS diaChiNV, nv.ngaySinh AS ngaySinhNV, nv.gioiTinh AS gioiTinhNV, nv.luongCoBan, " +
            "dv.tenDV, dv.moTa AS moTaDV, dv.giaDV, dv.thoiGian AS thoiGianDV, dv.trangThai AS trangThaiDV " +
            "FROM LichHen lh " +
            "LEFT JOIN ThuCung tc ON tc.maTC = lh.maTC " +
            "LEFT JOIN KhachHang kh ON kh.maKH = lh.maKH " +
            "LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien " +
            "LEFT JOIN DichVu dv ON dv.maDV = lh.maDV ";

    /**
     * Lấy toàn bộ danh sách lịch hẹn bằng 1 truy vấn JOIN duy nhất (cực nhanh).
     */
    public ArrayList<LichHen_entity> getAllLichHen() throws SQLException {
        ArrayList<LichHen_entity> dsLichHen = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để tải lịch hẹn.");
        }

        String sql = SELECT_BASE + "ORDER BY lh.ngayGio DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsLichHen.add(mapRowToEntity(rs));
            }
        }

        return dsLichHen;
    }

    /**
     * Tìm một lịch hẹn theo mã
     */
    public LichHen_entity getLichHenTheoMa(String maLichHen) {
        if (maLichHen == null || maLichHen.isBlank()) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = SELECT_BASE + "WHERE lh.maLichHen = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm lịch hẹn mới
     */
    public boolean createLichHen(LichHen_entity lh) {
        if (lh == null || lh.getMaLichHen() == null) return false;
        String sql = "INSERT INTO LichHen (maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lh.getMaLichHen());
            ps.setString(2, lh.getThuCung() != null ? lh.getThuCung().getMaTC() : null);
            ps.setString(3, lh.getKhachHang() != null ? lh.getKhachHang().getMaKH() : null);
            ps.setString(4, lh.getNhanVien() != null ? lh.getNhanVien().getMaNhanVien() : null);

            if (lh.getDichVu() != null) {
                ps.setString(5, lh.getDichVu().getMaDV());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            if (lh.getNgayGio() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(lh.getNgayGio()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.setString(7, lh.getLyDo());
            ps.setString(8, lh.getTrangThai());

            if (lh.getGhiChu() != null) {
                ps.setString(9, lh.getGhiChu());
            } else {
                ps.setNull(9, Types.NVARCHAR);
            }

            ps.setString(10, lh.getLoaiHen());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin lịch hẹn
     */
    public boolean updateLichHen(LichHen_entity lh) {
        if (lh == null || lh.getMaLichHen() == null) return false;
        String sql = "UPDATE LichHen SET maTC = ?, maKH = ?, maNhanVien = ?, maDV = ?, "
                   + "ngayGio = ?, lyDo = ?, trangThai = ?, ghiChu = ?, loaiHen = ? WHERE maLichHen = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lh.getThuCung() != null ? lh.getThuCung().getMaTC() : null);
            ps.setString(2, lh.getKhachHang() != null ? lh.getKhachHang().getMaKH() : null);
            ps.setString(3, lh.getNhanVien() != null ? lh.getNhanVien().getMaNhanVien() : null);

            if (lh.getDichVu() != null) {
                ps.setString(4, lh.getDichVu().getMaDV());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (lh.getNgayGio() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(lh.getNgayGio()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setString(6, lh.getLyDo());
            ps.setString(7, lh.getTrangThai());

            if (lh.getGhiChu() != null) {
                ps.setString(8, lh.getGhiChu());
            } else {
                ps.setNull(8, Types.NVARCHAR);
            }

            ps.setString(9, lh.getLoaiHen());
            ps.setString(10, lh.getMaLichHen());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa lịch hẹn theo mã
     */
    public boolean deleteLichHen(String maLichHen) {
        if (maLichHen == null || maLichHen.isBlank()) return false;
        String sql = "DELETE FROM LichHen WHERE maLichHen = ?";

        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tự động phát sinh mã lịch hẹn tiếp theo (ví dụ: LH016)
     */
    public String phatSinhMaLichHenMoi() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "LH001";

        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(maLichHen, 3, LEN(maLichHen)) AS INT)), 0) FROM LichHen WHERE maLichHen LIKE 'LH%'";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                return String.format("LH%03d", maxId + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "LH001";
    }

    // =====================================================================
    // HÀM HỖ TRỢ (Map nhanh từ JOIN ResultSet mà không query lại CSDL)
    // =====================================================================

    private LichHen_entity mapRowToEntity(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("ngayGio");
        LocalDateTime ngayGio = (ts != null) ? ts.toLocalDateTime() : null;

        String maTC = rs.getString("maTC");
        String maKH = rs.getString("maKH");
        String maNV = rs.getString("maNhanVien");
        String maDV = rs.getString("maDV");

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

        ThuCung_entity tc = null;
        if (maTC != null) {
            Date ngaySinhTC = rs.getDate("ngaySinhTC");
            tc = new ThuCung_entity(
                    maTC,
                    kh,
                    rs.getString("tenTC"),
                    rs.getString("loaiTC"),
                    rs.getString("gioiTinhTC"),
                    ngaySinhTC != null ? ngaySinhTC.toLocalDate() : null,
                    rs.getString("chuHoTC"),
                    rs.getString("dacDiemNhanDien")
            );
        }

        NhanVien_entity nv = null;
        if (maNV != null) {
            Date ngaySinhNV = rs.getDate("ngaySinhNV");
            nv = new NhanVien_entity(
                    maNV,
                    rs.getString("maTKNV") != null ? new entity.TaiKhoan.TaiKhoan_entity(rs.getString("maTKNV")) : null,
                    rs.getString("hoTenNV"),
                    rs.getString("chucVuNV"),
                    rs.getString("sdtNV"),
                    rs.getString("diaChiNV"),
                    ngaySinhNV != null ? ngaySinhNV.toLocalDate() : null,
                    rs.getString("gioiTinhNV"),
                    rs.getDouble("luongCoBan"),
                    null
            );
        }

        DichVu_entity dv = null;
        if (maDV != null) {
            Time tg = rs.getTime("thoiGianDV");
            java.time.LocalTime thoiGian = tg != null ? tg.toLocalTime() : java.time.LocalTime.of(0, 30);
            dv = new DichVu_entity(
                    maDV,
                    rs.getString("tenDV"),
                    rs.getString("moTaDV"),
                    rs.getDouble("giaDV"),
                    thoiGian,
                    rs.getString("trangThaiDV")
            );
        }

        return new LichHen_entity(
                rs.getString("maLichHen"),
                tc,
                kh,
                nv,
                dv,
                ngayGio,
                rs.getString("lyDo"),
                rs.getString("trangThai"),
                rs.getString("ghiChu"),
                rs.getString("loaiHen")
        );
    }
}
