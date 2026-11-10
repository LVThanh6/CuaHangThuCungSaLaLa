package Service.KhachHang;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dao.KhachHang.HangThanhVien_DAO;
import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.HangThanhVien;
import entity.KhachHang.KhachHang;

/**
 * ============================================================================
 * LỚP DỊCH VỤ NGHIỆP VỤ: HangThanhVienService (Quản lý hạng thành viên)
 * ============================================================================
 * - Đảm nhận toàn bộ Logic nghiệp vụ (Business Logic) liên quan đến hạng hội
 * viên.
 * - Đứng giữa giao diện QuanLyHangThanhVienGUI và cơ sở dữ liệu
 * (HangThanhVien_DAO, KhachHang_DAO).
 * - Tự động đồng bộ và tính toán: điểm tích lũy theo tổng chi tiêu, tỷ lệ %
 * khách,
 * quyền lợi đặc quyền, kiểm tra ràng buộc nhập liệu chính sách hạng.
 *
 * MỤC LỤC CÁC CHỨC NĂNG THUYẾT TRÌNH:
 * 1. HẰNG SỐ CẤU HÌNH BẢNG & TÊN CÁC HẠNG THÀNH VIÊN
 * 2. KHỞI TẠO SERVICE & KẾT NỐI DAO
 * 3. CHỨC NĂNG 1: THỐNG KÊ TỔNG QUAN HẠNG THÀNH VIÊN (DASHBOARD)
 * 4. CHỨC NĂNG 2: QUẢN LÝ & TẠO 4 THẺ HẠNG THÀNH VIÊN (TIER CARDS)
 * 5. CHỨC NĂNG 3: HIỂN THỊ & TÌM KIẾM THÀNH VIÊN THEO TỪNG HẠNG
 * 6. CHỨC NĂNG 4: CHUYỂN NHANH HẠNG THÀNH VIÊN (TÍNH LẠI ĐIỂM)
 * 7. CHỨC NĂNG 5: CẤU HÌNH & CẬP NHẬT CHÍNH SÁCH ƯU ĐÃI
 * 8. CHỨC NĂNG 6: GÁN / PHÂN HẠNG CHO KHÁCH HÀNG ĐÃ ĐĂNG KÝ
 * 9. CÁC HÀM TIỆN ÍCH ĐỊNH DẠNG TIỀN TỆ & KHOẢNG CHI TIÊU
 * 10. CÁC LỚP ĐỐI TƯỢNG ĐÓNG GÓI DỮ LIỆU & KẾT QUẢ (DTOs)
 * ============================================================================
 */
public class HangThanhVienService {

    // =========================================================================
    // 1. HẰNG SỐ CẤU HÌNH BẢNG & TÊN CÁC HẠNG THÀNH VIÊN
    // =========================================================================
    // Tiêu đề các cột trong bảng danh sách thành viên thuộc từng hạng
    public static final String[] DANH_SACH_COT_THANH_VIEN = {
            "Mã hạng", "Mã KH", "Họ và tên", "Số điện thoại", "Email", "Số thú cưng",
            "Tổng chi tiêu", "Điểm tích lũy", "Ngày cập nhật"
    };

    // 4 nhóm hạng thành viên chính thức của hệ thống SALALA
    public static final String[] DANH_SACH_TEN_HANG = { "Đồng", "Bạc", "Vàng", "Kim cương" };

    // Vị trí các cột dữ liệu trong JTable (tránh hardcode ở tầng giao diện)
    public static final int COT_MA_HANG = 0;
    public static final int COT_MA_KH = 1;
    public static final int COT_HO_TEN = 2;
    public static final int COT_SO_DIEN_THOAI = 3;
    public static final int COT_EMAIL = 4;
    public static final int COT_SO_THU_CUNG = 5;
    public static final int COT_TONG_CHI_TIEU = 6;
    public static final int COT_DIEM_TICH_LUY = 7;
    public static final int COT_NGAY_CAP_NHAT = 8;

    // =========================================================================
    // 2. KHỞI TẠO SERVICE & KẾT NỐI DAO
    // =========================================================================
    private final HangThanhVien_DAO hangThanhVienDAO;
    private final KhachHang_DAO khachHangDAO;
    private final KhachHangService khachHangService;

    /**
     * Khởi tạo mặc định, tự động kết nối các DAO tương ứng.
     */
    public HangThanhVienService() {
        this(new HangThanhVien_DAO(), new KhachHang_DAO());
    }

    /**
     * Khởi tạo với DAO được chỉ định sẵn.
     */
    public HangThanhVienService(HangThanhVien_DAO tierDao, KhachHang_DAO customerDao) {
        this(tierDao, customerDao, new KhachHangService(customerDao));
    }

    /**
     * Khởi tạo đầy đủ phục vụ Dependency Injection.
     */
    public HangThanhVienService(HangThanhVien_DAO tierDao, KhachHang_DAO customerDao,
            KhachHangService khachHangService) {
        this.hangThanhVienDAO = tierDao;
        this.khachHangDAO = customerDao;
        this.khachHangService = khachHangService;
    }

