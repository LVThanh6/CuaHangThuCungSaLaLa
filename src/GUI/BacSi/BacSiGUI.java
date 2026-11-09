package GUI.BacSi;

import GUI.Chung.RangBuocGiaoDien;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import controller.BacSi.BacSiController;
import controller.BacSi.BacSiController.TrangThai;
import entity.BacSi.ChuyenNganh_entity;
import entity.BacSi.BacSi_entity;

/**
 * Class BacSiGUI: Màn hình "Danh sách bác sĩ" kết nối CSDL SALALA.
 */
public class BacSiGUI extends JPanel {

    private static final String THEM_CHUYEN_NGANH = "+ Thêm chuyên ngành mới...";

    private static final int COT_HO_TEN = BacSiController.COT_HO_TEN;
    private static final int COT_SO_CA = BacSiController.COT_SO_CA;

    private static final Color NEN_CHON = new Color(240, 250, 247);
    private static final Color NEN_HEADER = new Color(246, 248, 247);
    private static final Color NEN_ICON = new Color(226, 244, 240);
    private static final Color MAU_DO = new Color(220, 38, 38);

    private final List<BacSi_entity> tatCa = new ArrayList<>();
    private final List<BacSi_entity> daLoc = new ArrayList<>();
    private final BacSiController controller = new BacSiController();
    private int cotSapXep = -1;
    private boolean sapXepTang = true;
    private int tongNhanVien = 0;

    private OTimKiem txtTim;
    private JComboBox<String> cbChuyenNganh, cbTrangThai;
    private JTable bang;
    private BangModel model;
    private JButton btnXemChiTiet;
    private JButton btnLichLamViec;
    private JButton btnChinhSua;
    private JButton btnDoiTrangThai;
    private JButton btnXoa;
    private JLabel lblHienThi;
    private final JLabel[] lblSo = new JLabel[4];
    private final JLabel[] lblMo = new JLabel[4];

    public BacSiGUI() {
        khoiTaoGiaoDien();
        taiLai();
    }

    public void datDanhSach(List<BacSi_entity> ds) {
        tatCa.clear();
        tatCa.addAll(ds);
        lamMoi();
    }

    public void datTongNhanVien(int n) {
        tongNhanVien = n;
        capNhatSoLieu();
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

        JLabel lblDuongDan = new JLabel("Nhân viên / Danh sách bác sĩ");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblTieuDe = new JLabel("Danh sách bác sĩ");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        trai.add(lblDuongDan);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTieuDe);

        panel.add(trai, BorderLayout.WEST);

        JButton btnThem = new JButton("+   Thêm bác sĩ");
        dinhDangNut(btnThem, true);
        btnThem.addActionListener(e -> moForm(null, false));

