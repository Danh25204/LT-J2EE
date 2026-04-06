package com.example.antique.dto;

import com.example.antique.entity.LyDoKiemTra;
import com.example.antique.entity.TrangThaiKiemTra;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO cho yêu cầu kiểm tra đồ cổ.
 */
@Getter
@Setter
@NoArgsConstructor
public class AntiqueInspectionDTO {

    private Long id;

    // Antique info
    private Long antiqueId;
    private String maDocCo;
    private String tenDocCo;
    private String anhChinh;
    private String categoryName;
    private Integer soLuongTon;

    // Inspection info
    private LyDoKiemTra lyDoKiemTra;
    private Integer soNgayTonKho;
    private TrangThaiKiemTra trangThai;
    private String moTa;
    private String ketQuaKiemTra;
    private Long exportReceiptId;

    // Người kiểm tra
    private Long nguoiKiemTraId;
    private String nguoiKiemTraName;

    private LocalDateTime ngayKiemTra;
    private LocalDateTime createdAt;

    // Helper methods for template
    public String getLyDoMoTa() {
        return lyDoKiemTra != null ? lyDoKiemTra.getMoTa() : "";
    }

    public String getLyDoBadgeColor() {
        return lyDoKiemTra != null ? lyDoKiemTra.getBadgeColor() : "secondary";
    }

    public String getLyDoIcon() {
        return lyDoKiemTra != null ? lyDoKiemTra.getIcon() : "bi-question";
    }

    public String getTrangThaiMoTa() {
        return trangThai != null ? trangThai.getMoTa() : "";
    }

    public String getTrangThaiBadgeColor() {
        return trangThai != null ? trangThai.getBadgeColor() : "secondary";
    }
}
