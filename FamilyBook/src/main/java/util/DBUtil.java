package util;

import java.sql.*;

public class DBUtil {
    // 数据库连接配置
    private static final String URL = "jdbc:mysql://localhost:3306/family_account?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true";
    private static final String USER = "root";      // MySQL用户名
    private static final String PASSWORD = "522622ab@YW"; // MySQL密码

    // 静态代码块，注册驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 关闭连接
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 简化版关闭连接
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.getConnection();
            System.out.println("数据库连接成功！" + conn);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接失败：" + e.getMessage());
        }
    }
}