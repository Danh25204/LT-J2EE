package com.example.antique.controller;

import com.example.antique.dto.InventoryDTO;
import com.example.antique.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller cho Inventory Management.
 * 
 * Routes:
 * - GET  /inventory       → Danh sách tồn kho
 * - GET  /inventory/{id}  → Chi tiết 1 item (modal hoặc page riêng)
 * - POST /inventory/{id}  → Cập nhật vị trí/ghi chú
 */
@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Danh sách tồn kho (tất cả đồ cổ).
     */
    @GetMapping
    public String listInventory(
            @RequestParam(required = false) String filter,
            Model model) {
        
        log.info("→ GET /inventory (filter={})", filter);
        
        List<InventoryDTO> inventories;
        
        if ("low".equals(filter)) {
            // Chỉ hiển thị sắp hết/hết hàng
            inventories = inventoryService.getLowStockItems();
            model.addAttribute("filterActive", "low");
        } else {
            // Tất cả
            inventories = inventoryService.getAllInventory();
            model.addAttribute("filterActive", "all");
        }
        
        model.addAttribute("inventories", inventories);
        
        // Thống kê
        long totalItems = inventories.size();
        long outOfStock = inventories.stream().filter(i -> "HET".equals(i.getStatusBadge())).count();
        long lowStock = inventories.stream().filter(i -> "THAP".equals(i.getStatusBadge())).count();
        
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("outOfStock", outOfStock);
        model.addAttribute("lowStock", lowStock);
        
        return "inventory/list";
    }

    /**
     * Cập nhật vị trí lưu trữ và ghi chú.
     * Gọi qua AJAX hoặc form submit.
     */
    @PostMapping("/{id}/update")
    public String updateInventory(
            @PathVariable Long id,
            @RequestParam(required = false) String viTriLuuTru,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {
        
        log.info("→ POST /inventory/{}/update: viTri={}, ghiChu={}", id, viTriLuuTru, ghiChu);
        
        try {
            inventoryService.updateLocationAndNote(id, viTriLuuTru, ghiChu);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin tồn kho!");
        } catch (Exception e) {
            log.error("✗ Lỗi cập nhật inventory", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/inventory";
    }
}
