package controller;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.KhachHang;

public class KhachHangController {
    public static final String[] DANH_SACH_COT = {
            "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
            "Địa chỉ", "Hạng thành viên"
    };

    public static final String[] TUY_CHON_LOC = {
            "Hạng thành viên: Tất cả", "Đồng", "Bạc", "Vàng", "Kim cương"
    };

    // Tương thích ngược
    public static final String[] CUSTOMER_COLUMNS = DANH_SACH_COT;
    public static final String[] FILTER_OPTIONS = TUY_CHON_LOC;

    private final KhachHang_DAO dao;

    public KhachHangController() {
        this(new KhachHang_DAO());
    }

    public KhachHangController(KhachHang_DAO dao) {
        this.dao = dao;
    }

    public String[] layDanhSachCot() {
        return DANH_SACH_COT.clone();
    }

    public String[] layDanhSachTuyChonLoc() {
        return TUY_CHON_LOC.clone();
    }

    public List<KhachHang> taiDanhSachKhachHang() {
        return java.util.Objects.requireNonNull(dao.getAllKhachHangDayDu(),
                "Dữ liệu khách hàng không được trả về từ DAO");
    }

    public Object[][] taiDuLieuBang() {
        List<KhachHang> dsKhachHang = taiDanhSachKhachHang();
        return chuyenThanhDuLieuBang(dsKhachHang);
    }

    public Object[][] chuyenThanhDuLieuBang(List<KhachHang> dsKhachHang) {
        Object[][] duLieu = new Object[dsKhachHang.size()][6];
        for (int i = 0; i < dsKhachHang.size(); i++) {
            KhachHang kh = dsKhachHang.get(i);
            duLieu[i][0] = kh.getMaKH();
            duLieu[i][1] = kh.getHoTen();
            duLieu[i][2] = kh.getSdt();
            duLieu[i][3] = kh.getEmail();
            duLieu[i][4] = kh.getDiaChi() == null ? "" : kh.getDiaChi();
            duLieu[i][5] = kh.getHangThanhVien() == null ? "Đồng" : kh.getHangThanhVien();
        }
        return duLieu;
    }

    public String taoRegexLoc(String luaChon) {
        if (luaChon == null || luaChon.equals("Hạng thành viên: Tất cả")) {
            return null;
        }
        return luaChon;
    }

    public static Color layMauHang(String tenHang) {
        if (tenHang == null) {
            return new Color(180, 83, 9);
        }
        if (tenHang.contains("Kim cương")) {
            return new Color(2, 132, 199);
        } else if (tenHang.contains("Vàng")) {
            return new Color(217, 119, 6);
        } else if (tenHang.contains("Bạc")) {
            return new Color(100, 116, 139);
        } else {
            return new Color(180, 83, 9);
        }
    }

    public CustomerStatistics layThongKe() {
        return new CustomerStatistics(dao.getTongSoKhachHang(), dao.getTongSoThuCung(),
                dao.getSoLuongThanhVienVangTroLen(), dao.getSoLuongKhachHangMoi());
    }

    public String phatSinhMaMoi() {
        return dao.phatSinhMaKHMoi();
    }

    public CustomerInputException kiemTraDauVao(String ma, String ten, String sdt, String email,
            String diaChi, boolean dangCapNhat) {
        if (ma.trim().isEmpty()) {
            return new CustomerInputException(InputField.CODE, "Vui lòng nhập mã khách hàng!");
        }
        if (ten.trim().isEmpty()) {
            return new CustomerInputException(InputField.NAME,
                    dangCapNhat ? "Họ và tên không được để trống!" : "Vui lòng nhập họ và tên khách hàng!");
        }
        if (!sdt.trim().matches("^0\\d{9}$")) {
            return new CustomerInputException(InputField.PHONE,
                    dangCapNhat ? "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!"
                            : "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (!email.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new CustomerInputException(InputField.EMAIL,
                    dangCapNhat ? "Email không đúng định dạng!"
                            : "Email không đúng định dạng (ví dụ: ten@gmail.com)!");
        }
        return null;
    }

