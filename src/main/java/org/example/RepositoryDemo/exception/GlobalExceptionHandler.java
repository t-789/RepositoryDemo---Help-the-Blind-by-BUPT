package org.example.RepositoryDemo.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.FeedbackRepository;
import org.example.RepositoryDemo.service.FeedbackService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex, WebRequest request) throws NoResourceFoundException, RuntimeException {
        // 不再直接提交反馈到系统，而是让异常继续传播到 CustomErrorController 处理
        // 这样可以避免重复提交反馈的问题
        
        // 特别处理 NoResourceFoundException，让它由容器处理
        if (ex instanceof NoResourceFoundException) {
            logger.warn("Resource not found: {}", request.getDescription(false));
            throw (NoResourceFoundException) ex;
        }
        
        // 继续传播异常，让其他异常处理器处理
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else {
            FeedbackService feedbackService = new FeedbackService(new FeedbackRepository());
            feedbackService.saveSystemFeedback(null, "system", ex.getMessage(), request.getDescription(false), "Unknown", Arrays.toString(ex.getStackTrace()));
            throw new RuntimeException(ex);
        }
    }
}