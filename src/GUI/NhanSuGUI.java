package GUI;

import connectDB.ConnectDB;
import dao.BacSi_DAO;
import dao.NhanVien_DAO;
import entity.BacSi_entity;
import entity.NhanVien_entity;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ============================================================================
 * Lớp NhanSuGUI: Giao diện Quản lý Nhân sự (Nhân viên & Bác sĩ) - SALALA Pet
 * Shop
 * ----------------------------------------------------------------------------
 * ĐẶC ĐIỂM CẢI TIẾN:
 * 1. Bỏ nút "Tải lại dữ liệu" ở góc trên.
 * 2. Tách chuyển đổi Nhân viên / Bác sĩ lên thanh điều hướng con (Sub-Nav),
 * loại bỏ hoàn toàn JTabbedPane dính liền trên bảng.
 * 3. Đưa các nút chức năng (Thêm, Xem chi tiết, Chỉnh sửa, Xóa, Xuất Excel)
 * ra HÀNG ĐỘC LẬP bên ngoài bảng, triệt để khắc phục tình trạng chồng đè.
 * 4. Lấy chuẩn thiết kế của Nhân viên làm khuôn mẫu đồng bộ cho tất cả các tab.
 * 5. Loại bỏ hoàn toàn các nút có chức năng trùng lặp nhau.
 * 6. Không dùng icon emoji đa byte tránh lỗi hiển thị ô vuông [] trên Windows
 * Swing.
 * ============================================================================
 */
public class NhanSuGUI extends JPanel {

    private static final long serialVersionUID = 1L;

    // Chế độ hiển thị
    public static final int CHE_DO_NHAN_VIEN = 0;
    public static final int CHE_DO_BAC_SI = 1;

