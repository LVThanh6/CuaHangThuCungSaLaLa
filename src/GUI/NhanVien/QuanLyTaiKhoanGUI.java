package GUI.NhanVien;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;

import dao.TaiKhoan_DAO;
import dao.TaiKhoan_DAO.TaiKhoanChiTiet;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Quản lý Tài khoản người dùng trong SALALA Pet Shop.
 * Hỗ trợ toàn diện xem danh sách, lọc, tìm kiếm, cấp mới, sửa vai trò,
 * đặt lại mật khẩu (SHA-256), khóa/mở khóa và xóa tài khoản.
 */
public class QuanLyTaiKhoanGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    private final TaiKhoan_DAO taiKhoanDAO;
    private TableMau bangTaiKhoan;
    private DefaultTableModel modelBang;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtTimKiem;
    private JComboBox<String> cbLocVaiTro;
    private JComboBox<String> cbLocTrangThai;
    private JButton btnKhoaMo;
    private JButton btnResetPass;
    private JButton btnSua;
    private JButton btnXoa;

    private JLabel lblTongTK;
    private JLabel lblDangHoatDong;
    private JLabel lblBiKhoa;
    private JLabel lblPhanBo;

    private List<TaiKhoanChiTiet> danhSachHienTai;

    public QuanLyTaiKhoanGUI() {
        this.taiKhoanDAO = new TaiKhoan_DAO();
        khoiTaoGiaoDien();
        taiDuLieuLenBang();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        // 1. PHẦN TRÊN: TIÊU ĐỀ + THẺ KPI
        add(taoPhanTren(), BorderLayout.NORTH);

        // 2. PHẦN DƯỚI: THANH CÔNG CỤ + BẢNG DỮ LIỆU
        add(taoPhanBang(), BorderLayout.CENTER);
    }

    // ==========================================
    // PHẦN TRÊN: TIÊU ĐỀ & THỐNG KÊ NHANH KPI
    // ==========================================
    private JPanel taoPhanTren() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Hàng Tiêu đề & Nút thao tác chính
        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);

        JPanel pTrai = new JPanel();
        pTrai.setLayout(new BoxLayout(pTrai, BoxLayout.Y_AXIS));
        pTrai.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("Hệ thống  /  Quản lý tài khoản người dùng");
        lblBreadcrumb.setFont(RangBuocGiaoDien.fontThuong(12));
        lblBreadcrumb.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblTitle = new JLabel("Quản Lý Tài Khoản Người Dùng");
        lblTitle.setFont(RangBuocGiaoDien.fontDam(22));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblSub = new JLabel("Phân quyền tài khoản, cấp phát người dùng, đặt lại mật khẩu và kiểm soát trạng thái đăng nhập");
        lblSub.setFont(RangBuocGiaoDien.fontThuong(12));
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        pTrai.add(lblBreadcrumb);
        pTrai.add(Box.createVerticalStrut(2));
        pTrai.add(lblTitle);
        pTrai.add(Box.createVerticalStrut(2));
        pTrai.add(lblSub);
        hangTieuDe.add(pTrai, BorderLayout.WEST);

        // Nút thêm mới & làm mới
        JPanel pPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pPhai.setOpaque(false);

        JButton btnLamMoi = taoNutPhu("🔄 Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieuLenBang());
        pPhai.add(btnLamMoi);

        JButton btnThemTK = taoNutChinh("+ Thêm tài khoản mới");
        btnThemTK.addActionListener(e -> xuLyThemTaiKhoan());
        pPhai.add(btnThemTK);

        hangTieuDe.add(pPhai, BorderLayout.EAST);
        p.add(hangTieuDe);
        p.add(Box.createVerticalStrut(14));

        // 4 Thẻ KPI Thống kê
        JPanel pKpi = new JPanel(new GridLayout(1, 4, 14, 0));
        pKpi.setOpaque(false);

        lblTongTK = new JLabel("0");
        lblDangHoatDong = new JLabel("0");
        lblBiKhoa = new JLabel("0");
        lblPhanBo = new JLabel("0 QL / 0 NV / 0 BS");

        pKpi.add(taoTheKpi("TỔNG TÀI KHOẢN", lblTongTK, new Color(13, 148, 136), "👥"));
        pKpi.add(taoTheKpi("ĐANG HOẠT ĐỘNG", lblDangHoatDong, new Color(16, 185, 129), "🟢"));
        pKpi.add(taoTheKpi("ĐANG BỊ KHÓA", lblBiKhoa, new Color(239, 68, 68), "🔒"));
        pKpi.add(taoTheKpi("PHÂN BỔ VAI TRÒ", lblPhanBo, new Color(99, 102, 241), "🛡"));

        p.add(pKpi);
        return p;
    }

    private JPanel taoTheKpi(String tieuDe, JLabel lblGiaTri, Color mauNhan, String icon) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel pTrai = new JPanel();
        pTrai.setLayout(new BoxLayout(pTrai, BoxLayout.Y_AXIS));
        pTrai.setOpaque(false);

        JLabel lblT = new JLabel(tieuDe);
        lblT.setFont(RangBuocGiaoDien.fontDam(11));
        lblT.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblGiaTri.setFont(RangBuocGiaoDien.fontDam(18));
        lblGiaTri.setForeground(mauNhan);

        pTrai.add(lblT);
        pTrai.add(Box.createVerticalStrut(4));
        pTrai.add(lblGiaTri);
        card.add(pTrai, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        card.add(lblIcon, BorderLayout.EAST);

        return card;
    }

    // ==========================================
    // PHẦN BẢNG DỮ LIỆU + THANH CÔNG CỤ
    // ==========================================
    private JPanel taoPhanBang() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // 1. THANH TÌM KIẾM & BỘ LỌC
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel pLocTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pLocTrai.setOpaque(false);

        txtTimKiem = new JTextField("Tìm theo tên đăng nhập, họ tên, mã TK...");
        txtTimKiem.setPreferredSize(new Dimension(280, 36));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        txtTimKiem.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        txtTimKiem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().equals("Tìm theo tên đăng nhập, họ tên, mã TK...")) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().trim().isEmpty()) {
                    txtTimKiem.setText("Tìm theo tên đăng nhập, họ tên, mã TK...");
                    txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
                }
            }
        });
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                locDuLieu();
            }
        });
        pLocTrai.add(txtTimKiem);

        // Lọc vai trò
        cbLocVaiTro = new JComboBox<>(new String[]{
                "Vai trò: Tất cả",
                "Quản lý",
                "Nhân viên bán hàng",
                "Nhân viên dịch vụ",
                "Nhân viên quản lý kho",
                "Bác sĩ"
        });
        cbLocVaiTro.setFont(RangBuocGiaoDien.fontThuong(12));
        cbLocVaiTro.setPreferredSize(new Dimension(170, 36));
        cbLocVaiTro.addActionListener(e -> locDuLieu());
        pLocTrai.add(cbLocVaiTro);

        // Lọc trạng thái
        cbLocTrangThai = new JComboBox<>(new String[]{
                "Trạng thái: Tất cả",
                "Hoạt động",
                "Đã khóa"
        });
        cbLocTrangThai.setFont(RangBuocGiaoDien.fontThuong(12));
        cbLocTrangThai.setPreferredSize(new Dimension(150, 36));
        cbLocTrangThai.addActionListener(e -> locDuLieu());
        pLocTrai.add(cbLocTrangThai);

        toolbar.add(pLocTrai, BorderLayout.WEST);

        // Các nút thao tác dòng được chọn
        JPanel pThaoTacPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pThaoTacPhai.setOpaque(false);

        btnKhoaMo = taoNutPhu("Khóa / Mở");
        btnKhoaMo.addActionListener(e -> xuLyKhoaMoKhoa());
        pThaoTacPhai.add(btnKhoaMo);

        btnResetPass = taoNutPhu("Đặt lại MK");
        btnResetPass.addActionListener(e -> xuLyResetMatKhau());
        pThaoTacPhai.add(btnResetPass);

        btnSua = taoNutPhu("Sửa vai trò");
        btnSua.addActionListener(e -> xuLySuaTaiKhoan());
        pThaoTacPhai.add(btnSua);

        btnXoa = taoNutPhu("Xóa");
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.addActionListener(e -> xuLyXoaTaiKhoan());
        pThaoTacPhai.add(btnXoa);

        toolbar.add(pThaoTacPhai, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // 2. BẢNG DỮ LIỆU
        String[] tieuDeCot = {"Mã TK", "Tên đăng nhập", "Họ tên người dùng", "Vai trò", "Trạng thái", "Quản lý phụ trách", "Số điện thoại"};
        modelBang = new DefaultTableModel(tieuDeCot, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bangTaiKhoan = new TableMau();
        bangTaiKhoan.setModel(modelBang);
        bangTaiKhoan.getSelectionModel().addListSelectionListener(e -> capNhatNutThaoTac());

        rowSorter = new TableRowSorter<>(modelBang);
        bangTaiKhoan.setRowSorter(rowSorter);

        // Căn chỉnh renderer cho cột Trạng thái
        bangTaiKhoan.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value != null) ? value.toString() : "";
                if (val.contains("Hoạt động")) {
                    l.setForeground(new Color(16, 185, 129));
                    l.setFont(RangBuocGiaoDien.fontDam(12));
                } else {
                    l.setForeground(new Color(239, 68, 68));
                    l.setFont(RangBuocGiaoDien.fontDam(12));
                }
                return l;
            }
        });

        capNhatNutThaoTac();

        JScrollPane scroll = new JScrollPane(bangTaiKhoan);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Footer gợi ý
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel lblGoiY = new JLabel("Chọn một dòng rồi dùng các nút thao tác phía trên. Mật khẩu được mã hóa SHA-256.");
        lblGoiY.setFont(RangBuocGiaoDien.fontThuong(11));
        lblGoiY.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        footer.add(lblGoiY, BorderLayout.WEST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void capNhatNutThaoTac() {
        boolean coDong = bangTaiKhoan != null && bangTaiKhoan.getSelectedRow() >= 0;
        if (btnKhoaMo != null) btnKhoaMo.setEnabled(coDong);
        if (btnResetPass != null) btnResetPass.setEnabled(coDong);
        if (btnSua != null) btnSua.setEnabled(coDong);
        if (btnXoa != null) btnXoa.setEnabled(coDong);
    }

    // ==========================================
    // TẢI DỮ LIỆU & LỌC
    // ==========================================
    public void taiDuLieuLenBang() {
        modelBang.setRowCount(0);
        danhSachHienTai = taiKhoanDAO.getAllTaiKhoanChiTiet();

        int tong = danhSachHienTai.size();
        int hoatDong = 0;
        int biKhoa = 0;
        int ql = 0, nv = 0, bs = 0;

        for (TaiKhoanChiTiet item : danhSachHienTai) {
            if (item.isTrangThai()) {
                hoatDong++;
            } else {
                biKhoa++;
            }

            String vt = item.getVaiTro();
            if ("Quản lý".equalsIgnoreCase(vt)) {
                ql++;
            } else if (vt != null && vt.toLowerCase().contains("bác sĩ")) {
                bs++;
            } else {
                nv++;
            }

            String trangThaiText = item.isTrangThai() ? "🟢 Hoạt động" : "🔴 Đã khóa";
            String qlText = (item.getTenQuanLy() != null) ? item.getTenQuanLy() + " (" + item.getMaQuanLy() + ")" : "Toàn quyền";

            modelBang.addRow(new Object[]{
                    item.getMaTK(),
                    item.getTenDangNhap(),
                    item.getHoTenNguoiDung(),
                    item.getVaiTro(),
                    trangThaiText,
                    qlText,
                    item.getSdtNguoiDung()
            });
        }

        // Cập nhật số liệu thẻ KPI
        lblTongTK.setText(String.valueOf(tong));
        lblDangHoatDong.setText(String.valueOf(hoatDong));
        lblBiKhoa.setText(String.valueOf(biKhoa));
        lblPhanBo.setText(ql + " QL / " + nv + " NV / " + bs + " BS");

        locDuLieu();
        capNhatNutThaoTac();
    }

    private void locDuLieu() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.equals("Tìm theo tên đăng nhập, họ tên, mã TK...")) keyword = "";

        String vaiTro = (String) cbLocVaiTro.getSelectedItem();
        String trangThai = (String) cbLocTrangThai.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + keyword, 0, 1, 2));
        }

        if (vaiTro != null && !vaiTro.equals("Vai trò: Tất cả")) {
            filters.add(RowFilter.regexFilter("(?i)^" + vaiTro, 3));
        }

        if (trangThai != null && !trangThai.equals("Trạng thái: Tất cả")) {
            filters.add(RowFilter.regexFilter("(?i)" + trangThai, 4));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private TaiKhoanChiTiet getDongDuocChon() {
        int viewRow = bangTaiKhoan.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = bangTaiKhoan.convertRowIndexToModel(viewRow);
        String maTK = (String) modelBang.getValueAt(modelRow, 0);

        if (danhSachHienTai != null) {
            for (TaiKhoanChiTiet item : danhSachHienTai) {
                if (item.getMaTK().equals(maTK)) return item;
            }
        }
        return null;
    }

    // ==========================================
    // CÁC HÀNH ĐỘNG CRUD
    // ==========================================
    private void xuLyThemTaiKhoan() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm tài khoản mới", true);
        dlg.setSize(480, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        String maPhatSinh = taiKhoanDAO.phatSinhMaTK();

        JTextField txtMa = taoInputText(maPhatSinh, false);
        JTextField txtUser = taoInputText("", true);
        JPasswordField txtPass = new JPasswordField("123456");
        txtPass.setPreferredSize(new Dimension(300, 36));
        txtPass.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true), new EmptyBorder(0, 10, 0, 10)));

        JComboBox<String> cbVaiTro = new JComboBox<>(new String[]{
                "Nhân viên bán hàng",
                "Nhân viên dịch vụ",
                "Nhân viên quản lý kho",
                "Bác sĩ",
                "Quản lý"
        });
        cbVaiTro.setPreferredSize(new Dimension(300, 36));

        // Danh sách liên kết nhân viên / bác sĩ chưa có tài khoản
        List<Map<String, String>> dsNVChuaCo = taiKhoanDAO.getNhanVienChuaCoTaiKhoan();
        List<Map<String, String>> dsBSChuaCo = taiKhoanDAO.getBacSiChuaCoTaiKhoan();

        DefaultComboBoxModel<String> modelLienKet = new DefaultComboBoxModel<>();
        modelLienKet.addElement("-- Không liên kết đối tượng cụ thể --");
        for (Map<String, String> m : dsNVChuaCo) {
            modelLienKet.addElement("[Nhân viên] " + m.get("ten") + " (" + m.get("ma") + " - " + m.get("vaiTro") + ")");
        }
        for (Map<String, String> m : dsBSChuaCo) {
            modelLienKet.addElement("[Bác sĩ] " + m.get("ten") + " (" + m.get("ma") + " - " + m.get("vaiTro") + ")");
        }
        JComboBox<String> cbLienKet = new JComboBox<>(modelLienKet);
        cbLienKet.setPreferredSize(new Dimension(300, 36));

        // Danh sách quản lý
        List<Map<String, String>> dsQL = taiKhoanDAO.getDanhSachQuanLy();
        DefaultComboBoxModel<String> modelQL = new DefaultComboBoxModel<>();
        modelQL.addElement("-- Không chỉ định (Toàn quyền) --");
        for (Map<String, String> ql : dsQL) {
            modelQL.addElement(ql.get("hoTen") + " (" + ql.get("maQuanLy") + ")");
        }
        JComboBox<String> cbQL = new JComboBox<>(modelQL);
        cbQL.setPreferredSize(new Dimension(300, 36));

        JCheckBox chkKichHoat = new JCheckBox("Kích hoạt tài khoản ngay (Trạng thái hoạt động)", true);
        chkKichHoat.setOpaque(false);

        // Tự động gợi ý vai trò khi chọn đối tượng liên kết
        cbLienKet.addActionListener(e -> {
            int idx = cbLienKet.getSelectedIndex();
            if (idx > 0) {
                int nvCount = dsNVChuaCo.size();
                if (idx <= nvCount) {
                    Map<String, String> nv = dsNVChuaCo.get(idx - 1);
                    String chucVu = nv.get("vaiTro");
                    if (chucVu != null) cbVaiTro.setSelectedItem(chucVu);
                } else {
                    cbVaiTro.setSelectedItem("Bác sĩ");
                }
            }
        });

        p.add(taoDongField("Mã tài khoản (Tự động):", txtMa));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Tên đăng nhập (*):", txtUser));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Mật khẩu ban đầu (Mặc định: 123456):", txtPass));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Vai trò người dùng:", cbVaiTro));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Liên kết đối tượng:", cbLienKet));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Quản lý phụ trách:", cbQL));
        p.add(Box.createVerticalStrut(10));
        p.add(chkKichHoat);
        p.add(Box.createVerticalStrut(16));

        // Nút lưu
        JPanel pNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pNut.setOpaque(false);

        JButton btnHuy = taoNutPhu("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());
        pNut.add(btnHuy);

        JButton btnLuu = taoNutChinh("Lưu tài khoản");
        btnLuu.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String pWord = new String(txtPass.getPassword()).trim();
            if (u.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (taiKhoanDAO.isTenDangNhapTonTai(u, null)) {
                JOptionPane.showMessageDialog(dlg, "Tên đăng nhập '" + u + "' đã tồn tại! Vui lòng chọn tên khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pWord.isEmpty()) {
                pWord = "123456";
            }

            String vt = (String) cbVaiTro.getSelectedItem();
            boolean tt = chkKichHoat.isSelected();

            // Mã quản lý
            String maQLChon = null;
            int qlIdx = cbQL.getSelectedIndex();
            if (qlIdx > 0) {
                maQLChon = dsQL.get(qlIdx - 1).get("maQuanLy");
            }

            // Đối tượng liên kết
            String loaiDT = null;
            String maDT = null;
            int lkIdx = cbLienKet.getSelectedIndex();
            if (lkIdx > 0) {
                if (lkIdx <= dsNVChuaCo.size()) {
                    loaiDT = "NhanVien";
                    maDT = dsNVChuaCo.get(lkIdx - 1).get("ma");
                } else {
                    loaiDT = "BacSy";
                    maDT = dsBSChuaCo.get(lkIdx - 1 - dsNVChuaCo.size()).get("ma");
                }
            }

            TaiKhoan tk = new TaiKhoan(maPhatSinh, u, pWord, vt, tt, maQLChon);
            boolean ok = taiKhoanDAO.themTaiKhoan(tk, loaiDT, maDT);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Tạo tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(dlg, "Không thể lưu tài khoản vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        pNut.add(btnLuu);

        p.add(pNut);
        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void xuLySuaTaiKhoan() {
        TaiKhoanChiTiet chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh sửa tài khoản " + chon.getMaTK(), true);
        dlg.setSize(440, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtMa = taoInputText(chon.getMaTK() + " (" + chon.getTenDangNhap() + ")", false);
        JTextField txtChu = taoInputText(chon.getHoTenNguoiDung(), false);

        JComboBox<String> cbVaiTro = new JComboBox<>(new String[]{
                "Nhân viên bán hàng",
                "Nhân viên dịch vụ",
                "Nhân viên quản lý kho",
                "Bác sĩ",
                "Quản lý"
        });
        cbVaiTro.setSelectedItem(chon.getVaiTro());
        cbVaiTro.setPreferredSize(new Dimension(300, 36));

        // Quản lý
        List<Map<String, String>> dsQL = taiKhoanDAO.getDanhSachQuanLy();
        DefaultComboBoxModel<String> modelQL = new DefaultComboBoxModel<>();
        modelQL.addElement("-- Không chỉ định (Toàn quyền) --");
        int selectQLIdx = 0;
        for (int i = 0; i < dsQL.size(); i++) {
            Map<String, String> ql = dsQL.get(i);
            modelQL.addElement(ql.get("hoTen") + " (" + ql.get("maQuanLy") + ")");
            if (ql.get("maQuanLy").equals(chon.getMaQuanLy())) {
                selectQLIdx = i + 1;
            }
        }
        JComboBox<String> cbQL = new JComboBox<>(modelQL);
        cbQL.setSelectedIndex(selectQLIdx);
        cbQL.setPreferredSize(new Dimension(300, 36));

        JCheckBox chkKichHoat = new JCheckBox("Trạng thái: Đang hoạt động", chon.isTrangThai());
        chkKichHoat.setOpaque(false);

        p.add(taoDongField("Tài khoản:", txtMa));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Chủ tài khoản:", txtChu));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Vai trò người dùng:", cbVaiTro));
        p.add(Box.createVerticalStrut(10));
        p.add(taoDongField("Quản lý phụ trách:", cbQL));
        p.add(Box.createVerticalStrut(10));
        p.add(chkKichHoat);
        p.add(Box.createVerticalStrut(16));

        JPanel pNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pNut.setOpaque(false);
        JButton btnHuy = taoNutPhu("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());
        pNut.add(btnHuy);

        JButton btnLuu = taoNutChinh("Cập nhật");
        btnLuu.addActionListener(e -> {
            String vtMoi = (String) cbVaiTro.getSelectedItem();
            boolean ttMoi = chkKichHoat.isSelected();
            String maQLMoi = null;
            if (cbQL.getSelectedIndex() > 0) {
                maQLMoi = dsQL.get(cbQL.getSelectedIndex() - 1).get("maQuanLy");
            }

            TaiKhoan tk = new TaiKhoan(chon.getMaTK(), chon.getTenDangNhap(), null, vtMoi, ttMoi, maQLMoi);
            boolean ok = taiKhoanDAO.capNhatTaiKhoan(tk);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Cập nhật tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(dlg, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        pNut.add(btnLuu);

        p.add(pNut);
        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void xuLyKhoaMoKhoa() {
        TaiKhoanChiTiet chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để thao tác!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean trangThaiMoi = !chon.isTrangThai();
        String hanhDong = trangThaiMoi ? "MỞ KHÓA" : "KHÓA";

        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + hanhDong + " tài khoản [" + chon.getTenDangNhap() + "]?",
                "Xác nhận " + hanhDong,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = taiKhoanDAO.datTrangThaiTaiKhoan(chon.getMaTK(), trangThaiMoi);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã " + hanhDong.toLowerCase() + " tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái tài khoản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyResetMatKhau() {
        TaiKhoanChiTiet chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để đặt lại mật khẩu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String matKhauMoi = JOptionPane.showInputDialog(this,
                "Nhập mật khẩu mới cho tài khoản [" + chon.getTenDangNhap() + "]:\n(Mật khẩu sẽ được mã hóa bảo mật SHA-256)",
                "123456");

        if (matKhauMoi != null) {
            matKhauMoi = matKhauMoi.trim();
            if (matKhauMoi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (matKhauMoi.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu phải từ 6 ký tự trở lên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = taiKhoanDAO.doiMatKhau(chon.getMaTK(), matKhauMoi);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công!\nMật khẩu mới: " + matKhauMoi, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXoaTaiKhoan() {
        TaiKhoanChiTiet chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XÓA tài khoản [" + chon.getTenDangNhap() + "] (" + chon.getHoTenNguoiDung() + ")?\n" +
                        "Hệ thống sẽ tự động gỡ liên kết tài khoản này khỏi thông tin nhân viên/bác sĩ.",
                "Cảnh báo xóa tài khoản",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = taiKhoanDAO.xoaTaiKhoan(chon.getMaTK());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // TIỆN ÍCH GIAO DIỆN HỖ TRỢ
    // ==========================================
    private JPanel taoDongField(String tieuDe, Component comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel l = new JLabel(tieuDe);
        l.setFont(RangBuocGiaoDien.fontDam(12));
        l.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        return p;
    }

    private JTextField taoInputText(String val, boolean editable) {
        JTextField tf = new JTextField(val);
        tf.setEditable(editable);
        tf.setPreferredSize(new Dimension(300, 36));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
        tf.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }

    private JButton taoNutChinh(String text) {
        JButton btn = new JButton(text);
        btn.setFont(RangBuocGiaoDien.fontDam(13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    private JButton taoNutPhu(String text) {
        JButton btn = new JButton(text);
        btn.setFont(RangBuocGiaoDien.fontVua(12));
        btn.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }
}
