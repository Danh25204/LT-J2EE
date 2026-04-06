package com.example.antique.controller;

import com.example.antique.dto.WarehouseActivityDTO;
import com.example.antique.entity.LoaiHoatDong;
import com.example.antique.service.WarehouseActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Controller xem lịch sử hoạt động kho.
 * Chỉ đọc — không có chức năng tạo/sửa/xóa.
 */
@Controller
@RequestMapping("/warehouse-activities")
@RequiredArgsConstructor
public class WarehouseActivityController {

    private final WarehouseActivityService warehouseActivityService;

    /**
     * Danh sách lịch sử có lọc + phân trang.
     * GET /warehouse-activities
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) LoaiHoatDong loai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<WarehouseActivityDTO> pagedResult =
                warehouseActivityService.findPaged(loai, tuNgay, denNgay, keyword, page, size);

        model.addAttribute("activities", pagedResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedResult.getTotalPages());
        model.addAttribute("totalElements", pagedResult.getTotalElements());
        model.addAttribute("pageSize", size);

        // Filter params (để giữ trạng thái form)
        model.addAttribute("loai", loai);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("keyword", keyword);

        // Danh sách loại để dropdown lọc
        model.addAttribute("loaiList", LoaiHoatDong.values());

        return "warehouse-activity/list";
    }
}
