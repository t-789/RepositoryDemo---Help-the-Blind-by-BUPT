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
    public String admin(Model model, Authentication authentication) {
        // 检查用户是否为管理员
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            // 获取所有用户信息
            List<User> users = UserRepository.ListUsers();
            model.addAttribute("users", users);
            return "admin";
        } else {
            // 如果不是管理员，重定向到首页或登录页
            return "redirect:/";
        }
    }
}