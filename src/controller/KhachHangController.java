package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.KhachHang;

public class KhachHangController {
    private final KhachHang_DAO dao;

    public KhachHangController() {
        this(new KhachHang_DAO());
    }

    KhachHangController(KhachHang_DAO dao) {
        this.dao = dao;
    }

    public List<KhachHang> loadCustomers() {
        return java.util.Objects.requireNonNull(dao.getAllKhachHangDayDu(),
                "Customer data was not returned by the DAO");
    }

    public CustomerStatistics getStatistics() {
        return new CustomerStatistics(dao.getTongSoKhachHang(), dao.getTongSoThuCung(),
                dao.getSoLuongThanhVienVangTroLen(), dao.getSoLuongKhachHangMoi());
    }

    public String suggestNewCode() {
        return dao.phatSinhMaKHMoi();
    }

    public CustomerInputException validateInput(String code, String name, String phone, String email,
            String address, boolean updating) {
        if (code.trim().isEmpty()) {
            return new CustomerInputException(InputField.CODE, "Vui lòng nhập mã khách hàng!");
        }
        if (name.trim().isEmpty()) {
            return new CustomerInputException(InputField.NAME,
                    updating ? "Họ và tên không được để trống!" : "Vui lòng nhập họ và tên khách hàng!");
        }
        if (!phone.trim().matches("^0\\d{9}$")) {
            return new CustomerInputException(InputField.PHONE,
                    updating ? "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!"
                            : "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (!email.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new CustomerInputException(InputField.EMAIL,
                    updating ? "Email không đúng định dạng!"
                            : "Email không đúng định dạng (ví dụ: ten@gmail.com)!");
        }
        return null;
    }

    public boolean create(String code, String name, String phone, String email, String address) {
        validateOrThrow(code, name, phone, email, address, false);
        return dao.createKhachHang(new KhachHang(
                code.trim(), name.trim(), phone.trim(), email.trim(), address.trim()));
    }

    public boolean update(String code, String name, String phone, String email, String address) {
        validateOrThrow(code, name, phone, email, address, true);
        return dao.updateKhachHang(new KhachHang(
                code.trim(), name.trim(), phone.trim(), email.trim(), address.trim()));
    }

    private void validateOrThrow(String code, String name, String phone, String email, String address,
            boolean updating) {
        CustomerInputException error = validateInput(code, name, phone, email, address, updating);
        if (error != null) {
            throw error;
        }
    }

    public KhachHang getCustomer(String code) {
        return dao.getKhachHangTheoMa(code);
    }

    public CustomerDetails getDetails(String code) {
        KhachHang customer = dao.getKhachHangTheoMa(code);
        if (customer == null) {
            return null;
        }
        List<PetRow> pets = new ArrayList<>();
        for (Object[] row : java.util.Objects.requireNonNull(
                dao.getDanhSachThuCungCuaKhachHang(code), "Pet details were not returned by the DAO")) {
            pets.add(new PetRow(
                    (String) row[0], (String) row[1], (String) row[2],
                    (String) row[3], (Date) row[4], (String) row[5]));
        }
        List<InvoiceRow> invoices = new ArrayList<>();
        for (Object[] row : java.util.Objects.requireNonNull(
                dao.getDanhSachHoaDonCuaKhachHang(code), "Invoice details were not returned by the DAO")) {
            invoices.add(new InvoiceRow((String) row[0], (Date) row[1], (String) row[2],
                    ((Number) row[3]).doubleValue(), (String) row[4]));
        }
        return new CustomerDetails(customer, pets, invoices);
    }

    public DeleteResult delete(String code) {
        if (dao.kiemTraRangBuoc(code)) {
            return DeleteResult.BLOCKED_BY_RELATED_DATA;
        }
        return dao.deleteKhachHang(code) ? DeleteResult.DELETED : DeleteResult.FAILED;
    }

    public enum InputField { CODE, NAME, PHONE, EMAIL }

    public enum DeleteResult { DELETED, BLOCKED_BY_RELATED_DATA, FAILED }

    public static final class CustomerInputException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final InputField field;

        private CustomerInputException(InputField field, String message) {
            super(message);
            this.field = field;
        }

        public InputField getField() {
            return field;
        }
    }

    public static final class CustomerStatistics {
        public final int customers;
        public final int pets;
        public final int goldOrHigherMembers;
        public final int newCustomers;

        private CustomerStatistics(int customers, int pets, int goldOrHigherMembers, int newCustomers) {
            this.customers = customers;
            this.pets = pets;
            this.goldOrHigherMembers = goldOrHigherMembers;
            this.newCustomers = newCustomers;
        }
    }

    public static final class CustomerDetails {
        public final KhachHang customer;
        public final List<PetRow> pets;
        public final List<InvoiceRow> invoices;

        private CustomerDetails(KhachHang customer, List<PetRow> pets, List<InvoiceRow> invoices) {
            this.customer = customer;
            this.pets = pets;
            this.invoices = invoices;
        }
    }

    public static final class PetRow {
        public final String code;
        public final String name;
        public final String species;
        public final String gender;
        public final Date birthDate;
        public final String traits;

        private PetRow(String code, String name, String species, String gender, Date birthDate, String traits) {
            this.code = code;
            this.name = name;
            this.species = species;
            this.gender = gender;
            this.birthDate = birthDate;
            this.traits = traits;
        }
    }

    public static final class InvoiceRow {
        public final String code;
        public final Date date;
        public final String status;
        public final double amount;
        public final String paymentMethod;

        private InvoiceRow(String code, Date date, String status, double amount, String paymentMethod) {
            this.code = code;
            this.date = date;
            this.status = status;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }
    }
}
