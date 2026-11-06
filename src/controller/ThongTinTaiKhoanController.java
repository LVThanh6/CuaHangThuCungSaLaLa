package controller;

public class ThongTinTaiKhoanController {
    public ThongTinHoSo taoThongTin(String tenHienThi) {
        String displayName = tenHienThi == null ? "Người dùng" : tenHienThi;
        String initials = "SL";
        if (!displayName.trim().isEmpty()) {
            String[] parts = displayName.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = (parts[parts.length - 2].substring(0, 1)
                        + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            } else {
                initials = parts[0].substring(0, 1).toUpperCase();
            }
        }
        return new ThongTinHoSo(displayName, initials);
    }

    public static final class ThongTinHoSo {
        public final String tenHienThi, chuCaiDau;
        private ThongTinHoSo(String tenHienThi, String chuCaiDau) {
            this.tenHienThi = tenHienThi; this.chuCaiDau = chuCaiDau;
        }
    }
}
