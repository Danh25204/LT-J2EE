package com.example.antique.dto;

import com.example.antique.entity.LoaiHoatDong;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho lịch sử hoạt động kho.
 */
@Getter
@Setter
@NoArgsConstructor
public class WarehouseActivityDTO {

    private Long id;
    private LoaiHoatDong loaiHoatDong;
    private String moTa;
    private String thamChieu;        // Mã phiếu
    private Integer soLuongThayDoi;
    private BigDecimal giaTri;

    // Antique info (nullable)
    private Long antiqueId;
    private String tenDocCo;
    private String maDocCo;

    // User info
    private Long userId;
    private String userName;

    // Receipt links
    private Long importReceiptId;
    private Long exportReceiptId;

    private LocalDateTime createdAt;

    // Computed helpers for template
    public String getBadgeColor() {
        return loaiHoatDong != null ? loaiHoatDong.getBadgeColor() : "secondary";
    }

    public String getIcon() {
        return loaiHoatDong != null ? loaiHoatDong.getIcon() : "bi-question";
    }

    public String getLoaiMoTa() {
        return loaiHoatDong != null ? loaiHoatDong.getMoTa() : "";
    }
}
