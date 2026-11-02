package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import connectDB.ConnectDB;
import entity.HangThanhVien;
import entity.KhachHang;

/**
 * DAO HangThanhVien - Truy xuất và cập nhật trực tiếp bảng QuanLyHangThanhVien trong CSDL SALALA.
 */
public class HangThanhVien_DAO {

    public HangThanhVien_DAO() {
    }

    /**
     * Lấy toàn bộ danh sách thành viên chi tiết từ bảng QuanLyHangThanhVien kèm thông tin khách hàng và số thú cưng
     */
    public ArrayList<HangThanhVien> getAllQuanLyHangThanhVien() {
        ArrayList<HangThanhVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return ds;
        }

        String sql = "SELECT q.maHang, q.maKH, q.tenHang, q.chiTieuToiThieu, q.chiTieuToiDa, " +
                "q.mucGiamGia, q.diemTichLuy1Van, q.quyenLoi, q.tongChiTieu, q.diemTichLuy, " +
                "q.ngayCapNhatHang, q.trangThai, k.hoTen, k.sdt, k.email, ISNULL(tc.soTC, 0) AS soThuCung " +
                "FROM QuanLyHangThanhVien q " +
                "JOIN KhachHang k ON q.maKH = k.maKH " +
                "LEFT JOIN (SELECT maKH, COUNT(*) AS soTC FROM ThuCung GROUP BY maKH) tc ON tc.maKH = k.maKH " +
                "ORDER BY q.maHang ASC";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String maHang = rs.getString("maHang");
                String maKH = rs.getString("maKH");
                String tenHang = rs.getString("tenHang");
                double chiTieuToiThieu = rs.getDouble("chiTieuToiThieu");
                Double chiTieuToiDa = rs.getObject("chiTieuToiDa") != null ? rs.getDouble("chiTieuToiDa") : null;
                double mucGiamGia = rs.getDouble("mucGiamGia");
                double diemTichLuy1Van = rs.getDouble("diemTichLuy1Van");
                String quyenLoi = rs.getString("quyenLoi");
                double tongChiTieu = rs.getDouble("tongChiTieu");
                int diemTichLuy = rs.getInt("diemTichLuy");
                Timestamp ngayCapNhat = rs.getTimestamp("ngayCapNhatHang");
                String trangThai = rs.getString("trangThai");

                HangThanhVien htv = new HangThanhVien(maHang, maKH, tenHang, chiTieuToiThieu, chiTieuToiDa,
                        mucGiamGia, diemTichLuy1Van, quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhat, trangThai);

                htv.setHoTenKhachHang(rs.getString("hoTen"));
                htv.setSdtKhachHang(rs.getString("sdt"));
                htv.setEmailKhachHang(rs.getString("email"));
                htv.setSoThuCung(rs.getInt("soThuCung"));

