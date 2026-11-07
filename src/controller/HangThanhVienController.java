package controller;

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

    // Tương thích ngược
    public static final String[] MEMBER_COLUMNS = DANH_SACH_COT_THANH_VIEN;
    public static final String[] TIER_NAMES = DANH_SACH_TEN_HANG;

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

    public PolicyResult capNhatChinhSach(String tenHang, String minText, String maxText, String giamGiaText,
            String diemText, String quyenLoi) {
        double minimum = chuyenChuoiSangSo(minText, PolicyField.MINIMUM, "Chi tiêu tối thiểu phải là số >= 0!");
        if (minimum < 0) {
            throw new PolicyInputException(PolicyField.MINIMUM, "Chi tiêu tối thiểu phải là số >= 0!");
        }

        Double maximum = null;
        if (!maxText.trim().isEmpty()) {
            maximum = chuyenChuoiSangSo(maxText, PolicyField.MAXIMUM, "Chi tiêu tối đa phải là số hợp lệ!");
            if (maximum <= minimum) {
                throw new PolicyInputException(PolicyField.MAXIMUM,
                        "Chi tiêu tối đa phải lớn hơn chi tiêu tối thiểu!");
            }
        }

        double discount = chuyenChuoiSangSo(giamGiaText, PolicyField.DISCOUNT, "Mức giảm giá phải từ 0% đến 100%!");
        if (discount < 0 || discount > 100) {
            throw new PolicyInputException(PolicyField.DISCOUNT, "Mức giảm giá phải từ 0% đến 100%!");
        }
        double points = chuyenChuoiSangSo(diemText, PolicyField.POINTS,
                "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        if (points < 0) {
            throw new PolicyInputException(PolicyField.POINTS, "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        }
        boolean saved = tierDao.capNhatChinhSachHang(tenHang, minimum, maximum, discount, points, quyenLoi.trim());
        return new PolicyResult(saved, minimum, maximum, discount, points);
    }

    private static double chuyenChuoiSangSo(String text, PolicyField field, String message) {
        try {
            return Double.parseDouble(text.trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new PolicyInputException(field, message, ex);
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

    // Các hàm tương thích ngược (aliases)
    public String[] getMemberColumns() { return layDanhSachCotThanhVien(); }
    public String[] getTierNames() { return layDanhSachTenHang(); }
    public Dashboard getDashboard() { return layThongKeTongQuan(); }
    public List<TierCardModel> getTierCards(Dashboard dashboard) { return layDanhSachTheHang(dashboard); }
    public List<HangThanhVien> getAllTiers() { return layTatCaHangThanhVien(); }
    public List<HangThanhVien> getMembersByTier(String tierName) { return layDanhSachThanhVienTheoHang(tierName); }
    public Object[][] getMembersTableData(String tierName) { return taiDuLieuBangThanhVien(tierName); }
    public Object[][] toMembersTableData(List<HangThanhVien> list) { return chuyenThanhDuLieuBangThanhVien(list); }
    public String buildMemberSearchRegex(String keyword) { return taoRegexTimKiem(keyword); }
    public KhachHangController.CustomerDetails getCustomerDetails(String customerCode) { return layChiTietKhachHang(customerCode); }
    public List<KhachHang> getCustomers() { return layDanhSachKhachHang(); }
    public List<String> getCustomerDisplayList() { return layDanhSachHienThiKhachHang(); }
    public boolean assignCustomerBySelection(String selection, String newTierName) { return ganHangTheoLuaChon(selection, newTierName); }
    public TierPolicyFormData getTierPolicyFormData(String tierName) { return layDuLieuFormChinhSach(tierName); }
    public PolicyResult updatePolicy(String name, String minText, String maxText, String discountText, String pointsText, String benefits) {
        return capNhatChinhSach(name, minText, maxText, discountText, pointsText, benefits);
    }
    public boolean assignTier(String tierCode, String customerCode, String tierName) { return ganHangThanhVien(tierCode, customerCode, tierName); }
    public boolean assignOrCreateTier(String customerCode, String tierName) { return ganHoacTaoHangThanhVien(customerCode, tierName); }
    public static String formatSpendingRange(double minimum, Double maximum) { return dinhDangKhoangChiTieu(minimum, maximum); }

    public enum PolicyField { MINIMUM, MAXIMUM, DISCOUNT, POINTS }

    public static final class PolicyInputException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final PolicyField field;

        public PolicyInputException(PolicyField field, String message) {
            super(message);
            this.field = field;
        }

        public PolicyInputException(PolicyField field, String message, Throwable cause) {
            super(message, cause);
            this.field = field;
        }

        public PolicyField getField() {
            return field;
        }
    }

    public static final class Dashboard {
        public final int memberCount;
        public final int recentUpgrades;
        public final double totalSpending;
        public final double totalDiscount;
        public final List<HangThanhVien> tiers;

        public final int tongThanhVien;
        public final int soNangHangGanDay;
        public final double tongChiTieu;
        public final double tongGiamGia;
        public final List<HangThanhVien> danhSachHang;

        private Dashboard(int memberCount, int recentUpgrades, double totalSpending,
                double totalDiscount, List<HangThanhVien> tiers) {
            this.memberCount = memberCount;
            this.recentUpgrades = recentUpgrades;
            this.totalSpending = totalSpending;
            this.totalDiscount = totalDiscount;
            this.tiers = java.util.Objects.requireNonNull(tiers, "Dữ liệu hạng không được trả về từ DAO");

            this.tongThanhVien = memberCount;
            this.soNangHangGanDay = recentUpgrades;
            this.tongChiTieu = totalSpending;
            this.tongGiamGia = totalDiscount;
            this.danhSachHang = this.tiers;
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

        public String getFormattedSpending() { return layChiTieuDinhDang(); }
        public String getFormattedDiscount() { return layGiamGiaDinhDang(); }
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
        public final String minSpend;
        public final String maxSpend;
        public final String discount;
        public final String pointsPer10k;
        public final String benefits;

        public final String chiTieuMin;
        public final String chiTieuMax;
        public final String mucGiamGia;
        public final String diemTichLuy1Van;
        public final String quyenLoi;

        public TierPolicyFormData(String minSpend, String maxSpend, String discount,
                String pointsPer10k, String benefits) {
            this.minSpend = minSpend;
            this.maxSpend = maxSpend;
            this.discount = discount;
            this.pointsPer10k = pointsPer10k;
            this.benefits = benefits;

            this.chiTieuMin = minSpend;
            this.chiTieuMax = maxSpend;
            this.mucGiamGia = discount;
            this.diemTichLuy1Van = pointsPer10k;
            this.quyenLoi = benefits;
        }
    }

    public static final class PolicyResult {
        public final boolean saved;
        public final double minimumSpend;
        public final Double maximumSpend;
        public final double discount;
        public final double pointsPerTenThousand;

        public final boolean thanhCong;
        public final double chiTieuToiThieu;
        public final Double chiTieuToiDa;
        public final double mucGiamGia;
        public final double diemTichLuy1Van;

        public PolicyResult(boolean saved, double minimumSpend, Double maximumSpend,
                double discount, double pointsPerTenThousand) {
            this.saved = saved;
            this.minimumSpend = minimumSpend;
            this.maximumSpend = maximumSpend;
            this.discount = discount;
            this.pointsPerTenThousand = pointsPerTenThousand;

            this.thanhCong = saved;
            this.chiTieuToiThieu = minimumSpend;
            this.chiTieuToiDa = maximumSpend;
            this.mucGiamGia = discount;
            this.diemTichLuy1Van = pointsPerTenThousand;
        }

        public String layThongBaoThanhCong(String tenHang) {
            return "Đã lưu chính sách hạng \"" + tenHang + "\" thành công vào database SALALA!\n" +
                    "- Chi tiêu: từ " + String.format(Locale.US, "%,.0f ₫", minimumSpend).replace(',', '.')
                    + (maximumSpend != null ? " đến " + String.format(Locale.US, "%,.0f ₫", maximumSpend).replace(',', '.') : " trở lên")
                    + "\n" +
                    "- Giảm giá: " + discount + "% | Điểm/10.000₫: " + pointsPerTenThousand;
        }

        public String getSuccessSummary(String tierName) {
            return layThongBaoThanhCong(tierName);
        }
    }
}
