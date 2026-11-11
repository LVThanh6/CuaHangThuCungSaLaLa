package Service.DichVu;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dao.DichVu.KhuyenMai_DAO;
import entity.DichVu.KhuyenMai_Entity;

public class KhuyenMaiService {
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<String> DANH_SACH_TAM_DUNG = new HashSet<>();
    private static final DecimalFormat DINH_DANG_TIEN;

    static {
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols(Locale.US);
        kyHieu.setGroupingSeparator('.');
        DINH_DANG_TIEN = new DecimalFormat("#,##0", kyHieu);
    }

    private final KhuyenMai_DAO dao;

    public KhuyenMaiService() {
        this(new KhuyenMai_DAO());
    }

    KhuyenMaiService(KhuyenMai_DAO dao) {
        this.dao = dao;
    }

    public List<KhuyenMai_Entity> taiDanhSach() {
        return java.util.Objects.requireNonNull(dao.getAllKhuyenMai(),
                "Promotion data was not returned by the DAO");
    }

    public Status trangThai(KhuyenMai_Entity khuyenMai) {
        LocalDate homNay = LocalDate.now();
        if (khuyenMai.getNgayKetThuc() != null && khuyenMai.getNgayKetThuc().isBefore(homNay)) return Status.HET_HAN;
        if (DANH_SACH_TAM_DUNG.contains(khuyenMai.getMaKM())) return Status.TAM_DUNG;
        if (khuyenMai.getNgayBatDau() != null && khuyenMai.getNgayBatDau().isAfter(homNay)) return Status.SAP_DIEN_RA;
        return Status.DANG_AP_DUNG;
    }

    public boolean dangTamDung(String ma) {
        return DANH_SACH_TAM_DUNG.contains(ma);
    }

