package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.DichVu_entity;
import entity.KhachHang;
import entity.LichHen_entity;
import entity.NhanVien_entity;
import entity.ThuCung_entity;

public class LichHen_DAO {

    private final ThuCung_DAO tcDAO = new ThuCung_DAO();
    private final KhachHang_DAO khDAO = new KhachHang_DAO();
    private final NhanVien_DAO nvDAO = new NhanVien_DAO();
    private final DichVu_DAO dvDAO = new DichVu_DAO();

    /**
     * Lấy toàn bộ danh sách lịch hẹn
     */
    public ArrayList<LichHen_entity> getAllLichHen() {
        ArrayList<LichHen_entity> dsLichHen = new ArrayList<>();

        String sql = "SELECT maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen "
                   + "FROM LichHen ORDER BY ngayGio DESC";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return dsLichHen;

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    dsLichHen.add(mapToEntity(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsLichHen;
    }

    /**
     * Tìm một lịch hẹn theo mã
     */
    public LichHen_entity getLichHenTheoMa(String maLichHen) {
        String sql = "SELECT maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen "
                   + "FROM LichHen WHERE maLichHen = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return null;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maLichHen);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapToEntity(rs);
                    }
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
        String sql = "INSERT INTO LichHen (maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return false;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, lh.getMaLichHen());
                ps.setString(2, lh.getThuCung().getMaTC());
                ps.setString(3, lh.getKhachHang().getMaKH());
                ps.setString(4, lh.getNhanVien().getMaNhanVien());

                // maDV có thể null
                if (lh.getDichVu() != null) {
                    ps.setString(5, lh.getDichVu().getMaDV());
                } else {
                    ps.setNull(5, Types.VARCHAR);
                }

                ps.setTimestamp(6, Timestamp.valueOf(lh.getNgayGio()));
                ps.setString(7, lh.getLyDo());
                ps.setString(8, lh.getTrangThai());
                
                // ghiChu có thể null
                if (lh.getGhiChu() != null) {
                    ps.setString(9, lh.getGhiChu());
                } else {
                    ps.setNull(9, Types.NVARCHAR);
                }
                
                ps.setString(10, lh.getLoaiHen());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin lịch hẹn
     */
    public boolean updateLichHen(LichHen_entity lh) {
        String sql = "UPDATE LichHen SET maTC = ?, maKH = ?, maNhanVien = ?, maDV = ?, "
                   + "ngayGio = ?, lyDo = ?, trangThai = ?, ghiChu = ?, loaiHen = ? WHERE maLichHen = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return false;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, lh.getThuCung().getMaTC());
                ps.setString(2, lh.getKhachHang().getMaKH());
                ps.setString(3, lh.getNhanVien().getMaNhanVien());

                if (lh.getDichVu() != null) {
                    ps.setString(4, lh.getDichVu().getMaDV());
                } else {
                    ps.setNull(4, Types.VARCHAR);
                }

                ps.setTimestamp(5, Timestamp.valueOf(lh.getNgayGio()));
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa lịch hẹn theo mã
     */
    public boolean deleteLichHen(String maLichHen) {
        String sql = "DELETE FROM LichHen WHERE maLichHen = ?";

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return false;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maLichHen);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tự động phát sinh mã lịch hẹn tiếp theo (ví dụ: LH016)
     */
    public String phatSinhMaLichHenMoi() {
        String maMoi = "LH001";
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) return "LH016";

            String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(maLichHen, 3, LEN(maLichHen)) AS INT)), 0) FROM LichHen WHERE maLichHen LIKE 'LH%'";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int maxId = rs.getInt(1);
                    maMoi = String.format("LH%03d", maxId + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }

    // =====================================================================
    // HÀM HỖ TRỢ (Helper)
    // =====================================================================

    /**
     * Chuyển đổi 1 dòng ResultSet thành đối tượng LichHen_entity
     */
    private LichHen_entity mapToEntity(ResultSet rs) throws SQLException {
        // Lấy thời gian
        Timestamp ts = rs.getTimestamp("ngayGio");
        LocalDateTime ngayGio = (ts != null) ? ts.toLocalDateTime() : null;

        // Lấy các mã khóa ngoại
        String maTC = rs.getString("maTC");
        String maKH = rs.getString("maKH");
        String maNV = rs.getString("maNhanVien");
        String maDV = rs.getString("maDV");

        // Gọi các DAO khác để load Object liên quan
        ThuCung_entity tc = (maTC != null) ? tcDAO.getThuCungTheoMa(maTC) : null;
        KhachHang kh = (maKH != null) ? khDAO.getKhachHangTheoMa(maKH) : null;
        NhanVien_entity nv = (maNV != null) ? nvDAO.getNhanVienTheoMa(maNV) : null;
        DichVu_entity dv = (maDV != null) ? dvDAO.getDichVuTheoMa(maDV) : null;

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