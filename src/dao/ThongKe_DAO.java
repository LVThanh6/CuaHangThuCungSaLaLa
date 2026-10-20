package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;

public class ThongKe_DAO {

    /**
     * DTO chứa dữ liệu tổng quan cho các thẻ KPI và danh sách chỉ số biểu đồ.
     */
    public static class DuLieuThongKe {
        public int soNhanVien;
        public int soBacSi;
        public int soKhachHang;
        public int soThuCung;
        public double doanhThu;
        public int soHoaDon;
        public List<ChiSoThongKe> chiSo = new ArrayList<>();
    }

    /**
     * DTO cấu trúc dữ liệu cho một biểu đồ (một chỉ số).
     */
    public static class ChiSoThongKe {
        public String ten;
        public String donVi;
        public String hauTo;
        public List<String> nhan = new ArrayList<>();
        public List<String> chuoi = new ArrayList<>();
        public List<List<Double>> giaTri = new ArrayList<>();

        public ChiSoThongKe(String ten, String donVi, String hauTo) {
            this.ten = ten;
            this.donVi = donVi;
            this.hauTo = hauTo;
        }
    }

    /**
     * Hàm chính lấy toàn bộ dữ liệu thống kê từ ngày 'tu' đến ngày 'den'.
     */
    public DuLieuThongKe layDuLieuThongKe(LocalDate tu, LocalDate den) throws Exception {
        DuLieuThongKe kq = new DuLieuThongKe();
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new Exception("Không thể kết nối đến cơ sở dữ liệu.");
        }

        // 1. Lấy dữ liệu KPI tổng (Toàn hệ thống)
        kq.soNhanVien = demTongTuBang("NhanVien");
        kq.soBacSi = demTongTuBang("BacSyThuY");
        kq.soKhachHang = demTongTuBang("KhachHang");
        kq.soThuCung = demTongTuBang("ThuCung");

        // 2. Lấy dữ liệu KPI theo khoảng thời gian đã chọn
        Date sqlTu = Date.valueOf(tu);
        Date sqlDen = Date.valueOf(den);

        kq.soHoaDon = tinhSoHoaDonTrongKhoang(sqlTu, sqlDen);
        kq.doanhThu = tinhDoanhThuTrongKhoang(sqlTu, sqlDen);

        // 3. Tải các cấu hình dữ liệu biểu đồ (ChiSoThongKe)
        kq.chiSo.add(thongKeDoanhThuTheoThoiGian(tu, den));
        kq.chiSo.add(thongKeSoLuongHoaDonTheoThoiGian(tu, den));
        kq.chiSo.add(thongKeTrangThaiHoaDon(sqlTu, sqlDen));

