package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thống kê bán hàng đơn giản cho cửa hàng.
 * Mọi chỉ số đều gắn với khoảng thời gian người dùng chọn và tập trung vào
 * doanh thu, dịch vụ và hàng hóa trong kho. Không thống kê nhân sự.
 */
public class ThongKe_DAO {
    private static final DateTimeFormatter FORMAT_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_THANG = DateTimeFormatter.ofPattern("MM/yyyy");

    /** Mã các chỉ số thống kê mà giao diện sử dụng. */
    public static final String MA_DOANH_THU_TONG = "doanh-thu-tong";
    public static final String MA_DOANH_THU_HANG_HOA = "doanh-thu-hang-hoa";
    public static final String MA_DOANH_THU_DICH_VU = "doanh-thu-dich-vu";
    public static final String MA_SO_HOA_DON = "so-hoa-don";
    public static final String MA_SO_LUONG_BAN = "so-luong-ban";
    public static final String MA_TOP_HANG_HOA = "top-hang-hoa";
    public static final String MA_TOP_DICH_VU = "top-dich-vu";
    public static final String MA_TON_KHO = "ton-kho-hien-tai";
    public static final String MA_TOP_TON_KHO = "top-ton-kho";
    public static final String MA_TONG_LUONG = "tong-luong";
    public static final String MA_LUONG_NHAN_VIEN = "luong-theo-nhan-vien";

    public static class DuLieuThongKe {
        public double doanhThuTong;
        public double doanhThuHangHoa;
        public double doanhThuDichVu;
        public int soHoaDon;
        public int soLuongBan;
        public double tongLuong;
        public final List<ChiSoThongKe> chiSo = new ArrayList<>();
    }

    public static class ChiSoThongKe {
        public final String ma;
        public final String ten;
        public final String donVi;
        public final String hauTo;
        public final String ghiChu;
        public final List<String> nhan = new ArrayList<>();
        public final List<String> chuoi = new ArrayList<>();
        public final List<List<Double>> giaTri = new ArrayList<>();
        public double tong;

        ChiSoThongKe(String ma, String ten, String donVi, String hauTo, String ghiChu) {
            this.ma = ma;
            this.ten = ten;
            this.donVi = donVi;
            this.hauTo = hauTo;
            this.ghiChu = ghiChu;
        }

        void them(String nhanMoi, double giaTriMoi) {
            nhan.add(nhanMoi);
            if (giaTri.isEmpty()) {
                giaTri.add(new ArrayList<>());
            }
            giaTri.get(0).add(giaTriMoi);
            tong += giaTriMoi;
        }
    }

    private enum KieuThoiGian {
        NGAY, THANG, NAM
    }

    public DuLieuThongKe layDuLieuThongKe(LocalDate tu, LocalDate den) throws Exception {
        if (tu == null || den == null || tu.isAfter(den)) {
            throw new IllegalArgumentException("Khoảng thời gian thống kê không hợp lệ.");
        }
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu.");
        }

        DuLieuThongKe ketQua = new DuLieuThongKe();
        KieuThoiGian kieu = chonKieuThoiGian(tu, den);

        ketQua.chiSo.add(doanhThuTheoThoiGian(con, tu, den, kieu));
        ketQua.chiSo.add(doanhThuTheoNguon(con, tu, den, kieu, MA_DOANH_THU_HANG_HOA));
        ketQua.chiSo.add(doanhThuTheoNguon(con, tu, den, kieu, MA_DOANH_THU_DICH_VU));
        ketQua.chiSo.add(soHoaDonTheoThoiGian(con, tu, den, kieu));
        ketQua.chiSo.add(soLuongBanTheoThoiGian(con, tu, den, kieu));
        ketQua.chiSo.add(topHangHoa(con, tu, den));
        ketQua.chiSo.add(topDichVu(con, tu, den));
        ketQua.chiSo.add(tonKhoHienTai(con));
        ketQua.chiSo.add(topTonKho(con));
        ketQua.chiSo.add(tongLuongNhanSu(con, tu, den));
        ketQua.chiSo.add(luongTheoNhanVien(con, tu, den));

