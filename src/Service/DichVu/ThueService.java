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

    public List<Thue_entity> getAll() {
        return java.util.Objects.requireNonNull(dao.getAllThue(), "Tax data was not returned by the DAO");
    }

    public boolean save(String ma, String ten, String muc, boolean create) {
        String tenDaChuanHoa = ten.trim();
        if (tenDaChuanHoa.isEmpty()) {
            throw new IllegalArgumentException("Tên thuế không được để trống!");
        }
        double mucThue;
        try {
            mucThue = Double.parseDouble(muc.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mức thuế phải là số hợp lệ.", ex);
        }

        Thue_entity thue = new Thue_entity(ma.trim(), tenDaChuanHoa, mucThue);
        return create ? dao.createThue(thue) : dao.updateThue(thue);
    }

    public boolean delete(String ma) {
        return dao.deleteThue(ma);
    }
}
