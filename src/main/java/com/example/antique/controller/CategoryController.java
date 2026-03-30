package com.example.antique.controller;

import com.example.antique.dto.CategoryDTO;
import com.example.antique.entity.Category;
import com.example.antique.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller quản lý danh mục loại đồ cổ.
 * Xem: cả ADMIN và NHAN_VIEN.
 * Tạo / Sửa / Xóa: chỉ ADMIN.
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ─── Danh sách ───────────────────────────────────────────────────────────────

    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Danh mục loại đồ cổ");
        return "category/list";
    }

    // ─── Tạo mới ─────────────────────────────────────────────────────────────────

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm danh mục mới");
        return "category/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("categoryDTO") CategoryDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Validate business logic (duplicate mã/tên)
        if (!bindingResult.hasFieldErrors("maLoai")
                && categoryService.existsByMaLoai(dto.getMaLoai(), null)) {
            bindingResult.rejectValue("maLoai", "duplicate", "Mã loại này đã tồn tại");
        }
        if (!bindingResult.hasFieldErrors("tenLoai")
                && categoryService.existsByTenLoai(dto.getTenLoai(), null)) {
            bindingResult.rejectValue("tenLoai", "duplicate", "Tên loại này đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm danh mục mới");
            return "category/form";
        }

        categoryService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Thêm danh mục \"" + dto.getTenLoai() + "\" thành công!");
        return "redirect:/categories";
    }

    // ─── Chỉnh sửa ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("categoryDTO", categoryService.toDTO(category));
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh sửa danh mục");
        return "category/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("categoryDTO") CategoryDTO dto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasFieldErrors("maLoai")
                && categoryService.existsByMaLoai(dto.getMaLoai(), id)) {
            bindingResult.rejectValue("maLoai", "duplicate", "Mã loại này đã tồn tại");
        }
        if (!bindingResult.hasFieldErrors("tenLoai")
                && categoryService.existsByTenLoai(dto.getTenLoai(), id)) {
            bindingResult.rejectValue("tenLoai", "duplicate", "Tên loại này đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh sửa danh mục");
            return "category/form";
        }

        categoryService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Cập nhật danh mục \"" + dto.getTenLoai() + "\" thành công!");
        return "redirect:/categories";
    }

    // ─── Xóa ─────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (IllegalStateException e) {
            // Danh mục đang có đồ cổ liên kết
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}
