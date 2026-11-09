package controller.TaiKhoan;

import dao.TaiKhoan.TaiKhoan_DAO;
import entity.TaiKhoan.TaiKhoan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controller cho toàn bộ nghiệp vụ tài khoản người dùng:
 *  1. Hồ sơ tài khoản đang đăng nhập (thông tin hiển thị)
 *  2. Đổi mật khẩu cá nhân
 *  3. Quản lý tài khoản (danh sách, lọc, thêm, sửa vai trò, khóa/mở, đặt lại mật khẩu, xóa)
 * Không chứa thành phần giao diện; GUI chỉ gọi các hàm bên dưới và hiển thị kết quả.
 */
public class ThongTinTaiKhoanController {

    public static final String MAT_KHAU_MAC_DINH = "123456";
    public static final int DO_DAI_MAT_KHAU_TOI_THIEU = 6;

    public static final String LOC_VAI_TRO_TAT_CA = "Vai trò: Tất cả";
    public static final String LOC_TRANG_THAI_TAT_CA = "Trạng thái: Tất cả";
    public static final String LOC_HOAT_DONG = "Hoạt động";
    public static final String LOC_DA_KHOA = "Đã khóa";

    private static final String[] VAI_TRO = {
            "Admin", "Quản lý", "Bác sĩ",
            "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho"
    };

    private final TaiKhoan_DAO dao = new TaiKhoan_DAO();

    // ======================================================================
    // 1. HỒ SƠ TÀI KHOẢN ĐANG ĐĂNG NHẬP
    // ======================================================================
    public static class ThongTinHoSo {
        public final String chuCaiDau;
        public final String tenHienThi;
        public final String vaiTro;
        public final String maTK;
        public final String tenDangNhap;
        public final String trangThai;
        public final String maQuanLy;

        ThongTinHoSo(String chuCaiDau, String tenHienThi, String vaiTro, String maTK,
                     String tenDangNhap, String trangThai, String maQuanLy) {
            this.chuCaiDau = chuCaiDau;
            this.tenHienThi = tenHienThi;
            this.vaiTro = vaiTro;
            this.maTK = maTK;
            this.tenDangNhap = tenDangNhap;
            this.trangThai = trangThai;
            this.maQuanLy = maQuanLy;
        }
    }

    public ThongTinHoSo taoThongTin(TaiKhoan tk, String tenHienThi) {
        String ten = (tenHienThi == null || tenHienThi.trim().isEmpty()) ? "Người dùng" : tenHienThi.trim();
        return new ThongTinHoSo(
                layChuCaiDau(ten),
                ten,
                tk != null && tk.getVaiTro() != null ? tk.getVaiTro() : "",
                tk != null ? tk.getMaTK() : "---",
                tk != null ? tk.getTenDangNhap() : "---",
                (tk != null && tk.isTrangThai()) ? "Hoạt động" : "Đã khóa",
                tk != null && tk.getMaQuanLy() != null ? tk.getMaQuanLy().getMaQuanLy() : "Toàn quyền");
    }

