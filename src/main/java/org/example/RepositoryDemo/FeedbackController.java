package org.example.RepositoryDemo;

import org.example.RepositoryDemo.dto.FeedbackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
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
            
            boolean success = feedbackService.saveUserFeedback(
                    userId, 
                    username, 
                    feedbackRequest.getContent(),
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
            
            boolean success = feedbackService.saveSystemFeedback(
                    userId,
                    username,
                    feedbackRequest.getContent(),
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
            if (!isAdmin(authentication)) {
                return ResponseEntity.badRequest().body(Map.of("error", "权限不足"));
            }
            
            List<Feedback> feedbacks = feedbackService.getAllFeedback();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取反馈列表失败: " + e.getMessage()));
        }
    }
    
    // 根据类型获取反馈（仅管理员）
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getFeedbackByType(@PathVariable String type, Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.badRequest().body(Map.of("error", "权限不足"));
            }
            
            List<Feedback> feedbacks = feedbackService.getFeedbackByType(type);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取反馈列表失败: " + e.getMessage()));
        }
    }
    
    // 更新反馈解决状态（仅管理员）
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveFeedback(@PathVariable int id, 
                                           @RequestBody Map<String, Object> payload,
                                           Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.badRequest().body(Map.of("error", "权限不足"));
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
            return ResponseEntity.badRequest().body(Map.of("error", "参数错误: " + e.getMessage()));
        }
    }
    
    // 检查是否为管理员
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        return user != null && user.type == 2;
    }
}