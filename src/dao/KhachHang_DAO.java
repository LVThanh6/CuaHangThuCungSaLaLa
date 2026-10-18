package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.KhachHang;

public class KhachHang_DAO {
    ArrayList<KhachHang> dskh;
    KhachHang kh;

    public KhachHang_DAO() {
        dskh = new ArrayList<KhachHang>();
        kh = new KhachHang();
    }

    /**
     * Lấy toàn bộ danh sách khách hàng cơ bản từ bảng KhachHang
     */
    public ArrayList<KhachHang> getAlltbKhachHang() {
        dskh.clear();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return dskh;
            }
            String sql = "SELECT * FROM KhachHang";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                String maKH = rs.getString(1);
                String hoTen = rs.getString(2);
                String sdt = rs.getString(3);
                String email = rs.getString(4);
                String diaChi = rs.getString(5);
                KhachHang s = new KhachHang(maKH, hoTen, sdt, email, diaChi);
                dskh.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dskh;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng kèm số thú cưng, tổng chi tiêu và hạng thành viên
     * (Phục vụ nạp dữ liệu trực tiếp lên giao diện DanhSachKhachHangGUI)
     */
    public ArrayList<KhachHang> getAllKhachHangDayDu() {
        ArrayList<KhachHang> dsDayDu = new ArrayList<KhachHang>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return dsDayDu;
            }
            String sql = "SELECT k.maKH, k.hoTen, k.sdt, k.email, k.diaChi, " +
                    "COUNT(DISTINCT tc.maTC) AS soThuCung, " +
                    "ISNULL(SUM(tt.soTien), 0) AS tongChiTieu " +
                    "FROM KhachHang k " +
                    "LEFT JOIN ThuCung tc ON tc.maKH = k.maKH " +
                    "LEFT JOIN HoaDon h ON h.maKH = k.maKH " +
                    "LEFT JOIN ThanhToan tt ON tt.maThanhToan = h.maThanhToan " +
                    "GROUP BY k.maKH, k.hoTen, k.sdt, k.email, k.diaChi " +
                    "ORDER BY k.maKH";

            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                int soThuCung = rs.getInt("soThuCung");
                double tongChiTieu = rs.getDouble("tongChiTieu");
                String hang = tinhHangThanhVien(tongChiTieu);

