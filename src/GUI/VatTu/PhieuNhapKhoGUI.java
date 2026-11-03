package GUI.VatTu;

import GUI.Chung.RangBuocGiaoDien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Giao diện "Phiếu nhập kho" - SALALA Pet Shop.
 * Danh sách quản lý phiếu nhập kho với tìm kiếm, lọc,
 * các nút Xem / Sửa / Xuất danh sách trên toolbar, lập phiếu mới, xác nhận nhập kho.
 */
public class PhieuNhapKhoGUI extends JPanel {

    // ===== Màu sắc =====
    private static final Color BG          = new Color(0xF8FAFC);
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER      = new Color(0xE5E7EB);
    private static final Color TEXT_DARK   = new Color(0x111827);
    private static final Color TEXT_GRAY   = new Color(0x6B7280);
    private static final Color PRIMARY     = new Color(0x0F6B5A);
    private static final Color PRIMARY_BG  = new Color(0xE6F7F3);
    private static final Color GREEN       = new Color(0x047857);
    private static final Color ORANGE      = new Color(0xC2410C);
    private static final Color RED         = new Color(0xB91C1C);
    private static final Color GREEN_BG    = new Color(0xDCFCE7);
    private static final Color ORANGE_BG   = new Color(0xFEF3C7);
    private static final Color RED_BG      = new Color(0xFEE2E2);
    private static final Color GRAY_BG     = new Color(0xF3F4F6);

    private static final String FONT = "Segoe UI";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    private static final String[] NHA_CUNG_CAP = {
            "Công ty Dược Thú Y VetGroup",
            "Công ty TNHH Thú Y Á Châu",
            "Pharma Pet Việt Nam"
    };

    // =====================================================================
    // Mô hình dữ liệu
    // =====================================================================
    enum TrangThai {
        CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");
        final String text;
        TrangThai(String text) { this.text = text; }
    }

    static class Dong {
        String ma, ten, donVi;
        int sl;
        long donGia;
        Dong(String ma, String ten, String donVi, int sl, long donGia) {
            this.ma = ma; this.ten = ten; this.donVi = donVi; this.sl = sl; this.donGia = donGia;
        }
        long thanhTien() { return (long) sl * donGia; }
    }

    static class Phieu {
        String so, nhanVien, nhaCungCap, ghiChu = "";
        LocalDate ngay;
        TrangThai tt = TrangThai.CHO_XAC_NHAN;
        java.util.List<Dong> dong = new ArrayList<>();

        int tongSL() { int t = 0; for (Dong d : dong) t += d.sl; return t; }
        long tongTien() { long t = 0; for (Dong d : dong) t += d.thanhTien(); return t; }
    }

    // ===== Thành phần =====
    private final Supplier<java.util.List<Object[]>> catalogSupplier;
    private final BiConsumer<String, Integer> stockUpdater;
    private final Runnable onLapPhieuXuat;

    private final java.util.List<Phieu> all = new ArrayList<>();
    private java.util.List<Phieu> filtered = new ArrayList<>();
    private final java.util.List<Phieu> pageRows = new ArrayList<>();

    private LocalDate tuNgay, denNgay;
    private int seq = 42;
    private String nhanVien = "Lê Minh";

    private JTextField txtSearch;
    private RoundButton btnDate;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblShowing;

    public PhieuNhapKhoGUI(Supplier<java.util.List<Object[]>> catalogSupplier,
                           BiConsumer<String, Integer> stockUpdater,
                           Runnable onLapPhieuXuat) {
        this.catalogSupplier = catalogSupplier;
        this.stockUpdater = stockUpdater;
        this.onLapPhieuXuat = onLapPhieuXuat;

        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadSampleData();
        reload(true);
    }

    public void setNhanVien(String ten) { this.nhanVien = ten; }

