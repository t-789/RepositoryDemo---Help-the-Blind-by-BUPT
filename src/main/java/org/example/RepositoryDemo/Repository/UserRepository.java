package org.example.RepositoryDemo.Repository;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.RepositoryDemoApplication;
import org.example.RepositoryDemo.entity.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Setter
public class UserRepository {
    private JdbcTemplate jdbcTemplate;

    private PasswordEncoder passwordEncoder;
    
    private static final Logger logger = LogManager.getLogger(UserRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;

    public static void createUserTable() throws SQLException {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "type INTEGER NOT NULL DEFAULT 0, " +
                "credit INTEGER NOT NULL DEFAULT 0," +
                "isBanned BOOLEAN NOT NULL DEFAULT 0, " +
                "banEndTime TIMESTAMP NULL, " +
                "avatar TEXT NULL)"; // 添加avatar字段
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUserTableSQL);
            logger.info("用户表创建成功！");
        } catch (Exception e) {
            logger.error("创建用户表时发生错误: {}", e.getMessage());
        }
    }


    /**
     * 检查并更新用户表结构
     * 自动添加缺失的列以保证数据库结构完整性
     */
    public static void checkAndUpdateUserTable() {
        try {
            // 检查并添加 credit 列
            addCreditColumnIfNotExists();
            // 检查并添加 avatar 列
            addAvatarColumnIfNotExists();
            logger.info("用户表结构检查完成");
        } catch (SQLException e) {
            logger.error("检查用户表结构时发生错误: {}", e.getMessage());
        }
    }

    /**
     * 检查并添加 credit 列
     */
    private static void addCreditColumnIfNotExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN credit INTEGER NOT NULL DEFAULT 0");
            logger.info("成功添加 credit 列");
        } catch (SQLException e) {
            // 如果列已存在，则会抛出异常，我们检查是否是这个原因
            if (!e.getMessage().contains("duplicate column name") &&
                !e.getMessage().contains("column credit already exists")) {
                // 如果是其他错误，则重新抛出
                throw e;
            }
            // 列已存在，这是正常的，不需要处理
            logger.debug("credit 列已存在，跳过添加");
        }
    }

    /**
     * 检查并添加 avatar 列
     */
    private static void addAvatarColumnIfNotExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN avatar TEXT NULL");
            logger.info("成功添加 avatar 列");
        } catch (SQLException e) {
            // 如果列已存在，则会抛出异常，我们检查是否是这个原因
            if (!e.getMessage().contains("duplicate column name") &&
                !e.getMessage().contains("column avatar already exists")) {
                // 如果是其他错误，则重新抛出
                throw e;
            }
            // 列已存在，这是正常的，不需要处理
            logger.debug("avatar 列已存在，跳过添加");
        }
    }

    // 检查并创建默认管理员账户
    public void createDefaultAdminUser() {
        String sql = "SELECT COUNT(*) AS adminCount FROM users WHERE type = 2";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getInt("adminCount") == 0) {
                logger.info("管理员不存在！正在创建管理员用户（用户名与密码均为admin）...");
                User adminUser = new User();
                adminUser.username = "admin";
                adminUser.password_hash = passwordEncoder.encode("admin");
                adminUser.type = 2;
                registerUser(adminUser);
            }
        } catch (SQLException e) {
            logger.error("检查或创建管理员用户失败: {}", e.getMessage());
        }
    }

    // 用户注册
    public static int registerUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, type, avatar) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.username);
            pstmt.setString(2, user.password_hash);
            pstmt.setInt(3, user.type != null ? user.type : 0); // 0、1-普通用户，2-管理员
            pstmt.setString(4, user.avatar); // 插入头像字段，可以为null
            pstmt.executeUpdate();
            logger.info("用户{}注册成功！", user.username);
            StatisticRepository.updateUserCount(1);
            return 1; // 注册成功
        } catch (SQLException e) {
            logger.error("用户{}注册失败: {}", user.username, e.getMessage());
            return 0; // 注册失败
        }
    }

    // 添加根据用户名查找用户的方法
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.password_hash = rs.getString("password_hash");
                user.type = rs.getInt("type");
                user.credit = rs.getInt("credit");
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
                user.avatar = rs.getString("avatar"); // 读取头像字段
                return user;
            }, username);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Incorrect result size: expected 1, actual 0")){
                logger.info("用户{}不存在",  username);
                return null;
            }
            logger.error("findByUsername()：查询用户失败: {}", e.getMessage());
            return null; // 用户不存在
        }
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.password_hash = rs.getString("password_hash");
                user.type = rs.getInt("type");
                user.credit = rs.getInt("credit");
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
                user.avatar = rs.getString("avatar"); // 读取头像字段
                return user;
            }, id);
        } catch (DataAccessException e) {
            logger.error("findById()：查询用户失败: {}", e.getMessage());
            return null; // 用户不存在
        }
    }
    
    // 更新用户权限
