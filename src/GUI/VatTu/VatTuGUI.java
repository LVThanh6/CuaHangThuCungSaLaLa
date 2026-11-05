package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;
import dao.VatTu_DAO;
import entity.NhaCungCap_entity;
import entity.VatTu_entity;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Collator;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.Normalizer;
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

    public enum TrangThai {
        CON_HANG("Còn hàng"),
        SAP_HET("Sắp hết"),
        HET_TON("Hết tồn");

        public final String nhan;

        TrangThai(String nhan) {
            this.nhan = nhan;
        }

        public static TrangThai tuSoLuong(int ton) {
            if (ton <= 0) return HET_TON;
            if (ton <= 30) return SAP_HET; // Ngưỡng sắp hết là 30
            return CON_HANG;
        }
    }

    // --- Entity cho Vật Tư ---
    public static class VatTuEntity {
        String ma, ten, danhMuc, donVi, maThuoc;
        int tonKho;
        long giaNhap;
        // Các trường lưu kèm để phục vụ thêm / sửa xuống CSDL
        String maNCC, trangThaiDb;
        double giaBan;

        public VatTuEntity(String ma, String ten, String danhMuc, int tonKho, String donVi, long giaNhap, String maThuoc) {
            this.ma = ma; this.ten = ten; this.danhMuc = danhMuc;
            this.tonKho = tonKho; this.donVi = donVi; this.giaNhap = giaNhap;
            this.maThuoc = maThuoc;
        }
        // Getters
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDanhMuc() { return danhMuc; }
        public int getTonKho() { return tonKho; }
        public String getDonVi() { return donVi; }
        public long getGiaNhap() { return giaNhap; }
        public String getMaThuoc() { return maThuoc; }
    }

    private static final String[] DANH_MUC = {
            "Vật tư thông thường",
            "Thuốc điều trị"
    };

    private static final Color NEN_CHON = new Color(240, 250, 247);
    private static final Color NEN_HEADER = new Color(246, 248, 247);
    private static final Color NEN_ICON = new Color(226, 244, 240);
    private static final Color MAU_DO = new Color(220, 38, 38);
    private static final Color MAU_XANH_LA = new Color(16, 185, 129);
    private static final Color MAU_CAM = new Color(245, 158, 11);

    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    private final List<VatTuEntity> tatCa = new ArrayList<>();
    private final List<VatTuEntity> daLoc = new ArrayList<>();
    private final Set<String> daChon = new HashSet<>();
    private final VatTu_DAO vatTuDAO = new VatTu_DAO();
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
        Color[] mauChu = { RangBuocGiaoDien.MAU_CHU_CHINH, MAU_XANH_LA, MAU_CAM, MAU_DO };

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
        the.setBackground(Color.WHITE);
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
        the.setBackground(Color.WHITE);
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

        String[] dsTrangThai = new String[TrangThai.values().length + 1];
        dsTrangThai[0] = "Trạng thái";
        for (int i = 0; i < TrangThai.values().length; i++)
            dsTrangThai[i + 1] = TrangThai.values()[i].nhan;
        cbTrangThai = new JComboBox<>(dsTrangThai);

        for (JComboBox<String> cb : java.util.List.of(cbDanhMuc, cbTrangThai)) {
            cb.setFont(RangBuocGiaoDien.fontThuong(13));
            cb.setMaximumSize(new Dimension(150, 38));
            cb.setPreferredSize(new Dimension(150, 38));
            cb.addActionListener(e -> locTuDau());
        }

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(RangBuocGiaoDien.fontThuong(13));
        btnLamMoi.setBackground(Color.WHITE);
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

        JButton btnThem = taoNut("+ Thêm vật tư", MAU_XANH_LA, Color.BLACK, MAU_XANH_LA, 130);
        JButton btnSua = taoNut("Sửa", Color.BLACK, RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_DUONG_VIEN, 80);
        JButton btnXoa = taoNut("Xóa", Color.BLACK, MAU_DO, MAU_DO, 80);
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
        JButton b = new JButton(chu);
        b.setFont(RangBuocGiaoDien.fontDam(13));
        b.setBackground(nen);
        b.setForeground(mauChu);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
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
        bang.setSelectionBackground(NEN_CHON);
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
        sp.getViewport().setBackground(Color.WHITE);
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
        int du = 0, sapHet = 0, het = 0;
        for (VatTuEntity vt : tatCa) {
            switch (TrangThai.tuSoLuong(vt.getTonKho())) {
                case CON_HANG: du++; break;
                case SAP_HET:  sapHet++; break;
                case HET_TON:  het++; break;
            }
        }
        lblSoLieu[0].setText(tatCa.size() + " Mặt hàng");
        lblSoLieu[1].setText(du + " Mặt hàng");
        lblSoLieu[2].setText(sapHet + " Mặt hàng");
        lblSoLieu[3].setText(het + " Mặt hàng");
    }

    private void apDungLoc() {
        String tu = boDau(txtTim.getText().trim());
        int dm = cbDanhMuc.getSelectedIndex();
        int tt = cbTrangThai.getSelectedIndex();

        daLoc.clear();
        for (VatTuEntity vt : tatCa) {
            if (dm > 0 && !DANH_MUC[dm - 1].equals(vt.getDanhMuc())) continue;
            if (tt > 0 && TrangThai.tuSoLuong(vt.getTonKho()) != TrangThai.values()[tt - 1]) continue;
            if (!tu.isEmpty()
                    && !boDau(vt.getTen()).contains(tu)
                    && !boDau(vt.getMa()).contains(tu))
                continue;
            daLoc.add(vt);
        }

        if (sapXepTang != null) {
            Collator cl = Collator.getInstance(VN);
            boolean tang = sapXepTang;
            daLoc.sort((a, b) -> {
                int kq = cl.compare(a.getTen(), b.getTen());
                return tang ? kq : -kq;
            });
        }

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

    private static String boDau(String s) {
        String t = Normalizer.normalize(s.toLowerCase(VN), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replace('đ', 'd');
    }

    private void taiDuLieuMau() {
        tatCa.clear();
        for (VatTu_entity vatTu : vatTuDAO.getAllVatTu()) {
            boolean laThuoc = vatTu.getMaThuoc() != null && vatTu.getMaThuoc().getMaThuoc() != null && !vatTu.getMaThuoc().getMaThuoc().isBlank();
            String danhMuc = laThuoc ? "Thuốc điều trị" : "Vật tư thông thường";
            String donVi = laThuoc ? "Viên" : "Gói / Hộp";
            VatTuEntity vt = new VatTuEntity(
                    vatTu.getMaVT(),
                    vatTu.getTenVT(),
                    danhMuc,
                    vatTu.getSoLuongTon(),
                    donVi,
                    Math.round(vatTu.getGiaNhap()),
                    (vatTu.getMaThuoc() != null ? vatTu.getMaThuoc().getMaThuoc() : null));
            vt.maNCC = vatTu.getMaNCC() != null ? vatTu.getMaNCC().getMaNCC() : null;
            vt.giaBan = vatTu.getGiaBanCoBan();
            vt.trangThaiDb = vatTu.getTrangThai();
            tatCa.add(vt);
        }
        lamMoi();
    }
    
    // ===================== THÊM / SỬA / XÓA =====================

    private void themVatTu() {
        VatTuEntity moi = hienFormVatTu(null);
        if (moi == null) return;

        if (vatTuDAO.createVatTu(chuyenSangEntityDB(moi))) {
            taiDuLieuMau();
            chonDong(moi.getMa());
            JOptionPane.showMessageDialog(this, "Đã thêm vật tư " + moi.getMa() + ".");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể thêm vật tư. Kiểm tra lại mã nhà cung cấp, mã thuốc hoặc kết nối CSDL.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVatTu() {
        int r = bang.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vật tư để sửa.");
            return;
        }
        VatTuEntity cu = model.tai(r);
        VatTuEntity moi = hienFormVatTu(cu);
        if (moi == null) return;

        if (vatTuDAO.updateVatTu(chuyenSangEntityDB(moi))) {
            taiDuLieuMau();
            chonDong(moi.getMa());
            JOptionPane.showMessageDialog(this, "Đã cập nhật vật tư " + moi.getMa() + ".");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể cập nhật vật tư. Kiểm tra lại mã nhà cung cấp, mã thuốc hoặc kết nối CSDL.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaVatTu() {
        int r = bang.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vật tư để xóa.");
            return;
        }
        VatTuEntity vt = model.tai(r);
        int chon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa vật tư \"" + vt.getTen() + "\" (" + vt.getMa() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) return;

        if (vatTuDAO.deleteVatTu(vt.getMa())) {
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

    private VatTu_entity chuyenSangEntityDB(VatTuEntity v) {
        NhaCungCap_entity ncc = new NhaCungCap_entity(v.maNCC, null, null, null, null);
        return new VatTu_entity(v.getMa(), ncc, v.getTen(), v.getTonKho(),
                v.trangThaiDb, v.giaBan, v.getGiaNhap(), v.getMaThuoc());
    }

    private static Long docTien(String s) {
        String so = s == null ? "" : s.replaceAll("[^0-9]", "");
        if (so.isEmpty()) return null;
        try {
            return Long.parseLong(so);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Hiện form thêm (cu == null) hoặc sửa (cu != null).
     * Trả về entity mới nếu người dùng bấm Lưu và dữ liệu hợp lệ, ngược lại trả về null.
     */
    private VatTuEntity hienFormVatTu(VatTuEntity cu) {
        final boolean sua = cu != null;

        ArrayList<String[]> dsNCC = vatTuDAO.getDanhSachNCC();
        if (dsNCC.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có nhà cung cấp nào trong CSDL nên không thể thêm / sửa vật tư.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Window cha = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(cha, sua ? "Sửa vật tư" : "Thêm vật tư", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtMa = new JTextField(sua ? cu.getMa() : vatTuDAO.taoMaVatTuMoi(), 22);
        txtMa.setEditable(false); // mã tự phát sinh (VT + STT), không cho nhập tay
        txtMa.setBackground(NEN_HEADER);
        JTextField txtTen = new JTextField(sua ? cu.getTen() : "", 22);

        JComboBox<String> cbNcc = new JComboBox<>();
        int viTriNcc = 0;
        for (int i = 0; i < dsNCC.size(); i++) {
            String[] n = dsNCC.get(i);
            cbNcc.addItem(n[0] + " - " + (n[1] == null ? "" : n[1]));
            if (sua && n[0] != null && n[0].equals(cu.maNCC)) viTriNcc = i;
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
        JTextField txtGiaBan = new JTextField(sua ? String.valueOf(Math.round(cu.giaBan)) : "", 22);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        String[] nhan = { "Mã vật tư (tự động)", "Tên vật tư *", "Nhà cung cấp *", "Danh mục", "Mã thuốc (nếu là thuốc)",
                "Tồn kho", "Giá nhập (₫) *", "Giá bán cơ bản (₫) *" };
        JComponent[] oNhap = { txtMa, txtTen, cbNcc, cbDm, txtMaThuoc, spTon, txtGiaNhap, txtGiaBan };
        for (int i = 0; i < nhan.length; i++) {
            JLabel l = new JLabel(nhan[i]);
            l.setFont(RangBuocGiaoDien.fontThuong(13));
            oNhap[i].setFont(RangBuocGiaoDien.fontThuong(13));
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            form.add(l, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add(oNhap[i], gc);
        }

        JButton btnLuu = taoNut("Lưu", MAU_XANH_LA, Color.BLACK, MAU_XANH_LA, 90);
        JButton btnHuy = taoNut("Hủy", Color.BLACK, RangBuocGiaoDien.MAU_CHU_CHINH, RangBuocGiaoDien.MAU_DUONG_VIEN, 90);
        JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        chan.setBorder(new EmptyBorder(0, 12, 8, 12));
        chan.add(btnHuy);
        chan.add(btnLuu);

        final VatTuEntity[] ketQua = new VatTuEntity[1];

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String maThuoc = txtMaThuoc.getText().trim();
            boolean laThuoc = DANH_MUC[1].equals(cbDm.getSelectedItem());

            if (ma.isEmpty()) { canhBao(dlg, "Vui lòng nhập mã vật tư.", txtMa); return; }
            if (!sua && vatTuDAO.getVatTuTheoMa(ma) != null) {
                canhBao(dlg, "Mã vật tư \"" + ma + "\" đã tồn tại.", txtMa);
                return;
            }
            if (ten.isEmpty()) { canhBao(dlg, "Vui lòng nhập tên vật tư.", txtTen); return; }
            if (laThuoc && maThuoc.isEmpty()) {
                canhBao(dlg, "Danh mục \"Thuốc điều trị\" cần nhập mã thuốc.", txtMaThuoc);
                return;
            }
            Long giaNhap = docTien(txtGiaNhap.getText());
            if (giaNhap == null) { canhBao(dlg, "Giá nhập không hợp lệ.", txtGiaNhap); return; }
            Long giaBan = docTien(txtGiaBan.getText());
            if (giaBan == null) { canhBao(dlg, "Giá bán cơ bản không hợp lệ.", txtGiaBan); return; }

            int ton = (Integer) spTon.getValue();
            String donVi = laThuoc ? "Viên" : "Gói / Hộp";
            VatTuEntity moi = new VatTuEntity(ma, ten, (String) cbDm.getSelectedItem(),
                    ton, donVi, giaNhap, laThuoc ? maThuoc : null);
            moi.maNCC = dsNCC.get(cbNcc.getSelectedIndex())[0];
            moi.giaBan = giaBan;
            moi.trangThaiDb = (sua && cu.trangThaiDb != null)
                    ? cu.trangThaiDb
                    : TrangThai.tuSoLuong(ton).nhan;
            ketQua[0] = moi;
            dlg.dispose();
        });

        dlg.getRootPane().setDefaultButton(btnLuu);
        dlg.getContentPane().setLayout(new BorderLayout());
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
    
    public int getTonKho(String ma) {
        for (VatTuEntity vt : tatCa) {
            if (vt.getMa().equals(ma)) return vt.getTonKho();
        }
        return 0;
    }

    public java.util.List<Object[]> getDanhSachVatTu() {
        java.util.List<Object[]> ds = new java.util.ArrayList<>();
        for (VatTuEntity vt : tatCa) {
            ds.add(new Object[]{vt.getMa(), vt.getTen(), vt.getDonVi(), vt.getGiaNhap()});
        }
        return ds;
    }

    public void tangTon(String ma, int soLuong) {
        for (VatTuEntity vt : tatCa) {
            if (vt.getMa().equals(ma)) {
                vt.tonKho += soLuong;
                lamMoi(); // Cập nhật lại giao diện
                return;
            }
        }
    }


    private class BangModel extends AbstractTableModel {
        private final String[] cot = { "MÃ VT", "TÊN VẬT TƯ", "DANH MỤC", "TỒN KHO", "ĐƠN VỊ", "GIÁ NHẬP", "TRẠNG THÁI" };
        private List<VatTuEntity> hang = new ArrayList<>();

        void datHang(List<VatTuEntity> h) {
            hang = new ArrayList<>(h);
            fireTableDataChanged();
        }

        VatTuEntity tai(int r) { return hang.get(r); }

        @Override public int getRowCount() { return hang.size(); }
        @Override public int getColumnCount() { return cot.length; }
        @Override public String getColumnName(int c) { return cot[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Object getValueAt(int r, int c) { return hang.get(r); }
    }

    private static Color nen(boolean chon) { return chon ? NEN_CHON : Color.WHITE; }

    private static void batKhuRang(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String layChu(VatTuEntity vt, int cot) {
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
            VatTuEntity vt = (VatTuEntity) v;
            setText(layChu(vt, cot));
            setBackground(nen(sel));
            
            if (cot == 0 || cot == 2 || cot == 4) {
                 setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            } else if (cot == 1) {
                 setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
                 setFont(RangBuocGiaoDien.fontDam(13));
            } else if (cot == 3) {
                 int ton = vt.getTonKho();
                 setForeground(ton <= 0 ? MAU_DO : (ton <= 30 ? MAU_CAM : RangBuocGiaoDien.MAU_CHU_CHINH));
                 setFont(RangBuocGiaoDien.fontDam(13));
            } else {
                 setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
                 setFont(RangBuocGiaoDien.fontThuong(13));
            }
            return this;
        }
    }

    private static class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private TrangThai tt = TrangThai.CON_HANG;

        TrangThaiRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            tt = TrangThai.tuSoLuong(((VatTuEntity) v).getTonKho());
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
                case CON_HANG:
                    nenPill = new Color(209, 250, 229);
                    chu = new Color(6, 95, 70);
                    cham = new Color(16, 185, 129);
                    break;
                case SAP_HET:
                    nenPill = new Color(254, 243, 199);
                    chu = new Color(180, 83, 9);
                    cham = new Color(245, 158, 11);
                    break;
                default: // HET_TON
                    nenPill = new Color(254, 226, 226);
                    chu = new Color(185, 28, 28);
                    cham = new Color(239, 68, 68);
            }

            Font f = RangBuocGiaoDien.fontVua(11);
            g2.setFont(f);
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