package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Yêu cầu kiểm tra tình trạng đồ cổ.
 *
 * Được tạo tự động khi:
 *   1. Đồ cổ tồn kho quá lâu không có hoạt động (scheduled job hàng ngày)
 *   2. Đồ cổ vừa được trả lại sau khi cho mượn (trigger khi hủy phiếu xuất CHO_MUON)
 *
 * Nhân viên nhận thông báo và ghi lại kết quả kiểm tra.
 */
@Entity
@Table(name = "antique_inspections",
       indexes = {
           @Index(name = "idx_inspection_antique", columnList = "antique_id"),
           @Index(name = "idx_inspection_trang_thai", columnList = "trang_thai"),
           @Index(name = "idx_inspection_created_at", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class AntiqueInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Đồ cổ cần kiểm tra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "antique_id", nullable = false)
    private Antique antique;

    // Lý do cần kiểm tra
    @Enumerated(EnumType.STRING)
    @Column(name = "ly_do_kiem_tra", nullable = false, length = 30)
    private LyDoKiemTra lyDoKiemTra;

    // Số ngày tồn kho (chỉ áp dụng cho TON_KHO_LAU)
    @Column(name = "so_ngay_ton_kho")
    private Integer soNgayTonKho;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiKiemTra trangThai = TrangThaiKiemTra.CHO_KIEM_TRA;

    // Ghi chú khi tạo yêu cầu (mô tả lý do cụ thể)
    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    // Kết quả kiểm tra (nhân viên điền sau khi kiểm tra)
    @Column(name = "ket_qua_kiem_tra", columnDefinition = "TEXT")
    private String ketQuaKiemTra;

    // Người thực hiện kiểm tra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_kiem_tra_id")
    private User nguoiKiemTra;

    // Thời điểm nhân viên hoàn tất kiểm tra
    @Column(name = "ngay_kiem_tra")
    private LocalDateTime ngayKiemTra;

    // Liên kết phiếu xuất gốc (áp dụng cho SAU_CHO_MUON)
    @Column(name = "export_receipt_id")
    private Long exportReceiptId;

    // Thời điểm tạo yêu cầu
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
