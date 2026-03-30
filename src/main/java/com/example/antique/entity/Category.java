package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Danh mục loại đồ cổ.
 * Ví dụ: Gốm sứ (GOM), Đồ gỗ (GO), Kim loại (KIM), Tranh ảnh (TRA),...
 *
 * Quan hệ: 1 Category có nhiều Antique (OneToMany)
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã loại rút gọn: GOM, GO, KIM, TRA,... (tối đa 20 ký tự, phải unique)
    @Column(name = "ma_loai", unique = true, nullable = false, length = 20)
    private String maLoai;

    // Tên loại đồ cổ đầy đủ
    @Column(name = "ten_loai", nullable = false, length = 100)
    private String tenLoai;

    // Mô tả loại đồ cổ (có thể null)
    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    // Tự động điền thời điểm tạo, không cho phép update
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Quan hệ ngược One-to-Many: 1 category → nhiều antique.
     * mappedBy = "category": chỉ đây là phía KHÔNG giữ foreign key.
     * Phía Antique.java mới là phía giữ FK (category_id).
     *
     * cascade = PERSIST, MERGE: khi save category thì cũng save antique liên kết.
     * KHÔNG dùng cascade ALL (tránh xóa toàn bộ đồ cổ khi xóa category).
     * FetchType.LAZY: chỉ load danh sách antique khi thực sự gọi getAntiques()
     *   → tránh N+1 query performance issue.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Antique> antiques = new ArrayList<>();

    /**
     * Đếm số lượng đồ cổ trong danh mục này (dùng trên UI)
     */
    public int getSoLuongDocCo() {
        return antiques.size();
    }

}
