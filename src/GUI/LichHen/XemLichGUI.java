package GUI.LichHen;

import GUI.Chung.TableMau;
import GUI.Chung.RangBuocGiaoDien;
import dao.DichVu_DAO;
import dao.LichHen_DAO;
import dao.NhanVien_DAO;
import entity.DichVu_entity;
import entity.LichHen_entity;
import entity.NhanVien_entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.awt.BasicStroke;
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
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Class XemLichGUI: Giao diện "Lịch khám sức khỏe" (Xem lịch) của hệ thống SALALA Pet Shop.
 * Bố cục gồm:
 * 1. Phần đầu trang: đường dẫn, tiêu đề, mô tả và nút "Tạo lịch khám".
 * 2. Hàng thẻ thống kê nhanh (hôm nay, chờ khám, đang khám, đã khám / đã hủy).
 * 3. Thẻ danh sách: thanh bộ lọc (ngày, bác sĩ, trạng thái, chế độ xem),
 *    bảng lịch khám (dùng TableMau) và chân bảng phân trang.
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

    private static final String[] TIEU_DE_BANG = {
            "Thời gian", "Tên thú cưng", "Chủ nuôi", "Nhân viên phụ trách", "Nội dung khám", "Trạng thái"
    };
    private static final int[] DO_RONG_COT = { 170, 150, 215, 190, 300, 165 };

    // Dữ liệu mẫu minh họa (sau này thay bằng dữ liệu từ lịch hẹn trong CSDL)
    private static final String[] DANH_SACH_NHAN_VIEN = { "Nhân viên: Tất cả" };
    private static final String[] DANH_SACH_TRANG_THAI = {
            "Trạng thái: Tất cả", "Chờ khám", "Đang khám", "Đã khám", "Đã hủy"
    };
    private static final String[] DANH_SACH_SO_DONG = { "8", "16", "32" };
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final LichHen_DAO lichHenDAO = new LichHen_DAO();
    private final NhanVien_DAO nhanVienDAO = new NhanVien_DAO();
    private final DichVu_DAO dichVuDAO = new DichVu_DAO();
    private List<LichHen_entity> danhSachLichHen = new ArrayList<>();
    private List<LichHen_entity> danhSachDangHienThi = new ArrayList<>();
    private LocalDate tuNgay;
    private LocalDate denNgay;

    // =========================================================================
    // 2. CÁC THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    // --- Phần đầu trang ---
    private JPanel panelPhanTren;
    private JPanel panelDauTrang;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBo nutTaoLichKham;

    // --- Hàng thẻ thống kê ---
    private JPanel panelThongKe;
    private TheThongKe theLichHomNay;
    private TheThongKe theChoKham;
    private TheThongKe theDangKham;
    private TheThongKe theDaKhamDaHuy;

    // --- Thẻ danh sách ---
    private TheBoGoc theDanhSach;

    // Thanh bộ lọc
    private JPanel panelThanhCongCu;
    private TheBoGoc oChonNgay;
    private JLabel nhanNgayLoc;
    private ComboHienDai cboBacSi;
    private ComboHienDai cboTrangThai;
    private TheBoGoc panelCheDoXem;
    private NutBo nutThang;
    private NutBo nutTuan;
    private NutBo nutDanhSach;
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
        caiDatSuKien();
        loadDataToTable();
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

        nhanMoTa = new JLabel("Theo dõi tiến độ khám và lịch tái khám của thú cưng.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel khoiTieuDe = new JPanel(new BorderLayout(0, 2));
        khoiTieuDe.setOpaque(false);
        khoiTieuDe.add(nhanTieuDe, BorderLayout.NORTH);
        khoiTieuDe.add(nhanMoTa, BorderLayout.CENTER);

        nutTaoLichKham = new NutBo("Tạo lịch khám", NutBo.KIEU_CHINH);
        nutTaoLichKham.setFont(RangBuocGiaoDien.fontDam(13));
        nutTaoLichKham.datBieuTuong(new BieuTuong(BieuTuong.LICH_THEM, 18, Color.WHITE));
        nutTaoLichKham.setPreferredSize(new Dimension(145, 40));

        JPanel khoiNut = new JPanel(new GridBagLayout());
        khoiNut.setOpaque(false);
        khoiNut.add(nutTaoLichKham);

        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);
        hangTieuDe.add(khoiTieuDe, BorderLayout.WEST);
        hangTieuDe.add(khoiNut, BorderLayout.EAST);

        panelDauTrang.add(nhanDuongDan, BorderLayout.NORTH);
        panelDauTrang.add(hangTieuDe, BorderLayout.CENTER);
        return panelDauTrang;
    }

    // -------------------------------------------------------------------------
    // HÀNG THẺ THỐNG KÊ
    // -------------------------------------------------------------------------
    private JPanel taoHangThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH, 0));
        panelThongKe.setOpaque(false);

        theLichHomNay = new TheThongKe(BieuTuong.LICH, "Lịch khám hôm nay", "0", "Ngày " + DINH_DANG_NGAY.format(LocalDate.now()));
        theChoKham = new TheThongKe(BieuTuong.DONG_HO, "Chờ khám", "0", "Chưa tiếp nhận");
        theDangKham = new TheThongKe(BieuTuong.ONG_NGHE, "Đang khám", "0", "Đang xử lý");
        theDaKhamDaHuy = new TheThongKe(BieuTuong.BANG_GHI, "Đã khám / đã hủy", "0 / 0", "Trong hôm nay");

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
        panelThanhCongCu = new JPanel(new BorderLayout());
        panelThanhCongCu.setOpaque(false);
        panelThanhCongCu.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // --- Ô chọn khoảng ngày ---
        oChonNgay = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        oChonNgay.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        oChonNgay.setPreferredSize(new Dimension(255, CHIEU_CAO_O));
        nhanNgayLoc = new JLabel("Ngày: Tất cả");
        nhanNgayLoc.setFont(RangBuocGiaoDien.fontThuong(13));
        nhanNgayLoc.setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
        oChonNgay.add(nhanNgayLoc, BorderLayout.CENTER);
        oChonNgay.add(new JLabel(new BieuTuong(BieuTuong.LICH, 16, RangBuocGiaoDien.MAU_CHU_PHU)), BorderLayout.EAST);
        oChonNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        oChonNgay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                chonKhoangNgay();
            }
        });

        // --- Combo bác sĩ & trạng thái ---
        cboBacSi = new ComboHienDai(DANH_SACH_NHAN_VIEN, 240, CHIEU_CAO_O);
        cboTrangThai = new ComboHienDai(DANH_SACH_TRANG_THAI, 230, CHIEU_CAO_O);

        JPanel nhomBoLoc = new JPanel();
        nhomBoLoc.setOpaque(false);
        nhomBoLoc.setLayout(new BoxLayout(nhomBoLoc, BoxLayout.X_AXIS));
        nhomBoLoc.add(oChonNgay);
        nhomBoLoc.add(Box.createHorizontalStrut(12));
        nhomBoLoc.add(cboBacSi);
        nhomBoLoc.add(Box.createHorizontalStrut(12));
        nhomBoLoc.add(cboTrangThai);

        // --- Nhóm nút chuyển chế độ xem: Tháng / Tuần / Danh sách ---
        panelCheDoXem = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelCheDoXem.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCheDoXem.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        nutThang = new NutBo("Tháng", NutBo.KIEU_PHANG);
        nutTuan = new NutBo("Tuần", NutBo.KIEU_PHANG);
        nutDanhSach = new NutBo("Danh sách", NutBo.KIEU_PHANG);
        nutThang.setPreferredSize(new Dimension(72, 30));
        nutTuan.setPreferredSize(new Dimension(58, 30));
        nutDanhSach.setPreferredSize(new Dimension(92, 30));
        nutDanhSach.datChon(true);

        // Chỉ đổi trạng thái hiển thị của nút đang chọn (chưa xử lý nghiệp vụ)
        ActionListener chonCheDoXem = e -> {
            nutThang.datChon(e.getSource() == nutThang);
            nutTuan.datChon(e.getSource() == nutTuan);
            nutDanhSach.datChon(e.getSource() == nutDanhSach);
        };
        nutThang.addActionListener(chonCheDoXem);
        nutTuan.addActionListener(chonCheDoXem);
        nutDanhSach.addActionListener(chonCheDoXem);

        panelCheDoXem.add(nutThang);
        panelCheDoXem.add(nutTuan);
        panelCheDoXem.add(nutDanhSach);

        nutXemChiTiet = new NutBo("Xem chi tiết", NutBo.KIEU_VIEN);
        nutSuaLich = new NutBo("Sửa lịch", NutBo.KIEU_VIEN);
        nutHuyLich = new NutBo("Hủy lịch", NutBo.KIEU_VIEN);
        nutXemChiTiet.setPreferredSize(new Dimension(112, 34));
        nutSuaLich.setPreferredSize(new Dimension(92, 34));
        nutHuyLich.setPreferredSize(new Dimension(92, 34));
        nutXemChiTiet.setEnabled(false);
        nutSuaLich.setEnabled(false);
        nutHuyLich.setEnabled(false);
        nutXemChiTiet.addActionListener(e -> xemChiTietLich());
        nutSuaLich.addActionListener(e -> suaLich());
        nutHuyLich.addActionListener(e -> huyLich());
        nutDanhSach.addActionListener(e -> xuatDanhSach());

        JPanel nhomThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nhomThaoTac.setOpaque(false);
        nhomThaoTac.add(nutXemChiTiet);
        nhomThaoTac.add(nutSuaLich);
        nhomThaoTac.add(nutHuyLich);

        JPanel khoiPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        khoiPhai.setOpaque(false);
        khoiPhai.add(nhomThaoTac);
        khoiPhai.add(panelCheDoXem);

        panelThanhCongCu.add(nhomBoLoc, BorderLayout.WEST);
        panelThanhCongCu.add(khoiPhai, BorderLayout.EAST);
        return panelThanhCongCu;
    }

    private JScrollPane taoBang() {
        bangLichKham = new TableMau(TIEU_DE_BANG, new Object[0][TIEU_DE_BANG.length]);
        bangLichKham.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bangLichKham.getTableHeader().setDefaultRenderer(new RendererTieuDe());
        bangLichKham.getTableHeader().setPreferredSize(new Dimension(0, 42));
        bangLichKham.setIntercellSpacing(new Dimension(0, 1)); // Bỏ khe dọc giữa các cột để dòng xen kẽ liền mạch

        for (int i = 0; i < DO_RONG_COT.length; i++) {
            bangLichKham.getColumnModel().getColumn(i).setPreferredWidth(DO_RONG_COT[i]);
        }
        bangLichKham.getColumnModel().getColumn(COT_THOI_GIAN)
                .setCellRenderer(new RendererVanBanMau(RangBuocGiaoDien.MAU_LIEN_KET));
        bangLichKham.getColumnModel().getColumn(COT_TRANG_THAI)
                .setCellRenderer(new RendererTrangThai());
        bangLichKham.setRowSelectionAllowed(true);
        bangLichKham.setColumnSelectionAllowed(false);
        bangLichKham.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatNutThaoTac();
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

        nhanGoiYChon = new JLabel("Chọn lịch khám để xem hồ sơ sức khỏe và tiến hành khám.");
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

        nutTrangTruoc = taoNutTrang("‹");
        nutTrang1 = taoNutTrang("1");
        nutTrang2 = taoNutTrang("2");
        nutTrang3 = taoNutTrang("3");
        nutTrangCham = taoNutTrang("…");
        nutTrangSau = taoNutTrang("›");
        nutTrang1.datChon(true);

        // Chỉ đổi trạng thái hiển thị của số trang đang chọn
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

    private void capNhatNutThaoTac() {
        boolean chonMot = bangLichKham != null && bangLichKham.getSelectedRowCount() == 1;
        if (nutXemChiTiet != null) {
            nutXemChiTiet.setEnabled(chonMot);
        }
        if (nutSuaLich != null) {
            nutSuaLich.setEnabled(chonMot);
        }
        if (nutHuyLich != null) {
            nutHuyLich.setEnabled(chonMot);
        }
    }

    private void caiDatSuKien() {
        nutTaoLichKham.addActionListener(e -> moFormDatLich());
        cboBacSi.addActionListener(e -> locDuLieu());
        cboTrangThai.addActionListener(e -> locDuLieu());
        cboSoDong.addActionListener(e -> capNhatThongTinHienThi());
        bangLichKham.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangLichKham.getSelectedRowCount() == 1) {
                    xemChiTietLich();
                }
            }
        });
    }

    public void loadDataToTable() {
        try {
            danhSachLichHen = lichHenDAO.getAllLichHen();
            List<NhanVien_entity> nhanVien = nhanVienDAO.getAll();
            Object selected = cboBacSi.getSelectedItem();
            cboBacSi.removeAllItems();
            cboBacSi.addItem("Nhân viên: Tất cả");
            for (NhanVien_entity item : nhanVien) {
                cboBacSi.addItem(item.getHoTen() + " (" + item.getMaNhanVien() + ")");
            }
            if (selected != null && !selected.toString().startsWith("Nhân viên:")) {
                cboBacSi.setSelectedItem(selected);
            } else {
                cboBacSi.setSelectedIndex(0);
            }
            locDuLieu();
            capNhatThongKeTuDuLieu();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch hẹn: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDuLieu() {
        if (bangLichKham == null) {
            return;
        }
        String nhanVienChon = String.valueOf(cboBacSi.getSelectedItem());
        String trangThaiChon = String.valueOf(cboTrangThai.getSelectedItem());
        DefaultTableModel model = (DefaultTableModel) bangLichKham.getModel();
        model.setRowCount(0);
        danhSachDangHienThi = new ArrayList<>();
        for (LichHen_entity lich : danhSachLichHen) {
            if (lich == null) {
                continue;
            }
            String maNhanVien = lich.getNhanVien() == null ? "" : lich.getNhanVien().getMaNhanVien();
            String tenNhanVien = lich.getNhanVien() == null ? "" : lich.getNhanVien().getHoTen();
            String trangThai = lich.getTrangThai() == null ? "" : lich.getTrangThai();
            LocalDate ngay = lich.getNgayGio() == null ? null : lich.getNgayGio().toLocalDate();
            boolean locNhanVien = nhanVienChon == null || nhanVienChon.startsWith("Nhân viên:")
                    || nhanVienChon.endsWith("(" + maNhanVien + ")");
            boolean locTrangThai = trangThaiChon == null || trangThaiChon.startsWith("Trạng thái:")
                    || trangThaiChon.equals(trangThai);
            boolean locNgay = (tuNgay == null || (ngay != null && !ngay.isBefore(tuNgay)))
                    && (denNgay == null || (ngay != null && !ngay.isAfter(denNgay)));
            if (locNhanVien && locTrangThai && locNgay) {
                danhSachDangHienThi.add(lich);
                model.addRow(new Object[] {
                        lich.getNgayGio() == null ? "" : DINH_DANG_NGAY_GIO.format(lich.getNgayGio()),
                        lich.getThuCung() == null ? "" : lich.getThuCung().getTenTC(),
                        lich.getKhachHang() == null ? "" : lich.getKhachHang().getHoTen(),
                        tenNhanVien, lich.getLyDo(), trangThai
                });
            }
        }
        bangLichKham.clearSelection();
        capNhatNutThaoTac();
        capNhatThongTinHienThi();
    }

    private void capNhatThongKeTuDuLieu() {
        LocalDate homNay = LocalDate.now();
        int soHomNay = 0;
        int choKham = 0;
        int dangKham = 0;
        int daKham = 0;
        int daHuy = 0;
        for (LichHen_entity lich : danhSachLichHen) {
            if (lich.getNgayGio() == null || !lich.getNgayGio().toLocalDate().equals(homNay)) {
                continue;
            }
            soHomNay++;
            if ("Chờ khám".equalsIgnoreCase(lich.getTrangThai())) {
                choKham++;
            } else if ("Đang khám".equalsIgnoreCase(lich.getTrangThai())) {
                dangKham++;
            } else if ("Đã khám".equalsIgnoreCase(lich.getTrangThai())) {
                daKham++;
            } else if ("Đã hủy".equalsIgnoreCase(lich.getTrangThai())) {
                daHuy++;
            }
        }
        capNhatThongKe(DINH_DANG_NGAY.format(homNay), soHomNay, choKham, dangKham, daKham, daHuy);
    }

    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        form.add(tu);
        form.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        form.add(den);
        if (JOptionPane.showConfirmDialog(this, form, "Lọc lịch theo ngày",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            tuNgay = tu.getText().trim().isEmpty() ? null : LocalDate.parse(tu.getText().trim(), DINH_DANG_NGAY);
            denNgay = den.getText().trim().isEmpty() ? null : LocalDate.parse(den.getText().trim(), DINH_DANG_NGAY);
            if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
                throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            }
            nhanNgayLoc.setText(tuNgay == null && denNgay == null ? "Ngày: Tất cả"
                    : "Ngày: " + (tuNgay == null ? "…" : DINH_DANG_NGAY.format(tuNgay))
                            + " – " + (denNgay == null ? "…" : DINH_DANG_NGAY.format(denNgay)));
            locDuLieu();
        } catch (DateTimeParseException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Khoảng ngày không hợp lệ: " + e.getMessage(),
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void moFormDatLich() {
        DatLichMoiGUI form = new DatLichMoiGUI();
        form.setXuLySauKhiDatLich(this::loadDataToTable);
        JOptionPane.showMessageDialog(this, form, "Tạo lịch khám", JOptionPane.PLAIN_MESSAGE);
    }

    private int layDongDaChon() {
        if (bangLichKham == null || bangLichKham.getSelectedRowCount() != 1) {
            return -1;
        }
        int dongHienThi = bangLichKham.getSelectedRow();
        return dongHienThi < 0 ? -1 : bangLichKham.convertRowIndexToModel(dongHienThi);
    }

    private void xemChiTietLich() {
        LichHen_entity lich = layLichHenDaChon();
        if (lich == null) {
            return;
        }
        try {
            LichHen_entity chiTiet = lichHenDAO.getLichHenTheoMa(lich.getMaLichHen());
            if (chiTiet == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn " + lich.getMaLichHen() + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String text = "Mã lịch hẹn: " + chiTiet.getMaLichHen()
                    + "\nThời gian: " + (chiTiet.getNgayGio() == null ? "" : DINH_DANG_NGAY_GIO.format(chiTiet.getNgayGio()))
                    + "\nThú cưng: " + (chiTiet.getThuCung() == null ? "" : chiTiet.getThuCung().getTenTC())
                    + "\nChủ nuôi: " + (chiTiet.getKhachHang() == null ? "" : chiTiet.getKhachHang().getHoTen())
                    + "\nNhân viên phụ trách: " + (chiTiet.getNhanVien() == null ? "" : chiTiet.getNhanVien().getHoTen())
                    + "\nNội dung: " + chiTiet.getLyDo()
                    + "\nDịch vụ: " + (chiTiet.getDichVu() == null ? "" : chiTiet.getDichVu().getTenDV())
                    + "\nLoại hẹn: " + chiTiet.getLoaiHen()
                    + "\nGhi chú: " + chiTiet.getGhiChu()
                    + "\nTrạng thái: " + chiTiet.getTrangThai();
            JOptionPane.showMessageDialog(this, text, "Chi tiết lịch hẹn", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaLich() {
        LichHen_entity lich = layLichHenDaChon();
        if (lich == null) {
            return;
        }
        try {
            lich = lichHenDAO.getLichHenTheoMa(lich.getMaLichHen());
            if (lich == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn cần sửa.",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<NhanVien_entity> nhanVien = nhanVienDAO.getAll();
            List<DichVu_entity> dichVu = dichVuDAO.getAllDichVu();
            JTextField txtNgayGio = new JTextField(DINH_DANG_NGAY_GIO.format(lich.getNgayGio()));
            JTextField txtLyDo = new JTextField(lich.getLyDo());
            JTextField txtLoaiHen = new JTextField(lich.getLoaiHen());
            javax.swing.JTextArea txtGhiChu = new javax.swing.JTextArea(lich.getGhiChu(), 3, 25);
            JComboBox<NhanVien_entity> cboNhanVien = new JComboBox<>(nhanVien.toArray(new NhanVien_entity[0]));
            cboNhanVien.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof NhanVien_entity) setText(((NhanVien_entity) value).getHoTen());
                    return this;
                }
            });
            for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
                if (lich.getNhanVien() != null && lich.getNhanVien().getMaNhanVien()
                        .equals(cboNhanVien.getItemAt(i).getMaNhanVien())) cboNhanVien.setSelectedIndex(i);
            }
            JComboBox<DichVu_entity> cboDichVu = new JComboBox<>(dichVu.toArray(new DichVu_entity[0]));
            cboDichVu.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof DichVu_entity) setText(((DichVu_entity) value).getTenDV());
                    return this;
                }
            });
            cboDichVu.insertItemAt(null, 0);
            cboDichVu.setSelectedIndex(0);
            if (lich.getDichVu() != null) {
                for (int i = 1; i < cboDichVu.getItemCount(); i++) {
                    if (lich.getDichVu().getMaDV().equals(cboDichVu.getItemAt(i).getMaDV())) cboDichVu.setSelectedIndex(i);
                }
            }
            JComboBox<String> cboTrangThai = new JComboBox<>(DANH_SACH_TRANG_THAI);
            cboTrangThai.removeItemAt(0);
            cboTrangThai.setSelectedItem(lich.getTrangThai());
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Thời gian (dd/MM/yyyy HH:mm):")); form.add(txtNgayGio);
            form.add(new JLabel("Nhân viên phụ trách:")); form.add(cboNhanVien);
            form.add(new JLabel("Nội dung:")); form.add(txtLyDo);
            form.add(new JLabel("Loại hẹn:")); form.add(txtLoaiHen);
            form.add(new JLabel("Dịch vụ:")); form.add(cboDichVu);
            form.add(new JLabel("Trạng thái:")); form.add(cboTrangThai);
            form.add(new JLabel("Ghi chú:")); form.add(new JScrollPane(txtGhiChu));
            if (JOptionPane.showConfirmDialog(this, form, "Sửa lịch hẹn",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            LocalDateTime ngayGio = LocalDateTime.parse(txtNgayGio.getText().trim(), DINH_DANG_NGAY_GIO);
            NhanVien_entity nv = (NhanVien_entity) cboNhanVien.getSelectedItem();
            String lyDo = txtLyDo.getText().trim();
            String loaiHen = txtLoaiHen.getText().trim();
            if (ngayGio.isBefore(LocalDateTime.now()) || nv == null || lyDo.isEmpty() || loaiHen.isEmpty()) {
                throw new IllegalArgumentException("Thời gian phải ở tương lai; nhân viên, nội dung và loại hẹn là bắt buộc.");
            }
            lich.setNgayGio(ngayGio);
            lich.setNhanVien(nv);
            lich.setLyDo(lyDo);
            lich.setLoaiHen(loaiHen);
            lich.setDichVu((DichVu_entity) cboDichVu.getSelectedItem());
            lich.setTrangThai(String.valueOf(cboTrangThai.getSelectedItem()));
            lich.setGhiChu(txtGhiChu.getText().trim());
            if (lichHenDAO.updateLichHen(lich)) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật lịch hẹn.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không cập nhật được lịch hẹn.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Thời gian không hợp lệ (dd/MM/yyyy HH:mm).",
                    "Sai định dạng", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huyLich() {
        LichHen_entity lichChon = layLichHenDaChon();
        if (lichChon == null) {
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy lịch " + lichChon.getMaLichHen() + " của "
                        + lichChon.getThuCung().getTenTC() + " không?",
                "Xác nhận hủy lịch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            LichHen_entity lich = lichHenDAO.getLichHenTheoMa(lichChon.getMaLichHen());
            if (lich == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn để hủy.",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("Đã hủy".equalsIgnoreCase(lich.getTrangThai())) {
                JOptionPane.showMessageDialog(this, "Lịch hẹn này đã được hủy trước đó.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            lich.setTrangThai("Đã hủy");
            if (lichHenDAO.updateLichHen(lich)) {
                JOptionPane.showMessageDialog(this, "Đã hủy lịch hẹn.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái lịch hẹn.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi hủy lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LichHen_entity layLichHenDaChon() {
        int row = layDongDaChon();
        return row < 0 || row >= danhSachDangHienThi.size() ? null : danhSachDangHienThi.get(row);
    }

    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("danh-sach-lich-hen.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại, ghi đè?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            DefaultTableModel model = (DefaultTableModel) bangLichKham.getModel();
            for (int row = -1; row < model.getRowCount(); row++) {
                for (int column = 0; column < model.getColumnCount(); column++) {
                    if (column > 0) writer.write(',');
                    String text = row < 0 ? TIEU_DE_BANG[column] : String.valueOf(model.getValueAt(row, column));
                    writer.write("\"" + text.replace("\"", "\"\"") + "\"");
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Đã xuất danh sách ra:\n" + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể xuất danh sách: " + e.getMessage(),
                    "Lỗi xuất tệp", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatThongTinHienThi() {
        int soDong = bangLichKham.getRowCount();
        nhanThongTinHienThi.setText(soDong == 0 ? "Không có lịch khám"
                : "Hiển thị 1–" + soDong + " trong " + soDong + " lịch khám");
    }

    private NutBo taoNutTrang(String chu) {
        NutBo nut = new NutBo(chu, NutBo.KIEU_VIEN);
        nut.setFont(RangBuocGiaoDien.fontVua(12));
        nut.setPreferredSize(new Dimension(32, 32));
        return nut;
    }

    // =========================================================================
    // 4. PHƯƠNG THỨC TIỆN ÍCH CÔNG KHAI (để nhúng dữ liệu sau này)
    // =========================================================================
    /**
     * Cập nhật 4 thẻ thống kê phía trên bảng.
     */
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

    public JComboBox<String> getCboBacSi() {
        return cboBacSi;
    }

    public JComboBox<String> getCboTrangThai() {
        return cboTrangThai;
    }

    // =========================================================================
    // 5. CÁC LỚP CON HỖ TRỢ VẼ GIAO DIỆN
    // =========================================================================

    /** Màu huy hiệu trạng thái: {nền, chữ, chấm} theo bảng màu của RangBuocGiaoDien. */
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

    // -------------------------------------------------------------------------
    // Thẻ bo góc (dùng cho thẻ thống kê, thẻ danh sách, ô nhập, nhóm nút)
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Thẻ thống kê nhanh: [biểu tượng] nhãn / giá trị + chú thích
    // -------------------------------------------------------------------------
    private static class TheThongKe extends TheBoGoc {
        private JLabel nhanTieuDe;
        private JLabel nhanGiaTri;
        private JLabel nhanChuThich;

        TheThongKe(int loaiBieuTuong, String tieuDe, String giaTri, String chuThich) {
            super(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            HopBieuTuong hopBieuTuong = new HopBieuTuong(loaiBieuTuong);
            JPanel khoiIcon = new JPanel(new GridBagLayout());
            khoiIcon.setOpaque(false);
            khoiIcon.add(hopBieuTuong);

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

    /** Ô vuông bo góc chứa biểu tượng của thẻ thống kê. */
    private static class HopBieuTuong extends JComponent {
        private BieuTuong bieuTuong;

        HopBieuTuong(int loai) {
            this.bieuTuong = new BieuTuong(loai, 22, RangBuocGiaoDien.MAU_CHU_DAO);
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            bieuTuong.paintIcon(this, g2, (getWidth() - bieuTuong.getIconWidth()) / 2,
                    (getHeight() - bieuTuong.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Nút bấm bo góc: 3 kiểu (chính - nền teal, viền - nền trắng, phẳng - không viền)
    // -------------------------------------------------------------------------
    private static class NutBo extends JButton {
        static final int KIEU_CHINH = 0;
        static final int KIEU_VIEN = 1;
        static final int KIEU_PHANG = 2;

        private int kieu;
        private boolean chon;
        private Icon bieuTuongNut;

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

        void datBieuTuong(Icon bieuTuongNut) {
            this.bieuTuongNut = bieuTuongNut;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);

            boolean reChuot = getModel().isRollover();
            boolean noi = (kieu == KIEU_CHINH) || chon;
            Color nen;
            Color vien;
            Color chu;
            if (noi) {
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
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, rong - 1, cao - 1, 12, 12);
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
            int rongIcon = (bieuTuongNut != null) ? bieuTuongNut.getIconWidth() + 8 : 0;
            int x = (rong - rongChu - rongIcon) / 2;
            if (bieuTuongNut != null) {
                bieuTuongNut.paintIcon(this, g2, x, (cao - bieuTuongNut.getIconHeight()) / 2);
                x += rongIcon;
            }
            g2.setColor(chu);
            g2.drawString(getText(), x, (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Combo box hiện đại: bo góc, mũi tên chevron, danh sách thả xuống đồng bộ màu
    // -------------------------------------------------------------------------
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
            JButton nut = new JButton(new BieuTuong(BieuTuong.MUI_TEN_XUONG, 14, RangBuocGiaoDien.MAU_CHU_PHU));
            nut.setBorder(BorderFactory.createEmptyBorder());
            nut.setContentAreaFilled(false);
            nut.setFocusPainted(false);
            nut.setOpaque(false);
            return nut;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Không tô nền: phần nền bo góc đã được vẽ trong ComboHienDai
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

    // -------------------------------------------------------------------------
    // Renderer cho bảng
    // -------------------------------------------------------------------------
    /** Tiêu đề cột: nền xám nhạt, chữ đậm, viền trên/dưới theo mẫu Figma. */
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

    /** Ô văn bản có màu chữ riêng cho cột Thời gian. */
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

    /** Ô trạng thái: vẽ huy hiệu bo tròn có chấm màu (Chờ khám / Đang khám / Đã khám / Đã hủy). */
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

    // -------------------------------------------------------------------------
    // Bộ biểu tượng vẽ bằng Graphics2D (lưới 24 x 24)
    // -------------------------------------------------------------------------
    private static class BieuTuong implements Icon {
        static final int LICH = 0;
        static final int LICH_THEM = 1;
        static final int DONG_HO = 2;
        static final int ONG_NGHE = 3;
        static final int BANG_GHI = 4;
        static final int MUI_TEN_XUONG = 5;

        private int loai;
        private int kichThuoc;
        private Color mau;

        BieuTuong(int loai, int kichThuoc, Color mau) {
            this.loai = loai;
            this.kichThuoc = kichThuoc;
            this.mau = mau;
        }

        @Override
        public int getIconWidth() {
            return kichThuoc;
        }

        @Override
        public int getIconHeight() {
            return kichThuoc;
        }

        private void veDuongGap(Graphics2D g2, double... toaDo) {
            Path2D.Double duong = new Path2D.Double();
            duong.moveTo(toaDo[0], toaDo[1]);
            for (int i = 2; i < toaDo.length; i += 2) {
                duong.lineTo(toaDo[i], toaDo[i + 1]);
            }
            g2.draw(duong);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double tyLe = kichThuoc / 24.0;
            g2.scale(tyLe, tyLe);
            g2.setColor(mau);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (loai) {
                case LICH:
                    g2.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 4, 4));
                    veDuongGap(g2, 3.5, 10, 20.5, 10);
                    veDuongGap(g2, 8, 3, 8, 7);
                    veDuongGap(g2, 16, 3, 16, 7);
                    g2.fill(new Ellipse2D.Double(7.2, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(11.1, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(15, 13.2, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(7.2, 16.6, 1.8, 1.8));
                    g2.fill(new Ellipse2D.Double(11.1, 16.6, 1.8, 1.8));
                    break;
                case LICH_THEM:
                    g2.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 4, 4));
                    veDuongGap(g2, 3.5, 10, 20.5, 10);
                    veDuongGap(g2, 8, 3, 8, 7);
                    veDuongGap(g2, 16, 3, 16, 7);
                    veDuongGap(g2, 12, 12.5, 12, 18);
                    veDuongGap(g2, 9.25, 15.25, 14.75, 15.25);
                    break;
                case DONG_HO:
                    g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    veDuongGap(g2, 12, 7, 12, 12, 15.5, 14);
                    break;
                case ONG_NGHE:
                    veDuongGap(g2, 6, 3, 6, 9);
                    g2.draw(new Arc2D.Double(6, 5, 8, 8, 180, 180, Arc2D.OPEN));
                    veDuongGap(g2, 14, 9, 14, 3);
                    veDuongGap(g2, 10, 13, 10, 16);
                    Path2D.Double day = new Path2D.Double();
                    day.moveTo(10, 16);
                    day.curveTo(10, 20.5, 18, 20.5, 18, 16.7);
                    g2.draw(day);
                    g2.draw(new Ellipse2D.Double(15.8, 12.3, 4.4, 4.4));
                    break;
                case BANG_GHI:
                    g2.draw(new RoundRectangle2D.Double(5, 4.5, 14, 17, 4, 4));
                    g2.draw(new RoundRectangle2D.Double(9, 2.5, 6, 4, 2, 2));
                    veDuongGap(g2, 9, 13.5, 11.2, 15.8, 15, 11.5);
                    break;
                case MUI_TEN_XUONG:
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    veDuongGap(g2, 6, 9, 12, 15, 18, 9);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }
}
