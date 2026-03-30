package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity tồn kho - liên kết 1-1 với Antique.
 *
 * Thiết kế: mỗi đồ cổ có ĐÚNG 1 bản ghi tồn kho.
 * Khi tạo mới Antique → DataService tự tạo Inventory với soLuongTon = 0.
 * Khi nhập kho   → soLuongTon += soLuong từ phiếu nhập.
 * Khi xuất kho   → soLuongTon -= soLuong từ phiếu xuất.
 *
 * Lưu ý: không có bảng Warehouse vì hệ thống chỉ có 1 kho duy nhất.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ 1-1 với Antique.
     * @JoinColumn: bảng inventory giữ FK antique_id.
     * unique = true: đảm bảo 1 antique chỉ có 1 inventory record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "antique_id", unique = true, nullable = false)
    private Antique antique;

    // Số lượng hiện có trong kho (>=0 luôn)
    @Column(name = "so_luong_ton", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer soLuongTon = 0;

    // Vị trí lưu trữ trong kho: "Kệ A - Tầng 2 - Ô 3", "Tủ kính B", ...
    @Column(name = "vi_tri_luu_tru", length = 100)
    private String viTriLuuTru;

    // Ghi chú bổ sung về tình trạng lưu kho
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // Tự động cập nhật khi có nhập/xuất kho
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
