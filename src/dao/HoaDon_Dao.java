package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import connectDB.ConnectDB;
import entity.HoaDon_entity;
import entity.KhachHang;
import entity.NhanVien_entity;
import entity.KhuyenMai_Entity;
import entity.Thue_entity;
import entity.ThanhToan_entity;

public class HoaDon_Dao {

    private final KhachHang_DAO khDAO = new KhachHang_DAO();
    private final NhanVien_DAO nvDAO = new NhanVien_DAO();
    private final KhuyenMai_DAO kmDAO = new KhuyenMai_DAO();
    private final Thue_DAO thueDAO = new Thue_DAO();
    private final ThanhToan_DAO ttDAO = new ThanhToan_DAO();

    // 1. Lấy danh sách hóa đơn và các đối tượng khóa ngoại (Đã tối ưu tốc độ bằng Caching)
    public ArrayList<HoaDon_entity> getDSHD() {
        ArrayList<HoaDon_entity> dsHD = new ArrayList<>();

        String sql = """
            SELECT maHD, ngayLap, trangThai,
                   maKH, maNhanVien, maKM, maThue, maThanhToan
            FROM HoaDon
            ORDER BY ngayLap DESC, maHD DESC
            """;

        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return dsHD;
            }

            // TỐI ƯU HÓA: Sử dụng Map để cache dữ liệu, tránh lỗi N+1 Query gây lag
            Map<String, KhachHang> cacheKH = new HashMap<>();
            Map<String, NhanVien_entity> cacheNV = new HashMap<>();
            Map<String, KhuyenMai_Entity> cacheKM = new HashMap<>();
            Map<String, Thue_entity> cacheThue = new HashMap<>();
            Map<String, ThanhToan_entity> cacheTT = new HashMap<>();

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maHD = rs.getString("maHD");

                    Timestamp timestamp = rs.getTimestamp("ngayLap");
                    java.time.LocalDate ngayLap =
                        timestamp != null
                            ? timestamp.toLocalDateTime().toLocalDate()
                            : null;

                    String trangThai = rs.getString("trangThai");

                    // Lấy Khách Hàng (có dùng cache)
                    String maKH = rs.getString("maKH");
                    KhachHang kh = null;
                    if (maKH != null) {
                        kh = cacheKH.computeIfAbsent(maKH, k -> khDAO.getKhachHangTheoMa(k));
                    }

                    // Lấy Nhân Viên (có dùng cache)
                 // Lấy Nhân Viên (có dùng cache)
                    String maNV = rs.getString("maNhanVien");
                    NhanVien_entity nv = null;
                    if (maNV != null) {
                        nv = cacheNV.computeIfAbsent(maNV, k -> {
                            try {
                                return nvDAO.getNhanVienTheoMa(k);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null; // Sửa 'return nv;' thành 'return null;' ở đây
                            }
                        });
                    }

                    // Lấy Khuyến Mãi (có dùng cache)
                    String maKM = rs.getString("maKM");
                    KhuyenMai_Entity km = null;
                    if (maKM != null) {
                        km = cacheKM.computeIfAbsent(maKM, k -> kmDAO.getKhuyenMaiTheoMa(k));
                    }

                    // Lấy Thuế (có dùng cache)
                    String maThue = rs.getString("maThue");
                    Thue_entity thue = null;
                    if (maThue != null) {
                        thue = cacheThue.computeIfAbsent(maThue, k -> thueDAO.getThueTheoMa(k));
                    }

                    // Lấy Thanh Toán (có dùng cache)
                    String maThanhToan = rs.getString("maThanhToan");
                    ThanhToan_entity tt = null;
                    if (maThanhToan != null) {
                        tt = cacheTT.computeIfAbsent(maThanhToan, k -> ttDAO.getThanhToanTheoMa(k));
                    }

                    HoaDon_entity hd = new HoaDon_entity(
                        maHD, ngayLap, trangThai,
                        kh, nv, km, thue, tt
                    );

