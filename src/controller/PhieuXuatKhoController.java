package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class PhieuXuatKhoController {
    private final List<Phieu> all = new ArrayList<>();
    private int sequence = 12;

    public PhieuXuatKhoController() {
        loadSampleData();
    }

    public List<Phieu> getAll() {
        return new ArrayList<>(all);
    }

    public List<Phieu> loc(Filter filter) {
        String keyword = filter.keyword.trim().toLowerCase();
        List<Phieu> result = new ArrayList<>();
        for (Phieu issue : all) {
            if (!keyword.isEmpty() && !issue.so.toLowerCase().contains(keyword)) continue;
            if (filter.status != null && issue.tt != filter.status) continue;
            if (filter.from != null && issue.ngay.isBefore(filter.from)) continue;
            if (filter.to != null && issue.ngay.isAfter(filter.to)) continue;
            result.add(issue);
        }
        return result;
    }

    public String previewNextCode() {
        return String.format("PX%04d", sequence + 1);
    }

    public void acceptCode(String code) {
        if (code != null && code.matches("PX\\d+")) {
            sequence = Math.max(sequence, Integer.parseInt(code.substring(2)));
        }
    }

    public static String validateDateRange(LocalDate from, LocalDate to) {
        return from != null && to != null && from.isAfter(to)
                ? "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc." : null;
    }

    public static LocalDate parseDate(String value, java.time.format.DateTimeFormatter formatter) {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim(), formatter);
    }

    public Statistics statistics() {
        int exported = 0, drafts = 0;
        long value = 0;
        for (Phieu issue : all) {
            if (issue.tt == TrangThai.DA_XUAT) {
                exported++;
                value += tongTien(issue);
            } else if (issue.tt == TrangThai.NHAP) {
                drafts++;
            }
        }
        return new Statistics(all.size(), exported, drafts, value);
    }

    public void add(Phieu issue) {
        all.add(0, issue);
    }

    public void update(Phieu original, Phieu edited) {
        original.ngay = edited.ngay;
        original.lyDo = edited.lyDo;
        original.ghiChu = edited.ghiChu;
        original.dong = new ArrayList<>(edited.dong);
    }

    public List<String> kiemTraTon(Phieu issue, ToIntFunction<String> tonKho) {
        List<String> errors = new ArrayList<>();
        for (Dong line : issue.dong) {
            int currentStock = tonKho.applyAsInt(line.ma);
            if (line.sl > currentStock) {
                errors.add("• " + line.ten + ": cần xuất " + line.sl + ", tồn hiện tại " + currentStock);
            }
        }
        return errors;
    }

    public int tonKho(String maVatTu, ToIntFunction<String> tonKho) {
        return tonKho.applyAsInt(maVatTu);
    }

    public String validate(Draft draft, ToIntFunction<String> tonKho) {
        if (draft.ngay == null) return "Ngày xuất không hợp lệ.";
        if (draft.lyDo == null || draft.lyDo.isBlank()) return "Vui lòng nhập lý do xuất.";
        if (draft.lines.isEmpty()) return "Phiếu cần có ít nhất một vật tư.";
        for (int i = 0; i < draft.lines.size(); i++) {
            Dong line = draft.lines.get(i);
            if (line.sl <= 0) return "Số lượng phải > 0 (dòng " + (i + 1) + ").";
            int stock = tonKho.applyAsInt(line.ma);
            if (line.sl > stock) return "Dòng " + (i + 1) + " vượt tồn kho (tồn: " + stock + ").";
        }
        return null;
    }

    public QuantityResult normalizeEditedQuantity(int requested, int stock) {
        int quantity = requested;
        boolean exceededStock = quantity > stock;
        if (exceededStock) quantity = stock;
        if (quantity < 1) quantity = 1;
        return new QuantityResult(quantity, exceededStock);
    }

    public static long tongTienDong(List<Dong> lines) {
        long total = 0;
        for (Dong line : lines) total += thanhTien(line);
        return total;
    }

    public static int tongSoLuongDong(List<Dong> lines) {
        int total = 0;
        for (Dong line : lines) total += line.sl;
        return total;
    }

    public LineAdditionResult mergeLine(List<Dong> lines, Dong added, int currentStock) {
        for (Dong line : lines) {
            if (!line.ma.equals(added.ma)) continue;
            if (line.sl + added.sl > currentStock) {
                return new LineAdditionResult(false, currentStock, line.sl);
            }
            line.sl += added.sl;
            return new LineAdditionResult(true, currentStock, line.sl - added.sl);
        }
        if (added.sl > currentStock) {
            return new LineAdditionResult(false, currentStock, 0);
        }
        lines.add(added);
        return new LineAdditionResult(true, currentStock, 0);
    }

    public Dong taoDong(CatalogItem item, int quantity) {
        return new Dong(item.ma, item.ten, item.donVi, quantity, item.giaNhap);
    }

    public static final class CatalogItem {
        private final String ma, ten, donVi;
        private final long giaNhap;
        public CatalogItem(String ma, String ten, String donVi, long giaNhap) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.giaNhap = giaNhap;
        }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDonVi() { return donVi; }
        public long getGiaNhap() { return giaNhap; }
    }

    public boolean confirm(Phieu issue, BiConsumer<String, Integer> stockUpdater,
                           ToIntFunction<String> tonKho) {
        if (issue.tt != TrangThai.NHAP || issue.dong.isEmpty()
                || !kiemTraTon(issue, tonKho).isEmpty()) return false;
        for (Dong line : issue.dong) stockUpdater.accept(line.ma, -line.sl);
        issue.tt = TrangThai.DA_XUAT;
        return true;
    }

    public boolean cancel(Phieu issue) {
        if (issue.tt != TrangThai.NHAP) return false;
        issue.tt = TrangThai.DA_HUY;
        return true;
    }

    public static long thanhTien(Dong line) {
        return (long) line.sl * line.donGia;
    }

    public static int tongSoLuong(Phieu issue) {
        int total = 0;
        for (Dong line : issue.dong) total += line.sl;
        return total;
    }

    public static long tongTien(Phieu issue) {
        long total = 0;
        for (Dong line : issue.dong) total += thanhTien(line);
        return total;
    }

    public static String toCsv(List<Phieu> issues) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Mã phiếu xuất,Ngày xuất,Nhân viên,Lý do xuất,Số lượng mặt hàng,Tổng giá trị,Trạng thái\r\n");
        for (Phieu issue : issues) {
            csv.append(csv(issue.so)).append(',')
                    .append(csv(issue.ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))).append(',')
                    .append(csv(issue.nhanVien)).append(',')
                    .append(csv(issue.lyDo)).append(',')
                    .append(issue.dong.size()).append(',')
                    .append(tongTien(issue)).append(',')
                    .append(csv(issue.tt.text)).append("\r\n");
        }
        return csv.toString();
    }

    public static String buildText(Phieu issue, java.text.NumberFormat money) {
        StringBuilder text = new StringBuilder();
        String line = "-".repeat(78) + "\n";
        text.append("SALALA PET SHOP\nPHIẾU XUẤT KHO\n").append(line)
                .append("Mã phiếu     : ").append(issue.so).append('\n')
                .append("Ngày xuất    : ").append(issue.ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append('\n')
                .append("Nhân viên    : ").append(issue.nhanVien).append('\n')
                .append("Lý do xuất   : ").append(issue.lyDo).append('\n')
                .append("Ghi chú      : ").append(issue.ghiChu == null ? "" : issue.ghiChu).append('\n')
                .append("Trạng thái   : ").append(issue.tt.text).append('\n').append(line)
                .append(String.format("%-28s %-7s %5s %14s %17s%n", "Tên vật tư", "ĐVT", "SL", "Giá nhập", "Thành tiền"))
                .append(line);
        for (Dong item : issue.dong) {
            text.append(String.format("%-28s %-7s %5d %14s %17s%n", item.ten, item.donVi, item.sl,
                    money.format(item.donGia) + " đ", money.format(thanhTien(item)) + " đ"));
        }
        text.append(line).append(String.format("%-28s %-7s %5d %14s %17s%n",
                "TỔNG CỘNG", "", tongSoLuong(issue), "", money.format(tongTien(issue)) + " đ"));
        return text.toString();
    }

    private void loadSampleData() {
        all.add(mau("PX0012", LocalDate.of(2026, 10, 5), "Lê Minh", "Sử dụng cho điều trị", TrangThai.NHAP,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 4, 45000),
                dong("VT-004", "Fronil Spot-on Dog", "Tuýp", 1, 78000),
                dong("VT-006", "Kim tiêm 5ml", "Hộp", 4, 35000)));
        all.add(mau("PX0011", LocalDate.of(2026, 10, 5), "Võ Hoàng Nam", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 6, 45000),
                dong("VT-005", "Bông y tế vô trùng", "Gói", 5, 12000)));
        all.add(mau("PX0010", LocalDate.of(2026, 10, 4), "Phạm Ngọc Mai", "Chăm sóc thú cưng", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 2, 45000),
                dong("VT-003", "Prednisolone 5mg", "Viên", 8, 8500)));
        all.add(mau("PX0009", LocalDate.of(2026, 10, 4), "Lê Minh", "Sử dụng cho điều trị", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 5, 45000),
                dong("VT-002", "Rabisin Vắc-xin Dại", "Lọ", 1, 320000),
                dong("VT-003", "Prednisolone 5mg", "Viên", 2, 8500),
                dong("VT-004", "Fronil Spot-on Dog", "Tuýp", 1, 78000)));
        all.add(mau("PX0008", LocalDate.of(2026, 10, 3), "Trần Quốc Huy", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 1, 45000),
                dong("VT-002", "Rabisin Vắc-xin Dại", "Lọ", 1, 320000),
                dong("VT-005", "Bông y tế vô trùng", "Gói", 5, 12000)));
        all.add(mau("PX0007", LocalDate.of(2026, 10, 3), "Võ Hoàng Nam", "Vật tư hư hỏng", TrangThai.DA_HUY,
                dong("VT-005", "Bông y tế vô trùng", "Gói", 5, 13000)));
        all.add(mau("PX0006", LocalDate.of(2026, 10, 2), "Đặng Thanh Trúc", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 1, 45000),
                dong("VT-006", "Kim tiêm 5ml", "Hộp", 5, 35000)));
        all.add(mau("PX0005", LocalDate.of(2026, 10, 2), "Lê Minh", "Sử dụng cho điều trị", TrangThai.DA_XUAT,
                dong("VT-001", "Amoxicillin 250mg", "Ống", 4, 45000),
                dong("VT-003", "Prednisolone 5mg", "Viên", 4, 8500),
                dong("VT-005", "Bông y tế vô trùng", "Gói", 7, 12000)));
        all.add(mau("PX0004", LocalDate.of(2026, 10, 1), "Phạm Ngọc Mai", "Chăm sóc thú cưng", TrangThai.DA_XUAT,
                dong("VT-004", "Fronil Spot-on Dog", "Tuýp", 2, 78000)));
        all.add(mau("PX0003", LocalDate.of(2026, 10, 1), "Trần Quốc Huy", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-006", "Kim tiêm 5ml", "Hộp", 3, 35000)));
        all.add(mau("PX0002", LocalDate.of(2026, 9, 30), "Lê Minh", "Sử dụng cho điều trị", TrangThai.NHAP,
                dong("VT-002", "Rabisin Vắc-xin Dại", "Lọ", 2, 320000)));
        all.add(mau("PX0001", LocalDate.of(2026, 9, 30), "Đặng Thanh Trúc", "Bán hàng cho khách", TrangThai.DA_XUAT,
                dong("VT-005", "Bông y tế vô trùng", "Gói", 10, 12000)));
    }

    private static Dong dong(String ma, String ten, String donVi, int soLuong, long donGia) {
        return new Dong(ma, ten, donVi, soLuong, donGia);
    }

    private static Phieu mau(String so, LocalDate ngay, String nhanVien, String lyDo,
                             TrangThai status, Dong... lines) {
        Phieu issue = new Phieu();
        issue.so = so;
        issue.ngay = ngay;
        issue.nhanVien = nhanVien;
        issue.lyDo = lyDo;
        issue.tt = status;
        for (Dong line : lines) issue.dong.add(line);
        return issue;
    }

    private static String csv(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    public enum TrangThai {
        NHAP("Nháp"), DA_XUAT("Đã xuất"), DA_HUY("Đã hủy");
        public final String text;
        TrangThai(String text) { this.text = text; }
    }

    public static final class Dong {
        public final String ma, ten, donVi;
        public int sl;
        public long donGia;
        public Dong(String ma, String ten, String donVi, int sl, long donGia) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.sl = sl; this.donGia = donGia;
        }
    }

    public static final class Phieu {
        public String so, nhanVien, lyDo, ghiChu = "";
        public LocalDate ngay;
        public TrangThai tt = TrangThai.NHAP;
        public List<Dong> dong = new ArrayList<>();
    }

    public static final class Filter {
        public final String keyword;
        public final TrangThai status;
        public final LocalDate from, to;
        public Filter(String keyword, TrangThai status, LocalDate from, LocalDate to) {
            this.keyword = keyword == null ? "" : keyword;
            this.status = status;
            this.from = from;
            this.to = to;
        }
    }

    public static final class Draft {
        public final LocalDate ngay;
        public final String lyDo;
        public final List<Dong> lines;
        public Draft(LocalDate ngay, String lyDo, List<Dong> lines) {
            this.ngay = ngay;
            this.lyDo = lyDo;
            this.lines = new ArrayList<>(lines);
        }
    }

    public static final class Statistics {
        private final int total, exported, drafts;
        private final long value;
        private Statistics(int total, int exported, int drafts, long value) {
            this.total = total; this.exported = exported; this.drafts = drafts; this.value = value;
        }
        public int getTotal() { return total; }
        public int getExported() { return exported; }
        public int getDrafts() { return drafts; }
        public long getValue() { return value; }
    }

    public static final class LineAdditionResult {
        private final boolean successful;
        private final int stock, alreadyAdded;
        private LineAdditionResult(boolean successful, int stock, int alreadyAdded) {
            this.successful = successful; this.stock = stock; this.alreadyAdded = alreadyAdded;
        }
        public boolean isSuccessful() { return successful; }
        public int getStock() { return stock; }
        public int getAlreadyAdded() { return alreadyAdded; }
    }

    public static final class QuantityResult {
        private final int quantity;
        private final boolean exceededStock;
        private QuantityResult(int quantity, boolean exceededStock) {
            this.quantity = quantity; this.exceededStock = exceededStock;
        }
        public int getQuantity() { return quantity; }
        public boolean exceededStock() { return exceededStock; }
    }
}
