package com.example.antique.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ExportReceiptDetailDTO {

    private Long id;
    private Long exportReceiptId;
    private Long antiqueId;
    private String antiqueName;
    private String antiqueImage;
    private Integer soLuong = 1;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String ghiChu;
    private Integer soLuongTon; // tồn kho hiện tại, để hiển thị trong form

    public void tinhThanhTien() {
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(BigDecimal.valueOf(this.soLuong));
        } else {
            this.thanhTien = BigDecimal.ZERO;
        }
    }
}