    // Các hàm cung cấp cấu hình cột cho giao diện
    public String[] layDanhSachCotThanhVien() {
        return DANH_SACH_COT_THANH_VIEN.clone();
    }

    public String[] layDanhSachTenHang() {
        return DANH_SACH_TEN_HANG.clone();
    }

    public int layViTriCotMaHang() {
        return COT_MA_HANG;
    }

    public int layViTriCotMaKHThanhVien() {
        return COT_MA_KH;
    }

    public int layViTriCotHoTenThanhVien() {
        return COT_HO_TEN;
    }

    public int layViTriCotChiTieuThanhVien() {
        return COT_TONG_CHI_TIEU;
    }

    public int layViTriCotDiemTichLuy() {
        return COT_DIEM_TICH_LUY;
    }

    public String layTieuDeDialogDanhSachThanhVien(String tenHang, int soLuong) {
        return "Danh Sách Thành Viên - Hạng " + tenHang + " (" + soLuong + " thành viên)";
    }

    public String layTieuDeNhomKhachHang(String tenHang) {
        return "Nhóm khách hàng đạt Hạng " + tenHang + " (Từ bảng QuanLyHangThanhVien)";
    }

    public String layThongBaoChuaChonKhachHangChuyenHang() {
        return "Vui lòng chọn khách hàng cần chuyển hạng trong bảng!";
    }

    public String layNhanKhachHangChuyenHang(String hoTen, String maKH) {
        return "Khách hàng: " + hoTen + " (" + maKH + ")";
    }

    /**
     * Trích xuất thông tin khách hàng được chọn trong bảng JTable (Mã hạng, Mã KH,
     * Họ tên).
     */
    public ThongTinDongThanhVien trichXuatThongTinDong(javax.swing.table.TableModel model, int modelRow) {
        String maHang = String.valueOf(model.getValueAt(modelRow, COT_MA_HANG));
        String maKH = String.valueOf(model.getValueAt(modelRow, COT_MA_KH));
        String hoTen = String.valueOf(model.getValueAt(modelRow, COT_HO_TEN));
        return new ThongTinDongThanhVien(maHang, maKH, hoTen);
    }

    // =========================================================================
    // 3. CHỨC NĂNG 1: THỐNG KÊ TỔNG QUAN HẠNG THÀNH VIÊN (DASHBOARD)
    // =========================================================================
    /**
     * Thu thập 4 chỉ số thống kê tổng thể về hội viên:
     * - Tổng số lượng khách hàng đã phân hạng.
     * - Số lượt thành viên được nâng hạng gần đây.
     * - Tổng chi tiêu của toàn bộ hội viên (tính theo doanh số tích lũy).
     * - Tổng tiền chiết khấu / giảm giá đã áp dụng.
     * Trả về DTO ThongKeHangThanhVien (Dashboard).
     */
    public ThongKeHangThanhVien layThongKeTongQuan() {
        List<HangThanhVien> dsHang = hangThanhVienDAO.getAllHangThanhVien();
        return new ThongKeHangThanhVien(
                hangThanhVienDAO.getTongSoThanhVien(),
                hangThanhVienDAO.getSoLuongNangHangGanDay(),
                hangThanhVienDAO.getTongChiTieuThanhVien(),
                hangThanhVienDAO.getTongGiamGiaDaApDung(),
                dsHang);
    }

    // =========================================================================
    // 4. CHỨC NĂNG 2: QUẢN LÝ & TẠO 4 THẺ HẠNG THÀNH VIÊN (TIER CARDS)
    // =========================================================================
    /**
     * Tạo danh sách 4 thẻ hạng trực quan (Đồng, Bạc, Vàng, Kim Cương):
     * - Tính toán tỷ lệ % số khách hàng của từng hạng trên tổng số khách.
     * - Định dạng chuỗi điều kiện chi tiêu (Từ ... đến ...).
     * - Gán màu sắc đặc trưng (màu viền đỉnh, màu nền header thẻ).
     * - Trích xuất danh sách các quyền lợi đặc quyền của từng hạng từ CSDL.
     * GUI chỉ việc duyệt danh sách model này để render giao diện thẻ.
     */
    public List<TheHangThanhVienModel> layDanhSachTheHang(ThongKeHangThanhVien thongKe) {
        List<TheHangThanhVienModel> dsThe = new ArrayList<>();
        int tongKH = thongKe.tongThanhVien;
        List<HangThanhVien> dsHang = thongKe.danhSachHang;

        // Bảng màu chuẩn phân biệt 4 hạng
        Color[] mauTops = {
                new Color(245, 158, 11), // Đồng: Hổ phách (Amber)
                new Color(100, 116, 139), // Bạc: Xám Slate bạc
                new Color(217, 119, 6), // Vàng: Vàng kim Gold đậm
                new Color(2, 132, 199) // Kim cương: Xanh Sky blue
        };
        Color[] mauNens = {
                new Color(254, 243, 199),
                new Color(241, 245, 249),
                new Color(254, 240, 138),
                new Color(224, 242, 254)
        };

        for (int i = 0; i < dsHang.size(); i++) {
            HangThanhVien h = dsHang.get(i);
            int soLuong = h.getSoLuongThanhVien();
            double tiLe = tongKH > 0 ? (soLuong * 100.0 / tongKH) : 0.0;
            String dieuKien = dinhDangKhoangChiTieu(h.getChiTieuToiThieu(), h.getChiTieuToiDa());
            String strGiam = String.format(Locale.US, "%.0f%%", h.getMucGiamGia());
            String strSoKhach = soLuong + " khách hàng";
            String strTiLe = String.format(Locale.US, "%.1f%% tổng khách", tiLe);

            List<String> qlList = h.getDanhSachQuyenLoi();
            String[] arrQL = qlList.toArray(new String[0]);

            Color mauTop = i < mauTops.length ? mauTops[i] : new Color(79, 70, 229);
            Color mauNen = i < mauNens.length ? mauNens[i] : new Color(243, 244, 246);

            dsThe.add(new TheHangThanhVienModel(
                    h.getTenHang(), dieuKien, strGiam, strSoKhach, strTiLe, arrQL, mauTop, mauNen));
        }
        return dsThe;
    }