    // =====================================================================
    // Header
    // =====================================================================
    private JComponent createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Phiếu nhập kho");
        t.setFont(new Font(FONT, Font.BOLD, 18));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel("Tạo mới, xem, sửa và xác nhận phiếu nhập kho cho vật tư");
        s.setFont(new Font(FONT, Font.PLAIN, 12));
        s.setForeground(TEXT_GRAY);
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        RoundButton btnLap = new RoundButton("+ Lập phiếu nhập", PRIMARY, PRIMARY, WHITE);
        RoundButton btnXuat = new RoundButton("+ Lập phiếu xuất", WHITE, PRIMARY, PRIMARY);
        btnLap.addActionListener(e -> lapPhieu());
        btnXuat.addActionListener(e -> {
            if (onLapPhieuXuat != null) onLapPhieuXuat.run();
        });
        right.add(btnLap);
        right.add(btnXuat);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // Khung bảng danh sách phiếu nhập
    // =====================================================================
    private JComponent createTablePanel() {
        RoundedPanel box = new RoundedPanel(12, WHITE, BORDER);
        box.setLayout(new BorderLayout());

        // Thanh lọc
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã phiếu, nhân viên, NCC...");
        txtSearch.setPreferredSize(new Dimension(260, 34));
        txtSearch.setFont(new Font(FONT, Font.PLAIN, 12));
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(() -> reload(true)));

        btnDate = new RoundButton("Ngày lập: Tất cả", WHITE, BORDER, TEXT_DARK);
        btnDate.setFont(new Font(FONT, Font.PLAIN, 12));
        btnDate.setPreferredSize(new Dimension(230, 34));
        btnDate.addActionListener(e -> showDateFilter());

        cboTrangThai = new JComboBox<>(new String[]{
                "Trạng thái: Tất cả", "Trạng thái: Chờ xác nhận", "Trạng thái: Đã xác nhận", "Trạng thái: Đã hủy"});
        cboTrangThai.setFont(new Font(FONT, Font.PLAIN, 12));
        cboTrangThai.setBackground(WHITE);
        cboTrangThai.setPreferredSize(new Dimension(180, 34));
        cboTrangThai.addActionListener(e -> reload(true));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setOpaque(false);
        filterLeft.add(txtSearch);
        filterLeft.add(btnDate);
        filterLeft.add(cboTrangThai);

        RoundButton btnXem = new RoundButton("Xem", WHITE, BORDER, TEXT_DARK);
        RoundButton btnSua = new RoundButton("Sửa", WHITE, BORDER, TEXT_DARK);
        RoundButton btnExport = new RoundButton("⤓  Xuất danh sách", WHITE, BORDER, TEXT_DARK);

