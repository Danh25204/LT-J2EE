package com.example.antique.controller;

import com.example.antique.dto.ExportReceiptDTO;
import com.example.antique.dto.ExportReceiptDetailDTO;
import com.example.antique.entity.LyDoXuat;
import com.example.antique.entity.User;
import com.example.antique.service.ExportReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý phiếu xuất kho.
 */
@Controller
@RequestMapping("/export-receipts")
@RequiredArgsConstructor
public class ExportReceiptController {

    private final ExportReceiptService exportReceiptService;

    /**
     * Danh sách phiếu xuất.
     * GET /export-receipts
     */
    @GetMapping
    public String list(Model model) {
        List<ExportReceiptDTO> receipts = exportReceiptService.findAll();
        model.addAttribute("receipts", receipts);
        model.addAttribute("countXuatKho", receipts.stream()
                .filter(r -> r.getTrangThai() != null && r.getTrangThai().name().equals("XUAT_KHO"))
                .count());
        model.addAttribute("countHuy", receipts.stream()
                .filter(r -> r.getTrangThai() != null && r.getTrangThai().name().equals("HUY"))
                .count());
        return "export-receipt/list";
    }

    /**
     * Xem chi tiết phiếu xuất.
     * GET /export-receipts/{id}
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ExportReceiptDTO receipt = exportReceiptService.findById(id);
        model.addAttribute("receipt", receipt);
        return "export-receipt/detail";
    }

    /**
     * Form tạo phiếu xuất mới.
     * GET /export-receipts/new
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        ExportReceiptDTO dto = new ExportReceiptDTO();
        dto.setNgayXuat(LocalDate.now());
        dto.setDetails(new ArrayList<>());

        model.addAttribute("exportReceiptDTO", dto);
        model.addAttribute("lyDoList", LyDoXuat.values());

        // Chỉ hiển thị đồ cổ còn tồn kho — dùng findAllWithAntique() có JOIN FETCH tránh LazyInitializationException
        model.addAttribute("inventories", exportReceiptService.findInventoriesWithStock());

        return "export-receipt/form";
    }

    /**
     * Lưu phiếu xuất mới.
     * POST /export-receipts
     */
    @PostMapping
    public String create(@ModelAttribute ExportReceiptDTO dto,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            User currentUser = exportReceiptService.findUserByUsername(userDetails.getUsername());
            dto.setUserId(currentUser.getId());

            if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Phải có ít nhất 1 đồ cổ trong phiếu xuất!");
                return "redirect:/export-receipts/new";
            }

            dto.getDetails().forEach(ExportReceiptDetailDTO::tinhThanhTien);
            dto.tinhTongGiaTri();

            ExportReceiptDTO saved = exportReceiptService.create(dto);

            redirectAttributes.addFlashAttribute("success",
                    "Tạo phiếu xuất thành công! Mã: " + saved.getMaPhieuXuat());
            return "redirect:/export-receipts/" + saved.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi tạo phiếu xuất: " + e.getMessage());
            return "redirect:/export-receipts/new";
        }
    }

    /**
     * Hủy phiếu xuất — hoàn lại tồn kho.
     * POST /export-receipts/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            exportReceiptService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy phiếu xuất và hoàn lại tồn kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/export-receipts/" + id;
    }
}
