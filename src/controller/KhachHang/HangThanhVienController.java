package controller.KhachHang;

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

public class HangThanhVienController {
    public static final String[] DANH_SACH_COT_THANH_VIEN = {
            "Mã hạng", "Mã KH", "Họ và tên", "Số điện thoại", "Email", "Số thú cưng",
            "Tổng chi tiêu", "Điểm tích lũy", "Ngày cập nhật"
    };

    public static final String[] DANH_SACH_TEN_HANG = { "Đồng", "Bạc", "Vàng", "Kim cương" };

    public static final int COT_MA_HANG = 0;
    public static final int COT_MA_KH = 1;
    public static final int COT_HO_TEN = 2;
    public static final int COT_SO_DIEN_THOAI = 3;
    public static final int COT_EMAIL = 4;
    public static final int COT_SO_THU_CUNG = 5;
    public static final int COT_TONG_CHI_TIEU = 6;
    public static final int COT_DIEM_TICH_LUY = 7;
    public static final int COT_NGAY_CAP_NHAT = 8;

    private final HangThanhVien_DAO tierDao;
    private final KhachHang_DAO customerDao;
    private final KhachHangController khachHangController;

    public HangThanhVienController() {
        this(new HangThanhVien_DAO(), new KhachHang_DAO());
    }

    public HangThanhVienController(HangThanhVien_DAO tierDao, KhachHang_DAO customerDao) {
        this(tierDao, customerDao, new KhachHangController(customerDao));
    }

    public HangThanhVienController(HangThanhVien_DAO tierDao, KhachHang_DAO customerDao,
            KhachHangController khachHangController) {
        this.tierDao = tierDao;
        this.customerDao = customerDao;
        this.khachHangController = khachHangController;
    }

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

    public ThongTinDongThanhVien trichXuatThongTinDong(javax.swing.table.TableModel model, int modelRow) {
        String maHang = String.valueOf(model.getValueAt(modelRow, COT_MA_HANG));
        String maKH = String.valueOf(model.getValueAt(modelRow, COT_MA_KH));
        String hoTen = String.valueOf(model.getValueAt(modelRow, COT_HO_TEN));
        return new ThongTinDongThanhVien(maHang, maKH, hoTen);
    }

    public Dashboard layThongKeTongQuan() {
        List<HangThanhVien> dsHang = tierDao.getAllHangThanhVien();
        return new Dashboard(tierDao.getTongSoThanhVien(), tierDao.getSoLuongNangHangGanDay(),
                tierDao.getTongChiTieuThanhVien(), tierDao.getTongGiamGiaDaApDung(), dsHang);
    }

    public List<TierCardModel> layDanhSachTheHang(Dashboard thongKe) {
        List<TierCardModel> dsThe = new ArrayList<>();
        int tongKH = thongKe.tongThanhVien;
        List<HangThanhVien> dsHang = thongKe.danhSachHang;

        Color[] mauTops = {
                new Color(245, 158, 11), // Đồng: Amber
                new Color(100, 116, 139), // Bạc: Slate
                new Color(217, 119, 6), // Vàng: Amber đậm / Gold
                new Color(2, 132, 199) // Kim cương: Sky blue
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

            dsThe.add(new TierCardModel(h.getTenHang(), dieuKien, strGiam, strSoKhach, strTiLe, arrQL, mauTop, mauNen));
        }
        return dsThe;
    }

    public List<HangThanhVien> layTatCaHangThanhVien() {
        return java.util.Objects.requireNonNull(tierDao.getAllHangThanhVien(),
                "Dữ liệu hạng thành viên không được trả về từ DAO");
    }

    public List<HangThanhVien> layDanhSachThanhVienTheoHang(String tenHang) {
        return java.util.Objects.requireNonNull(tierDao.getDanhSachThanhVienTheoHang(tenHang),
                "Dữ liệu thành viên không được trả về từ DAO");
    }

    public Object[][] taiDuLieuBangThanhVien(String tenHang) {
        List<HangThanhVien> ds = layDanhSachThanhVienTheoHang(tenHang);
        return chuyenThanhDuLieuBangThanhVien(ds);
    }

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

