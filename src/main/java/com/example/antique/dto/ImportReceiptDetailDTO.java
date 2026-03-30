package com.example.antique.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO cho chi tiết phiếu nhập kho.
 * Mỗi DTO = 1 dòng trong form nhập kho.
 */
@Getter
@Setter
@NoArgsConstructor
public class ImportReceiptDetailDTO {

    private Long id;
    
    // ID phiếu nhập (FK)
    private Long importReceiptId;
    
    // ID đồ cổ được nhập
    private Long antiqueId;
    
    // Tên đồ cổ (cho hiển thị, không cần lưu DB)
    private String antiqueName;
    
    // Ảnh chính (cho hiển thị trong form)
    private String antiqueImage;
    
    // Số lượng nhập
    private Integer soLuong = 1;
    
    // Đơn giá nhập
    private BigDecimal donGia;
    
    // Thành tiền = soLuong × donGia
    private BigDecimal thanhTien;
    
    private String ghiChu;
    
    /**
     * Tính thành tiền.
     */
    public void tinhThanhTien() {
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(BigDecimal.valueOf(this.soLuong));
        } else {
            this.thanhTien = BigDecimal.ZERO;
        }
    }
    
}
