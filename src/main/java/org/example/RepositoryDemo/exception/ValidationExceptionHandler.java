package org.example.RepositoryDemo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

//    @ExceptionHandler(InternalAuthenticationServiceException.class)
//    public ResponseEntity<Map<String, String>> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("error", "登录错误");
//        error.put("message", "用户名或密码错误");
//        return ResponseEntity.badRequest().body(error);
//    }
//
//    @ExceptionHandler(WebSecurityConfig.DisabledException.class)
//    public ResponseEntity<Map<String, String>> handleDisabledExceptionException(InternalAuthenticationServiceException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("error", "登录错误");
//        error.put("message", "账户被封禁");
//        return ResponseEntity.badRequest().body(error);
//    }
    
    // 处理通用异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) throws NoResourceFoundException, NoHandlerFoundException {
        // 排除NoResourceFoundException，让它由容器处理以显示正确的404页面
        if (ex instanceof NoResourceFoundException) {
            throw (NoResourceFoundException) ex;
        }
        if (ex instanceof NoHandlerFoundException) {
            throw (NoHandlerFoundException) ex;
        }
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "请求处理失败");
        error.put("message", "服务器内部错误，请稍后再试");
        return ResponseEntity.status(500).body(error);
    }
}