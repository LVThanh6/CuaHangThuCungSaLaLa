package controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import connectDB.ConnectDB;
import dao.BacSi.BacSi_DAO;
import dao.DichVu.DichVu_DAO;
import dao.DichVu.KhuyenMai_DAO;
import dao.DichVu.Thue_DAO;
import dao.HoaDon.ChiTietHoaDon_DAO;
import dao.HoaDon.HoaDon_Dao;
import dao.KhachHang.KhachHang_DAO;
import dao.NhanVien.NhanVien_DAO;
import dao.TaiKhoan.DangNhap_DAO;
import dao.ThuCung.ChiTietDonThuoc_DAO;
import dao.ThuCung.DonThuoc_DAO;
import dao.ThuCung.ThuCung_DAO;
import dao.VatTu.VatTu_DAO;
import entity.BacSi.BacSi_entity;
import entity.DichVu.DichVu_entity;
import entity.DichVu.KhuyenMai_Entity;
import entity.DichVu.Thue_entity;
import entity.HoaDon.ChiTietHoaDon_entity;
import entity.HoaDon.HoaDon_entity;
import entity.KhachHang.KhachHang;
import entity.NhanVien.NhanVien_entity;
import entity.TaiKhoan.TaiKhoan;
import entity.ThuCung.ChiTietDonThuoc_entity;
import entity.ThuCung.DonThuoc_entity;
import entity.ThuCung.ThuCung_entity;
import entity.VatTu.VatTu_entity;

public class LapHoaDonController {
    private final KhachHang_DAO khachHangDao;
    private final ThuCung_DAO thuCungDao;
    private final DichVu_DAO dichVuDao;
    private final VatTu_DAO vatTuDao;
    private final HoaDon_Dao hoaDonDao;
    private final ChiTietHoaDon_DAO chiTietHoaDonDao;
    private final KhuyenMai_DAO khuyenMaiDao;
    private final Thue_DAO thueDao;
    private final NhanVien_DAO nhanVienDao;
    private final BacSi_DAO bacSiDao;
    private final DonThuoc_DAO donThuocDao;
    private final ChiTietDonThuoc_DAO chiTietDonThuocDao;

    public LapHoaDonController() {
        this(new KhachHang_DAO(), new ThuCung_DAO(), new DichVu_DAO(), new VatTu_DAO(),
                new HoaDon_Dao(), new ChiTietHoaDon_DAO(), new KhuyenMai_DAO(), new Thue_DAO(),
                new NhanVien_DAO(), new BacSi_DAO(), new DonThuoc_DAO(), new ChiTietDonThuoc_DAO());
    }

    LapHoaDonController(
            KhachHang_DAO khachHangDao,
            ThuCung_DAO thuCungDao,
            DichVu_DAO dichVuDao,
            VatTu_DAO vatTuDao,
            HoaDon_Dao hoaDonDao,
            ChiTietHoaDon_DAO chiTietHoaDonDao,
            KhuyenMai_DAO khuyenMaiDao,
            Thue_DAO thueDao,
            NhanVien_DAO nhanVienDao,
            BacSi_DAO bacSiDao,
            DonThuoc_DAO donThuocDao,
            ChiTietDonThuoc_DAO chiTietDonThuocDao) {
        this.khachHangDao = khachHangDao;
        this.thuCungDao = thuCungDao;
        this.dichVuDao = dichVuDao;
        this.vatTuDao = vatTuDao;
        this.hoaDonDao = hoaDonDao;
        this.chiTietHoaDonDao = chiTietHoaDonDao;
        this.khuyenMaiDao = khuyenMaiDao;
        this.thueDao = thueDao;
        this.nhanVienDao = nhanVienDao;
        this.bacSiDao = bacSiDao;
        this.donThuocDao = donThuocDao;
        this.chiTietDonThuocDao = chiTietDonThuocDao;
    }

