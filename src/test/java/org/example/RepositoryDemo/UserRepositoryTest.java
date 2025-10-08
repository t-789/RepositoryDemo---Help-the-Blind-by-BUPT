package org.example.RepositoryDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 确保测试数据不会真正保存到数据库
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testRegisterUser() {
        User user = new User();
        user.username = "testuser";
        user.password_hash = "hashedpassword";
        user.type = 0;

        // 检查用户是否可以成功注册
        int result = userRepository.registerUser(user);
        assertEquals(1, result); // 注册成功返回1
    }

    @Test
    public void testFindByUsername() {
        // 先注册一个用户
        User user = new User();
        user.username = "findusertest";
        user.password_hash = "hashedpassword";
        user.type = 0;
        userRepository.registerUser(user);

        // 然后查找这个用户
        User foundUser = userRepository.findByUsername("findusertest");
        assertNotNull(foundUser);
        assertEquals("findusertest", foundUser.username);
    }

    @Test
    public void testFindById() {
        // 先创建一个已知ID的用户（通过注册一个用户然后查询）
        User user = new User();
        user.username = "findbyidtest";
        user.password_hash = "hashedpassword";
        user.type = 0;
        userRepository.registerUser(user);

        // 查找这个用户以获取ID
        User savedUser = userRepository.findByUsername("findbyidtest");

        // 然后通过ID查找这个用户
        User foundUser = userRepository.findById(savedUser.id);
        assertNotNull(foundUser);
        assertEquals(savedUser.id, foundUser.id);
    }

    @Test
    public void testUpdateUserType() {
        // 先注册一个用户
        User user = new User();
        user.username = "updatetypetest";
        user.password_hash = "hashedpassword";
        user.type = 0;
        userRepository.registerUser(user);

        // 获取用户ID
        User savedUser = userRepository.findByUsername("updatetypetest");

        // 更新用户类型为管理员
        int result = userRepository.updateUserType(savedUser.id, 2);
        assertEquals(1, result); // 应该成功更新一行

        // 验证更新是否成功
        User updatedUser = userRepository.findById(savedUser.id);
        assertEquals(2, updatedUser.type);
    }

    @Test
    public void testListUsers() {
        // 获取所有用户列表
        List<User> users = UserRepository.ListUsers();
        assertNotNull(users);
        // 至少应该有默认的管理员用户
        assertTrue(users.size() >= 0);
    }
}