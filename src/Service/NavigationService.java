package Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dao.DangNhap_DAO;
import entity.TaiKhoan_entity;

public class NavigationService {
    public enum ManHinh {
        MAC_DINH,
        NHAN_VIEN,
        BAC_SI,
        THONG_KE,
        QUAN_LY_TAI_KHOAN,
        HO_SO_THU_CUNG,
        DON_THUOC,
        LICH_SU_KHAM,
        KHACH_HANG,
        HANG_THANH_VIEN,
        TAO_HOA_DON,
        DANH_SACH_HOA_DON,
        VAT_TU,
        NHA_CUNG_CAP,
        PHIEU_NHAP_KHO,
        PHIEU_XUAT_KHO,
        DICH_VU,
        KHUYEN_MAI,
        THUE,
        LICH_HEN,
        DAT_LICH,
        LICH_LAM_VIEC
    }

    public enum VaiTroHeThong {
        ADMIN("Admin (Toàn quyền)"),
        QUAN_LY("Quản lý"),
        BAC_SI("Bác sĩ thú y"),
        NHAN_VIEN_BAN_HANG("Nhân viên bán hàng"),
        NHAN_VIEN_DICH_VU("Nhân viên dịch vụ"),
        NHAN_VIEN_KHO("Nhân viên quản lý kho");

        private final String tenHienThi;