    public boolean themKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        kiemTraVaNemLoi(ma, ten, sdt, email, diaChi, false);
        return dao.createKhachHang(new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi.trim()));
    }

    public boolean capNhatKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        kiemTraVaNemLoi(ma, ten, sdt, email, diaChi, true);
        return dao.updateKhachHang(new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi.trim()));
    }

    private void kiemTraVaNemLoi(String ma, String ten, String sdt, String email, String diaChi,
            boolean dangCapNhat) {
        CustomerInputException loi = kiemTraDauVao(ma, ten, sdt, email, diaChi, dangCapNhat);
        if (loi != null) {
            throw loi;
        }
    }

    public KhachHang layKhachHangTheoMa(String ma) {
        return dao.getKhachHangTheoMa(ma);
    }

    public CustomerDetails layChiTiet(String ma) {
        KhachHang customer = dao.getKhachHangTheoMa(ma);
        if (customer == null) {
            return null;
        }
        List<PetRow> pets = new ArrayList<>();
        for (Object[] row : java.util.Objects.requireNonNull(
                dao.getDanhSachThuCungCuaKhachHang(ma), "Dữ liệu thú cưng không được trả về từ DAO")) {
            pets.add(new PetRow(
                    (String) row[0], (String) row[1], (String) row[2],
                    (String) row[3], (Date) row[4], (String) row[5]));
        }
        List<InvoiceRow> invoices = new ArrayList<>();
        for (Object[] row : java.util.Objects.requireNonNull(
                dao.getDanhSachHoaDonCuaKhachHang(ma), "Dữ liệu hóa đơn không được trả về từ DAO")) {
            invoices.add(new InvoiceRow((String) row[0], (Date) row[1], (String) row[2],
                    ((Number) row[3]).doubleValue(), (String) row[4]));
        }
        return new CustomerDetails(customer, pets, invoices);
    }

    public DeleteResult xoaKhachHang(String ma) {
        if (dao.kiemTraRangBuoc(ma)) {
            return DeleteResult.BLOCKED_BY_RELATED_DATA;
        }
        return dao.deleteKhachHang(ma) ? DeleteResult.DELETED : DeleteResult.FAILED;
    }

    // Các hàm tương thích ngược (aliases)
    public String[] getCustomerColumns() { return layDanhSachCot(); }
    public String[] getFilterOptions() { return layDanhSachTuyChonLoc(); }
    public List<KhachHang> loadCustomers() { return taiDanhSachKhachHang(); }
    public Object[][] loadCustomerTableData() { return taiDuLieuBang(); }
    public Object[][] toCustomerTableData(List<KhachHang> customers) { return chuyenThanhDuLieuBang(customers); }
    public String buildFilterRegex(String luaChon) { return taoRegexLoc(luaChon); }
    public static Color getTierColor(String valStr) { return layMauHang(valStr); }
    public CustomerStatistics getStatistics() { return layThongKe(); }
    public String suggestNewCode() { return phatSinhMaMoi(); }
    public CustomerInputException validateInput(String code, String name, String phone, String email, String address, boolean updating) {
        return kiemTraDauVao(code, name, phone, email, address, updating);
    }
    public boolean create(String code, String name, String phone, String email, String address) {
        return themKhachHang(code, name, phone, email, address);
    }
    public boolean update(String code, String name, String phone, String email, String address) {
        return capNhatKhachHang(code, name, phone, email, address);
    }
    public KhachHang getCustomer(String code) { return layKhachHangTheoMa(code); }
    public CustomerDetails getDetails(String code) { return layChiTiet(code); }
    public DeleteResult delete(String code) { return xoaKhachHang(code); }

    public enum InputField { CODE, NAME, PHONE, EMAIL }

    public enum DeleteResult { DELETED, BLOCKED_BY_RELATED_DATA, FAILED }

    public static final class CustomerInputException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final InputField field;

        public CustomerInputException(InputField field, String message) {
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
        public final String averagePetsPerCustomerText;

        public final int tongKhachHang;
        public final int tongThuCung;
        public final int thanhVienVangTroLen;
        public final int khachHangMoi;
        public final String chuoiMoTaThuCungTrungBinh;

        private CustomerStatistics(int customers, int pets, int goldOrHigherMembers, int newCustomers) {
            this.customers = customers;
            this.pets = pets;
            this.goldOrHigherMembers = goldOrHigherMembers;
            this.newCustomers = newCustomers;
            this.averagePetsPerCustomerText = customers > 0
                    ? String.format(Locale.US, "%.1f thú cưng / khách", (double) pets / customers)
                    : "0.0 thú cưng / khách";

            this.tongKhachHang = customers;
            this.tongThuCung = pets;
            this.thanhVienVangTroLen = goldOrHigherMembers;
            this.khachHangMoi = newCustomers;
            this.chuoiMoTaThuCungTrungBinh = this.averagePetsPerCustomerText;
        }
    }

    public static final class CustomerDetails {
        public static final String[] COT_THU_CUNG = {
                "Mã TC", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh", "Đặc điểm"
        };
        public static final String[] COT_HOA_DON = {
                "Mã HĐ", "Ngày lập", "Trạng thái", "Số tiền (₫)", "Hình thức"
        };

        public static final String[] PET_COLUMNS = COT_THU_CUNG;
        public static final String[] INVOICE_COLUMNS = COT_HOA_DON;

        public final KhachHang customer;
        public final List<PetRow> pets;
        public final List<InvoiceRow> invoices;

        private CustomerDetails(KhachHang customer, List<PetRow> pets, List<InvoiceRow> invoices) {
            this.customer = customer;
            this.pets = pets;
            this.invoices = invoices;
        }

        public Object[][] layDuLieuBangThuCung() {
            Object[][] dataTC = new Object[pets.size()][6];
            for (int i = 0; i < pets.size(); i++) {
                PetRow pet = pets.get(i);
                dataTC[i] = new Object[] {
                        pet.code, pet.name, pet.species, pet.gender, pet.birthDate, pet.traits
                };
            }
            return dataTC;
        }

        public Object[][] layDuLieuBangHoaDon() {
            Object[][] dataHD = new Object[invoices.size()][5];
            for (int i = 0; i < invoices.size(); i++) {
                InvoiceRow invoice = invoices.get(i);
                dataHD[i][0] = invoice.code;
                dataHD[i][1] = invoice.date;
                dataHD[i][2] = invoice.status;
                dataHD[i][3] = String.format(Locale.US, "%,.0f ₫", invoice.amount).replace(',', '.');
                dataHD[i][4] = invoice.paymentMethod;
            }
            return dataHD;
        }

        public String layDiaChiHienThi() {
            return customer.getDiaChi() != null ? customer.getDiaChi() : "Chưa cập nhật";
        }

        public Object[][] getPetTableData() { return layDuLieuBangThuCung(); }
        public Object[][] getInvoiceTableData() { return layDuLieuBangHoaDon(); }
        public String getDiaChiHienThi() { return layDiaChiHienThi(); }
    }

    public static final class PetRow {
        public final String code;
        public final String name;
        public final String species;
        public final String gender;
        public final Date birthDate;
        public final String traits;

        public final String ma;
        public final String ten;
        public final String loai;
        public final String gioiTinh;
        public final Date ngaySinh;
        public final String dacDiem;

        private PetRow(String code, String name, String species, String gender, Date birthDate, String traits) {
            this.code = code;
            this.name = name;
            this.species = species;
            this.gender = gender;
            this.birthDate = birthDate;
            this.traits = traits;

            this.ma = code;
            this.ten = name;
            this.loai = species;
            this.gioiTinh = gender;
            this.ngaySinh = birthDate;
            this.dacDiem = traits;
        }
    }

    public static final class InvoiceRow {
        public final String code;
        public final Date date;
        public final String status;
        public final double amount;
        public final String paymentMethod;

        public final String ma;
        public final Date ngayLap;
        public final String trangThai;
        public final double soTien;
        public final String hinhThucThanhToan;

        private InvoiceRow(String code, Date date, String status, double amount, String paymentMethod) {
            this.code = code;
            this.date = date;
            this.status = status;
            this.amount = amount;
            this.paymentMethod = paymentMethod;

            this.ma = code;
            this.ngayLap = date;
            this.trangThai = status;
            this.soTien = amount;
            this.hinhThucThanhToan = paymentMethod;
        }
    }
}
