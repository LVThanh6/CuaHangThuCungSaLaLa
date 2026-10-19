package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import connectDB.ConnectDB;
import entity.HangThanhVien;
import entity.KhachHang;

/**
 * DAO HangThanhVien - phục vụ dữ liệu và nghiệp vụ cho QuanLyHangThanhVienGUI
 */
public class HangThanhVien_DAO {
    private KhachHang_DAO khachHangDAO;

    public HangThanhVien_DAO() {
        this.khachHangDAO = new KhachHang_DAO();
    }

    /**
     * Lấy danh sách 4 hạng thành viên kèm số lượng thành viên thực tế từ CSDL SALALA
     */
    public List<HangThanhVien> getAllHangThanhVien() {
        List<HangThanhVien> dsHang = new ArrayList<>();

        // Khởi tạo 4 hạng theo tiêu chuẩn thiết kế SALALA Pet Shop
        HangThanhVien dong = new HangThanhVien(
                "DONG", "Đồng", 0.0, 2.0, 0, 0.0,
                Arrays.asList(
                        "Giảm 2% hóa đơn dịch vụ",
                        "Tích 1 điểm / 10.000 ₫",
                        "Nhắc lịch tiêm và tái khám",
                        "Ưu đãi sinh nhật thú cưng"));

        HangThanhVien bac = new HangThanhVien(
                "BAC", "Bạc", 5_000_000.0, 5.0, 0, 0.0,
                Arrays.asList(
                        "Giảm 5% hóa đơn dịch vụ",
                        "Tích 1,2 điểm / 10.000 ₫",
                        "01 lần vệ sinh tai miễn phí / quý",
                        "Ưu tiên khung giờ khám"));

        HangThanhVien vang = new HangThanhVien(
                "VANG", "Vàng", 15_000_000.0, 10.0, 0, 0.0,
                Arrays.asList(
                        "Giảm 10% hóa đơn dịch vụ",
                        "Tích 1,5 điểm / 10.000 ₫",
                        "01 gói spa tắm sấy miễn phí / quý",
                        "Miễn phí khám lâm sàng ban đầu",
                        "Tư vấn bác sĩ riêng qua Zalo"));

        HangThanhVien kimCuong = new HangThanhVien(
                "KIMCUONG", "Kim cương", 30_000_000.0, 15.0, 0, 0.0,
                Arrays.asList(
                        "Giảm 15% hóa đơn dịch vụ",
                        "Tích 2 điểm / 10.000 ₫",
                        "Ưu tiên cấp cứu và đặt lịch 24/7",
                        "01 đêm lưu chuồng VIP miễn phí / năm",
                        "Quà sinh nhật độc quyền từ SALALA"));

        // Tính toán số lượng khách hàng và tổng chi tiêu thực tế từ cơ sở dữ liệu
        ArrayList<KhachHang> dsKhachHang = khachHangDAO.getAllKhachHangDayDu();
        if (dsKhachHang != null && !dsKhachHang.isEmpty()) {
            for (KhachHang kh : dsKhachHang) {
                String hang = kh.getHangThanhVien();
                double chiTieu = kh.getTongChiTieu();

                if ("Kim cương".equalsIgnoreCase(hang)) {
                    kimCuong.setSoLuongThanhVien(kimCuong.getSoLuongThanhVien() + 1);
                    kimCuong.setTongChiTieu(kimCuong.getTongChiTieu() + chiTieu);
                } else if ("Vàng".equalsIgnoreCase(hang)) {
                    vang.setSoLuongThanhVien(vang.getSoLuongThanhVien() + 1);
                    vang.setTongChiTieu(vang.getTongChiTieu() + chiTieu);
                } else if ("Bạc".equalsIgnoreCase(hang)) {
                    bac.setSoLuongThanhVien(bac.getSoLuongThanhVien() + 1);
                    bac.setTongChiTieu(bac.getTongChiTieu() + chiTieu);
                } else {
                    dong.setSoLuongThanhVien(dong.getSoLuongThanhVien() + 1);
                    dong.setTongChiTieu(dong.getTongChiTieu() + chiTieu);
                }
            }
        }

        dsHang.add(dong);
        dsHang.add(bac);
        dsHang.add(vang);
        dsHang.add(kimCuong);

        return dsHang;
    }

    /**
     * Lấy danh sách khách hàng thuộc một hạng cụ thể
     */
    public List<KhachHang> getDanhSachKhachHangTheoHang(String tenHang) {
        List<KhachHang> ketQua = new ArrayList<>();
        ArrayList<KhachHang> ds = khachHangDAO.getAllKhachHangDayDu();
        if (ds != null) {
            for (KhachHang kh : ds) {
                if (kh.getHangThanhVien() != null && kh.getHangThanhVien().equalsIgnoreCase(tenHang)) {
                    ketQua.add(kh);
                }
            }
        }
        return ketQua;
    }

    /**
     * Tổng chi tiêu tích lũy của toàn bộ khách hàng
     */
    public double getTongChiTieuThanhVien() {
        double tong = 0.0;
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return 0.0;
            }
            String sql = "SELECT ISNULL(SUM(soTien), 0) FROM ThanhToan";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                tong = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }

    /**
     * Tổng số tiền giảm giá khuyến mãi đã áp dụng từ các hóa đơn
     */
    public double getTongGiamGiaDaApDung() {
        double tong = 0.0;
        try {
            Connection con = ConnectDB.getCon();
            if (con == null) {
                return 0.0;
            }
            String sql = "SELECT ISNULL(SUM(c.soLuong * c.donGia * (ISNULL(km.mucKM, 0) / 100.0)), 0) " +
                    "FROM HoaDon h " +
                    "JOIN ChiTietHoaDon c ON c.maHD = h.maHD " +
                    "JOIN KhuyenMai km ON km.maKM = h.maKM";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                tong = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }
}
