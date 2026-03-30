package com.example.antique.controller;

import com.example.antique.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

/**
 * Controller trang Báo cáo - thống kê tổng hợp.
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String index(Model model) {
        LocalDate now = LocalDate.now();

        // Tổng quan kho
        model.addAttribute("totalAntiques", reportService.countTotalAntiques());
        model.addAttribute("totalAntiquesConKho", reportService.countTotalAntiquesConTrongKho());
        model.addAttribute("tongSoLuongTon", reportService.getTongSoLuongTon());
        model.addAttribute("tongGiaTriKho", reportService.getTongGiaTriKho());

        // Tháng hiện tại
        model.addAttribute("thangHienTai", String.format("%02d/%d", now.getMonthValue(), now.getYear()));
        model.addAttribute("monthlyImports", reportService.countMonthlyImports());
        model.addAttribute("monthlyExports", reportService.countMonthlyExports());
        model.addAttribute("tongGiaTriNhapThang", reportService.getTongGiaTriNhapThang());
        model.addAttribute("tongGiaTriXuatThang", reportService.getTongGiaTriXuatThang());

        // Đồ cổ theo danh mục
        model.addAttribute("countByCategory", reportService.getCountByCategory());

        // 6 tháng gần nhất
        model.addAttribute("last6Months", reportService.getLast6MonthsStats());

        return "reports/index";
    }
}
