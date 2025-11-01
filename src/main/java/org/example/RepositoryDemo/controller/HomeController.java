package org.example.RepositoryDemo.controller;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Getter
@Controller
public class HomeController {

    private static final Logger logger = LogManager.getLogger(HomeController.class);
    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
	public String index() {
		return "home";
	}

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String adminMain(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin_main";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }

    @GetMapping("/admin/user")
    public String userManagement(Model model, Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            // 获取所有用户信息
            List<User> users = userRepository.ListUsers();
            model.addAttribute("users", users);
            return "user_management";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }

    @GetMapping("/admin/point")
    public String pointManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "point_management";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }

    @GetMapping("/admin/forum")
    public String forumManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "forum_management";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }
    @GetMapping("/admin/security_question_management")
    public String securityQuestionManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "security_question_management";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }
    
    @GetMapping("/feedback_management")
    public String feedbackManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "feedback_management";
        } else {
            // 如果不是管理员，重定向到403错误页面
            return "redirect:/error/403";
        }
    }
    
    @GetMapping("/release-notes")
    public String releaseNotes() {
        return "release_notes";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/index")
    public String indexHtml() {
        return "index";
    }

    @GetMapping("/errortest")
    public String errorTest() { // 测试错误
        throw new RuntimeException("测试错误");
    }
}