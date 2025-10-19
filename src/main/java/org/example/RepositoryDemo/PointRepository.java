// PointRepository.java
package org.example.RepositoryDemo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PointRepository {
    private static final Logger logger = LogManager.getLogger(PointRepository.class);
    private static final Connection connection = RepositoryDemoApplication.connection;
    
    // 初始化表
    public static void createPointTable() throws SQLException {
        String createPointTableSQL = "CREATE TABLE IF NOT EXISTS points (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "x REAL NOT NULL, " +
                "y REAL NOT NULL, " +
                "marked_time TIMESTAMP NOT NULL, " +
                "deleted BOOLEAN NOT NULL DEFAULT 0, " +
                "deleted_time TIMESTAMP NULL, " +
                "propose_delete INTEGER NOT NULL DEFAULT 0, " +
                "type INTEGER NOT NULL DEFAULT 0, " +
                "description TEXT)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPointTableSQL);
//            alterPointTable();
            logger.info("点位表初始化完成。");
        }
    }

    public static void alterPointTable() throws SQLException {
        // 检查并添加 type 列
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE points ADD COLUMN type INTEGER NOT NULL DEFAULT 0");
            logger.info("成功添加 type 列");
        } catch (SQLException e) {
            // 列可能已经存在，忽略错误
            if (!e.getMessage().contains("duplicate column name")) {
                logger.warn("添加 type 列时出错: {}", e.getMessage());
            }
        }

        // 检查并添加 description 列
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE points ADD COLUMN description TEXT");
            logger.info("成功添加 description 列");
        } catch (SQLException e) {
            // 列可能已经存在，忽略错误
            if (!e.getMessage().contains("duplicate column name")) {
                logger.warn("添加 description 列时出错: {}", e.getMessage());
            }
        }
    }

    
    // 添加配置表用于存储阈值
    public static void createConfigTable() throws SQLException {
        String createConfigTableSQL = "CREATE TABLE IF NOT EXISTS config (" +
                "key TEXT PRIMARY KEY, " +
                "value TEXT)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createConfigTableSQL);
            // 插入默认阈值
            String insertDefaultThreshold = "INSERT OR IGNORE INTO config (key, value) VALUES ('delete_threshold', '5')";
            stmt.execute(insertDefaultThreshold);
            logger.info("配置表初始化完成。");
        }
    }

    public static void createPointProposalTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS point_proposals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "point_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "proposal_time TIMESTAMP NOT NULL, " +
                "UNIQUE(point_id, user_id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("点位提议表初始化完成。");
        }
    }
    // 保存点位
    public int savePoint(Point point) {
        String existsSQL = "SELECT * FROM points WHERE x=? AND y=?";
        try (PreparedStatement stmt = connection.prepareStatement(existsSQL)) {
            stmt.setDouble(1, point.x);
            stmt.setDouble(2, point.y);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                logger.warn("点位({}, {})已存在", point.x, point.y);
                return -1;
            }
        } catch (SQLException e) {
            logger.error("点位查重失败: {}", e.getMessage());
        }

        String sql = "INSERT INTO points (user_id, x, y, marked_time, deleted, propose_delete, type, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, point.userId);
            pstmt.setDouble(2, point.x);
            pstmt.setDouble(3, point.y);
            pstmt.setTimestamp(4, point.markedTime);
            pstmt.setBoolean(5, point.deleted != null ? point.deleted : false);
            pstmt.setInt(6, point.proposeDelete != null ? point.proposeDelete : 0);
            pstmt.setInt(7, point.type);
            pstmt.setString(8, point.description != null ? point.description : "没有描述");
            int result = pstmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    point.id = rs.getInt(1);
                }
                String add_credit_sql = "UPDATE users SET credit = credit + 1 WHERE id = ?";
                try (PreparedStatement add_credit_pstmt = connection.prepareStatement(add_credit_sql)) {
                    add_credit_pstmt.setInt(1, point.userId);
                    int creditResult = add_credit_pstmt.executeUpdate();
                    if (creditResult > 0) {
                        logger.info("用户{}积分增加成功", point.userId);
                    } else {
                        logger.warn("未找到用户{}，积分未增加", point.userId);
                    }
                } catch (SQLException e) {
                    logger.error("更新用户{}积分失败: {}", point.userId, e.getMessage());
                }
                logger.info("点位保存成功，ID: {}", point.id);
                return point.id;
            }
        } catch (SQLException e) {
            logger.error("保存点位失败: {}", e.getMessage());
        }
        return -1;
    }
    
    // 获取所有未删除的点位
    public List<Point> getAllActivePoints() {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT * FROM points WHERE deleted = 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                points.add(getPoint(rs));
            }
        } catch (SQLException e) {
            logger.error("获取点位列表失败: {}", e.getMessage());
        }
        return points;
    }

    // 根据ID获取点位
    public Point getPointById(int id) {
        String sql = "SELECT * FROM points WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getPoint(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("获取点位失败: {}", e.getMessage());
        }
        return null;
    }

    private static Point getPoint(ResultSet rs) throws SQLException {
        Point point = new Point();
        point.id = rs.getInt("id");
        point.userId = rs.getInt("user_id");
        point.x = rs.getDouble("x");
        point.y = rs.getDouble("y");
        point.markedTime = rs.getTimestamp("marked_time");
        point.deleted = rs.getBoolean("deleted");
        point.deletedTime = rs.getTimestamp("deleted_time");
        point.proposeDelete = rs.getInt("propose_delete");
        point.type = rs.getInt("type");
        point.description = rs.getString("description");
        return point;
    }

    // 用户提议删除点位
    public int proposeDeletePoint(int pointId, int userId) {
//        logger.info("用户{}尝试删除{}", userId, pointId);
        // 检查用户是否已经提议过删除这个点位
        String checkSql = "SELECT COUNT(*) FROM point_proposals WHERE point_id = ? AND user_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, pointId);
            checkStmt.setInt(2, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.info("用户{}已对点位{}提议过删除", userId, pointId);
                    return -2; // 用户已提议过，不允许重复提议
                }
            } catch (SQLException e) {
                logger.error("检查点位提议记录失败: {}", e.getMessage());
                return -3;
            }

        } catch (SQLException e) {
            logger.error("检查提议删除记录失败: {}", e.getMessage());
            return -3;
        }
        String checkDeletedSql = "SELECT * FROM points WHERE id = ?";
        try (PreparedStatement checkDeletedStmt = connection.prepareStatement(checkDeletedSql)) {
            checkDeletedStmt.setInt(1, pointId);
            try (ResultSet rs = checkDeletedStmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getBoolean("deleted")) {
                        logger.info("点位{}已删除，无法再次删除。", pointId);
                        return -4; // 点位已删除，无法再次删除
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("检查点位删除状态失败: {}", e.getMessage());
            return -3;
        }

        // 更新点位的提议删除计数
        String updateSql = "UPDATE points SET propose_delete = propose_delete + 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
            pstmt.setInt(1, pointId);
//            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                // 检查是否超过阈值
                boolean success = checkAndMarkDeleted(pointId);
                if (!success) {
                    logger.error("无法判断点位{}是否超过阈值。", pointId);
                }
                logger.info("用户{}提议删除点位{}", userId, pointId);
                // 记录提议删除
                String insertSql = "INSERT INTO point_proposals (point_id, user_id, proposal_time) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, pointId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    insertStmt.executeUpdate();
                    logger.info("记录用户{}对点位{}的提议删除成功", userId, pointId);
                } catch (SQLException e) {
                    logger.error("记录提议删除失败: {}", e.getMessage());
                    return -1;
                }
                return 1;
            } else {
                logger.warn("点位{}不存在", pointId);
                return -1;

            }
        } catch (SQLException e) {
            logger.error("提议删除点位失败: {}", e.getMessage());
            return -3;
        }
    }
    
    // 管理员删除点位
    public boolean adminDeletePoint(int pointId) {
        String sql = "UPDATE points SET deleted = 1, deleted_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, pointId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("管理员删除点位{}", pointId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("管理员删除点位失败: {}", e.getMessage());
        }
        return false;
    }
    
    // 管理员恢复点位
    public boolean adminRestorePoint(int pointId) {
        String sql = "UPDATE points SET deleted = 0, deleted_time = NULL, propose_delete = 0 WHERE id = ?";
        String sql2 = "DELETE FROM point_proposals WHERE point_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
        PreparedStatement pstmt2 = connection.prepareStatement(sql2)) {
            pstmt.setInt(1, pointId);
            pstmt2.setInt(1, pointId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                pstmt2.executeUpdate();
                logger.info("管理员恢复点位{}", pointId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("管理员恢复点位失败: {}", e.getMessage());
        }
        return false;
    }
    
    // 检查并标记删除（超过阈值自动标记）
    private boolean checkAndMarkDeleted(int pointId) {
        try {
            int threshold = getDeleteThreshold();
            String sql = "SELECT propose_delete FROM points WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, pointId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int proposeCount = rs.getInt("propose_delete");
                        if (proposeCount >= threshold) {
                            // 标记为删除
                            String updateSql = "UPDATE points SET deleted = 1, deleted_time = ? WHERE id = ?";
                            try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                                updatePstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                                updatePstmt.setInt(2, pointId);
                                updatePstmt.executeUpdate();
                                logger.info("点位{}因超过阈值{}被自动标记为删除", pointId, threshold);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("检查并标记删除失败: {}", e.getMessage());
            return false;
        }
        return true;
    }
    
    // 获取删除阈值
    public int getDeleteThreshold() {
        String sql = "SELECT value FROM config WHERE key = 'delete_threshold'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString("value"));
            }
        } catch (SQLException e) {
            logger.error("获取删除阈值失败: {}\n默认5。", e.getMessage());
        }
        return 5; // 默认阈值
    }
    
    // 设置删除阈值（仅管理员）
    public boolean setDeleteThreshold(int threshold) {
        String sql = "UPDATE config SET value = ? WHERE key = 'delete_threshold'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, String.valueOf(threshold));
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("删除阈值更新为{}", threshold);
                return true;
            }
        } catch (SQLException e) {
            logger.error("设置删除阈值失败: {}", e.getMessage());
        }
        return false;
    }

    // 获取所有点位（包括已删除的）
    public List<Point> getAllPoints() {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT * FROM points ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                points.add(getPoint(rs));
            }
        } catch (SQLException e) {
            logger.error("获取所有点位列表失败: {}", e.getMessage());
        }
        return points;
    }

}
