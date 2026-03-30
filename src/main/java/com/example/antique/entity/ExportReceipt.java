package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity phiếu xuất kho.
 *
 * Điểm khác biệt so với nhập kho:
 *   - Có thuộc tính lyDo (lý do xuất: Bán, Cho mượn, Điều chuyển, Khác)
 *   - Có nguoiNhan (người nhận/mua)
 *   - Khi tạo: phải kiểm tra tồn kho ĐỦ trước khi xuất (InsufficientStockException)
 *   - Khi tạo: Inventory.soLuongTon -= detail.soLuong
 *
 * Khi HỦY phiếu xuất: phải HOÀN LẠI tồn kho (soLuongTon += detail.soLuong).
 */
@Entity
@Table(name = "export_receipts")
@Getter
@Setter
@NoArgsConstructor
public class ExportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã phiếu xuất: PX-YYYYMM-001 (sinh tự động trong service)
    @Column(name = "ma_phieu_xuat", unique = true, nullable = false, length = 30)
    private String maPhieuXuat;

    // Ngày xuất kho thực tế
    @Column(name = "ngay_xuat", nullable = false)
    private LocalDate ngayXuat;

    // Lý do xuất kho (enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "ly_do", nullable = false, length = 20)
    private LyDoXuat lyDo;

    // Tên người/đơn vị nhận hàng
    @Column(name = "nguoi_nhan", length = 200)
    private String nguoiNhan;

    // Tổng giá trị xuất kho
    @Column(name = "tong_gia_tri", precision = 15, scale = 2)
    private BigDecimal tongGiaTri = BigDecimal.ZERO;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // Trạng thái: XUAT_KHO (mặc định) hoặc HUY
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiPhieuXuat trangThai = TrangThaiPhieuXuat.XUAT_KHO;

    // Người tạo phiếu
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "exportReceipt", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExportReceiptDetail> details = new ArrayList<>();

    /**
     * Tính lại tổng giá trị từ danh sách chi tiết.
     */
    public void tinhLaiTongGiaTri() {
        this.tongGiaTri = details.stream()
                .map(d -> d.getThanhTien() != null ? d.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
