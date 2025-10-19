// ForumService.java
package org.example.RepositoryDemo;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ForumService {
    private final forumRepository forumRepository;
    private final UserRepository userRepository;

    public ForumService(forumRepository forumRepository, UserRepository userRepository) {
        this.forumRepository = forumRepository;
        this.userRepository = userRepository;
    }

    // 创建论坛帖子
    public int createForum(int userId, String title, String content) throws SQLException {
        return forumRepository.createForum(userId, title, content);
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
        forumRepository.deleteForum(id);
    }
}