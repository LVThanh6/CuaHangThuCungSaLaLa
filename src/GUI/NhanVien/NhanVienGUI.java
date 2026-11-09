package GUI.NhanVien;

import GUI.Chung.RangBuocGiaoDien;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import controller.NhanVien.NhanVienController;
import controller.NhanVien.NhanVienController.TrangThai;
import entity.NhanVien.NhanVien_entity;

public class NhanVienGUI extends JPanel {

    private static final String[] CHUC_VU = NhanVienController.CHUC_VU;
    private static final String[] GIOI_TINH = NhanVienController.GIOI_TINH;

    private static final Color NEN_CHON = new Color(240, 250, 247);
    private static final Color NEN_HEADER = new Color(246, 248, 247);
    private static final Color MAU_DO = new Color(220, 38, 38);

    private final List<NhanVien_entity> tatCa = new ArrayList<>();
    private final List<NhanVien_entity> daLoc = new ArrayList<>();
    private final NhanVienController controller = new NhanVienController();
    private Boolean sapXepTang = null;

    private OTimKiem txtTim;
    private JComboBox<String> cbVaiTro, cbTrangThai;
    private JTable bang;
    private BangModel model;
    private JLabel lblHienThi;
    private JButton btnXemChiTiet, btnChinhSua, btnDoiTrangThai, btnXoa;
    private final JLabel[] lblSoLieu = new JLabel[4];

    public NhanVienGUI() {
        khoiTaoGiaoDien();
        taiLai();
    }

    public void datDanhSach(List<NhanVien_entity> ds) {
        tatCa.clear();
        tatCa.addAll(ds);
        lamMoi();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout());
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel phanTren = new JPanel();
        phanTren.setLayout(new BoxLayout(phanTren, BoxLayout.Y_AXIS));
        phanTren.setOpaque(false);
        phanTren.add(taoTieuDe());
        phanTren.add(taoHangSoLieu());
        add(phanTren, BorderLayout.NORTH);

