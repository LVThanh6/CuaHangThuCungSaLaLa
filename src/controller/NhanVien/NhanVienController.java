package controller.NhanVien;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dao.NhanVien.NhanVien_DAO;
import entity.NhanVien.NhanVien_entity;

/**
 * Bộ điều khiển Nhân viên: toàn bộ nghiệp vụ (tải, thống kê, lọc/sắp xếp,
 * kiểm tra hợp lệ, thêm/sửa/xóa, đổi trạng thái tài khoản, tính lương tháng,
 * xuất CSV) nằm ở đây. NhanVienGUI chỉ hiển thị và gọi các hàm này.
 */
public class NhanVienController {

    // ====================== HẰNG SỐ NGHIỆP VỤ ======================

    public static final String CV_BAN_HANG = "Nhân viên bán hàng";
    public static final String CV_DICH_VU = "Nhân viên dịch vụ";
    public static final String CV_KHO = "Nhân viên quản lý kho";
    public static final String CV_QUAN_LY = "Quản lý";

    public static final String[] CHUC_VU = { CV_BAN_HANG, CV_DICH_VU, CV_KHO, CV_QUAN_LY };
    public static final String[] GIOI_TINH = { "Nam", "Nữ" };

    /** Hoa hồng dịch vụ của nhân viên dịch vụ: 5% doanh thu dịch vụ họ thực hiện. */
    private static final double TY_LE_HOA_HONG = 0.05;

    private static final Locale VN = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Trạng thái hiển thị của nhân viên, suy ra từ tài khoản liên kết. */
    public enum TrangThai {
        DANG_LAM("Đang làm việc"),
        NGUNG("Ngừng hoạt động"),
        CHUA_TK("Chưa có tài khoản");

        public final String nhan;

        TrangThai(String nhan) {
            this.nhan = nhan;
        }

        public static TrangThai tuEntity(NhanVien_entity nv) {
            Boolean tk = nv.getTrangThaiTK();
            if (tk == null)
                return CHUA_TK;
            return tk ? DANG_LAM : NGUNG;
        }
    }

    // ====================== KẾT QUẢ TRẢ VỀ CHO GUI ======================

    public static class ThongKe {
        public final int tongSo, dangLam, ngung, chuaTaiKhoan;

        ThongKe(int tongSo, int dangLam, int ngung, int chuaTaiKhoan) {
            this.tongSo = tongSo;
            this.dangLam = dangLam;
            this.ngung = ngung;
            this.chuaTaiKhoan = chuaTaiKhoan;
        }
    }

    public static class KetQuaXoa {
        public final boolean thanhCong;
        public final boolean coDuLieuLienQuan;

        KetQuaXoa(boolean thanhCong, boolean coDuLieuLienQuan) {
            this.thanhCong = thanhCong;
            this.coDuLieuLienQuan = coDuLieuLienQuan;
        }
    }

    public static class KetQuaLuu {
        private final boolean thanhCong;
        private final String thongBaoLoi;

        private KetQuaLuu(boolean thanhCong, String thongBaoLoi) {
            this.thanhCong = thanhCong;
            this.thongBaoLoi = thongBaoLoi;
        }

        static KetQuaLuu ok() {
            return new KetQuaLuu(true, "");
        }

        static KetQuaLuu loi(String thongBao) {
            return new KetQuaLuu(false, thongBao);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongBaoLoi() {
            return thongBaoLoi;
        }
    }

    private final NhanVien_DAO dao = new NhanVien_DAO();

    // ====================== TẢI DỮ LIỆU / THỐNG KÊ ======================

    public List<NhanVien_entity> taiDanhSach() throws SQLException {
        return dao.getAll();
    }

    public ThongKe thongKe(List<NhanVien_entity> ds) {
        int dangLam = 0, ngung = 0, chuaTk = 0;
        for (NhanVien_entity nv : ds) {
            switch (TrangThai.tuEntity(nv)) {
                case DANG_LAM: dangLam++; break;
                case NGUNG: ngung++; break;
                default: chuaTk++;
            }
        }
        return new ThongKe(ds.size(), dangLam, ngung, chuaTk);
    }