    /** Chữ cái đầu của tên gọi (từ cuối của phần họ tên, bỏ phần trong ngoặc như "(Admin)"). */
    private String layChuCaiDau(String ten) {
        String phanTen = ten;
        int ngoac = phanTen.indexOf('(');
        if (ngoac > 0) phanTen = phanTen.substring(0, ngoac);
        phanTen = phanTen.trim();
        if (phanTen.isEmpty()) phanTen = ten;
        String[] tu = phanTen.split("\\s+");
        String tuCuoi = tu[tu.length - 1];
        return tuCuoi.isEmpty() ? "?" : tuCuoi.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    // ======================================================================
    // 2. ĐỔI MẬT KHẨU CÁ NHÂN
    // ======================================================================
    public enum TruongNhap { MAT_KHAU_CU, MAT_KHAU_MOI, XAC_NHAN, KHONG }

    public static class KetQuaDoiMatKhau {
        private final boolean thanhCong;
        private final String thongBao;
        private final TruongNhap truongNhap;

        public KetQuaDoiMatKhau(boolean thanhCong, String thongBao, TruongNhap truongNhap) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.truongNhap = truongNhap;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public TruongNhap getTruongNhap() { return truongNhap; }
    }

    public KetQuaDoiMatKhau doiMatKhau(TaiKhoan tk, String matKhauCu, String matKhauMoi, String xacNhan) {
        if (tk == null) {
            return new KetQuaDoiMatKhau(false, "Không xác định được tài khoản cần đổi mật khẩu!", TruongNhap.KHONG);
        }
        String cu = matKhauCu == null ? "" : matKhauCu.trim();
        String moi = matKhauMoi == null ? "" : matKhauMoi.trim();
        String xn = xacNhan == null ? "" : xacNhan.trim();

        if (cu.isEmpty()) {
            return new KetQuaDoiMatKhau(false, "Vui lòng nhập mật khẩu hiện tại!", TruongNhap.MAT_KHAU_CU);
        }
        // Kiểm tra mật khẩu hiện tại trực tiếp với CSDL (tránh dùng dữ liệu cũ trong phiên)
        if (!dao.dangNhap(tk.getTenDangNhap(), cu).isThanhCong()) {
            return new KetQuaDoiMatKhau(false, "Mật khẩu hiện tại không chính xác!", TruongNhap.MAT_KHAU_CU);
        }
        if (moi.isEmpty()) {
            return new KetQuaDoiMatKhau(false, "Vui lòng nhập mật khẩu mới!", TruongNhap.MAT_KHAU_MOI);
        }
        if (moi.length() < DO_DAI_MAT_KHAU_TOI_THIEU) {
            return new KetQuaDoiMatKhau(false,
                    "Mật khẩu mới phải có tối thiểu " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự!", TruongNhap.MAT_KHAU_MOI);
        }
        if (moi.equals(cu)) {
            return new KetQuaDoiMatKhau(false, "Mật khẩu mới phải khác mật khẩu hiện tại!", TruongNhap.MAT_KHAU_MOI);
        }
        if (!moi.equals(xn)) {
            return new KetQuaDoiMatKhau(false, "Mật khẩu xác nhận không khớp!", TruongNhap.XAC_NHAN);
        }
        if (!dao.doiMatKhau(tk.getMaTK(), moi)) {
            return new KetQuaDoiMatKhau(false, "Không thể cập nhật mật khẩu. Vui lòng thử lại!", TruongNhap.KHONG);
        }
        return new KetQuaDoiMatKhau(true, "Đổi mật khẩu thành công!", TruongNhap.KHONG);
    }

    // ======================================================================
    // 3. QUẢN LÝ TÀI KHOẢN
    // ======================================================================
    public static class TaiKhoanItem {
        private final String maTK, tenDangNhap, vaiTro, maQuanLy, tenQuanLy,
                hoTenNguoiDung, maDoiTuong, sdtNguoiDung;
        private final boolean trangThai;

        TaiKhoanItem(TaiKhoan_DAO.TaiKhoanChiTiet ct) {
            this.maTK = ct.getMaTK();
            this.tenDangNhap = ct.getTenDangNhap();
            this.vaiTro = ct.getVaiTro();
            this.trangThai = ct.isTrangThai();
            this.maQuanLy = ct.getMaQuanLy();
            this.tenQuanLy = ct.getTenQuanLy();
            this.hoTenNguoiDung = ct.getHoTenNguoiDung();
            this.maDoiTuong = ct.getMaDoiTuong();
            this.sdtNguoiDung = ct.getSdtNguoiDung();
        }

        public String getMaTK() { return maTK; }
        public String getTenDangNhap() { return tenDangNhap; }
        public String getVaiTro() { return vaiTro; }
        public boolean isTrangThai() { return trangThai; }
        public String getMaQuanLy() { return maQuanLy; }
        public String getTenQuanLy() { return tenQuanLy; }
        public String getHoTenNguoiDung() { return hoTenNguoiDung; }
        public String getMaDoiTuong() { return maDoiTuong; }
        public String getSdtNguoiDung() { return sdtNguoiDung; }
    }

    public static class DuLieuDanhSach {
        public final List<TaiKhoanItem> taiKhoan;
        public final int hoatDong, biKhoa, quanLy, nhanVien, bacSi;

        DuLieuDanhSach(List<TaiKhoanItem> taiKhoan, int hoatDong, int biKhoa, int quanLy, int nhanVien, int bacSi) {
            this.taiKhoan = taiKhoan;
            this.hoatDong = hoatDong;
            this.biKhoa = biKhoa;
            this.quanLy = quanLy;
            this.nhanVien = nhanVien;
            this.bacSi = bacSi;
        }
    }

    public static class DoiTuongOption {
        public final String ma, ten, vaiTro, loai;
        DoiTuongOption(String ma, String ten, String vaiTro, String loai) {
            this.ma = ma; this.ten = ten; this.vaiTro = vaiTro; this.loai = loai;
        }
    }

    public static class QuanLyOption {
        public final String ma, hoTen;
        QuanLyOption(String ma, String hoTen) { this.ma = ma; this.hoTen = hoTen; }
    }

    public static class DanhMucTaoTaiKhoan {
        public final List<DoiTuongOption> nhanVien, bacSi;
        public final List<QuanLyOption> quanLy;
        DanhMucTaoTaiKhoan(List<DoiTuongOption> nhanVien, List<DoiTuongOption> bacSi, List<QuanLyOption> quanLy) {
            this.nhanVien = nhanVien; this.bacSi = bacSi; this.quanLy = quanLy;
        }
    }

    public static class ThongTinTaiKhoanMoi {
        public final String maTK, tenDangNhap, matKhau, vaiTro, maQuanLy;
        public final boolean kichHoat;
        public final DoiTuongOption doiTuong;

        public ThongTinTaiKhoanMoi(String maTK, String tenDangNhap, String matKhau, String vaiTro,
                                   boolean kichHoat, String maQuanLy, DoiTuongOption doiTuong) {
            this.maTK = maTK;
            this.tenDangNhap = tenDangNhap;
            this.matKhau = matKhau;
            this.vaiTro = vaiTro;
            this.kichHoat = kichHoat;
            this.maQuanLy = maQuanLy;
            this.doiTuong = doiTuong;
        }
    }

    public static class KetQuaTaoTaiKhoan {
        public final boolean thanhCong;
        public final String thongBao;
        KetQuaTaoTaiKhoan(boolean thanhCong, String thongBao) { this.thanhCong = thanhCong; this.thongBao = thongBao; }
    }

    public static class KetQuaMatKhau {
        public final boolean thanhCong;
        public final String thongBao;
        KetQuaMatKhau(boolean thanhCong, String thongBao) { this.thanhCong = thanhCong; this.thongBao = thongBao; }
    }

    // ---- Danh mục dùng cho combobox ----
    public String[] danhSachVaiTro() {
        return Arrays.copyOf(VAI_TRO, VAI_TRO.length);
    }

    /** Danh sách vai trò cho bộ lọc (có mục "Tất cả" ở đầu). */
    public String[] danhSachVaiTroLoc() {
        String[] r = new String[VAI_TRO.length + 1];
        r[0] = LOC_VAI_TRO_TAT_CA;
        System.arraycopy(VAI_TRO, 0, r, 1, VAI_TRO.length);
        return r;
    }

    public String[] danhSachTrangThaiLoc() {
        return new String[] { LOC_TRANG_THAI_TAT_CA, LOC_HOAT_DONG, LOC_DA_KHOA };
    }

    // ---- Tải dữ liệu ----
    public DuLieuDanhSach taiDanhSach() {
        List<TaiKhoanItem> ds = new ArrayList<>();
        int hoatDong = 0, biKhoa = 0, quanLy = 0, nhanVien = 0, bacSi = 0;
        for (TaiKhoan_DAO.TaiKhoanChiTiet ct : dao.getAllTaiKhoanChiTiet()) {
            TaiKhoanItem item = new TaiKhoanItem(ct);
            ds.add(item);
            if (item.isTrangThai()) hoatDong++; else biKhoa++;

            String vt = item.getVaiTro() == null ? "" : item.getVaiTro().trim().toLowerCase(Locale.ROOT);
            if (vt.equals("admin") || vt.equals("quản lý")) quanLy++;
            else if (vt.startsWith("nhân viên")) nhanVien++;
            else if (vt.equals("bác sĩ")) bacSi++;
        }
        return new DuLieuDanhSach(ds, hoatDong, biKhoa, quanLy, nhanVien, bacSi);
    }

    /** Dữ liệu 1 dòng hiển thị trên bảng (đúng thứ tự cột của GUI). */
    public Object[] taoDongBang(TaiKhoanItem item) {
        String trangThaiText = item.isTrangThai() ? "🟢 Hoạt động" : "🔴 Đã khóa";
        String qlText = (item.getTenQuanLy() != null)
                ? item.getTenQuanLy() + " (" + item.getMaQuanLy() + ")" : "Toàn quyền";
        return new Object[] {
                item.getMaTK(), item.getTenDangNhap(), item.getHoTenNguoiDung(),
                item.getVaiTro(), trangThaiText, qlText, item.getSdtNguoiDung()
        };
    }

    /**
     * Điều kiện lọc dòng. Từ khóa tìm theo mã TK, tên đăng nhập, họ tên (không phân biệt hoa/thường);
     * vaiTro/trangThai là giá trị đang chọn trên combobox (null hoặc "Tất cả" = không lọc).
     */
    public boolean khopBoLoc(TaiKhoanItem item, String tuKhoa, String vaiTro, String trangThai) {
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        if (!kw.isEmpty()) {
            boolean khop = chua(item.getMaTK(), kw) || chua(item.getTenDangNhap(), kw)
                    || chua(item.getHoTenNguoiDung(), kw);
            if (!khop) return false;
        }
        if (vaiTro != null && !vaiTro.equals(LOC_VAI_TRO_TAT_CA)) {
            String vt = item.getVaiTro() == null ? "" : item.getVaiTro().toLowerCase(Locale.ROOT);
            if (!vt.startsWith(vaiTro.toLowerCase(Locale.ROOT))) return false;
        }
        if (trangThai != null && !trangThai.equals(LOC_TRANG_THAI_TAT_CA)) {
            boolean canHoatDong = trangThai.equals(LOC_HOAT_DONG);
            if (item.isTrangThai() != canHoatDong) return false;
        }
        return true;
    }

    private boolean chua(String s, String kwThuong) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(kwThuong);
    }

    public String taoMaMoi() {
        return dao.phatSinhMaTK();
    }

    public DanhMucTaoTaiKhoan taiDanhMucTaoTaiKhoan() {
        List<DoiTuongOption> nv = new ArrayList<>();
        for (Map<String, String> m : dao.getNhanVienChuaCoTaiKhoan()) {
            nv.add(new DoiTuongOption(m.get("ma"), m.get("ten"), m.get("vaiTro"), m.get("loai")));
        }
        List<DoiTuongOption> bs = new ArrayList<>();
        for (Map<String, String> m : dao.getBacSiChuaCoTaiKhoan()) {
            bs.add(new DoiTuongOption(m.get("ma"), m.get("ten"), m.get("vaiTro"), m.get("loai")));
        }
        return new DanhMucTaoTaiKhoan(nv, bs, taiDanhSachQuanLy());
    }

    public List<QuanLyOption> taiDanhSachQuanLy() {
        List<QuanLyOption> ds = new ArrayList<>();
        for (Map<String, String> m : dao.getDanhSachQuanLy()) {
            ds.add(new QuanLyOption(m.get("maQuanLy"), m.get("hoTen")));
        }
        return ds;
    }

    /** Gợi ý vai trò hệ thống từ đối tượng liên kết (nhân viên/bác sĩ) được chọn. */
    public String vaiTroGoiY(DoiTuongOption doiTuong) {
        if (doiTuong == null) return VAI_TRO[3];
        if ("BacSy".equalsIgnoreCase(doiTuong.loai)) return "Bác sĩ";

        String chucVu = doiTuong.vaiTro == null ? "" : doiTuong.vaiTro.trim();
        for (String vt : VAI_TRO) {
            if (vt.equalsIgnoreCase(chucVu)) return vt;
        }
        String c = chucVu.toLowerCase(Locale.ROOT);
        if (c.contains("kho")) return "Nhân viên quản lý kho";
        if (c.contains("dịch vụ") || c.contains("spa") || c.contains("grooming")) return "Nhân viên dịch vụ";
        if (c.contains("quản lý")) return "Quản lý";
        return "Nhân viên bán hàng";
    }

    // ---- Thao tác CRUD ----
    public KetQuaTaoTaiKhoan taoTaiKhoan(ThongTinTaiKhoanMoi tt) {
        String user = tt.tenDangNhap == null ? "" : tt.tenDangNhap.trim();
        if (user.isEmpty()) {
            return new KetQuaTaoTaiKhoan(false, "Vui lòng nhập tên đăng nhập!");
        }
        if (dao.isTenDangNhapTonTai(user, null)) {
            return new KetQuaTaoTaiKhoan(false, "Tên đăng nhập [" + user + "] đã tồn tại!");
        }
        String pass = tt.matKhau == null ? "" : tt.matKhau.trim();
        if (pass.isEmpty()) pass = MAT_KHAU_MAC_DINH;
        if (pass.length() < DO_DAI_MAT_KHAU_TOI_THIEU) {
            return new KetQuaTaoTaiKhoan(false,
                    "Mật khẩu phải có tối thiểu " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự!");
        }

        TaiKhoan tk = new TaiKhoan(tt.maTK, user, pass, tt.vaiTro, tt.kichHoat, tt.maQuanLy);
        String loai = tt.doiTuong != null ? tt.doiTuong.loai : null;
        String maDoiTuong = tt.doiTuong != null ? tt.doiTuong.ma : null;
        if (!dao.themTaiKhoan(tk, loai, maDoiTuong)) {
            return new KetQuaTaoTaiKhoan(false, "Không thể tạo tài khoản. Vui lòng kiểm tra lại dữ liệu!");
        }
        return new KetQuaTaoTaiKhoan(true, "Tạo tài khoản thành công!");
    }

    public boolean capNhatTaiKhoan(String maTK, String tenDangNhap, String vaiTro,
                                   boolean trangThai, String maQuanLy) {
        TaiKhoan tk = new TaiKhoan(maTK, tenDangNhap, "", vaiTro, trangThai, maQuanLy);
        return dao.capNhatTaiKhoan(tk);
    }

    public boolean datTrangThai(String maTK, boolean trangThai) {
        return dao.datTrangThaiTaiKhoan(maTK, trangThai);
    }

    public KetQuaMatKhau datLaiMatKhau(String maTK, String matKhauMoi) {
        String mk = matKhauMoi == null ? "" : matKhauMoi.trim();
        if (mk.isEmpty()) {
            return new KetQuaMatKhau(false, "Mật khẩu mới không được để trống!");
        }
        if (mk.length() < DO_DAI_MAT_KHAU_TOI_THIEU) {
            return new KetQuaMatKhau(false,
                    "Mật khẩu mới phải có tối thiểu " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự!");
        }
        if (!dao.doiMatKhau(maTK, mk)) {
            return new KetQuaMatKhau(false, "Không thể đặt lại mật khẩu cho tài khoản này!");
        }
        return new KetQuaMatKhau(true, "Đặt lại mật khẩu thành công!");
    }

    public boolean xoaTaiKhoan(String maTK) {
        return dao.xoaTaiKhoan(maTK);
    }
}