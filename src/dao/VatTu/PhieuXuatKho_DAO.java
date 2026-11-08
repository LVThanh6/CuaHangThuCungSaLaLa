package dao.VatTu;

import connectDB.ConnectDB;
import entity.VatTu.ChiTietPhieuXuatKho_entity;
import entity.VatTu.PhieuXuatKho_entity;
import entity.VatTu.PhieuXuatKho_entity.ChiTiet;
import entity.VatTu.PhieuXuatKho_entity.TrangThai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO "Phiếu xuất kho" - khớp với CSDL SALALA (file SALALA_Database.sql).
 *
 * Bảng được GHI:
 *   PhieuXuatKho(maPXK, ngayXuat DATETIME2, ghiChu, trangThai, maNhanVien)
 *   ChiTietPhieuXuatKho(maCTPXK, maPXK, maVT, soLuong)       (qua ChiTietPhieuXuatKho_DAO)
 *   VatTu(soLuongTon, trangThai)  -- DUY NHẤT ở hàm xacNhan(): trừ tồn kho khi phiếu chuyển sang "Đã xuất"
 * Bảng chỉ ĐỌC (JOIN / danh mục): NhanVien, VatTu, Thuoc.
 *
 * CSDL không có cột lý do và đơn giá:
 *   - lý do xuất được ghép vào cột ghiChu (PhieuXuatKho_entity.ghepGhiChu / tachGhiChu);
 *   - đơn giá = VatTu.giaNhap hiện tại.
 * trangThai lưu: "Nháp" / "Hoàn thành" (= Đã xuất) / "Đã hủy"  (xem TrangThai.getDbText()).
 */
public class PhieuXuatKho_DAO {

    /** Ngưỡng "Sắp hết" - giống trigger trg_ChiTietHoaDon_DongBoKhoVaDonThuoc của CSDL. */
    private static final int NGUONG_SAP_HET = 30;

    private final ChiTietPhieuXuatKho_DAO chiTietDAO = new ChiTietPhieuXuatKho_DAO();
    private String lastError = "";

    /** Lý do thất bại của thao tác ghi gần nhất (để GUI hiển thị). */
    public String getLastError() { return lastError; }

