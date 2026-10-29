package GUI.DichVu;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Giao diện "Danh sách dịch vụ" (chỉ phần nội dung, không gồm Header/Tab).
 * Nhúng vào GiaoDienChinhGUI: setNoiDung(new DichVu());
 */
public class DichVu extends JPanel {

    // ===== Màu sắc & font =====
    private static final Color XANH = new Color(0x0F6B5C), XANH_NHAT = new Color(0xDDF3EA);
    private static final Color NEN = new Color(0xF8FAFA), VIEN = new Color(0xE5E7EB);
    private static final Color CHU = new Color(0x111827), XAM = new Color(0x6B7280), DO = new Color(0xDC2626);

    private static Font font(boolean dam, int size) {
        return new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, size);
    }

    // ===== Dữ liệu =====
    static class DV {
        String ma, ten, nhom, moTa;
        int phut;
        long gia;
        boolean bat;

        DV(String ma, String ten, String nhom, String moTa, int phut, long gia, boolean bat) {
            this.ma = ma; this.ten = ten; this.nhom = nhom; this.moTa = moTa;
            this.phut = phut; this.gia = gia; this.bat = bat;
        }
    }

    private final List<DV> tatCa = new ArrayList<>();   // toàn bộ dịch vụ
    private List<DV> loc = new ArrayList<>();           // sau khi tìm kiếm/lọc
    private List<DV> hienTai = new ArrayList<>();       // các dòng của trang đang xem
    private int trang = 1, soDong = 8;
    private boolean dangNap = false;

    // ===== Thành phần giao diện =====
    private JTextField txtTim;
    private JComboBox<String> cboNhom, cboTrangThai;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnChinhSua, btnThietLapGia, btnXoa;
    private final JLabel[] giaTri = new JLabel[4], phu = new JLabel[4];
    private JLabel lblThongTin;
    private JPanel panelTrang;

    public DichVu() {
        napDuLieuMau();
        setLayout(new BorderLayout(0, 16));
        setBackground(NEN);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel tren = new JPanel();
        tren.setOpaque(false);
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        tren.add(taoTieuDe());
        tren.add(Box.createVerticalStrut(16));
        tren.add(taoThongKe());
        add(tren, BorderLayout.NORTH);
        add(taoTheBang(), BorderLayout.CENTER);

        napNhom();
        napLai(false);
    }

    // ==========================================
    // DỮ LIỆU MẪU (thay bằng dữ liệu từ DAO khi có)
    // ==========================================
    private void napDuLieuMau() {
        them("Tắm cơ bản", "Tắm", "Tắm, sấy khô và chải lông theo loại da", 45, 180000, true);
        them("Tắm và vệ sinh toàn diện", "Tắm", "Tắm, vệ sinh tai, mắt và cắt móng", 60, 300000, true);
        them("Vệ sinh tai và cắt móng", "Vệ sinh", "Làm sạch tai, kẽ chân và cắt móng an toàn", 30, 100000, true);
        them("Cắt tỉa tạo kiểu", "Cắt tỉa", "Tư vấn và tạo kiểu lông theo giống thú cưng", 90, 450000, true);
        them("Chải lông chuyên sâu", "Chăm sóc thú cưng", "Gỡ rối, loại bỏ lông rụng và dưỡng bóng", 40, 150000, true);
        them("Chăm sóc thú cưng theo giờ", "Chăm sóc thú cưng", "Trông giữ, cho ăn và vận động tại cửa hàng", 120, 220000, false);
        them("Khám sức khỏe tổng quát", "Khám sức khỏe", "Khám lâm sàng và tư vấn chăm sóc định kỳ", 30, 250000, true);
        them("Vệ sinh răng miệng", "Chăm sóc sức khỏe", "Làm sạch răng, nướu và kiểm tra khoang miệng", 45, 280000, false);
        for (int i = 9; i <= 24; i++) // 16 dịch vụ còn lại cho đủ 24
            them("Dịch vụ mẫu " + i, "Tắm", "Mô tả dịch vụ mẫu " + i, 30 + i, 100000 + i * 5000L, i != 24);
    }

    private void them(String ten, String nhom, String moTa, int phut, long gia, boolean bat) {
        tatCa.add(new DV(String.format("DV%03d", tatCa.size() + 1), ten, nhom, moTa, phut, gia, bat));
    }

    // ==========================================
    // PHẦN TRÊN: TIÊU ĐỀ + 4 THẺ THỐNG KÊ
    // ==========================================
    private JPanel taoTieuDe() {
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        trai.add(nhan("Dịch vụ / Danh sách dịch vụ", 12, false, XAM));
        trai.add(Box.createVerticalStrut(6));
        trai.add(nhan("Danh sách dịch vụ", 26, true, CHU));
        trai.add(nhan("Quản lý danh mục, giá bán và trạng thái cung cấp dịch vụ tại SALALA.", 12, false, XAM));

        JButton btnThem = nut("+  Thêm dịch vụ", true);
        btnThem.setPreferredSize(new Dimension(142, 40));
        btnThem.addActionListener(e -> {
            DV moi = new DV(String.format("DV%03d", tatCa.size() + 1), "", "", "", 30, 0, true);
            if (hopThoai(moi, "Thêm dịch vụ")) {
                tatCa.add(moi);
                napNhom();
                napLai(true);
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(trai, BorderLayout.WEST);
        p.add(btnThem, BorderLayout.EAST);
        return p;
    }

    private JPanel taoThongKe() {
        String[] tieuDe = { "Tổng dịch vụ", "Đang cung cấp", "Tạm ẩn", "Giá trung bình" };
        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        for (int i = 0; i < 4; i++) {
            giaTri[i] = nhan("0", 26, true, CHU);
            phu[i] = nhan(" ", 11, false, XAM);
            JPanel the = new JPanel(new GridBagLayout());
            the.setBackground(Color.WHITE);
            the.setBorder(new CompoundBorder(new LineBorder(VIEN), new EmptyBorder(10, 16, 10, 16)));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
            the.add(nhan(tieuDe[i], 12, false, XAM), c);
            c.gridy = 1; c.gridwidth = 1;
            the.add(giaTri[i], c);
            c.gridx = 1; c.weightx = 1; c.insets = new Insets(0, 10, 4, 0); c.anchor = GridBagConstraints.SOUTHWEST;
            the.add(phu[i], c);
            hang.add(the);
        }
        return hang;
    }

    private void capNhatThongKe() {
        long dang = tatCa.stream().filter(d -> d.bat).count();
        long tong = tatCa.size();
        long tb = tong == 0 ? 0 : Math.round(tatCa.stream().mapToLong(d -> d.gia).average().orElse(0));
        giaTri[0].setText("" + tong);
        phu[0].setText(tatCa.stream().map(d -> d.nhom).distinct().count() + " nhóm dịch vụ");
        giaTri[1].setText("" + dang);
        phu[1].setText(String.format("%.1f%% danh mục", tong == 0 ? 0 : dang * 100.0 / tong).replace('.', ','));
        giaTri[2].setText("" + (tong - dang));
        phu[2].setText("Không nhận lịch mới");
        giaTri[3].setText(dinhDangGia(tb));
        phu[3].setText("Theo bảng giá hiện tại");
    }

    // ==========================================
    // THẺ CHỨA BẢNG: thanh lọc + bảng + phân trang
    // ==========================================
    private JPanel taoTheBang() {
        // --- Thanh tìm kiếm / lọc ---
        txtTim = new JTextField();
        txtTim.setPreferredSize(new Dimension(300, 38));
        txtTim.setToolTipText("Tìm mã hoặc tên dịch vụ...");
        txtTim.setBorder(new CompoundBorder(new LineBorder(VIEN), new EmptyBorder(0, 10, 0, 10)));
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { napLai(false); }
        });

        cboNhom = new JComboBox<>();
        cboNhom.setPreferredSize(new Dimension(220, 38));
        cboNhom.addActionListener(e -> { if (!dangNap) napLai(false); });
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả", "Đang cung cấp", "Tạm ẩn" });
        cboTrangThai.setPreferredSize(new Dimension(160, 38));
        cboTrangThai.addActionListener(e -> napLai(false));

        JButton btnXuat = nut("Xuất danh sách", false);
        btnXuat.setPreferredSize(new Dimension(150, 38));
        btnXuat.addActionListener(e -> xuatCSV());

        btnChinhSua = nut("Chỉnh sửa", false);
        btnChinhSua.setPreferredSize(new Dimension(110, 38));
        btnChinhSua.setEnabled(false);
        btnChinhSua.addActionListener(e -> {
            DV d = dichVuDaChon();
            if (d != null && hopThoai(d, "Sửa dịch vụ")) {
                napNhom();
                napLai(true);
            }
        });

        btnThietLapGia = nut("Thiết lập giá", false);
        btnThietLapGia.setPreferredSize(new Dimension(130, 38));
        btnThietLapGia.setEnabled(false);
        btnThietLapGia.addActionListener(e -> {
            DV d = dichVuDaChon();
            if (d != null) datGia(d);
        });

        btnXoa = nut("Xóa", false);
        btnXoa.setPreferredSize(new Dimension(80, 38));
        btnXoa.setForeground(DO);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> {
            DV d = dichVuDaChon();
            if (d != null) xoa(d);
        });

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        trai.setOpaque(false);
        trai.add(txtTim);
        trai.add(new JLabel("Nhóm:"));
        trai.add(cboNhom);
        trai.add(new JLabel("Trạng thái:"));
        trai.add(cboTrangThai);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(16, 20, 16, 20));
        toolbar.add(trai, BorderLayout.WEST);
        JPanel phaiToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiToolbar.setOpaque(false);
        phaiToolbar.add(btnChinhSua);
        phaiToolbar.add(btnThietLapGia);
        phaiToolbar.add(btnXoa);
        phaiToolbar.add(btnXuat);
        toolbar.add(phaiToolbar, BorderLayout.EAST);

        // --- Bảng ---
        String[] cot = { "Mã dịch vụ", "Tên dịch vụ", "Mô tả", "Thời gian thực hiện", "Giá", "Trạng thái", "Bật / tắt" };
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xF0F1F3));
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new OBangRenderer());
        int[] rong = { 90, 200, 330, 130, 110, 140, 110 };
        for (int i = 0; i < rong.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(100, 40));
        th.setDefaultRenderer((t, v, s, f, r, c) -> {
            JLabel l = nhan(String.valueOf(v), 12, true, new Color(0x4B5563));
            l.setOpaque(true);
            l.setBackground(new Color(0xF9FAFB));
            l.setBorder(new EmptyBorder(0, 10, 0, 14));
            l.setHorizontalAlignment(c == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return l;
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) capNhatNutThaoTac();
        });

        // Click vào cột "Bật / tắt"
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r < 0 || r >= hienTai.size()) return;
                DV d = hienTai.get(r);
                if (c == 6) {
                    d.bat = !d.bat;
                    napLai(true);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, VIEN));
        sp.getViewport().setBackground(Color.WHITE);

        // --- Phân trang ---
        lblThongTin = nhan(" ", 12, false, XAM);
        panelTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelTrang.setOpaque(false);
        JComboBox<String> cboSoDong = new JComboBox<>(new String[] { "8", "16", "24" });
        cboSoDong.addActionListener(e -> {
            soDong = Integer.parseInt((String) cboSoDong.getSelectedItem());
            napLai(false);
        });
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(nhan("Số dòng / trang", 12, false, XAM));
        phai.add(cboSoDong);
        phai.add(panelTrang);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.add(lblThongTin, BorderLayout.WEST);
        footer.add(phai, BorderLayout.EAST);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(Color.WHITE);
        the.setBorder(new LineBorder(VIEN));
        the.add(toolbar, BorderLayout.NORTH);
        the.add(sp, BorderLayout.CENTER);
        the.add(footer, BorderLayout.SOUTH);
        return the;
    }

    // ==========================================
    // LỌC + PHÂN TRANG + VẼ LẠI BẢNG
    // ==========================================
    /** Lọc dữ liệu rồi vẽ lại bảng. giuTrang=true: giữ nguyên trang đang xem. */
    private void napLai(boolean giuTrang) {
        if (table == null) return;
        String tu = txtTim.getText().trim().toLowerCase();
        String nhom = (String) cboNhom.getSelectedItem();
        int tt = cboTrangThai.getSelectedIndex(); // 0: tất cả, 1: đang cung cấp, 2: tạm ẩn

        loc = tatCa.stream()
                .filter(d -> (d.ma + " " + d.ten).toLowerCase().contains(tu))
                .filter(d -> nhom == null || nhom.equals("Tất cả") || nhom.equals(d.nhom))
                .filter(d -> tt == 0 || (tt == 1) == d.bat)
                .collect(Collectors.toList());

        int tongTrang = Math.max(1, (loc.size() + soDong - 1) / soDong);
        trang = giuTrang ? Math.min(trang, tongTrang) : 1;
        int tuDong = (trang - 1) * soDong, denDong = Math.min(tuDong + soDong, loc.size());
        hienTai = loc.subList(tuDong, denDong);

        table.clearSelection();
        model.setRowCount(0);
        for (DV d : hienTai)
            model.addRow(new Object[] { d.ma, d, d.moTa, d.phut + " phút", dinhDangGia(d.gia), d, d });

        lblThongTin.setText(loc.isEmpty() ? "Không có dịch vụ phù hợp"
                : "Hiển thị " + (tuDong + 1) + "–" + denDong + " trong " + loc.size() + " dịch vụ");
        vePhanTrang(tongTrang);
        capNhatThongKe();
        capNhatNutThaoTac();
    }

    private DV dichVuDaChon() {
        if (table == null || table.getSelectedRowCount() != 1) return null;
        int r = table.getSelectedRow();
        return r >= 0 && r < hienTai.size() ? hienTai.get(r) : null;
    }

    private void capNhatNutThaoTac() {
        boolean chonMot = table != null && table.getSelectedRowCount() == 1;
        if (btnChinhSua != null) btnChinhSua.setEnabled(chonMot);
        if (btnThietLapGia != null) btnThietLapGia.setEnabled(chonMot);
        if (btnXoa != null) btnXoa.setEnabled(chonMot);
    }

    private void vePhanTrang(int tongTrang) {
        panelTrang.removeAll();
        JButton truoc = nutTrang("<", false);
        truoc.setEnabled(trang > 1);
        truoc.addActionListener(e -> { trang--; napLai(true); });
        panelTrang.add(truoc);
        for (int p = 1; p <= tongTrang; p++) {
            JButton b = nutTrang("" + p, p == trang);
            final int pp = p;
            b.addActionListener(e -> { trang = pp; napLai(true); });
            panelTrang.add(b);
        }
        JButton sau = nutTrang(">", false);
        sau.setEnabled(trang < tongTrang);
        sau.addActionListener(e -> { trang++; napLai(true); });
        panelTrang.add(sau);
        panelTrang.revalidate();
        panelTrang.repaint();
    }

    /** Nạp lại danh sách nhóm cho combo (giữ lựa chọn hiện tại nếu còn). */
    private void napNhom() {
        dangNap = true;
        Object chon = cboNhom.getSelectedItem();
        cboNhom.removeAllItems();
        cboNhom.addItem("Tất cả");
        new LinkedHashSet<>(tatCa.stream().map(d -> d.nhom).collect(Collectors.toList())).forEach(cboNhom::addItem);
        cboNhom.setSelectedItem(chon != null ? chon : "Tất cả");
        if (cboNhom.getSelectedItem() == null) cboNhom.setSelectedIndex(0);
        dangNap = false;
    }

    // ==========================================
    // CÁC THAO TÁC: SỬA / ĐẶT GIÁ / XÓA / XUẤT CSV
    // ==========================================
    /** Hộp thoại thêm/sửa. Trả về true nếu người dùng bấm OK và dữ liệu hợp lệ. */
    private boolean hopThoai(DV d, String tieuDe) {
        JTextField ten = new JTextField(d.ten), nhom = new JTextField(d.nhom), mo = new JTextField(d.moTa);
        JTextField phut = new JTextField("" + d.phut), gia = new JTextField("" + d.gia);
        Object[] form = { "Tên dịch vụ", ten, "Nhóm dịch vụ", nhom, "Mô tả", mo, "Thời gian (phút)", phut, "Giá (VNĐ)", gia };
        while (JOptionPane.showConfirmDialog(this, form, tieuDe, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int p = Integer.parseInt(phut.getText().trim());
                long g = Long.parseLong(gia.getText().trim());
                if (ten.getText().isBlank() || nhom.getText().isBlank()) throw new NumberFormatException();
                d.ten = ten.getText().trim();
                d.nhom = nhom.getText().trim();
                d.moTa = mo.getText().trim();
                d.phut = p;
                d.gia = g;
                return true;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void datGia(DV d) {
        String s = JOptionPane.showInputDialog(this, "Giá mới cho \"" + d.ten + "\" (VNĐ):", d.gia);
        if (s == null) return;
        try {
            d.gia = Long.parseLong(s.replaceAll("[^0-9]", ""));
            napLai(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa(DV d) {
        if (JOptionPane.showConfirmDialog(this, "Xóa dịch vụ \"" + d.ten + "\"?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tatCa.remove(d);
            napNhom();
            napLai(true);
        }
    }

    private void xuatCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh_sach_dich_vu.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StringBuilder sb = new StringBuilder("\uFEFFMã,Tên,Nhóm,Mô tả,Phút,Giá,Trạng thái\n");
        for (DV d : loc)
            sb.append(String.format("%s,\"%s\",\"%s\",\"%s\",%d,%d,%s\n", d.ma, d.ten, d.nhom, d.moTa, d.phut, d.gia,
                    d.bat ? "Đang cung cấp" : "Tạm ẩn"));
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage());
        }
    }

    // ==========================================
    // VẼ Ô CỦA BẢNG (mỗi cột một kiểu)
    // ==========================================
    private static class OBangRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            Color nen = s ? new Color(0xF0FAF6) : Color.WHITE;
            if (c == 0) return o(nhan(String.valueOf(v), 13, false, XANH), nen);
            if (c == 2 || c == 3) return o(nhan(String.valueOf(v), 13, false, CHU), nen);
            if (c == 4) {
                JLabel l = nhan(String.valueOf(v), 13, true, CHU);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setBorder(new EmptyBorder(0, 10, 0, 14));
                return o(l, nen);
            }
            DV d = (DV) v;
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(nen);
            GridBagConstraints g = new GridBagConstraints();
            g.anchor = GridBagConstraints.WEST;
            g.weightx = 1;
            switch (c) {
                case 1: // tên + nhóm (2 dòng)
                    JPanel hai = new JPanel(new GridLayout(2, 1));
                    hai.setOpaque(false);
                    hai.setBorder(new EmptyBorder(0, 10, 0, 0));
                    hai.add(nhan(d.ten, 13, true, CHU));
                    hai.add(nhan(d.nhom, 11, false, XAM));
                    p.add(hai, g);
                    break;
                case 5: // trạng thái
                    JLabel tt = nhan(d.bat ? "Đang cung cấp" : "Tạm ẩn", 11, true, d.bat ? XANH : XAM);
                    tt.setOpaque(true);
                    tt.setBackground(d.bat ? XANH_NHAT : new Color(0xEEF0F3));
                    tt.setBorder(new EmptyBorder(3, 10, 3, 10));
                    g.insets = new Insets(0, 10, 0, 0);
                    p.add(tt, g);
                    break;
                case 6: // công tắc bật/tắt
                    g.insets = new Insets(0, 10, 0, 0);
                    g.weightx = 0;
                    p.add(new Toggle(d.bat), g);
                    g.insets = new Insets(0, 8, 0, 0);
                    g.weightx = 1;
                    p.add(nhan(d.bat ? "Bật" : "Tắt", 12, false, XAM), g);
                    break;
            }
            return p;
        }

        private static JLabel o(JLabel l, Color nen) { // ô nền trắng
            l.setOpaque(true);
            l.setBackground(nen);
            if (l.getBorder() == null) l.setBorder(new EmptyBorder(0, 10, 0, 10));
            return l;
        }
    }

    /** Công tắc bật/tắt vẽ bằng Graphics2D. */
    private static class Toggle extends JComponent {
        private final boolean bat;

        Toggle(boolean bat) {
            this.bat = bat;
            setPreferredSize(new Dimension(38, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bat ? XANH : new Color(0xD1D5DB));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(Color.WHITE);
            g2.fillOval(bat ? getWidth() - getHeight() + 3 : 3, 3, getHeight() - 6, getHeight() - 6);
            g2.dispose();
        }
    }

    // ==========================================
    // HÀM TIỆN ÍCH
    // ==========================================
    private static JLabel nhan(String text, int size, boolean dam, Color mau) {
        JLabel l = new JLabel(text);
        l.setFont(font(dam, size));
        l.setForeground(mau);
        return l;
    }

    private static JButton nut(String text, boolean noiBat) {
        JButton b = new JButton(text);
        b.setFont(font(true, 13));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(noiBat ? XANH : Color.WHITE);
        b.setForeground(noiBat ? Color.WHITE : CHU);
        b.setBorder(noiBat ? new EmptyBorder(8, 14, 8, 14) : new LineBorder(VIEN));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton nutTrang(String text, boolean dangChon) {
        JButton b = nut(text, dangChon);
        b.setFont(font(true, 12));
        b.setPreferredSize(new Dimension(34, 34));
        return b;
    }

    private static String dinhDangGia(long v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", s).format(v) + " \u20AB";
    }
}
