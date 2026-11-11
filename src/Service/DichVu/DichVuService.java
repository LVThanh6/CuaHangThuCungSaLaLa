package Service.DichVu;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import dao.DichVu.DichVu_DAO;
import entity.DichVu.DichVu_entity;

public class DichVuService {
    private final DichVu_DAO dao;

    public DichVuService() {
        this(new DichVu_DAO());
    }

    DichVuService(DichVu_DAO dao) {
        this.dao = dao;
    }

    public List<DichVuData> taiDanhSach() {
        List<DichVuData> ketQua = new ArrayList<>();
        List<DichVu_entity> danhSach = java.util.Objects.requireNonNull(
                dao.getAllDichVu(), "Service data was not returned by the DAO");
        for (DichVu_entity dichVu : danhSach) {
            int phut = 30;
            if (dichVu.getThoiGian() != null) {
                phut = dichVu.getThoiGian().getHour() * 60 + dichVu.getThoiGian().getMinute();
                if (phut <= 0) {
                    phut = 30;
                }
            }
            boolean hoatDong = laDangHoatDong(dichVu.getTrangThai());
            ketQua.add(new DichVuData(
                    dichVu.getMaDV(), dichVu.getTenDV(), nhomTheoTen(dichVu.getTenDV()),
                    dichVu.getMoTa() == null ? "" : dichVu.getMoTa(),
                    phut, Math.round(dichVu.getGiaDV()), hoatDong));
        }
        return ketQua;
    }

    public String maTiepTheo() {
        return dao.getMaTiepTheo();
    }

