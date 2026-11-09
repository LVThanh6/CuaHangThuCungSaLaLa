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


    }

    public enum KhoangThoiGianNhanh {
        HOM_NAY, BAY_NGAY_QUA, BA_MUOI_NGAY_QUA, THANG_NAY, NAM_NAY
    }


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


    }

    public static class DuLieuBaoCao {
        private List<ChiSoBaoCao> danhSachChiSo = new ArrayList<>();
        private double tongDoanhThu, soHoaDon, soLuongBan, doanhThuHangHoa, doanhThuDichVu, tongLuong;


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

    }

    // ==========================================================
    // 2. CÁC HÀM XỬ LÝ LOGIC ĐIỀU KIỆN
    // ==========================================================


        }
    }

    public static String loiKhoangNgay(LocalDate tu, LocalDate den) {
        if (tu.isAfter(den))
            return "Ngày bắt đầu không được lớn hơn ngày kết thúc.";
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

        if (ma.contains("luong")) {
            return ma.contains("chi-tiet") ? DanhMuc.NHAN_SU : null;
        }
        return DanhMuc.BAN_HANG; 
    }


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


        for (NhanVien_entity nv : dsNhanVienTraCuu) {
            String ma = boDau(nv.getMaNhanVien());
            String ten = boDau(nv.getHoTen());

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

            }
            baoCao.setDanhSachChiSo(danhSachChiSo);


        }
    }

    // ==========================================================
    // 4. LOGIC XUẤT FILE PDF TRỰC TIẾP
    // ==========================================================



            noiDungBody.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));


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



            chuoiBuilder.append("0.85 0.88 0.92 RG\n1 w\n35 742 m 560 742 l S\n");

            chuoiBuilder.append("0.96 0.97 0.98 rg\n35 680 120 50 re f\n0.96 0.97 0.98 rg\n170 680 120 50 re f\n");
            chuoiBuilder.append("0.96 0.97 0.98 rg\n305 680 120 50 re f\n0.96 0.97 0.98 rg\n440 680 120 50 re f\n");



            if (mangByteAnh.length > 0) {
                chuoiBuilder.append("0.85 0.88 0.92 RG\n35 480 525 185 re S\nq\n523 0 0 183 36 481 cm\n/ImChart Do\nQ\n");
            }


                    stt++;
                }
            }


                    stt++;
                }
            }



            noiDungStream.write(chuoiBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
            byte[] mangByteStream = noiDungStream.toByteArray();


            int viTriXref = noiDungBody.size();
            noiDungBody.write("xref\n0 8\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 7; i++) {
                noiDungBody.write(String.format("%010d 00000 n \n", viTriOffsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }



            luongGhiFile.write(noiDungBody.toByteArray());
            return true;
        } catch (Exception loi) {
            loi.printStackTrace();
            return false;
        }
    }


    }
}