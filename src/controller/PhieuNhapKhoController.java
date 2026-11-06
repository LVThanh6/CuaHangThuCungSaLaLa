package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PhieuNhapKhoController {
    private static final List<String> SUPPLIER_SUGGESTIONS = List.of(
            "Công ty Dược Thú Y VetGroup",
            "Công ty TNHH Thú Y Á Châu",
            "Pharma Pet Việt Nam");
    private final List<Phieu> all = new ArrayList<>();
    private int sequence = 42;

    public PhieuNhapKhoController() {
        loadSampleData();
    }

    public List<Phieu> getAll() {
        return new ArrayList<>(all);
    }

    public List<String> getSupplierSuggestions() {
        return SUPPLIER_SUGGESTIONS;
    }

    public List<Phieu> loc(Filter filter) {
        List<Phieu> result = new ArrayList<>();
        String keyword = filter.keyword.trim().toLowerCase();
        for (Phieu receipt : all) {
            if (!keyword.isEmpty()
                    && !receipt.so.toLowerCase().contains(keyword)
                    && !receipt.nhanVien.toLowerCase().contains(keyword)
                    && !receipt.nhaCungCap.toLowerCase().contains(keyword)) continue;
            if (filter.status != null && receipt.tt != filter.status) continue;
            if (filter.from != null && receipt.ngay.isBefore(filter.from)) continue;
            if (filter.to != null && receipt.ngay.isAfter(filter.to)) continue;
            result.add(receipt);
        }
        return result;
    }

    public int nextSequence() {
        return ++sequence;
    }

    public static String validateDateRange(LocalDate from, LocalDate to) {
        return from != null && to != null && from.isAfter(to)
                ? "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc." : null;
    }

    public static LocalDate parseDate(String value, java.time.format.DateTimeFormatter formatter) {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim(), formatter);
    }

    public void add(Phieu receipt) {
        all.add(0, receipt);
    }

    public void update(Phieu original, Phieu edited) {
        original.ngay = edited.ngay;
        original.nhaCungCap = edited.nhaCungCap;
        original.ghiChu = edited.ghiChu;
        original.dong = new ArrayList<>(edited.dong);
    }

    public boolean confirm(Phieu receipt, BiConsumer<String, Integer> stockUpdater) {
        if (receipt.tt != TrangThai.CHO_XAC_NHAN || receipt.dong.isEmpty()) return false;
        for (Dong line : receipt.dong) stockUpdater.accept(line.ma, line.sl);
        receipt.tt = TrangThai.DA_XAC_NHAN;
        return true;
    }

    public boolean cancel(Phieu receipt) {
        if (receipt.tt != TrangThai.CHO_XAC_NHAN) return false;
        receipt.tt = TrangThai.DA_HUY;
        return true;
    }

    public String validate(Draft draft) {
        if (draft.ngay == null) return "Ngày lập không hợp lệ.";
        if (draft.nhaCungCap == null || draft.nhaCungCap.isBlank()) return "Vui lòng nhập nhà cung cấp.";
        if (draft.lines.isEmpty()) return "Phiếu cần có ít nhất một vật tư.";
        return null;
    }

    public void mergeLine(List<Dong> lines, Dong added) {
        for (Dong line : lines) {
            if (line.ma.equals(added.ma)) {
                line.sl += added.sl;
                line.donGia = added.donGia;
                return;
            }
        }
        lines.add(added);
    }

    public Dong taoDong(CatalogItem item, int quantity, long price) {
        return new Dong(item.ma, item.ten, item.donVi, quantity, price);
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

    public static Long parsePrice(String text) {
        try {
            long value = Long.parseLong(text.trim().replace(".", "").replace(",", ""));
            return value < 0 ? null : value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static long thanhTien(Dong line) {
        return (long) line.sl * line.donGia;
    }

    public static int tongSoLuong(Phieu receipt) {
        int total = 0;
        for (Dong line : receipt.dong) total += line.sl;
        return total;
    }

    public static long tongTien(Phieu receipt) {
        long total = 0;
        for (Dong line : receipt.dong) total += thanhTien(line);
        return total;
    }

    public static String toCsv(List<Phieu> receipts) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Mã phiếu nhập,Ngày lập,Nhân viên,Nhà cung cấp,Số lượng mặt hàng,Tổng giá trị,Trạng thái\r\n");
        for (Phieu receipt : receipts) {
            csv.append(csv(receipt.so)).append(',')
                    .append(csv(receipt.ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))).append(',')
                    .append(csv(receipt.nhanVien)).append(',')
                    .append(csv(receipt.nhaCungCap)).append(',')
                    .append(receipt.dong.size()).append(',')
                    .append(tongTien(receipt)).append(',')
                    .append(csv(receipt.tt.text)).append("\r\n");
        }
        return csv.toString();
    }

    public static String buildText(Phieu receipt, java.text.NumberFormat money) {
        StringBuilder text = new StringBuilder();
        String line = "-".repeat(78) + "\n";
        text.append("SALALA PET SHOP\nPHIẾU NHẬP KHO\n").append(line)
                .append("Số phiếu     : ").append(receipt.so).append('\n')
                .append("Ngày lập     : ").append(receipt.ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append('\n')
                .append("Nhân viên lập: ").append(receipt.nhanVien).append('\n')
                .append("Nhà cung cấp : ").append(receipt.nhaCungCap).append('\n')
                .append("Ghi chú      : ").append(receipt.ghiChu == null ? "" : receipt.ghiChu).append('\n')
                .append("Trạng thái   : ").append(receipt.tt.text).append('\n').append(line)
                .append(String.format("%-28s %-7s %5s %14s %17s%n", "Tên vật tư", "ĐVT", "SL", "Đơn giá", "Thành tiền"))
                .append(line);
        for (Dong item : receipt.dong) {
            text.append(String.format("%-28s %-7s %5d %14s %17s%n", item.ten, item.donVi, item.sl,
                    money.format(item.donGia) + " đ", money.format(thanhTien(item)) + " đ"));
        }
        text.append(line).append(String.format("%-28s %-7s %5d %14s %17s%n",
                "TỔNG CỘNG", "", tongSoLuong(receipt), "", money.format(tongTien(receipt)) + " đ"));
        return text.toString();
    }

    private void loadSampleData() {
        Phieu p42 = new Phieu();
        p42.so = "PN-2025-0042";
        p42.ngay = LocalDate.of(2025, 7, 5);
        p42.nhanVien = "Lê Minh";
        p42.nhaCungCap = "Công ty Dược Thú Y VetGroup";
        p42.ghiChu = "Nhập bổ sung vắc-xin và kháng sinh tháng 7/2025";
        p42.dong.add(new Dong("VT-001", "Amoxicillin 250mg", "Ống", 100, 45000));
        p42.dong.add(new Dong("VT-002", "Rabisin Vắc-xin Dại", "Lọ", 30, 320000));
        p42.dong.add(new Dong("VT-006", "Kim tiêm 5ml", "Hộp", 10, 35000));
        Phieu p41 = new Phieu();
        p41.so = "PN-2025-0041";
        p41.ngay = LocalDate.of(2025, 6, 20);
        p41.nhanVien = "Lê Minh";
        p41.nhaCungCap = "Công ty TNHH Thú Y Á Châu";
        p41.ghiChu = "Bổ sung thuốc giảm đau";
        p41.dong.add(new Dong("VT-003", "Prednisolone 5mg", "Viên", 200, 8500));
        p41.tt = TrangThai.DA_XAC_NHAN;
        all.add(p42);
        all.add(p41);
    }

    private static String csv(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    public enum TrangThai {
        CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");
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
        public String so, nhanVien, nhaCungCap, ghiChu = "";
        public LocalDate ngay;
        public TrangThai tt = TrangThai.CHO_XAC_NHAN;
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
        public final String nhaCungCap;
        public final List<Dong> lines;
        public Draft(LocalDate ngay, String nhaCungCap, List<Dong> lines) {
            this.ngay = ngay;
            this.nhaCungCap = nhaCungCap;
            this.lines = new ArrayList<>(lines);
        }
    }
}
