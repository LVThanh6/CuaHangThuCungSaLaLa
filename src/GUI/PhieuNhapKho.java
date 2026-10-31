package GUI;

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
 * Chức năng: xem phiếu, lập phiếu mới, sửa phiếu, xác nhận & cập nhật tồn kho,
 *            hủy phiếu, xuất / in phiếu.
 * (Lưu file với encoding UTF-8 để hiển thị đúng tiếng Việt.)
 */
public class PhieuNhapKho extends JPanel {

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
    private static final Color NOTE_BG     = new Color(0xFFFBEB);

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
        long thanhTien() { return sl * donGia; }
    }

    static class Phieu {
        String so, nhanVien, nhaCungCap, ghiChu = "";
        LocalDate ngay;
        TrangThai tt = TrangThai.CHO_XAC_NHAN;
        java.util.List<Dong> dong = new ArrayList<>();

        int tongSL() { int t = 0; for (Dong d : dong) t += d.sl; return t; }
        long tongTien() { long t = 0; for (Dong d : dong) t += d.thanhTien(); return t; }
        @Override public String toString() { return so + "  ·  " + tt.text; }
    }

    // ===== Thành phần =====
    private final Supplier<java.util.List<Object[]>> catalogSupplier; // {ma, ten, donVi, giaNhap}
    private final BiConsumer<String, Integer> stockUpdater;           // (maVT, soLuongThem)
    private final Runnable onLapPhieuXuat;

    private final DefaultComboBoxModel<Phieu> phieuModel = new DefaultComboBoxModel<>();
    private JComboBox<Phieu> cboPhieu;
    private JLabel lblSo, lblNgay, lblNhanVien, lblNCC, lblGhiChu;
    private JLabel lblTongSL, lblTongTien;
    private Badge badge;
    private JTable table;
    private DefaultTableModel model;
    private RoundButton btnXacNhan, btnSua, btnIn, btnHuy;

    private Phieu current;
    private boolean updating = false;
    private int seq = 42;                 // số thứ tự phiếu gần nhất
    private String nhanVien = "Lê Minh";  // nhân viên đang đăng nhập

    /**
     * @param catalogSupplier danh sách vật tư hiện có: mỗi phần tử {ma, ten, donVi, giaNhap}
     * @param stockUpdater    được gọi khi xác nhận phiếu: (mã vật tư, số lượng cộng thêm)
     * @param onLapPhieuXuat  được gọi khi bấm "+ Lập phiếu xuất" (có thể null)
     */
    public PhieuNhapKho(Supplier<java.util.List<Object[]>> catalogSupplier,
                        BiConsumer<String, Integer> stockUpdater,
                        Runnable onLapPhieuXuat) {
        this.catalogSupplier = catalogSupplier;
        this.stockUpdater = stockUpdater;
        this.onLapPhieuXuat = onLapPhieuXuat;

        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setOpaque(true);

        add(createHeader(), BorderLayout.NORTH);
        add(createCard(), BorderLayout.CENTER);

        loadSampleData();
    }

    public void setNhanVien(String ten) { this.nhanVien = ten; }

    // =====================================================================
    // Header: tiêu đề + 2 nút
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
        JLabel s = new JLabel("Tạo mới, chỉnh sửa và xác nhận phiếu nhập kho cho vật tư");
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
    // Khung chi tiết phiếu
    // =====================================================================
    private JComponent createCard() {
        RoundedPanel card = new RoundedPanel(12, WHITE, BORDER);
        card.setLayout(new BorderLayout());

        // ---------- phần đầu: tiêu đề + thông tin ----------
        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(14, 18, 0, 18));

        // dòng tiêu đề
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Phiếu nhập kho — Đang xem");
        t1.setFont(new Font(FONT, Font.BOLD, 13));
        t1.setForeground(TEXT_DARK);
        JLabel t2 = new JLabel("Chọn phiếu ở góc phải để xem / chỉnh sửa");
        t2.setFont(new Font(FONT, Font.PLAIN, 11));
        t2.setForeground(TEXT_GRAY);
        t1.setAlignmentX(LEFT_ALIGNMENT);
        t2.setAlignmentX(LEFT_ALIGNMENT);
        titles.add(t1);
        titles.add(t2);

        badge = new Badge();
        cboPhieu = new JComboBox<>(phieuModel);
        cboPhieu.setFont(new Font(FONT, Font.PLAIN, 12));
        cboPhieu.setBackground(WHITE);
        cboPhieu.setPreferredSize(new Dimension(210, 28));
        cboPhieu.addActionListener(e -> {
            if (!updating) showPhieu((Phieu) cboPhieu.getSelectedItem());
        });
        JPanel pick = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pick.setOpaque(false);
        pick.add(badge);
        pick.add(cboPhieu);

        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(pick, BorderLayout.EAST);

        // lưới thông tin
        lblSo = new JLabel();
        lblNgay = new JLabel();
        lblNhanVien = new JLabel();
        lblNCC = new JLabel();
        lblGhiChu = new JLabel();

        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 10));
        grid.setOpaque(false);
        grid.add(field("Số phiếu", lblSo, TEXT_DARK));
        grid.add(field("Ngày lập", lblNgay, TEXT_DARK));
        grid.add(field("Nhân viên lập", lblNhanVien, TEXT_DARK));
        grid.add(field("Nhà cung cấp", lblNCC, PRIMARY));

        JPanel info = new JPanel(new BorderLayout(0, 10));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 0, 12, 0)));
        info.add(grid, BorderLayout.NORTH);
        info.add(field("Ghi chú", lblGhiChu, TEXT_DARK), BorderLayout.CENTER);

        north.add(titleRow, BorderLayout.NORTH);
        north.add(info, BorderLayout.CENTER);

        // ---------- bảng chi tiết ----------
        String[] cols = {"TÊN VẬT TƯ", "ĐƠN VỊ", "SL", "ĐƠN GIÁ", "THÀNH TIỀN"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(34);
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
        h.setPreferredSize(new Dimension(0, 32));
        h.setDefaultRenderer(new HeaderRenderer());

        table.getColumnModel().getColumn(0).setCellRenderer(new Cell(TEXT_DARK, Font.BOLD, false));
        table.getColumnModel().getColumn(1).setCellRenderer(new Cell(TEXT_GRAY, Font.PLAIN, false));
        table.getColumnModel().getColumn(2).setCellRenderer(new Cell(PRIMARY, Font.BOLD, false));
        table.getColumnModel().getColumn(3).setCellRenderer(new Cell(TEXT_DARK, Font.PLAIN, true));
        table.getColumnModel().getColumn(4).setCellRenderer(new Cell(TEXT_DARK, Font.BOLD, true));
        int[] widths = {260, 80, 70, 120, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);

        // dòng tổng cộng
        lblTongSL = new JLabel();
        lblTongTien = new JLabel();
        JLabel lblTc = new JLabel("TỔNG CỘNG");
        lblTc.setFont(new Font(FONT, Font.BOLD, 12));
        lblTc.setForeground(PRIMARY);
        lblTongSL.setFont(new Font(FONT, Font.BOLD, 12));
        lblTongSL.setForeground(PRIMARY);
        lblTongTien.setFont(new Font(FONT, Font.BOLD, 13));
        lblTongTien.setForeground(PRIMARY);

        JPanel totalRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 28, 0));
        totalRight.setOpaque(false);
        totalRight.add(lblTongSL);
        totalRight.add(lblTongTien);
        JPanel total = new JPanel(new BorderLayout());
        total.setBackground(PRIMARY_BG);
        total.setBorder(new EmptyBorder(9, 12, 9, 8));
        total.add(lblTc, BorderLayout.WEST);
        total.add(totalRight, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(sp, BorderLayout.CENTER);
        center.add(total, BorderLayout.SOUTH);

        // ---------- nút thao tác + lưu ý ----------
        btnXacNhan = new RoundButton("Xác nhận & Cập nhật tồn", PRIMARY, PRIMARY, WHITE);
        btnSua = new RoundButton("Sửa phiếu", WHITE, BORDER, TEXT_DARK);
        btnIn = new RoundButton("Xuất / In phiếu", WHITE, BORDER, TEXT_DARK);
        btnHuy = new RoundButton("Hủy nhập", WHITE, new Color(0xFCA5A5), RED);
        btnXacNhan.addActionListener(e -> xacNhan());
        btnSua.addActionListener(e -> suaPhieu());
        btnIn.addActionListener(e -> xuatIn());
        btnHuy.addActionListener(e -> huyPhieu());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(12, 10, 12, 10));
        actions.add(btnXacNhan);
        actions.add(btnSua);
        actions.add(btnIn);
        actions.add(btnHuy);

        JLabel note = new JLabel("ⓘ  Tồn kho chỉ được cập nhật sau khi xác nhận. Phiếu nhập không ảnh hưởng đến số liệu tồn. "
                + "Không được xuất quá số lượng tồn hiện tại.");
        note.setFont(new Font(FONT, Font.PLAIN, 11));
        note.setForeground(ORANGE);
        JPanel noteBox = new JPanel(new BorderLayout());
        noteBox.setBackground(NOTE_BG);
        noteBox.setBorder(new EmptyBorder(9, 18, 9, 18));
        noteBox.add(note, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(actions, BorderLayout.NORTH);
        south.add(noteBox, BorderLayout.SOUTH);

        card.add(north, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JComponent field(String caption, JLabel value, Color color) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel c = new JLabel(caption);
        c.setFont(new Font(FONT, Font.PLAIN, 11));
        c.setForeground(TEXT_GRAY);
        value.setFont(new Font(FONT, Font.BOLD, 12));
        value.setForeground(color);
        c.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c);
        p.add(Box.createVerticalStrut(2));
        p.add(value);
        return p;
    }

    // =====================================================================
    // Dữ liệu mẫu & hiển thị
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

        updating = true;
        phieuModel.addElement(p42);
        phieuModel.addElement(p41);
        updating = false;
        chonPhieu(p42);
    }

    private void chonPhieu(Phieu p) {
        updating = true;
        cboPhieu.setSelectedItem(p);
        updating = false;
        showPhieu(p);
    }

    private void showPhieu(Phieu p) {
        current = p;
        model.setRowCount(0);
        if (p == null) {
            lblSo.setText(""); lblNgay.setText(""); lblNhanVien.setText("");
            lblNCC.setText(""); lblGhiChu.setText("");
            lblTongSL.setText(""); lblTongTien.setText("");
            badge.setText("");
            btnXacNhan.setEnabled(false); btnSua.setEnabled(false);
            btnIn.setEnabled(false); btnHuy.setEnabled(false);
            return;
        }
        lblSo.setText(p.so);
        lblNgay.setText(p.ngay.format(DF));
        lblNhanVien.setText(p.nhanVien);
        lblNCC.setText(p.nhaCungCap);
        lblGhiChu.setText(p.ghiChu == null || p.ghiChu.isEmpty() ? "—" : p.ghiChu);

        for (Dong d : p.dong) {
            model.addRow(new Object[]{d.ten, d.donVi, d.sl, d.donGia, d.thanhTien()});
        }
        lblTongSL.setText(String.valueOf(p.tongSL()));
        lblTongTien.setText(MONEY.format(p.tongTien()) + " đ");

        badge.setText(p.tt.text);
        switch (p.tt) {
            case DA_XAC_NHAN: badge.setColors(GREEN_BG, GREEN); break;
            case DA_HUY:      badge.setColors(RED_BG, RED); break;
            default:          badge.setColors(ORANGE_BG, ORANGE); break;
        }

        boolean cho = p.tt == TrangThai.CHO_XAC_NHAN;
        btnXacNhan.setEnabled(cho);
        btnSua.setEnabled(cho);
        btnHuy.setEnabled(cho);
        btnIn.setEnabled(true);
        cboPhieu.repaint();
    }

    // =====================================================================
    // Chức năng
    // =====================================================================
    /** Lập phiếu mới. */
    private void lapPhieu() {
        Phieu p = openEditor(null);
        if (p != null) {
            phieuModel.addElement(p);
            chonPhieu(p);
        }
    }

    /** Sửa phiếu đang xem (chỉ khi chờ xác nhận). */
    private void suaPhieu() {
        if (current == null || current.tt != TrangThai.CHO_XAC_NHAN) return;
        Phieu p = openEditor(current);
        if (p != null) {
            current.ngay = p.ngay;
            current.nhaCungCap = p.nhaCungCap;
            current.ghiChu = p.ghiChu;
            current.dong = p.dong;
            showPhieu(current);
        }
    }

    /** Xác nhận phiếu -> cộng số lượng vào tồn kho. */
    private void xacNhan() {
        if (current == null || current.tt != TrangThai.CHO_XAC_NHAN) return;
        if (current.dong.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu chưa có vật tư nào.", "Không thể xác nhận",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xác nhận phiếu " + current.so + " và cộng " + current.tongSL() + " đơn vị vào tồn kho?",
                "Xác nhận nhập kho", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        for (Dong d : current.dong) {
            stockUpdater.accept(d.ma, d.sl);
        }
        current.tt = TrangThai.DA_XAC_NHAN;
        showPhieu(current);
        JOptionPane.showMessageDialog(this, "Đã xác nhận phiếu và cập nhật tồn kho.", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Hủy phiếu (chỉ khi chờ xác nhận) - tồn kho không thay đổi. */
    private void huyPhieu() {
        if (current == null || current.tt != TrangThai.CHO_XAC_NHAN) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Hủy phiếu nhập " + current.so + "? Thao tác này không thể hoàn tác.",
                "Hủy phiếu nhập", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        current.tt = TrangThai.DA_HUY;
        showPhieu(current);
    }

    /** Xem trước, in hoặc lưu phiếu ra file .txt. */
    private void xuatIn() {
        if (current == null) return;
        final String text = buildText(current);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(10, 12, 10, 12));

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Phiếu " + current.so, Dialog.ModalityType.APPLICATION_MODAL);
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
            fc.setSelectedFile(new File(current.so + ".txt"));
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
        d.setLocationRelativeTo(this);
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
    // Hộp thoại lập / sửa phiếu
    // =====================================================================
    /** @param src null = lập mới; khác null = sửa. Trả về phiếu đã nhập, hoặc null nếu hủy. */
    private Phieu openEditor(Phieu src) {
        final java.util.List<Object[]> catalog = catalogSupplier.get();
        final Phieu[] result = {null};

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                src == null ? "Lập phiếu nhập" : "Sửa phiếu nhập", Dialog.ModalityType.APPLICATION_MODAL);

        // ---- thông tin chung ----
        JTextField txtSo = new JTextField(src == null
                ? String.format("PN-%d-%04d", LocalDate.now().getYear(), seq + 1) : src.so);
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
        addFormRow(form, 0, "Số phiếu", txtSo);
        addFormRow(form, 1, "Ngày lập (dd/MM/yyyy)", txtNgay);
        addFormRow(form, 2, "Nhân viên lập", txtNV);
        addFormRow(form, 3, "Nhà cung cấp", cboNCC);
        addFormRow(form, 4, "Ghi chú", txtGhiChu);

        // ---- bảng vật tư của phiếu ----
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
        t.getTableHeader().setReorderingAllowed(false);
        t.getColumnModel().getColumn(0).setPreferredWidth(70);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);
        t.getColumnModel().getColumn(2).setPreferredWidth(60);
        t.getColumnModel().getColumn(3).setPreferredWidth(60);
        t.getColumnModel().getColumn(4).setPreferredWidth(100);
        t.getColumnModel().getColumn(5).setPreferredWidth(110);

        JLabel lblTong = new JLabel();
        lblTong.setFont(new Font(FONT, Font.BOLD, 13));
        lblTong.setForeground(PRIMARY);
        Runnable capNhatTong = () -> {
            int sl = 0; long tien = 0;
            for (int r = 0; r < m.getRowCount(); r++) {
                sl += ((Number) m.getValueAt(r, 3)).intValue();
                tien += ((Number) m.getValueAt(r, 5)).longValue();
            }
            lblTong.setText("Tổng SL: " + sl + "     Tổng tiền: " + MONEY.format(tien) + " đ");
        };
        m.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 3 || e.getColumn() == 4)) {
                int r = e.getFirstRow();
                if (r >= 0 && r < m.getRowCount()) {
                    int sl = ((Number) m.getValueAt(r, 3)).intValue();
                    long gia = ((Number) m.getValueAt(r, 4)).longValue();
                    m.setValueAt(sl * gia, r, 5);
                }
            }
            capNhatTong.run();
        });

        if (src != null) {
            for (Dong dg : src.dong) {
                m.addRow(new Object[]{dg.ma, dg.ten, dg.donVi, dg.sl, dg.donGia, dg.thanhTien()});
            }
        }
        capNhatTong.run();

        // ---- thanh thêm vật tư ----
        JComboBox<String> cboVT = new JComboBox<>();
        for (Object[] r : catalog) cboVT.addItem(r[0] + " - " + r[1]);
        JSpinner spSL = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        JTextField txtGia = new JTextField(9);
        Runnable fillGia = () -> {
            int i = cboVT.getSelectedIndex();
            if (i >= 0) txtGia.setText(String.valueOf(catalog.get(i)[3]));
        };
        cboVT.addActionListener(e -> fillGia.run());
        fillGia.run();

        JButton btnThem = new JButton("Thêm vào phiếu");
        JButton btnXoa = new JButton("Xóa dòng chọn");
        btnThem.addActionListener(e -> {
            int i = cboVT.getSelectedIndex();
            if (i < 0) return;
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
            for (int r = 0; r < m.getRowCount(); r++) {           // trùng mã -> cộng dồn
                if (vt[0].equals(m.getValueAt(r, 0))) {
                    m.setValueAt(((Number) m.getValueAt(r, 3)).intValue() + sl, r, 3);
                    m.setValueAt(gia, r, 4);
                    return;
                }
            }
            m.addRow(new Object[]{vt[0], vt[1], vt[2], sl, gia, sl * gia});
        });
        btnXoa.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            int r = t.getSelectedRow();
            if (r >= 0) m.removeRow(r);
        });

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addBar.add(cboVT);
        addBar.add(new JLabel("SL"));
        addBar.add(spSL);
        addBar.add(new JLabel("Đơn giá"));
        addBar.add(txtGia);
        addBar.add(btnThem);
        addBar.add(btnXoa);

        JPanel mid = new JPanel(new BorderLayout());
        mid.setBorder(new EmptyBorder(0, 14, 0, 14));
        mid.add(addBar, BorderLayout.NORTH);
        mid.add(new JScrollPane(t), BorderLayout.CENTER);

        // ---- Lưu / Hủy ----
        JButton btnLuu = new JButton(src == null ? "Lập phiếu" : "Lưu thay đổi");
        JButton btnDong = new JButton("Hủy");
        btnDong.addActionListener(e -> d.dispose());
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
                JOptionPane.showMessageDialog(d, "Phiếu cần có ít nhất một vật tư.", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Phieu p = new Phieu();
            for (int r = 0; r < m.getRowCount(); r++) {
                int sl = ((Number) m.getValueAt(r, 3)).intValue();
                long gia = ((Number) m.getValueAt(r, 4)).longValue();
                if (sl <= 0 || gia < 0) {
                    JOptionPane.showMessageDialog(d, "Số lượng phải > 0 và đơn giá phải >= 0 (dòng " + (r + 1) + ").",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                p.dong.add(new Dong((String) m.getValueAt(r, 0), (String) m.getValueAt(r, 1),
                        (String) m.getValueAt(r, 2), sl, gia));
            }
            if (src == null) {
                seq++;
                p.so = String.format("PN-%d-%04d", LocalDate.now().getYear(), seq);
                p.nhanVien = nhanVien;
            } else {
                p.so = src.so;
                p.nhanVien = src.nhanVien;
                p.tt = src.tt;
            }
            p.ngay = ngay;
            p.nhaCungCap = ncc;
            p.ghiChu = txtGhiChu.getText().trim();
            result[0] = p;
            d.dispose();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(6, 14, 10, 14));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.add(btnLuu);
        btns.add(btnDong);
        bottom.add(lblTong, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);

        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.NORTH);
        d.add(mid, BorderLayout.CENTER);
        d.add(bottom, BorderLayout.SOUTH);
        d.setSize(760, 600);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        return result[0];
    }

    private static void addFormRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(3, 0, 3, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);
        form.add(comp, gc);
    }

    // =====================================================================
    // Renderers & component phụ
    // =====================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(new Font(FONT, Font.BOLD, 10));
            setForeground(TEXT_GRAY);
            setBackground(new Color(0xF9FAFB));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 12, 0, 8)));
            return this;
        }
    }

    /** Renderer ô bảng: chọn màu / kiểu chữ, có thể định dạng tiền. */
    private static class Cell extends DefaultTableCellRenderer {
        private final Color color;
        private final int style;
        private final boolean money;
        Cell(Color color, int style, boolean money) {
            this.color = color; this.style = style; this.money = money;
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setFont(new Font(FONT, style, 12));
            setForeground(color);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            if (money && v instanceof Number) setText(MONEY.format(((Number) v).longValue()) + " đ");
            return this;
        }
    }

    private static class Badge extends JLabel {
        private Color bg = GREEN_BG;
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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút bo góc. */
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
            g2.setColor(en ? fill : new Color(0xF3F4F6));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(en ? line : BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panel bo góc có viền. */
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;
        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc; this.fill = fill; this.line = line;
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
}