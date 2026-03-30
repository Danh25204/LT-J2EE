package com.example.antique.dto;

import com.example.antique.entity.LyDoXuat;
import com.example.antique.entity.TrangThaiPhieuXuat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExportReceiptDTO {

    private Long id;
    private String maPhieuXuat;
    private LocalDate ngayXuat;
    private LyDoXuat lyDo;
    private String nguoiNhan;
    private BigDecimal tongGiaTri;
    private String ghiChu;
    private TrangThaiPhieuXuat trangThai;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private List<ExportReceiptDetailDTO> details = new ArrayList<>();

    public void tinhTongGiaTri() {
        this.tongGiaTri = details.stream()
                .map(d -> d.getThanhTien() != null ? d.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
