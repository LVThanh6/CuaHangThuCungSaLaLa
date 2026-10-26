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
 * Giao diện "Danh sách dịch vụ / Bảng giá dịch vụ" của SALALA Pet Shop.
 * Tuân thủ chuẩn thiết kế mới:
 * 1. Loại bỏ HOÀN TOÀN cột "Thao tác" khỏi bảng dữ liệu (7 cột tinh gọn).
 * 2. Cụm nút thao tác độc lập đặt tại thanh công cụ phía trên bảng:
 * [+ Thêm mới], [Xem chi tiết], [Chỉnh sửa], [Xóa], [Xuất Excel].
 * 3. Các nút [Xem chi tiết], [Chỉnh sửa], [Xóa] mặc định bị disabled,
 * chỉ bật sáng khi người dùng click chọn 1 dòng trong bảng thông qua
 * ListSelectionListener.
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
            this.ma = ma;
            this.ten = ten;
            this.nhom = nhom;
            this.moTa = moTa;
            this.phut = phut;
            this.gia = gia;
            this.bat = bat;
        }
    }

    private final List<DV> tatCa = new ArrayList<>(); // toàn bộ dịch vụ
    private List<DV> loc = new ArrayList<>(); // sau khi tìm kiếm/lọc
    private List<DV> hienTai = new ArrayList<>(); // các dòng của trang đang xem
    private int trang = 1, soDong = 8;
    private boolean dangNap = false;

    // ===== Thành phần giao diện =====
    private JTextField txtTim;
    private JComboBox<String> cboNhom, cboTrangThai;
    private JTable table;
    private DefaultTableModel model;
    private final JLabel[] giaTri = new JLabel[4], phu = new JLabel[4];
    private JLabel lblThongTin;
    private JPanel panelTrang;

    // Cụm nút thao tác độc lập phía trên bảng
    private JButton btnThem;
    private JButton btnXemChiTiet;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnXuat;

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
    // DỮ LIỆU MẪU
    // ==========================================
    private void napDuLieuMau() {
        them("Tắm cơ bản", "Tắm", "Tắm, sấy khô và chải lông theo loại da", 45, 180000, true);
        them("Tắm và vệ sinh toàn diện", "Tắm", "Tắm, vệ sinh tai, mắt và cắt móng", 60, 300000, true);
        them("Vệ sinh tai và cắt móng", "Vệ sinh", "Làm sạch tai, kẽ chân và cắt móng an toàn", 30, 100000, true);
        them("Cắt tỉa tạo kiểu", "Cắt tỉa", "Tư vấn và tạo kiểu lông theo giống thú cưng", 90, 450000, true);
        them("Chải lông chuyên sâu", "Chăm sóc thú cưng", "Gỡ rối, loại bỏ lông rụng và dưỡng bóng", 40, 150000, true);
        them("Chăm sóc thú cưng theo giờ", "Chăm sóc thú cưng", "Trông giữ, cho ăn và vận động tại cửa hàng", 120,
                220000, false);
        them("Khám sức khỏe tổng quát", "Khám sức khỏe", "Khám lâm sàng và tư vấn chăm sóc định kỳ", 30, 250000, true);
        them("Vệ sinh răng miệng", "Chăm sóc sức khỏe", "Làm sạch răng, nướu và kiểm tra khoang miệng", 45, 280000,
                false);
        for (int i = 9; i <= 24; i++)
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

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(trai, BorderLayout.WEST);
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
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.WEST;
            the.add(nhan(tieuDe[i], 12, false, XAM), c);
            c.gridy = 1;
            c.gridwidth = 1;
            the.add(giaTri[i], c);
            c.gridx = 1;
            c.weightx = 1;
            c.insets = new Insets(0, 10, 4, 0);
            c.anchor = GridBagConstraints.SOUTHWEST;
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
    // THẺ CHỨA BẢNG: thanh lọc + cụm nút + bảng + phân trang
    // ==========================================
    private JPanel taoTheBang() {
        // --- Thanh tìm kiếm / lọc ---
        txtTim = new JTextField();
        txtTim.setPreferredSize(new Dimension(240, 38));
        txtTim.setToolTipText("Tìm mã hoặc tên dịch vụ...");
        txtTim.setBorder(new CompoundBorder(new LineBorder(VIEN), new EmptyBorder(0, 10, 0, 10)));
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                napLai(false);
            }
        });

        cboNhom = new JComboBox<>();
        cboNhom.setPreferredSize(new Dimension(170, 38));
        cboNhom.addActionListener(e -> {
            if (!dangNap)
                napLai(false);
        });
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả", "Đang cung cấp", "Tạm ẩn" });
        cboTrangThai.setPreferredSize(new Dimension(140, 38));
        cboTrangThai.addActionListener(e -> napLai(false));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trai.setOpaque(false);
        trai.add(txtTim);
        trai.add(nhan("Nhóm:", 13, false, XAM));
        trai.add(cboNhom);
        trai.add(nhan("Trạng thái:", 13, false, XAM));
        trai.add(cboTrangThai);

        // --- CỤM NÚT THAO TÁC ĐỘC LẬP PHÍA TRÊN BẢNG ---
        btnThem = taoNutThaoTac("+ Thêm mới", true, false);
        btnThem.setPreferredSize(new Dimension(115, 38));

        btnXemChiTiet = taoNutThaoTac("Xem chi tiết", false, false);
        btnXemChiTiet.setPreferredSize(new Dimension(110, 38));
        btnXemChiTiet.setEnabled(false);

        btnSua = taoNutThaoTac("Chỉnh sửa", false, false);
        btnSua.setPreferredSize(new Dimension(100, 38));
        btnSua.setEnabled(false);

        btnXoa = taoNutThaoTac("Xóa", false, true);
        btnXoa.setPreferredSize(new Dimension(75, 38));
        btnXoa.setEnabled(false);

        btnXuat = taoNutThaoTac("Xuất Excel", false, false);
        btnXuat.setPreferredSize(new Dimension(105, 38));

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(btnThem);
        phai.add(btnXemChiTiet);
        phai.add(btnSua);
        phai.add(btnXoa);
        phai.add(btnXuat);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(16, 18, 16, 18));
        toolbar.add(trai, BorderLayout.WEST);
        toolbar.add(phai, BorderLayout.EAST);

        // --- Bảng (7 cột - Đã loại bỏ hoàn toàn cột Thao tác cũ) ---
        String[] cot = { "Mã dịch vụ", "Tên dịch vụ", "Mô tả", "Thời gian thực hiện", "Giá", "Trạng thái",
                "Bật / tắt" };
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xF0F1F3));
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(230, 244, 241));
        table.setSelectionForeground(CHU);
        table.setFocusable(true);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new OBangRenderer());
        int[] rong = { 100, 220, 360, 150, 130, 150, 110 };
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

        // Lắng nghe sự kiện chọn dòng để bật/tắt các nút thao tác phía trên
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = table.getSelectedRow() != -1;
                btnXemChiTiet.setEnabled(coChon);
                btnSua.setEnabled(coChon);
                btnXoa.setEnabled(coChon);
            }
        });

        // Click chuyển trạng thái bật/tắt ở cột 6 hoặc double-click xem chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r < 0 || r >= hienTai.size())
                    return;
                DV d = hienTai.get(r);
                if (c == 6) {
                    d.bat = !d.bat;
                    napLai(true);
                } else if (e.getClickCount() == 2) {
                    hienThiDialogChiTietDichVu(d);
                }
            }
        });

        ganSuKienNutThaoTac();

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
        JPanel phaiFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiFooter.setOpaque(false);
        phaiFooter.add(nhan("Số dòng / trang", 12, false, XAM));
        phaiFooter.add(cboSoDong);
        phaiFooter.add(panelTrang);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.add(lblThongTin, BorderLayout.WEST);
        footer.add(phaiFooter, BorderLayout.EAST);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(Color.WHITE);
        the.setBorder(new LineBorder(VIEN));
        the.add(toolbar, BorderLayout.NORTH);
        the.add(sp, BorderLayout.CENTER);
        the.add(footer, BorderLayout.SOUTH);
        return the;
    }

    // ==========================================
    // SỰ KIỆN NÚT THAO TÁC MỚI
    // ==========================================
    private void ganSuKienNutThaoTac() {
        btnThem.addActionListener(e -> {
            DV moi = new DV(String.format("DV%03d", tatCa.size() + 1), "", "", "", 30, 0, true);
            if (hopThoai(moi, "Thêm dịch vụ")) {
                tatCa.add(moi);
                napNhom();
                napLai(true);
            }
        });

        btnXemChiTiet.addActionListener(e -> {
            DV d = getSelectedDV();
            if (d != null) {
                hienThiDialogChiTietDichVu(d);
            }
        });

        btnSua.addActionListener(e -> {
            DV d = getSelectedDV();
            if (d != null && hopThoai(d, "Sửa dịch vụ")) {
                napNhom();
                napLai(true);
            }
        });

        btnXoa.addActionListener(e -> {
            DV d = getSelectedDV();
            if (d != null) {
                xoa(d);
            }
        });

        btnXuat.addActionListener(e -> xuatCSV());
    }

    private DV getSelectedDV() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= hienTai.size())
            return null;
        return hienTai.get(r);
    }

    /**
     * Mở Dialog hiển thị chi tiết dịch vụ được chọn
     */
    private void hienThiDialogChiTietDichVu(DV d) {
        if (d == null)
            return;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết dịch vụ - " + d.ten,
                true);
        dialog.setSize(480, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        pnlNoiDung.setBorder(new EmptyBorder(20, 24, 20, 24));
        pnlNoiDung.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("THÔNG TIN DỊCH VỤ CHI TIẾT");
        lblHeader.setFont(font(true, 16));
        lblHeader.setForeground(XANH);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlNoiDung.add(lblHeader);
        pnlNoiDung.add(Box.createVerticalStrut(16));
        pnlNoiDung.add(taoDongChiTiet("Mã dịch vụ:", d.ma));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Tên dịch vụ:", d.ten));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Nhóm dịch vụ:", d.nhom));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Thời gian thực hiện:", d.phut + " phút"));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Đơn giá:", dinhDangGia(d.gia)));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Trạng thái:", d.bat ? "Đang cung cấp" : "Tạm ẩn"));
        pnlNoiDung.add(Box.createVerticalStrut(10));
        pnlNoiDung.add(taoDongChiTiet("Mô tả:", d.moTa));

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlBottom.setBackground(new Color(248, 250, 252));
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));

        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(font(true, 13));
        btnDong.setPreferredSize(new Dimension(90, 36));
        btnDong.addActionListener(e -> dialog.dispose());
        pnlBottom.add(btnDong);

        dialog.add(pnlNoiDung, BorderLayout.CENTER);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel taoDongChiTiet(String nhanStr, String giaTriStr) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNhan = new JLabel(nhanStr);
        lblNhan.setFont(font(true, 13));
        lblNhan.setForeground(XAM);
        lblNhan.setPreferredSize(new Dimension(140, 22));

        JLabel lblGiaTri = new JLabel(giaTriStr);
        lblGiaTri.setFont(font(false, 13));
        lblGiaTri.setForeground(CHU);

        p.add(lblNhan, BorderLayout.WEST);
        p.add(lblGiaTri, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // LỌC + PHÂN TRANG + VẼ LẠI BẢNG
    // ==========================================
    private void napLai(boolean giuTrang) {
        if (table == null)
            return;
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

        model.setRowCount(0);
        for (DV d : hienTai)
            model.addRow(new Object[] { d.ma, d, d.moTa, d.phut + " phút", dinhDangGia(d.gia), d, d });

        table.clearSelection();
        btnXemChiTiet.setEnabled(false);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        lblThongTin.setText(loc.isEmpty() ? "Không có dịch vụ phù hợp"
                : "Hiển thị " + (tuDong + 1) + "–" + denDong + " trong " + loc.size() + " dịch vụ");
        vePhanTrang(tongTrang);
        capNhatThongKe();
    }

    private void vePhanTrang(int tongTrang) {
        panelTrang.removeAll();
        JButton truoc = nutTrang("<", false);
        truoc.setEnabled(trang > 1);
        truoc.addActionListener(e -> {
            trang--;
            napLai(true);
        });
        panelTrang.add(truoc);
        for (int p = 1; p <= tongTrang; p++) {
            JButton b = nutTrang("" + p, p == trang);
            final int pp = p;
            b.addActionListener(e -> {
                trang = pp;
                napLai(true);
            });
            panelTrang.add(b);
        }
        JButton sau = nutTrang(">", false);
        sau.setEnabled(trang < tongTrang);
        sau.addActionListener(e -> {
            trang++;
            napLai(true);
        });
        panelTrang.add(sau);
        panelTrang.revalidate();
        panelTrang.repaint();
    }

    private void napNhom() {
        dangNap = true;
        Object chon = cboNhom.getSelectedItem();
        cboNhom.removeAllItems();
        cboNhom.addItem("Tất cả");
        new LinkedHashSet<>(tatCa.stream().map(d -> d.nhom).collect(Collectors.toList())).forEach(cboNhom::addItem);
        cboNhom.setSelectedItem(chon != null ? chon : "Tất cả");
        if (cboNhom.getSelectedItem() == null)
            cboNhom.setSelectedIndex(0);
        dangNap = false;
    }

    // ==========================================
    // CÁC THAO TÁC: SỬA / ĐẶT GIÁ / XÓA / XUẤT CSV
    // ==========================================
    private boolean hopThoai(DV d, String tieuDe) {
        JTextField ten = new JTextField(d.ten), nhom = new JTextField(d.nhom), mo = new JTextField(d.moTa);
        JTextField phut = new JTextField("" + d.phut), gia = new JTextField("" + d.gia);
        Object[] form = { "Tên dịch vụ", ten, "Nhóm dịch vụ", nhom, "Mô tả", mo, "Thời gian (phút)", phut, "Giá (VNĐ)",
                gia };
        while (JOptionPane.showConfirmDialog(this, form, tieuDe,
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int p = Integer.parseInt(phut.getText().trim());
                long g = Long.parseLong(gia.getText().trim());
                if (ten.getText().isBlank() || nhom.getText().isBlank())
                    throw new NumberFormatException();
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
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
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
    // VẼ Ô CỦA BẢNG (Đã loại bỏ Renderer cho cột Thao tác cũ)
    // ==========================================
    private static class OBangRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            Color bg = s ? new Color(230, 244, 241) : Color.WHITE;
            if (c == 0)
                return o(nhan(String.valueOf(v), 13, false, XANH), s);
            if (c == 2 || c == 3)
                return o(nhan(String.valueOf(v), 13, false, CHU), s);
            if (c == 4) {
                JLabel l = nhan(String.valueOf(v), 13, true, CHU);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setBorder(new EmptyBorder(0, 10, 0, 14));
                return o(l, s);
            }
            if (v instanceof DV) {
                DV d = (DV) v;
                JPanel p = new JPanel(new GridBagLayout());
                p.setBackground(bg);
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
                    default:
                        break;
                }
                return p;
            }
            return o(nhan(String.valueOf(v), 13, false, CHU), s);
        }

        private static JLabel o(JLabel l, boolean isSelected) {
            l.setOpaque(true);
            l.setBackground(isSelected ? new Color(230, 244, 241) : Color.WHITE);
            if (l.getBorder() == null)
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
            return l;
        }
    }

    /** Công tắc bật/tắt */
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

    private static JButton taoNutThaoTac(String text, boolean noiBat, boolean nguyHiem) {
        JButton b = new JButton(text) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                if (!enabled) {
                    setBackground(new Color(243, 244, 246));
                    setForeground(new Color(156, 163, 175));
                    setBorder(new LineBorder(new Color(229, 231, 235)));
                } else {
                    if (noiBat) {
                        setBackground(XANH);
                        setForeground(Color.WHITE);
                        setBorder(new EmptyBorder(8, 14, 8, 14));
                    } else if (nguyHiem) {
                        setBackground(Color.WHITE);
                        setForeground(DO);
                        setBorder(new LineBorder(DO));
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(CHU);
                        setBorder(new LineBorder(VIEN));
                    }
                }
            }
        };
        b.setFont(font(true, 13));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        if (noiBat) {
            b.setBackground(XANH);
            b.setForeground(Color.WHITE);
            b.setBorder(new EmptyBorder(8, 14, 8, 14));
        } else if (nguyHiem) {
            b.setBackground(Color.WHITE);
            b.setForeground(DO);
            b.setBorder(new LineBorder(DO));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(CHU);
            b.setBorder(new LineBorder(VIEN));
        }
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton nutTrang(String text, boolean dangChon) {
        JButton b = new JButton(text);
        b.setFont(font(true, 12));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(dangChon ? XANH : Color.WHITE);
        b.setForeground(dangChon ? Color.WHITE : CHU);
        b.setBorder(dangChon ? new EmptyBorder(4, 8, 4, 8) : new LineBorder(VIEN));
        b.setPreferredSize(new Dimension(34, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static String dinhDangGia(long v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", s).format(v) + " \u20AB";
    }
}