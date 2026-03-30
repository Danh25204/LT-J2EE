package com.example.antique.controller;

import com.example.antique.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller cho trang Dashboard (trang chính sau khi đăng nhập).
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalAntiques", reportService.countTotalAntiques());
        model.addAttribute("monthlyImports", reportService.countMonthlyImports());
        model.addAttribute("monthlyExports", reportService.countMonthlyExports());
        model.addAttribute("totalValue", reportService.getTongGiaTriKho());
        return "dashboard/index";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }
}
