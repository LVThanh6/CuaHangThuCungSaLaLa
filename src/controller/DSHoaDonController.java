package controller;

import java.util.ArrayList;
import java.util.List;

import dao.HoaDon.HoaDon_Dao;
import entity.HoaDon.HoaDon_entity;

public class DSHoaDonController {
    private final HoaDon_Dao hoaDonDao;

    public DSHoaDonController() {
        this(new HoaDon_Dao());
    }

    DSHoaDonController(HoaDon_Dao hoaDonDao) {
        this.hoaDonDao = hoaDonDao;
    }

    public ArrayList<HoaDon_entity> taiDanhSach() {
        return hoaDonDao.getDSHD();
    }

    public ArrayList<HoaDon_entity> locDanhSach(
            List<HoaDon_entity> danhSach,
            String tuKhoa,
            String trangThai) {
        String tuKhoaChuanHoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        ArrayList<HoaDon_entity> ketQua = new ArrayList<>();
        for (HoaDon_entity hoaDon : danhSach) {
            String maHD = hoaDon.getMaHD() == null ? "" : hoaDon.getMaHD().toLowerCase();
            if (!maHD.contains(tuKhoaChuanHoa)) {
                continue;
            }

            boolean daThanhToan = laDaThanhToan(hoaDon.getTrangThai());
            if ("Đã thanh toán".equals(trangThai) && !daThanhToan) {
                continue;
            }
            if ("Chưa thanh toán".equals(trangThai) && daThanhToan) {
                continue;
            }
            ketQua.add(hoaDon);
        }
        return ketQua;
    }

    public ThongKe thongKe(List<HoaDon_entity> danhSach) {
        int daThanhToan = 0;
        int chuaThanhToan = 0;
        for (HoaDon_entity hoaDon : danhSach) {
            if (laDaThanhToan(hoaDon.getTrangThai())) {
                daThanhToan++;
            } else {
                chuaThanhToan++;
            }
        }
        return new ThongKe(danhSach.size(), daThanhToan, chuaThanhToan);
    }

    public boolean daThanhToan(String trangThai) {
        return laDaThanhToan(trangThai);
    }

    public boolean xoaHoaDon(String maHD) {
        return hoaDonDao.xoaHD(maHD);
    }

    private boolean laDaThanhToan(String trangThai) {
        return trangThai != null
                && (trangThai.equalsIgnoreCase("Đã thanh toán")
                        || trangThai.equalsIgnoreCase("Paid"));
    }

    public static final class ThongKe {
        private final int tongSo;
        private final int daThanhToan;
        private final int chuaThanhToan;

        private ThongKe(int tongSo, int daThanhToan, int chuaThanhToan) {
            this.tongSo = tongSo;
            this.daThanhToan = daThanhToan;
            this.chuaThanhToan = chuaThanhToan;
        }

        public int getTongSo() { return tongSo; }
        public int getDaThanhToan() { return daThanhToan; }
        public int getChuaThanhToan() { return chuaThanhToan; }
    }
}
