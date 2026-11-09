package controller.DichVu;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dao.DichVu.KhuyenMai_DAO;
import entity.DichVu.KhuyenMai_Entity;

public class KhuyenMaiController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<String> PAUSED = new HashSet<>();
    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", symbols);
    }

    private final KhuyenMai_DAO dao;

    public KhuyenMaiController() {
        this(new KhuyenMai_DAO());
    }

    KhuyenMaiController(KhuyenMai_DAO dao) {
        this.dao = dao;
    }

    public List<KhuyenMai_Entity> load() {
        return java.util.Objects.requireNonNull(dao.getAllKhuyenMai(),
                "Promotion data was not returned by the DAO");
    }

    public Status status(KhuyenMai_Entity promotion) {
        LocalDate today = LocalDate.now();
        if (promotion.getNgayKetThuc() != null && promotion.getNgayKetThuc().isBefore(today))
            return Status.EXPIRED;
        if (PAUSED.contains(promotion.getMaKM()))
            return Status.PAUSED;
        if (promotion.getNgayBatDau() != null && promotion.getNgayBatDau().isAfter(today))
            return Status.UPCOMING;
        return Status.ACTIVE;
    }

    public boolean isPaused(String code) {
        return PAUSED.contains(code);
    }

    public DateRange parseDateRange(String fromText, String toText) {
        try {
            LocalDate from = fromText.trim().isEmpty() ? null : LocalDate.parse(fromText.trim(), DATE_FORMAT);
            LocalDate to = toText.trim().isEmpty() ? null : LocalDate.parse(toText.trim(), DATE_FORMAT);
            if (from != null && to != null && from.isAfter(to)) {
                throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            }
            return new DateRange(from, to);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng dd/MM/yyyy).", ex);
        }
    }

    public void pause(String code) {
        PAUSED.add(code);
    }

    public void resume(String code) {
        PAUSED.remove(code);
    }

    public static boolean isPausedPromotion(String code) {
        return PAUSED.contains(code);
    }

    public boolean isPercentage(KhuyenMai_Entity promotion) {
        return promotion.getMucKM() <= 100;
    }

    public static boolean isPercentageValue(double amount) {
        return amount <= 100;
    }

    public static String formatNumber(double amount) {
        return plainNumber(amount);
    }

    public String formatAmount(KhuyenMai_Entity promotion) {
        double amount = promotion.getMucKM();
        return amount <= 100 ? plainNumber(amount) + "%" : MONEY.format((long) amount) + " ₫";
    }

    public List<KhuyenMai_Entity> filter(List<KhuyenMai_Entity> promotions, String keyword, int statusIndex,
            LocalDate from, LocalDate to) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        Status[] statuses = { null, Status.ACTIVE, Status.UPCOMING, Status.PAUSED, Status.EXPIRED };
        List<KhuyenMai_Entity> filtered = new ArrayList<>();
        for (KhuyenMai_Entity promotion : promotions) {
            if (!normalizedKeyword.isEmpty()
                    && !safe(promotion.getMaKM()).toLowerCase().contains(normalizedKeyword)
                    && !safe(promotion.getTenKM()).toLowerCase().contains(normalizedKeyword))
                continue;
            if (statusIndex > 0 && status(promotion) != statuses[statusIndex])
                continue;
            if (from != null && promotion.getNgayKetThuc() != null
                    && promotion.getNgayKetThuc().isBefore(from))
                continue;
            if (to != null && promotion.getNgayBatDau() != null
                    && promotion.getNgayBatDau().isAfter(to))
                continue;
            filtered.add(promotion);
        }
        return filtered;
    }

    public Summary summarize(List<KhuyenMai_Entity> promotions) {
        LocalDate today = LocalDate.now();
        int active = 0;
        int upcoming = 0;
        int paused = 0;
        int expired = 0;
        for (KhuyenMai_Entity promotion : promotions) {
            switch (status(promotion)) {
                case ACTIVE:
                    active++;
                    break;
                case UPCOMING:
                    if (promotion.getNgayBatDau() == null
                            || !promotion.getNgayBatDau().isAfter(today.plusDays(30)))
                        upcoming++;
                    break;
                case PAUSED:
                    paused++;
                    break;
                case EXPIRED:
                    expired++;
                    break;
                default:
                    throw new IllegalStateException("Unknown promotion status");
            }
        }
        return new Summary(promotions.size(), active, upcoming, paused, expired);
    }

    public boolean save(String code, String name, boolean percentage, String amountText, String startText,
            String endText, String condition, List<KhuyenMai_Entity> existing, boolean update) {
        String normalizedCode = code.trim();
        String normalizedName = name.trim();
        if (normalizedCode.isEmpty())
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
        if (normalizedName.isEmpty())
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống!");

        double amount;
        LocalDate start;
        LocalDate end;
        try {
            amount = percentage
                    ? Double.parseDouble(amountText.trim().replace(",", "."))
                    : Double.parseDouble(amountText.trim().replace(".", "").replace(",", ""));
            start = LocalDate.parse(startText.trim(), DATE_FORMAT);
            end = LocalDate.parse(endText.trim(), DATE_FORMAT);
        } catch (NumberFormatException | DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Dữ liệu nhập không hợp lệ (kiểm tra định dạng số và ngày dd/MM/yyyy).", ex);
        }

        if (percentage && (amount <= 0 || amount > 100)) {
            throw new IllegalArgumentException("Mức giảm theo phần trăm phải trong khoảng (0, 100].");
        }
        if (!percentage && amount <= 100) {
            throw new IllegalArgumentException("Mức giảm theo số tiền phải lớn hơn 100 ₫.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }
        if (!update) {
            for (KhuyenMai_Entity promotion : existing) {
                if (normalizedCode.equalsIgnoreCase(promotion.getMaKM())) {
                    throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại!");
                }
            }
        }

        KhuyenMai_Entity promotion = new KhuyenMai_Entity(
                normalizedCode, normalizedName, amount, start, end, condition.trim());
        return update ? dao.updateKhuyenMai(promotion) : dao.createKhuyenMai(promotion);
    }

    public boolean delete(String code) {
        return dao.deleteKhuyenMai(code);
    }

    public String toCsv(List<KhuyenMai_Entity> promotions) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(
                "Mã khuyến mãi,Tên khuyến mãi,Mức giảm,Loại giảm,Ngày bắt đầu,Ngày kết thúc,Điều kiện,Trạng thái\r\n");
        for (KhuyenMai_Entity promotion : promotions) {
            csv.append(csv(promotion.getMaKM())).append(',')
                    .append(csv(promotion.getTenKM())).append(',')
                    .append(csv(formatAmount(promotion))).append(',')
                    .append(csv(isPercentage(promotion) ? "Phần trăm" : "Số tiền")).append(',')
                    .append(csv(formatDate(promotion.getNgayBatDau()))).append(',')
                    .append(csv(formatDate(promotion.getNgayKetThuc()))).append(',')
                    .append(csv(safe(promotion.getDieuKien()))).append(',')
                    .append(csv(status(promotion).label)).append("\r\n");
        }
        return csv.toString();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private static String csv(String value) {
        return "\"" + safe(value).replace("\"", "\"\"") + "\"";
    }

    private static String plainNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public enum Status {
        ACTIVE("Đang áp dụng"), UPCOMING("Sắp diễn ra"), PAUSED("Tạm dừng"), EXPIRED("Hết hạn");

        public final String label;

        Status(String label) {
            this.label = label;
        }
    }

    public static final class DateRange {
        public final LocalDate from;
        public final LocalDate to;

        private DateRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }
    }

    public static final class Summary {
        public final int total;
        public final int active;
        public final int upcoming;
        public final int paused;
        public final int expired;

        private Summary(int total, int active, int upcoming, int paused, int expired) {
            this.total = total;
            this.active = active;
            this.upcoming = upcoming;
            this.paused = paused;
            this.expired = expired;
        }
    }
}
