package Service.KhachHang;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dao.KhachHang.KhachHang_DAO;
import entity.KhachHang.KhachHang;

public class KhachHangService {
    public static final String[] DANH_SACH_COT = {
            "Mã khách hàng", "Họ tên", "Số điện thoại", "Email",
            "Địa chỉ", "Hạng thành viên"
    };

    public static final String[] TUY_CHON_LOC = {
            "Hạng thành viên: Tất cả", "Đồng", "Bạc", "Vàng", "Kim cương"
    };

    public static final int COT_MA_KH = 0;
    public static final int COT_HO_TEN = 1;
    public static final int COT_SO_DIEN_THOAI = 2;
    public static final int COT_EMAIL = 3;
    public static final int COT_DIA_CHI = 4;
    public static final int COT_HANG_THANH_VIEN = 5;

    private final KhachHang_DAO dao;

    public KhachHangService() {
        this(new KhachHang_DAO());
    }

    public KhachHangService(KhachHang_DAO dao) {
        this.dao = dao;
    }

    public String[] layDanhSachCot() {
        return DANH_SACH_COT.clone();
    }

    public String[] layDanhSachTuyChonLoc() {
        return TUY_CHON_LOC.clone();
    }

    public String layLuaChonLocMacDinh() {
        return TUY_CHON_LOC[0];
    }

    public int layViTriCotMaKhachHang() {
        return COT_MA_KH;
    }

    public int layViTriCotHoTen() {
        return COT_HO_TEN;
    }

    public int layViTriCotHangThanhVien() {
        return COT_HANG_THANH_VIEN;
    }

    public boolean coTheThaoTacDuyNhat(int soDongDuocChon) {
        return soDongDuocChon == 1;
    }

