package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử hoạt động kho.
 *
 * Mỗi khi nhập kho / xuất kho / hủy phiếu, hệ thống tự động
 * ghi 1 bản ghi vào bảng này để theo dõi toàn bộ sự kiện.
 */
@Entity
@Table(name = "warehouse_activities",
       indexes = {
           @Index(name = "idx_wa_created_at", columnList = "created_at"),
           @Index(name = "idx_wa_loai", columnList = "loai_hoat_dong"),
           @Index(name = "idx_wa_antique", columnList = "antique_id")
       })
@Getter
@Setter
@NoArgsConstructor
public class WarehouseActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Loại hoạt động: NHAP_KHO, XUAT_KHO, HUY_NHAP, HUY_XUAT
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_hoat_dong", nullable = false, length = 20)
    private LoaiHoatDong loaiHoatDong;

    // Mô tả ngắn gọn sự kiện: "Nhập 5 chiếc Bình gốm Thanh Hoa theo PN-202604-001"
    @Column(name = "mo_ta", nullable = false, columnDefinition = "TEXT")
    private String moTa;

    // Tham chiếu đến mã phiếu (PN-... hoặc PX-...)
    @Column(name = "tham_chieu", length = 50)
    private String thamChieu;

    // Số lượng thay đổi (dương = nhập, âm = xuất)
    @Column(name = "so_luong_thay_doi")
    private Integer soLuongThayDoi;

    // Giá trị phiếu (tổng giá trị nhập/xuất)
    @Column(name = "gia_tri", precision = 15, scale = 2)
    private BigDecimal giaTri;

    // Đồ cổ liên quan (nullable — phiếu có thể có nhiều đồ cổ, đây là tóm tắt)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "antique_id")
    private Antique antique;

    // Người thực hiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ID phiếu nhập/xuất liên quan (để có thể link sang chi tiết)
    @Column(name = "import_receipt_id")
    private Long importReceiptId;

    @Column(name = "export_receipt_id")
    private Long exportReceiptId;

    // Thời điểm xảy ra (tự động)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
