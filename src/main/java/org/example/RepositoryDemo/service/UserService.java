package org.example.RepositoryDemo.service;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 用户注册
    public boolean register(String username, String password, int type) {
        logger.debug("开始注册用户: username={}, type={}", username, type);
        boolean result = registerWithAvatar(username, password, type, null);
        logger.debug("用户注册结果: username={}, result={}", username, result);
        return result;
    }

    // 带头像的用户注册
    public boolean registerWithAvatar(String username, String password, int type, String avatarPath) {
        User user = new User();
        user.username = username;
        user.password_hash = passwordEncoder.encode(password); // 加密密码
        user.type = type; // 默认普通用户
        user.avatar = avatarPath; // 头像路径

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

    // 更新用户头像
    public boolean updateUserAvatar(int userId, String avatarPath) {
        return userRepository.updateUserAvatar(userId, avatarPath);
    }

    // 设置用户密保问题
    public void setSecurityQuestions(int userId, int questionId1, String answer1, int questionId2, String answer2) {
        // 对答案进行加密
        String encodedAnswer1 = passwordEncoder.encode(answer1);
        String encodedAnswer2 = passwordEncoder.encode(answer2);
        UserRepository.setQuestionForUser(userId, questionId1, encodedAnswer1, questionId2, encodedAnswer2);
    }

    // 验证用户密保问题答案
    public boolean verifySecurityAnswers(int userId, String answer1, String answer2) {
        return userRepository.verifySecurityAnswers(userId, answer1, answer2);
    }

    // 重置用户密码
    public boolean resetPassword(int userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        return userRepository.updateUserPassword(userId, encodedPassword);
    }
    
    // 添加安全问题
    public boolean addSecurityQuestion(String question) {
        return userRepository.addSecurityQuestion(question);
    }
    
    // 删除安全问题
    public boolean deleteSecurityQuestion(int questionId) {
        return userRepository.deleteSecurityQuestion(questionId);
    }
    
    // 更新安全问题
    public boolean updateSecurityQuestion(int questionId, String newQuestion) {
        return userRepository.updateSecurityQuestion(questionId, newQuestion);
    }

    // 获取所有安全问题列表
    public Map<Integer, String> getAllSecurityQuestions() {
        return userRepository.getAllSecurityQuestions();
    }


}