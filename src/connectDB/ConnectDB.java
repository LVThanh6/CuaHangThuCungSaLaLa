package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lop ket noi SQL Server dung chung cho toan bo du an SALALA.
 * Hay sua SERVER_NAME, DATABASE, USER va PASSWORD theo may cua ban.
 */
public class ConnectDB {

    private static final String SERVER_NAME = "localhost";
    private static final String PORT = "1433";
    private static final String DATABASE = "SALALA";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    private static final String URL =
            "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=true;trustServerCertificate=true;";

    private ConnectDB() {
        // Khong tao doi tuong ket noi. Su dung ham static getConnection().
    }

    /** Tao va tra ve ket noi moi den SQL Server. */
    public static Connection getCon() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