    // ====================== LỌC / SẮP XẾP ======================

    /**
     * @param tuKhoa   tìm theo tên, mã, SĐT (không phân biệt hoa thường và dấu); rỗng = bỏ qua
     * @param chucVu   null = tất cả
     * @param trangThai tên của {@link TrangThai} (DANG_LAM...), null = tất cả
     * @param sapXep   true = sắp xếp theo tên
     * @param tang     chiều sắp xếp khi sapXep = true
     */
    public List<NhanVien_entity> locDanhSach(List<NhanVien_entity> ds, String tuKhoa, String chucVu,
            String trangThai, boolean sapXep, boolean tang) {
        String kw = boDau(tuKhoa).trim();
        List<NhanVien_entity> kq = new ArrayList<>();
        for (NhanVien_entity nv : ds) {
            if (!kw.isEmpty()) {
                String hay = boDau(nz(nv.getHoTen()) + " " + nz(nv.getMaNhanVien()) + " " + nz(nv.getSdt()));
                if (!hay.contains(kw))
                    continue;
            }
            if (chucVu != null && !chucVu.equals(nv.getChucVu()))
                continue;
            if (trangThai != null && !trangThai.equals(TrangThai.tuEntity(nv).name()))
                continue;
            kq.add(nv);
        }
        if (sapXep) {
            Collator col = Collator.getInstance(VN);
            // Tên người Việt: so sánh theo tên (từ cuối), sau đó theo cả họ tên
            Comparator<NhanVien_entity> cmp = Comparator
                    .comparing((NhanVien_entity nv) -> tuCuoi(nv.getHoTen()), col)
                    .thenComparing(nv -> nz(nv.getHoTen()), col);
            kq.sort(tang ? cmp : cmp.reversed());
        }
        return kq;
    }

    // ====================== THÊM / SỬA ======================

    public String taoMaMoi() throws SQLException {
        String ma = dao.getMaTiepTheo();
        if (ma == null)
            throw new SQLException("Không tạo được mã nhân viên, hãy kiểm tra kết nối cơ sở dữ liệu.");
        return ma;
    }