    // =========================================================================
    // REGION 1: HẰNG SỐ & BẢNG MÀU CHUẨN SALALA
    // =========================================================================
    private static final String FONT_NAME = "Segoe UI";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FMT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        MONEY_FMT = new DecimalFormat("#,##0", symbols);
    }

    // Tiêu đề các cột dữ liệu (KHÔNG CÓ CỘT THAO TÁC)
    private static final String[] COT_NHAN_VIEN = {
            "MÃ NV", "HỌ VÀ TÊN", "CHỨC VỤ", "GIỚI TÍNH", "SỐ ĐIỆN THOẠI", "NGÀY SINH", "LƯƠNG CƠ BẢN", "TRẠNG THÁI"
    };

    private static final String[] COT_BAC_SI = {
            "MÃ BÁC SĨ", "HỌ VÀ TÊN", "CHUYÊN NGÀNH", "GIỚI TÍNH", "SỐ ĐIỆN THOẠI", "EMAIL", "SỐ CA KHÁM", "TRẠNG THÁI"
    };

    private static final String[] LOC_CHUC_VU = {
            "Tất cả chức vụ", "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Thu ngân"
    };

    private static final String[] LOC_CHUYEN_NGANH = {
            "Tất cả chuyên ngành", "Nội khoa", "Ngoại khoa", "Da liễu thú y", "Răng hàm mặt", "Sản khoa",
            "Chẩn đoán hình ảnh"
    };

    private static final String[] LOC_TRANG_THAI = {
            "Tất cả trạng thái", "Đang làm việc", "Tạm nghỉ", "Chưa có tài khoản"
    };

    // =========================================================================
    // REGION 2: BIẾN THÀNH PHẦN GIAO DIỆN & DỮ LIỆU
    // =========================================================================
    private int cheDoHienTai = CHE_DO_NHAN_VIEN;

    // DAO
    private final NhanVien_DAO nhanVienDAO = new NhanVien_DAO();
    private final BacSi_DAO bacSiDAO = new BacSi_DAO();

    // Dữ liệu nội bộ
    private final List<NhanVien_entity> danhSachNV = new ArrayList<>();
    private final List<BacSi_entity> danhSachBS = new ArrayList<>();

    // Tiêu đề & mô tả động
    private JLabel lblBreadcrumb;
    private JLabel lblTitle;
    private JLabel lblDesc;

    // 4 Thẻ thống kê KPI
    private JLabel lblKpi1TieuDe, lblKpi1Val, lblKpi1Sub;
    private JLabel lblKpi2TieuDe, lblKpi2Val, lblKpi2Sub;
    private JLabel lblKpi3TieuDe, lblKpi3Val, lblKpi3Sub;
    private JLabel lblKpi4TieuDe, lblKpi4Val, lblKpi4Sub;

    // Container chuyển đổi nội dung
    private JPanel panelNoiDungChinh;
    private CardLayout cardLayout;

    // --- Tab Nhân viên ---
    private JPanel panelViewNhanVien;
    private TableMau bangNhanVien;
    private DefaultTableModel modelNV;
    private TableRowSorter<DefaultTableModel> sorterNV;
    private SearchField txtTimNV;
    private ComboHienDai cboChucVuNV;
    private ComboHienDai cboTrangThaiNV;
    private JButton btnThemNV, btnXemNV, btnSuaNV, btnXoaNV, btnXuatNV;
    private JLabel lblDemNV;
    private JLabel lblAvatarNV;
    private JLabel lblTenNVPreview;
    private JLabel lblChucVuNVPreview;
    private JLabel lblMaNVPreview;
    private JLabel lblSdtNVPreview;
    private JLabel lblLuongNVPreview;
    private JLabel lblTrangThaiNVPreview;

    // --- Tab Bác sĩ ---
    private JPanel panelViewBacSi;
    private TableMau bangBacSi;
    private DefaultTableModel modelBS;
    private TableRowSorter<DefaultTableModel> sorterBS;
    private SearchField txtTimBS;
    private ComboHienDai cboChuyenNganhBS;
    private ComboHienDai cboTrangThaiBS;
    private JButton btnThemBS, btnXemBS, btnSuaBS, btnXoaBS, btnXuatBS;
    private JLabel lblDemBS;
    private JLabel lblAvatarBS;
    private JLabel lblTenBSPreview;
    private JLabel lblChuyenNganhBSPreview;
    private JLabel lblMaBSPreview;
    private JLabel lblSdtBSPreview;
    private JLabel lblSoCaBSPreview;
    private JLabel lblTrangThaiBSPreview;

    // =========================================================================
    // REGION 3: KHỞI TẠO BỐ CỤC CHÍNH (CONSTRUCTORS & LAYOUT)
    // =========================================================================
    public NhanSuGUI() {
        this(CHE_DO_NHAN_VIEN);
    }

    public NhanSuGUI(int cheDoBanDau) {
        this.cheDoHienTai = cheDoBanDau;
        khoiTaoGiaoDien();
        taiDuLieu();
        chuyenCheDo(cheDoBanDau);
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 14));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 20, 24));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        // 1. PHẦN ĐỈNH: Breadcrumb, Thanh điều hướng Sub-Nav, Tiêu đề & 4 thẻ KPI
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.add(taoPhanDieuHuongVaTieuDe());
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(taoHangTheThongKe());
        add(topPanel, BorderLayout.NORTH);

        // 2. PHẦN TRUNG TÂM: CardLayout chứa view Nhân viên và view Bác sĩ độc lập
        cardLayout = new CardLayout();
        panelNoiDungChinh = new JPanel(cardLayout);
        panelNoiDungChinh.setOpaque(false);

        panelViewNhanVien = taoViewNhanVien();
        panelViewBacSi = taoViewBacSi();

        panelNoiDungChinh.add(panelViewNhanVien, "VIEW_NHAN_VIEN");
        panelNoiDungChinh.add(panelViewBacSi, "VIEW_BAC_SI");

        add(panelNoiDungChinh, BorderLayout.CENTER);
    }

    // =========================================================================
    // REGION 4: SUB-NAV, TIÊU ĐỀ & 4 THẺ THỐNG KÊ KPI
    // =========================================================================
    private JPanel taoPhanDieuHuongVaTieuDe() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        // Hàng 1: Breadcrumb
        lblBreadcrumb = new JLabel("Nhân sự / Danh sách nhân viên");
        lblBreadcrumb.setFont(RangBuocGiaoDien.fontThuong(12));
        lblBreadcrumb.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        lblBreadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hàng 2: Tiêu đề trang và mô tả
        JPanel pTieuDe = new JPanel(new BorderLayout());
        pTieuDe.setOpaque(false);
        pTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pChu = new JPanel();
        pChu.setOpaque(false);
        pChu.setLayout(new BoxLayout(pChu, BoxLayout.Y_AXIS));

        lblTitle = new JLabel("Quản lý danh sách nhân viên");
        lblTitle.setFont(RangBuocGiaoDien.fontDam(22));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        lblDesc = new JLabel("Quản lý hồ sơ, chức vụ, thông tin liên hệ và lương cơ bản của nhân viên.");
        lblDesc.setFont(RangBuocGiaoDien.fontThuong(13));
        lblDesc.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pChu.add(lblTitle);
        pChu.add(Box.createVerticalStrut(3));
        pChu.add(lblDesc);
        pTieuDe.add(pChu, BorderLayout.WEST);

        wrap.add(lblBreadcrumb);
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(pTieuDe);

        return wrap;
    }

    private JPanel taoHangTheThongKe() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        lblKpi1TieuDe = new JLabel("TỔNG NHÂN SỰ");
        lblKpi1Val = new JLabel("0");
        lblKpi1Sub = new JLabel("Đang tải dữ liệu");

        lblKpi2TieuDe = new JLabel("ĐANG LÀM VIỆC");
        lblKpi2Val = new JLabel("0");
        lblKpi2Sub = new JLabel("Sẵn sàng làm việc");

        lblKpi3TieuDe = new JLabel("TẠM NGHỈ");
        lblKpi3Val = new JLabel("0");
        lblKpi3Sub = new JLabel("Tạm ngừng công tác");

        lblKpi4TieuDe = new JLabel("CHƯA CÓ TÀI KHOẢN");
        lblKpi4Val = new JLabel("0");
        lblKpi4Sub = new JLabel("Cần cấp tài khoản");

        row.add(taoTheKPI(lblKpi1TieuDe, lblKpi1Val, lblKpi1Sub, RangBuocGiaoDien.MAU_CHU_CHINH));
        row.add(taoTheKPI(lblKpi2TieuDe, lblKpi2Val, lblKpi2Sub, RangBuocGiaoDien.MAU_THANH_CONG_CHU));
        row.add(taoTheKPI(lblKpi3TieuDe, lblKpi3Val, lblKpi3Sub, RangBuocGiaoDien.MAU_CANH_BAO_CHU));
        row.add(taoTheKPI(lblKpi4TieuDe, lblKpi4Val, lblKpi4Sub, RangBuocGiaoDien.MAU_TRUNG_TINH_CHU));

        return row;
    }

    private JPanel taoTheKPI(JLabel lblTieuDe, JLabel lblGiaTri, JLabel lblMoTa, Color mauChuDao) {
        RoundedPanel card = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        lblTieuDe.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblGiaTri.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        lblGiaTri.setForeground(mauChuDao);

        lblMoTa.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTieuDe);
        card.add(Box.createVerticalStrut(4));
        card.add(lblGiaTri);
        card.add(Box.createVerticalStrut(3));
        card.add(lblMoTa);
        return card;
    }

    // =========================================================================
    // REGION 5: VIEW QUẢN LÝ NHÂN VIÊN
    // =========================================================================
    private JPanel taoViewNhanVien() {
        JPanel view = new JPanel(new BorderLayout(16, 12));
        view.setOpaque(false);

        // 1. HÀNG NÚT THAO TÁC ĐỘC LẬP (NẰM RA NGOÀI TABLE)
        JPanel pTopActions = new JPanel(new BorderLayout());
        pTopActions.setOpaque(false);

        JLabel lblSec = new JLabel("DANH SÁCH NHÂN VIÊN");
        lblSec.setFont(RangBuocGiaoDien.fontDam(13));
        lblSec.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        pTopActions.add(lblSec, BorderLayout.WEST);

        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pButtons.setOpaque(false);

        btnThemNV = taoNutChinh("+ Thêm nhân viên");
        btnXemNV = taoNutPhu("Xem chi tiết");
        btnSuaNV = taoNutPhu("Chỉnh sửa");
        btnXoaNV = taoNutNguyHiem("Xóa");
        btnXuatNV = taoNutPhu("Xuất Excel");

        btnXemNV.setEnabled(false);
        btnSuaNV.setEnabled(false);
        btnXoaNV.setEnabled(false);

        btnThemNV.addActionListener(e -> moDialogThemSuaNhanVien(null));
        btnXemNV.addActionListener(e -> moDialogXemChiTietNhanVien());
        btnSuaNV.addActionListener(e -> {
            NhanVien_entity nv = layNhanVienDangChon();
            if (nv != null)
                moDialogThemSuaNhanVien(nv);
        });
        btnXoaNV.addActionListener(e -> xuLyXoaNhanVien());
        btnXuatNV.addActionListener(e -> xuatDuLieuExcel(bangNhanVien, "DanhSach_NhanVien.csv"));

        pButtons.add(btnThemNV);
        pButtons.add(btnXemNV);
        pButtons.add(btnSuaNV);
        pButtons.add(btnXoaNV);
        pButtons.add(btnXuatNV);
        pTopActions.add(pButtons, BorderLayout.EAST);

        view.add(pTopActions, BorderLayout.NORTH);

        // 2. KHUNG TRUNG TÂM: Thẻ bảng bo góc trắng chứa thanh Filter và Table
        RoundedPanel cardBang = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        cardBang.setLayout(new BorderLayout());

        // Thanh Tìm kiếm & Bộ lọc (nằm riêng biệt trong thẻ)
        JPanel filterBar = new JPanel(new BorderLayout(8, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel pFilterInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pFilterInputs.setOpaque(false);

        txtTimNV = new SearchField("Tìm theo tên, mã NV, số điện thoại...");
        txtTimNV.setPreferredSize(new Dimension(240, 36));
        txtTimNV.getDocument().addDocumentListener(new SimpleDocListener(this::locBangNhanVien));

        cboChucVuNV = new ComboHienDai(LOC_CHUC_VU, 175, 36);
        cboChucVuNV.addActionListener(e -> locBangNhanVien());

        cboTrangThaiNV = new ComboHienDai(LOC_TRANG_THAI, 160, 36);
        cboTrangThaiNV.addActionListener(e -> locBangNhanVien());

        JButton btnLamMoiNV = taoNutPhu("Làm mới");
        btnLamMoiNV.addActionListener(e -> {
            txtTimNV.setText("");
            cboChucVuNV.setSelectedIndex(0);
            cboTrangThaiNV.setSelectedIndex(0);
            bangNhanVien.clearSelection();
            taiDuLieu();
        });

        pFilterInputs.add(txtTimNV);
        pFilterInputs.add(cboChucVuNV);
        pFilterInputs.add(cboTrangThaiNV);
        pFilterInputs.add(btnLamMoiNV);

        lblDemNV = new JLabel("Hiển thị: 0 nhân viên");
        lblDemNV.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDemNV.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel pFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pFilterRight.setOpaque(false);
        pFilterRight.add(lblDemNV);

        filterBar.add(pFilterInputs, BorderLayout.WEST);
        filterBar.add(pFilterRight, BorderLayout.EAST);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.add(filterBar, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        cardBang.add(filterWrap, BorderLayout.NORTH);

        // Bảng dữ liệu TableMau
        modelNV = new DefaultTableModel(COT_NHAN_VIEN, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bangNhanVien = new TableMau();
        bangNhanVien.setModel(modelNV);
        cauHinhBang(bangNhanVien, true);

        sorterNV = new TableRowSorter<>(modelNV);
        bangNhanVien.setRowSorter(sorterNV);

        bangNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangNhanVien.getSelectedRow() != -1;
                btnXemNV.setEnabled(coChon);
                btnSuaNV.setEnabled(coChon);
                btnXoaNV.setEnabled(coChon);
                capNhatPreviewNhanVien();
            }
        });

        JScrollPane scroll = new JScrollPane(bangNhanVien);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        cardBang.add(scroll, BorderLayout.CENTER);

        // 3. KHUNG BÊN PHẢI: Hồ sơ tóm tắt & Ảnh đại diện Avatar
        JPanel sidePreview = taoPanelPreviewNhanVien();

        view.add(cardBang, BorderLayout.CENTER);
        view.add(sidePreview, BorderLayout.EAST);
        return view;
    }

    private JPanel taoPanelPreviewNhanVien() {
        RoundedPanel card = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setPreferredSize(new Dimension(280, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 16, 18, 16));

        JLabel lblTieuDe = new JLabel("HỒ SƠ NHÂN VIÊN");
        lblTieuDe.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAvatarNV = new JLabel("NV");
        lblAvatarNV.setPreferredSize(new Dimension(88, 88));
        lblAvatarNV.setMaximumSize(new Dimension(88, 88));
        lblAvatarNV.setOpaque(true);
        lblAvatarNV.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        lblAvatarNV.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblAvatarNV.setFont(new Font(FONT_NAME, Font.BOLD, 26));
        lblAvatarNV.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatarNV.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatarNV.setBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 2, true));

        lblTenNVPreview = new JLabel("Chưa chọn nhân viên");
        lblTenNVPreview.setFont(RangBuocGiaoDien.fontDam(15));
        lblTenNVPreview.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTenNVPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblChucVuNVPreview = new JLabel("Vui lòng click chọn dòng trên bảng");
        lblChucVuNVPreview.setFont(RangBuocGiaoDien.fontThuong(12));
        lblChucVuNVPreview.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblChucVuNVPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTieuDe);
        card.add(Box.createVerticalStrut(14));
        card.add(lblAvatarNV);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTenNVPreview);
        card.add(Box.createVerticalStrut(2));
        card.add(lblChucVuNVPreview);
        card.add(Box.createVerticalStrut(14));
        card.add(new JSeparator(SwingConstants.HORIZONTAL));
        card.add(Box.createVerticalStrut(14));

        JPanel pThongTin = new JPanel(new GridLayout(4, 2, 6, 10));
        pThongTin.setOpaque(false);
        pThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblMaNVPreview = new JLabel("---");
        lblSdtNVPreview = new JLabel("---");
        lblLuongNVPreview = new JLabel("---");
        lblTrangThaiNVPreview = new JLabel("---");

        themDongTieuDeGiaTri(pThongTin, "Mã nhân viên:", lblMaNVPreview);
        themDongTieuDeGiaTri(pThongTin, "Số điện thoại:", lblSdtNVPreview);
        themDongTieuDeGiaTri(pThongTin, "Lương cơ bản:", lblLuongNVPreview);
        themDongTieuDeGiaTri(pThongTin, "Trạng thái:", lblTrangThaiNVPreview);

        card.add(pThongTin);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // =========================================================================
    // REGION 6: VIEW QUẢN LÝ BÁC SĨ (ĐỒNG BỘ CHUẨN VỚI NHÂN VIÊN)
    // =========================================================================
    private JPanel taoViewBacSi() {
        JPanel view = new JPanel(new BorderLayout(16, 12));
        view.setOpaque(false);

        // 1. HÀNG NÚT THAO TÁC ĐỘC LẬP (NẰM RA NGOÀI TABLE)
        JPanel pTopActions = new JPanel(new BorderLayout());
        pTopActions.setOpaque(false);

        JLabel lblSec = new JLabel("DANH SÁCH BÁC SĨ THÚ Y");
        lblSec.setFont(RangBuocGiaoDien.fontDam(13));
        lblSec.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        pTopActions.add(lblSec, BorderLayout.WEST);

        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pButtons.setOpaque(false);

        btnThemBS = taoNutChinh("+ Thêm bác sĩ");
        btnXemBS = taoNutPhu("Xem chi tiết");
        btnSuaBS = taoNutPhu("Chỉnh sửa");
        btnXoaBS = taoNutNguyHiem("Xóa");
        btnXuatBS = taoNutPhu("Xuất Excel");

        btnXemBS.setEnabled(false);
        btnSuaBS.setEnabled(false);
        btnXoaBS.setEnabled(false);

        btnThemBS.addActionListener(e -> moDialogThemSuaBacSi(null));
        btnXemBS.addActionListener(e -> moDialogXemChiTietBacSi());
        btnSuaBS.addActionListener(e -> {
            BacSi_entity bs = layBacSiDangChon();
            if (bs != null)
                moDialogThemSuaBacSi(bs);
        });
        btnXoaBS.addActionListener(e -> xuLyXoaBacSi());
        btnXuatBS.addActionListener(e -> xuatDuLieuExcel(bangBacSi, "DanhSach_BacSi.csv"));

        pButtons.add(btnThemBS);
        pButtons.add(btnXemBS);
        pButtons.add(btnSuaBS);
        pButtons.add(btnXoaBS);
        pButtons.add(btnXuatBS);
        pTopActions.add(pButtons, BorderLayout.EAST);

        view.add(pTopActions, BorderLayout.NORTH);

        // 2. KHUNG TRUNG TÂM: Thẻ bảng bo góc trắng chứa thanh Filter và Table
        RoundedPanel cardBang = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        cardBang.setLayout(new BorderLayout());

        JPanel filterBar = new JPanel(new BorderLayout(8, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel pFilterInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pFilterInputs.setOpaque(false);

        txtTimBS = new SearchField("Tìm theo tên, mã BS, chuyên ngành...");
        txtTimBS.setPreferredSize(new Dimension(240, 36));
        txtTimBS.getDocument().addDocumentListener(new SimpleDocListener(this::locBangBacSi));

        cboChuyenNganhBS = new ComboHienDai(LOC_CHUYEN_NGANH, 185, 36);
        cboChuyenNganhBS.addActionListener(e -> locBangBacSi());

        cboTrangThaiBS = new ComboHienDai(LOC_TRANG_THAI, 160, 36);
        cboTrangThaiBS.addActionListener(e -> locBangBacSi());

        JButton btnLamMoiBS = taoNutPhu("Làm mới");
        btnLamMoiBS.addActionListener(e -> {
            txtTimBS.setText("");
            cboChuyenNganhBS.setSelectedIndex(0);
            cboTrangThaiBS.setSelectedIndex(0);
            bangBacSi.clearSelection();
            taiDuLieu();
        });

        pFilterInputs.add(txtTimBS);
        pFilterInputs.add(cboChuyenNganhBS);
        pFilterInputs.add(cboTrangThaiBS);
        pFilterInputs.add(btnLamMoiBS);

        lblDemBS = new JLabel("Hiển thị: 0 bác sĩ");
        lblDemBS.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDemBS.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel pFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pFilterRight.setOpaque(false);
        pFilterRight.add(lblDemBS);

        filterBar.add(pFilterInputs, BorderLayout.WEST);
        filterBar.add(pFilterRight, BorderLayout.EAST);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.add(filterBar, BorderLayout.CENTER);
        filterWrap.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        cardBang.add(filterWrap, BorderLayout.NORTH);

        // Bảng dữ liệu TableMau
        modelBS = new DefaultTableModel(COT_BAC_SI, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bangBacSi = new TableMau();
        bangBacSi.setModel(modelBS);
        cauHinhBang(bangBacSi, false);

        sorterBS = new TableRowSorter<>(modelBS);
        bangBacSi.setRowSorter(sorterBS);

        bangBacSi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = bangBacSi.getSelectedRow() != -1;
                btnXemBS.setEnabled(coChon);
                btnSuaBS.setEnabled(coChon);
                btnXoaBS.setEnabled(coChon);
                capNhatPreviewBacSi();
            }
        });

        JScrollPane scroll = new JScrollPane(bangBacSi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        cardBang.add(scroll, BorderLayout.CENTER);

        // 3. KHUNG BÊN PHẢI: Hồ sơ tóm tắt & Ảnh đại diện Avatar
        JPanel sidePreview = taoPanelPreviewBacSi();

        view.add(cardBang, BorderLayout.CENTER);
        view.add(sidePreview, BorderLayout.EAST);
        return view;
    }

    private JPanel taoPanelPreviewBacSi() {
        RoundedPanel card = new RoundedPanel(12, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        card.setPreferredSize(new Dimension(280, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 16, 18, 16));

        JLabel lblTieuDe = new JLabel("HỒ SƠ BÁC SĨ THÚ Y");
        lblTieuDe.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblTieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAvatarBS = new JLabel("BS");
        lblAvatarBS.setPreferredSize(new Dimension(88, 88));
        lblAvatarBS.setMaximumSize(new Dimension(88, 88));
        lblAvatarBS.setOpaque(true);
        lblAvatarBS.setBackground(RangBuocGiaoDien.MAU_THONG_TIN_NEN);
        lblAvatarBS.setForeground(RangBuocGiaoDien.MAU_THONG_TIN_CHU);
        lblAvatarBS.setFont(new Font(FONT_NAME, Font.BOLD, 26));
        lblAvatarBS.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatarBS.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatarBS.setBorder(new LineBorder(new Color(186, 230, 253), 2, true));

        lblTenBSPreview = new JLabel("Chưa chọn bác sĩ");
        lblTenBSPreview.setFont(RangBuocGiaoDien.fontDam(15));
        lblTenBSPreview.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTenBSPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblChuyenNganhBSPreview = new JLabel("Vui lòng click chọn dòng trên bảng");
        lblChuyenNganhBSPreview.setFont(RangBuocGiaoDien.fontThuong(12));
        lblChuyenNganhBSPreview.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        lblChuyenNganhBSPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTieuDe);
        card.add(Box.createVerticalStrut(14));
        card.add(lblAvatarBS);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTenBSPreview);
        card.add(Box.createVerticalStrut(2));
        card.add(lblChuyenNganhBSPreview);
        card.add(Box.createVerticalStrut(14));
        card.add(new JSeparator(SwingConstants.HORIZONTAL));
        card.add(Box.createVerticalStrut(14));

        JPanel pThongTin = new JPanel(new GridLayout(4, 2, 6, 10));
        pThongTin.setOpaque(false);
        pThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblMaBSPreview = new JLabel("---");
        lblSdtBSPreview = new JLabel("---");
        lblSoCaBSPreview = new JLabel("---");
        lblTrangThaiBSPreview = new JLabel("---");

        themDongTieuDeGiaTri(pThongTin, "Mã bác sĩ:", lblMaBSPreview);
        themDongTieuDeGiaTri(pThongTin, "Số điện thoại:", lblSdtBSPreview);
        themDongTieuDeGiaTri(pThongTin, "Số ca khám:", lblSoCaBSPreview);
        themDongTieuDeGiaTri(pThongTin, "Trạng thái:", lblTrangThaiBSPreview);

        card.add(pThongTin);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private void themDongTieuDeGiaTri(JPanel panel, String tieuDe, JLabel lblGiaTri) {
        JLabel t = new JLabel(tieuDe);
        t.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        t.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblGiaTri.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        lblGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        panel.add(t);
        panel.add(lblGiaTri);
    }

    // =========================================================================
    // REGION 7: CHUYỂN ĐỔI CHẾ ĐỘ HIỂN THỊ (NHÂN VIÊN <-> BÁC SĨ)
    // =========================================================================
    public void chuyenCheDo(int cheDoMoi) {
        this.cheDoHienTai = cheDoMoi;
        boolean laNhanVien = (cheDoMoi == CHE_DO_NHAN_VIEN);

        if (laNhanVien) {
            lblBreadcrumb.setText("Nhân sự / Danh sách nhân viên");
            lblTitle.setText("Quản lý danh sách nhân viên");
            lblDesc.setText("Quản lý hồ sơ, chức vụ, thông tin liên hệ và lương cơ bản của nhân sự.");
            cardLayout.show(panelNoiDungChinh, "VIEW_NHAN_VIEN");
            capNhatThongKeNhanVien();
        } else {
            lblBreadcrumb.setText("Nhân sự / Danh sách bác sĩ");
            lblTitle.setText("Quản lý danh sách bác sĩ thú y");
            lblDesc.setText("Quản lý hồ sơ bác sĩ thú y, chuyên ngành điều trị, số ca khám và trạng thái.");
            cardLayout.show(panelNoiDungChinh, "VIEW_BAC_SI");
            capNhatThongKeBacSi();
        }
    }

    public void chonTabNhanVien() {
        chuyenCheDo(CHE_DO_NHAN_VIEN);
    }

    public void chonTabBacSi() {
        chuyenCheDo(CHE_DO_BAC_SI);
    }

    private void capNhatThongKeNhanVien() {
        int tong = danhSachNV.size();
        int dangLam = 0;
        int tamNghi = 0;
        int chuaTK = 0;

        for (NhanVien_entity nv : danhSachNV) {
            if (nv.getTrangThaiTK() == null) {
                chuaTK++;
            } else if (nv.getTrangThaiTK()) {
                dangLam++;
            } else {
                tamNghi++;
            }
        }

        lblKpi1TieuDe.setText("TỔNG NHÂN VIÊN");
        lblKpi1Val.setText(String.valueOf(tong));
        lblKpi1Sub.setText("Nhân sự toàn hệ thống");

        lblKpi2TieuDe.setText("ĐANG LÀM VIỆC");
        lblKpi2Val.setText(String.valueOf(dangLam));
        lblKpi2Sub.setText("Đang công tác tại shop");

        lblKpi3TieuDe.setText("TẠM NGHỈ");
        lblKpi3Val.setText(String.valueOf(tamNghi));
        lblKpi3Sub.setText("Đang tạm ngừng làm");

        lblKpi4TieuDe.setText("CHƯA CÓ TÀI KHOẢN");
        lblKpi4Val.setText(String.valueOf(chuaTK));
        lblKpi4Sub.setText("Cần kích hoạt tài khoản");
    }

    private void capNhatThongKeBacSi() {
        int tong = danhSachBS.size();
        int dangLam = 0;
        int tamNghi = 0;
        int chuaTK = 0;

        for (BacSi_entity bs : danhSachBS) {
            if (bs.getTrangThaiTK() == null) {
                chuaTK++;
            } else if (bs.getTrangThaiTK()) {
                dangLam++;
            } else {
                tamNghi++;
            }
        }

        lblKpi1TieuDe.setText("TỔNG BÁC SĨ THÚ Y");
        lblKpi1Val.setText(String.valueOf(tong));
        lblKpi1Sub.setText("Bác sĩ chuyên khoa");

        lblKpi2TieuDe.setText("ĐANG LÀM VIỆC");
        lblKpi2Val.setText(String.valueOf(dangLam));
        lblKpi2Sub.setText("Sẵn sàng khám bệnh");

        lblKpi3TieuDe.setText("TẠM NGHỈ");
        lblKpi3Val.setText(String.valueOf(tamNghi));
        lblKpi3Sub.setText("Nghỉ phép / đào tạo");

        lblKpi4TieuDe.setText("CHƯA CÓ TÀI KHOẢN");
        lblKpi4Val.setText(String.valueOf(chuaTK));
        lblKpi4Sub.setText("Cần kích hoạt tài khoản");
    }

    private void capNhatPreviewNhanVien() {
        NhanVien_entity nv = layNhanVienDangChon();
        if (nv == null) {
            lblAvatarNV.setText("NV");
            lblAvatarNV.setIcon(null);
            lblTenNVPreview.setText("Chưa chọn nhân viên");
            lblChucVuNVPreview.setText("Vui lòng click chọn dòng trên bảng");
            lblMaNVPreview.setText("---");
            lblSdtNVPreview.setText("---");
            lblLuongNVPreview.setText("---");
            lblTrangThaiNVPreview.setText("---");
            return;
        }

        String[] parts = nv.getHoTen().trim().split("\\s+");
        String avtTxt = parts.length >= 2
                ? (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase()
                : nv.getHoTen().substring(0, Math.min(2, nv.getHoTen().length())).toUpperCase();

        lblAvatarNV.setText(avtTxt);
        lblTenNVPreview.setText(nv.getHoTen());
        lblChucVuNVPreview.setText(nv.getChucVu());
        lblMaNVPreview.setText(nv.getMaNhanVien());
        lblSdtNVPreview.setText(nv.getSdt() != null ? nv.getSdt() : "---");
        lblLuongNVPreview.setText(MONEY_FMT.format(nv.getLuongCoBan()) + " đ");

        String tt = "Chưa có tài khoản";
        if (nv.getTrangThaiTK() != null) {
            tt = nv.getTrangThaiTK() ? "Đang làm việc" : "Tạm nghỉ";
        }
        lblTrangThaiNVPreview.setText(tt);
    }

    private void capNhatPreviewBacSi() {
        BacSi_entity bs = layBacSiDangChon();
        if (bs == null) {
            lblAvatarBS.setText("BS");
            lblAvatarBS.setIcon(null);
            lblTenBSPreview.setText("Chưa chọn bác sĩ");
            lblChuyenNganhBSPreview.setText("Vui lòng click chọn dòng trên bảng");
            lblMaBSPreview.setText("---");
            lblSdtBSPreview.setText("---");
            lblSoCaBSPreview.setText("---");
            lblTrangThaiBSPreview.setText("---");
            return;
        }

        String[] parts = bs.getHoTen().trim().split("\\s+");
        String avtTxt = parts.length >= 2
                ? (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase()
                : bs.getHoTen().substring(0, Math.min(2, bs.getHoTen().length())).toUpperCase();

        lblAvatarBS.setText(avtTxt);
        lblTenBSPreview.setText(bs.getHoTen());
        lblChuyenNganhBSPreview.setText("Khoa: " + bs.getChuyenNganh());
        lblMaBSPreview.setText(bs.getMaBacSi());
        lblSdtBSPreview.setText(bs.getSdt() != null ? bs.getSdt() : "---");
        lblSoCaBSPreview.setText(bs.getSoCaKham() + " ca khám");

        String tt = "Chưa có tài khoản";
        if (bs.getTrangThaiTK() != null) {
            tt = bs.getTrangThaiTK() ? "Đang làm việc" : "Tạm nghỉ";
        }
        lblTrangThaiBSPreview.setText(tt);
    }

    // =========================================================================
    // REGION 8: XỬ LÝ LỌC & TÌM KIẾM
    // =========================================================================
    private void cauHinhBang(TableMau table, boolean laNhanVien) {
        table.setRowHeight(40);
        table.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        header.setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
        header.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_CHINH, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(1)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.LEFT));
        table.getColumnModel().getColumn(2)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(3)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_PHU, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(4)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_CHINH, Font.PLAIN, SwingConstants.LEFT));
        table.getColumnModel().getColumn(5)
                .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_PHU, Font.PLAIN, SwingConstants.LEFT));

        if (laNhanVien) {
            table.getColumnModel().getColumn(6).setCellRenderer(new CurrencyRenderer());
        } else {
            table.getColumnModel().getColumn(6)
                    .setCellRenderer(new TextRenderer(RangBuocGiaoDien.MAU_CHU_DAO, Font.BOLD, SwingConstants.LEFT));
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new BadgeStatusRenderer());

        int[] widthsNV = { 85, 180, 160, 75, 110, 100, 120, 130 };
        int[] widthsBS = { 85, 180, 140, 75, 110, 160, 95, 130 };
        int[] widths = laNhanVien ? widthsNV : widthsBS;
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void locBangNhanVien() {
        String kw = txtTimNV.getText().trim().toLowerCase();
        int idxChucVu = cboChucVuNV.getSelectedIndex();
        int idxTrangThai = cboTrangThaiNV.getSelectedIndex();

        String valChucVu = idxChucVu > 0 ? (String) cboChucVuNV.getSelectedItem() : null;
        String valTrangThai = idxTrangThai > 0 ? (String) cboTrangThaiNV.getSelectedItem() : null;

        sorterNV.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String ma = entry.getStringValue(0).toLowerCase();
                String ten = entry.getStringValue(1).toLowerCase();
                String sdt = entry.getStringValue(4).toLowerCase();
                String cv = entry.getStringValue(2);
                String tt = entry.getStringValue(7);

                boolean matchKw = kw.isEmpty() || ma.contains(kw) || ten.contains(kw) || sdt.contains(kw);
                boolean matchCv = (valChucVu == null) || cv.equalsIgnoreCase(valChucVu);
                boolean matchTt = (valTrangThai == null) || tt.equalsIgnoreCase(valTrangThai);
                return matchKw && matchCv && matchTt;
            }
        });

        lblDemNV.setText("Hiển thị: " + bangNhanVien.getRowCount() + " / " + danhSachNV.size() + " nhân viên");
    }

    private void locBangBacSi() {
        String kw = txtTimBS.getText().trim().toLowerCase();
        int idxChuyenNganh = cboChuyenNganhBS.getSelectedIndex();
        int idxTrangThai = cboTrangThaiBS.getSelectedIndex();

        String valChuyenNganh = idxChuyenNganh > 0 ? (String) cboChuyenNganhBS.getSelectedItem() : null;
        String valTrangThai = idxTrangThai > 0 ? (String) cboTrangThaiBS.getSelectedItem() : null;

        sorterBS.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String ma = entry.getStringValue(0).toLowerCase();
                String ten = entry.getStringValue(1).toLowerCase();
                String sdt = entry.getStringValue(4).toLowerCase();
                String email = entry.getStringValue(5).toLowerCase();
                String cn = entry.getStringValue(2);
                String tt = entry.getStringValue(7);

                boolean matchKw = kw.isEmpty() || ma.contains(kw) || ten.contains(kw) || sdt.contains(kw)
                        || email.contains(kw);
                boolean matchCn = (valChuyenNganh == null) || cn.equalsIgnoreCase(valChuyenNganh);
                boolean matchTt = (valTrangThai == null) || tt.equalsIgnoreCase(valTrangThai);
                return matchKw && matchCn && matchTt;
            }
        });

        lblDemBS.setText("Hiển thị: " + bangBacSi.getRowCount() + " / " + danhSachBS.size() + " bác sĩ");
    }

    private NhanVien_entity layNhanVienDangChon() {
        int row = bangNhanVien.getSelectedRow();
        if (row == -1)
            return null;
        int modelRow = bangNhanVien.convertRowIndexToModel(row);
        String maNV = (String) modelNV.getValueAt(modelRow, 0);
        for (NhanVien_entity nv : danhSachNV) {
            if (nv.getMaNhanVien().equalsIgnoreCase(maNV))
                return nv;
        }
        return null;
    }

    private BacSi_entity layBacSiDangChon() {
        int row = bangBacSi.getSelectedRow();
        if (row == -1)
            return null;
        int modelRow = bangBacSi.convertRowIndexToModel(row);
        String maBS = (String) modelBS.getValueAt(modelRow, 0);
        for (BacSi_entity bs : danhSachBS) {
            if (bs.getMaBacSi().equalsIgnoreCase(maBS))
                return bs;
        }
        return null;
    }

    // =========================================================================
    // REGION 9: DIALOG CHI TIẾT, THÊM MỚI, CHỈNH SỬA, XÓA
    // =========================================================================
    private void moDialogXemChiTietNhanVien() {
        NhanVien_entity nv = layNhanVienDangChon();
        if (nv == null)
            return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Chi tiết hồ sơ nhân viên - " + nv.getMaNhanVien(), true);
        dlg.setSize(480, 500);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblAvt = new JLabel();
        lblAvt.setPreferredSize(new Dimension(80, 80));
        lblAvt.setOpaque(true);
        lblAvt.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
        lblAvt.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblAvt.setFont(new Font(FONT_NAME, Font.BOLD, 28));
        lblAvt.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvt.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvt.setBorder(new LineBorder(RangBuocGiaoDien.MAU_VIEN_CHU_DAO, 2, true));

        String[] parts = nv.getHoTen().trim().split("\\s+");
        String avtTxt = parts.length >= 2
                ? (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase()
                : "NV";
        lblAvt.setText(avtTxt);

        JLabel lblTen = new JLabel(nv.getHoTen());
        lblTen.setFont(RangBuocGiaoDien.fontDam(18));
        lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblChucVu = new JLabel(nv.getChucVu());
        lblChucVu.setFont(RangBuocGiaoDien.fontThuong(13));
        lblChucVu.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblChucVu.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(lblAvt);
        p.add(Box.createVerticalStrut(8));
        p.add(lblTen);
        p.add(Box.createVerticalStrut(2));
        p.add(lblChucVu);
        p.add(Box.createVerticalStrut(16));

        JPanel pThongTin = new JPanel(new GridLayout(6, 2, 8, 10));
        pThongTin.setOpaque(false);
        pThongTin.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        themDongTieuDeGiaTri(pThongTin, "Mã nhân viên:", new JLabel(nv.getMaNhanVien()));
        themDongTieuDeGiaTri(pThongTin, "Giới tính:", new JLabel(nv.getGioiTinh()));
        themDongTieuDeGiaTri(pThongTin, "Số điện thoại:", new JLabel(nv.getSdt()));
        themDongTieuDeGiaTri(pThongTin, "Ngày sinh:",
                new JLabel(nv.getNgaySinh() != null ? DATE_FMT.format(nv.getNgaySinh()) : "---"));
        themDongTieuDeGiaTri(pThongTin, "Lương cơ bản:", new JLabel(MONEY_FMT.format(nv.getLuongCoBan()) + " VNĐ"));
        themDongTieuDeGiaTri(pThongTin, "Địa chỉ:", new JLabel(nv.getDiaChi() != null ? nv.getDiaChi() : "---"));

        p.add(pThongTin);
        p.add(Box.createVerticalStrut(18));

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pBtn.setOpaque(false);

        JButton btnSua = taoNutPhu("Chỉnh sửa");
        btnSua.addActionListener(e -> {
            dlg.dispose();
            moDialogThemSuaNhanVien(nv);
        });

        JButton btnDong = taoNutChinh("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());

        pBtn.add(btnSua);
        pBtn.add(btnDong);
        p.add(pBtn);

        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void moDialogXemChiTietBacSi() {
        BacSi_entity bs = layBacSiDangChon();
        if (bs == null)
            return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Chi tiết hồ sơ bác sĩ - " + bs.getMaBacSi(), true);
        dlg.setSize(480, 500);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblAvt = new JLabel();
        lblAvt.setPreferredSize(new Dimension(80, 80));
        lblAvt.setOpaque(true);
        lblAvt.setBackground(RangBuocGiaoDien.MAU_THONG_TIN_NEN);
        lblAvt.setForeground(RangBuocGiaoDien.MAU_THONG_TIN_CHU);
        lblAvt.setFont(new Font(FONT_NAME, Font.BOLD, 28));
        lblAvt.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvt.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvt.setBorder(new LineBorder(new Color(186, 230, 253), 2, true));

        String[] parts = bs.getHoTen().trim().split("\\s+");
        String avtTxt = parts.length >= 2
                ? (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase()
                : "BS";
        lblAvt.setText(avtTxt);

        JLabel lblTen = new JLabel(bs.getHoTen());
        lblTen.setFont(RangBuocGiaoDien.fontDam(18));
        lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblChuyenNganh = new JLabel("Bác sĩ Thú y · Khoa " + bs.getChuyenNganh());
        lblChuyenNganh.setFont(RangBuocGiaoDien.fontThuong(13));
        lblChuyenNganh.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        lblChuyenNganh.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(lblAvt);
        p.add(Box.createVerticalStrut(8));
        p.add(lblTen);
        p.add(Box.createVerticalStrut(2));
        p.add(lblChuyenNganh);
        p.add(Box.createVerticalStrut(16));

        JPanel pThongTin = new JPanel(new GridLayout(6, 2, 8, 10));
        pThongTin.setOpaque(false);
        pThongTin.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        themDongTieuDeGiaTri(pThongTin, "Mã bác sĩ:", new JLabel(bs.getMaBacSi()));
        themDongTieuDeGiaTri(pThongTin, "Giới tính:", new JLabel(bs.getGioiTinh()));
        themDongTieuDeGiaTri(pThongTin, "Số điện thoại:", new JLabel(bs.getSdt()));
        themDongTieuDeGiaTri(pThongTin, "Email:", new JLabel(bs.getEmail() != null ? bs.getEmail() : "---"));
        themDongTieuDeGiaTri(pThongTin, "Ngày sinh:",
                new JLabel(bs.getNgaySinh() != null ? DATE_FMT.format(bs.getNgaySinh()) : "---"));
        themDongTieuDeGiaTri(pThongTin, "Số ca khám:", new JLabel(bs.getSoCaKham() + " ca"));

        p.add(pThongTin);
        p.add(Box.createVerticalStrut(18));

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pBtn.setOpaque(false);

        JButton btnSua = taoNutPhu("Chỉnh sửa");
        btnSua.addActionListener(e -> {
            dlg.dispose();
            moDialogThemSuaBacSi(bs);
        });

        JButton btnDong = taoNutChinh("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());

        pBtn.add(btnSua);
        pBtn.add(btnDong);
        p.add(pBtn);

        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void moDialogThemSuaNhanVien(NhanVien_entity nvSua) {
        boolean laThem = (nvSua == null);
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, laThem ? "Thêm mới nhân viên" : "Cập nhật nhân viên " + nvSua.getMaNhanVien(),
                true);
        dlg.setSize(480, 520);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel pForm = new JPanel(new GridLayout(8, 2, 10, 12));
        pForm.setBackground(Color.WHITE);
        pForm.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtMa = new JTextField(laThem ? sinhMaNVMoi() : nvSua.getMaNhanVien());
        txtMa.setEditable(false);
        txtMa.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);

        JTextField txtTen = new JTextField(laThem ? "" : nvSua.getHoTen());
        JComboBox<String> cbChucVu = new JComboBox<>(
                new String[] { "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Thu ngân" });
        if (!laThem)
            cbChucVu.setSelectedItem(nvSua.getChucVu());

        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
        if (!laThem)
            cbGioiTinh.setSelectedItem(nvSua.getGioiTinh());

        JTextField txtSdt = new JTextField(laThem ? "" : nvSua.getSdt());
        JTextField txtNgaySinh = new JTextField(
                laThem ? "15/08/1998" : (nvSua.getNgaySinh() != null ? DATE_FMT.format(nvSua.getNgaySinh()) : ""));
        JTextField txtLuong = new JTextField(laThem ? "8000000" : String.valueOf((long) nvSua.getLuongCoBan()));
        JTextField txtDiaChi = new JTextField(laThem ? "" : (nvSua.getDiaChi() != null ? nvSua.getDiaChi() : ""));

        themDongForm(pForm, "Mã nhân viên (*):", txtMa);
        themDongForm(pForm, "Họ và tên (*):", txtTen);
        themDongForm(pForm, "Chức vụ (*):", cbChucVu);
        themDongForm(pForm, "Giới tính:", cbGioiTinh);
        themDongForm(pForm, "Số điện thoại (*):", txtSdt);
        themDongForm(pForm, "Ngày sinh (dd/MM/yyyy):", txtNgaySinh);
        themDongForm(pForm, "Lương cơ bản (VNĐ):", txtLuong);
        themDongForm(pForm, "Địa chỉ liên hệ:", txtDiaChi);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        pBtn.setBackground(Color.WHITE);
        pBtn.setBorder(new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        JButton btnHuy = taoNutPhu("Hủy bỏ");
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = taoNutChinh(laThem ? "Thêm nhân viên" : "Lưu thông tin");
        btnLuu.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập họ và tên nhân viên!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sdt.matches("0\\d{9}")) {
                JOptionPane.showMessageDialog(dlg, "Số điện thoại phải bắt đầu bằng 0 và gồm 10 chữ số!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double luong = 7000000;
            try {
                luong = Double.parseDouble(txtLuong.getText().trim().replace(".", "").replace(",", ""));
            } catch (Exception ignored) {
            }

            LocalDate ns = null;
            try {
                ns = LocalDate.parse(txtNgaySinh.getText().trim(), DATE_FMT);
            } catch (Exception ignored) {
            }

            if (laThem) {
                NhanVien_entity nvMoi = new NhanVien_entity(
                        txtMa.getText().trim(), null, ten, (String) cbChucVu.getSelectedItem(),
                        sdt, txtDiaChi.getText().trim(), ns, (String) cbGioiTinh.getSelectedItem(),
                        luong, true);
                try {
                    nhanVienDAO.them(nvMoi);
                } catch (Exception ex) {
                    System.err.println("Lỗi lưu DB (dùng bộ nhớ tạm): " + ex.getMessage());
                }
                danhSachNV.add(0, nvMoi);
                hienThiDuLieuLenBangNV();
                JOptionPane.showMessageDialog(dlg, "Thêm nhân viên mới thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                nvSua.setHoTen(ten);
                nvSua.setChucVu((String) cbChucVu.getSelectedItem());
                nvSua.setGioiTinh((String) cbGioiTinh.getSelectedItem());
                nvSua.setSdt(sdt);
                nvSua.setNgaySinh(ns);
                nvSua.setLuongCoBan(luong);
                nvSua.setDiaChi(txtDiaChi.getText().trim());

                try {
                    nhanVienDAO.capNhat(nvSua);
                } catch (Exception ex) {
                    System.err.println("Lỗi update DB: " + ex.getMessage());
                }
                hienThiDuLieuLenBangNV();
                capNhatPreviewNhanVien();
                JOptionPane.showMessageDialog(dlg, "Cập nhật thông tin nhân viên thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            capNhatThongKeNhanVien();
            dlg.dispose();
        });

        pBtn.add(btnHuy);
        pBtn.add(btnLuu);

        dlg.add(pForm, BorderLayout.CENTER);
        dlg.add(pBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void moDialogThemSuaBacSi(BacSi_entity bsSua) {
        boolean laThem = (bsSua == null);
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, laThem ? "Thêm mới bác sĩ thú y" : "Cập nhật bác sĩ " + bsSua.getMaBacSi(),
                true);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel pForm = new JPanel(new GridLayout(7, 2, 10, 12));
        pForm.setBackground(Color.WHITE);
        pForm.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtMa = new JTextField(laThem ? sinhMaBSMoi() : bsSua.getMaBacSi());
        txtMa.setEditable(false);
        txtMa.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);

        JTextField txtTen = new JTextField(laThem ? "" : bsSua.getHoTen());
        JComboBox<String> cbChuyenNganh = new JComboBox<>(new String[] { "Nội khoa", "Ngoại khoa", "Da liễu thú y",
                "Răng hàm mặt", "Sản khoa", "Chẩn đoán hình ảnh" });
        if (!laThem)
            cbChuyenNganh.setSelectedItem(bsSua.getChuyenNganh());

        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
        if (!laThem)
            cbGioiTinh.setSelectedItem(bsSua.getGioiTinh());

        JTextField txtSdt = new JTextField(laThem ? "" : bsSua.getSdt());
        JTextField txtEmail = new JTextField(laThem ? "" : (bsSua.getEmail() != null ? bsSua.getEmail() : ""));
        JTextField txtNgaySinh = new JTextField(
                laThem ? "20/10/1992" : (bsSua.getNgaySinh() != null ? DATE_FMT.format(bsSua.getNgaySinh()) : ""));

        themDongForm(pForm, "Mã bác sĩ (*):", txtMa);
        themDongForm(pForm, "Họ và tên (*):", txtTen);
        themDongForm(pForm, "Chuyên ngành (*):", cbChuyenNganh);
        themDongForm(pForm, "Giới tính:", cbGioiTinh);
        themDongForm(pForm, "Số điện thoại (*):", txtSdt);
        themDongForm(pForm, "Email liên hệ:", txtEmail);
        themDongForm(pForm, "Ngày sinh (dd/MM/yyyy):", txtNgaySinh);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        pBtn.setBackground(Color.WHITE);
        pBtn.setBorder(new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));

        JButton btnHuy = taoNutPhu("Hủy bỏ");
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = taoNutChinh(laThem ? "Thêm bác sĩ" : "Lưu thông tin");
        btnLuu.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập họ và tên bác sĩ!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sdt.matches("0\\d{9}")) {
                JOptionPane.showMessageDialog(dlg, "Số điện thoại phải bắt đầu bằng 0 và gồm 10 chữ số!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate ns = null;
            try {
                ns = LocalDate.parse(txtNgaySinh.getText().trim(), DATE_FMT);
            } catch (Exception ignored) {
            }

            if (laThem) {
                BacSi_entity bsMoi = new BacSi_entity(
                        txtMa.getText().trim(), null, ten, (String) cbGioiTinh.getSelectedItem(),
                        sdt, (String) cbChuyenNganh.getSelectedItem(), txtEmail.getText().trim(), ns,
                        0, true);
                try {
                    bacSiDAO.them(bsMoi);
                } catch (Exception ex) {
                    System.err.println("Lỗi lưu DB: " + ex.getMessage());
                }
                danhSachBS.add(0, bsMoi);
                hienThiDuLieuLenBangBS();
                JOptionPane.showMessageDialog(dlg, "Thêm bác sĩ mới thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                bsSua.setHoTen(ten);
                bsSua.setChuyenNganh((String) cbChuyenNganh.getSelectedItem());
                bsSua.setGioiTinh((String) cbGioiTinh.getSelectedItem());
                bsSua.setSdt(sdt);
                bsSua.setEmail(txtEmail.getText().trim());
                bsSua.setNgaySinh(ns);

                try {
                    bacSiDAO.capNhat(bsSua);
                } catch (Exception ex) {
                    System.err.println("Lỗi update DB: " + ex.getMessage());
                }
                hienThiDuLieuLenBangBS();
                capNhatPreviewBacSi();
                JOptionPane.showMessageDialog(dlg, "Cập nhật thông tin bác sĩ thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            capNhatThongKeBacSi();
            dlg.dispose();
        });

        pBtn.add(btnHuy);
        pBtn.add(btnLuu);

        dlg.add(pForm, BorderLayout.CENTER);
        dlg.add(pBtn, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void themDongForm(JPanel panel, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setFont(RangBuocGiaoDien.fontDam(12));
        l.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        comp.setFont(RangBuocGiaoDien.fontThuong(12));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setBorder(new CompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(4, 8, 4, 8)));
        }
        panel.add(l);
        panel.add(comp);
    }

    private void xuLyXoaNhanVien() {
        NhanVien_entity nv = layNhanVienDangChon();
        if (nv == null)
            return;

        int opt = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa nhân viên [" + nv.getMaNhanVien() + " - " + nv.getHoTen()
                        + "] không?\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa nhân viên",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                nhanVienDAO.xoa(nv.getMaNhanVien());
            } catch (Exception ex) {
                System.err.println("Lỗi xóa trong DB: " + ex.getMessage());
            }
            danhSachNV.remove(nv);
            hienThiDuLieuLenBangNV();
            capNhatThongKeNhanVien();
            bangNhanVien.clearSelection();
            capNhatPreviewNhanVien();
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void xuLyXoaBacSi() {
        BacSi_entity bs = layBacSiDangChon();
        if (bs == null)
            return;

        int opt = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa bác sĩ [" + bs.getMaBacSi() + " - " + bs.getHoTen()
                        + "] không?\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa bác sĩ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                bacSiDAO.xoa(bs.getMaBacSi());
            } catch (Exception ex) {
                System.err.println("Lỗi xóa trong DB: " + ex.getMessage());
            }
            danhSachBS.remove(bs);
            hienThiDuLieuLenBangBS();
            capNhatThongKeBacSi();
            bangBacSi.clearSelection();
            capNhatPreviewBacSi();
            JOptionPane.showMessageDialog(this, "Đã xóa bác sĩ thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String sinhMaNVMoi() {
        int max = 0;
        for (NhanVien_entity nv : danhSachNV) {
            if (nv.getMaNhanVien() != null && nv.getMaNhanVien().startsWith("NV")) {
                try {
                    int num = Integer.parseInt(nv.getMaNhanVien().substring(2));
                    if (num > max)
                        max = num;
                } catch (Exception ignored) {
                }
            }
        }
        return String.format("NV%03d", max + 1);
    }

    private String sinhMaBSMoi() {
        int max = 0;
        for (BacSi_entity bs : danhSachBS) {
            if (bs.getMaBacSi() != null && bs.getMaBacSi().startsWith("BS")) {
                try {
                    int num = Integer.parseInt(bs.getMaBacSi().substring(2));
                    if (num > max)
                        max = num;
                } catch (Exception ignored) {
                }
            }
        }
        return String.format("BS%03d", max + 1);
    }

    // =========================================================================
    // REGION 10: TẢI DỮ LIỆU TỪ DAO / DỮ LIỆU MẪU
    // =========================================================================
    private void taiDuLieu() {
        danhSachNV.clear();
        danhSachBS.clear();

        boolean coDuLieuDB = false;
        try {
            ArrayList<NhanVien_entity> dsDbNV = nhanVienDAO.getAll();
            if (dsDbNV != null && !dsDbNV.isEmpty()) {
                danhSachNV.addAll(dsDbNV);
                coDuLieuDB = true;
            }

            List<BacSi_entity> dsDbBS = bacSiDAO.getAll();
            if (dsDbBS != null && !dsDbBS.isEmpty()) {
                danhSachBS.addAll(dsDbBS);
                coDuLieuDB = true;
            }
        } catch (Exception e) {
            System.err.println("Không thể kết nối CSDL SQL Server. Sử dụng dữ liệu mẫu độc lập: " + e.getMessage());
        }

        if (!coDuLieuDB || danhSachNV.isEmpty() || danhSachBS.isEmpty()) {
            napDuLieuMau();
        }

        hienThiDuLieuLenBangNV();
        hienThiDuLieuLenBangBS();

        if (cheDoHienTai == CHE_DO_NHAN_VIEN) {
            capNhatThongKeNhanVien();
        } else {
            capNhatThongKeBacSi();
        }

        capNhatPreviewNhanVien();
        capNhatPreviewBacSi();
    }

    private void napDuLieuMau() {
        if (danhSachNV.isEmpty()) {
            danhSachNV.add(new NhanVien_entity("NV001", "TK001", "Lê Văn Thành", "Quản lý cửa hàng", "0901234567",
                    "Quận 1, TP.HCM", LocalDate.of(1995, 4, 12), "Nam", 15000000, true));
            danhSachNV.add(new NhanVien_entity("NV002", "TK002", "Trần Thị Bích Ngọc", "Nhân viên bán hàng",
                    "0902345678", "Quận 3, TP.HCM", LocalDate.of(1998, 8, 20), "Nữ", 8500000, true));
            danhSachNV.add(new NhanVien_entity("NV003", "TK003", "Nguyễn Hoàng Nam", "Nhân viên dịch vụ", "0903456789",
                    "Bình Thạnh, TP.HCM", LocalDate.of(1997, 11, 5), "Nam", 8000000, true));
            danhSachNV.add(new NhanVien_entity("NV004", "TK004", "Phạm Thị Mai Hương", "Thu ngân", "0904567890",
                    "Phú Nhuận, TP.HCM", LocalDate.of(1999, 1, 15), "Nữ", 7500000, true));
            danhSachNV.add(new NhanVien_entity("NV005", "TK005", "Vũ Minh Quân", "Nhân viên quản lý kho", "0905678901",
                    "Gò Vấp, TP.HCM", LocalDate.of(1996, 6, 28), "Nam", 9000000, false));
            danhSachNV.add(new NhanVien_entity("NV006", null, "Đặng Thu Thảo", "Nhân viên bán hàng", "0906789012",
                    "Tân Bình, TP.HCM", LocalDate.of(2001, 3, 10), "Nữ", 7000000, null));
            danhSachNV.add(new NhanVien_entity("NV007", "TK007", "Hoàng Quốc Bảo", "Nhân viên dịch vụ", "0907890123",
                    "Quận 7, TP.HCM", LocalDate.of(1998, 9, 22), "Nam", 8000000, true));
        }

        if (danhSachBS.isEmpty()) {
            danhSachBS.add(new BacSi_entity("BS001", "TK008", "BS. Bùi Anh Tuấn", "Nam", "0912345678", "Ngoại khoa",
                    "anhtuan@salala.vn", LocalDate.of(1988, 5, 14), 48, true));
            danhSachBS.add(new BacSi_entity("BS002", "TK009", "BS. Lê Minh", "Nam", "0913456789", "Nội khoa",
                    "leminh@salala.vn", LocalDate.of(1990, 8, 25), 52, true));
            danhSachBS.add(new BacSi_entity("BS003", "TK010", "BS. Nguyễn Thảo Vy", "Nữ", "0914567890", "Da liễu thú y",
                    "thaovy@salala.vn", LocalDate.of(1992, 12, 3), 36, true));
            danhSachBS.add(new BacSi_entity("BS004", "TK011", "BS. Trần Đức Hải", "Nam", "0915678901", "Răng hàm mặt",
                    "duchai@salala.vn", LocalDate.of(1989, 7, 19), 29, true));
            danhSachBS.add(new BacSi_entity("BS005", "TK012", "BS. Phạm Quỳnh Chi", "Nữ", "0916789012",
                    "Chẩn đoán hình ảnh", "quynhchi@salala.vn", LocalDate.of(1994, 2, 8), 15, false));
            danhSachBS.add(new BacSi_entity("BS006", null, "BS. Vũ Hoàng Long", "Nam", "0917890123", "Sản khoa",
                    "hoanglong@salala.vn", LocalDate.of(1991, 10, 30), 0, null));
        }
    }

    private void hienThiDuLieuLenBangNV() {
        modelNV.setRowCount(0);
        for (NhanVien_entity nv : danhSachNV) {
            String ngaySinh = nv.getNgaySinh() != null ? DATE_FMT.format(nv.getNgaySinh()) : "---";
            String trangThai = "Chưa có tài khoản";
            if (nv.getTrangThaiTK() != null) {
                trangThai = nv.getTrangThaiTK() ? "Đang làm việc" : "Tạm nghỉ";
            }
            modelNV.addRow(new Object[] {
                    nv.getMaNhanVien(),
                    nv.getHoTen(),
                    nv.getChucVu(),
                    nv.getGioiTinh(),
                    nv.getSdt(),
                    ngaySinh,
                    nv.getLuongCoBan(),
                    trangThai
            });
        }
        locBangNhanVien();
    }

    private void hienThiDuLieuLenBangBS() {
        modelBS.setRowCount(0);
        for (BacSi_entity bs : danhSachBS) {
            String trangThai = "Chưa có tài khoản";
            if (bs.getTrangThaiTK() != null) {
                trangThai = bs.getTrangThaiTK() ? "Đang làm việc" : "Tạm nghỉ";
            }
            modelBS.addRow(new Object[] {
                    bs.getMaBacSi(),
                    bs.getHoTen(),
                    bs.getChuyenNganh(),
                    bs.getGioiTinh(),
                    bs.getSdt(),
                    bs.getEmail() != null ? bs.getEmail() : "---",
                    bs.getSoCaKham() + " ca",
                    trangThai
            });
        }
        locBangBacSi();
    }

    private void xuatDuLieuExcel(JTable table, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultFileName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                writer.write('\ufeff'); // BOM cho Excel tiếng Việt

                for (int c = 0; c < table.getColumnCount(); c++) {
                    writer.write("\"" + table.getColumnName(c) + "\"");
                    if (c < table.getColumnCount() - 1)
                        writer.write(",");
                }
                writer.write("\n");

                for (int r = 0; r < table.getRowCount(); r++) {
                    for (int c = 0; c < table.getColumnCount(); c++) {
                        Object val = table.getValueAt(r, c);
                        writer.write("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                        if (c < table.getColumnCount() - 1)
                            writer.write(",");
                    }
                    writer.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Xuất dữ liệu thành công ra file:\n" + f.getAbsolutePath(),
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // REGION 11: COMPONENT TÙY CHỈNH (FLAT DESIGN - KHÔNG DÙNG EMOJI GÂY LỖI Ô
    // VUÔNG)
    // =========================================================================
    public static JButton taoNutChinh(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        btn.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, 36));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
            }
        });
        return btn;
    }

    public static JButton taoNutPhu(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 36));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static JButton taoNutNguyHiem(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 36));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(RangBuocGiaoDien.MAU_NGUY_HIEM_NEN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;

        public RoundedPanel(int arc, Color fill, Color border) {
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class SearchField extends JTextField {
        private final String placeholder;

        public SearchField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                RangBuocGiaoDien.batKhuRangCua(g2);
                g2.setFont(getFont());
                g2.setColor(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, insets.left, y);
                g2.dispose();
            }
        }
    }

    public static class ComboHienDai extends JComboBox<String> {
        public ComboHienDai(String[] items, int width, int height) {
            super(items);
            setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            setBackground(Color.WHITE);
            setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            setPreferredSize(new Dimension(width, height));
            setUI(new FlatComboUI());
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                    lbl.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
                    if (isSelected) {
                        lbl.setBackground(RangBuocGiaoDien.MAU_CHU_DAO_NHAT);
                        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                    } else {
                        lbl.setBackground(Color.WHITE);
                        lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                    }
                    return lbl;
                }
            });
        }
    }

    private static class FlatComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton("v");
            btn.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
            btn.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            btn.setContentAreaFilled(false);
            btn.setBorder(new EmptyBorder(0, 0, 0, 8));
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            return btn;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
            return popup;
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Object obj = comboBox.getSelectedItem();
            if (obj == null)
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            g2.setFont(comboBox.getFont());
            g2.setColor(RangBuocGiaoDien.MAU_CHU_CHINH);
            FontMetrics fm = g2.getFontMetrics();
            int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(obj.toString(), bounds.x + 10, y);
            g2.dispose();
        }
    }

    private static class TextRenderer extends DefaultTableCellRenderer {
        private final Color color;
        private final int style;

        TextRenderer(Color color, int style, int align) {
            this.color = color;
            this.style = style;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setFont(new Font(FONT_NAME, style, 13));
            setForeground(sel ? t.getSelectionForeground() : color);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            return this;
        }
    }

    private static class CurrencyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font(FONT_NAME, Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 12, 0, 8));
            if (v instanceof Number) {
                setText(MONEY_FMT.format(((Number) v).longValue()) + " đ");
            }
            return this;
        }
    }

    private static class BadgeStatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JLabel badge = new JLabel();

        BadgeStatusRenderer() {
            badge.setFont(new Font(FONT_NAME, Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1;
            gbc.insets = new Insets(0, 10, 0, 0);
            panel.add(badge, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            String s = (value != null) ? value.toString() : "";
            badge.setText(s);

            if ("Đang làm việc".equalsIgnoreCase(s)) {
                badge.setBackground(RangBuocGiaoDien.MAU_THANH_CONG_NEN);
                badge.setForeground(RangBuocGiaoDien.MAU_THANH_CONG_CHU);
            } else if ("Tạm nghỉ".equalsIgnoreCase(s)) {
                badge.setBackground(RangBuocGiaoDien.MAU_CANH_BAO_NEN);
                badge.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_CHU);
            } else if ("Ngừng hoạt động".equalsIgnoreCase(s)) {
                badge.setBackground(RangBuocGiaoDien.MAU_NGUY_HIEM_NEN);
                badge.setForeground(RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
            } else {
                badge.setBackground(RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
                badge.setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            }

            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : RangBuocGiaoDien.MAU_DONG_XEN_KE));
            return panel;
        }
    }

    private static class SimpleDocListener implements DocumentListener {
        private final Runnable runnable;

        SimpleDocListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            runnable.run();
        }
    }

    // =========================================================================
    // REGION 12: HÀM MAIN KIỂM THỬ ĐỘC LẬP
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ thống Quản lý SALALA Pet Shop - Quản lý Nhân sự");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TOI_THIEU);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setContentPane(new NhanSuGUI(CHE_DO_NHAN_VIEN));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
