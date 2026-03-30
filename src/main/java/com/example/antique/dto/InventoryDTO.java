package com.example.antique.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin tồn kho.
 * Dùng để truyền dữ liệu từ Service → Controller → View.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    
    // Inventory info
    private Long inventoryId;
    private Integer soLuongTon;
    private String viTriLuuTru;
    private String ghiChu;
    private LocalDateTime updatedAt;
    
    // Antique info (để hiển thị trong list)
    private Long antiqueId;
    private String maDocCo;
    private String tenDocCo;
    private String anhChinh;
    private String categoryName;
    private Integer soLuongMacDinh;
    
    // Computed fields
    private String statusBadge; // "TOT" (đủ), "THAP" (< macDinh), "HET" (= 0)
    
    /**
     * Tính badge trạng thái dựa vào soLuongTon vs soLuongMacDinh.
     */
    public String getStatusBadge() {
        if (soLuongTon == null || soLuongTon == 0) {
            return "HET";
        }
        if (soLuongMacDinh != null && soLuongTon < soLuongMacDinh) {
            return "THAP";
        }
        return "TOT";
    }
    
    public String getStatusClass() {
        return switch (getStatusBadge()) {
            case "HET" -> "danger";
            case "THAP" -> "warning";
            default -> "success";
        };
    }
    
    public String getStatusText() {
        return switch (getStatusBadge()) {
            case "HET" -> "Hết hàng";
            case "THAP" -> "Sắp hết";
            default -> "Còn hàng";
        };
    }
}