    public DateRange phanTichKhoangNgay(String chuoiTu, String chuoiDen) {
        try {
            LocalDate tuNgay = chuoiTu.trim().isEmpty() ? null : LocalDate.parse(chuoiTu.trim(), DINH_DANG_NGAY);
            LocalDate denNgay = chuoiDen.trim().isEmpty() ? null : LocalDate.parse(chuoiDen.trim(), DINH_DANG_NGAY);
            if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
                throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            }
            return new DateRange(tuNgay, denNgay);
        } catch (DateTimeParseException loi) {
            throw new IllegalArgumentException("Ngày không hợp lệ (định dạng dd/MM/yyyy).", loi);
        }
    }

    public void tamDung(String ma) {
        DANH_SACH_TAM_DUNG.add(ma);
    }

    public void tiepTuc(String ma) {
        DANH_SACH_TAM_DUNG.remove(ma);
    }

    public static boolean laKhuyenMaiTamDung(String ma) {
        return DANH_SACH_TAM_DUNG.contains(ma);
    }

    public boolean laPhanTram(KhuyenMai_Entity khuyenMai) {
        return khuyenMai.getMucKM() <= 100;
    }

    public static boolean laGiaTriPhanTram(double muc) {
        return muc <= 100;
    }

    public static String dinhDangSo(double muc) {
        return soThuan(muc);
    }

    public String dinhDangMuc(KhuyenMai_Entity khuyenMai) {
        double muc = khuyenMai.getMucKM();
        return muc <= 100 ? soThuan(muc) + "%" : DINH_DANG_TIEN.format((long) muc) + " ₫";
    }

    public List<KhuyenMai_Entity> loc(List<KhuyenMai_Entity> danhSach, String tuKhoa, int statusIndex,
            LocalDate tuNgay, LocalDate denNgay) {
        String tuKhoaDaChuanHoa = tuKhoa.trim().toLowerCase();
        Status[] cacTrangThai = {null, Status.DANG_AP_DUNG, Status.SAP_DIEN_RA, Status.TAM_DUNG, Status.HET_HAN};
        List<KhuyenMai_Entity> ketQua = new ArrayList<>();
        for (KhuyenMai_Entity khuyenMai : danhSach) {
            if (!tuKhoaDaChuanHoa.isEmpty()
                    && !anToan(khuyenMai.getMaKM()).toLowerCase().contains(tuKhoaDaChuanHoa)
                    && !anToan(khuyenMai.getTenKM()).toLowerCase().contains(tuKhoaDaChuanHoa)) continue;
            if (statusIndex > 0 && trangThai(khuyenMai) != cacTrangThai[statusIndex]) continue;
            if (tuNgay != null && khuyenMai.getNgayKetThuc() != null
                    && khuyenMai.getNgayKetThuc().isBefore(tuNgay)) continue;
            if (denNgay != null && khuyenMai.getNgayBatDau() != null
                    && khuyenMai.getNgayBatDau().isAfter(denNgay)) continue;
            ketQua.add(khuyenMai);
        }
        return ketQua;
    }

    public Summary tomTat(List<KhuyenMai_Entity> danhSach) {
        LocalDate homNay = LocalDate.now();
        int soDangApDung = 0;
        int soSapDienRa = 0;
        int soTamDung = 0;
        int soHetHan = 0;
        for (KhuyenMai_Entity khuyenMai : danhSach) {
            switch (trangThai(khuyenMai)) {
                case DANG_AP_DUNG:
                    soDangApDung++;
                    break;
                case SAP_DIEN_RA:
                    if (khuyenMai.getNgayBatDau() == null
                            || !khuyenMai.getNgayBatDau().isAfter(homNay.plusDays(30))) soSapDienRa++;
                    break;
                case TAM_DUNG:
                    soTamDung++;
                    break;
                case HET_HAN:
                    soHetHan++;
                    break;
                default:
                    throw new IllegalStateException("Unknown promotion status");
            }
        }
        return new Summary(danhSach.size(), soDangApDung, soSapDienRa, soTamDung, soHetHan);
    }

    public boolean luu(String ma, String ten, boolean theoPhanTram, String chuoiMuc, String chuoiBatDau,
            String chuoiKetThuc, String dieuKien, List<KhuyenMai_Entity> daCo, boolean capNhat) {
        String maDaChuanHoa = ma.trim();
        String tenDaChuanHoa = ten.trim();
        if (maDaChuanHoa.isEmpty()) throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
        if (tenDaChuanHoa.isEmpty()) throw new IllegalArgumentException("Tên khuyến mãi không được để trống!");

        double muc;
        LocalDate ngayBatDau;
        LocalDate ngayKetThuc;
        try {
            muc = theoPhanTram
                    ? Double.parseDouble(chuoiMuc.trim().replace(",", "."))
                    : Double.parseDouble(chuoiMuc.trim().replace(".", "").replace(",", ""));
            ngayBatDau = LocalDate.parse(chuoiBatDau.trim(), DINH_DANG_NGAY);
            ngayKetThuc = LocalDate.parse(chuoiKetThuc.trim(), DINH_DANG_NGAY);
        } catch (NumberFormatException | DateTimeParseException loi) {
            throw new IllegalArgumentException(
                    "Dữ liệu nhập không hợp lệ (kiểm tra định dạng số và ngày dd/MM/yyyy).", loi);
        }

        if (theoPhanTram && (muc <= 0 || muc > 100)) {
            throw new IllegalArgumentException("Mức giảm theo phần trăm phải trong khoảng (0, 100].");
        }
        if (!theoPhanTram && muc <= 100) {
            throw new IllegalArgumentException("Mức giảm theo số tiền phải lớn hơn 100 ₫.");
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }
        if (!capNhat) {
            for (KhuyenMai_Entity khuyenMai : daCo) {
                if (maDaChuanHoa.equalsIgnoreCase(khuyenMai.getMaKM())) {
                    throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại!");
                }
            }
        }

        KhuyenMai_Entity khuyenMai = new KhuyenMai_Entity(
                maDaChuanHoa, tenDaChuanHoa, muc, ngayBatDau, ngayKetThuc, dieuKien.trim());
        return capNhat ? dao.updateKhuyenMai(khuyenMai) : dao.createKhuyenMai(khuyenMai);
    }

    public boolean xoa(String ma) {
        return dao.deleteKhuyenMai(ma);
    }

    public String taoCsv(List<KhuyenMai_Entity> danhSach) {
        StringBuilder noiDungCsv = new StringBuilder("\uFEFF");
        noiDungCsv.append("Mã khuyến mãi,Tên khuyến mãi,Mức giảm,Loại giảm,Ngày bắt đầu,Ngày kết thúc,Điều kiện,Trạng thái\r\n");
        for (KhuyenMai_Entity khuyenMai : danhSach) {
            noiDungCsv.append(trichDanCsv(khuyenMai.getMaKM())).append(',')
                    .append(trichDanCsv(khuyenMai.getTenKM())).append(',')
                    .append(trichDanCsv(dinhDangMuc(khuyenMai))).append(',')
                    .append(trichDanCsv(laPhanTram(khuyenMai) ? "Phần trăm" : "Số tiền")).append(',')
                    .append(trichDanCsv(dinhDangNgay(khuyenMai.getNgayBatDau()))).append(',')
                    .append(trichDanCsv(dinhDangNgay(khuyenMai.getNgayKetThuc()))).append(',')
                    .append(trichDanCsv(anToan(khuyenMai.getDieuKien()))).append(',')
                    .append(trichDanCsv(trangThai(khuyenMai).nhan)).append("\r\n");
        }
        return noiDungCsv.toString();
    }

    private static String dinhDangNgay(LocalDate ngay) {
        return ngay == null ? "" : DINH_DANG_NGAY.format(ngay);
    }

    private static String trichDanCsv(String giaTri) {
        return "\"" + anToan(giaTri).replace("\"", "\"\"") + "\"";
    }

    private static String soThuan(double giaTri) {
        return giaTri == Math.rint(giaTri) ? String.valueOf((long) giaTri) : String.valueOf(giaTri);
    }

    private static String anToan(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }

    public enum Status {
        DANG_AP_DUNG("Đang áp dụng"), SAP_DIEN_RA("Sắp diễn ra"), TAM_DUNG("Tạm dừng"), HET_HAN("Hết hạn");

        public final String nhan;

        Status(String nhan) {
            this.nhan = nhan;
        }
    }

    public static final class DateRange {
        public final LocalDate tuNgay;
        public final LocalDate denNgay;

        private DateRange(LocalDate tuNgay, LocalDate denNgay) {
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
        }
    }

    public static final class Summary {
        public final int tong;
        public final int soDangApDung;
        public final int soSapDienRa;
        public final int soTamDung;
        public final int soHetHan;

        private Summary(int tong, int soDangApDung, int soSapDienRa, int soTamDung, int soHetHan) {
            this.tong = tong;
            this.soDangApDung = soDangApDung;
            this.soSapDienRa = soSapDienRa;
            this.soTamDung = soTamDung;
            this.soHetHan = soHetHan;
        }
    }
}