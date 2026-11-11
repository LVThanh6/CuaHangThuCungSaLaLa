package GUI.LichHen;

import GUI.Chung.TableMau;
import GUI.Chung.RangBuocGiaoDien;
import Service.LichHen.XemLichService;
import entity.DichVu.DichVu_entity;
import entity.LichHen.LichHen_entity;
import entity.NhanVien.NhanVien_entity;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class XemLichGUI extends JPanel {

    private static final int BAN_KINH_THE = 12;
    private static final int BAN_KINH_O = 8;
    private static final int CHIEU_CAO_O = 38;
    private static final int LE_TRANG = 20;
    private static final int KHOANG_CACH = 16;

    private static final int COT_THOI_GIAN = 0;
    private static final int COT_TRANG_THAI = 5;

    private static final String[] TIEU_DE_BANG = {
            "Thời gian", "Tên thú cưng", "Chủ nuôi", "Nhân viên phụ trách", "Nội dung khám", "Trạng thái"
    };
    private static final int[] DO_RONG_COT = { 170, 150, 215, 190, 300, 165 };

    private static final String[] DANH_SACH_NHAN_VIEN = { "Nhân viên: Tất cả" };
    private static final String[] DANH_SACH_TRANG_THAI = {
            "Trạng thái: Tất cả", "Chờ khám", "Đang khám", "Đã khám", "Đã hủy"
    };
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final XemLichService service = new XemLichService();
    private List<LichHen_entity> danhSachLichHen = new ArrayList<>();
    private List<LichHen_entity> danhSachDangHienThi = new ArrayList<>();
    private LocalDate tuNgay;
    private LocalDate denNgay;

    private JPanel panelPhanTren;
    private JPanel panelDauTrang;
    private JLabel nhanDuongDan;
    private JLabel nhanTieuDe;
    private JLabel nhanMoTa;
    private NutBo nutTaoLichKham;

    private JPanel panelThongKe;
    private TheThongKe theLichHomNay;
    private TheThongKe theChoKham;
    private TheThongKe theDangKham;
    private TheThongKe theDaKhamDaHuy;

    private TheBoGoc theDanhSach;

    private JPanel panelThanhCongCu;
    private TheBoGoc oChonNgay;
    private JLabel nhanNgayLoc;
    private ComboHienDai cboBacSi;
    private ComboHienDai cboTrangThai;
    private TheBoGoc panelCheDoXem;
    private NutBo nutThang;
    private NutBo nutTuan;
    private NutBo nutDanhSach;
    private NutBo nutXemChiTiet;
    private NutBo nutSuaLich;
    private NutBo nutHuyLich;

    private TableMau bangLichKham;
    private JScrollPane khungCuonBang;

    private JPanel panelChanBang;
    private JLabel nhanThongTinHienThi;
    private JLabel nhanGoiYChon;

    public XemLichGUI() {
        khoiTaoGiaoDien();
        caiDatSuKien();
        loadDataToTable();
    }

    //Hàm gốc định hình toàn bộ khung màn hình chính
    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, KHOANG_CACH));
        setBackground(RangBuocGiaoDien.MAU_NEN_TRANG);
        setBorder(BorderFactory.createEmptyBorder(16, LE_TRANG, 20, LE_TRANG));

        panelPhanTren = new JPanel(new BorderLayout(0, KHOANG_CACH));
        panelPhanTren.setOpaque(false);
        panelPhanTren.add(taoPhanDauTrang(), BorderLayout.NORTH);
        panelPhanTren.add(taoHangThongKe(), BorderLayout.CENTER);
        add(panelPhanTren, BorderLayout.NORTH);

        add(taoTheDanhSach(), BorderLayout.CENTER);
    }

    //Tạo dòng chữ "Lịch khám sức khỏe" và đặt nút bấm "Tạo lịch khám" ở góc phải.
    private JPanel taoPhanDauTrang() {
        panelDauTrang = new JPanel(new BorderLayout(0, 8));
        panelDauTrang.setOpaque(false);

        nhanDuongDan = new JLabel("Lịch hẹn / Lịch khám sức khỏe");
        nhanDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanTieuDe = new JLabel("Lịch khám sức khỏe");
        nhanTieuDe.setFont(RangBuocGiaoDien.fontDam(26));
        nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        nhanMoTa = new JLabel("Theo dõi tiến độ khám và lịch tái khám của thú cưng.");
        nhanMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel khoiTieuDe = new JPanel(new BorderLayout(0, 2));
        khoiTieuDe.setOpaque(false);
        khoiTieuDe.add(nhanTieuDe, BorderLayout.NORTH);
        khoiTieuDe.add(nhanMoTa, BorderLayout.CENTER);

        nutTaoLichKham = new NutBo("Tạo lịch khám", NutBo.KIEU_CHINH);
        nutTaoLichKham.setFont(RangBuocGiaoDien.fontDam(13));
        nutTaoLichKham.setPreferredSize(new Dimension(135, 40));

        JPanel khoiNut = new JPanel(new GridBagLayout());
        khoiNut.setOpaque(false);
        khoiNut.add(nutTaoLichKham);

        JPanel hangTieuDe = new JPanel(new BorderLayout());
        hangTieuDe.setOpaque(false);
        hangTieuDe.add(khoiTieuDe, BorderLayout.WEST);
        hangTieuDe.add(khoiNut, BorderLayout.EAST);

        panelDauTrang.add(nhanDuongDan, BorderLayout.NORTH);
        panelDauTrang.add(hangTieuDe, BorderLayout.CENTER);
        return panelDauTrang;
    }

    //Dựng 4 thẻ hiển thị con số nằm ngang (Lịch hôm nay, Chờ khám, Đang khám, Đã khám/Hủy).
    private JPanel taoHangThongKe() {
        panelThongKe = new JPanel(new GridLayout(1, 4, KHOANG_CACH, 0));
        panelThongKe.setOpaque(false);

        theLichHomNay = new TheThongKe(null, "Lịch khám hôm nay", "0",
                "Ngày " + DINH_DANG_NGAY.format(LocalDate.now()));
        theChoKham = new TheThongKe(null, "Chờ khám", "0", "Chưa tiếp nhận");
        theDangKham = new TheThongKe(null, "Đang khám", "0", "Đang xử lý");
        theDaKhamDaHuy = new TheThongKe(null, "Đã khám / đã hủy", "0 / 0", "Trong hôm nay");

        panelThongKe.add(theLichHomNay);
        panelThongKe.add(theChoKham);
        panelThongKe.add(theDangKham);
        panelThongKe.add(theDaKhamDaHuy);
        return panelThongKe;
    }

    //Dựng một khung bo tròn lớn để bao bọc thanh công cụ và bảng danh sách bên trong
    private TheBoGoc taoTheDanhSach() {
        theDanhSach = new TheBoGoc(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        theDanhSach.add(taoThanhCongCu(), BorderLayout.NORTH);
        theDanhSach.add(taoBang(), BorderLayout.CENTER);
        theDanhSach.add(taoChanBang(), BorderLayout.SOUTH);
        return theDanhSach;
    }
    
    //Thiết kế bộ lọc (Ngày, Nhân viên, Trạng thái) và cụm nút thao tác (Xem, Sửa, Hủy) nằm ngay trên bảng.
    private JPanel taoThanhCongCu() {
        panelThanhCongCu = new JPanel(new BorderLayout());
        panelThanhCongCu.setOpaque(false);
        panelThanhCongCu.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        oChonNgay = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        oChonNgay.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        oChonNgay.setPreferredSize(new Dimension(255, CHIEU_CAO_O));
        nhanNgayLoc = new JLabel("Ngày: Tất cả");
        nhanNgayLoc.setFont(RangBuocGiaoDien.fontThuong(13));
        nhanNgayLoc.setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
        oChonNgay.add(nhanNgayLoc, BorderLayout.CENTER);

        JLabel lblIconLich = new JLabel("(Lịch)");
        lblIconLich.setFont(RangBuocGiaoDien.fontThuong(11));
        lblIconLich.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
        oChonNgay.add(lblIconLich, BorderLayout.EAST);

        oChonNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        oChonNgay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                chonKhoangNgay();
            }
        });

        cboBacSi = new ComboHienDai(DANH_SACH_NHAN_VIEN, 240, CHIEU_CAO_O);
        cboTrangThai = new ComboHienDai(DANH_SACH_TRANG_THAI, 230, CHIEU_CAO_O);

        JPanel nhomBoLoc = new JPanel();
        nhomBoLoc.setOpaque(false);
        nhomBoLoc.setLayout(new BoxLayout(nhomBoLoc, BoxLayout.X_AXIS));
        nhomBoLoc.add(oChonNgay);
        nhomBoLoc.add(Box.createHorizontalStrut(12));
        nhomBoLoc.add(cboBacSi);
        nhomBoLoc.add(Box.createHorizontalStrut(12));
        nhomBoLoc.add(cboTrangThai);

        panelCheDoXem = new TheBoGoc(BAN_KINH_O, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
        panelCheDoXem.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCheDoXem.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        nutThang = new NutBo("Tháng", NutBo.KIEU_PHANG);
        nutTuan = new NutBo("Tuần", NutBo.KIEU_PHANG);
        nutDanhSach = new NutBo("Danh sách", NutBo.KIEU_PHANG);
        nutThang.setPreferredSize(new Dimension(72, 30));
        nutTuan.setPreferredSize(new Dimension(58, 30));
        nutDanhSach.setPreferredSize(new Dimension(92, 30));
        nutDanhSach.datChon(true);

        ActionListener chonCheDoXem = e -> {
            nutThang.datChon(e.getSource() == nutThang);
            nutTuan.datChon(e.getSource() == nutTuan);
            nutDanhSach.datChon(e.getSource() == nutDanhSach);
        };
        nutThang.addActionListener(chonCheDoXem);
        nutTuan.addActionListener(chonCheDoXem);
        nutDanhSach.addActionListener(chonCheDoXem);

        panelCheDoXem.add(nutThang);
        panelCheDoXem.add(nutTuan);
        panelCheDoXem.add(nutDanhSach);

        nutXemChiTiet = new NutBo("Xem chi tiết", NutBo.KIEU_VIEN);
        nutSuaLich = new NutBo("Sửa lịch", NutBo.KIEU_VIEN);
        nutHuyLich = new NutBo("Hủy lịch", NutBo.KIEU_VIEN);
        nutXemChiTiet.setPreferredSize(new Dimension(112, 34));
        nutSuaLich.setPreferredSize(new Dimension(92, 34));
        nutHuyLich.setPreferredSize(new Dimension(92, 34));
        nutXemChiTiet.setEnabled(false);
        nutSuaLich.setEnabled(false);
        nutHuyLich.setEnabled(false);
        nutXemChiTiet.addActionListener(e -> xemChiTietLich());
        nutSuaLich.addActionListener(e -> suaLich());
        nutHuyLich.addActionListener(e -> huyLich());
        nutDanhSach.addActionListener(e -> xuatDanhSach());

        JPanel nhomThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nhomThaoTac.setOpaque(false);
        nhomThaoTac.add(nutXemChiTiet);
        nhomThaoTac.add(nutSuaLich);
        nhomThaoTac.add(nutHuyLich);

        JPanel khoiPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        khoiPhai.setOpaque(false);
        khoiPhai.add(nhomThaoTac);
        khoiPhai.add(panelCheDoXem);

        panelThanhCongCu.add(nhomBoLoc, BorderLayout.WEST);
        panelThanhCongCu.add(khoiPhai, BorderLayout.EAST);
        return panelThanhCongCu;
    }

    //Khai báo các cột cho bảng lịch khám, căn chỉnh chiều rộng và gắn màu cho tiêu đề cột.
    private JScrollPane taoBang() {
        bangLichKham = new TableMau(TIEU_DE_BANG, new Object[0][TIEU_DE_BANG.length]);
        bangLichKham.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bangLichKham.getTableHeader().setDefaultRenderer(new RendererTieuDe());
        bangLichKham.getTableHeader().setPreferredSize(new Dimension(0, 42));
        bangLichKham.setIntercellSpacing(new Dimension(0, 1));

        for (int i = 0; i < DO_RONG_COT.length; i++) {
            bangLichKham.getColumnModel().getColumn(i).setPreferredWidth(DO_RONG_COT[i]);
        }
        bangLichKham.getColumnModel().getColumn(COT_THOI_GIAN)
                .setCellRenderer(new RendererVanBanMau(RangBuocGiaoDien.MAU_LIEN_KET));
        bangLichKham.getColumnModel().getColumn(COT_TRANG_THAI)
                .setCellRenderer(new RendererTrangThai());
        bangLichKham.setRowSelectionAllowed(true);
        bangLichKham.setColumnSelectionAllowed(false);
        bangLichKham.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatNutThaoTac();
            }
        });

        khungCuonBang = new JScrollPane(bangLichKham);
        khungCuonBang.setBorder(null);
        khungCuonBang.getViewport().setBackground(RangBuocGiaoDien.MAU_NEN_THE);
        khungCuonBang.getVerticalScrollBar().setUnitIncrement(16);
        return khungCuonBang;
    }

    //Tạo vùng dưới cùng của bảng để hiển thị các dòng chữ chú thích như "Không có lịch khám" hay "Hiển thị x lịch khám".
    private JPanel taoChanBang() {
        panelChanBang = new JPanel(new BorderLayout());
        panelChanBang.setOpaque(false);
        panelChanBang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        nhanThongTinHienThi = new JLabel("Không có lịch khám");
        nhanThongTinHienThi.setFont(RangBuocGiaoDien.fontThuong(12));
        nhanThongTinHienThi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        nhanGoiYChon = new JLabel("Chọn lịch khám để xem hồ sơ sức khỏe và tiến hành khám.");
        nhanGoiYChon.setFont(RangBuocGiaoDien.fontThuong(11));
        nhanGoiYChon.setForeground(RangBuocGiaoDien.MAU_CHU_GOI_Y);

        JPanel khoiTrai = new JPanel(new BorderLayout(0, 2));
        khoiTrai.setOpaque(false);
        khoiTrai.add(nhanThongTinHienThi, BorderLayout.NORTH);
        khoiTrai.add(nhanGoiYChon, BorderLayout.CENTER);

        panelChanBang.add(khoiTrai, BorderLayout.WEST);
        return panelChanBang;
    }

    //Tự động bật/tắt (Enable/Disable) các nút Xem, Sửa, Hủy. (Chỉ bật khi người dùng chọn đúng 1 dòng trên bảng).
    private void capNhatNutThaoTac() {
        boolean chonMot = bangLichKham != null && bangLichKham.getSelectedRowCount() == 1;
        if (nutXemChiTiet != null)
            nutXemChiTiet.setEnabled(chonMot);
        if (nutSuaLich != null)
            nutSuaLich.setEnabled(chonMot);
        if (nutHuyLich != null)
            nutHuyLich.setEnabled(chonMot);
    }

    //Trung tâm gắn Listener (hành động) cho các nút bấm, combobox và thao tác double-click chuột.
    private void caiDatSuKien() {
        nutTaoLichKham.addActionListener(e -> moFormDatLich());
        cboBacSi.addActionListener(e -> locDuLieu());
        cboTrangThai.addActionListener(e -> locDuLieu());
        bangLichKham.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangLichKham.getSelectedRowCount() == 1) {
                    xemChiTietLich();
                }
            }
        });
    }

    //Gọi Service để lấy toàn bộ dữ liệu lịch hẹn và danh sách nhân viên từ cơ sở dữ liệu lên.
    public void loadDataToTable() {
        try {
            XemLichService.DuLieuManHinh duLieu = service.taiDuLieu();
            danhSachLichHen = duLieu.lichHen;
            List<NhanVien_entity> nhanVien = duLieu.nhanVien;
            Object selected = cboBacSi.getSelectedItem();
            cboBacSi.removeAllItems();
            cboBacSi.addItem("Nhân viên: Tất cả");
            for (NhanVien_entity item : nhanVien) {
                cboBacSi.addItem(item.getHoTen() + " (" + item.getMaNhanVien() + ")");
            }
            if (selected != null && !selected.toString().startsWith("Nhân viên:")) {
                cboBacSi.setSelectedItem(selected);
            } else {
                cboBacSi.setSelectedIndex(0);
            }
            locDuLieu();
            capNhatThongKeTuDuLieu();
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch hẹn: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    //tìm theo ngày, cboBacSi, cboTrangThai.
    private void locDuLieu() {
        if (bangLichKham == null)
            return;
        List<LichHen_entity> ketQua = service.loc(danhSachLichHen,
                String.valueOf(cboBacSi.getSelectedItem()),
                String.valueOf(cboTrangThai.getSelectedItem()), tuNgay, denNgay);
        DefaultTableModel model = (DefaultTableModel) bangLichKham.getModel();
        model.setRowCount(0);
        danhSachDangHienThi = ketQua;
        for (LichHen_entity lich : danhSachDangHienThi) {
            String tenNhanVien = lich.getNhanVien() == null ? "" : lich.getNhanVien().getHoTen();
            String trangThai = lich.getTrangThai() == null ? "" : lich.getTrangThai();
            model.addRow(new Object[] {
                    lich.getNgayGio() == null ? "" : DINH_DANG_NGAY_GIO.format(lich.getNgayGio()),
                    lich.getThuCung() == null ? "" : lich.getThuCung().getTenTC(),
                    lich.getKhachHang() == null ? "" : lich.getKhachHang().getHoTen(),
                    tenNhanVien, lich.getLyDo(), trangThai
            });
        }
        bangLichKham.clearSelection();
        capNhatNutThaoTac();
        capNhatThongTinHienThi();
    }

    //Đếm và tính toán lại các con số (tổng, chờ khám, đang khám...) của ngày hôm nay để đưa lên 4 thẻ thống kê.
    private void capNhatThongKeTuDuLieu() {
        LocalDate homNay = LocalDate.now();
        XemLichService.ThongKe thongKe = service.thongKeHomNay(danhSachLichHen, homNay);
        capNhatThongKe(DINH_DANG_NGAY.format(homNay), thongKe.tong, thongKe.choKham,
                thongKe.dangKham, thongKe.daKham, thongKe.daHuy);
    }

    //Mở hộp thoại lọc lịch popup cho phép người dùng nhập từ ngày - đến ngày để lọc lịch.
    private void chonKhoangNgay() {
        JTextField tu = new JTextField(tuNgay == null ? "" : DINH_DANG_NGAY.format(tuNgay));
        JTextField den = new JTextField(denNgay == null ? "" : DINH_DANG_NGAY.format(denNgay));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        form.add(tu);
        form.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        form.add(den);
        if (JOptionPane.showConfirmDialog(this, form, "Lọc lịch theo ngày",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return;
        try {
            XemLichService.KhoangNgay khoang = service.khoangNgay(tu.getText(), den.getText());
            tuNgay = khoang.tuNgay;
            denNgay = khoang.denNgay;
            nhanNgayLoc.setText(tuNgay == null && denNgay == null ? "Ngày: Tất cả"
                    : "Ngày: " + (tuNgay == null ? "…" : DINH_DANG_NGAY.format(tuNgay))
                            + " – " + (denNgay == null ? "…" : DINH_DANG_NGAY.format(denNgay)));
            locDuLieu();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Khoảng ngày không hợp lệ: " + e.getMessage(),
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        }
    }

    //Mở giao diện DatLichMoiGUI để người dùng thêm lịch khám mới.
    private void moFormDatLich() {
        DatLichMoiGUI form = new DatLichMoiGUI();
        form.setXuLySauKhiDatLich(this::loadDataToTable);
        JOptionPane.showMessageDialog(this, form, "Tạo lịch khám", JOptionPane.PLAIN_MESSAGE);
    }

    //Lấy dòng đã chọn
    private int layDongDaChon() {
        if (bangLichKham == null || bangLichKham.getSelectedRowCount() != 1)
            return -1;
        int dongHienThi = bangLichKham.getSelectedRow();
        return dongHienThi < 0 ? -1 : bangLichKham.convertRowIndexToModel(dongHienThi);
    }

    //Xem chi tiết lịch
    private void xemChiTietLich() {
        LichHen_entity lich = layLichHenDaChon();
        if (lich == null)
            return;
        try {
            LichHen_entity chiTiet = service.timTheoMa(lich.getMaLichHen());
            if (chiTiet == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn " + lich.getMaLichHen() + ".",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String text = "Mã lịch hẹn: " + chiTiet.getMaLichHen()
                    + "\nThời gian: "
                    + (chiTiet.getNgayGio() == null ? "" : DINH_DANG_NGAY_GIO.format(chiTiet.getNgayGio()))
                    + "\nThú cưng: " + (chiTiet.getThuCung() == null ? "" : chiTiet.getThuCung().getTenTC())
                    + "\nChủ nuôi: " + (chiTiet.getKhachHang() == null ? "" : chiTiet.getKhachHang().getHoTen())
                    + "\nNhân viên phụ trách: "
                    + (chiTiet.getNhanVien() == null ? "" : chiTiet.getNhanVien().getHoTen())
                    + "\nNội dung: " + chiTiet.getLyDo()
                    + "\nDịch vụ: " + (chiTiet.getDichVu() == null ? "" : chiTiet.getDichVu().getTenDV())
                    + "\nLoại hẹn: " + chiTiet.getLoaiHen()
                    + "\nGhi chú: " + chiTiet.getGhiChu()
                    + "\nTrạng thái: " + chiTiet.getTrangThai();
            JOptionPane.showMessageDialog(this, text, "Chi tiết lịch hẹn", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Chỉnh sửa lịch
    private void suaLich() {
        LichHen_entity lich = layLichHenDaChon();
        if (lich == null)
            return;
        try {
            lich = service.timTheoMa(lich.getMaLichHen());
            if (lich == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn cần sửa.",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<NhanVien_entity> nhanVien = service.taiNhanVien();
            List<DichVu_entity> dichVu = service.taiDichVu();
            JTextField txtNgayGio = new JTextField(DINH_DANG_NGAY_GIO.format(lich.getNgayGio()));
            JTextField txtLyDo = new JTextField(lich.getLyDo());
            JTextField txtLoaiHen = new JTextField(lich.getLoaiHen());
            javax.swing.JTextArea txtGhiChu = new javax.swing.JTextArea(lich.getGhiChu(), 3, 25);
            JComboBox<NhanVien_entity> cboNhanVien = new JComboBox<>(nhanVien.toArray(new NhanVien_entity[0]));
            cboNhanVien.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof NhanVien_entity)
                        setText(((NhanVien_entity) value).getHoTen());
                    return this;
                }
            });
            for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
                if (lich.getNhanVien() != null && lich.getNhanVien().getMaNhanVien()
                        .equals(cboNhanVien.getItemAt(i).getMaNhanVien()))
                    cboNhanVien.setSelectedIndex(i);
            }
            JComboBox<DichVu_entity> cboDichVu = new JComboBox<>(dichVu.toArray(new DichVu_entity[0]));
            cboDichVu.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof DichVu_entity)
                        setText(((DichVu_entity) value).getTenDV());
                    return this;
                }
            });
            cboDichVu.insertItemAt(null, 0);
            cboDichVu.setSelectedIndex(0);
            if (lich.getDichVu() != null) {
                for (int i = 1; i < cboDichVu.getItemCount(); i++) {
                    if (lich.getDichVu().getMaDV().equals(cboDichVu.getItemAt(i).getMaDV()))
                        cboDichVu.setSelectedIndex(i);
                }
            }
            JComboBox<String> cboTrangThai = new JComboBox<>(DANH_SACH_TRANG_THAI);
            cboTrangThai.removeItemAt(0);
            cboTrangThai.setSelectedItem(lich.getTrangThai());
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Thời gian (dd/MM/yyyy HH:mm):"));
            form.add(txtNgayGio);
            form.add(new JLabel("Nhân viên phụ trách:"));
            form.add(cboNhanVien);
            form.add(new JLabel("Nội dung:"));
            form.add(txtLyDo);
            form.add(new JLabel("Loại hẹn:"));
            form.add(txtLoaiHen);
            form.add(new JLabel("Dịch vụ:"));
            form.add(cboDichVu);
            form.add(new JLabel("Trạng thái:"));
            form.add(cboTrangThai);
            form.add(new JLabel("Ghi chú:"));
            form.add(new JScrollPane(txtGhiChu));
            if (JOptionPane.showConfirmDialog(this, form, "Sửa lịch hẹn",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
                return;
            NhanVien_entity nv = (NhanVien_entity) cboNhanVien.getSelectedItem();
            if (service.capNhat(lich, new XemLichService.ThongTinCapNhat(
                    txtNgayGio.getText(), nv, txtLyDo.getText(), txtLoaiHen.getText(),
                    (DichVu_entity) cboDichVu.getSelectedItem(),
                    String.valueOf(cboTrangThai.getSelectedItem()), txtGhiChu.getText()))) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật lịch hẹn.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không cập nhật được lịch hẹn.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    //hủy lịch, nếu đồng ý sẽ đổi trạng thái của lịch thành "Đã hủy".
    private void huyLich() {
        LichHen_entity lichChon = layLichHenDaChon();
        if (lichChon == null)
            return;
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy lịch " + lichChon.getMaLichHen() + " của "
                        + lichChon.getThuCung().getTenTC() + " không?",
                "Xác nhận hủy lịch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION)
            return;

        try {
            XemLichService.KetQuaHuyLich ketQua = service.huy(lichChon.getMaLichHen());
            if (ketQua == XemLichService.KetQuaHuyLich.KHONG_TIM_THAY) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn để hủy.",
                        "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ketQua == XemLichService.KetQuaHuyLich.DA_HUY) {
                JOptionPane.showMessageDialog(this, "Lịch hẹn này đã được hủy trước đó.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (ketQua == XemLichService.KetQuaHuyLich.THANH_CONG) {
                JOptionPane.showMessageDialog(this, "Đã hủy lịch hẹn.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái lịch hẹn.",
                        "Thao tác thất bại", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hủy lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LichHen_entity layLichHenDaChon() {
        int row = layDongDaChon();
        return row < 0 || row >= danhSachDangHienThi.size() ? null : danhSachDangHienThi.get(row);
    }

    //Xuất danh sách lịch hẹn ra tệp CSV
    private void xuatDanhSach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("danh-sach-lich-hen.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại, ghi đè?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            service.xuatDanhSach(file, danhSachDangHienThi);
            JOptionPane.showMessageDialog(this, "Đã xuất danh sách ra:\n" + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể xuất danh sách: " + e.getMessage(),
                    "Lỗi xuất tệp", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Cập nhật dòng chữ dưới cùng của bảng (VD: báo số lượng dòng, hoặc báo "Không có lịch khám").
    private void capNhatThongTinHienThi() {
        int soDong = bangLichKham.getRowCount();
        if (soDong > 0) {
            nhanThongTinHienThi.setText("Hiển thị " + soDong + " lịch khám");
        } else if (danhSachLichHen.isEmpty()) {
            nhanThongTinHienThi.setText("Chưa có lịch khám. Nhấn “Tạo lịch khám” để thêm mới.");
        } else {
            nhanThongTinHienThi.setText("Không có lịch khám phù hợp với bộ lọc.");
        }
    }

    //Đưa các con số đã tính toán hiển thị lên 4 thẻ giao diện thống kê.
    public void capNhatThongKe(String ngayHienThi, int soHomNay, int soChoKham, int soDangKham, int soDaKham,
            int soDaHuy) {
        theLichHomNay.datGiaTri(String.valueOf(soHomNay), "Ngày " + ngayHienThi);
        theChoKham.datGiaTri(String.valueOf(soChoKham), "Chưa tiếp nhận");
        theDangKham.datGiaTri(String.valueOf(soDangKham), "Đang xử lý");
        theDaKhamDaHuy.datGiaTri(soDaKham + " / " + soDaHuy, "Trong hôm nay");
    }

    public TableMau getBangLichKham() {
        return bangLichKham;
    }

    public JButton getNutTaoLichKham() {
        return nutTaoLichKham;
    }

    public JComboBox<String> getCboBacSi() {
        return cboBacSi;
    }

    public JComboBox<String> getCboTrangThai() {
        return cboTrangThai;
    }

    private static Color[] layMauTrangThai(String trangThai) {
        switch (trangThai) {
            case "Chờ khám":
                return new Color[] { RangBuocGiaoDien.MAU_CANH_BAO_NEN, RangBuocGiaoDien.MAU_CANH_BAO_CHU };
            case "Đang khám":
                return new Color[] { RangBuocGiaoDien.MAU_THONG_TIN_NEN, RangBuocGiaoDien.MAU_THONG_TIN_CHU };
            case "Đã khám":
                return new Color[] { RangBuocGiaoDien.MAU_THANH_CONG_NEN, RangBuocGiaoDien.MAU_THANH_CONG_CHU };
            case "Đã hủy":
                return new Color[] { RangBuocGiaoDien.MAU_NGUY_HIEM_NEN, RangBuocGiaoDien.MAU_NGUY_HIEM_CHU };
            default:
                return new Color[] { RangBuocGiaoDien.MAU_TRUNG_TINH_NEN, RangBuocGiaoDien.MAU_TRUNG_TINH_CHU };
        }
    }

    //từ class TheBoGoc cho đến RendererTrangThai đều là các Class kế thừa dùng để tùy chỉnh giao diện cho đẹp hơn
    private static class TheBoGoc extends JPanel {
        private int banKinh;
        private Color mauNen;
        private Color mauVien;

        TheBoGoc(int banKinh, Color mauNen, Color mauVien) {
            super(new BorderLayout());
            this.banKinh = banKinh;
            this.mauNen = mauNen;
            this.mauVien = mauVien;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    banKinh * 2, banKinh * 2);
            g2.setColor(mauNen);
            g2.fill(hinh);
            if (mauVien != null) {
                g2.setColor(mauVien);
                g2.draw(hinh);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class TheThongKe extends TheBoGoc {
        private JLabel nhanTieuDe;
        private JLabel nhanGiaTri;
        private JLabel nhanChuThich;

        TheThongKe(String nhanGhiChu, String tieuDe, String giaTri, String chuThich) {
            super(BAN_KINH_THE, RangBuocGiaoDien.MAU_NEN_THE, RangBuocGiaoDien.MAU_DUONG_VIEN);
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            JLabel lblNhanGhiChu = new JLabel(nhanGhiChu);
            lblNhanGhiChu.setFont(RangBuocGiaoDien.fontDam(11));
            lblNhanGhiChu.setForeground(RangBuocGiaoDien.MAU_CHU_DAO);

            JPanel khoiNhan = new JPanel(new GridBagLayout());
            khoiNhan.setOpaque(false);
            khoiNhan.add(lblNhanGhiChu);

            nhanTieuDe = new JLabel(tieuDe);
            nhanTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
            nhanTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            nhanGiaTri = new JLabel(giaTri);
            nhanGiaTri.setFont(RangBuocGiaoDien.fontDam(26));
            nhanGiaTri.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

            nhanChuThich = new JLabel(chuThich);
            nhanChuThich.setFont(RangBuocGiaoDien.fontThuong(11));
            nhanChuThich.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

            JPanel khoiChu = new JPanel(new GridBagLayout());
            khoiChu.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.gridwidth = 2;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.WEST;
            khoiChu.add(nhanTieuDe, gc);

            gc.gridy = 1;
            gc.gridwidth = 1;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.BASELINE_LEADING;
            khoiChu.add(nhanGiaTri, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.insets = new Insets(0, 10, 0, 0);
            khoiChu.add(nhanChuThich, gc);

            add(khoiNhan, BorderLayout.WEST);
            add(khoiChu, BorderLayout.CENTER);
        }

        void datGiaTri(String giaTri, String chuThich) {
            nhanGiaTri.setText(giaTri);
            nhanChuThich.setText(chuThich);
        }
    }

    private static class NutBo extends JButton {
        static final int KIEU_CHINH = 0;
        static final int KIEU_VIEN = 1;
        static final int KIEU_PHANG = 2;

        private int kieu;
        private boolean chon;

        NutBo(String chu, int kieu) {
            super(chu);
            this.kieu = kieu;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(RangBuocGiaoDien.fontVua(13));
        }

        void datChon(boolean chon) {
            this.chon = chon;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);

            boolean reChuot = getModel().isRollover();
            boolean noi = (kieu == KIEU_CHINH) || chon;
            Color nen;
            Color vien;
            Color chu;
            if (noi) {
                nen = reChuot ? RangBuocGiaoDien.MAU_CHU_DAO_RE_CHUOT : RangBuocGiaoDien.MAU_CHU_DAO;
                vien = nen;
                chu = Color.WHITE;
            } else if (kieu == KIEU_VIEN) {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : RangBuocGiaoDien.MAU_NEN_THE;
                vien = RangBuocGiaoDien.MAU_DUONG_VIEN;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            } else {
                nen = reChuot ? RangBuocGiaoDien.MAU_DONG_RE_CHUOT : null;
                vien = null;
                chu = RangBuocGiaoDien.MAU_TRUNG_TINH_CHU;
            }

            int rong = getWidth();
            int cao = getHeight();
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, rong - 1, cao - 1, 12, 12);
            if (nen != null) {
                g2.setColor(nen);
                g2.fill(hinh);
            }
            if (vien != null) {
                g2.setColor(vien);
                g2.draw(hinh);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth(getText());
            int x = (rong - rongChu) / 2;
            g2.setColor(chu);
            g2.drawString(getText(), x, (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    private static class ComboHienDai extends JComboBox<String> {

        ComboHienDai(String[] mucChon, int rong, int cao) {
            super(mucChon);
            setUI(new GiaoDienCombo());
            setOpaque(false);
            setFont(RangBuocGiaoDien.fontThuong(13));
            setForeground(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            setBorder(BorderFactory.createEmptyBorder());
            setPreferredSize(new Dimension(rong, cao));
            setMaximumRowCount(8);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel nhan = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    nhan.setFont(RangBuocGiaoDien.fontThuong(13));
                    nhan.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    nhan.setForeground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
                    nhan.setBackground(isSelected ? RangBuocGiaoDien.MAU_CHU_DAO_NHAT : RangBuocGiaoDien.MAU_NEN_THE);
                    return nhan;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            RoundRectangle2D.Double hinh = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    BAN_KINH_O * 2, BAN_KINH_O * 2);
            g2.setColor(RangBuocGiaoDien.MAU_NEN_THE);
            g2.fill(hinh);
            g2.setColor((isPopupVisible() || hasFocus()) ? RangBuocGiaoDien.MAU_CHU_DAO
                    : RangBuocGiaoDien.MAU_DUONG_VIEN);
            g2.draw(hinh);
            g2.dispose();
            RangBuocGiaoDien.batKhuRangCua(g);
            super.paintComponent(g);
        }
    }

    private static class GiaoDienCombo extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton nut = new JButton("▼");
            nut.setFont(RangBuocGiaoDien.fontThuong(10));
            nut.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            nut.setBorder(BorderFactory.createEmptyBorder());
            nut.setContentAreaFilled(false);
            nut.setFocusPainted(false);
            nut.setOpaque(false);
            return nut;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            Object giaTri = comboBox.getSelectedItem();
            if (giaTri == null)
                return;
            g.setFont(comboBox.getFont());
            g.setColor(RangBuocGiaoDien.MAU_TRUNG_TINH_CHU);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(giaTri.toString(), bounds.x + 12,
                    bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN));
            return popup;
        }
    }

    private static class RendererTieuDe extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(RangBuocGiaoDien.fontDam(12));
            setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            setBackground(RangBuocGiaoDien.MAU_TIEU_DE_BANG);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 1, 0, RangBuocGiaoDien.MAU_DUONG_VIEN),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class RendererVanBanMau extends DefaultTableCellRenderer {
        private Color mauChu;

        RendererVanBanMau(Color mauChu) {
            this.mauChu = mauChu;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setForeground(mauChu);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_NEN_TRANG);
            }
            return this;
        }
    }

    private static class RendererTrangThai extends JPanel implements TableCellRenderer {
        private String trangThai = "";

        RendererTrangThai() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            trangThai = (value == null) ? "" : value.toString();
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? RangBuocGiaoDien.MAU_NEN_THE : RangBuocGiaoDien.MAU_NEN_TRANG);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (trangThai.isEmpty())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            RangBuocGiaoDien.batKhuRangCua(g2);
            Color[] mau = layMauTrangThai(trangThai);

            g2.setFont(RangBuocGiaoDien.fontDam(11));
            FontMetrics fm = g2.getFontMetrics();
            int cao = 22;
            int rong = 16 + fm.stringWidth(trangThai);
            int x = 12;
            int y = (getHeight() - cao) / 2;

            g2.setColor(mau[0]);
            g2.fill(new RoundRectangle2D.Double(x, y, rong, cao, cao, cao));
            g2.setColor(mau[1]);
            g2.drawString(trangThai, x + 8, y + (cao + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}