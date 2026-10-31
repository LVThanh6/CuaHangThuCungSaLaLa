package GUI.NhanVien;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
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

import dao.NhanVien_DAO;
import entity.NhanVien_entity;

public class NhanVienGUI extends JPanel {

    public enum TrangThai {
        DANG_LAM("Đang làm việc"),
        NGUNG("Ngừng hoạt động"),
        CHUA_TK("Chưa có tài khoản");

        public final String nhan;

        TrangThai(String nhan) {
            this.nhan = nhan;
        }

        public static TrangThai tuEntity(NhanVien_entity nv) {
            Boolean tk = nv.getTrangThaiTK();
            if (tk == null)
                return CHUA_TK;
            return tk ? DANG_LAM : NGUNG;
        }
    }

    private static final String[] CHUC_VU = {
            "Nhân viên bán hàng", "Nhân viên dịch vụ", "Nhân viên quản lý kho", "Quản lý"
    };
    private static final String[] GIOI_TINH = { "Nam", "Nữ" };

    private static final Color NEN_CHON = new Color(240, 250, 247);
    private static final Color NEN_HEADER = new Color(246, 248, 247);
    private static final Color NEN_ICON = new Color(226, 244, 240);
    private static final Color MAU_DO = new Color(220, 38, 38);

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    private final List<NhanVien_entity> tatCa = new ArrayList<>();
    private final List<NhanVien_entity> daLoc = new ArrayList<>();
    private final Set<String> daChon = new HashSet<>();
    private final NhanVien_DAO dao = new NhanVien_DAO();
    private int trangHienTai = 1;
    private int soDongMoiTrang = 8;
    private Boolean sapXepTang = null;

    private OTimKiem txtTim;
    private JComboBox<String> cbVaiTro, cbTrangThai, cbSoDong;
    private JTable bang;
    private BangModel model;
    private JLabel lblHienThi;
    private JPanel panelTrang;
    private JButton btnXemChiTiet, btnChinhSua, btnDoiTrangThai, btnXoa;
    private final JLabel[] lblSoLieu = new JLabel[4];

    public NhanVienGUI() {
        khoiTaoGiaoDien();
        taiLai();
    }

    public void datDanhSach(List<NhanVien_entity> ds) {
        tatCa.clear();
        tatCa.addAll(ds);
        daChon.clear();
        trangHienTai = 1;
        lamMoi();
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

        JLabel lblDuongDan = new JLabel("Nhân viên / Danh sách nhân viên");
        lblDuongDan.setFont(RangBuocGiaoDien.fontThuong(12));
        lblDuongDan.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JLabel lblTieuDe = new JLabel("Danh sách nhân viên");
        lblTieuDe.setFont(RangBuocGiaoDien.fontDam(24));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel("Quản lý hồ sơ, chức vụ và trạng thái tài khoản của đội ngũ SALALA.");
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        trai.add(lblDuongDan);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblTieuDe);
        trai.add(Box.createVerticalStrut(4));
        trai.add(lblMoTa);
        panel.add(trai, BorderLayout.WEST);

        JButton btnThem = new JButton("+   Thêm nhân viên");
        dinhDangNut(btnThem, true);
        btnThem.addActionListener(e -> moForm(null, false));

