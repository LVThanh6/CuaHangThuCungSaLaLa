package Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;

import dao.VatTu_DAO;
import entity.NhaCungCap_entity;
import entity.VatTu_entity;

public class VatTuService {
    private static final int NGUONG_SAP_HET = 30;
    private final VatTu_DAO vatTuDao;

    public VatTuService() {
        this(new VatTu_DAO());
    }

    VatTuService(VatTu_DAO vatTuDao) {
        this.vatTuDao = vatTuDao;
    }

    public List<VatTu> taiDanhSach() {
        List<VatTu> result = new ArrayList<>();
        for (VatTu_entity entity : vatTuDao.getAllVatTu()) {
            boolean laThuoc = entity.getMaThuoc() != null
                    && entity.getMaThuoc().getMaThuoc() != null
                    && !entity.getMaThuoc().getMaThuoc().isBlank();
            VatTu vatTu = new VatTu(
                    entity.getMaVT(), entity.getTenVT(),
                    laThuoc ? "Thuốc điều trị" : "Vật tư thông thường",
                    entity.getSoLuongTon(), laThuoc ? "Viên" : "Gói / Hộp",
                    Math.round(entity.getGiaNhap()),
                    entity.getMaThuoc() == null ? null : entity.getMaThuoc().getMaThuoc(),
                    entity.getMaNCC() == null ? null : entity.getMaNCC().getMaNCC(),
                    entity.getTrangThai(), entity.getGiaBanCoBan());
            result.add(vatTu);
        }
        return result;
    }

    public List<SupplierOption> taiNhaCungCap() {
        List<SupplierOption> result = new ArrayList<>();
        for (String[] row : vatTuDao.getDanhSachNCC()) {
            result.add(new SupplierOption(row[0], row[1]));
        }
        return result;
    }

    public String taoMaMoi() {
        return vatTuDao.taoMaVatTuMoi();
    }

    public SaveResult luu(VatTuInput input, boolean editing) {
        String validationError = validate(input, editing);
        if (validationError != null) {
            return new SaveResult(false, validationError);
        }
        NhaCungCap_entity supplier = new NhaCungCap_entity(input.maNCC, null, null, null, null);
        VatTu_entity entity = new VatTu_entity(
                input.ma, supplier, input.ten, input.soLuongTon, input.trangThai,
                input.giaBan, input.giaNhap, input.maThuoc);
        boolean saved = editing ? vatTuDao.updateVatTu(entity) : vatTuDao.createVatTu(entity);
        String error = editing
                ? "Không thể cập nhật vật tư. Kiểm tra lại mã nhà cung cấp, mã thuốc hoặc kết nối CSDL."
                : "Không thể thêm vật tư. Kiểm tra lại mã nhà cung cấp, mã thuốc hoặc kết nối CSDL.";
        return new SaveResult(saved, saved ? null : error);
    }

    public boolean xoa(String maVatTu) {
        return vatTuDao.deleteVatTu(maVatTu);
    }

    public boolean maVatTuTonTai(String maVatTu) {
        return vatTuDao.getVatTuTheoMa(maVatTu) != null;
    }

