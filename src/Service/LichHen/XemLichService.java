package Service.LichHen;

import dao.DichVu.DichVu_DAO;
import dao.LichHen.LichHen_DAO;
import dao.NhanVien.NhanVien_DAO;
import entity.DichVu.DichVu_entity;
import entity.LichHen.LichHen_entity;
import entity.NhanVien.NhanVien_entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class XemLichService {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final LichHen_DAO lichHenDao;
    private final NhanVien_DAO nhanVienDao;
    private final DichVu_DAO dichVuDao;

    public XemLichService() {
        this(new LichHen_DAO(), new NhanVien_DAO(), new DichVu_DAO());
    }

    XemLichService(LichHen_DAO lichHenDao, NhanVien_DAO nhanVienDao, DichVu_DAO dichVuDao) {
        this.lichHenDao = lichHenDao;
        this.nhanVienDao = nhanVienDao;
        this.dichVuDao = dichVuDao;
    }

    //taiDuLieu() / taiNhanVien() / taiDichVu()
    //Lấy dữ liệu Lịch hẹn, nhân viên, dịch vụ để đổ ra bảng và combobox lọc.
    public DuLieuManHinh taiDuLieu() throws SQLException {
        return new DuLieuManHinh(lichHenDao.getAllLichHen(), nhanVienDao.getAll());
    }

    public List<NhanVien_entity> taiNhanVien() throws SQLException {
        return nhanVienDao.getAll();
    }

    public List<DichVu_entity> taiDichVu() {
        return dichVuDao.getAllDichVu();
    }
    
    //Tìm kiếm lịch theo mã
    public LichHen_entity timTheoMa(String maLichHen) {
        return lichHenDao.getLichHenTheoMa(maLichHen);
    }

    //Lọc danh sách lịch hẹn theo nhân viên, trạng thái và khoảng ngày.
    public List<LichHen_entity> loc(List<LichHen_entity> danhSach, String nhanVienChon,
            String trangThaiChon, LocalDate tuNgay, LocalDate denNgay) {
        List<LichHen_entity> ketQua = new ArrayList<>();
        for (LichHen_entity lich : danhSach) {
            if (lich == null) continue;
            String maNhanVien = lich.getNhanVien() == null ? "" : lich.getNhanVien().getMaNhanVien();
            String trangThai = lich.getTrangThai() == null ? "" : lich.getTrangThai();
            LocalDate ngay = lich.getNgayGio() == null ? null : lich.getNgayGio().toLocalDate();
            boolean locNhanVien = nhanVienChon == null || nhanVienChon.startsWith("Nhân viên:")
                    || nhanVienChon.endsWith("(" + maNhanVien + ")");
            boolean locTrangThai = trangThaiChon == null || trangThaiChon.startsWith("Trạng thái:")
                    || trangThaiChon.equals(trangThai);
            boolean locNgay = (tuNgay == null || (ngay != null && !ngay.isBefore(tuNgay)))
                    && (denNgay == null || (ngay != null && !ngay.isAfter(denNgay)));
            if (locNhanVien && locTrangThai && locNgay) ketQua.add(lich);
        }
        return ketQua;
    }

    //Thống kê số lượng lịch hẹn trong ngày hôm nay, phân loại theo trạng thái (Tổng, Chờ khám, Đang khám, Đã khám, Đã hủy)
    public ThongKe thongKeHomNay(List<LichHen_entity> danhSach, LocalDate homNay) {
        int tong = 0;
        int choKham = 0;
        int dangKham = 0;
        int daKham = 0;
        int daHuy = 0;
        for (LichHen_entity lich : danhSach) {
            if (lich.getNgayGio() == null || !lich.getNgayGio().toLocalDate().equals(homNay)) continue;
            tong++;
            if ("Chờ khám".equalsIgnoreCase(lich.getTrangThai())) choKham++;
            else if ("Đang khám".equalsIgnoreCase(lich.getTrangThai())) dangKham++;
            else if ("Đã khám".equalsIgnoreCase(lich.getTrangThai())) daKham++;
            else if ("Đã hủy".equalsIgnoreCase(lich.getTrangThai())) daHuy++;
        }
        return new ThongKe(tong, choKham, dangKham, daKham, daHuy);
    }

    //Xuất danh sách lịch hẹn ra file CSV, bao gồm tiêu đề và dữ liệu, xử lý mã hóa UTF-8 và dấu phẩy trong dữ liệu.
    public void xuatDanhSach(File file, List<LichHen_entity> danhSach) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            ghiDong(writer, "Thời gian", "Tên thú cưng", "Chủ nuôi",
                    "Nhân viên phụ trách", "Nội dung khám", "Trạng thái");
            for (LichHen_entity lich : danhSach) {
                ghiDong(writer,
                        lich.getNgayGio() == null ? "" : DATE_TIME_FORMAT.format(lich.getNgayGio()),
                        lich.getThuCung() == null ? "" : lich.getThuCung().getTenTC(),
                        lich.getKhachHang() == null ? "" : lich.getKhachHang().getHoTen(),
                        lich.getNhanVien() == null ? "" : lich.getNhanVien().getHoTen(),
                        String.valueOf(lich.getLyDo()),
                        lich.getTrangThai() == null ? "" : lich.getTrangThai());
            }
        }
    }

    //Ghi một dòng vào file CSV, tự động thêm dấu ngoặc kép và xử lý dấu phẩy trong dữ liệu.
    private void ghiDong(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(',');
            writer.write("\"" + values[i].replace("\"", "\"\"") + "\"");
        }
        writer.newLine();
    }

    //Xử lý chuỗi văn bản thành đối tượng ngày tháng để lọc khoảng ngày.
    public KhoangNgay khoangNgay(String tuNgayText, String denNgayText) {
        LocalDate tuNgay = parseNgay(tuNgayText);
        LocalDate denNgay = parseNgay(denNgayText);
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }
        return new KhoangNgay(tuNgay, denNgay);
    }

    private LocalDate parseNgay(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy.", e);
        }
    }

    //Cập nhật thông tin lịch hẹn, bao gồm ngày giờ, nhân viên, lý do, loại hẹn, dịch vụ, trạng thái và ghi chú. Kiểm tra điều kiện: ngày giờ phải ở tương lai, nhân viên, lý do và loại hẹn là bắt buộc.
    public boolean capNhat(LichHen_entity lich, ThongTinCapNhat thongTin) {
        LocalDateTime ngayGio;
        try {
            ngayGio = LocalDateTime.parse(thongTin.ngayGio.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Thời gian không hợp lệ (dd/MM/yyyy HH:mm).", e);
        }
        String lyDo = thongTin.lyDo.trim();
        String loaiHen = thongTin.loaiHen.trim();
        if (ngayGio.isBefore(LocalDateTime.now()) || thongTin.nhanVien == null
                || lyDo.isEmpty() || loaiHen.isEmpty()) {
            throw new IllegalArgumentException(
                    "Thời gian phải ở tương lai; nhân viên, nội dung và loại hẹn là bắt buộc.");
        }
        lich.setNgayGio(ngayGio);
        lich.setNhanVien(thongTin.nhanVien);
        lich.setLyDo(lyDo);
        lich.setLoaiHen(loaiHen);
        lich.setDichVu(thongTin.dichVu);
        lich.setTrangThai(thongTin.trangThai);
        lich.setGhiChu(thongTin.ghiChu.trim());
        return lichHenDao.updateLichHen(lich);
    }

    //Hủy lịch hẹn theo mã, kiểm tra điều kiện: lịch phải tồn tại và chưa bị hủy trước đó. Cập nhật trạng thái thành "Đã hủy" nếu hợp lệ.
    public KetQuaHuyLich huy(String maLichHen) {
        LichHen_entity lich = lichHenDao.getLichHenTheoMa(maLichHen);
        if (lich == null) return KetQuaHuyLich.KHONG_TIM_THAY;
        if ("Đã hủy".equalsIgnoreCase(lich.getTrangThai())) return KetQuaHuyLich.DA_HUY;
        lich.setTrangThai("Đã hủy");
        return lichHenDao.updateLichHen(lich) ? KetQuaHuyLich.THANH_CONG : KetQuaHuyLich.THAT_BAI;
    }

    public enum KetQuaHuyLich {
        KHONG_TIM_THAY, DA_HUY, THANH_CONG, THAT_BAI
    }

    public static final class DuLieuManHinh {
        public final List<LichHen_entity> lichHen;
        public final List<NhanVien_entity> nhanVien;
        public DuLieuManHinh(List<LichHen_entity> lichHen, List<NhanVien_entity> nhanVien) {
            this.lichHen = lichHen;
            this.nhanVien = nhanVien;
        }
    }

    public static final class ThongTinCapNhat {
        public final String ngayGio;
        public final NhanVien_entity nhanVien;
        public final String lyDo;
        public final String loaiHen;
        public final DichVu_entity dichVu;
        public final String trangThai;
        public final String ghiChu;
        public ThongTinCapNhat(String ngayGio, NhanVien_entity nhanVien, String lyDo, String loaiHen,
                DichVu_entity dichVu, String trangThai, String ghiChu) {
            this.ngayGio = ngayGio;
            this.nhanVien = nhanVien;
            this.lyDo = lyDo;
            this.loaiHen = loaiHen;
            this.dichVu = dichVu;
            this.trangThai = trangThai;
            this.ghiChu = ghiChu;
        }
    }

    public static final class ThongKe {
        public final int tong;
        public final int choKham;
        public final int dangKham;
        public final int daKham;
        public final int daHuy;
        public ThongKe(int tong, int choKham, int dangKham, int daKham, int daHuy) {
            this.tong = tong;
            this.choKham = choKham;
            this.dangKham = dangKham;
            this.daKham = daKham;
            this.daHuy = daHuy;
        }
    }

    public static final class KhoangNgay {
        public final LocalDate tuNgay;
        public final LocalDate denNgay;
        private KhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
        }
    }
}
