package GUI.HoaDon;

import dao.HoaDon_Dao;
import entity.HoaDon_entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DSHoaDon_gui extends JPanel {

    private final HoaDon_Dao hoaDonDAO = new HoaDon_Dao();

    private final Color XANH = Color.decode("#0F766E");
    private final Color NEN = new Color(248, 250, 252);
    private final Color VIEN = new Color(226, 232, 240);
    private final Color CHU = new Color(30, 41, 59);

    private final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField txtTimKiem = new JTextField();

    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {
            "Tất cả",
            "Đã thanh toán",
            "Chưa thanh toán"
    });

    private final JLabel lblTong = new JLabel("0");
    private final JLabel lblDaThanhToan = new JLabel("0");
    private final JLabel lblChuaThanhToan = new JLabel("0");

    // Cụm nút thao tác độc lập trên toolbar
    private final JButton btnLapHoaDon = new JButton("+ Lập Hóa Đơn");
    private final JButton btnXemChiTiet = new JButton("Xem chi tiết");
    private final JButton btnXoa = new JButton("Xóa");
    private final JButton btnXuat = new JButton("Xuất Excel");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {
                    "Mã Hóa Đơn",
                    "Ngày Lập",
                    "Trạng Thái",
                    "Mã Khách Hàng",
                    "Mã Nhân Viên",
                    "Mã Khuyến Mãi",
                    "Mã Thuế",
                    "Mã Thanh Toán"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private ArrayList<HoaDon_entity> danhSach = new ArrayList<>();

    // Nhận hành động chuyển sang màn hình lập hóa đơn nếu cần.
    private final Runnable hanhDongLapHoaDon;

    public DSHoaDon_gui() {
        this(null);
    }

    public DSHoaDon_gui(Runnable hanhDongLapHoaDon) {
        this.hanhDongLapHoaDon = hanhDongLapHoaDon;

        setLayout(new BorderLayout());
        setBackground(NEN);

        add(taoNoiDung(), BorderLayout.CENTER);

        caiDatSuKien();
        taiDuLieu();
    }

    // ==================== GIAO DIỆN CHÍNH ====================

    private JPanel taoNoiDung() {

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(NEN);
        main.setBorder(new EmptyBorder(14, 14, 12, 14));

        // Tiêu đề
        JPanel tieuDe = new JPanel(new BorderLayout());
        tieuDe.setOpaque(false);

        JPanel thongTinTieuDe = new JPanel();
        thongTinTieuDe.setLayout(
                new BoxLayout(thongTinTieuDe, BoxLayout.Y_AXIS));
        thongTinTieuDe.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Danh sách hóa đơn");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 23));
        lblTieuDe.setForeground(CHU);

        JLabel lblMoTa = new JLabel(
                "Theo dõi và tra cứu thông tin hóa đơn của cửa hàng.");
        lblMoTa.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMoTa.setForeground(new Color(100, 116, 139));

        thongTinTieuDe.add(lblTieuDe);
        thongTinTieuDe.add(Box.createVerticalStrut(4));
        thongTinTieuDe.add(lblMoTa);

        tieuDe.add(thongTinTieuDe, BorderLayout.CENTER);

        // Ba thẻ thống kê
        JPanel thongKe = new JPanel(new GridLayout(1, 3, 12, 0));
        thongKe.setOpaque(false);

        thongKe.add(taoTheThongKe(
                "Tổng số hóa đơn", lblTong));

        thongKe.add(taoTheThongKe(
                "Đã thanh toán", lblDaThanhToan));

        thongKe.add(taoTheThongKe(
                "Chưa thanh toán", lblChuaThanhToan));

        // Khu vực bảng
        JPanel khuVucBang = new JPanel(new BorderLayout(0, 12));
        khuVucBang.setBackground(Color.WHITE);
        khuVucBang.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN, 1, true),
                new EmptyBorder(12, 10, 8, 10)));

        JPanel boLoc = taoBoLoc();

        caiDatBang();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(VIEN, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        khuVucBang.add(boLoc, BorderLayout.NORTH);
        khuVucBang.add(scroll, BorderLayout.CENTER);

        JPanel phiaTren = new JPanel(new BorderLayout(0, 14));
        phiaTren.setOpaque(false);
        phiaTren.add(tieuDe, BorderLayout.NORTH);
        phiaTren.add(thongKe, BorderLayout.CENTER);

        main.add(phiaTren, BorderLayout.NORTH);
        main.add(khuVucBang, BorderLayout.CENTER);

        return main;
    }

    // ==================== THẺ THỐNG KÊ ====================

    private JPanel taoTheThongKe(String tieuDe, JLabel giaTri) {

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(new Color(240, 253, 250));

        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(204, 251, 241), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel chu = new JPanel();
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.setOpaque(false);

        JLabel lblTen = new JLabel(tieuDe);
        lblTen.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 116, 139));

        giaTri.setFont(new Font("Arial", Font.BOLD, 21));
        giaTri.setForeground(CHU);

        chu.add(lblTen);
        chu.add(Box.createVerticalStrut(4));
        chu.add(giaTri);

        the.add(chu, BorderLayout.CENTER);

        return the;
    }

    // ==================== THANH TÌM KIẾM VÀ LỌC ====================

    private JPanel taoBoLoc() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel benTrai = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 8, 0));
        benTrai.setOpaque(false);

        txtTimKiem.setPreferredSize(new Dimension(230, 34));
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 12));
        txtTimKiem.setToolTipText("Tìm theo mã hóa đơn");

        cboTrangThai.setPreferredSize(new Dimension(145, 34));
        cboTrangThai.setFont(new Font("Arial", Font.PLAIN, 12));

        benTrai.add(txtTimKiem);
        benTrai.add(cboTrangThai);

        JPanel benPhai = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 8, 0));
        benPhai.setOpaque(false);

        btnLapHoaDon.setFont(new Font("Arial", Font.BOLD, 12));
        btnLapHoaDon.setForeground(Color.WHITE);
        btnLapHoaDon.setBackground(Color.decode("#0F766E"));
        btnLapHoaDon.setOpaque(true);
        btnLapHoaDon.setContentAreaFilled(true);
        btnLapHoaDon.setBorderPainted(false);
        btnLapHoaDon.setFocusPainted(false);
        btnLapHoaDon.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnLapHoaDon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnXemChiTiet.setFont(new Font("Arial", Font.PLAIN, 12));
        btnXemChiTiet.setForeground(CHU);
        btnXemChiTiet.setBackground(Color.WHITE);
        btnXemChiTiet.setFocusPainted(false);
        btnXemChiTiet.setBorder(new LineBorder(VIEN, 1, true));
        btnXemChiTiet.setPreferredSize(new Dimension(105, 34));
        btnXemChiTiet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnXoa.setFont(new Font("Arial", Font.PLAIN, 12));
        btnXoa.setForeground(new Color(220, 38, 38));
        btnXoa.setBackground(Color.WHITE);
        btnXoa.setFocusPainted(false);
        btnXoa.setBorder(new LineBorder(new Color(254, 202, 202), 1, true));
        btnXoa.setPreferredSize(new Dimension(75, 34));
        btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnXuat.setFont(new Font("Arial", Font.PLAIN, 12));
        btnXuat.setForeground(CHU);
        btnXuat.setBackground(Color.WHITE);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorder(new LineBorder(VIEN, 1, true));
        btnXuat.setPreferredSize(new Dimension(105, 34));
        btnXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnXemChiTiet.setEnabled(false);
        btnXoa.setEnabled(false);

        benPhai.add(btnLapHoaDon);
        benPhai.add(btnXemChiTiet);
        benPhai.add(btnXoa);
        benPhai.add(btnXuat);

        panel.add(benTrai, BorderLayout.CENTER);
        panel.add(benPhai, BorderLayout.EAST);

        return panel;
    }

    // ==================== CÀI ĐẶT BẢNG ====================

    private void caiDatBang() {

        table.setRowHeight(34);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(CHU);
        table.setSelectionBackground(new Color(204, 251, 241));
        table.setSelectionForeground(CHU);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setReorderingAllowed(false);

        // Màu trạng thái
        table.getColumnModel().getColumn(2).setCellRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean selected,
                            boolean focus, int row, int column) {

                        JLabel label = new JLabel(
                                value == null ? "" : value.toString());

                        label.setOpaque(true);
                        label.setFont(new Font("Arial", Font.PLAIN, 11));
                        label.setHorizontalAlignment(SwingConstants.CENTER);

                        String tt = value == null
                                ? ""
                                : value.toString();

                        if (laDaThanhToan(tt)) {
                            label.setForeground(new Color(21, 128, 61));
                            label.setBackground(new Color(220, 252, 231));
                        } else {
                            label.setForeground(new Color(180, 83, 9));
                            label.setBackground(new Color(254, 243, 199));
                        }

                        return label;
                    }
                });
    }

    // ==================== SỰ KIỆN ====================

    private void caiDatSuKien() {

        // Nhấn Enter để tìm kiếm
        txtTimKiem.addActionListener(e -> locDuLieu());

        // Thay đổi trạng thái sẽ lọc ngay
        cboTrangThai.addActionListener(e -> locDuLieu());

        // Sự kiện cụm nút thao tác
        btnLapHoaDon.addActionListener(e -> {
            if (hanhDongLapHoaDon != null) {
                hanhDongLapHoaDon.run();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy kết nối nút này với màn hình lập hóa đơn.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnXemChiTiet.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                String maHD = model.getValueAt(modelRow, 0).toString();
                hienThiChiTiet(maHD);
            }
        });

        btnXoa.addActionListener(e -> xoaHoaDon());
        btnXuat.addActionListener(e -> xuatCSV());

        // Lắng nghe chọn dòng: chỉ bật Xem chi tiết / Xóa khi có dòng được chọn
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = table.getSelectedRow() != -1;
                btnXemChiTiet.setEnabled(coChon);
                btnXoa.setEnabled(coChon);
            }
        });

        // Click đúp vào dòng để xem chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow != -1) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        String maHD = model.getValueAt(modelRow, 0).toString();
                        hienThiChiTiet(maHD);
                    }
                }
            }
        });
    }

    // ==================== TẢI DỮ LIỆU SQL ====================

    private void taiDuLieu() {

        danhSach = hoaDonDAO.getDSHD();

        if (danhSach == null) {
            danhSach = new ArrayList<>();
        }

        capNhatThongKe(danhSach);
        hienThiBang(danhSach);
    }

    private void capNhatThongKe(ArrayList<HoaDon_entity> ds) {

        int daThanhToan = 0;
        int chuaThanhToan = 0;

        for (HoaDon_entity hd : ds) {
            if (laDaThanhToan(hd.getTrangThai())) {
                daThanhToan++;
            } else {
                chuaThanhToan++;
            }
        }

        lblTong.setText(String.valueOf(ds.size()));
        lblDaThanhToan.setText(String.valueOf(daThanhToan));
        lblChuaThanhToan.setText(String.valueOf(chuaThanhToan));
    }

    private boolean laDaThanhToan(String trangThai) {

        if (trangThai == null) {
            return false;
        }

        return trangThai.equalsIgnoreCase("Đã thanh toán")
                || trangThai.equalsIgnoreCase("Paid");
    }

    private void hienThiBang(ArrayList<HoaDon_entity> ds) {

        model.setRowCount(0);

        for (HoaDon_entity hd : ds) {

            model.addRow(new Object[] {
                    hd.getMaHD(),

                    hd.getNgayLap() == null
                            ? ""
                            : hd.getNgayLap().format(DMY),

                    hd.getTrangThai(),

                    layMa(hd.getMaKH()),
                    layMa(hd.getMaNhanVien()),
                    layMa(hd.getMaKM()),
                    layMa(hd.getMaThue()),
                    layMa(hd.getMaThanhToan())
            });
        }
    }

    // ==================== LẤY MÃ KHÓA NGOẠI ====================

    private String layMa(Object doiTuong) {

        if (doiTuong == null) {
            return "NULL";
        }

        String[] cacGetter = {
                "getMaKH",
                "getMaNhanVien",
                "getMaKM",
                "getMaThue",
                "getMaThanhToan",
                "getMaNCC",
                "getMaDV",
                "getMaVT",
                "getMa"
        };

        for (String tenGetter : cacGetter) {

            try {
                Method method = doiTuong.getClass().getMethod(tenGetter);

                Object ketQua = method.invoke(doiTuong);

                if (ketQua != null) {
                    return ketQua.toString();
                }

            } catch (ReflectiveOperationException ignored) {
                // Thử getter tiếp theo
            }
        }

        return doiTuong.toString();
    }

    // ==================== TÌM KIẾM VÀ LỌC TRẠNG THÁI ====================

    private void locDuLieu() {

        String tuKhoa = txtTimKiem.getText()
                .trim().toLowerCase();

        String trangThaiChon = cboTrangThai.getSelectedItem().toString();

        ArrayList<HoaDon_entity> ketQua = new ArrayList<>();

        for (HoaDon_entity hd : danhSach) {

            // Tìm theo mã hóa đơn
            String maHD = hd.getMaHD() == null
                    ? ""
                    : hd.getMaHD().toLowerCase();

            if (!maHD.contains(tuKhoa)) {
                continue;
            }

            // Lọc trạng thái
            boolean daTT = laDaThanhToan(hd.getTrangThai());

            if (trangThaiChon.equals("Đã thanh toán") && !daTT) {
                continue;
            }

            if (trangThaiChon.equals("Chưa thanh toán") && daTT) {
                continue;
            }

            ketQua.add(hd);
        }

        hienThiBang(ketQua);
    }

    // ==================== XEM CHI TIẾT HÓA ĐƠN ====================

    private void hienThiChiTiet(String maHD) {

        // Mở màn hình chi tiết của đúng hóa đơn ở dòng được chọn
        ChiTietHoaDon_gui.hienThi(this, maHD);
    }

    // ==================== XÓA HÓA ĐƠN ====================

    private void xoaHoaDon() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một hóa đơn cần xóa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Đổi chỉ số dòng hiển thị sang chỉ số model (do có sort/lọc)
        int modelRow = table.convertRowIndexToModel(viewRow);
        String maHD = model.getValueAt(modelRow, 0).toString();

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa hóa đơn " + maHD + " không?\n"
                        + "Các chi tiết của hóa đơn cũng sẽ bị xóa.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        if (hoaDonDAO.xoaHD(maHD)) {

            // Tải lại dữ liệu từ CSDL, cập nhật thống kê
            taiDuLieu();

            // Giữ nguyên bộ lọc đang áp dụng
            locDuLieu();

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa hóa đơn " + maHD + ".",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa hóa đơn " + maHD + ".",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== XUẤT DANH SÁCH CSV ====================

    private void xuatCSV() {

        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
                new File("DanhSachHoaDon.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {

            // BOM giúp Excel đọc tiếng Việt UTF-8
            writer.write('\uFEFF');

            // Tiêu đề cột
            for (int c = 0; c < model.getColumnCount(); c++) {

                if (c > 0) {
                    writer.print(",");
                }

                writer.print("\""
                        + model.getColumnName(c) + "\"");
            }

            writer.println();

            // Chỉ xuất các dòng đang hiển thị
            for (int r = 0; r < table.getRowCount(); r++) {

                for (int c = 0; c < table.getColumnCount(); c++) {

                    if (c > 0) {
                        writer.print(",");
                    }

                    Object value = table.getValueAt(r, c);

                    String text = value == null
                            ? ""
                            : value.toString();

                    writer.print("\""
                            + text.replace("\"", "\"\"")
                            + "\"");
                }

                writer.println();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Xuất danh sách thành công.");

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xuất file: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