    private Connection conn() throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Không kết nối được CSDL SALALA.");
        return c;
    }

    // ===================================================================
    // Truy vấn
    // ===================================================================
    /** Tất cả phiếu (kèm chi tiết), mới nhất trước. Dùng 2 câu lệnh (không N+1). */
    public List<PhieuXuatKho_entity> getAll() throws SQLException {
        Map<String, PhieuXuatKho_entity> map = new LinkedHashMap<>();
        Connection c = conn();

        String sql = "SELECT p.maPXK, p.ngayXuat, p.ghiChu, p.trangThai, p.maNhanVien, nv.hoTen "
                + "FROM PhieuXuatKho p LEFT JOIN NhanVien nv ON nv.maNhanVien = p.maNhanVien "
                + "ORDER BY p.ngayXuat DESC, p.maPXK DESC";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuXuatKho_entity p = mapPhieu(rs);
                map.put(p.getMaPhieu(), p);
            }
        }

        String ct = "SELECT c.maPXK, c.maVT, v.tenVT, COALESCE(t.donViTinh, N'"
                + ChiTietPhieuXuatKho_DAO.DON_VI_MAC_DINH + "') AS donVi, c.soLuong, v.giaNhap "
                + "FROM ChiTietPhieuXuatKho c JOIN VatTu v ON v.maVT = c.maVT "
                + "LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc ORDER BY c.maPXK, c.maCTPXK";
        try (PreparedStatement ps = c.prepareStatement(ct); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuXuatKho_entity p = map.get(rs.getString("maPXK"));
                if (p != null) p.getChiTiet().add(mapChiTiet(rs));
            }
        }
        return new ArrayList<>(map.values());
    }

    /** Một phiếu theo mã (kèm chi tiết), không có -> null. */
    public PhieuXuatKho_entity getByMa(String maPXK) {
        String sql = "SELECT p.maPXK, p.ngayXuat, p.ghiChu, p.trangThai, p.maNhanVien, nv.hoTen "
                + "FROM PhieuXuatKho p LEFT JOIN NhanVien nv ON nv.maNhanVien = p.maNhanVien "
                + "WHERE p.maPXK = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuXuatKho_entity p = mapPhieu(rs);
                    p.setChiTiet(getChiTiet(maPXK));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Các dòng vật tư của phiếu (JOIN VatTu, Thuoc để lấy tên, đơn vị, giá nhập). */
    public List<ChiTiet> getChiTiet(String maPXK) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        for (ChiTietPhieuXuatKho_entity e : chiTietDAO.getByMaPhieu(maPXK)) ds.add(e.toChiTiet());
        return ds;
    }

    /** Tồn kho hiện tại của một vật tư, làm tròn xuống số nguyên (không có -> 0). */
    public int getTonKho(String maVT) {
        String sql = "SELECT soLuongTon FROM VatTu WHERE maVT = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maVT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return floorInt(rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Dòng danh mục vật tư dùng cho hộp thoại lập phiếu (chỉ đọc bảng VatTu / Thuoc). */
    public static final class VatTuRow {
        public final String maVT, tenVT, donVi;
        public final long giaNhap;
        public final int tonKho;
        VatTuRow(String maVT, String tenVT, String donVi, long giaNhap, int tonKho) {
            this.maVT = maVT; this.tenVT = tenVT; this.donVi = donVi;
            this.giaNhap = giaNhap; this.tonKho = tonKho;
        }
    }

    /** Danh mục vật tư (kèm giá nhập, tồn kho hiện tại) - chỉ đọc. */
    public List<VatTuRow> getDanhMucVatTu() throws SQLException {
        List<VatTuRow> ds = new ArrayList<>();
        String sql = "SELECT v.maVT, v.tenVT, COALESCE(t.donViTinh, N'"
                + ChiTietPhieuXuatKho_DAO.DON_VI_MAC_DINH + "') AS donVi, v.giaNhap, v.soLuongTon "
                + "FROM VatTu v LEFT JOIN Thuoc t ON t.maThuoc = v.maThuoc ORDER BY v.maVT";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new VatTuRow(rs.getString("maVT"), rs.getString("tenVT"), rs.getString("donVi"),
                        ChiTietPhieuXuatKho_DAO.toLong(rs.getBigDecimal("giaNhap")),
                        floorInt(rs.getBigDecimal("soLuongTon"))));
            }
        }
        return ds;
    }

    /** Danh sách nhân viên {maNhanVien, hoTen} để chọn người lập phiếu - chỉ đọc. */
    public List<String[]> getDanhSachNhanVien() throws SQLException {
        List<String[]> ds = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien ORDER BY maNhanVien";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return ds;
    }

    /** Sinh mã phiếu kế tiếp, dạng PXK001 (đúng định dạng dữ liệu mẫu PXK001, PXK002...). */
    public String taoMaPhieuMoi() {
        try {
            return taoMaPhieuMoi(conn());
        } catch (SQLException e) {
            e.printStackTrace();
            return "PXK001";
        }
    }

    private String taoMaPhieuMoi(Connection c) throws SQLException {
        String sql = "SELECT MAX(TRY_CAST(SUBSTRING(maPXK, 4, 17) AS INT)) "
                + "FROM PhieuXuatKho WHERE maPXK LIKE 'PXK%'";
        int max = 0;
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) max = rs.getInt(1);   // NULL -> 0
        }
        return String.format("PXK%03d", max + 1);
    }

    // ===================================================================
    // Thêm / sửa
    // ===================================================================
    /**
     * Lập phiếu mới (trạng thái Nháp; header + chi tiết trong 1 transaction).
     * Nếu mã phiếu đã bị người khác dùng thì tự sinh mã mới và gán lại vào p.
     */
    public boolean insert(PhieuXuatKho_entity p) {
        lastError = "";
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            String ma = p.getMaPhieu();
            if (ma == null || ma.trim().isEmpty() || tonTaiMa(c, ma)) {
                ma = taoMaPhieuMoi(c);
                p.setMaPhieu(ma);
            }

            String sql = "INSERT INTO PhieuXuatKho(maPXK, ngayXuat, ghiChu, trangThai, maNhanVien) "
                    + "VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setTimestamp(2, toTimestamp(p.getNgayXuat()));
                ps.setString(3, PhieuXuatKho_entity.ghepGhiChu(p.getLyDo(), p.getGhiChu()));
                ps.setString(4, TrangThai.NHAP.getDbText());
                ps.setString(5, p.getNhanVien() != null ? p.getNhanVien().getMaNhanVien() : null);
                ps.executeUpdate();
            }
            insertChiTiet(c, ma, p.getChiTiet());
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    /** Sửa phiếu (chỉ khi Nháp): cập nhật ngày, lý do/ghi chú và thay toàn bộ chi tiết. */
    public boolean update(PhieuXuatKho_entity p) {
        lastError = "";
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);
            // Giữ nguyên giờ nếu người dùng không đổi ngày
            String sql = "UPDATE PhieuXuatKho SET "
                    + "ngayXuat = CASE WHEN CAST(ngayXuat AS DATE) = ? THEN ngayXuat ELSE ? END, "
                    + "ghiChu = ? WHERE maPXK = ? AND trangThai = ?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDate(1, java.sql.Date.valueOf(p.getNgayXuat()));
                ps.setTimestamp(2, toTimestamp(p.getNgayXuat()));
                ps.setString(3, PhieuXuatKho_entity.ghepGhiChu(p.getLyDo(), p.getGhiChu()));
                ps.setString(4, p.getMaPhieu());
                ps.setString(5, TrangThai.NHAP.getDbText());
                n = ps.executeUpdate();
            }
            if (n == 0) {
                c.rollback();
                lastError = "Phiếu không còn ở trạng thái Nháp hoặc không tồn tại.";
                return false;
            }
            chiTietDAO.deleteByMaPhieu(c, p.getMaPhieu());
            insertChiTiet(c, p.getMaPhieu(), p.getChiTiet());
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    // ===================================================================
    // Xác nhận (trừ tồn) / hủy
    // ===================================================================
    /**
     * Xác nhận xuất kho: chuyển phiếu sang Đã xuất và trừ tồn từng vật tư (1 transaction).
     * Nếu bất kỳ vật tư nào không đủ tồn thì rollback toàn bộ và trả về false.
     */
    public boolean xacNhan(String maPXK) {
        lastError = "";
        Connection c = null;
        try {
            c = conn();
            c.setAutoCommit(false);

            String sql = "UPDATE PhieuXuatKho SET trangThai = ? WHERE maPXK = ? AND trangThai = ?";
            int n;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, TrangThai.DA_XUAT.getDbText());
                ps.setString(2, maPXK);
                ps.setString(3, TrangThai.NHAP.getDbText());
                n = ps.executeUpdate();
            }
            if (n == 0) {
                c.rollback();
                lastError = "Phiếu không còn ở trạng thái Nháp hoặc không tồn tại.";
                return false;
            }

            // Điều kiện soLuongTon >= soLuong đảm bảo không xuất quá tồn (kể cả khi có người khác thao tác song song).
            // Các biểu thức trong SET đều dùng giá trị tồn TRƯỚC khi cập nhật.
            String upd = "UPDATE VatTu SET soLuongTon = soLuongTon - ?, "
                    + "trangThai = CASE WHEN soLuongTon - ? <= 0 THEN N'Hết tồn' "
                    + "WHEN soLuongTon - ? <= " + NGUONG_SAP_HET + " THEN N'Sắp hết' ELSE N'Còn hàng' END "
                    + "WHERE maVT = ? AND soLuongTon >= ?";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                for (ChiTietPhieuXuatKho_entity d : docChiTietTrongTx(c, maPXK)) {
                    String maVT = d.getMaVT() != null ? d.getMaVT().getMaVT() : null;
                    ps.setInt(1, d.getSoLuong());
                    ps.setInt(2, d.getSoLuong());
                    ps.setInt(3, d.getSoLuong());
                    ps.setString(4, maVT);
                    ps.setInt(5, d.getSoLuong());
                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        lastError = "Vật tư " + maVT + " không đủ tồn kho để xuất " + d.getSoLuong() + ".";
                        return false;
                    }
                }
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(c);
        }
    }

    /** Hủy phiếu (chỉ khi Nháp); tồn kho không đổi. */
    public boolean huy(String maPXK) {
        lastError = "";
        String sql = "UPDATE PhieuXuatKho SET trangThai = ? WHERE maPXK = ? AND trangThai = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, TrangThai.DA_HUY.getDbText());
            ps.setString(2, maPXK);
            ps.setString(3, TrangThai.NHAP.getDbText());
            if (ps.executeUpdate() > 0) return true;
            lastError = "Phiếu không còn ở trạng thái Nháp hoặc không tồn tại.";
            return false;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    // ===================================================================
    // Hàm hỗ trợ
    // ===================================================================
    private boolean tonTaiMa(Connection c, String maPXK) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM PhieuXuatKho WHERE maPXK = ?")) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Ghi chi tiết bằng ChiTietPhieuXuatKho_DAO (tự sinh maCTPXK). */
    private void insertChiTiet(Connection c, String maPXK, List<ChiTiet> lines) throws SQLException {
        List<ChiTietPhieuXuatKho_entity> ds = new ArrayList<>();
        for (ChiTiet d : lines) ds.add(ChiTietPhieuXuatKho_entity.fromChiTiet(maPXK, d));
        chiTietDAO.insertAll(c, maPXK, ds);
    }

    /** Đọc chi tiết bằng đúng connection đang trong transaction. */
    private List<ChiTietPhieuXuatKho_entity> docChiTietTrongTx(Connection c, String maPXK) throws SQLException {
        List<ChiTietPhieuXuatKho_entity> ds = new ArrayList<>();
        String sql = "SELECT maVT, soLuong FROM ChiTietPhieuXuatKho WHERE maPXK = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPXK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuXuatKho_entity d = new ChiTietPhieuXuatKho_entity();
                    d.setMaVT(rs.getString("maVT"));
                    d.setSoLuong(ChiTietPhieuXuatKho_DAO.toInt(rs.getBigDecimal("soLuong")));
                    ds.add(d);
                }
            }
        }
        return ds;
    }

    private PhieuXuatKho_entity mapPhieu(ResultSet rs) throws SQLException {
        String[] tach = PhieuXuatKho_entity.tachGhiChu(rs.getString("ghiChu"));
        PhieuXuatKho_entity p = new PhieuXuatKho_entity(
                rs.getString("maPXK"),
                rs.getTimestamp("ngayXuat").toLocalDateTime().toLocalDate(),
                rs.getString("maNhanVien"),
                tach[0],
                tach[1],
                TrangThai.fromText(rs.getString("trangThai")));
        String ten = rs.getString("hoTen");
        p.setTenNhanVien(ten != null ? ten : rs.getString("maNhanVien"));
        return p;
    }

    private ChiTiet mapChiTiet(ResultSet rs) throws SQLException {
        return new ChiTiet(rs.getString("maVT"), rs.getString("tenVT"), rs.getString("donVi"),
                ChiTietPhieuXuatKho_DAO.toInt(rs.getBigDecimal("soLuong")),
                ChiTietPhieuXuatKho_DAO.toLong(rs.getBigDecimal("giaNhap")));
    }

    /** Ngày hôm nay -> giờ hiện tại; ngày khác -> 00:00 của ngày đó. */
    private static Timestamp toTimestamp(LocalDate d) {
        LocalDateTime t = d.equals(LocalDate.now()) ? LocalDateTime.now() : d.atStartOfDay();
        return Timestamp.valueOf(t);
    }

    private static int floorInt(BigDecimal v) {
        return v == null ? 0 : v.setScale(0, RoundingMode.FLOOR).intValue();
    }

    private void rollback(Connection c) {
        try { if (c != null) c.rollback(); } catch (SQLException ignored) { }
    }

    private void resetAutoCommit(Connection c) {
        try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) { }
    }
}