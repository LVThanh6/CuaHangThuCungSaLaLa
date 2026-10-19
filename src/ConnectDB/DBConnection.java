package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp kết nối Cơ sở dữ liệu Microsoft SQL Server cho SALALA Pet Shop.
 * Áp dụng mô hình Singleton để quản lý duy nhất một kết nối.
 */
public class DBConnection {
    private Connection con = null;
    private static DBConnection instance = new DBConnection();

    public static DBConnection getInstance() {
        return instance;
    }

    private DBConnection() {
    }

    private static final String[] CANDIDATE_URLS = {
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS01;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=SALALA;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS01;databaseName=SALALA;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost;databaseName=SALALA;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;"
    };

    private static String successfulUrl = null;
    private long lastConnectAttempt = 0;
    private static final long RETRY_INTERVAL_MS = 5000;

    /**
     * Khởi tạo kết nối đến CSDL SALALA trên SQL Server
     */
    public synchronized void connect() {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    return;
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

        String user = "sa";
        String password = "sapassword";

        // Thử URL đã từng kết nối thành công trước tiên
        if (successfulUrl != null) {
            try {
                if (successfulUrl.contains("integratedSecurity=true")) {
                    con = DriverManager.getConnection(successfulUrl);
                } else {
                    con = DriverManager.getConnection(successfulUrl, user, password);
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
                    con = DriverManager.getConnection(url, user, password);
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
     * Ngắt kết nối CSDL
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
     * Lấy đối tượng Connection hiện tại, tự động kết nối lại nếu chưa có
     */
    public Connection getCon() {
        try {
            if (con == null || con.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return con;
    }
}
