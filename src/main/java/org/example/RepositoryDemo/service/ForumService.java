// ForumService.java
package org.example.RepositoryDemo.service;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.Repository.forumRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ForumService {
    private static final Logger logger = LogManager.getLogger(ForumService.class);
    
    private final forumRepository forumRepository;
    @Getter
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
        return forumRepository.getAllForums();
    }

    // 根据用户ID获取论坛帖子
    public List<Forum> getForumsByUserId(int userId) throws SQLException {
        return forumRepository.getForumsByUserId(userId);
    }

    // 根据ID获取特定帖子
    public Forum getForumById(int id) throws SQLException {
        return forumRepository.getForumById(id);
    }

    // 删除论坛帖子（管理员）
    public void deleteForum(int id) throws SQLException {
        org.example.RepositoryDemo.Repository.forumRepository.deleteForum(id);
    }

}