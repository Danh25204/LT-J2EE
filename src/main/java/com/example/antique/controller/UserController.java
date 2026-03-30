package com.example.antique.controller;

import com.example.antique.dto.UserDTO;
import com.example.antique.entity.User;
import com.example.antique.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý người dùng - CHỈ truy cập được bởi ADMIN.
 *
 * @PreAuthorize("hasRole('ADMIN')") ở cấp class = áp dụng cho TẤT CẢ method.
 * Yêu cầu @EnableMethodSecurity trong SecurityConfig.
 *
 * URL pattern: /admin/users/**
 * Luồng CRUD:
 *   - Danh sách: GET /admin/users
 *   - Tạo mới:   GET /admin/users/create  → POST /admin/users/create
 *   - Chỉnh sửa: GET /admin/users/{id}/edit → POST /admin/users/{id}/edit
 *   - Toggle:    POST /admin/users/{id}/toggle-active
 *   - Xóa mềm:  POST /admin/users/{id}/delete
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    // ===== DANH SÁCH =====

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("pageTitle", "Quản lý người dùng");
        return "user/list";
    }

    // ===== TẠO MỚI =====

    /**
     * Hiển thị form tạo mới người dùng
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("pageTitle", "Thêm người dùng mới");
        model.addAttribute("isEdit", false);
        return "user/form";
    }

    /**
     * Xử lý form tạo mới người dùng.
     *
     * @Valid: kích hoạt Bean Validation trên UserDTO
     * BindingResult: chứa lỗi validation (PHẢI đứng ngay sau @Valid object)
     * RedirectAttributes: truyền flash message qua redirect
     */
    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra mật khẩu khi tạo mới (bắt buộc phải nhập)
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "password.required",
                    "Mật khẩu không được để trống khi tạo tài khoản mới");
        } else if (userDTO.getPassword().trim().length() < 6) {
            bindingResult.rejectValue("password", "password.tooShort",
                    "Mật khẩu phải có ít nhất 6 ký tự");
        } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch",
                    "Mật khẩu xác nhận không khớp");
        }

        // Nếu có lỗi validation → quay lại form với thông báo lỗi
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm người dùng mới");
            model.addAttribute("isEdit", false);
            return "user/form";
        }

        try {
            userService.createUser(userDTO);
            // Flash message: tồn tại sau 1 redirect, sau đó tự xóa
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo người dùng '" + userDTO.getUsername() + "' thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // ===== CHỈNH SỬA =====

    /**
     * Hiển thị form chỉnh sửa người dùng.
     * Load dữ liệu hiện tại từ DB và đổ vào UserDTO để bind vào form.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);

        // Convert User entity → UserDTO (chỉ lấy các field cần thiết)
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setActive(user.getActive());
        // Lấy role đầu tiên của user (trong đồ án mỗi user chỉ có 1 role)
        userDTO.setRoleName(user.getPrimaryRoleName());

        model.addAttribute("userDTO", userDTO);
        model.addAttribute("pageTitle", "Chỉnh sửa người dùng");
        model.addAttribute("isEdit", true);
        return "user/form";
    }

    /**
     * Xử lý form chỉnh sửa người dùng.
     * Password là tùy chọn khi edit: để trống = giữ nguyên pass cũ.
     */
    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra mật khẩu mới (chỉ khi có nhập)
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            if (userDTO.getPassword().trim().length() < 6) {
                bindingResult.rejectValue("password", "password.tooShort",
                        "Mật khẩu mới phải có ít nhất 6 ký tự");
            } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "password.mismatch",
                        "Mật khẩu xác nhận không khớp");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh sửa người dùng");
            model.addAttribute("isEdit", true);
            return "user/form";
        }

        try {
            userService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật người dùng '" + userDTO.getUsername() + "' thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // ===== TOGGLE ACTIVE / XÓA =====

    /**
     * Bật/tắt trạng thái tài khoản
     * Dùng POST form thay vì GET link để tránh CSRF và crawler kích hoạt
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái tài khoản thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Xóa mềm người dùng (đặt active = false, không xóa record)
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã vô hiệu hóa tài khoản người dùng!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

}