// 修复 updateUserType 方法
    public int updateUserType(int userId, int t) {
        String sql = "UPDATE users SET type = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, t);
            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("用户{}权限更新成功！", userId);
                return 1;
            } else {
                logger.warn("用户{}不存在！", userId);
                return -1;
            }
        } catch (SQLException e) {
            logger.error("更新用户{}权限失败: {}", userId, e.getMessage());
            return -2;
        }
    }

    // 封禁用户（用于测试模拟）
    public boolean banUser(int userId, String banTime) {
        try {
            handleBanUser(String.valueOf(userId), banTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 解封用户（用于测试模拟）
    public boolean unbanUser(int userId) {
        try {
            handleUnbanUser(String.valueOf(userId));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 更新用户头像
    public boolean updateUserAvatar(int userId, String avatarPath) {
        String sql = "UPDATE users SET avatar = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, avatarPath);
            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("更新用户{}头像失败: {}", userId, e.getMessage());
            return false;
        }
    }

    public static List<User> ListUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.password_hash = rs.getString("password_hash");
                user.type = rs.getInt("type");
                user.credit = rs.getInt("credit");
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
                user.avatar = rs.getString("avatar"); // 读取头像字段
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("获取用户列表失败: {}", e.getMessage());
        }
        return users;
    }

    private static long parseBanTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            logger.warn("时间字符串无效");
            return -1;
        }

        long totalMillis = 0;
        int i = 0;

        while (i < timeStr.length()) {
            // 提取数字部分
            int numStart = i;
            while (i < timeStr.length() && Character.isDigit(timeStr.charAt(i))) {
                i++;
            }

            if (i == numStart) {
                logger.warn("时间字符串无效，没有数字");
                return -1; // 没有数字
            }

            int number;
            try {
                number = Integer.parseInt(timeStr.substring(numStart, i));
            } catch (NumberFormatException e) {
                logger.error("时间字符串无效\n报错信息：", e);
                return -1;
            }

            if (i >= timeStr.length()) {
                logger.warn("时间字符串无效，没有单位");
                return -1; // 没有单位
            }

            // 提取单位
            char unit = timeStr.charAt(i);
            i++;

            // 转换为毫秒
            switch (unit) {
                case 'y': // 年
                    totalMillis += (long) number * 365 * 24 * 60 * 60 * 1000;
                    break;
                case 'm': // 月
                    totalMillis += (long) number * 30 * 24 * 60 * 60 * 1000;
                    break;
                case 'd': // 天
                    totalMillis += (long) number * 24 * 60 * 60 * 1000;
                    break;
                case 'h': // 小时
                    totalMillis += (long) number * 60 * 60 * 1000;
                    break;
                default:
                    logger.warn("时间字符串无效，无效单位");
                    return -1; // 无效单位
            }
        }
        return totalMillis;
    }
    
    public static void handleBanUser(String id, String banTime) {
        logger.info("正在封禁用户{}", id);
        try {
            int userId = Integer.parseInt(id);
            // 检查是否要永久封禁
            if (banTime.equals("0")) {
                // 永久封禁
                String sql = "UPDATE users SET isBanned = 1, banEndTime = NULL WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.info("handleBanUser(): 用户{}已被永久封禁", id);
                    } else {
                        logger.warn("handleBanUser(): 永久封禁：用户{}不存在", userId);
                    }
                }
            } else {
                // 解析时间格式 y年m月d天h小时 (例如: 1y2m3d4h 表示1年2月3天4小时)
                long banDurationMillis = parseBanTime(banTime);
                if (banDurationMillis == -1) {
                    logger.warn("封禁时间格式错误。正确格式如：1y2m3d4h 或 0（永久封禁）");
                    return;
                }

                // 计算解封时间
                Timestamp unbanTime = new Timestamp(System.currentTimeMillis() + banDurationMillis);

                String sql = "UPDATE users SET isBanned = 1, banEndTime = ? WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setTimestamp(1, unbanTime);
                    pstmt.setInt(2, userId);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.info("handleBanUser(): 用户{}已封禁至：{}", id, unbanTime);
                    } else {
                        logger.warn("handleBanUser(): 临时封禁：用户{}不存在", userId);
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("用户ID必须是数字。");
        } catch (SQLException e) {
            logger.error("封禁用户{}失败: {}", id, e.getMessage());
        }
    }

    public static void handleUnbanUser(String id) {
        logger.info("正在解禁用户{}", id);
        try {
            Timestamp unbanTime = new Timestamp(System.currentTimeMillis());
            String sql = "UPDATE users SET isBanned = 0, banEndTime = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, unbanTime);
                pstmt.setInt(2, Integer.parseInt(id));
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    logger.info("用户{}已被解禁", id);
                } else {
                    logger.warn("handleUnbanUser(): 用户{}不存在", id);
                }
            }
        } catch (SQLException e) {
            logger.error("解禁用户{}失败: {}", id, e.getMessage());
        }
    }

    public static void deleteUser(String id) {
        logger.info("正在删除用户{}", id);
        try {
            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    logger.info("deleteUser(): 用户{}已被删除", id);
                    StatisticRepository.updateUserCount(-1);
                } else {
                    logger.warn("deleteUser(): 用户{}不存在", id);
                }
            }
        } catch (SQLException e) {
            logger.error("deleteUser(): 删除用户{}失败: {}", id, e.getMessage());
        }
    }
    
    // 更新用户密码
    public boolean updateUserPassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("更新用户{}密码失败: {}", userId, e.getMessage());
            return false;
        }
    }

    public String findQuestionById(int id) {
        String sql = "SELECT * FROM security_question_map WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("question");
            } else {
                return null; // 问题不存在
            }
        } catch (java.sql.SQLException e) {
            logger.error("findQuestionById()：查询用户失败: {}", e.getMessage());
            return null; // 用户不存在
        }
    }

    // 定义密保问题集类
    public static class QuestionSet {
        public String question1;
        public String question2;
        public String answer1;
        public String answer2;

        public QuestionSet(String question1, String question2, String answer1, String answer2) {
            this.question1 = question1;
            this.question2 = question2;
            this.answer1 = answer1;
            this.answer2 = answer2;
        }
    }

    public QuestionSet getQuestionsById(int id) {
        String user_sql = "SELECT * FROM security_questions WHERE id = ?";
        try {
            PreparedStatement user_pstmt = connection.prepareStatement(user_sql);
            user_pstmt.setInt(1, id);
            ResultSet user_rs = user_pstmt.executeQuery();
            if (user_rs.next()) {
                int question1 = user_rs.getInt("question1"),
                        question2 = user_rs.getInt("question2");
                return new QuestionSet(findQuestionById(question1), findQuestionById(question2),
                        user_rs.getString("answer1"), user_rs.getString("answer2"));
            }
        } catch (SQLException e) {
            logger.error("getQuestionsById()：查询用户密保问题失败: {}", e.getMessage());
            return null;
        }
        return null;
    }

    public static void setQuestionForUser(int userId, int questionId1, String ans1, int questionId2, String ans2) {
        String check_if_exist_sql = "SELECT * FROM security_questions WHERE id = ?";
        try (PreparedStatement check_if_exist_pstmt = connection.prepareStatement(check_if_exist_sql)) {
            check_if_exist_pstmt.setInt(1, userId);
            ResultSet check_if_exist_rs = check_if_exist_pstmt.executeQuery();
            if (check_if_exist_rs.next()) {
                String update_sql = "UPDATE security_questions SET question1 = ?, answer1 = ?, question2 = ?, answer2 = ? WHERE id = ?";
                try (PreparedStatement update_pstmt = connection.prepareStatement(update_sql)) {
                    update_pstmt.setInt(1, questionId1);
                    update_pstmt.setString(2, ans1);
                    update_pstmt.setInt(3, questionId2);
                    update_pstmt.setString(4, ans2);
                    update_pstmt.setInt(5, userId);
                    int rowsUpdated = update_pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.info("用户{}的密保问题已更新", userId);
                    } else {
                        logger.warn("用户{}的密保问题更新失败", userId);
                    }
                } catch (SQLException e) {
                    logger.error("setQuestionForUser()：更新用户密保问题失败: {}", e.getMessage());
                }
            } else {
                String insert_sql = "INSERT INTO security_questions (id, question1, answer1, question2, answer2) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insert_pstmt = connection.prepareStatement(insert_sql)) {
                    insert_pstmt.setInt(1, userId);
                    insert_pstmt.setInt(2, questionId1);
                    insert_pstmt.setString(3, ans1);
                    insert_pstmt.setInt(4, questionId2);
                    insert_pstmt.setString(5, ans2);
                    int rowsInserted = insert_pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        logger.info("用户{}的密保问题已保存", userId);
                    } else {
                        logger.warn("用户{}的密保问题保存失败", userId);
                    }
                } catch (SQLException e) {
                    logger.error("setQuestionForUser()：插入用户密保问题失败: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error("setQuestionForUser()：查询用户密保问题失败: {}", e.getMessage());
        }
    }

    // 验证用户密保问题答案
    public boolean verifySecurityAnswers(int userId, String ans1, String ans2) {
        String sql = "SELECT answer1, answer2 FROM security_questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedAns1 = rs.getString("answer1");
                String storedAns2 = rs.getString("answer2");
                
                // 使用密码编码器验证答案（答案在存储时应被编码）
                return passwordEncoder.matches(ans1, storedAns1) && 
                       passwordEncoder.matches(ans2, storedAns2);
            }
        } catch (SQLException e) {
            logger.error("验证用户{}密保问题答案失败: {}", userId, e.getMessage());
        }
        return false;
    }

    // 获取所有安全问题列表
    public Map<Integer, String> getAllSecurityQuestions() {
        Map<Integer, String> questions = new LinkedHashMap<>();
        String sql = "SELECT id, question FROM security_question_map";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String question = rs.getString("question");
                questions.put(id, question);
            }
        } catch (SQLException e) {
            logger.error("获取安全问题列表失败: {}", e.getMessage());
        }
        return questions;
    }
    
    // 添加安全问题
    public boolean addSecurityQuestion(String question) {
        return SecurityQuestionRepository.addQuestion(question);
    }
    
    // 删除安全问题
    public boolean deleteSecurityQuestion(int questionId) {
        return SecurityQuestionRepository.deleteQuestion(questionId);
    }
    
    // 更新安全问题
    public boolean updateSecurityQuestion(int questionId, String newQuestion) {
        return SecurityQuestionRepository.updateQuestion(questionId, newQuestion);
    }
}