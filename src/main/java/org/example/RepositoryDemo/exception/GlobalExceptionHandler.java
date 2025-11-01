package org.example.RepositoryDemo.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex, WebRequest request) {
        // 不再直接提交反馈到系统，而是让异常继续传播到 CustomErrorController 处理
        // 这样可以避免重复提交反馈的问题
        
        // 继续传播异常，让其他异常处理器处理
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else {
            throw new RuntimeException(ex);
        }
    }
}