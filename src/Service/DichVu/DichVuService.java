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

    public List<DichVuData> load() {
        List<DichVuData> result = new ArrayList<>();
        List<DichVu_entity> services = java.util.Objects.requireNonNull(
                dao.getAllDichVu(), "Service data was not returned by the DAO");
        for (DichVu_entity service : services) {
            int minutes = 30;
            if (service.getThoiGian() != null) {
                minutes = service.getThoiGian().getHour() * 60 + service.getThoiGian().getMinute();
                if (minutes <= 0) {
                    minutes = 30;
                }
            }
            boolean active = isActive(service.getTrangThai());
            result.add(new DichVuData(
                    service.getMaDV(), service.getTenDV(), categoryFor(service.getTenDV()),
                    service.getMoTa() == null ? "" : service.getMoTa(),
                    minutes, Math.round(service.getGiaDV()), active));
        }
        return result;
    }

    public String nextCode() {
        return dao.getMaTiepTheo();
    }

    public DichVuData parseForm(String code, String name, String description, String minutes, String price,
            boolean active) {
        int duration;
        long amount;
        try {
            duration = Integer.parseInt(minutes.trim());
            amount = Long.parseLong(price.trim().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Thời gian hoặc giá không hợp lệ!", ex);
        }
        String normalizedName = name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống!");
        }
        if (duration <= 0 || amount < 0) {
            throw new IllegalArgumentException("Thời gian thực hiện phải > 0 và giá dịch vụ phải >= 0!");
        }
        return new DichVuData(code.trim(), normalizedName, categoryFor(normalizedName), description.trim(),
                duration, amount, active);
    }

    public long parsePrice(String value) {
        try {
            long amount = Long.parseLong(value.replaceAll("[^0-9]", ""));
            if (amount < 0) {
                throw new IllegalArgumentException("Giá nhập không hợp lệ!");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ!", ex);
        }
    }

    public boolean create(DichVuData service) {
        return dao.createDichVu(toEntity(service));
    }

    public boolean update(DichVuData service) {
        return dao.updateDichVu(toEntity(service));
    }

    public boolean updatePrice(DichVuData service, long price) {
        DichVuData updated = service.withPrice(price);
        return dao.updateDichVu(toEntity(updated));
    }

    public boolean updateStatus(String code, boolean active) {
        return dao.updateTrangThai(code, active ? "Đang áp dụng" : "Tạm ẩn");
    }

    public boolean delete(String code) {
        return dao.deleteDichVu(code);
    }

    public List<DichVuData> filter(List<DichVuData> services, String keyword, String category, int statusIndex) {
        String search = keyword.trim().toLowerCase();
        return services.stream()
                .filter(service -> (service.ma + " " + service.ten + " " + service.moTa)
                        .toLowerCase().contains(search))
                .filter(service -> category == null || category.equals("Tất cả") || category.equals(service.nhom))
                .filter(service -> statusIndex == 0 || (statusIndex == 1 && service.bat)
                        || (statusIndex == 2 && !service.bat))
                .collect(Collectors.toList());
    }

    public List<String> categories(List<DichVuData> services) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        for (DichVuData service : services) {
            categories.add(service.nhom);
        }
        return new ArrayList<>(categories);
    }

    public Statistics statistics(List<DichVuData> services) {
        long active = services.stream().filter(service -> service.bat).count();
        long total = services.size();
        long averagePrice = total == 0 ? 0
                : Math.round(services.stream().mapToLong(service -> service.gia).average().orElse(0));
        return new Statistics(total, active, total - active, averagePrice, categories(services).size());
    }

    public String toCsv(List<DichVuData> services) {
        StringBuilder csv = new StringBuilder("\uFEFFMã,Tên,Nhóm,Mô tả,Phút,Giá,Trạng thái\n");
        for (DichVuData service : services) {
            csv.append(String.format("%s,\"%s\",\"%s\",\"%s\",%d,%d,%s\n",
                    service.ma, service.ten, service.nhom, service.moTa, service.phut, service.gia,
                    service.bat ? "Đang áp dụng" : "Tạm ẩn"));
        }
        return csv.toString();
    }

    private static boolean isActive(String status) {
        return status != null
                && (status.equalsIgnoreCase("Đang áp dụng") || status.equalsIgnoreCase("Đang cung cấp"));
    }

    private static String categoryFor(String name) {
        if (name == null) return "Dịch vụ chung";
        String lower = name.toLowerCase();
        if (lower.contains("tắm")) return "Tắm & Vệ sinh";
        if (lower.contains("cắt") || lower.contains("tỉa") || lower.contains("lông")) return "Cắt tỉa & Tạo kiểu";
        if (lower.contains("tiêm") || lower.contains("phòng") || lower.contains("dại")) return "Tiêm phòng";
        if (lower.contains("khám") || lower.contains("chẩn đoán")) return "Khám bệnh";
        if (lower.contains("răng") || lower.contains("tai") || lower.contains("móng")) return "Chăm sóc sức khỏe";
        return "Dịch vụ chung";
    }

    private static DichVu_entity toEntity(DichVuData service) {
        return new DichVu_entity(service.ma, service.ten, service.moTa, service.gia,
                LocalTime.of(service.phut / 60, service.phut % 60),
                service.bat ? "Đang áp dụng" : "Tạm ẩn");
    }

    public static final class DichVuData {
        public String ma;
        public String ten;
        public String nhom;
        public String moTa;
        public int phut;
        public long gia;
        public boolean bat;

        public DichVuData(String code, String name, String category, String description, int minutes, long price,
                boolean active) {
            this.ma = code;
            this.ten = name;
            this.nhom = category;
            this.moTa = description;
            this.phut = minutes;
            this.gia = price;
            this.bat = active;
        }

        public DichVuData withPrice(long newPrice) {
            return new DichVuData(ma, ten, nhom, moTa, phut, newPrice, bat);
        }
    }

    public static final class Statistics {
        public final long total;
        public final long active;
        public final long inactive;
        public final long averagePrice;
        public final int categoryCount;

        private Statistics(long total, long active, long inactive, long averagePrice, int categoryCount) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
            this.averagePrice = averagePrice;
            this.categoryCount = categoryCount;
        }
    }
}