        VaiTroHeThong(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    public String getTenNguoiDungHienTai() {
        return DangNhap_DAO.getTenNguoiDung();
    }

    public boolean isDaDangNhap() {
        return DangNhap_DAO.isDaDangNhap();
    }

    public String getVaiTroNguoiDungHienTai() {
        return DangNhap_DAO.getVaiTro();
    }

    public TaiKhoan_entity getTaiKhoanHienTai() {
        return DangNhap_DAO.getTaiKhoanHienTai();
    }

    public void dangXuat() {
        DangNhap_DAO.dangXuat();
    }

    public VaiTroHeThong xacDinhVaiTro(String vaiTro) {
        TaiKhoan_entity taiKhoan = DangNhap_DAO.getTaiKhoanHienTai();
        if (DangNhap_DAO.isDaDangNhap()
                && taiKhoan != null
                && "admin".equalsIgnoreCase(taiKhoan.getTenDangNhap())) {
            return VaiTroHeThong.ADMIN;
        }
        if (vaiTro == null || vaiTro.trim().isEmpty()) {
            return VaiTroHeThong.QUAN_LY;
        }
        String vaiTroChuanHoa = vaiTro.trim().toLowerCase();
        if (vaiTroChuanHoa.contains("admin")
                || vaiTroChuanHoa.contains("quản trị")
                || vaiTroChuanHoa.contains("quantri")) {
            return VaiTroHeThong.ADMIN;
        }
        if (vaiTroChuanHoa.contains("bác sĩ")
                || vaiTroChuanHoa.contains("bác sỹ")
                || vaiTroChuanHoa.contains("bac si")
                || vaiTroChuanHoa.contains("bacsi")) {
            return VaiTroHeThong.BAC_SI;
        }
        if (vaiTroChuanHoa.contains("bán hàng")
                || vaiTroChuanHoa.contains("ban hang")
                || vaiTroChuanHoa.contains("thu ngân")
                || vaiTroChuanHoa.contains("thungan")
                || vaiTroChuanHoa.contains("banhang")) {
            return VaiTroHeThong.NHAN_VIEN_BAN_HANG;
        }
        if (vaiTroChuanHoa.contains("dịch vụ")
                || vaiTroChuanHoa.contains("dich vu")
                || vaiTroChuanHoa.contains("dichvu")) {
            return VaiTroHeThong.NHAN_VIEN_DICH_VU;
        }
        if (vaiTroChuanHoa.contains("kho")) {
            return VaiTroHeThong.NHAN_VIEN_KHO;
        }
        if (vaiTroChuanHoa.contains("quản lý")
                || vaiTroChuanHoa.contains("quan ly")
                || vaiTroChuanHoa.contains("quanly")
                || vaiTroChuanHoa.contains("ql")) {
            return VaiTroHeThong.QUAN_LY;
        }
        return VaiTroHeThong.QUAN_LY;
    }

    public MenuQuyen layMenuQuyen(String vaiTro) {
        return layMenuQuyen(xacDinhVaiTro(vaiTro));
    }

    public MenuQuyen layMenuQuyen(VaiTroHeThong vaiTro) {
        if (vaiTro == null) {
            vaiTro = VaiTroHeThong.QUAN_LY;
        }
        Map<String, String[]> menu = new LinkedHashMap<>();
        String[] tabs;
        switch (vaiTro) {
            case ADMIN:
                tabs = new String[] { "Nhân viên", "Bác sĩ", "Thú cưng", "Khách hàng", "Hóa đơn", "Vật tư", "Lịch", "Dịch vụ" };
                menu.put("Nhân viên", new String[] { "Danh sách nhân viên", "Thống kê", "Quản lý tài khoản" });
                menu.put("Bác sĩ", new String[] { "Danh sách bác sĩ" });
                menu.put("Thú cưng", new String[] { "Hồ sơ thú cưng", "Đơn Thuốc", "Lịch sử khám" });
                menu.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                menu.put("Hóa đơn", new String[] { "Tạo hóa đơn mới", "Danh sách hóa đơn" });
                menu.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp", "Phiếu nhập kho", "Phiếu xuất kho" });
                menu.put("Lịch", new String[] { "Lịch hẹn (Khám)", "Đặt lịch mới", "Lịch làm việc" });
                menu.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                break;
            case QUAN_LY:
                tabs = new String[] { "Nhân viên", "Bác sĩ", "Khách hàng", "Lịch", "Dịch vụ", "Hóa đơn", "Vật tư" };
                menu.put("Nhân viên", new String[] { "Danh sách nhân viên", "Thống kê" });
                menu.put("Bác sĩ", new String[] { "Danh sách bác sĩ" });
                menu.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                menu.put("Lịch", new String[] { "Lịch làm việc", "Lịch hẹn (Khám)" });
                menu.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                menu.put("Hóa đơn", new String[] { "Danh sách hóa đơn" });
                menu.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp" });
                break;
            case BAC_SI:
                tabs = new String[] { "Thú cưng", "Lịch", "Khách hàng" };
                menu.put("Thú cưng", new String[] { "Hồ sơ thú cưng", "Đơn Thuốc", "Lịch sử khám" });
                menu.put("Lịch", new String[] { "Lịch hẹn (Khám)", "Lịch làm việc" });
                menu.put("Khách hàng", new String[] { "Danh sách khách hàng" });
                break;
            case NHAN_VIEN_BAN_HANG:
                tabs = new String[] { "Hóa đơn", "Thú cưng", "Vật tư", "Dịch vụ", "Khách hàng" };
                menu.put("Hóa đơn", new String[] { "Tạo hóa đơn mới", "Danh sách hóa đơn" });
                menu.put("Thú cưng", new String[] { "Đơn Thuốc" });
                menu.put("Vật tư", new String[] { "Vật tư" });
                menu.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi", "Thuế" });
                menu.put("Khách hàng", new String[] { "Danh sách khách hàng", "Quản lý hạng thành viên" });
                break;
            case NHAN_VIEN_DICH_VU:
                tabs = new String[] { "Lịch", "Dịch vụ", "Thú cưng", "Khách hàng" };
                menu.put("Lịch", new String[] { "Đặt lịch mới", "Lịch hẹn (Khám)", "Lịch làm việc" });
                menu.put("Dịch vụ", new String[] { "Bảng giá dịch vụ", "Khuyến mãi" });
                menu.put("Thú cưng", new String[] { "Hồ sơ thú cưng" });
                menu.put("Khách hàng", new String[] { "Danh sách khách hàng" });
                break;
            case NHAN_VIEN_KHO:
                tabs = new String[] { "Vật tư" };
                menu.put("Vật tư", new String[] { "Vật tư", "Nhà cung cấp", "Phiếu nhập kho", "Phiếu xuất kho" });
                break;
            default:
                throw new IllegalStateException("Vai trò hệ thống không được hỗ trợ: " + vaiTro);
        }
        return new MenuQuyen(vaiTro, tabs, menu);
    }

    public ManHinh layManHinhChoTab(String tenTab) {
        if (tenTab == null) {
            return ManHinh.MAC_DINH;
        }
        switch (tenTab) {
            case "Nhân viên": return ManHinh.NHAN_VIEN;
            case "Bác sĩ": return ManHinh.BAC_SI;
            case "Thú cưng": return ManHinh.HO_SO_THU_CUNG;
            case "Khách hàng": return ManHinh.KHACH_HANG;
            case "Hóa đơn": return ManHinh.DANH_SACH_HOA_DON;
            case "Vật tư": return ManHinh.VAT_TU;
            case "Lịch": return ManHinh.LICH_HEN;
            case "Dịch vụ": return ManHinh.DICH_VU;
            case "Thống kê": return ManHinh.THONG_KE;
            case "Quản lý tài khoản": return ManHinh.QUAN_LY_TAI_KHOAN;
            default: return ManHinh.MAC_DINH;
        }
    }

    public ManHinh layManHinhChoMenuCon(String tenMenuCon) {
        if (tenMenuCon == null) {
            return ManHinh.MAC_DINH;
        }
        switch (tenMenuCon) {
            case "Danh sách nhân viên": return ManHinh.NHAN_VIEN;
            case "Danh sách bác sĩ": return ManHinh.BAC_SI;
            case "Thống kê": return ManHinh.THONG_KE;
            case "Quản lý tài khoản": return ManHinh.QUAN_LY_TAI_KHOAN;
            case "Hồ sơ thú cưng": return ManHinh.HO_SO_THU_CUNG;
            case "Đơn Thuốc": return ManHinh.DON_THUOC;
            case "Lịch sử khám": return ManHinh.LICH_SU_KHAM;
            case "Danh sách khách hàng": return ManHinh.KHACH_HANG;
            case "Quản lý hạng thành viên":
            case "Thẻ thành viên": return ManHinh.HANG_THANH_VIEN;
            case "Tạo hóa đơn mới": return ManHinh.TAO_HOA_DON;
            case "Danh sách hóa đơn": return ManHinh.DANH_SACH_HOA_DON;
            case "Vật tư": return ManHinh.VAT_TU;
            case "Nhà cung cấp": return ManHinh.NHA_CUNG_CAP;
            case "Phiếu nhập kho": return ManHinh.PHIEU_NHAP_KHO;
            case "Phiếu xuất kho": return ManHinh.PHIEU_XUAT_KHO;
            case "Bảng giá dịch vụ": return ManHinh.DICH_VU;
            case "Khuyến mãi": return ManHinh.KHUYEN_MAI;
            case "Thuế": return ManHinh.THUE;
            case "Lịch hẹn (Khám)": return ManHinh.LICH_HEN;
            case "Đặt lịch mới": return ManHinh.DAT_LICH;
            case "Lịch làm việc": return ManHinh.LICH_LAM_VIEC;
            default: return ManHinh.MAC_DINH;
        }
    }

    public static final class MenuQuyen {
        private final VaiTroHeThong vaiTro;
        private final String[] danhSachTabs;
        private final Map<String, String[]> menuCon;

        private MenuQuyen(VaiTroHeThong vaiTro, String[] danhSachTabs, Map<String, String[]> menuCon) {
            this.vaiTro = vaiTro;
            this.danhSachTabs = danhSachTabs.clone();
            Map<String, String[]> menuSaoChep = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> muc : menuCon.entrySet()) {
                menuSaoChep.put(muc.getKey(), muc.getValue().clone());
            }
            this.menuCon = Collections.unmodifiableMap(menuSaoChep);
        }

        public VaiTroHeThong getVaiTro() {
            return vaiTro;
        }

        public String[] getDanhSachTabs() {
            return danhSachTabs.clone();
        }

        public String[] getMenuCon(String tenTab) {
            String[] cacMenu = menuCon.get(tenTab);
            return cacMenu == null ? null : cacMenu.clone();
        }
    }
}
