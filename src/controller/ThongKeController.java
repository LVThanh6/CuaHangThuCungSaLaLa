package controller;

import dao.ThongKe.ThongKe_DAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThongKeController {
    private final ThongKe_DAO thongKeDao;

    public ThongKeController() {
        this(new ThongKe_DAO());
    }

    ThongKeController(ThongKe_DAO thongKeDao) {
        if (thongKeDao == null) {
            throw new IllegalArgumentException("DAO thống kê không được để trống.");
        }
        this.thongKeDao = thongKeDao;
    }

    public LoadResult taiDuLieu(LocalDate tu, LocalDate den) {
        String loi = loiKhoangNgay(tu, den);
        if (loi != null) {
            return LoadResult.failure(loi);
        }
        try {
            return LoadResult.success(new ReportData(thongKeDao.layDuLieuThongKe(tu, den)));
        } catch (Exception ex) {
            String thongDiep = ex.getMessage();
            return LoadResult.failure(thongDiep == null || thongDiep.trim().isEmpty()
                    ? "Không thể tải dữ liệu thống kê."
                    : thongDiep);
        }
    }

    public static DateRange khoangNhanh(QuickRange quickRange, LocalDate homNay) {
        if (quickRange == null || homNay == null) {
            throw new IllegalArgumentException("Khoảng thời gian nhanh và ngày hiện tại không được để trống.");
        }
        LocalDate tu;
        switch (quickRange) {
            case TODAY:
                tu = homNay;
                break;
            case LAST_7_DAYS:
                tu = homNay.minusDays(6);
                break;
            case LAST_30_DAYS:
                tu = homNay.minusDays(29);
                break;
            case THIS_MONTH:
                tu = homNay.withDayOfMonth(1);
                break;
            case THIS_YEAR:
                tu = homNay.withDayOfYear(1);
                break;
            default:
                throw new IllegalArgumentException("Khoảng thời gian nhanh không được hỗ trợ.");
        }
        return new DateRange(tu, homNay);
    }

    public static String loiKhoangNgay(LocalDate tu, LocalDate den) {
        if (tu == null || den == null || tu.isAfter(den)) {
            return "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.";
        }
        return null;
    }

    public static String moTaNhomThoiGian(LocalDate tu, LocalDate den) {
        long soNgay = ChronoUnit.DAYS.between(tu, den) + 1;
        if (soNgay <= 62) {
            return "nhóm theo ngày";
        }
        if (soNgay <= 730) {
            return "nhóm theo tháng";
        }
        return "nhóm theo năm";
    }

    public static Category categoryFor(String indicatorCode) {
        if (ThongKe_DAO.MA_TOP_HANG_HOA.equals(indicatorCode)
                || ThongKe_DAO.MA_SO_LUONG_BAN.equals(indicatorCode)
                || ThongKe_DAO.MA_DOANH_THU_HANG_HOA.equals(indicatorCode)
                || ThongKe_DAO.MA_TOP_TON_KHO.equals(indicatorCode)) {
            return Category.GOODS;
        }
        if (ThongKe_DAO.MA_TOP_DICH_VU.equals(indicatorCode)
                || ThongKe_DAO.MA_DOANH_THU_DICH_VU.equals(indicatorCode)) {
            return Category.SERVICES;
        }
        if (ThongKe_DAO.MA_TON_KHO.equals(indicatorCode)) {
            return Category.INVENTORY;
        }
        if (ThongKe_DAO.MA_TONG_LUONG.equals(indicatorCode)
                || ThongKe_DAO.MA_LUONG_NHAN_VIEN.equals(indicatorCode)) {
            return Category.PAYROLL;
        }
        return Category.REVENUE;
    }

    public static boolean isTimeSeries(String indicatorCode) {
        return ThongKe_DAO.MA_TONG_LUONG.equals(indicatorCode)
                || ThongKe_DAO.MA_SO_HOA_DON.equals(indicatorCode)
                || ThongKe_DAO.MA_SO_LUONG_BAN.equals(indicatorCode);
    }

    public enum QuickRange {
        TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, THIS_YEAR
    }

    public enum Category {
        REVENUE("Doanh thu"),
        GOODS("Hàng hóa"),
        SERVICES("Dịch vụ"),
        INVENTORY("Tồn kho"),
        PAYROLL("Tiền lương");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        public DateRange(LocalDate start, LocalDate end) {
            String error = loiKhoangNgay(start, end);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            this.start = start;
            this.end = end;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }
    }

    public static final class LoadResult {
        private final ReportData report;
        private final String errorMessage;

        private LoadResult(ReportData report, String errorMessage) {
            this.report = report;
            this.errorMessage = errorMessage;
        }

        private static LoadResult success(ReportData report) {
            return new LoadResult(report, null);
        }

        private static LoadResult failure(String errorMessage) {
            return new LoadResult(null, errorMessage);
        }

        public boolean isSuccess() {
            return report != null;
        }

        public ReportData getReport() {
            return report;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static final class ReportData {
        private final double totalRevenue;
        private final int invoiceCount;
        private final int itemsSold;
        private final double goodsRevenue;
        private final double serviceRevenue;
        private final double totalPayroll;
        private final List<Indicator> indicators;

        private ReportData(ThongKe_DAO.DuLieuThongKe source) {
            totalRevenue = source.doanhThuTong;
            invoiceCount = source.soHoaDon;
            itemsSold = source.soLuongBan;
            goodsRevenue = source.doanhThuHangHoa;
            serviceRevenue = source.doanhThuDichVu;
            totalPayroll = source.tongLuong;
            List<Indicator> copies = new ArrayList<>();
            for (ThongKe_DAO.ChiSoThongKe indicator : source.chiSo) {
                copies.add(new Indicator(indicator));
            }
            indicators = Collections.unmodifiableList(copies);
        }

        public double getTotalRevenue() { return totalRevenue; }
        public int getInvoiceCount() { return invoiceCount; }
        public int getItemsSold() { return itemsSold; }
        public double getGoodsRevenue() { return goodsRevenue; }
        public double getServiceRevenue() { return serviceRevenue; }
        public double getTotalPayroll() { return totalPayroll; }
        public List<Indicator> getIndicators() { return indicators; }
    }

    public static final class Indicator {
        private final String code;
        private final String name;
        private final String unit;
        private final String suffix;
        private final String description;
        private final List<String> labels;
        private final List<String> series;
        private final List<List<Double>> values;
        private final double total;

        private Indicator(ThongKe_DAO.ChiSoThongKe source) {
            code = source.ma;
            name = source.ten;
            unit = source.donVi;
            suffix = source.hauTo;
            description = source.ghiChu;
            labels = Collections.unmodifiableList(new ArrayList<>(source.nhan));
            series = Collections.unmodifiableList(new ArrayList<>(source.chuoi));
            List<List<Double>> rows = new ArrayList<>();
            for (List<Double> row : source.giaTri) {
                rows.add(Collections.unmodifiableList(new ArrayList<>(row)));
            }
            if (series.size() != rows.size()) {
                throw new IllegalArgumentException("Chỉ số '" + name
                        + "' có số chuỗi và số dòng giá trị không khớp.");
            }
            for (List<Double> row : rows) {
                if (row.size() != labels.size()) {
                    throw new IllegalArgumentException("Mỗi chuỗi của chỉ số '" + name
                            + "' phải có một giá trị cho mỗi nhãn.");
                }
            }
            values = Collections.unmodifiableList(rows);
            total = source.tong;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public String getSuffix() { return suffix; }
        public String getDescription() { return description; }
        public List<String> getLabels() { return labels; }
        public List<String> getSeries() { return series; }
        public List<List<Double>> getValues() { return values; }
        public double getTotal() { return total; }
    }
}
