package org.example.RepositoryDemo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SecurityQuestionManagement {
    private static final Logger logger = LogManager.getLogger(RepositoryDemoApplication.class);
    private static final Connection connection = RepositoryDemoApplication.connection;
    public static boolean addQuestion(String question) {
        // add question to database
        String check_sql = "SELECT * FROM security_question_map WHERE question = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(check_sql)) {
            pstmt.setString(1, question);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                logger.error("问题已存在");
                return false;
            }
        } catch (SQLException e) {
            logger.error("检查问题失败: {}", e.getMessage());
            return false;
        }
        String sql = "INSERT INTO security_question_map (question) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                logger.info("添加问题\"{}\"成功", question);
                return true;
            } else {
                logger.error("添加问题失败");
            }
        } catch (SQLException e) {
            logger.error("添加问题失败: {}", e.getMessage());
        }
        return false;
    }
    public static boolean deleteQuestion(int questionId) {
        // delete question from database
        String sql = "DELETE FROM security_question_map WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                logger.info("删除问题成功");
                return true;
            } else {
                logger.error("删除问题失败");
            }
        } catch (SQLException e) {
            logger.error("删除问题失败: {}", e.getMessage());
        }
        return false;
    }
    public static boolean updateQuestion(int questionId, String newQuestion) {
        // update question in database
        String sql = "UPDATE security_question_map SET question = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newQuestion);
            pstmt.setInt(2, questionId);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                logger.info("更新问题成功");
                return true;
            } else {
                logger.error("更新问题失败");
            }
        } catch (SQLException e) {
            logger.error("更新问题失败: {}", e.getMessage());
        }
        return false;
    }
}
