package org.example.RepositoryDemo;

import org.example.RepositoryDemo.dto.RegisterRequest;
import org.example.RepositoryDemo.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.Map;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PointService pointService;

    @Autowired
    private FeedbackService feedbackService;
    
    // 用户注册
    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            boolean success = userService.register(registerRequest.getUsername(), registerRequest.getPassword(), 0);
            if (success) {
                return ResponseEntity.ok("注册成功");
            } else {
                return ResponseEntity.badRequest().body("注册失败，用户名可能已存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // 调用 UserService 的登录方法
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            if (user != null) {
                // 通过Spring Security进行认证
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    userDetails.getPassword(), 
                    userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 将认证信息存储在会话中
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                    SecurityContextHolder.getContext()
                );
                
                return ResponseEntity.ok("登录成功");
            } else {
                return ResponseEntity.badRequest().body("登录失败，用户名或密码错误");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("账户被封禁至")){
                return ResponseEntity.badRequest().body("账户被封禁至" + e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API登出接口
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // 清除Spring Security上下文
            SecurityContextHolder.clearContext();
            
            // 使会话失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok("登出成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("登出失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable int userId, @RequestBody Map<String, String> payload) {
        try {
            String banTime = payload.get("banTime");
            if (banTime == null || banTime.isEmpty()) {
                return ResponseEntity.badRequest().body("封禁时间不能为空");
            }

            // 调用 UserService 的封禁方法
            boolean success = userService.banUser(userId, banTime);
            if (success) {
                return ResponseEntity.ok("用户封禁操作完成");
            } else {
                return ResponseEntity.badRequest().body("封禁操作失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("封禁操作失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable int userId) {
        try {
            boolean success = userService.unbanUser(userId);
            if (success) {
                return ResponseEntity.ok("用户解封操作完成");
            } else {
                return ResponseEntity.badRequest().body("解封操作失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("解封操作失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/grant-admin")
    public ResponseEntity<?> grantAdmin(@PathVariable int userId) {
        try {
            int result = userService.grantAdminPermission(userId);
            return switch (result) {
                case 1 -> ResponseEntity.ok("用户" + userId + "已被赋予管理员权限");
                case -1 -> ResponseEntity.badRequest().body("用户" + userId + "不存在");
                default -> ResponseEntity.badRequest().body("未知错误。请联系管理员");
            };
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/revoke-admin")
    public ResponseEntity<?> revokeAdmin(@PathVariable int userId) {
        try {
            int result = userService.revokeAdminPermission(userId);
            return switch (result) {
                case 1 -> ResponseEntity.ok("用户" + userId + "已被撤销管理员权限");
                case -1 -> ResponseEntity.badRequest().body("用户" + userId + "不存在");
                default -> ResponseEntity.badRequest().body("未知错误。请联系管理员");
            };
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    // 管理员重置用户密码
    @PostMapping("/admin/reset-password/{userId}")
    public ResponseEntity<?> adminResetPassword(@PathVariable int userId, Authentication authentication) {
        try {
            // 检查权限
            String username = authentication.getName();
            User adminUser = userRepository.findByUsername(username);
            if (adminUser == null || adminUser.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            // 查找要重置密码的用户
            User targetUser = userRepository.findById(userId);
            if (targetUser == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            // 重置密码为"000000"
            String defaultPassword = "000000";
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            boolean success = userRepository.updateUserPassword(targetUser.id, encodedPassword);
            
            if (success) {
                // 记录日志
                System.out.println("管理员 " + adminUser.username + " 重置了用户 " + targetUser.username + " 的密码");
                return ResponseEntity.ok("密码重置成功，新密码为: " + defaultPassword);
            } else {
                return ResponseEntity.badRequest().body("密码重置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
}