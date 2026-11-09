package controller.HoaDon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dao.HoaDon.HoaDon_Dao;
import entity.HoaDon.HoaDon_entity;

/**
 * Controller xử lý nghiệp vụ của màn hình danh sách hóa đơn.
 * Không chứa mã tạo Swing component hoặc hiển thị JOptionPane.
 */
public class DSHoaDonController {
    private final HoaDon_Dao hoaDonDao;

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] TIEU_DE_CSV = {
            "Mã Hóa Đơn", "Ngày Lập", "Trạng Thái", "Mã Khách Hàng",
            "Mã Nhân Viên", "Mã Khuyến Mãi", "Mã Thuế", "Mã Thanh Toán"
    };

    public DSHoaDonController() {
        this(new HoaDon_Dao());
    }

    DSHoaDonController(HoaDon_Dao hoaDonDao) {
        this.hoaDonDao = hoaDonDao;
    }

    // Lấy dữ liệu hóa đơn từ DAO.
    public ArrayList<HoaDon_entity> taiDanhSach() {
        return hoaDonDao.getDSHD();
    }

    // Tìm kiếm theo mã hóa đơn và lọc theo trạng thái.
    public ArrayList<HoaDon_entity> locDanhSach(
            List<HoaDon_entity> danhSach,
            String tuKhoa,
            String trangThai) {

        String tuKhoaChuanHoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();

        ArrayList<HoaDon_entity> ketQua = new ArrayList<>();

        if (danhSach == null) {
            return ketQua;
        }

        for (HoaDon_entity hoaDon : danhSach) {
            if (hoaDon == null) {
                continue;
            }

            String maHD = hoaDon.getMaHD() == null
                    ? ""
                    : hoaDon.getMaHD().toLowerCase();

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

    // Tính số lượng hóa đơn theo trạng thái.
    public ThongKe thongKe(List<HoaDon_entity> danhSach) {
        int daThanhToan = 0;
        int chuaThanhToan = 0;

        if (danhSach != null) {
            for (HoaDon_entity hoaDon : danhSach) {
                if (hoaDon == null) {
                    continue;
                }

                if (laDaThanhToan(hoaDon.getTrangThai())) {
                    daThanhToan++;
                } else {
                    chuaThanhToan++;
                }
            }
        }

        int tongSo = daThanhToan + chuaThanhToan;
        return new ThongKe(tongSo, daThanhToan, chuaThanhToan);
    }

    public boolean daThanhToan(String trangThai) {
        return laDaThanhToan(trangThai);
    }

    // Kiểm tra hóa đơn đã thanh toán hay chưa.
    private boolean laDaThanhToan(String trangThai) {
        return trangThai != null
                && (trangThai.equalsIgnoreCase("Đã thanh toán")
                        || trangThai.equalsIgnoreCase("Paid"));
    }

    // Xóa hóa đơn thông qua DAO.
    public boolean xoaHoaDon(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }
        return hoaDonDao.xoaHD(maHD);
    }

    /*
     * Chuyển một hóa đơn thành dữ liệu từng cột để GUI đưa vào JTable.
     * Controller xử lý việc lấy mã từ các entity liên quan.
     */
    public Object[] taoDongHienThi(HoaDon_entity hd) {
        return new Object[] {
                hd.getMaHD(),
                hd.getNgayLap() == null ? "" : hd.getNgayLap().format(DMY),
                hd.getTrangThai(),
                layMa(hd.getMaKH()),
                layMa(hd.getMaNhanVien()),
                layMa(hd.getMaKM()),
                layMa(hd.getMaThue()),
                layMa(hd.getMaThanhToan())
        };
    }

    // Lấy mã từ đối tượng liên quan (KhachHang, NhanVien, KhuyenMai...).
    public String layMa(Object doiTuong) {
        if (doiTuong == null) {
            return "NULL";
        }

        if (doiTuong instanceof String) {
            return (String) doiTuong;
        }

        String[] cacGetter = {
                "getMaKH", "getMaNhanVien", "getMaKM", "getMaThue",
                "getMaThanhToan", "getMaNCC", "getMaDV", "getMaVT", "getMa"
        };

        for (String tenGetter : cacGetter) {
            try {
                Method method = doiTuong.getClass().getMethod(tenGetter);
                Object ketQua = method.invoke(doiTuong);

                if (ketQua != null && ketQua != doiTuong) {
                    return layMa(ketQua);
                }
            } catch (ReflectiveOperationException ignored) {
                // Thử getter tiếp theo nếu đối tượng không có getter này.
            }
        }

        return doiTuong.toString();
    }

    // Xuất danh sách hóa đơn đang được hiển thị ra CSV.
    public void xuatCSV(List<HoaDon_entity> danhSach, File file)
            throws IOException {

        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');

            for (int i = 0; i < TIEU_DE_CSV.length; i++) {
                if (i > 0) {
                    writer.print(",");
                }
                writer.print("\"" + TIEU_DE_CSV[i] + "\"");
            }
            writer.println();

            if (danhSach == null) {
                return;
            }

            for (HoaDon_entity hd : danhSach) {
                Object[] dong = taoDongHienThi(hd);

                for (int i = 0; i < dong.length; i++) {
                    if (i > 0) {
                        writer.print(",");
                    }

                    String giaTri = dong[i] == null ? "" : dong[i].toString();
                    writer.print("\"" + giaTri.replace("\"", "\"\"") + "\"");
                }
                writer.println();
            }
        }
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

        public int getTongSo() {
            return tongSo;
        }

        public int getDaThanhToan() {
            return daThanhToan;
        }

        public int getChuaThanhToan() {
            return chuaThanhToan;
        }
    }
}