    /**
     * Kiểm tra dữ liệu form rồi thêm (goc == null) hoặc cập nhật (goc != null).
     * Tài khoản liên kết của nhân viên được giữ nguyên; chỉ trạng thái được đổi nếu khác.
     *
     * @param dangLam trạng thái tài khoản mong muốn (bỏ qua nếu nhân viên chưa có tài khoản)
     */
    public KetQuaLuu validateAndSave(String ma, NhanVien_entity goc, String hoTen, String sdt, String diaChi,
            String ngaySinh, String gioiTinh, String chucVu, String luong, boolean dangLam) throws SQLException {

        hoTen = nz(hoTen).trim();
        sdt = nz(sdt).trim();
        diaChi = nz(diaChi).trim();
        ngaySinh = nz(ngaySinh).trim();

        if (hoTen.isEmpty())
            return KetQuaLuu.loi("Vui lòng nhập họ và tên.");
        if (hoTen.length() > 100)
            return KetQuaLuu.loi("Họ và tên tối đa 100 ký tự.");

        String sdtSo = sdt.replaceAll("[\\s.\\-]", "");
        if (sdtSo.isEmpty())
            return KetQuaLuu.loi("Vui lòng nhập số điện thoại.");
        if (!sdtSo.matches("0\\d{9}"))
            return KetQuaLuu.loi("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");

        if (diaChi.length() > 255)
            return KetQuaLuu.loi("Địa chỉ tối đa 255 ký tự.");

        LocalDate ngay = null;
        if (!ngaySinh.isEmpty()) {
            try {
                ngay = LocalDate.parse(ngaySinh, DINH_DANG_NGAY);
            } catch (DateTimeParseException ex) {
                return KetQuaLuu.loi("Ngày sinh không hợp lệ (định dạng dd/MM/yyyy).");
            }
            if (ngay.isAfter(LocalDate.now()))
                return KetQuaLuu.loi("Ngày sinh không được ở tương lai.");
        }

        if (!contains(GIOI_TINH, gioiTinh))
            return KetQuaLuu.loi("Giới tính không hợp lệ.");
        if (!contains(CHUC_VU, chucVu))
            return KetQuaLuu.loi("Chức vụ không hợp lệ.");

        String luongSo = nz(luong).replaceAll("[\\s.,]", "");
        if (!luongSo.matches("\\d{1,12}"))
            return KetQuaLuu.loi("Lương cơ bản phải là số không âm.");
        double luongCoBan = Double.parseDouble(luongSo);

        NhanVien_entity nv;
        boolean ok;
        if (goc == null) {
            // Phòng trường hợp mã hiển thị trên form đã bị người khác dùng mất
            String maLuu = dao.getNhanVienTheoMa(ma) != null ? taoMaMoi() : ma;
            nv = new NhanVien_entity(maLuu, null, hoTen, chucVu, sdtSo, diaChi, ngay, gioiTinh, luongCoBan, null);
            ok = dao.them(nv);
        } else {
            nv = new NhanVien_entity(goc.getMaNhanVien(), goc.getMaTK(), hoTen, chucVu, sdtSo, diaChi, ngay,
                    gioiTinh, luongCoBan, goc.getTrangThaiTK());
            ok = dao.capNhat(nv);
        }
        if (!ok)
            return KetQuaLuu.loi("Không lưu được nhân viên vào cơ sở dữ liệu.");

        if (goc != null && goc.getMaTK() != null && dangLam != Boolean.TRUE.equals(goc.getTrangThaiTK())) {
            dao.doiTrangThaiTaiKhoan(goc.getMaNhanVien(), dangLam);
        }
        return KetQuaLuu.ok();
    }

    // ====================== ĐỔI TRẠNG THÁI / XÓA ======================

    public void doiTrangThai(String maNhanVien, boolean hoatDong) throws SQLException {
        if (!dao.doiTrangThaiTaiKhoan(maNhanVien, hoatDong))
            throw new SQLException("Không cập nhật được trạng thái tài khoản của " + maNhanVien + ".");
    }

    /**
     * Xóa nhân viên. Nếu đã có dữ liệu tham chiếu (LichLamViec, LichHen, HoaDon,
     * ChiTietHoaDon, PhieuXuatKho, PhieuNhapKho) thì SQL Server báo lỗi khóa ngoại 547
     * và kết quả trả về coDuLieuLienQuan = true.
     */
    public KetQuaXoa xoa(String maNhanVien) throws SQLException {
        try {
            return new KetQuaXoa(dao.xoa(maNhanVien), false);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547 || "23000".equals(ex.getSQLState()))
                return new KetQuaXoa(false, true);
            throw ex;
        }
    }

    // ====================== TÍNH LƯƠNG THÁNG ======================

