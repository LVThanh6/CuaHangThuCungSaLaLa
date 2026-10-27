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

    private final JLabel lblTong = new JLabel("Đang tải...");
    private final JLabel lblDaThanhToan = new JLabel("...");
    private final JLabel lblChuaThanhToan = new JLabel("...");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {
                    "Mã Hóa Đơn",
                    "Ngày Lập",
                    "Trạng Thái",
                    "Mã Khách Hàng",
                    "Mã Nhân Viên",
                    "Mã Khuyến Mãi",
                    "Mã Thuế",
                    "Mã Thanh Toán",
                    "Thao tác"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private ArrayList<HoaDon_entity> danhSach = new ArrayList<>();

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

        JButton btnLapHoaDon = new JButton("Lập Hóa Đơn");
        btnLapHoaDon.setFont(new Font("Arial", Font.BOLD, 12));
        btnLapHoaDon.setForeground(Color.WHITE);
        btnLapHoaDon.setBackground(Color.decode("#0F766E"));
        btnLapHoaDon.setOpaque(true);
        btnLapHoaDon.setContentAreaFilled(true);
        btnLapHoaDon.setBorderPainted(false);
        btnLapHoaDon.setFocusPainted(false);
        btnLapHoaDon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLapHoaDon.addActionListener(e -> {
            if (hanhDongLapHoaDon != null) {
                hanhDongLapHoaDon.run();
            } else {
                JOptionPane.showMessageDialog(this, "Hãy kết nối nút này với màn hình lập hóa đơn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        tieuDe.add(thongTinTieuDe, BorderLayout.CENTER);
        tieuDe.add(btnLapHoaDon, BorderLayout.EAST);

        JPanel thongKe = new JPanel(new GridLayout(1, 3, 12, 0));
        thongKe.setOpaque(false);
        thongKe.add(taoTheThongKe("Tổng số hóa đơn", lblTong));
        thongKe.add(taoTheThongKe("Đã thanh toán", lblDaThanhToan));
        thongKe.add(taoTheThongKe("Chưa thanh toán", lblChuaThanhToan));

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

    private JPanel taoBoLoc() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel benTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        benTrai.setOpaque(false);

        txtTimKiem.setPreferredSize(new Dimension(230, 34));
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 12));
        txtTimKiem.setToolTipText("Tìm theo mã hóa đơn");

        cboTrangThai.setPreferredSize(new Dimension(145, 34));
        cboTrangThai.setFont(new Font("Arial", Font.PLAIN, 12));

        benTrai.add(txtTimKiem);
        benTrai.add(cboTrangThai);

        JPanel benPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        benPhai.setOpaque(false);

        JButton btnXoa = new JButton("Xóa");
        btnXoa.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoa.setForeground(Color.WHITE);
        btnXoa.setBackground(new Color(220, 38, 38));
        btnXoa.setOpaque(true);
        btnXoa.setContentAreaFilled(true);
        btnXoa.setBorderPainted(false);
        btnXoa.setFocusPainted(false);
        btnXoa.setBorder(new EmptyBorder(9, 16, 9, 16));
        btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXoa.addActionListener(e -> xoaHoaDon());

        JButton btnXuat = new JButton("Xuất Danh Sách");
        btnXuat.setFont(new Font("Arial", Font.BOLD, 12));
        btnXuat.setForeground(CHU);
        btnXuat.setBackground(Color.WHITE);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorder(BorderFactory.createCompoundBorder(new LineBorder(VIEN, 1, true), new EmptyBorder(9, 12, 9, 12)));
        btnXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXuat.addActionListener(e -> xuatCSV());

        benPhai.add(btnXoa);
        benPhai.add(btnXuat);

        panel.add(benTrai, BorderLayout.CENTER);
        panel.add(benPhai, BorderLayout.EAST);
        return panel;
    }

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

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
                JButton btnXem = new JButton("Xem");
                btnXem.setFont(new Font("Arial", Font.PLAIN, 11));
                btnXem.setForeground(XANH);
                btnXem.setBackground(new Color(240, 253, 250));
                btnXem.setBorder(new LineBorder(new Color(204, 251, 241), 1, true));
                btnXem.setFocusPainted(false);
                return btnXem;
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setFont(new Font("Arial", Font.PLAIN, 11));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String tt = value == null ? "" : value.toString();

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

    private void caiDatSuKien() {
        txtTimKiem.addActionListener(e -> locDuLieu());
        cboTrangThai.addActionListener(e -> locDuLieu());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol != 8) return;

                int modelRow = table.convertRowIndexToModel(viewRow);
                String maHD = model.getValueAt(modelRow, 0).toString();
                hienThiChiTiet(maHD);
            }
        });
    }

    // TỐI ƯU HÓA: Chạy load dữ liệu ngầm để không bị đơ giật UI
    private void taiDuLieu() {
        SwingWorker<ArrayList<HoaDon_entity>, Void> worker = new SwingWorker<>() {
            @Override
            protected ArrayList<HoaDon_entity> doInBackground() {
                return hoaDonDAO.getDSHD();
            }

            @Override
            protected void done() {
                try {
                    danhSach = get();
                    if (danhSach == null) danhSach = new ArrayList<>();
                    capNhatThongKe(danhSach);
                    hienThiBang(danhSach);
                } catch (Exception e) {
                    e.printStackTrace();
                    lblTong.setText("Lỗi");
                }
            }
        };
        worker.execute();
    }

    private void capNhatThongKe(ArrayList<HoaDon_entity> ds) {
        int daThanhToan = 0;
        int chuaThanhToan = 0;
        for (HoaDon_entity hd : ds) {
            if (laDaThanhToan(hd.getTrangThai())) daThanhToan++;
            else chuaThanhToan++;
        }
        lblTong.setText(String.valueOf(ds.size()));
        lblDaThanhToan.setText(String.valueOf(daThanhToan));
        lblChuaThanhToan.setText(String.valueOf(chuaThanhToan));
    }

    private boolean laDaThanhToan(String trangThai) {
        if (trangThai == null) return false;
        return trangThai.equalsIgnoreCase("Đã thanh toán") || trangThai.equalsIgnoreCase("Paid");
    }

    private void hienThiBang(ArrayList<HoaDon_entity> ds) {
        model.setRowCount(0);
        for (HoaDon_entity hd : ds) {
            model.addRow(new Object[] {
                    hd.getMaHD(),
                    hd.getNgayLap() == null ? "" : hd.getNgayLap().format(DMY),
                    hd.getTrangThai(),
                    layMa(hd.getMaKH()),
                    layMa(hd.getMaNhanVien()),
                    layMa(hd.getMaKM()),
                    layMa(hd.getMaThue()),
                    layMa(hd.getMaThanhToan()),
                    "Xem"
            });
        }
    }

    private String layMa(Object doiTuong) {
        if (doiTuong == null) return "NULL";
        String[] cacGetter = {"getMaKH", "getMaNhanVien", "getMaKM", "getMaThue", "getMaThanhToan", "getMaNCC", "getMaDV", "getMaVT", "getMa"};
        for (String tenGetter : cacGetter) {
            try {
                Method method = doiTuong.getClass().getMethod(tenGetter);
                Object ketQua = method.invoke(doiTuong);
                if (ketQua != null) return ketQua.toString();
            } catch (ReflectiveOperationException ignored) {}
        }
        return doiTuong.toString();
    }

    private void locDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        String trangThaiChon = cboTrangThai.getSelectedItem().toString();
        ArrayList<HoaDon_entity> ketQua = new ArrayList<>();

        for (HoaDon_entity hd : danhSach) {
            String maHD = hd.getMaHD() == null ? "" : hd.getMaHD().toLowerCase();
            if (!maHD.contains(tuKhoa)) continue;

            boolean daTT = laDaThanhToan(hd.getTrangThai());
            if (trangThaiChon.equals("Đã thanh toán") && !daTT) continue;
            if (trangThaiChon.equals("Chưa thanh toán") && daTT) continue;

            ketQua.add(hd);
        }
        hienThiBang(ketQua);
    }

    private void hienThiChiTiet(String maHD) {
        ChiTietHoaDon_gui.hienThi(this, maHD);
    }

    private void xoaHoaDon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        String maHD = model.getValueAt(modelRow, 0).toString();
        int xacNhan = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa hóa đơn " + maHD + " không?\nCác chi tiết của hóa đơn cũng sẽ bị xóa.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        if (hoaDonDAO.xoaHD(maHD)) {
            taiDuLieu();
            JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn " + maHD + ".", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa hóa đơn " + maHD + ".", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("DanhSachHoaDon.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.write('\uFEFF');
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) writer.print(",");
                writer.print("\"" + model.getColumnName(c) + "\"");
            }
            writer.println();

            for (int r = 0; r < table.getRowCount(); r++) {
                for (int c = 0; c < table.getColumnCount(); c++) {
                    if (c > 0) writer.print(",");
                    Object value = table.getValueAt(r, c);
                    String text = value == null ? "" : value.toString();
                    writer.print("\"" + text.replace("\"", "\"\"") + "\"");
                }
                writer.println();
            }
            JOptionPane.showMessageDialog(this, "Xuất danh sách thành công.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}