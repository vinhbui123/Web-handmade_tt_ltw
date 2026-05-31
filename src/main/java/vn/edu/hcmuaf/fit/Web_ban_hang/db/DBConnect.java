package vn.edu.hcmuaf.fit.Web_ban_hang.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;


public class DBConnect {
    private static final Logger log = LoggerFactory.getLogger(DBConnect.class);
    static String url = "jdbc:mysql://" + DBProperties.host() + ":" + DBProperties.port() + "/" + DBProperties.dbname() + "?" + DBProperties.option();

    // Mỗi lần gọi sẽ trả về connection MỚI an toàn hơn
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, DBProperties.username(), DBProperties.password());
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Failed to establish database connection.", e);
            throw new SQLException("Connection failed: " + e.getMessage());
        }
    }
    public static Statement getStatement() {
        try {
            Connection conn = getConnection();
            return conn.createStatement();
        } catch (SQLException e) {
            log.error("Failed to get database statement.", e);
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Connection connection = DBConnect.getConnection();
            if (connection != null) {
                System.out.println("Connected to the database successfully!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            // Use log.error instead of System.err.println()
            log.error("Error connecting to database in main method.", e);
        }
    }
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            log.error("Error closing connection: {}", e.getMessage(), e);
        }
    }

    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null && !stmt.isClosed()) stmt.close();
        } catch (SQLException e) {
            log.error("Error closing statement: {}", e.getMessage(), e);
        }
    }

    public static void closePreparedStatement(PreparedStatement ps) {
        try {
            if (ps != null && !ps.isClosed()) ps.close();
        } catch (SQLException e) {
            // Use log.error instead of e.printStackTrace()
            log.error("Error closing prepared statement.", e);
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) rs.close();
        } catch (SQLException e) {
            // Use log.error instead of e.printStackTrace()
            log.error("Error closing result set.", e);
        }
    }

}