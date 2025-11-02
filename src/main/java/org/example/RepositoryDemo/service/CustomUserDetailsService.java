package org.example.RepositoryDemo.service;

import lombok.Setter;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.security.WebSecurityConfig;
import org.example.RepositoryDemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.*;

import org.apache.logging.log4j.*;

@Service
@Setter
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(CustomUserDetailsService.class);

    private static String JudgeType(int type){
        return switch (type) {
            case 0 -> "USER";
            case 1 -> "BLIND";
            case 2 -> "ADMIN";
            default -> "UNKNOWN";
        };
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("用户{}不存在", username);
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 检查账户是否被封禁
        if (user.isBanned != null && user.isBanned) {
            // 检查封禁是否已经结束
            if (user.banEndTime != null && user.banEndTime.after(new java.util.Date())) {
                // 账户仍在封禁期内，抛出自定义异常
                throw new WebSecurityConfig.DisabledException("账户被封禁至" + user.banEndTime);
            }
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.username)
                .password(user.password_hash)
                .roles(JudgeType(user.type))
                .disabled(user.isBanned && (user.banEndTime == null || user.banEndTime.after(new java.util.Date()))) // 如果被封禁且仍在封禁期内则禁用账户
                .build();
    }
}