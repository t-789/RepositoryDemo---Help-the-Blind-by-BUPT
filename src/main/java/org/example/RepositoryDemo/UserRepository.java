package org.example.RepositoryDemo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final Logger logger = LogManager.getLogger(RepositoryDemoApplication.class);
    private static final Connection connection = RepositoryDemoApplication.connection;

    public static void createUserTable() throws SQLException {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "type INTEGER NOT NULL DEFAULT 0, " +
                "isBanned BOOLEAN NOT NULL DEFAULT 0, " +
                "banEndTime TIMESTAMP NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUserTableSQL);
            logger.info("用户表创建成功！");
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
        String sql = "INSERT INTO users (username, password_hash, type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.username);
            pstmt.setString(2, user.password_hash);
            pstmt.setInt(3, user.type != null ? user.type : 0); // 0、1-普通用户，2-管理员
            pstmt.executeUpdate();
            logger.info("用户{}注册成功！", user.username);
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
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.password_hash = rs.getString("password_hash");
                user.type = rs.getInt("type");
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
                return user;
            });
        } catch (DataAccessException e) {
            logger.error("findByUsername()：查询用户失败: {}", e.getMessage());
            return null; // 用户不存在
        }
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                user.password_hash = rs.getString("password_hash");
                user.type = rs.getInt("type");
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
                return user;
            });
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
                user.isBanned = rs.getBoolean("isBanned");
                user.banEndTime = rs.getTimestamp("banEndTime");
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
                        logger.info("用户{}已被永久封禁", id);
                    } else {
                        logger.warn("用户{}不存在", userId);
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
                        logger.info("用户{}已封禁至：{}", id, unbanTime);
                    } else {
                        logger.warn("用户{}不存在", userId);
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
                    logger.warn("用户{}不存在", id);
                }
            }
        } catch (SQLException e) {
            logger.error("解禁用户{}失败: {}", id, e.getMessage());
        }
    }
}