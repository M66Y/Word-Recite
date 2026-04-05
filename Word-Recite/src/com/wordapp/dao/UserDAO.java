package com.wordapp.dao;

import com.wordapp.database.DatabaseHelper;
import com.wordapp.model.User;
import com.wordapp.util.SecurityUtil;

import java.sql.*;

public class UserDAO {

    public boolean register(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseHelper.getInstance().getConnection();
            String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, SecurityUtil.md5(password));
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseHelper.getInstance().close(conn, stmt);
        }
    }

    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseHelper.getInstance().getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, SecurityUtil.md5(password));

            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.getInstance().close(conn, stmt, rs);
        }
        return null;
    }
    
    public boolean isUserExists(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseHelper.getInstance().getConnection();
            String sql = "SELECT 1 FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseHelper.getInstance().close(conn, stmt, rs);
        }
        return false;
    }
}
