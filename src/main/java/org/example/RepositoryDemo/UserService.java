package org.example.RepositoryDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 用户注册
    public boolean register(String username, String password, int type) {
        User user = new User();
        user.username = username;
        user.password_hash = passwordEncoder.encode(password); // 加密密码
        user.type = type; // 默认普通用户

        int result = UserRepository.registerUser(user);
        return result > 0;
    }

    // 用户登录
    public User login(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // 检查账户是否被封禁
            if (user.isBanned != null && user.isBanned) {
                // 检查封禁是否已经结束
                if (user.banEndTime != null && user.banEndTime.after(new java.util.Date())) {
                    throw new Exception("账户被封禁至" + user.banEndTime);
                }
            }
            
            // 验证密码
            if (passwordEncoder.matches(password, user.password_hash)) {
                return user;
            }
        }
        return null;
    }

    // 用户登出
//    public boolean logout() {
//        // 登出逻辑在Controller层通过使会话失效处理
//        return true;
//    }

    // 管理员权限管理
    public int grantAdminPermission(int userId) {
        return userRepository.updateUserType(userId, 2);
    }

    // 撤销管理员权限
    public int revokeAdminPermission(int userId) {
        return userRepository.updateUserType(userId, 0);
    }

    // 用户封禁
    public boolean banUser(int userId, String banTime) {
        return userRepository.banUser(userId, banTime);
    }
    
    // 用户解禁
    public boolean unbanUser(int userId) {
        return userRepository.unbanUser(userId);
    }
}