    public ArrayList<KhachHang> taiKhachHang() {
        return khachHangDao.getAlltbKhachHang();
    }

    public ArrayList<ThuCung_entity> taiThuCung(String maKH) {
        return maKH == null ? new ArrayList<>() : thuCungDao.getThuCungTheoMaKH(maKH);
    }

    public ArrayList<DichVu_entity> taiDichVu() {
        return dichVuDao.getAllDichVu();
    }

    public ArrayList<VatTu_entity> taiVatTu() {
        return vatTuDao.getAllVatTu();
    }

    public ArrayList<KhuyenMai_Entity> taiKhuyenMaiHieuLuc(LocalDate ngay) {
        ArrayList<KhuyenMai_Entity> ketQua = new ArrayList<>();
        for (KhuyenMai_Entity khuyenMai : khuyenMaiDao.getAllKhuyenMai()) {
            boolean trongThoiHan = (khuyenMai.getNgayBatDau() == null
                    || !ngay.isBefore(khuyenMai.getNgayBatDau()))
                    && (khuyenMai.getNgayKetThuc() == null
                    || !ngay.isAfter(khuyenMai.getNgayKetThuc()));
            if (trongThoiHan) {
                ketQua.add(khuyenMai);
            }
        }
        return ketQua;
    }

    public ArrayList<Thue_entity> taiThue() {
        return thueDao.getAllThue();
    }

    public List<NhanVien_entity> taiNhanVienDichVuThucHien() throws SQLException {
        List<NhanVien_entity> nhanVienDichVu = new ArrayList<>();
        for (NhanVien_entity nhanVien : nhanVienDao.getAll()) {
            if ("Nhân viên dịch vụ".equals(nhanVien.getChucVu())) {
                nhanVienDichVu.add(nhanVien);
            }
        }
        return nhanVienDichVu;
    }

    public List<BacSi_entity> taiBacSiThucHien() throws SQLException {
        return bacSiDao.getAll();
    }

    public NhanVien_entity layNhanVienDangNhapHienTai() throws SQLException {
        String maDoiTuong = DangNhap_DAO.getMaDoiTuong();
        if (maDoiTuong != null && !maDoiTuong.isBlank()) {
            NhanVien_entity nhanVien = nhanVienDao.getNhanVienTheoMa(maDoiTuong);
            if (nhanVien != null) {
                return nhanVien;
            }
        }

        TaiKhoan taiKhoan = DangNhap_DAO.getTaiKhoanHienTai();
        ArrayList<NhanVien_entity> danhSach = nhanVienDao.getAll();
        if (taiKhoan != null && taiKhoan.getMaTK() != null) {
            for (NhanVien_entity nhanVien : danhSach) {
                if (taiKhoan.getMaTK().equals(nhanVien.getMaTK())) {
                    return nhanVien;
                }
            }
        }
        return danhSach.isEmpty() ? null : danhSach.get(0);
    }

    public int laySoHoaDonTiepTheo() {
        return hoaDonDao.laySoHoaDonLonNhat() + 1;
    }

    public boolean laSoLuongHopLe(int soLuong) {
        return soLuong > 0;
    }

    public ValidationError kiemTraThuCungBatBuoc(LoaiMatHang loai, boolean coThuCung) {
        if (coThuCung) return ValidationError.NONE;
        if (loai == LoaiMatHang.DICH_VU) return ValidationError.DICH_VU_CAN_THU_CUNG;
        if (loai == LoaiMatHang.THUOC) return ValidationError.THUOC_CAN_THU_CUNG;
        return ValidationError.NONE;
    }

    public ValidationError kiemTraTonKho(VatTu_entity vatTu, int soLuong) {
        return vatTu != null && vatTu.getSoLuongTon() < soLuong
                ? ValidationError.KHONG_DU_TON_KHO
                : ValidationError.NONE;
    }

