package org.example.RepositoryDemo;

import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.service.PointService;
import org.example.RepositoryDemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class ComprehensiveServiceTest {


    @Test
    public void testDatabaseConnection() throws Exception {
        // 测试数据库连接
        String url = "jdbc:sqlite:test.db";
        try (Connection connection = DriverManager.getConnection(url)) {
            assertTrue(connection != null && !connection.isClosed(), "数据库连接应该成功建立");
        }
    }

    @Test
    public void testTableExistence() throws Exception {
        // 测试用户表是否存在
        String url = "jdbc:sqlite:test.db";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到用户表");
        } catch (Exception e) {
            fail("用户表应该存在: " + e.getMessage());
        }

        // 测试点位表是否存在
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM points");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到点位表");
        } catch (Exception e) {
            fail("点位表应该存在: " + e.getMessage());
        }

        // 测试配置表是否存在
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM config");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到配置表");
        } catch (Exception e) {
            fail("配置表应该存在: " + e.getMessage());
        }

        // 测试提议删除表是否存在
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM point_proposals");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到提议删除表");
        } catch (Exception e) {
            fail("提议删除表应该存在: " + e.getMessage());
        }
    }

    @Test
    public void testDefaultAdminUser() throws Exception {
        // 测试默认管理员用户是否存在
        String url = "jdbc:sqlite:test.db";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = 'admin' AND type = 2");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到默认管理员用户");
            assertTrue(rs.getInt(1) > 0, "应该存在用户名为admin且类型为2的管理员用户");
        } catch (Exception e) {
            fail("默认管理员用户应该存在: " + e.getMessage());
        }
    }

    @Test
    public void testDataInitialization() throws Exception {
        // 测试配置数据是否初始化
        String url = "jdbc:sqlite:test.db";
        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT value FROM config WHERE key = 'delete_threshold'");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到删除阈值配置");
            int threshold = Integer.parseInt(rs.getString("value"));
            assertTrue(threshold > 0, "删除阈值应该为正数");
        } catch (Exception e) {
            fail("删除阈值配置应该存在: " + e.getMessage());
        }
    }
}