        btnXem.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu nhập trong danh sách để xem.", "Chưa chọn phiếu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int mr = table.convertRowIndexToModel(sel);
            if (mr >= 0 && mr < pageRows.size()) xemPhieu(pageRows.get(mr));
        });

        btnSua.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu nhập trong danh sách để sửa.", "Chưa chọn phiếu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int mr = table.convertRowIndexToModel(sel);
            if (mr >= 0 && mr < pageRows.size()) suaPhieu(pageRows.get(mr));
        });

        btnExport.addActionListener(e -> xuatDanhSach());

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRight.setOpaque(false);
        filterRight.add(btnXem);
        filterRight.add(btnSua);
        filterRight.add(btnExport);

        JPanel filter = new JPanel(new BorderLayout());
        filter.setOpaque(false);
        filter.setBorder(new EmptyBorder(14, 16, 14, 16));
        filter.add(filterLeft, BorderLayout.WEST);
        filter.add(filterRight, BorderLayout.EAST);

        // Bảng
        String[] cols = {"Mã phiếu nhập", "Ngày lập", "Nhân viên", "Nhà cung cấp",
                "Số lượng mặt hàng", "Tổng giá trị", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font(FONT, Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xF0F1F3));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xE8F5F2));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(4, 5));

        table.getColumnModel().getColumn(0).setCellRenderer(new Cell(PRIMARY, Font.BOLD, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(1).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(2).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(3).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.LEFT, false));
        table.getColumnModel().getColumn(4).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.RIGHT, false));
        table.getColumnModel().getColumn(5).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, SwingConstants.RIGHT, true));
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        int[] widths = {100, 100, 130, 220, 120, 120, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // Thông tin bên dưới bảng
        lblShowing = new JLabel();
        lblShowing.setFont(new Font(FONT, Font.PLAIN, 12));
        lblShowing.setForeground(TEXT_GRAY);
        JLabel note = new JLabel("Tồn kho chỉ được cập nhật sau khi xác nhận phiếu nhập.");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(TEXT_GRAY);
        JPanel chanTrai = new JPanel();
        chanTrai.setOpaque(false);
        chanTrai.setLayout(new BoxLayout(chanTrai, BoxLayout.Y_AXIS));
        lblShowing.setAlignmentX(LEFT_ALIGNMENT);
        note.setAlignmentX(LEFT_ALIGNMENT);
        chanTrai.add(lblShowing);
        chanTrai.add(Box.createVerticalStrut(2));
        chanTrai.add(note);

        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 16, 10, 12)));
        chan.add(chanTrai, BorderLayout.WEST);

        box.add(filter, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        box.add(chan, BorderLayout.SOUTH);
        return box;
    }

    // =====================================================================
    // Dữ liệu & Lọc
    // =====================================================================
    private void loadSampleData() {
        Phieu p42 = new Phieu();
        p42.so = "PN-2025-0042";
        p42.ngay = LocalDate.of(2025, 7, 5);
        p42.nhanVien = "Lê Minh";
        p42.nhaCungCap = "Công ty Dược Thú Y VetGroup";
        p42.ghiChu = "Nhập bổ sung vắc-xin và kháng sinh tháng 7/2025";
        p42.dong.add(new Dong("VT-001", "Amoxicillin 250mg", "Ống", 100, 45000));
        p42.dong.add(new Dong("VT-002", "Rabisin Vắc-xin Dại", "Lọ", 30, 320000));
        p42.dong.add(new Dong("VT-006", "Kim tiêm 5ml", "Hộp", 10, 35000));

        Phieu p41 = new Phieu();
        p41.so = "PN-2025-0041";
        p41.ngay = LocalDate.of(2025, 6, 20);
        p41.nhanVien = "Lê Minh";
        p41.nhaCungCap = "Công ty TNHH Thú Y Á Châu";
        p41.ghiChu = "Bổ sung thuốc giảm đau";
        p41.dong.add(new Dong("VT-003", "Prednisolone 5mg", "Viên", 200, 8500));
        p41.tt = TrangThai.DA_XAC_NHAN;

        all.add(p42);
        all.add(p41);
    }

    private void reload(boolean resetPage) {
        String kw = txtSearch.getText().trim().toLowerCase();
        int stIdx = cboTrangThai.getSelectedIndex();
        TrangThai[] stMap = {null, TrangThai.CHO_XAC_NHAN, TrangThai.DA_XAC_NHAN, TrangThai.DA_HUY};

        filtered = new ArrayList<>();
        for (Phieu p : all) {
            if (!kw.isEmpty()
                    && !p.so.toLowerCase().contains(kw)
                    && !p.nhanVien.toLowerCase().contains(kw)
                    && !p.nhaCungCap.toLowerCase().contains(kw)) continue;
            if (stIdx > 0 && p.tt != stMap[stIdx]) continue;
            if (tuNgay != null && p.ngay.isBefore(tuNgay)) continue;
            if (denNgay != null && p.ngay.isAfter(denNgay)) continue;
            filtered.add(p);
        }

        model.setRowCount(0);
        pageRows.clear();
        for (int i = 0; i < filtered.size(); i++) {
            Phieu p = filtered.get(i);
            pageRows.add(p);
            model.addRow(new Object[]{
                    p.so,
                    p.ngay.format(DF),
                    p.nhanVien,
                    p.nhaCungCap,
                    p.dong.size(),
                    p.tongTien(),
                    p.tt.text
            });
        }

        lblShowing.setText(filtered.isEmpty()
                ? "Không có phiếu nhập nào"
                : "Hiển thị tất cả " + filtered.size() + " phiếu nhập");
    }

    private void showDateFilter() {
        JTextField from = new JTextField(tuNgay == null ? "" : tuNgay.format(DF), 10);
        JTextField to = new JTextField(denNgay == null ? "" : denNgay.format(DF), 10);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Từ ngày (dd/MM/yyyy)"));
        p.add(from);
        p.add(new JLabel("Đến ngày (dd/MM/yyyy)"));
        p.add(to);
        p.add(new JLabel(""));
        p.add(new JLabel("Để trống = không giới hạn"));
        int ok = JOptionPane.showConfirmDialog(this, p, "Lọc theo ngày lập",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            LocalDate f = from.getText().trim().isEmpty() ? null : LocalDate.parse(from.getText().trim(), DF);
            LocalDate t = to.getText().trim().isEmpty() ? null : LocalDate.parse(to.getText().trim(), DF);
            if (f != null && t != null && f.isAfter(t)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            tuNgay = f;
            denNgay = t;
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (định dạng dd/MM/yyyy).", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text;
        if (tuNgay == null && denNgay == null) text = "Ngày lập: Tất cả";
        else if (tuNgay != null && denNgay != null) text = "Ngày lập: " + tuNgay.format(DF) + " – " + denNgay.format(DF);
        else if (tuNgay != null) text = "Ngày lập: từ " + tuNgay.format(DF);
        else text = "Ngày lập: đến " + denNgay.format(DF);
        btnDate.setText(text);
        reload(true);
    }

    // =====================================================================
    // Chức năng Xem, Sửa, Lập, Xác nhận, Hủy, In/Xuất
    // =====================================================================
    private void lapPhieu() {
        Phieu p = openEditor(null);
        if (p != null) {
            all.add(0, p);
            reload(true);
        }
    }

    private void xemPhieu(Phieu p) {
        openDetail(p);
    }

    private void suaPhieu(Phieu p) {
        if (p.tt != TrangThai.CHO_XAC_NHAN) {
            JOptionPane.showMessageDialog(this, "Chỉ sửa được phiếu đang ở trạng thái Chờ xác nhận.", "Không thể sửa",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Phieu np = openEditor(p);
        if (np != null) {
            p.ngay = np.ngay;
            p.nhaCungCap = np.nhaCungCap;
            p.ghiChu = np.ghiChu;
            p.dong = np.dong;
            reload(false);
        }
    }

    private void openDetail(Phieu p) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Chi tiết phiếu nhập: " + p.so, Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(720, 520);
        d.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.setBackground(WHITE);

        JPanel info = new JPanel(new GridLayout(2, 2, 16, 8));
        info.setOpaque(false);
        info.add(detailItem("Số phiếu", p.so));
        info.add(detailItem("Ngày lập", p.ngay.format(DF)));
        info.add(detailItem("Nhân viên lập", p.nhanVien));
        info.add(detailItem("Nhà cung cấp", p.nhaCungCap));

        String[] cols = {"Tên vật tư", "Đơn vị", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Dong dItem : p.dong) {
            m.addRow(new Object[]{dItem.ten, dItem.donVi, dItem.sl, MONEY.format(dItem.donGia) + " đ", MONEY.format(dItem.thanhTien()) + " đ"});
        }
        JTable tbl = new JTable(m);
        tbl.setRowHeight(32);
        tbl.setFont(new Font(FONT, Font.PLAIN, 12));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel lblSum = new JLabel("Tổng số lượng: " + p.tongSL() + "    Tổng giá trị: " + MONEY.format(p.tongTien()) + " đ");
        lblSum.setFont(new Font(FONT, Font.BOLD, 13));
        lblSum.setForeground(PRIMARY);
        bottom.add(lblSum, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.setOpaque(false);

        RoundButton btnXN = new RoundButton("Xác nhận & Cập nhật tồn", PRIMARY, PRIMARY, WHITE);
        RoundButton btnHuyP = new RoundButton("Hủy phiếu", WHITE, new Color(0xFCA5A5), RED);
        RoundButton btnIn = new RoundButton("In / Xuất", WHITE, BORDER, TEXT_DARK);
        RoundButton btnDong = new RoundButton("Đóng", WHITE, BORDER, TEXT_DARK);

        boolean cho = p.tt == TrangThai.CHO_XAC_NHAN;
        btnXN.setEnabled(cho);
        btnHuyP.setEnabled(cho);

        btnXN.addActionListener(e -> {
            if (p.dong.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Phiếu chưa có vật tư nào.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(d,
                    "Xác nhận phiếu nhập " + p.so + " và cộng " + p.tongSL() + " đơn vị vào tồn kho?",
                    "Xác nhận nhập kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;

            for (Dong dItem : p.dong) {
                stockUpdater.accept(dItem.ma, dItem.sl);
            }
            p.tt = TrangThai.DA_XAC_NHAN;
            reload(false);
            JOptionPane.showMessageDialog(d, "Đã xác nhận phiếu nhập và cập nhật tồn kho thành công.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });

        btnHuyP.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(d,
                    "Hủy phiếu nhập " + p.so + "? Thao tác này không thể hoàn tác.",
                    "Hủy phiếu nhập", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
            p.tt = TrangThai.DA_HUY;
            reload(false);
            d.dispose();
        });

        btnIn.addActionListener(e -> xuatIn(p, d));
        btnDong.addActionListener(e -> d.dispose());

        btnBar.add(btnXN);
        btnBar.add(btnHuyP);
        btnBar.add(btnIn);
        btnBar.add(btnDong);

        content.add(info, BorderLayout.NORTH);
        content.add(new JScrollPane(tbl), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        d.setLayout(new BorderLayout());
        d.add(content, BorderLayout.CENTER);
        d.add(btnBar, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JPanel detailItem(String label, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT, Font.PLAIN, 11));
        l.setForeground(TEXT_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font(FONT, Font.BOLD, 12));
        v.setForeground(TEXT_DARK);
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private Phieu openEditor(Phieu src) {
        final java.util.List<Object[]> catalog = catalogSupplier.get();
        final Phieu[] result = {null};

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                src == null ? "Lập phiếu nhập" : "Sửa phiếu nhập", Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(720, 560);
        d.setLocationRelativeTo(this);

        JTextField txtSo = new JTextField(src == null
                ? String.format("PN-%d-%04d", LocalDate.now().getYear(), ++seq) : src.so);
        txtSo.setEditable(false);
        JTextField txtNgay = new JTextField((src == null ? LocalDate.now() : src.ngay).format(DF));
        JTextField txtNV = new JTextField(src == null ? nhanVien : src.nhanVien);
        txtNV.setEditable(false);
        JComboBox<String> cboNCC = new JComboBox<>(NHA_CUNG_CAP);
        cboNCC.setEditable(true);
        if (src != null) cboNCC.setSelectedItem(src.nhaCungCap);
        JTextField txtGhiChu = new JTextField(src == null ? "" : src.ghiChu);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 6, 14));
        form.setBackground(WHITE);
        addFormRow(form, 0, "Số phiếu", txtSo);
        addFormRow(form, 1, "Ngày lập (dd/MM/yyyy)", txtNgay);
        addFormRow(form, 2, "Nhân viên lập", txtNV);
        addFormRow(form, 3, "Nhà cung cấp", cboNCC);
        addFormRow(form, 4, "Ghi chú", txtGhiChu);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Mã VT", "Tên vật tư", "Đơn vị", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3 || c == 4; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : (c >= 4 ? Long.class : String.class);
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(28);
        t.setFont(new Font(FONT, Font.PLAIN, 12));

        m.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 3 || e.getColumn() == 4)) {
                int r = e.getFirstRow();
                if (r >= 0 && r < m.getRowCount()) {
                    int sl = ((Number) m.getValueAt(r, 3)).intValue();
                    long gia = ((Number) m.getValueAt(r, 4)).longValue();
                    m.setValueAt((long) sl * gia, r, 5);
                }
            }
        });

        if (src != null) {
            for (Dong dg : src.dong) {
                m.addRow(new Object[]{dg.ma, dg.ten, dg.donVi, dg.sl, dg.donGia, dg.thanhTien()});
            }
        }

        JComboBox<String> cboVT = new JComboBox<>();
        for (Object[] r : catalog) cboVT.addItem(r[0] + " - " + r[1]);
        JSpinner spSL = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        JTextField txtGia = new JTextField(9);
        Runnable fillGia = () -> {
            int i = cboVT.getSelectedIndex();
            if (i >= 0 && i < catalog.size()) txtGia.setText(String.valueOf(catalog.get(i)[3]));
        };
        cboVT.addActionListener(e -> fillGia.run());
        if (catalog.size() > 0) fillGia.run();

        JButton btnThem = new JButton("Thêm vật tư");
        JButton btnXoa = new JButton("Xóa dòng");
        btnThem.addActionListener(e -> {
            int i = cboVT.getSelectedIndex();
            if (i < 0 || i >= catalog.size()) return;
            long gia;
            try {
                gia = Long.parseLong(txtGia.getText().trim().replace(".", "").replace(",", ""));
                if (gia < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Đơn giá không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sl = (Integer) spSL.getValue();
            Object[] vt = catalog.get(i);
            for (int r = 0; r < m.getRowCount(); r++) {
                if (vt[0].equals(m.getValueAt(r, 0))) {
                    int slMoi = ((Number) m.getValueAt(r, 3)).intValue() + sl;
                    m.setValueAt(slMoi, r, 3);
                    m.setValueAt(gia, r, 4);
                    m.setValueAt((long) slMoi * gia, r, 5);
                    return;
                }
            }
            m.addRow(new Object[]{vt[0], vt[1], vt[2], sl, gia, (long) sl * gia});
        });
        btnXoa.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            int r = t.getSelectedRow();
            if (r >= 0) m.removeRow(r);
        });

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addBar.setOpaque(false);
        addBar.add(cboVT);
        addBar.add(new JLabel("SL"));
        addBar.add(spSL);
        addBar.add(new JLabel("Đơn giá"));
        addBar.add(txtGia);
        addBar.add(btnThem);
        addBar.add(btnXoa);

        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.setBorder(new EmptyBorder(0, 14, 0, 14));
        mid.add(addBar, BorderLayout.NORTH);
        mid.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton btnLuu = new RoundButton(src == null ? "Lập phiếu" : "Lưu thay đổi", PRIMARY, PRIMARY, WHITE);
        JButton btnHuy = new RoundButton("Hủy", WHITE, BORDER, TEXT_DARK);
        btnHuy.addActionListener(e -> d.dispose());
        btnLuu.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            LocalDate ngay;
            try {
                ngay = LocalDate.parse(txtNgay.getText().trim(), DF);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(d, "Ngày lập không hợp lệ (định dạng dd/MM/yyyy).", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object nccObj = cboNCC.getEditor().getItem();
            String ncc = nccObj == null ? "" : nccObj.toString().trim();
            if (ncc.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Vui lòng nhập nhà cung cấp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (m.getRowCount() == 0) {
                JOptionPane.showMessageDialog(d, "Phiếu cần có ít nhất một vật tư.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Phieu p = src == null ? new Phieu() : src;
            p.so = txtSo.getText().trim();
            p.ngay = ngay;
            p.nhanVien = txtNV.getText().trim();
            p.nhaCungCap = ncc;
            p.ghiChu = txtGhiChu.getText().trim();
            p.dong.clear();
            for (int r = 0; r < m.getRowCount(); r++) {
                String ma = (String) m.getValueAt(r, 0);
                String ten = (String) m.getValueAt(r, 1);
                String dv = (String) m.getValueAt(r, 2);
                int sl = ((Number) m.getValueAt(r, 3)).intValue();
                long gia = ((Number) m.getValueAt(r, 4)).longValue();
                p.dong.add(new Dong(ma, ten, dv, sl, gia));
            }
            result[0] = p;
            d.dispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setOpaque(false);
        btns.add(btnHuy);
        btns.add(btnLuu);

        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.NORTH);
        d.add(mid, BorderLayout.CENTER);
        d.add(btns, BorderLayout.SOUTH);
        d.setVisible(true);

        return result[0];
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(4, 0, 4, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(FONT, Font.PLAIN, 12));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        comp.setFont(new Font(FONT, Font.PLAIN, 12));
        form.add(comp, gc);
    }

    private void xuatDanhSach() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh-sach-phieu-nhap-kho.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Mã phiếu nhập,Ngày lập,Nhân viên,Nhà cung cấp,Số lượng mặt hàng,Tổng giá trị,Trạng thái\r\n");
        for (Phieu p : filtered) {
            sb.append(csv(p.so)).append(',')
              .append(csv(p.ngay.format(DF))).append(',')
              .append(csv(p.nhanVien)).append(',')
              .append(csv(p.nhaCungCap)).append(',')
              .append(p.dong.size()).append(',')
              .append(p.tongTien()).append(',')
              .append(csv(p.tt.text)).append("\r\n");
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " phiếu ra file.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }

    private void xuatIn(Phieu p, Window owner) {
        final String text = buildText(p);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(10, 12, 10, 12));

        JDialog d = new JDialog(owner, "Phiếu " + p.so, Dialog.ModalityType.APPLICATION_MODAL);
        JButton btnPrint = new JButton("In");
        JButton btnSave = new JButton("Lưu file .txt");
        JButton btnClose = new JButton("Đóng");
        btnPrint.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(d, "Không in được: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnSave.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(p.so + ".txt"));
            if (fc.showSaveDialog(d) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(fc.getSelectedFile().toPath(), text.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(d, "Không lưu được file: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnClose.addActionListener(e -> d.dispose());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bar.add(btnPrint);
        bar.add(btnSave);
        bar.add(btnClose);

        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(area), BorderLayout.CENTER);
        d.add(bar, BorderLayout.SOUTH);
        d.setSize(680, 480);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    private String buildText(Phieu p) {
        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(78) + "\n";
        sb.append("SALALA PET SHOP\n");
        sb.append("PHIẾU NHẬP KHO\n");
        sb.append(line);
        sb.append("Số phiếu     : ").append(p.so).append('\n');
        sb.append("Ngày lập     : ").append(p.ngay.format(DF)).append('\n');
        sb.append("Nhân viên lập: ").append(p.nhanVien).append('\n');
        sb.append("Nhà cung cấp : ").append(p.nhaCungCap).append('\n');
        sb.append("Ghi chú      : ").append(p.ghiChu == null ? "" : p.ghiChu).append('\n');
        sb.append("Trạng thái   : ").append(p.tt.text).append('\n');
        sb.append(line);
        sb.append(String.format("%-28s %-7s %5s %14s %17s%n", "Tên vật tư", "ĐVT", "SL", "Đơn giá", "Thành tiền"));
        sb.append(line);
        for (Dong d : p.dong) {
            sb.append(String.format("%-28s %-7s %5d %14s %17s%n", d.ten, d.donVi, d.sl,
                    MONEY.format(d.donGia) + " đ", MONEY.format(d.thanhTien()) + " đ"));
        }
        sb.append(line);
        sb.append(String.format("%-28s %-7s %5d %14s %17s%n", "TỔNG CỘNG", "", p.tongSL(), "",
                MONEY.format(p.tongTien()) + " đ"));
        return sb.toString();
    }

    // =====================================================================
    // Renderers & Component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private final java.util.Set<Integer> phai = new java.util.HashSet<>();
        HeaderRenderer(int... cotPhai) { for (int c : cotPhai) phai.add(c); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(phai.contains(c) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(new Font(FONT, Font.BOLD, 10));
            setForeground(TEXT_GRAY);
            setBackground(new Color(0xF9FAFB));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class Cell extends DefaultTableCellRenderer {
        private final Color color;
        private final int style, align;
        private final boolean money;
        Cell(Color color, int style, int align, boolean money) {
            this.color = color; this.style = style; this.align = align; this.money = money;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setHorizontalAlignment(align);
            setFont(new Font(FONT, style, 12));
            setForeground(color);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (money && v instanceof Number) setText(MONEY.format(((Number) v).longValue()) + " đ");
            return this;
        }
    }

    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Badge badge = new Badge();

        StatusRenderer() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.insets = new Insets(0, 8, 0, 0);
            panel.add(badge, gc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();
            badge.setText("● " + s);
            if ("Đã xác nhận".equals(s)) badge.setColors(GREEN_BG, GREEN);
            else if ("Đã hủy".equals(s)) badge.setColors(RED_BG, RED);
            else badge.setColors(ORANGE_BG, ORANGE);
            panel.setOpaque(true);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    private static class Badge extends JLabel {
        private Color bg = ORANGE_BG;
        Badge() {
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 11));
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }
        void setColors(Color bg, Color fg) { this.bg = bg; setForeground(fg); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundButton extends JButton {
        private final Color fill, line, fg;
        RoundButton(String text, Color fill, Color line, Color fg) {
            super(text);
            this.fill = fill; this.line = line; this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font(FONT, Font.BOLD, 12));
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }
        @Override public void setEnabled(boolean b) {
            super.setEnabled(b);
            setForeground(b ? fg : new Color(0x9CA3AF));
            setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean en = isEnabled();
            g2.setColor(en ? fill : GRAY_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(en ? line : BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;
        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc; this.fill = RangBuocGiaoDien.theMo(fill); this.line = line;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
