package org.example.RepositoryDemo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.User;
import org.example.RepositoryDemo.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LogManager.getLogger(RateLimitInterceptor.class);
    
    // 存储用户请求信息：用户ID -> 请求计数
    private final ConcurrentHashMap<Integer, Integer> requestCounts = new ConcurrentHashMap<>();
    
    // 存储用户请求时间戳：用户ID -> 时间戳
    private final ConcurrentHashMap<Integer, Long> requestTimestamps = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    
    public RateLimitInterceptor() {
        // 每分钟清理一次过期的请求记录
        // 自动清理过期记录的线程池
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanupExpiredRecords, 1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 如果用户未认证，允许访问（或者根据需要进行处理）
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return true;
        }
        
        // 获取用户名
        String username = authentication.getName();
        
        // 根据用户名查找用户
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return true; // 用户不存在，允许访问
        }
        
        int userId = user.id;
        long currentTime = System.currentTimeMillis();
        
        // 检查是否需要清理过期记录（超过1分钟的记录）
        Long userLastRequestTime = requestTimestamps.get(userId);
        if (userLastRequestTime != null && currentTime - userLastRequestTime > 60000) {
            requestCounts.remove(userId);
            requestTimestamps.remove(userId);
        }
        
        // 更新用户的请求计数
        int count = requestCounts.getOrDefault(userId, 0);
        
        // 如果请求次数超过阈值（例如：1分钟内超过60次请求），则封禁用户1小时
        if (count >= 600) {
            // 自动封禁用户1小时
            boolean banResult = userService.banUser(userId, "1h");
            if (banResult) {
                logger.warn("IMPORTANT 用户 {} 因高频访问被自动封禁1小时", username);
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("请求过于频繁，账户已被临时封禁");
                response.getWriter().flush();
                return false;
            }
        }
        
        // 更新请求计数和时间戳
        requestCounts.put(userId, count + 1);
        requestTimestamps.put(userId, currentTime);
        
        return true;
    }
    
    /**
     * 清理过期的请求记录
     */
    private void cleanupExpiredRecords() {
        long currentTime = System.currentTimeMillis();
        requestTimestamps.forEach((userId, timestamp) -> {
            // 清理超过5分钟的记录
            if (currentTime - timestamp > 300000) {
                requestCounts.remove(userId);
                requestTimestamps.remove(userId);
            }
        });
    }
    
    /**
     * 获取指定用户的请求计数
     * @param userId 用户ID
     * @return 请求计数
     */
    public int getRequestCount(int userId) {
        return requestCounts.getOrDefault(userId, 0);
    }
}