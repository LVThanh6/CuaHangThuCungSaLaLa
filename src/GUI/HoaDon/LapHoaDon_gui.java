package GUI.HoaDon;

import dao.KhachHang_DAO;
import dao.ThuCung_DAO;
import dao.DichVu_DAO;
import dao.VatTu_DAO;
import dao.HoaDon_Dao;
import dao.ChiTietHoaDon_DAO;
import dao.KhuyenMai_DAO;
import dao.Thue_DAO;
import dao.BacSi_DAO;
import dao.NhanVien_DAO;
import dao.DangNhap_DAO;
import model.TaiKhoan;

import entity.KhachHang;
import entity.ThuCung_entity;
import entity.DichVu_entity;
import entity.VatTu_entity;
import entity.HoaDon_entity;
import entity.NhanVien_entity;
import entity.BacSi_entity;
import entity.ChiTietHoaDon_entity;
import entity.KhuyenMai_Entity;
import entity.Thue_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LapHoaDon_gui extends JPanel {

    // ==================== DAO ====================

    private final KhachHang_DAO khDAO = new KhachHang_DAO();
    private final ThuCung_DAO tcDAO = new ThuCung_DAO();
    private final DichVu_DAO dvDAO = new DichVu_DAO();
    private final VatTu_DAO vtDAO = new VatTu_DAO();
    private final HoaDon_Dao hdDAO = new HoaDon_Dao();
    private final ChiTietHoaDon_DAO ctDAO = new ChiTietHoaDon_DAO();
    private final KhuyenMai_DAO kmDAO = new KhuyenMai_DAO();
    private final Thue_DAO thueDAO = new Thue_DAO();
    private final NhanVien_DAO nvDAO = new NhanVien_DAO();
    private final BacSi_DAO bacSiDAO = new BacSi_DAO();

    // Nhân viên đang đăng nhập
    private NhanVien_entity nhanVienDangNhap;

    // ==================== COMPONENT ====================

    private JComboBox<KhachHang> cboKhachHang;
    private JComboBox<ThuCung_entity> cboThuCung;
    private JComboBox<String> cboLoai;
    private JComboBox<Object> cboMatHang;
    private JComboBox<Object> cboNguoiThucHien;

    private JComboBox<KhuyenMai_Entity> cboKhuyenMai;
    private JComboBox<Thue_entity> cboThue;

    private JTextField txtMaHD;
    private JTextField txtNgayLap;
    private JTextField txtNhanVien;
    private JTextField txtTrangThai;
    private JTextField txtSoLuong;

    private JTable tblChiTiet;
    private DefaultTableModel model;
    private final Map<Integer, Object> nguoiThucHienTheoDong = new HashMap<>();

    private JLabel lblTamTinh;
    private JLabel lblTienGiam;
    private JLabel lblTienThue;
    private JLabel lblTongTien;

    // ==================== MÀU SẮC ====================

    private static final Color MAU_CHU =
            new Color(45, 55, 72);

    private static final Color MAU_NEN =
            new Color(247, 249, 252);

    private static final Color MAU_XANH =
            new Color(37, 99, 235);

    // ==================== CONSTRUCTOR ====================

    public LapHoaDon_gui() {
        this(null);
    }

    public LapHoaDon_gui(NhanVien_entity nhanVienDangNhap) {
        if (nhanVienDangNhap != null) {
            this.nhanVienDangNhap = nhanVienDangNhap;
        } else {
            this.nhanVienDangNhap = layNhanVienDangNhapHienTai();
        }

        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        khoiTaoGiaoDien();

        taiDanhSachKhachHang();
        taiDanhSachMatHang();
        taiDanhSachKhuyenMai();
        taiDanhSachThue();

        capNhatTongTien();
    }

    /**
     * Tự động lấy thông tin nhân viên đang đăng nhập từ Session (DangNhap_DAO).
     */
    public static NhanVien_entity layNhanVienDangNhapHienTai() {
        try {
            NhanVien_DAO nvDAO = new NhanVien_DAO();
            String maDT = DangNhap_DAO.getMaDoiTuong();
            if (maDT != null && !maDT.isBlank()) {
                NhanVien_entity nv = nvDAO.getNhanVienTheoMa(maDT);
                if (nv != null) {
                    return nv;
                }
            }
            TaiKhoan tk = DangNhap_DAO.getTaiKhoanHienTai();
            if (tk != null && tk.getMaTK() != null) {
                ArrayList<NhanVien_entity> ds = nvDAO.getAll();
                for (NhanVien_entity nv : ds) {
                    if (tk.getMaTK().equals(nv.getMaTK())) {
                        return nv;
                    }
                }
            }
            // Fallback khi đăng nhập bằng tài khoản quản lý hoặc tài khoản chưa liên kết trực tiếp
            ArrayList<NhanVien_entity> dsAll = nvDAO.getAll();
            if (!dsAll.isEmpty()) {
                return dsAll.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== KHỞI TẠO GIAO DIỆN ====================

    private void khoiTaoGiaoDien() {

        JLabel lblTieuDe = new JLabel("LẬP HÓA ĐƠN MỚI");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 23));
        lblTieuDe.setForeground(MAU_CHU);

        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);

        add(pnlTieuDe, BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 14));
        pnlNoiDung.setOpaque(false);

        pnlNoiDung.add(
                taoPanelThongTin(),
                BorderLayout.NORTH
        );

        pnlNoiDung.add(
                taoPanelChiTiet(),
                BorderLayout.CENTER
        );

        pnlNoiDung.add(
                taoPanelTongTien(),
                BorderLayout.SOUTH
        );

        add(pnlNoiDung, BorderLayout.CENTER);
    }

    // ==================== THÔNG TIN HÓA ĐƠN ====================

    private JPanel taoPanelThongTin() {

        JPanel pnl = taoKhung("Thông tin hóa đơn");
        pnl.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        txtMaHD = new JTextField(phatSinhMaHD());
        txtMaHD.setEditable(false);

        txtNgayLap = new JTextField(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );
        txtNgayLap.setEditable(false);

        cboKhachHang = new JComboBox<>();
        cboThuCung = new JComboBox<>();
        cboKhuyenMai = new JComboBox<>();
        cboThue = new JComboBox<>();

        // Hiển thị khách hàng
        cboKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof KhachHang kh) {
                    setText(kh.getMaKH() + " - " + kh.getHoTen());
                } else {
                    setText("-- Chọn khách hàng --");
                }

                return this;
            }
        });

        // Hiển thị thú cưng
        cboThuCung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof ThuCung_entity tc) {
                    setText(tc.getMaTC() + " - " + tc.getTenTC());
                } else {
                    setText("-- Chọn thú cưng --");
                }

                return this;
            }
        });

        // Hiển thị khuyến mãi
        cboKhuyenMai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof KhuyenMai_Entity km) {
                    setText(
                            km.getTenKM()
                            + " - Giảm "
                            + km.getMucKM()
                            + "%"
                    );
                } else {
                    setText("-- Không áp dụng khuyến mãi --");
                }

                return this;
            }
        });

        // Hiển thị thuế
        cboThue.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Thue_entity thue) {
                    setText(
                            thue.getTenThue()
                            + " - "
                            + thue.getMucThue()
                            + "%"
                    );
                } else {
                    setText("-- Chọn thuế --");
                }

                return this;
            }
        });

        txtNhanVien = new JTextField();
        txtNhanVien.setEditable(false);
        capNhatHienThiNhanVien();

        txtTrangThai = new JTextField("Chờ thanh toán");
        txtTrangThai.setEditable(false);

        themTruong(
                pnl, gbc, 0, 0,
                "Mã hóa đơn", txtMaHD
        );

        themTruong(
                pnl, gbc, 1, 0,
                "Ngày lập", txtNgayLap
        );

        themTruong(
                pnl, gbc, 0, 1,
                "Khách hàng", cboKhachHang
        );

        themTruong(
                pnl, gbc, 1, 1,
                "Thú cưng", cboThuCung
        );

        themTruong(
                pnl, gbc, 0, 2,
                "Khuyến mãi", cboKhuyenMai
        );

        themTruong(
                pnl, gbc, 1, 2,
                "Thuế", cboThue
        );

        themTruong(
                pnl, gbc, 0, 3,
                "Nhân viên lập", txtNhanVien
        );

        themTruong(
                pnl, gbc, 1, 3,
                "Trạng thái", txtTrangThai
        );

        cboKhachHang.addActionListener(
                e -> taiThuCungTheoKhachHang()
        );

        cboKhuyenMai.addActionListener(
                e -> capNhatTongTien()
        );

        cboThue.addActionListener(
                e -> capNhatTongTien()
        );

        return pnl;
    }

    // ==================== CHI TIẾT HÓA ĐƠN ====================

    private JPanel taoPanelChiTiet() {

        JPanel pnl = taoKhung("Chi tiết dịch vụ / vật tư");
        pnl.setLayout(new BorderLayout(0, 10));

        JPanel pnlThem = new JPanel(new GridBagLayout());
        pnlThem.setOpaque(false);

        cboLoai = new JComboBox<>(
                new String[]{"Dịch vụ", "Vật tư"}
        );

        cboMatHang = new JComboBox<>();
        cboNguoiThucHien = new JComboBox<>();
        txtSoLuong = new JTextField("1", 5);

        JButton btnThem = new JButton("Thêm vào hóa đơn");
        btnThem.setFont(new Font("Arial", Font.BOLD, 12));
        btnThem.setForeground(Color.WHITE);
        btnThem.setBackground(Color.BLUE);
        btnThem.setOpaque(true);
        btnThem.setContentAreaFilled(true);
        btnThem.setBorderPainted(false);
        btnThem.setFocusPainted(false);
        btnThem.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        
        JButton btnXoa = new JButton("Xóa dòng");
        btnXoa.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoa.setForeground(Color.WHITE);
        btnXoa.setBackground(Color.RED);
        btnXoa.setOpaque(true);
        btnXoa.setContentAreaFilled(true);
        btnXoa.setBorderPainted(false);
        btnXoa.setFocusPainted(false);
        btnXoa.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));		

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlThem.add(new JLabel("Loại"), gbc);

        gbc.gridx = 1;
        pnlThem.add(new JLabel("Dịch vụ / vật tư"), gbc);

        gbc.gridx = 2;
        pnlThem.add(new JLabel("Người thực hiện"), gbc);

        gbc.gridx = 3;
        pnlThem.add(new JLabel("Số lượng"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlThem.add(cboLoai, gbc);

        gbc.gridx = 1;
        pnlThem.add(cboMatHang, gbc);

        gbc.gridx = 2;
        pnlThem.add(cboNguoiThucHien, gbc);

        gbc.gridx = 3;
        pnlThem.add(txtSoLuong, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        pnlThem.add(btnThem, gbc);

        model = new DefaultTableModel(
                new Object[]{
                    "Mã",
                    "Tên",
                    "Loại",
                    "Đơn giá",
                    "Số lượng",
                    "Thành tiền"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        tblChiTiet = new JTable(model);
        tblChiTiet.setRowHeight(30);
        tblChiTiet.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        tblChiTiet.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setPreferredSize(new Dimension(700, 220));

        JPanel pnlNut = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        pnlNut.setOpaque(false);
        pnlNut.add(btnXoa);

        pnl.add(pnlThem, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.SOUTH);

        cboLoai.addActionListener(
                e -> {
                    taiDanhSachMatHang();
                    taiDanhSachNguoiThucHien();
                }
        );

        cboMatHang.addActionListener(
                e -> taiDanhSachNguoiThucHien()
        );

        btnThem.addActionListener(
                e -> themMatHang()
        );

        btnXoa.addActionListener(
                e -> xoaDong()
        );

        return pnl;
    }

    // ==================== THANH TOÁN ====================

    private JPanel taoPanelTongTien() {

        JPanel pnl = taoKhung("Thanh toán");
        pnl.setLayout(new BorderLayout(10, 10));

        JPanel pnlTien = new JPanel(
                new GridLayout(4, 2, 10, 8)
        );
        pnlTien.setOpaque(false);

        lblTamTinh = new JLabel("0 đ");
        lblTienGiam = new JLabel("0 đ");
        lblTienThue = new JLabel("0 đ");
        lblTongTien = new JLabel("0 đ");

        lblTongTien.setFont(
                new Font("Arial", Font.BOLD, 21)
        );

        lblTongTien.setForeground(MAU_XANH);

        pnlTien.add(new JLabel("Tạm tính:"));
        pnlTien.add(lblTamTinh);

        pnlTien.add(new JLabel("Tiền giảm:"));
        pnlTien.add(lblTienGiam);

        pnlTien.add(new JLabel("Tiền thuế:"));
        pnlTien.add(lblTienThue);

        pnlTien.add(new JLabel("Tổng thanh toán:"));
        pnlTien.add(lblTongTien);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(new Font("Arial", Font.BOLD, 12));
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.setBackground(Color.GREEN);
        btnLamMoi.setOpaque(true);
        btnLamMoi.setContentAreaFilled(true);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        JButton btnLuu = new JButton("Lưu hóa đơn");
        btnLuu.setFont(new Font("Arial", Font.BOLD, 12));
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setBackground(Color.GREEN);
        btnLuu.setOpaque(true);
        btnLuu.setContentAreaFilled(true);
        btnLuu.setBorderPainted(false);
        btnLuu.setFocusPainted(false);
        btnLuu.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        JPanel pnlNut = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnLuu);

        pnl.add(pnlTien, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.SOUTH);

        btnLamMoi.addActionListener(e -> lamMoi());
        btnLuu.addActionListener(e -> luuHoaDon());

        return pnl;
    }

    // ==================== HÀM GIAO DIỆN DÙNG CHUNG ====================

    private JPanel taoKhung(String tieuDe) {

        JPanel pnl = new JPanel();
        pnl.setBackground(Color.WHITE);

        pnl.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        BorderFactory.createTitledBorder(tieuDe)
                )
        );

        return pnl;
    }

    private void themTruong(
            JPanel pnl,
            GridBagConstraints gbc,
            int cot,
            int hang,
            String ten,
            JComponent component) {

        gbc.gridx = cot * 2;
        gbc.gridy = hang;
        gbc.weightx = 0;

        pnl.add(new JLabel(ten), gbc);

        gbc.gridx = cot * 2 + 1;
        gbc.weightx = 1;

        pnl.add(component, gbc);
    }

    // ==================== TẢI KHÁCH HÀNG ====================

    private void taiDanhSachKhachHang() {

        cboKhachHang.removeAllItems();

        ArrayList<KhachHang> ds =
                khDAO.getAlltbKhachHang();

        for (KhachHang kh : ds) {
            cboKhachHang.addItem(kh);
        }

        taiThuCungTheoKhachHang();
    }

    // ==================== TẢI THÚ CƯNG THEO KHÁCH HÀNG ====================

    private void taiThuCungTheoKhachHang() {

        cboThuCung.removeAllItems();

        KhachHang kh =
                (KhachHang) cboKhachHang.getSelectedItem();

        if (kh == null) {
            return;
        }

        ArrayList<ThuCung_entity> ds =
                tcDAO.getThuCungTheoMaKH(kh.getMaKH());

        for (ThuCung_entity tc : ds) {
            cboThuCung.addItem(tc);
        }
    }

    // ==================== TẢI DỊCH VỤ / VẬT TƯ ====================

    private void taiDanhSachNguoiThucHien() {
        if (cboNguoiThucHien == null) {
            return;
        }

        cboNguoiThucHien.removeAllItems();
        cboNguoiThucHien.addItem(null);

        try {
            String loai = (String) cboLoai.getSelectedItem();
            if ("Dịch vụ".equals(loai)) {
                for (NhanVien_entity nv : nvDAO.getAll()) {
                    if ("Nhân viên dịch vụ".equals(nv.getChucVu())) {
                        cboNguoiThucHien.addItem(nv);
                    }
                }
                for (BacSi_entity bs : bacSiDAO.getAll()) {
                    cboNguoiThucHien.addItem(bs);
                }
            } else {
                cboNguoiThucHien.addItem("Không có người thực hiện");
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách người thực hiện: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }

        cboNguoiThucHien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien_entity nv) {
                    setText(nv.getMaNhanVien() + " - " + nv.getHoTen() + " (NV)");
                } else if (value instanceof BacSi_entity bs) {
                    setText(bs.getMaBacSi() + " - " + bs.getHoTen() + " (BS)");
                } else if (value == null) {
                    setText("-- Chọn người thực hiện --");
                } else {
                    setText("Không có người thực hiện");
                }
                return this;
            }
        });
    }

    private void taiDanhSachMatHang() {

        if (cboMatHang == null || cboLoai == null) {
            return;
        }

        cboMatHang.removeAllItems();

        String loai = (String) cboLoai.getSelectedItem();

        if ("Dịch vụ".equals(loai)) {

            for (DichVu_entity dv : dvDAO.getAllDichVu()) {
                cboMatHang.addItem(dv);
            }

        } else {

            for (VatTu_entity vt : vtDAO.getAllVatTu()) {
                cboMatHang.addItem(vt);
            }
        }

        cboMatHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof DichVu_entity dv) {

                    setText(
                            dv.getMaDV()
                            + " - " + dv.getTenDV()
                            + " - " + dinhDangTien(dv.getGiaDV())
                    );

                } else if (value instanceof VatTu_entity vt) {

                    setText(
                            vt.getMaVT()
                            + " - " + vt.getTenVT()
                            + " - " + dinhDangTien(vt.getGiaBanCoBan())
                    );

                } else {
                    setText("");
                }

                return this;
            }
        });
    }

    // ==================== TẢI KHUYẾN MÃI ====================

    private void taiDanhSachKhuyenMai() {

        cboKhuyenMai.removeAllItems();
        cboKhuyenMai.addItem(null);

        LocalDate homNay = LocalDate.now();

        ArrayList<KhuyenMai_Entity> ds =
                kmDAO.getAllKhuyenMai();

        System.out.println("Số khuyến mãi lấy từ DB: " + ds.size());
        System.out.println("Ngày hiện tại: " + homNay);

        for (KhuyenMai_Entity km : ds) {

            boolean dungNgay =
                    (km.getNgayBatDau() == null
                    || !homNay.isBefore(km.getNgayBatDau()))
                    &&
                    (km.getNgayKetThuc() == null
                    || !homNay.isAfter(km.getNgayKetThuc()));

            

            if (dungNgay) {
                cboKhuyenMai.addItem(km);
            }
        }
    }

    // ==================== TẢI THUẾ ====================

    private void taiDanhSachThue() {

        cboThue.removeAllItems();

        ArrayList<Thue_entity> ds =
                thueDAO.getAllThue();

        for (Thue_entity thue : ds) {
            cboThue.addItem(thue);
        }
    }

    // ==================== THÊM MẶT HÀNG ====================

    private void themMatHang() {

        Object item = cboMatHang.getSelectedItem();

        if (item == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dịch vụ hoặc vật tư."
            );
            return;
        }

        int soLuong;

        try {

            soLuong = Integer.parseInt(
                    txtSoLuong.getText().trim()
            );

            if (soLuong <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Số lượng phải là số nguyên dương."
            );

            txtSoLuong.requestFocus();
            return;
        }

        String ma;
        String ten;
        String loai;
        double donGia;

        if (item instanceof DichVu_entity dv) {

            ma = dv.getMaDV();
            ten = dv.getTenDV();
            loai = "Dịch vụ";
            donGia = dv.getGiaDV();

        } else if (item instanceof VatTu_entity vt) {

            ma = vt.getMaVT();
            ten = vt.getTenVT();
            loai = "Vật tư";
            donGia = vt.getGiaBanCoBan();

            if (vt.getSoLuongTon() < soLuong) {
                JOptionPane.showMessageDialog(
                        this,
                        "Số lượng tồn kho không đủ."
                );
                return;
            }

        } else {
            return;
        }

        Object nguoiThucHien = cboNguoiThucHien.getSelectedItem();
        if ("Dịch vụ".equals(loai)
                && !(nguoiThucHien instanceof NhanVien_entity)
                && !(nguoiThucHien instanceof BacSi_entity)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người thực hiện dịch vụ.");
            return;
        }

        // Kiểm tra mặt hàng đã có trong bảng chưa
        for (int i = 0; i < model.getRowCount(); i++) {

            if (ma.equals(model.getValueAt(i, 0))
                    && loai.equals(model.getValueAt(i, 2))
                    && Objects.equals(nguoiThucHienTheoDong.get(i), nguoiThucHien)) {

                int slCu = Integer.parseInt(
                        model.getValueAt(i, 4).toString()
                );

                int slMoi = slCu + soLuong;

                if (item instanceof VatTu_entity vt
                        && vt.getSoLuongTon() < slMoi) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Số lượng tồn kho không đủ."
                    );

                    return;
                }

                model.setValueAt(slMoi, i, 4);

                model.setValueAt(
                        dinhDangTien(donGia * slMoi),
                        i, 5
                );
                nguoiThucHienTheoDong.put(i, nguoiThucHien);

                capNhatTongTien();
                return;
            }
        }

        model.addRow(new Object[]{
                ma,
                ten,
                loai,
                donGia,
                soLuong,
                dinhDangTien(donGia * soLuong)
        });
        nguoiThucHienTheoDong.put(model.getRowCount() - 1, nguoiThucHien);

        capNhatTongTien();
    }

    // ==================== XÓA DÒNG CHI TIẾT ====================

    private void xoaDong() {

        int row = tblChiTiet.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dòng cần xóa."
            );
            return;
        }

        int modelRow = tblChiTiet.convertRowIndexToModel(row);

        model.removeRow(modelRow);
        Map<Integer, Object> daCapNhat = new HashMap<>();
        for (Map.Entry<Integer, Object> entry : nguoiThucHienTheoDong.entrySet()) {
            if (entry.getKey() < modelRow) {
                daCapNhat.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey() > modelRow) {
                daCapNhat.put(entry.getKey() - 1, entry.getValue());
            }
        }
        nguoiThucHienTheoDong.clear();
        nguoiThucHienTheoDong.putAll(daCapNhat);

        capNhatTongTien();
    }

    // ==================== TÍNH TẠM TÍNH ====================

    private double tinhTamTinh() {

        double tong = 0;

        for (int i = 0; i < model.getRowCount(); i++) {

            double donGia = Double.parseDouble(
                    model.getValueAt(i, 3).toString()
            );

            int soLuong = Integer.parseInt(
                    model.getValueAt(i, 4).toString()
            );

            tong += donGia * soLuong;
        }

        return tong;
    }

    // ==================== TÍNH TIỀN GIẢM ====================

    private double tinhTienGiam(double tienHang) {

        KhuyenMai_Entity km =
                (KhuyenMai_Entity) cboKhuyenMai.getSelectedItem();

        if (km == null) {
            return 0;
        }

        // Giả định mucKM lưu theo phần trăm.
        double tienGiam = tienHang * km.getMucKM() / 100.0;

        // Tiền giảm không vượt quá tiền hàng.
        return Math.max(0, Math.min(tienGiam, tienHang));
    }

    // ==================== TÍNH TIỀN THUẾ ====================

    private double tinhTienThue(double tienSauGiam) {

        Thue_entity thue =
                (Thue_entity) cboThue.getSelectedItem();

        if (thue == null) {
            return 0;
        }

        // Giả định mucThue lưu theo phần trăm.
        return Math.max(
                0,
                tienSauGiam * thue.getMucThue() / 100.0
        );
    }

    // ==================== TÍNH TỔNG THANH TOÁN ====================

    private double tinhTongThanhToan() {

        double tienHang = tinhTamTinh();

        double tienGiam = tinhTienGiam(tienHang);

        double tienSauGiam = tienHang - tienGiam;

        double tienThue = tinhTienThue(tienSauGiam);

        return tienSauGiam + tienThue;
    }

    private void capNhatTongTien() {

        if (lblTamTinh == null
                || lblTienGiam == null
                || lblTienThue == null
                || lblTongTien == null) {
            return;
        }

        double tienHang = tinhTamTinh();

        double tienGiam = tinhTienGiam(tienHang);

        double tienSauGiam = tienHang - tienGiam;

        double tienThue = tinhTienThue(tienSauGiam);

        double tongThanhToan = tienSauGiam + tienThue;

        lblTamTinh.setText(
                dinhDangTien(tienHang)
        );

        lblTienGiam.setText(
                dinhDangTien(tienGiam)
        );

        lblTienThue.setText(
                dinhDangTien(tienThue)
        );

        lblTongTien.setText(
                dinhDangTien(tongThanhToan)
        );
    }

    // ==================== LƯU HÓA ĐƠN ====================

    private void luuHoaDon() {

        KhachHang kh =
                (KhachHang) cboKhachHang.getSelectedItem();

        ThuCung_entity tc =
                (ThuCung_entity) cboThuCung.getSelectedItem();

        if (nhanVienDangNhap == null) {
            nhanVienDangNhap = layNhanVienDangNhapHienTai();
            capNhatHienThiNhanVien();
        }

        if (nhanVienDangNhap == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Chưa có thông tin nhân viên đăng nhập để lập hóa đơn.\nVui lòng kiểm tra lại tài khoản hoặc CSDL.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (kh == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn khách hàng."
            );
            return;
        }

        if (tc == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn thú cưng."
            );
            return;
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hóa đơn phải có ít nhất một dòng chi tiết."
            );
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn lưu hóa đơn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        String maHD = txtMaHD.getText().trim();

        KhuyenMai_Entity km =
                (KhuyenMai_Entity) cboKhuyenMai.getSelectedItem();

        Thue_entity thue =
                (Thue_entity) cboThue.getSelectedItem();

        HoaDon_entity hd = new HoaDon_entity(
                maHD,
                LocalDate.now(),
                "Chờ thanh toán",
                kh,
                nhanVienDangNhap,
                km,
                thue,
                null // Chưa tạo đối tượng thanh toán
        );

        if (!hdDAO.themHD(hd)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu hóa đơn.\n"
                    + "Vui lòng kiểm tra console và ràng buộc database."
            );

            return;
        }

        boolean thanhCong = true;

        ArrayList<DichVu_entity> dsDV =
                dvDAO.getAllDichVu();

        for (int i = 0; i < model.getRowCount(); i++) {

            String ma = model.getValueAt(i, 0).toString();

            String loai = model.getValueAt(i, 2).toString();

            double donGia = Double.parseDouble(
                    model.getValueAt(i, 3).toString()
            );

            int soLuong = Integer.parseInt(
                    model.getValueAt(i, 4).toString()
            );

            DichVu_entity dv = null;
            VatTu_entity vt = null;
            NhanVien_entity nvThucHien = null;
            BacSi_entity bsThucHien = null;
            Object nguoiThucHien = nguoiThucHienTheoDong.get(i);
            if (nguoiThucHien instanceof NhanVien_entity) {
                nvThucHien = (NhanVien_entity) nguoiThucHien;
            } else if (nguoiThucHien instanceof BacSi_entity) {
                bsThucHien = (BacSi_entity) nguoiThucHien;
            }

            if ("Dịch vụ".equals(loai)) {

                for (DichVu_entity x : dsDV) {

                    if (ma.equals(x.getMaDV())) {
                        dv = x;
                        break;
                    }
                }

            } else {

                vt = vtDAO.getVatTuTheoMa(ma);
            }

            if (dv == null && vt == null) {
                thanhCong = false;
                break;
            }

            String maCTHD =
                    "CT" + maHD + String.format("%02d", i + 1);

            ChiTietHoaDon_entity ct =
                    new ChiTietHoaDon_entity(
                            maCTHD,
                            hd,
                            dv,
                            vt,
                            nvThucHien,
                            bsThucHien,
                            soLuong,
                            donGia
                    );

            if (!ctDAO.createChiTietHoaDon(ct)) {
                thanhCong = false;
                break;
            }
        }

        if (thanhCong) {

            double tienHang = tinhTamTinh();
            double tienGiam = tinhTienGiam(tienHang);
            double tienSauGiam = tienHang - tienGiam;
            double tienThue = tinhTienThue(tienSauGiam);
            double tongThanhToan = tienSauGiam + tienThue;

            int luaChon = JOptionPane.showConfirmDialog(
                    this,
                    "Đã lưu hóa đơn " + maHD + " thành công!"
                    + "\nTạm tính: " + dinhDangTien(tienHang)
                    + "\nTiền giảm: " + dinhDangTien(tienGiam)
                    + "\nTiền thuế: " + dinhDangTien(tienThue)
                    + "\nTổng thanh toán: " + dinhDangTien(tongThanhToan)
                    + "\n\nBạn có muốn mở xem / in hóa đơn ngay không?",
                    "Lập hóa đơn thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            lamMoi();

            if (luaChon == JOptionPane.YES_OPTION) {
                ChiTietHoaDon_gui.hienThi(this, maHD);
            }

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Hóa đơn đã được tạo nhưng có chi tiết "
                    + "chưa lưu thành công.\n"
                    + "Cần kiểm tra dữ liệu trong SQL Server."
            );
        }
    }

    // ==================== LÀM MỚI ====================

    private void lamMoi() {

        model.setRowCount(0);
        nguoiThucHienTheoDong.clear();

        txtMaHD.setText(phatSinhMaHD());

        txtNgayLap.setText(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        txtSoLuong.setText("1");

        if (nhanVienDangNhap == null) {
            nhanVienDangNhap = layNhanVienDangNhapHienTai();
        }
        capNhatHienThiNhanVien();

        taiDanhSachKhachHang();
        taiDanhSachMatHang();
        taiDanhSachKhuyenMai();
        taiDanhSachThue();

        capNhatTongTien();
    }

    private void capNhatHienThiNhanVien() {
        if (txtNhanVien != null) {
            if (nhanVienDangNhap != null) {
                txtNhanVien.setText(nhanVienDangNhap.getMaNhanVien() + " - " + nhanVienDangNhap.getHoTen());
            } else {
                txtNhanVien.setText("Chưa xác định");
            }
        }
    }

    // ==================== MÃ HÓA ĐƠN ====================

    private String phatSinhMaHD() {

        return "HD" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")
        );
    }

    // ==================== ĐỊNH DẠNG TIỀN ====================

    private String dinhDangTien(double tien) {

        return String.format("%,.0f đ", tien);
    }
}