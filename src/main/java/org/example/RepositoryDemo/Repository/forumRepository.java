package org.example.RepositoryDemo.Repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class forumRepository {
    private static final Logger logger = LogManager.getLogger(forumRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;
    
    @Autowired
    private UserRepository userRepository;

    public static void createForumTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS forum (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "release_time TIMESTAMP NOT NULL)";
        try(Statement stmt = connection.createStatement()){
            stmt.execute(sql);
            logger.info("论坛表创建成功！");
        } catch (SQLException e) {
            logger.error("创建论坛表失败！");
            throw e;
        }
    }

    public static int createForum(int userId, String title, String content) throws SQLException {
        String sql = "INSERT INTO forum (user_id, title, content, release_time) VALUES (?, ?, ?, ?)";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            logger.info("{}创建的帖子{}创建成功！", userId, title);
            StatisticRepository.updatePostCount(1);
            return 1;
        } catch (SQLException e) {
            logger.error("创建帖子{}失败: {}", title, e.getMessage());
            return 0;
        }
    }

    public List<Forum> getAllForums() throws SQLException {
        String sql = "SELECT * FROM forum";
        List<Forum> forums = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Forum forum = new Forum();
                forum.id = rs.getInt("id");
                forum.user_id = rs.getInt("user_id");
                forum.title = rs.getString("title");
                forum.content = rs.getString("content");
                forum.release_time = rs.getTimestamp("release_time");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
                forums.add(forum);
            }
        } catch (SQLException e) {
            logger.error("获取所有帖子失败: {}", e.getMessage());
        }
        return forums;
    }

    public Forum getForumById(int id) throws SQLException {
        String sql = "SELECT * FROM forum WHERE id = ?";
        Forum forum = new Forum();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                forum.id = rs.getInt("id");
                forum.user_id = rs.getInt("user_id");
                forum.title = rs.getString("title");
                forum.content = rs.getString("content");
                forum.release_time = rs.getTimestamp("release_time");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
            }
        } catch (SQLException e) {
            logger.error("获取帖子{}失败: {}", id, e.getMessage());
        }
        return forum;
    }

    public List<Forum> getForumsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM forum WHERE user_id = ?";
        List<Forum> forums = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Forum forum = new Forum();
                forum.id = rs.getInt("id");
                forum.user_id = rs.getInt("user_id");
                forum.title = rs.getString("title");
                forum.content = rs.getString("content");
                forum.release_time = rs.getTimestamp("release_time");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
                forums.add(forum);
            }
        } catch (SQLException e) {
            logger.error("获取用户{}的帖子失败: {}", userId, e.getMessage());
        }
        return forums;
    }

    public static void deleteForum(int id) throws SQLException { // need admin role
        String sql = "DELETE FROM forum WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            StatisticRepository.updatePostCount(-1);
            logger.info("删除帖子{}成功！", id);
        } catch (SQLException e) {
            logger.error("删除帖子{}失败: {}", id, e.getMessage());
        }
    }
}