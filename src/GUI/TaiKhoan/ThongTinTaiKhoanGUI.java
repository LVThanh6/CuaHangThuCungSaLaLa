package GUI.TaiKhoan;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;

import controller.TaiKhoan.ThongTinTaiKhoanController;
import entity.TaiKhoan.TaiKhoan;

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

/**
 * Giao diện Tài khoản người dùng trong SALALA Pet Shop (gộp từ
 * QuanLyTaiKhoanGUI,
 * ThongTinTaiKhoanDialog và DoiMatKhauDialog). Nghiệp vụ nằm ở
 * ThongTinTaiKhoanController.
 * - Panel chính: xem danh sách, lọc, tìm kiếm, cấp mới, sửa vai trò,
 * đặt lại mật khẩu (SHA-256), khóa/mở khóa và xóa tài khoản.
 * - ThongTinTaiKhoanDialog: hồ sơ tài khoản đang đăng nhập.
 * - DoiMatKhauDialog: người dùng tự đổi mật khẩu cá nhân.
 */
public class ThongTinTaiKhoanGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ThongTinTaiKhoanController controller = new ThongTinTaiKhoanController();
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

    private List<ThongTinTaiKhoanController.TaiKhoanItem> danhSachHienTai;

    public ThongTinTaiKhoanGUI() {
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

        JLabel lblSub = new JLabel(
                "Phân quyền tài khoản, cấp phát người dùng, đặt lại mật khẩu và kiểm soát trạng thái đăng nhập");
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

        JButton btnLamMoi = taoNutPhu("Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieuLenBang());
        pPhai.add(btnLamMoi);

        JButton btnThemTK = taoNutChinh("Thêm tài khoản mới");
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

        pKpi.add(taoTheKpi("TỔNG TÀI KHOẢN", lblTongTK, new Color(13, 148, 136)));
        pKpi.add(taoTheKpi("ĐANG HOẠT ĐỘNG", lblDangHoatDong, new Color(16, 185, 129)));
        pKpi.add(taoTheKpi("ĐANG BỊ KHÓA", lblBiKhoa, new Color(239, 68, 68)));
        pKpi.add(taoTheKpi("PHÂN BỔ VAI TRÒ", lblPhanBo, new Color(99, 102, 241)));

        p.add(pKpi);
        return p;
    }

    private JPanel taoTheKpi(String tieuDe, JLabel lblGiaTri, Color mauNhan) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

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
                new EmptyBorder(14, 16, 14, 16)));

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
                new EmptyBorder(0, 10, 0, 10)));
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
        cbLocVaiTro = new JComboBox<>(controller.danhSachVaiTroLoc());
        cbLocVaiTro.setFont(RangBuocGiaoDien.fontThuong(12));
        cbLocVaiTro.setPreferredSize(new Dimension(180, 36));
        cbLocVaiTro.addActionListener(e -> locDuLieu());
        pLocTrai.add(cbLocVaiTro);

        // Lọc trạng thái
        cbLocTrangThai = new JComboBox<>(controller.danhSachTrangThaiLoc());
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
        String[] tieuDeCot = { "Mã TK", "Tên đăng nhập", "Họ tên người dùng", "Vai trò", "Trạng thái",
                "Quản lý phụ trách", "Số điện thoại" };
        modelBang = new DefaultTableModel(tieuDeCot, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        bangTaiKhoan = new TableMau();
        bangTaiKhoan.setModel(modelBang);
        bangTaiKhoan.getSelectionModel().addListSelectionListener(e -> capNhatNutThaoTac());

        rowSorter = new TableRowSorter<>(modelBang);
        bangTaiKhoan.setRowSorter(rowSorter);

        // Căn chỉnh renderer cho cột Trạng thái
        bangTaiKhoan.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
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
        if (btnKhoaMo != null)
            btnKhoaMo.setEnabled(coDong);
        if (btnResetPass != null)
            btnResetPass.setEnabled(coDong);
        if (btnSua != null)
            btnSua.setEnabled(coDong);
        if (btnXoa != null)
            btnXoa.setEnabled(coDong);
    }

    // ==========================================
    // TẢI DỮ LIỆU & LỌC
    // ==========================================
    public void taiDuLieuLenBang() {
        modelBang.setRowCount(0);
        ThongTinTaiKhoanController.DuLieuDanhSach data = controller.taiDanhSach();
        danhSachHienTai = data.taiKhoan;

        for (ThongTinTaiKhoanController.TaiKhoanItem item : danhSachHienTai) {
            modelBang.addRow(controller.taoDongBang(item));
        }

        // Cập nhật số liệu thẻ KPI
        lblTongTK.setText(String.valueOf(danhSachHienTai.size()));
        lblDangHoatDong.setText(String.valueOf(data.hoatDong));
        lblBiKhoa.setText(String.valueOf(data.biKhoa));
        lblPhanBo.setText(data.quanLy + " QL/Admin / " + data.nhanVien + " NV / " + data.bacSi + " BS");

        locDuLieu();
        capNhatNutThaoTac();
    }

    private void locDuLieu() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.equals("Tìm theo tên đăng nhập, họ tên, mã TK..."))
            keyword = "";
        final String tuKhoa = keyword;
        final String vaiTro = (String) cbLocVaiTro.getSelectedItem();
        final String trangThai = (String) cbLocTrangThai.getSelectedItem();

        // Quy tắc lọc nằm ở controller; GUI chỉ ánh xạ dòng bảng -> dữ liệu
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (danhSachHienTai == null)
                    return true;
                int modelRow = entry.getIdentifier();
                if (modelRow < 0 || modelRow >= danhSachHienTai.size())
                    return true;
                return controller.khopBoLoc(danhSachHienTai.get(modelRow), tuKhoa, vaiTro, trangThai);
            }
        });
    }

    private ThongTinTaiKhoanController.TaiKhoanItem getDongDuocChon() {
        int viewRow = bangTaiKhoan.getSelectedRow();
        if (viewRow < 0)
            return null;
        int modelRow = bangTaiKhoan.convertRowIndexToModel(viewRow);
        String maTK = (String) modelBang.getValueAt(modelRow, 0);

        if (danhSachHienTai != null) {
            for (ThongTinTaiKhoanController.TaiKhoanItem item : danhSachHienTai) {
                if (item.getMaTK().equals(maTK))
                    return item;
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

        String maPhatSinh = controller.taoMaMoi();

        JTextField txtMa = taoInputText(maPhatSinh, false);
        JTextField txtUser = taoInputText("", true);
        JPasswordField txtPass = new JPasswordField(ThongTinTaiKhoanController.MAT_KHAU_MAC_DINH);
        txtPass.setPreferredSize(new Dimension(300, 36));
        txtPass.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(0, 10, 0, 10)));

        JComboBox<String> cbVaiTro = new JComboBox<>(controller.danhSachVaiTro());
        cbVaiTro.setPreferredSize(new Dimension(300, 36));

        // Danh sách liên kết nhân viên / bác sĩ chưa có tài khoản
        ThongTinTaiKhoanController.DanhMucTaoTaiKhoan catalog = controller.taiDanhMucTaoTaiKhoan();
        List<ThongTinTaiKhoanController.DoiTuongOption> doiTuongOptions = new ArrayList<>();

        DefaultComboBoxModel<String> modelLienKet = new DefaultComboBoxModel<>();
        modelLienKet.addElement("-- Không liên kết đối tượng cụ thể --");
        for (ThongTinTaiKhoanController.DoiTuongOption option : catalog.nhanVien) {
            modelLienKet.addElement("[Nhân viên] " + option.ten + " (" + option.ma + " - " + option.vaiTro + ")");
            doiTuongOptions.add(option);
        }
        for (ThongTinTaiKhoanController.DoiTuongOption option : catalog.bacSi) {
            modelLienKet.addElement("[Bác sĩ] " + option.ten + " (" + option.ma + " - " + option.vaiTro + ")");
            doiTuongOptions.add(option);
        }
        JComboBox<String> cbLienKet = new JComboBox<>(modelLienKet);
        cbLienKet.setPreferredSize(new Dimension(300, 36));

        // Danh sách quản lý
        List<ThongTinTaiKhoanController.QuanLyOption> dsQL = catalog.quanLy;
        DefaultComboBoxModel<String> modelQL = new DefaultComboBoxModel<>();
        modelQL.addElement("-- Không chỉ định (Toàn quyền) --");
        for (ThongTinTaiKhoanController.QuanLyOption ql : dsQL) {
            modelQL.addElement(ql.hoTen + " (" + ql.ma + ")");
        }
        JComboBox<String> cbQL = new JComboBox<>(modelQL);
        cbQL.setPreferredSize(new Dimension(300, 36));

        JCheckBox chkKichHoat = new JCheckBox("Kích hoạt tài khoản ngay (Trạng thái hoạt động)", true);
        chkKichHoat.setOpaque(false);

        // Tự động gợi ý vai trò khi chọn đối tượng liên kết
        cbLienKet.addActionListener(e -> {
            int idx = cbLienKet.getSelectedIndex();
            if (idx > 0) {
                cbVaiTro.setSelectedItem(controller.vaiTroGoiY(doiTuongOptions.get(idx - 1)));
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
            String maQLChon = null;
            int qlIdx = cbQL.getSelectedIndex();
            if (qlIdx > 0)
                maQLChon = dsQL.get(qlIdx - 1).ma;
            ThongTinTaiKhoanController.DoiTuongOption doiTuong = null;
            int lkIdx = cbLienKet.getSelectedIndex();
            if (lkIdx > 0)
                doiTuong = doiTuongOptions.get(lkIdx - 1);
            ThongTinTaiKhoanController.KetQuaTaoTaiKhoan result = controller.taoTaiKhoan(
                    new ThongTinTaiKhoanController.ThongTinTaiKhoanMoi(maPhatSinh,
                            txtUser.getText(), new String(txtPass.getPassword()),
                            (String) cbVaiTro.getSelectedItem(), chkKichHoat.isSelected(),
                            maQLChon, doiTuong));
            if (result.thanhCong) {
                JOptionPane.showMessageDialog(dlg, "Tạo tài khoản thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(dlg, result.thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        pNut.add(btnLuu);

        p.add(pNut);
        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void xuLySuaTaiKhoan() {
        ThongTinTaiKhoanController.TaiKhoanItem chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để chỉnh sửa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh sửa tài khoản " + chon.getMaTK(), true);
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

        JComboBox<String> cbVaiTro = new JComboBox<>(controller.danhSachVaiTro());
        cbVaiTro.setSelectedItem(chon.getVaiTro());
        cbVaiTro.setPreferredSize(new Dimension(300, 36));

        // Quản lý
        List<ThongTinTaiKhoanController.QuanLyOption> dsQL = controller.taiDanhSachQuanLy();
        DefaultComboBoxModel<String> modelQL = new DefaultComboBoxModel<>();
        modelQL.addElement("-- Không chỉ định (Toàn quyền) --");
        int selectQLIdx = 0;
        for (int i = 0; i < dsQL.size(); i++) {
            ThongTinTaiKhoanController.QuanLyOption ql = dsQL.get(i);
            modelQL.addElement(ql.hoTen + " (" + ql.ma + ")");
            if (ql.ma.equals(chon.getMaQuanLy())) {
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
                maQLMoi = dsQL.get(cbQL.getSelectedIndex() - 1).ma;
            }

            boolean ok = controller.capNhatTaiKhoan(chon.getMaTK(), chon.getTenDangNhap(), vtMoi, ttMoi, maQLMoi);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Cập nhật tài khoản thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
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
        ThongTinTaiKhoanController.TaiKhoanItem chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để thao tác!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean trangThaiMoi = !chon.isTrangThai();
        String hanhDong = trangThaiMoi ? "MỞ KHÓA" : "KHÓA";

        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + hanhDong + " tài khoản [" + chon.getTenDangNhap() + "]?",
                "Xác nhận " + hanhDong,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = controller.datTrangThai(chon.getMaTK(), trangThaiMoi);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã " + hanhDong.toLowerCase() + " tài khoản thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái tài khoản!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyResetMatKhau() {
        ThongTinTaiKhoanController.TaiKhoanItem chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để đặt lại mật khẩu!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String matKhauMoi = JOptionPane.showInputDialog(this,
                "Nhập mật khẩu mới cho tài khoản [" + chon.getTenDangNhap()
                        + "]:\n(Mật khẩu sẽ được mã hóa bảo mật SHA-256)",
                ThongTinTaiKhoanController.MAT_KHAU_MAC_DINH);

        if (matKhauMoi != null) {
            matKhauMoi = matKhauMoi.trim();
            ThongTinTaiKhoanController.KetQuaMatKhau result = controller.datLaiMatKhau(chon.getMaTK(), matKhauMoi);
            if (result.thanhCong) {
                JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công!\nMật khẩu mới: " + matKhauMoi,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXoaTaiKhoan() {
        ThongTinTaiKhoanController.TaiKhoanItem chon = getDongDuocChon();
        if (chon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XÓA tài khoản [" + chon.getTenDangNhap() + "] (" + chon.getHoTenNguoiDung() + ")?\n" +
                        "Hệ thống sẽ tự động gỡ liên kết tài khoản này khỏi thông tin nhân viên/bác sĩ.",
                "Cảnh báo xóa tài khoản",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = controller.xoaTaiKhoan(chon.getMaTK());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
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
                new EmptyBorder(0, 10, 0, 10)));
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

    // ==========================================
    // DIALOG: HỒ SƠ TÀI KHOẢN ĐANG ĐĂNG NHẬP
    // ==========================================
    /**
     * Dialog hiển thị hồ sơ thông tin tài khoản người dùng đang đăng nhập.
     */
    public static class ThongTinTaiKhoanDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        public ThongTinTaiKhoanDialog(Frame parent, TaiKhoan tk, String tenHienThi) {
            super(parent, "Thông tin tài khoản", true);
            ThongTinTaiKhoanController.ThongTinHoSo thongTin = new ThongTinTaiKhoanController().taoThongTin(tk,
                    tenHienThi);
            setSize(440, 480);
            setLocationRelativeTo(parent);
            setResizable(false);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(24, 28, 20, 28));

            // Tên viết tắt đại diện (thay thế khối vẽ avatar tròn đồ họa)
            JLabel lblAvatar = new JLabel("[" + thongTin.chuCaiDau + "]");
            lblAvatar.setFont(RangBuocGiaoDien.fontDam(22));
            lblAvatar.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
            lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblAvatar);
            p.add(Box.createVerticalStrut(8));

            // Tên & Vai trò
            JLabel lblTen = new JLabel(thongTin.tenHienThi);
            lblTen.setFont(RangBuocGiaoDien.fontDam(18));
            lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblTen);

            JLabel lblVaiTro = new JLabel(thongTin.vaiTro);
            lblVaiTro.setFont(RangBuocGiaoDien.fontVua(13));
            lblVaiTro.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
            lblVaiTro.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblVaiTro);
            p.add(Box.createVerticalStrut(20));

            // Danh sách thông tin chi tiết
            JPanel pThongTin = new JPanel(new GridLayout(4, 2, 8, 10));
            pThongTin.setOpaque(false);
            pThongTin.setBorder(new CompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(14, 16, 14, 16)));

            themDongThongTin(pThongTin, "Mã tài khoản:", thongTin.maTK);
            themDongThongTin(pThongTin, "Tên đăng nhập:", thongTin.tenDangNhap);
            themDongThongTin(pThongTin, "Trạng thái:", thongTin.trangThai);
            themDongThongTin(pThongTin, "Mã quản lý:", thongTin.maQuanLy);

            p.add(pThongTin);
            p.add(Box.createVerticalStrut(20));

            // Nút Đổi mật khẩu & Đóng
            JPanel pNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            pNut.setOpaque(false);

            JButton btnDoiMatKhau = new JButton("Đổi mật khẩu");
            btnDoiMatKhau.setFont(RangBuocGiaoDien.fontVua(12));
            btnDoiMatKhau.setPreferredSize(new Dimension(130, 34));
            btnDoiMatKhau.setBackground(new Color(240, 253, 250));
            btnDoiMatKhau.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
            btnDoiMatKhau.setBorder(new LineBorder(new Color(204, 251, 241), 1));
            btnDoiMatKhau.setFocusPainted(false);
            btnDoiMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDoiMatKhau.addActionListener(e -> {
                dispose();
                new DoiMatKhauDialog(parent, tk).setVisible(true);
            });
            pNut.add(btnDoiMatKhau);

            JButton btnDong = new JButton("Đóng");
            btnDong.setFont(RangBuocGiaoDien.fontDam(12));
            btnDong.setPreferredSize(new Dimension(90, 34));
            btnDong.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
            btnDong.setForeground(Color.WHITE);
            btnDong.setBorderPainted(false);
            btnDong.setFocusPainted(false);
            btnDong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDong.addActionListener(e -> dispose());
            pNut.add(btnDong);

            p.add(pNut);
            add(p, BorderLayout.CENTER);
        }

        private void themDongThongTin(JPanel panel, String tieuDe, String giaTri) {
            JLabel lTieuDe = new JLabel(tieuDe);
            lTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
            lTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            JLabel lGiaTri = new JLabel(giaTri);
            lGiaTri.setFont(RangBuocGiaoDien.fontDam(12));
            lGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

            panel.add(lTieuDe);
            panel.add(lGiaTri);
        }
    }

    // ==========================================
    // DIALOG: ĐỔI MẬT KHẨU CÁ NHÂN
    // ==========================================
    /**
     * Dialog hỗ trợ người dùng tự đổi mật khẩu cá nhân.
     */
    public static class DoiMatKhauDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private JPasswordField txtMatKhauCu;
        private JPasswordField txtMatKhauMoi;
        private JPasswordField txtXacNhanMatKhau;
        private JLabel lblLoi;
        private final TaiKhoan taiKhoan;
        private final ThongTinTaiKhoanController controller = new ThongTinTaiKhoanController();

        public DoiMatKhauDialog(Frame parent, TaiKhoan taiKhoan) {
            super(parent, "Đổi mật khẩu tài khoản", true);
            this.taiKhoan = taiKhoan;
            khoiTaoGiaoDien();
        }

        private void khoiTaoGiaoDien() {
            setSize(420, 380);
            setLocationRelativeTo(getOwner());
            setResizable(false);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(24, 28, 20, 28));

            JLabel lblTieuDe = new JLabel("Đổi mật khẩu cá nhân");
            lblTieuDe.setFont(RangBuocGiaoDien.fontDam(18));
            lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            p.add(lblTieuDe);

            JLabel lblSub = new JLabel("Tài khoản: " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : ""));
            lblSub.setFont(RangBuocGiaoDien.fontThuong(12));
            lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            p.add(lblSub);
            p.add(Box.createVerticalStrut(18));

            // 1. Mật khẩu hiện tại
            p.add(taoNhan("Mật khẩu hiện tại:"));
            p.add(Box.createVerticalStrut(4));
            txtMatKhauCu = taoInputPass();
            p.add(txtMatKhauCu);
            p.add(Box.createVerticalStrut(12));

            // 2. Mật khẩu mới
            p.add(taoNhan("Mật khẩu mới (tối thiểu 6 ký tự):"));
            p.add(Box.createVerticalStrut(4));
            txtMatKhauMoi = taoInputPass();
            p.add(txtMatKhauMoi);
            p.add(Box.createVerticalStrut(12));

            // 3. Xác nhận mật khẩu mới
            p.add(taoNhan("Xác nhận mật khẩu mới:"));
            p.add(Box.createVerticalStrut(4));
            txtXacNhanMatKhau = taoInputPass();
            p.add(txtXacNhanMatKhau);
            p.add(Box.createVerticalStrut(8));

            lblLoi = new JLabel(" ");
            lblLoi.setFont(RangBuocGiaoDien.fontVua(11));
            lblLoi.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
            p.add(lblLoi);
            p.add(Box.createVerticalStrut(14));

            // Nút bấm
            JPanel pNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pNut.setOpaque(false);

            JButton btnHuy = new JButton("Hủy bỏ");
            btnHuy.setFont(RangBuocGiaoDien.fontVua(13));
            btnHuy.setPreferredSize(new Dimension(90, 34));
            btnHuy.setBackground(new Color(241, 245, 249));
            btnHuy.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1));
            btnHuy.setFocusPainted(false);
            btnHuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnHuy.addActionListener(e -> dispose());
            pNut.add(btnHuy);

            JButton btnXacNhan = new JButton("Cập nhật");
            btnXacNhan.setFont(RangBuocGiaoDien.fontDam(13));
            btnXacNhan.setPreferredSize(new Dimension(100, 34));
            btnXacNhan.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
            btnXacNhan.setForeground(Color.WHITE);
            btnXacNhan.setFocusPainted(false);
            btnXacNhan.setBorderPainted(false);
            btnXacNhan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnXacNhan.addActionListener(e -> xuLyDoiMatKhau());
            pNut.add(btnXacNhan);

            p.add(pNut);
            add(p, BorderLayout.CENTER);
        }

        private JLabel taoNhan(String text) {
            JLabel l = new JLabel(text);
            l.setFont(RangBuocGiaoDien.fontDam(12));
            l.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            return l;
        }

        private JPasswordField taoInputPass() {
            JPasswordField pf = new JPasswordField();
            pf.setPreferredSize(new Dimension(340, 36));
            pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pf.setBorder(new CompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(0, 10, 0, 10)));
            return pf;
        }

        private void xuLyDoiMatKhau() {
            String passCu = new String(txtMatKhauCu.getPassword()).trim();
            String passMoi = new String(txtMatKhauMoi.getPassword()).trim();
            String passXacNhan = new String(txtXacNhanMatKhau.getPassword()).trim();

            ThongTinTaiKhoanController.KetQuaDoiMatKhau result = controller.doiMatKhau(taiKhoan, passCu, passMoi,
                    passXacNhan);
            if (result.isThanhCong()) {
                JOptionPane.showMessageDialog(this,
                        "Đổi mật khẩu thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                lblLoi.setText(result.getThongBao());
                switch (result.getTruongNhap()) {
                    case MAT_KHAU_CU:
                        txtMatKhauCu.requestFocus();
                        break;
                    case MAT_KHAU_MOI:
                        txtMatKhauMoi.requestFocus();
                        break;
                    case XAC_NHAN:
                        txtXacNhanMatKhau.requestFocus();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}