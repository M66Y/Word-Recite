package com.wordapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接助手类
 * 负责建立和管理与MySQL数据库的连接
 */
public class DatabaseHelper {

    // 数据库连接配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/word_recite_db" +
            "?useSSL=false" +
            "&serverTimezone=Asia/Shanghai" +
            "&characterEncoding=utf8" +
            "&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456"; // 请根据实际情况修改

    // 单例连接（简单实现）
    private static Connection connection = null;

    /**
     * 静态初始化块，加载MySQL驱动
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DatabaseHelper] MySQL驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseHelper] MySQL驱动加载失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     * 若连接不存在或已关闭，则重新创建
     *
     * @return Connection 数据库连接对象
     * @throws SQLException 连接失败时抛出
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DatabaseHelper] 数据库连接成功");
        }
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DatabaseHelper] 数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("[DatabaseHelper] 关闭连接失败: " + e.getMessage());
            }
        }
    }

    /**
     * 测试数据库连接是否正常
     *
     * @return boolean 连接正常返回true
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DatabaseHelper] 连接测试失败: " + e.getMessage());
            return false;
        }
    }
}