    public DichVuData phanTichBieuMau(String ma, String ten, String moTa, String phut, String gia,
            boolean hoatDong) {
        int thoiLuong;
        long soTien;
        try {
            thoiLuong = Integer.parseInt(phut.trim());
            soTien = Long.parseLong(gia.trim().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Thời gian hoặc giá không hợp lệ!", loi);
        }
        String tenDaChuanHoa = ten.trim();
        if (tenDaChuanHoa.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống!");
        }
        if (thoiLuong <= 0 || soTien < 0) {
            throw new IllegalArgumentException("Thời gian thực hiện phải > 0 và giá dịch vụ phải >= 0!");
        }
        return new DichVuData(ma.trim(), tenDaChuanHoa, nhomTheoTen(tenDaChuanHoa), moTa.trim(),
                thoiLuong, soTien, hoatDong);
    }

    public long phanTichGia(String giaTri) {
        try {
            long soTien = Long.parseLong(giaTri.replaceAll("[^0-9]", ""));
            if (soTien < 0) {
                throw new IllegalArgumentException("Giá nhập không hợp lệ!");
            }
            return soTien;
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ!", loi);
        }
    }

    public boolean tao(DichVuData dichVu) {
        return dao.createDichVu(chuyenSangEntity(dichVu));
    }

    public boolean capNhat(DichVuData dichVu) {
        return dao.updateDichVu(chuyenSangEntity(dichVu));
    }

    public boolean capNhatGia(DichVuData dichVu, long gia) {
        DichVuData daCapNhat = dichVu.withPrice(gia);
        return dao.updateDichVu(chuyenSangEntity(daCapNhat));
    }

    public boolean capNhatTrangThai(String ma, boolean hoatDong) {
        return dao.updateTrangThai(ma, hoatDong ? "Đang áp dụng" : "Tạm ẩn");
    }

    public boolean xoa(String ma) {
        return dao.deleteDichVu(ma);
    }

    public List<DichVuData> loc(List<DichVuData> danhSach, String tuKhoa, String nhom, int chiSoTrangThai) {
        String chuoiTim = tuKhoa.trim().toLowerCase();
        return danhSach.stream()
                .filter(dichVu -> (dichVu.ma + " " + dichVu.ten + " " + dichVu.moTa)
                        .toLowerCase().contains(chuoiTim))
                .filter(dichVu -> nhom == null || nhom.equals("Tất cả") || nhom.equals(dichVu.nhom))
                .filter(dichVu -> chiSoTrangThai == 0 || (chiSoTrangThai == 1 && dichVu.bat)
                        || (chiSoTrangThai == 2 && !dichVu.bat))
                .collect(Collectors.toList());
    }

    public List<String> danhSachNhom(List<DichVuData> danhSach) {
        LinkedHashSet<String> tapNhom = new LinkedHashSet<>();
        for (DichVuData dichVu : danhSach) {
            tapNhom.add(dichVu.nhom);
        }
        return new ArrayList<>(tapNhom);
    }

    public Statistics thongKe(List<DichVuData> danhSach) {
        long dangHoatDong = danhSach.stream().filter(dichVu -> dichVu.bat).count();
        long tong = danhSach.size();
        long giaTrungBinh = tong == 0 ? 0
                : Math.round(danhSach.stream().mapToLong(dichVu -> dichVu.gia).average().orElse(0));
        return new Statistics(tong, dangHoatDong, tong - dangHoatDong, giaTrungBinh, danhSachNhom(danhSach).size());
    }

    public String taoCsv(List<DichVuData> danhSach) {
        StringBuilder noiDungCsv = new StringBuilder("\uFEFFMã,Tên,Nhóm,Mô tả,Phút,Giá,Trạng thái\n");
        for (DichVuData dichVu : danhSach) {
            noiDungCsv.append(String.format("%s,\"%s\",\"%s\",\"%s\",%d,%d,%s\n",
                    dichVu.ma, dichVu.ten, dichVu.nhom, dichVu.moTa, dichVu.phut, dichVu.gia,
                    dichVu.bat ? "Đang áp dụng" : "Tạm ẩn"));
        }
        return noiDungCsv.toString();
    }

    private static boolean laDangHoatDong(String trangThai) {
        return trangThai != null
                && (trangThai.equalsIgnoreCase("Đang áp dụng") || trangThai.equalsIgnoreCase("Đang cung cấp"));
    }

    private static String nhomTheoTen(String ten) {
        if (ten == null) return "Dịch vụ chung";
        String chuThuong = ten.toLowerCase();
        if (chuThuong.contains("tắm")) return "Tắm & Vệ sinh";
        if (chuThuong.contains("cắt") || chuThuong.contains("tỉa") || chuThuong.contains("lông")) return "Cắt tỉa & Tạo kiểu";
        if (chuThuong.contains("tiêm") || chuThuong.contains("phòng") || chuThuong.contains("dại")) return "Tiêm phòng";
        if (chuThuong.contains("khám") || chuThuong.contains("chẩn đoán")) return "Khám bệnh";
        if (chuThuong.contains("răng") || chuThuong.contains("tai") || chuThuong.contains("móng")) return "Chăm sóc sức khỏe";
        return "Dịch vụ chung";
    }

    private static DichVu_entity chuyenSangEntity(DichVuData dichVu) {
        return new DichVu_entity(dichVu.ma, dichVu.ten, dichVu.moTa, dichVu.gia,
                LocalTime.of(dichVu.phut / 60, dichVu.phut % 60),
                dichVu.bat ? "Đang áp dụng" : "Tạm ẩn");
    }

    public static final class DichVuData {
        public String ma;
        public String ten;
        public String nhom;
        public String moTa;
        public int phut;
        public long gia;
        public boolean bat;

        public DichVuData(String ma, String ten, String nhom, String moTa, int phut, long gia,
                boolean hoatDong) {
            this.ma = ma;
            this.ten = ten;
            this.nhom = nhom;
            this.moTa = moTa;
            this.phut = phut;
            this.gia = gia;
            this.bat = hoatDong;
        }

        public DichVuData withPrice(long gia2) {
			// TODO Auto-generated method stub
			return null;
		}

		public DichVuData voiGia(long giaMoi) {
            return new DichVuData(ma, ten, nhom, moTa, phut, giaMoi, bat);
        }
    }

    public static final class Statistics {
        public final long tong;
        public final long dangHoatDong;
        public final long tamAn;
        public final long giaTrungBinh;
        public final int soNhom;

        private Statistics(long tong, long dangHoatDong, long tamAn, long giaTrungBinh, int soNhom) {
            this.tong = tong;
            this.dangHoatDong = dangHoatDong;
            this.tamAn = tamAn;
            this.giaTrungBinh = giaTrungBinh;
            this.soNhom = soNhom;
        }
    }
}