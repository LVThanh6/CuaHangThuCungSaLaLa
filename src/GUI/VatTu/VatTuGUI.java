package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;
import controller.VatTu.VatTuController;
import controller.VatTu.VatTuController.CatalogItem;
import controller.VatTu.VatTuController.InventorySummary;
import controller.VatTu.VatTuController.SupplierOption;
import controller.VatTu.VatTuController.VatTuInput;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class VatTuGUI extends JPanel {

    private static final String[] DANH_MUC = {
            "Vật tư thông thường",
            "Thuốc điều trị"
    };


    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    private final List<VatTuController.VatTu> tatCa = new ArrayList<>();
    private final List<VatTuController.VatTu> daLoc = new ArrayList<>();
    private final Set<String> daChon = new HashSet<>();
    private final VatTuController controller = new VatTuController();
    private Boolean sapXepTang = null;

    private OTimKiem txtTim;
    private JComboBox<String> cbDanhMuc, cbTrangThai;
    private JTable bang;
    private BangModel model;
    private JLabel lblHienThi;
    private final JLabel[] lblSoLieu = new JLabel[4];

    public VatTuGUI() {
        khoiTaoGiaoDien();
        taiDuLieuMau(); 
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

        JLabel lblTieuDe = new JLabel("Tồn kho vật tư");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel("Kiểm tra và cập nhật số lượng tồn kho qua phiếu nhập / xuất");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        trai.add(lblTieuDe);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblMoTa);
        panel.add(trai, BorderLayout.WEST);

        return panel;
    }

    private JComponent taoHangSoLieu() {
        String[] tieuDe = { "TỔNG DANH MỤC", "TỒN ĐỦ", "SẮP HẾT TỒN", "HẾT TỒN" };
        Color[] mauChu = { RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_CHU_PHU, RangBuocGiaoDien.MAU_CHU_CHINH };

        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 16, 0));
        for (int i = 0; i < 4; i++) {
            lblSoLieu[i] = new JLabel("0 Mặt hàng");
            hang.add(taoTheSoLieu(tieuDe[i], lblSoLieu[i], mauChu[i]));
        }
        return hang;
    }

    private JPanel taoTheSoLieu(String tieuDe, JLabel lblSo, Color mau) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        if(mau == RangBuocGiaoDien.MAU_CHU_CHINH) {
            lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        } else {
             lblTieuDe.setForeground(mau);
        }

        lblSo.setFont(RangBuocGiaoDien.fontDam(24));
        lblSo.setForeground(mau);

        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        the.add(lblTieuDe);
        the.add(Box.createVerticalStrut(8));
        the.add(lblSo);
        return the;
    }

    private JComponent taoTheDanhSach() {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        the.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));

        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setOpaque(false);
        thanhTren.add(taoThanhBoLoc(), BorderLayout.NORTH);
        the.add(thanhTren, BorderLayout.NORTH);
        the.add(taoBang(), BorderLayout.CENTER);
        the.add(taoChanTrang(), BorderLayout.SOUTH);
        return the;
    }

    private JComponent taoThanhBoLoc() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setOpaque(false); // nền trắng đã có ở thẻ bao ngoài, tránh chồng lớp
        bar.setBorder(new EmptyBorder(16, 18, 16, 18));

        txtTim = new OTimKiem("Tìm theo tên hoặc mã...");
        txtTim.setPreferredSize(new Dimension(300, 38));
        txtTim.setMaximumSize(new Dimension(300, 38));
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locTuDau(); }
            @Override public void removeUpdate(DocumentEvent e) { locTuDau(); }
            @Override public void changedUpdate(DocumentEvent e) { locTuDau(); }
        });

        String[] dsDanhMuc = new String[DANH_MUC.length + 1];
        dsDanhMuc[0] = "Danh mục";
        System.arraycopy(DANH_MUC, 0, dsDanhMuc, 1, DANH_MUC.length);
        cbDanhMuc = new JComboBox<>(dsDanhMuc);

        String[] dsTrangThai = new String[VatTuController.TrangThai.values().length + 1];
        dsTrangThai[0] = "Trạng thái";
        for (int i = 0; i < VatTuController.TrangThai.values().length; i++)
            dsTrangThai[i + 1] = VatTuController.TrangThai.values()[i].label;
        cbTrangThai = new JComboBox<>(dsTrangThai);

        for (JComboBox<String> cb : java.util.List.of(cbDanhMuc, cbTrangThai)) {
            cb.setFont(RangBuocGiaoDien.fontThuong(13));
            cb.setMaximumSize(new Dimension(150, 38));
            cb.setPreferredSize(new Dimension(150, 38));
            cb.addActionListener(e -> locTuDau());
        }

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(RangBuocGiaoDien.fontThuong(13));
        btnLamMoi.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLamMoi.setPreferredSize(new Dimension(100, 38));
        btnLamMoi.setMaximumSize(new Dimension(100, 38));
        btnLamMoi.addActionListener(e -> taiDuLieuMau());

        bar.add(txtTim);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbDanhMuc);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbTrangThai);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(btnLamMoi);
        bar.add(Box.createHorizontalGlue());

        JButton btnThem = taoNut("+ Thêm vật tư", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_CHU_DAO, 130);
        JButton btnSua = taoNut("Sửa", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_DUONG_VIEN, 80);
        JButton btnXoa = taoNut("Xóa", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_DUONG_VIEN, 80);
        btnThem.addActionListener(e -> themVatTu());
        btnSua.addActionListener(e -> suaVatTu());
        btnXoa.addActionListener(e -> xoaVatTu());

        bar.add(btnThem);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnSua);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnXoa);
        return bar;
    }

    private JButton taoNut(String chu, Color nen, Color mauChu, Color vien, int rong) {
        // Tự vẽ nền để không bị L&F hệ thống / LamMoNen làm trắng hoặc trong suốt
        JButton b = new JButton(chu) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(nen);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setFont(RangBuocGiaoDien.fontDam(13));
        b.setForeground(mauChu);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(vien, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(rong, 38));
        b.setMaximumSize(new Dimension(rong, 38));
        return b;
    }

    private JComponent taoBang() {
        model = new BangModel();
        bang = new JTable(model);
        bang.setRowHeight(48);
        bang.setShowVerticalLines(false);
        bang.setShowHorizontalLines(true);
        bang.setGridColor(RangBuocGiaoDien.MAU_DUONG_VIEN);
        bang.setIntercellSpacing(new Dimension(0, 1));
        bang.setSelectionBackground(RangBuocGiaoDien.MAU_DUONG_VIEN);
        bang.setSelectionForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
        bang.setFont(RangBuocGiaoDien.fontThuong(13));
        bang.setFillsViewportHeight(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] rong = { 80, 230, 120, 80, 80, 110, 120 };
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        bang.getColumnModel().getColumn(0).setCellRenderer(new ChuRenderer(0)); // MÃ VT
        bang.getColumnModel().getColumn(1).setCellRenderer(new ChuRenderer(1)); // TÊN
        bang.getColumnModel().getColumn(2).setCellRenderer(new ChuRenderer(2)); // DANH MỤC
        bang.getColumnModel().getColumn(3).setCellRenderer(new ChuRenderer(3)); // TỒN KHO
        bang.getColumnModel().getColumn(4).setCellRenderer(new ChuRenderer(4)); // ĐƠN VỊ
        bang.getColumnModel().getColumn(5).setCellRenderer(new ChuRenderer(5)); // GIÁ NHẬP
        bang.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bang.rowAtPoint(e.getPoint()) >= 0) {
                    suaVatTu();
                }
            }
        });
        bang.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "xoaVatTu");
        bang.getActionMap().put("xoaVatTu", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                xoaVatTu();
            }
        });

        JTableHeader header = bang.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new HeaderRenderer());
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = header.columnAtPoint(e.getPoint());
                if (c == 1) { // Click cột Tên để sort
                    sapXepTang = (sapXepTang == null) ? Boolean.TRUE : (sapXepTang ? Boolean.FALSE : null);
                    apDungLoc();
                }
            }
        });

        JScrollPane sp = new JScrollPane(bang);
        sp.setBorder(new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JComponent taoChanTrang() {
        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false); // nền trắng đã có ở thẻ bao ngoài, tránh chồng lớp
        chan.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(12, 18, 12, 18)));

        lblHienThi = new JLabel(" ");
        lblHienThi.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chan.add(lblHienThi, BorderLayout.WEST);

        return chan;
    }

    private void locTuDau() {
        apDungLoc();
    }

    private void lamMoi() {
        capNhatSoLieu();
        apDungLoc();
    }

    private void capNhatSoLieu() {
        InventorySummary summary = controller.thongKe(tatCa);
        lblSoLieu[0].setText(summary.getTotal() + " Mặt hàng");
        lblSoLieu[1].setText(summary.getSufficient() + " Mặt hàng");
        lblSoLieu[2].setText(summary.getLow() + " Mặt hàng");
        lblSoLieu[3].setText(summary.getEmpty() + " Mặt hàng");
    }

    private void apDungLoc() {
        int dm = cbDanhMuc.getSelectedIndex();
        int tt = cbTrangThai.getSelectedIndex();
        VatTuController.TrangThai status = tt == 0 ? null
                : VatTuController.TrangThai.values()[tt - 1];
        List<VatTuController.VatTu> filtered = controller.loc(
                tatCa, txtTim.getText(), dm == 0 ? null : DANH_MUC[dm - 1],
                status, sapXepTang);
        daLoc.clear();
        daLoc.addAll(filtered);
        capNhatBang();
    }

    private void capNhatBang() {
        int tong = daLoc.size();
        model.datHang(daLoc);
        lblHienThi.setText(tong == 0
                ? "Không có mặt hàng phù hợp"
                : "Hiển thị tất cả " + tong + " mặt hàng");
        bang.getTableHeader().repaint();
    }

    private void taiDuLieuMau() {
        tatCa.clear();
        tatCa.addAll(controller.taiDanhSach());
        lamMoi();
    }
    
    // ===================== THÊM / SỬA / XÓA =====================

    private void themVatTu() {
        VatTuInput moi = hienFormVatTu(null);
        if (moi == null) return;

        VatTuController.SaveResult result = controller.luu(moi, false);
        if (result.isSaved()) {
            taiDuLieuMau();
            chonDong(moi.getMa());
            JOptionPane.showMessageDialog(this, "Đã thêm vật tư " + moi.getMa() + ".");
        } else {
            JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVatTu() {
        int r = bang.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vật tư để sửa.");
            return;
        }
        VatTuController.VatTu cu = model.tai(r);
        VatTuInput moi = hienFormVatTu(cu);
        if (moi == null) return;

        VatTuController.SaveResult result = controller.luu(moi, true);
        if (result.isSaved()) {
            taiDuLieuMau();
            chonDong(moi.getMa());
            JOptionPane.showMessageDialog(this, "Đã cập nhật vật tư " + moi.getMa() + ".");
        } else {
            JOptionPane.showMessageDialog(this, result.getError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaVatTu() {
        int r = bang.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vật tư để xóa.");
            return;
        }
        VatTuController.VatTu vt = model.tai(r);
        int chon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa vật tư \"" + vt.getTen() + "\" (" + vt.getMa() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) return;

        if (controller.xoa(vt.getMa())) {
            taiDuLieuMau();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa vật tư này. Có thể vật tư đã được dùng trong phiếu nhập / xuất.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Chọn lại dòng có mã tương ứng sau khi nạp lại bảng (nếu dòng đó đang hiển thị). */
    private void chonDong(String ma) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.tai(i).getMa().equals(ma)) {
                bang.setRowSelectionInterval(i, i);
                bang.scrollRectToVisible(bang.getCellRect(i, 0, true));
                return;
            }
        }
    }

    /**
     * Hiện form thêm (cu == null) hoặc sửa (cu != null).
     * Trả về entity mới nếu người dùng bấm Lưu và dữ liệu hợp lệ, ngược lại trả về null.
     */
    private VatTuInput hienFormVatTu(VatTuController.VatTu cu) {
        final boolean sua = cu != null;

        List<SupplierOption> dsNCC = controller.taiNhaCungCap();
        if (dsNCC.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có nhà cung cấp nào trong CSDL nên không thể thêm / sửa vật tư.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, sua ? "Sửa vật tư" : "Thêm vật tư", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(sua ? cu.getMa() : controller.taoMaMoi(), 22);
        txtMa.setEditable(false); // mã tự phát sinh (VT + STT), không cho nhập tay
        txtMa.setBackground(RangBuocGiaoDien.MAU_DUONG_VIEN);   // ô chỉ đọc: nền khác ô nhập
        txtMa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        JTextField txtTen = new JTextField(sua ? cu.getTen() : "", 22);

        JComboBox<String> cbNcc = new JComboBox<>();
        int viTriNcc = 0;
        for (int i = 0; i < dsNCC.size(); i++) {
            SupplierOption n = dsNCC.get(i);
            cbNcc.addItem(n.getCode() + " - " + (n.getName() == null ? "" : n.getName()));
            if (sua && n.getCode() != null && n.getCode().equals(cu.getMaNCC())) viTriNcc = i;
        }
        cbNcc.setSelectedIndex(viTriNcc);

        JComboBox<String> cbDm = new JComboBox<>(DANH_MUC);
        JTextField txtMaThuoc = new JTextField(sua && cu.getMaThuoc() != null ? cu.getMaThuoc() : "", 22);
        if (sua) cbDm.setSelectedItem(cu.getDanhMuc());
        Runnable capNhatMaThuoc = () -> {
            boolean laThuoc = DANH_MUC[1].equals(cbDm.getSelectedItem());
            txtMaThuoc.setEnabled(laThuoc);
            if (!laThuoc) txtMaThuoc.setText("");
        };
        cbDm.addActionListener(e -> capNhatMaThuoc.run());
        capNhatMaThuoc.run();

        JSpinner spTon = new JSpinner(new SpinnerNumberModel(sua ? cu.getTonKho() : 0, 0, 10_000_000, 1));
        spTon.setEnabled(!sua); // tồn kho chỉ nhập ban đầu; sau đó thay đổi qua phiếu nhập / xuất

        JTextField txtGiaNhap = new JTextField(sua ? String.valueOf(cu.getGiaNhap()) : "", 22);
        JTextField txtGiaBan = new JTextField(sua ? String.valueOf(cu.getGiaBan()) : "", 22);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        form.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        txtMaThuoc.setDisabledTextColor(RangBuocGiaoDien.MAU_CHU_PHU);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        String[] nhan = { "Mã vật tư (tự động)", "Tên vật tư *", "Nhà cung cấp *", "Danh mục", "Mã thuốc (nếu là thuốc)",
                "Tồn kho", "Giá nhập (₫) *", "Giá bán cơ bản (₫) *" };
        JComponent[] oNhap = { txtMa, txtTen, cbNcc, cbDm, txtMaThuoc, spTon, txtGiaNhap, txtGiaBan };
        for (int i = 0; i < nhan.length; i++) {
            JLabel l = new JLabel(nhan[i]);
            l.setFont(RangBuocGiaoDien.fontThuong(13));
            l.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            oNhap[i].setFont(RangBuocGiaoDien.fontThuong(13));
            if (oNhap[i] != txtMa) {
                oNhap[i].setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
                oNhap[i].setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            }
            if (oNhap[i] instanceof JTextField) {
                oNhap[i].setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                        new EmptyBorder(5, 8, 5, 8)));
            }
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            form.add(l, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add(oNhap[i], gc);
        }

        JButton btnLuu = taoNut("Lưu", RangBuocGiaoDien.MAU_CHU_DAO, RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_CHU_DAO, 90);
        JButton btnHuy = taoNut("Hủy", RangBuocGiaoDien.MAU_NEN_TRANG, RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_DUONG_VIEN, 90);
        JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        chan.setBorder(new EmptyBorder(0, 12, 8, 12));
        chan.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        chan.add(btnHuy);
        chan.add(btnLuu);

        final VatTuInput[] ketQua = new VatTuInput[1];

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String maThuoc = txtMaThuoc.getText().trim();
            boolean laThuoc = DANH_MUC[1].equals(cbDm.getSelectedItem());

            if (ma.isEmpty()) { canhBao(dlg, "Vui lòng nhập mã vật tư.", txtMa); return; }
            if (!sua && controller.maVatTuTonTai(ma)) {
                canhBao(dlg, "Mã vật tư \"" + ma + "\" đã tồn tại.", txtMa);
                return;
            }
            if (ten.isEmpty()) { canhBao(dlg, "Vui lòng nhập tên vật tư.", txtTen); return; }
            if (laThuoc && maThuoc.isEmpty()) {
                canhBao(dlg, "Danh mục \"Thuốc điều trị\" cần nhập mã thuốc.", txtMaThuoc);
                return;
            }
            Long giaNhap = VatTuController.docSoTien(txtGiaNhap.getText());
            if (giaNhap == null) { canhBao(dlg, "Giá nhập không hợp lệ.", txtGiaNhap); return; }
            Long giaBan = VatTuController.docSoTien(txtGiaBan.getText());
            if (giaBan == null) { canhBao(dlg, "Giá bán cơ bản không hợp lệ.", txtGiaBan); return; }

            int ton = (Integer) spTon.getValue();
            String supplierCode = dsNCC.get(cbNcc.getSelectedIndex()).getCode();
            String databaseStatus = (sua && cu.getTrangThai() != null)
                    ? cu.getTrangThai()
                    : VatTuController.trangThai(ton).label;
            VatTuInput moi = new VatTuInput(ma, ten, supplierCode,
                    laThuoc ? maThuoc : null, laThuoc, ton, giaNhap, giaBan, databaseStatus);
            String validationError = controller.validate(moi, sua);
            if (validationError != null) {
                JComponent focus = validationError.contains("mã vật tư") ? txtMa
                        : validationError.contains("tên vật tư") ? txtTen
                        : validationError.contains("mã thuốc") ? txtMaThuoc
                        : validationError.contains("Giá nhập") ? txtGiaNhap
                        : validationError.contains("Giá bán") ? txtGiaBan : cbNcc;
                canhBao(dlg, validationError, focus);
                return;
            }
            ketQua[0] = moi;
            dlg.dispose();
        });

        dlg.getRootPane().setDefaultButton(btnLuu);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        dlg.getContentPane().add(form, BorderLayout.CENTER);
        dlg.getContentPane().add(chan, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true); // chặn cho tới khi đóng form
        return ketQua[0];
    }

    private static void canhBao(Component cha, String noiDung, JComponent focus) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thông báo", JOptionPane.WARNING_MESSAGE);
        if (focus != null) focus.requestFocusInWindow();
    }

    // --- Các hàm phục vụ phiếu nhập / xuất ---
    