                    dsHD.add(hd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    // 2. Thêm hóa đơn mới
    public boolean themHD(HoaDon_entity hd) {
        if (hd == null
                || hd.getMaHD() == null
                || hd.getNgayLap() == null
                || hd.getTrangThai() == null
                || hd.getMaKH() == null
                || hd.getMaNhanVien() == null) {
            return false;
        }

        String sql = """
            INSERT INTO HoaDon
                (maHD, ngayLap, trangThai, maKH,
                 maNhanVien, maKM, maThue, maThanhToan)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hd.getMaHD());
                ps.setTimestamp(2,
                    Timestamp.valueOf(hd.getNgayLap().atStartOfDay()));
                ps.setString(3, hd.getTrangThai());

                ps.setString(4, hd.getMaKH().getMaKH());
                ps.setString(5, hd.getMaNhanVien().getMaNhanVien());

                if (hd.getMaKM() != null) {
                    ps.setString(6, hd.getMaKM().getMaKM());
                } else {
                    ps.setNull(6, java.sql.Types.VARCHAR);
                }

                if (hd.getMaThue() != null) {
                    ps.setString(7, hd.getMaThue().getMaThue());
                } else {
                    ps.setNull(7, java.sql.Types.VARCHAR);
                }

                if (hd.getMaThanhToan() != null) {
                    ps.setString(8,
                        hd.getMaThanhToan().getMaThanhToan());
                } else {
                    ps.setNull(8, java.sql.Types.VARCHAR);
                }

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 3. Xóa hóa đơn và chi tiết hóa đơn
    public boolean xoaHD(String maHD) {
        if (maHD == null || maHD.isBlank()) {
            return false;
        }

        String sqlXoaChiTiet =
            "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
        String sqlXoaHoaDon =
            "DELETE FROM HoaDon WHERE maHD = ?";

        Connection con = null;

        try {
            con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            boolean autoCommitCu = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                // Xóa các dòng chi tiết trước để không vi phạm FK
                try (PreparedStatement ps =
                         con.prepareStatement(sqlXoaChiTiet)) {
                    ps.setString(1, maHD);
                    ps.executeUpdate();
                }

                int soDongXoa;

                try (PreparedStatement ps =
                         con.prepareStatement(sqlXoaHoaDon)) {
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

    // 4. Tìm hóa đơn theo mã
    public HoaDon_entity getHDTheoMa(String maHD) {
        String sql = """
            SELECT maHD, ngayLap, trangThai,
                   maKH, maNhanVien, maKM, maThue, maThanhToan
            FROM HoaDon
            WHERE maHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Timestamp timestamp = rs.getTimestamp("ngayLap");

                        java.time.LocalDate ngayLap =
                            timestamp != null
                                ? timestamp.toLocalDateTime().toLocalDate()
                                : null;

                        String maKH = rs.getString("maKH");
                        String maNV = rs.getString("maNhanVien");
                        String maKM = rs.getString("maKM");
                        String maThue = rs.getString("maThue");
                        String maTT = rs.getString("maThanhToan");

                        return new HoaDon_entity(
                            rs.getString("maHD"),
                            ngayLap,
                            rs.getString("trangThai"),
                            khDAO.getKhachHangTheoMa(maKH),
                            nvDAO.getNhanVienTheoMa(maNV),
                            maKM != null
                                ? kmDAO.getKhuyenMaiTheoMa(maKM) : null,
                            maThue != null
                                ? thueDAO.getThueTheoMa(maThue) : null,
                            maTT != null
                                ? ttDAO.getThanhToanTheoMa(maTT) : null
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 5. Xác nhận đã thanh toán hóa đơn
    public boolean xacNhanThanhToan(String maHD, String phuongThuc, double soTien) {
        if (maHD == null || maHD.isBlank()) {
            return false;
        }

        Connection con = null;
        try {
            con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }

            boolean autoCommitCu = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                // Kiểm tra maThanhToan hiện tại của hóa đơn
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
                    // Tạo mã thanh toán mới (tối đa 20 ký tự)
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
    //Lấy số hóa đơn lớn nhất
    public int laySoHoaDonLonNhat() {
        String sql = """
            SELECT ISNULL(
                MAX(TRY_CAST(SUBSTRING(maHD, 3, LEN(maHD)) AS INT)),
                0
            )
            FROM HoaDon
            WHERE maHD LIKE 'HD%'
        """;

        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}