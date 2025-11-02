package org.example.RepositoryDemo.errorHandle;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.service.FeedbackService;
import org.example.RepositoryDemo.entity.User;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@Setter
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LogManager.getLogger(CustomErrorController.class);

    private UserRepository userRepository;

    private FeedbackService feedbackService;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 获取错误信息
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // 获取用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = null;
        String username = "Anonymous";
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String authUsername = authentication.getName();
            User user = userRepository.findByUsername(authUsername);
            if (user != null) {
                userId = user.id;
                username = user.username;
            }
        }
        
        // 自动提交系统错误反馈
        if (status != null) {
            try {
                HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
                
                // 记录5xx系列服务器错误
                if (httpStatus.is5xxServerError()) {
                    final Integer finalUserId = userId;
                    final String finalUsername = username;
                    String content = "HTTP " + status + ": " + (errorMessage != null ? errorMessage : "未知错误");
                    String url = requestUri != null ? requestUri.toString() : request.getRequestURI();
                    String userAgent = request.getHeader("User-Agent");
                    
                    // 获取异常对象以获取实际的堆栈跟踪
                    Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                    String stackTrace;
                    if (exception instanceof Throwable) {
                        StringBuilder stackTraceBuilder = new StringBuilder();
                        stackTraceBuilder.append("Exception: ").append(exception.getClass().getName()).append("\n");
                        stackTraceBuilder.append("Message: ").append(((Throwable) exception).getMessage()).append("\n");
                        stackTraceBuilder.append("Stack Trace:\n");
                        
                        for (StackTraceElement element : ((Throwable) exception).getStackTrace()) {
                            stackTraceBuilder.append("  at ").append(element.toString()).append("\n");
                        }
                        
                        stackTrace = stackTraceBuilder.toString();
                    } else {
                        stackTrace = "Status: " + status + ", Message: " + errorMessage;
                    }

                    // 异步提交错误反馈
                    executorService.submit(() -> {
                        feedbackService.saveSystemFeedback(finalUserId, finalUsername, content, url, userAgent, stackTrace);
                    });
                }
            } catch (Exception e) {
                // 忽略错误处理中的异常
            }
        }
        
        // 设置错误页面显示信息
        model.addAttribute("statusCode", status);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("requestUri", requestUri);
        
        // 根据错误状态码返回不同的错误页面
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            return switch (statusCode) {
                case 403 -> "error/403";
                case 404 -> "error/404";
                case 500 -> "error/500";
                default -> "error/general";
            };
        }
        
        return "error/general";
    }
    
    @RequestMapping("/error/403")
    public String handle403Error() {
        return "error/403";
    }
}