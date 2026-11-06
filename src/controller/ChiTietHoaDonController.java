package controller;

import java.util.ArrayList;

import dao.HoaDon.ChiTietHoaDon_DAO;
import dao.HoaDon.HoaDon_Dao;
import dao.KhachHang.HangThanhVien_DAO;
import dao.ThuCung.ThuCung_DAO;
import entity.HoaDon.ChiTietHoaDon_entity;
import entity.HoaDon.HoaDon_entity;
import entity.HoaDon.ThanhToan_entity;
import entity.KhachHang.HangThanhVien;
import entity.KhachHang.KhachHang;
import entity.ThuCung.ThuCung_entity;

public class ChiTietHoaDonController {
    private final HoaDon_Dao hoaDonDao;
    private final ChiTietHoaDon_DAO chiTietHoaDonDao;
    private final ThuCung_DAO thuCungDao;
    private final HangThanhVien_DAO hangThanhVienDao;

    public ChiTietHoaDonController() {
        this(new HoaDon_Dao(), new ChiTietHoaDon_DAO(), new ThuCung_DAO(), new HangThanhVien_DAO());
    }

    ChiTietHoaDonController(
            HoaDon_Dao hoaDonDao,
            ChiTietHoaDon_DAO chiTietHoaDonDao,
            ThuCung_DAO thuCungDao,
            HangThanhVien_DAO hangThanhVienDao) {
        this.hoaDonDao = hoaDonDao;
        this.chiTietHoaDonDao = chiTietHoaDonDao;
        this.thuCungDao = thuCungDao;
        this.hangThanhVienDao = hangThanhVienDao;
    }

    public DuLieuChiTiet taiDuLieu(String maHD) {
        HoaDon_entity hoaDon = hoaDonDao.getHDTheoMa(maHD);
        if (hoaDon == null) {
            return null;
        }

        ArrayList<ChiTietHoaDon_entity> cacDong = chiTietHoaDonDao.getChiTietHoaDonTheoMaHD(maHD);
        ArrayList<ThuCung_entity> thuCung = new ArrayList<>();
        if (hoaDon.getMaTC() != null) {
            ThuCung_entity thongTinThuCung = thuCungDao.getThuCungTheoMa(hoaDon.getMaTC().getMaTC());
            if (thongTinThuCung != null) {
                thuCung.add(thongTinThuCung);
            }
        }

        return new DuLieuChiTiet(hoaDon, cacDong, thuCung, layHangThanhVien(hoaDon.getMaKH()));
    }

    public TongTien tinhTongTien(HoaDon_entity hoaDon, ArrayList<ChiTietHoaDon_entity> cacDong) {
        double mucKhuyenMai = hoaDon.getMaKM() == null ? 0 : hoaDon.getMaKM().getMucKM();
        double mucThue = hoaDon.getMaThue() == null ? 0 : hoaDon.getMaThue().getMucThue();
        double tongTien = 0;
        for (ChiTietHoaDon_entity dong : cacDong) {
            tongTien += dong.getSoLuong() * dong.getDonGia();
        }

        double tienKhuyenMai = tongTien * mucKhuyenMai / 100.0;
        double tienThue = (tongTien - tienKhuyenMai) * mucThue / 100.0;
        double thanhTien = tongTien - tienKhuyenMai + tienThue;
        ThanhToan_entity thanhToan = hoaDon.getMaThanhToan();
        double daThanhToan = thanhToan != null && laDaThanhToan(thanhToan.getTrangThai())
                ? thanhToan.getSoTien()
                : 0;
        double conPhaiThu = Math.max(0, thanhTien - daThanhToan);
        if (conPhaiThu < 0.5) {
            conPhaiThu = 0;
        }

        return new TongTien(
                mucKhuyenMai, mucThue, tongTien, tienKhuyenMai, tienThue,
                thanhTien, daThanhToan, conPhaiThu);
    }

    public boolean xacNhanThanhToan(HoaDon_entity hoaDon, String phuongThuc, double soTien) {
        if (!hoaDonDao.xacNhanThanhToan(hoaDon.getMaHD(), phuongThuc, soTien)) {
            return false;
        }

        hoaDon.setTrangThai("Đã thanh toán");
        ThanhToan_entity thanhToan = hoaDon.getMaThanhToan();
        if (thanhToan == null) {
            thanhToan = new ThanhToan_entity(
                    "TT" + System.currentTimeMillis(), soTien,
                    java.time.LocalDate.now(), phuongThuc, "Đã thanh toán");
            hoaDon.setMaThanhToan(thanhToan);
        } else {
            thanhToan.setTrangThai("Đã thanh toán");
            thanhToan.setSoTien(soTien);
            thanhToan.setPhuongThuc(phuongThuc);
            thanhToan.setThoiGian(java.time.LocalDate.now());
        }
        return true;
    }

    public boolean laHoaDonDaThanhToan(HoaDon_entity hoaDon) {
        if (hoaDon == null) {
            return false;
        }
        if (laDaThanhToan(hoaDon.getTrangThai())) {
            return true;
        }
        ThanhToan_entity thanhToan = hoaDon.getMaThanhToan();
        return thanhToan != null && laDaThanhToan(thanhToan.getTrangThai());
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
        HangThanhVien hang = hangThanhVienDao.getHangThanhVienTheoMaKH(khachHang.getMaKH());
        return hang == null ? null : hang.getTenHang();
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

        public HoaDon_entity getHoaDon() {
            return hoaDon;
        }

        public ArrayList<ChiTietHoaDon_entity> getCacDong() {
            return cacDong;
        }

        public ArrayList<ThuCung_entity> getThuCung() {
            return thuCung;
        }

        public String getHangThanhVien() {
            return hangThanhVien;
        }
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
