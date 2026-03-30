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
 * Entity phiếu nhập kho.
 * Mỗi phiếu nhập gồm:
 *   - Thông tin phiếu (mã, ngày, nguồn gốc, người tạo)
 *   - Danh sách chi tiết (ImportReceiptDetail): từng đồ cổ nhập vào
 *
 * Khi tạo phiếu nhập:
 *   @Transactional trong ImportReceiptService:
 *   1. Lưu ImportReceipt
 *   2. Lưu từng ImportReceiptDetail
 *   3. Với mỗi detail: Inventory.soLuongTon += detail.soLuong
 *   Nếu bất kỳ bước nào lỗi → ROLLBACK toàn bộ.
 */
@Entity
@Table(name = "import_receipts")
@Getter
@Setter
@NoArgsConstructor
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã phiếu nhập: PN-YYYYMM-001 (sinh tự động trong service)
    @Column(name = "ma_phieu_nhap", unique = true, nullable = false, length = 30)
    private String maPhieuNhap;

    // Ngày nhập kho thực tế
    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate ngayNhap;

    // Nguồn cung cấp: tên người bán, đấu giá, thu mua,...
    @Column(name = "nguon_goc", length = 200)
    private String nguonGoc;

    // Tổng giá trị phiếu nhập (= tổng thanhTien của tất cả chi tiết)
    // Được tính lại khi thêm/sửa chi tiết
    @Column(name = "tong_gia_tri", precision = 15, scale = 2)
    private BigDecimal tongGiaTri = BigDecimal.ZERO;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // Trạng thái: NHAP_KHO (mặc định khi tạo) hoặc HUY
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiPhieuNhap trangThai = TrangThaiPhieuNhap.NHAP_KHO;

    // Người tạo phiếu
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Danh sách chi tiết phiếu nhập.
     * cascade ALL + orphanRemoval: khi xóa phiếu → tự xóa tất cả chi tiết.
     */
    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImportReceiptDetail> details = new ArrayList<>();

    /**
     * Tính lại tổng giá trị từ danh sách chi tiết.
     * Gọi method này trước khi save phiếu.
     */
    public void tinhLaiTongGiaTri() {
        this.tongGiaTri = details.stream()
                .map(d -> d.getThanhTien() != null ? d.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
