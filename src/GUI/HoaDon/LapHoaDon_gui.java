package GUI.HoaDon;

import entity.KhachHang.KhachHang;
import entity.ThuCung.ChiTietDonThuoc_entity;
import entity.ThuCung.ThuCung_entity;
import entity.DichVu.DichVu_entity;
import entity.VatTu.VatTu_entity;
import entity.NhanVien.NhanVien_entity;
import entity.BacSi.BacSi_entity;
import entity.DichVu.KhuyenMai_Entity;
import entity.DichVu.Thue_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import controller.LapHoaDonController;
import controller.LapHoaDonController.InvoiceInput;
import controller.LapHoaDonController.InvoiceLine;
import controller.LapHoaDonController.InvoiceTotals;
import controller.LapHoaDonController.LoaiMatHang;
import controller.LapHoaDonController.PrescriptionOption;
import controller.LapHoaDonController.ValidationError;

public class LapHoaDon_gui extends JPanel {

    private final LapHoaDonController controller = new LapHoaDonController();

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
    private final ArrayList<InvoiceLine> chiTietHoaDon = new ArrayList<>();

    private JLabel lblTamTinh;
    private JLabel lblTienGiam;
    private JLabel lblTienThue;
    private JLabel lblTongTien;

    // ==================== MÀU SẮC ====================

    private static final Color MAU_CHU =
            new Color(45, 55, 72);

