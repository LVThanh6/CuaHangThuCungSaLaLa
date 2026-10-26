package GUI.LichHen;

import GUI.Chung.TableMau;
import GUI.Chung.RangBuocGiaoDien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Class XemLichGUI: Giao diện "Lịch khám sức khỏe" (Xem lịch) của hệ thống SALALA Pet Shop.
 * Bố cục chuẩn thiết kế mới:
 * 1. Phần đầu trang: đường dẫn, tiêu đề, mô tả.
 * 2. Hàng thẻ thống kê nhanh (hôm nay, chờ khám, đang khám, đã khám / đã hủy).
 * 3. Thẻ danh sách: 
 *    - Thanh bộ lọc: bộ lọc ngày, bác sĩ, trạng thái (đã loại bỏ cụm Tháng|Tuần|Danh sách).
 *    - Cụm nút thao tác độc lập phía trên bảng: [+ Tạo lịch khám], [Xem chi tiết], [Sửa lịch], [Hủy lịch].
 *    - Bảng lịch khám (loại bỏ cột Thao tác cũ, dùng ListSelectionListener điều khiển các nút).
 *    - Chân bảng phân trang.
 */
public class XemLichGUI extends JPanel {

    // =========================================================================
    // 1. HẰNG SỐ KÍCH THƯỚC & DỮ LIỆU MẪU
    // =========================================================================
    private static final int BAN_KINH_THE = 12;
    private static final int BAN_KINH_O = 8;
    private static final int CHIEU_CAO_O = 38;
    private static final int LE_TRANG = 20;
    private static final int KHOANG_CACH = 16;

    private static final int COT_THOI_GIAN = 0;
    private static final int COT_TRANG_THAI = 5;

    // Loại bỏ cột Thao tác - Bảng gồm 6 cột tinh gọn
    private static final String[] TIEU_DE_BANG = {
            "Thời gian", "Tên thú cưng", "Chủ nuôi", "Bác sĩ", "Nội dung khám", "Trạng thái"
    };
    private static final int[] DO_RONG_COT = { 170, 160, 220, 190, 320, 150 };

    // Dữ liệu mẫu minh họa không có cột thao tác
    private static final Object[][] DU_LIEU_MAU = {
            { "05/10/2026 · 09:00", "Bánh Bao", "Nguyễn Hoàng Nam", "BS. Lê Minh", "Tái khám viêm da dị ứng", "Chờ khám" },
            { "05/10/2026 · 09:30", "Milu", "Nguyễn Văn A", "BS. Bùi Anh Tuấn", "Khám tổng quát", "Đang khám" },
            { "05/10/2026 · 08:00", "Miu Miu", "Lê Văn D", "BS. Nguyễn Thảo Vy", "Kiểm tra da và lông", "Đã khám" },
            { "05/10/2026 · 11:00", "Cục Bông", "Trần Thị Mai", "BS. Lê Minh", "Khám định kỳ", "Đã hủy" },
            { "06/10/2026 · 09:00", "Khoai Lang", "Phạm Quốc Hưng", "BS. Bùi Anh Tuấn", "Tiêm phòng nhắc lại", "Chờ khám" },
            { "07/10/2026 · 10:00", "Bơ", "Hoàng Văn Tú", "BS. Nguyễn Thảo Vy", "Khám tổng quát", "Chờ khám" },
            { "08/10/2026 · 14:00", "Đậu Đậu", "Nguyễn Hoàng Nam", "BS. Lê Minh", "Tư vấn dinh dưỡng", "Chờ khám" },
            { "09/10/2026 · 15:00", "Bông Tuyết", "Trần Thị Mai", "BS. Bùi Anh Tuấn", "Kiểm tra răng miệng", "Chờ khám" }
    };

    private static final String[] DANH_SACH_BAC_SI = {
            "Bác sĩ: Tất cả", "BS. Lê Minh", "BS. Bùi Anh Tuấn", "BS. Nguyễn Thảo Vy"
    };
    private static final String[] DANH_SACH_TRANG_THAI = {
            "Trạng thái: Tất cả", "Chờ khám", "Đang khám", "Đã khám", "Đã hủy"
    };
    private static final String[] DANH_SACH_SO_DONG = { "8", "16", "32" };

