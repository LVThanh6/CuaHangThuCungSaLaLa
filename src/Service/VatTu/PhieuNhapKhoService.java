package Service.VatTu;

import connectDB.ConnectDB;
import dao.VatTu.PhieuNhapKho_DAO;
import entity.VatTu.PhieuNhapKho_entity;
import entity.VatTu.PhieuNhapKho_entity.ChiTiet;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service "Phiếu nhập kho" - cầu nối giữa PhieuNhapKhoGUI và CSDL SALALA.
 *
 * Không còn dữ liệu tĩnh: mọi thứ đọc/ghi qua PhieuNhapKho_DAO (ConnectDB -> SQL Server).
 * Các lớp lồng (Phieu, Dong, CatalogItem, Filter, Draft, TrangThai) là mô hình hiển thị cho GUI;
 * controller chuyển đổi qua lại với entity.VatTu.PhieuNhapKho_entity.
 */
public class PhieuNhapKhoService {

    // =====================================================================
    // Mô hình dữ liệu cho GUI
    // =====================================================================
    public enum TrangThai {
        CHO_XAC_NHAN("Chờ xác nhận", PhieuNhapKho_entity.TrangThai.CHO_XAC_NHAN),
        DA_XAC_NHAN("Đã xác nhận", PhieuNhapKho_entity.TrangThai.DA_XAC_NHAN),
        DA_HUY("Đã hủy", PhieuNhapKho_entity.TrangThai.DA_HUY);

        public final String text;
        private final PhieuNhapKho_entity.TrangThai entity;

        TrangThai(String text, PhieuNhapKho_entity.TrangThai entity) {
            this.text = text;
            this.entity = entity;
        }

        static TrangThai of(PhieuNhapKho_entity.TrangThai e) {
            for (TrangThai t : values()) if (t.entity == e) return t;
            return CHO_XAC_NHAN;
        }
    }

    /** Một dòng vật tư trong phiếu. */
    public static class Dong {
        public String ma, ten, donVi;
        public int sl;
        public long donGia;

