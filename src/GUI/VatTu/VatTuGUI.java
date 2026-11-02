package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;
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

    // --- Entity tạm thời cho Vật Tư (vì không có file entity gốc) ---
    public static class VatTuEntity {
        String ma, ten, danhMuc, donVi;
        int tonKho;
        long giaNhap;
        long giaBan;

        public VatTuEntity(String ma, String ten, String danhMuc, int tonKho, String donVi, long giaNhap, long giaBan) {
            this.ma = ma; this.ten = ten; this.danhMuc = danhMuc;
            this.tonKho = tonKho; this.donVi = donVi;
            this.giaNhap = giaNhap; this.giaBan = giaBan;
        }
        // Getters
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public String getDanhMuc() { return danhMuc; }
        public int getTonKho() { return tonKho; }
        public String getDonVi() { return donVi; }
        public long getGiaNhap() { return giaNhap; }
        public long getGiaBan() { return giaBan; }
    }

    private static final String[] DANH_MUC = {
            "Kháng sinh", "Vắc-xin", "Giảm đau", "Ký sinh", "Tiêu hao"
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
    private int trangHienTai = 1;
    private int soDongMoiTrang = 8;
    private Boolean sapXepTang = null;

    private OTimKiem txtTim;
    private JComboBox<String> cbDanhMuc, cbTrangThai, cbSoDong;
    private JTable bang;
    private BangModel model;
    private JLabel lblHienThi;
    private JPanel panelTrang;
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
        bar.setBackground(Color.WHITE);
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

        bar.add(txtTim);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbDanhMuc);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbTrangThai);
        bar.add(Box.createHorizontalGlue());
        return bar;
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

        int[] rong = { 80, 220, 110, 70, 70, 105, 105, 120 };
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        bang.getColumnModel().getColumn(0).setCellRenderer(new ChuRenderer(0)); // MÃ VT
        bang.getColumnModel().getColumn(1).setCellRenderer(new ChuRenderer(1)); // TÊN
        bang.getColumnModel().getColumn(2).setCellRenderer(new ChuRenderer(2)); // DANH MỤC
        bang.getColumnModel().getColumn(3).setCellRenderer(new ChuRenderer(3)); // TỒN KHO
        bang.getColumnModel().getColumn(4).setCellRenderer(new ChuRenderer(4)); // ĐƠN VỊ
        bang.getColumnModel().getColumn(5).setCellRenderer(new ChuRenderer(5)); // GIÁ NHẬP
        bang.getColumnModel().getColumn(6).setCellRenderer(new ChuRenderer(6)); // GIÁ BÁN
        bang.getColumnModel().getColumn(7).setCellRenderer(new TrangThaiRenderer());

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
        chan.setBackground(Color.WHITE);
        chan.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                new EmptyBorder(12, 18, 12, 18)));

        lblHienThi = new JLabel(" ");
        lblHienThi.setFont(RangBuocGiaoDien.fontThuong(12));
        lblHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        chan.add(lblHienThi, BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);

        JLabel lblSoDong = new JLabel("Số dòng / trang");
        lblSoDong.setFont(RangBuocGiaoDien.fontThuong(12));
        lblSoDong.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        cbSoDong = new JComboBox<>(new String[] { "8", "16", "24" });
        cbSoDong.setFont(RangBuocGiaoDien.fontThuong(12));
        cbSoDong.setPreferredSize(new Dimension(64, 32));
        cbSoDong.addActionListener(e -> {
            soDongMoiTrang = Integer.parseInt((String) cbSoDong.getSelectedItem());
            trangHienTai = 1;
            capNhatBang();
        });

        panelTrang = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelTrang.setOpaque(false);

        phai.add(lblSoDong);
        phai.add(cbSoDong);
        phai.add(Box.createHorizontalStrut(10));
        phai.add(panelTrang);
        chan.add(phai, BorderLayout.EAST);
        return chan;
    }

    private void locTuDau() {
        trangHienTai = 1;
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

        trangHienTai = Math.max(1, Math.min(trangHienTai, tongTrang()));
        capNhatBang();
    }

    private int tongTrang() {
        return Math.max(1, (int) Math.ceil(daLoc.size() / (double) soDongMoiTrang));
    }

    private void capNhatBang() {
        int tong = daLoc.size();
        int tu = (trangHienTai - 1) * soDongMoiTrang;
        int den = Math.min(tu + soDongMoiTrang, tong);
        model.datHang(tu < den ? daLoc.subList(tu, den) : new ArrayList<VatTuEntity>());
        lblHienThi.setText(tong == 0
                ? "Không có mặt hàng phù hợp"
                : "Hiển thị " + (tu + 1) + "–" + den + " trong " + tong + " mặt hàng");
        veNutTrang();
        bang.getTableHeader().repaint();
    }

    private void veNutTrang() {
        panelTrang.removeAll();
        int tong = tongTrang();
        panelTrang.add(taoNutTrang("<", false, trangHienTai > 1, () -> {
            trangHienTai--;
            capNhatBang();
        }));

        int dau = Math.max(1, Math.min(trangHienTai - 2, tong - 4));
        int cuoi = Math.min(tong, dau + 4);
        for (int p = dau; p <= cuoi; p++) {
            final int trang = p;
            panelTrang.add(taoNutTrang(String.valueOf(p), p == trangHienTai, true, () -> {
                trangHienTai = trang;
                capNhatBang();
            }));
        }

        panelTrang.add(taoNutTrang(">", false, trangHienTai < tong, () -> {
            trangHienTai++;
            capNhatBang();
        }));
        panelTrang.revalidate();
        panelTrang.repaint();
    }

    private JButton taoNutTrang(String text, boolean chon, boolean choPhep, Runnable hanhDong) {
        JButton b = new JButton(text);
        b.setFont(RangBuocGiaoDien.fontVua(12));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(34, 32));
        b.setBorder(new LineBorder(chon ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));
        b.setBackground(chon ? RangBuocGiaoDien.MAU_CHU_DAO : Color.WHITE);
        b.setForeground(chon ? Color.WHITE : RangBuocGiaoDien.MAU_CHU_CHINH);
        b.setEnabled(choPhep);
        if (choPhep) {
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> hanhDong.run());
        }
        return b;
    }


    private static String boDau(String s) {
        String t = Normalizer.normalize(s.toLowerCase(VN), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replace('đ', 'd');
    }

    private void taiDuLieuMau() {
        tatCa.clear();
        // Giá bán tham khảo theo mặt bằng thị trường thú y, lợi nhuận ~25-35% so với giá nhập
        tatCa.add(new VatTuEntity("VT-001", "Amoxicillin 250mg", "Kháng sinh", 340, "Ống", 45000, 58000));
        tatCa.add(new VatTuEntity("VT-002", "Rabisin Vắc-xin Dại", "Vắc-xin", 15, "Lọ", 320000, 420000));
        tatCa.add(new VatTuEntity("VT-003", "Prednisolone 5mg", "Giảm đau", 0, "Viên", 8500, 12000));
        tatCa.add(new VatTuEntity("VT-004", "Fronil Spot-on Dog", "Ký sinh", 85, "Tuýp", 78000, 99000));
        tatCa.add(new VatTuEntity("VT-005", "Bông y tế vô trùng", "Tiêu hao", 60, "Gói", 12000, 16000));
        tatCa.add(new VatTuEntity("VT-006", "Kim tiêm 5ml", "Tiêu hao", 22, "Hộp", 35000, 45000));
        lamMoi();
    }
    
    // --- Các hàm phục vụ phiếu nhập / xuất ---
    
    public int getTonKho(String ma) {
        for (VatTuEntity vt : tatCa) {
            if (vt.getMa().equals(ma)) return vt.getTonKho();
        }
        return 0;
    }

    // Mỗi phần tử: {mã, tên, đơn vị, giá nhập, giá bán}
    public java.util.List<Object[]> getDanhSachVatTu() {
        java.util.List<Object[]> ds = new java.util.ArrayList<>();
        for (VatTuEntity vt : tatCa) {
            ds.add(new Object[]{vt.getMa(), vt.getTen(), vt.getDonVi(), vt.getGiaNhap(), vt.getGiaBan()});
        }
        return ds;
    }

    public long getGiaBan(String ma) {
        for (VatTuEntity vt : tatCa) {
            if (vt.getMa().equals(ma)) return vt.getGiaBan();
        }
        return 0;
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
        private final String[] cot = { "MÃ VT", "TÊN VẬT TƯ", "DANH MỤC", "TỒN KHO", "ĐƠN VỊ", "GIÁ NHẬP", "GIÁ BÁN", "TRẠNG THÁI" };
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
            case 6: return NumberFormat.getInstance(VN).format(vt.getGiaBan()) + " ₫";
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
            } else if (cot == 6) {
                 setForeground(RangBuocGiaoDien.MAU_CHU_DAO);
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