    // =========================================================================
    // 2. CÁC THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    // --- Phần đầu trang ---
    private JPanel panelPhanTren;
    private JPanel panelDauTrang;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;

    // --- Hàng thẻ thống kê ---
    private JPanel panelThongKe;
    private TheThongKe theLichHomNay;
    private TheThongKe theChoKham;
    private TheThongKe theDangKham;
    private TheThongKe theDaKhamDaHuy;

    // --- Thẻ danh sách ---
    private TheBoGoc theDanhSach;

    // Thanh bộ lọc & cụm nút thao tác độc lập
    private JPanel panelThanhCongCu;
    private TheBoGoc oChonNgay;
    private JLabel nhanNgayLoc;
    private ComboHienDai cboBacSi;
    private ComboHienDai cboTrangThai;

    // Cụm nút thao tác mới đưa lên thanh công cụ
    private NutBo nutTaoLichKham;
    private NutBo nutXemChiTiet;
    private NutBo nutSuaLich;
    private NutBo nutHuyLich;

    // Bảng lịch khám
    private TableMau bangLichKham;
    private JScrollPane khungCuonBang;

    // Chân bảng + phân trang
    private JPanel panelChanBang;
    private JLabel nhanThongTinHienThi;
    private JLabel nhanGoiYChon;
    private JLabel nhanSoDongTrang;
    private ComboHienDai cboSoDong;
    private NutBo nutTrangTruoc;
    private NutBo nutTrang1;
    private NutBo nutTrang2;
    private NutBo nutTrang3;
    private NutBo nutTrangCham;
    private NutBo nutTrangSau;

