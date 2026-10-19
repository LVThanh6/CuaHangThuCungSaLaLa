package dao;

import connectDB.ConnectDB;
import model.TaiKhoan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp DAO quản lý truy vấn và thao tác dữ liệu bảng TaiKhoan trong CSDL SALALA.
 */
public class TaiKhoan_DAO {

    public TaiKhoan_DAO() {
    }

    // =========================================================================
    // HÀM MÃ HÓA MẬT KHẨU SHA-256 (TƯƠNG THÍCH VỚI SQL SERVER HASHBYTES)
    // =========================================================================
    /**
     * Băm mật khẩu theo chuẩn SHA-256 và trả về chuỗi Hex chữ hoa (64 ký tự).
     * Khớp 100% với hàm SQL Server: CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'matkhau'), 2)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi thuật toán SHA-256: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // XÁC THỰC ĐĂNG NHẬP
    // =========================================================================
    public static class KetQuaDangNhap {
        private boolean thanhCong;
        private String thongBao;
        private TaiKhoan taiKhoan;
        private String tenNguoiDung;
        private String maDoiTuong;

        public KetQuaDangNhap(boolean thanhCong, String thongBao, TaiKhoan taiKhoan, String tenNguoiDung, String maDoiTuong) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.taiKhoan = taiKhoan;
            this.tenNguoiDung = tenNguoiDung;
            this.maDoiTuong = maDoiTuong;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public TaiKhoan getTaiKhoan() { return taiKhoan; }
        public String getTenNguoiDung() { return tenNguoiDung; }
        public String getMaDoiTuong() { return maDoiTuong; }
    }

    /**
     * Kiểm tra thông tin đăng nhập từ tên đăng nhập và mật khẩu
     */
    public KetQuaDangNhap dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập tên đăng nhập!", null, null, null);
        }
        if (matKhau == null || matKhau.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập mật khẩu!", null, null, null);
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return new KetQuaDangNhap(false, "Không thể kết nối đến Cơ sở dữ liệu SALALA!", null, null, null);
        }

        String sql = "SELECT maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy FROM TaiKhoan WHERE tenDangNhap = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tenDangNhap.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbMatKhau = rs.getString("matKhau");
                    boolean trangThai = rs.getBoolean("trangThai");
                    String hashedInput = hashPassword(matKhau);

                    // So sánh mật khẩu băm hoặc dạng text thuần phòng trường hợp dữ liệu cũ
                    boolean khopMatKhau = hashedInput.equalsIgnoreCase(dbMatKhau) || matKhau.equals(dbMatKhau);

                    if (!khopMatKhau) {
                        return new KetQuaDangNhap(false, "Mật khẩu không chính xác!", null, null, null);
                    }

                    if (!trangThai) {
                        return new KetQuaDangNhap(false, "Tài khoản này đang bị khóa. Vui lòng liên hệ Quản lý!", null, null, null);
                    }

                    TaiKhoan tk = new TaiKhoan(
                            rs.getString("maTK"),
                            rs.getString("tenDangNhap"),
                            dbMatKhau,
                            rs.getString("vaiTro"),
                            trangThai,
                            rs.getString("maQuanLy")
                    );

                    // Truy vấn họ tên thực tế của chủ sở hữu tài khoản
                    String[] ttNguoiDung = getThongTinChuSoHuu(tk.getMaTK());
                    String hoTen = ttNguoiDung[0];
                    String maDT = ttNguoiDung[1];

                    return new KetQuaDangNhap(true, "Đăng nhập thành công!", tk, hoTen, maDT);
                } else {
                    return new KetQuaDangNhap(false, "Tên đăng nhập không tồn tại trong hệ thống!", null, null, null);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực đăng nhập: " + e.getMessage());
            return new KetQuaDangNhap(false, "Lỗi truy vấn CSDL: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Lấy họ tên và mã đối tượng (QL, NV, BS) của tài khoản
     * @return String[2] = { hoTen, maDoiTuong }
     */
    public String[] getThongTinChuSoHuu(String maTK) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || maTK == null) return new String[]{"Người dùng", ""};

        // 1. Kiểm tra trong QuanLyCuaHang
        String sqlQL = "SELECT maQuanLy, hoTenQuanLy FROM QuanLyCuaHang WHERE maTK = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlQL)) {
            ps.setString(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("hoTenQuanLy"), rs.getString("maQuanLy")};
                }
            }
        } catch (SQLException ignored) {}

        // 2. Kiểm tra trong NhanVien
        String sqlNV = "SELECT maNhanVien, hoTen FROM NhanVien WHERE maTK = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlNV)) {
            ps.setString(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("hoTen"), rs.getString("maNhanVien")};
                }
            }
        } catch (SQLException ignored) {}

        // 3. Kiểm tra trong BacSyThuY
        String sqlBS = "SELECT maBacSi, hoTen FROM BacSyThuY WHERE maTK = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlBS)) {
            ps.setString(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("hoTen"), rs.getString("maBacSi")};
                }
            }
        } catch (SQLException ignored) {}

        return new String[]{"Người dùng", ""};
    }

    // =========================================================================
    // DTO DÀNH CHO BẢNG QUẢN LÝ TÀI KHOẢN (ĐẦY ĐỦ THÔNG TIN LIÊN KẾT)
    // =========================================================================
    public static class TaiKhoanChiTiet {
        private String maTK;
        private String tenDangNhap;
        private String vaiTro;
        private boolean trangThai;
        private String maQuanLy;
        private String tenQuanLy;
        private String hoTenNguoiDung;
        private String maDoiTuong;
        private String sdtNguoiDung;

        public TaiKhoanChiTiet(String maTK, String tenDangNhap, String vaiTro, boolean trangThai,
                               String maQuanLy, String tenQuanLy, String hoTenNguoiDung,
                               String maDoiTuong, String sdtNguoiDung) {
            this.maTK = maTK;
            this.tenDangNhap = tenDangNhap;
            this.vaiTro = vaiTro;
            this.trangThai = trangThai;
            this.maQuanLy = maQuanLy;
            this.tenQuanLy = tenQuanLy;
            this.hoTenNguoiDung = hoTenNguoiDung;
            this.maDoiTuong = maDoiTuong;
            this.sdtNguoiDung = sdtNguoiDung;
        }

        public String getMaTK() { return maTK; }
        public String getTenDangNhap() { return tenDangNhap; }
        public String getVaiTro() { return vaiTro; }
        public boolean isTrangThai() { return trangThai; }
        public String getMaQuanLy() { return maQuanLy; }
        public String getTenQuanLy() { return tenQuanLy; }
        public String getHoTenNguoiDung() { return hoTenNguoiDung; }
        public String getMaDoiTuong() { return maDoiTuong; }
        public String getSdtNguoiDung() { return sdtNguoiDung; }
    }

    /**
     * Lấy toàn bộ danh sách tài khoản kèm họ tên người sở hữu và tên quản lý
     */
    public List<TaiKhoanChiTiet> getAllTaiKhoanChiTiet() {
        List<TaiKhoanChiTiet> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT tk.maTK, tk.tenDangNhap, tk.vaiTro, tk.trangThai, tk.maQuanLy, " +
                "ql.hoTenQuanLy AS tenQuanLy, " +
                "COALESCE(ql2.hoTenQuanLy, nv.hoTen, bs.hoTen, N'Chưa liên kết') AS hoTenNguoiDung, " +
                "COALESCE(ql2.maQuanLy, nv.maNhanVien, bs.maBacSi, '') AS maDoiTuong, " +
                "COALESCE(ql2.sdt, nv.sdt, bs.sdt, '') AS sdtNguoiDung " +
                "FROM TaiKhoan tk " +
                "LEFT JOIN QuanLyCuaHang ql ON tk.maQuanLy = ql.maQuanLy " +
                "LEFT JOIN QuanLyCuaHang ql2 ON tk.maTK = ql2.maTK " +
                "LEFT JOIN NhanVien nv ON tk.maTK = nv.maTK " +
                "LEFT JOIN BacSyThuY bs ON tk.maTK = bs.maTK " +
                "ORDER BY tk.maTK ASC";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TaiKhoanChiTiet(
                        rs.getString("maTK"),
                        rs.getString("tenDangNhap"),
                        rs.getString("vaiTro"),
                        rs.getBoolean("trangThai"),
                        rs.getString("maQuanLy"),
                        rs.getString("tenQuanLy"),
                        rs.getString("hoTenNguoiDung"),
                        rs.getString("maDoiTuong"),
                        rs.getString("sdtNguoiDung")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách tài khoản: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // CÁC THAO TÁC CRUD TÀI KHOẢN
    // =========================================================================
    /**
     * Thêm tài khoản mới, hỗ trợ băm mật khẩu và liên kết trực tiếp đối tượng
     */
    public boolean themTaiKhoan(TaiKhoan tk, String loaiDoiTuong, String maDoiTuong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String hashed = hashPassword(tk.getMatKhau());
        String sql = "INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            con.setAutoCommit(false);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, tk.getMaTK());
                pstmt.setString(2, tk.getTenDangNhap());
                pstmt.setString(3, hashed);
                pstmt.setString(4, tk.getVaiTro());
                pstmt.setBoolean(5, tk.isTrangThai());
                if (tk.getMaQuanLy() != null && !tk.getMaQuanLy().trim().isEmpty()) {
                    pstmt.setString(6, tk.getMaQuanLy());
                } else {
                    pstmt.setNull(6, Types.VARCHAR);
                }
                pstmt.executeUpdate();
            }

            // Liên kết tài khoản vào đối tượng tương ứng nếu có
            if (maDoiTuong != null && !maDoiTuong.trim().isEmpty()) {
                String sqlLink = null;
                if ("NhanVien".equalsIgnoreCase(loaiDoiTuong)) {
                    sqlLink = "UPDATE NhanVien SET maTK = ? WHERE maNhanVien = ?";
                } else if ("BacSy".equalsIgnoreCase(loaiDoiTuong)) {
                    sqlLink = "UPDATE BacSyThuY SET maTK = ? WHERE maBacSi = ?";
                } else if ("QuanLy".equalsIgnoreCase(loaiDoiTuong)) {
                    sqlLink = "UPDATE QuanLyCuaHang SET maTK = ? WHERE maQuanLy = ?";
                }

                if (sqlLink != null) {
                    try (PreparedStatement psLink = con.prepareStatement(sqlLink)) {
                        psLink.setString(1, tk.getMaTK());
                        psLink.setString(2, maDoiTuong);
                        psLink.executeUpdate();
                    }
                }
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm tài khoản: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            return false;
        }
    }

    /**
     * Cập nhật thông tin vai trò, trạng thái và quản lý của tài khoản
     */
    public boolean capNhatTaiKhoan(TaiKhoan tk) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE TaiKhoan SET vaiTro = ?, trangThai = ?, maQuanLy = ? WHERE maTK = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tk.getVaiTro());
            pstmt.setBoolean(2, tk.isTrangThai());
            if (tk.getMaQuanLy() != null && !tk.getMaQuanLy().trim().isEmpty()) {
                pstmt.setString(3, tk.getMaQuanLy());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            pstmt.setString(4, tk.getMaTK());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật tài khoản: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đổi mật khẩu tài khoản
     */
    public boolean doiMatKhau(String maTK, String matKhauMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String hashed = hashPassword(matKhauMoi);
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTK = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, hashed);
            pstmt.setString(2, maTK);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Khóa hoặc mở khóa tài khoản
     */
    public boolean datTrangThaiTaiKhoan(String maTK, boolean trangThai) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maTK = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setBoolean(1, trangThai);
            pstmt.setString(2, maTK);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái tài khoản: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa tài khoản, tự động gỡ liên kết từ NhanVien/BacSy/QuanLy trước khi xóa
     */
    public boolean xoaTaiKhoan(String maTK) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            con.setAutoCommit(false);
            // Gỡ khóa ngoại ở các bảng liên quan
            try (PreparedStatement ps1 = con.prepareStatement("UPDATE NhanVien SET maTK = NULL WHERE maTK = ?")) {
                ps1.setString(1, maTK);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement("UPDATE BacSyThuY SET maTK = NULL WHERE maTK = ?")) {
                ps2.setString(1, maTK);
                ps2.executeUpdate();
            }

            // Xóa tài khoản
            try (PreparedStatement psDel = con.prepareStatement("DELETE FROM TaiKhoan WHERE maTK = ?")) {
                psDel.setString(1, maTK);
                int r = psDel.executeUpdate();
                con.commit();
                con.setAutoCommit(true);
                return r > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xóa tài khoản: " + e.getMessage());
            try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            return false;
        }
    }

    /**
     * Tự động phát sinh mã tài khoản tiếp theo dạng TK046, TK047...
     */
    public String phatSinhMaTK() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT TOP 1 maTK FROM TaiKhoan ORDER BY maTK DESC";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    String lastMa = rs.getString(1);
                    if (lastMa != null && lastMa.startsWith("TK")) {
                        try {
                            int num = Integer.parseInt(lastMa.substring(2));
                            return String.format("TK%03d", num + 1);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (SQLException ignored) {}
        }
        return "TK046";
    }

    /**
     * Kiểm tra xem tên đăng nhập đã được sử dụng hay chưa
     */
    public boolean isTenDangNhapTonTai(String tenDangNhap, String maTKBoQua) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?" +
                (maTKBoQua != null ? " AND maTK <> ?" : "");
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tenDangNhap.trim());
            if (maTKBoQua != null) {
                pstmt.setString(2, maTKBoQua);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ignored) {}
        return false;
    }

    /**
     * Lấy danh sách Quản lý để chọn trong ComboBox maQuanLy
     */
    public List<Map<String, String>> getDanhSachQuanLy() {
        List<Map<String, String>> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT maQuanLy, hoTenQuanLy FROM QuanLyCuaHang ORDER BY maQuanLy ASC";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("maQuanLy", rs.getString("maQuanLy"));
                m.put("hoTen", rs.getString("hoTenQuanLy"));
                list.add(m);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    /**
     * Lấy danh sách Nhân viên chưa có tài khoản
     */
    public List<Map<String, String>> getNhanVienChuaCoTaiKhoan() {
        List<Map<String, String>> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT maNhanVien, hoTen, chucVu FROM NhanVien WHERE maTK IS NULL ORDER BY maNhanVien ASC";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("ma", rs.getString("maNhanVien"));
                m.put("ten", rs.getString("hoTen"));
                m.put("vaiTro", rs.getString("chucVu"));
                m.put("loai", "NhanVien");
                list.add(m);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    /**
     * Lấy danh sách Bác sĩ chưa có tài khoản
     */
    public List<Map<String, String>> getBacSiChuaCoTaiKhoan() {
        List<Map<String, String>> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT maBacSi, hoTen, tenChuyenNganh FROM BacSyThuY WHERE maTK IS NULL ORDER BY maBacSi ASC";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("ma", rs.getString("maBacSi"));
                m.put("ten", rs.getString("hoTen"));
                m.put("vaiTro", "Bác sĩ (" + rs.getString("tenChuyenNganh") + ")");
                m.put("loai", "BacSy");
                list.add(m);
            }
        } catch (SQLException ignored) {}
        return list;
    }
}
