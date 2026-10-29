
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.BacSi_entity;
import entity.ChiTietHoaDon_entity;
import entity.HoaDon_entity;
import entity.DichVu_entity;
import entity.NhanVien_entity;
import entity.VatTu_entity;

public class ChiTietHoaDon_DAO {

    private final HoaDon_Dao hdDAO = new HoaDon_Dao();
    private final DichVu_DAO dvDAO = new DichVu_DAO();
    private final VatTu_DAO vtDAO = new VatTu_DAO();

    // 1. Lấy toàn bộ danh sách chi tiết hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getAllChiTietHoaDon() {
        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia
            FROM ChiTietHoaDon
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsCTHD;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maHD = rs.getString("maHD");
                    String maDV = rs.getString("maDV");
                    String maVT = rs.getString("maVT");
                    String maNhanVien = rs.getString("maNhanVien");
                    String maBacSi = rs.getString("maBacSi");

                    HoaDon_entity hd =
                        hdDAO.getHDTheoMa(maHD);

                    DichVu_entity dv = maDV != null
                        ? dvDAO.getDichVuTheoMa(maDV)
                        : null;

                    VatTu_entity vt = maVT != null
                        ? vtDAO.getVatTuTheoMa(maVT)
                        : null;

                    NhanVien_entity nv = maNhanVien != null ? new NhanVien_DAO().getNhanVienTheoMa(maNhanVien) : null;
                    BacSi_entity bs = maBacSi != null ? new BacSi_DAO().getAll().stream().filter(x -> x.getMaBacSi().equals(maBacSi)).findFirst().orElse(null) : null;

                    ChiTietHoaDon_entity cthd =
                        new ChiTietHoaDon_entity(
                            rs.getString("maCTHD"),
                            hd,
                            dv,
                            vt,
                            nv,
                            bs,
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                        );

                    dsCTHD.add(cthd);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    private void resolveReferences(ChiTietHoaDon_entity ct) {
        if (ct == null) {
            return;
        }

        if (ct.getMaHD() == null && ct.getMaDVString() == null && ct.getMaVTString() == null) {
            return;
        }

        String maDVCode = ct.getMaDVString();
        String maVTCode = ct.getMaVTString();

        if (ct.getMaDV() == null && maDVCode != null && !maDVCode.isBlank()) {
            ct.setMaDV(dvDAO.getDichVuTheoMa(maDVCode));
        }

        if (ct.getMaVT() == null && maVTCode != null && !maVTCode.isBlank()) {
            ct.setMaVT(vtDAO.getVatTuTheoMa(maVTCode));
        }

        if (ct.getMaDV() != null) {
            ct.setMaDVCode(ct.getMaDV().getMaDV());
            ct.setMaVT(null);
            ct.setMaVTCode(null);
        }

        if (ct.getMaVT() != null) {
            ct.setMaVTCode(ct.getMaVT().getMaVT());
            ct.setMaDV(null);
            ct.setMaDVCode(null);
        }
    }

    // 2. Thêm chi tiết hóa đơn
    public boolean createChiTietHoaDon(
            ChiTietHoaDon_entity ct) {

        if (ct == null) {
            System.err.println("[ChiTietHoaDon_DAO] createChiTietHoaDon: ct == null");
            return false;
        }

        resolveReferences(ct);

        if (ct.getMaHD() == null) {
            System.err.println("[ChiTietHoaDon_DAO] createChiTietHoaDon: maHD null");
            return false;
        }

        if (!duLieuHopLe(ct)) {
            System.err.println("[ChiTietHoaDon_DAO] Dữ liệu chi tiết hóa đơn không hợp lệ: maCTHD=" + ct.getMaCTHD()
                    + ", maHD=" + (ct.getMaHD() == null ? "null" : ct.getMaHD().getMaHD())
                    + ", maDV=" + ct.getMaDVString() + ", maVT=" + ct.getMaVTString() + ", soLuong=" + ct.getSoLuong());
            return false;
        }

        String sql = """
            INSERT INTO ChiTietHoaDon
                (maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            if (ct.getMaCTHD() != null && ct.getMaCTHD().length() > 20) {
                System.err.println("[ChiTietHoaDon_DAO] maCTHD vượt quá 20 ký tự: " + ct.getMaCTHD());
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ct.getMaCTHD());
                ps.setString(2, ct.getMaHD().getMaHD());

                String maDV = ct.getMaDV() != null ? ct.getMaDV().getMaDV() : ct.getMaDVString();
                String maVT = ct.getMaVT() != null ? ct.getMaVT().getMaVT() : ct.getMaVTString();

                if (maDV != null && !maDV.isBlank()) {
                    ps.setString(3, maDV);
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                    ps.setString(4, maVT);
                }

                if (ct.getNhanVienThucHien() != null) {
                    ps.setString(5, ct.getNhanVienThucHien().getMaNhanVien());
                    ps.setNull(6, java.sql.Types.VARCHAR);
                } else if (ct.getBacSiThucHien() != null) {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.setString(6, ct.getBacSiThucHien().getMaBacSi());
                } else {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.setNull(6, java.sql.Types.VARCHAR);
                }
                ps.setInt(7, ct.getSoLuong());
                ps.setDouble(8, ct.getDonGia());

                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    System.err.println("[ChiTietHoaDon_DAO] INSERT chi tiết trả về 0 dòng: " + ct.getMaCTHD());
                    return false;
                }
                System.out.println("[ChiTietHoaDon_DAO] INSERT OK: maCTHD=" + ct.getMaCTHD() + ", maHD=" + ct.getMaHD().getMaHD() + ", maDV=" + maDV + ", maVT=" + maVT);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[ChiTietHoaDon_DAO] Lỗi khi insert chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // 3. Xóa chi tiết hóa đơn theo mã
    public boolean deleteChiTietHoaDon(String maCTHD) {
        String sql =
            "DELETE FROM ChiTietHoaDon WHERE maCTHD = ?";

        try {
            Connection con = ConnectDB.getCon();

            if (con == null || maCTHD == null
                    || maCTHD.isBlank()) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCTHD);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 4. Cập nhật chi tiết hóa đơn
    public boolean updateChiTietHoaDon(
            ChiTietHoaDon_entity ct) {

        if (!duLieuHopLe(ct)) {
            return false;
        }

        String sql = """
            UPDATE ChiTietHoaDon
            SET maHD = ?, maDV = ?, maVT = ?, maNhanVien = ?, maBacSi = ?,
                soLuong = ?, donGia = ?
            WHERE maCTHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ct.getMaHD().getMaHD());