        public Dong(String ma, String ten, String donVi, int sl, long donGia) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.sl = sl; this.donGia = donGia;
        }
    }

    /** Một vật tư trong danh mục (bảng VatTu) để chọn khi lập phiếu. */
    public static class CatalogItem {
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

    /** Một phiếu nhập (header + dòng). maNV / maNCC là khóa ngoại; nhanVien / nhaCungCap là tên hiển thị. */
    public static class Phieu {
        public String so;                 // maPNK
        public LocalDate ngay;            // ngayNhap
        public String maNV, nhanVien;     // maNhanVien, hoTen
        public String maNCC, nhaCungCap;  // maNCC, tenNCC
        public String ghiChu = "";
        public List<Dong> dong = new ArrayList<>();
        public TrangThai tt = TrangThai.CHO_XAC_NHAN;
    }

    /** Bộ lọc danh sách. */
    public static class Filter {
        public final String keyword;
        public final TrangThai status;
        public final LocalDate from, to;

        public Filter(String keyword, TrangThai status, LocalDate from, LocalDate to) {
            this.keyword = keyword == null ? "" : keyword;
            this.status = status; this.from = from; this.to = to;
        }
    }

    /** Dữ liệu nhập từ form để kiểm tra hợp lệ (ncc = mã NCC). */
    public static class Draft {
        public final LocalDate ngay;
        public final String nhaCungCap;   // mã NCC (maNCC) chọn trên form
        public final List<Dong> lines;

        public Draft(LocalDate ngay, String nhaCungCap, List<Dong> lines) {
            this.ngay = ngay; this.nhaCungCap = nhaCungCap; this.lines = new ArrayList<>(lines);
        }
    }

    // =====================================================================
    // Trạng thái nội bộ
    // =====================================================================
    private final PhieuNhapKho_DAO dao = new PhieuNhapKho_DAO();

    /** Bộ nhớ đệm danh sách phiếu: gõ tìm kiếm không phải truy vấn lại CSDL. */
    private List<Phieu> cache;
    private Map<String, String> tenNV = new HashMap<>();
    private Map<String, String> tenNCC = new HashMap<>();

    // =====================================================================
    // Kết nối / tải dữ liệu
    // =====================================================================
    public boolean isConnected() {
        return ConnectDB.getInstance().getConnection() != null;
    }

    /** Bỏ bộ nhớ đệm: lần gọi loc() kế tiếp sẽ đọc lại CSDL. */
    public void refresh() {
        cache = null;
    }

    private void ensureLoaded() {
        if (cache != null) return;
        tenNV = toMap(dao.getNhanVienOptions());
        tenNCC = toMap(dao.getNhaCungCapOptions());
        List<Phieu> ds = new ArrayList<>();
        for (PhieuNhapKho_entity e : dao.getAll()) ds.add(fromEntity(e));
        cache = ds;
    }

    public List<Phieu> getAll() {
        ensureLoaded();
        return new ArrayList<>(cache);
    }

    /** Lọc theo từ khóa (mã phiếu, nhân viên, NCC), trạng thái, khoảng ngày lập. */
    public List<Phieu> loc(Filter f) {
        ensureLoaded();
        String kw = f.keyword.trim().toLowerCase(Locale.ROOT);
        List<Phieu> out = new ArrayList<>();
        for (Phieu p : cache) {
            if (f.status != null && p.tt != f.status) continue;
            if (f.from != null && p.ngay.isBefore(f.from)) continue;
            if (f.to != null && p.ngay.isAfter(f.to)) continue;
            if (!kw.isEmpty()
                    && !contains(p.so, kw) && !contains(p.nhanVien, kw) && !contains(p.maNV, kw)
                    && !contains(p.nhaCungCap, kw) && !contains(p.maNCC, kw)) continue;
            out.add(p);
        }
        return out;
    }

    private static boolean contains(String s, String kw) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(kw);
    }

    // =====================================================================
    // Danh mục cho form lập / sửa phiếu
    // =====================================================================
    public String taoMaPhieuMoi() { return dao.taoMaPhieuMoi(); }

    /** {maNCC, tenNCC} */
    public List<String[]> getNhaCungCapOptions() { return dao.getNhaCungCapOptions(); }

    /** {maNhanVien, hoTen} */
    public List<String[]> getNhanVienOptions() { return dao.getNhanVienOptions(); }

    public List<CatalogItem> getCatalog() {
        List<CatalogItem> out = new ArrayList<>();
        for (Object[] r : dao.getVatTuCatalog()) {
            out.add(new CatalogItem((String) r[0], (String) r[1], (String) r[2], ((Number) r[3]).longValue()));
        }
        return out;
    }

    /** Tên nhân viên theo mã; không có -> null. */
    public String tenNhanVien(String maNV) {
        if (maNV == null) return null;
        for (String[] r : dao.getNhanVienOptions()) if (r[0].equals(maNV)) return r[1];
        return null;
    }

    // =====================================================================
    // Ghi dữ liệu (đều trả về false khi CSDL từ chối / lỗi)
    // =====================================================================
    public boolean add(Phieu p) {
        boolean ok = dao.insert(toEntity(p));
        refresh();
        return ok;
    }

    /** Sửa phiếu đang Chờ xác nhận (np mang số phiếu cần sửa). */
    public boolean update(Phieu old, Phieu np) {
        np.so = old.so;
        boolean ok = dao.update(toEntity(np));
        refresh();
        return ok;
    }

    /** Xác nhận phiếu và cộng tồn kho (VatTu.soLuongTon) trong cùng transaction. */
    public boolean confirm(Phieu p) {
        boolean ok = dao.xacNhan(p.so);
        refresh();
        return ok;
    }

    public boolean cancel(Phieu p) {
        boolean ok = dao.huy(p.so);
        refresh();
        return ok;
    }

    // =====================================================================
    // Chuyển đổi entity <-> mô hình GUI
    // =====================================================================
    private Phieu fromEntity(PhieuNhapKho_entity e) {
        Phieu p = new Phieu();
        p.so = e.getMaPNK();
        p.ngay = e.getNgayNhap();
        p.maNV = e.getNhanVien() != null ? e.getNhanVien().getMaNhanVien() : null;
        p.nhanVien = tenNV.getOrDefault(p.maNV, p.maNV == null ? "" : p.maNV);
        p.maNCC = e.getNhaCungCap() != null ? e.getNhaCungCap().getMaNCC() : null;
        p.nhaCungCap = tenNCC.getOrDefault(p.maNCC, p.maNCC == null ? "" : p.maNCC);
        p.ghiChu = e.getGhiChu() == null ? "" : e.getGhiChu();
        p.tt = TrangThai.of(e.getTrangThai());
        for (ChiTiet d : e.getChiTiet()) {
            p.dong.add(new Dong(d.getMaVT() != null ? d.getMaVT().getMaVT() : null,
                    d.getTenVT(), d.getDonVi(), d.getSoLuong(), d.getDonGia()));
        }
        return p;
    }

    private PhieuNhapKho_entity toEntity(Phieu p) {
        PhieuNhapKho_entity e = new PhieuNhapKho_entity(p.so, p.ngay, p.maNV, p.maNCC, p.ghiChu,
                PhieuNhapKho_entity.TrangThai.fromText(p.tt.text));
        List<ChiTiet> ds = new ArrayList<>();
        for (Dong d : p.dong) ds.add(new ChiTiet(d.ma, d.ten, d.donVi, d.sl, d.donGia));
        e.setChiTiet(ds);
        return e;
    }

    private static Map<String, String> toMap(List<String[]> rows) {
        Map<String, String> m = new HashMap<>();
        for (String[] r : rows) m.put(r[0], r[1]);
        return m;
    }

    // =====================================================================
    // Nghiệp vụ dòng vật tư & kiểm tra hợp lệ
    // =====================================================================
    public Dong taoDong(CatalogItem item, int sl, long donGia) {
        return new Dong(item.getMa(), item.getTen(), item.getDonVi(), sl, donGia);
    }

    /** Thêm dòng vào danh sách; nếu vật tư đã có thì cộng dồn số lượng và lấy đơn giá mới. */
    public void mergeLine(List<Dong> lines, Dong add) {
        for (Dong d : lines) {
            if (d.ma != null && d.ma.equals(add.ma)) {
                d.sl += add.sl;
                d.donGia = add.donGia;
                return;
            }
        }
        lines.add(add);
    }

    /** Trả về thông báo lỗi, hoặc null nếu hợp lệ. */
    public String validate(Draft d) {
        if (d.ngay == null) return "Ngày lập không hợp lệ.";
        if (d.nhaCungCap == null || d.nhaCungCap.trim().isEmpty()) return "Vui lòng chọn nhà cung cấp.";
        if (d.lines.isEmpty()) return "Phiếu cần có ít nhất một vật tư.";
        for (Dong l : d.lines) {
            if (l.sl <= 0) return "Số lượng của \"" + l.ten + "\" phải lớn hơn 0.";
            if (l.donGia < 0) return "Đơn giá của \"" + l.ten + "\" không được âm.";
        }
        return null;
    }

    // =====================================================================
    // Hàm tĩnh dùng chung
    // =====================================================================
    public static long thanhTien(Dong d) { return (long) d.sl * d.donGia; }

    public static long tongTien(Phieu p) {
        long t = 0;
        for (Dong d : p.dong) t += thanhTien(d);
        return t;
    }

    public static int tongSoLuong(Phieu p) {
        int t = 0;
        for (Dong d : p.dong) t += d.sl;
        return t;
    }

    /** Ô trống -> null; sai định dạng -> ném DateTimeParseException. */
    public static LocalDate parseDate(String s, DateTimeFormatter df) {
        if (s == null || s.trim().isEmpty()) return null;
        return LocalDate.parse(s.trim(), df);
    }

    public static String validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            return "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.";
        }
        return null;
    }

    public static Long parsePrice(String text) {
        try {
            long value = Long.parseLong(text.trim().replace(".", "").replace(",", ""));
            return value < 0 ? null : value;
        } catch (NumberFormatException exception) {
            return null;
        }
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

    private static String csv(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }
}