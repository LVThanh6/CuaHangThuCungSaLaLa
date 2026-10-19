
package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lop ket noi SQL Server dung chung cho toan bo du an SALALA.
 * Hay sua SERVER_NAME, DATABASE, USER va PASSWORD theo may cua ban.
 */
public class ConnectDB {

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

    private ConnectDB() {
        // Khong tao doi tuong ket noi. Su dung ham static getConnection().
    }

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