        JPanel phai = new JPanel(new GridBagLayout());
        phai.setOpaque(false);
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
        String[] tieuDe = { "Tổng số bác sĩ", "Đang làm việc", "Tạm nghỉ", "Số ca khám tháng này" };

        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 16, 0));
        for (int i = 0; i < 4; i++) {
            lblSo[i] = new JLabel("0");
            lblMo[i] = new JLabel(" ");
            hang.add(taoTheSoLieu(i, tieuDe[i], lblSo[i], lblMo[i]));
        }
        lblMo[1].setText("Bác sĩ hoạt động");
        lblMo[2].setText("Nghỉ phép / tạm nghỉ");
        return hang;
    }

    private JPanel taoTheSoLieu(int loaiIcon, String tieuDe, JLabel so, JLabel moTa) {
        JPanel the = new JPanel(new BorderLayout(14, 0));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        the.add(new HuyHieuIcon(loaiIcon), BorderLayout.WEST);

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        so.setFont(RangBuocGiaoDien.fontDam(24));
        so.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        moTa.setFont(RangBuocGiaoDien.fontThuong(12));
        moTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel hangSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangSo.setOpaque(false);
        hangSo.add(so);
        hangSo.add(moTa);

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

        txtTim = new OTimKiem("Tìm theo tên, mã bác sĩ, số điện thoại...");
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

        cbChuyenNganh = new JComboBox<>();
        taiDanhSachChuyenNganh(cbChuyenNganh, true);

        String[] dsTT = new String[TrangThai.values().length + 1];
        dsTT[0] = "Trạng thái: Tất cả";
        for (int i = 0; i < TrangThai.values().length; i++)
            dsTT[i + 1] = "Trạng thái: " + TrangThai.values()[i].nhan;
        cbTrangThai = new JComboBox<>(dsTT);

        for (JComboBox<String> cb : java.util.List.of(cbChuyenNganh, cbTrangThai)) {
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
        bar.add(cbChuyenNganh);
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
        btnLichLamViec = taoNutHanhDong("Lịch làm việc");
        btnChinhSua = taoNutHanhDong("Chỉnh sửa");
        btnDoiTrangThai = taoNutHanhDong("Đổi trạng thái");
        btnXoa = taoNutHanhDong("Xóa");
        btnXoa.setForeground(MAU_DO);

        btnXemChiTiet.addActionListener(e -> {
            BacSi_entity bs = bacSiDangChon();
            if (bs != null)
                moForm(bs, true);
        });
        btnLichLamViec.addActionListener(e -> {
            BacSi_entity bs = bacSiDangChon();
            if (bs != null)
                moLich(bs);
        });
        btnChinhSua.addActionListener(e -> {
            BacSi_entity bs = bacSiDangChon();
            if (bs != null)
                moForm(bs, false);
        });
        btnDoiTrangThai.addActionListener(e -> {
            BacSi_entity bs = bacSiDangChon();
            if (bs != null)
                hienPopupDoiTrangThai(bs, btnDoiTrangThai);
        });
        btnXoa.addActionListener(e -> {
            BacSi_entity bs = bacSiDangChon();
            if (bs != null)
                xoaBacSi(bs);
        });

        bar.add(btnXemChiTiet);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnLichLamViec);
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

    private BacSi_entity bacSiDangChon() {
        int[] hangChon = bang.getSelectedRows();
        if (hangChon.length != 1)
            return null;
        int hang = bang.convertRowIndexToModel(hangChon[0]);
        return model.tai(hang);
    }

    private void capNhatNutHanhDong() {
        boolean chonMot = bang != null && bang.getSelectedRowCount() == 1;
        btnXemChiTiet.setEnabled(chonMot);
        btnLichLamViec.setEnabled(chonMot);
        btnChinhSua.setEnabled(chonMot);
        btnDoiTrangThai.setEnabled(chonMot);
        btnXoa.setEnabled(chonMot);
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

        int[] rong = { 90, 170, 80, 130, 160, 200, 100, 150 };
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        for (int c = 0; c <= 6; c++) {
            bang.getColumnModel().getColumn(c).setCellRenderer(new ChuRenderer(c));
        }
        bang.getColumnModel().getColumn(7).setCellRenderer(new TrangThaiRenderer());

        JTableHeader header = bang.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new HeaderRenderer());
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = header.columnAtPoint(e.getPoint());
                if (c != COT_HO_TEN && c != COT_SO_CA)
                    return;
                if (cotSapXep != c) {
                    cotSapXep = c;
                    sapXepTang = true;
                } else if (sapXepTang) {
                    sapXepTang = false;
                } else {
                    cotSapXep = -1;
                }
                apDungLoc();
            }
        });

        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = bang.rowAtPoint(e.getPoint());
                if (r < 0)
                    return;
                BacSi_entity bs = model.tai(r);

                if (SwingUtilities.isRightMouseButton(e)) {
                    bang.setRowSelectionInterval(r, r);
                    hienMenu(bs, bang, e.getX(), e.getY());
                } else if (e.getClickCount() == 2) {
                    moForm(bs, false);
                }
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
        BacSiController.ThongKe thongKe = controller.thongKe(tatCa);
        lblSo[0].setText(String.valueOf(thongKe.tongSo));
        lblSo[1].setText(String.valueOf(thongKe.dangLam));
        lblSo[2].setText(String.valueOf(thongKe.tamNghi));
        lblSo[3].setText(String.valueOf(thongKe.soCa));
        lblMo[0].setText("Thuộc " + tongNhanVien + " nhân viên");
        lblMo[3].setText("Tháng " + BacSiController.nhanThangHienTai());
    }

    private void apDungLoc() {
        int cn = cbChuyenNganh.getSelectedIndex();
        int tt = cbTrangThai.getSelectedIndex();
        String tenCnChon = cn > 0 ? (String) cbChuyenNganh.getSelectedItem() : null;
        String trangThai = tt == 0 ? null : TrangThai.values()[tt - 1].name();
        daLoc.clear();
        daLoc.addAll(controller.locDanhSach(tatCa, txtTim.getText(), tenCnChon, trangThai,
                cotSapXep, sapXepTang));
        capNhatBang();
    }

    private void capNhatBang() {
        int tong = daLoc.size();
        model.datHang(daLoc);
        lblHienThi.setText(tong == 0
                ? "Không có bác sĩ phù hợp"
                : "Hiển thị tất cả " + tong + " bác sĩ");
        bang.getTableHeader().repaint();
    }

    private void baoLoi(String tieuDe, SQLException ex) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
    }

    private void hienMenu(BacSi_entity bs, Component goc, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem xem = new JMenuItem("Xem chi tiết");
        xem.addActionListener(e -> moForm(bs, true));
        JMenuItem lich = new JMenuItem("Lịch làm việc");
        lich.addActionListener(e -> moLich(bs));
        JMenuItem sua = new JMenuItem("Chỉnh sửa");
        sua.addActionListener(e -> moForm(bs, false));

        JMenu doiTT = taoMenuDoiTrangThai(bs);

        JMenuItem xoa = new JMenuItem("Xóa bác sĩ");
        xoa.setForeground(MAU_DO);
        xoa.addActionListener(e -> xoaBacSi(bs));

        menu.add(xem);
        menu.add(lich);
        menu.add(sua);
        menu.add(doiTT);
        menu.addSeparator();
        menu.add(xoa);
        menu.show(goc, x, y);
    }

    private JMenu taoMenuDoiTrangThai(BacSi_entity bs) {
        JMenu doiTT = new JMenu("Đổi trạng thái");
        doiTT.setEnabled(bs.getMaTK() != null);
        for (TrangThai t : new TrangThai[] { TrangThai.DANG_LAM, TrangThai.TAM_NGHI })
            doiTT.add(taoMucTrangThai(bs, t));
        return doiTT;
    }

    private JMenuItem taoMucTrangThai(BacSi_entity bs, TrangThai t) {
        JMenuItem muc = new JMenuItem(t.nhan);
        muc.setEnabled(bs.getMaTK() != null && TrangThai.tuEntity(bs) != t);
        muc.addActionListener(e -> doiTrangThai(bs, t));
        return muc;
    }

    private void hienPopupDoiTrangThai(BacSi_entity bs, Component goc) {
        JPopupMenu popup = new JPopupMenu();
        if (bs.getMaTK() == null) {
            JMenuItem muc = new JMenuItem("Chưa có tài khoản");
            muc.setEnabled(false);
            popup.add(muc);
        } else {
            popup.add(taoMucTrangThai(bs, TrangThai.DANG_LAM));
            popup.add(taoMucTrangThai(bs, TrangThai.TAM_NGHI));
        }
        popup.show(goc, 0, goc.getHeight());
    }

    private void doiTrangThai(BacSi_entity bs, TrangThai t) {
        try {
            controller.doiTrangThai(bs.getMaBacSi(), t == TrangThai.DANG_LAM);
            taiLai();
        } catch (SQLException ex) {
            baoLoi("Không thể đổi trạng thái", ex);
        }
    }

    private void xoaBacSi(BacSi_entity bs) {
        int kq = JOptionPane.showConfirmDialog(this,
                "Xóa bác sĩ " + bs.getHoTen() + " (" + bs.getMaBacSi() + ")?\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (kq != JOptionPane.YES_OPTION)
            return;
        try {
            BacSiController.KetQuaXoa result = controller.xoa(bs.getMaBacSi());
            if (result.coDuLieuLienQuan) {
                JOptionPane.showMessageDialog(this,
                        "Bác sĩ này đã có hồ sơ khám hoặc đơn thuốc liên quan\n"
                                + "nên không thể xóa. Hãy chuyển sang \"Tạm nghỉ\" thay vì xóa.",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!result.thanhCong) {
                JOptionPane.showMessageDialog(this, "Không thể xóa bác sĩ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            taiLai();
        } catch (SQLException ex) {
            baoLoi("Không thể xóa bác sĩ", ex);
        }
    }

    private void moForm(BacSi_entity goc, boolean chiXem) {
        String maHienThi;
        if (goc == null) {
            try { maHienThi = controller.taoMaMoi(); } catch (SQLException ex) { baoLoi("Không thể tạo mã mới", ex); return; }
        } else {
            maHienThi = goc.getMaBacSi();
        }

        Window cha = SwingUtilities.getWindowAncestor(this);
        String tieuDe = goc == null ? "Thêm bác sĩ mới" : (chiXem ? "Chi tiết hồ sơ bác sĩ" : "Cập nhật hồ sơ bác sĩ");
        JDialog dlg = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);

        final boolean coTK = goc != null && goc.getMaTK() != null;

        // Khởi tạo các Component
        JTextField txtMa = new JTextField(maHienThi);
        JTextField txtTen = new JTextField(goc == null ? "" : nz(goc.getHoTen()));
        JTextField txtSdt = new JTextField(goc == null ? "" : nz(goc.getSdt()));
        JTextField txtEmail = new JTextField(goc == null ? "" : nz(goc.getEmail()));
        JTextField txtNgay = new JTextField(goc == null || goc.getNgaySinh() == null ? "" : BacSiController.dinhDangNgay(goc.getNgaySinh()));
        JTextField txtLuongThang = new JTextField(goc == null ? "" : String.format("%.0f", goc.getLuongThang()));
        JTextField txtSoCa = new JTextField(String.valueOf(goc == null ? 0 : goc.getSoCaKham()));
        
        JComboBox<String> cbGT = new JComboBox<>(BacSiController.GIOI_TINH);
        JComboBox<String> cbCN = new JComboBox<>();
        taiDanhSachChuyenNganh(cbCN, false);
        
        JComboBox<String> cbTT = coTK
                ? new JComboBox<>(new String[] { TrangThai.DANG_LAM.nhan, TrangThai.TAM_NGHI.nhan })
                : new JComboBox<>(new String[] { TrangThai.CHUA_TK.nhan });

        // Nạp dữ liệu
        if (goc != null) {
            if (goc.getGioiTinh() != null) cbGT.setSelectedItem(goc.getGioiTinh());
            if (goc.getChuyenNganh() != null) cbCN.setSelectedItem(BacSiController.tenChuyenNganh(goc));
            if (coTK) cbTT.setSelectedIndex(Boolean.TRUE.equals(goc.getTrangThaiTK()) ? 0 : 1);
        }

        // Khóa các trường không cho sửa thủ công
        txtMa.setEditable(false);
        txtSoCa.setEditable(false);
        cbTT.setEnabled(coTK && !chiXem);

        // Thêm chuyên ngành mới
        cbCN.addActionListener(e -> {
            if (!chiXem && THEM_CHUYEN_NGANH.equals(cbCN.getSelectedItem())) {
                String tenMoi = JOptionPane.showInputDialog(dlg, "Nhập tên chuyên ngành mới:", "Thêm chuyên ngành", JOptionPane.QUESTION_MESSAGE);
                if (tenMoi != null && !tenMoi.isBlank()) {
                    try {
                        controller.themChuyenNganh(tenMoi);
                        taiDanhSachChuyenNganh(cbCN, false);
                        cbCN.setSelectedItem(tenMoi.trim());
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        cbCN.setSelectedIndex(0);
                    }
                } else cbCN.setSelectedIndex(0);
            }
        });

        // Định dạng style cho Form
        JComponent[] fields = {txtMa, txtTen, cbGT, txtSdt, cbCN, txtEmail, txtNgay, txtLuongThang, txtSoCa, cbTT};
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
            cbGT.setEnabled(false); cbCN.setEnabled(false);
        }

        // Bố cục Form (Grid 2 Cột)
        JPanel pnlForm = new JPanel(new GridLayout(0, 2, 24, 16));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(24, 32, 16, 32));

        String[] nhanLbl = { 
            "Mã bác sĩ", "Họ và tên (*)", 
            "Số điện thoại (*)", "Email", 
            "Giới tính", "Ngày sinh (dd/MM/yyyy)", 
            "Chuyên ngành", "Lương cơ bản (VNĐ)", 
            "Trạng thái hoạt động", "Số ca khám tháng này"
        };
        JComponent[] nhanCmp = {txtMa, txtTen, txtSdt, txtEmail, cbGT, txtNgay, cbCN, txtLuongThang, cbTT, txtSoCa};

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
                BacSiController.KetQuaLuu result = controller.validateAndSave(txtMa.getText(), goc,
                        txtTen.getText(), txtSdt.getText(), txtEmail.getText(), txtNgay.getText(),
                        (String) cbGT.getSelectedItem(), (String) cbCN.getSelectedItem(),
                        txtLuongThang.getText(), cbTT.getSelectedIndex() == 0);
                if (!result.isThanhCong()) { lblLoi.setText(result.getThongBaoLoi()); return; }
            } catch (SQLException ex) { lblLoi.setText("Lỗi CSDL: " + ex.getMessage()); return; }
            
            dlg.dispose(); taiLai();
        });

        JPanel pnlLoi = new JPanel(new BorderLayout());
        pnlLoi.setBackground(Color.WHITE);
        pnlLoi.add(lblLoi, BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        nut.setBackground(Color.WHITE);
        nut.setBorder(new EmptyBorder(16, 32, 24, 32));
        nut.add(btnHuy);
        if (!chiXem) nut.add(btnLuu);

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
        dlg.setLocationRelativeTo(null); // HIỂN THỊ GIỮA MÀN HÌNH
        dlg.setVisible(true);
    }

    private void moLich(BacSi_entity bs) {
        String[] cot = { "Ngày", "Bắt đầu", "Kết thúc", "Trạng thái" };
        DefaultTableModel m = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        BacSiController.LichTuan lich;
        try {
            lich = controller.taiLichTuan(bs.getMaBacSi());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được lịch làm việc của bác sĩ: " + ex.getMessage(),
                    "Lỗi tải lịch", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (String[] dong : lich.dong)
            m.addRow(dong);

        JTable t = new JTable(m);
        t.setFont(RangBuocGiaoDien.fontThuong(13));
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setGridColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
        t.getTableHeader().setFont(RangBuocGiaoDien.fontDam(12));
        t.getTableHeader().setReorderingAllowed(false);

        JLabel lblTieuDe = new JLabel(bs.getHoTen() + " · " + BacSiController.tenChuyenNganh(bs));
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(14));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        JLabel lblGhiChu = new JLabel("Lịch đã xếp từ " + lich.tuNgay + " đến " + lich.denNgay);
        lblGhiChu.setFont(RangBuocGiaoDien.fontThuong(12));
        lblGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel dau = new JPanel();
        dau.setLayout(new BoxLayout(dau, BoxLayout.Y_AXIS));
        dau.setOpaque(false);
        dau.setBorder(new EmptyBorder(0, 0, 10, 0));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        dau.add(lblTieuDe);
        dau.add(lblGhiChu);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.setPreferredSize(new Dimension(520, 7 * 32 + 30));

        JButton btnDong = new JButton("Đóng");
        dinhDangNut(btnDong, false);

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, "Lịch làm việc", Dialog.ModalityType.APPLICATION_MODAL);
        btnDong.addActionListener(e -> dlg.dispose());

        JPanel noiDung = new JPanel(new BorderLayout());
        noiDung.setBackground(Color.WHITE);
        noiDung.setBorder(new EmptyBorder(20, 24, 12, 24));
        noiDung.add(dau, BorderLayout.NORTH);
        noiDung.add(sp, BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setBackground(Color.WHITE);
        nut.setBorder(new EmptyBorder(0, 24, 16, 24));
        nut.add(btnDong);

        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(noiDung, BorderLayout.CENTER);
        dlg.getContentPane().add(nut, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void xuatCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh_sach_bac_si.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        try {
            controller.ghiCsv(fc.getSelectedFile(), daLoc);
            JOptionPane.showMessageDialog(this, "Đã xuất " + daLoc.size() + " bác sĩ.",
                    "Xuất danh sách", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taiDanhSachChuyenNganh(JComboBox<String> cb, boolean coTatCa) {
        cb.removeAllItems();
        if (coTatCa) {
            cb.addItem("Chuyên ngành: Tất cả");
        }
        try {
            List<ChuyenNganh_entity> ds = controller.taiChuyenNganh();
            for (ChuyenNganh_entity cn : ds) {
                cb.addItem(cn.getTenChuyenNganh());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách chuyên ngành: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
        if (!coTatCa) {
            cb.addItem(THEM_CHUYEN_NGANH);
        }
    }

    private void taiLai() {
        try {
            BacSiController.DuLieuDanhSach data = controller.taiDanhSach();
            tatCa.clear();
            tatCa.addAll(data.getBacSi());
            tongNhanVien = data.getTongNhanVien();
        } catch (SQLException ex) {
            baoLoi("Không thể tải danh sách bác sĩ", ex);
        }
        lamMoi();
    }

    private static String nz(Object s) {
        return s == null ? "" : String.valueOf(s);
    }

    private class BangModel extends AbstractTableModel {
        private final String[] cot = { "Mã bác sĩ", "Họ tên", "Giới tính", "Số điện thoại", "Chuyên ngành",
                "Email", "Số ca khám", "Trạng thái" };
        private List<BacSi_entity> hang = new ArrayList<>();

        void datHang(List<BacSi_entity> h) {
            hang = new ArrayList<>(h);
            fireTableDataChanged();
        }

        BacSi_entity tai(int r) {
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

    private static void batKhuRang(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String layChu(BacSi_entity bs, int cot) {
        switch (cot) {
            case 0:
                return bs.getMaBacSi();
            case 1:
                return nz(bs.getHoTen());
            case 2:
                return nz(bs.getGioiTinh());
            case 3:
                return BacSiController.dinhDangSdt(bs.getSdt());
            case 4:
                return BacSiController.tenChuyenNganh(bs);
            case 5:
                return nz(bs.getEmail());
            case 6:
                return String.valueOf(bs.getSoCaKham());
            default:
                return "";
        }
    }

    private static class ChuRenderer extends JLabel implements TableCellRenderer {
        private final int cot;

        ChuRenderer(int cot) {
            this.cot = cot;
            setOpaque(true);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(cot == COT_HO_TEN ? RangBuocGiaoDien.fontDam(13) : RangBuocGiaoDien.fontThuong(13));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText(layChu((BacSi_entity) v, cot));
            setBackground(nen(sel));
            setForeground(cot == 0 ? RangBuocGiaoDien.MAU_CHU_PHU : RangBuocGiaoDien.MAU_CHU_CHINH);
            return this;
        }
    }

    private static class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private TrangThai tt = TrangThai.DANG_LAM;

        TrangThaiRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            tt = TrangThai.tuEntity((BacSi_entity) v);
            setBackground(nen(sel));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);

            Color nenPill, chu, cham;
            switch (tt) {
                case DANG_LAM:
                    nenPill = new Color(209, 250, 229);
                    chu = new Color(6, 95, 70);
                    cham = new Color(16, 185, 129);
                    break;
                case CHUA_TK:
                    nenPill = new Color(254, 243, 199);
                    chu = new Color(146, 64, 14);
                    cham = new Color(217, 119, 6);
                    break;
                default:
                    nenPill = new Color(238, 241, 245);
                    chu = new Color(71, 85, 105);
                    cham = new Color(148, 163, 184);
            }

            g2.setFont(RangBuocGiaoDien.fontVua(11));
            FontMetrics fm = g2.getFontMetrics();
            int h = 22, w = 10 + 6 + 6 + fm.stringWidth(tt.nhan) + 10;
            int x = 8, y = (getHeight() - h) / 2;
            g2.setColor(nenPill);
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
            g2.setColor(cham);
            g2.fill(new Ellipse2D.Double(x + 10, y + h / 2.0 - 3, 6, 6));
            g2.setColor(chu);
            g2.drawString(tt.nhan, x + 22, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private class HeaderRenderer implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        HeaderRenderer() {
            lbl.setOpaque(true);
            lbl.setBackground(NEN_HEADER);
            lbl.setFont(RangBuocGiaoDien.fontDam(12));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v == null ? "" : v.toString();
            if (c == COT_HO_TEN || c == COT_SO_CA) {
                text += (cotSapXep != c) ? "  ↕" : (sapXepTang ? "  ↑" : "  ↓");
            }
            lbl.setText(text);
            return lbl;
        }
    }

    private static class OTimKiem extends JTextField {
        private final String goiY;

        OTimKiem(String goiY) {
            this.goiY = goiY;
            setFont(RangBuocGiaoDien.fontThuong(13));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                    new EmptyBorder(6, 38, 6, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);
            int cy = getHeight() / 2;

            g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(13, cy - 7, 10, 10);
            g2.drawLine(21, cy + 2, 25, cy + 6);

            if (getText().isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(goiY, 38, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private static class HuyHieuIcon extends JPanel {
        private final int loai;

        HuyHieuIcon(int loai) {
            this.loai = loai;
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(NEN_ICON);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

            g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (loai) {
                case 0:
                    g2.drawArc(cx - 7, cy - 10, 14, 16, 180, 180);
                    g2.drawLine(cx - 7, cy - 2, cx - 7, cy - 9);
                    g2.drawLine(cx + 7, cy - 2, cx + 7, cy - 9);
                    g2.drawLine(cx, cy + 6, cx + 4, cy + 8);
                    g2.drawOval(cx + 2, cy + 5, 7, 7);
                    break;
                case 1:
                    g2.drawOval(cx - 6, cy - 9, 7, 7);
                    g2.drawArc(cx - 10, cy + 1, 14, 12, 0, 180);
                    g2.drawLine(cx + 4, cy + 3, cx + 7, cy + 6);
                    g2.drawLine(cx + 7, cy + 6, cx + 12, cy);
                    break;
                case 2:
                    g2.drawOval(cx - 8, cy - 8, 16, 16);
                    g2.drawLine(cx, cy, cx, cy - 5);
                    g2.drawLine(cx, cy, cx + 4, cy + 2);
                    break;
                default:
                    g2.drawRoundRect(cx - 7, cy - 8, 14, 17, 3, 3);
                    g2.drawRect(cx - 3, cy - 10, 6, 4);
                    g2.drawLine(cx - 3, cy + 1, cx - 1, cy + 3);
                    g2.drawLine(cx - 1, cy + 3, cx + 3, cy - 2);
            }
            g2.dispose();
        }
    }
}