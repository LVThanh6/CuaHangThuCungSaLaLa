package controller;

import java.sql.Timestamp;
import java.util.List;

import dao.KhachHang.HangThanhVien_DAO;
import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.HangThanhVien;
import entity.KhachHang.KhachHang;

public class HangThanhVienController {
    private final HangThanhVien_DAO tierDao;
    private final KhachHang_DAO customerDao;

    public HangThanhVienController() {
        this(new HangThanhVien_DAO(), new KhachHang_DAO());
    }

    HangThanhVienController(HangThanhVien_DAO tierDao, KhachHang_DAO customerDao) {
        this.tierDao = tierDao;
        this.customerDao = customerDao;
    }

    public Dashboard getDashboard() {
        List<HangThanhVien> tiers = tierDao.getAllHangThanhVien();
        return new Dashboard(tierDao.getTongSoThanhVien(), tierDao.getSoLuongNangHangGanDay(),
                tierDao.getTongChiTieuThanhVien(), tierDao.getTongGiamGiaDaApDung(), tiers);
    }

    public List<HangThanhVien> getAllTiers() {
        return java.util.Objects.requireNonNull(tierDao.getAllHangThanhVien(),
                "Membership tier data was not returned by the DAO");
    }

    public List<HangThanhVien> getMembersByTier(String tierName) {
        return java.util.Objects.requireNonNull(tierDao.getDanhSachThanhVienTheoHang(tierName),
                "Member data was not returned by the DAO");
    }

    public List<KhachHang> getCustomers() {
        return java.util.Objects.requireNonNull(customerDao.getAlltbKhachHang(),
                "Customer data was not returned by the DAO");
    }

    public PolicyResult updatePolicy(String name, String minText, String maxText, String discountText,
            String pointsText, String benefits) {
        double minimum = parseNumber(minText, PolicyField.MINIMUM, "Chi tiêu tối thiểu phải là số >= 0!");
        if (minimum < 0) {
            throw new PolicyInputException(PolicyField.MINIMUM, "Chi tiêu tối thiểu phải là số >= 0!");
        }

        Double maximum = null;
        if (!maxText.trim().isEmpty()) {
            maximum = parseNumber(maxText, PolicyField.MAXIMUM, "Chi tiêu tối đa phải là số hợp lệ!");
            if (maximum <= minimum) {
                throw new PolicyInputException(PolicyField.MAXIMUM,
                        "Chi tiêu tối đa phải lớn hơn chi tiêu tối thiểu!");
            }
        }

        double discount = parseNumber(discountText, PolicyField.DISCOUNT, "Mức giảm giá phải từ 0% đến 100%!");
        if (discount < 0 || discount > 100) {
            throw new PolicyInputException(PolicyField.DISCOUNT, "Mức giảm giá phải từ 0% đến 100%!");
        }
        double points = parseNumber(pointsText, PolicyField.POINTS,
                "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        if (points < 0) {
            throw new PolicyInputException(PolicyField.POINTS, "Điểm tích lũy trên 1 vạn phải là số >= 0!");
        }
        boolean saved = tierDao.capNhatChinhSachHang(name, minimum, maximum, discount, points, benefits.trim());
        return new PolicyResult(saved, minimum, maximum, discount, points);
    }