    public String taoRegexTimKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return null;
        }
        return "(?i)" + java.util.regex.Pattern.quote(tuKhoa.trim());
    }

    public KhachHangController.CustomerDetails layChiTietKhachHang(String maKH) {
        return khachHangController.layChiTiet(maKH);
    }

    public List<KhachHang> layDanhSachKhachHang() {
        return java.util.Objects.requireNonNull(customerDao.getAlltbKhachHang(),
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

    public boolean ganHangTheoLuaChon(String chuoiChon, String tenHangMoi) {
        if (chuoiChon == null || !chuoiChon.contains(" - ")) {
            return false;
        }
        String maKH = chuoiChon.split(" - ")[0].trim();
        return ganHoacTaoHangThanhVien(maKH, tenHangMoi);
    }

    public TierPolicyFormData layDuLieuFormChinhSach(String tenHang) {
        HangThanhVien tier = timHangTheoTen(tenHang);
        if (tier == null) {
            return new TierPolicyFormData("", "", "", "", "");
        }
        String min = String.format(Locale.US, "%.0f", tier.getChiTieuToiThieu());
        String max = tier.getChiTieuToiDa() != null
                ? String.format(Locale.US, "%.0f", tier.getChiTieuToiDa()) : "";
        String discount = String.format(Locale.US, "%.1f", tier.getMucGiamGia());
        String points = String.format(Locale.US, "%.1f", tier.getDiemTichLuy1Van());
        String benefits = tier.getQuyenLoi() != null ? tier.getQuyenLoi() : "";
        return new TierPolicyFormData(min, max, discount, points, benefits);
    }

    public KetQuaCapNhatChinhSach xuLyCapNhatChinhSach(String tenHang, String minText, String maxText, String giamGiaText,
            String diemText, String quyenLoi) {
        Double minimum = chuyenChuoiSangSo(minText);
        if (minimum == null || minimum < 0) {
            return new KetQuaCapNhatChinhSach(false, true, PolicyField.MINIMUM, "Chi tiêu tối thiểu phải là số >= 0!");
        }

        Double maximum = null;
        if (maxText != null && !maxText.trim().isEmpty()) {
            maximum = chuyenChuoiSangSo(maxText);
            if (maximum == null) {
                return new KetQuaCapNhatChinhSach(false, true, PolicyField.MAXIMUM, "Chi tiêu tối đa phải là số hợp lệ!");
            }
            if (maximum <= minimum) {
                return new KetQuaCapNhatChinhSach(false, true, PolicyField.MAXIMUM,
                        "Chi tiêu tối đa phải lớn hơn chi tiêu tối thiểu!");
            }
        }

        Double discount = chuyenChuoiSangSo(giamGiaText);
        if (discount == null || discount < 0 || discount > 100) {
            return new KetQuaCapNhatChinhSach(false, true, PolicyField.DISCOUNT, "Mức giảm giá phải từ 0% đến 100%!");
        }

        Double points = chuyenChuoiSangSo(diemText);
        if (points == null || points < 0) {
            return new KetQuaCapNhatChinhSach(false, true, PolicyField.POINTS,
                    "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        }

        boolean saved = tierDao.capNhatChinhSachHang(tenHang, minimum, maximum, discount, points,
                quyenLoi != null ? quyenLoi.trim() : "");
        if (saved) {
            String thongBao = "Đã lưu chính sách hạng \"" + tenHang + "\" thành công vào database SALALA!\n" +
                    "- Chi tiêu: từ " + String.format(Locale.US, "%,.0f ₫", minimum).replace(',', '.')
                    + (maximum != null ? " đến " + String.format(Locale.US, "%,.0f ₫", maximum).replace(',', '.') : " trở lên")
                    + "\n" +
                    "- Giảm giá: " + discount + "% | Điểm/10.000₫: " + points;
            return new KetQuaCapNhatChinhSach(true, false, null, thongBao);
        } else {
            return new KetQuaCapNhatChinhSach(false, false, null,
                    "Không thể cập nhật chính sách hạng. Vui lòng thử lại!");
        }
    }

    public KetQuaChuyenHang xuLyChuyenHang(String maHang, String maKH, String hoTen, String hangHienTai, String hangMoi) {
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

    private static Double chuyenChuoiSangSo(String text) {
        if (text == null) return null;
        try {
            return Double.parseDouble(text.trim().replace(".", "").replace(",", ""));
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean ganHangThanhVien(String maHang, String maKH, String tenHangMoi) {
        HangThanhVien targetTier = timHangTheoTen(tenHangMoi);
        if (targetTier == null) {
            return false;
        }

        HangThanhVien current = tierDao.getHangThanhVienTheoMaKH(maKH);
        double spending = current == null ? 0.0 : current.getTongChiTieu();
        int points = (int) Math.floor(spending / 10000.0 * targetTier.getDiemTichLuy1Van());
        HangThanhVien updated = new HangThanhVien();
        updated.setMaHang(maHang);
        updated.setMaKH(maKH);
        dongBoChinhSachHang(updated, targetTier);
        updated.setTongChiTieu(spending);
        updated.setDiemTichLuy(points);
        updated.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
        updated.setTrangThai("Đang áp dụng");
        return tierDao.capNhatHangThanhVien(updated);
    }

    public boolean ganHoacTaoHangThanhVien(String maKH, String tenHangMoi) {
        HangThanhVien targetTier = timHangTheoTen(tenHangMoi);
        if (targetTier == null) {
            return false;
        }
        HangThanhVien current = tierDao.getHangThanhVienTheoMaKH(maKH);
        if (current != null) {
            current.setTenHang(tenHangMoi);
            dongBoChinhSachHang(current, targetTier);
            current.setDiemTichLuy((int) Math.floor(
                    current.getTongChiTieu() / 10000.0 * targetTier.getDiemTichLuy1Van()));
            current.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
            return tierDao.capNhatHangThanhVien(current);
        }

        HangThanhVien membership = new HangThanhVien(
                tierDao.phatSinhMaHangMoi(), maKH, tenHangMoi,
                targetTier.getChiTieuToiThieu(), targetTier.getChiTieuToiDa(),
                targetTier.getMucGiamGia(), targetTier.getDiemTichLuy1Van(),
                targetTier.getQuyenLoi(), 0.0, 0,
                new Timestamp(System.currentTimeMillis()), "Đang áp dụng");
        return tierDao.themHangThanhVien(membership);
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

    public static String dinhDangKhoangChiTieu(double toiThieu, Double toiDa) {
        if (toiThieu <= 0 && toiDa != null) {
            return "Tổng chi tiêu dưới " + dinhDangTienTe(toiDa);
        }
        if (toiDa == null) {
            return "Tổng chi tiêu từ " + dinhDangTienTe(toiThieu);
        }
        return "Từ " + dinhDangTienTe(toiThieu) + " đến dưới " + dinhDangTienTe(toiDa);
    }

    public static String dinhDangTienTe(double giaTri) {
        return String.format(Locale.US, "%,.0f ₫", giaTri).replace(',', '.');
    }

    public enum PolicyField { MINIMUM, MAXIMUM, DISCOUNT, POINTS }

    public static final class ThongTinDongThanhVien {
        private final String maHang;
        private final String maKH;
        private final String hoTen;

        public ThongTinDongThanhVien(String maHang, String maKH, String hoTen) {
            this.maHang = maHang;
            this.maKH = maKH;
            this.hoTen = hoTen;
        }

        public String getMaHang() { return maHang; }
        public String getMaKH() { return maKH; }
        public String getHoTen() { return hoTen; }
    }

    public static final class KetQuaChuyenHang {
        private final boolean thanhCong;
        private final boolean coThayDoi;
        private final String thongBao;

        public KetQuaChuyenHang(boolean thanhCong, boolean coThayDoi, String thongBao) {
            this.thanhCong = thanhCong;
            this.coThayDoi = coThayDoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isCoThayDoi() { return coThayDoi; }
        public String getThongBao() { return thongBao; }
    }

    public static final class KetQuaCapNhatChinhSach {
        private final boolean thanhCong;
        private final boolean loiNhapLieu;
        private final PolicyField truongLoi;
        private final String thongBao;

        public KetQuaCapNhatChinhSach(boolean thanhCong, boolean loiNhapLieu, PolicyField truongLoi, String thongBao) {
            this.thanhCong = thanhCong;
            this.loiNhapLieu = loiNhapLieu;
            this.truongLoi = truongLoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isLoiNhapLieu() { return loiNhapLieu; }
        public PolicyField getTruongLoi() { return truongLoi; }
        public String getThongBao() { return thongBao; }
    }

    public static final class KetQuaPhanHang {
        private final boolean thanhCong;
        private final boolean danhSachRong;
        private final String thongBao;

        public KetQuaPhanHang(boolean thanhCong, boolean danhSachRong, String thongBao) {
            this.thanhCong = thanhCong;
            this.danhSachRong = danhSachRong;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isDanhSachRong() { return danhSachRong; }
        public String getThongBao() { return thongBao; }
    }

    public static final class Dashboard {
        public final int tongThanhVien;
        public final int soNangHangGanDay;
        public final double tongChiTieu;
        public final double tongGiamGia;
        public final List<HangThanhVien> danhSachHang;

        private Dashboard(int memberCount, int recentUpgrades, double totalSpending,
                double totalDiscount, List<HangThanhVien> tiers) {
            this.tongThanhVien = memberCount;
            this.soNangHangGanDay = recentUpgrades;
            this.tongChiTieu = totalSpending;
            this.tongGiamGia = totalDiscount;
            this.danhSachHang = java.util.Objects.requireNonNull(tiers, "Dữ liệu hạng không được trả về từ DAO");
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

    public static final class TierCardModel {
        public final String tenHang;
        public final String dieuKienChiTieu;
        public final String mucGiamGia;
        public final String soKhachHang;
        public final String tiLeKhachHang;
        public final String[] danhSachQuyenLoi;
        public final Color mauVienTop;
        public final Color mauNenNhe;

        public TierCardModel(String tenHang, String dieuKienChiTieu, String mucGiamGia,
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

    public static final class TierPolicyFormData {
        public final String chiTieuMin;
        public final String chiTieuMax;
        public final String mucGiamGia;
        public final String diemTichLuy1Van;
        public final String quyenLoi;

        public TierPolicyFormData(String chiTieuMin, String chiTieuMax, String mucGiamGia,
                String diemTichLuy1Van, String quyenLoi) {
            this.chiTieuMin = chiTieuMin;
            this.chiTieuMax = chiTieuMax;
            this.mucGiamGia = mucGiamGia;
            this.diemTichLuy1Van = diemTichLuy1Van;
            this.quyenLoi = quyenLoi;
        }
    }
}
