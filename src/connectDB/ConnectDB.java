package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

<<<<<<< Updated upstream
    public static ConnectDB getInstance() {
        return instance;
    }

    public static void setInstance(ConnectDB instance) {
        ConnectDB.instance = instance;
    }

    public void connect() {
        String user = "sa";
        String pwd = "sapassword";
        try {
            DriverManager.setLoginTimeout(3);
            // Thử cổng 1433 chuẩn
            String url = "jdbc:sqlserver://localhost:1433;databasename=SALALA;trustServerCertificate=true";
            con = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException e) {
            try {
                // Tự động kết nối qua cổng 1434 nếu cổng 1433 bị đóng/từ chối
                String url1434 = "jdbc:sqlserver://localhost:1434;databasename=SALALA;trustServerCertificate=true";
                con = DriverManager.getConnection(url1434, user, pwd);
            } catch (SQLException e2) {
                try {
                    // Thử mật khẩu dự phòng 123456789
                    String urlBackup = "jdbc:sqlserver://localhost:1434;databasename=SALALA;trustServerCertificate=true";
                    con = DriverManager.getConnection(urlBackup, user, "123456789");
                } catch (SQLException e3) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getCon() {
        if (con == null) {
            instance.connect();
        }
        return con;
    }

}
=======
    // Thay đổi thông số này cho khớp với máy bạn:
    private static final String SERVER_NAME = "localhost\\SQLEXPRESS01"; // Hoặc dùng "MSI\\SQLEXPRESS01"
    private static final String DATABASE = "SALALA";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    // URL kết nối dành cho Named Instance (loại bỏ PORT cố định 1433)
    private static final String URL =
            "jdbc:sqlserver://" + SERVER_NAME 
            + ";databaseName=" + DATABASE
            + ";encrypt=true;trustServerCertificate=true;";
            
    // ... các đoạn code phía dưới giữ nguyên ...
    /** Tao va tra ve ket noi moi den SQL Server. */
    public static Connection getCon() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Khong the ket noi SQL Server: " + e.getMessage());
            return null;
        }
    }
    
    /** 
     * Kiểm tra trạng thái kết nối tới SQL Server.
     * @return true nếu kết nối thành công và hợp lệ, ngược lại trả về false.
     */
    public static boolean checkConnection() {
        // Sử dụng try-with-resources để tự động đóng kết nối sau khi kiểm tra xong, tránh rò rỉ bộ nhớ
        try (Connection conn = getCon()) {
            if (conn != null && !conn.isClosed()) {
                // isValid(timeout) kiểm tra xem kết nối còn sống hay không (timeout tính bằng giây)
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
            System.out.println(" Kết nối thành công đến cơ sở dữ liệu SQL Server!");
        } else {
            System.out.println("❌ Kết nối thất bại! Vui lòng kiểm tra lại cấu hình (Server, Port, Tài khoản, Mật khẩu, hoặc trạng thái SQL Server).");
        }
    }
}
>>>>>>> Stashed changes