        return kq;
    }

    private int demTongTuBang(String tenBang) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM " + tenBang;
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private int tinhSoHoaDonTrongKhoang(Date tu, Date den) {
        int count = 0;
        // Giả sử bảng HoaDon có cột ngayLap kiểu DATE hoặc DATETIME
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ?";
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql)) {
            ps.setDate(1, tu);
            ps.setDate(2, den);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private double tinhDoanhThuTrongKhoang(Date tu, Date den) {
        double tong = 0.0;
        // Tính tổng tiền từ bảng ThanhToan. HoaDon có FK maThanhToan -> ThanhToan
        String sql = "SELECT ISNULL(SUM(tt.soTien), 0) FROM HoaDon hd " +
                     "JOIN ThanhToan tt ON hd.maThanhToan = tt.maThanhToan " +
                     "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ?";
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql)) {
            ps.setDate(1, tu);
            ps.setDate(2, den);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tong = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    // =========================================================================
    // CÁC HÀM TẠO DỮ LIỆU BIỂU ĐỒ (Chỉ số)
    // =========================================================================

    /**
     * Thống kê Doanh thu (đơn vị: VNĐ) nhóm theo ngày trong khoảng thời gian đã chọn.
     */
    private ChiSoThongKe thongKeDoanhThuTheoThoiGian(LocalDate tu, LocalDate den) {
        ChiSoThongKe cs = new ChiSoThongKe("Doanh thu", "VNĐ", "");
        cs.chuoi.add("Doanh thu thực tế");

        List<Double> giaTriChuoi = new ArrayList<>();
        
        // Sinh danh sách các nhãn (các ngày trong khoảng)
        LocalDate current = tu;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        while (!current.isAfter(den)) {
            cs.nhan.add(current.format(dtf));
            giaTriChuoi.add(0.0); // Mặc định là 0
            current = current.plusDays(1);
        }

        // Truy vấn CSDL để lấy doanh thu theo từng ngày
        String sql = "SELECT CAST(hd.ngayLap AS DATE) AS Ngay, ISNULL(SUM(tt.soTien), 0) AS TongTien " +
                     "FROM HoaDon hd " +
                     "JOIN ThanhToan tt ON hd.maThanhToan = tt.maThanhToan " +
                     "WHERE CAST(hd.ngayLap AS DATE) >= ? AND CAST(hd.ngayLap AS DATE) <= ? " +
                     "GROUP BY CAST(hd.ngayLap AS DATE)";
        
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tu));
            ps.setDate(2, Date.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngaySQL = rs.getDate("Ngay");
                    double tongTien = rs.getDouble("TongTien");
                    
                    if (ngaySQL != null) {
                        LocalDate ngayGiaoDich = ngaySQL.toLocalDate();
                        String nhanNgay = ngayGiaoDich.format(dtf);
                        int index = cs.nhan.indexOf(nhanNgay);
                        if (index != -1) {
                            giaTriChuoi.set(index, tongTien);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        cs.giaTri.add(giaTriChuoi);
        return cs;
    }

    /**
     * Thống kê Số lượng hóa đơn nhóm theo ngày trong khoảng thời gian đã chọn.
     */
    private ChiSoThongKe thongKeSoLuongHoaDonTheoThoiGian(LocalDate tu, LocalDate den) {
        ChiSoThongKe cs = new ChiSoThongKe("Số lượng hóa đơn", "Hóa đơn", "");
        cs.chuoi.add("Số hóa đơn");

        List<Double> giaTriChuoi = new ArrayList<>();
        LocalDate current = tu;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        while (!current.isAfter(den)) {
            cs.nhan.add(current.format(dtf));
            giaTriChuoi.add(0.0);
            current = current.plusDays(1);
        }

        String sql = "SELECT CAST(ngayLap AS DATE) AS Ngay, COUNT(*) AS SoLuong " +
                     "FROM HoaDon " +
                     "WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                     "GROUP BY CAST(ngayLap AS DATE)";
        
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tu));
            ps.setDate(2, Date.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngaySQL = rs.getDate("Ngay");
                    int soLuong = rs.getInt("SoLuong");
                    
                    if (ngaySQL != null) {
                        LocalDate ngay = ngaySQL.toLocalDate();
                        String nhanNgay = ngay.format(dtf);
                        int index = cs.nhan.indexOf(nhanNgay);
                        if (index != -1) {
                            giaTriChuoi.set(index, (double) soLuong);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        cs.giaTri.add(giaTriChuoi);
        return cs;
    }

    /**
     * Thống kê trạng thái hóa đơn (ví dụ: Đã thanh toán, Chưa thanh toán)
     * Thích hợp cho biểu đồ tròn.
     */
    private ChiSoThongKe thongKeTrangThaiHoaDon(Date tu, Date den) {
        ChiSoThongKe cs = new ChiSoThongKe("Trạng thái hóa đơn", "Hóa đơn", "");
        cs.chuoi.add("Số lượng");
        List<Double> giaTriChuoi = new ArrayList<>();

        String sql = "SELECT ISNULL(trangThai, N'Không xác định') AS TrangThai, COUNT(*) AS SoLuong " +
                     "FROM HoaDon " +
                     "WHERE CAST(ngayLap AS DATE) >= ? AND CAST(ngayLap AS DATE) <= ? " +
                     "GROUP BY trangThai";
        
        try (PreparedStatement ps = ConnectDB.getCon().prepareStatement(sql)) {
            ps.setDate(1, tu);
            ps.setDate(2, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String trangThai = rs.getString("TrangThai");
                    double soLuong = rs.getDouble("SoLuong");
                    cs.nhan.add(trangThai);
                    giaTriChuoi.add(soLuong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Nếu không có dữ liệu, trả về mặc định để tránh lỗi vẽ biểu đồ
        if (cs.nhan.isEmpty()) {
            cs.nhan.add("Không có dữ liệu");
            giaTriChuoi.add(0.0);
        }

        cs.giaTri.add(giaTriChuoi);
        return cs;
    }
}