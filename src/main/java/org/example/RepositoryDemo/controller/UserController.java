package org.example.RepositoryDemo.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.UserProfileResponse;
import org.example.RepositoryDemo.dto.registerWithSecurityRequest;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.Point;
import org.example.RepositoryDemo.service.UserService;
import org.example.RepositoryDemo.dto.RegisterRequest;
import org.example.RepositoryDemo.dto.LoginRequest;
import org.example.RepositoryDemo.entity.User;
import org.example.RepositoryDemo.service.PointService;
import org.example.RepositoryDemo.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private ForumService forumService;

    private static final Logger logger = LogManager.getLogger(UserController.class);
    
    // 用户注册 - JSON格式
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

    // 用户注册 - 带头像上传和密保问题设置
    @PostMapping(value = "/register-with-security", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerWithSecurity(@Valid @ModelAttribute registerWithSecurityRequest request) {
        try {
            // 先进行基本的注册
            boolean success = userService.register(request.getUsername(), request.getPassword(), 0);
            
            if (success) {
                // 查找刚创建的用户
                User user = userRepository.findByUsername(request.getUsername());
                
                // 如果提供了头像文件，则处理头像上传
                if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
                    // 处理头像上传
                    String avatarPath = saveAvatarFile(request.getAvatar());
                    
                    if (avatarPath != null) {
                        // 更新用户头像信息
                        userService.updateUserAvatar(user.id, avatarPath);
                    }
                }
                
                // 设置密保问题
                userService.setSecurityQuestions(user.id, request.getQuestion1(), request.getAnswer1(), request.getQuestion2(), request.getAnswer2());
                
                return ResponseEntity.ok("注册成功");
            } else {
                return ResponseEntity.badRequest().body("注册失败，用户名可能已存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }

    // 保存头像文件的辅助方法
    private String saveAvatarFile(MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return null;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return null;
            }

            // 检查文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return null;
            }

            // 创建头像存储目录
            String uploadDir = "./external/static/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if(!dir.mkdirs()){
                    logger.fatal("创建头像存储目录失败");
                    return null;
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;
            Path filePath = Paths.get(uploadDir + fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回头像访问路径
            return "/avatars/" + fileName;
        } catch (IOException e) {
            logger.error("保存头像文件失败: " + e.getMessage());
            return null;
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

    // 获取当前用户信息
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    // 创建一个不包含敏感信息的用户对象
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.id);
                    userInfo.put("username", user.username);
                    userInfo.put("type", user.type);
                    userInfo.put("credit", user.credit);
                    userInfo.put("avatar", user.avatar); // 添加头像信息
                    return ResponseEntity.ok(userInfo);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户信息失败: " + e.getMessage());
        }
    }

    // 上传用户头像
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择一个文件");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return ResponseEntity.badRequest().body("只允许上传JPEG或PNG格式的图片");
            }

            // 检查文件大小（限制为2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("文件大小不能超过2MB");
            }

            // 获取当前用户
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }

            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }

            // 创建头像存储目录
            String uploadDir = "./external/static/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    logger.fatal("创建头像存储目录失败");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建头像存储目录失败");
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;
            Path filePath = Paths.get(uploadDir + fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 更新数据库中的用户头像路径
            String avatarPath = "/avatars/" + fileName;
            boolean success = userService.updateUserAvatar(user.id, avatarPath);
            
            if (success) {
                // 更新会话中的用户信息
                user.avatar = avatarPath;
                session.setAttribute("user", user);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "头像上传成功");
                response.put("avatarUrl", avatarPath);
                return ResponseEntity.ok(response);
            } else {
                // 删除刚刚保存的文件
                Files.deleteIfExists(filePath);
                return ResponseEntity.badRequest().body("头像上传失败");
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("头像上传失败: " + e.getMessage());
        }
    }

    // 获取所有安全问题
    @GetMapping("/security-questions")
    public ResponseEntity<?> getSecurityQuestions() {
        logger.info("获取所有安全问题");
        try {
            Map<Integer, String> questions = userService.getAllSecurityQuestions();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("获取安全问题失败: ", e);
            return ResponseEntity.badRequest().body("获取安全问题失败: " + e.getMessage());
        }
    }

    // 获取用户安全问题（用于密码重置）
    @GetMapping("/{username}/security-questions")
    public ResponseEntity<?> getUserSecurityQuestions(@PathVariable String username) {
        try {
            // 查找用户
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            // 获取用户的安全问题
            UserRepository.QuestionSet questionSet = userRepository.getQuestionsById(user.id);
            if (questionSet == null) {
                return ResponseEntity.badRequest().body("该用户未设置密保问题");
            }
            
            // 返回用户的安全问题（不返回答案）
            Map<String, String> questions = new HashMap<>();
            questions.put("question1", questionSet.question1);
            questions.put("question2", questionSet.question2);
            
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户安全问题失败: " + e.getMessage());
        }
    }

    // 验证密保问题并重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            String answer1 = (String) payload.get("answer1");
            String answer2 = (String) payload.get("answer2");
            
            // 查找用户
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            // 验证密保问题答案
            boolean isVerified = userService.verifySecurityAnswers(user.id, answer1, answer2);
            if (!isVerified) {
                return ResponseEntity.badRequest().body("密保问题答案不正确");
            }
            
            // 重置密码为"000000"
            boolean success = userService.resetPassword(user.id, "000000");
            if (success) {
                return ResponseEntity.ok("密码重置成功，新密码为: 000000");
            } else {
                return ResponseEntity.badRequest().body("密码重置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("密码重置失败: " + e.getMessage());
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
                logger.info("管理员 {} 重置了用户 {} 的密码", adminUser.username, targetUser.username);
                return ResponseEntity.ok("密码重置成功，新密码为: " + defaultPassword);
            } else {
                return ResponseEntity.badRequest().body("密码重置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 添加安全问题（管理员接口）
    @PostMapping("/admin/security-question")
    public ResponseEntity<?> addSecurityQuestion(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            // 检查权限
            String username = authentication.getName();
            User adminUser = userRepository.findByUsername(username);
            if (adminUser == null || adminUser.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            String questionText = (String) payload.get("questionText");
            
            if (questionText == null || questionText.isEmpty()) {
                return ResponseEntity.badRequest().body("问题文本不能为空");
            }
            
            boolean success = userService.addSecurityQuestion(questionText);
            if (success) {
                return ResponseEntity.ok("密保问题添加成功");
            } else {
                return ResponseEntity.badRequest().body("密保问题添加失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 删除安全问题（管理员接口）
    @DeleteMapping("/admin/security-question/{questionId}")
    public ResponseEntity<?> deleteSecurityQuestion(@PathVariable int questionId, Authentication authentication) {
        try {
            // 检查权限
            String username = authentication.getName();
            User adminUser = userRepository.findByUsername(username);
            if (adminUser == null || adminUser.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            boolean success = userService.deleteSecurityQuestion(questionId);
            if (success) {
                return ResponseEntity.ok("密保问题删除成功");
            } else {
                return ResponseEntity.badRequest().body("密保问题删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 更新安全问题（管理员接口）
    @PutMapping("/admin/security-question/{questionId}")
    public ResponseEntity<?> updateSecurityQuestion(@PathVariable int questionId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            // 检查权限
            String username = authentication.getName();
            User adminUser = userRepository.findByUsername(username);
            if (adminUser == null || adminUser.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            String questionText = (String) payload.get("questionText");
            
            if (questionText == null || questionText.isEmpty()) {
                return ResponseEntity.badRequest().body("问题文本不能为空");
            }
            
            boolean success = userService.updateSecurityQuestion(questionId, questionText);
            if (success) {
                return ResponseEntity.ok("密保问题更新成功");
            } else {
                return ResponseEntity.badRequest().body("密保问题更新失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    // 获取用户个人主页信息
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            // 获取当前认证用户
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户未登录");
            }
            
            // 获取用户点位数量
            List<Point> userPoints = pointService.getPointsByUserId(user.id);
            int pointCount = userPoints != null ? userPoints.size() : 0;
            
            // 获取用户帖子数量
            List<Forum> userPosts = forumService.getForumsByUserId(user.id);
            int postCount = userPosts != null ? userPosts.size() : 0;
            
            // 获取用户积分
            int credit = user.credit;
            
            // 构造响应对象
            UserProfileResponse profileResponse = new UserProfileResponse(pointCount, postCount, credit);
            
            return ResponseEntity.ok(profileResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户信息失败: " + e.getMessage());
        }
    }
}