    public List<HangThanhVien> layTatCaHangThanhVien() {
        return java.util.Objects.requireNonNull(hangThanhVienDAO.getAllHangThanhVien(),
                "Dữ liệu hạng thành viên không được trả về từ DAO");
    }

    // =========================================================================
    // 5. CHỨC NĂNG 3: HIỂN THỊ & TÌM KIẾM THÀNH VIÊN THEO TỪNG HẠNG
    // =========================================================================
    /**
     * Lấy danh sách thành viên thực tế thuộc 1 hạng cụ thể (VD: nhóm khách hàng
     * hạng Vàng).
     */
    public List<HangThanhVien> layDanhSachThanhVienTheoHang(String tenHang) {
        return java.util.Objects.requireNonNull(hangThanhVienDAO.getDanhSachThanhVienTheoHang(tenHang),
                "Dữ liệu thành viên không được trả về từ DAO");
    }

    /**
     * Tải và chuyển danh sách thành viên của hạng thành mảng Object 2 chiều cho
     * JTable.
     */
    public Object[][] taiDuLieuBangThanhVien(String tenHang) {
        List<HangThanhVien> ds = layDanhSachThanhVienTheoHang(tenHang);
        return chuyenThanhDuLieuBangThanhVien(ds);
    }

    /**
     * Định dạng chi tiết từng cột trong bảng thành viên (tiền tệ, ngày cập nhật, số
     * bé cưng).
     */
    public Object[][] chuyenThanhDuLieuBangThanhVien(List<HangThanhVien> ds) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Object[][] duLieu = new Object[ds.size()][9];
        for (int i = 0; i < ds.size(); i++) {
            HangThanhVien h = ds.get(i);
            duLieu[i][0] = h.getMaHang();
            duLieu[i][1] = h.getMaKH() != null ? h.getMaKH().getMaKH() : "";
            duLieu[i][2] = h.getHoTenKhachHang();
            duLieu[i][3] = h.getSdtKhachHang();
            duLieu[i][4] = h.getEmailKhachHang();
            duLieu[i][5] = h.getSoThuCung() + " bé";
            duLieu[i][6] = String.format(Locale.US, "%,.0f ₫", h.getTongChiTieu()).replace(',', '.');
            duLieu[i][7] = h.getDiemTichLuy() + " điểm";
            duLieu[i][8] = h.getNgayCapNhatHang() != null ? sdf.format(h.getNgayCapNhatHang()) : "-";
        }
        return duLieu;
    }

    /**
     * Tạo biểu thức chính quy (Regex) không phân biệt hoa thường để tìm kiếm thành
     * viên trong bảng.
     */
    public String taoRegexTimKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return null;
        }
        return "(?i)" + java.util.regex.Pattern.quote(tuKhoa.trim());
    }

    /**
     * Cho phép xem nhanh toàn bộ hồ sơ khách hàng (thú cưng & hóa đơn) ngay tại
     * bảng thành viên.
     */
    public KhachHangService.ChiTietKhachHang layChiTietKhachHang(String maKH) {
        return khachHangService.layChiTiet(maKH);
    }

    // =========================================================================
    // 6. CHỨC NĂNG 4: CHUYỂN NHANH HẠNG THÀNH VIÊN (TÍNH LẠI ĐIỂM)
    // =========================================================================
    /**
     * Quy trình chuyển nhanh hạng cho một thành viên:
     * 1. Kiểm tra xem người dùng có chọn trùng với hạng hiện tại không (nếu trùng
     * -> không làm gì).
     * 2. Lấy thông tin chính sách của hạng mới.
     * 3. Tính toán lại Điểm tích lũy dựa trên Tổng chi tiêu thực tế và tỷ lệ
     * Điểm/10.000đ của hạng mới.
     * 4. Cập nhật thời điểm chuyển hạng (Timestamp hiện tại).
     * 5. Lưu vào CSDL và trả về thông báo thành công.
     */
    public KetQuaChuyenHang xuLyChuyenHang(String maHang, String maKH, String hoTen, String hangHienTai,
            String hangMoi) {
        if (hangMoi == null || hangMoi.equals(hangHienTai)) {
            return new KetQuaChuyenHang(false, false, "Hạng thành viên không thay đổi.");
        }
        boolean updated = ganHangThanhVien(maHang, maKH, hangMoi);
        if (updated) {
            return new KetQuaChuyenHang(true, true,
                    "Chuyển khách hàng " + hoTen + " sang hạng " + hangMoi + " thành công!");
        } else {
            return new KetQuaChuyenHang(false, true,
                    "Không thể cập nhật hạng thành viên. Vui lòng kiểm tra kết nối!");
        }
    }

    /**
     * Hàm nội bộ thực hiện cập nhật phân hạng và tính lại điểm trong CSDL.
     * - Tính tổng chi tiêu THỰC TẾ từ hóa đơn đã thanh toán (không dùng giá trị cũ trong bảng).
     * - Tính lại điểm theo tỷ lệ của hạng mới.
     */
    public boolean ganHangThanhVien(String maHang, String maKH, String tenHangMoi) {
        HangThanhVien hangMucTieu = timHangTheoTen(tenHangMoi);
        if (hangMucTieu == null) {
            return false;
        }

        // Luôn tính lại từ hóa đơn thực tế, không lấy giá trị cũ trong bảng
        double tongChiTieuThucTe = hangThanhVienDAO.tinhTongChiTieuTuHoaDon(maKH);
        int diemTichLuy = tinhDiemTichLuy(tongChiTieuThucTe, hangMucTieu.getDiemTichLuy1Van());

        HangThanhVien hangCapNhat = new HangThanhVien();
        hangCapNhat.setMaHang(maHang);
        hangCapNhat.setMaKH(maKH);
        dongBoChinhSachHang(hangCapNhat, hangMucTieu);
        hangCapNhat.setTongChiTieu(tongChiTieuThucTe);
        hangCapNhat.setDiemTichLuy(diemTichLuy);
        hangCapNhat.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
        hangCapNhat.setTrangThai("Đang áp dụng");

        return hangThanhVienDAO.capNhatHangThanhVien(hangCapNhat);
    }

    // =========================================================================
    // 7. CHỨC NĂNG 5: CẤU HÌNH & CẬP NHẬT CHÍNH SÁCH ƯU ĐÃI
    // =========================================================================
    /**
     * Nạp dữ liệu chính sách hiện tại của hạng (chi tiêu min/max, % giảm giá, quyền
     * lợi)
     * vào DTO DuLieuFormChinhSachHang để điền sẵn vào các ô nhập liệu Form.
     */
    public DuLieuFormChinhSachHang layDuLieuFormChinhSach(String tenHang) {
        HangThanhVien tier = timHangTheoTen(tenHang);
        if (tier == null) {
            return new DuLieuFormChinhSachHang("", "", "", "", "");
        }
        String min = String.format(Locale.US, "%.0f", tier.getChiTieuToiThieu());
        String max = tier.getChiTieuToiDa() != null
                ? String.format(Locale.US, "%.0f", tier.getChiTieuToiDa())
                : "";
        String discount = String.format(Locale.US, "%.1f", tier.getMucGiamGia());
        String points = String.format(Locale.US, "%.1f", tier.getDiemTichLuy1Van());
        String benefits = tier.getQuyenLoi() != null ? tier.getQuyenLoi() : "";
        return new DuLieuFormChinhSachHang(min, max, discount, points, benefits);
    }

    /**
     * Quy trình lưu chính sách ưu đãi của hạng:
     * 1. Kiểm tra Chi tiêu tối thiểu: phải là số >= 0.
     * 2. Kiểm tra Chi tiêu tối đa: nếu có nhập thì phải > Chi tiêu tối thiểu.
     * 3. Kiểm tra Mức giảm giá: phải nằm trong khoảng 0% - 100%.
     * 4. Kiểm tra Điểm tích lũy: phải là số >= 0.
     * 5. Lưu vào CSDL và trả về thông báo chi tiết cho người quản trị.
     */
    public KetQuaCapNhatChinhSach xuLyCapNhatChinhSach(String tenHang, String minText, String maxText,
            String giamGiaText,
            String diemText, String quyenLoi) {
        Double minimum = chuyenChuoiSangSo(minText);
        if (minimum == null || minimum < 0) {
            return new KetQuaCapNhatChinhSach(false, true, TruongChinhSach.CHI_TIEU_TOI_THIEU,
                    "Chi tiêu tối thiểu phải là số >= 0!");
        }

        Double maximum = null;
        if (maxText != null && !maxText.trim().isEmpty()) {
            maximum = chuyenChuoiSangSo(maxText);
            if (maximum == null) {
                return new KetQuaCapNhatChinhSach(false, true, TruongChinhSach.CHI_TIEU_TOI_DA,
                        "Chi tiêu tối đa phải là số hợp lệ!");
            }
            if (maximum <= minimum) {
                return new KetQuaCapNhatChinhSach(false, true, TruongChinhSach.CHI_TIEU_TOI_DA,
                        "Chi tiêu tối đa phải lớn hơn chi tiêu tối thiểu!");
            }
        }

        Double discount = chuyenChuoiSangSo(giamGiaText);
        if (discount == null || discount < 0 || discount > 100) {
            return new KetQuaCapNhatChinhSach(false, true, TruongChinhSach.MUC_GIAM_GIA,
                    "Mức giảm giá phải từ 0% đến 100%!");
        }

        Double points = chuyenChuoiSangSo(diemText);
        if (points == null || points < 0) {
            return new KetQuaCapNhatChinhSach(false, true, TruongChinhSach.DIEM_TICH_LUY,
                    "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        }

        boolean saved = hangThanhVienDAO.capNhatChinhSachHang(tenHang, minimum, maximum, discount, points,
                quyenLoi != null ? quyenLoi.trim() : "");
        if (saved) {
            String thongBao = "Đã lưu chính sách hạng \"" + tenHang + "\" thành công vào database SALALA!\n" +
                    "- Chi tiêu: từ " + String.format(Locale.US, "%,.0f ₫", minimum).replace(',', '.')
                    + (maximum != null ? " đến " + String.format(Locale.US, "%,.0f ₫", maximum).replace(',', '.')
                            : " trở lên")
                    + "\n" +
                    "- Giảm giá: " + discount + "% | Điểm/10.000₫: " + points;
            return new KetQuaCapNhatChinhSach(true, false, null, thongBao);
        } else {
            return new KetQuaCapNhatChinhSach(false, false, null,
                    "Không thể cập nhật chính sách hạng. Vui lòng thử lại!");
        }
    }

    // =========================================================================
    // 8. CHỨC NĂNG 6: GÁN / PHÂN HẠNG CHO KHÁCH HÀNG ĐÃ ĐĂNG KÝ
    // =========================================================================
    /**
     * Lấy danh sách khách hàng để hiển thị lên ComboBox chọn khách hàng: "KH001 -
     * Nguyễn Văn A".
     */
    public List<KhachHang> layDanhSachKhachHang() {
        return java.util.Objects.requireNonNull(khachHangDAO.getAlltbKhachHang(),
                "Dữ liệu khách hàng không được trả về từ DAO");
    }

    public List<String> layDanhSachHienThiKhachHang() {
        List<KhachHang> ds = layDanhSachKhachHang();
        List<String> ketQua = new ArrayList<>();
        for (KhachHang kh : ds) {
            ketQua.add(kh.getMaKH() + " - " + kh.getHoTen());
        }
        return ketQua;
    }

    /**
     * Tách mã khách hàng từ chuỗi ComboBox "KH001 - Tên" và gọi gán hạng.
     */
    public boolean ganHangTheoLuaChon(String chuoiChon, String tenHangMoi) {
        if (chuoiChon == null || !chuoiChon.contains(" - ")) {
            return false;
        }
        String maKH = chuoiChon.split(" - ")[0].trim();
        return ganHoacTaoHangThanhVien(maKH, tenHangMoi);
    }

    /**
     * Xử lý gán hạng hoặc tạo mới bản ghi hạng hội viên nếu khách chưa từng có
     * hạng.
     */
    public KetQuaPhanHang xuLyPhanHangKhachHang(int soLuongKhachHang, String khSelected, String tenHangMoi) {
        if (soLuongKhachHang <= 0 || khSelected == null || khSelected.trim().isEmpty()) {
            return new KetQuaPhanHang(false, true, "Không có khách hàng nào trong hệ thống!");
        }
        boolean ok = ganHangTheoLuaChon(khSelected, tenHangMoi);
        if (ok) {
            return new KetQuaPhanHang(true, false,
                    "Phân khách hàng " + khSelected + " sang hạng " + tenHangMoi + " thành công!");
        } else {
            return new KetQuaPhanHang(false, false, "Không thể lưu phân hạng. Vui lòng thử lại!");
        }
    }

    /**
     * Gán hạng thành viên: nếu khách đã có hồ sơ thì cập nhật, nếu chưa có thì thêm
     * mới.
     * - Tính tổng chi tiêu THỰC TẾ từ hóa đơn đã thanh toán trong CSDL.
     * - Tính lại điểm tích lũy theo tỷ lệ của hạng mới.
     */
    public boolean ganHoacTaoHangThanhVien(String maKH, String tenHangMoi) {
        HangThanhVien hangMucTieu = timHangTheoTen(tenHangMoi);
        if (hangMucTieu == null) {
            return false;
        }

        // Lấy tổng chi tiêu THỰC TẾ từ hóa đơn đã thanh toán
        double tongChiTieuThucTe = hangThanhVienDAO.tinhTongChiTieuTuHoaDon(maKH);
        int diemTichLuy = tinhDiemTichLuy(tongChiTieuThucTe, hangMucTieu.getDiemTichLuy1Van());

        HangThanhVien hangHienTai = hangThanhVienDAO.getHangThanhVienTheoMaKH(maKH);
        if (hangHienTai != null) {
            // Cập nhật hạng + chi tiêu + điểm mới tính
            hangHienTai.setTenHang(tenHangMoi);
            dongBoChinhSachHang(hangHienTai, hangMucTieu);
            hangHienTai.setTongChiTieu(tongChiTieuThucTe);
            hangHienTai.setDiemTichLuy(diemTichLuy);
            hangHienTai.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
            return hangThanhVienDAO.capNhatHangThanhVien(hangHienTai);
        }

        // Tạo mới hồ sơ hạng cho khách — điền tổng chi tiêu thực tế ngay từ đầu
        HangThanhVien hangMoiTao = new HangThanhVien(
                hangThanhVienDAO.phatSinhMaHangMoi(), maKH, tenHangMoi,
                hangMucTieu.getChiTieuToiThieu(), hangMucTieu.getChiTieuToiDa(),
                hangMucTieu.getMucGiamGia(), hangMucTieu.getDiemTichLuy1Van(),
                hangMucTieu.getQuyenLoi(), tongChiTieuThucTe, diemTichLuy,
                new Timestamp(System.currentTimeMillis()), "Đang áp dụng");
        return hangThanhVienDAO.themHangThanhVien(hangMoiTao);
    }

    private HangThanhVien timHangTheoTen(String ten) {
        for (HangThanhVien tier : layTatCaHangThanhVien()) {
            if (tier.getTenHang().equalsIgnoreCase(ten)) {
                return tier;
            }
        }
        return null;
    }

    private static void dongBoChinhSachHang(HangThanhVien destination, HangThanhVien policy) {
        destination.setTenHang(policy.getTenHang());
        destination.setChiTieuToiThieu(policy.getChiTieuToiThieu());
        destination.setChiTieuToiDa(policy.getChiTieuToiDa());
        destination.setMucGiamGia(policy.getMucGiamGia());
        destination.setDiemTichLuy1Van(policy.getDiemTichLuy1Van());
        destination.setQuyenLoi(policy.getQuyenLoi());
    }

    // =========================================================================
    // 9. CHỨC NĂNG 7: TÍNH TỔNG CHI TIÊU & ĐIỂM TÍCH LŨY TỪ HÓA ĐƠN THỰC TẾ
    // =========================================================================

    /**
     * Tính điểm tích lũy dựa trên tổng chi tiêu và tỷ lệ điểm/10.000đ của hạng.
     * Công thức: FLOOR(tongChiTieu / 10.000 × diemTichLuy1Van)
     *
     * @param tongChiTieu     Tổng chi tiêu thực tế (đơn vị: đồng)
     * @param diemTichLuy1Van Tỷ lệ điểm tích lũy trên mỗi 10.000đ của hạng
     * @return Điểm tích lũy nguyên dương
     */
    public int tinhDiemTichLuy(double tongChiTieu, double diemTichLuy1Van) {
        if (tongChiTieu <= 0 || diemTichLuy1Van <= 0) return 0;
        return (int) Math.floor(tongChiTieu / 10_000.0 * diemTichLuy1Van);
    }

    /**
     * Lấy tổng chi tiêu thực tế của một khách hàng từ các hóa đơn đã thanh toán.
     * Dùng để hiển thị trên giao diện hoặc kiểm tra trước khi lưu.
     *
     * @param maKH Mã khách hàng
     * @return Tổng tiền đã chi tiêu (đơn vị: đồng)
     */
    public double layTongChiTieuThucTe(String maKH) {
        return hangThanhVienDAO.tinhTongChiTieuTuHoaDon(maKH);
    }

    /**
     * Đồng bộ lại tổng chi tiêu và điểm tích lũy cho một khách hàng:
     * 1. Truy vấn tổng chi tiêu từ bảng HoaDon (hóa đơn đã thanh toán).
     * 2. Tính lại điểm theo tỷ lệ của hạng hiện tại.
     * 3. Cập nhật vào bảng QuanLyHangThanhVien.
     *
     * @param maKH Mã khách hàng cần đồng bộ
     * @return KetQuaDongBoChiTieu chứa trạng thái, tổng chi tiêu và điểm đã cập nhật
     */
    public KetQuaDongBoChiTieu dongBoChiTieuVaDiem(String maKH) {
        HangThanhVien hangHienTai = hangThanhVienDAO.getHangThanhVienTheoMaKH(maKH);
        if (hangHienTai == null) {
            return new KetQuaDongBoChiTieu(false, 0.0, 0,
                    "Khách hàng " + maKH + " chưa có hồ sơ hạng thành viên!");
        }

        double tongChiTieu = hangThanhVienDAO.tinhTongChiTieuTuHoaDon(maKH);
        int diem = tinhDiemTichLuy(tongChiTieu, hangHienTai.getDiemTichLuy1Van());
        boolean ok = hangThanhVienDAO.dongBoChiTieuVaDiem(maKH, hangHienTai.getDiemTichLuy1Van());

        if (ok) {
            return new KetQuaDongBoChiTieu(true, tongChiTieu, diem,
                    "Đã đồng bộ: " + dinhDangTienTe(tongChiTieu) + " — " + diem + " điểm");
        } else {
            return new KetQuaDongBoChiTieu(false, tongChiTieu, diem,
                    "Không thể cập nhật CSDL. Vui lòng thử lại!");
        }
    }

    // =========================================================================
    // 10. CÁC HÀM TIỆN ÍCH ĐỊNH DẠNG TIỀN TỆ & KHOẢNG CHI TIÊU
    // =========================================================================
    /**
     * Định dạng khoảng chi tiêu dạng text dễ đọc: "Tổng chi tiêu từ 1.000.000 ₫ đến
     * dưới 5.000.000 ₫"
     */
    public static String dinhDangKhoangChiTieu(double toiThieu, Double toiDa) {
        if (toiThieu <= 0 && toiDa != null) {
            return "Tổng chi tiêu dưới " + dinhDangTienTe(toiDa);
        }
        if (toiDa == null) {
            return "Tổng chi tiêu từ " + dinhDangTienTe(toiThieu);
        }
        return "Từ " + dinhDangTienTe(toiThieu) + " đến dưới " + dinhDangTienTe(toiDa);
    }

    /**
     * Định dạng số tiền sang chuẩn tiền tệ Việt Nam (VNĐ có dấu phân cách chấm).
     */
    public static String dinhDangTienTe(double giaTri) {
        return String.format(Locale.US, "%,.0f ₫", giaTri).replace(',', '.');
    }

    /**
     * Tiện ích chuyển chuỗi nhập liệu (loại bỏ dấu chấm, phẩy) thành số thực
     * Double.
     */
    private static Double chuyenChuoiSangSo(String text) {
        if (text == null)
            return null;
        try {
            return Double.parseDouble(text.trim().replace(".", "").replace(",", ""));
        } catch (Exception ex) {
            return null;
        }
    }

    // =========================================================================
    // 10. CÁC LỚP ĐỐI TƯỢNG ĐÓNG GÓI DỮ LIỆU & KẾT QUẢ (DTOs)
    // =========================================================================

    /**
     * Danh mục các trường dữ liệu trên form chính sách hạng (dùng trỏ focus khi
     * lỗi).
     */
    public enum TruongChinhSach {
        CHI_TIEU_TOI_THIEU, CHI_TIEU_TOI_DA, MUC_GIAM_GIA, DIEM_TICH_LUY
    }

    /**
     * DTO lưu trữ thông tin trích xuất của dòng thành viên được chọn.
     */
    public static final class ThongTinDongThanhVien {
        private final String maHang;
        private final String maKH;
        private final String hoTen;

        public ThongTinDongThanhVien(String maHang, String maKH, String hoTen) {
            this.maHang = maHang;
            this.maKH = maKH;
            this.hoTen = hoTen;
        }

        public String getMaHang() {
            return maHang;
        }

        public String getMaKH() {
            return maKH;
        }

        public String getHoTen() {
            return hoTen;
        }
    }

    /**
     * DTO kết quả của nghiệp vụ Chuyển nhanh hạng thành viên.
     */
    public static final class KetQuaChuyenHang {
        private final boolean thanhCong;
        private final boolean coThayDoi;
        private final String thongBao;

        public KetQuaChuyenHang(boolean thanhCong, boolean coThayDoi, String thongBao) {
            this.thanhCong = thanhCong;
            this.coThayDoi = coThayDoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public boolean isCoThayDoi() {
            return coThayDoi;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    /**
     * DTO kết quả của nghiệp vụ Cập nhật chính sách hạng.
     */
    public static final class KetQuaCapNhatChinhSach {
        private final boolean thanhCong;
        private final boolean loiNhapLieu;
        private final TruongChinhSach truongLoi;
        private final String thongBao;

        public KetQuaCapNhatChinhSach(boolean thanhCong, boolean loiNhapLieu, TruongChinhSach truongLoi,
                String thongBao) {
            this.thanhCong = thanhCong;
            this.loiNhapLieu = loiNhapLieu;
            this.truongLoi = truongLoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public boolean isLoiNhapLieu() {
            return loiNhapLieu;
        }

        public TruongChinhSach getTruongLoi() {
            return truongLoi;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    /**
     * DTO kết quả của nghiệp vụ Phân hạng khách hàng.
     */
    public static final class KetQuaPhanHang {
        private final boolean thanhCong;
        private final boolean danhSachRong;
        private final String thongBao;

        public KetQuaPhanHang(boolean thanhCong, boolean danhSachRong, String thongBao) {
            this.thanhCong = thanhCong;
            this.danhSachRong = danhSachRong;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public boolean isDanhSachRong() {
            return danhSachRong;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    /**
     * DTO chứa 4 chỉ số thống kê hội viên tổng thể.
     */
    public static class ThongKeHangThanhVien {
        public final int tongThanhVien;
        public final int soNangHangGanDay;
        public final double tongChiTieu;
        public final double tongGiamGia;
        public final List<HangThanhVien> danhSachHang;

        public ThongKeHangThanhVien(int tongThanhVien, int soNangHangGanDay, double tongChiTieu,
                double tongGiamGia, List<HangThanhVien> danhSachHang) {
            this.tongThanhVien = tongThanhVien;
            this.soNangHangGanDay = soNangHangGanDay;
            this.tongChiTieu = tongChiTieu;
            this.tongGiamGia = tongGiamGia;
            this.danhSachHang = java.util.Objects.requireNonNull(danhSachHang, "Dữ liệu hạng không được trả về từ DAO");
        }

        public String layChuoiTongThanhVien() {
            return String.valueOf(tongThanhVien);
        }

        public String layChuoiSoNangHangGanDay() {
            return String.valueOf(soNangHangGanDay);
        }

        public String layChiTieuDinhDang() {
            return tongChiTieu >= 1_000_000
                    ? String.format(Locale.US, "%.1f tr", tongChiTieu / 1_000_000)
                    : String.format(Locale.US, "%,.0f ₫", tongChiTieu).replace(',', '.');
        }

        public String layGiamGiaDinhDang() {
            return tongGiamGia >= 1_000_000
                    ? String.format(Locale.US, "%.1f tr", tongGiamGia / 1_000_000)
                    : String.format(Locale.US, "%,.0f ₫", tongGiamGia).replace(',', '.');
        }
    }

    // Bí danh tương thích lớp cũ
    public static class Dashboard extends ThongKeHangThanhVien {
        public Dashboard(int tongThanhVien, int soNangHangGanDay, double tongChiTieu,
                double tongGiamGia, List<HangThanhVien> danhSachHang) {
            super(tongThanhVien, soNangHangGanDay, tongChiTieu, tongGiamGia, danhSachHang);
        }
    }

    /**
     * DTO lưu trữ dữ liệu hoàn chỉnh của 1 thẻ hạng thành viên (card).
     */
    public static class TheHangThanhVienModel {
        public final String tenHang;
        public final String dieuKienChiTieu;
        public final String mucGiamGia;
        public final String soKhachHang;
        public final String tiLeKhachHang;
        public final String[] danhSachQuyenLoi;
        public final Color mauVienTop;
        public final Color mauNenNhe;

        public TheHangThanhVienModel(String tenHang, String dieuKienChiTieu, String mucGiamGia,
                String soKhachHang, String tiLeKhachHang, String[] danhSachQuyenLoi,
                Color mauVienTop, Color mauNenNhe) {
            this.tenHang = tenHang;
            this.dieuKienChiTieu = dieuKienChiTieu;
            this.mucGiamGia = mucGiamGia;
            this.soKhachHang = soKhachHang;
            this.tiLeKhachHang = tiLeKhachHang;
            this.danhSachQuyenLoi = danhSachQuyenLoi;
            this.mauVienTop = mauVienTop;
            this.mauNenNhe = mauNenNhe;
        }
    }

    // Bí danh tương thích lớp cũ
    public static class TierCardModel extends TheHangThanhVienModel {
        public TierCardModel(String tenHang, String dieuKienChiTieu, String mucGiamGia,
                String soKhachHang, String tiLeKhachHang, String[] danhSachQuyenLoi,
                Color mauVienTop, Color mauNenNhe) {
            super(tenHang, dieuKienChiTieu, mucGiamGia, soKhachHang, tiLeKhachHang, danhSachQuyenLoi, mauVienTop,
                    mauNenNhe);
        }
    }

    /**
     * DTO chứa dữ liệu nạp lên form chỉnh sửa chính sách của một hạng.
     */
    public static class DuLieuFormChinhSachHang {
        public final String chiTieuMin;
        public final String chiTieuMax;
        public final String mucGiamGia;
        public final String diemTichLuy1Van;
        public final String quyenLoi;

        public DuLieuFormChinhSachHang(String chiTieuMin, String chiTieuMax, String mucGiamGia,
                String diemTichLuy1Van, String quyenLoi) {
            this.chiTieuMin = chiTieuMin;
            this.chiTieuMax = chiTieuMax;
            this.mucGiamGia = mucGiamGia;
            this.diemTichLuy1Van = diemTichLuy1Van;
            this.quyenLoi = quyenLoi;
        }
    }

    /**
     * DTO kết quả trả về sau khi đồng bộ tổng chi tiêu và điểm tích lũy từ hóa đơn thực tế.
     */
    public static final class KetQuaDongBoChiTieu {
        private final boolean thanhCong;
        private final double tongChiTieu;
        private final int diemTichLuy;
        private final String thongBao;

        public KetQuaDongBoChiTieu(boolean thanhCong, double tongChiTieu, int diemTichLuy, String thongBao) {
            this.thanhCong = thanhCong;
            this.tongChiTieu = tongChiTieu;
            this.diemTichLuy = diemTichLuy;
            this.thongBao = thongBao;
        }

        /** true nếu đồng bộ thành công vào CSDL */
        public boolean isThanhCong() { return thanhCong; }

        /** Tổng chi tiêu thực tế tính từ hóa đơn (đơn vị: đồng) */
        public double getTongChiTieu() { return tongChiTieu; }

        /** Điểm tích lũy đã tính lại theo hạng hiện tại */
        public int getDiemTichLuy() { return diemTichLuy; }

        /** Thông báo kết quả hiển thị cho người dùng */
        public String getThongBao() { return thongBao; }
    }
}
