package GUI.DichVu;

import GUI.Chung.RangBuocGiaoDien;
import Service.DichVu.DichVuService;
import Service.DichVu.DichVuService.DichVuData;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Giao diện "Danh sách dịch vụ" hiển thị danh mục SALALA thông qua service.
 */
public class DichVu extends JPanel {

    // ===== Màu sắc & font =====

    private static Font font(boolean dam, int size) {
        return new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, size);
    }

    private final DichVuService service = new DichVuService();

    // ===== Dữ liệu hiển thị =====
    private final List<DichVuData> tatCa = new ArrayList<>();
    private List<DichVuData> loc = new ArrayList<>();
    private List<DichVuData> hienTai = new ArrayList<>();
    private boolean dangNap = false;

    // ===== Thành phần giao diện =====
    private JTextField txtTim;
    private JComboBox<String> cboNhom, cboTrangThai;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnChinhSua, btnThietLapGia, btnXoa;
    private final JLabel[] giaTri = new JLabel[4], phu = new JLabel[4];
    private JLabel lblThongTin;

    public DichVu() {
        setLayout(new BorderLayout(0, 16));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel tren = new JPanel();
        tren.setOpaque(false);
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        tren.add(taoTieuDe());
        tren.add(Box.createVerticalStrut(16));
        tren.add(taoThongKe());
        add(tren, BorderLayout.NORTH);
        add(taoTheBang(), BorderLayout.CENTER);

        napDuLieuTuDatabase();
    }

    // ==========================================
    // NẠP DỮ LIỆU TỪ CƠ SỞ DỮ LIỆU SALALA
    // ==========================================
    public void napDuLieuTuDatabase() {
        tatCa.clear();
        tatCa.addAll(service.load());
        napNhom();
        napLai(false);
    }

    // ==========================================
    // PHẦN TRÊN: TIÊU ĐỀ + 4 THẺ THỐNG KÊ
    // ==========================================
    private JPanel taoTieuDe() {
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        trai.add(nhan("Dịch vụ / Danh sách dịch vụ", 12, false, RangBuocGiaoDien.MAU_CHU_PHU));
        trai.add(Box.createVerticalStrut(6));
        trai.add(nhan("Danh sách dịch vụ", 26, true, RangBuocGiaoDien.MAU_CHU_CHINH));
        trai.add(nhan("Quản lý danh mục, giá bán và trạng thái cung cấp dịch vụ tại SALALA (Dữ liệu thật CSDL).", 12,
                false, RangBuocGiaoDien.MAU_CHU_PHU));

        JButton btnThem = nut("Thêm dịch vụ", true);
        btnThem.setPreferredSize(new Dimension(142, 40));
        btnThem.addActionListener(e -> {
            DichVuData moi = new DichVuData(service.nextCode(), "", "Dịch vụ chung", "", 30, 0, true);
            if (hopThoai(moi, "Thêm dịch vụ mới", true)) {
                if (service.create(moi)) {
                    JOptionPane.showMessageDialog(this, "Đã thêm dịch vụ " + moi.ma + " thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    napDuLieuTuDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể thêm dịch vụ vào cơ sở dữ liệu!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
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
            giaTri[i] = nhan("0", 26, true, RangBuocGiaoDien.MAU_CHU_CHINH);
            phu[i] = nhan(" ", 11, false, RangBuocGiaoDien.MAU_CHU_PHU);
            JPanel the = new JPanel(new GridBagLayout());
            the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
            the.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN), new EmptyBorder(10, 16, 10, 16)));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.WEST;
            the.add(nhan(tieuDe[i], 12, false, RangBuocGiaoDien.MAU_CHU_PHU), c);
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
        DichVuService.Statistics stats = service.statistics(tatCa);
        giaTri[0].setText("" + stats.total);
        phu[0].setText(stats.categoryCount + " nhóm dịch vụ");
        giaTri[1].setText("" + stats.active);
        phu[1].setText(String.format("%.1f%% danh mục", stats.total == 0 ? 0 : stats.active * 100.0 / stats.total)
                .replace('.', ','));
        giaTri[2].setText("" + stats.inactive);
        phu[2].setText("Không nhận lịch mới");
        giaTri[3].setText(dinhDangGia(stats.averagePrice));
        phu[3].setText("Theo bảng giá hiện tại");
    }

    // ==========================================
    // THẺ CHỨA BẢNG: thanh lọc + bảng
    // ==========================================
    private JPanel taoTheBang() {
        // --- Thanh tìm kiếm / lọc ---
        txtTim = new JTextField();
        txtTim.setPreferredSize(new Dimension(280, 38));
        txtTim.setToolTipText("Tìm mã hoặc tên dịch vụ...");
        txtTim.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN), new EmptyBorder(0, 10, 0, 10)));
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                napLai(false);
            }
        });

        cboNhom = new JComboBox<>();
        cboNhom.setPreferredSize(new Dimension(180, 38));
        cboNhom.addActionListener(e -> {
            if (!dangNap)
                napLai(false);
        });
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang cung cấp", "Tạm ẩn" });
        cboTrangThai.setPreferredSize(new Dimension(160, 38));
        cboTrangThai.addActionListener(e -> napLai(false));

        JButton btnLamMoi = nut("Làm mới", false);
        btnLamMoi.setPreferredSize(new Dimension(100, 38));
        btnLamMoi.addActionListener(e -> napDuLieuTuDatabase());

        JButton btnXuat = nut("Xuất danh sách", false);
        btnXuat.setPreferredSize(new Dimension(130, 38));
        btnXuat.addActionListener(e -> xuatCSV());

        btnChinhSua = nut("Chỉnh sửa", false);
        btnChinhSua.setPreferredSize(new Dimension(100, 38));
        btnChinhSua.setEnabled(false);
        btnChinhSua.addActionListener(e -> {
            DichVuData d = dichVuDaChon();
            if (d != null && hopThoai(d, "Sửa thông tin dịch vụ", false)) {
                if (service.update(d)) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật dịch vụ " + d.ma + " thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    napDuLieuTuDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dịch vụ vào cơ sở dữ liệu!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnThietLapGia = nut("Thiết lập giá", false);
        btnThietLapGia.setPreferredSize(new Dimension(120, 38));
        btnThietLapGia.setEnabled(false);
        btnThietLapGia.addActionListener(e -> {
            DichVuData d = dichVuDaChon();
            if (d != null)
                datGia(d);
        });

        btnXoa = nut("Xóa", false);
        btnXoa.setPreferredSize(new Dimension(80, 38));
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> {
            DichVuData d = dichVuDaChon();
            if (d != null)
                xoa(d);
        });

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trai.setOpaque(false);
        trai.add(txtTim);
        trai.add(new JLabel("Nhóm:"));
        trai.add(cboNhom);
        trai.add(new JLabel("Trạng thái:"));
        trai.add(cboTrangThai);
        trai.add(btnLamMoi);

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
        String[] cot = { "Mã dịch vụ", "Tên dịch vụ", "Mô tả", "Thời gian", "Giá", "Trạng thái", "Bật / tắt" };
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new OBangRenderer());
        int[] rong = { 90, 200, 330, 110, 120, 130, 100 };
        for (int i = 0; i < rong.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(100, 40));
        th.setDefaultRenderer((t, v, s, f, r, c) -> {
            JLabel l = nhan(String.valueOf(v), 12, true, RangBuocGiaoDien.MAU_CHU_TIEU_DE_BANG);
            l.setOpaque(true);
            l.setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            l.setBorder(new EmptyBorder(0, 10, 0, 14));
            l.setHorizontalAlignment(c == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return l;
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                capNhatNutThaoTac();
        });

        // Click vào cột "Bật / tắt" (cột 6) để toggle trạng thái
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r < 0 || r >= hienTai.size())
                    return;
                DichVuData d = hienTai.get(r);
                if (c == 6) {
                    boolean batMoi = !d.bat;
                    if (service.updateStatus(d.ma, batMoi)) {
                        d.bat = batMoi;
                        napLai(true);
                    } else {
                        JOptionPane.showMessageDialog(DichVu.this, "Không thể cập nhật trạng thái trong cơ sở dữ liệu!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        sp.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);

        lblThongTin = nhan(" ", 12, false, RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.add(lblThongTin, BorderLayout.WEST);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        the.add(toolbar, BorderLayout.NORTH);
        the.add(sp, BorderLayout.CENTER);
        the.add(footer, BorderLayout.SOUTH);
        return the;
    }

    // ==========================================
    // LỌC + VẼ LẠI BẢNG
    // ==========================================
    private void napLai(boolean giuTrang) {
        if (table == null)
            return;
        String tu = txtTim.getText().trim().toLowerCase();
        String nhom = (String) cboNhom.getSelectedItem();
        int tt = cboTrangThai.getSelectedIndex(); // 0: tất cả, 1: đang cung cấp, 2: tạm ẩn

        loc = service.filter(tatCa, tu, nhom, tt);

        hienTai = loc;

        table.clearSelection();
        model.setRowCount(0);
        for (DichVuData d : hienTai)
            model.addRow(new Object[] { d.ma, d, d.moTa, d.phut + " phút", dinhDangGia(d.gia), d, d });

        lblThongTin.setText(loc.isEmpty() ? "Không có dịch vụ phù hợp"
                : "Hiển thị tất cả " + loc.size() + " dịch vụ từ cơ sở dữ liệu");
        capNhatThongKe();
        capNhatNutThaoTac();
    }

    private DichVuData dichVuDaChon() {
        if (table == null || table.getSelectedRowCount() != 1)
            return null;
        int r = table.getSelectedRow();
        return r >= 0 && r < hienTai.size() ? hienTai.get(r) : null;
    }

    private void capNhatNutThaoTac() {
        boolean chonMot = table != null && table.getSelectedRowCount() == 1;
        if (btnChinhSua != null)
            btnChinhSua.setEnabled(chonMot);
        if (btnThietLapGia != null)
            btnThietLapGia.setEnabled(chonMot);
        if (btnXoa != null)
            btnXoa.setEnabled(chonMot);
    }

    private void napNhom() {
        dangNap = true;
        Object chon = cboNhom.getSelectedItem();
        cboNhom.removeAllItems();
        cboNhom.addItem("Tất cả");
        service.categories(tatCa).forEach(cboNhom::addItem);
        cboNhom.setSelectedItem(chon != null ? chon : "Tất cả");
        if (cboNhom.getSelectedItem() == null)
            cboNhom.setSelectedIndex(0);
        dangNap = false;
    }

    // ==========================================
    // CÁC THAO TÁC: THÊM / SỬA / ĐẶT GIÁ / XÓA / XUẤT CSV
    // ==========================================
    private boolean hopThoai(DichVuData d, String tieuDe, boolean laThemMoi) {
        JTextField txtMa = new JTextField(d.ma);
        txtMa.setEditable(false);
        JTextField ten = new JTextField(d.ten);
        JTextField mo = new JTextField(d.moTa);
        JTextField phut = new JTextField("" + (d.phut > 0 ? d.phut : 30));
        JTextField gia = new JTextField("" + d.gia);
        JComboBox<String> cboTrangThaiDialog = new JComboBox<>(new String[] { "Đang áp dụng", "Tạm ẩn" });
        cboTrangThaiDialog.setSelectedItem(d.bat ? "Đang áp dụng" : "Tạm ẩn");

        Object[] form = {
                "Mã dịch vụ", txtMa,
                "Tên dịch vụ", ten,
                "Mô tả dịch vụ", mo,
                "Thời gian thực hiện (phút)", phut,
                "Giá dịch vụ (VNĐ)", gia,
                "Trạng thái", cboTrangThaiDialog
        };

        while (JOptionPane.showConfirmDialog(this, form, tieuDe, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                DichVuData parsed = service.parseForm(txtMa.getText(), ten.getText(), mo.getText(),
                        phut.getText(), gia.getText(), "Đang áp dụng".equals(cboTrangThaiDialog.getSelectedItem()));
                d.ma = parsed.ma;
                d.ten = parsed.ten;
                d.nhom = parsed.nhom;
                d.moTa = parsed.moTa;
                d.phut = parsed.phut;
                d.gia = parsed.gia;
                d.bat = parsed.bat;
                return true;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void datGia(DichVuData d) {
        String s = JOptionPane.showInputDialog(this, "Giá mới cho \"" + d.ten + "\" (VNĐ):", d.gia);
        if (s == null)
            return;
        try {
            long giaMoi = service.parsePrice(s);
            if (service.updatePrice(d, giaMoi)) {
                d.gia = giaMoi;
                JOptionPane.showMessageDialog(this, "Đã cập nhật giá dịch vụ thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                napDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật giá trong cơ sở dữ liệu!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa(DichVuData d) {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dịch vụ \"" + d.ten + "\" (" + d.ma + ") khỏi cơ sở dữ liệu?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (xacNhan == JOptionPane.YES_OPTION) {
            if (service.delete(d.ma)) {
                JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ " + d.ma + " thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                napDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể xóa dịch vụ này vì đã có dữ liệu hóa đơn hoặc lịch hẹn liên kết!\n"
                                + "Gợi ý: Bạn có thể tắt công tắc để chuyển trạng thái dịch vụ sang \"Tạm ẩn\".",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void xuatCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh_sach_dich_vu.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        try {
            Files.write(fc.getSelectedFile().toPath(), service.toCsv(loc).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Xuất file CSV thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // VẼ Ô CỦA BẢNG
    // ==========================================
    private static class OBangRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            Color nen = s ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_NEN_THE;
            if (c == 0)
                return o(nhan(String.valueOf(v), 13, false, RangBuocGiaoDien.MAU_CHU_DAO), nen);
            if (c == 2 || c == 3)
                return o(nhan(String.valueOf(v), 13, false, RangBuocGiaoDien.MAU_CHU_CHINH), nen);
            if (c == 4) {
                JLabel l = nhan(String.valueOf(v), 13, true, RangBuocGiaoDien.MAU_CHU_CHINH);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setBorder(new EmptyBorder(0, 10, 0, 14));
                return o(l, nen);
            }
            if (!(v instanceof DichVuData))
                return o(nhan(String.valueOf(v), 13, false, RangBuocGiaoDien.MAU_CHU_CHINH), nen);
            DichVuData d = (DichVuData) v;
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
                    hai.add(nhan(d.ten, 13, true, RangBuocGiaoDien.MAU_CHU_CHINH));
                    hai.add(nhan(d.nhom, 11, false, RangBuocGiaoDien.MAU_CHU_PHU));
                    p.add(hai, g);
                    break;
                case 5: // trạng thái
                    JLabel tt = nhan(d.bat ? "Đang áp dụng" : "Tạm ẩn", 11, true, d.bat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_PHU);
                    tt.setOpaque(true);
                    tt.setBackground(d.bat ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
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
                    p.add(nhan(d.bat ? "Bật" : "Tắt", 12, false, RangBuocGiaoDien.MAU_CHU_PHU), g);
                    break;
            }
            return p;
        }

        private static JLabel o(JLabel l, Color nen) {
            l.setOpaque(true);
            l.setBackground(nen);
            if (l.getBorder() == null)
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
            return l;
        }
    }

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
            g2.setColor(bat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_GOI_Y);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(RangBuocGiaoDien.MAU_NEN_THE);
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
        b.setBackground(noiBat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_NEN_THE);
        b.setForeground(noiBat ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_CHU_CHINH);
        b.setBorder(noiBat ? new EmptyBorder(8, 14, 8, 14) : new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static String dinhDangGia(long v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", s).format(v) + " ₫";
    }
}