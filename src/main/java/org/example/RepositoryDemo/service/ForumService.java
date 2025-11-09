// ForumService.java
package org.example.RepositoryDemo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.Repository.forumRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

@Service
public class ForumService {
    private static final Logger logger = LogManager.getLogger(ForumService.class);
    @Autowired
    private FeedbackService feedbackService;
    
    private final forumRepository forumRepository;
    private final UserRepository userRepository;

    public ForumService(forumRepository forumRepository, UserRepository userRepository) {
        this.forumRepository = forumRepository;
        this.userRepository = userRepository;
    }

    // 创建论坛帖子
    public int createForum(int userId, String title, String content) throws SQLException {
        logger.debug("开始创建论坛帖子: userId={}, title={}", userId, title);
        int result = org.example.RepositoryDemo.Repository.forumRepository.createForum(userId, title, content);
        logger.debug("论坛帖子创建结果: userId={}, title={}, result={}", userId, title, result);
        return result;
    }

    // 获取所有论坛帖子
    public List<Forum> getAllForums() throws SQLException {
        try {
            return forumRepository.getAllForums();
        } catch (SQLException e) {
            logger.error("获取所有论坛帖子失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取所有论坛帖子失败: " + e.getMessage(),
                        "/api/forum/list",
                        "Forum Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            throw e;
        }
    }

    // 根据用户ID获取论坛帖子
    public List<Forum> getForumsByUserId(int userId) throws SQLException {
        try {
            return forumRepository.getForumsByUserId(userId);
        } catch (SQLException e) {
            logger.error("根据用户ID获取论坛帖子失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "根据用户ID获取论坛帖子失败: " + e.getMessage(),
                        "/api/forum/user",
                        "Forum Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            throw e;
        }
    }

    // 根据ID获取特定帖子
    public Forum getForumById(int id) throws SQLException {
        try {
            return forumRepository.getForumById(id);
        } catch (SQLException e) {
            logger.error("根据ID获取特定帖子失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "根据ID获取特定帖子失败: " + e.getMessage(),
                        "/api/forum/id",
                        "Forum Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            throw e;
        }
    }

    // 删除论坛帖子（管理员）
    public void deleteForum(int id) throws SQLException {
        try {
            org.example.RepositoryDemo.Repository.forumRepository.deleteForum(id);
        } catch (SQLException e) {
            logger.error("删除论坛帖子失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "删除论坛帖子失败: " + e.getMessage(),
                        "/api/forum/delete",
                        "Forum Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            throw e;
        }
    }

}