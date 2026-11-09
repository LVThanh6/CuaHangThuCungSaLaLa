package controller.LichHen;

import dao.LichHen.LichLamViec_DAO;
import entity.LichHen.LichLamViec_entity;
import entity.LichHen.NhanSuLichLamViec_entity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.time.temporal.TemporalAdjusters;

public class LichLamViecController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final LichLamViec_DAO dao;

    public LichLamViecController() {
        this(new LichLamViec_DAO());
    }

    LichLamViecController(LichLamViec_DAO dao) {
        this.dao = dao;
    }

    public DuLieuLich taiDuLieu(LocalDate tuNgay, LocalDate denNgayExclusive, YearMonth thang) throws SQLException {
        return new DuLieuLich(dao.getLichTrongKhoang(tuNgay, denNgayExclusive),
                dao.getSoLichTheoNgay(thang.atDay(1), thang.plusMonths(1).atDay(1)));
    }

    public KhoangNgay khoangXem(LocalDate ngayDangChon, String cheDoXem) {
        switch (cheDoXem) {
            case "NGAY":
                return new KhoangNgay(ngayDangChon, ngayDangChon.plusDays(1));
            case "TUAN":
                LocalDate dauTuan = ngayDangChon.with(TemporalAdjusters.previousOrSame(
                        java.time.DayOfWeek.MONDAY));
                return new KhoangNgay(dauTuan, dauTuan.plusWeeks(1));
            case "THANG":
                YearMonth thang = YearMonth.from(ngayDangChon);
                return new KhoangNgay(thang.atDay(1), thang.plusMonths(1).atDay(1));
            default:
                throw new IllegalArgumentException("Chế độ xem lịch không hợp lệ.");
        }
    }

    public List<NhanSuLichLamViec_entity> taiDanhSachNhanSu() throws SQLException {
        return dao.getDanhSachNhanSu();
    }

    public void xoa(String maLich) throws SQLException {
        if (!dao.xoaLich(maLich)) {
            throw new IllegalStateException("Không thể xóa lịch làm việc.");
        }
    }

    public void capNhatTrangThai(String maLich, String trangThai) throws SQLException {
        if (!dao.capNhatTrangThai(maLich, trangThai)) {
            throw new IllegalStateException("Không thể cập nhật trạng thái lịch làm việc.");
        }
    }

    public void capNhatTrangThaiTuDongQuaNgay() throws SQLException {
        dao.capNhatTrangThaiTuDongQuaNgay();
    }

    public void luu(NhanSuLichLamViec_entity nhanSu, LocalDate ngay, String gioBatDauText,
            String gioKetThucText, LichLamViec_entity lichHienTai) throws SQLException {
        if (nhanSu == null) throw new IllegalArgumentException("Vui lòng chọn nhân viên hoặc bác sĩ.");
        LocalTime gioBatDau;
        LocalTime gioKetThuc;
        try {
            gioBatDau = LocalTime.parse(gioBatDauText.trim(), TIME_FORMAT);
            gioKetThuc = LocalTime.parse(gioKetThucText.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Giờ phải đúng định dạng 24 giờ, ví dụ 08:30 hoặc 14:00.", e);
        }
        if (!gioKetThuc.isAfter(gioBatDau)) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
        boolean themMoi = lichHienTai == null;
        String maLich = themMoi ? dao.phatSinhMaLLV() : lichHienTai.getMaLLV();
        LichLamViec_entity lich = new LichLamViec_entity(maLich,
                nhanSu.getMaNhanSu(), nhanSu.getTenNhanSu(), nhanSu.getVaiTro(),
                ngay, gioBatDau, gioKetThuc,
                themMoi ? LichLamViec_entity.TRANG_THAI_CHUA_LAM : lichHienTai.getTrangThai(),
                nhanSu.getLoaiNhanSu());
        boolean daLuu = themMoi ? dao.themLich(lich) : dao.capNhatLich(lich);
        if (!daLuu) throw new IllegalStateException(themMoi
                ? "Không thể tạo lịch làm việc." : "Không thể cập nhật lịch làm việc.");
    }

    public boolean coTheThem(LocalDate ngay, LocalDate homNay) {
        return !ngay.isBefore(homNay);
    }

    public boolean coTheSua(LichLamViec_entity lich, LocalDateTime bayGio) {
        return lich != null && lich.coTheSuaLichTai(bayGio);
    }

    public boolean coTheCapNhatTrangThai(LichLamViec_entity lich, LocalDate ngayHienTai) {
        return lich != null && lich.coTheCapNhatTrangThaiTrongNgay(ngayHienTai);
    }

    public static final class DuLieuLich {
        public final List<LichLamViec_entity> danhSach;
        public final Map<LocalDate, Integer> soLichTheoNgay;
        public DuLieuLich(List<LichLamViec_entity> danhSach, Map<LocalDate, Integer> soLichTheoNgay) {
            this.danhSach = danhSach;
            this.soLichTheoNgay = soLichTheoNgay;
        }
    }

    public static final class KhoangNgay {
        public final LocalDate tuNgay;
        public final LocalDate denNgayExclusive;
        private KhoangNgay(LocalDate tuNgay, LocalDate denNgayExclusive) {
            this.tuNgay = tuNgay;
            this.denNgayExclusive = denNgayExclusive;
        }
    }
}
