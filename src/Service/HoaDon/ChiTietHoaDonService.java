package Service.HoaDon;

import java.util.ArrayList;

import dao.HoaDon.ChiTietHoaDon_DAO;
import dao.HoaDon.HoaDon_Dao;
import dao.KhachHang.HangThanhVien_DAO;
import dao.ThuCung.ThuCung_DAO;
import entity.HoaDon.ChiTietHoaDon_entity;
import entity.HoaDon.HoaDon_entity;
import entity.KhachHang.HangThanhVien;
import entity.KhachHang.KhachHang;
import entity.ThuCung.ThuCung_entity;

/**
 * Service xử lý dữ liệu và nghiệp vụ của màn hình chi tiết hóa đơn.
 * Không chứa mã tạo Swing component, JOptionPane hoặc PrinterJob.
 */
public class ChiTietHoaDonService {
    private final HoaDon_Dao hoaDonDao;
    private final ChiTietHoaDon_DAO chiTietHoaDonDao;
    private final ThuCung_DAO thuCungDao;
    private final HangThanhVien_DAO hangThanhVienDao;

    public ChiTietHoaDonService() {
        this(new HoaDon_Dao(), new ChiTietHoaDon_DAO(),
                new ThuCung_DAO(), new HangThanhVien_DAO());
    }

    ChiTietHoaDonService(
            HoaDon_Dao hoaDonDao,
            ChiTietHoaDon_DAO chiTietHoaDonDao,
            ThuCung_DAO thuCungDao,
            HangThanhVien_DAO hangThanhVienDao) {
        this.hoaDonDao = hoaDonDao;
        this.chiTietHoaDonDao = chiTietHoaDonDao;
        this.thuCungDao = thuCungDao;
        this.hangThanhVienDao = hangThanhVienDao;
    }

    // Lấy toàn bộ dữ liệu cần thiết để GUI hiển thị chi tiết hóa đơn.
    public DuLieuChiTiet taiDuLieu(String maHD) {
        HoaDon_entity hoaDon = hoaDonDao.getHDTheoMa(maHD);
        if (hoaDon == null) {
            return null;
        }

        ArrayList<ChiTietHoaDon_entity> cacDong =
                chiTietHoaDonDao.getChiTietHoaDonTheoMaHD(maHD);

        if (cacDong == null) {
            cacDong = new ArrayList<>();
        }

        ArrayList<ThuCung_entity> thuCung = new ArrayList<>();
        if (hoaDon.getMaTC() != null) {
            ThuCung_entity thongTinThuCung =
                    thuCungDao.getThuCungTheoMa(hoaDon.getMaTC().getMaTC());
            if (thongTinThuCung != null) {
                thuCung.add(thongTinThuCung);
            }
        }

        return new DuLieuChiTiet(
                hoaDon, cacDong, thuCung, layHangThanhVien(hoaDon.getMaKH()));
    }

    // Tính tổng tiền, khuyến mãi, thuế, số tiền đã thu và còn phải thu.
    public TongTien tinhTongTien(
            HoaDon_entity hoaDon,
            ArrayList<ChiTietHoaDon_entity> cacDong) {

        double mucKhuyenMai = hoaDon.getMaKM() == null
                ? 0 : hoaDon.getMaKM().getMucKM();

        double mucThue = hoaDon.getMaThue() == null
                ? 0 : hoaDon.getMaThue().getMucThue();

        double tongTien = 0;
        if (cacDong != null) {
            for (ChiTietHoaDon_entity dong : cacDong) {
                if (dong != null) {
                    tongTien += dong.getSoLuong() * dong.getDonGia();
                }
            }
        }

        double tienKhuyenMai = tongTien * mucKhuyenMai / 100.0;
        double tienThue = (tongTien - tienKhuyenMai) * mucThue / 100.0;
        double thanhTien = tongTien - tienKhuyenMai + tienThue;

        double daThanhToan = laDaThanhToan(hoaDon.getTrangThai())
                ? thanhTien : 0;

        double conPhaiThu = Math.max(0, thanhTien - daThanhToan);
        if (conPhaiThu < 0.5) {
            conPhaiThu = 0;
        }

        return new TongTien(
                mucKhuyenMai, mucThue, tongTien, tienKhuyenMai,
                tienThue, thanhTien, daThanhToan, conPhaiThu);
    }

    /*
     * Chuẩn bị dữ liệu hiển thị cho một dòng chi tiết.
     * GUI chỉ nhận Object[] và đưa vào JTable, không tự tính nghiệp vụ.
     */
    public Object[] taoDongChiTietHienThi(
            ChiTietHoaDon_entity chiTiet, double mucKhuyenMai) {

        String ma;
        String ten;
        String loai;

        if (chiTiet.getMaDV() != null) {
            ma = chiTiet.getMaDV().getMaDV();
            ten = chiTiet.getMaDV().getTenDV();
            loai = "Dịch vụ";
        } else if (chiTiet.getMaVT() != null) {
            ma = chiTiet.getMaVT().getMaVT();
            ten = chiTiet.getMaVT().getTenVT();
            loai = chiTiet.getMaVT().getMaThuoc() == null
                    ? "Vật tư" : "Thuốc kê đơn";
        } else {
            ma = null;
            ten = null;
            loai = "—";
        }

        double tienGoc = chiTiet.getSoLuong() * chiTiet.getDonGia();
        double tienKhuyenMai = tienGoc * mucKhuyenMai / 100.0;
        double thanhTienDong = tienGoc - tienKhuyenMai;

        return new Object[] {
            giaTri(ma),
            giaTri(ten),
            loai,
            chiTiet.getSoLuong(),
            tienGocDonVi(chiTiet.getDonGia()),
            tienKhuyenMai,
            thanhTienDong
        };
    }

