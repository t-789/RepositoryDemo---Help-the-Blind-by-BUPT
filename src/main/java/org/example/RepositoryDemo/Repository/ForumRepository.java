package org.example.RepositoryDemo.Repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;
import org.example.RepositoryDemo.entity.Comment;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class ForumRepository {
    private static final Logger logger = LogManager.getLogger(ForumRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;
    
    @Autowired
    private org.example.RepositoryDemo.service.FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;
    // TODO: 为论坛添加分类，普通的论坛帖子，标记一处位置的帖子。
    public static void createForumTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS forum (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "release_time TIMESTAMP NOT NULL, " +
                "like INTEGER NOT NULL DEFAULT 0, " +
                "favorite INTEGER NOT NULL DEFAULT 0, " +
                "comment INTEGER NOT NULL DEFAULT 0)";
        try(Statement stmt = connection.createStatement()){
            stmt.execute(sql);
            logger.info("论坛表创建成功！");
        } catch (SQLException e) {
            logger.error("创建论坛表失败！");
        }
    }

    public static void createLikeTable(){
        String sql = "CREATE TABLE IF NOT EXISTS like_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "forum_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "like_time TIMESTAMP NOT NULL, " +
                "UNIQUE(user_id, forum_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (forum_id) REFERENCES forum(id))";
        try(Statement stmt = connection.createStatement()){
            stmt.execute(sql);
            logger.info("点赞表创建成功！");
        } catch (SQLException e) {
            logger.error("创建点赞表失败！");
        }
    }

    public static void createFavoriteTable(){
        String sql = "CREATE TABLE IF NOT EXISTS favorite (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "forum_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "favorite_time TIMESTAMP NOT NULL, " +
                "UNIQUE(user_id, forum_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (forum_id) REFERENCES forum(id))";
        try(Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("收藏表创建成功！");
        } catch (SQLException e) {
            logger.error("创建收藏表失败！");
        }
    }

    public static void createCommentTable(){
        String sql = "CREATE TABLE IF NOT EXISTS comment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "forum_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "comment_time TIMESTAMP NOT NULL, " +
                "deleted BOOLEAN NOT NULL DEFAULT FALSE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (forum_id) REFERENCES forum(id))";
        try(Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("评论表创建成功！");
        } catch (SQLException e) {
            logger.error("创建评论表失败！");
        }
    }
    public static void createPictureTable(){
        String sql = "CREATE TABLE IF NOT EXISTS forum_picture (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "forum_id INTEGER NOT NULL, " +
                "picture_count INTEGER NOT NULL," +
                "picture_paths TEXT NOT NULL, " +
                "FOREIGN KEY (forum_id) REFERENCES forum(id))";
        try(Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("图片表创建成功！");
        } catch (SQLException e) {
            logger.error("创建图片表失败！");
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
                forum.like = rs.getInt("like");
                forum.favorite = rs.getInt("favorite");
                forum.comment = rs.getInt("comment");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
                // 加载评论
                forum.setComments(getCommentsByForumId(forum.id));
                forums.add(forum);
            }
        } catch (SQLException e) {
            logger.error("获取所有帖子失败: {}", e.getMessage());
            try {
                feedbackService.saveSystemFeedback(
                    null,
                    "system",
                    "获取所有帖子失败: " + e.getMessage(),
                    "/api/forum/list",
                    "Forum Service",
                    "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                );
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
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
                forum.like = rs.getInt("like");
                forum.favorite = rs.getInt("favorite");
                forum.comment = rs.getInt("comment");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
                // 加载评论
                forum.setComments(getCommentsByForumId(forum.id));
            }
        } catch (SQLException e) {
            logger.error("获取帖子{}失败: {}", id, e.getMessage());
            try {
                feedbackService.saveSystemFeedback(
                    null,
                    "system",
                    "获取帖子" + id + "失败: " + e.getMessage(),
                    "/api/forum/id",
                    "Forum Service",
                    "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                );
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
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
                forum.like = rs.getInt("like");
                forum.favorite = rs.getInt("favorite");
                forum.comment = rs.getInt("comment");
                // 修复：正确设置用户名
                User user = userRepository.findById(forum.user_id);
                if (user != null) {
                    forum.username = user.username;
                }
                // 加载评论
                forum.setComments(getCommentsByForumId(forum.id));
                forums.add(forum);
            }
        } catch (SQLException e) {
            logger.error("获取用户{}的帖子失败: {}", userId, e.getMessage());
            try {
                feedbackService.saveSystemFeedback(
                    null,
                    "system",
                    "获取用户" + userId + "的帖子失败: " + e.getMessage(),
                    "/api/forum/user",
                    "Forum Service",
                    "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                );
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
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
            // 由于这是静态方法，我们需要通过其他方式获取feedbackService
            // 这里暂时注释掉，因为静态方法中无法直接使用注入的feedbackService
        }
    }

    // 新增：点赞功能
    public static boolean likeForum(int forumId, int userId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM like_table WHERE forum_id = ? AND user_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, forumId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 已经点赞过了
                return false;
            }
        }

        String insertSql = "INSERT INTO like_table (forum_id, user_id, like_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            
            // 同时更新forum表中的like计数
            String updateForumSql = "UPDATE forum SET like = like + 1 WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateForumSql)) {
                updateStmt.setInt(1, forumId);
                updateStmt.executeUpdate();
            }
            
            logger.info("用户{}对帖子{}点赞成功！", userId, forumId);
            return true;
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}点赞失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }

    // 新增：取消点赞功能
    public static boolean unlikeForum(int forumId, int userId) throws SQLException {
        String deleteSql = "DELETE FROM like_table WHERE forum_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 同时更新forum表中的like计数
                String updateForumSql = "UPDATE forum SET like = like - 1 WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateForumSql)) {
                    updateStmt.setInt(1, forumId);
                    updateStmt.executeUpdate();
                }
                
                logger.info("用户{}对帖子{}取消点赞成功！", userId, forumId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}取消点赞失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }

    // 新增：收藏功能
    public static boolean favoriteForum(int forumId, int userId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM favorite WHERE forum_id = ? AND user_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, forumId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 已经收藏过了
                return false;
            }
        }

        String insertSql = "INSERT INTO favorite (forum_id, user_id, favorite_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            
            // 同时更新forum表中的favorite计数
            String updateForumSql = "UPDATE forum SET favorite = favorite + 1 WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateForumSql)) {
                updateStmt.setInt(1, forumId);
                updateStmt.executeUpdate();
            }
            
            logger.info("用户{}对帖子{}收藏成功！", userId, forumId);
            return true;
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}收藏失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }

    // 新增：取消收藏功能
    public static boolean unfavoriteForum(int forumId, int userId) throws SQLException {
        String deleteSql = "DELETE FROM favorite WHERE forum_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 同时更新forum表中的favorite计数
                String updateForumSql = "UPDATE forum SET favorite = favorite - 1 WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateForumSql)) {
                    updateStmt.setInt(1, forumId);
                    updateStmt.executeUpdate();
                }
                
                logger.info("用户{}对帖子{}取消收藏成功！", userId, forumId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}取消收藏失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }

    // 新增：添加评论功能
    public static int addComment(int forumId, int userId, String content) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            throw new SQLException("评论内容不能为空");
        }
        
        String insertSql = "INSERT INTO comment (forum_id, user_id, content, comment_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            
            // 获取生成的评论ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            int commentId = -1;
            if (generatedKeys.next()) {
                commentId = generatedKeys.getInt(1);
            }
            
            // 同时更新forum表中的comment计数
            String updateForumSql = "UPDATE forum SET comment = comment + 1 WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateForumSql)) {
                updateStmt.setInt(1, forumId);
                updateStmt.executeUpdate();
            }
            
            logger.info("用户{}对帖子{}添加评论成功！评论ID: {}", userId, forumId, commentId);
            return commentId;
        } catch (SQLException e) {
            logger.error("用户{}对帖子{}添加评论失败: {}", userId, forumId, e.getMessage());
            throw e;
        }
    }

    // 新增：删除评论（管理员功能，软删除）
    public static boolean deleteComment(int commentId) throws SQLException {
        String updateSql = "UPDATE comment SET deleted = TRUE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, commentId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("评论{}已被标记为删除", commentId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("删除评论{}失败: {}", commentId, e.getMessage());
            throw e;
        }
    }

    // 新增：获取用户所有点赞的帖子ID
    public static List<Integer> getUserLikedForums(int userId) throws SQLException {
        String sql = "SELECT forum_id FROM like_table WHERE user_id = ?";
        List<Integer> forumIds = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                forumIds.add(rs.getInt("forum_id"));
            }
        }
        return forumIds;
    }

    // 新增：获取用户所有收藏的帖子ID
    public static List<Integer> getUserFavoritedForums(int userId) throws SQLException {
        String sql = "SELECT forum_id FROM favorite WHERE user_id = ?";
        List<Integer> forumIds = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                forumIds.add(rs.getInt("forum_id"));
            }
        }
        return forumIds;
    }

    // 新增：获取帖子的所有未删除评论
    public List<Comment> getCommentsByForumId(int forumId) throws SQLException {
        String sql = "SELECT c.*, u.username FROM comment c JOIN users u ON c.user_id = u.id WHERE c.forum_id = ? AND c.deleted = FALSE ORDER BY c.comment_time ASC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setForumId(rs.getInt("forum_id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setContent(rs.getString("content"));
                comment.setCommentTime(rs.getTimestamp("comment_time"));
                comment.setDeleted(rs.getBoolean("deleted"));
                comment.setUsername(rs.getString("username"));
                comments.add(comment);
            }
        }
        return comments;
    }

    public static boolean addPictureToForum(int forumId, String picturePaths) throws SQLException {
        if (picturePaths == null || picturePaths.split(",").length == 0) {
            throw new SQLException("图片路径不能为空");
        }
        String sql = "INSERT INTO forum_picture (forum_id, picture_count, picture_paths) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, forumId);
            stmt.setInt(2, picturePaths.split(",").length);
            stmt.setString(3, picturePaths);
            if (stmt.executeUpdate() > 0) {
                logger.info("帖子{}成功添加{}张图片", forumId, picturePaths.split(",").length);
                return true;
            }
        } catch (SQLException e) {
            logger.error("帖子{}添加图片失败: {}", forumId, e.getMessage());
        }
        return false;
    }

    public static boolean removePictureFromForum(int forumId) throws SQLException {
        String sql = "DELETE FROM forum_picture WHERE forum_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, forumId);
            if (stmt.executeUpdate() > 0) {
                logger.info("帖子{}成功删除图片", forumId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("帖子{}删除图片失败: {}", forumId, e.getMessage());
        }
        return false;
    }
    public static boolean updatePicturePathsInForum(int forumId, String picturePaths) throws SQLException {
        String sql = "UPDATE forum_picture SET picture_paths = ? WHERE forum_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, picturePaths);
            stmt.setInt(2, forumId);
            if (stmt.executeUpdate() > 0) {
                logger.info("帖子{}成功更新图片", forumId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("帖子{}更新图片失败: {}", forumId, e.getMessage());
        }
        return false;
    }
    public static List<String> getPicturePathsFromForum(int forumId) throws SQLException {
        String sql = "SELECT picture_paths FROM forum_picture WHERE forum_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, forumId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String paths = rs.getString("picture_paths");
                logger.debug("getPicturePathsFromForum(): 帖子{}有{}张图片", forumId, paths.split(",").length);
                return Arrays.asList(paths.split(","));
            }
        } catch (SQLException e) {
            logger.error("getPicturePathsFromForum(): 获取帖子{}图片路径失败: {}", forumId, e.getMessage());
        }
        return null;
    }
    public static List<Integer> getForumsBySubstring(String substring) throws SQLException {
        String sql = "SELECT id FROM forum WHERE content LIKE ?";
        List<Integer> forumIds = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + substring + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                forumIds.add(rs.getInt("id"));
            }
            logger.info("getForumsBySubstring(): 找到{}个帖子", forumIds.size());
            if (forumIds.isEmpty()){
                return null;
            } else {
                return forumIds;
            }
        }
    }

}