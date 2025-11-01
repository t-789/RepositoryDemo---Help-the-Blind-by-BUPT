package org.example.RepositoryDemo.Repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;
import org.example.RepositoryDemo.entity.Feedback;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FeedbackRepository {
    private static final Logger logger = LogManager.getLogger(FeedbackRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;

    // 创建反馈表
    public static void createFeedbackTable() throws SQLException {
        String createFeedbackTableSQL = "CREATE TABLE IF NOT EXISTS feedback (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NULL, " +
                "username TEXT NULL, " +
                "content TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +  // system 或 user
                "url TEXT NULL, " +
                "user_agent TEXT NULL, " +
                "stack_trace TEXT NULL, " +
                "create_time TIMESTAMP NOT NULL, " +
                "resolved BOOLEAN NOT NULL DEFAULT 0, " +
                "resolved_by TEXT NULL, " +
                "resolved_time TIMESTAMP NULL)";

        try (var stmt = connection.createStatement()) {
            stmt.execute(createFeedbackTableSQL);
            logger.info("反馈表初始化完成。");
        }
    }
    // 保存反馈
    public boolean saveFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback (user_id, username, content, type, url, user_agent, stack_trace, create_time, resolved) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, feedback.getUserId(), Types.INTEGER);
            pstmt.setString(2, feedback.getUsername());
            pstmt.setString(3, feedback.getContent());
            pstmt.setString(4, feedback.getType());
            pstmt.setString(5, feedback.getUrl());
            pstmt.setString(6, feedback.getUserAgent());
            pstmt.setString(7, feedback.getStackTrace());
            pstmt.setTimestamp(8, feedback.getCreateTime());
            pstmt.setBoolean(9, feedback.getResolved() != null ? feedback.getResolved() : false);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("保存反馈失败: {}", e.getMessage());
            return false;
        }
    }
    
    // 获取所有反馈
    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT * FROM feedback ORDER BY create_time DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                feedbacks.add(extractFeedbackFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("获取反馈列表失败: {}", e.getMessage());
        }
        return feedbacks;
    }
    
    // 根据类型获取反馈
    public List<Feedback> getFeedbackByType(String type) {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT * FROM feedback WHERE type = ? ORDER BY create_time DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("根据类型获取反馈列表失败: {}", e.getMessage());
        }
        return feedbacks;
    }
    
    // 根据解决状态获取反馈
    public List<Feedback> getFeedbackByResolvedStatus(boolean resolved) {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT * FROM feedback WHERE resolved = ? ORDER BY create_time DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolved);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("根据解决状态获取反馈列表失败: {}", e.getMessage());
        }
        return feedbacks;
    }
    
    // 更新反馈解决状态
    public boolean updateResolvedStatus(int feedbackId, boolean resolved, String resolvedBy) {
        String sql = "UPDATE feedback SET resolved = ?, resolved_by = ?, resolved_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolved);
            pstmt.setString(2, resolvedBy);
            pstmt.setTimestamp(3, resolved ? new Timestamp(System.currentTimeMillis()) : null);
            pstmt.setInt(4, feedbackId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("更新反馈解决状态失败: {}", e.getMessage());
            return false;
        }
    }
    
    // 根据ID获取反馈
    public Feedback getFeedbackById(int id) {
        String sql = "SELECT * FROM feedback WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractFeedbackFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据ID获取反馈失败: {}", e.getMessage());
        }
        return null;
    }
    
    // 从ResultSet中提取Feedback对象
    private Feedback extractFeedbackFromResultSet(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setId(rs.getInt("id"));
        feedback.setUserId(rs.getObject("user_id", Integer.class));
        feedback.setUsername(rs.getString("username"));
        feedback.setContent(rs.getString("content"));
        feedback.setType(rs.getString("type"));
        feedback.setUrl(rs.getString("url"));
        feedback.setUserAgent(rs.getString("user_agent"));
        feedback.setStackTrace(rs.getString("stack_trace"));
        feedback.setCreateTime(rs.getTimestamp("create_time"));
        feedback.setResolved(rs.getBoolean("resolved"));
        feedback.setResolvedBy(rs.getString("resolved_by"));
        feedback.setResolvedTime(rs.getTimestamp("resolved_time"));
        return feedback;
    }
}