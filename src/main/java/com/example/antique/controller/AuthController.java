package com.example.antique.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller xử lý các URL liên quan đến xác thực:
 * - Hiển thị trang đăng nhập
 * ---
 * Lưu ý: Spring Security TỰ XỬ LÝ POST /auth/login
 * (đã cấu hình loginProcessingUrl trong SecurityConfig).
 * Controller này CHỈ cần xử lý GET /auth/login để hiển thị form.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * Hiển thị trang đăng nhập.
     * @param error  có giá trị khi đăng nhập sai (Spring Security truyền qua URL param)
     * @param logout có giá trị khi vừa đăng xuất thành công
     * @param authentication đối tượng xác thực hiện tại (null nếu chưa đăng nhập)
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            Authentication authentication) {

        // Nếu đã đăng nhập rồi → không cần vào trang login, chuyển về dashboard
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }

        // Thêm thông báo lỗi/logout vào model để hiển thị trên template
        if (error != null) {
            model.addAttribute("loginError", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công.");
        }

        return "auth/login"; // → src/main/resources/templates/auth/login.html
    }

}
