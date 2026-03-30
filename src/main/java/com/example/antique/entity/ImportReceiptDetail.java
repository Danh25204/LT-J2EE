package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity chi tiết phiếu nhập kho.
 * Mỗi record = 1 dòng trong phiếu nhập: đồ cổ nào, số lượng bao nhiêu, giá bao nhiêu.
 */
@Entity
@Table(name = "import_receipt_details")
@Getter
@Setter
@NoArgsConstructor
public class ImportReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK về phiếu nhập (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_receipt_id", nullable = false)
    private ImportReceipt importReceipt;

    // FK về đồ cổ nào được nhập
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "antique_id", nullable = false)
    private Antique antique;

    // Số lượng nhập (mặc định 1 cho đồ cổ độc bản)
    @Column(name = "so_luong", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer soLuong = 1;

    // Đơn giá nhập cho đồ cổ này (có thể khác giaNhap trong Antique)
    @Column(name = "don_gia", precision = 15, scale = 2)
    private BigDecimal donGia;

    // Thành tiền = soLuong × donGia (tính tự động)
    @Column(name = "thanh_tien", precision = 15, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    /**
     * Tính thành tiền: soLuong × donGia.
     * Gọi trước khi save để đảm bảo thanhTien luôn chính xác.
     */
    public void tinhThanhTien() {
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(BigDecimal.valueOf(this.soLuong));
        } else {
            this.thanhTien = BigDecimal.ZERO;
        }
    }

}
