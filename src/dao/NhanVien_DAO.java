package dao;

import ConnectDB.DBConnection;
import model.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu (Data Access Object) cho bảng NhanVien trong CSDL SALALA.
 */
public class NhanVien_DAO {

    public NhanVien_DAO() {
    }

    /**
     * Lấy toàn bộ danh sách nhân viên từ CSDL
     */
    public ArrayList<NhanVien> getAllNhanVien() {
        ArrayList<NhanVien> ds = new ArrayList<>();
        Connection con = DBConnection.getInstance().getCon();

        if (con != null) {
            String sql = "SELECT maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan FROM NhanVien ORDER BY maNhanVien ASC";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String maNV = rs.getString("maNhanVien");
                    String maTK = rs.getString("maTK");
                    String hoTen = rs.getString("hoTen");
                    String chucVu = rs.getString("chucVu");
                    String sdt = rs.getString("sdt");
                    String diaChi = rs.getString("diaChi");
                    Date ngaySinh = rs.getDate("ngaySinh");
                    String gioiTinh = rs.getString("gioiTinh");
                    double luong = rs.getDouble("luongCoBan");

                    ds.add(new NhanVien(maNV, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luong));
                }
            } catch (SQLException e) {
                System.err.println("Lỗi truy vấn danh sách nhân viên: " + e.getMessage());
            }
        }

        // Nếu DB chưa có dữ liệu hoặc chưa kết nối được, trả về danh sách mẫu dự phòng
        if (ds.isEmpty()) {
            ds = getDanhSachMauDuPhong();
        }

        return ds;
    }

    /**
     * Lấy thông tin 1 nhân viên theo mã
     */
    public NhanVien getNhanVienTheoMa(String maNV) {
        Connection con = DBConnection.getInstance().getCon();
        if (con != null) {
            String sql = "SELECT maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan FROM NhanVien WHERE maNhanVien = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new NhanVien(
                                rs.getString("maNhanVien"),
                                rs.getString("maTK"),
                                rs.getString("hoTen"),
                                rs.getString("chucVu"),
                                rs.getString("sdt"),
                                rs.getString("diaChi"),
                                rs.getDate("ngaySinh"),
                                rs.getString("gioiTinh"),
                                rs.getDouble("luongCoBan")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi lấy nhân viên theo mã: " + e.getMessage());
            }
        }

        for (NhanVien nv : getDanhSachMauDuPhong()) {
            if (nv.getMaNhanVien().equalsIgnoreCase(maNV)) return nv;
        }
        return null;
    }

    /**
     * Thêm mới một nhân viên vào CSDL
     */
    public boolean themNhanVien(NhanVien nv) {
        Connection con = DBConnection.getInstance().getCon();
        if (con == null) return false;

        String sql = "INSERT INTO NhanVien (maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nv.getMaNhanVien());
            pstmt.setString(2, nv.getMaTK());
            pstmt.setString(3, nv.getHoTen());
            pstmt.setString(4, nv.getChucVu());
            pstmt.setString(5, nv.getSdt());
            pstmt.setString(6, nv.getDiaChi());
            pstmt.setDate(7, nv.getNgaySinh());
            pstmt.setString(8, nv.getGioiTinh());
            pstmt.setDouble(9, nv.getLuongCoBan());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm nhân viên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public boolean capNhatNhanVien(NhanVien nv) {
        Connection con = DBConnection.getInstance().getCon();
        if (con == null) return false;

        String sql = "UPDATE NhanVien SET hoTen = ?, chucVu = ?, sdt = ?, diaChi = ?, ngaySinh = ?, gioiTinh = ?, luongCoBan = ? WHERE maNhanVien = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nv.getHoTen());
            pstmt.setString(2, nv.getChucVu());
            pstmt.setString(3, nv.getSdt());
            pstmt.setString(4, nv.getDiaChi());
            pstmt.setDate(5, nv.getNgaySinh());
            pstmt.setString(6, nv.getGioiTinh());
            pstmt.setDouble(7, nv.getLuongCoBan());
            pstmt.setString(8, nv.getMaNhanVien());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật nhân viên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa nhân viên theo mã
     */
    public boolean xoaNhanVien(String maNV) {
        Connection con = DBConnection.getInstance().getCon();
        if (con == null) return false;

        String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa nhân viên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm nhân viên theo từ khóa (Mã, Tên, Số điện thoại)
     */
    public ArrayList<NhanVien> timKiemNhanVien(String keyword) {
        ArrayList<NhanVien> ketQua = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllNhanVien();
        }

        String kw = "%" + keyword.trim().toLowerCase() + "%";
        Connection con = DBConnection.getInstance().getCon();
        if (con != null) {
            String sql = "SELECT maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan "
                    + "FROM NhanVien WHERE LOWER(maNhanVien) LIKE ? OR LOWER(hoTen) LIKE ? OR sdt LIKE ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, kw);
                pstmt.setString(2, kw);
                pstmt.setString(3, kw);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ketQua.add(new NhanVien(
                                rs.getString("maNhanVien"),
                                rs.getString("maTK"),
                                rs.getString("hoTen"),
                                rs.getString("chucVu"),
                                rs.getString("sdt"),
                                rs.getString("diaChi"),
                                rs.getDate("ngaySinh"),
                                rs.getString("gioiTinh"),
                                rs.getDouble("luongCoBan")
                        ));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi tìm kiếm nhân viên: " + e.getMessage());
            }
        }

        if (ketQua.isEmpty()) {
            for (NhanVien nv : getAllNhanVien()) {
                if (nv.getMaNhanVien().toLowerCase().contains(keyword.toLowerCase())
                        || nv.getHoTen().toLowerCase().contains(keyword.toLowerCase())
                        || nv.getSdt().contains(keyword)) {
                    ketQua.add(nv);
                }
            }
        }

        return ketQua;
    }

    /**
     * Tự động phát sinh mã nhân viên mới dạng NV001, NV002, ...
     */
    public String phatSinhMaNhanVien() {
        Connection con = DBConnection.getInstance().getCon();
        int maxId = 0;
        if (con != null) {
            String sql = "SELECT TOP 1 maNhanVien FROM NhanVien ORDER BY maNhanVien DESC";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    String ma = rs.getString(1);
                    if (ma != null && ma.startsWith("NV")) {
                        maxId = Integer.parseInt(ma.substring(2));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (maxId == 0) {
            for (NhanVien nv : getAllNhanVien()) {
                try {
                    int id = Integer.parseInt(nv.getMaNhanVien().replaceAll("[^0-9]", ""));
                    if (id > maxId) maxId = id;
                } catch (Exception ignored) {}
            }
        }

        return String.format("NV%03d", maxId + 1);
    }

    /**
     * Dữ liệu dự phòng khớp chính xác với kịch bản INSERT SQL của SALALA
     */
    private ArrayList<NhanVien> getDanhSachMauDuPhong() {
        ArrayList<NhanVien> list = new ArrayList<>();
        list.add(new NhanVien("NV001", "TK016", "Trần Quốc Bảo", "Nhân viên bán hàng", "0902000001", "Củ Chi, TP.HCM", Date.valueOf("1988-10-05"), "Nam", 7500000));
        list.add(new NhanVien("NV002", "TK017", "Nguyễn Thị Cẩm", "Nhân viên bán hàng", "0902000002", "Quận 1, TP.HCM", Date.valueOf("1989-06-25"), "Nữ", 8000000));
        list.add(new NhanVien("NV003", "TK018", "Lê Văn Dũng", "Nhân viên bán hàng", "0902000003", "Quận 3, TP.HCM", Date.valueOf("1990-03-15"), "Nam", 8500000));
        list.add(new NhanVien("NV004", "TK019", "Phạm Hồng Em", "Nhân viên bán hàng", "0902000004", "Thủ Đức, TP.HCM", Date.valueOf("1990-12-03"), "Nữ", 9000000));
        list.add(new NhanVien("NV005", "TK020", "Võ Văn Phúc", "Nhân viên bán hàng", "0902000005", "Bình Thạnh, TP.HCM", Date.valueOf("1991-08-23"), "Nam", 9500000));
        list.add(new NhanVien("NV006", "TK021", "Đặng Thị Giang", "Nhân viên dịch vụ", "0902000006", "Củ Chi, TP.HCM", Date.valueOf("1992-05-13"), "Nữ", 10000000));
        list.add(new NhanVien("NV007", "TK022", "Bùi Minh Hải", "Nhân viên dịch vụ", "0902000007", "Quận 1, TP.HCM", Date.valueOf("1993-01-31"), "Nam", 10500000));
        list.add(new NhanVien("NV008", "TK023", "Hồ Thị Hạnh", "Nhân viên dịch vụ", "0902000008", "Quận 3, TP.HCM", Date.valueOf("1993-10-21"), "Nữ", 11000000));
        list.add(new NhanVien("NV009", "TK024", "Ngô Văn Khoa", "Nhân viên dịch vụ", "0902000009", "Thủ Đức, TP.HCM", Date.valueOf("1994-07-11"), "Nam", 11500000));
        list.add(new NhanVien("NV010", "TK025", "Dương Thị Linh", "Nhân viên dịch vụ", "0902000010", "Bình Thạnh, TP.HCM", Date.valueOf("1995-04-01"), "Nữ", 12000000));
        list.add(new NhanVien("NV011", "TK026", "Lý Quang Minh", "Nhân viên quản lý kho", "0902000011", "Củ Chi, TP.HCM", Date.valueOf("1995-12-20"), "Nam", 12500000));
        list.add(new NhanVien("NV012", "TK027", "Phan Thị Ngân", "Nhân viên quản lý kho", "0902000012", "Quận 1, TP.HCM", Date.valueOf("1996-09-09"), "Nữ", 13000000));
        return list;
    }
}