    public int tonKho(String maVatTu, List<VatTu> vatTu) {
        for (VatTu item : vatTu) {
            if (item.ma.equals(maVatTu)) {
                return item.soLuongTon;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vật tư: " + maVatTu);
    }

    public void capNhatTon(String maVatTu, int thayDoi, List<VatTu> vatTu) {
        for (VatTu item : vatTu) {
            if (item.ma.equals(maVatTu)) {
                item.soLuongTon += thayDoi;
                return;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vật tư: " + maVatTu);
    }

    public List<CatalogItem> danhMucVatTu(List<VatTu> vatTu) {
        List<CatalogItem> result = new ArrayList<>();
        for (VatTu item : vatTu) {
            result.add(new CatalogItem(item.ma, item.ten, item.donVi, item.giaNhap));
        }
        return result;
    }

    public List<VatTu> loc(List<VatTu> source, String tuKhoa, String danhMuc,
                           TrangThai trangThai, Boolean sapXepTang) {
        String keyword = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<VatTu> result = new ArrayList<>();
        for (VatTu item : source) {
            if (danhMuc != null && !danhMuc.isEmpty() && !danhMuc.equals(item.danhMuc)) continue;
            if (trangThai != null && trangThai(item.soLuongTon) != trangThai) continue;
            if (!keyword.isEmpty() && !boDau(item.ten).contains(keyword)
                    && !boDau(item.ma).contains(keyword)) continue;
            result.add(item);
        }
        if (sapXepTang != null) {
            Comparator<VatTu> comparator = Comparator.comparing(VatTu::getTen,
                    java.text.Collator.getInstance(Locale.forLanguageTag("vi-VN")));
            if (!sapXepTang) comparator = comparator.reversed();
            result.sort(comparator);
        }
        return result;
    }

    public InventorySummary thongKe(List<VatTu> vatTu) {
        int conHang = 0, sapHet = 0, hetTon = 0;
        for (VatTu item : vatTu) {
            switch (trangThai(item.soLuongTon)) {
                case CON_HANG: conHang++; break;
                case SAP_HET: sapHet++; break;
                case HET_TON: hetTon++; break;
            }
        }
        return new InventorySummary(vatTu.size(), conHang, sapHet, hetTon);
    }

    public static TrangThai trangThai(int soLuong) {
        if (soLuong <= 0) return TrangThai.HET_TON;
        if (soLuong <= NGUONG_SAP_HET) return TrangThai.SAP_HET;
        return TrangThai.CON_HANG;
    }

    public String validate(VatTuInput input, boolean editing) {
        if (input.ma == null || input.ma.isBlank()) return "Vui lòng nhập mã vật tư.";
        if (!editing && maVatTuTonTai(input.ma)) return "Mã vật tư \"" + input.ma + "\" đã tồn tại.";
        if (input.ten == null || input.ten.isBlank()) return "Vui lòng nhập tên vật tư.";
        if (input.maNCC == null || input.maNCC.isBlank()) return "Vui lòng chọn nhà cung cấp.";
        if (input.laThuoc && (input.maThuoc == null || input.maThuoc.isBlank())) {
            return "Danh mục \"Thuốc điều trị\" cần nhập mã thuốc.";
        }
        if (input.giaNhap < 0) return "Giá nhập không hợp lệ.";
        if (input.giaBan < 0) return "Giá bán cơ bản không hợp lệ.";
        if (input.soLuongTon < 0) return "Tồn kho không hợp lệ.";
        return null;
    }

    private static String boDau(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.forLanguageTag("vi-VN")),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.replace('đ', 'd');
    }

    public enum TrangThai {
        CON_HANG("Còn hàng"), SAP_HET("Sắp hết"), HET_TON("Hết tồn");
        public final String label;
        TrangThai(String label) { this.label = label; }
    }

    public static final class VatTu {
        private final String ma, ten, danhMuc, donVi, maThuoc, maNCC;
        private final long giaNhap, giaBan;
        private int soLuongTon;
        private String trangThai;

        public VatTu(String ma, String ten, String danhMuc, int soLuongTon, String donVi,
                     long giaNhap, String maThuoc, String maNCC, String trangThai, double giaBan) {
            this.ma = ma;
            this.ten = ten;
            this.danhMuc = danhMuc;
            this.soLuongTon = soLuongTon;
            this.donVi = donVi;
            this.giaNhap = giaNhap;
            this.maThuoc = maThuoc;
            this.maNCC = maNCC;
            this.trangThai = trangThai;
            this.giaBan = Math.round(giaBan);
        }

        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDanhMuc() { return danhMuc; }
        public int getTonKho() { return soLuongTon; }
        public String getDonVi() { return donVi; }
        public long getGiaNhap() { return giaNhap; }
        public String getMaThuoc() { return maThuoc; }
        public String getMaNCC() { return maNCC; }
        public String getTrangThai() { return trangThai; }
        public long getGiaBan() { return giaBan; }
    }

    public static final class VatTuInput {
        private final String ma, ten, maNCC, maThuoc;
        private final boolean laThuoc;
        private final int soLuongTon;
        private final long giaNhap, giaBan;
        private final String trangThai;

        public VatTuInput(String ma, String ten, String maNCC, String maThuoc, boolean laThuoc,
                          int soLuongTon, long giaNhap, long giaBan, String trangThai) {
            this.ma = ma;
            this.ten = ten;
            this.maNCC = maNCC;
            this.maThuoc = maThuoc;
            this.laThuoc = laThuoc;
            this.soLuongTon = soLuongTon;
            this.giaNhap = giaNhap;
            this.giaBan = giaBan;
            this.trangThai = trangThai;
        }
        public String getMa() { return ma; }
    }

    public static final class SupplierOption {
        private final String code, name;
        public SupplierOption(String code, String name) { this.code = code; this.name = name; }
        public String getCode() { return code; }
        public String getName() { return name; }
    }

    public static final class CatalogItem {
        private final String code, name, unit;
        private final long purchasePrice;
        public CatalogItem(String code, String name, String unit, long purchasePrice) {
            this.code = code; this.name = name; this.unit = unit; this.purchasePrice = purchasePrice;
        }
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public long getPurchasePrice() { return purchasePrice; }
    }

    public static final class SaveResult {
        private final boolean saved;
        private final String error;
        private SaveResult(boolean saved, String error) { this.saved = saved; this.error = error; }
        public boolean isSaved() { return saved; }
        public String getError() { return error; }
    }

    public static final class InventorySummary {
        private final int total, sufficient, low, empty;
        private InventorySummary(int total, int sufficient, int low, int empty) {
            this.total = total; this.sufficient = sufficient; this.low = low; this.empty = empty;
        }
        public int getTotal() { return total; }
        public int getSufficient() { return sufficient; }
        public int getLow() { return low; }
        public int getEmpty() { return empty; }
    }

    public static Long docSoTien(String text) {
        String digits = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