        add(taoTheDanhSach(), BorderLayout.CENTER);
    }

    private JComponent taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel trai = new JPanel();
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        trai.setOpaque(false);

        JLabel lblDuongDan = new JLabel("Nhân viên / Danh sách nhân viên");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblTieuDe = new JLabel("Danh sách nhân viên");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel("Quản lý hồ sơ, chức vụ và trạng thái tài khoản của đội ngũ SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        trai.add(lblDuongDan);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTieuDe);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblMoTa);
        panel.add(trai, BorderLayout.WEST);

        JButton btnThem = new JButton("Thêm nhân viên");
        dinhDangNut(btnThem, true);
        btnThem.addActionListener(e -> moForm(null, false));

        JButton btnLamMoi = new JButton("Làm mới");
        dinhDangNut(btnLamMoi, false);
        btnLamMoi.addActionListener(e -> taiLai());

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(btnLamMoi);
        phai.add(btnThem);
        panel.add(phai, BorderLayout.EAST);
        return panel;
    }

    private void dinhDangNut(JButton btn, boolean chinh) {
        btn.setFont(RangBuocGiaoDien.fontDam(13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color vien = chinh ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_DUONG_VIEN;
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(vien, 1, true), new EmptyBorder(9, 18, 9, 18)));
        btn.setBackground(chinh ? RangBuocGiaoDien.MAU_CHU_DAO : Color.WHITE);
        btn.setForeground(chinh ? Color.WHITE : RangBuocGiaoDien.MAU_CHU_CHINH);
    }

    private JComponent taoHangSoLieu() {
        String[] tieuDe = { "Tổng nhân viên", "Đang làm việc", "Ngừng hoạt động", "Chưa có tài khoản" };
        String[] moTa = { "Toàn bộ đội ngũ", "Tài khoản hoạt động", "Tài khoản bị khóa", "Chưa cấp quyền đăng nhập" };

        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 16, 0));
        for (int i = 0; i < 4; i++) {
            lblSoLieu[i] = new JLabel("0");
            hang.add(taoTheSoLieu(tieuDe[i], moTa[i], lblSoLieu[i]));
        }
        return hang;
    }

    private JPanel taoTheSoLieu(String tieuDe, String moTa, JLabel lblSo) {
        JPanel the = new JPanel(new BorderLayout(14, 0));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblSo.setFont(RangBuocGiaoDien.fontDam(24));
        lblSo.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel hangSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangSo.setOpaque(false);
        hangSo.add(lblSo);
        hangSo.add(lblMoTa);

        JPanel giua = new JPanel();
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.setOpaque(false);
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(lblTieuDe);
        giua.add(hangSo);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JComponent taoTheDanhSach() {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(Color.WHITE);
        the.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));

        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setOpaque(false);
        thanhTren.add(taoThanhBoLoc(), BorderLayout.NORTH);
        thanhTren.add(taoThanhHanhDong(), BorderLayout.SOUTH);
        the.add(thanhTren, BorderLayout.NORTH);
        the.add(taoBang(), BorderLayout.CENTER);
        the.add(taoChanTrang(), BorderLayout.SOUTH);
        return the;
    }

    private JComponent taoThanhBoLoc() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(16, 18, 16, 18));

        txtTim = new OTimKiem("Tìm theo tên, mã nhân viên, số điện thoại...");
        txtTim.setPreferredSize(new Dimension(400, 38));
        txtTim.setMaximumSize(new Dimension(400, 38));
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                apDungLoc();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                apDungLoc();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                apDungLoc();
            }
        });

        String[] dsVaiTro = new String[CHUC_VU.length + 1];
        dsVaiTro[0] = "Chức vụ: Tất cả";
        for (int i = 0; i < CHUC_VU.length; i++)
            dsVaiTro[i + 1] = "Chức vụ: " + CHUC_VU[i];
        cbVaiTro = new JComboBox<>(dsVaiTro);

        String[] dsTrangThai = new String[TrangThai.values().length + 1];
        dsTrangThai[0] = "Trạng thái: Tất cả";
        for (int i = 0; i < TrangThai.values().length; i++)
            dsTrangThai[i + 1] = "Trạng thái: " + TrangThai.values()[i].nhan;
        cbTrangThai = new JComboBox<>(dsTrangThai);

        for (JComboBox<String> cb : java.util.List.of(cbVaiTro, cbTrangThai)) {
            cb.setFont(RangBuocGiaoDien.fontThuong(13));
            cb.setMaximumSize(new Dimension(230, 38));
            cb.setPreferredSize(new Dimension(220, 38));
            cb.addActionListener(e -> apDungLoc());
        }

        JButton btnXuat = new JButton("Xuất danh sách");
        dinhDangNut(btnXuat, false);
        btnXuat.addActionListener(e -> xuatCsv());

        bar.add(txtTim);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbVaiTro);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbTrangThai);
        bar.add(Box.createHorizontalGlue());
        bar.add(btnXuat);
        return bar;
    }

    private JComponent taoThanhHanhDong() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 18, 12, 18));

        btnXemChiTiet = taoNutHanhDong("Xem chi tiết");
        btnChinhSua = taoNutHanhDong("Chỉnh sửa");
        btnDoiTrangThai = taoNutHanhDong("Đổi trạng thái");
        btnXoa = taoNutHanhDong("Xóa");
        btnXoa.setForeground(MAU_DO);

        btnXemChiTiet.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                moForm(nv, true);
        });
        btnChinhSua.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                moForm(nv, false);
        });
        btnDoiTrangThai.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                hienPopupDoiTrangThai(nv, btnDoiTrangThai);
        });
        btnXoa.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                xoaNhanVien(nv);
        });

        bar.add(btnXemChiTiet);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnChinhSua);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnDoiTrangThai);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnXoa);
        return bar;
    }

    private JButton taoNutHanhDong(String text) {
        JButton button = new JButton(text);
        dinhDangNut(button, false);
        button.setEnabled(false);
        return button;
    }

    private JComponent taoBang() {
        model = new BangModel();
        bang = new JTable(model);
        bang.setRowHeight(48);
        bang.setShowVerticalLines(false);
        bang.setShowHorizontalLines(true);
        bang.setGridColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
        bang.setIntercellSpacing(new Dimension(0, 1));
        bang.setSelectionBackground(NEN_CHON);
        bang.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        bang.setFont(RangBuocGiaoDien.fontThuong(13));
        bang.setFillsViewportHeight(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] rong = { 100, 220, 180, 260, 120, 160 };
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // Dùng luôn ChuRenderer cho tất cả các cột
        bang.getColumnModel().getColumn(0).setCellRenderer(new ChuRenderer(0)); // Mã NV
        bang.getColumnModel().getColumn(1).setCellRenderer(new ChuRenderer(1)); // Họ Tên
        bang.getColumnModel().getColumn(2).setCellRenderer(new ChuRenderer(2)); // Chức vụ
        bang.getColumnModel().getColumn(3).setCellRenderer(new LienHeRenderer()); // Liên hệ
        bang.getColumnModel().getColumn(4).setCellRenderer(new ChuRenderer(4)); // Ngày sinh
        bang.getColumnModel().getColumn(5).setCellRenderer(new ChuRenderer(5)); // Trạng thái bằng TEXT

        JTableHeader header = bang.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new HeaderRenderer());
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = header.columnAtPoint(e.getPoint());
                if (c == 1) { // Sắp xếp theo tên
                    sapXepTang = (sapXepTang == null) ? Boolean.TRUE : (sapXepTang ? Boolean.FALSE : null);
                    apDungLoc();
                }
            }
        });

        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = bang.rowAtPoint(e.getPoint());
                if (r < 0)
                    return;
                if (e.getClickCount() == 2)
                    moForm(model.tai(r), true);
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                capNhatNutHanhDong();
        });
        capNhatNutHanhDong();

        JScrollPane sp = new JScrollPane(bang);
        sp.setBorder(new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JComponent taoChanTrang() {
        JPanel chan = new JPanel(new BorderLayout());
        chan.setBackground(Color.WHITE);
        chan.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(12, 18, 12, 18)));

        lblHienThi = new JLabel(" ");
        lblHienThi.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chan.add(lblHienThi, BorderLayout.WEST);

        return chan;
    }

    private void lamMoi() {
        capNhatSoLieu();
        apDungLoc();
    }

    private void capNhatSoLieu() {
        NhanVienController.ThongKe thongKe = controller.thongKe(tatCa);
        lblSoLieu[0].setText(String.valueOf(thongKe.tongSo));
        lblSoLieu[1].setText(String.valueOf(thongKe.dangLam));
        lblSoLieu[2].setText(String.valueOf(thongKe.ngung));
        lblSoLieu[3].setText(String.valueOf(thongKe.chuaTaiKhoan));
    }

    private void apDungLoc() {
        int vt = cbVaiTro.getSelectedIndex();
        int tt = cbTrangThai.getSelectedIndex();
        String role = vt == 0 ? null : CHUC_VU[vt - 1];
        String status = tt == 0 ? null : TrangThai.values()[tt - 1].name();
        daLoc.clear();
        daLoc.addAll(controller.locDanhSach(tatCa, txtTim.getText(), role, status,
                sapXepTang != null, Boolean.TRUE.equals(sapXepTang)));
        capNhatBang();
    }

    private void capNhatBang() {
        int tong = daLoc.size();
        model.datHang(daLoc);
        lblHienThi.setText(tong == 0
                ? "Không có nhân viên phù hợp"
                : "Hiển thị " + tong + " / " + tatCa.size() + " nhân viên");
        bang.getTableHeader().repaint();
    }

    private void baoLoi(String tieuDe, SQLException ex) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
    }

    private NhanVien_entity nhanVienDangChon() {
        int[] hangChon = bang.getSelectedRows();
        return hangChon.length == 1 ? model.tai(hangChon[0]) : null;
    }

    private void capNhatNutHanhDong() {
        boolean chonMot = bang != null && bang.getSelectedRowCount() == 1;
        btnXemChiTiet.setEnabled(chonMot);
        btnChinhSua.setEnabled(chonMot);
        btnDoiTrangThai.setEnabled(chonMot);
        btnXoa.setEnabled(chonMot);
    }

    private void hienPopupDoiTrangThai(NhanVien_entity nv, Component goc) {
        JPopupMenu popup = new JPopupMenu();
        if (nv.getMaTK() == null) {
            JMenuItem muc = new JMenuItem(TrangThai.CHUA_TK.nhan);
            muc.setEnabled(false);
            popup.add(muc);
        } else {
            popup.add(taoMucTrangThai(nv, TrangThai.DANG_LAM));
            popup.add(taoMucTrangThai(nv, TrangThai.NGUNG));
        }
        popup.show(goc, 0, goc.getHeight());
    }

    private JMenuItem taoMucTrangThai(NhanVien_entity nv, TrangThai trangThai) {
        JMenuItem muc = new JMenuItem(trangThai.nhan);
        muc.setEnabled(TrangThai.tuEntity(nv) != trangThai);
        muc.addActionListener(e -> doiTrangThai(nv, trangThai));
        return muc;
    }

    private void doiTrangThai(NhanVien_entity nv, TrangThai trangThai) {
        if (nv.getMaTK() == null)
            return;
        try {
            controller.doiTrangThai(nv.getMaNhanVien(), trangThai == TrangThai.DANG_LAM);
            taiLai();
        } catch (SQLException ex) {
            baoLoi("Không thể đổi trạng thái", ex);
        }
    }

    private void xoaNhanVien(NhanVien_entity nv) {
        int kq = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + nv.getHoTen() + " (" + nv.getMaNhanVien()
                        + ")?\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (kq != JOptionPane.YES_OPTION)
            return;
        try {
            NhanVienController.KetQuaXoa result = controller.xoa(nv.getMaNhanVien());
            if (result.coDuLieuLienQuan) {
                JOptionPane.showMessageDialog(this,
                        "Nhân viên này đã có dữ liệu liên quan (lịch làm việc, lịch hẹn, hóa đơn, phiếu kho...)\n"
                                + "nên không thể xóa. Hãy chuyển sang \"Ngừng hoạt động\" thay vì xóa.",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!result.thanhCong) {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            taiLai();
        } catch (SQLException ex) {
            baoLoi("Không thể xóa nhân viên", ex);
        }
    }

    private void moForm(NhanVien_entity goc, boolean chiXem) {
        String maHienThi;
        if (goc == null) {
            try {
                maHienThi = controller.taoMaMoi();
            } catch (SQLException ex) {
                baoLoi("Không thể tạo mã mới", ex);
                return;
            }
        } else {
            maHienThi = goc.getMaNhanVien();
        }

        Window cha = SwingUtilities.getWindowAncestor(this);
        String tieuDe = goc == null ? "Thêm nhân viên mới"
                : (chiXem ? "Chi tiết hồ sơ nhân viên" : "Cập nhật hồ sơ nhân viên");
        JDialog dlg = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);

        final boolean coTK = goc != null && goc.getMaTK() != null;

        JTextField txtMa = new JTextField(maHienThi);
        JTextField txtTen = new JTextField(goc == null ? "" : nz(goc.getHoTen()));
        JTextField txtSdt = new JTextField(goc == null ? "" : nz(goc.getSdt()));
        JTextField txtDiaChi = new JTextField(goc == null ? "" : nz(goc.getDiaChi()));
        JTextField txtNgay = new JTextField(
                goc == null || goc.getNgaySinh() == null ? "" : NhanVienController.dinhDangNgay(goc.getNgaySinh()));
        JComboBox<String> cbGT = new JComboBox<>(GIOI_TINH);
        JComboBox<String> cbChucVu = new JComboBox<>(CHUC_VU);
        JTextField txtLuong = new JTextField(goc == null ? "" : String.format("%.0f", goc.getLuongCoBan()));
        JTextField txtTongLuong = new JTextField();

        JComboBox<String> cbTT = coTK
                ? new JComboBox<>(new String[] { TrangThai.DANG_LAM.nhan, TrangThai.NGUNG.nhan })
                : new JComboBox<>(new String[] { TrangThai.CHUA_TK.nhan });

        if (goc != null) {
            if (goc.getChucVu() != null)
                cbChucVu.setSelectedItem(goc.getChucVu());
            if (goc.getGioiTinh() != null)
                cbGT.setSelectedItem(goc.getGioiTinh());
            if (coTK)
                cbTT.setSelectedIndex(Boolean.TRUE.equals(goc.getTrangThaiTK()) ? 0 : 1);
        }

        try {
            double totalSalary = controller.tinhTongLuongThang(goc, LocalDate.now());
            txtTongLuong
                    .setText(Double.isNaN(totalSalary) ? "Chưa áp dụng" : NhanVienController.dinhDangTien(totalSalary));
        } catch (SQLException ex) {
            txtTongLuong.setText("Không thể tính");
        }
        txtMa.setEditable(false);
        txtTongLuong.setEditable(false);
        cbTT.setEnabled(coTK && !chiXem);

        Runnable apDungLuong = () -> {
            if (goc == null)
                txtLuong.setText(String.valueOf(NhanVienController.luongMacDinh((String) cbChucVu.getSelectedItem())));
        };
        cbChucVu.addActionListener(e -> apDungLuong.run());
        apDungLuong.run();

        JComponent[] fields = { txtMa, txtTen, cbChucVu, txtSdt, txtDiaChi, txtNgay, cbGT, txtLuong, txtTongLuong,
                cbTT };
        for (JComponent f : fields) {
            f.setFont(RangBuocGiaoDien.fontDam(13));
            if (f instanceof JTextField) {
                JTextField tf = (JTextField) f;
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN), new EmptyBorder(8, 12, 8, 12)));
                if (chiXem || !tf.isEditable()) {
                    tf.setEditable(false);
                    tf.setBackground(new Color(248, 250, 252));
                    tf.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                }
            }
        }

        if (chiXem) {
            cbChucVu.setEnabled(false);
            cbGT.setEnabled(false);
        }

        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 24, 16));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(24, 32, 16, 32));

        String[] nhanLbl = {
                "Mã nhân viên", "Họ và tên (*)",
                "Số điện thoại (*)", "Ngày sinh (dd/MM/yyyy)",
                "Giới tính", "Địa chỉ",
                "Chức vụ", "Lương cơ bản (VNĐ)",
                "Trạng thái tài khoản", "Tổng lương tháng này"
        };
        JComponent[] nhanCmp = { txtMa, txtTen, txtSdt, txtNgay, cbGT, txtDiaChi, cbChucVu, txtLuong, cbTT,
                txtTongLuong };

        for (int i = 0; i < nhanLbl.length; i++) {
            JPanel box = new JPanel(new BorderLayout(0, 6));
            box.setBackground(Color.WHITE);
            JLabel lbl = new JLabel(nhanLbl[i]);
            lbl.setFont(RangBuocGiaoDien.fontThuong(12));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            box.add(lbl, BorderLayout.NORTH);
            box.add(nhanCmp[i], BorderLayout.CENTER);
            pnlForm.add(box);
        }

        JLabel lblLoi = new JLabel(" ");
        lblLoi.setFont(RangBuocGiaoDien.fontThuong(12));
        lblLoi.setForeground(MAU_DO);
        lblLoi.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnHuy = new JButton(chiXem ? "Đóng" : "Hủy thao tác");
        dinhDangNut(btnHuy, false);
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = new JButton("Lưu thay đổi");
        dinhDangNut(btnLuu, true);
        btnLuu.addActionListener(e -> {
            try {
                NhanVienController.KetQuaLuu result = controller.validateAndSave(txtMa.getText(), goc,
                        txtTen.getText(), txtSdt.getText(), txtDiaChi.getText(), txtNgay.getText(),
                        (String) cbGT.getSelectedItem(), (String) cbChucVu.getSelectedItem(),
                        txtLuong.getText(), cbTT.getSelectedIndex() == 0);
                if (!result.isThanhCong()) {
                    lblLoi.setText(result.getThongBaoLoi());
                    return;
                }
            } catch (SQLException ex) {
                lblLoi.setText("Lỗi CSDL: " + ex.getMessage());
                return;
            }
            dlg.dispose();
            taiLai();
        });

        JPanel pnlLoi = new JPanel(new BorderLayout());
        pnlLoi.setBackground(Color.WHITE);
        pnlLoi.add(lblLoi, BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        nut.setBackground(Color.WHITE);
        nut.setBorder(new EmptyBorder(16, 32, 24, 32));
        nut.add(btnHuy);
        if (!chiXem)
            nut.add(btnLuu);

        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.setBackground(Color.WHITE);
        pnlDuoi.add(pnlLoi, BorderLayout.NORTH);
        pnlDuoi.add(nut, BorderLayout.SOUTH);

        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.getContentPane().add(pnlForm, BorderLayout.CENTER);
        dlg.getContentPane().add(pnlDuoi, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setSize(750, dlg.getHeight() + 20);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }

    private void xuatCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh_sach_nhan_vien.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        try {
            controller.ghiCsv(fc.getSelectedFile(), daLoc);
            JOptionPane.showMessageDialog(this, "Đã xuất " + daLoc.size() + " nhân viên.",
                    "Xuất danh sách", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taiLai() {
        try {
            List<NhanVien_entity> ds = controller.taiDanhSach();
            tatCa.clear();
            tatCa.addAll(ds);
        } catch (SQLException ex) {
            baoLoi("Không thể tải danh sách nhân viên", ex);
        }
        lamMoi();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm thử Quản lý nhân viên SALALA");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new NhanVienGUI());
            frame.setVisible(true);
        });
    }

    private class BangModel extends AbstractTableModel {
        private final String[] cot = { "Mã nhân viên", "Họ và tên", "Chức vụ", "Liên hệ", "Ngày sinh", "Trạng thái" };
        private List<NhanVien_entity> hang = new ArrayList<>();

        void datHang(List<NhanVien_entity> h) {
            hang = new ArrayList<>(h);
            fireTableDataChanged();
        }

        NhanVien_entity tai(int r) {
            return hang.get(r);
        }

        @Override
        public int getRowCount() {
            return hang.size();
        }

        @Override
        public int getColumnCount() {
            return cot.length;
        }

        @Override
        public String getColumnName(int c) {
            return cot[c];
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Object getValueAt(int r, int c) {
            return hang.get(r);
        }
    }

    private static Color nen(boolean chon) {
        return chon ? NEN_CHON : Color.WHITE;
    }

    private static String layChu(NhanVien_entity nv, int cot) {
        switch (cot) {
            case 0:
                return nv.getMaNhanVien();
            case 1:
                return nz(nv.getHoTen());
            case 2:
                return nz(nv.getChucVu());
            case 4:
                return NhanVienController.dinhDangNgay(nv.getNgaySinh());
            case 5:
                return TrangThai.tuEntity(nv).nhan;
            default:
                return "";
        }
    }

    private static class ChuRenderer extends JLabel implements TableCellRenderer {
        private final int cot;

        ChuRenderer(int cot) {
            this.cot = cot;
            setOpaque(true);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setFont(RangBuocGiaoDien.fontThuong(13));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText(layChu((NhanVien_entity) v, cot));
            setBackground(nen(sel));
            setForeground(cot == 0 ? RangBuocGiaoDien.MAU_CHU_PHU : RangBuocGiaoDien.MAU_CHU_CHINH);
            if (cot == 1)
                setFont(RangBuocGiaoDien.fontDam(13));
            return this;
        }
    }

    private static class LienHeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblSdt = new JLabel();
        private final JLabel lblDiaChi = new JLabel();

        LienHeRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            lblSdt.setFont(RangBuocGiaoDien.fontThuong(13));
            lblSdt.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lblDiaChi.setFont(RangBuocGiaoDien.fontThuong(11));
            lblDiaChi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            add(Box.createVerticalGlue());
            add(lblSdt);
            add(lblDiaChi);
            add(Box.createVerticalGlue());
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            NhanVien_entity nv = (NhanVien_entity) v;
            lblSdt.setText(NhanVienController.dinhDangSdt(nv.getSdt()));
            lblDiaChi.setText(nz(nv.getDiaChi()));
            lblDiaChi.setToolTipText(nz(nv.getDiaChi()));
            setBackground(nen(sel));
            return this;
        }
    }

    private class HeaderRenderer implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        HeaderRenderer() {
            lbl.setOpaque(true);
            lbl.setBackground(NEN_HEADER);
            lbl.setFont(RangBuocGiaoDien.fontDam(12));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v == null ? "" : v.toString();
            lbl.setText(text);
            return lbl;
        }
    }

    private static class OTimKiem extends JTextField {
        OTimKiem(String goiY) {
            setFont(RangBuocGiaoDien.fontThuong(13));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(6, 12, 6, 12)));
        }
    }
}