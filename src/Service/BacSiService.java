package Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Collator;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dao.BacSi_DAO;
import dao.ChuyenNganh_DAO;
import entity.BacSi_entity;
import entity.ChuyenNganh_entity;

/**
 * Bộ điều khiển Bác sĩ: toàn bộ nghiệp vụ (tải, thống kê, lọc/sắp xếp, kiểm tra
 * hợp lệ,
 * thêm/sửa/xóa, đổi trạng thái tài khoản, chuyên ngành, lịch làm việc, xuất
 * CSV).
 * BacSiGUI chỉ hiển thị và gọi các hàm này.
 */
public class BacSiService {

    // ====================== HẰNG SỐ ======================

    public static final String[] GIOI_TINH = { "Nam", "Nữ" };

    /** Chỉ số cột của bảng có hỗ trợ sắp xếp. */
    public static final int COT_HO_TEN = 1;
    public static final int COT_SO_CA = 6;

    private static final Locale VN = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DINH_DANG_THANG = DateTimeFormatter.ofPattern("MM/yyyy");

    /** Trạng thái hiển thị của bác sĩ, suy ra từ tài khoản liên kết. */
    public enum TrangThai {
        DANG_LAM("Đang làm việc"),
        TAM_NGHI("Tạm nghỉ"),
        CHUA_TK("Chưa có tài khoản");

        public final String nhan;

        TrangThai(String nhan) {
            this.nhan = nhan;
        }

        public static TrangThai tuEntity(BacSi_entity bs) {
            Boolean tk = bs.getTrangThaiTK();
            if (tk == null)
                return CHUA_TK;
            return tk ? DANG_LAM : TAM_NGHI;
        }
    }

    // ====================== KẾT QUẢ TRẢ VỀ CHO GUI ======================

    public static class DuLieuDanhSach {
        private final List<BacSi_entity> bacSi;
        private final int tongNhanVien;

        DuLieuDanhSach(List<BacSi_entity> bacSi, int tongNhanVien) {
            this.bacSi = bacSi;
            this.tongNhanVien = tongNhanVien;
        }

        public List<BacSi_entity> getBacSi() {
            return bacSi;
        }

        public int getTongNhanVien() {
            return tongNhanVien;
        }
    }

    public static class ThongKe {
        public final int tongSo, dangLam, tamNghi, soCa;

