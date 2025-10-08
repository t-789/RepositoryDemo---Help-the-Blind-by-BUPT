package org.example.RepositoryDemo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.logging.log4j.*;

@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger logger = LogManager.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        System.out.println("status: " + status);

        if (status != null) {
            try {
                int statusCode = Integer.parseInt(status.toString());

                if(statusCode == HttpStatus.FORBIDDEN.value()) {
                    model.addAttribute("errorCode", "403");
                    model.addAttribute("errorMessage", "访问被拒绝");
                    model.addAttribute("errorDescription", "您没有权限访问该页面，请联系管理员。");
                    return "error/403";
                }
                else if(statusCode == HttpStatus.NOT_FOUND.value()) {
                    model.addAttribute("errorCode", "404");
                    model.addAttribute("errorMessage", "页面未找到");
                    model.addAttribute("errorDescription", "您访问的页面不存在。");
                    return "error/404";
                }
                else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    model.addAttribute("errorCode", "500");
                    model.addAttribute("errorMessage", "服务器内部错误");
                    model.addAttribute("errorDescription", "服务器发生了未知错误，请稍后再试。");
                    return "error/500";
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing status code: {}", status);
                logger.error(e.getMessage());
            }
        }

        model.addAttribute("errorCode", "未知错误");
        model.addAttribute("errorMessage", "发生未知错误");
        model.addAttribute("errorDescription", "系统发生了未知错误，请稍后再试。");
        return "error/general";
    }

    // 添加这个方法处理NoHandlerFoundException异常
    @RequestMapping("/error/404")
    public String handleError404(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "页面未找到");
        model.addAttribute("errorDescription", "您访问的页面不存在。");
        return "error/404";
    }

    @RequestMapping("/error/403")
    public String handleError403(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "访问被拒绝");
        model.addAttribute("errorDescription", "您没有权限访问该页面，请联系管理员。");
        return "error/403";
    }

    @RequestMapping("/error/500")
    public String handleError500(Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "服务器内部错误");
        model.addAttribute("errorDescription", "服务器发生了未知错误，请稍后再试。");
        return "error/500";
    }



    public String getErrorPath() {
        return "/error";
    }
}