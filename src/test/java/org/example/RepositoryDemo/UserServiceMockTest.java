package org.example.RepositoryDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceMockTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testRegisterWithValidData() {
        // 模拟userRepository的行为
        User mockUser = new User();
        mockUser.id = 1;
        mockUser.username = "testuser";
        mockUser.password_hash = "hashedpassword";
        mockUser.type = 0;

        when(userRepository.registerUser(any(User.class))).thenReturn(1);

        boolean result = userService.register("testuser", "password123", 0);
        assertTrue(result);

        // 验证userRepository的方法被调用了一次
        verify(userRepository, times(1)).registerUser(any(User.class));
    }

    @Test
    public void testGrantAdminPermission() {
        // 模拟userRepository.updateUserType方法的行为
        when(userRepository.updateUserType(1, 2)).thenReturn(1);

        int result = userService.grantAdminPermission(1);
        assertEquals(1, result);

        // 验证userRepository的方法被调用了一次
        verify(userRepository, times(1)).updateUserType(1, 2);
    }

    @Test
    public void testRevokeAdminPermission() {
        // 模拟userRepository.updateUserType方法的行为
        when(userRepository.updateUserType(1, 0)).thenReturn(1);

        int result = userService.revokeAdminPermission(1);
        assertEquals(1, result);

        // 验证userRepository的方法被调用了一次
        verify(userRepository, times(1)).updateUserType(1, 0);
    }

    @Test
    public void testBanUser() {
        // 模拟userRepository.banUser方法的行为
        when(userRepository.banUser(1, "1d")).thenReturn(true);

        boolean result = userService.banUser(1, "1d");
        assertTrue(result);
    }

    @Test
    public void testUnbanUser() {
        // 模拟userRepository.unbanUser方法的行为
        when(userRepository.unbanUser(1)).thenReturn(true);

        boolean result = userService.unbanUser(1);
        assertTrue(result);
    }
}