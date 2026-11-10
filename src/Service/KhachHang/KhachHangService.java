package Service.KhachHang;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.KhachHang;

/**
 * ============================================================================
 * LỚP DỊCH VỤ NGHIỆP VỤ: KhachHangService (Quản lý khách hàng)
 * ============================================================================
 * - Đảm nhận toàn bộ Logic nghiệp vụ (Business Logic) liên quan đến khách hàng.
 * - Đứng giữa tầng Giao diện (GUI) và tầng Truy xuất dữ liệu (DAO).
 * - GUI chỉ việc gọi hàm từ Service để lấy dữ liệu hoặc nhận kết quả xử lý,
 *   không can thiệp vào logic tính toán, kiểm tra dữ liệu hay câu lệnh CSDL.
 *
 * MỤC LỤC CÁC CHỨC NĂNG THUYẾT TRÌNH:
 *   1. HẰNG SỐ CẤU HÌNH BẢNG & BỘ LỌC
 *   2. KHỞI TẠO SERVICE & KẾT NỐI DAO
 *   3. CHỨC NĂNG 1: TẢI & ĐỊNH DẠNG DỮ LIỆU HIỂN THỊ LÊN BẢNG (TABLE)
 *   4. CHỨC NĂNG 2: THỐNG KÊ SỐ LIỆU TỔNG QUAN (DASHBOARD CARDS)
 *   5. CHỨC NĂNG 3: THÊM KHÁCH HÀNG MỚI (KIỂM TRA DỮ LIỆU & LƯU)
 *   6. CHỨC NĂNG 4: CHỈNH SỬA THÔNG TIN KHÁCH HÀNG
 *   7. CHỨC NĂNG 5: XÓA KHÁCH HÀNG AN TOÀN (RÀNG BUỘC TOÀN VẸN)
 *   8. CHỨC NĂNG 6: XEM HỒ SƠ CHI TIẾT (THÚ CƯNG & LỊCH SỬ HÓA ĐƠN)
 *   9. CÁC LỚP ĐỐI TƯỢNG ĐÓNG GÓI DỮ LIỆU (DTO - DATA TRANSFER OBJECTS)
 *   10. CHỨC NĂNG 7: XUẤT DANH SÁCH KHÁCH HÀNG RA FILE CSV
 * ============================================================================
 */
public class KhachHangService {

    // =========================================================================
    // 1. HẰNG SỐ CẤU HÌNH BẢNG & BỘ LỌC
    // =========================================================================
    // Tiêu đề các cột hiển thị trên JTable Danh sách khách hàng
    public static final String[] DANH_SACH_COT = {
            "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
            "Địa chỉ", "Hạng thành viên"
    };

    // Các tùy chọn trong combobox bộ lọc hạng thành viên
    public static final String[] TUY_CHON_LOC = {
            "Hạng thành viên: Tất cả", "Đồng", "Bạc", "Vàng", "Kim cương"
    };

    // Chỉ số cột trong bảng (tránh hardcode số nguyên ở tầng giao diện)
    public static final int COT_MA_KH = 0;
    public static final int COT_HO_TEN = 1;
    public static final int COT_SO_DIEN_THOAI = 2;
    public static final int COT_EMAIL = 3;
    public static final int COT_DIA_CHI = 4;
    public static final int COT_HANG_THANH_VIEN = 5;

    // =========================================================================
    // 2. KHỞI TẠO SERVICE & KẾT NỐI DAO
    // =========================================================================
    private final KhachHang_DAO khachHangDAO;

    /**
     * Khởi tạo mặc định, tự động tạo mới đối tượng KhachHang_DAO kết nối database SALALA.
     */
    public KhachHangService() {
        this(new KhachHang_DAO());
    }

    /**
     * Khởi tạo kèm DAO (Dependency Injection phục vụ Unit Test hoặc dùng chung DAO).
     */
    public KhachHangService(KhachHang_DAO dao) {
        this.khachHangDAO = dao;
    }

    // Các hàm cung cấp cấu hình cột cho giao diện
    public String[] layDanhSachCot() {
        return DANH_SACH_COT.clone();
    }

    public String[] layDanhSachTuyChonLoc() {
        return TUY_CHON_LOC.clone();
    }

    public String layLuaChonLocMacDinh() {
        return TUY_CHON_LOC[0];
    }

    public int layViTriCotMaKhachHang() {
        return COT_MA_KH;
    }

