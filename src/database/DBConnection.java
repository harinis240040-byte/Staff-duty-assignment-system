package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple DBConnection: returns a new Connection for each call.
 * This is safe for desktop apps and avoids using a closed/expired shared Connection.
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_duty_db?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "9042"; // <- change accordingly

    static {
        try {
            // Optional: ensure driver loaded (modern drivers auto-register)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // driver not found: handle/report
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        // return a fresh connection each time
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
