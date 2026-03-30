package com.example.antique.dto;

import com.example.antique.entity.TrangThaiPhieuNhap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho phiếu nhập kho.
 * Dùng để transfer data giữa Controller <-> Service <-> View.
 */
@Getter
@Setter
@NoArgsConstructor
public class ImportReceiptDTO {

    private Long id;
    
    // Mã phiếu (sinh tự động, chỉ hiển thị)
    private String maPhieuNhap;
    
    // Ngày nhập (người dùng chọn, mặc định hôm nay)
    private LocalDate ngayNhap;
    
    // Nguồn cung cấp
    private String nguonGoc;
    
    // Tổng giá trị (tính tự động từ details)
    private BigDecimal tongGiaTri;
    
    private String ghiChu;
    
    private TrangThaiPhieuNhap trangThai;
    
    // ID người tạo (lấy từ Security Context)
    private Long userId;
    
    // Tên người tạo (cho hiển thị)
    private String userName;
    
    private LocalDateTime createdAt;
    
    /**
     * Danh sách chi tiết phiếu nhập.
     * Form sẽ cho phép thêm nhiều dòng (dynamic rows).
     */
    private List<ImportReceiptDetailDTO> details = new ArrayList<>();
    
    /**
     * Tính tổng giá trị từ danh sách chi tiết.
     */
    public void tinhTongGiaTri() {
        this.tongGiaTri = details.stream()
                .map(d -> d.getThanhTien() != null ? d.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
}
