// ForumService.java
package org.example.RepositoryDemo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.Comment;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.Repository.ForumRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class ForumService {
    private static final Logger logger = LogManager.getLogger(ForumService.class);
    @Autowired
    private FeedbackService feedbackService;
    
    private final ForumRepository forumRepository;
    private final UserRepository userRepository;

    public ForumService(ForumRepository forumRepository, UserRepository userRepository) {
        this.forumRepository = forumRepository;
        this.userRepository = userRepository;
    }

    // 创建论坛帖子
    public int createForum(int userId, String title, String content) throws SQLException {
        logger.debug("开始创建论坛帖子: userId={}, title={}", userId, title);
        int result = ForumRepository.createForum(userId, title, content);
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
            ForumRepository.deleteForum(id);
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
    
    // 点赞帖子
    public boolean likeForum(int forumId, int userId) throws SQLException {
        try {
            return ForumRepository.likeForum(forumId, userId);
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}点赞失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }
    
    // 取消点赞帖子
    public boolean unlikeForum(int forumId, int userId) throws SQLException {
        try {
            return ForumRepository.unlikeForum(forumId, userId);
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}取消点赞失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }
    
    // 收藏帖子
    public boolean favoriteForum(int forumId, int userId) throws SQLException {
        try {
            return ForumRepository.favoriteForum(forumId, userId);
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}收藏失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }
    
    // 取消收藏帖子
    public boolean unfavoriteForum(int forumId, int userId) throws SQLException {
        try {
            return ForumRepository.unfavoriteForum(forumId, userId);
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}取消收藏失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }
    
    // 添加评论
    public int addComment(int forumId, int userId, String content) throws SQLException {
        try {
            return ForumRepository.addComment(forumId, userId, content);
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}添加评论失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }
    
    // 删除评论（管理员）
    public boolean deleteComment(int commentId) throws SQLException {
        try {
            return ForumRepository.deleteComment(commentId);
        } catch (SQLException e) {
            logger.error("删除评论{}失败: {}", commentId, e.getMessage());
            throw e;
        }
    }
    
    // 获取用户所有点赞的帖子ID
    public List<Integer> getUserLikedForums(int userId) throws SQLException {
        try {
            return ForumRepository.getUserLikedForums(userId);
        } catch (SQLException e) {
            logger.error("获取用户{}点赞的帖子失败: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    // 获取用户所有收藏的帖子ID
    public List<Integer> getUserFavoritedForums(int userId) throws SQLException {
        try {
            return ForumRepository.getUserFavoritedForums(userId);
        } catch (SQLException e) {
            logger.error("获取用户{}收藏的帖子失败: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    // 获取帖子的所有未删除评论
    public List<Comment> getCommentsByForumId(int forumId) throws SQLException {
        try {
            return forumRepository.getCommentsByForumId(forumId);
        } catch (SQLException e) {
            logger.error("获取帖子{}的评论失败: {}", forumId, e.getMessage());
            throw e;
        }
    }
    
    // 获取带用户点赞/收藏状态的所有帖子
    public List<Forum> getAllForumsWithUserStatus(int userId) throws SQLException {
        List<Forum> forums = getAllForums();
        Set<Integer> likedForums = new HashSet<>(getUserLikedForums(userId));
        Set<Integer> favoritedForums = new HashSet<>(getUserFavoritedForums(userId));
        
        for (Forum forum : forums) {
            forum.setUserLiked(likedForums.contains(forum.getId()) ? 1 : 0);
            forum.setUserFavorited(favoritedForums.contains(forum.getId()) ? 1 : 0);
        }
        
        return forums;
    }
}