                KhachHang k = new KhachHang(maKH, hoTen, sdt, email, diaChi, soThuCung, hang, tongChiTieu);
                dsDayDu.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsDayDu;
    }

    /**
     * Thêm mới một khách hàng
     */
    public boolean createKhachHang(KhachHang kh) {
    	PreparedStatement stmt = null;
        int n = 0;
        try {
        	Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            
            stmt = con.prepareStatement("INSERT INTO KhachHang VALUES(?, ?, ?, ?, ?)");
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getHoTen());
            stmt.setString(3, kh.getSdt());
            stmt.setString(4, kh.getEmail());
            stmt.setString(5, kh.getDiaChi());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public boolean updateKhachHang(KhachHang kh) {
        
        PreparedStatement stmt = null;
        int n = 0;
        try {
        	Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            stmt = con.prepareStatement(
                    "UPDATE KhachHang SET hoTen = ?, sdt = ?, email = ?, diaChi = ? WHERE maKH = ?");
            stmt.setString(1, kh.getHoTen());
            stmt.setString(2, kh.getSdt());
            stmt.setString(3, kh.getEmail());
            stmt.setString(4, kh.getDiaChi());
            stmt.setString(5, kh.getMaKH());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    /**
     * Xóa một khách hàng theo mã
     */
    public boolean deleteKhachHang(String maKH) {
    	
        PreparedStatement stmt = null;
        int n = 0;
        try {
        	Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            stmt = con.prepareStatement("DELETE FROM KhachHang WHERE maKH = ?");
            stmt.setString(1, maKH);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    /**
     * Tìm khách hàng theo mã (LIKE)
     */
    public List<KhachHang> timKhachHangTheoMa(String ma) {
        List<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return ds;
            }
            String sql = "SELECT * FROM KhachHang WHERE maKH LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + ma + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                KhachHang nv = new KhachHang(maKH, hoTen, sdt, email, diaChi);
                ds.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Tìm khách hàng theo họ tên (LIKE)
     */
    public List<KhachHang> timKhachHangTheoTen(String ten) {
        List<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return ds;
            }
            String sql = "SELECT * FROM KhachHang WHERE hoTen LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + ten + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                KhachHang nv = new KhachHang(maKH, hoTen, sdt, email, diaChi);
                ds.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Tìm kiếm tổng hợp theo mã, tên hoặc số điện thoại (kèm số thú cưng và chi tiêu)
     */
    public List<KhachHang> timKiemTongHop(String keyword) {
        List<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return ds;
            }
            String sql = "SELECT k.maKH, k.hoTen, k.sdt, k.email, k.diaChi, " +
                    "COUNT(DISTINCT tc.maTC) AS soThuCung, " +
                    "ISNULL(SUM(tt.soTien), 0) AS tongChiTieu " +
                    "FROM KhachHang k " +
                    "LEFT JOIN ThuCung tc ON tc.maKH = k.maKH " +
                    "LEFT JOIN HoaDon h ON h.maKH = k.maKH " +
                    "LEFT JOIN ThanhToan tt ON tt.maThanhToan = h.maThanhToan " +
                    "WHERE k.maKH LIKE ? OR k.hoTen LIKE ? OR k.sdt LIKE ? " +
                    "GROUP BY k.maKH, k.hoTen, k.sdt, k.email, k.diaChi " +
                    "ORDER BY k.maKH";

            PreparedStatement ps = con.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                int soThuCung = rs.getInt("soThuCung");
                double tongChiTieu = rs.getDouble("tongChiTieu");
                String hang = tinhHangThanhVien(tongChiTieu);

                KhachHang k = new KhachHang(maKH, hoTen, sdt, email, diaChi, soThuCung, hang, tongChiTieu);
                ds.add(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy 1 khách hàng theo mã chính xác
     */
    public KhachHang getKhachHangTheoMa(String maKH) {
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return null;
            }
            String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKH);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                return new KhachHang(maKH, hoTen, sdt, email, diaChi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Đếm tổng số lượng khách hàng trong hệ thống
     */
    public int getTongSoKhachHang() {
        int tong = 0;
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return 0;
            }
            String sql = "SELECT COUNT(*) FROM KhachHang";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                tong = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    /**
     * Đếm tổng số thú cưng của khách hàng trong hệ thống
     */
    public int getTongSoThuCung() {
        int tong = 0;
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return 0;
            }
            String sql = "SELECT COUNT(*) FROM ThuCung";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                tong = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    /**
     * Đếm số lượng khách hàng đạt hạng Vàng trở lên (chi tiêu >= 15.000.000)
     */
    public int getSoLuongThanhVienVangTroLen() {
        int count = 0;
        ArrayList<KhachHang> list = getAllKhachHangDayDu();
        for (KhachHang k : list) {
            if ("Vàng".equals(k.getHangThanhVien()) || "Kim cương".equals(k.getHangThanhVien())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Đếm số lượng khách hàng mới (5 khách hàng gần nhất hoặc mới thêm)
     */
    public int getSoLuongKhachHangMoi() {
        int tong = getTongSoKhachHang();
        return Math.min(5, tong);
    }

    /**
     * Quy tắc phân hạng thành viên theo mức tổng chi tiêu:
     * - Từ 30.000.000 ₫ trở lên: Hạng Kim cương
     * - Từ 15.000.000 ₫ đến dưới 30.000.000 ₫: Hạng Vàng
     * - Từ 5.000.000 ₫ đến dưới 15.000.000 ₫: Hạng Bạc
     * - Dưới 5.000.000 ₫: Hạng Đồng
     */
    public static String tinhHangThanhVien(double tongChiTieu) {
        if (tongChiTieu >= 30_000_000) {
            return "Kim cương";
        } else if (tongChiTieu >= 15_000_000) {
            return "Vàng";
        } else if (tongChiTieu >= 5_000_000) {
            return "Bạc";
        } else {
            return "Đồng";
        }
    }

    /**
     * Tự động phát sinh mã khách hàng tiếp theo (ví dụ: KH016, KH017,...)
     */
    public String phatSinhMaKHMoi() {
        String maMoi = "KH001";
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return "KH016";
            }
            String sql = "SELECT MAX(CAST(SUBSTRING(maKH, 3, LEN(maKH)) AS INT)) FROM KhachHang WHERE maKH LIKE 'KH%'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int maxId = rs.getInt(1);
                maMoi = String.format("KH%03d", maxId + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }

    /**
     * Lấy danh sách thú cưng của khách hàng để hiển thị trong Xem chi tiết
     */
    public List<Object[]> getDanhSachThuCungCuaKhachHang(String maKH) {
        List<Object[]> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return ds;
            }
            String sql = "SELECT maTC, tenTC, loai, gioiTinh, ngaySinh, dacDiemNhanDien FROM ThuCung WHERE maKH = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKH);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[] {
                        rs.getString("maTC"),
                        rs.getString("tenTC"),
                        rs.getString("loai"),
                        rs.getString("gioiTinh"),
                        rs.getDate("ngaySinh"),
                        rs.getString("dacDiemNhanDien")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy lịch sử hóa đơn thanh toán của khách hàng
     */
    public List<Object[]> getDanhSachHoaDonCuaKhachHang(String maKH) {
        List<Object[]> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return ds;
            }
            String sql = "SELECT h.maHD, h.ngayLap, h.trangThai, ISNULL(tt.soTien, 0) AS soTien, ISNULL(tt.phuongThuc, N'Tiền mặt') AS phuongThuc "
                    +
                    "FROM HoaDon h " +
                    "LEFT JOIN ThanhToan tt ON tt.maThanhToan = h.maThanhToan " +
                    "WHERE h.maKH = ? ORDER BY h.ngayLap DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKH);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[] {
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayLap"),
                        rs.getString("trangThai"),
                        rs.getDouble("soTien"),
                        rs.getString("phuongThuc")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Kiểm tra xem khách hàng có dữ liệu ràng buộc (Thú cưng, Lịch hẹn, Hóa đơn) hay không
     */
    public boolean kiemTraRangBuoc(String maKH) {
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return false;
            }
            String sql = "SELECT (SELECT COUNT(*) FROM ThuCung WHERE maKH = ?) + " +
                    "(SELECT COUNT(*) FROM HoaDon WHERE maKH = ?) + " +
                    "(SELECT COUNT(*) FROM LichHen WHERE maKH = ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKH);
            ps.setString(2, maKH);
            ps.setString(3, maKH);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