        JPanel phai = new JPanel(new GridBagLayout());
        phai.setOpaque(false);
        phai.add(btnThem);
        panel.add(phai, BorderLayout.EAST);
        return panel;
    }

    private void dinhDangNut(JButton btn, boolean chinh) {
        btn.setFont(RangBuocGiaoDien.fontDam(13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color vien = chinh ? RangBuocGiaoDien.MAU_CHU_DAO : RangBuocGiaoDien.MAU_DUONG_VIEN;
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(vien, 1, true), new EmptyBorder(9, 18, 9, 18)));
        btn.setBackground(chinh ? RangBuocGiaoDien.MAU_CHU_DAO : Color.WHITE);
        btn.setForeground(chinh ? Color.WHITE : RangBuocGiaoDien.MAU_CHU_CHINH);
    }

    private JComponent taoHangSoLieu() {
        String[] tieuDe = { "Tổng nhân viên", "Đang làm việc", "Ngừng hoạt động", "Chưa có tài khoản" };
        String[] moTa = { "Toàn bộ đội ngũ", "Tài khoản hoạt động", "Tài khoản bị khóa", "Chưa cấp quyền đăng nhập" };

        JPanel hang = new JPanel(new GridLayout(1, 4, 16, 0));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 16, 0));
        for (int i = 0; i < 4; i++) {
            lblSoLieu[i] = new JLabel("0");
            hang.add(taoTheSoLieu(i, tieuDe[i], moTa[i], lblSoLieu[i]));
        }
        return hang;
    }

    private JPanel taoTheSoLieu(int loaiIcon, String tieuDe, String moTa, JLabel lblSo) {
        JPanel the = new JPanel(new BorderLayout(14, 0));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        the.add(new HuyHieuIcon(loaiIcon), BorderLayout.WEST);

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(RangBuocGiaoDien.fontThuong(12));
        lblTieuDe.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        lblSo.setFont(RangBuocGiaoDien.fontDam(24));
        lblSo.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);

        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(RangBuocGiaoDien.fontThuong(12));
        lblMoTa.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);

        JPanel hangSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangSo.setOpaque(false);
        hangSo.add(lblSo);
        hangSo.add(lblMoTa);

        JPanel giua = new JPanel();
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.setOpaque(false);
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(lblTieuDe);
        giua.add(hangSo);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JComponent taoTheDanhSach() {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(Color.WHITE);
        the.setBorder(new LineBorder(RangBuocGiaoDien.MAU_DUONG_VIEN, 1, true));

        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setOpaque(false);
        thanhTren.add(taoThanhBoLoc(), BorderLayout.NORTH);
        thanhTren.add(taoThanhHanhDong(), BorderLayout.SOUTH);
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

        txtTim = new OTimKiem("Tìm theo tên, mã nhân viên, số điện thoại...");
        txtTim.setPreferredSize(new Dimension(400, 38));
        txtTim.setMaximumSize(new Dimension(400, 38));
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                locTuDau();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                locTuDau();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                locTuDau();
            }
        });

        String[] dsVaiTro = new String[CHUC_VU.length + 1];
        dsVaiTro[0] = "Chức vụ: Tất cả";
        for (int i = 0; i < CHUC_VU.length; i++)
            dsVaiTro[i + 1] = "Chức vụ: " + CHUC_VU[i];
        cbVaiTro = new JComboBox<>(dsVaiTro);

        String[] dsTrangThai = new String[TrangThai.values().length + 1];
        dsTrangThai[0] = "Trạng thái: Tất cả";
        for (int i = 0; i < TrangThai.values().length; i++)
            dsTrangThai[i + 1] = "Trạng thái: " + TrangThai.values()[i].nhan;
        cbTrangThai = new JComboBox<>(dsTrangThai);

        for (JComboBox<String> cb : java.util.List.of(cbVaiTro, cbTrangThai)) {
            cb.setFont(RangBuocGiaoDien.fontThuong(13));
            cb.setMaximumSize(new Dimension(230, 38));
            cb.setPreferredSize(new Dimension(220, 38));
            cb.addActionListener(e -> locTuDau());
        }

        JButton btnXuat = new JButton("Xuất danh sách");
        dinhDangNut(btnXuat, false);
        btnXuat.addActionListener(e -> xuatCsv());

        bar.add(txtTim);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbVaiTro);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(cbTrangThai);
        bar.add(Box.createHorizontalGlue());
        bar.add(btnXuat);
        return bar;
    }

    private JComponent taoThanhHanhDong() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 18, 12, 18));

        btnXemChiTiet = taoNutHanhDong("Xem chi tiết");
        btnChinhSua = taoNutHanhDong("Chỉnh sửa");
        btnDoiTrangThai = taoNutHanhDong("Đổi trạng thái");
        btnXoa = taoNutHanhDong("Xóa");
        btnXoa.setForeground(MAU_DO);

        btnXemChiTiet.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                moForm(nv, true);
        });
        btnChinhSua.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                moForm(nv, false);
        });
        btnDoiTrangThai.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                hienPopupDoiTrangThai(nv, btnDoiTrangThai);
        });
        btnXoa.addActionListener(e -> {
            NhanVien_entity nv = nhanVienDangChon();
            if (nv != null)
                xoaNhanVien(nv);
        });

        bar.add(btnXemChiTiet);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnChinhSua);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnDoiTrangThai);
        bar.add(Box.createHorizontalStrut(8));
        bar.add(btnXoa);
        return bar;
    }

    private JButton taoNutHanhDong(String text) {
        JButton button = new JButton(text);
        dinhDangNut(button, false);
        button.setEnabled(false);
        return button;
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

        int[] rong = { 44, 90, 200, 180, 260, 120, 160 };
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }
        bang.getColumnModel().getColumn(0).setMaxWidth(50);

        bang.getColumnModel().getColumn(1).setCellRenderer(new ChuRenderer(1));
        bang.getColumnModel().getColumn(2).setCellRenderer(new TenRenderer());
        bang.getColumnModel().getColumn(3).setCellRenderer(new ChuRenderer(3));
        bang.getColumnModel().getColumn(4).setCellRenderer(new LienHeRenderer());
        bang.getColumnModel().getColumn(5).setCellRenderer(new ChuRenderer(5));
        bang.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        JTableHeader header = bang.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new HeaderRenderer());
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = header.columnAtPoint(e.getPoint());
                if (c == 0) {
                    chonTatCaTrang();
                } else if (c == 2) {
                    sapXepTang = (sapXepTang == null) ? Boolean.TRUE : (sapXepTang ? Boolean.FALSE : null);
                    apDungLoc();
                }
            }
        });

        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = bang.rowAtPoint(e.getPoint());
                int c = bang.columnAtPoint(e.getPoint());
                if (r < 0 || c <= 0)
                    return;
                if (e.getClickCount() == 2)
                    moForm(model.tai(r), true);
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                capNhatNutHanhDong();
        });
        capNhatNutHanhDong();

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
        int dang = 0, ngung = 0, chuaTk = 0;
        for (NhanVien_entity nv : tatCa) {
            switch (TrangThai.tuEntity(nv)) {
                case DANG_LAM:
                    dang++;
                    break;
                case NGUNG:
                    ngung++;
                    break;
                default:
                    chuaTk++;
            }
        }
        lblSoLieu[0].setText(String.valueOf(tatCa.size()));
        lblSoLieu[1].setText(String.valueOf(dang));
        lblSoLieu[2].setText(String.valueOf(ngung));
        lblSoLieu[3].setText(String.valueOf(chuaTk));
    }

    private void apDungLoc() {
        String tu = boDau(txtTim.getText().trim());
        String tuSo = tu.replace(" ", "");
        int vt = cbVaiTro.getSelectedIndex();
        int tt = cbTrangThai.getSelectedIndex();

        daLoc.clear();
        for (NhanVien_entity nv : tatCa) {
            if (vt > 0 && !CHUC_VU[vt - 1].equals(nv.getChucVu()))
                continue;
            if (tt > 0 && TrangThai.tuEntity(nv) != TrangThai.values()[tt - 1])
                continue;
            if (!tu.isEmpty()
                    && !boDau(nv.getHoTen()).contains(tu)
                    && !boDau(nv.getMaNhanVien()).contains(tu)
                    && !boDau(nz(nv.getDiaChi())).contains(tu)
                    && !nz(nv.getSdt()).replace(" ", "").contains(tuSo))
                continue;
            daLoc.add(nv);
        }

        if (sapXepTang != null) {
            Collator cl = Collator.getInstance(VN);
            boolean tang = sapXepTang;
            daLoc.sort((a, b) -> {
                int kq = cl.compare(tenGoi(a.getHoTen()), tenGoi(b.getHoTen()));
                if (kq == 0)
                    kq = cl.compare(a.getHoTen(), b.getHoTen());
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
        model.datHang(tu < den ? daLoc.subList(tu, den) : new ArrayList<NhanVien_entity>());
        lblHienThi.setText(tong == 0
                ? "Không có nhân viên phù hợp"
                : "Hiển thị " + (tu + 1) + "–" + den + " trong " + tong + " nhân viên");
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

    private boolean tatCaTrangDuocChon() {
        if (model.hang.isEmpty())
            return false;
        for (NhanVien_entity nv : model.hang) {
            if (!daChon.contains(nv.getMaNhanVien()))
                return false;
        }
        return true;
    }

    private void chonTatCaTrang() {
        boolean tatCaDaChon = tatCaTrangDuocChon();
        for (NhanVien_entity nv : model.hang) {
            if (tatCaDaChon)
                daChon.remove(nv.getMaNhanVien());
            else
                daChon.add(nv.getMaNhanVien());
        }
        model.fireTableDataChanged();
        bang.getTableHeader().repaint();
    }

    private static String boDau(String s) {
        String t = Normalizer.normalize(s.toLowerCase(VN), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replace('đ', 'd');
    }

    private static String tenGoi(String ten) {
        int i = ten.lastIndexOf(' ');
        return i < 0 ? ten : ten.substring(i + 1);
    }

    private void baoLoi(String tieuDe, SQLException ex) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
    }

    private NhanVien_entity nhanVienDangChon() {
        int[] hangChon = bang.getSelectedRows();
        return hangChon.length == 1 ? model.tai(hangChon[0]) : null;
    }

    private void capNhatNutHanhDong() {
        boolean chonMot = bang != null && bang.getSelectedRowCount() == 1;
        btnXemChiTiet.setEnabled(chonMot);
        btnChinhSua.setEnabled(chonMot);
        btnDoiTrangThai.setEnabled(chonMot);
        btnXoa.setEnabled(chonMot);
    }

    private void hienPopupDoiTrangThai(NhanVien_entity nv, Component goc) {
        JPopupMenu popup = new JPopupMenu();
        if (nv.getMaTK() == null) {
            JMenuItem muc = new JMenuItem(TrangThai.CHUA_TK.nhan);
            muc.setEnabled(false);
            popup.add(muc);
        } else {
            popup.add(taoMucTrangThai(nv, TrangThai.DANG_LAM));
            popup.add(taoMucTrangThai(nv, TrangThai.NGUNG));
        }
        popup.show(goc, 0, goc.getHeight());
    }

    private JMenuItem taoMucTrangThai(NhanVien_entity nv, TrangThai trangThai) {
        JMenuItem muc = new JMenuItem(trangThai.nhan);
        muc.setEnabled(TrangThai.tuEntity(nv) != trangThai);
        muc.addActionListener(e -> doiTrangThai(nv, trangThai));
        return muc;
    }

    private void doiTrangThai(NhanVien_entity nv, TrangThai trangThai) {
        if (nv.getMaTK() == null)
            return;
        try {
            dao.doiTrangThaiTaiKhoan(nv.getMaNhanVien(), trangThai == TrangThai.DANG_LAM);
            taiLai();
        } catch (SQLException ex) {
            baoLoi("Không thể đổi trạng thái", ex);
        }
    }

    private void xoaNhanVien(NhanVien_entity nv) {
        int kq = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + nv.getHoTen() + " (" + nv.getMaNhanVien()
                        + ")?\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (kq != JOptionPane.YES_OPTION)
            return;
        try {
            dao.xoa(nv.getMaNhanVien());
            daChon.remove(nv.getMaNhanVien());
            taiLai();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) {
                JOptionPane.showMessageDialog(this,
                        "Nhân viên này đã có dữ liệu liên quan (lịch làm việc, lịch hẹn, hóa đơn, phiếu kho...)\n"
                                + "nên không thể xóa. Hãy chuyển sang \"Ngừng hoạt động\" thay vì xóa.",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            } else {
                baoLoi("Không thể xóa nhân viên", ex);
            }
        }
    }

    private static String dinhDangSdt(String s) {
        if (s == null)
            return "";
        String so = s.replaceAll("\\D", "");
        if (so.length() == 10)
            return so.substring(0, 4) + " " + so.substring(4, 7) + " " + so.substring(7);
        return s;
    }

    private void moForm(NhanVien_entity goc, boolean chiXem) {
        String maHienThi;
        if (goc == null) {
            try {
                maHienThi = dao.getMaTiepTheo();
            } catch (SQLException ex) {
                baoLoi("Không thể tạo mã nhân viên mới", ex);
                return;
            }
        } else {
            maHienThi = goc.getMaNhanVien();
        }

        Window cha = SwingUtilities.getWindowAncestor(this);
        String tieuDe = goc == null ? "Thêm nhân viên" : (chiXem ? "Thông tin nhân viên" : "Chỉnh sửa nhân viên");
        JDialog dlg = new JDialog(cha, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);

        final boolean coTK = goc != null && goc.getMaTK() != null;

        JTextField txtMa = new JTextField(maHienThi);
        txtMa.setEditable(false);
        JTextField txtTen = new JTextField(goc == null ? "" : nz(goc.getHoTen()));
        JComboBox<String> cbChucVu = new JComboBox<>(CHUC_VU);
        JTextField txtSdt = new JTextField(goc == null ? "" : nz(goc.getSdt()));
        JTextField txtDiaChi = new JTextField(goc == null ? "" : nz(goc.getDiaChi()));
        JTextField txtNgay = new JTextField(goc == null || goc.getNgaySinh() == null
                ? ""
                : DINH_DANG_NGAY.format(goc.getNgaySinh()));
        JComboBox<String> cbGT = new JComboBox<>(GIOI_TINH);
        JTextField txtLuong = new JTextField(goc == null ? "45000" : String.format("%.0f", goc.getLuongCoBan()));
        txtLuong.setEditable(!chiXem);
        JTextField txtTongLuong = new JTextField();
        txtTongLuong.setEditable(false);
        JComboBox<String> cbTT = coTK
                ? new JComboBox<>(new String[] { TrangThai.DANG_LAM.nhan, TrangThai.NGUNG.nhan })
                : new JComboBox<>(new String[] { TrangThai.CHUA_TK.nhan });

        if (goc != null) {
            if (goc.getChucVu() != null)
                cbChucVu.setSelectedItem(goc.getChucVu());
            if (goc.getGioiTinh() != null)
                cbGT.setSelectedItem(goc.getGioiTinh());
            if (coTK)
                cbTT.setSelectedIndex(Boolean.TRUE.equals(goc.getTrangThaiTK()) ? 0 : 1);
        }
        Runnable apDungLuong = () -> {
            String chucVu = (String) cbChucVu.getSelectedItem();
            if (goc == null) {
                if ("Nhân viên bán hàng".equals(chucVu)) txtLuong.setText("45000");
                else if ("Nhân viên dịch vụ".equals(chucVu)) txtLuong.setText("60000");
                else if ("Nhân viên quản lý kho".equals(chucVu)) txtLuong.setText("13500000");
                else if ("Quản lý".equals(chucVu)) txtLuong.setText("15000000");
            }
        };
        cbChucVu.addActionListener(e -> apDungLuong.run());
        apDungLuong.run();
        txtTongLuong.setText(tinhTongLuongThang(goc));
        cbTT.setEnabled(coTK && !chiXem);

        if (chiXem) {
            txtTen.setEditable(false);
            txtSdt.setEditable(false);
            txtDiaChi.setEditable(false);
            txtNgay.setEditable(false);
            txtLuong.setEditable(false);
            cbChucVu.setEnabled(false);
            cbGT.setEnabled(false);
        }

        JPanel noiDung = new JPanel(new GridBagLayout());
        noiDung.setBackground(Color.WHITE);
        noiDung.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 4, 0);

        String[] nhan = { "Mã nhân viên", "Họ và tên", "Chức vụ", "Số điện thoại", "Địa chỉ",
                "Ngày sinh (dd/MM/yyyy, có thể để trống)", "Giới tính", "Lương cơ bản (theo chức vụ)",
                "Tổng lương tháng này (đến hôm nay)", "Trạng thái tài khoản" };
        JComponent[] truong = { txtMa, txtTen, cbChucVu, txtSdt, txtDiaChi, txtNgay, cbGT, txtLuong, txtTongLuong, cbTT };
        for (int i = 0; i < nhan.length; i++) {
            JLabel l = new JLabel(nhan[i]);
            l.setFont(RangBuocGiaoDien.fontThuong(12));
            l.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            gc.gridy = i * 2;
            noiDung.add(l, gc);
            truong[i].setFont(RangBuocGiaoDien.fontThuong(13));
            truong[i].setPreferredSize(new Dimension(340, 34));
            gc.gridy = i * 2 + 1;
            gc.insets = new Insets(0, 0, 10, 0);
            noiDung.add(truong[i], gc);
            gc.insets = new Insets(0, 0, 4, 0);
        }

        JLabel lblLoi = new JLabel(" ");
        lblLoi.setFont(RangBuocGiaoDien.fontThuong(12));
        lblLoi.setForeground(MAU_DO);
        gc.gridy = nhan.length * 2;
        noiDung.add(lblLoi, gc);

        JButton btnHuy = new JButton(chiXem ? "Đóng" : "Hủy");
        dinhDangNut(btnHuy, false);
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnLuu = new JButton("Lưu");
        dinhDangNut(btnLuu, true);
        btnLuu.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().replaceAll("\\s", "");
            String diaChi = txtDiaChi.getText().trim();
            String ngayStr = txtNgay.getText().trim();
            String loi = null;
            LocalDate ngay = null;

            if (ten.isEmpty()) {
                loi = "Vui lòng nhập họ và tên.";
            } else if (!sdt.matches("0\\d{9}")) {
                loi = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.";
            } else {
                if (!ngayStr.isEmpty()) {
                    try {
                        ngay = LocalDate.parse(ngayStr, DINH_DANG_NGAY);
                        if (ngay.isAfter(LocalDate.now()))
                            loi = "Ngày sinh không được ở tương lai.";
                    } catch (DateTimeParseException ex) {
                        loi = "Ngày sinh không hợp lệ (định dạng dd/MM/yyyy).";
                    }
                }
            }
            if (loi != null) {
                lblLoi.setText(loi);
                return;
            }

            double luong = 0;
            try {
                luong = Double.parseDouble(txtLuong.getText().replaceAll("[.,\\s]", ""));
                if (luong < 0) {
                    lblLoi.setText("Lương cơ bản không được âm.");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblLoi.setText("Lương cơ bản không hợp lệ (nhập số).");
                return;
            }
            String gt = (String) cbGT.getSelectedItem();
            String chucVu = (String) cbChucVu.getSelectedItem();
            String dc = diaChi.isEmpty() ? null : diaChi;
            try {
                if (goc == null) {
                    dao.them(new NhanVien_entity(txtMa.getText(), null, ten, chucVu, sdt, dc, ngay, gt, luong, null));
                } else {
                    dao.capNhat(new NhanVien_entity(goc.getMaNhanVien(), goc.getMaTK(), ten, chucVu, sdt, dc, ngay, gt,
                            luong, goc.getTrangThaiTK()));
                    if (coTK) {
                        boolean hoatDong = cbTT.getSelectedIndex() == 0;
                        if (hoatDong != Boolean.TRUE.equals(goc.getTrangThaiTK())) {
                            dao.doiTrangThaiTaiKhoan(goc.getMaNhanVien(), hoatDong);
                        }
                    }
                }
            } catch (SQLException ex) {
                lblLoi.setText("Lỗi CSDL: " + ex.getMessage());
                return;
            }
            dlg.dispose();
            taiLai();
        });

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setBackground(Color.WHITE);
        nut.setBorder(new EmptyBorder(0, 24, 16, 24));
        nut.add(btnHuy);
        if (!chiXem)
            nut.add(btnLuu);

        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.getContentPane().add(noiDung, BorderLayout.CENTER);
        dlg.getContentPane().add(nut, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Tổng lương nhận trong tháng hiện tại tính đến hôm nay:
     * lương chức vụ phân bổ theo số ngày đã qua trong tháng + hoa hồng 5% dịch vụ
     * (nhân viên dịch vụ). Quản lý chưa tính lương.
     */
    private String tinhTongLuongThang(NhanVien_entity nv) {
        if (nv == null) {
            return "Chưa áp dụng";
        }
        LocalDate homNay = LocalDate.now();
        LocalDate dauThang = homNay.withDayOfMonth(1);
        String chucVu = nz(nv.getChucVu());
        double luongCoBanGoc = nv.getLuongCoBan();
        double luongCoBan = 0;
        String moTaThem = "";

        if ("Nhân viên bán hàng".equals(chucVu) || "Nhân viên dịch vụ".equals(chucVu)) {
            double soGio = 0;
            try {
                soGio = dao.getSoGioLamViecCuaNhanVien(nv.getMaNhanVien(), dauThang, homNay);
            } catch (SQLException ex) {
                soGio = 0;
            }
            luongCoBan = soGio * luongCoBanGoc;
            moTaThem = " · " + soGio + " giờ làm (" + dinhDangTien(luongCoBanGoc) + "/giờ)";
        } else {
            luongCoBan = luongCoBanGoc * homNay.getDayOfMonth() / homNay.lengthOfMonth();
            moTaThem = " · " + homNay.getDayOfMonth() + "/" + homNay.lengthOfMonth() + " ngày (" + dinhDangTien(luongCoBanGoc) + "/tháng)";
        }

        double hoaHong = 0;
        if ("Nhân viên dịch vụ".equals(chucVu)) {
            try {
                hoaHong = dao.getDoanhThuDichVuCuaNhanVien(nv.getMaNhanVien(), dauThang, homNay) * 0.05;
            } catch (SQLException ex) {
                hoaHong = 0;
            }
        }
        return dinhDangTien(luongCoBan + hoaHong)
                + "  (lương " + dinhDangTien(luongCoBan)
                + (hoaHong > 0 ? " + hoa hồng " + dinhDangTien(hoaHong) : "")
                + moTaThem + ")";
    }

    private void xuatCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danh_sach_nhan_vien.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(
                "Mã nhân viên,Họ và tên,Chức vụ,Số điện thoại,Địa chỉ,Ngày sinh,Giới tính,Trạng thái tài khoản\n");
        for (NhanVien_entity nv : daLoc) {
            sb.append(csv(nv.getMaNhanVien())).append(',')
                    .append(csv(nz(nv.getHoTen()))).append(',')
                    .append(csv(nz(nv.getChucVu()))).append(',')
                    .append(csv("'" + nz(nv.getSdt()).replace(" ", ""))).append(',')
                    .append(csv(nz(nv.getDiaChi()))).append(',')
                    .append(csv(nv.getNgaySinh() == null ? "" : DINH_DANG_NGAY.format(nv.getNgaySinh()))).append(',')
                    .append(csv(nz(nv.getGioiTinh()))).append(',')
                    .append(csv(TrangThai.tuEntity(nv).nhan)).append('\n');
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất " + daLoc.size() + " nhân viên.",
                    "Xuất danh sách", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private void taiLai() {
        try {
            List<NhanVien_entity> ds = dao.getAll();
            tatCa.clear();
            tatCa.addAll(ds);
            Set<String> maHienCo = new HashSet<>();
            for (NhanVien_entity nv : tatCa)
                maHienCo.add(nv.getMaNhanVien());
            daChon.retainAll(maHienCo);
        } catch (SQLException ex) {
            baoLoi("Không thể tải danh sách nhân viên", ex);
        }
        lamMoi();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String dinhDangTien(double so) {
        return NumberFormat.getInstance(VN).format(so) + " ₫";
    }

    private class BangModel extends AbstractTableModel {
        private final String[] cot = { "", "Mã nhân viên", "Họ và tên", "Chức vụ", "Liên hệ", "Ngày sinh",
                "Trạng thái" };
        private List<NhanVien_entity> hang = new ArrayList<>();

        void datHang(List<NhanVien_entity> h) {
            hang = new ArrayList<>(h);
            fireTableDataChanged();
        }

        NhanVien_entity tai(int r) {
            return hang.get(r);
        }

        @Override
        public int getRowCount() {
            return hang.size();
        }

        @Override
        public int getColumnCount() {
            return cot.length;
        }

        @Override
        public String getColumnName(int c) {
            return cot[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 0;
        }

        @Override
        public Object getValueAt(int r, int c) {
            NhanVien_entity nv = hang.get(r);
            return c == 0 ? (Object) daChon.contains(nv.getMaNhanVien()) : nv;
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            if (c != 0)
                return;
            String ma = hang.get(r).getMaNhanVien();
            if (Boolean.TRUE.equals(v))
                daChon.add(ma);
            else
                daChon.remove(ma);
            fireTableRowsUpdated(r, r);
            bang.getTableHeader().repaint();
        }
    }

    private static Color nen(boolean chon) {
        return chon ? NEN_CHON : Color.WHITE;
    }

    private static void batKhuRang(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String layChu(NhanVien_entity nv, int cot) {
        switch (cot) {
            case 1:
                return nv.getMaNhanVien();
            case 3:
                return nz(nv.getChucVu());
            case 5:
                return nv.getNgaySinh() == null ? "" : DINH_DANG_NGAY.format(nv.getNgaySinh());
            default:
                return "";
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
            setText(layChu((NhanVien_entity) v, cot));
            setBackground(nen(sel));
            setForeground(cot == 1 ? RangBuocGiaoDien.MAU_CHU_PHU : RangBuocGiaoDien.MAU_CHU_CHINH);
            return this;
        }
    }

    private static class TenRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblAvatar = new JLabel();
        private final JLabel lblTen = new JLabel();

        TenRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            lblTen.setFont(RangBuocGiaoDien.fontDam(13));
            lblTen.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            add(lblAvatar);
            add(Box.createHorizontalStrut(10));
            add(lblTen);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            NhanVien_entity nv = (NhanVien_entity) v;
            lblAvatar.setIcon(new AvatarIcon(chuCaiDau(nv.getHoTen())));
            lblTen.setText(nv.getHoTen());
            setBackground(nen(sel));
            return this;
        }

        private static String chuCaiDau(String ten) {
            String[] tu = ten.trim().split("\\s+");
            if (tu.length == 1)
                return tu[0].substring(0, 1).toUpperCase(VN);
            return (tu[tu.length - 2].substring(0, 1) + tu[tu.length - 1].substring(0, 1)).toUpperCase(VN);
        }
    }

    private static class AvatarIcon implements Icon {
        private final String chu;

        AvatarIcon(String chu) {
            this.chu = chu;
        }

        @Override
        public int getIconWidth() {
            return 32;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);
            g2.setColor(NEN_ICON);
            g2.fill(new Ellipse2D.Double(x, y, 32, 32));
            g2.setFont(RangBuocGiaoDien.fontDam(11));
            g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(chu, x + (32 - fm.stringWidth(chu)) / 2, y + (32 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static class LienHeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblSdt = new JLabel();
        private final JLabel lblDiaChi = new JLabel();

        LienHeRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            lblSdt.setFont(RangBuocGiaoDien.fontThuong(13));
            lblSdt.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lblDiaChi.setFont(RangBuocGiaoDien.fontThuong(11));
            lblDiaChi.setForeground(RangBuocGiaoDien.MAU_CHU_PHU);
            add(Box.createVerticalGlue());
            add(lblSdt);
            add(lblDiaChi);
            add(Box.createVerticalGlue());
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            NhanVien_entity nv = (NhanVien_entity) v;
            lblSdt.setText(dinhDangSdt(nv.getSdt()));
            lblDiaChi.setText(nz(nv.getDiaChi()));
            lblDiaChi.setToolTipText(nz(nv.getDiaChi()));
            setBackground(nen(sel));
            return this;
        }
    }

    private static class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private TrangThai tt = TrangThai.DANG_LAM;

        TrangThaiRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            tt = TrangThai.tuEntity((NhanVien_entity) v);
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
                case DANG_LAM:
                    nenPill = new Color(209, 250, 229);
                    chu = new Color(6, 95, 70);
                    cham = new Color(16, 185, 129);
                    break;
                case CHUA_TK:
                    nenPill = new Color(254, 243, 199);
                    chu = new Color(146, 64, 14);
                    cham = new Color(217, 119, 6);
                    break;
                default:
                    nenPill = new Color(238, 241, 245);
                    chu = new Color(71, 85, 105);
                    cham = new Color(148, 163, 184);
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
        private final JCheckBox chk = new JCheckBox();

        HeaderRenderer() {
            lbl.setOpaque(true);
            lbl.setBackground(NEN_HEADER);
            lbl.setFont(RangBuocGiaoDien.fontDam(12));
            lbl.setForeground(RangBuocGiaoDien.MAU_CHU_CHINH);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            chk.setOpaque(true);
            chk.setBackground(NEN_HEADER);
            chk.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            if (c == 0) {
                chk.setSelected(tatCaTrangDuocChon());
                return chk;
            }
            String text = v == null ? "" : v.toString();
            if (c == 2) {
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

    private static class HuyHieuIcon extends JPanel {
        private final int loai;

        HuyHieuIcon(int loai) {
            this.loai = loai;
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            batKhuRang(g2);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(NEN_ICON);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

            g2.setColor(RangBuocGiaoDien.MAU_CHU_DAO);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (loai == 2) {
                g2.drawOval(cx - 8, cy - 8, 16, 16);
                g2.drawLine(cx, cy, cx, cy - 5);
                g2.drawLine(cx, cy, cx + 4, cy + 2);
            } else {
                g2.drawOval(cx - 6, cy - 9, 7, 7);
                g2.drawArc(cx - 10, cy + 1, 14, 12, 0, 180);
                if (loai == 0) {
                    g2.drawArc(cx + 2, cy - 1, 10, 10, 20, 140);
                } else if (loai == 1) {
                    g2.drawLine(cx + 4, cy + 3, cx + 7, cy + 6);
                    g2.drawLine(cx + 7, cy + 6, cx + 12, cy);
                } else {
                    g2.drawLine(cx + 5, cy + 3, cx + 12, cy + 3);
                }
            }
            g2.dispose();
        }
    }
}
