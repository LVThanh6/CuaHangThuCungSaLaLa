package GUI.Chung;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Vùng chứa nội dung: vẽ một "tấm nền" kem bán trong suốt ngay phía sau nội
 * dung
 * để chữ luôn rõ nhưng ảnh vẫn thấp thoáng bên dưới.
 * Mặc định (lề = 0) tấm nền phủ kín 100% vùng còn lại dưới header; nếu đặt lề >
 * 0
 * thì tấm nền bo góc, có bóng và ảnh nền hiện ra như khung viền quanh mép.
 */
public class TamNenNoiDung extends JPanel {

    private static final int BO_GOC = 18;

    public TamNenNoiDung(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(
                RangBuocGiaoDien.LE_NOI_DUNG_DOC, RangBuocGiaoDien.LE_NOI_DUNG_NGANG,
                RangBuocGiaoDien.LE_NOI_DUNG_DOC, RangBuocGiaoDien.LE_NOI_DUNG_NGANG));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets in = getInsets();
        boolean kinMep = in.left == 0 && in.top == 0 && in.right == 0 && in.bottom == 0;
        int x = in.left;
        int y = in.top;
        int w = getWidth() - in.left - in.right;
        int h = getHeight() - in.top - in.bottom;
        if (kinMep) {
            // Phủ kín toàn bộ vùng nội dung: không bo góc, không bóng, không hở khe
            Color kem = RangBuocGiaoDien.MAU_TAM_NOI_DUNG;
            g2.setColor(new Color(kem.getRed(), kem.getGreen(), kem.getBlue(),
                    RangBuocGiaoDien.DO_DUC_TAM_NOI_DUNG));
            g2.fillRect(0, 0, getWidth(), getHeight());
            // Đường kẻ mảnh phân cách với header
            g2.setColor(new Color(226, 232, 240, 220));
            g2.drawLine(0, 0, getWidth(), 0);
        } else if (w > 0 && h > 0) {
            // Bóng đổ nhẹ
            for (int i = 1; i <= 4; i++) {
                g2.setColor(new Color(60, 40, 20, 10));
                g2.fillRoundRect(x - i, y - i + 2, w + 2 * i, h + 2 * i, BO_GOC + i, BO_GOC + i);
            }
            // Tấm nền kem bán trong suốt
            Color kem = RangBuocGiaoDien.MAU_TAM_NOI_DUNG;
            g2.setColor(new Color(kem.getRed(), kem.getGreen(), kem.getBlue(),
                    RangBuocGiaoDien.DO_DUC_TAM_NOI_DUNG));
            g2.fillRoundRect(x, y, w, h, BO_GOC, BO_GOC);
            // Viền mảnh
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawRoundRect(x, y, w - 1, h - 1, BO_GOC, BO_GOC);
        }
        g2.dispose();
    }
}
