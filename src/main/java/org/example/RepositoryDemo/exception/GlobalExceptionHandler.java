package org.example.RepositoryDemo.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.FeedbackRepository;
import org.example.RepositoryDemo.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Arrays;
import java.util.Set;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    private static final Set<String> BOT_USER_AGENTS = Set.of(
            "bot", "crawler", "spider", "scraper", "python", "curl", "wget"
    );
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex, WebRequest request) throws RuntimeException {
        // 不再直接提交反馈到系统，而是让异常继续传播到 CustomErrorController 处理
        // 这样可以避免重复提交反馈的问题
        if (isBotRequest(request.getHeader("User-Agent"))) {
            logger.warn("Bot request: {}", request.getDescription(false));
            return ResponseEntity.status(403).body("Forbidden");
        }
        // 特别处理 NoResourceFoundException，让它由容器处理
        if (ex instanceof NoResourceFoundException) {
            logger.warn("Resource not found: {}", request.getDescription(false));
            return ResponseEntity.notFound().build();
        }
        else if (ex instanceof HttpRequestMethodNotSupportedException) {
            logger.warn("Method not supported: {}", request.getDescription(false));
            return ResponseEntity.notFound().build();
        } else if (ex instanceof org.springframework.web.HttpMediaTypeNotSupportedException){
            logger.warn("Media type not supported: {}", request.getDescription(false));
            return ResponseEntity.status(400).body("Unsupported media type");
        } else if (ex instanceof jakarta.validation.ConstraintViolationException){
            logger.warn("Constraint violation: {}", request.getDescription(false));
            return ResponseEntity.badRequest().body("Invalid input");
        } else if (ex instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException){
            logger.warn("Method argument type mismatch: {}", request.getDescription(false));
            return ResponseEntity.badRequest().body("Method argument type mismatch");
        } else if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException){
            logger.warn("Method argument not valid: {}", request.getDescription(false));
            return ResponseEntity.badRequest().body("Method argument not valid.");
        } else if (ex instanceof org.springframework.web.bind.MissingServletRequestParameterException){
            logger.warn("Missing servlet request parameter: {}", request.getDescription(false));
            return ResponseEntity.badRequest().body("Missing servlet request parameter.");
        } else if (ex instanceof org.springframework.security.authentication.InternalAuthenticationServiceException){
            logger.warn("InternalAuthenticationServiceException: {}", request.getDescription(false));
        }
        
        // 记录异常详细信息
        logger.error("Unhandled exception occurred: ", ex);
        
        // 继续传播异常，让其他异常处理器处理
        if (ex instanceof RuntimeException) {
            return ResponseEntity.status(500).body("Internal Server Error");
        } else {
            try {
                FeedbackService feedbackService = new FeedbackService(new FeedbackRepository());
                feedbackService.saveSystemFeedback(null, "system", ex.getMessage(), request.getDescription(false), "Unknown", Arrays.toString(ex.getStackTrace()));
            } catch (Exception e) {
                logger.error("Failed to save system feedback: ", e);
            }
            throw new RuntimeException(ex);
        }
    }
    
    private boolean isBotRequest(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return true;
        String ua = userAgent.toLowerCase();
        return BOT_USER_AGENTS.stream().anyMatch(ua::contains);
    }
}