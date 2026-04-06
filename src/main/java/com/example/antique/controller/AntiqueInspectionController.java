package com.example.antique.controller;

import com.example.antique.dto.AntiqueInspectionDTO;
import com.example.antique.entity.LyDoKiemTra;
import com.example.antique.entity.TrangThaiKiemTra;
import com.example.antique.service.AntiqueInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Controller quản lý yêu cầu kiểm tra đồ cổ.
 *
 * Hai nguồn kích hoạt:
 *  1. Scheduled job: đồ cổ tồn kho > 30 ngày không có hoạt động
 *  2. Trigger thủ công: hủy phiếu xuất CHO_MUON (trả hàng)
 */
@Controller
@RequestMapping("/antique-inspections")
@RequiredArgsConstructor
public class AntiqueInspectionController {

    private final AntiqueInspectionService antiqueInspectionService;

    // ─── Danh sách yêu cầu kiểm tra ──────────────────────────────────────────────

    @GetMapping
    public String list(
            @RequestParam(required = false) TrangThaiKiemTra trangThai,
            @RequestParam(required = false) LyDoKiemTra lyDo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<AntiqueInspectionDTO> inspections =
                antiqueInspectionService.findPaged(trangThai, lyDo, tuNgay, denNgay, page, size);

        model.addAttribute("inspections", inspections.getContent());
        model.addAttribute("totalPages", inspections.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", inspections.getTotalElements());
        model.addAttribute("pendingCount", antiqueInspectionService.countPending());

        // Bộ lọc
        model.addAttribute("trangThaiList", TrangThaiKiemTra.values());
        model.addAttribute("lyDoList", LyDoKiemTra.values());
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("selectedLyDo", lyDo);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        model.addAttribute("pageTitle", "Kiểm tra đồ cổ");
        return "antique-inspection/list";
    }

    // ─── Đánh dấu đã kiểm tra ────────────────────────────────────────────────────

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           @RequestParam(defaultValue = "") String ketQua,
                           RedirectAttributes redirectAttributes) {
        try {
            antiqueInspectionService.hoanThanhKiemTra(id, ketQua);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đánh dấu hoàn thành kiểm tra!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/antique-inspections";
    }

    // ─── Bỏ qua yêu cầu kiểm tra ─────────────────────────────────────────────────

    @PostMapping("/{id}/skip")
    public String skip(@PathVariable Long id,
                       RedirectAttributes redirectAttributes) {
        try {
            antiqueInspectionService.boQua(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã bỏ qua yêu cầu kiểm tra.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/antique-inspections";
    }
}
