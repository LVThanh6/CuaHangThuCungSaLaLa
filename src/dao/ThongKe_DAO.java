package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongKe_DAO {
    private static final DateTimeFormatter FORMAT_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_THANG = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter FORMAT_NAM = DateTimeFormatter.ofPattern("yyyy");

    public static class DuLieuThongKe {
        public int soNhanVien;
        public int soBacSi;
        public int soKhachHang;
        public int soThuCung;
        public double doanhThu;
        public int soHoaDon;
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

        private ChiSoThongKe(String ma, String ten, String donVi, String hauTo, String ghiChu) {
            this.ma = ma;
            this.ten = ten;
            this.donVi = donVi;
            this.hauTo = hauTo;
            this.ghiChu = ghiChu;
        }
    }

    private enum KieuThoiGian {
        NGAY,
        THANG,
        NAM,
        HIEN_TAI
    }

    private static class DinhNghiaChiSo {
        private final String ma;
        private final String ten;
        private final String donVi;
        private final String hauTo;
        private final String from;
        private final String ngay;
        private final String bieuThuc;
        private final String nhom;
        private final String ghiChu;

        private DinhNghiaChiSo(String ma, String ten, String donVi, String hauTo,
                String from, String ngay, String bieuThuc, String nhom, String ghiChu) {
            this.ma = ma;
            this.ten = ten;
            this.donVi = donVi;
            this.hauTo = hauTo;
            this.from = from;
            this.ngay = ngay;
            this.bieuThuc = bieuThuc;
            this.nhom = nhom;
            this.ghiChu = ghiChu;
        }
    }

    private static DinhNghiaChiSo bang(String ma, String ten, String bang, String ngay) {
        return new DinhNghiaChiSo(ma, ten, "bản ghi", "", bang, ngay, "COUNT(*)", null,
                ngay == null ? "Số liệu hiện tại, không có cột ngày." : "Số bản ghi theo thời gian.");
    }

    private static DinhNghiaChiSo tong(String ma, String ten, String bang, String ngay,
            String bieuThuc, String donVi, String hauTo) {
        return new DinhNghiaChiSo(ma, ten, donVi, hauTo, bang, ngay, bieuThuc, null,
                ngay == null ? "Số liệu hiện tại, không có cột ngày." : "Tổng theo thời gian.");
    }

    private static DinhNghiaChiSo nhom(String ma, String ten, String from, String ngay,
            String bieuThuc, String nhom, String donVi, String hauTo, String ghiChu) {
        return new DinhNghiaChiSo(ma, ten, donVi, hauTo, from, ngay, bieuThuc, nhom, ghiChu);
    }

    private static final List<DinhNghiaChiSo> DINH_NGHIA = taoDinhNghia();

    private static List<DinhNghiaChiSo> taoDinhNghia() {
        List<DinhNghiaChiSo> ds = new ArrayList<>();
        ds.add(bang("tai-khoan", "Tài khoản", "TaiKhoan", null));
        ds.add(bang("quan-ly", "Quản lý cửa hàng", "QuanLyCuaHang", null));
        ds.add(bang("nhan-vien", "Nhân viên", "NhanVien", null));
        ds.add(bang("bac-si", "Bác sĩ thú y", "BacSyThuY", null));
        ds.add(bang("khach-hang", "Khách hàng", "KhachHang", null));
        ds.add(bang("thu-cung", "Thú cưng", "ThuCung", null));
        ds.add(bang("lich-lam-viec", "Lịch làm việc", "LichLamViec", "ngay"));
        ds.add(bang("ho-so-suc-khoe", "Hồ sơ sức khỏe", "HoSoSucKhoe", "ngayKham"));
        ds.add(bang("thuoc", "Thuốc", "Thuoc", null));
        ds.add(bang("don-thuoc", "Đơn thuốc", "DonThuoc", "ngayKeDon"));
        ds.add(bang("chi-tiet-don-thuoc", "Chi tiết đơn thuốc",
                "ChiTietDonThuoc c JOIN DonThuoc p ON p.maDonThuoc = c.maDonThuoc", "p.ngayKeDon"));
        ds.add(bang("dich-vu", "Dịch vụ", "DichVu", null));
        ds.add(bang("lich-hen", "Lịch hẹn", "LichHen", "ngayGio"));
        ds.add(bang("khuyen-mai", "Khuyến mãi", "KhuyenMai", "ngayBatDau"));
        ds.add(bang("thue", "Thuế", "Thue", null));
        ds.add(bang("thanh-toan", "Thanh toán", "ThanhToan", "thoiGian"));
        ds.add(bang("hoa-don", "Hóa đơn", "HoaDon", "ngayLap"));
        ds.add(bang("nha-cung-cap", "Nhà cung cấp", "NhaCungCap", null));
        ds.add(bang("vat-tu", "Vật tư", "VatTu", null));
        ds.add(bang("chi-tiet-hoa-don", "Chi tiết hóa đơn",
                "ChiTietHoaDon c JOIN HoaDon p ON p.maHD = c.maHD", "p.ngayLap"));
        ds.add(bang("phieu-xuat-kho", "Phiếu xuất kho", "PhieuXuatKho", "ngayXuat"));
        ds.add(bang("chi-tiet-phieu-xuat-kho", "Chi tiết phiếu xuất kho",
                "ChiTietPhieuXuatKho c JOIN PhieuXuatKho p ON p.maPXK = c.maPXK", "p.ngayXuat"));
        ds.add(bang("phieu-nhap-kho", "Phiếu nhập kho", "PhieuNhapKho", "ngayNhap"));
        ds.add(bang("chi-tiet-phieu-nhap-kho", "Chi tiết phiếu nhập kho",
                "ChiTietPhieuNhapKho c JOIN PhieuNhapKho p ON p.maPNK = c.maPNK", "p.ngayNhap"));

        ds.add(tong("doanh-thu", "Doanh thu thanh toán",
                "HoaDon h LEFT JOIN ThanhToan t ON t.maThanhToan = h.maThanhToan",
                "t.thoiGian", "ISNULL(SUM(t.soTien), 0)", "VNĐ", "đ"));
        ds.add(tong("so-luong-ban", "Số lượng vật tư/dịch vụ bán",
                "ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD",
                "h.ngayLap", "ISNULL(SUM(c.soLuong), 0)", "sản phẩm", ""));
        ds.add(tong("luong-nhap-kho", "Số lượng vật tư nhập kho",
                "ChiTietPhieuNhapKho c JOIN PhieuNhapKho p ON p.maPNK = c.maPNK",
                "p.ngayNhap", "ISNULL(SUM(c.soLuong), 0)", "vật tư", ""));
        ds.add(tong("gia-tri-nhap-kho", "Giá trị vật tư nhập kho",
                "ChiTietPhieuNhapKho c JOIN PhieuNhapKho p ON p.maPNK = c.maPNK",
                "p.ngayNhap", "ISNULL(SUM(c.soLuong * c.donGia), 0)", "VNĐ", "đ"));
        ds.add(tong("luong-xuat-kho", "Số lượng vật tư xuất kho",
                "ChiTietPhieuXuatKho c JOIN PhieuXuatKho p ON p.maPXK = c.maPXK",
                "p.ngayXuat", "ISNULL(SUM(c.soLuong), 0)", "vật tư", ""));
        ds.add(tong("ton-kho", "Tổng số lượng tồn kho",
                "VatTu", null, "ISNULL(SUM(soLuongTon), 0)", "vật tư", ""));
        ds.add(tong("gia-tri-ton-kho", "Giá trị tồn kho theo giá nhập",
                "VatTu", null, "ISNULL(SUM(soLuongTon * giaNhap), 0)", "VNĐ", "đ"));
        ds.add(tong("doanh-thu-vat-tu", "Doanh thu bán vật tư",
                "(SELECT c.soLuong, c.donGia, h.ngayLap FROM ChiTietHoaDon c "
                        + "JOIN HoaDon h ON h.maHD = c.maHD WHERE c.maVT IS NOT NULL) q",
                "q.ngayLap", "ISNULL(SUM(q.soLuong * q.donGia), 0)",
                "VNĐ", "đ"));
        ds.add(tong("doanh-thu-dich-vu", "Doanh thu dịch vụ",
                "(SELECT c.soLuong, c.donGia, h.ngayLap FROM ChiTietHoaDon c "
                        + "JOIN HoaDon h ON h.maHD = c.maHD WHERE c.maDV IS NOT NULL) q",
                "q.ngayLap", "ISNULL(SUM(q.soLuong * q.donGia), 0)",
                "VNĐ", "đ"));
        ds.add(nhom("doanh-so-nhan-vien", "Doanh số hóa đơn theo nhân viên",
                "ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                        + "JOIN NhanVien nv ON nv.maNhanVien = h.maNhanVien",
                "h.ngayLap", "ISNULL(SUM(c.soLuong * c.donGia), 0)",
                "nv.hoTen", "VNĐ", "đ",
                "Doanh số gộp được ghi nhận theo nhân viên lập hóa đơn."));
        ds.add(nhom("doanh-thu-dich-vu-nguoi-thuc-hien", "Doanh thu dịch vụ theo người thực hiện",
                "ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                        + "LEFT JOIN NhanVien nv ON nv.maNhanVien = c.maNhanVien "
                        + "LEFT JOIN BacSyThuY bs ON bs.maBacSi = c.maBacSi",
                "h.ngayLap", "ISNULL(SUM(c.soLuong * c.donGia), 0)",
                "COALESCE(nv.hoTen, bs.hoTen, N'Chưa phân công')", "VNĐ", "đ",
                "Doanh thu gộp của dịch vụ theo nhân viên hoặc bác sĩ được phân công."));
        ds.add(nhom("luot-ban-theo-vat-tu", "Vật tư bán chạy",
                "ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                        + "JOIN VatTu v ON v.maVT = c.maVT",
                "h.ngayLap", "ISNULL(SUM(c.soLuong), 0)",
                "v.tenVT", "đơn vị", "",
                "Xếp hạng số lượng bán theo vật tư trong khoảng thời gian đã chọn."));
        ds.add(nhom("hoa-don-trang-thai", "Hóa đơn theo trạng thái",
                "HoaDon", "ngayLap", "COUNT(*)", "trangThai", "hóa đơn", "",
                "Phân loại hóa đơn theo trạng thái trong khoảng ngày đã chọn."));
        ds.add(nhom("lich-hen-trang-thai", "Lịch hẹn theo trạng thái",
                "LichHen", "ngayGio", "COUNT(*)", "trangThai", "lịch hẹn", "",
                "Phân loại lịch hẹn theo trạng thái trong khoảng ngày đã chọn."));
        ds.add(nhom("thanh-toan-phuong-thuc", "Thanh toán theo phương thức",
                "ThanhToan", "thoiGian", "COUNT(*)", "phuongThuc", "giao dịch", "",
                "Phân loại giao dịch theo phương thức trong khoảng ngày đã chọn."));
        ds.add(nhom("thu-cung-theo-loai", "Thú cưng theo loài",
                "ThuCung", null, "COUNT(*)", "loai", "thú cưng", "",
                "Phân loại thú cưng theo loài hiện có."));
        ds.add(nhom("vat-tu-theo-trang-thai", "Vật tư theo trạng thái",
                "VatTu", null, "COUNT(*)", "trangThai", "vật tư", "",
                "Phân loại vật tư theo trạng thái hiện tại."));
        ds.add(nhom("doanh-thu-theo-dich-vu", "Doanh thu theo dịch vụ",
                "ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD"
                        + " JOIN DichVu d ON d.maDV = c.maDV",
                "h.ngayLap", "ISNULL(SUM(c.soLuong * c.donGia), 0)",
                "d.tenDV", "VNĐ", "đ",
                "Doanh thu từng dịch vụ trong khoảng ngày đã chọn."));
        ds.add(nhom("ho-so-kham-theo-bac-si", "Hồ sơ khám theo bác sĩ",
                "HoSoSucKhoe h JOIN BacSyThuY bs ON bs.maBacSi = h.maBacSi",
                "h.ngayKham", "COUNT(*)", "bs.hoTen", "hồ sơ", "",
                "Số hồ sơ sức khỏe được ghi nhận theo bác sĩ trong khoảng thời gian đã chọn."));
        return ds;
    }

    public DuLieuThongKe layDuLieuThongKe(LocalDate tu, LocalDate den) throws Exception {
        if (tu == null || den == null || tu.isAfter(den)) {
            throw new IllegalArgumentException("Khoảng thời gian thống kê không hợp lệ.");
        }
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu.");
        }

        KieuThoiGian kieu = chonKieuThoiGian(tu, den);
        DuLieuThongKe ketQua = new DuLieuThongKe();
        Map<String, ChiSoThongKe> theoMa = new LinkedHashMap<>();
        for (DinhNghiaChiSo dinhNghia : DINH_NGHIA) {
            ChiSoThongKe chiSo = layChiSo(con, dinhNghia, tu, den, kieu);
            ketQua.chiSo.add(chiSo);
            theoMa.put(chiSo.ma, chiSo);
        }
        ketQua.chiSo.add(layChiSoHangTonKhongBan(con, tu, den));
        List<ChiSoThongKe> baoCaoLuong = layChiSoLuong(con, tu, den);
        ketQua.chiSo.addAll(baoCaoLuong);

        ketQua.soNhanVien = (int) theoMa.get("nhan-vien").tong;
        ketQua.soBacSi = (int) theoMa.get("bac-si").tong;
        ketQua.soKhachHang = (int) theoMa.get("khach-hang").tong;
        ketQua.soThuCung = (int) theoMa.get("thu-cung").tong;
        ketQua.doanhThu = theoMa.get("doanh-thu").tong;
        ketQua.soHoaDon = (int) theoMa.get("hoa-don").tong;
        double tongLuong = baoCaoLuong.stream()
                .filter(x -> x.ma.equals("tong-luong-du-tinh"))
                .mapToDouble(x -> x.tong)
                .findFirst().orElse(0);
        ChiSoThongKe loiNhuan = new ChiSoThongKe("loi-nhuan-uoc-tinh", "Lợi nhuận ước tính",
                "VNĐ", "đ", "Doanh thu thanh toán trừ chi phí nhập hàng và lương ước tính. Chưa trừ chi phí vận hành khác vì chưa có bảng ghi nhận.");
        loiNhuan.nhan.add("Toàn kỳ");
        loiNhuan.chuoi.add("Toàn kỳ");
        loiNhuan.tong = ketQua.doanhThu - theoMa.get("gia-tri-nhap-kho").tong - tongLuong;
        loiNhuan.giaTri.add(new ArrayList<>(List.of(loiNhuan.tong)));
        ketQua.chiSo.add(loiNhuan);
        return ketQua;
    }

    private ChiSoThongKe layChiSoHangTonKhongBan(Connection con, LocalDate tu, LocalDate den)
            throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe("hang-ton-khong-ban-trong-ky",
                "Tồn kho không bán trong kỳ", "đơn vị", "",
                "Số lượng tồn hiện tại của vật tư không có giao dịch bán trong kỳ; đây chỉ là chỉ báo, không phải tuổi tồn kho.");
        chiSo.chuoi.add("Số lượng tồn");
        chiSo.giaTri.add(new ArrayList<>());
        String sql = "SELECT v.tenVT, v.soLuongTon FROM VatTu v "
                + "WHERE v.soLuongTon > 0 AND NOT EXISTS ("
                + "SELECT 1 FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE c.maVT = v.maVT AND h.ngayLap >= ? AND h.ngayLap < ?) "
                + "ORDER BY v.soLuongTon DESC, v.tenVT";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double ton = rs.getDouble("soLuongTon");
                    chiSo.nhan.add(rs.getString("tenVT"));
                    chiSo.giaTri.get(0).add(ton);
                    chiSo.tong += ton;
                }
            }
        }
        if (chiSo.nhan.isEmpty()) {
            chiSo.nhan.add("Không có vật tư tồn không bán");
            chiSo.giaTri.get(0).add(0.0);
        }
        return chiSo;
    }

    private List<ChiSoThongKe> layChiSoLuong(Connection con, LocalDate tu, LocalDate den)
            throws SQLException {
        Map<String, Double> luongCoBan = new LinkedHashMap<>();
        Map<String, Double> hoaHong = new LinkedHashMap<>();
        Map<String, Double> gioLamViec = new LinkedHashMap<>();
        String sqlNhanVien = "SELECT nv.maNhanVien, nv.hoTen, nv.chucVu, "
                + "COALESCE(lc.luongCoBan, nv.luongCoBan, 0) AS luongCoBan "
                + "FROM NhanVien nv LEFT JOIN LuongChucVu lc ON lc.chucVu = nv.chucVu "
                + "ORDER BY nv.hoTen";
        try (PreparedStatement ps = con.prepareStatement(sqlNhanVien);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maNhanVien");
                String ten = rs.getString("hoTen") + " (NV)";
                String chucVu = rs.getString("chucVu");
                if ("Quản lý".equals(chucVu)) continue;
                double soGio = laySoGioLamViec(con, ma, tu, den);
                double tienCoBan = soGio * rs.getDouble("luongCoBan") / GIO_CHUAN_THANG;
                gioLamViec.put(ten, soGio);
                double tyLe = "Nhân viên dịch vụ".equals(chucVu) ? 0.05 : 0;
                double tienHoaHong = tyLe == 0 ? 0 : layDoanhThuDichVuNhanVien(con, ma, tu, den) * tyLe;
                luongCoBan.put(ten, tienCoBan);
                hoaHong.put(ten, tienHoaHong);
            }
        }

        String sqlBacSi = "SELECT maBacSi, hoTen, luongThang FROM BacSyThuY ORDER BY hoTen";
        try (PreparedStatement ps = con.prepareStatement(sqlBacSi);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maBacSi");
                String ten = rs.getString("hoTen") + " (BS)";
                double tienCoBan = tinhLuongThangTrongKy(rs.getDouble("luongThang"), tu, den);
                double tienHoaHong = layDoanhThuDichVuBacSi(con, ma, tu, den) * 0.10;
                luongCoBan.put(ten, tienCoBan);
                hoaHong.put(ten, tienHoaHong);
            }
        }

        ChiSoThongKe tongLuong = new ChiSoThongKe("tong-luong-du-tinh", "Tổng lương dự tính theo nhân sự",
                "VNĐ", "đ", "Lương theo giờ làm việc thực tế, quy đổi từ mức lương chức vụ 208 giờ/tháng, cộng hoa hồng.");
        ChiSoThongKe luongCoBanChiSo = new ChiSoThongKe("luong-co-ban-du-tinh", "Lương cơ bản dự tính",
                "VNĐ", "đ", "Số giờ làm việc thực tế nhân mức lương chức vụ chia 208 giờ chuẩn/tháng.");
        ChiSoThongKe hoaHongChiSo = new ChiSoThongKe("hoa-hong-du-tinh", "Hoa hồng dịch vụ dự tính",
                "VNĐ", "đ", "Hoa hồng tính trên doanh thu dịch vụ trước khuyến mãi và thuế: nhân viên dịch vụ 5%, bác sĩ 10%. Do bảng dịch vụ chưa phân loại khám bệnh, bác sĩ hiện tính trên mọi dòng dịch vụ được gán.");
        ChiSoThongKe gioLamViecChiSo = new ChiSoThongKe("so-gio-lam-viec", "Số giờ làm việc",
                "giờ", "giờ", "Tổng giờ từ các ca Đang làm hoặc Hoàn thành, làm tròn đến giờ.");
        tongLuong.chuoi.add("Tổng lương");
        luongCoBanChiSo.chuoi.add("Lương cơ bản");
        hoaHongChiSo.chuoi.add("Hoa hồng");
        gioLamViecChiSo.chuoi.add("Số giờ");
        tongLuong.giaTri.add(new ArrayList<>());
        luongCoBanChiSo.giaTri.add(new ArrayList<>());
        hoaHongChiSo.giaTri.add(new ArrayList<>());
        gioLamViecChiSo.giaTri.add(new ArrayList<>());
        for (Map.Entry<String, Double> entry : luongCoBan.entrySet()) {
            double coBan = entry.getValue();
            double commission = hoaHong.getOrDefault(entry.getKey(), 0.0);
            tongLuong.nhan.add(entry.getKey());
            tongLuong.giaTri.get(0).add(coBan + commission);
            luongCoBanChiSo.nhan.add(entry.getKey());
            luongCoBanChiSo.giaTri.get(0).add(coBan);
            hoaHongChiSo.nhan.add(entry.getKey());
            hoaHongChiSo.giaTri.get(0).add(commission);
            tongLuong.tong += coBan + commission;
            luongCoBanChiSo.tong += coBan;
            hoaHongChiSo.tong += commission;
        }
        for (Map.Entry<String, Double> entry : gioLamViec.entrySet()) {
            gioLamViecChiSo.nhan.add(entry.getKey());
            gioLamViecChiSo.giaTri.get(0).add(entry.getValue());
            gioLamViecChiSo.tong += entry.getValue();
        }
        if (tongLuong.nhan.isEmpty()) {
            for (ChiSoThongKe chiSo : List.of(tongLuong, luongCoBanChiSo, hoaHongChiSo, gioLamViecChiSo)) {
                chiSo.nhan.add("Không có dữ liệu");
                chiSo.giaTri.get(0).add(0.0);
            }
        }
        return List.of(tongLuong, luongCoBanChiSo, hoaHongChiSo, gioLamViecChiSo);
    }

    private static final double GIO_CHUAN_THANG = 26 * 8;

    private double laySoGioLamViec(Connection con, String maNhanVien, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = "SELECT ROUND(ISNULL(SUM(DATEDIFF(MINUTE, gioBatDau, gioKetThuc) / 60.0), 0), 0) "
                + "FROM LichLamViec WHERE maNhanVien = ? AND ngay >= ? AND ngay < ? "
                + "AND gioBatDau IS NOT NULL AND gioKetThuc IS NOT NULL "
                + "AND trangThai IN (N'Đang làm', N'Hoàn thành')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setDate(2, Date.valueOf(tu));
            ps.setDate(3, Date.valueOf(den.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private double layDoanhThuDichVuNhanVien(Connection con, String maNhanVien, LocalDate tu, LocalDate den)
            throws SQLException {
        return layDoanhThuDichVuNguoiThucHien(con, "maNhanVien", maNhanVien, tu, den);
    }

    private double layDoanhThuDichVuBacSi(Connection con, String maBacSi, LocalDate tu, LocalDate den)
            throws SQLException {
        return layDoanhThuDichVuNguoiThucHien(con, "maBacSi", maBacSi, tu, den);
    }

    private double layDoanhThuDichVuNguoiThucHien(Connection con, String cotNguoiThucHien,
            String maNguoiThucHien, LocalDate tu, LocalDate den) throws SQLException {
        String sql = "SELECT ISNULL(SUM(c.soLuong * c.donGia), 0) "
                + "FROM ChiTietHoaDon c JOIN HoaDon h ON h.maHD = c.maHD "
                + "WHERE c.maDV IS NOT NULL AND c." + cotNguoiThucHien + " = ? "
                + "AND h.ngayLap >= ? AND h.ngayLap < ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNguoiThucHien);
            ps.setDate(2, Date.valueOf(tu));
            ps.setDate(3, Date.valueOf(den.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private static double tinhLuongThangTrongKy(double luongThang, LocalDate tu, LocalDate den) {
        double tong = 0;
        LocalDate thang = tu.withDayOfMonth(1);
        LocalDate thangCuoi = den.withDayOfMonth(1);
        while (!thang.isAfter(thangCuoi)) {
            LocalDate ngayDauKy = tu.isAfter(thang) ? tu : thang;
            LocalDate cuoiThang = thang.withDayOfMonth(thang.lengthOfMonth());
            LocalDate ngayCuoiKy = den.isBefore(cuoiThang) ? den : cuoiThang;
            long ngayTrongKy = ChronoUnit.DAYS.between(ngayDauKy, ngayCuoiKy) + 1;
            tong += luongThang * ngayTrongKy / thang.lengthOfMonth();
            thang = thang.plusMonths(1);
        }
        return tong;
    }

    private ChiSoThongKe layChiSo(Connection con, DinhNghiaChiSo dinhNghia,
            LocalDate tu, LocalDate den, KieuThoiGian kieu) throws SQLException {
        ChiSoThongKe chiSo = new ChiSoThongKe(
                dinhNghia.ma, dinhNghia.ten, dinhNghia.donVi, dinhNghia.hauTo, dinhNghia.ghiChu);
        chiSo.chuoi.add(dinhNghia.ten);
        List<Double> values = new ArrayList<>();

        if (dinhNghia.nhom != null) {
            layDuLieuTheoNhom(con, dinhNghia, tu, den, chiSo, values);
        } else if (dinhNghia.ngay == null) {
            String sql = "SELECT " + dinhNghia.bieuThuc + " FROM " + dinhNghia.from;
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    values.add(rs.getDouble(1));
                }
            }
            chiSo.nhan.add("Toàn hệ thống");
        } else {
            layDuLieuTheoThoiGian(con, dinhNghia, tu, den, kieu, chiSo, values);
        }

        chiSo.giaTri.add(values);
        for (double value : values) {
            chiSo.tong += value;
        }
        return chiSo;
    }

    private void layDuLieuTheoNhom(Connection con, DinhNghiaChiSo dinhNghia,
            LocalDate tu, LocalDate den, ChiSoThongKe chiSo, List<Double> values) throws SQLException {
        String nhanNhom = "ISNULL(CAST(" + dinhNghia.nhom + " AS NVARCHAR(255)), N'Không xác định')";
        StringBuilder sql = new StringBuilder("SELECT ").append(nhanNhom)
                .append(" AS Nhom, ").append(dinhNghia.bieuThuc).append(" AS GiaTri FROM ")
                .append(dinhNghia.from);
        if (dinhNghia.ngay != null) {
            sql.append(" WHERE ").append(dinhNghia.ngay).append(" >= ? AND ")
                    .append(dinhNghia.ngay).append(" < ?");
        }
        sql.append(" GROUP BY ").append(dinhNghia.nhom);
        if ("luot-ban-theo-vat-tu".equals(dinhNghia.ma)) {
            sql.append(" ORDER BY GiaTri DESC, Nhom");
        } else {
            sql.append(" ORDER BY Nhom");
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (dinhNghia.ngay != null) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(tu.atStartOfDay()));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chiSo.nhan.add(rs.getString("Nhom"));
                    values.add(rs.getDouble("GiaTri"));
                }
            }
        }
        if (chiSo.nhan.isEmpty()) {
            chiSo.nhan.add("Không có dữ liệu");
            values.add(0.0);
        }
    }

    private void layDuLieuTheoThoiGian(Connection con, DinhNghiaChiSo dinhNghia,
            LocalDate tu, LocalDate den, KieuThoiGian kieu,
            ChiSoThongKe chiSo, List<Double> values) throws SQLException {
        Map<LocalDate, Double> ketQuaTheoNgay = new LinkedHashMap<>();
        for (LocalDate moc : cacMocThoiGian(tu, den, kieu)) {
            ketQuaTheoNgay.put(moc, 0.0);
        }

        String mocSql = bieuThucMocThoiGian(dinhNghia.ngay, kieu);
        String sql = "SELECT " + mocSql + " AS Moc, " + dinhNghia.bieuThuc
                + " AS GiaTri FROM " + dinhNghia.from
                + " WHERE " + dinhNghia.ngay + " >= ? AND " + dinhNghia.ngay + " < ?"
                + " GROUP BY " + mocSql + " ORDER BY Moc";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(tu.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date moc = rs.getDate("Moc");
                    if (moc != null) {
                        ketQuaTheoNgay.put(moc.toLocalDate(), rs.getDouble("GiaTri"));
                    }
                }
            }
        }

        DateTimeFormatter formatter = formatter(kieu);
        for (Map.Entry<LocalDate, Double> entry : ketQuaTheoNgay.entrySet()) {
            chiSo.nhan.add(entry.getKey().format(formatter));
            values.add(entry.getValue());
        }
    }

    private static KieuThoiGian chonKieuThoiGian(LocalDate tu, LocalDate den) {
        long soNgay = ChronoUnit.DAYS.between(tu, den) + 1;
        if (soNgay <= 45) {
            return KieuThoiGian.NGAY;
        }
        if (soNgay <= 730) {
            return KieuThoiGian.THANG;
        }
        return KieuThoiGian.NAM;
    }

    private static List<LocalDate> cacMocThoiGian(LocalDate tu, LocalDate den, KieuThoiGian kieu) {
        List<LocalDate> mocThoiGian = new ArrayList<>();
        LocalDate moc = batDauMoc(tu, kieu);
        LocalDate mocCuoi = batDauMoc(den, kieu);
        while (!moc.isAfter(mocCuoi)) {
            mocThoiGian.add(moc);
            moc = kieu == KieuThoiGian.NGAY ? moc.plusDays(1)
                    : kieu == KieuThoiGian.THANG ? moc.plusMonths(1) : moc.plusYears(1);
        }
        return mocThoiGian;
    }

    private static LocalDate batDauMoc(LocalDate ngay, KieuThoiGian kieu) {
        if (kieu == KieuThoiGian.THANG) {
            return ngay.withDayOfMonth(1);
        }
        if (kieu == KieuThoiGian.NAM) {
            return ngay.withDayOfYear(1);
        }
        return ngay;
    }

    private static String bieuThucMocThoiGian(String ngay, KieuThoiGian kieu) {
        if (kieu == KieuThoiGian.THANG) {
            return "DATEFROMPARTS(YEAR(" + ngay + "), MONTH(" + ngay + "), 1)";
        }
        if (kieu == KieuThoiGian.NAM) {
            return "DATEFROMPARTS(YEAR(" + ngay + "), 1, 1)";
        }
        return "CONVERT(date, " + ngay + ")";
    }

    private static DateTimeFormatter formatter(KieuThoiGian kieu) {
        if (kieu == KieuThoiGian.THANG) {
            return FORMAT_THANG;
        }
        if (kieu == KieuThoiGian.NAM) {
            return FORMAT_NAM;
        }
        return FORMAT_NGAY;
    }
}
