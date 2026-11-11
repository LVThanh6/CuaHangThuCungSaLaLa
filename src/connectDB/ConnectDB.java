package connectDB;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Lớp kết nối CSDL Microsoft SQL Server cho SALALA Pet Shop (Tối ưu hóa hiệu năng & chống lag).
 *
 * Hỗ trợ:
 * 1. Static: Connection con = ConnectDB.getCon();
 * 2. Singleton: Connection con = ConnectDB.getInstance().getConnection();
 * 3. Tái sử dụng kết nối (Connection Reusing / Pooling proxy), triệt tiêu chi phí mở kết nối TCP lặp lại.
 * 4. Ngăn chặn việc vô tình đóng kết nối dùng chung khi các DAO gọi con.close().
 * 5. Giảm tần suất kiểm tra ping isValid() tránh giật lag giao diện.
 */
public class ConnectDB {

    // ========================= CẤU HÌNH =========================

    private static final String DATABASE_NAME = "SALALA";

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

    // Thời gian chờ kết nối tối đa mỗi lần thử (giây) - giảm xuống 2s để failover nhanh
    private static final int LOGIN_TIMEOUT = 2;

    private static final String[] CANDIDATE_URLS = createCandidateUrls();

    // ======================== SINGLETON =========================

    private static final ConnectDB instance = new ConnectDB();

    private Connection physicalConnection = null;
    private Connection proxyConnection = null;

    private static volatile String successfulUrl = null;
    private static volatile boolean successfulIntegratedAuth = false;
    private long lastConnectAttempt = 0;
    private long lastHealthCheckTime = 0;
    private static final long HEALTH_CHECK_INTERVAL_MS = 30000; // 30 giây mới ping 1 lần
    private static final long RETRY_INTERVAL_MS = 3000;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    // ======================= CONFIGURATION =======================

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

    private static String[] createCandidateUrls() {
        LinkedHashSet<String> uniqueUrls = new LinkedHashSet<>();

        addInstanceUrls(uniqueUrls, INSTANCE_NAME);
        addInstanceUrls(uniqueUrls, "SQLEXPRESS01");
        addInstanceUrls(uniqueUrls, "SQLEXPRESS");
        addPortUrl(uniqueUrls, PORT);
        addDefaultInstanceUrl(uniqueUrls);

        List<String> urls = new ArrayList<>();
        // SQL Server Authentication trước
        for (String url : uniqueUrls) {
            urls.add(url);
        }
        // Windows Authentication sau
        for (String url : uniqueUrls) {
            urls.add(url + "integratedSecurity=true;");
        }

        return urls.toArray(new String[0]);
    }

    private static void addInstanceUrls(LinkedHashSet<String> urls, String instanceName) {
        if (instanceName == null || instanceName.isEmpty()) return;
        String url = "jdbc:sqlserver://" + HOST
                + ";instanceName=" + instanceName
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";
        urls.add(url);
    }

    private static void addPortUrl(LinkedHashSet<String> urls, String port) {
        if (port == null || port.isEmpty()) return;
        String url = "jdbc:sqlserver://" + HOST + ":" + port
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";
        urls.add(url);
    }

    private static void addDefaultInstanceUrl(LinkedHashSet<String> urls) {
        String url = "jdbc:sqlserver://" + HOST
                + ";databaseName=" + DATABASE_NAME
                + ";encrypt=true"
                + ";trustServerCertificate=true"
                + ";loginTimeout=" + LOGIN_TIMEOUT
                + ";";
        urls.add(url);
    }

