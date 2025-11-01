package org.example.RepositoryDemo.Repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;

import java.sql.*;

public class SecurityQuestionRepository {
    private static final Logger logger = LogManager.getLogger(SecurityQuestionRepository.class);
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

    public static void createSecurityQuestionTable() throws SQLException {
        String createSecurityQuestionTableSQL = "CREATE TABLE IF NOT EXISTS security_questions (" +
                "id INTEGER PRIMARY KEY NOT NULL, " +
                "question1 INTEGER NOT NULL, " +
                "answer1 TEXT NOT NULL, " +
                "question2 INTEGER NOT NULL, " +
                "answer2 TEXT NOT NULL, " +
                "FOREIGN KEY (id) REFERENCES users(id))";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSecurityQuestionTableSQL);
            logger.info("安全提问表创建成功！");
        }
    }

    public static void createSecurityQuestionMapTable() throws SQLException {
        String createSecurityQuestionMapTableSQL = "CREATE TABLE IF NOT EXISTS security_question_map (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSecurityQuestionMapTableSQL);
            logger.info("安全提问映射表创建成功！");
        }
    }
}
