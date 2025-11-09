package org.example.RepositoryDemo;

import lombok.Setter;
import org.example.RepositoryDemo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.sql.*;
import org.apache.logging.log4j.*;


@SpringBootApplication
public class RepositoryDemoApplication {

    @Autowired
    private UserRepository userRepository;

    public static final Logger logger = LogManager.getLogger(RepositoryDemoApplication.class);
    public static Connection connection;
    private static void createTables() throws SQLException {
        UserRepository.createUserTable();
        forumRepository.createForumTable();
        SecurityQuestionRepository.createSecurityQuestionTable();
        SecurityQuestionRepository.createSecurityQuestionMapTable();
        PointRepository.createPointTable();
        PointRepository.createConfigTable();
        PointRepository.createPointProposalTable();
        PointRepository.createPointConfirmTable();
        PointRepository.createTypeMapTable();
        FeedbackRepository.createFeedbackTable();
        StatisticRepository.createTable();
    }

    static void main(String[] args) {
        String url = "jdbc:sqlite:test.db";

        try {
            connection = DriverManager.getConnection(url);
            if (connection != null) {
                logger.info("成功连接到数据库！");
            }
            createTables();
        } catch (SQLException e) {
            logger.error("连接数据库时发生错误：{}", e.getMessage());
        }
        SpringApplication.run(RepositoryDemoApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 应用启动完成后创建默认管理员用户
        userRepository.createDefaultAdminUser();
    }
}