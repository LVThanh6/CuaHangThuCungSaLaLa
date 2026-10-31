
package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Lớp kết nối CSDL Microsoft SQL Server cho SALALA Pet Shop.
 *
 * Hỗ trợ:
 * 1. Static: Connection con = ConnectDB.getCon();
 * 2. Singleton: Connection con = ConnectDB.getInstance().getConnection();
 * 3. Nhiều SQL Server Instance.
 * 4. Kết nối bằng SQL Server Authentication.
 * 5. Kết nối bằng Windows Authentication.
 * 6. Tự động dò URL kết nối.
 * 7. Tự động reconnect khi mất kết nối.
 * 8. Giới hạn tần suất retry để tránh đơ giao diện Swing.
 *
 * Cấu hình riêng cho từng thiết bị thông qua Environment Variables:
 *
 * SALALA_DB_HOST      : Địa chỉ máy chủ (mặc định localhost)
 * SALALA_DB_INSTANCE  : Tên SQL Instance (mặc định SQLEXPRESS)
 * SALALA_DB_PORT      : Cổng TCP (mặc định 1433)
 * SALALA_DB_USER      : Tài khoản SQL Server (mặc định sa)
 * SALALA_DB_PASSWORD  : Mật khẩu (mặc định sapassword)
 *
 * Có thể ghi đè bằng Java System Properties tương ứng:
 * salala.db.host
 * salala.db.instance
 * salala.db.port
 * salala.db.user
 * salala.db.password
 */
public class ConnectDB {

    // ========================= CẤU HÌNH =========================

    private static final String DATABASE_NAME = "SALALA";

    // Cho phép mỗi thiết bị sử dụng cấu hình riêng.
    private static final String HOST = getConfig(
            "salala.db.host",
            "SALALA_DB_HOST",
            "localhost"
    );

    private static final String INSTANCE_NAME = getConfig(
            "salala.db.instance",
            "SALALA_DB_INSTANCE",
            "SQLEXPRESS"
    );

    private static final String PORT = getConfig(
            "salala.db.port",
            "SALALA_DB_PORT",
            "1433"
    );

    private static final String USER = getConfig(
            "salala.db.user",
            "SALALA_DB_USER",
            "sa"
    );

    private static final String PASSWORD = getConfig(
            "salala.db.password",
            "SALALA_DB_PASSWORD",
            "sapassword"
    );

    // Thời gian chờ kết nối (giây).
    private static final int LOGIN_TIMEOUT = 5;

    // Danh sách URL kết nối.
    private static final String[] CANDIDATE_URLS = createCandidateUrls();

    // ======================== SINGLETON =========================

    private static final ConnectDB instance = new ConnectDB();

    private Connection con = null;

    // URL kết nối thành công gần nhất.
    private static volatile String successfulUrl = null;

    // Loại xác thực của URL thành công.
    private static volatile boolean successfulIntegratedAuth = false;

    // Thời điểm thử kết nối gần nhất.
    private long lastConnectAttempt = 0;

    // Khoảng cách tối thiểu giữa hai lần retry.
    private static final long RETRY_INTERVAL_MS = 5000;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    // ======================= CONFIGURATION =======================

    /**
     * Lấy cấu hình theo thứ tự ưu tiên:
     * 1. Java System Properties.
     * 2. Environment Variables.
     * 3. Giá trị mặc định.
     */
    private static String getConfig(
            String propertyName,
            String environmentName,
            String defaultValue) {

        String value = System.getProperty(propertyName);

        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(environmentName);
        }

        if (value == null || value.trim().isEmpty()) {
            value = defaultValue;
        }

