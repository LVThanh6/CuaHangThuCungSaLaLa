package dao.LichHen;

import connectDB.ConnectDB;
import entity.LichHen.LichLamViec_entity;
import entity.LichHen.NhanSuLichLamViec_entity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LichLamViec_DAO {

    private static volatile boolean daKiemTraCauTrucBang = false;

    private static final String SQL_LICH = "SELECT * FROM ("
            + "SELECT l.maLLV, l.maNhanVien AS maNhanSu, nv.hoTen AS ten, "
            + "nv.chucVu AS vaiTro, l.ngay, l.gioBatDau, l.gioKetThuc, "
            + "l.trangThai, CAST('NHAN_VIEN' AS VARCHAR(20)) AS loaiNhanSu "
            + "FROM LichLamViec l JOIN NhanVien nv ON nv.maNhanVien = l.maNhanVien "
            + "WHERE l.maNhanVien IS NOT NULL "
            + "UNION ALL "
            + "SELECT l.maLLV, l.maBacSi AS maNhanSu, bs.hoTen AS ten, "
            + "N'Bác sĩ' AS vaiTro, l.ngay, l.gioBatDau, l.gioKetThuc, "
            + "l.trangThai, CAST('BAC_SI' AS VARCHAR(20)) AS loaiNhanSu "
            + "FROM LichLamViec l JOIN BacSyThuY bs ON bs.maBacSi = l.maBacSi "
            + "WHERE l.maBacSi IS NOT NULL"
            + ") AS lich ";

    public int capNhatTrangThaiTuDongQuaNgay() throws SQLException {
        String sql = "UPDATE LichLamViec SET trangThai = N'Hoàn thành' "
                + "WHERE ngay < CAST(GETDATE() AS DATE) "
                + "AND (trangThai IS NULL OR trangThai NOT IN (N'Hoàn thành', N'Đã hủy', N'Vắng mặt'))";

        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    public List<LichLamViec_entity> getLichTheoNgay(LocalDate ngay) throws SQLException {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày xem lịch không được để trống.");
        }
        return getLichTrongKhoang(ngay, ngay.plusDays(1));
    }

    public List<LichLamViec_entity> getLichTrongKhoang(LocalDate tuNgay, LocalDate denNgayExclusive)
            throws SQLException {
        kiemTraKhoangNgay(tuNgay, denNgayExclusive);
        capNhatTrangThaiTuDongQuaNgay();

        String sql = SQL_LICH
                + "WHERE ngay >= ? AND ngay < ? "
                + "ORDER BY ngay ASC, gioBatDau ASC, maLLV ASC";
        List<LichLamViec_entity> danhSach = new ArrayList<>();

        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgayExclusive));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngay = rs.getDate("ngay");
                    Time gioBatDau = rs.getTime("gioBatDau");
                    Time gioKetThuc = rs.getTime("gioKetThuc");

                    danhSach.add(new LichLamViec_entity(
                            rs.getString("maLLV"),
                            rs.getString("maNhanSu"),
                            rs.getString("ten"),
                            rs.getString("vaiTro"),
                            ngay != null ? ngay.toLocalDate() : null,
                            gioBatDau != null ? gioBatDau.toLocalTime() : null,
                            gioKetThuc != null ? gioKetThuc.toLocalTime() : null,
                            rs.getString("trangThai"),
                            LichLamViec_entity.LoaiNhanSu.valueOf(rs.getString("loaiNhanSu"))));
                }
            }
        }
        return danhSach;
    }

    public Map<LocalDate, Integer> getSoLichTheoNgay(LocalDate tuNgay, LocalDate denNgayExclusive)
            throws SQLException {
        kiemTraKhoangNgay(tuNgay, denNgayExclusive);
        capNhatTrangThaiTuDongQuaNgay();

        String sql = "SELECT ngay, COUNT(*) AS soLuong FROM LichLamViec "
                + "WHERE ngay >= ? AND ngay < ? GROUP BY ngay ORDER BY ngay";
        Map<LocalDate, Integer> soLichTheoNgay = new LinkedHashMap<>();

        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgayExclusive));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngay = rs.getDate("ngay");
                    if (ngay != null) {
                        soLichTheoNgay.put(ngay.toLocalDate(), rs.getInt("soLuong"));
                    }
                }
            }
        }
        return soLichTheoNgay;
    }

    public List<NhanSuLichLamViec_entity> getDanhSachNhanSu() throws SQLException {
        String sql = "SELECT maNhanVien AS maNhanSu, hoTen AS tenNhanSu, "
                + "chucVu AS vaiTro, CAST('NHAN_VIEN' AS VARCHAR(20)) AS loaiNhanSu "
                + "FROM NhanVien "
                + "UNION ALL "
                + "SELECT maBacSi AS maNhanSu, hoTen AS tenNhanSu, "
                + "N'Bác sĩ' AS vaiTro, CAST('BAC_SI' AS VARCHAR(20)) AS loaiNhanSu "
                + "FROM BacSyThuY ORDER BY tenNhanSu";
        List<NhanSuLichLamViec_entity> danhSach = new ArrayList<>();

        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new NhanSuLichLamViec_entity(
                        rs.getString("maNhanSu"),
                        rs.getString("tenNhanSu"),
                        rs.getString("vaiTro"),
                        LichLamViec_entity.LoaiNhanSu.valueOf(rs.getString("loaiNhanSu"))));
            }
        }
        return danhSach;
    }

    public boolean themLich(LichLamViec_entity lich) throws SQLException {
        kiemTraLich(lich);
        if (!LichLamViec_entity.TRANG_THAI_CHUA_LAM.equals(lich.getTrangThai())) {
            throw new IllegalArgumentException("Lịch mới phải bắt đầu ở trạng thái Chưa làm.");
        }

        try (Connection con = moKetNoi()) {
            batDauGiaoDichLich(con);
            try {
                kiemTraNgayBatDauConHieuLuc(con, lich.getNgay(), lich.getGioBatDau());
                kiemTraKhongTrungCa(con, lich, null);

                String sql = "INSERT INTO LichLamViec "
                        + "(maLLV, maNhanVien, maBacSi, ngay, gioBatDau, gioKetThuc, trangThai) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, lich.getMaLLV());
                    ganMaNhanSu(ps, 2, 3, lich);
                    ps.setDate(4, Date.valueOf(lich.getNgay()));
                    ps.setTime(5, Time.valueOf(lich.getGioBatDau()));
                    ps.setTime(6, Time.valueOf(lich.getGioKetThuc()));
                    ps.setString(7, LichLamViec_entity.TRANG_THAI_CHUA_LAM);
                    boolean daThem = ps.executeUpdate() > 0;
                    con.commit();
                    return daThem;
                }
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            }
        }
    }

    public boolean capNhatLich(LichLamViec_entity lich) throws SQLException {
        kiemTraLich(lich);

        try (Connection con = moKetNoi()) {
            batDauGiaoDichLich(con);
            try {
                kiemTraLichChuaBatDau(con, lich.getMaLLV());
                kiemTraNgayBatDauConHieuLuc(con, lich.getNgay(), lich.getGioBatDau());
                kiemTraKhongTrungCa(con, lich, lich.getMaLLV());

                String sql = "UPDATE LichLamViec SET maNhanVien = ?, maBacSi = ?, ngay = ?, "
                        + "gioBatDau = ?, gioKetThuc = ? "
                        + "WHERE maLLV = ? AND "
                        + "(ngay > CAST(GETDATE() AS DATE) OR "
                        + "(ngay = CAST(GETDATE() AS DATE) "
                        + "AND gioBatDau > CAST(GETDATE() AS TIME)))";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ganMaNhanSu(ps, 1, 2, lich);
                    ps.setDate(3, Date.valueOf(lich.getNgay()));
                    ps.setTime(4, Time.valueOf(lich.getGioBatDau()));
                    ps.setTime(5, Time.valueOf(lich.getGioKetThuc()));
                    ps.setString(6, lich.getMaLLV());
                    boolean daCapNhat = ps.executeUpdate() > 0;
                    if (!daCapNhat) {
                        throw new IllegalStateException("Lịch đã đến giờ bắt đầu hoặc đã bị khóa.");
                    }
                    con.commit();
                    return true;
                }
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            }
        }
    }

    public boolean capNhatTrangThai(String maLLV, String trangThai) throws SQLException {
        if (maLLV == null || maLLV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch làm việc không hợp lệ.");
        }
        if (!LichLamViec_entity.TRANG_THAI_CHO_PHEP.contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái lịch làm việc không hợp lệ.");
        }

        String sql = "UPDATE LichLamViec SET trangThai = ? "
                + "WHERE maLLV = ? AND ngay = CAST(GETDATE() AS DATE)";
        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maLLV.trim());
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Chỉ có thể cập nhật trạng thái trong ngày diễn ra lịch.");
            }
            return true;
        }
    }

    public boolean capNhatTrangThaiKhoa(String maLLV, String trangThai) throws SQLException {
        return capNhatTrangThai(maLLV, trangThai);
    }

    public boolean xoaLich(String maLLV) throws SQLException {
        if (maLLV == null || maLLV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch làm việc không hợp lệ.");
        }

        String sql = "DELETE FROM LichLamViec WHERE maLLV = ? AND "
                + "(ngay > CAST(GETDATE() AS DATE) OR "
                + "(ngay = CAST(GETDATE() AS DATE) "
                + "AND gioBatDau > CAST(GETDATE() AS TIME)))";
        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLLV.trim());
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Lịch đã đến giờ bắt đầu hoặc đã bị khóa.");
            }
            return true;
        }
    }

    public String phatSinhMaLLV() throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maLLV, 4, LEN(maLLV)) AS INT)), 0) "
                + "FROM LichLamViec WHERE maLLV LIKE 'LLV%'";
        try (Connection con = moKetNoi();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.format("LLV%04d", rs.getInt(1) + 1);
            }
        }
        throw new SQLException("Không thể tạo mã lịch làm việc.");
    }

    /**
     * Tự động kiểm tra và đảm bảo bảng LichLamViec có đầy đủ cột maBacSi và ràng
     * buộc cần thiết.
     * Tự động vá (Self-healing) để ứng dụng không bao giờ bị lỗi Invalid column
     * name 'maBacSi'.
     */
    private static synchronized void damBaoCauTrucBang(Connection con) {
        if (daKiemTraCauTrucBang || con == null)
            return;
        try {
            java.sql.DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "LichLamViec", "maBacSi")) {
                if (!rs.next()) {
                    try (java.sql.Statement st = con.createStatement()) {
                        st.executeUpdate("ALTER TABLE LichLamViec ALTER COLUMN maNhanVien VARCHAR(20) NULL");
                        st.executeUpdate("ALTER TABLE LichLamViec ADD maBacSi VARCHAR(20) NULL");
                        try {
                            st.executeUpdate(
                                    "ALTER TABLE LichLamViec ADD CONSTRAINT FK_LichLamViec_BacSi FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi)");
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            daKiemTraCauTrucBang = true;
        } catch (Exception e) {
            System.err.println("Cảnh báo kiểm tra cấu trúc bảng LichLamViec: " + e.getMessage());
        }
    }

    private static Connection moKetNoi() throws SQLException {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }
        damBaoCauTrucBang(con);
        return con;
    }

    private static void batDauGiaoDichLich(Connection con) throws SQLException {
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        con.setAutoCommit(false);
    }

    private static void kiemTraNgayBatDauConHieuLuc(Connection con, LocalDate ngay, java.time.LocalTime gio)
            throws SQLException {
        String sql = "SELECT CASE WHEN ? > CAST(GETDATE() AS DATE) OR "
                + "(? = CAST(GETDATE() AS DATE) AND ? > CAST(GETDATE() AS TIME)) "
                + "THEN 1 ELSE 0 END";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            ps.setDate(2, Date.valueOf(ngay));
            ps.setTime(3, Time.valueOf(gio));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new IllegalStateException("Chỉ có thể xếp lịch khi giờ bắt đầu còn ở tương lai.");
                }
            }
        }
    }

    private static void kiemTraLichChuaBatDau(Connection con, String maLLV) throws SQLException {
        String sql = "SELECT 1 FROM LichLamViec WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maLLV = ? AND (ngay > CAST(GETDATE() AS DATE) OR "
                + "(ngay = CAST(GETDATE() AS DATE) "
                + "AND gioBatDau > CAST(GETDATE() AS TIME)))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLLV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Lịch đã đến giờ bắt đầu hoặc đã bị khóa.");
                }
            }
        }
    }

    private static void kiemTraKhongTrungCa(Connection con, LichLamViec_entity lich, String maLLV)
            throws SQLException {
        String cotNhanSu = lich.getLoaiNhanSu() == LichLamViec_entity.LoaiNhanSu.BAC_SI
                ? "maBacSi"
                : "maNhanVien";
        String sql = "SELECT TOP 1 maLLV FROM LichLamViec WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE " + cotNhanSu + " = ? AND ngay = ? "
                + "AND (trangThai IS NULL OR trangThai <> N'Đã hủy') "
                + "AND gioBatDau < ? AND gioKetThuc > ? "
                + "AND (? IS NULL OR maLLV <> ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lich.getMaNhanSu());
            ps.setDate(2, Date.valueOf(lich.getNgay()));
            ps.setTime(3, Time.valueOf(lich.getGioKetThuc()));
            ps.setTime(4, Time.valueOf(lich.getGioBatDau()));
            if (maLLV == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, maLLV);
                ps.setString(6, maLLV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Nhân sự đã có lịch trùng trong khoảng thời gian này.");
                }
            }
        }
    }

    private static void ganMaNhanSu(
            PreparedStatement ps, int viTriNhanVien, int viTriBacSi, LichLamViec_entity lich)
            throws SQLException {
        if (lich.getLoaiNhanSu() == LichLamViec_entity.LoaiNhanSu.BAC_SI) {
            ps.setNull(viTriNhanVien, java.sql.Types.VARCHAR);
            ps.setString(viTriBacSi, lich.getMaNhanSu());
        } else {
            ps.setString(viTriNhanVien, lich.getMaNhanSu());
            ps.setNull(viTriBacSi, java.sql.Types.VARCHAR);
        }
    }

    private static void kiemTraLich(LichLamViec_entity lich) {
        if (lich == null
                || lich.getMaLLV() == null || lich.getMaLLV().trim().isEmpty()
                || lich.getMaNhanSu() == null || lich.getMaNhanSu().trim().isEmpty()
                || lich.getLoaiNhanSu() == null
                || lich.getNgay() == null
                || lich.getGioBatDau() == null
                || lich.getGioKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ nhân viên, ngày và giờ làm việc.");
        }
        if (!lich.getGioKetThuc().isAfter(lich.getGioBatDau())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
        if (lich.getNgay().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể xếp lịch vào ngày đã qua.");
        }
    }

    private static void kiemTraKhoangNgay(LocalDate tuNgay, LocalDate denNgayExclusive) {
        if (tuNgay == null || denNgayExclusive == null || !denNgayExclusive.isAfter(tuNgay)) {
            throw new IllegalArgumentException("Khoảng ngày xem lịch không hợp lệ.");
        }
    }

    private static void rollback(Connection con, Exception cause) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}
