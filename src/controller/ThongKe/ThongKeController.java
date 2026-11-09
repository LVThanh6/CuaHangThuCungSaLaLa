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
    // 1. CÁC ENUM VÀ CLASS ĐỊNH NGHĨA DỮ LIỆU
    // ==========================================================

    public enum DanhMuc {
        DOANH_THU("Doanh thu"),
        BAN_HANG("Bán hàng"),
        NHAN_SU("Nhân sự");

        private final String ten;
        DanhMuc(String ten) { this.ten = ten; }
        @Override public String toString() { return ten; }
    }

    public enum KhoangThoiGianNhanh {
        HOM_NAY, BAY_NGAY_QUA, BA_MUOI_NGAY_QUA, THANG_NAY, NAM_NAY
    }

    public static class KhoangNgay {
        private final LocalDate batDau;
        private final LocalDate ketThuc;
        public KhoangNgay(LocalDate batDau, LocalDate ketThuc) { this.batDau = batDau; this.ketThuc = ketThuc; }
        public LocalDate getBatDau() { return batDau; }
        public LocalDate getKetThuc() { return ketThuc; }
    }

    public static class ChiSoBaoCao {
        private String ma, ten, donVi, hauTo, moTa;
        private double tong;
        private List<String> nhan, chuoi;
        private List<List<Double>> giaTri;

        public ChiSoBaoCao(String ma, String ten, String donVi, String hauTo, String moTa) {
            this.ma = ma;
            this.ten = ten;
            this.donVi = donVi;
            this.hauTo = hauTo;
            this.moTa = moTa;
        }

        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDonVi() { return donVi; }
        public String getHauTo() { return hauTo; }
        public String getMoTa() { return moTa; }
        public double getTong() { return tong; }
        public List<String> getNhan() { return nhan; }
        public List<String> getChuoi() { return chuoi; }
        public List<List<Double>> getGiaTri() { return giaTri; }

        public void setTong(double tong) { this.tong = tong; }
        public void setNhan(List<String> nhan) { this.nhan = nhan; }
        public void setChuoi(List<String> chuoi) { this.chuoi = chuoi; }
        public void setGiaTri(List<List<Double>> giaTri) { this.giaTri = giaTri; }
    }

    public static class DuLieuBaoCao {
        private List<ChiSoBaoCao> danhSachChiSo = new ArrayList<>();
        private double tongDoanhThu, soHoaDon, soLuongBan, doanhThuHangHoa, doanhThuDichVu, tongLuong;

        public List<ChiSoBaoCao> getDanhSachChiSo() { return danhSachChiSo; }
        public void setDanhSachChiSo(List<ChiSoBaoCao> danhSachChiSo) { this.danhSachChiSo = danhSachChiSo; }

        public double getTongDoanhThu() { return tongDoanhThu; }
        public void setTongDoanhThu(double tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

        public double getSoHoaDon() { return soHoaDon; }
        public void setSoHoaDon(double soHoaDon) { this.soHoaDon = soHoaDon; }

        public double getSoLuongBan() { return soLuongBan; }
        public void setSoLuongBan(double soLuongBan) { this.soLuongBan = soLuongBan; }

        public double getDoanhThuHangHoa() { return doanhThuHangHoa; }
        public void setDoanhThuHangHoa(double doanhThuHangHoa) { this.doanhThuHangHoa = doanhThuHangHoa; }

        public double getDoanhThuDichVu() { return doanhThuDichVu; }
        public void setDoanhThuDichVu(double doanhThuDichVu) { this.doanhThuDichVu = doanhThuDichVu; }

        public double getTongLuong() { return tongLuong; }
        public void setTongLuong(double tongLuong) { this.tongLuong = tongLuong; }
    }

    public static class KetQuaTaiDuLieu {
        private final boolean thanhCong;
        private final String thongBaoLoi;
        private final DuLieuBaoCao baoCao;

        public KetQuaTaiDuLieu(boolean thanhCong, String thongBaoLoi, DuLieuBaoCao baoCao) {
            this.thanhCong = thanhCong;
            this.thongBaoLoi = thongBaoLoi;
            this.baoCao = baoCao;
        }
        public boolean isThanhCong() { return thanhCong; }
        public String getThongBaoLoi() { return thongBaoLoi; }
        public DuLieuBaoCao getBaoCao() { return baoCao; }
    }

    // ==========================================================
    // 2. CÁC HÀM XỬ LÝ LOGIC ĐIỀU KIỆN
    // ==========================================================

    public static KhoangNgay layKhoangNhanh(KhoangThoiGianNhanh khoang, LocalDate hienTai) {
        switch (khoang) {
            case HOM_NAY: return new KhoangNgay(hienTai, hienTai);
            case BAY_NGAY_QUA: return new KhoangNgay(hienTai.minusDays(6), hienTai);
            case BA_MUOI_NGAY_QUA: return new KhoangNgay(hienTai.minusDays(29), hienTai);
            case THANG_NAY: return new KhoangNgay(hienTai.withDayOfMonth(1), hienTai);
            case NAM_NAY: return new KhoangNgay(hienTai.with(TemporalAdjusters.firstDayOfYear()), hienTai);
            default: return new KhoangNgay(hienTai, hienTai);
        }
    }

    public static String loiKhoangNgay(LocalDate tu, LocalDate den) {
        if (tu.isAfter(den)) return "Ngày bắt đầu không được lớn hơn ngày kết thúc.";
        return null;
    }

    public static String moTaNhomThoiGian(LocalDate tu, LocalDate den) {
        return ThongKe_DAO.moTaNhomThoiGian(tu, den);
    }

    public static boolean laChuoiThoiGian(String ma) {
        return ma.equals(ThongKe_DAO.MA_DOANH_THU_TONG) ||
               ma.equals(ThongKe_DAO.MA_DOANH_THU_HANG_HOA) ||
               ma.equals(ThongKe_DAO.MA_DOANH_THU_DICH_VU) ||
               ma.equals(ThongKe_DAO.MA_SO_HOA_DON) ||
               ma.equals(ThongKe_DAO.MA_SO_LUONG_BAN);
    }

    public static DanhMuc layDanhMucCho(String ma) {
        return layDanhMucCho(ma, null);
    }

    public static DanhMuc layDanhMucCho(String ma, String ten) {
        String tenKhongDau = boDau(ten);
        if (tenKhongDau.contains("chi tiet luong")) return DanhMuc.NHAN_SU;
        if (tenKhongDau.contains("tien luong") && tenKhongDau.contains("du tinh")) return null;

        if (ma.startsWith("doanh-thu")) return DanhMuc.DOANH_THU;
        if (ma.equals(ThongKe_DAO.MA_SO_LUONG_BAN) || ma.contains("so-luong")) return DanhMuc.BAN_HANG;
        if (ma.contains("luong")) {
            return ma.contains("chi-tiet") ? DanhMuc.NHAN_SU : null;
        }
        return DanhMuc.BAN_HANG; 
    }

    private static String boDau(String chuoi) {
        if (chuoi == null) return "";
        String chuoiKhongDau = java.text.Normalizer.normalize(chuoi, java.text.Normalizer.Form.NFD);
        return chuoiKhongDau.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                  .replace('đ', 'd').replace('Đ', 'D')
                  .toLowerCase(Locale.ROOT).trim();
    }

    // ----------------------------------------------------------
    // Tìm kiếm nhân viên
    // ----------------------------------------------------------
    private volatile List<NhanVien_entity> dsNhanVienTraCuu = new ArrayList<>();

    private void napDanhSachNhanVien() {
        try {
            dsNhanVienTraCuu = new NhanVien_DAO().getAllNhanVien();
        } catch (Exception loi) {
            loi.printStackTrace();
            dsNhanVienTraCuu = new ArrayList<>();
        }
    }

    public boolean khopTuKhoaNhanVien(String tuKhoa, String nhanDong) {
        String tuKhoaKhongDau = boDau(tuKhoa);
        if (tuKhoaKhongDau.isEmpty()) return true;
        String nhanKhongDau = boDau(nhanDong);
        if (nhanKhongDau.contains(tuKhoaKhongDau)) return true;

        for (NhanVien_entity nv : dsNhanVienTraCuu) {
            String ma = boDau(nv.getMaNhanVien());
            String ten = boDau(nv.getHoTen());
            boolean dongThuocNv = (!ma.isEmpty() && nhanKhongDau.contains(ma)) || (!ten.isEmpty() && nhanKhongDau.contains(ten));
            if (!dongThuocNv) continue;
            String sdt = nv.getSdt() == null ? "" : nv.getSdt().replaceAll("\\s+", "");
            if (ma.contains(tuKhoaKhongDau) || ten.contains(tuKhoaKhongDau) || sdt.contains(tuKhoaKhongDau.replaceAll("\\s+", ""))) return true;
        }
        return false;
    }

    // ==========================================================
    // 3. HÀM TẢI DỮ LIỆU TỪ DAO
    // ==========================================================

    public KetQuaTaiDuLieu taiDuLieu(LocalDate tu, LocalDate den) {
        try {
            napDanhSachNhanVien();
            ThongKe_DAO dao = new ThongKe_DAO();
            DuLieuThongKe duLieuDao = dao.layDuLieuThongKe(tu, den);

            DuLieuBaoCao baoCao = new DuLieuBaoCao();
            baoCao.setTongDoanhThu(duLieuDao.doanhThuTong);
            baoCao.setDoanhThuHangHoa(duLieuDao.doanhThuHangHoa);
            baoCao.setDoanhThuDichVu(duLieuDao.doanhThuDichVu);
            baoCao.setSoHoaDon(duLieuDao.soHoaDon);
            baoCao.setSoLuongBan(duLieuDao.soLuongBan);
            baoCao.setTongLuong(duLieuDao.tongLuong);

            List<ChiSoBaoCao> danhSachChiSo = new ArrayList<>();
            for (ChiSoThongKe chiSoDao : duLieuDao.chiSo) {
                ChiSoBaoCao chiSoBaoCao = new ChiSoBaoCao(
                    chiSoDao.ma, 
                    chiSoDao.ten, 
                    chiSoDao.donVi, 
                    chiSoDao.hauTo, 
                    chiSoDao.ghiChu
                );
                chiSoBaoCao.setTong(chiSoDao.tong);
                chiSoBaoCao.setNhan(new ArrayList<>(chiSoDao.nhan));
                chiSoBaoCao.setChuoi(new ArrayList<>(chiSoDao.chuoi));
                chiSoBaoCao.setGiaTri(new ArrayList<>(chiSoDao.giaTri));
                
                danhSachChiSo.add(chiSoBaoCao);
            }
            baoCao.setDanhSachChiSo(danhSachChiSo);

            return new KetQuaTaiDuLieu(true, null, baoCao);
            
        } catch (Exception loi) {
            loi.printStackTrace();
            return new KetQuaTaiDuLieu(false, "Lỗi truy xuất dữ liệu: " + loi.getMessage(), null);
        }
    }

    // ==========================================================
    // 4. LOGIC XUẤT FILE PDF TRỰC TIẾP
    // ==========================================================

    public boolean xuatBaoCaoPDF(File tapTin, String kyBaoCao, String nguoiLap,
                                 String tongDoanhThu, String tongHoaDon,
                                 String tongKhachHang, String tongThuCung,
                                 List<Object[]> topDichVu, List<Object[]> doanhSoNV,
                                 BufferedImage bieuDoImg) {
        try (FileOutputStream luongGhiFile = new FileOutputStream(tapTin)) {
            ByteArrayOutputStream noiDungBody = new ByteArrayOutputStream();
            int[] viTriOffsets = new int[20];
            int demDoiTuong = 0;

            noiDungBody.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 1; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 2; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 3; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> /XObject << /ImChart 7 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            byte[] mangByteAnh = new byte[0];
            int rongAnh = 500, caoAnh = 220;
            if (bieuDoImg != null) {
                rongAnh = bieuDoImg.getWidth();
                caoAnh = bieuDoImg.getHeight();
                ByteArrayOutputStream luongByteAnh = new ByteArrayOutputStream();
                ImageIO.write(bieuDoImg, "jpg", luongByteAnh);
                mangByteAnh = luongByteAnh.toByteArray();
            }

            ByteArrayOutputStream noiDungStream = new ByteArrayOutputStream();
            SimpleDateFormat dinhDangNgay = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String thoiGianLap = dinhDangNgay.format(new Date());

            StringBuilder chuoiBuilder = new StringBuilder();
            chuoiBuilder.append("0.05 0.58 0.53 rg\n35 770 525 45 re f\nBT\n/F2 16 Tf\n1 1 1 rg\n");
            chuoiBuilder.append("50 788 Td\n(HE THONG QUAN LY SALALA PET SHOP - BAO CAO THONG KE) Tj\nET\n");

            chuoiBuilder.append("BT\n/F1 10 Tf\n0.2 0.25 0.3 rg\n35 750 Td\n(").append(thoatKyTuPdf("Ky bao cao: " + boDauTiengViet(kyBaoCao))).append(") Tj\n");
            chuoiBuilder.append("300 0 Td\n(").append(thoatKyTuPdf("Ngay xuat: " + thoiGianLap)).append(") Tj\nET\n");

            chuoiBuilder.append("0.85 0.88 0.92 RG\n1 w\n35 742 m 560 742 l S\n");

            chuoiBuilder.append("0.96 0.97 0.98 rg\n35 680 120 50 re f\n0.96 0.97 0.98 rg\n170 680 120 50 re f\n");
            chuoiBuilder.append("0.96 0.97 0.98 rg\n305 680 120 50 re f\n0.96 0.97 0.98 rg\n440 680 120 50 re f\n");

            chuoiBuilder.append("0.85 0.88 0.92 RG\n35 680 120 50 re S\n170 680 120 50 re S\n305 680 120 50 re S\n440 680 120 50 re S\n");

            chuoiBuilder.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n42 715 Td\n(TONG DOANH THU) Tj\n/F2 11 Tf\n0.05 0.58 0.53 rg\n0 -16 Td\n(").append(thoatKyTuPdf(boDauTiengViet(tongDoanhThu))).append(") Tj\nET\n");
            chuoiBuilder.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n177 715 Td\n(SO HOA DON) Tj\n/F2 11 Tf\n0.1 0.4 0.8 rg\n0 -16 Td\n(").append(thoatKyTuPdf(boDauTiengViet(tongHoaDon))).append(") Tj\nET\n");
            chuoiBuilder.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n312 715 Td\n(KHACH HANG) Tj\n/F2 11 Tf\n0.1 0.6 0.3 rg\n0 -16 Td\n(").append(thoatKyTuPdf(boDauTiengViet(tongKhachHang))).append(") Tj\nET\n");
            chuoiBuilder.append("BT\n/F1 9 Tf\n0.4 0.45 0.5 rg\n447 715 Td\n(THU CUNG) Tj\n/F2 11 Tf\n0.6 0.2 0.7 rg\n0 -16 Td\n(").append(thoatKyTuPdf(boDauTiengViet(tongThuCung))).append(") Tj\nET\n");

            if (mangByteAnh.length > 0) {
                chuoiBuilder.append("0.85 0.88 0.92 RG\n35 480 525 185 re S\nq\n523 0 0 183 36 481 cm\n/ImChart Do\nQ\n");
            }

            int toaDoY = 450;
            chuoiBuilder.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(toaDoY).append(" Td\n(1. TOP 5 DICH VU VA SAN PHAM DOANH THU CAO NHAT) Tj\nET\n");
            toaDoY -= 18;
            chuoiBuilder.append("0.93 0.95 0.97 rg\n35 ").append(toaDoY - 4).append(" 525 18 re f\nBT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            chuoiBuilder.append("42 ").append(toaDoY).append(" Td\n(STT) Tj\n50 0 Td\n(TEN DICH VU / SAN PHAM) Tj\n260 0 Td\n(SO LUOT) Tj\n90 0 Td\n(DOANH THU) Tj\nET\n");

            if (topDichVu != null) {
                int stt = 1;
                for (Object[] dong : topDichVu) {
                    if (stt > 5) break;
                    toaDoY -= 16;
                    if (stt % 2 == 0) chuoiBuilder.append("0.98 0.98 0.99 rg\n35 ").append(toaDoY - 3).append(" 525 15 re f\n");
                    chuoiBuilder.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n42 ").append(toaDoY).append(" Td\n(").append(stt).append(") Tj\n");
                    chuoiBuilder.append("50 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[0])))).append(") Tj\n260 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[1])))).append(") Tj\n90 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[2])))).append(") Tj\nET\n");
                    stt++;
                }
            }

            toaDoY -= 26;
            chuoiBuilder.append("BT\n/F2 11 Tf\n0.1 0.15 0.2 rg\n35 ").append(toaDoY).append(" Td\n(2. DOANH SO PHUC VU THEO NHAN VIEN VA BAC SI) Tj\nET\n");
            toaDoY -= 18;
            chuoiBuilder.append("0.93 0.95 0.97 rg\n35 ").append(toaDoY - 4).append(" 525 18 re f\nBT\n/F2 9 Tf\n0.15 0.2 0.25 rg\n");
            chuoiBuilder.append("42 ").append(toaDoY).append(" Td\n(MA NV) Tj\n60 0 Td\n(HO VA TEN) Tj\n180 0 Td\n(VAI TRO) Tj\n110 0 Td\n(SO DON) Tj\n70 0 Td\n(DOANH SO) Tj\nET\n");

            if (doanhSoNV != null) {
                int stt = 1;
                for (Object[] dong : doanhSoNV) {
                    if (stt > 4) break;
                    toaDoY -= 16;
                    if (stt % 2 == 0) chuoiBuilder.append("0.98 0.98 0.99 rg\n35 ").append(toaDoY - 3).append(" 525 15 re f\n");
                    chuoiBuilder.append("BT\n/F1 9 Tf\n0.2 0.25 0.3 rg\n42 ").append(toaDoY).append(" Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[0])))).append(") Tj\n");
                    chuoiBuilder.append("60 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[1])))).append(") Tj\n180 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[2])))).append(") Tj\n110 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[3])))).append(") Tj\n70 0 Td\n(").append(thoatKyTuPdf(boDauTiengViet(String.valueOf(dong[4])))).append(") Tj\nET\n");
                    stt++;
                }
            }

            toaDoY -= 35;
            chuoiBuilder.append("BT\n/F2 10 Tf\n0.2 0.25 0.3 rg\n60 ").append(toaDoY).append(" Td\n(NGUOI LAP BIEU) Tj\n350 0 Td\n(GIAM DOC / QUAN LY) Tj\n");
            chuoiBuilder.append("/F1 9 Tf\n0.4 0.45 0.5 rg\n-340 -14 Td\n(Ky va ghi ro ho ten) Tj\n350 0 Td\n(Ky, dong dau xac nhan) Tj\n");

            String tenNguoiKy = (nguoiLap != null && !nguoiLap.trim().isEmpty()) ? nguoiLap : "Le Van Thanh";
            chuoiBuilder.append("-345 -36 Td\n/F2 10 Tf\n0.1 0.15 0.2 rg\n(").append(thoatKyTuPdf(boDauTiengViet(tenNguoiKy))).append(") Tj\nET\n");

            chuoiBuilder.append("BT\n/F1 8 Tf\n0.5 0.55 0.6 rg\n180 20 Td\n(Bao cao duoc tao tu dong boi He thong SALALA Pet Shop - salala.vn) Tj\nET\n");

            noiDungStream.write(chuoiBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
            byte[] mangByteStream = noiDungStream.toByteArray();

            viTriOffsets[4] = noiDungBody.size();
            noiDungBody.write(("4 0 obj\n<< /Length " + mangByteStream.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            noiDungBody.write(mangByteStream);
            noiDungBody.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 5; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 6; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            demDoiTuong = 7; viTriOffsets[demDoiTuong] = noiDungBody.size();
            noiDungBody.write(("7 0 obj\n<< /Type /XObject /Subtype /Image /Width " + rongAnh + " /Height " + caoAnh + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + mangByteAnh.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            noiDungBody.write(mangByteAnh);
            noiDungBody.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            int viTriXref = noiDungBody.size();
            noiDungBody.write("xref\n0 8\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 7; i++) {
                noiDungBody.write(String.format("%010d 00000 n \n", viTriOffsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }

            noiDungBody.write(("trailer\n<< /Size 8 /Root 1 0 R >>\nstartxref\n" + viTriXref + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

            luongGhiFile.write(noiDungBody.toByteArray());
            return true;
        } catch (Exception loi) {
            loi.printStackTrace();
            return false;
        }
    }

    private String thoatKyTuPdf(String chuoiGoc) {
        if (chuoiGoc == null) return "";
        return chuoiGoc.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    public String boDauTiengViet(String chuoi) {
        if (chuoi == null) return "";
        String chuoiKhongDau = java.text.Normalizer.normalize(chuoi, java.text.Normalizer.Form.NFD);
        String ketQua = chuoiKhongDau.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        ketQua = ketQua.replace('đ', 'd').replace('Đ', 'D');
        return ketQua;
    }
}