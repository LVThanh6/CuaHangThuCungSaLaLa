package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.ConnectDB;
import entity.NhaCungCap_entity;

public class NhaCungCap_DAO {

    // 1. Lấy danh sách tất cả nhà cung cấp
    public ArrayList<NhaCungCap_entity> getAllNhaCungCap() {
        ArrayList<NhaCungCap_entity> dsNCC = new ArrayList<>();

        String sql = "SELECT maNCC, tenNCC, diaChi, sdt, email "
                   + "FROM NhaCungCap";

        try {
            Connection con = ConnectDB.getCon();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                NhaCungCap_entity ncc = new NhaCungCap_entity(
                    rs.getString("maNCC"),
                    rs.getString("tenNCC"),
                    rs.getString("diaChi"),
                    rs.getString("sdt"),
                    rs.getString("email")
                );

                dsNCC.add(ncc);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsNCC;
    }

    // 2. Thêm nhà cung cấp
    public boolean createNhaCungCap(NhaCungCap_entity ncc) {
        String sql = "INSERT INTO NhaCungCap "
                   + "(maNCC, tenNCC, diaChi, sdt, email) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getCon();
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, ncc.getMaNCC());
            stmt.setString(2, ncc.getTenNCC());
            stmt.setString(3, ncc.getDiaChi());
            stmt.setString(4, ncc.getSdt());
            stmt.setString(5, ncc.getEmail());

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 3. Xóa nhà cung cấp theo mã
    public boolean deleteNhaCungCap(String maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE maNCC = ?";

        try {
            Connection con = ConnectDB.getCon();
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, maNCC);

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 4. Cập nhật nhà cung cấp
    public boolean updateNhaCungCap(NhaCungCap_entity ncc) {
        String sql = "UPDATE NhaCungCap "
                   + "SET tenNCC = ?, diaChi = ?, sdt = ?, email = ? "
                   + "WHERE maNCC = ?";

        try {
            Connection con = ConnectDB.getCon();
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, ncc.getTenNCC());
            stmt.setString(2, ncc.getDiaChi());
            stmt.setString(3, ncc.getSdt());
            stmt.setString(4, ncc.getEmail());
            stmt.setString(5, ncc.getMaNCC());

            int n = stmt.executeUpdate();
            stmt.close();

            return n > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 5. Tìm nhà cung cấp theo mã
    public NhaCungCap_entity getNhaCungCapTheoMa(String maNCC) {
        String sql = "SELECT maNCC, tenNCC, diaChi, sdt, email "
                   + "FROM NhaCungCap WHERE maNCC = ?";

        try {
            Connection con = ConnectDB.getCon();
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, maNCC);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                NhaCungCap_entity ncc = new NhaCungCap_entity(
                    rs.getString("maNCC"),
                    rs.getString("tenNCC"),
                    rs.getString("diaChi"),
                    rs.getString("sdt"),
                    rs.getString("email")
                );

                rs.close();
                stmt.close();

                return ncc;
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}