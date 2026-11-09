package controller.Chung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Logic dùng chung cho bảng dữ liệu (tìm kiếm, xuất CSV) và header
 * (chuẩn hóa thông tin người dùng, chữ cái avatar). Không chứa thành phần giao
 * diện.
 */
public class BangVaHeaderController {

    public static final String TEN_NGUOI_DUNG_MAC_DINH = "Người dùng";
    public static final String CHUC_VU_MAC_DINH = "Quản lý";
    public static final String AVATAR_MAC_DINH = "SL";
    public static final String TEN_FILE_CSV_MAC_DINH = "DanhSach.csv";

    // ==========================================================
    // HEADER
    // ==========================================================
    public String chuanHoaTenNguoiDung(String ten) {
        return (ten != null && !ten.trim().isEmpty()) ? ten : TEN_NGUOI_DUNG_MAC_DINH;
    }

    public String chuanHoaChucVu(String chucVu) {
        return (chucVu != null && !chucVu.trim().isEmpty()) ? chucVu : CHUC_VU_MAC_DINH;
    }

    /** Hai chữ cái cuối của họ tên (hoặc 1 chữ nếu chỉ có một từ), viết hoa. */
    public String layChuCaiAvatar(String tenNguoiDung) {
        String ten = tenNguoiDung == null ? "" : tenNguoiDung.trim();
        String[] parts = ten.split("\\s+");
        if (parts.length >= 2) {
            return (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                    .toUpperCase(Locale.ROOT);
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return AVATAR_MAC_DINH;
    }

    // ==========================================================
    // BẢNG DỮ LIỆU: TÌM KIẾM
    // ==========================================================
    /**
     * Từ khóa thật sự dùng để lọc; trả về "" nếu ô còn placeholder hoặc để trống.
     */
    public String layTuKhoaTimKiem(String noiDungOTim, String placeholder) {
        if (noiDungOTim == null)
            return "";
        String text = noiDungOTim.trim();
        if (text.isEmpty() || text.equals(placeholder))
            return "";
        return text;
    }

    /** Dòng khớp nếu có ít nhất một ô chứa từ khóa (không phân biệt hoa/thường). */
    public boolean khopTimKiem(String tuKhoa, Object[] giaTriCacCot) {
        if (tuKhoa == null || tuKhoa.isEmpty())
            return true;
        String kw = tuKhoa.toLowerCase(Locale.ROOT);
        for (Object o : giaTriCacCot) {
            if (o != null && o.toString().toLowerCase(Locale.ROOT).contains(kw)) {
                return true;
            }
        }
        return false;
    }

    // ==========================================================
    // BẢNG DỮ LIỆU: XUẤT CSV
    // ==========================================================
    /** Ghi UTF-8 có BOM để Excel hiển thị đúng tiếng Việt. */
    public void xuatCSV(File file, String[] tenCot, Object[][] duLieu) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            for (int c = 0; c < tenCot.length; c++) {
                writer.write("\"" + tenCot[c] + "\"" + (c < tenCot.length - 1 ? "," : ""));
            }
            writer.write("\r\n");
            for (Object[] dong : duLieu) {
                for (int c = 0; c < tenCot.length; c++) {
                    Object val = c < dong.length ? dong[c] : null;
                    String str = val != null ? val.toString().replace("\"", "\"\"") : "";
                    writer.write("\"" + str + "\"" + (c < tenCot.length - 1 ? "," : ""));
                }
                writer.write("\r\n");
            }
        }
    }
}