    // =========================================================================
    // 3. KHỞI TẠO
    // =========================================================================
    public XemLichGUI() {
        khoiTaoGiaoDien();
        ganSuKien();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, KHOANG_CACH));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(BorderFactory.createEmptyBorder(16, LE_TRANG, 20, LE_TRANG));

        // Phần trên: đầu trang + hàng thống kê
        panelPhanTren = new JPanel(new BorderLayout(0, KHOANG_CACH));
        panelPhanTren.setOpaque(false);
        panelPhanTren.add(taoPhanDauTrang(), BorderLayout.NORTH);
        panelPhanTren.add(taoHangThongKe(), BorderLayout.CENTER);
        add(panelPhanTren, BorderLayout.NORTH);

        // Phần giữa: thẻ danh sách lịch khám
        add(taoTheDanhSach(), BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // PHẦN ĐẦU TRANG
    // -------------------------------------------------------------------------
    private JPanel taoPhanDauTrang() {
        panelDauTrang = new JPanel(new BorderLayout(0, 8));
        panelDauTrang.setOpaque(false);

        nhanDuongDan = new JLabel("Lịch hẹn / Lịch khám sức khỏe");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Lịch khám sức khỏe");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Danh sách lịch khám từ 05/10 đến 09/10/2026 · Theo dõi tiến độ khám và tái khám.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel khoiTieuDe = new JPanel(new BorderLayout(0, 2));
        khoiTieuDe.setOpaque(false);
        khoiTieuDe.add(nhanTieuDe, BorderLayout.NORTH);
        khoiTieuDe.add(nhanMoTa, BorderLayout.CENTER);

        panelDauTrang.add(nhanDuongDan, BorderLayout.NORTH);
        panelDauTrang.add(khoiTieuDe, BorderLayout.CENTER);
        return panelDauTrang;
    }

    // -------------------------------------------------------------------------
    // HÀNG THẺ THỐNG KÊ (Sử dụng Text/Emoji thay cho Graphics2D icon)
    // -------------------------------------------------------------------------
    private JPanel taoHangThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH, 0));
        panelThongKe.setOpaque(false);

        theLichHomNay = new TheThongKe("📅", "Lịch khám hôm nay", "4", "Ngày 05/10/2026");
        theChoKham = new TheThongKe("⏱", "Chờ khám", "1", "Chưa tiếp nhận");
        theDangKham = new TheThongKe("🩺", "Đang khám", "1", "Đang xử lý");
        theDaKhamDaHuy = new TheThongKe("📋", "Đã khám / đã hủy", "1 / 1", "Trong hôm nay");

        panelThongKe.add(theLichHomNay);
        panelThongKe.add(theChoKham);
        panelThongKe.add(theDangKham);
        panelThongKe.add(theDaKhamDaHuy);
        return panelThongKe;
    }

    // -------------------------------------------------------------------------
    // THẺ DANH SÁCH (BỘ LỌC + BẢNG + PHÂN TRANG)
    // -------------------------------------------------------------------------
    private TheBoGoc taoTheDanhSach() {
        theDanhSach = new TheBoGoc(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        theDanhSach.add(taoThanhCongCu(), BorderLayout.NORTH);
        theDanhSach.add(taoBang(), BorderLayout.CENTER);
        theDanhSach.add(taoChanBang(), BorderLayout.SOUTH);
        return theDanhSach;
    }

    private JPanel taoThanhCongCu() {
        panelThanhCongCu = new JPanel(new BorderLayout(16, 0));
        panelThanhCongCu.setOpaque(false);
        panelThanhCongCu.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // --- Ô chọn khoảng ngày & bộ lọc ---
        oChonNgay = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        oChonNgay.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        oChonNgay.setPreferredSize(new Dimension(240, CHIEU_CAO_O));
        nhanNgayLoc = new JLabel("Ngày: 05/10 – 09/10/2026");
        nhanNgayLoc.setFont(RangBuocGiaoDien.fontThuong(13));
        nhanNgayLoc.setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
        oChonNgay.add(nhanNgayLoc, BorderLayout.CENTER);

        JLabel lblIconLich = new JLabel("📅");
        lblIconLich.setFont(RangBuocGiaoDien.fontThuong(13));
        lblIconLich.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        oChonNgay.add(lblIconLich, BorderLayout.EAST);

        // --- Combo bác sĩ & trạng thái ---
        cboBacSi = new ComboHienDai(DANH_SACH_BAC_SI, 190, CHIEU_CAO_O);
        cboTrangThai = new ComboHienDai(DANH_SACH_TRANG_THAI, 175, CHIEU_CAO_O);

        JPanel nhomBoLoc = new JPanel();
        nhomBoLoc.setOpaque(false);
        nhomBoLoc.setLayout(new BoxLayout(nhomBoLoc, BoxLayout.X_AXIS));
        nhomBoLoc.add(oChonNgay);
        nhomBoLoc.add(Box.createHorizontalStrut(10));
        nhomBoLoc.add(cboBacSi);
        nhomBoLoc.add(Box.createHorizontalStrut(10));
        nhomBoLoc.add(cboTrangThai);

        // --- CỤM NÚT THAO TÁC MỚI (Thay thế hoàn toàn cụm Tháng|Tuần|Danh sách cũ) ---
        JPanel panelThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelThaoTac.setOpaque(false);

        nutTaoLichKham = new NutBo("+ Tạo lịch khám", NutBo.KIEU_CHINH);
        nutTaoLichKham.setPreferredSize(new Dimension(145, CHIEU_CAO_O));

        nutXemChiTiet = new NutBo("Xem chi tiết", NutBo.KIEU_VIEN);
        nutXemChiTiet.setPreferredSize(new Dimension(115, CHIEU_CAO_O));
        nutXemChiTiet.setEnabled(false);

        nutSuaLich = new NutBo("Sửa lịch", NutBo.KIEU_VIEN);
        nutSuaLich.setPreferredSize(new Dimension(95, CHIEU_CAO_O));
        nutSuaLich.setEnabled(false);

        nutHuyLich = new NutBo("Hủy lịch", NutBo.KIEU_NGUY_HIEM);
        nutHuyLich.setPreferredSize(new Dimension(95, CHIEU_CAO_O));
        nutHuyLich.setEnabled(false);

        panelThaoTac.add(nutTaoLichKham);
        panelThaoTac.add(nutXemChiTiet);
        panelThaoTac.add(nutSuaLich);
        panelThaoTac.add(nutHuyLich);

        panelThanhCongCu.add(nhomBoLoc, BorderLayout.WEST);
        panelThanhCongCu.add(panelThaoTac, BorderLayout.EAST);
        return panelThanhCongCu;
    }

    private JScrollPane taoBang() {
        bangLichKham = new TableMau(TIEU_DE_BANG, DU_LIEU_MAU);
        bangLichKham.getTableHeader().setDefaultRenderer(new RendererTieuDe());
        bangLichKham.getTableHeader().setPreferredSize(new Dimension(0, 42));
        bangLichKham.setIntercellSpacing(new Dimension(0, 1));

        for (int i = 0; i < DO_RONG_COT.length; i++) {
            bangLichKham.getColumnModel().getColumn(i).setPreferredWidth(DO_RONG_COT[i]);
        }
        bangLichKham.getColumnModel().getColumn(COT_THOI_GIAN)
                .setCellRenderer(new RendererVanBanMau(RangBuocGiaoDien.MAU_LIEN_KET));
        bangLichKham.getColumnModel().getColumn(COT_TRANG_THAI)
                .setCellRenderer(new RendererTrangThai());

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt các nút thao tác
        bangLichKham.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangLichKham.getSelectedRow() != -1;
                nutXemChiTiet.setEnabled(coChon);
                nutSuaLich.setEnabled(coChon);
                nutHuyLich.setEnabled(coChon);
            }
        });

        khungCuonBang = new JScrollPane(bangLichKham);
        khungCuonBang.setBorder(null);
        khungCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        khungCuonBang.getVerticalScrollBar().setUnitIncrement(16);
        return khungCuonBang;
    }

    private JPanel taoChanBang() {
        panelChanBang = new JPanel(new BorderLayout());
        panelChanBang.setOpaque(false);
        panelChanBang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // --- Bên trái: thông tin hiển thị ---
        nhanThongTinHienThi = new JLabel("Hiển thị 1–8 trong 8 lịch khám");
        nhanThongTinHienThi.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanGoiYChon = new JLabel("Chọn một lịch khám để Xem chi tiết, Sửa lịch hoặc Hủy lịch.");
        nhanGoiYChon.setFont(RangBuocGiaoDien.fontThuong(11));
        nhanGoiYChon.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JPanel khoiTrai = new JPanel(new BorderLayout(0, 2));
        khoiTrai.setOpaque(false);
        khoiTrai.add(nhanThongTinHienThi, BorderLayout.NORTH);
        khoiTrai.add(nhanGoiYChon, BorderLayout.CENTER);

        // --- Bên phải: số dòng / trang + nút phân trang ---
        nhanSoDongTrang = new JLabel("Số dòng / trang");
        nhanSoDongTrang.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanSoDongTrang.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        cboSoDong = new ComboHienDai(DANH_SACH_SO_DONG, 66, 34);

        nutTrangTruoc = taoNutTrang("<");
        nutTrang1 = taoNutTrang("1");
        nutTrang2 = taoNutTrang("2");
        nutTrang3 = taoNutTrang("3");
        nutTrangCham = taoNutTrang("...");
        nutTrangSau = taoNutTrang(">");
        nutTrang1.datChon(true);

        ActionListener chonTrang = e -> {
            nutTrang1.datChon(e.getSource() == nutTrang1);
            nutTrang2.datChon(e.getSource() == nutTrang2);
            nutTrang3.datChon(e.getSource() == nutTrang3);
        };
        nutTrang1.addActionListener(chonTrang);
        nutTrang2.addActionListener(chonTrang);
        nutTrang3.addActionListener(chonTrang);

        JPanel khoiPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        khoiPhai.setOpaque(false);
        khoiPhai.add(nhanSoDongTrang);
        khoiPhai.add(cboSoDong);
        khoiPhai.add(Box.createHorizontalStrut(6));
        khoiPhai.add(nutTrangTruoc);
        khoiPhai.add(nutTrang1);
        khoiPhai.add(nutTrang2);
        khoiPhai.add(nutTrang3);
        khoiPhai.add(nutTrangCham);
        khoiPhai.add(nutTrangSau);

        panelChanBang.add(khoiTrai, BorderLayout.WEST);
        panelChanBang.add(khoiPhai, BorderLayout.EAST);
        return panelChanBang;
    }

    private NutBo taoNutTrang(String chu) {
        NutBo nut = new NutBo(chu, NutBo.KIEU_VIEN);
        nut.setFont(RangBuocGiaoDien.fontVua(12));
        nut.setPreferredSize(new Dimension(32, 32));
        return nut;
    }

    // =========================================================================
    // 4. XỬ LÝ SỰ KIỆN CÁC NÚT THAO TÁC MỚI
    // =========================================================================
    private void ganSuKien() {
        nutTaoLichKham.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Mở chức năng lập lịch khám mới cho thú cưng.",
                    "Tạo lịch khám",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        nutXemChiTiet.addActionListener(e -> hienThiDialogChiTietLich());
        nutSuaLich.addActionListener(e -> hienThiDialogSuaLich());
        nutHuyLich.addActionListener(e -> xuLyHuyLich());
    }

    /**
     * Mở Dialog hiển thị chi tiết lịch khám khi chọn dòng
     */
    private void hienThiDialogChiTietLich() {
        int row = bangLichKham.getSelectedRow();
        if (row == -1) return;

        String thoiGian = String.valueOf(bangLichKham.getValueAt(row, 0));
        String thuCung = String.valueOf(bangLichKham.getValueAt(row, 1));
        String chuNuoi = String.valueOf(bangLichKham.getValueAt(row, 2));
        String bacSi = String.valueOf(bangLichKham.getValueAt(row, 3));
        String noiDung = String.valueOf(bangLichKham.getValueAt(row, 4));
        String trangThai = String.valueOf(bangLichKham.getValueAt(row, 5));

        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết lịch khám - " + thuCung, true);
        dialog.setSize(520, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        pnlNoiDung.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("THÔNG TIN LỊCH KHÁM CHI TIẾT");
        lblHeader.setFont(RangBuocGiaoDien.fontDam(16));
        lblHeader.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlNoiDung.add(lblHeader);
        pnlNoiDung.add(Box.createVerticalStrut(16));
        pnlNoiDung.add(taoDongChiTiet("Thời gian hẹn:", thoiGian));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Tên thú cưng:", thuCung));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Chủ nuôi:", chuNuoi));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Bác sĩ phụ trách:", bacSi));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Nội dung khám:", noiDung));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Trạng thái:", trangThai));

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlBottom.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(RangBuocGiaoDien.fontVua(13));
        btnDong.setPreferredSize(new Dimension(90, 36));
        btnDong.addActionListener(e -> dialog.dispose());
        pnlBottom.add(btnDong);

        dialog.add(pnlNoiDung, BorderLayout.CENTER);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel taoDongChiTiet(String nhan, String giaTri) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(RangBuocGiaoDien.fontDam(13));
        lblNhan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblNhan.setPreferredSize(new Dimension(140, 24));

        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(RangBuocGiaoDien.fontThuong(13));
        lblGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        p.add(lblNhan, BorderLayout.WEST);
        p.add(lblGiaTri, BorderLayout.CENTER);
        return p;
    }

    /**
     * Mở Dialog chỉnh sửa thông tin lịch khám
     */
    private void hienThiDialogSuaLich() {
        int row = bangLichKham.getSelectedRow();
        if (row == -1) return;

        String thoiGianHienTai = String.valueOf(bangLichKham.getValueAt(row, 0));
        String thuCung = String.valueOf(bangLichKham.getValueAt(row, 1));
        String bacSiHienTai = String.valueOf(bangLichKham.getValueAt(row, 3));
        String noiDungHienTai = String.valueOf(bangLichKham.getValueAt(row, 4));

        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Sửa lịch khám - " + thuCung, true);
        dialog.setSize(480, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 14));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(new JLabel("Thời gian khám:"));
        JTextField txtThoiGian = new JTextField(thoiGianHienTai);
        pnlForm.add(txtThoiGian);

        pnlForm.add(new JLabel("Bác sĩ phụ trách:"));
        JComboBox<String> cboBs = new JComboBox<>(new String[] { "BS. Lê Minh", "BS. Bùi Anh Tuấn", "BS. Nguyễn Thảo Vy" });
        cboBs.setSelectedItem(bacSiHienTai);
        pnlForm.add(cboBs);

        pnlForm.add(new JLabel("Nội dung khám:"));
        JTextField txtNoiDung = new JTextField(noiDungHienTai);
        pnlForm.add(txtNoiDung);

        pnlForm.add(new JLabel("Trạng thái:"));
        JComboBox<String> cboTt = new JComboBox<>(new String[] { "Chờ khám", "Đang khám", "Đã khám", "Đã hủy" });
        cboTt.setSelectedItem(String.valueOf(bangLichKham.getValueAt(row, COT_TRANG_THAI)));
        pnlForm.add(cboTt);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlBottom.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        JButton btnLuu = new JButton("Lưu thay đổi");
        btnLuu.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFocusPainted(false);

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.addActionListener(e -> dialog.dispose());

        btnLuu.addActionListener(e -> {
            bangLichKham.getModel().setValueAt(txtThoiGian.getText().trim(), row, 0);
            bangLichKham.getModel().setValueAt(cboBs.getSelectedItem().toString(), row, 3);
            bangLichKham.getModel().setValueAt(txtNoiDung.getText().trim(), row, 4);
            bangLichKham.getModel().setValueAt(cboTt.getSelectedItem().toString(), row, COT_TRANG_THAI);
            bangLichKham.repaint();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin lịch khám thành công!");
        });

        pnlBottom.add(btnHuy);
        pnlBottom.add(btnLuu);

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Xác nhận và thực hiện hủy lịch khám
     */
    private void xuLyHuyLich() {
        int row = bangLichKham.getSelectedRow();
        if (row == -1) return;

        String thuCung = String.valueOf(bangLichKham.getValueAt(row, 1));
        String trangThaiHienTai = String.valueOf(bangLichKham.getValueAt(row, COT_TRANG_THAI));
        if ("Đã hủy".equalsIgnoreCase(trangThaiHienTai)) {
            JOptionPane.showMessageDialog(this, "Lịch khám này đã ở trạng thái 'Đã hủy'!");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn hủy lịch khám của thú cưng '" + thuCung + "'?",
                "Xác nhận hủy lịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            bangLichKham.getModel().setValueAt("Đã hủy", row, COT_TRANG_THAI);
            bangLichKham.repaint();
            JOptionPane.showMessageDialog(this, "Đã hủy lịch khám thành công.");
        }
    }

    // =========================================================================
    // 5. PHƯƠNG THỨC TIỆN ÍCH CÔNG KHAI
    // =========================================================================
    public void capNhatThongKe(String ngayHienThi, int soHomNay, int soChoKham, int soDangKham, int soDaKham,
            int soDaHuy) {
        theLichHomNay.datGiaTri(String.valueOf(soHomNay), "Ngày " + ngayHienThi);
        theChoKham.datGiaTri(String.valueOf(soChoKham), "Chưa tiếp nhận");
        theDangKham.datGiaTri(String.valueOf(soDangKham), "Đang xử lý");
        theDaKhamDaHuy.datGiaTri(soDaKham + " / " + soDaHuy, "Trong hôm nay");
    }

    public TableMau getBangLichKham() {
        return bangLichKham;
    }

    public JButton getNutTaoLichKham() {
        return nutTaoLichKham;
    }

    public JButton getNutXemChiTiet() {
        return nutXemChiTiet;
    }

    public JButton getNutSuaLich() {
        return nutSuaLich;
    }

    public JButton getNutHuyLich() {
        return nutHuyLich;
    }

    public JComboBox<String> getCboBacSi() {
        return cboBacSi;
    }

    public JComboBox<String> getCboTrangThai() {
        return cboTrangThai;
    }

    // =========================================================================
    // 6. CÁC LỚP CON HỖ TRỢ VẼ GIAO DIỆN
    // =========================================================================

    /** Màu huy hiệu trạng thái: {nền, chữ, chấm} */
    private static Color[] layMauTrangThai(String trangThai) {
        switch (trangThai) {
            case "Chờ khám":
                return new Color[] { RangBuocGiaoDien.MAU_CANH_BAO_NEN, RangBuocGiaoDien.MAU_CANH_BAO_CHU,
                        RangBuocGiaoDien.MAU_CANH_BAO_CHAM };
            case "Đang khám":
                return new Color[] { RangBuocGiaoDien.MAU_THONG_TIN_NEN, RangBuocGiaoDien.MAU_THONG_TIN_CHU,
                        RangBuocGiaoDien.MAU_THONG_TIN_CHAM };
            case "Đã khám":
                return new Color[] { RangBuocGiaoDien.MAU_THANH_CONG_NEN, RangBuocGiaoDien.MAU_THANH_CONG_CHU,
                        RangBuocGiaoDien.MAU_THANH_CONG_CHAM };
            case "Đã hủy":
                return new Color[] { RangBuocGiaoDien.MAU_NGUY_HIEM_NEN, RangBuocGiaoDien.MAU_NGUY_HIEM_CHU,
                        RangBuocGiaoDien.MAU_NGUY_HIEM_CHAM };
            default:
                return new Color[] { RangBuocGiaoDien.MAU_TRUNG_TINH_NEN, RangBuocGiaoDien.MAU_TRUNG_TINH_CHU,
                        RangBuocGiaoDien.MAU_TRUNG_TINH_CHAM };
        }
    }

    /** Thẻ bo góc tổng quát */
    private static class TheBoGoc extends JPanel {
        private int banKinh;
        private Color mauNen;
        private Color mauVien;

        TheBoGoc(int banKinh, Color mauNen, Color mauVien) {
            super(new BorderLayout());
            this.banKinh = banKinh;
            this.mauNen = mauNen;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    banKinh * 2, banKinh * 2);
            g2.setColor(mauNen);
            g2.fill(hinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.draw(hinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Thẻ thống kê nhanh dùng Text icon */
    private static class TheThongKe extends TheBoGoc {
        private JLabel nhanTieuDe;
        private JLabel nhanGiaTri;
        private JLabel nhanChuThich;

        TheThongKe(String kyHieuText, String tieuDe, String giaTri, String chuThich) {
            super(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            HopKyHieu hopKyHieu = new HopKyHieu(kyHieuText);
            JPanel khoiIcon = new JPanel(new GridBagLayout());
            khoiIcon.setOpaque(false);
            khoiIcon.add(hopKyHieu);

            nhanTieuDe = new JLabel(tieuDe);
            nhanTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
            nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            nhanGiaTri = new JLabel(giaTri);
            nhanGiaTri.setFont(RangBuocGiaoDien.fontDam(26));
            nhanGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

            nhanChuThich = new JLabel(chuThich);
            nhanChuThich.setFont(RangBuocGiaoDien.fontThuong(11));
            nhanChuThich.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            JPanel khoiChu = new JPanel(new GridBagLayout());
            khoiChu.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 2;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.WEST;
            khoiChu.add(nhanTieuDe, gc);

            gc.gridy = 1;
            gc.gridwidth = 1;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.BASELINE_LEADING;
            khoiChu.add(nhanGiaTri, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.insets = new Insets(0, 10, 0, 0);
            khoiChu.add(nhanChuThich, gc);

            add(khoiIcon, BorderLayout.WEST);
            add(khoiChu, BorderLayout.CENTER);
        }

        void datGiaTri(String giaTri, String chuThich) {
            nhanGiaTri.setText(giaTri);
            nhanChuThich.setText(chuThich);
        }
    }

    /** Ô vuông bo góc hiển thị ký hiệu text */
    private static class HopKyHieu extends JPanel {
        HopKyHieu(String kyHieu) {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(44, 44));

            JLabel lbl = new JLabel(kyHieu, SwingConstants.CENTER);
            lbl.setFont(RangBuocGiaoDien.fontDam(18));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
            add(lbl);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút bấm bo góc hỗ trợ trạng thái Enabled/Disabled và kiểu Nguy hiểm */
    private static class NutBo extends JButton {
        static final int KIEU_CHINH = 0;
        static final int KIEU_VIEN = 1;
        static final int KIEU_PHANG = 2;
        static final int KIEU_NGUY_HIEM = 3;

        private int kieu;
        private boolean chon;

        NutBo(String chu, int kieu) {
            super(chu);
            this.kieu = kieu;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(RangBuocGiaoDien.fontVua(13));
        }

        void datChon(boolean chon) {
            this.chon = chon;
            repaint();
        }

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setCursor(b ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);

            boolean reChuot = getModel().isRollover() && isEnabled();
            boolean noi = (kieu == KIEU_CHINH) || chon;
            Color nen;
            Color vien;
            Color chu;

            if (!isEnabled()) {
                nen = RangBuocGiaoDien.MAU_TRUNG_TINH_NEN;
                vien = RangBuocGiaoDien.MAU_DUONG_VIEN;
                chu = RangBuocGiaoDien.MAU_CHU_GOI_Y;
            } else if (kieu == KIEU_NGUY_HIEM) {
                nen = reChuot ? new Color(254, 226, 226) : RangBuocGiaoDien.MAU_NEN_THE;
                vien = RangBuocGiaoDien.MAU_NGUY_HIEM_CHU;
                chu = RangBuocGiaoDien.MAU_NGUY_HIEM_CHU;
            } else if (noi) {
                nen = reChuot ? RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT : RangBuocGiaoDien.MAU_CHU_DAO;
                vien = nen;
                chu = Color.WHITE;
            } else if (kieu == KIEU_VIEN) {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : RangBuocGiaoDien.MAU_NEN_THE;
                vien = RangBuocGiaoDien.MAU_DUONG_VIEN;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            } else {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : null;
                vien = null;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            }

            int rong = getWidth();
            int cao = getHeight();
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, rong - 1, cao - 1, 10, 10);
            if (nen != null) {
                g2.setColor(nen);
                g2.fill(hinh);
            }
            if (vien != null) {
                g2.setColor(vien);
                g2.draw(hinh);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth(getText());
            int x = (rong - rongChu) / 2;
            g2.setColor(chu);
            g2.drawString(getText(), x, (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Combo box hiện đại bo góc */
    private static class ComboHienDai extends JComboBox<String> {

        ComboHienDai(String[] mucChon, int rong, int cao) {
            super(mucChon);
            setUI(new GiaoDienCombo());
            setOpaque(false);
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            setBorder(BorderFactory.createEmptyBorder());
            setPreferredSize(new Dimension(rong, cao));
            setMaximumRowCount(8);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel nhan = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    nhan.setFont(RangBuocGiaoDien.fontThuong(13));
                    nhan.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    nhan.setForeground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
                    nhan.setBackground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_NEN_THE);
                    return nhan;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    BAN_KINH_O * 2, BAN_KINH_O * 2);
            g2.setColor(RangBuocGiaoDien.MAU_NEN_THE);
            g2.fill(hinh);
            g2.setColor((isPopupVisible() || hasFocus()) ? RangBuocGiaoDien.MAU_CHU_DAO
                    : RangBuocGiaoDien.MAU_DUONG_VIEN);
            g2.draw(hinh);
            g2.dispose();
            RangBuocGiaoDien.batKhuRangCua(g);
            super.paintComponent(g);
        }
    }

    private static class GiaoDienCombo extends BasicComboBoxUI {
        @Override
        protected javax.swing.JButton createArrowButton() {
            JButton nut = new JButton("v");
            nut.setFont(RangBuocGiaoDien.fontDam(9));
            nut.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            nut.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            nut.setContentAreaFilled(false);
            nut.setFocusPainted(false);
            nut.setOpaque(false);
            return nut;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Object giaTri = comboBox.getSelectedItem();
            if (giaTri == null) {
                return;
            }
            g.setFont(comboBox.getFont());
            g.setColor(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(giaTri.toString(), bounds.x + 12,
                    bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
            return popup;
        }
    }

    /** Tiêu đề cột */
    private static class RendererTieuDe extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(RangBuocGiaoDien.fontDam(12));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    /** Ô văn bản có màu chữ */
    private static class RendererVanBanMau extends DefaultTableCellRenderer {
        private Color mauChu;

        RendererVanBanMau(Color mauChu) {
            this.mauChu = mauChu;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setForeground(mauChu);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_NEN_TRANG);
            }
            return this;
        }
    }

    /** Ô trạng thái dạng pill badge */
    private static class RendererTrangThai extends JPanel implements TableCellRenderer {
        private String trangThai = "";

        RendererTrangThai() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            trangThai = (value == null) ? "" : value.toString();
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_NEN_TRANG);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (trangThai.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            Color[] mau = layMauTrangThai(trangThai);

            g2.setFont(RangBuocGiaoDien.fontDam(11));
            FontMetrics fm = g2.getFontMetrics();
            int cao = 22;
            int rong = 10 + 6 + 6 + fm.stringWidth(trangThai) + 10;
            int x = 12;
            int y = (getHeight() - cao) / 2;

            g2.setColor(mau[0]);
            g2.fill(new RoundRectangle2D.Double(x, y, rong, cao, cao, cao));
            g2.setColor(mau[2]);
            g2.fill(new Ellipse2D.Double(x + 10, y + (cao - 6) / 2.0, 6, 6));
            g2.setColor(mau[1]);
            g2.drawString(trangThai, x + 22, y + (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
