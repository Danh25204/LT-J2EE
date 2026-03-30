package com.example.antique.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity chi tiết phiếu xuất kho.
 */
@Entity
@Table(name = "export_receipt_details")
@Getter
@Setter
@NoArgsConstructor
public class ExportReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK về phiếu xuất
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "export_receipt_id", nullable = false)
    private ExportReceipt exportReceipt;

    // FK về đồ cổ nào được xuất
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "antique_id", nullable = false)
    private Antique antique;

    // Số lượng xuất (phải <= soLuongTon hiện tại trong Inventory)
    @Column(name = "so_luong", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer soLuong = 1;

    // Đơn giá xuất thực tế (giá bán, giá thỏa thuận,...)
    @Column(name = "don_gia", precision = 15, scale = 2)
    private BigDecimal donGia;

    // Thành tiền = soLuong × donGia
    @Column(name = "thanh_tien", precision = 15, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    /**
     * Tính thành tiền tự động.
     */
    public void tinhThanhTien() {
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(BigDecimal.valueOf(this.soLuong));
        } else {
            this.thanhTien = BigDecimal.ZERO;
        }
    }

}