// --- Các hàm phục vụ phiếu nhập / xuất (Đã ủy quyền sang Controller) ---
    
    public int getTonKho(String ma) {
        try {
            return controller.tonKho(ma, tatCa);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không tìm thấy vật tư", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    public List<CatalogItem> getDanhSachVatTu() {
        return controller.danhMucVatTu(tatCa);
    }

    public void tangTon(String ma, int soLuong) {
        try {
            controller.tangSoLuongTon(ma, soLuong, tatCa);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không tìm thấy vật tư", JOptionPane.ERROR_MESSAGE);
            return;
        }
        lamMoi();
    }


    private class BangModel extends AbstractTableModel {
        private final String[] cot = { "MÃ VT", "TÊN VẬT TƯ", "DANH MỤC", "TỒN KHO", "ĐƠN VỊ", "GIÁ NHẬP", "TRẠNG THÁI" };
        private List<VatTuController.VatTu> hang = new ArrayList<>();

        void datHang(List<VatTuController.VatTu> h) {
            hang = new ArrayList<>(h);
            fireTableDataChanged();
        }

        VatTuController.VatTu tai(int r) { return hang.get(r); }

        @Override public int getRowCount() { return hang.size(); }
        @Override public int getColumnCount() { return cot.length; }
        @Override public String getColumnName(int c) { return cot[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Object getValueAt(int r, int c) { return hang.get(r); }
    }

    private static Color nen(boolean chon) { return chon ? RangBuocGiaoDien.MAU_DUONG_VIEN : RangBuocGiaoDien.MAU_NEN_TRANG; }

    private static void batKhuRang(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String layChu(VatTuController.VatTu vt, int cot) {
        switch (cot) {
            case 0: return vt.getMa();
            case 1: return vt.getTen();
            case 2: return vt.getDanhMuc();
            case 3: return String.valueOf(vt.getTonKho());
            case 4: return vt.getDonVi();
            case 5: return NumberFormat.getInstance(VN).format(vt.getGiaNhap()) + " ₫";
            default: return "";
        }
    }

    private static class ChuRenderer extends JLabel implements TableCellRenderer {
        private final int cot;

        ChuRenderer(int cot) {
            this.cot = cot;
            setOpaque(true);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(RangBuocGiaoDien.fontThuong(13));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            VatTuController.VatTu vt = (VatTuController.VatTu) v;
            setText(layChu(vt, cot));
            setBackground(nen(sel));
            
            if (cot == 0 || cot == 2 || cot == 4) {
                 setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            } else if (cot == 1) {
                 setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                 setFont(RangBuocGiaoDien.fontDam(13));
            } else if (cot == 3) {
                 int ton = vt.getTonKho();
                 setForeground(ton <= 0 ? RangBuocGiaoDien.MAU_CHU_CHINH : (ton <= 30 ? RangBuocGiaoDien.MAU_CHU_PHU : RangBuocGiaoDien.MAU_CHU_DAO));
                 setFont(RangBuocGiaoDien.fontDam(13));
            } else {
                 setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                 setFont(RangBuocGiaoDien.fontThuong(13));
            }
            return this;
        }
    }

    private static class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private VatTuController.TrangThai tt = VatTuController.TrangThai.CON_HANG;

        TrangThaiRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            tt = VatTuController.trangThai(((VatTuController.VatTu) v).getTonKho());
            setBackground(nen(sel));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);

            Color nenPill = RangBuocGiaoDien.MAU_DUONG_VIEN, chu, cham;
            switch (tt) {
                case CON_HANG:
                    chu = RangBuocGiaoDien.MAU_CHU_DAO;
                    cham = RangBuocGiaoDien.MAU_CHU_DAO;
                    break;
                case SAP_HET:
                    chu = RangBuocGiaoDien.MAU_CHU_PHU;
                    cham = RangBuocGiaoDien.MAU_CHU_PHU;
                    break;
                default: // HET_TON
                    chu = RangBuocGiaoDien.MAU_CHU_CHINH;
                    cham = RangBuocGiaoDien.MAU_CHU_CHINH;
            }

            Font f = RangBuocGiaoDien.fontVua(11);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int h = 22, w = 10 + 6 + 6 + fm.stringWidth(tt.label) + 10;
            int x = 8, y = (getHeight() - h) / 2;
            g2.setColor(nenPill);
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
            g2.setColor(cham);
            g2.fill(new Ellipse2D.Double(x + 10, y + h / 2.0 - 3, 6, 6));
            g2.setColor(chu);
            g2.drawString(tt.label, x + 22, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private class HeaderRenderer implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        HeaderRenderer() {
            lbl.setOpaque(true);
            lbl.setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
            lbl.setFont(RangBuocGiaoDien.fontDam(12));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v == null ? "" : v.toString();
            if (c == 1) { // Mũi tên sort ở cột Tên
                text += sapXepTang == null ? "  ↕" : (sapXepTang ? "  ↑" : "  ↓");
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
                g2.setColor(RangBuocGiaoDien.MAU_CHU_PHU);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(goiY, 38, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}