    /**
     * Tổng lương nhận trong tháng của {@code homNay}, tính từ ngày 1 đến hết {@code homNay}:
     * <ul>
     * <li>Bán hàng: lương theo giờ x số giờ làm (lịch trạng thái hoàn thành).</li>
     * <li>Dịch vụ: lương theo giờ x số giờ làm + hoa hồng 5% doanh thu dịch vụ đã thực hiện.</li>
     * <li>Quản lý kho: lương tháng cố định phân bổ theo số ngày đã qua / số ngày của tháng.</li>
     * <li>Quản lý hoặc nhân viên mới (chưa lưu): chưa áp dụng, trả về NaN.</li>
     * </ul>
     */
    public double tinhTongLuongThang(NhanVien_entity nv, LocalDate homNay) throws SQLException {
        if (nv == null || nv.getChucVu() == null)
            return Double.NaN;

        LocalDate dauThang = homNay.withDayOfMonth(1);
        String cv = nv.getChucVu();
        if (CV_BAN_HANG.equals(cv)) {
            return nv.getLuongCoBan() * dao.getSoGioLamViecCuaNhanVien(nv.getMaNhanVien(), dauThang, homNay);
        }
        if (CV_DICH_VU.equals(cv)) {
            double gio = dao.getSoGioLamViecCuaNhanVien(nv.getMaNhanVien(), dauThang, homNay);
            double doanhThu = dao.getDoanhThuDichVuCuaNhanVien(nv.getMaNhanVien(), dauThang, homNay);
            return nv.getLuongCoBan() * gio + doanhThu * TY_LE_HOA_HONG;
        }
        if (CV_KHO.equals(cv)) {
            return nv.getLuongCoBan() * homNay.getDayOfMonth() / homNay.lengthOfMonth();
        }
        return Double.NaN;
    }

    /** Lương cơ bản gợi ý khi chọn chức vụ lúc thêm nhân viên (khớp dữ liệu mẫu trong DB). */
    public static long luongMacDinh(String chucVu) {
        if (CV_BAN_HANG.equals(chucVu)) return 45_000;       // theo giờ
        if (CV_DICH_VU.equals(chucVu)) return 60_000;        // theo giờ
        if (CV_KHO.equals(chucVu)) return 13_500_000;        // theo tháng
        if (CV_QUAN_LY.equals(chucVu)) return 15_000_000;    // theo tháng
        return 0;
    }

    // ====================== XUẤT CSV ======================

    public String taoNoiDungCsv(List<NhanVien_entity> ds) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM UTF-8 để Excel đọc đúng tiếng Việt
        sb.append(csvDong("Mã nhân viên", "Họ và tên", "Chức vụ", "Giới tính", "Ngày sinh",
                "Số điện thoại", "Địa chỉ", "Lương cơ bản", "Trạng thái"));
        for (NhanVien_entity nv : ds) {
            sb.append(csvDong(
                    nv.getMaNhanVien(),
                    nv.getHoTen(),
                    nv.getChucVu(),
                    nv.getGioiTinh(),
                    dinhDangNgay(nv.getNgaySinh()),
                    nv.getSdt(),
                    nv.getDiaChi(),
                    String.format("%.0f", nv.getLuongCoBan()),
                    TrangThai.tuEntity(nv).nhan));
        }
        return sb.toString();
    }

    public void ghiCsv(File file, List<NhanVien_entity> ds) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(taoNoiDungCsv(ds));
        }
    }

    private static String csvDong(String... o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append('"').append(nz(o[i]).replace("\"", "\"\"")).append('"');
        }
        return sb.append("\r\n").toString();
    }

    // ====================== ĐỊNH DẠNG DÙNG CHUNG ======================

    public static String dinhDangNgay(LocalDate d) {
        return d == null ? "" : DINH_DANG_NGAY.format(d);
    }

    public static String dinhDangTien(double so) {
        return NumberFormat.getInstance(VN).format(so) + " ₫";
    }

    public static String dinhDangSdt(String s) {
        if (s == null)
            return "";
        String so = s.replaceAll("\\D", "");
        if (so.length() == 10)
            return so.substring(0, 4) + " " + so.substring(4, 7) + " " + so.substring(7);
        return s;
    }

    // ====================== TIỆN ÍCH NỘI BỘ ======================

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean contains(String[] arr, String v) {
        for (String s : arr)
            if (s.equals(v))
                return true;
        return false;
    }

    private static String tuCuoi(String ten) {
        String[] tu = nz(ten).trim().split("\\s+");
        return tu[tu.length - 1];
    }

    private static String boDau(String s) {
        String t = Normalizer.normalize(nz(s), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return t.toLowerCase(VN);
    }
}