    public int layViTriCotHoTen() {
        return COT_HO_TEN;
    }

    public int layViTriCotHangThanhVien() {
        return COT_HANG_THANH_VIEN;
    }

    /**
     * Kiểm tra điều kiện người dùng có đang chọn đúng duy nhất 1 dòng trong bảng không.
     * Dùng để bật/tắt (enable/disable) các nút Xem chi tiết, Chỉnh sửa, Xóa.
     */
    public boolean coTheThaoTacDuyNhat(int soDongDuocChon) {
        return soDongDuocChon == 1;
    }

    /**
     * Màu nền dòng xen kẽ để giao diện bảng hiển thị hiện đại, dễ nhìn.
     */
    public static Color layMauNenDong(int soDong) {
        return soDong % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);
    }

    /**
     * Màu chữ đại diện cho từng hạng thành viên trong cột bảng.
     */
    public static Color layMauHang(String tenHang) {
        if (tenHang == null) {
            return new Color(180, 83, 9); // Mặc định tông màu đồng
        }
        if (tenHang.contains("Kim cương")) {
            return new Color(2, 132, 199); // Xanh dương kim cương
        } else if (tenHang.contains("Vàng")) {
            return new Color(217, 119, 6);  // Vàng đậm
        } else if (tenHang.contains("Bạc")) {
            return new Color(100, 116, 139); // Bạc xám slate
        } else {
            return new Color(180, 83, 9);  // Đồng hổ phách
        }
    }

    // =========================================================================
    // 3. CHỨC NĂNG 1: TẢI & ĐỊNH DẠNG DỮ LIỆU HIỂN THỊ LÊN BẢNG (TABLE)
    // =========================================================================
    /**
     * Lấy toàn bộ danh sách thực thể KhachHang từ CSDL qua DAO.
     */
    public List<KhachHang> taiDanhSachKhachHang() {
        return java.util.Objects.requireNonNull(khachHangDAO.getAllKhachHangDayDu(),
                "Dữ liệu khách hàng không được trả về từ DAO");
    }

    /**
     * Tải và chuyển đổi danh sách khách hàng thành mảng Object 2 chiều để nạp vào DefaultTableModel.
     */
    public Object[][] taiDuLieuBang() {
        List<KhachHang> dsKhachHang = taiDanhSachKhachHang();
        return chuyenThanhDuLieuBang(dsKhachHang);
    }

    /**
     * Chuyển danh sách Entity thành ma trận dữ liệu hiển thị (Object[][]), xử lý giá trị null.
     */
    public Object[][] chuyenThanhDuLieuBang(List<KhachHang> dsKhachHang) {
        Object[][] duLieu = new Object[dsKhachHang.size()][6];
        for (int i = 0; i < dsKhachHang.size(); i++) {
            KhachHang kh = dsKhachHang.get(i);
            duLieu[i][0] = kh.getMaKH();
            duLieu[i][1] = kh.getHoTen();
            duLieu[i][2] = kh.getSdt();
            duLieu[i][3] = kh.getEmail();
            duLieu[i][4] = kh.getDiaChi() == null ? "" : kh.getDiaChi();
            duLieu[i][5] = kh.getHangThanhVien() == null ? "Đồng" : kh.getHangThanhVien();
        }
        return duLieu;
    }

    /**
     * Tạo biểu thức chính quy (Regex) để lọc bảng theo hạng thành viên đã chọn.
     * Nếu chọn "Tất cả" thì trả về null để hủy bộ lọc.
     */
    public String taoRegexLoc(String luaChon) {
        if (luaChon == null || luaChon.equals(TUY_CHON_LOC[0])) {
            return null;
        }
        return luaChon;
    }

    // =========================================================================
    // 4. CHỨC NĂNG 2: THỐNG KÊ SỐ LIỆU TỔNG QUAN (DASHBOARD CARDS)
    // =========================================================================
    /**
     * Thu thập 4 chỉ số thống kê từ CSDL thông qua DAO:
     * - Tổng số khách hàng trong toàn hệ thống.
     * - Tổng số thú cưng sở hữu.
     * - Số lượng thành viên đạt hạng Vàng và Kim Cương (khách VIP).
     * - Số lượng khách hàng mới đăng ký gần đây.
     * Trả về DTO ThongKeKhachHang có sẵn chuỗi hiển thị.
     */
    public ThongKeKhachHang layThongKe() {
        return new ThongKeKhachHang(
                khachHangDAO.getTongSoKhachHang(),
                khachHangDAO.getTongSoThuCung(),
                khachHangDAO.getSoLuongThanhVienVangTroLen(),
                khachHangDAO.getSoLuongKhachHangMoi());
    }

    // =========================================================================
    // 5. CHỨC NĂNG 3: THÊM KHÁCH HÀNG MỚI (KIỂM TRA DỮ LIỆU & LƯU)
    // =========================================================================
    /**
     * Tự động sinh mã khách hàng mới theo mẫu (KH001, KH002, ...) để điền sẵn vào Form.
     */
    public String phatSinhMaMoi() {
        return khachHangDAO.phatSinhMaKHMoi();
    }

    /**
     * Kiểm tra tính hợp lệ dữ liệu nhập (Validation Logic):
     * - Mã khách hàng: không rỗng.
     * - Họ và tên: không rỗng.
     * - Số điện thoại: đúng định dạng 10 chữ số bắt đầu bằng số 0.
     * - Email: đúng định dạng thư điện tử (ví dụ: ten@domain.com).
     */
    private KetQuaXuLyKhachHang kiemTraDauVao(String ma, String ten, String sdt, String email, boolean dangCapNhat) {
        if (ma == null || ma.trim().isEmpty()) {
            return new KetQuaXuLyKhachHang(false, true, TruongNhapLieu.MA_KHACH_HANG, "Vui lòng nhập mã khách hàng!");
        }
        if (ten == null || ten.trim().isEmpty()) {
            return new KetQuaXuLyKhachHang(false, true, TruongNhapLieu.HO_TEN,
                    dangCapNhat ? "Họ và tên không được để trống!" : "Vui lòng nhập họ và tên khách hàng!");
        }
        if (sdt == null || !sdt.trim().matches("^0\\d{9}$")) {
            return new KetQuaXuLyKhachHang(false, true, TruongNhapLieu.SO_DIEN_THOAI,
                    dangCapNhat ? "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!"
                            : "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (email == null || !email.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new KetQuaXuLyKhachHang(false, true, TruongNhapLieu.EMAIL,
                    dangCapNhat ? "Email không đúng định dạng!"
                            : "Email không đúng định dạng (ví dụ: ten@gmail.com)!");
        }
        return null;
    }

    /**
     * Quy trình thêm khách hàng hoàn chỉnh:
     * 1. Kiểm tra ràng buộc hợp lệ dữ liệu nhập.
     * 2. Gọi DAO tạo mới bản ghi trong CSDL.
     * 3. Trả về thông báo thành công hoặc thông báo lỗi cụ thể để GUI hiển thị.
     */
    public KetQuaXuLyKhachHang xuLyThemKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        KetQuaXuLyKhachHang loi = kiemTraDauVao(ma, ten, sdt, email, false);
        if (loi != null) {
            return loi;
        }
        KhachHang khachHangMoi = new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi != null ? diaChi.trim() : "");
        boolean thanhCong = khachHangDAO.createKhachHang(khachHangMoi);
        if (thanhCong) {
            return new KetQuaXuLyKhachHang(true, false, null, "Thêm khách hàng thành công!");
        }
        return new KetQuaXuLyKhachHang(false, false, null,
                "Thêm thất bại! Vui lòng kiểm tra xem Mã khách hàng đã tồn tại chưa.");
    }

    // =========================================================================
    // 6. CHỨC NĂNG 4: CHỈNH SỬA THÔNG TIN KHÁCH HÀNG
    // =========================================================================
    /**
     * Quy trình cập nhật thông tin khách hàng:
     * 1. Kiểm tra tính hợp lệ của dữ liệu đã sửa.
     * 2. Gọi DAO update thông tin khách hàng vào CSDL.
     * 3. Trả về kết quả kèm thông điệp phản hồi cho người dùng.
     */
    public KetQuaXuLyKhachHang xuLyCapNhatKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        KetQuaXuLyKhachHang loi = kiemTraDauVao(ma, ten, sdt, email, true);
        if (loi != null) {
            return loi;
        }
        KhachHang khachHangCapNhat = new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi != null ? diaChi.trim() : "");
        boolean thanhCong = khachHangDAO.updateKhachHang(khachHangCapNhat);
        if (thanhCong) {
            return new KetQuaXuLyKhachHang(true, false, null, "Cập nhật thông tin khách hàng thành công!");
        }
        return new KetQuaXuLyKhachHang(false, false, null, "Cập nhật thất bại!");
    }

    /**
     * Lấy thực thể KhachHang theo mã để nạp lên form chỉnh sửa.
     */
    public KhachHang layKhachHangTheoMa(String ma) {
        return khachHangDAO.getKhachHangTheoMa(ma);
    }

    public String layTieuDeDialogSua(String maKH) {
        return "Chỉnh sửa thông tin - " + maKH;
    }

    // =========================================================================
    // 7. CHỨC NĂNG 5: XÓA KHÁCH HÀNG AN TOÀN (RÀNG BUỘC TOÀN VẸN)
    // =========================================================================
    /**
     * Chuẩn bị thông điệp xác nhận trước khi xóa.
     */
    public String taoThongDiepXacNhanXoa(String hoTen, String maKH) {
        return "Bạn có chắc chắn muốn xóa khách hàng:\n" + hoTen + " (Mã: " + maKH + ") không?";
    }

    /**
     * Quy trình xóa an toàn (Đảm bảo toàn vẹn dữ liệu quan hệ):
     * 1. Kiểm tra ràng buộc khóa ngoại (Foreign Key) qua kiemTraRangBuoc():
     *    Nếu khách hàng đã có Thú cưng hoặc Hóa đơn giao dịch thì CHẶN XÓA để bảo vệ dữ liệu kế toán.
     * 2. Nếu khách hàng chưa có dữ liệu phụ thuộc -> Cho phép gọi DAO thực hiện xóa.
     * 3. Trả về kết quả và thông điệp tương ứng.
     */
    public KetQuaXoaKhachHang xuLyXoaKhachHang(String maKH) {
        if (khachHangDAO.kiemTraRangBuoc(maKH)) {
            return new KetQuaXoaKhachHang(false, true,
                    "Khách hàng " + maKH + " đang có dữ liệu thú cưng hoặc hóa đơn liên quan trong hệ thống.\n" +
                            "Để đảm bảo tính toàn vẹn dữ liệu kế toán, không thể xóa khách hàng này!");
        }
        boolean thanhCong = khachHangDAO.deleteKhachHang(maKH);
        if (thanhCong) {
            return new KetQuaXoaKhachHang(true, false, "Đã xóa khách hàng " + maKH + " thành công!");
        }
        return new KetQuaXoaKhachHang(false, false, "Xóa khách hàng thất bại!");
    }

    public String layThongDiepKhongTimThayKhachHang() {
        return "Không tìm thấy thông tin khách hàng!";
    }

    // =========================================================================
    // 8. CHỨC NĂNG 6: XEM HỒ SƠ CHI TIẾT (THÚ CƯNG & LỊCH SỬ HÓA ĐƠN)
    // =========================================================================
    /**
     * Lấy toàn bộ hồ sơ chi tiết của khách hàng bao gồm:
     * - Thông tin cá nhân (Mã, Tên, SĐT, Email, Địa chỉ).
     * - Danh sách toàn bộ thú cưng mà khách hàng đó sở hữu.
     * - Lịch sử toàn bộ các hóa đơn đã giao dịch tại cửa hàng.
     * Đóng gói vào DTO ChiTietKhachHang.
     */
    public ChiTietKhachHang layChiTiet(String ma) {
        KhachHang khachHang = khachHangDAO.getKhachHangTheoMa(ma);
        if (khachHang == null) {
            return null;
        }

        // Lấy danh sách thú cưng của khách từ CSDL
        List<DongThuCung> danhSachThuCung = new ArrayList<>();
        List<Object[]> rowsThuCung = khachHangDAO.getDanhSachThuCungCuaKhachHang(ma);
        if (rowsThuCung != null) {
            for (Object[] row : rowsThuCung) {
                danhSachThuCung.add(new DongThuCung(
                        (String) row[0], (String) row[1], (String) row[2],
                        (String) row[3], (Date) row[4], (String) row[5]));
            }
        }

        // Lấy danh sách hóa đơn giao dịch của khách từ CSDL
        List<DongHoaDon> danhSachHoaDon = new ArrayList<>();
        List<Object[]> rowsHoaDon = khachHangDAO.getDanhSachHoaDonCuaKhachHang(ma);
        if (rowsHoaDon != null) {
            for (Object[] row : rowsHoaDon) {
                danhSachHoaDon.add(new DongHoaDon(
                        (String) row[0], (Date) row[1], (String) row[2],
                        ((Number) row[3]).doubleValue(), (String) row[4]));
            }
        }

        return new ChiTietKhachHang(khachHang, danhSachThuCung, danhSachHoaDon);    }

    // =========================================================================
    // 9. CÁC LỚP ĐỐI TƯỢNG ĐÓNG GÓI DỮ LIỆU (DTO - DATA TRANSFER OBJECTS)
    // =========================================================================

    /**
     * Liệt kê các trường nhập liệu trong Form (dùng để trỏ chuột focus khi có lỗi nhập liệu).
     */
    public enum TruongNhapLieu {
        MA_KHACH_HANG, HO_TEN, SO_DIEN_THOAI, EMAIL,
        // Tương thích ngược với tên cũ nếu có
        CODE, NAME, PHONE
    }

    // Tương thích tên gọi cũ (giữ lại để tránh lỗi biên dịch nếu còn tham chiếu)
    public static final class TruongNhapLieuCu {
        public static final TruongNhapLieu MA = TruongNhapLieu.MA_KHACH_HANG;
        public static final TruongNhapLieu TEN = TruongNhapLieu.HO_TEN;
        public static final TruongNhapLieu SDT = TruongNhapLieu.SO_DIEN_THOAI;
        public static final TruongNhapLieu EMAIL = TruongNhapLieu.EMAIL;
    }

    /**
     * DTO kết quả của thao tác Thêm / Sửa khách hàng.
     */
    public static final class KetQuaXuLyKhachHang {
        private final boolean thanhCong;
        private final boolean loiNhapLieu;
        private final TruongNhapLieu truongLoi;
        private final String thongBao;

        public KetQuaXuLyKhachHang(boolean thanhCong, boolean loiNhapLieu, TruongNhapLieu truongLoi, String thongBao) {
            this.thanhCong = thanhCong;
            this.loiNhapLieu = loiNhapLieu;
            this.truongLoi = truongLoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isLoiNhapLieu() { return loiNhapLieu; }
        public TruongNhapLieu getTruongLoi() { return truongLoi; }
        public String getThongBao() { return thongBao; }
    }

    /**
     * DTO kết quả của thao tác Xóa khách hàng.
     */
    public static final class KetQuaXoaKhachHang {
        private final boolean thanhCong;
        private final boolean biRangBuoc;
        private final String thongBao;

        public KetQuaXoaKhachHang(boolean thanhCong, boolean biRangBuoc, String thongBao) {
            this.thanhCong = thanhCong;
            this.biRangBuoc = biRangBuoc;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isBiRangBuoc() { return biRangBuoc; }
        public String getThongBao() { return thongBao; }
    }

    /**
     * DTO lưu trữ số liệu 4 thẻ thống kê khách hàng phía trên màn hình.
     */
    public static class ThongKeKhachHang {
        public final int tongKhachHang;
        public final int tongThuCung;
        public final int thanhVienVangTroLen;
        public final int khachHangMoi;
        public final String chuoiMoTaThuCungTrungBinh;

        public ThongKeKhachHang(int tongKhachHang, int tongThuCung, int thanhVienVangTroLen, int khachHangMoi) {
            this.tongKhachHang = tongKhachHang;
            this.tongThuCung = tongThuCung;
            this.thanhVienVangTroLen = thanhVienVangTroLen;
            this.khachHangMoi = khachHangMoi;
            this.chuoiMoTaThuCungTrungBinh = tongKhachHang > 0
                    ? String.format(Locale.US, "%.1f thú cưng / khách", (double) tongThuCung / tongKhachHang)
                    : "0.0 thú cưng / khách";
        }

        public String layChuoiTongKhachHang() { return String.valueOf(tongKhachHang); }
        public String layChuoiKhachHangMoi() { return String.valueOf(khachHangMoi); }
        public String layChuoiTongThuCung() { return String.valueOf(tongThuCung); }
        public String layChuoiThanhVienVang() { return String.valueOf(thanhVienVangTroLen); }
        public String layChuoiMoTaThuCung() { return chuoiMoTaThuCungTrungBinh; }
    }

    // Bí danh tương thích lớp cũ — đổi tên tiếng Việt
    public static class ThongKeKhachHangCu extends ThongKeKhachHang {
        public ThongKeKhachHangCu(int tongKhachHang, int tongThuCung, int thanhVienVangTroLen, int khachHangMoi) {
            super(tongKhachHang, tongThuCung, thanhVienVangTroLen, khachHangMoi);
        }
    }

    /**
     * DTO chứa toàn bộ dữ liệu hồ sơ chi tiết của một khách hàng (Thông tin + Thú cưng + Hóa đơn).
     */
    public static class ChiTietKhachHang {
        public static final String[] COT_THU_CUNG = {
                "Mã TC", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh", "Đặc điểm"
        };
        public static final String[] COT_HOA_DON = {
                "Mã HĐ", "Ngày lập", "Trạng thái", "Số tiền (₫)", "Hình thức"
        };

        public final KhachHang khachHang;
        public final List<DongThuCung> danhSachThuCung;
        public final List<DongHoaDon> danhSachHoaDon;

        public ChiTietKhachHang(KhachHang khachHang, List<DongThuCung> danhSachThuCung, List<DongHoaDon> danhSachHoaDon) {
            this.khachHang = khachHang;
            this.danhSachThuCung = danhSachThuCung;
            this.danhSachHoaDon = danhSachHoaDon;
        }

        public Object[][] layDuLieuBangThuCung() {
            Object[][] duLieu = new Object[danhSachThuCung.size()][6];
            for (int i = 0; i < danhSachThuCung.size(); i++) {
                DongThuCung tc = danhSachThuCung.get(i);
                duLieu[i] = new Object[] {
                        tc.ma, tc.ten, tc.loai, tc.gioiTinh, tc.ngaySinh, tc.dacDiem
                };
            }
            return duLieu;
        }

        public Object[][] layDuLieuBangHoaDon() {
            Object[][] duLieu = new Object[danhSachHoaDon.size()][5];
            for (int i = 0; i < danhSachHoaDon.size(); i++) {
                DongHoaDon hd = danhSachHoaDon.get(i);
                duLieu[i][0] = hd.ma;
                duLieu[i][1] = hd.ngayLap;
                duLieu[i][2] = hd.trangThai;
                duLieu[i][3] = String.format(Locale.US, "%,.0f ₫", hd.soTien).replace(',', '.');
                duLieu[i][4] = hd.hinhThucThanhToan;
            }
            return duLieu;
        }

        public String layDiaChiHienThi() {
            return (khachHang != null && khachHang.getDiaChi() != null && !khachHang.getDiaChi().trim().isEmpty())
                    ? khachHang.getDiaChi() : "Chưa cập nhật";
        }

        public String layTieuDeDialogChiTiet() {
            return "Hồ Sơ Chi Tiết Khách Hàng - " + (khachHang != null ? khachHang.getMaKH() : "");
        }

        public String layThongTinMaKH() {
            return "Mã KH: " + (khachHang != null ? khachHang.getMaKH() : "");
        }

        public String layThongTinHoTen() {
            return "Họ tên: " + (khachHang != null ? khachHang.getHoTen() : "");
        }

        public String layThongTinSDT() {
            return "Điện thoại: " + (khachHang != null ? khachHang.getSdt() : "");
        }

        public String layThongTinEmail() {
            return "Email: " + (khachHang != null ? khachHang.getEmail() : "");
        }

        public String layThongTinDiaChi() {
            return "Địa chỉ: " + layDiaChiHienThi();
        }

        public String layTieuDeThuCung() {
            return " Thú cưng sở hữu (" + danhSachThuCung.size() + ") ";
        }

        public String layTieuDeHoaDon() {
            return " Lịch sử hóa đơn giao dịch (" + danhSachHoaDon.size() + ") ";
        }
    }

    // Bí danh tương thích lớp cũ — đổi tên tiếng Việt
    public static class ChiTietKhachHangCu extends ChiTietKhachHang {
        public ChiTietKhachHangCu(KhachHang khachHang, List<DongThuCung> danhSachThuCung, List<DongHoaDon> danhSachHoaDon) {
            super(khachHang, danhSachThuCung, danhSachHoaDon);
        }
    }

    /**
     * DTO mô tả 1 dòng trong bảng thú cưng của khách hàng.
     */
    public static final class DongThuCung {
        public final String ma;
        public final String ten;
        public final String loai;
        public final String gioiTinh;
        public final Date ngaySinh;
        public final String dacDiem;

        public DongThuCung(String ma, String ten, String loai, String gioiTinh, Date ngaySinh, String dacDiem) {
            this.ma = ma;
            this.ten = ten;
            this.loai = loai;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.dacDiem = dacDiem;
        }
    }

    // =========================================================================
    // 10. CHỨC NĂNG 7: XUẤT DANH SÁCH KHÁCH HÀNG RA FILE CSV
    // =========================================================================

    /**
     * Trả về tên file CSV mặc định khi người dùng mở hộp thoại lưu.
     */
    public String layTenFileCSVMacDinh() {
        return "DanhSachKhachHang.csv";
    }

    /**
     * Xuất toàn bộ dữ liệu hiện tại trên bảng (JTable) ra file CSV.
     * - Tôn trọng bộ lọc / sắp xếp đang áp dụng trên bảng (dùng getValueAt / getRowCount).
     * - Ghi UTF-8 BOM (\uFEFF) ở đầu file để Excel hiển thị đúng tiếng Việt có dấu.
     * - Mỗi ô được bọc ngoặc kép; dấu ngoặc kép bên trong được nhân đôi (chuẩn CSV RFC 4180).
     *
     * @param file  File đích người dùng đã chọn qua JFileChooser
     * @param bang  JTable đang hiển thị (đã áp dụng filter/sorter)
     * @return KetQuaXuatFile chứa trạng thái thành công và thông báo
     */
    public KetQuaXuatFile xuatDanhSachKhachHangRaCSV(java.io.File file, javax.swing.JTable bang) {
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {

            // Bước 1: Ghi UTF-8 BOM để Excel mở đúng tiếng Việt
            writer.write('\uFEFF');

            // Bước 2: Ghi hàng tiêu đề cột
            int soCot = bang.getColumnCount();
            for (int c = 0; c < soCot; c++) {
                String tenCot = bang.getColumnName(c).replace("\"", "\"\"");
                writer.write("\"" + tenCot + "\"" + (c < soCot - 1 ? "," : ""));
            }
            writer.write("\r\n");

            // Bước 3: Ghi từng dòng dữ liệu (theo thứ tự hiển thị sau bộ lọc)
            int soDong = bang.getRowCount();
            for (int r = 0; r < soDong; r++) {
                for (int c = 0; c < soCot; c++) {
                    Object giaTri = bang.getValueAt(r, c);
                    String chuoi = (giaTri != null) ? giaTri.toString().replace("\"", "\"\"") : "";
                    writer.write("\"" + chuoi + "\"" + (c < soCot - 1 ? "," : ""));
                }
                writer.write("\r\n");
            }

            return new KetQuaXuatFile(true,
                    "Xuất file thành công!\nĐường dẫn: " + file.getAbsolutePath(),
                    file.getAbsolutePath());

        } catch (Exception ex) {
            return new KetQuaXuatFile(false,
                    "Lỗi khi xuất file: " + ex.getMessage(),
                    null);
        }
    }

    /**
     * DTO kết quả trả về sau khi thực hiện xuất file CSV.
     */
    public static final class KetQuaXuatFile {
        private final boolean thanhCong;
        private final String thongBao;
        private final String duongDanFile;

        public KetQuaXuatFile(boolean thanhCong, String thongBao, String duongDanFile) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.duongDanFile = duongDanFile;
        }

        /** Trả về true nếu xuất file thành công */
        public boolean isThanhCong() { return thanhCong; }

        /** Thông báo kết quả hiển thị cho người dùng */
        public String getThongBao() { return thongBao; }

        /** Đường dẫn tuyệt đối của file đã xuất (null nếu thất bại) */
        public String getDuongDanFile() { return duongDanFile; }
    }

    /**
     * DTO mô tả 1 dòng trong bảng lịch sử hóa đơn của khách hàng.
     */
    public static final class DongHoaDon {
        public final String ma;
        public final Date ngayLap;
        public final String trangThai;
        public final double soTien;
        public final String hinhThucThanhToan;

        public DongHoaDon(String ma, Date ngayLap, String trangThai, double soTien, String hinhThucThanhToan) {
            this.ma = ma;
            this.ngayLap = ngayLap;
            this.trangThai = trangThai;
            this.soTien = soTien;
            this.hinhThucThanhToan = hinhThucThanhToan;
        }
    }
}
