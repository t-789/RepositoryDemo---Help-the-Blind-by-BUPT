package org.example.RepositoryDemo;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Getter
@Controller
public class HomeController {

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
            // 如果不是管理员，重定向到首页或登录页
            return "redirect:/";
        }
    }

    @GetMapping("/admin/user")
    public String userManagement(Model model, Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            // 获取所有用户信息
            List<User> users = UserRepository.ListUsers();
            model.addAttribute("users", users);
            return "user_management";
        } else {
            // 如果不是管理员，重定向到首页或登录页
            return "redirect:/";
        }
    }

    @GetMapping("/admin/point")
    public String pointManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "point_management";
        } else {
            // 如果不是管理员，重定向到首页或登录页
            return "redirect:/";
        }
    }

    @GetMapping("/admin/forum")
    public String forumManagement(Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "forum_management";
        } else {
            // 如果不是管理员，重定向到首页或登录页
            return "redirect:/";
        }
    }
    
    @GetMapping("/release-notes")
    public String releaseNotes() {
        return "release_notes";
    }
}