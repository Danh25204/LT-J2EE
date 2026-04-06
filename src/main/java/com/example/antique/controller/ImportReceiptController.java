package com.example.antique.controller;

import com.example.antique.dto.ImportReceiptDTO;
import com.example.antique.dto.ImportReceiptDetailDTO;
import com.example.antique.entity.Antique;
import com.example.antique.entity.User;
import com.example.antique.service.ImportReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
 * Controller xử lý phiếu nhập kho.
 */
@Controller
@RequestMapping("/import-receipts")
@RequiredArgsConstructor
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;

    /**
     * Danh sách phiếu nhập có lọc theo ngày và phân trang.
     * GET /import-receipts
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<ImportReceiptDTO> pagedResult = importReceiptService.findAllPaged(tuNgay, denNgay, page, size);
        model.addAttribute("receipts", pagedResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedResult.getTotalPages());
        model.addAttribute("totalElements", pagedResult.getTotalElements());
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("pageSize", size);
        return "import-receipt/list";
    }

    /**
     * Xem chi tiết phiếu nhập.
     * GET /import-receipts/{id}
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ImportReceiptDTO receipt = importReceiptService.findById(id);
        model.addAttribute("receipt", receipt);
        model.addAttribute("isDetail", true);
        return "import-receipt/detail";
    }

    /**
     * Form tạo phiếu nhập mới.
     * GET /import-receipts/new
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        ImportReceiptDTO dto = new ImportReceiptDTO();
        dto.setNgayNhap(LocalDate.now()); // Mặc định là hôm nay
        dto.setDetails(new ArrayList<>()); // Khởi tạo list rỗng
        
        model.addAttribute("importReceiptDTO", dto);
        model.addAttribute("isEdit", false);
        
        // Danh sách đồ cổ để chọn
        List<Antique> antiques = importReceiptService.findAllAntiques();
        model.addAttribute("antiques", antiques);
        
        return "import-receipt/form";
    }

    /**
     * Lưu phiếu nhập mới.
     * POST /import-receipts
     */
    @PostMapping
    public String create(@ModelAttribute ImportReceiptDTO dto,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        try {
            // Lấy user từ database theo username
            User currentUser = importReceiptService.findUserByUsername(userDetails.getUsername());
            dto.setUserId(currentUser.getId());
            
            // Kiểm tra có chi tiết nào không
            if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Phải có ít nhất 1 đồ cổ trong phiếu nhập!");
                return "redirect:/import-receipts/new";
            }
            
            // Tính thành tiền cho từng detail
            dto.getDetails().forEach(ImportReceiptDetailDTO::tinhThanhTien);
            
            // Tính tổng giá trị
            dto.tinhTongGiaTri();
            
            // Lưu
            ImportReceiptDTO saved = importReceiptService.create(dto);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Tạo phiếu nhập thành công! Mã: " + saved.getMaPhieuNhap());
            return "redirect:/import-receipts/" + saved.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Lỗi khi tạo phiếu nhập: " + e.getMessage());
            return "redirect:/import-receipts/new";
        }
    }

    /**
     * Hủy phiếu nhập.
     * POST /import-receipts/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            importReceiptService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy phiếu nhập!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/import-receipts/" + id;
    }

    /**
     * Form sửa thông tin phiếu nhập (header-only).
     * GET /import-receipts/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ImportReceiptDTO receipt = importReceiptService.findById(id);
        model.addAttribute("receipt", receipt);
        return "import-receipt/edit";
    }

    /**
     * Lưu chỉnh sửa thông tin phiếu nhập.
     * POST /import-receipts/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute ImportReceiptDTO dto,
                         RedirectAttributes redirectAttributes) {
        try {
            importReceiptService.updateHeader(id, dto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật phiếu nhập thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/import-receipts/" + id;
    }

}