    /*
     * Lấy trạng thái thanh toán từ hóa đơn.
     */
    public String layTrangThaiThanhToan(HoaDon_entity hoaDon) {
        if (hoaDon == null) {
            return null;
        }

        return hoaDon.getTrangThai();
    }

    // Lưu xác nhận thanh toán vào CSDL và cập nhật entity đang được hiển thị.
    public boolean xacNhanThanhToan(
            HoaDon_entity hoaDon, String phuongThuc) {

        if (hoaDon == null || phuongThuc == null || phuongThuc.trim().isEmpty()
                || laHoaDonDaThanhToan(hoaDon)) {
            return false;
        }

        if (!hoaDonDao.xacNhanThanhToan(
                hoaDon.getMaHD(), phuongThuc)) {
            return false;
        }

        hoaDon.setTrangThai("Đã thanh toán");
        hoaDon.setPhuongThucThanhToan(phuongThuc.trim());
        return true;
    }

    public boolean laHoaDonDaThanhToan(HoaDon_entity hoaDon) {
        return hoaDon != null
                && laDaThanhToan(hoaDon.getTrangThai());
    }

    public static boolean laDaThanhToan(String trangThai) {
        return trangThai != null
                && (trangThai.equalsIgnoreCase("Đã thanh toán")
                || trangThai.equalsIgnoreCase("Paid"));
    }

    private String layHangThanhVien(KhachHang khachHang) {
        if (khachHang == null || khachHang.getMaKH() == null) {
            return null;
        }

        if (khachHang.getHangThanhVien() != null) {
            return khachHang.getHangThanhVien();
        }

        HangThanhVien hang =
                hangThanhVienDao.getHangThanhVienTheoMaKH(khachHang.getMaKH());

        return hang == null ? null : hang.getTenHang();
    }

    // Định dạng giá trị rỗng cho dữ liệu hiển thị.
    private String giaTri(String giaTri) {
        return giaTri == null || giaTri.isBlank() ? "—" : giaTri;
    }

    /*
     * Giữ kiểu dữ liệu số cho JTable; GUI chịu trách nhiệm định dạng tiền
     * theo giao diện (dấu phân cách, ký hiệu VNĐ).
     */
    private double tienGocDonVi(double donGia) {
        return donGia;
    }

    public static final class DuLieuChiTiet {
        private final HoaDon_entity hoaDon;
        private final ArrayList<ChiTietHoaDon_entity> cacDong;
        private final ArrayList<ThuCung_entity> thuCung;
        private final String hangThanhVien;

        private DuLieuChiTiet(
                HoaDon_entity hoaDon,
                ArrayList<ChiTietHoaDon_entity> cacDong,
                ArrayList<ThuCung_entity> thuCung,
                String hangThanhVien) {
            this.hoaDon = hoaDon;
            this.cacDong = cacDong;
            this.thuCung = thuCung;
            this.hangThanhVien = hangThanhVien;
        }

        public HoaDon_entity getHoaDon() { return hoaDon; }
        public ArrayList<ChiTietHoaDon_entity> getCacDong() { return cacDong; }
        public ArrayList<ThuCung_entity> getThuCung() { return thuCung; }
        public String getHangThanhVien() { return hangThanhVien; }
    }

    public static final class TongTien {
        private final double mucKhuyenMai;
        private final double mucThue;
        private final double tongTien;
        private final double tienKhuyenMai;
        private final double tienThue;
        private final double thanhTien;
        private final double daThanhToan;
        private final double conPhaiThu;

        private TongTien(
                double mucKhuyenMai,
                double mucThue,
                double tongTien,
                double tienKhuyenMai,
                double tienThue,
                double thanhTien,
                double daThanhToan,
                double conPhaiThu) {
            this.mucKhuyenMai = mucKhuyenMai;
            this.mucThue = mucThue;
            this.tongTien = tongTien;
            this.tienKhuyenMai = tienKhuyenMai;
            this.tienThue = tienThue;
            this.thanhTien = thanhTien;
            this.daThanhToan = daThanhToan;
            this.conPhaiThu = conPhaiThu;
        }

        public double getMucKhuyenMai() { return mucKhuyenMai; }
        public double getMucThue() { return mucThue; }
        public double getTongTien() { return tongTien; }
        public double getTienKhuyenMai() { return tienKhuyenMai; }
        public double getTienThue() { return tienThue; }
        public double getThanhTien() { return thanhTien; }
        public double getDaThanhToan() { return daThanhToan; }
        public double getConPhaiThu() { return conPhaiThu; }
    }
}
