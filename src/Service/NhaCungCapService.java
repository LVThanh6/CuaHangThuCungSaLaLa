package Service;

import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import java.util.Locale;

import dao.NhaCungCap_DAO;
import entity.NhaCungCap_entity;

public class NhaCungCapService {
    private final NhaCungCap_DAO nhaCungCapDao;

    public NhaCungCapService() {
        this(new NhaCungCap_DAO());
    }

    NhaCungCapService(NhaCungCap_DAO nhaCungCapDao) {
        this.nhaCungCapDao = nhaCungCapDao;
    }

    public List<Supplier> taiDanhSach() {
        List<Supplier> result = new ArrayList<>();
        for (NhaCungCap_entity entity : nhaCungCapDao.getAllNhaCungCap()) {
            result.add(new Supplier(entity.getMaNCC(), entity.getTenNCC(),
                    entity.getDiaChi(), entity.getSdt(), entity.getEmail()));
        }
        return result;
    }

    public OperationResult tao(SupplierInput input) {
        String error = validate(input);
        if (error != null) return new OperationResult(false, error);
        boolean saved = nhaCungCapDao.createNhaCungCap(toEntity(input));
        return result(saved, "Không thể thêm nhà cung cấp. Kiểm tra dữ liệu hoặc kết nối CSDL.");
    }

    public OperationResult capNhat(SupplierInput input) {
        String error = validate(input);
        if (error != null) return new OperationResult(false, error);
        boolean saved = nhaCungCapDao.updateNhaCungCap(toEntity(input));
        return result(saved, "Không thể cập nhật nhà cung cấp. Kiểm tra dữ liệu hoặc kết nối CSDL.");
    }

    public OperationResult xoa(String maNCC) {
        if (maNCC == null || maNCC.isBlank()) return new OperationResult(false, "Mã nhà cung cấp không hợp lệ.");
        boolean deleted = nhaCungCapDao.deleteNhaCungCap(maNCC);
        return result(deleted, "Không thể xóa nhà cung cấp. Có thể nhà cung cấp đang được sử dụng.");
    }

    public String taoMaMoi(List<Supplier> suppliers) {
        int max = 0;
        for (Supplier supplier : suppliers) {
            String code = supplier.getCode();
            if (code != null && code.startsWith("NCC")) {
                String suffix = code.substring(3);
                if (suffix.matches("\\d+")) {
                    try {
                        int number = Integer.parseInt(suffix);
                        max = Math.max(max, number);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return String.format("NCC%03d", max + 1);
    }

    public boolean phuHopTimKiem(Supplier supplier, String keyword) {
        String normalizedKeyword = normalize(keyword);
        return normalizedKeyword.isEmpty()
                || normalize(supplier.code).contains(normalizedKeyword)
                || normalize(supplier.name).contains(normalizedKeyword)
                || normalize(supplier.address).contains(normalizedKeyword)
                || normalize(supplier.phone).contains(normalizedKeyword)
                || normalize(supplier.email).contains(normalizedKeyword);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.toLowerCase(Locale.forLanguageTag("vi-VN")).trim(),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    public static String validate(SupplierInput input) {
        if (input == null || input.ma == null || input.ma.isBlank()) return "Mã nhà cung cấp không được để trống.";
        if (input.ten == null || input.ten.isBlank()) return "Tên nhà cung cấp không được để trống.";
        return null;
    }

    private static NhaCungCap_entity toEntity(SupplierInput input) {
        return new NhaCungCap_entity(input.ma, input.ten, input.diaChi, input.sdt, input.email);
    }

    private static OperationResult result(boolean success, String error) {
        return new OperationResult(success, success ? null : error);
    }

    public static final class Supplier {
        private final String code, name, address, phone, email;
        public Supplier(String code, String name, String address, String phone, String email) {
            this.code = code; this.name = name; this.address = address; this.phone = phone; this.email = email;
        }
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }

    public static final class SupplierInput {
        private final String ma, ten, diaChi, sdt, email;
        public SupplierInput(String ma, String ten, String diaChi, String sdt, String email) {
            this.ma = ma; this.ten = ten; this.diaChi = diaChi; this.sdt = sdt; this.email = email;
        }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDiaChi() { return diaChi; }
        public String getSdt() { return sdt; }
        public String getEmail() { return email; }
    }

    public static final class OperationResult {
        private final boolean successful;
        private final String error;
        private OperationResult(boolean successful, String error) {
            this.successful = successful; this.error = error;
        }
        public boolean isSuccessful() { return successful; }
        public String getError() { return error; }
    }
}
