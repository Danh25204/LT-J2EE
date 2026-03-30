package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đồ cổ - BẢNG TRUNG TÂM của toàn bộ hệ thống.
 *
 * Đặc thù đồ cổ:
 *   - soLuongMacDinh = 1 (đồ cổ thường là độc bản)
 *   - namSanXuat có thể null (không rõ năm)
 *   - Liên kết 1-1 với Inventory (thông qua @OneToOne trong Inventory)
 *
 * Quan hệ:
 *   - Many-to-One với Category (nhiều đồ cổ thuộc 1 loại)
 *   - One-to-One ngược với Inventory
 *   - One-to-Many với ImportReceiptDetail, ExportReceiptDetail
 */
@Entity
@Table(name = "antiques")
@Getter
@Setter
@NoArgsConstructor
public class Antique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã định danh duy nhất: DC-YYYY-001 (sinh tự động trong AntiqueService)
    @Column(name = "ma_do_co", unique = true, nullable = false, length = 30)
    private String maDocCo;

    // Tên gọi đồ cổ
    @Column(name = "ten_do_co", nullable = false, length = 200)
    private String tenDocCo;

    /**
     * Quan hệ Many-to-One: nhiều đồ cổ thuộc 1 danh mục loại.
     * @JoinColumn: cột FK trong bảng antiques (category_id)
     * FetchType.EAGER: load category ngay khi load antique
     *   → cần thiết vì hầu hết màn hình đều hiển thị tên loại.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Năm sản xuất/chế tác ước tính (null nếu không rõ)
    @Column(name = "nam_san_xuat")
    private Integer namSanXuat;

    // Triều đại hoặc thời kỳ lịch sử: "Nhà Nguyễn", "Triều Lý", "Thế kỷ XVIII",...
    @Column(name = "trieu_dai", length = 100)
    private String trieuDai;

    // Xuất xứ / nguồn gốc: "Hà Nội", "Huế", "Trung Quốc",...
    @Column(name = "xuat_xu", length = 100)
    private String xuatXu;

    // Chất liệu: "Gốm men ngọc", "Đồng thau", "Gỗ trắc",...
    @Column(name = "chat_lieu", length = 100)
    private String chatLieu;

    // Kích thước: "Cao 25cm, Đường kính 15cm"
    @Column(name = "kich_thuoc", length = 100)
    private String kichThuoc;

    /**
     * Tình trạng hiện vật.
     * @Enumerated(EnumType.STRING): lưu tên enum ("TOT", "KHA",...) vào DB
     *   → Dễ đọc SQL, không bị lỗi khi thêm enum mới vào giữa.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tinh_trang", nullable = false, length = 20)
    private TinhTrang tinhTrang = TinhTrang.TOT;

    // Mô tả lịch sử, đặc điểm chi tiết của hiện vật
    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    // Giá nhập kho - giá vốn thực tế
    // DECIMAL(15,2): tối đa 15 chữ số, 2 số thập phân → hỗ trợ đồ cổ hàng tỷ VNĐ
    @Column(name = "gia_nhap", precision = 15, scale = 2)
    private BigDecimal giaNhap;

    // Giá bán dự kiến để tham khảo
    @Column(name = "gia_ban_du_kien", precision = 15, scale = 2)
    private BigDecimal giaBanDuKien;

    // Đường dẫn ảnh chính (tên file: uuid.jpg)
    // URL đầy đủ sẽ được build trong template: /uploads/{anhChinh}
    @Column(name = "anh_chinh", length = 255)
    private String anhChinh;

    /**
     * Số lượng mặc định khi nhập kho.
     * Hầu hết đồ cổ là độc bản → mặc định = 1.
     * Vẫn cho phép > 1 (bộ đồ, cặp bình,...).
     */
    @Column(name = "so_luong_mac_dinh", columnDefinition = "INT DEFAULT 1")
    private Integer soLuongMacDinh = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Tự động cập nhật khi entity được sửa
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Quan hệ 1-1 ngược với Inventory.
     * mappedBy = "antique": chỉ Inventory mới giữ FK (antique_id).
     * cascade ALL: tạo Antique → tự tạo Inventory; xóa Antique → xóa Inventory.
     * orphanRemoval = true: nếu antique không còn inventory nào → tự xóa inventory.
     */
    @OneToOne(mappedBy = "antique", cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

}
