package com.example.antique.controller;

import com.example.antique.dto.ImportReceiptDTO;
import com.example.antique.dto.ImportReceiptDetailDTO;
import com.example.antique.entity.Antique;
import com.example.antique.entity.User;
import com.example.antique.service.ImportReceiptService;
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
 * Controller xử lý phiếu nhập kho.
 */
@Controller
@RequestMapping("/import-receipts")
@RequiredArgsConstructor
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;

    /**
     * Danh sách phiếu nhập.
     * GET /import-receipts
     */
    @GetMapping
    public String list(Model model) {
        List<ImportReceiptDTO> receipts = importReceiptService.findAll();
        model.addAttribute("receipts", receipts);
        model.addAttribute("countNhapKho", receipts.stream()
                .filter(r -> r.getTrangThai() != null && r.getTrangThai().name().equals("NHAP_KHO"))
                .count());
        model.addAttribute("countHuy", receipts.stream()
                .filter(r -> r.getTrangThai() != null && r.getTrangThai().name().equals("HUY"))
                .count());
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

}
