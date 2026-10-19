package GUI;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Lớp tiện ích xuất file Báo cáo PDF (PDF 1.4) thuần túy trong Java.
 * Không phụ thuộc thư viện bên ngoài, tích hợp đầy đủ:
 * - Tiêu đề & Thông tin thương hiệu SALALA
 * - 4 Chỉ số KPI hoạt động
 * - Biểu đồ trực quan chất lượng cao (nhúng trực tiếp từ BieuDoPanel)
 * - Bảng số liệu Top Dịch vụ và Doanh số Nhân viên
 * - Phần chữ ký xác nhận báo cáo
 */
public class BaoCaoPDF_Exporter {

    public static boolean xuatBaoCaoPDF(File file,
                                        String kyBaoCao,
                                        String nguoiLap,
                                        String tongDoanhThu,
                                        String tongHoaDon,
                                        String tongKhachHang,
                                        String tongThuCung,
                                        List<Object[]> topDichVu,
                                        List<Object[]> doanhSoNV,
                                        BufferedImage bieuDoImg) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            int[] offsets = new int[20];
            int objCount = 0;

            body.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 1: Catalog
            objCount = 1;
            offsets[objCount] = body.size();
            body.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 2: Pages
            objCount = 2;
            offsets[objCount] = body.size();
            body.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 3: Page (Khổ A4: 595 x 842 points)
            objCount = 3;
            offsets[objCount] = body.size();
            body.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> /XObject << /ImChart 7 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Chuyển biểu đồ sang ảnh JPEG
            byte[] imgBytes = new byte[0];
            int imgW = 500, imgH = 220;
            if (bieuDoImg != null) {
                imgW = bieuDoImg.getWidth();
                imgH = bieuDoImg.getHeight();
                ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                ImageIO.write(bieuDoImg, "jpg", imgBaos);
                imgBytes = imgBaos.toByteArray();
            }

            // Obj 4: Content Stream (Nội dung trang PDF)
            ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String thoiGianLap = sdf.format(new Date());

            StringBuilder sb = new StringBuilder();

            // Khung Banner tiêu đề màu xanh Teal
            sb.append("0.05 0.58 0.53 rg\n"); // Màu Teal #0D9488
            sb.append("35 770 525 45 re f\n");

            // Chữ tiêu đề trên Banner
            sb.append("BT\n");
            sb.append("/F2 16 Tf\n1 1 1 rg\n"); // Chữ trắng
            sb.append("50 788 Td\n(HE THONG QUAN LY SALALA PET SHOP - BAO CAO THONG KE) Tj\n");
            sb.append("ET\n");

            // Thông tin ngày lập và kỳ báo cáo
            sb.append("BT\n");
            sb.append("/F1 10 Tf\n0.2 0.25 0.3 rg\n");
            sb.append("35 750 Td\n(").append(escapePdf("Ky bao cao: " + boDauTiengViet(kyBaoCao))).append(") Tj\n");
            sb.append("300 0 Td\n(").append(escapePdf("Ngay xuat: " + thoiGianLap)).append(") Tj\n");
            sb.append("ET\n");

            // Đường kẻ ngang phân cách
            sb.append("0.85 0.88 0.92 RG\n1 w\n35 742 m 560 742 l S\n");

            // Khung 4 Thẻ KPI (vẽ hình chữ nhật bo góc nhẹ)
            sb.append("0.96 0.97 0.98 rg\n35 680 120 50 re f\n");
            sb.append("0.96 0.97 0.98 rg\n170 680 120 50 re f\n");
            sb.append("0.96 0.97 0.98 rg\n305 680 120 50 re f\n");
            sb.append("0.96 0.97 0.98 rg\n440 680 120 50 re f\n");

            // Viền các thẻ KPI
            sb.append("0.85 0.88 0.92 RG\n");
            sb.append("35 680 120 50 re S\n");
            sb.append("170 680 120 50 re S\n");
            sb.append("305 680 120 50 re S\n");
            sb.append("440 680 120 50 re S\n");

            // Nội dung thẻ KPI 1: Doanh thu
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n42 715 Td\n(TONG DOANH THU) Tj\n");
            sb.append("/F2 11 Tf\n0.05 0.58 0.53 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongDoanhThu))).append(") Tj\nET\n");

            // Thẻ KPI 2: Hóa đơn
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n177 715 Td\n(SO HOA DON) Tj\n");
            sb.append("/F2 11 Tf\n0.1 0.4 0.8 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongHoaDon))).append(") Tj\nET\n");

            // Thẻ KPI 3: Khách hàng
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n312 715 Td\n(KHACH HANG) Tj\n");
            sb.append("/F2 11 Tf\n0.1 0.6 0.3 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongKhachHang))).append(") Tj\nET\n");

            // Thẻ KPI 4: Thú cưng
            sb.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n447 715 Td\n(THU CUNG) Tj\n");
            sb.append("/F2 11 Tf\n0.6 0.2 0.7 rg\n0 -16 Td\n(").append(escapePdf(boDauTiengViet(tongThuCung))).append(") Tj\nET\n");

            // Chèn biểu đồ đồ họa chất lượng cao
            if (imgBytes.length > 0) {
                // Viền khung chứa biểu đồ
                sb.append("0.85 0.88 0.92 RG\n35 480 525 185 re S\n");
                // Lệnh vẽ hình ảnh ImChart trong PDF
                sb.append("q\n523 0 0 183 36 481 cm\n/ImChart Do\nQ\n");
            }

