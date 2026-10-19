
package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp kết nối Cơ sở dữ liệu Microsoft SQL Server cho SALALA Pet Shop.
 * Gộp chức năng từ ConnectDB (static) và DBConnection (Singleton).
 *
 * Cách sử dụng:
 *   - Static:    Connection con = ConnectDB.getCon();
 *   - Singleton: Connection con = ConnectDB.getInstance().getConnection();
 *
 * Hỗ trợ tự động dò nhiều URL cấu hình tiềm năng (Named Instance, Default Instance,
 * Windows Authentication), tự động reconnect khi mất kết nối, và giới hạn tần suất retry
 * để tránh đơ giao diện Swing.
 */
public class ConnectDB {

    // ========================= CẤU HÌNH =========================
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    /** Danh sách URL ứng viên – sẽ thử lần lượt cho đến khi thành công. */
    private static final String[] CANDIDATE_URLS = {
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS01;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS01;databaseName=SALALA;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;databaseName=SALALA;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;"
    };

    // ======================== SINGLETON =========================
    private static final ConnectDB instance = new ConnectDB();

    /** Kết nối hiện tại do Singleton quản lý. */
    private Connection con = null;

    /** URL đã từng kết nối thành công – dùng lại để tránh dò lại mỗi lần. */
    private static String successfulUrl = null;

    /** Thời điểm lần thử kết nối gần nhất (ms). */
    private long lastConnectAttempt = 0;

    /** Khoảng cách tối thiểu giữa hai lần thử kết nối (ms). */
    private static final long RETRY_INTERVAL_MS = 5000;

    private ConnectDB() {
        // Private constructor – sử dụng getInstance() hoặc getCon().
    }

    /** Trả về Singleton instance. */
    public static ConnectDB getInstance() {
        return instance;
    }

    // =================== STATIC API (tương thích ConnectDB cũ) ===================

    /**
     * Tạo và trả về một kết nối mới đến SQL Server.
     * Đây là API static gốc của ConnectDB cũ – mỗi lần gọi sẽ dò URL và mở kết nối mới.
     */
    public static Connection getCon() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ignored) {
        }

        // Thử URL đã từng thành công trước
        if (successfulUrl != null) {
            try {
                if (successfulUrl.contains("integratedSecurity=true")) {
                    return DriverManager.getConnection(successfulUrl);
                } else {
                    return DriverManager.getConnection(successfulUrl, USER, PASSWORD);
                }
            } catch (SQLException e) {
                successfulUrl = null; // reset để dò lại
            }
        }

        // Duyệt danh sách URL cấu hình tiềm năng
        for (String url : CANDIDATE_URLS) {
            try {
                Connection conn;
                if (url.contains("integratedSecurity=true")) {
                    conn = DriverManager.getConnection(url);
                } else {
                    conn = DriverManager.getConnection(url, USER, PASSWORD);
                }
                successfulUrl = url;
                System.out.println("Kết nối CSDL SALALA thành công qua: " + url);
                return conn;
            } catch (SQLException ignored) {
            }
        }

        System.err.println("Không thể kết nối SQL Server (SALALA). Vui lòng kiểm tra dịch vụ SQL Server và CSDL SALALA.");
        return null;
    }

    // =============== SINGLETON API (tương thích DBConnection cũ) ===============

    /**
     * Khởi tạo / tái kết nối kết nối Singleton đến CSDL.
     * Có cơ chế giới hạn tần suất retry để tránh đơ giao diện.
     */
    public synchronized void connect() {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    return; // Đã có kết nối hợp lệ
                }
            } catch (SQLException ignored) {
            }
        }

        long now = System.currentTimeMillis();
        if (now - lastConnectAttempt < RETRY_INTERVAL_MS && successfulUrl == null) {
            return; // Tránh thử lại liên tục làm đơ giao diện Swing
        }
        lastConnectAttempt = now;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ignored) {
        }

        // Thử URL đã từng kết nối thành công trước tiên
        if (successfulUrl != null) {
            try {
                if (successfulUrl.contains("integratedSecurity=true")) {
                    con = DriverManager.getConnection(successfulUrl);
                } else {
                    con = DriverManager.getConnection(successfulUrl, USER, PASSWORD);
                }
                return;
            } catch (SQLException e) {
                successfulUrl = null;
            }
        }

        // Duyệt danh sách URL cấu hình tiềm năng
        for (String url : CANDIDATE_URLS) {
            try {
                if (url.contains("integratedSecurity=true")) {
                    con = DriverManager.getConnection(url);
                } else {
                    con = DriverManager.getConnection(url, USER, PASSWORD);
                }
                successfulUrl = url;
                System.out.println("Kết nối CSDL SALALA thành công qua: " + url);
                return;
            } catch (SQLException ignored) {
            }
        }

        System.err.println("Chưa thể kết nối CSDL SQL Server (SALALA). Vui lòng kiểm tra dịch vụ SQL Server và CSDL SALALA.");
    }

    /**
     * Ngắt kết nối CSDL (Singleton).
     */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy đối tượng Connection Singleton, tự động kết nối lại nếu chưa có hoặc đã đóng.
     * Đây là API instance dành cho các file đang dùng DBConnection.getInstance().getCon().
     *
     * Lưu ý: Khác với getCon() static (tạo connection mới mỗi lần),
     * phương thức này quản lý một connection duy nhất và tái sử dụng.
     */
    public Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return con;
    }

    // ======================== TIỆN ÍCH ========================

    /**
     * Kiểm tra trạng thái kết nối tới SQL Server.
     * @return true nếu kết nối thành công và hợp lệ, ngược lại trả về false.
     */
    public static boolean checkConnection() {
        try (Connection conn = getCon()) {
            if (conn != null && !conn.isClosed()) {
                return conn.isValid(2);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra kết nối: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Đang kiểm tra kết nối tới cơ sở dữ liệu...");

        if (checkConnection()) {
            System.out.println("✅ Kết nối thành công đến cơ sở dữ liệu SQL Server!");
        } else {
            System.out.println("❌ Kết nối thất bại! Vui lòng kiểm tra lại cấu hình.");
        }
    }
}
