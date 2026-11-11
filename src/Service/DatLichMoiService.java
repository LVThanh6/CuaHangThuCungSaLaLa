package Service;

import dao.DichVu_DAO;
import dao.KhachHang_DAO;
import dao.LichHen_DAO;
import dao.NhanVien_DAO;
import dao.HoSoSucKhoe_DAO;
import dao.ThuCung_DAO;
import entity.DichVu_entity;
import entity.KhachHang;
import entity.LichHen_entity;
import entity.NhanVien_entity;
import entity.HoSoSucKhoe_entity;
import entity.ThuCung_entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatLichMoiService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final LichHen_DAO lichHenDao;
    private final KhachHang_DAO khachHangDao;
    private final ThuCung_DAO thuCungDao;
    private final NhanVien_DAO nhanVienDao;
    private final DichVu_DAO dichVuDao;
    private final HoSoSucKhoe_DAO hoSoDao;

    public DatLichMoiService() {
        this(new LichHen_DAO(), new KhachHang_DAO(), new ThuCung_DAO(),
                new NhanVien_DAO(), new DichVu_DAO(), new HoSoSucKhoe_DAO());
    }

    DatLichMoiService(LichHen_DAO lichHenDao, KhachHang_DAO khachHangDao,
            ThuCung_DAO thuCungDao, NhanVien_DAO nhanVienDao, DichVu_DAO dichVuDao,
            HoSoSucKhoe_DAO hoSoDao) {
        this.lichHenDao = lichHenDao;
        this.khachHangDao = khachHangDao;
        this.thuCungDao = thuCungDao;
        this.nhanVienDao = nhanVienDao;
        this.dichVuDao = dichVuDao;
        this.hoSoDao = hoSoDao;
    }

    //Nạp danh sách Nhân viên và Dịch vụ (tự động loại bỏ các dịch vụ đang bị khóa/ngưng hoạt động).
    public DuLieuTuyChon taiDuLieuTuyChon() throws SQLException {
        List<NhanVien_entity> nhanVien = nhanVienDao.getAll();
        List<DichVu_entity> dichVu = new ArrayList<>();
        for (DichVu_entity item : dichVuDao.getAllDichVu()) {
            if (item != null && item.getTenDV() != null && !laDichVuNgungHoatDong(item.getTrangThai())) {
                dichVu.add(item);
            }
        }
        return new DuLieuTuyChon(nhanVien, dichVu);
    }

    //Nhận số điện thoại nhập vào, lọc bỏ ký tự thừa, tìm và trả về đúng thông tin chủ nuôi.
    public KhachHang timKhachHangTheoSoDienThoai(String soDienThoai) {
        String sdtSo = soDienThoai == null ? "" : soDienThoai.trim().replaceAll("[^0-9]", "");
        if (sdtSo.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập số điện thoại khách hàng.");
        for (KhachHang khachHang : khachHangDao.timKiemTongHop(sdtSo)) {
            if (khachHang.getSdt() != null
                    && khachHang.getSdt().replaceAll("[^0-9]", "").equals(sdtSo)) {
                return khachHang;
            }
        }
        return null;
    }

    //Lấy toàn bộ thú cưng của khách hàng vừa tìm thấy. Đặc biệt: Tự động dò tìm cân nặng ở lần khám gần nhất của từng bé thú cưng để điền sẵn cho bác sĩ.
    public DuLieuThuCung taiThuCung(String maKhachHang) throws SQLException {
        List<ThuCung_entity> thuCung = thuCungDao.getThuCungTheoMaKH(maKhachHang);
        Map<String, CanNangGanNhat> canNang = new HashMap<>();
        for (ThuCung_entity item : thuCung) {
            for (HoSoSucKhoe_entity hoSo : hoSoDao.getHoSoSucKhoeTheoMaTC(item.getMaTC())) {
                if (hoSo.getCanNang() == null || hoSo.getNgayKham() == null || hoSo.getMaTC() == null) {
                    continue;
                }
                String maThuCung = hoSo.getMaTC().getMaTC();
                CanNangGanNhat truocDo = canNang.get(maThuCung);
                if (truocDo == null || hoSo.getNgayKham().isAfter(truocDo.ngayKham)) {
                    canNang.put(maThuCung, new CanNangGanNhat(
                            hoSo.getNgayKham(), hoSo.getCanNang().stripTrailingZeros().toPlainString()));
                }
            }
        }
        return new DuLieuThuCung(thuCung, canNang);
    }

    //Tạo mã lịch hẹn mới, đảm bảo không trùng lặp với các mã đã tồn tại trong cơ sở dữ liệu.
    public String maLichHenMoi() {
        return lichHenDao.phatSinhMaLichHenMoi();
    }

    //Kiểm tra toàn bộ điều kiện (Phải có khách, thú cưng, bác sĩ, ngày giờ phải ở tương lai, ghi chú không quá 500 ký tự) rồi tạo lịch với trạng thái mặc định "Chờ khám".
    public String datLich(ThongTinDatLich thongTin) {
        if (thongTin.khachHang == null) {
            throw new IllegalArgumentException("Vui lòng tìm và xác nhận khách hàng trước khi đặt lịch.");
        }
        if (thongTin.thuCung == null || thongTin.nhanVien == null
                || (thongTin.dichVu == null && (thongTin.noiDung == null
                    || thongTin.noiDung.startsWith("--")))
                || thongTin.gioKham == null) {
            throw new IllegalArgumentException("Vui lòng chọn thú cưng, nhân viên, nội dung khám và giờ khám.");
        }
        String lyDo = thongTin.dichVu == null ? thongTin.noiDung : thongTin.dichVu.getTenDV();
        if (lyDo.length() > 255) {
            throw new IllegalArgumentException("Nội dung khám không được vượt quá 255 ký tự.");
        }
        LocalDate ngay;
        try {
            ngay = LocalDate.parse(thongTin.ngayKham.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày khám không đúng định dạng dd/MM/yyyy.", e);
        }
        LocalDateTime ngayGio;
        try {
            ngayGio = LocalDateTime.of(ngay, LocalTime.parse(thongTin.gioKham));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Giờ khám không hợp lệ.", e);
        }
        if (ngayGio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải ở hiện tại hoặc trong tương lai.");
        }
        String ghiChu = thongTin.ghiChu == null ? "" : thongTin.ghiChu.trim();
        if (ghiChu.length() > 500) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự.");
        }
        String maLichHen = lichHenDao.phatSinhMaLichHenMoi();
        LichHen_entity lichHen = new LichHen_entity(maLichHen, thongTin.thuCung, thongTin.khachHang,
                thongTin.nhanVien, thongTin.dichVu, ngayGio, lyDo,
                "Chờ khám", ghiChu, "Khám bệnh");
        if (!lichHenDao.createLichHen(lichHen)) {
            throw new IllegalStateException(
                    "Không thể lưu lịch hẹn. Vui lòng kiểm tra dữ liệu và kết nối cơ sở dữ liệu.");
        }
        return maLichHen;
    }

    //Kiểm tra trạng thái của dịch vụ xem có chứa chữ "ngưng", "ngừng" để ẩn đi.
    private boolean laDichVuNgungHoatDong(String trangThai) {
        if (trangThai == null) return false;
        String normalized = trangThai.trim().toLowerCase();
        return normalized.contains("ngưng") || normalized.contains("ngừng")
                || normalized.contains("không hoạt động");
    }

    public static final class DuLieuTuyChon {
        public final List<NhanVien_entity> nhanVien;
        public final List<DichVu_entity> dichVu;
        public DuLieuTuyChon(List<NhanVien_entity> nhanVien, List<DichVu_entity> dichVu) {
            this.nhanVien = nhanVien;
            this.dichVu = dichVu;
        }
    }

    public static final class DuLieuThuCung {
        public final List<ThuCung_entity> thuCung;
        public final Map<String, CanNangGanNhat> canNangGanNhat;
        public DuLieuThuCung(List<ThuCung_entity> thuCung,
                Map<String, CanNangGanNhat> canNangGanNhat) {
            this.thuCung = thuCung;
            this.canNangGanNhat = canNangGanNhat;
        }
    }

    public static final class CanNangGanNhat {
        public final LocalDateTime ngayKham;
        public final String canNang;
        public CanNangGanNhat(LocalDateTime ngayKham, String canNang) {
            this.ngayKham = ngayKham;
            this.canNang = canNang;
        }
    }

    public static final class ThongTinDatLich {
        public final KhachHang khachHang;
        public final ThuCung_entity thuCung;
        public final NhanVien_entity nhanVien;
        public final DichVu_entity dichVu;
        public final String noiDung;
        public final String ngayKham;
        public final String gioKham;
        public final String ghiChu;
        public ThongTinDatLich(KhachHang khachHang, ThuCung_entity thuCung,
                NhanVien_entity nhanVien, DichVu_entity dichVu, String noiDung,
                String ngayKham, String gioKham, String ghiChu) {
            this.khachHang = khachHang;
            this.thuCung = thuCung;
            this.nhanVien = nhanVien;
            this.dichVu = dichVu;
            this.noiDung = noiDung;
            this.ngayKham = ngayKham;
            this.gioKham = gioKham;
            this.ghiChu = ghiChu;
        }
    }
}
