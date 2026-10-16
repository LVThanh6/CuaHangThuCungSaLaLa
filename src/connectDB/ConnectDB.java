package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

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
