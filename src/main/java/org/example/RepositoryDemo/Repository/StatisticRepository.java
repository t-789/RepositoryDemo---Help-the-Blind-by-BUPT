package org.example.RepositoryDemo.Repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;
import org.example.RepositoryDemo.entity.Statistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Repository
public class StatisticRepository {

    private static final Logger logger = LogManager.getLogger(StatisticRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;

    @Autowired
    private static org.example.RepositoryDemo.service.FeedbackService feedbackService;

    public static void createTable() {
        String createSql = "CREATE TABLE IF NOT EXISTS Statistic (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_count INTEGER NOT NULL DEFAULT 0, " +
                "point_count INTEGER NOT NULL DEFAULT 0, " +
                "post_count INTEGER NOT NULL DEFAULT 0)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSql);
            createStatistics();
            logger.info("统计表创建成功！");

        } catch (Exception e) {
            logger.error("创建统计表时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "创建统计表时发生错误: " + e.getMessage(),
                        "/initialization",
                        "Database Initialization",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
        }
    }

    public static void createStatistics() {
        String checkSql = "SELECT * FROM Statistic WHERE id=1";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkSql);
            if (!rs.next()) {
                String insertSql = "INSERT INTO Statistic (user_count, point_count, post_count) VALUES (0, 0, 0)";
                stmt.execute(insertSql);
            }
        } catch (Exception e) {
            logger.error("检查统计表时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "检查统计表时发生错误: " + e.getMessage(),
                        "/initialization",
                        "Database Initialization",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
        }
    }
    public static boolean updateUserCount(int change) {
        String updateSql = "UPDATE Statistic SET user_count = user_count + " + change + " WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(updateSql);
            logger.info("用户数更新成功！");
            return true;
        } catch (Exception e) {
            logger.error("更新用户数时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "更新用户数时发生错误: " + e.getMessage(),
                        "/api/statistics/update-user-count",
                        "Statistics Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }
    public static boolean updatePointCount(int change) {
        String updateSql = "UPDATE Statistic SET point_count = point_count + " + change + " WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(updateSql);
            logger.info("点位数更新成功！");
            return true;
        } catch (Exception e) {
            logger.error("更新点位数时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "更新点位数时发生错误: " + e.getMessage(),
                        "/api/statistics/update-point-count",
                        "Statistics Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }
    public static boolean updatePostCount(int change) {
        String updateSql = "UPDATE Statistic SET post_count = post_count + " + change + " WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(updateSql);
            logger.info("帖子数更新成功！");
            return true;
        } catch (Exception e) {
            logger.error("更新帖子数时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "更新帖子数时发生错误: " + e.getMessage(),
                        "/api/statistics/update-post-count",
                        "Statistics Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }

    public static Statistic getStatistic() {
        String selectSql = "SELECT * FROM Statistic WHERE id = 1";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectSql);
            if (rs.next()) {
                int userCount = rs.getInt("user_count");
                int pointCount = rs.getInt("point_count");
                int postCount = rs.getInt("post_count");
                return new Statistic(pointCount, userCount, postCount);
            }
        } catch (Exception e) {
            logger.error("获取统计信息时发生错误: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取统计信息时发生错误: " + e.getMessage(),
                        "/api/statistics",
                        "Statistics Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
        }
        return null;
    }
}