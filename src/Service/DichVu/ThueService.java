package Service.DichVu;

import java.util.List;

import dao.DichVu.Thue_DAO;
import entity.DichVu.Thue_entity;

public class ThueService {
    private final Thue_DAO dao;

    public ThueService() {
        this(new Thue_DAO());
    }

    ThueService(Thue_DAO dao) {
        this.dao = dao;
    }

    public List<Thue_entity> layTatCa() {
        return java.util.Objects.requireNonNull(dao.getAllThue(), "Tax data was not returned by the DAO");
    }

    public boolean luu(String ma, String ten, String muc, boolean taoMoi) {
        String tenDaChuanHoa = ten.trim();
        if (tenDaChuanHoa.isEmpty()) {
            throw new IllegalArgumentException("Tên thuế không được để trống!");
        }
        double mucThue;
        try {
            mucThue = Double.parseDouble(muc.trim().replace(",", "."));
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Mức thuế phải là số hợp lệ.", loi);
        }

        Thue_entity thue = new Thue_entity(ma.trim(), tenDaChuanHoa, mucThue);
        return taoMoi ? dao.createThue(thue) : dao.updateThue(thue);
    }

    public boolean xoa(String ma) {
        return dao.deleteThue(ma);
    }
}