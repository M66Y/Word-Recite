package com.wordapp.dao;

import com.wordapp.database.DatabaseHelper;
import com.wordapp.model.User;
import com.wordapp.util.SecurityUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 * 封装所有与用户相关的数据库操作
 */
public class UserDAO {

    // ==================== 用户基础操作 ====================

    /**
     * 注册新用户
     *
     * @param username 用户名
     * @param password 原始密码（将在此处加密）
     * @param email    邮箱
     * @return 注册成功返回true
     */
    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, SecurityUtil.hashPassword(password));
            pstmt.setString(3, email);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[UserDAO] 用户名已存在: " + username);
            return false;
        } catch (SQLException e) {
            System.err.println("[UserDAO] 注册失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 用户登录验证
     *
     * @param username 用户名
     * @param password 原始密码
     * @return 登录成功返回User对象，失败返回null
     */
    public User login(String username, String password) {
        String sql = "SELECT user_id, username, password_hash, email, created_at " +
                "FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (SecurityUtil.verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(storedHash);
                    user.setEmail(rs.getString("email"));
                    user.setCreatedAt(rs.getString("created_at"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 登录查询失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 存在返回true
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 检查用户名失败: " + e.getMessage());
        }
        return false;
    }

    // ==================== 背诵计划操作 ====================

    /**
     * 创建背诵计划（按天数分配）
     *
     * @param userId      用户ID
     * @param planName    计划名称
     * @param wordBookId  词书ID
     * @param totalDays   总天数
     * @return 创建成功返回计划ID，失败返回-1
     */
    public int createPlanByDays(int userId, String planName, int wordBookId, int totalDays) {
        // 先获取词书总词数
        int totalWords = getWordBookSize(wordBookId);
        if (totalWords <= 0) return -1;

        int wordsPerDay = (int) Math.ceil((double) totalWords / totalDays);

        String sql = "INSERT INTO study_plans (user_id, plan_name, word_book_id, total_days, words_per_day, created_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, planName);
            pstmt.setInt(3, wordBookId);
            pstmt.setInt(4, totalDays);
            pstmt.setInt(5, wordsPerDay);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 创建计划失败: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 创建背诵计划（按每日单词数分配）
     *
     * @param userId        用户ID
     * @param planName      计划名称
     * @param wordBookId    词书ID
     * @param wordsPerDay   每天背诵数量
     * @return 创建成功返回计划ID，失败返回-1
     */
    public int createPlanByWordsPerDay(int userId, String planName, int wordBookId, int wordsPerDay) {
        int totalWords = getWordBookSize(wordBookId);
        if (totalWords <= 0) return -1;

        int totalDays = (int) Math.ceil((double) totalWords / wordsPerDay);

        String sql = "INSERT INTO study_plans (user_id, plan_name, word_book_id, total_days, words_per_day, created_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, planName);
            pstmt.setInt(3, wordBookId);
            pstmt.setInt(4, totalDays);
            pstmt.setInt(5, wordsPerDay);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 创建计划失败: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 获取用户所有背诵计划
     *
     * @param userId 用户ID
     * @return 计划列表（每项为Object数组：[planId, planName, wordBookId, totalDays, wordsPerDay, progress]）
     */
    public List<Object[]> getUserPlans(int userId) {
        List<Object[]> plans = new ArrayList<>();
        String sql = "SELECT sp.plan_id, sp.plan_name, sp.word_book_id, wb.book_name, " +
                "sp.total_days, sp.words_per_day, sp.current_day, sp.created_at " +
                "FROM study_plans sp " +
                "JOIN word_books wb ON sp.word_book_id = wb.book_id " +
                "WHERE sp.user_id = ? ORDER BY sp.created_at DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                plans.add(new Object[]{
                        rs.getInt("plan_id"),
                        rs.getString("plan_name"),
                        rs.getInt("word_book_id"),
                        rs.getString("book_name"),
                        rs.getInt("total_days"),
                        rs.getInt("words_per_day"),
                        rs.getInt("current_day"),
                        rs.getString("created_at")
                });
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 获取计划列表失败: " + e.getMessage());
        }
        return plans;
    }

    /**
     * 删除背诵计划
     *
     * @param planId 计划ID
     * @param userId 用户ID（防止越权删除）
     * @return 删除成功返回true
     */
    public boolean deletePlan(int planId, int userId) {
        String sql = "DELETE FROM study_plans WHERE plan_id = ? AND user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, planId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] 删除计划失败: " + e.getMessage());
            return false;
        }
    }

    // ==================== 词书与单词操作 ====================

    /**
     * 获取词书列表
     *
     * @return 词书列表（每项为Object数组：[bookId, bookName, totalWords]）
     */
    public List<Object[]> getAllWordBooks() {
        List<Object[]> books = new ArrayList<>();
        String sql = "SELECT book_id, book_name, total_words, description FROM word_books ORDER BY book_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("total_words"),
                        rs.getString("description")
                });
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 获取词书列表失败: " + e.getMessage());
        }
        return books;
    }

    /**
     * 获取词书中指定天的单词列表
     *
     * @param wordBookId  词书ID
     * @param planId      计划ID
     * @param dayNumber   第几天
     * @return 单词列表（每项为Object数组：[wordId, word, definition, phonetic]）
     */
    public List<Object[]> getWordsForDay(int wordBookId, int planId, int dayNumber) {
        List<Object[]> words = new ArrayList<>();
        // 通过计划获取每天单词数，然后分页查询
        String planSql = "SELECT words_per_day FROM study_plans WHERE plan_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement planStmt = conn.prepareStatement(planSql)) {

            planStmt.setInt(1, planId);
            ResultSet planRs = planStmt.executeQuery();
            if (!planRs.next()) return words;

            int wordsPerDay = planRs.getInt("words_per_day");
            int offset = (dayNumber - 1) * wordsPerDay;

            String sql = "SELECT word_id, word, definition, phonetic " +
                    "FROM words WHERE book_id = ? " +
                    "ORDER BY word_id LIMIT ? OFFSET ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, wordBookId);
            pstmt.setInt(2, wordsPerDay);
            pstmt.setInt(3, offset);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                words.add(new Object[]{
                        rs.getInt("word_id"),
                        rs.getString("word"),
                        rs.getString("definition"),
                        rs.getString("phonetic")
                });
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 获取今日单词失败: " + e.getMessage());
        }
        return words;
    }

    /**
     * 记录单词学习结果
     *
     * @param userId    用户ID
     * @param planId    计划ID
     * @param wordId    单词ID
     * @param correct   是否答对
     */
    public void recordWordResult(int userId, int planId, int wordId, boolean correct) {
        String sql = "INSERT INTO word_records (user_id, plan_id, word_id, correct, record_time) " +
                "VALUES (?, ?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE correct = ?, record_time = NOW()";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, planId);
            pstmt.setInt(3, wordId);
            pstmt.setBoolean(4, correct);
            pstmt.setBoolean(5, correct);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[UserDAO] 记录学习结果失败: " + e.getMessage());
        }
    }

    /**
     * 更新计划当前进度（当前天数）
     *
     * @param planId     计划ID
     * @param currentDay 当前天数
     */
    public void updatePlanProgress(int planId, int currentDay) {
        String sql = "UPDATE study_plans SET current_day = ? WHERE plan_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentDay);
            pstmt.setInt(2, planId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[UserDAO] 更新计划进度失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取词书单词总数
     */
    private int getWordBookSize(int wordBookId) {
        String sql = "SELECT total_words FROM word_books WHERE book_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, wordBookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total_words");

        } catch (SQLException e) {
            System.err.println("[UserDAO] 获取词书大小失败: " + e.getMessage());
        }
        return 0;
    }
}