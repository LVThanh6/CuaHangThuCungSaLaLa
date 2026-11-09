package controller.VatTu;

import dao.VatTu.PhieuXuatKho_DAO;
import entity.VatTu.PhieuXuatKho_entity;
import entity.VatTu.PhieuXuatKho_entity.ChiTiet;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller "Quản lý xuất kho" - lấy dữ liệu thật từ CSDL SALALA thông qua PhieuXuatKho_DAO.
 * Không còn dữ liệu mẫu: danh sách phiếu, danh mục vật tư, tồn kho, nhân viên đều đọc từ CSDL.
 * Các lớp Phieu / Dong là mô hình hiển thị cho GUI, được chuyển đổi qua lại với PhieuXuatKho_entity.
 */
public class PhieuXuatKhoController {
    private final PhieuXuatKho_DAO dao = new PhieuXuatKho_DAO();
    private List<Phieu> all = new ArrayList<>();
    private String lastError = "";

    // =====================================================================
    // Nạp dữ liệu từ CSDL
    // =====================================================================
    /** Nạp lại toàn bộ phiếu xuất từ CSDL. Trả về false (và có getLastError()) nếu không đọc được. */
    public boolean reload() {
        try {
            List<Phieu> list = new ArrayList<>();
            for (PhieuXuatKho_entity e : dao.getAll()) list.add(fromEntity(e));
            all = list;
            lastError = "";
            return true;
        } catch (SQLException ex) {
            all = new ArrayList<>();
            lastError = ex.getMessage();
            ex.printStackTrace();
            return false;
        }
    }