        return value.trim();
    }

    /**
     * Tạo danh sách URL kết nối.
     *
     * Giữ các nhánh:
     * - Named Instance SQLEXPRESS01.
     * - Named Instance SQLEXPRESS.
     * - Kết nối bằng TCP Port.
     * - SQL Server Authentication.
     * - Windows Authentication.
     */
    private static String[] createCandidateUrls() {

        List<String> urls = new ArrayList<>();

        // Tránh URL bị trùng.
        LinkedHashSet<String> uniqueUrls = new LinkedHashSet<>();

        // 1. Instance được cấu hình riêng cho thiết bị.
        addInstanceUrls(uniqueUrls, INSTANCE_NAME);

        // 2. Các Instance dự phòng.
        addInstanceUrls(uniqueUrls, "SQLEXPRESS01");
        addInstanceUrls(uniqueUrls, "SQLEXPRESS");

        // 3. Kết nối trực tiếp bằng TCP Port.
        addPortUrl(uniqueUrls, PORT);

        // 4. Default Instance không chỉ định cổng.
        addDefaultInstanceUrl(uniqueUrls);

        // Tạo danh sách SQL Authentication trước.
        for (String url : uniqueUrls) {
            urls.add(url);
        }

        // Sau đó tạo các URL Windows Authentication.
        for (String url : uniqueUrls) {
            urls.add(url + "integratedSecurity=true;");
        }

        return urls.toArray(new String[0]);
    }

    /**
     * Thêm URL theo Named Instance.
     */
    private static void addInstanceUrls(
            LinkedHashSet<String> urls,
            String instanceName) {

        if (instanceName == null || instanceName.isEmpty()) {
            return;
        }

        String url = "jdbc:sqlserver://" + HOST
                + ";instanceName=" + instanceName
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";

        urls.add(url);
    }

    /**
     * Thêm URL kết nối trực tiếp bằng TCP Port.
     */
    private static void addPortUrl(
            LinkedHashSet<String> urls,
            String port) {

        if (port == null || port.isEmpty()) {
            return;
        }

        String url = "jdbc:sqlserver://" + HOST + ":" + port
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";

        urls.add(url);
    }

    /**
     * Thêm URL cho Default Instance.
     */
    private static void addDefaultInstanceUrl(
            LinkedHashSet<String> urls) {

        String url = "jdbc:sqlserver://" + HOST
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";

        urls.add(url);
    }

    // =================== CONNECTION HELPER ======================

    /**
     * Mở kết nối theo URL và phương thức xác thực.
     */
    private static Connection openConnection(
            String url,
            boolean integratedAuth) throws SQLException {

        if (integratedAuth) {
            return DriverManager.getConnection(url);
        }

        return DriverManager.getConnection(
                url,
                USER,
                PASSWORD
        );
    }

    /**
     * Kiểm tra driver JDBC.
     */
    private static boolean loadDriver() {

        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );

            return true;

        } catch (ClassNotFoundException e) {

            System.err.println(
                    "Không tìm thấy Microsoft SQL Server JDBC Driver!"
            );

            e.printStackTrace();

            return false;
        }
    }

    /**
     * Xác định URL có sử dụng Windows Authentication hay không.
     */
    private static boolean isIntegratedUrl(String url) {

        return url.contains("integratedSecurity=true");
    }

    /**
     * In thông tin lỗi kết nối.
     */
    private static void printConnectionError(
            String url,
            SQLException e) {

        System.err.println("----------------------------------");
        System.err.println("KẾT NỐI SQL SERVER THẤT BẠI");
        System.err.println("URL: " + url);
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Message: " + e.getMessage());
        System.err.println("----------------------------------");
    }

    // =================== STATIC API =============================

    /**
     * Tạo và trả về một kết nối mới.
     *
     * Tương thích với:
     * Connection con = ConnectDB.getCon();
     */
    public static Connection getCon() {

        if (!loadDriver()) {
            return null;
        }

        // Thử URL đã từng kết nối thành công trước.
        String previousUrl = successfulUrl;

        if (previousUrl != null) {

            try {

                Connection conn = openConnection(
                        previousUrl,
                        successfulIntegratedAuth
                );

                if (conn != null && conn.isValid(2)) {
                    return conn;
                }

                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException e) {

                System.err.println(
                        "URL kết nối trước đó không còn hoạt động: "
                        + e.getMessage()
                );

                successfulUrl = null;
            }
        }

        SQLException lastError = null;

        // Dò lần lượt các URL.
        for (String url : CANDIDATE_URLS) {

            boolean integratedAuth = isIntegratedUrl(url);

            try {

                Connection conn = openConnection(
                        url,
                        integratedAuth
                );

                if (conn != null && conn.isValid(2)) {

                    successfulUrl = url;
                    successfulIntegratedAuth = integratedAuth;

                    System.out.println(
                            "Kết nối CSDL SALALA thành công!"
                    );

                    System.out.println(
                            "Phương thức xác thực: "
                            + (integratedAuth
                                    ? "Windows Authentication"
                                    : "SQL Server Authentication")
                    );

                    return conn;
                }

                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException e) {

                lastError = e;

                printConnectionError(url, e);
            }
        }

        System.err.println(
                "Không thể kết nối SQL Server (SALALA)."
        );

        if (lastError != null) {
            System.err.println(
                    "Lỗi cuối cùng: " + lastError.getMessage()
            );
        }

        return null;
    }

    // =================== SINGLETON API ==========================

    /**
     * Khởi tạo hoặc tái kết nối Singleton.
     */
    public synchronized void connect() {

        // Nếu kết nối hiện tại vẫn hoạt động thì không tạo mới.
        if (con != null) {

            try {

                if (!con.isClosed() && con.isValid(2)) {
                    return;
                }

            } catch (SQLException e) {

                System.err.println(
                        "Kết nối cũ không còn hợp lệ: "
                        + e.getMessage()
                );
            }

            disconnect();
        }

        // Giới hạn retry.
        long now = System.currentTimeMillis();

        if (now - lastConnectAttempt < RETRY_INTERVAL_MS) {

            return;
        }

        lastConnectAttempt = now;

        if (!loadDriver()) {
            return;
        }

        // Thử URL đã kết nối thành công trước đó.
        String previousUrl = successfulUrl;

        if (previousUrl != null) {

            try {

                con = openConnection(
                        previousUrl,
                        successfulIntegratedAuth
                );

                if (con != null && con.isValid(2)) {
                    return;
                }

                disconnect();

            } catch (SQLException e) {

                System.err.println(
                        "Không thể sử dụng lại URL cũ: "
                        + e.getMessage()
                );

                successfulUrl = null;
                disconnect();
            }
        }

        SQLException lastError = null;

        // Dò các URL dự phòng.
        for (String url : CANDIDATE_URLS) {

            boolean integratedAuth = isIntegratedUrl(url);

            try {

                con = openConnection(
                        url,
                        integratedAuth
                );

                if (con != null && con.isValid(2)) {

                    successfulUrl = url;
                    successfulIntegratedAuth = integratedAuth;

                    System.out.println(
                            "Kết nối Singleton SALALA thành công!"
                    );

                    return;
                }

                disconnect();

            } catch (SQLException e) {

                lastError = e;

                printConnectionError(url, e);

                disconnect();
            }
        }

        System.err.println(
                "Chưa thể kết nối SQL Server (SALALA)."
        );

        if (lastError != null) {

            System.err.println(
                    "Lỗi cuối cùng: " + lastError.getMessage()
            );
        }
    }

    /**
     * Ngắt kết nối Singleton.
     */
    public synchronized void disconnect() {

        if (con != null) {

            try {

                con.close();

            } catch (SQLException e) {

                System.err.println(
                        "Lỗi khi ngắt kết nối: " + e.getMessage()
                );

            } finally {

                con = null;
            }
        }
    }

    /**
     * Lấy kết nối Singleton.
     *
     * Tương thích với:
     * ConnectDB.getInstance().getConnection();
     */
    public synchronized Connection getConnection() {

        try {

            if (con == null
                    || con.isClosed()
                    || !con.isValid(2)) {

                connect();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Lỗi kiểm tra Singleton Connection: "
                    + e.getMessage()
            );

            disconnect();
            connect();
        }

        return con;
    }

    // ======================== TIỆN ÍCH ==========================

    /**
     * Kiểm tra trạng thái kết nối.
     */
    public static boolean checkConnection() {

        Connection conn = getCon();

        if (conn == null) {
            return false;
        }

        try {

            return conn.isValid(2);

        } catch (SQLException e) {

            System.err.println(
                    "Lỗi kiểm tra kết nối: " + e.getMessage()
            );

            return false;

        } finally {

            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy URL đã kết nối thành công.
     */
    public static String getSuccessfulUrl() {
        return successfulUrl;
    }

    /**
     * Kiểm tra kết nối Singleton.
     */
    public synchronized boolean isConnected() {

        try {

            return con != null
                    && !con.isClosed()
                    && con.isValid(2);

        } catch (SQLException e) {

            return false;
        }
    }

    // ======================== MAIN ==============================

    public static void main(String[] args) {

        System.out.println(
                "======================================"
        );

        System.out.println(
                "KIỂM TRA KẾT NỐI DATABASE SALALA"
        );

        System.out.println(
                "======================================"
        );

        System.out.println("Host: " + HOST);
        System.out.println("Instance: " + INSTANCE_NAME);
        System.out.println("Database: " + DATABASE_NAME);
        System.out.println("User: " + USER);

        // Kiểm tra Static Connection.
        System.out.println(
                "\nĐang kiểm tra Static Connection..."
        );

        if (checkConnection()) {

            System.out.println(
                    "Kết nối Static thành công!"
            );

        } else {

            System.err.println(
                    "Kết nối Static thất bại!"
            );
        }

        // Kiểm tra Singleton Connection.
        System.out.println(
                "\nĐang kiểm tra Singleton Connection..."
        );

        ConnectDB db = ConnectDB.getInstance();

        Connection connection = db.getConnection();

        if (connection != null) {

            System.out.println(
                    "Kết nối Singleton thành công!"
            );

            System.out.println(
                    "URL: " + getSuccessfulUrl()
            );

        } else {

            System.err.println(
                    "Kết nối Singleton thất bại!"
            );
        }

        System.out.println(
                "\n======================================"
        );
    }
}