    private static Connection openConnection(String url, boolean integratedAuth) throws SQLException {
        if (integratedAuth) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    private static boolean loadDriver() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Microsoft SQL Server JDBC Driver!");
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isIntegratedUrl(String url) {
        return url.contains("integratedSecurity=true");
    }

    // =================== PROXY CONNECTION WRAPPER =================

    /**
     * Tạo connection proxy thông minh:
     * - Khi DAO gọi con.close(): Không đóng physical connection thật sự để tránh giết chết kết nối dùng chung.
     * - Tự động reset autoCommit về true nếu phương thức trước đó chạy transaction dang dở.
     */
    private Connection createProxy(final Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("close".equals(name)) {
                            try {
                                if (real != null && !real.isClosed() && !real.getAutoCommit()) {
                                    real.setAutoCommit(true);
                                }
                            } catch (SQLException ignored) {}
                            return null;
                        }
                        if ("isClosed".equals(name)) {
                            return real == null || real.isClosed();
                        }
                        if ("unwrap".equals(name) && args != null && args.length == 1) {
                            Class<?> iface = (Class<?>) args[0];
                            if (iface.isInstance(real)) return real;
                        }
                        try {
                            return method.invoke(real, args);
                        } catch (InvocationTargetException ite) {
                            throw ite.getCause();
                        }
                    }
                }
        );
    }

    // =================== STATIC & SINGLETON API =================

    /**
     * Lấy kết nối CSDL chung (nhanh, không tạo lại kết nối TCP mỗi lần gọi).
     */
    public static Connection getCon() {
        return getInstance().getConnection();
    }

    /**
     * Khởi tạo hoặc tái kết nối Singleton.
     */
    public synchronized void connect() {
        if (physicalConnection != null) {
            try {
                if (!physicalConnection.isClosed()) {
                    return;
                }
            } catch (SQLException ignored) {}
            disconnect();
        }

        long now = System.currentTimeMillis();
        if (now - lastConnectAttempt < RETRY_INTERVAL_MS) {
            return;
        }
        lastConnectAttempt = now;

        if (!loadDriver()) {
            return;
        }

        // 1. Thử URL đã từng kết nối thành công trước
        if (successfulUrl != null) {
            try {
                physicalConnection = openConnection(successfulUrl, successfulIntegratedAuth);
                if (physicalConnection != null && !physicalConnection.isClosed()) {
                    proxyConnection = createProxy(physicalConnection);
                    lastHealthCheckTime = System.currentTimeMillis();
                    return;
                }
            } catch (SQLException e) {
                successfulUrl = null;
                disconnect();
            }
        }

        // 2. Dò các URL ứng viên
        for (String url : CANDIDATE_URLS) {
            boolean integratedAuth = isIntegratedUrl(url);
            try {
                physicalConnection = openConnection(url, integratedAuth);
                if (physicalConnection != null && !physicalConnection.isClosed()) {
                    successfulUrl = url;
                    successfulIntegratedAuth = integratedAuth;
                    proxyConnection = createProxy(physicalConnection);
                    lastHealthCheckTime = System.currentTimeMillis();
                    System.out.println("Kết nối SALALA thành công! (" + (integratedAuth ? "Windows Auth" : "SQL Auth") + ")");
                    return;
                }
                disconnect();
            } catch (SQLException ignored) {
                disconnect();
            }
        }

        System.err.println("Chưa thể kết nối CSDL SALALA. Hãy kiểm tra SQL Server service.");
    }

    /**
     * Ngắt kết nối vật lý CSDL SALALA.
     */
    public synchronized void disconnect() {
        if (physicalConnection != null) {
            try {
                physicalConnection.close();
            } catch (SQLException ignored) {
            } finally {
                physicalConnection = null;
                proxyConnection = null;
            }
        }
    }

    /**
     * Lấy kết nối Singleton tái sử dụng cực nhanh.
     */
    public synchronized Connection getConnection() {
        try {
            long now = System.currentTimeMillis();
            if (physicalConnection == null || physicalConnection.isClosed()) {
                connect();
            } else if (now - lastHealthCheckTime > HEALTH_CHECK_INTERVAL_MS) {
                lastHealthCheckTime = now;
                if (!physicalConnection.isValid(1)) {
                    disconnect();
                    connect();
                }
            }
        } catch (SQLException e) {
            disconnect();
            connect();
        }

        if (physicalConnection == null) {
            return null;
        }

        if (proxyConnection == null) {
            proxyConnection = createProxy(physicalConnection);
        }
        return proxyConnection;
    }

    // ======================== TIỆN ÍCH ==========================

    public static boolean checkConnection() {
        Connection conn = getCon();
        return conn != null;
    }

    public static String getSuccessfulUrl() {
        return successfulUrl;
    }

    public synchronized boolean isConnected() {
        try {
            return physicalConnection != null && !physicalConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