    private static final Color MAU_NEN =
            new Color(248, 250, 252);

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
            return new LapHoaDonController().layNhanVienDangNhapHienTai();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Không thể tải thông tin nhân viên đăng nhập: " + e.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
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
                    setText("-- Không gắn thú cưng (vật tư thông thường) --");
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
                    if ("Dịch vụ".equals(cboLoai.getSelectedItem())
                            && cboThuCung.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this,
                                "Vui lòng chọn khách hàng và thú cưng trước khi chọn dịch vụ.");
                        cboLoai.setSelectedItem("Vật tư");
                        cboThuCung.requestFocusInWindow();
                        return;
                    }
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

        ArrayList<KhachHang> ds = controller.taiKhachHang();

        for (KhachHang kh : ds) {
            cboKhachHang.addItem(kh);
        }

        taiThuCungTheoKhachHang();
    }

    // ==================== TẢI THÚ CƯNG THEO KHÁCH HÀNG ====================

    private void taiThuCungTheoKhachHang() {

        cboThuCung.removeAllItems();
        cboThuCung.addItem(null);

        KhachHang kh =
                (KhachHang) cboKhachHang.getSelectedItem();

        if (kh == null) {
            return;
        }

        ArrayList<ThuCung_entity> ds = controller.taiThuCung(kh.getMaKH());

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

        String loai = (String) cboLoai.getSelectedItem();
        if ("Dịch vụ".equals(loai)) {
            try {
                for (NhanVien_entity nv : controller.taiNhanVienDichVuThucHien()) {
                    cboNguoiThucHien.addItem(nv);
                }
                for (BacSi_entity bs : controller.taiBacSiThucHien()) {
                    cboNguoiThucHien.addItem(bs);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách người thực hiện: " + ex.getMessage(),
                        "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            cboNguoiThucHien.addItem("Không có người thực hiện");
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

            for (DichVu_entity dv : controller.taiDichVu()) {
                cboMatHang.addItem(dv);
            }

        } else {

            for (VatTu_entity vt : controller.taiVatTu()) {
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
                            + (vt.getMaThuoc() != null ? " (Thuốc)" : "")
                            + " - " + dinhDangTien(vt.getGiaBanCoBan())
                    );

                } else {
                    setText("");
                }

                return this;
            }
        });
    }

    private ChiTietDonThuoc_entity chonChiTietDonThuoc(VatTu_entity vatTu) {
        ThuCung_entity thuCung = (ThuCung_entity) cboThuCung.getSelectedItem();
        List<PrescriptionOption> chiTietKhaDung;
        List<String> luaChon = new ArrayList<>();

        try {
            chiTietKhaDung = controller.taiChiTietDonThuocKhaDung(
                    thuCung.getMaTC(), vatTu.getMaThuoc().getMaThuoc());
            for (PrescriptionOption option : chiTietKhaDung) {
                    ChiTietDonThuoc_entity chiTiet = option.getChiTiet();
                    luaChon.add(chiTiet.getMaDonThuoc() + " / " + chiTiet.getMaCTDT()
                            + " - " + chiTiet.getTenThuoc() + " (còn "
                            + option.getSoLuongConLai().stripTrailingZeros().toPlainString()
                            + ")");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải đơn thuốc của thú cưng: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (luaChon.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy đơn thuốc còn thuốc này của thú cưng đã chọn.");
            return null;
        }

        Object chon = JOptionPane.showInputDialog(this,
                "Chọn chi tiết đơn thuốc cần cấp:",
                "Liên kết đơn thuốc",
                JOptionPane.QUESTION_MESSAGE,
                null,
                luaChon.toArray(),
                luaChon.get(0));
        if (chon == null) {
            return null;
        }
        int selectedIndex = luaChon.indexOf(chon.toString());
        return selectedIndex < 0 ? null : chiTietKhaDung.get(selectedIndex).getChiTiet();
    }

    // ==================== TẢI KHUYẾN MÃI ====================

    private void taiDanhSachKhuyenMai() {

        cboKhuyenMai.removeAllItems();
        cboKhuyenMai.addItem(null);

        LocalDate homNay = LocalDate.now();

        for (KhuyenMai_Entity km : controller.taiKhuyenMaiHieuLuc(homNay)) {
            cboKhuyenMai.addItem(km);
        }
    }

    // ==================== TẢI THUẾ ====================

    private void taiDanhSachThue() {

        cboThue.removeAllItems();

        for (Thue_entity thue : controller.taiThue()) {
            cboThue.addItem(thue);
        }
    }

    // ==================== THÊM MẶT HÀNG ====================

    private void themMatHang() {
        Object item = cboMatHang.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, ValidationError.CHUA_CHON_MAT_HANG.getMessage());
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương.");
            txtSoLuong.requestFocus();
            return;
        }

        if (!controller.laSoLuongHopLe(soLuong)) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương.");
            txtSoLuong.requestFocus();
            return;
        }

        InvoiceLine line;
        if (item instanceof DichVu_entity dv) {
            line = controller.taoDongDichVu(dv, soLuong);
        } else if (item instanceof VatTu_entity vt) {
            ValidationError stockValidation = controller.kiemTraTonKho(vt, soLuong);
            if (stockValidation != ValidationError.NONE) {
                JOptionPane.showMessageDialog(this, stockValidation.getMessage());
                return;
            }

            ChiTietDonThuoc_entity chiTietDon = null;
            if (controller.laThuocKeDon(vt)) {
                ValidationError petValidation = controller.kiemTraThuCungBatBuoc(
                        LoaiMatHang.THUOC, cboThuCung.getSelectedItem() != null);
                if (petValidation != ValidationError.NONE) {
                    JOptionPane.showMessageDialog(this, petValidation.getMessage());
                    return;
                }
                chiTietDon = chonChiTietDonThuoc(vt);
                if (chiTietDon == null) {
                    return;
                }
            }
            line = controller.taoDongVatTu(vt, chiTietDon, soLuong);
        } else {
            return;
        }

        Object nguoiThucHien = cboNguoiThucHien.getSelectedItem();
        NhanVien_entity nhanVienThucHien = nguoiThucHien instanceof NhanVien_entity
                ? (NhanVien_entity) nguoiThucHien : null;
        BacSi_entity bacSiThucHien = nguoiThucHien instanceof BacSi_entity
                ? (BacSi_entity) nguoiThucHien : null;
        line = controller.ganNguoiThucHien(line, nhanVienThucHien, bacSiThucHien);

        try {
            LapHoaDonController.AddLineResult result = controller.themHoacGopMatHang(
                    chiTietHoaDon, line, cboThuCung.getSelectedItem() != null);

            if (result.getValidation() != ValidationError.NONE) {
                JOptionPane.showMessageDialog(this, result.getValidation().getMessage());
                return;
            }

            int row = result.getIndex();
            InvoiceLine savedLine = result.getLine();

            if (result.isMerged()) {
                model.setValueAt(savedLine.getSoLuong(), row, 4);
                model.setValueAt(
                        dinhDangTien(controller.tinhThanhTienDong(savedLine)), row, 5);
            } else {
                model.addRow(new Object[]{
                        savedLine.getMaHang(),
                        savedLine.getTenHang(),
                        controller.layTenLoaiMatHang(savedLine.getLoai()),
                        savedLine.getDonGia(),
                        savedLine.getSoLuong(),
                        dinhDangTien(controller.tinhThanhTienDong(savedLine))
                });
            }

            capNhatTongTien();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kiểm tra số lượng thuốc đã cấp: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
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

        if (controller.xoaDongChiTiet(chiTietHoaDon, modelRow)) {
            model.removeRow(modelRow);
            capNhatTongTien();
        }
    }

    private void capNhatTongTien() {
        if (lblTamTinh == null
                || lblTienGiam == null
                || lblTienThue == null
                || lblTongTien == null) {
            return;
        }

        InvoiceTotals totals = tinhTongTienHienTai();
        lblTamTinh.setText(dinhDangTien(totals.getTienHang()));
        lblTienGiam.setText(dinhDangTien(totals.getTienGiam()));
        lblTienThue.setText(dinhDangTien(totals.getTienThue()));
        lblTongTien.setText(dinhDangTien(totals.getTongThanhToan()));
    }

    private InvoiceTotals tinhTongTienHienTai() {
        return controller.tinhTongTien(chiTietHoaDon,
                (KhuyenMai_Entity) cboKhuyenMai.getSelectedItem(),
                (Thue_entity) cboThue.getSelectedItem());
    }

    // ==================== LƯU HÓA ĐƠN ====================

    private void luuHoaDon() {

        KhachHang kh =
                (KhachHang) cboKhachHang.getSelectedItem();

        ThuCung_entity tc =
                (ThuCung_entity) cboThuCung.getSelectedItem();

        if (nhanVienDangNhap == null) {
            try {
                nhanVienDangNhap = controller.layNhanVienDangNhapHienTai();
                capNhatHienThiNhanVien();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ValidationError validation = controller.kiemTraHoaDon(
                kh, nhanVienDangNhap, chiTietHoaDon, tc != null);
        if (validation != ValidationError.NONE) {
            JOptionPane.showMessageDialog(this, validation.getMessage(),
                    validation == ValidationError.CHUA_CO_NHAN_VIEN ? "Lỗi" : "Thông báo",
                    validation == ValidationError.CHUA_CO_NHAN_VIEN
                            ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
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

        KhuyenMai_Entity km =
                (KhuyenMai_Entity) cboKhuyenMai.getSelectedItem();

        Thue_entity thue =
                (Thue_entity) cboThue.getSelectedItem();

        String maHD = txtMaHD.getText().trim();
        InvoiceTotals totals;
        try {
            totals = controller.luuHoaDon(new InvoiceInput(maHD, LocalDate.now(), kh, tc,
                    nhanVienDangNhap, km, thue, chiTietHoaDon));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể lưu hóa đơn; mọi thay đổi đã được hoàn tác.\nChi tiết: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

            int luaChon = JOptionPane.showConfirmDialog(
                    this,
                    "Đã lưu hóa đơn " + maHD + " thành công!"
                    + "\nTạm tính: " + dinhDangTien(totals.getTienHang())
                    + "\nTiền giảm: " + dinhDangTien(totals.getTienGiam())
                    + "\nTiền thuế: " + dinhDangTien(totals.getTienThue())
                    + "\nTổng thanh toán: " + dinhDangTien(totals.getTongThanhToan())
                    + "\n\nBạn có muốn mở xem / in hóa đơn ngay không?",
                    "Lập hóa đơn thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            lamMoi();

            if (luaChon == JOptionPane.YES_OPTION) {
                ChiTietHoaDon_gui.hienThi(this, maHD,null);
            }
    }

    // ==================== LÀM MỚI ====================

    private void lamMoi() {

        model.setRowCount(0);
        chiTietHoaDon.clear();

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
        return controller.taoMaHoaDonTiepTheo();
    }
    // ==================== ĐỊNH DẠNG TIỀN ====================
    private String dinhDangTien(double tien) {
        return String.format("%,.0f đ", tien);
    }
}