    public String getLastError() {
        return lastError;
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

    /** Danh mục vật tư (giá nhập + tồn hiện tại) để chọn khi lập phiếu. */
    public List<CatalogItem> layDanhMucVatTu() {
        List<CatalogItem> catalog = new ArrayList<>();
        try {
            for (PhieuXuatKho_DAO.VatTuRow r : dao.getDanhMucVatTu()) {
                catalog.add(new CatalogItem(r.maVT, r.tenVT, r.donVi, r.giaNhap, r.tonKho));
            }
            lastError = "";
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            ex.printStackTrace();
        }
        return catalog;
    }

    /** Danh sách nhân viên {maNhanVien, hoTen} để chọn người lập phiếu. */
    public List<String[]> layDanhSachNhanVien() {
        try {
            return dao.getDanhSachNhanVien();
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Mã phiếu kế tiếp (xem trước), ví dụ PXK003. */
    public String previewNextCode() {
        return dao.taoMaPhieuMoi();
    }

    /** Tồn kho hiện tại của vật tư (đọc trực tiếp từ CSDL). */
    public int tonKho(String maVatTu) {
        return dao.getTonKho(maVatTu);
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

    // =====================================================================
    // Ghi xuống CSDL (trả về false + getLastError() nếu thất bại; GUI gọi reload() sau khi thành công)
    // =====================================================================
    /** Lưu phiếu mới (trạng thái Nháp). Mã phiếu có thể được đổi nếu bị trùng; xem p.so sau khi gọi. */
    public boolean add(Phieu issue) {
        PhieuXuatKho_entity e = toEntity(issue);
        e.setTrangThai(PhieuXuatKho_entity.TrangThai.NHAP);
        boolean ok = dao.insert(e);
        if (ok) issue.so = e.getMaPhieu(); else lastError = dao.getLastError();
        return ok;
    }

    /** Lưu thay đổi của phiếu Nháp. */
    public boolean update(Phieu original, Phieu edited) {
        edited.so = original.so;
        boolean ok = dao.update(toEntity(edited));
        if (!ok) lastError = dao.getLastError();
        return ok;
    }

    /** Xác nhận xuất kho: kiểm tra tồn rồi trừ tồn trong 1 transaction. */
    public boolean confirm(Phieu issue) {
        if (issue.tt != TrangThai.NHAP || issue.dong.isEmpty()) return false;
        List<String> errors = kiemTraTon(issue);
        if (!errors.isEmpty()) {
            lastError = String.join("\n", errors);
            return false;
        }
        boolean ok = dao.xacNhan(issue.so);
        if (!ok) lastError = dao.getLastError();
        return ok;
    }

    public boolean cancel(Phieu issue) {
        if (issue.tt != TrangThai.NHAP) return false;
        boolean ok = dao.huy(issue.so);
        if (!ok) lastError = dao.getLastError();
        return ok;
    }

    // =====================================================================
    // Kiểm tra nghiệp vụ (dùng tồn kho thật trong CSDL)
    // =====================================================================
    public List<String> kiemTraTon(Phieu issue) {
        List<String> errors = new ArrayList<>();
        for (Dong line : issue.dong) {
            int currentStock = dao.getTonKho(line.ma);
            if (line.sl > currentStock) {
                errors.add("• " + line.ten + ": cần xuất " + line.sl + ", tồn hiện tại " + currentStock);
            }
        }
        return errors;
    }

    public String validate(Draft draft) {
        if (draft.ngay == null) return "Ngày xuất không hợp lệ.";
        if (draft.lyDo == null || draft.lyDo.trim().isEmpty()) return "Vui lòng nhập lý do xuất.";
        if (PhieuXuatKho_entity.ghepGhiChu(draft.lyDo, draft.ghiChu).length() > PhieuXuatKho_entity.GHI_CHU_TOI_DA) {
            return "Lý do và ghi chú quá dài (tối đa " + PhieuXuatKho_entity.GHI_CHU_TOI_DA + " ký tự).";
        }
        if (draft.lines.isEmpty()) return "Phiếu cần có ít nhất một vật tư.";
        for (int i = 0; i < draft.lines.size(); i++) {
            Dong line = draft.lines.get(i);
            if (line.sl <= 0) return "Số lượng phải > 0 (dòng " + (i + 1) + ").";
            int stock = dao.getTonKho(line.ma);
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

    // =====================================================================
    // Chuyển đổi Phieu <-> PhieuXuatKho_entity
    // =====================================================================
    private static Phieu fromEntity(PhieuXuatKho_entity e) {
        Phieu p = new Phieu();
        p.so = e.getMaPhieu();
        p.ngay = e.getNgayXuat();
        p.maNhanVien = e.getNhanVien() != null ? e.getNhanVien().getMaNhanVien() : null;
        p.nhanVien = e.getTenNhanVien() != null && !e.getTenNhanVien().isEmpty() ? e.getTenNhanVien() : p.maNhanVien;
        p.lyDo = e.getLyDo();
        p.ghiChu = e.getGhiChu() == null ? "" : e.getGhiChu();
        p.tt = TrangThai.valueOf(e.getTrangThai().name());
        for (ChiTiet d : e.getChiTiet()) {
            p.dong.add(new Dong(d.getMaVT() != null ? d.getMaVT().getMaVT() : null,
                    d.getTenVT(), d.getDonVi(), d.getSoLuong(), d.getDonGia()));
        }
        return p;
    }

    private static PhieuXuatKho_entity toEntity(Phieu p) {
        PhieuXuatKho_entity e = new PhieuXuatKho_entity();
        e.setMaPhieu(p.so);
        e.setNgayXuat(p.ngay);
        e.setNhanVien(p.maNhanVien);
        e.setTenNhanVien(p.nhanVien);
        e.setLyDo(p.lyDo);
        e.setGhiChu(p.ghiChu);
        e.setTrangThai(PhieuXuatKho_entity.TrangThai.valueOf(p.tt.name()));
        List<ChiTiet> lines = new ArrayList<>();
        for (Dong d : p.dong) lines.add(new ChiTiet(d.ma, d.ten, d.donVi, d.sl, d.donGia));
        e.setChiTiet(lines);
        return e;
    }

    // =====================================================================
    // Tính toán, xuất CSV / văn bản (không phụ thuộc CSDL)
    // =====================================================================
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

    private static String csv(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    public static final class CatalogItem {
        private final String ma, ten, donVi;
        private final long giaNhap;
        private final int ton;
        public CatalogItem(String ma, String ten, String donVi, long giaNhap, int ton) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.giaNhap = giaNhap; this.ton = ton;
        }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDonVi() { return donVi; }
        public long getGiaNhap() { return giaNhap; }
        public int getTon() { return ton; }
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
        public String so, nhanVien, maNhanVien, lyDo, ghiChu = "";   // nhanVien = họ tên (hiển thị), maNhanVien = khóa ngoại
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
        public final String lyDo, ghiChu;
        public final List<Dong> lines;
        public Draft(LocalDate ngay, String lyDo, String ghiChu, List<Dong> lines) {
            this.ngay = ngay;
            this.lyDo = lyDo;
            this.ghiChu = ghiChu == null ? "" : ghiChu;
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