        ThongKe(int tongSo, int dangLam, int tamNghi, int soCa) {
            this.tongSo = tongSo;
            this.dangLam = dangLam;
            this.tamNghi = tamNghi;
            this.soCa = soCa;
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

    /** Lịch làm việc 7 ngày (thứ Hai đến Chủ nhật) đã định dạng sẵn để hiển thị. */
    public static class LichTuan {
        public final String tuNgay, denNgay;
        /** Mỗi dòng: { ngày, giờ bắt đầu, giờ kết thúc, trạng thái }. */
        public final List<String[]> dong;

        LichTuan(String tuNgay, String denNgay, List<String[]> dong) {
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
            this.dong = dong;
        }
    }

    private final BacSi_DAO dao = new BacSi_DAO();
    private final ChuyenNganh_DAO chuyenNganhDao = new ChuyenNganh_DAO();

    // ====================== TẢI DỮ LIỆU / THỐNG KÊ ======================

    public DuLieuDanhSach taiDanhSach() throws SQLException {
        return new DuLieuDanhSach(dao.getAll(), dao.getTongNhanVien());
    }

    public ThongKe thongKe(List<BacSi_entity> ds) {
        int dangLam = 0, tamNghi = 0, soCa = 0;
        for (BacSi_entity bs : ds) {
            if (TrangThai.tuEntity(bs) == TrangThai.DANG_LAM)
                dangLam++;
            else if (TrangThai.tuEntity(bs) == TrangThai.TAM_NGHI)
                tamNghi++;
            soCa += bs.getSoCaKham();
        }
        return new ThongKe(ds.size(), dangLam, tamNghi, soCa);
    }

    public static String nhanThangHienTai() {
        return DINH_DANG_THANG.format(LocalDate.now());
    }

    // ====================== LỌC / SẮP XẾP ======================

    /**
     * @param tuKhoa      tìm theo tên, mã, SĐT (không phân biệt hoa thường và dấu)
     * @param chuyenNganh tên chuyên ngành, null = tất cả
     * @param trangThai   tên của {@link TrangThai}, null = tất cả
     * @param cotSapXep   {@link #COT_HO_TEN}, {@link #COT_SO_CA} hoặc -1 (không sắp
     *                    xếp)
     */
    public List<BacSi_entity> locDanhSach(List<BacSi_entity> ds, String tuKhoa, String chuyenNganh,
            String trangThai, int cotSapXep, boolean tang) {
        String kw = boDau(tuKhoa).trim();
        List<BacSi_entity> kq = new ArrayList<>();
        for (BacSi_entity bs : ds) {
            if (!kw.isEmpty()) {
                String hay = boDau(nz(bs.getHoTen()) + " " + nz(bs.getMaBacSi()) + " " + nz(bs.getSdt()));
                if (!hay.contains(kw))
                    continue;
            }
            if (chuyenNganh != null && !chuyenNganh.equals(tenChuyenNganh(bs)))
                continue;
            if (trangThai != null && !trangThai.equals(TrangThai.tuEntity(bs).name()))
                continue;
            kq.add(bs);
        }

        Comparator<BacSi_entity> cmp = null;
        if (cotSapXep == COT_HO_TEN) {
            Collator col = Collator.getInstance(VN);
            // Tên người Việt: so sánh theo tên (từ cuối), sau đó theo cả họ tên
            cmp = Comparator.comparing((BacSi_entity bs) -> tuCuoi(bs.getHoTen()), col)
                    .thenComparing(bs -> nz(bs.getHoTen()), col);
        } else if (cotSapXep == COT_SO_CA) {
            cmp = Comparator.comparingInt(BacSi_entity::getSoCaKham);
        }
        if (cmp != null)
            kq.sort(tang ? cmp : cmp.reversed());
        return kq;
    }

    // ====================== CHUYÊN NGÀNH ======================

    public List<ChuyenNganh_entity> taiChuyenNganh() throws SQLException {
        return chuyenNganhDao.getAll();
    }

    public void themChuyenNganh(String ten) throws SQLException {
        String t = nz(ten).trim();
        if (t.isEmpty())
            throw new SQLException("Tên chuyên ngành không được để trống.");
        if (t.length() > 100)
            throw new SQLException("Tên chuyên ngành tối đa 100 ký tự.");
        if (chuyenNganhDao.getMaTheoTen(t) != null)
            return; // đã tồn tại, dùng lại
        if (!chuyenNganhDao.them(t))
            throw new SQLException("Không thêm được chuyên ngành.");
    }

    // ====================== THÊM / SỬA ======================

    public String taoMaMoi() throws SQLException {
        return dao.getMaTiepTheo();
    }

    /**
     * Kiểm tra dữ liệu form rồi thêm (goc == null) hoặc cập nhật (goc != null).
     * Tài khoản liên kết được giữ nguyên; chỉ trạng thái được đổi nếu khác.
     *
     * @param dangLam trạng thái tài khoản mong muốn (bỏ qua nếu bác sĩ chưa có tài
     *                khoản)
     */
    public KetQuaLuu validateAndSave(String ma, BacSi_entity goc, String hoTen, String sdt, String email,
            String ngaySinh, String gioiTinh, String chuyenNganh, String luongThang, boolean dangLam)
            throws SQLException {

        hoTen = nz(hoTen).trim();
        email = nz(email).trim();
        ngaySinh = nz(ngaySinh).trim();
        chuyenNganh = nz(chuyenNganh).trim();

        if (hoTen.isEmpty())
            return KetQuaLuu.loi("Vui lòng nhập họ và tên.");
        if (hoTen.length() > 100)
            return KetQuaLuu.loi("Họ và tên tối đa 100 ký tự.");

        String sdtSo = nz(sdt).replaceAll("[\\s.\\-]", "");
        if (sdtSo.isEmpty())
            return KetQuaLuu.loi("Vui lòng nhập số điện thoại.");
        if (!sdtSo.matches("0\\d{9}"))
            return KetQuaLuu.loi("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");

        // Cột email trong DB là NOT NULL
        if (email.isEmpty())
            return KetQuaLuu.loi("Vui lòng nhập email.");
        if (email.length() > 100 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            return KetQuaLuu.loi("Email không hợp lệ.");

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
        // Cột maChuyenNganh trong DB là NOT NULL
        if (chuyenNganh.isEmpty())
            return KetQuaLuu.loi("Vui lòng chọn chuyên ngành.");

        String luongSo = nz(luongThang).replaceAll("[\\s.,]", "");
        if (!luongSo.matches("\\d{1,12}"))
            return KetQuaLuu.loi("Lương tháng phải là số không âm.");
        double luong = Double.parseDouble(luongSo);

        BacSi_entity bs;
        boolean ok;
        if (goc == null) {
            // Phòng trường hợp mã hiển thị trên form đã bị người khác dùng mất
            String maLuu = dao.getBacSiTheoMa(ma) != null ? taoMaMoi() : ma;
            bs = new BacSi_entity(maLuu, null, hoTen, gioiTinh, sdtSo, chuyenNganh, email, ngay, luong, 0, null);
            ok = dao.them(bs);
        } else {
            bs = new BacSi_entity(goc.getMaBacSi(), maTaiKhoan(goc), hoTen, gioiTinh, sdtSo, chuyenNganh, email,
                    ngay, luong, goc.getSoCaKham(), goc.getTrangThaiTK());
            ok = dao.capNhat(bs);
        }
        if (!ok)
            return KetQuaLuu.loi("Không lưu được bác sĩ vào cơ sở dữ liệu.");

        if (goc != null && goc.getMaTK() != null && dangLam != Boolean.TRUE.equals(goc.getTrangThaiTK())) {
            dao.doiTrangThaiTaiKhoan(goc.getMaBacSi(), dangLam);
        }
        return KetQuaLuu.ok();
    }

    // ====================== ĐỔI TRẠNG THÁI / XÓA ======================

    public void doiTrangThai(String maBacSi, boolean hoatDong) throws SQLException {
        if (!dao.doiTrangThaiTaiKhoan(maBacSi, hoatDong))
            throw new SQLException("Không cập nhật được trạng thái tài khoản của " + maBacSi + ".");
    }

    /**
     * Xóa bác sĩ. Nếu đã có dữ liệu tham chiếu (LichLamViec, HoSoSucKhoe, DonThuoc,
     * ChiTietHoaDon) thì SQL Server báo lỗi khóa ngoại 547 và coDuLieuLienQuan =
     * true.
     */
    public KetQuaXoa xoa(String maBacSi) throws SQLException {
        try {
            return new KetQuaXoa(dao.xoa(maBacSi), false);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547 || "23000".equals(ex.getSQLState()))
                return new KetQuaXoa(false, true);
            throw ex;
        }
    }

    // ====================== LỊCH LÀM VIỆC ======================

    /**
     * Lịch tuần hiện tại (thứ Hai - Chủ nhật); ngày không có lịch hiện "Không có
     * lịch".
     */
    public LichTuan taiLichTuan(String maBacSi) throws SQLException {
        LocalDate dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate cuoiTuan = dauTuan.plusWeeks(1); // không bao gồm
        List<BacSi_DAO.LichLamViecDong> lich = dao.getLichLamViec(maBacSi, dauTuan, cuoiTuan);

        List<String[]> dong = new ArrayList<>();
        for (LocalDate ngay = dauTuan; ngay.isBefore(cuoiTuan); ngay = ngay.plusDays(1)) {
            boolean co = false;
            for (BacSi_DAO.LichLamViecDong l : lich) {
                if (ngay.equals(l.ngay)) {
                    co = true;
                    dong.add(new String[] { dinhDangNgay(ngay), gio(l.gioBatDau), gio(l.gioKetThuc),
                            nz(l.trangThai) });
                }
            }
            if (!co)
                dong.add(new String[] { dinhDangNgay(ngay), "—", "—", "Không có lịch" });
        }
        return new LichTuan(dinhDangNgay(dauTuan), dinhDangNgay(cuoiTuan.minusDays(1)), dong);
    }

    // ====================== XUẤT CSV ======================

    public String taoNoiDungCsv(List<BacSi_entity> ds) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM UTF-8 để Excel đọc đúng tiếng Việt
        sb.append(csvDong("Mã bác sĩ", "Họ và tên", "Giới tính", "Ngày sinh", "Số điện thoại", "Email",
                "Chuyên ngành", "Lương tháng", "Số ca khám tháng này", "Trạng thái"));
        for (BacSi_entity bs : ds) {
            sb.append(csvDong(
                    bs.getMaBacSi(),
                    bs.getHoTen(),
                    bs.getGioiTinh(),
                    dinhDangNgay(bs.getNgaySinh()),
                    bs.getSdt(),
                    bs.getEmail(),
                    tenChuyenNganh(bs),
                    String.format("%.0f", bs.getLuongThang()),
                    String.valueOf(bs.getSoCaKham()),
                    TrangThai.tuEntity(bs).nhan));
        }
        return sb.toString();
    }

    public void ghiCsv(File file, List<BacSi_entity> ds) throws IOException {
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

    public static String dinhDangSdt(String s) {
        if (s == null)
            return "";
        String so = s.replaceAll("\\D", "");
        if (so.length() == 10)
            return so.substring(0, 4) + " " + so.substring(4, 7) + " " + so.substring(7);
        return s;
    }

    /** Tên chuyên ngành của bác sĩ ("" nếu chưa có). */
    public static String tenChuyenNganh(BacSi_entity bs) {
        return bs.getChuyenNganh() == null ? "" : nz(bs.getChuyenNganh().getTenChuyenNganh());
    }

    // ====================== TIỆN ÍCH NỘI BỘ ======================

    private static String maTaiKhoan(BacSi_entity bs) {
        return bs.getMaTK() == null ? null : bs.getMaTK().getMaTK();
    }

    private static String gio(LocalTime t) {
        return t == null ? "—" : DINH_DANG_GIO.format(t);
    }

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