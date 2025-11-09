package org.example.RepositoryDemo.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.service.FeedbackService;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.FeedbackRequest;
import org.example.RepositoryDemo.entity.Feedback;
import org.example.RepositoryDemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    private static final Logger logger = LogManager.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;
    
    // 用户提交反馈
    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest feedbackRequest, 
                                          Authentication authentication) {
        try {
            Integer userId = null;
            String username = "Anonymous";
            
            if (authentication != null && authentication.isAuthenticated()) {
                String authUsername = authentication.getName();
                User user = userRepository.findByUsername(authUsername);
                if (user != null) {
                    userId = user.id;
                    username = user.username;
                }
            }
            
            // 检查反馈内容
            String content = feedbackRequest.getContent();
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "反馈内容不能为空"));
            }
            
            boolean success = feedbackService.saveUserFeedback(
                    userId, 
                    username, 
                    content,
                    feedbackRequest.getUrl(),
                    feedbackRequest.getUserAgent()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "反馈提交成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "反馈提交失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "参数错误: " + e.getMessage()));
        }
    }
    
    // 系统自动提交错误反馈（内部使用）
    @PostMapping("/system-error")
    public ResponseEntity<?> submitSystemError(@RequestBody FeedbackRequest feedbackRequest,
                                             Authentication authentication) {
        try {
            Integer userId = null;
            String username = "Anonymous";
            
            if (authentication != null && authentication.isAuthenticated()) {
                String authUsername = authentication.getName();
                User user = userRepository.findByUsername(authUsername);
                if (user != null) {
                    userId = user.id;
                    username = user.username;
                }
            }
            
            // 检查反馈内容
            String content = feedbackRequest.getContent();
            if (content == null || content.trim().isEmpty()) {
                content = "未提供错误详情";
            }
            
            boolean success = feedbackService.saveSystemFeedback(
                    userId,
                    username,
                    content,
                    feedbackRequest.getUrl(),
                    feedbackRequest.getUserAgent(),
                    feedbackRequest.getStackTrace()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "系统错误报告已提交"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "系统错误报告提交失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "参数错误: " + e.getMessage()));
        }
    }
    
    // 获取所有反馈（仅管理员）
    @GetMapping("/all")
    public ResponseEntity<?> getAllFeedback(Authentication authentication) {
        try {
            if (isNotAdmin(authentication)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            List<Feedback> feedbacks = feedbackService.getAllFeedback();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            logger.error("获取反馈列表失败: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取反馈列表失败: " + e.getMessage()));
        }
    }
    
    // 根据类型获取反馈（仅管理员）
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getFeedbackByType(@PathVariable String type, Authentication authentication) {
        try {
            if (isNotAdmin(authentication)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            List<Feedback> feedbacks = feedbackService.getFeedbackByType(type);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            logger.error("获取反馈列表失败: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取反馈列表失败: " + e.getMessage()));
        }
    }
    
    // 更新反馈解决状态（仅管理员）
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveFeedback(@PathVariable int id, 
                                           @RequestBody Map<String, Object> payload,
                                           Authentication authentication) {
        try {
            if (isNotAdmin(authentication)) {
                return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
            }
            
            String resolvedBy = authentication.getName();
            Boolean resolved = (Boolean) payload.get("resolved");
            if (resolved == null) {
                resolved = true;
            }
            
            boolean success = feedbackService.updateResolvedStatus(id, resolved, resolvedBy);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "反馈状态更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "反馈状态更新失败"));
            }
        } catch (Exception e) {
            logger.error("更新反馈状态失败: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "参数错误: " + e.getMessage()));
        }
    }
    
    // 检查是否为管理员
    private boolean isNotAdmin(Authentication authentication) {
        // 检查认证状态
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("用户未认证或为匿名用户");
            return true;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        // 检查用户是否存在且为管理员（type=2）
        if (user == null) {
            logger.warn("用户 {} 不存在", username);
            return true;
        }
        
        if (user.type != 2) {
            logger.warn("用户 {} 不是管理员，类型: {}", username, user.type);
            return true;
        }
        
        logger.debug("用户 {} 是管理员", username);
        return false; // 用户是管理员
    }
}