    public List<PrescriptionOption> taiChiTietDonThuocKhaDung(String maThuCung, String maThuoc)
            throws SQLException {
        List<PrescriptionOption> khaDung = new ArrayList<>();
        if (maThuCung == null || maThuoc == null) {
            return khaDung;
        }
        for (DonThuoc_entity donThuoc : donThuocDao.getDonThuocTheoMaTC(maThuCung)) {
            for (ChiTietDonThuoc_entity chiTiet : chiTietDonThuocDao
                    .getChiTietTheoMaDon(donThuoc.getMaDonThuoc())) {
                if (!Objects.equals(maThuoc, chiTiet.getMaThuoc())) {
                    continue;
                }
                BigDecimal conLai = chiTiet.getSoLuong()
                        .subtract(chiTietHoaDonDao.getSoLuongThuocDaCap(chiTiet.getMaCTDT()));
                if (conLai.signum() > 0) {
                    khaDung.add(new PrescriptionOption(chiTiet, conLai));
                }
            }
        }
        return khaDung;
    }

    public BigDecimal laySoLuongConLaiDonThuoc(
            ChiTietDonThuoc_entity chiTiet,
            BigDecimal daThemTrongHoaDon) throws SQLException {
        return chiTiet.getSoLuong()
                .subtract(chiTietHoaDonDao.getSoLuongThuocDaCap(chiTiet.getMaCTDT()))
                .subtract(daThemTrongHoaDon);
    }