                ds.add(htv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy 4 thẻ nhóm hạng (Đồng, Bạc, Vàng, Kim Cương) tổng hợp từ bảng QuanLyHangThanhVien
     */
    public List<HangThanhVien> getAllHangThanhVien() {
        List<HangThanhVien> dsHang = new ArrayList<>();

        // 4 hạng chuẩn theo kịch bản SALALA
        String[] tenCacHang = {"Đồng", "Bạc", "Vàng", "Kim cương"};
        double[] defaultMin = {0.0, 5_000_000.0, 15_000_000.0, 30_000_000.0};
        Double[] defaultMax = {5_000_000.0, 15_000_000.0, 30_000_000.0, null};
        double[] defaultGiam = {2.0, 5.0, 10.0, 15.0};
        double[] defaultDiem = {1.0, 1.2, 1.5, 2.0};
        String[] defaultQL = {
                "Giảm 2% hóa đơn dịch vụ\nTích 1 điểm / 10.000 ₫\nNhắc lịch tiêm và tái khám\nƯu đãi sinh nhật thú cưng",
                "Giảm 5% hóa đơn dịch vụ\nTích 1,2 điểm / 10.000 ₫\n01 lần vệ sinh tai miễn phí / quý\nƯu tiên khung giờ khám",
                "Giảm 10% hóa đơn dịch vụ\nTích 1,5 điểm / 10.000 ₫\n01 lần khám tổng quát / năm\nMiễn phí giao hàng nội thành",
                "Giảm 15% hóa đơn dịch vụ\nTích 2 điểm / 10.000 ₫\n02 lần khám tổng quát / năm\nHotline bác sĩ ưu tiên 24/7"
        };

        Connection con = ConnectDB.getCon();
        for (int i = 0; i < tenCacHang.length; i++) {
            String ten = tenCacHang[i];
            int count = 0;
            double tongChiTieu = 0.0;
            double minSpend = defaultMin[i];
            Double maxSpend = defaultMax[i];
            double giamGia = defaultGiam[i];
            double diem = defaultDiem[i];
            String quyenLoi = defaultQL[i];

            if (con != null) {
                String sql = "SELECT COUNT(*) AS soLuong, " +
                        "ISNULL(SUM(tongChiTieu), 0) AS tongChiTieu, " +
                        "MIN(chiTieuToiThieu) AS minChiTieu, " +
                        "MAX(chiTieuToiDa) AS maxChiTieu, " +
                        "MAX(mucGiamGia) AS mucGiam, " +
                        "MAX(diemTichLuy1Van) AS diem1Van, " +
                        "MAX(quyenLoi) AS ql " +
                        "FROM QuanLyHangThanhVien WHERE tenHang = ?";
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, ten);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            count = rs.getInt("soLuong");
                            tongChiTieu = rs.getDouble("tongChiTieu");
                            if (count > 0) {
                                if (rs.getObject("minChiTieu") != null) minSpend = rs.getDouble("minChiTieu");
                                if (rs.getObject("maxChiTieu") != null) maxSpend = rs.getDouble("maxChiTieu");
                                if (rs.getObject("mucGiam") != null) giamGia = rs.getDouble("mucGiam");
                                if (rs.getObject("diem1Van") != null) diem = rs.getDouble("diem1Van");
                                if (rs.getString("ql") != null && !rs.getString("ql").trim().isEmpty()) {
                                    quyenLoi = rs.getString("ql");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            HangThanhVien hang = new HangThanhVien(ten, minSpend, maxSpend, giamGia, diem, quyenLoi, count, tongChiTieu);
            dsHang.add(hang);
        }

        return dsHang;
    }

    /**
     * Lấy danh sách thành viên thuộc hạng cụ thể (Đồng, Bạc, Vàng, Kim Cương)
     */
    public List<HangThanhVien> getDanhSachThanhVienTheoHang(String tenHang) {
        List<HangThanhVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT q.maHang, q.maKH, q.tenHang, q.chiTieuToiThieu, q.chiTieuToiDa, " +
                "q.mucGiamGia, q.diemTichLuy1Van, q.quyenLoi, q.tongChiTieu, q.diemTichLuy, " +
                "q.ngayCapNhatHang, q.trangThai, k.hoTen, k.sdt, k.email, ISNULL(tc.soTC, 0) AS soThuCung " +
                "FROM QuanLyHangThanhVien q " +
                "JOIN KhachHang k ON q.maKH = k.maKH " +
                "LEFT JOIN (SELECT maKH, COUNT(*) AS soTC FROM ThuCung GROUP BY maKH) tc ON tc.maKH = k.maKH " +
                "WHERE q.tenHang = ? " +
                "ORDER BY q.tongChiTieu DESC, q.maKH ASC";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tenHang);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String maHang = rs.getString("maHang");
                    String maKH = rs.getString("maKH");
                    String ten = rs.getString("tenHang");
                    double chiTieuToiThieu = rs.getDouble("chiTieuToiThieu");
                    Double chiTieuToiDa = rs.getObject("chiTieuToiDa") != null ? rs.getDouble("chiTieuToiDa") : null;
                    double mucGiamGia = rs.getDouble("mucGiamGia");
                    double diemTichLuy1Van = rs.getDouble("diemTichLuy1Van");
                    String quyenLoi = rs.getString("quyenLoi");
                    double tongChiTieu = rs.getDouble("tongChiTieu");
                    int diemTichLuy = rs.getInt("diemTichLuy");
                    Timestamp ngayCapNhat = rs.getTimestamp("ngayCapNhatHang");
                    String trangThai = rs.getString("trangThai");

                    HangThanhVien htv = new HangThanhVien(maHang, maKH, ten, chiTieuToiThieu, chiTieuToiDa,
                            mucGiamGia, diemTichLuy1Van, quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhat, trangThai);

                    htv.setHoTenKhachHang(rs.getString("hoTen"));
                    htv.setSdtKhachHang(rs.getString("sdt"));
                    htv.setEmailKhachHang(rs.getString("email"));
                    htv.setSoThuCung(rs.getInt("soThuCung"));

                    ds.add(htv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy thông tin hạng của một khách hàng theo maKH
     */
    public HangThanhVien getHangThanhVienTheoMaKH(String maKH) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT q.maHang, q.maKH, q.tenHang, q.chiTieuToiThieu, q.chiTieuToiDa, " +
                "q.mucGiamGia, q.diemTichLuy1Van, q.quyenLoi, q.tongChiTieu, q.diemTichLuy, " +
                "q.ngayCapNhatHang, q.trangThai, k.hoTen, k.sdt, k.email, ISNULL(tc.soTC, 0) AS soThuCung " +
                "FROM QuanLyHangThanhVien q " +
                "JOIN KhachHang k ON q.maKH = k.maKH " +
                "LEFT JOIN (SELECT maKH, COUNT(*) AS soTC FROM ThuCung GROUP BY maKH) tc ON tc.maKH = k.maKH " +
                "WHERE q.maKH = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String maHang = rs.getString("maHang");
                    String makh = rs.getString("maKH");
                    String ten = rs.getString("tenHang");
                    double chiTieuToiThieu = rs.getDouble("chiTieuToiThieu");
                    Double chiTieuToiDa = rs.getObject("chiTieuToiDa") != null ? rs.getDouble("chiTieuToiDa") : null;
                    double mucGiamGia = rs.getDouble("mucGiamGia");
                    double diemTichLuy1Van = rs.getDouble("diemTichLuy1Van");
                    String quyenLoi = rs.getString("quyenLoi");
                    double tongChiTieu = rs.getDouble("tongChiTieu");
                    int diemTichLuy = rs.getInt("diemTichLuy");
                    Timestamp ngayCapNhat = rs.getTimestamp("ngayCapNhatHang");
                    String trangThai = rs.getString("trangThai");

                    HangThanhVien htv = new HangThanhVien(maHang, makh, ten, chiTieuToiThieu, chiTieuToiDa,
                            mucGiamGia, diemTichLuy1Van, quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhat, trangThai);

                    htv.setHoTenKhachHang(rs.getString("hoTen"));
                    htv.setSdtKhachHang(rs.getString("sdt"));
                    htv.setEmailKhachHang(rs.getString("email"));
                    htv.setSoThuCung(rs.getInt("soThuCung"));

                    return htv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Thêm bản ghi hạng thành viên mới vào bảng QuanLyHangThanhVien
     */
    public boolean themHangThanhVien(HangThanhVien htv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO QuanLyHangThanhVien " +
                "(maHang, maKH, tenHang, chiTieuToiThieu, chiTieuToiDa, mucGiamGia, " +
                "diemTichLuy1Van, quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhatHang, trangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, htv.getMaHang());
            pstmt.setString(2, htv.getMaKH());
            pstmt.setString(3, htv.getTenHang());
            pstmt.setDouble(4, htv.getChiTieuToiThieu());
            if (htv.getChiTieuToiDa() != null) {
                pstmt.setDouble(5, htv.getChiTieuToiDa());
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
            }
            pstmt.setDouble(6, htv.getMucGiamGia());
            pstmt.setDouble(7, htv.getDiemTichLuy1Van());
            pstmt.setString(8, htv.getQuyenLoi() != null ? htv.getQuyenLoi() : "");
            pstmt.setDouble(9, htv.getTongChiTieu());
            pstmt.setInt(10, htv.getDiemTichLuy());
            pstmt.setTimestamp(11, htv.getNgayCapNhatHang() != null ? htv.getNgayCapNhatHang() : new Timestamp(System.currentTimeMillis()));
            pstmt.setString(12, htv.getTrangThai() != null ? htv.getTrangThai() : "Đang áp dụng");

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin hạng của một khách hàng trong QuanLyHangThanhVien
     */
    public boolean capNhatHangThanhVien(HangThanhVien htv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE QuanLyHangThanhVien SET " +
                "tenHang = ?, chiTieuToiThieu = ?, chiTieuToiDa = ?, mucGiamGia = ?, " +
                "diemTichLuy1Van = ?, quyenLoi = ?, tongChiTieu = ?, diemTichLuy = ?, " +
                "ngayCapNhatHang = ?, trangThai = ? " +
                "WHERE maHang = ? OR maKH = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, htv.getTenHang());
            pstmt.setDouble(2, htv.getChiTieuToiThieu());
            if (htv.getChiTieuToiDa() != null) {
                pstmt.setDouble(3, htv.getChiTieuToiDa());
            } else {
                pstmt.setNull(3, java.sql.Types.DECIMAL);
            }
            pstmt.setDouble(4, htv.getMucGiamGia());
            pstmt.setDouble(5, htv.getDiemTichLuy1Van());
            pstmt.setString(6, htv.getQuyenLoi() != null ? htv.getQuyenLoi() : "");
            pstmt.setDouble(7, htv.getTongChiTieu());
            pstmt.setInt(8, htv.getDiemTichLuy());
            pstmt.setTimestamp(9, htv.getNgayCapNhatHang() != null ? htv.getNgayCapNhatHang() : new Timestamp(System.currentTimeMillis()));
            pstmt.setString(10, htv.getTrangThai() != null ? htv.getTrangThai() : "Đang áp dụng");
            pstmt.setString(11, htv.getMaHang());
            pstmt.setString(12, htv.getMaKH());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật chính sách chung cho tất cả thành viên thuộc một hạng
     */
    public boolean capNhatChinhSachHang(String tenHang, double chiTieuToiThieu, Double chiTieuToiDa,
                                       double mucGiamGia, double diemTichLuy1Van, String quyenLoi) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE QuanLyHangThanhVien SET " +
                "chiTieuToiThieu = ?, chiTieuToiDa = ?, mucGiamGia = ?, diemTichLuy1Van = ?, quyenLoi = ?, " +
                "diemTichLuy = CAST(FLOOR(tongChiTieu / 10000.0 * ?) AS INT), " +
                "ngayCapNhatHang = SYSDATETIME() " +
                "WHERE tenHang = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDouble(1, chiTieuToiThieu);
            if (chiTieuToiDa != null) {
                pstmt.setDouble(2, chiTieuToiDa);
            } else {
                pstmt.setNull(2, java.sql.Types.DECIMAL);
            }
            pstmt.setDouble(3, mucGiamGia);
            pstmt.setDouble(4, diemTichLuy1Van);
            pstmt.setString(5, quyenLoi);
            pstmt.setDouble(6, diemTichLuy1Van);
            pstmt.setString(7, tenHang);

            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa bản ghi trong QuanLyHangThanhVien
     */
    public boolean xoaHangThanhVien(String maHang) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM QuanLyHangThanhVien WHERE maHang = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maHang);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tự động phát sinh mã hạng mới tiếp theo (ví dụ HTV016)
     */
    public String phatSinhMaHangMoi() {
        Connection con = ConnectDB.getCon();
        int maxIndex = 0;
        if (con != null) {
            String sql = "SELECT maHang FROM QuanLyHangThanhVien";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String ma = rs.getString(1);
                    if (ma != null && ma.toUpperCase().startsWith("HTV")) {
                        try {
                            int num = Integer.parseInt(ma.substring(3).trim());
                            if (num > maxIndex) {
                                maxIndex = num;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return String.format("HTV%03d", maxIndex + 1);
    }

    /**
     * Tổng số lượng khách hàng đã được phân hạng trong QuanLyHangThanhVien
     */
    public int getTongSoThanhVien() {
        int tong = 0;
        Connection con = ConnectDB.getCon();
        if (con != null) {
            String sql = "SELECT COUNT(*) FROM QuanLyHangThanhVien";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    tong = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tong;
    }

    /**
     * Tổng chi tiêu của toàn bộ thành viên trong QuanLyHangThanhVien
     */
    public double getTongChiTieuThanhVien() {
        double tong = 0.0;
        Connection con = ConnectDB.getCon();
        if (con != null) {
            String sql = "SELECT ISNULL(SUM(tongChiTieu), 0) FROM QuanLyHangThanhVien";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    tong = rs.getDouble(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tong;
    }

    /**
     * Tổng số tiền giảm giá khuyến mãi đã áp dụng từ các hóa đơn
     */
    public double getTongGiamGiaDaApDung() {
        double tong = 0.0;
        Connection con = ConnectDB.getCon();
        if (con != null) {
            String sql = "SELECT ISNULL(SUM(c.soLuong * c.donGia * (ISNULL(km.mucKM, 0) / 100.0)), 0) " +
                    "FROM HoaDon h " +
                    "JOIN ChiTietHoaDon c ON c.maHD = h.maHD " +
                    "JOIN KhuyenMai km ON km.maKM = h.maKM";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    tong = rs.getDouble(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tong;
    }

    /**
     * Số lượng thành viên có hạng cao hơn Đồng hoặc được cập nhật gần đây
     */
    public int getSoLuongNangHangGanDay() {
        int count = 0;
        Connection con = ConnectDB.getCon();
        if (con != null) {
            String sql = "SELECT COUNT(*) FROM QuanLyHangThanhVien WHERE tenHang <> N'Đồng' OR ngayCapNhatHang >= DATEADD(month, -1, SYSDATETIME())";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * Lấy danh sách mã và tên khách hàng chưa được tạo hạng (nếu có)
     */
    public List<String[]> getKhachHangChuaCoHang() {
        List<String[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con != null) {
            String sql = "SELECT k.maKH, k.hoTen FROM KhachHang k " +
                    "WHERE k.maKH NOT IN (SELECT maKH FROM QuanLyHangThanhVien) " +
                    "ORDER BY k.maKH ASC";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ds.add(new String[]{rs.getString("maKH"), rs.getString("hoTen")});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ds;
    }
}