                if (ct.getMaDV() != null) {
                    ps.setString(2, ct.getMaDV().getMaDV());
                    ps.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                    ps.setString(3, ct.getMaVT().getMaVT());
                }

                if (ct.getNhanVienThucHien() != null) {
                    ps.setString(4, ct.getNhanVienThucHien().getMaNhanVien());
                    ps.setNull(5, java.sql.Types.VARCHAR);
                } else if (ct.getBacSiThucHien() != null) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                    ps.setString(5, ct.getBacSiThucHien().getMaBacSi());
                } else {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                    ps.setNull(5, java.sql.Types.VARCHAR);
                }
                ps.setInt(6, ct.getSoLuong());
                ps.setDouble(7, ct.getDonGia());
                ps.setString(8, ct.getMaCTHD());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 5. Tìm chi tiết hóa đơn theo mã
    public ChiTietHoaDon_entity getChiTietHoaDonTheoMa(
            String maCTHD) {

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia
            FROM ChiTietHoaDon
            WHERE maCTHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCTHD);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return taoChiTietHoaDon(rs);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 6. Lấy danh sách chi tiết theo mã hóa đơn
    public ArrayList<ChiTietHoaDon_entity> getChiTietHoaDonTheoMaHD(
            String maHD) {

        ArrayList<ChiTietHoaDon_entity> dsCTHD = new ArrayList<>();

        String sql = """
            SELECT maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia
            FROM ChiTietHoaDon
            WHERE maHD = ?
            """;

        try {
            Connection con = ConnectDB.getCon();

            if (con == null) {
                return dsCTHD;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dsCTHD.add(taoChiTietHoaDon(rs));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }

    // 7. Chuyển một dòng ResultSet thành đối tượng entity
    private ChiTietHoaDon_entity taoChiTietHoaDon(
            ResultSet rs) throws SQLException {

        String maHD = rs.getString("maHD");
        String maDV = rs.getString("maDV");
        String maVT = rs.getString("maVT");
        String maNhanVien = rs.getString("maNhanVien");
        String maBacSi = rs.getString("maBacSi");

        HoaDon_entity hd = hdDAO.getHDTheoMa(maHD);

        DichVu_entity dv = maDV != null ? dvDAO.getDichVuTheoMa(maDV) : null;
        if (dv == null && maDV != null && !maDV.isBlank()) {
            for (DichVu_entity item : dvDAO.getAllDichVu()) {
                if (maDV.equals(item.getMaDV())) {
                    dv = item;
                    break;
                }
            }
        }

        VatTu_entity vt = maVT != null ? vtDAO.getVatTuTheoMa(maVT) : null;
        if (vt == null && maVT != null && !maVT.isBlank()) {
            for (VatTu_entity item : vtDAO.getAllVatTu()) {
                if (maVT.equals(item.getMaVT())) {
                    vt = item;
                    break;
                }
            }
        }

        NhanVien_entity nv = maNhanVien != null ? new NhanVien_DAO().getNhanVienTheoMa(maNhanVien) : null;
        BacSi_entity bs = maBacSi != null ? new BacSi_DAO().getAll().stream().filter(x -> x.getMaBacSi().equals(maBacSi)).findFirst().orElse(null) : null;

        ChiTietHoaDon_entity entity = new ChiTietHoaDon_entity(
            rs.getString("maCTHD"),
            hd,
            dv,
            vt,
            nv,
            bs,
            rs.getInt("soLuong"),
            rs.getDouble("donGia")
        );
        if (dv != null) {
            entity.setMaDV(dv);
        } else if (maDV != null && !maDV.isBlank()) {
            entity.setMaDVCode(maDV);
        }
        if (vt != null) {
            entity.setMaVT(vt);
        } else if (maVT != null && !maVT.isBlank()) {
            entity.setMaVTCode(maVT);
        }
        return entity;
    }

    // 8. Kiểm tra dữ liệu trước khi thêm hoặc cập nhật
    private boolean duLieuHopLe(ChiTietHoaDon_entity ct) {
        if (ct == null
                || ct.getMaCTHD() == null
                || ct.getMaCTHD().isBlank()
                || ct.getMaHD() == null
                || ct.getSoLuong() <= 0
                || ct.getDonGia() < 0) {
            return false;
        }

        resolveReferences(ct);

        String maDV = ct.getMaDVString();
        String maVT = ct.getMaVTString();
        boolean coDichVu = maDV != null && !maDV.isBlank();
        boolean coVatTu = maVT != null && !maVT.isBlank();

        if (coDichVu && ct.getMaDV() == null) {
            ct.setMaDV(dvDAO.getDichVuTheoMa(maDV));
        }
        if (coVatTu && ct.getMaVT() == null) {
            ct.setMaVT(vtDAO.getVatTuTheoMa(maVT));
        }

        return (coDichVu ^ coVatTu) && (ct.getMaDV() != null || ct.getMaVT() != null);
    }
}
