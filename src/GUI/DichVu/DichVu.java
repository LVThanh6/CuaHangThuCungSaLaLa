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

    private static Font kieuChu(boolean dam, int coChu) {
        return new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, coChu);
    }

    private final DichVuService dichVuService = new DichVuService();

    // ===== Dữ liệu hiển thị =====
    private final List<DichVuData> tatCa = new ArrayList<>();
    private List<DichVuData> loc = new ArrayList<>();
    private List<DichVuData> hienTai = new ArrayList<>();
    private boolean dangNap = false;

    // ===== Thành phần giao diện =====
    private JTextField txtTim;
    private JComboBox<String> cboNhom, cboTrangThai;
    private JTable bangDichVu;
    private DefaultTableModel moHinhBang;
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
        tatCa.addAll(dichVuService.taiDanhSach());
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
        btnThem.addActionListener(suKien -> {
            DichVuData moi = new DichVuData(dichVuService.maTiepTheo(), "", "Dịch vụ chung", "", 30, 0, true);
            if (hopThoai(moi, "Thêm dịch vụ mới", true)) {
                if (dichVuService.tao(moi)) {
                    JOptionPane.showMessageDialog(this, "Đã thêm dịch vụ " + moi.ma + " thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    napDuLieuTuDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể thêm dịch vụ vào cơ sở dữ liệu!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(trai, BorderLayout.WEST);
        panel.add(btnThem, BorderLayout.EAST);
        return panel;
    }

    private JPanel taoThongKe() {
        String[] tieuDe = { "Tổng dịch vụ", "Đang cung cấp", "Tạm ẩn", "Giá trung bình" };
        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        for (int chiSo = 0; chiSo < 4; chiSo++) {
            giaTri[chiSo] = nhan("0", 26, true, RangBuocGiaoDien.MAU_CHU_CHINH);
            phu[chiSo] = nhan(" ", 11, false, RangBuocGiaoDien.MAU_CHU_PHU);
            JPanel the = new JPanel(new GridBagLayout());
            the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
            the.setBorder(new CompoundBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN), new EmptyBorder(10, 16, 10, 16)));
            GridBagConstraints rangBuoc = new GridBagConstraints();
            rangBuoc.gridx = 0;
            rangBuoc.gridy = 0;
            rangBuoc.gridwidth = 2;
            rangBuoc.anchor = GridBagConstraints.WEST;
            the.add(nhan(tieuDe[chiSo], 12, false, RangBuocGiaoDien.MAU_CHU_PHU), rangBuoc);
            rangBuoc.gridy = 1;
            rangBuoc.gridwidth = 1;
            the.add(giaTri[chiSo], rangBuoc);
            rangBuoc.gridx = 1;
            rangBuoc.weightx = 1;
            rangBuoc.insets = new Insets(0, 10, 4, 0);
            rangBuoc.anchor = GridBagConstraints.SOUTHWEST;
            the.add(phu[chiSo], rangBuoc);
            hang.add(the);
        }
        return hang;
    }

    private void capNhatThongKe() {
        DichVuService.Statistics thongKe = dichVuService.thongKe(tatCa);
        giaTri[0].setText("" + thongKe.tong);
        phu[0].setText(thongKe.soNhom + " nhóm dịch vụ");
        giaTri[1].setText("" + thongKe.dangHoatDong);
        phu[1].setText(String.format("%.1f%% danh mục", thongKe.tong == 0 ? 0 : thongKe.dangHoatDong * 100.0 / thongKe.tong)
                .replace('.', ','));
        giaTri[2].setText("" + thongKe.tamAn);
        phu[2].setText("Không nhận lịch mới");
        giaTri[3].setText(dinhDangGia(thongKe.giaTrungBinh));
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
            public void keyReleased(KeyEvent suKien) {
                napLai(false);
            }
        });

        cboNhom = new JComboBox<>();
        cboNhom.setPreferredSize(new Dimension(180, 38));
        cboNhom.addActionListener(suKien -> {
            if (!dangNap)
                napLai(false);
        });
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang cung cấp", "Tạm ẩn" });
        cboTrangThai.setPreferredSize(new Dimension(160, 38));
        cboTrangThai.addActionListener(suKien -> napLai(false));

        JButton btnLamMoi = nut("Làm mới", false);
        btnLamMoi.setPreferredSize(new Dimension(100, 38));
        btnLamMoi.addActionListener(suKien -> napDuLieuTuDatabase());

        JButton btnXuat = nut("Xuất danh sách", false);
        btnXuat.setPreferredSize(new Dimension(130, 38));
        btnXuat.addActionListener(suKien -> xuatCSV());

        btnChinhSua = nut("Chỉnh sửa", false);
        btnChinhSua.setPreferredSize(new Dimension(100, 38));
        btnChinhSua.setEnabled(false);
        btnChinhSua.addActionListener(suKien -> {
            DichVuData dv = dichVuDaChon();
            if (dv != null && hopThoai(dv, "Sửa thông tin dịch vụ", false)) {
                if (dichVuService.capNhat(dv)) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật dịch vụ " + dv.ma + " thành công!", "Thông báo",
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
        btnThietLapGia.addActionListener(suKien -> {
            DichVuData dv = dichVuDaChon();
            if (dv != null)
                datGia(dv);
        });

        btnXoa = nut("Xóa", false);
        btnXoa.setPreferredSize(new Dimension(80, 38));
        btnXoa.setForeground(RangBuocGiaoDien.MAU_CANH_BAO_DO);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(suKien -> {
            DichVuData dv = dichVuDaChon();
            if (dv != null)
                xoa(dv);
        });

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trai.setOpaque(false);
        trai.add(txtTim);
        trai.add(new JLabel("Nhóm:"));
        trai.add(cboNhom);
        trai.add(new JLabel("Trạng thái:"));
        trai.add(cboTrangThai);
        trai.add(btnLamMoi);

        JPanel thanhCongCu = new JPanel(new BorderLayout());
        thanhCongCu.setOpaque(false);
        thanhCongCu.setBorder(new EmptyBorder(16, 20, 16, 20));
        thanhCongCu.add(trai, BorderLayout.WEST);

        JPanel phaiThanhCongCu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiThanhCongCu.setOpaque(false);
        phaiThanhCongCu.add(btnChinhSua);
        phaiThanhCongCu.add(btnThietLapGia);
        phaiThanhCongCu.add(btnXoa);
        phaiThanhCongCu.add(btnXuat);
        thanhCongCu.add(phaiThanhCongCu, BorderLayout.EAST);

        // --- Bảng ---
        String[] cot = { "Mã dịch vụ", "Tên dịch vụ", "Mô tả", "Thời gian", "Giá", "Trạng thái", "Bật / tắt" };
        moHinhBang = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int chiSoDong, int chiSoCot) {
                return false;
            }
        };
        bangDichVu = new JTable(moHinhBang);
        bangDichVu.setRowHeight(48);
        bangDichVu.setShowVerticalLines(false);
        bangDichVu.setGridColor(RangBuocGiaoDien.MAU_LUOI_BANG);
        bangDichVu.setRowSelectionAllowed(true);
        bangDichVu.setColumnSelectionAllowed(false);
        bangDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangDichVu.setFocusable(false);
        bangDichVu.setFillsViewportHeight(true);
        bangDichVu.setDefaultRenderer(Object.class, new OBangRenderer());
        int[] rong = { 90, 200, 330, 110, 120, 130, 100 };
        for (int chiSo = 0; chiSo < rong.length; chiSo++)
            bangDichVu.getColumnModel().getColumn(chiSo).setPreferredWidth(rong[chiSo]);

        JTableHeader tieuDeBang = bangDichVu.getTableHeader();
        tieuDeBang.setReorderingAllowed(false);
        tieuDeBang.setPreferredSize(new Dimension(100, 40));
        tieuDeBang.setDefaultRenderer((bangNguon, duLieu, daChon, coTieuDiem, chiSoDong, chiSoCot) -> {
            JLabel nhanChu = nhan(String.valueOf(duLieu), 12, true, RangBuocGiaoDien.MAU_CHU_TIEU_DE_BANG);
            nhanChu.setOpaque(true);
            nhanChu.setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            nhanChu.setBorder(new EmptyBorder(0, 10, 0, 14));
            nhanChu.setHorizontalAlignment(chiSoCot == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return nhanChu;
        });

        bangDichVu.getSelectionModel().addListSelectionListener(suKien -> {
            if (!suKien.getValueIsAdjusting())
                capNhatNutThaoTac();
        });

        // Click vào cột "Bật / tắt" (cột 6) để toggle trạng thái
        bangDichVu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int chiSoDong = bangDichVu.rowAtPoint(suKien.getPoint()), chiSoCot = bangDichVu.columnAtPoint(suKien.getPoint());
                if (chiSoDong < 0 || chiSoDong >= hienTai.size())
                    return;
                DichVuData dv = hienTai.get(chiSoDong);
                if (chiSoCot == 6) {
                    boolean batMoi = !dv.bat;
                    if (dichVuService.capNhatTrangThai(dv.ma, batMoi)) {
                        dv.bat = batMoi;
                        napLai(true);
                    } else {
                        JOptionPane.showMessageDialog(DichVu.this, "Không thể cập nhật trạng thái trong cơ sở dữ liệu!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JScrollPane cuonBang = new JScrollPane(bangDichVu);
        cuonBang.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN));
        cuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);

        lblThongTin = nhan(" ", 12, false, RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel chanTrang = new JPanel(new BorderLayout());
        chanTrang.setOpaque(false);
        chanTrang.setBorder(new EmptyBorder(10, 20, 10, 20));
        chanTrang.add(lblThongTin, BorderLayout.WEST);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        the.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(cuonBang, BorderLayout.CENTER);
        the.add(chanTrang, BorderLayout.SOUTH);
        return the;
    }

    // ==========================================
    // LỌC + VẼ LẠI BẢNG
    // ==========================================
    private void napLai(boolean giuTrang) {
        if (bangDichVu == null)
            return;
        String tu = txtTim.getText().trim().toLowerCase();
        String nhom = (String) cboNhom.getSelectedItem();
        int tt = cboTrangThai.getSelectedIndex(); // 0: tất cả, 1: đang cung cấp, 2: tạm ẩn

        loc = dichVuService.loc(tatCa, tu, nhom, tt);

        hienTai = loc;

        bangDichVu.clearSelection();
        moHinhBang.setRowCount(0);
        for (DichVuData dv : hienTai)
            moHinhBang.addRow(new Object[] { dv.ma, dv, dv.moTa, dv.phut + " phút", dinhDangGia(dv.gia), dv, dv });

        lblThongTin.setText(loc.isEmpty() ? "Không có dịch vụ phù hợp"
                : "Hiển thị tất cả " + loc.size() + " dịch vụ từ cơ sở dữ liệu");
        capNhatThongKe();
        capNhatNutThaoTac();
    }

    private DichVuData dichVuDaChon() {
        if (bangDichVu == null || bangDichVu.getSelectedRowCount() != 1)
            return null;
        int chiSoDong = bangDichVu.getSelectedRow();
        return chiSoDong >= 0 && chiSoDong < hienTai.size() ? hienTai.get(chiSoDong) : null;
    }

    private void capNhatNutThaoTac() {
        boolean chonMot = bangDichVu != null && bangDichVu.getSelectedRowCount() == 1;
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
        dichVuService.danhSachNhom(tatCa).forEach(cboNhom::addItem);
        cboNhom.setSelectedItem(chon != null ? chon : "Tất cả");
        if (cboNhom.getSelectedItem() == null)
            cboNhom.setSelectedIndex(0);
        dangNap = false;
    }

    // ==========================================
    // CÁC THAO TÁC: THÊM / SỬA / ĐẶT GIÁ / XÓA / XUẤT CSV
    // ==========================================
    private boolean hopThoai(DichVuData dv, String tieuDe, boolean laThemMoi) {
        JTextField txtMa = new JTextField(dv.ma);
        txtMa.setEditable(false);
        JTextField ten = new JTextField(dv.ten);
        JTextField mo = new JTextField(dv.moTa);
        JTextField phut = new JTextField("" + (dv.phut > 0 ? dv.phut : 30));
        JTextField gia = new JTextField("" + dv.gia);
        JComboBox<String> cboTrangThaiHopThoai = new JComboBox<>(new String[] { "Đang áp dụng", "Tạm ẩn" });
        cboTrangThaiHopThoai.setSelectedItem(dv.bat ? "Đang áp dụng" : "Tạm ẩn");

        Object[] bieuMau = {
                "Mã dịch vụ", txtMa,
                "Tên dịch vụ", ten,
                "Mô tả dịch vụ", mo,
                "Thời gian thực hiện (phút)", phut,
                "Giá dịch vụ (VNĐ)", gia,
                "Trạng thái", cboTrangThaiHopThoai
        };

        while (JOptionPane.showConfirmDialog(this, bieuMau, tieuDe, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                DichVuData daPhanTich = dichVuService.phanTichBieuMau(txtMa.getText(), ten.getText(), mo.getText(),
                        phut.getText(), gia.getText(), "Đang áp dụng".equals(cboTrangThaiHopThoai.getSelectedItem()));
                dv.ma = daPhanTich.ma;
                dv.ten = daPhanTich.ten;
                dv.nhom = daPhanTich.nhom;
                dv.moTa = daPhanTich.moTa;
                dv.phut = daPhanTich.phut;
                dv.gia = daPhanTich.gia;
                dv.bat = daPhanTich.bat;
                return true;
            } catch (IllegalArgumentException loi) {
                JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void datGia(DichVuData dv) {
        String chuoiNhap = JOptionPane.showInputDialog(this, "Giá mới cho \"" + dv.ten + "\" (VNĐ):", dv.gia);
        if (chuoiNhap == null)
            return;
        try {
            long giaMoi = dichVuService.phanTichGia(chuoiNhap);
            if (dichVuService.capNhatGia(dv, giaMoi)) {
                dv.gia = giaMoi;
                JOptionPane.showMessageDialog(this, "Đã cập nhật giá dịch vụ thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                napDuLieuTuDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật giá trong cơ sở dữ liệu!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException loi) {
            JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa(DichVuData dv) {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dịch vụ \"" + dv.ten + "\" (" + dv.ma + ") khỏi cơ sở dữ liệu?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (xacNhan == JOptionPane.YES_OPTION) {
            if (dichVuService.xoa(dv.ma)) {
                JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ " + dv.ma + " thành công!", "Thông báo",
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
        JFileChooser hopChonTep = new JFileChooser();
        hopChonTep.setSelectedFile(new File("danh_sach_dich_vu.csv"));
        if (hopChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        try {
            Files.write(hopChonTep.getSelectedFile().toPath(), dichVuService.taoCsv(loc).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Xuất file CSV thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException loi) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + loi.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // VẼ Ô CỦA BẢNG
    // ==========================================
    private static class OBangRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable bangNguon, Object duLieu, boolean daChon, boolean coTieuDiem, int chiSoDong, int chiSoCot) {
            Color nen = daChon ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_NEN_THE;
            if (chiSoCot == 0)
                return datNen(nhan(String.valueOf(duLieu), 13, false, RangBuocGiaoDien.MAU_CHU_DAO), nen);
            if (chiSoCot == 2 || chiSoCot == 3)
                return datNen(nhan(String.valueOf(duLieu), 13, false, RangBuocGiaoDien.MAU_CHU_CHINH), nen);
            if (chiSoCot == 4) {
                JLabel nhanChu = nhan(String.valueOf(duLieu), 13, true, RangBuocGiaoDien.MAU_CHU_CHINH);
                nhanChu.setHorizontalAlignment(SwingConstants.RIGHT);
                nhanChu.setBorder(new EmptyBorder(0, 10, 0, 14));
                return datNen(nhanChu, nen);
            }
            if (!(duLieu instanceof DichVuData))
                return datNen(nhan(String.valueOf(duLieu), 13, false, RangBuocGiaoDien.MAU_CHU_CHINH), nen);
            DichVuData dv = (DichVuData) duLieu;
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(nen);
            GridBagConstraints rangBuoc = new GridBagConstraints();
            rangBuoc.anchor = GridBagConstraints.WEST;
            rangBuoc.weightx = 1;
            switch (chiSoCot) {
                case 1: // tên + nhóm (2 dòng)
                    JPanel hai = new JPanel(new GridLayout(2, 1));
                    hai.setOpaque(false);
                    hai.setBorder(new EmptyBorder(0, 10, 0, 0));
                    hai.add(nhan(dv.ten, 13, true, RangBuocGiaoDien.MAU_CHU_CHINH));
                    hai.add(nhan(dv.nhom, 11, false, RangBuocGiaoDien.MAU_CHU_PHU));
                    panel.add(hai, rangBuoc);
                    break;
                case 5: // trạng thái
                    JLabel tt = nhan(dv.bat ? "Đang áp dụng" : "Tạm ẩn", 11, true, dv.bat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_PHU);
                    tt.setOpaque(true);
                    tt.setBackground(dv.bat ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_TRUNG_TINH_NEN);
                    tt.setBorder(new EmptyBorder(3, 10, 3, 10));
                    rangBuoc.insets = new Insets(0, 10, 0, 0);
                    panel.add(tt, rangBuoc);
                    break;
                case 6: // công tắc bật/tắt
                    rangBuoc.insets = new Insets(0, 10, 0, 0);
                    rangBuoc.weightx = 0;
                    panel.add(new Toggle(dv.bat), rangBuoc);
                    rangBuoc.insets = new Insets(0, 8, 0, 0);
                    rangBuoc.weightx = 1;
                    panel.add(nhan(dv.bat ? "Bật" : "Tắt", 12, false, RangBuocGiaoDien.MAU_CHU_PHU), rangBuoc);
                    break;
            }
            return panel;
        }

        private static JLabel datNen(JLabel nhanChu, Color nen) {
            nhanChu.setOpaque(true);
            nhanChu.setBackground(nen);
            if (nhanChu.getBorder() == null)
                nhanChu.setBorder(new EmptyBorder(0, 10, 0, 10));
            return nhanChu;
        }
    }

    private static class Toggle extends JComponent {
        private final boolean bat;

        Toggle(boolean bat) {
            this.bat = bat;
            setPreferredSize(new Dimension(38, 22));
        }

        @Override
        protected void paintComponent(Graphics doHoa) {
            Graphics2D doHoa2D = (Graphics2D) doHoa.create();
            doHoa2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            doHoa2D.setColor(bat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_CHU_GOI_Y);
            doHoa2D.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            doHoa2D.setColor(RangBuocGiaoDien.MAU_NEN_THE);
            doHoa2D.fillOval(bat ? getWidth() - getHeight() + 3 : 3, 3, getHeight() - 6, getHeight() - 6);
            doHoa2D.dispose();
        }
    }

    // ==========================================
    // HÀM TIỆN ÍCH
    // ==========================================
    private static JLabel nhan(String noiDung, int coChu, boolean dam, Color mau) {
        JLabel nhanChu = new JLabel(noiDung);
        nhanChu.setFont(kieuChu(dam, coChu));
        nhanChu.setForeground(mau);
        return nhanChu;
    }

    private static JButton nut(String noiDung, boolean noiBat) {
        JButton nutBam = new JButton(noiDung);
        nutBam.setFont(kieuChu(true, 13));
        nutBam.setFocusPainted(false);
        nutBam.setContentAreaFilled(false);
        nutBam.setOpaque(true);
        nutBam.setBackground(noiBat ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_NEN_THE);
        nutBam.setForeground(noiBat ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_CHU_CHINH);
        nutBam.setBorder(noiBat ? new EmptyBorder(8, 14, 8, 14) : new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
        nutBam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return nutBam;
    }

    private static String dinhDangGia(long soTien) {
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols();
        kyHieu.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", kyHieu).format(soTien) + " ₫";
    }
}