    public static Color layMauNenDong(int soDong) {
        return soDong % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);
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
        if (luaChon == null || luaChon.equals(TUY_CHON_LOC[0])) {
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

    private KetQuaXuLyKhachHang kiemTraDauVao(String ma, String ten, String sdt, String email, boolean dangCapNhat) {
        if (ma == null || ma.trim().isEmpty()) {
            return new KetQuaXuLyKhachHang(false, true, InputField.CODE, "Vui lòng nhập mã khách hàng!");
        }
        if (ten == null || ten.trim().isEmpty()) {
            return new KetQuaXuLyKhachHang(false, true, InputField.NAME,
                    dangCapNhat ? "Họ và tên không được để trống!" : "Vui lòng nhập họ và tên khách hàng!");
        }
        if (sdt == null || !sdt.trim().matches("^0\\d{9}$")) {
            return new KetQuaXuLyKhachHang(false, true, InputField.PHONE,
                    dangCapNhat ? "Số điện thoại phải có 10 chữ số bắt đầu bằng 0!"
                            : "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (email == null || !email.trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new KetQuaXuLyKhachHang(false, true, InputField.EMAIL,
                    dangCapNhat ? "Email không đúng định dạng!"
                            : "Email không đúng định dạng (ví dụ: ten@gmail.com)!");
        }
        return null;
    }

    public KetQuaXuLyKhachHang xuLyThemKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        KetQuaXuLyKhachHang loi = kiemTraDauVao(ma, ten, sdt, email, false);
        if (loi != null) {
            return loi;
        }
        boolean thanhCong = dao.createKhachHang(new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi != null ? diaChi.trim() : ""));
        if (thanhCong) {
            return new KetQuaXuLyKhachHang(true, false, null, "Thêm khách hàng thành công!");
        }
        return new KetQuaXuLyKhachHang(false, false, null,
                "Thêm thất bại! Vui lòng kiểm tra xem Mã khách hàng đã tồn tại chưa.");
    }

    public KetQuaXuLyKhachHang xuLyCapNhatKhachHang(String ma, String ten, String sdt, String email, String diaChi) {
        KetQuaXuLyKhachHang loi = kiemTraDauVao(ma, ten, sdt, email, true);
        if (loi != null) {
            return loi;
        }
        boolean thanhCong = dao.updateKhachHang(new KhachHang(
                ma.trim(), ten.trim(), sdt.trim(), email.trim(), diaChi != null ? diaChi.trim() : ""));
        if (thanhCong) {
            return new KetQuaXuLyKhachHang(true, false, null, "Cập nhật thông tin khách hàng thành công!");
        }
        return new KetQuaXuLyKhachHang(false, false, null, "Cập nhật thất bại!");
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

    public String taoThongDiepXacNhanXoa(String hoTen, String maKH) {
        return "Bạn có chắc chắn muốn xóa khách hàng:\n" + hoTen + " (Mã: " + maKH + ") không?";
    }

    public KetQuaXoaKhachHang xuLyXoaKhachHang(String maKH) {
        if (dao.kiemTraRangBuoc(maKH)) {
            return new KetQuaXoaKhachHang(false, true,
                    "Khách hàng " + maKH + " đang có dữ liệu thú cưng hoặc hóa đơn liên quan trong hệ thống.\n" +
                            "Để đảm bảo tính toàn vẹn dữ liệu kế toán, không thể xóa khách hàng này!");
        }
        boolean thanhCong = dao.deleteKhachHang(maKH);
        if (thanhCong) {
            return new KetQuaXoaKhachHang(true, false, "Đã xóa khách hàng " + maKH + " thành công!");
        }
        return new KetQuaXoaKhachHang(false, false, "Xóa khách hàng thất bại!");
    }

    public String layThongDiepKhongTimThayKhachHang() {
        return "Không tìm thấy thông tin khách hàng!";
    }

    public String layTieuDeDialogSua(String maKH) {
        return "Chỉnh sửa thông tin - " + maKH;
    }

    public enum InputField { CODE, NAME, PHONE, EMAIL }

    public static final class KetQuaXuLyKhachHang {
        private final boolean thanhCong;
        private final boolean loiNhapLieu;
        private final InputField truongLoi;
        private final String thongBao;

        public KetQuaXuLyKhachHang(boolean thanhCong, boolean loiNhapLieu, InputField truongLoi, String thongBao) {
            this.thanhCong = thanhCong;
            this.loiNhapLieu = loiNhapLieu;
            this.truongLoi = truongLoi;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isLoiNhapLieu() { return loiNhapLieu; }
        public InputField getTruongLoi() { return truongLoi; }
        public String getThongBao() { return thongBao; }
    }

    public static final class KetQuaXoaKhachHang {
        private final boolean thanhCong;
        private final boolean biRangBuoc;
        private final String thongBao;

        public KetQuaXoaKhachHang(boolean thanhCong, boolean biRangBuoc, String thongBao) {
            this.thanhCong = thanhCong;
            this.biRangBuoc = biRangBuoc;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public boolean isBiRangBuoc() { return biRangBuoc; }
        public String getThongBao() { return thongBao; }
    }

    public static final class CustomerStatistics {
        public final int tongKhachHang;
        public final int tongThuCung;
        public final int thanhVienVangTroLen;
        public final int khachHangMoi;
        public final String chuoiMoTaThuCungTrungBinh;

        private CustomerStatistics(int customers, int pets, int goldOrHigherMembers, int newCustomers) {
            this.tongKhachHang = customers;
            this.tongThuCung = pets;
            this.thanhVienVangTroLen = goldOrHigherMembers;
            this.khachHangMoi = newCustomers;
            this.chuoiMoTaThuCungTrungBinh = customers > 0
                    ? String.format(Locale.US, "%.1f thú cưng / khách", (double) pets / customers)
                    : "0.0 thú cưng / khách";
        }

        public String layChuoiTongKhachHang() { return String.valueOf(tongKhachHang); }
        public String layChuoiKhachHangMoi() { return String.valueOf(khachHangMoi); }
        public String layChuoiTongThuCung() { return String.valueOf(tongThuCung); }
        public String layChuoiThanhVienVang() { return String.valueOf(thanhVienVangTroLen); }
        public String layChuoiMoTaThuCung() { return chuoiMoTaThuCungTrungBinh; }
    }

    public static final class CustomerDetails {
        public static final String[] COT_THU_CUNG = {
                "Mã TC", "Tên thú cưng", "Loài", "Giới tính", "Ngày sinh", "Đặc điểm"
        };
        public static final String[] COT_HOA_DON = {
                "Mã HĐ", "Ngày lập", "Trạng thái", "Số tiền (₫)", "Hình thức"
        };

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
                        pet.ma, pet.ten, pet.loai, pet.gioiTinh, pet.ngaySinh, pet.dacDiem
                };
            }
            return dataTC;
        }

        public Object[][] layDuLieuBangHoaDon() {
            Object[][] dataHD = new Object[invoices.size()][5];
            for (int i = 0; i < invoices.size(); i++) {
                InvoiceRow invoice = invoices.get(i);
                dataHD[i][0] = invoice.ma;
                dataHD[i][1] = invoice.ngayLap;
                dataHD[i][2] = invoice.trangThai;
                dataHD[i][3] = String.format(Locale.US, "%,.0f ₫", invoice.soTien).replace(',', '.');
                dataHD[i][4] = invoice.hinhThucThanhToan;
            }
            return dataHD;
        }

        public String layDiaChiHienThi() {
            return (customer != null && customer.getDiaChi() != null && !customer.getDiaChi().trim().isEmpty())
                    ? customer.getDiaChi() : "Chưa cập nhật";
        }

        public String layTieuDeDialogChiTiet() {
            return "Hồ Sơ Chi Tiết Khách Hàng - " + (customer != null ? customer.getMaKH() : "");
        }

        public String layThongTinMaKH() {
            return "Mã KH: " + (customer != null ? customer.getMaKH() : "");
        }

        public String layThongTinHoTen() {
            return "Họ tên: " + (customer != null ? customer.getHoTen() : "");
        }

        public String layThongTinSDT() {
            return "Điện thoại: " + (customer != null ? customer.getSdt() : "");
        }

        public String layThongTinEmail() {
            return "Email: " + (customer != null ? customer.getEmail() : "");
        }

        public String layThongTinDiaChi() {
            return "Địa chỉ: " + layDiaChiHienThi();
        }

        public String layTieuDeThuCung() {
            return " Thú cưng sở hữu (" + pets.size() + ") ";
        }

        public String layTieuDeHoaDon() {
            return " Lịch sử hóa đơn giao dịch (" + invoices.size() + ") ";
        }
    }

    public static final class PetRow {
        public final String ma;
        public final String ten;
        public final String loai;
        public final String gioiTinh;
        public final Date ngaySinh;
        public final String dacDiem;

        private PetRow(String ma, String ten, String loai, String gioiTinh, Date ngaySinh, String dacDiem) {
            this.ma = ma;
            this.ten = ten;
            this.loai = loai;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.dacDiem = dacDiem;
        }
    }

    public static final class InvoiceRow {
        public final String ma;
        public final Date ngayLap;
        public final String trangThai;
        public final double soTien;
        public final String hinhThucThanhToan;

        private InvoiceRow(String ma, Date ngayLap, String trangThai, double soTien, String hinhThucThanhToan) {
            this.ma = ma;
            this.ngayLap = ngayLap;
            this.trangThai = trangThai;
            this.soTien = soTien;
            this.hinhThucThanhToan = hinhThucThanhToan;
        }
    }
}