    private static double parseNumber(String text, PolicyField field, String message) {
        try {
            return Double.parseDouble(text.trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new PolicyInputException(field, message, ex);
        }
    }

    public boolean assignTier(String tierCode, String customerCode, String tierName) {
        HangThanhVien targetTier = findTier(tierName);
        if (targetTier == null) {
            return false;
        }

        HangThanhVien current = tierDao.getHangThanhVienTheoMaKH(customerCode);
        double spending = current == null ? 0.0 : current.getTongChiTieu();
        int points = (int) Math.floor(spending / 10000.0 * targetTier.getDiemTichLuy1Van());
        HangThanhVien updated = new HangThanhVien();
        updated.setMaHang(tierCode);
        updated.setMaKH(customerCode);
        applyTierPolicy(updated, targetTier);
        updated.setTongChiTieu(spending);
        updated.setDiemTichLuy(points);
        updated.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
        updated.setTrangThai("Đang áp dụng");
        return tierDao.capNhatHangThanhVien(updated);
    }

    public boolean assignOrCreateTier(String customerCode, String tierName) {
        HangThanhVien targetTier = findTier(tierName);
        if (targetTier == null) {
            return false;
        }
        HangThanhVien current = tierDao.getHangThanhVienTheoMaKH(customerCode);
        if (current != null) {
            current.setTenHang(tierName);
            applyTierPolicy(current, targetTier);
            current.setDiemTichLuy((int) Math.floor(
                    current.getTongChiTieu() / 10000.0 * targetTier.getDiemTichLuy1Van()));
            current.setNgayCapNhatHang(new Timestamp(System.currentTimeMillis()));
            return tierDao.capNhatHangThanhVien(current);
        }

        HangThanhVien membership = new HangThanhVien(
                tierDao.phatSinhMaHangMoi(), customerCode, tierName,
                targetTier.getChiTieuToiThieu(), targetTier.getChiTieuToiDa(),
                targetTier.getMucGiamGia(), targetTier.getDiemTichLuy1Van(),
                targetTier.getQuyenLoi(), 0.0, 0,
                new Timestamp(System.currentTimeMillis()), "Đang áp dụng");
        return tierDao.themHangThanhVien(membership);
    }

    private HangThanhVien findTier(String name) {
        for (HangThanhVien tier : getAllTiers()) {
            if (tier.getTenHang().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return null;
    }

    private static void applyTierPolicy(HangThanhVien destination, HangThanhVien policy) {
        destination.setTenHang(policy.getTenHang());
        destination.setChiTieuToiThieu(policy.getChiTieuToiThieu());
        destination.setChiTieuToiDa(policy.getChiTieuToiDa());
        destination.setMucGiamGia(policy.getMucGiamGia());
        destination.setDiemTichLuy1Van(policy.getDiemTichLuy1Van());
        destination.setQuyenLoi(policy.getQuyenLoi());
    }

    public static String formatSpendingRange(double minimum, Double maximum) {
        if (minimum <= 0 && maximum != null) {
            return "Tổng chi tiêu dưới " + formatCurrency(maximum);
        }
        if (maximum == null) {
            return "Tổng chi tiêu từ " + formatCurrency(minimum);
        }
        return "Từ " + formatCurrency(minimum) + " đến dưới " + formatCurrency(maximum);
    }

    private static String formatCurrency(double value) {
        return String.format("%,.0f ₫", value).replace(',', '.');
    }

    public enum PolicyField { MINIMUM, MAXIMUM, DISCOUNT, POINTS }

    public static final class PolicyInputException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final PolicyField field;

        private PolicyInputException(PolicyField field, String message) {
            super(message);
            this.field = field;
        }

        private PolicyInputException(PolicyField field, String message, Throwable cause) {
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

        private Dashboard(int memberCount, int recentUpgrades, double totalSpending,
                double totalDiscount, List<HangThanhVien> tiers) {
            this.memberCount = memberCount;
            this.recentUpgrades = recentUpgrades;
            this.totalSpending = totalSpending;
            this.totalDiscount = totalDiscount;
            this.tiers = java.util.Objects.requireNonNull(tiers, "Tier data was not returned by the DAO");
        }
    }

    public static final class PolicyResult {
        public final boolean saved;
        public final double minimumSpend;
        public final Double maximumSpend;
        public final double discount;
        public final double pointsPerTenThousand;

        private PolicyResult(boolean saved, double minimumSpend, Double maximumSpend,
                double discount, double pointsPerTenThousand) {
            this.saved = saved;
            this.minimumSpend = minimumSpend;
            this.maximumSpend = maximumSpend;
            this.discount = discount;
            this.pointsPerTenThousand = pointsPerTenThousand;
        }
    }
}
