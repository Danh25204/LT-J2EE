package com.example.antique.controller;

import com.example.antique.dto.AntiqueDTO;
import com.example.antique.entity.Antique;
import com.example.antique.entity.Category;
import com.example.antique.entity.TinhTrang;
import com.example.antique.repository.CategoryRepository;
import com.example.antique.service.AntiqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller quản lý đồ cổ.
 * 
 * Phân quyền:
 * - ADMIN: Full quyền (CRUD)
 * - NHAN_VIEN_KHO: Xem, tạo, sửa (không xóa)
 * 
 * Routes:
 * - GET  /antiques           → Danh sách + tìm kiếm/lọc
 * - GET  /antiques/create    → Form tạo mới
 * - POST /antiques/create    → Xử lý tạo mớiGET  /antiques/{id}      → Chi tiết
 * - GET  /antiques/{id}/edit → Form sửa
 * - POST /antiques/{id}/edit → Xử lý sửa
 * - POST /antiques/{id}/delete → Xóa (chỉ ADMIN)
 */
@Controller
@RequestMapping("/antiques")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class AntiqueController {

    private final AntiqueService antiqueService;
    private final CategoryRepository categoryRepository;

    /**
     * Danh sách đồ cổ + tìm kiếm/lọc.
     * 
     * Query params:
     * - keyword: tìm theo tên hoặc mã
     * - categoryId: lọc theo danh mục
     * - tinhTrang: lọc theo tình trạng
     */
    @GetMapping
    public String listAntiques(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TinhTrang tinhTrang,
            Model model
    ) {
        log.info("→ GET /antiques (keyword={}, categoryId={}, tinhTrang={})",
                keyword, categoryId, tinhTrang);

        List<Antique> antiques;

        // Xử lý lọc/tìm kiếm
        if (keyword != null && !keyword.trim().isEmpty()) {
            antiques = antiqueService.search(keyword);
        } else if (categoryId != null) {
            antiques = antiqueService.findByCategory(categoryId);
        } else if (tinhTrang != null) {
            antiques = antiqueService.findByTinhTrang(tinhTrang);
        } else {
            antiques = antiqueService.findAll();
        }

        // Data cho filter dropdowns
        List<Category> categories = categoryRepository.findAll();
        TinhTrang[] tinhTrangValues = TinhTrang.values();

        model.addAttribute("antiques", antiques);
        model.addAttribute("categories", categories);
        model.addAttribute("tinhTrangValues", tinhTrangValues);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedTinhTrang", tinhTrang);

        return "antique/list";
    }

    /**
     * Form tạo đồ cổ mới.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.info("→ GET /antiques/create");

        AntiqueDTO dto = new AntiqueDTO();
        dto.setTinhTrang(TinhTrang.TOT); // Mặc định tình trạng TỐT
        dto.setSoLuongMacDinh(1); // Mặc định số lượng = 1

        List<Category> categories = categoryRepository.findAll();
        TinhTrang[] tinhTrangValues = TinhTrang.values();

        model.addAttribute("antiqueDTO", dto);
        model.addAttribute("categories", categories);
        model.addAttribute("tinhTrangValues", tinhTrangValues);
        model.addAttribute("isEdit", false); // Flag để template biết là create

        return "antique/form";
    }

    /**
     * Xử lý tạo đồ cổ mới.
     */
    @PostMapping("/create")
    public String createAntique(
            @Valid @ModelAttribute("antiqueDTO") AntiqueDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        log.info("→ POST /antiques/create: {}", dto.getTenDocCo());

        // Nếu có lỗi validation
        if (bindingResult.hasErrors()) {
            log.warn("✗ Validation errors: {}", bindingResult.getAllErrors());
            
            // Load lại data cho dropdowns
            List<Category> categories = categoryRepository.findAll();
            TinhTrang[] tinhTrangValues = TinhTrang.values();
            
            model.addAttribute("categories", categories);
            model.addAttribute("tinhTrangValues", tinhTrangValues);
            model.addAttribute("isEdit", false);
            
            return "antique/form";
        }

        try {
            Antique created = antiqueService.createAntique(dto);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "✓ Đã tạo đồ cổ: " + created.getMaDocCo() + " - " + created.getTenDocCo());
            
            return "redirect:/antiques";
            
        } catch (Exception e) {
            log.error("✗ Lỗi khi tạo đồ cổ", e);
            
            // Load lại data cho dropdowns
            List<Category> categories = categoryRepository.findAll();
            TinhTrang[] tinhTrangValues = TinhTrang.values();
            
            model.addAttribute("categories", categories);
            model.addAttribute("tinhTrangValues", tinhTrangValues);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "✗ Lỗi: " + e.getMessage());
            
            return "antique/form";
        }
    }

    /**
     * Xem chi tiết đồ cổ.
     */
    @GetMapping("/{id}")
    public String viewAntique(@PathVariable Long id, Model model) {
        log.info("→ GET /antiques/{}", id);

        try {
            Antique antique = antiqueService.findById(id);
            model.addAttribute("antique", antique);
            return "antique/detail";
            
        } catch (IllegalArgumentException e) {
            log.warn("✗ Không tìm thấy đồ cổ ID: {}", id);
            return "redirect:/antiques?error=notfound";
        }
    }

    /**
     * Form sửa đồ cổ.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("→ GET /antiques/{}/edit", id);

        try {
            Antique antique = antiqueService.findById(id);
            AntiqueDTO dto = antiqueService.toDTO(antique);

            List<Category> categories = categoryRepository.findAll();
            TinhTrang[] tinhTrangValues = TinhTrang.values();

            model.addAttribute("antiqueDTO", dto);
            model.addAttribute("categories", categories);
            model.addAttribute("tinhTrangValues", tinhTrangValues);
            model.addAttribute("isEdit", true); // Flag để template biết là edit

            return "antique/form";
            
        } catch (IllegalArgumentException e) {
            log.warn("✗ Không tìm thấy đồ cổ ID: {}", id);
            return "redirect:/antiques?error=notfound";
        }
    }

    /**
     * Xử lý sửa đồ cổ.
     */
    @PostMapping("/{id}/edit")
    public String updateAntique(
            @PathVariable Long id,
            @Valid @ModelAttribute("antiqueDTO") AntiqueDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        log.info("→ POST /antiques/{}/edit", id);

        // Nếu có lỗi validation
        if (bindingResult.hasErrors()) {
            log.warn("✗ Validation errors: {}", bindingResult.getAllErrors());
            
            // Load lại data cho dropdowns
            List<Category> categories = categoryRepository.findAll();
            TinhTrang[] tinhTrangValues = TinhTrang.values();
            
            model.addAttribute("categories", categories);
            model.addAttribute("tinhTrangValues", tinhTrangValues);
            model.addAttribute("isEdit", true);
            
            return "antique/form";
        }

        try {
            Antique updated = antiqueService.updateAntique(id, dto);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "✓ Đã cập nhật đồ cổ: " + updated.getMaDocCo());
            
            return "redirect:/antiques";
            
        } catch (Exception e) {
            log.error("✗ Lỗi khi cập nhật đồ cổ ID: {}", id, e);
            
            // Load lại data cho dropdowns
            List<Category> categories = categoryRepository.findAll();
            TinhTrang[] tinhTrangValues = TinhTrang.values();
            
            model.addAttribute("categories", categories);
            model.addAttribute("tinhTrangValues", tinhTrangValues);
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", "✗ Lỗi: " + e.getMessage());
            
            return "antique/form";
        }
    }

    /**
     * Xóa đồ cổ (chỉ ADMIN).
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAntique(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        log.info("→ POST /antiques/{}/delete", id);

        try {
            Antique antique = antiqueService.findById(id);
            String maDocCo = antique.getMaDocCo();
            
            antiqueService.deleteAntique(id);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "✓ Đã xóa đồ cổ: " + maDocCo);
            
            return "redirect:/antiques";
            
        } catch (IllegalArgumentException e) {
            log.warn("✗ Không tìm thấy đồ cổ ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "✗ Không tìm thấy đồ cổ!");
            return "redirect:/antiques";
            
        } catch (IllegalStateException e) {
            // Đồ cổ đã có lịch sử nhập/xuất → không cho xóa
            log.warn("✗ Không thể xóa đồ cổ ID: {} - {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "✗ " + e.getMessage());
            return "redirect:/antiques";
            
        } catch (Exception e) {
            log.error("✗ Lỗi khi xóa đồ cổ ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "✗ Lỗi khi xóa đồ cổ!");
            return "redirect:/antiques";
        }
    }
}