            // Phần Bảng số liệu: Top Dịch vụ & Sản phẩm
            int curY = 450;
            sb.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(curY).append(" Td\n(1. TOP 5 DICH VU VA SAN PHAM DOANH THU CAO NHAT) Tj\nET\n");

            curY -= 18;
            // Tiêu đề bảng
            sb.append("0.93 0.95 0.97 rg\n35 ").append(curY - 4).append(" 525 18 re f\n");
            sb.append("BT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            sb.append("42 ").append(curY).append(" Td\n(STT) Tj\n");
            sb.append("50 0 Td\n(TEN DICH VU / SAN PHAM) Tj\n");
            sb.append("260 0 Td\n(SO LUOT) Tj\n");
            sb.append("90 0 Td\n(DOANH THU) Tj\n");
            sb.append("ET\n");

            // Các dòng dữ liệu dịch vụ
            if (topDichVu != null) {
                int stt = 1;
                for (Object[] r : topDichVu) {
                    if (stt > 5) break;
                    curY -= 16;
                    if (stt % 2 == 0) {
                        sb.append("0.98 0.98 0.99 rg\n35 ").append(curY - 3).append(" 525 15 re f\n");
                    }
                    sb.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n");
                    sb.append("42 ").append(curY).append(" Td\n(").append(stt).append(") Tj\n");
                    sb.append("50 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[0])))).append(") Tj\n");
                    sb.append("260 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[1])))).append(") Tj\n");
                    sb.append("90 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[2])))).append(") Tj\n");
                    sb.append("ET\n");
                    stt++;
                }
            }

            // Phần Bảng số liệu: Doanh số nhân viên
            curY -= 26;
            sb.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(curY).append(" Td\n(2. DOANH SO PHUC VU THEO NHAN VIEN VA BAC SI) Tj\nET\n");

            curY -= 18;
            sb.append("0.93 0.95 0.97 rg\n35 ").append(curY - 4).append(" 525 18 re f\n");
            sb.append("BT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            sb.append("42 ").append(curY).append(" Td\n(MA NV) Tj\n");
            sb.append("60 0 Td\n(HO VA TEN) Tj\n");
            sb.append("180 0 Td\n(VAI TRO) Tj\n");
            sb.append("110 0 Td\n(SO DON) Tj\n");
            sb.append("70 0 Td\n(DOANH SO) Tj\n");
            sb.append("ET\n");

            if (doanhSoNV != null) {
                int stt = 1;
                for (Object[] r : doanhSoNV) {
                    if (stt > 4) break;
                    curY -= 16;
                    if (stt % 2 == 0) {
                        sb.append("0.98 0.98 0.99 rg\n35 ").append(curY - 3).append(" 525 15 re f\n");
                    }
                    sb.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n");
                    sb.append("42 ").append(curY).append(" Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[0])))).append(") Tj\n");
                    sb.append("60 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[1])))).append(") Tj\n");
                    sb.append("180 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[2])))).append(") Tj\n");
                    sb.append("110 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[3])))).append(") Tj\n");
                    sb.append("70 0 Td\n(").append(escapePdf(boDauTiengViet(String.valueOf(r[4])))).append(") Tj\n");
                    sb.append("ET\n");
                    stt++;
                }
            }

            // Chữ ký xác nhận bên dưới
            curY -= 35;
            sb.append("BT\n/F2 10 Tf\n0.2 0.25 0.3 rg\n");
            sb.append("60 ").append(curY).append(" Td\n(NGUOI LAP BIEU) Tj\n");
            sb.append("350 0 Td\n(GIAM DOC / QUAN LY) Tj\n");
            sb.append("/F1 9 Tf\n0.4 0.45 0.5 rg\n");
            sb.append("-340 -14 Td\n(Ky va ghi ro ho ten) Tj\n");
            sb.append("350 0 Td\n(Ky, dong dau xac nhan) Tj\n");

            // Tên người lập
            String tenNguoiKy = (nguoiLap != null && !nguoiLap.trim().isEmpty()) ? nguoiLap : "Le Van Thanh";
            sb.append("-345 -36 Td\n/F2 10 Tf\n0.1 0.15 0.2 rg\n(").append(escapePdf(boDauTiengViet(tenNguoiKy))).append(") Tj\n");
            sb.append("ET\n");

            // Chân trang bản quyền
            sb.append("BT\n/F1 8 Tf\n0.5 0.55 0.6 rg\n180 20 Td\n(Bao cao duoc tao tu dong boi He thong SALALA Pet Shop - salala.vn) Tj\nET\n");

            streamContent.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
            byte[] streamBytes = streamContent.toByteArray();

            offsets[4] = body.size();
            body.write(("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            body.write(streamBytes);
            body.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 5: Font F1 (Helvetica)
            objCount = 5;
            offsets[objCount] = body.size();
            body.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 6: Font F2 (Helvetica-Bold)
            objCount = 6;
            offsets[objCount] = body.size();
            body.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 7: Image XObject
            objCount = 7;
            offsets[objCount] = body.size();
            body.write(("7 0 obj\n<< /Type /XObject /Subtype /Image /Width " + imgW
                    + " /Height " + imgH + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length "
                    + imgBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            body.write(imgBytes);
            body.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Bảng XREF
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

    private static String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /**
     * Chuyển đổi chuỗi tiếng Việt có dấu sang không dấu chuẩn xác để hiển thị rõ đẹp trong PDF Type 1
     */
    public static String boDauTiengViet(String str) {
        if (str == null) return "";
        String nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        String s = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('đ', 'd').replace('Đ', 'D');
        return s;
    }
}