    public BigDecimal soLuongDaThem(List<InvoiceLine> lines, String maCTDT) {
        BigDecimal soLuong = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            if (Objects.equals(maCTDT, line.getMaCTDT())) {
                soLuong = soLuong.add(BigDecimal.valueOf(line.getSoLuong()));
            }
        }
        return soLuong;
    }

    public InvoiceLine taoDongDichVu(DichVu_entity dichVu, int soLuong) {
        return new InvoiceLine(dichVu.getMaDV(), dichVu.getTenDV(), LoaiMatHang.DICH_VU,
                soLuong, dichVu.getGiaDV(), null, null, null, null);
    }

    public InvoiceLine taoDongVatTu(VatTu_entity vatTu, ChiTietDonThuoc_entity chiTietDonThuoc,
            int soLuong) {
        LoaiMatHang loai = vatTu.getMaThuoc() == null ? LoaiMatHang.VAT_TU : LoaiMatHang.THUOC;
        return new InvoiceLine(vatTu.getMaVT(), vatTu.getTenVT(), loai, soLuong,
                vatTu.getGiaBanCoBan(), vatTu, chiTietDonThuoc, null, null);
    }

    public boolean laThuocKeDon(VatTu_entity vatTu) {
        return vatTu.getMaThuoc() != null;
    }

    public InvoiceLine ganNguoiThucHien(InvoiceLine line, NhanVien_entity nhanVien, BacSi_entity bacSi) {
        return new InvoiceLine(line.getMaHang(), line.getTenHang(), line.getLoai(),
                line.getSoLuong(), line.getDonGia(), line.getVatTu(), line.getChiTietDonThuoc(),
                nhanVien, bacSi);
    }

    public int timDongGopMatHang(List<InvoiceLine> lines, InvoiceLine candidate) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).cungMatHang(candidate)) {
                return i;
            }
        }
        return -1;
    }

    public double tinhThanhTienDong(InvoiceLine line) {
        return line.getDonGia() * line.getSoLuong();
    }

    public ValidationError kiemTraThemMatHang(
            InvoiceLine line,
            boolean coThuCung,
            List<InvoiceLine> lines) throws SQLException {
        if (line == null) {
            return ValidationError.CHUA_CHON_MAT_HANG;
        }
        if (line.getLoai() == LoaiMatHang.DICH_VU && !coThuCung) {
            return ValidationError.DICH_VU_CAN_THU_CUNG;
        }
        if (line.getLoai() == LoaiMatHang.THUOC && !coThuCung) {
            return ValidationError.THUOC_CAN_THU_CUNG;
        }
        if (line.getLoai() == LoaiMatHang.DICH_VU
                && line.getNhanVienThucHien() == null && line.getBacSiThucHien() == null) {
            return ValidationError.CHUA_CHON_NGUOI_THUC_HIEN;
        }
        ValidationError tonKho = kiemTraTonKho(line.getVatTu(), line.getSoLuong());
        if (tonKho != ValidationError.NONE) {
            return tonKho;
        }
        if (line.getLoai() == LoaiMatHang.THUOC) {
            BigDecimal daThem = soLuongDaThem(lines, line.getMaCTDT());
            BigDecimal conLai = laySoLuongConLaiDonThuoc(line.getChiTietDonThuoc(), daThem);
            if (BigDecimal.valueOf(line.getSoLuong()).compareTo(conLai) > 0) {
                return ValidationError.VUOT_SO_LUONG_DON_THUOC;
            }
        }
        return ValidationError.NONE;
    }

    public ValidationError kiemTraHoaDon(
            KhachHang khachHang,
            NhanVien_entity nhanVien,
            List<InvoiceLine> lines,
            boolean coThuCung) {
        if (nhanVien == null) return ValidationError.CHUA_CO_NHAN_VIEN;
        if (khachHang == null) return ValidationError.CHUA_CHON_KHACH_HANG;
        if (lines.isEmpty()) return ValidationError.HOA_DON_TRONG;
        if (!coThuCung && lines.stream().anyMatch(InvoiceLine::canThuCung)) {
            return ValidationError.HOA_DON_CAN_THU_CUNG;
        }
        return ValidationError.NONE;
    }

    public InvoiceTotals tinhTongTien(List<InvoiceLine> lines, KhuyenMai_Entity khuyenMai, Thue_entity thue) {
        double tienHang = 0;
        for (InvoiceLine line : lines) {
            tienHang += line.getDonGia() * line.getSoLuong();
        }
        double tienGiam = khuyenMai == null
                ? 0
                : Math.max(0, Math.min(tienHang * khuyenMai.getMucKM() / 100.0, tienHang));
        double tienSauGiam = tienHang - tienGiam;
        double tienThue = thue == null
                ? 0
                : Math.max(0, tienSauGiam * thue.getMucThue() / 100.0);
        return new InvoiceTotals(tienHang, tienGiam, tienThue, tienSauGiam + tienThue);
    }

    public InvoiceTotals luuHoaDon(InvoiceInput input) throws SQLException {
        ValidationError loi = kiemTraHoaDon(input.khachHang, input.nhanVien, input.lines,
                input.thuCung != null);
        if (loi != ValidationError.NONE) {
            throw new SQLException(loi.getMessage());
        }

        HoaDon_entity hoaDon = new HoaDon_entity(input.maHD, input.ngayLap, "Chờ thanh toán",
                input.khachHang, input.nhanVien, input.khuyenMai, input.thue, null);
        hoaDon.setMaTC(input.thuCung == null ? null : input.thuCung.getMaTC());

        Connection connection = ConnectDB.getCon();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu SALALA.");
        }

        boolean autoCommitCu = true;
        boolean daBatDauTransaction = false;
        SQLException loiChinh = null;
        try {
            autoCommitCu = connection.getAutoCommit();
            connection.setAutoCommit(false);
            daBatDauTransaction = true;
            if (!hoaDonDao.themHD(connection, hoaDon)) {
                throw new SQLException("Không thể lưu thông tin hóa đơn.");
            }

            ArrayList<DichVu_entity> dichVu = dichVuDao.getAllDichVu();
            for (int i = 0; i < input.lines.size(); i++) {
                InvoiceLine line = input.lines.get(i);
                DichVu_entity dichVuDuocChon = null;
                VatTu_entity vatTuDuocChon = null;
                if (line.getLoai() == LoaiMatHang.DICH_VU) {
                    for (DichVu_entity item : dichVu) {
                        if (line.getMaHang().equals(item.getMaDV())) {
                            dichVuDuocChon = item;
                            break;
                        }
                    }
                } else {
                    vatTuDuocChon = vatTuDao.getVatTuTheoMa(line.getMaHang());
                }
                if (dichVuDuocChon == null && vatTuDuocChon == null) {
                    throw new SQLException("Không tìm thấy mặt hàng " + line.getMaHang()
                            + " trong cơ sở dữ liệu.");
                }

                ChiTietHoaDon_entity chiTiet = new ChiTietHoaDon_entity(
                        i + 1, hoaDon, dichVuDuocChon, vatTuDuocChon,
                        line.getNhanVienThucHien(), line.getBacSiThucHien(),
                        line.getSoLuong(), line.getDonGia());
                chiTiet.setMaCTDT(line.getMaCTDT());
                if (!chiTietHoaDonDao.createChiTietHoaDon(connection, chiTiet)) {
                    throw new SQLException("Không thể lưu dòng chi tiết hóa đơn " + (i + 1) + ".");
                }
            }
            connection.commit();
        } catch (SQLException e) {
            loiChinh = e;
            if (daBatDauTransaction) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
        } finally {
            try {
                connection.setAutoCommit(autoCommitCu);
            } catch (SQLException restoreError) {
                if (loiChinh == null) loiChinh = restoreError;
                else loiChinh.addSuppressed(restoreError);
            }
            try {
                connection.close();
            } catch (SQLException closeError) {
                if (loiChinh == null) loiChinh = closeError;
                else loiChinh.addSuppressed(closeError);
            }
        }
        if (loiChinh != null) {
            throw loiChinh;
        }
        return tinhTongTien(input.lines, input.khuyenMai, input.thue);
    }

    public enum LoaiMatHang {
        DICH_VU, VAT_TU, THUOC
    }

    public static final class PrescriptionOption {
        private final ChiTietDonThuoc_entity chiTiet;
        private final BigDecimal soLuongConLai;

        private PrescriptionOption(ChiTietDonThuoc_entity chiTiet, BigDecimal soLuongConLai) {
            this.chiTiet = chiTiet;
            this.soLuongConLai = soLuongConLai;
        }

        public ChiTietDonThuoc_entity getChiTiet() { return chiTiet; }
        public BigDecimal getSoLuongConLai() { return soLuongConLai; }
    }

    public enum ValidationError {
        NONE(""),
        CHUA_CHON_MAT_HANG("Vui lòng chọn dịch vụ hoặc vật tư."),
        DICH_VU_CAN_THU_CUNG("Vui lòng chọn thú cưng trước khi thêm dịch vụ."),
        THUOC_CAN_THU_CUNG("Vui lòng chọn thú cưng trước khi thêm thuốc."),
        KHONG_DU_TON_KHO("Số lượng tồn kho không đủ."),
        VUOT_SO_LUONG_DON_THUOC("Số lượng bán vượt quá số thuốc còn lại trong đơn kê."),
        CHUA_CHON_NGUOI_THUC_HIEN("Vui lòng chọn người thực hiện dịch vụ."),
        CHUA_CO_NHAN_VIEN("Chưa có thông tin nhân viên đăng nhập để lập hóa đơn.\n"
                + "Vui lòng kiểm tra lại tài khoản hoặc CSDL."),
        CHUA_CHON_KHACH_HANG("Vui lòng chọn khách hàng."),
        HOA_DON_TRONG("Hóa đơn phải có ít nhất một dòng chi tiết."),
        HOA_DON_CAN_THU_CUNG("Hóa đơn có dịch vụ hoặc thuốc kê đơn phải gắn với thú cưng.");

        private final String message;

        ValidationError(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }

    public static final class InvoiceLine {
        private final String maHang;
        private final String tenHang;
        private final LoaiMatHang loai;
        private final int soLuong;
        private final double donGia;
        private final VatTu_entity vatTu;
        private final ChiTietDonThuoc_entity chiTietDonThuoc;
        private final NhanVien_entity nhanVienThucHien;
        private final BacSi_entity bacSiThucHien;

        public InvoiceLine(String maHang, String tenHang, LoaiMatHang loai, int soLuong, double donGia,
                VatTu_entity vatTu, ChiTietDonThuoc_entity chiTietDonThuoc,
                NhanVien_entity nhanVienThucHien, BacSi_entity bacSiThucHien) {
            this.maHang = maHang;
            this.tenHang = tenHang;
            this.loai = loai;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.vatTu = vatTu;
            this.chiTietDonThuoc = chiTietDonThuoc;
            this.nhanVienThucHien = nhanVienThucHien;
            this.bacSiThucHien = bacSiThucHien;
        }

        public String getMaHang() { return maHang; }
        public String getTenHang() { return tenHang; }
        public LoaiMatHang getLoai() { return loai; }
        public int getSoLuong() { return soLuong; }
        public double getDonGia() { return donGia; }
        public VatTu_entity getVatTu() { return vatTu; }
        public ChiTietDonThuoc_entity getChiTietDonThuoc() { return chiTietDonThuoc; }
        public String getMaCTDT() {
            return chiTietDonThuoc == null ? null : chiTietDonThuoc.getMaCTDT();
        }
        public NhanVien_entity getNhanVienThucHien() { return nhanVienThucHien; }
        public BacSi_entity getBacSiThucHien() { return bacSiThucHien; }
        public boolean canThuCung() {
            return loai == LoaiMatHang.DICH_VU || loai == LoaiMatHang.THUOC;
        }
        public InvoiceLine voiSoLuong(int soLuongMoi) {
            return new InvoiceLine(maHang, tenHang, loai, soLuongMoi, donGia, vatTu,
                    chiTietDonThuoc, nhanVienThucHien, bacSiThucHien);
        }
        public boolean cungMatHang(InvoiceLine other) {
            return maHang.equals(other.maHang) && loai == other.loai
                    && Objects.equals(getMaCTDT(), other.getMaCTDT())
                    && Objects.equals(nhanVienThucHien, other.nhanVienThucHien)
                    && Objects.equals(bacSiThucHien, other.bacSiThucHien);
        }
    }

    public static final class InvoiceInput {
        private final String maHD;
        private final LocalDate ngayLap;
        private final KhachHang khachHang;
        private final ThuCung_entity thuCung;
        private final NhanVien_entity nhanVien;
        private final KhuyenMai_Entity khuyenMai;
        private final Thue_entity thue;
        private final List<InvoiceLine> lines;

        public InvoiceInput(String maHD, LocalDate ngayLap, KhachHang khachHang, ThuCung_entity thuCung,
                NhanVien_entity nhanVien, KhuyenMai_Entity khuyenMai, Thue_entity thue, List<InvoiceLine> lines) {
            this.maHD = maHD;
            this.ngayLap = ngayLap;
            this.khachHang = khachHang;
            this.thuCung = thuCung;
            this.nhanVien = nhanVien;
            this.khuyenMai = khuyenMai;
            this.thue = thue;
            this.lines = new ArrayList<>(lines);
        }
    }

    public static final class InvoiceTotals {
        private final double tienHang;
        private final double tienGiam;
        private final double tienThue;
        private final double tongThanhToan;

        private InvoiceTotals(double tienHang, double tienGiam, double tienThue, double tongThanhToan) {
            this.tienHang = tienHang;
            this.tienGiam = tienGiam;
            this.tienThue = tienThue;
            this.tongThanhToan = tongThanhToan;
        }

        public double getTienHang() { return tienHang; }
        public double getTienGiam() { return tienGiam; }
        public double getTienThue() { return tienThue; }
        public double getTongThanhToan() { return tongThanhToan; }
    }
}
