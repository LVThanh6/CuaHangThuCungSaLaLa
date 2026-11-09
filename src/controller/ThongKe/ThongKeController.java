package controller.ThongKe;

import dao.NhanVien.NhanVien_DAO;
import dao.ThongKe.ThongKe_DAO;
import dao.ThongKe.ThongKe_DAO.ChiSoThongKe;
import dao.ThongKe.ThongKe_DAO.DuLieuThongKe;
import entity.NhanVien.NhanVien_entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public class ThongKeController {

    // ==========================================================
    // 1. CÁC ENUM VÀ CLASS ĐỊNH NGHĨA DỮ LIỆU (Giữ nguyên cho GUI)
    // ==========================================================

    public enum Category {
        DOANH_THU("Doanh thu"),
        BAN_HANG("Bán hàng"),
        NHAN_SU("Nhân sự");

        private final String name;
        Category(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum QuickRange {
        TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH, THIS_YEAR
    }

    public static class DateRange {
        private final LocalDate start;
        private final LocalDate end;
        public DateRange(LocalDate start, LocalDate end) { this.start = start; this.end = end; }
        public LocalDate getStart() { return start; }
        public LocalDate getEnd() { return end; }
    }

    public static class Indicator {
        private String code, name, unit, suffix, description;
        private double total;
        private List<String> labels, series;
        private List<List<Double>> values;

        public Indicator(String code, String name, String unit, String suffix, String description) {
            this.code = code;
            this.name = name;
            this.unit = unit;
            this.suffix = suffix;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public String getSuffix() { return suffix; }
        public String getDescription() { return description; }
        public double getTotal() { return total; }
        public List<String> getLabels() { return labels; }
        public List<String> getSeries() { return series; }
        public List<List<Double>> getValues() { return values; }

        public void setTotal(double total) { this.total = total; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public void setSeries(List<String> series) { this.series = series; }
        public void setValues(List<List<Double>> values) { this.values = values; }
    }

    public static class ReportData {
        private List<Indicator> indicators = new ArrayList<>();
        private double totalRevenue, invoiceCount, itemsSold, goodsRevenue, serviceRevenue, totalPayroll;

        public List<Indicator> getIndicators() { return indicators; }
        public void setIndicators(List<Indicator> indicators) { this.indicators = indicators; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public double getInvoiceCount() { return invoiceCount; }
        public void setInvoiceCount(double invoiceCount) { this.invoiceCount = invoiceCount; }

        public double getItemsSold() { return itemsSold; }
        public void setItemsSold(double itemsSold) { this.itemsSold = itemsSold; }

        public double getGoodsRevenue() { return goodsRevenue; }
        public void setGoodsRevenue(double goodsRevenue) { this.goodsRevenue = goodsRevenue; }

        public double getServiceRevenue() { return serviceRevenue; }
        public void setServiceRevenue(double serviceRevenue) { this.serviceRevenue = serviceRevenue; }

        public double getTotalPayroll() { return totalPayroll; }
        public void setTotalPayroll(double totalPayroll) { this.totalPayroll = totalPayroll; }
    }

    public static class LoadResult {
        private final boolean success;
        private final String errorMessage;
        private final ReportData report;

        public LoadResult(boolean success, String errorMessage, ReportData report) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.report = report;
        }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public ReportData getReport() { return report; }
    }

    // ==========================================================
    // 2. CÁC HÀM XỬ LÝ LOGIC ĐIỀU KIỆN
    // ==========================================================

    public static DateRange khoangNhanh(QuickRange range, LocalDate now) {
        switch (range) {
            case TODAY: return new DateRange(now, now);
            case LAST_7_DAYS: return new DateRange(now.minusDays(6), now);
            case LAST_30_DAYS: return new DateRange(now.minusDays(29), now);
            case THIS_MONTH: return new DateRange(now.withDayOfMonth(1), now);
            case THIS_YEAR: return new DateRange(now.with(TemporalAdjusters.firstDayOfYear()), now);
            default: return new DateRange(now, now);
        }
    }

    public static String loiKhoangNgay(LocalDate tu, LocalDate den) {
        if (tu.isAfter(den)) return "Ngày bắt đầu không được lớn hơn ngày kết thúc.";
        return null;
    }

    public static String moTaNhomThoiGian(LocalDate tu, LocalDate den) {
        return ThongKe_DAO.moTaNhomThoiGian(tu, den);
    }

    public static boolean isTimeSeries(String ma) {
        // Chỉ những biểu đồ biến động theo ngày/tháng thì mới là TimeSeries
        return ma.equals(ThongKe_DAO.MA_DOANH_THU_TONG) ||
               ma.equals(ThongKe_DAO.MA_DOANH_THU_HANG_HOA) ||
               ma.equals(ThongKe_DAO.MA_DOANH_THU_DICH_VU) ||
               ma.equals(ThongKe_DAO.MA_SO_HOA_DON) ||
               ma.equals(ThongKe_DAO.MA_SO_LUONG_BAN);
    }

    public static Category categoryFor(String ma) {
        return categoryFor(ma, null);
    }

    /**
     * Phân nhóm chỉ số. Trả về null nếu chỉ số bị ẩn khỏi giao diện
     * (ví dụ "Tiền lương nhân sự dự tính").
     * Nhóm Nhân sự chỉ còn "Chi tiết lương từng nhân sự".
     */
    public static Category categoryFor(String ma, String ten) {
        String tenKhongDau = boDau(ten);
        if (tenKhongDau.contains("chi tiet luong")) return Category.NHAN_SU;
        if (tenKhongDau.contains("tien luong") && tenKhongDau.contains("du tinh")) return null; // đã loại bỏ

        if (ma.startsWith("doanh-thu")) return Category.DOANH_THU;
        // "so-luong-ban" cũng chứa "luong" nên phải loại ra, nếu không sẽ lọt vào Nhân sự
        if (ma.equals(ThongKe_DAO.MA_SO_LUONG_BAN) || ma.contains("so-luong")) return Category.BAN_HANG;
        if (ma.contains("luong")) {
            // Chỉ giữ chỉ số chi tiết lương; chỉ số lương dự tính bị ẩn
            return ma.contains("chi-tiet") ? Category.NHAN_SU : null;
        }
        return Category.BAN_HANG; // Gồm hóa đơn, top, tồn kho...
    }

    private static String boDau(String s) {
        if (s == null) return "";
        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                  .replace('đ', 'd').replace('Đ', 'D')
                  .toLowerCase(Locale.ROOT).trim();
    }

    // ----------------------------------------------------------
    // Tìm kiếm nhân viên theo tên / SĐT / mã (áp dụng lên cột Danh mục)
    // ----------------------------------------------------------
    private volatile List<NhanVien_entity> dsNhanVienTraCuu = new ArrayList<>();

    private void napDanhSachNhanVien() {
        try {
            dsNhanVienTraCuu = new NhanVien_DAO().getAllNhanVien();
        } catch (Exception e) {
            e.printStackTrace();
            dsNhanVienTraCuu = new ArrayList<>();
        }
    }

    /**
     * Kiểm tra một dòng (nhãn ở cột Danh mục) có khớp từ khóa tìm kiếm không.
     * Từ khóa có thể là tên, SĐT hoặc mã nhân viên; không phân biệt hoa thường/dấu.
     */
    public boolean khopTuKhoaNhanVien(String tuKhoa, String nhanDong) {
        String kw = boDau(tuKhoa);
        if (kw.isEmpty()) return true;
        String nhan = boDau(nhanDong);
        if (nhan.contains(kw)) return true; // khớp trực tiếp trên nhãn (mã / tên)

        // Nhãn không chứa SĐT => tra theo danh sách nhân viên
        for (NhanVien_entity nv : dsNhanVienTraCuu) {
            String ma = boDau(nv.getMaNhanVien());
            String ten = boDau(nv.getHoTen());
            boolean dongThuocNv = (!ma.isEmpty() && nhan.contains(ma)) || (!ten.isEmpty() && nhan.contains(ten));
            if (!dongThuocNv) continue;
            String sdt = nv.getSdt() == null ? "" : nv.getSdt().replaceAll("\\s+", "");
            if (ma.contains(kw) || ten.contains(kw) || sdt.contains(kw.replaceAll("\\s+", ""))) return true;
        }
        return false;
    }

    // ==========================================================
    // 3. HÀM TẢI DỮ LIỆU TỪ DAO
    // ==========================================================

    public LoadResult taiDuLieu(LocalDate tu, LocalDate den) {
        try {
            // 1. Gọi DAO để lấy dữ liệu thống kê nguyên gốc
            napDanhSachNhanVien();
            ThongKe_DAO dao = new ThongKe_DAO();
            DuLieuThongKe duLieuDao = dao.layDuLieuThongKe(tu, den);

            // 2. Chuyển đổi (Map) Dữ liệu DAO sang định dạng ReportData của GUI
            ReportData report = new ReportData();
            report.setTotalRevenue(duLieuDao.doanhThuTong);
            report.setGoodsRevenue(duLieuDao.doanhThuHangHoa);
            report.setServiceRevenue(duLieuDao.doanhThuDichVu);
            report.setInvoiceCount(duLieuDao.soHoaDon);
            report.setItemsSold(duLieuDao.soLuongBan);
            report.setTotalPayroll(duLieuDao.tongLuong);

            List<Indicator> indicators = new ArrayList<>();
            for (ChiSoThongKe chiSoDao : duLieuDao.chiSo) {
                Indicator ind = new Indicator(
                    chiSoDao.ma, 
                    chiSoDao.ten, 
                    chiSoDao.donVi, 
                    chiSoDao.hauTo, 
                    chiSoDao.ghiChu
                );
                ind.setTotal(chiSoDao.tong);
                ind.setLabels(new ArrayList<>(chiSoDao.nhan));
                ind.setSeries(new ArrayList<>(chiSoDao.chuoi));
                ind.setValues(new ArrayList<>(chiSoDao.giaTri));
                
                indicators.add(ind);
            }
            report.setIndicators(indicators);

            return new LoadResult(true, null, report);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadResult(false, "Lỗi truy xuất dữ liệu: " + e.getMessage(), null);
        }
    }

    // ==========================================================
    // 4. LOGIC XUẤT FILE PDF TRỰC TIẾP
    // ==========================================================

    public boolean xuatBaoCaoPDF(File file, String kyBaoCao, String nguoiLap,
                                 String tongDoanhThu, String tongHoaDon,
                                 String tongKhachHang, String tongThuCung,
                                 List<Object[]> topDichVu, List<Object[]> doanhSoNV,
                                 BufferedImage bieuDoImg) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            int[] offsets = new int[20];
            int objCount = 0;

            body.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 1; offsets[objCount] = body.size();
            body.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 2; offsets[objCount] = body.size();
            body.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 3; offsets[objCount] = body.size();
            body.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> /XObject << /ImChart 7 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            byte[] imgBytes = new byte[0];
            int imgW = 500, imgH = 220;
            if (bieuDoImg != null) {
                imgW = bieuDoImg.getWidth();
                imgH = bieuDoImg.getHeight();
                ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                ImageIO.write(bieuDoImg, "jpg", imgBaos);
                imgBytes = imgBaos.toByteArray();
            }

            ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String thoiGianLap = sdf.format(new Date());

            StringBuilder sb = new StringBuilder();
            sb.append("0.05 0.58 0.53 rg\n35 770 525 45 re f\nBT\n/F2 16 Tf\n1 1 1 rg\n");
            sb.append("50 788 Td\n(HE THONG QUAN LY SALALA PET SHOP - BAO CAO THONG KE) Tj\nET\n");

            sb.append("BT\n/F1 10 Tf\n0.2 0.25 0.3 rg\n35 750 Td\n(").append(escapePdf("Ky bao cao: " + boDauTiengViet(kyBaoCao))).append(") Tj\n");
            sb.append("300 0 Td\n(").append(escapePdf("Ngay xuat: " + thoiGianLap)).append(") Tj\nET\n");

            sb.append("0.85 0.88 0.92 RG\n1 w\n35 742 m 560 742 l S\n");

            sb.append("0.96 0.97 0.98 rg\n35 680 120 50 re f\n0.96 0.97 0.98 rg\n170 680 120 50 re f\n");
            sb.append("0.96 0.97 0.98 rg\n305 680 120 50 re f\n0.96 0.97 0.98 rg\n440 680 120 50 re f\n");

            sb.append("0.85 0.88 0.92 RG\n35 680 120 50 re S\n170 680 120 50 re S\n305 680 120 50 re S\n440 680 120 50 re S\n");

            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n42 715 Td\n(TONG DOANH THU) Tj\n/F2 11 Tf\n0.05 0.58 0.53 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongDoanhThu))).append(") Tj\nET\n");
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n177 715 Td\n(SO HOA DON) Tj\n/F2 11 Tf\n0.1 0.4 0.8 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongHoaDon))).append(") Tj\nET\n");
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n312 715 Td\n(KHACH HANG) Tj\n/F2 11 Tf\n0.1 0.6 0.3 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongKhachHang))).append(") Tj\nET\n");
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n447 715 Td\n(THU CUNG) Tj\n/F2 11 Tf\n0.6 0.2 0.7 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongThuCung))).append(") Tj\nET\n");

            if (imgBytes.length > 0) {
                sb.append("0.85 0.88 0.92 RG\n35 480 525 185 re S\nq\n523 0 0 183 36 481 cm\n/ImChart Do\nQ\n");
            }

            int curY = 450;
            sb.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(curY).append(" Td\n(1. TOP 5 DICH VU VA SAN PHAM DOANH THU CAO NHAT) Tj\nET\n");
            curY -= 18;
            sb.append("0.93 0.95 0.97 rg\n35 ").append(curY - 4).append(" 525 18 re f\nBT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            sb.append("42 ").append(curY).append(" Td\n(STT) Tj\n50 0 Td\n(TEN DICH VU / SAN PHAM) Tj\n260 0 Td\n(SO LUOT) Tj\n90 0 Td\n(DOANH THU) Tj\nET\n");

            if (topDichVu != null) {
                int stt = 1;
                for (Object[] r : topDichVu) {
                    if (stt > 5) break;
                    curY -= 16;
                    if (stt % 2 == 0) sb.append("0.98 0.98 0.99 rg\n35 ").append(curY - 3).append(" 525 15 re f\n");
                    sb.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n42 ").append(curY).append(" Td\n(").append(stt).append(") Tj\n");
                    sb.append("50 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[0])))).append(") Tj\n260 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[1])))).append(") Tj\n90 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[2])))).append(") Tj\nET\n");
                    stt++;
                }
            }

            curY -= 26;
            sb.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(curY).append(" Td\n(2. DOANH SO PHUC VU THEO NHAN VIEN VA BAC SI) Tj\nET\n");
            curY -= 18;
            sb.append("0.93 0.95 0.97 rg\n35 ").append(curY - 4).append(" 525 18 re f\nBT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            sb.append("42 ").append(curY).append(" Td\n(MA NV) Tj\n60 0 Td\n(HO VA TEN) Tj\n180 0 Td\n(VAI TRO) Tj\n110 0 Td\n(SO DON) Tj\n70 0 Td\n(DOANH SO) Tj\nET\n");

            if (doanhSoNV != null) {
                int stt = 1;
                for (Object[] r : doanhSoNV) {
                    if (stt > 4) break;
                    curY -= 16;
                    if (stt % 2 == 0) sb.append("0.98 0.98 0.99 rg\n35 ").append(curY - 3).append(" 525 15 re f\n");
                    sb.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n42 ").append(curY).append(" Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[0])))).append(") Tj\n");
                    sb.append("60 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[1])))).append(") Tj\n180 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[2])))).append(") Tj\n110 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[3])))).append(") Tj\n70 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[4])))).append(") Tj\nET\n");
                    stt++;
                }
            }

            curY -= 35;
            sb.append("BT\n/F2 10 Tf\n0.2 0.25 0.3 rg\n60 ").append(curY).append(" Td\n(NGUOI LAP BIEU) Tj\n350 0 Td\n(GIAM DOC / QUAN LY) Tj\n");
            sb.append("/F1 9 Tf\n0.4 0.45 0.5 rg\n-340 -14 Td\n(Ky va ghi ro ho ten) Tj\n350 0 Td\n(Ky, dong dau xac nhan) Tj\n");

            String tenNguoiKy = (nguoiLap != null && !nguoiLap.trim().isEmpty()) ? nguoiLap : "Le Van Thanh";
            sb.append("-345 -36 Td\n/F2 10 Tf\n0.1 0.15 0.2 rg\n(").append(escapePdf(boDauTiengViet(tenNguoiKy))).append(") Tj\nET\n");

            sb.append("BT\n/F1 8 Tf\n0.5 0.55 0.6 rg\n180 20 Td\n(Bao cao duoc tao tu dong boi He thong SALALA Pet Shop - salala.vn) Tj\nET\n");

            streamContent.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
            byte[] streamBytes = streamContent.toByteArray();

            offsets[4] = body.size();
            body.write(("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            body.write(streamBytes);
            body.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 5; offsets[objCount] = body.size();
            body.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 6; offsets[objCount] = body.size();
            body.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            objCount = 7; offsets[objCount] = body.size();
            body.write(("7 0 obj\n<< /Type /XObject /Subtype /Image /Width " + imgW + " /Height " + imgH + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + imgBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            body.write(imgBytes);
            body.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            int xrefOffset = body.size();
            body.write("xref\n0 8\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 7; i++) {
                body.write(String.format("%010d 00000 n \n", offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }

            body.write(("trailer\n<< /Size 8 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

            fos.write(body.toByteArray());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    public String boDauTiengViet(String str) {
        if (str == null) return "";
        String nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        String s = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('đ', 'd').replace('Đ', 'D');
        return s;
    }
}