        for (ChiSoThongKe chiSo : ketQua.chiSo) {
            switch (chiSo.ma) {
                case MA_DOANH_THU_TONG:
                    ketQua.doanhThuTong = chiSo.tong;
                    break;
                case MA_SO_HOA_DON:
                    ketQua.soHoaDon = (int) chiSo.tong;
                    break;
                case MA_SO_LUONG_BAN:
                    ketQua.soLuongBan = (int) chiSo.tong;
                    break;
                case MA_DOANH_THU_HANG_HOA:
                    ketQua.doanhThuHangHoa = chiSo.tong;
                    break;
                case MA_DOANH_THU_DICH_VU:
                    ketQua.doanhThuDichVu = chiSo.tong;
                    break;
                case MA_TONG_LUONG:
                    ketQua.tongLuong = chiSo.tong;
                    break;
                default:
                    break;
            }
        }
        return ketQua;
    }

    // ==================== CÁC CHỈ SỐ ====================

    private ChiSoThongKe doanhThuTheoThoiGian(Connection con, LocalDate tu, LocalDate den, KieuThoiGian kieu)
            throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_DOANH_THU_TONG, "Doanh thu theo thời gian",
                "VNĐ", "đ", "Tổng giá trị các dòng hàng hóa và dịch vụ trên hóa đơn trong kỳ.");
        chiSo.chuoi.add("Doanh thu");

        String moc = bieuThucMoc("h.ngayLap", kieu);
        String sql = "SELECT " + moc + " AS Moc, ISNULL(SUM(c.soLuong * c.donGia), 0) AS GiaTri "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY " + moc + " ORDER BY Moc";

        Map<String, Double> soLieu = docTheoMoc(con, sql, tu, den);
        for (Map.Entry<String, Double> entry : soLieu.entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Chưa có doanh thu trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe doanhThuTheoNguon(Connection con, LocalDate tu, LocalDate den, KieuThoiGian kieu,
            String ma) throws SQLException {
        boolean hangHoa = MA_DOANH_THU_HANG_HOA.equals(ma);
        ChiSoThongKe chiSo = new ChiSoThongKe(ma,
                hangHoa ? "Doanh thu hàng hóa theo thời gian" : "Doanh thu dịch vụ theo thời gian",
                "VNĐ", "đ",
                hangHoa ? "Doanh thu từ các dòng vật tư bán ra trong kỳ."
                        : "Doanh thu từ các dòng dịch vụ đã thực hiện trong kỳ.");
        chiSo.chuoi.add(hangHoa ? "Hàng hóa" : "Dịch vụ");
        chiSo.giaTri.add(new ArrayList<>());

        String cotLoc = hangHoa ? "c.maVT" : "c.maDV";
        String moc = bieuThucMoc("h.ngayLap", kieu);
        String sql = "SELECT " + moc + " AS Moc, ISNULL(SUM(c.soLuong * c.donGia), 0) AS GiaTri "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE " + cotLoc + " IS NOT NULL AND h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY " + moc + " ORDER BY Moc";

        for (Map.Entry<String, Double> entry : docTheoMoc(con, sql, tu, den).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, hangHoa ? "Chưa bán hàng hóa trong kỳ" : "Chưa có dịch vụ trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe soHoaDonTheoThoiGian(Connection con, LocalDate tu, LocalDate den, KieuThoiGian kieu)
            throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_SO_HOA_DON, "Số hóa đơn theo thời gian",
                "hóa đơn", "", "Số hóa đơn được lập trong kỳ, nhóm theo ngày, tháng hoặc năm.");
        chiSo.chuoi.add("Số hóa đơn");

        String moc = bieuThucMoc("ngayLap", kieu);
        String sql = "SELECT " + moc + " AS Moc, COUNT(*) AS GiaTri FROM HoaDon "
                + "WHERE ngayLap >= ? AND ngayLap < ? "
                + "GROUP BY " + moc + " ORDER BY Moc";

        for (Map.Entry<String, Double> entry : docTheoMoc(con, sql, tu, den).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Chưa có hóa đơn trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe soLuongBanTheoThoiGian(Connection con, LocalDate tu, LocalDate den, KieuThoiGian kieu)
            throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_SO_LUONG_BAN, "Số lượng hàng bán theo thời gian",
                "sản phẩm", "", "Tổng số lượng vật tư được bán (không gồm dịch vụ) trong kỳ.");
        chiSo.chuoi.add("Số lượng bán");

        String moc = bieuThucMoc("h.ngayLap", kieu);
        String sql = "SELECT " + moc + " AS Moc, ISNULL(SUM(c.soLuong), 0) AS GiaTri "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE c.maVT IS NOT NULL AND h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY " + moc + " ORDER BY Moc";

        for (Map.Entry<String, Double> entry : docTheoMoc(con, sql, tu, den).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Chưa bán được hàng hóa trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe topHangHoa(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_TOP_HANG_HOA, "Hàng hóa bán chạy",
                "sản phẩm", "", "Xếp hạng vật tư theo số lượng bán trong kỳ.");
        chiSo.chuoi.add("Số lượng bán");

        String sql = "SELECT v.tenVT AS Nhan, ISNULL(SUM(c.soLuong), 0) AS GiaTri "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "JOIN VatTu v ON v.maVT = c.maVT "
                + "WHERE h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY v.tenVT ORDER BY GiaTri DESC, v.tenVT";

        for (Map.Entry<String, Double> entry : docTheoNhan(con, sql, tu, den).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Chưa có hàng hóa bán trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe topDichVu(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_TOP_DICH_VU, "Dịch vụ theo doanh thu",
                "VNĐ", "đ", "Xếp hạng dịch vụ theo doanh thu trong kỳ.");
        chiSo.chuoi.add("Doanh thu");

        String sql = "SELECT d.tenDV AS Nhan, ISNULL(SUM(c.soLuong * c.donGia), 0) AS GiaTri "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "JOIN DichVu d ON d.maDV = c.maDV "
                + "WHERE h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY d.tenDV ORDER BY GiaTri DESC, d.tenDV";

        for (Map.Entry<String, Double> entry : docTheoNhan(con, sql, tu, den).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Chưa có dịch vụ phát sinh trong kỳ");
        return chiSo;
    }

    private ChiSoThongKe tonKhoHienTai(Connection con) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_TON_KHO, "Hàng hóa hiện có trong kho",
                "sản phẩm", "", "Số lượng vật tư còn tồn tại thời điểm xem thống kê.");
        chiSo.chuoi.add("Số lượng tồn");

        String sql = "SELECT v.tenVT AS Nhan, ISNULL(v.soLuongTon, 0) AS GiaTri FROM VatTu v "
                + "ORDER BY v.soLuongTon DESC, v.tenVT";
        for (Map.Entry<String, Double> entry : docTheoNhanKhongLoc(con, sql).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Kho chưa có vật tư");
        return chiSo;
    }

    private ChiSoThongKe topTonKho(Connection con) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_TOP_TON_KHO, "Hàng tồn kho nhiều nhất",
                "sản phẩm", "", "5 vật tư có số lượng tồn cao nhất, giúp chủ động nhập hàng.");
        chiSo.chuoi.add("Số lượng tồn");

        String sql = "SELECT TOP 5 v.tenVT AS Nhan, ISNULL(v.soLuongTon, 0) AS GiaTri FROM VatTu v "
                + "ORDER BY v.soLuongTon DESC, v.tenVT";
        for (Map.Entry<String, Double> entry : docTheoNhanKhongLoc(con, sql).entrySet()) {
            chiSo.them(entry.getKey(), entry.getValue());
        }
        chuaCoDuLieu(chiSo, "Kho chưa có vật tư");
        return chiSo;
    }

    // ==================== TIỀN LƯƠNG ====================

    /**
     * Tổng lương nhân sự trong kỳ: lương nhân viên theo chức vụ + hoa hồng dịch vụ,
     * cộng lương tháng bác sĩ phân bổ theo kỳ và hoa hồng 10%. Quản lý chưa tính lương.
     */
    private ChiSoThongKe tongLuongNhanSu(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_TONG_LUONG, "Tiền lương nhân sự dự tính",
                "VNĐ", "đ", "Lương theo chức vụ và lương bác sĩ trong kỳ, cộng hoa hồng 5% dịch vụ cho nhân viên dịch vụ và 10% cho bác sĩ.");
        chiSo.chuoi.add("Tiền lương");

        double tong = 0;
        for (NguoiLaoDong nld : layDanhSachNguoiLaoDong(con, tu, den)) {
            tong += nld.tongLuong();
        }
        chiSo.them("Toàn kỳ", tong);
        return chiSo;
    }

    private ChiSoThongKe luongTheoNhanVien(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(MA_LUONG_NHAN_VIEN, "Lương từng nhân viên trong kỳ",
                "VNĐ", "đ", "Lương cơ bản của nhân viên theo chức vụ, cộng hoa hồng dịch vụ và lương bác sĩ tương ứng.");
        chiSo.chuoi.add("Tiền lương");

        for (NguoiLaoDong nld : layDanhSachNguoiLaoDong(con, tu, den)) {
            chiSo.them(nld.ten + " (" + nld.chucVu + ")", nld.tongLuong());
        }
        chuaCoDuLieu(chiSo, "Chưa có nhân sự để tính lương");
        return chiSo;
    }

    private static class NguoiLaoDong {
        private String ten;
        private String chucVu;
        private double luongCoBan;
        private double hoaHong;

        private double tongLuong() {
            return luongCoBan + hoaHong;
        }
    }

    private List<NguoiLaoDong> layDanhSachNguoiLaoDong(Connection con, LocalDate tu, LocalDate den)
            throws SQLException {
        List<NguoiLaoDong> ds = new ArrayList<>();

        String sqlNhanVien = "SELECT maNhanVien, hoTen, chucVu, luongCoBan FROM NhanVien ORDER BY hoTen";
        try (PreparedStatement ps = con.prepareStatement(sqlNhanVien);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maNhanVien");
                String ten = rs.getString("hoTen");
                String chucVu = rs.getString("chucVu");
                double luongCoBanGoc = rs.getDouble("luongCoBan");

                NguoiLaoDong nld = new NguoiLaoDong();
                nld.ten = ten;
                nld.chucVu = chucVu;

                if ("Nhân viên bán hàng".equals(chucVu) || "Nhân viên dịch vụ".equals(chucVu)) {
                    double soGio = laySoGioLamViec(con, ma, tu, den);
                    nld.luongCoBan = soGio * luongCoBanGoc;
                    if ("Nhân viên dịch vụ".equals(chucVu)) {
                        nld.hoaHong = doanhThuDichVuNhanVien(con, ma, tu, den) * 0.05;
                    }
                } else {
                    nld.luongCoBan = phanBoTheoKy(luongCoBanGoc, tu, den);
                }
                ds.add(nld);
            }
        }

        String sqlBacSi = "SELECT maBacSi, hoTen, luongThang FROM BacSyThuY ORDER BY hoTen";
        try (PreparedStatement ps = con.prepareStatement(sqlBacSi);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NguoiLaoDong nld = new NguoiLaoDong();
                nld.ten = rs.getString("hoTen");
                nld.chucVu = "Bác sĩ";
                nld.luongCoBan = phanBoTheoKy(rs.getDouble("luongThang"), tu, den);
                nld.hoaHong = doanhThuDichVuBacSi(con, rs.getString("maBacSi"), tu, den) * 0.10;
                ds.add(nld);
            }
        }
        return ds;
    }

    private double laySoGioLamViec(Connection con, String maNhanVien, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = "SELECT ROUND(ISNULL(SUM(DATEDIFF(MINUTE, gioBatDau, gioKetThuc) / 60.0), 0), 0) "
                + "FROM LichLamViec WHERE maNhanVien = ? AND ngay >= ? AND ngay < ? "
                + "AND gioBatDau IS NOT NULL AND gioKetThuc IS NOT NULL "
                + "AND trangThai IN (N'Đang làm', N'Hoàn thành')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setDate(2, java.sql.Date.valueOf(tu));
            ps.setDate(3, java.sql.Date.valueOf(den.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Phân bổ lương tháng theo số ngày của kỳ thống kê. */
    private static double phanBoTheoKy(double luongThang, LocalDate tu, LocalDate den) {
        if (luongThang <= 0) {
            return 0;
        }
        double tong = 0;
        LocalDate ngay = tu;
        while (!ngay.isAfter(den)) {
            tong += luongThang / ngay.lengthOfMonth();
            ngay = ngay.plusDays(1);
        }
        return tong;
    }

    private double doanhThuDichVuNhanVien(Connection con, String maNhanVien, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = "SELECT ISNULL(SUM(c.soLuong * c.donGia), 0) "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE c.maDV IS NOT NULL AND c.maNhanVien = ? "
                + "AND h.ngayLap >= ? AND h.ngayLap < ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setTimestamp(2, Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private double doanhThuDichVuBacSi(Connection con, String maBacSi, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = "SELECT ISNULL(SUM(c.soLuong * c.donGia), 0) "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE c.maDV IS NOT NULL AND c.maBacSi = ? "
                + "AND h.ngayLap >= ? AND h.ngayLap < ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBacSi);
            ps.setTimestamp(2, Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    // ==================== TRUY VẤN DÙNG CHUNG ====================

    private Map<String, Double> docTheoMoc(Connection con, String sql, LocalDate tu, LocalDate den)
            throws SQLException {
        Map<String, Double> ketQua = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.put(String.valueOf(rs.getString("Moc")), rs.getDouble("GiaTri"));
                }
            }
        }
        return ketQua;
    }

    private Map<String, Double> docTheoNhan(Connection con, String sql, LocalDate tu, LocalDate den)
            throws SQLException {
        Map<String, Double> ketQua = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.put(String.valueOf(rs.getString("Nhan")), rs.getDouble("GiaTri"));
                }
            }
        }
        return ketQua;
    }

    private Map<String, Double> docTheoNhanKhongLoc(Connection con, String sql) throws SQLException {
        Map<String, Double> ketQua = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ketQua.put(String.valueOf(rs.getString("Nhan")), rs.getDouble("GiaTri"));
            }
        }
        return ketQua;
    }

    private static void chuaCoDuLieu(ChiSoThongKe chiSo, String thongBao) {
        if (chiSo.nhan.isEmpty()) {
            chiSo.nhan.add(thongBao);
            if (chiSo.giaTri.isEmpty()) {
                chiSo.giaTri.add(new ArrayList<>());
            }
            chiSo.giaTri.get(0).add(0.0);
        }
    }

    private static KieuThoiGian chonKieuThoiGian(LocalDate tu, LocalDate den) {
        long soNgay = ChronoUnit.DAYS.between(tu, den) + 1;
        if (soNgay <= 62) {
            return KieuThoiGian.NGAY;
        }
        if (soNgay <= 730) {
            return KieuThoiGian.THANG;
        }
        return KieuThoiGian.NAM;
    }

    private static String bieuThucMoc(String cot, KieuThoiGian kieu) {
        switch (kieu) {
            case NGAY:
                return "CONVERT(VARCHAR(10), " + cot + ", 103)";
            case THANG:
                return "RIGHT('0' + CAST(MONTH(" + cot + ") AS VARCHAR(2)), 2) + '/' "
                        + "+ CAST(YEAR(" + cot + ") AS VARCHAR(4))";
            default:
                return "CAST(YEAR(" + cot + ") AS VARCHAR(4))";
        }
    }

    /** Tiện ích cho giao diện: mô tả cách nhóm mốc thời gian. */
    public static String moTaNhomThoiGian(LocalDate tu, LocalDate den) {
        switch (chonKieuThoiGian(tu, den)) {
            case NGAY:
                return "nhóm theo ngày";
            case THANG:
                return "nhóm theo tháng";
            default:
                return "nhóm theo năm";
        }
    }

    /** Tiện ích cho giao diện: định dạng khoảng thời gian đang xem. */
    public static String moTaKhoang(LocalDate tu, LocalDate den) {
        return FORMAT_NGAY.format(tu) + " – " + FORMAT_NGAY.format(den);
    }

    /** Tiện ích cho giao diện: nhãn tháng của một YearMonth. */
    public static String nhanThang(YearMonth thang) {
        return FORMAT_THANG.format(thang);
    }
}
