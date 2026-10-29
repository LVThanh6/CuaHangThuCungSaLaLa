package GUI.NhanVien;

import GUI.Chung.RangBuocGiaoDien;
import GUI.Chung.TableMau;

import dao.NhanVien_DAO;
import entity.NhanVien_entity;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Giao diện Quản lý Nhân viên hoàn chỉnh của SALALA Pet Shop.
 * Tích hợp đầy đủ CRUD (Thêm, Sửa, Xóa, Tìm kiếm, Lọc, Xuất Excel)
 * và kết nối trực tiếp với CSDL SALALA qua NhanVien_DAO.
 */
public class QuanLyNhanVienGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    private NhanVien_DAO nhanVienDAO;
    private TableMau bangNhanVien;
    private DefaultTableModel modelBang;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtTimKiem;
    private JComboBox<String> cbLocChucVu;
    private JLabel lblTongSoNV;
    private final DecimalFormat dfTien = new DecimalFormat("#,### ₫");
    private final SimpleDateFormat dfNgay = new SimpleDateFormat("dd/MM/yyyy");

    public QuanLyNhanVienGUI() {
        nhanVienDAO = new NhanVien_DAO();
        khoiTaoGiaoDien();
        taiDuLieuLenBang();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 24, 16, 24));
        setMinimumSize(RangBuocGiaoDien.KICH_THUOC_TAB_TOI_THIEU);

        // 1. PHẦN TRÊN: TIÊU ĐỀ & THỐNG KÊ NHANH & NÚT THÊM
        add(taoPhanTren(), BorderLayout.NORTH);

        // 2. PHẦN GIỮA: THANH CÔNG CỤ + BẢNG DỮ LIỆU
        add(taoPhanBang(), BorderLayout.CENTER);
    }

    private JPanel taoPhanTren() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Hàng 1: Breadcrumb + Tiêu đề + Các nút thao tác chính
        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);

        JPanel benTrai = new JPanel();
        benTrai.setLayout(new BoxLayout(benTrai, BoxLayout.Y_AXIS));
        benTrai.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("Nhân viên / Quản lý danh sách nhân viên");
        lblBreadcrumb.setFont(RangBuocGiaoDien.fontThuong(12));
        lblBreadcrumb.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JLabel lblTitle = new JLabel("Quản lý danh sách nhân viên");
        lblTitle.setFont(RangBuocGiaoDien.fontDam(22));
        lblTitle.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblSub = new JLabel("Quản lý hồ sơ, chức vụ, thông tin liên hệ và lương cơ bản của nhân viên.");
        lblSub.setFont(RangBuocGiaoDien.fontThuong(13));
        lblSub.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        benTrai.add(lblBreadcrumb);
        benTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        benTrai.add(lblTitle);
        benTrai.add(Box.createRigidArea(new Dimension(0, 4)));
        benTrai.add(lblSub);
        hangTieuDe.add(benTrai, BorderLayout.WEST);

        // Bên phải: Nút Làm mới + Nút Sửa + Nút Xóa + Nút Thêm mới
        JPanel benPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        benPhai.setOpaque(false);

        JButton btnLamMoi = taoNutPhu("🔄 Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieuLenBang());

        JButton btnSua = taoNutPhu("✏ Sửa nhân viên");
        btnSua.addActionListener(e -> xuLySuaNhanVien());

        JButton btnXoa = taoNutPhu("🗑 Xóa nhân viên");
        btnXoa.setForeground(RangBuocGiaoDien.MAU_NGUY_HIEM_CHU);
        btnXoa.addActionListener(e -> xuLyXoaNhanVien());

        JButton btnThem = new JButton("+ Thêm nhân viên");
        btnThem.setFont(RangBuocGiaoDien.fontDam(13));
        btnThem.setBackground(RangBuocGiaoDien.MAU_CHU_DAO);
        btnThem.setForeground(Color.WHITE);
        btnThem.setFocusPainted(false);
        btnThem.setPreferredSize(new Dimension(160, 38));
        btnThem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> xuLyThemNhanVien());

        benPhai.add(btnLamMoi);
        benPhai.add(btnSua);
        benPhai.add(btnXoa);
        benPhai.add(btnThem);
        hangTieuDe.add(benPhai, BorderLayout.EAST);

        p.add(hangTieuDe);
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        return p;
    }

    private JPanel taoPhanBang() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(16, 16, 14, 16)));

        // 1. Thanh công cụ tìm kiếm và bộ lọc
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel panelTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTrai.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(320, 38));
        txtTimKiem.setFont(RangBuocGiaoDien.fontThuong(13));
        txtTimKiem.setToolTipText("Tìm kiếm theo mã, họ tên hoặc số điện thoại...");
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        // Placeholder tìm kiếm
        String placeholder = "Tìm theo tên, mã nhân viên, số điện thoại...";
        txtTimKiem.setText(placeholder);
        txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        txtTimKiem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().equals(placeholder)) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().isEmpty()) {
                    txtTimKiem.setText(placeholder);
                    txtTimKiem.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
                }
            }
        });

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                locDuLieu();
            }
        });
        panelTrai.add(txtTimKiem);

        cbLocChucVu = new JComboBox<>(new String[]{
                "Chức vụ: Tất cả",
                "Nhân viên bán hàng",
                "Nhân viên dịch vụ",
                "Nhân viên quản lý kho",
                "Quản lý"
        });
        cbLocChucVu.setPreferredSize(new Dimension(200, 38));
        cbLocChucVu.setFont(RangBuocGiaoDien.fontThuong(12));
        cbLocChucVu.setBackground(Color.WHITE);
        cbLocChucVu.addActionListener(e -> locDuLieu());
        panelTrai.add(cbLocChucVu);

        toolbar.add(panelTrai, BorderLayout.CENTER);

        // Nút Xuất file Excel bên phải toolbar
        JButton btnXuatExcel = taoNutPhu("📥 Xuất danh sách Excel");
        btnXuatExcel.addActionListener(e -> xuLyXuatExcel());
        toolbar.add(btnXuatExcel, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // 2. Bảng dữ liệu TableMau
        String[] tieuDe = {"Mã NV", "Họ và tên", "Chức vụ", "Số điện thoại", "Giới tính", "Ngày sinh", "Địa chỉ", "Lương cơ bản"};
        modelBang = new DefaultTableModel(tieuDe, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bangNhanVien = new TableMau();
        bangNhanVien.setModel(modelBang);

        rowSorter = new TableRowSorter<>(modelBang);
        bangNhanVien.setRowSorter(rowSorter);

        JScrollPane scroll = new JScrollPane(bangNhanVien);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // 3. Thanh thông tin dòng bên dưới
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblTongSoNV = new JLabel("Tổng số nhân viên: 0");
        lblTongSoNV.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTongSoNV.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        footer.add(lblTongSoNV, BorderLayout.WEST);

        JLabel lblHuongDan = new JLabel("💡 Mẹo: Nhấp đúp chuột vào nhân viên để sửa thông tin nhanh.");
        lblHuongDan.setFont(RangBuocGiaoDien.fontThuong(11));
        lblHuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);
        footer.add(lblHuongDan, BorderLayout.EAST);

        // Double click để sửa nhanh
        bangNhanVien.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xuLySuaNhanVien();
                }
            }
        });

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Tải lại danh sách nhân viên từ CSDL lên JTable
     */
    public void taiDuLieuLenBang() {
        modelBang.setRowCount(0);
        ArrayList<NhanVien_entity> list = nhanVienDAO.getAllNhanVien();
        for (NhanVien_entity nv : list) {
            String ngaySinhStr = nv.getNgaySinh() != null
                    ? dfNgay.format(Date.valueOf(nv.getNgaySinh())) : "";
            modelBang.addRow(new Object[]{
                    nv.getMaNhanVien(),
                    nv.getHoTen(),
                    nv.getChucVu(),
                    nv.getSdt(),
                    nv.getGioiTinh(),
                    ngaySinhStr,
                    nv.getDiaChi(),
                    dfTien.format(nv.getLuongCoBan())
            });
        }
        capNhatSoLuongHienThi();
    }

    private void locDuLieu() {
        String keyword = txtTimKiem.getText().trim();
        String placeholder = "Tìm theo tên, mã nhân viên, số điện thoại...";
        if (keyword.equals(placeholder)) keyword = "";

        String chucVu = (String) cbLocChucVu.getSelectedItem();
        java.util.List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + keyword, 0, 1, 3));
        }
        if (chucVu != null && !chucVu.equals("Chức vụ: Tất cả")) {
            filters.add(RowFilter.regexFilter("^" + chucVu + "$", 2));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
        capNhatSoLuongHienThi();
    }

    private void capNhatSoLuongHienThi() {
        int soDong = bangNhanVien.getRowCount();
        int tong = modelBang.getRowCount();
        lblTongSoNV.setText("Hiển thị " + soDong + " / " + tong + " nhân viên");
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ NGHIỆP VỤ (CRUD)
    // ==========================================

    private void xuLyThemNhanVien() {
        String maTuDong = nhanVienDAO.phatSinhMaNhanVien();
        if (maTuDong == null) {
            JOptionPane.showMessageDialog(this, "Không thể tạo mã nhân viên. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField txtMa = new JTextField(maTuDong);
        txtMa.setEditable(false);
        JTextField txtHoTen = new JTextField();
        JComboBox<String> cbChucVu = new JComboBox<>(new String[]{
                "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Quản lý"
        });
        JTextField txtSdt = new JTextField();
        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        JTextField txtNgaySinh = new JTextField("1995-01-01");
        JTextField txtDiaChi = new JTextField("TP.HCM");
        JTextField txtLuong = new JTextField("Tự động theo chức vụ");
        txtLuong.setEditable(false);

        JPanel form = taoFormDialog(txtMa, txtHoTen, cbChucVu, txtSdt, cbGioiTinh, txtNgaySinh, txtDiaChi, txtLuong);

        int res = JOptionPane.showConfirmDialog(this, form, "Thêm mới nhân viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ và tên nhân viên không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (sdt.isEmpty() || !sdt.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double luong;
            try {
                luong = nhanVienDAO.getLuongTheoChucVu((String) cbChucVu.getSelectedItem());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể lấy lương theo chức vụ: " + ex.getMessage(),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date ngaySinh;
            try {
                ngaySinh = Date.valueOf(txtNgaySinh.getText().trim());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Ngày sinh phải theo định dạng yyyy-MM-dd!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            NhanVien_entity nv = new NhanVien_entity(
                    maTuDong,
                    null,
                    hoTen,
                    (String) cbChucVu.getSelectedItem(),
                    sdt,
                    txtDiaChi.getText().trim(),
                    ngaySinh.toLocalDate(),
                    (String) cbGioiTinh.getSelectedItem(),
                    luong
            );

            boolean ok = nhanVienDAO.createNhanVien(nv);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên " + hoTen + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu nhân viên vào cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLySuaNhanVien() {
        int row = bangNhanVien.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa từ bảng!", "Chưa chọn nhân viên", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bangNhanVien.convertRowIndexToModel(row);
        String maNV = (String) modelBang.getValueAt(modelRow, 0);
        NhanVien_entity nv = null;
        try {
            nv = nhanVienDAO.getNhanVienTheoMa(maNV);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (nv == null) return;

        JTextField txtMa = new JTextField(nv.getMaNhanVien());
        txtMa.setEditable(false);
        JTextField txtHoTen = new JTextField(nv.getHoTen());
        JComboBox<String> cbChucVu = new JComboBox<>(new String[]{
                "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Quản lý"
        });
        cbChucVu.setSelectedItem(nv.getChucVu());
        JTextField txtSdt = new JTextField(nv.getSdt());
        JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cbGioiTinh.setSelectedItem(nv.getGioiTinh());
        JTextField txtNgaySinh = new JTextField(nv.getNgaySinh() != null ? nv.getNgaySinh().toString() : "1995-01-01");
        JTextField txtDiaChi = new JTextField(nv.getDiaChi());
        JTextField txtLuong = new JTextField("Tự động theo chức vụ");
        txtLuong.setEditable(false);

        JPanel form = taoFormDialog(txtMa, txtHoTen, cbChucVu, txtSdt, cbGioiTinh, txtNgaySinh, txtDiaChi, txtLuong);

        int res = JOptionPane.showConfirmDialog(this, form, "Cập nhật thông tin nhân viên: " + nv.getMaNhanVien(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ và tên nhân viên không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double luong;
            try {
                luong = nhanVienDAO.getLuongTheoChucVu((String) cbChucVu.getSelectedItem());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể lấy lương theo chức vụ: " + ex.getMessage(),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }

            nv.setHoTen(hoTen);
            nv.setChucVu((String) cbChucVu.getSelectedItem());
            nv.setSdt(sdt);
            nv.setGioiTinh((String) cbGioiTinh.getSelectedItem());
            try {
                nv.setNgaySinh(LocalDate.parse(txtNgaySinh.getText().trim()));
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngày sinh phải theo định dạng yyyy-MM-dd!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            nv.setDiaChi(txtDiaChi.getText().trim());
            nv.setLuongCoBan(luong);

            if (nhanVienDAO.updateNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật nhân viên trong cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXoaNhanVien() {
        int row = bangNhanVien.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa từ bảng!", "Chưa chọn nhân viên", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bangNhanVien.convertRowIndexToModel(row);
        String maNV = (String) modelBang.getValueAt(modelRow, 0);
        String tenNV = (String) modelBang.getValueAt(modelRow, 1);

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhân viên [" + maNV + " - " + tenNV + "]?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (xacNhan == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.deleteNhanVien(maNV)) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên " + maNV + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                taiDuLieuLenBang();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên " + maNV + " khỏi cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXuatExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("danh_sach_nhan_vien.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()), StandardCharsets.UTF_8)) {
                writer.write('\uFEFF'); // UTF-8 BOM cho Excel
                for (int c = 0; c < modelBang.getColumnCount(); c++) {
                    if (c > 0) writer.write(',');
                    writer.write("\"" + modelBang.getColumnName(c) + "\"");
                }
                writer.write('\n');

                for (int r = 0; r < bangNhanVien.getRowCount(); r++) {
                    int mr = bangNhanVien.convertRowIndexToModel(r);
                    for (int c = 0; c < modelBang.getColumnCount(); c++) {
                        if (c > 0) writer.write(',');
                        Object val = modelBang.getValueAt(mr, c);
                        writer.write("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                    }
                    writer.write('\n');
                }
                JOptionPane.showMessageDialog(this, "Đã xuất danh sách nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel taoFormDialog(JTextField txtMa, JTextField txtHoTen, JComboBox<String> cbChucVu,
                                  JTextField txtSdt, JComboBox<String> cbGioiTinh, JTextField txtNgaySinh,
                                  JTextField txtDiaChi, JTextField txtLuong) {
        JPanel p = new JPanel(new GridLayout(8, 2, 8, 8));
        p.setPreferredSize(new Dimension(420, 280));

        p.add(new JLabel("Mã nhân viên:"));
        p.add(txtMa);
        p.add(new JLabel("Họ và tên (*):"));
        p.add(txtHoTen);
        p.add(new JLabel("Chức vụ:"));
        p.add(cbChucVu);
        p.add(new JLabel("Số điện thoại (*):"));
        p.add(txtSdt);
        p.add(new JLabel("Giới tính:"));
        p.add(cbGioiTinh);
        p.add(new JLabel("Ngày sinh (yyyy-MM-dd):"));
        p.add(txtNgaySinh);
        p.add(new JLabel("Địa chỉ:"));
        p.add(txtDiaChi);
        p.add(new JLabel("Lương cơ bản (VNĐ):"));
        p.add(txtLuong);

        return p;
    }

    private JButton taoNutPhu(String text) {
        JButton b = new JButton(text);
        b.setFont(RangBuocGiaoDien.fontDam(12));
        b.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử Quản lý nhân viên SALALA");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new QuanLyNhanVienGUI());
            frame.setVisible(true);
        });
    }
}
