package com.example.antique.repository;

import com.example.antique.entity.ImportReceipt;
import com.example.antique.entity.TrangThaiPhieuNhap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho phiếu nhập kho.
 */
@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {

    Optional<ImportReceipt> findByMaPhieuNhap(String maPhieuNhap);

    boolean existsByMaPhieuNhap(String maPhieuNhap);

    // Tất cả phiếu, mới nhất trước
    List<ImportReceipt> findAllByOrderByCreatedAtDesc();

    // Theo trạng thái
    List<ImportReceipt> findByTrangThai(TrangThaiPhieuNhap trangThai);

    // Lọc theo khoảng ngày nhập
    List<ImportReceipt> findByNgayNhapBetween(LocalDate from, LocalDate to);

    /**
     * Tổng giá trị nhập kho trong một tháng/năm (dùng báo cáo Phase 8).
     */
    @Query("SELECT COALESCE(SUM(r.tongGiaTri), 0) FROM ImportReceipt r " +
           "WHERE MONTH(r.ngayNhap) = :thang AND YEAR(r.ngayNhap) = :nam " +
           "AND r.trangThai = 'NHAP_KHO'")
    java.math.BigDecimal tongGiaTriNhapTheoThangNam(
            @Param("thang") int thang, @Param("nam") int nam);

    /**
     * Đếm số phiếu nhập trong tháng (dùng cho dashboard).
     */
    @Query("SELECT COUNT(r) FROM ImportReceipt r " +
           "WHERE MONTH(r.ngayNhap) = :thang AND YEAR(r.ngayNhap) = :nam " +
           "AND r.trangThai = 'NHAP_KHO'")
    long demPhieuNhapTheoThangNam(
            @Param("thang") int thang, @Param("nam") int nam);

    /**
     * Sinh mã phiếu tiếp theo: đếm số phiếu trong tháng để tạo sequence.
     */
    @Query("SELECT COUNT(r) FROM ImportReceipt r " +
           "WHERE MONTH(r.createdAt) = :thang AND YEAR(r.createdAt) = :nam")
    long demPhieuNhapTrongThang(@Param("thang") int thang, @Param("nam") int nam);

    /**
     * Lấy mã phiếu nhập lớn nhất có prefix cho trước (tránh race condition).
     */
    @Query("SELECT MAX(r.maPhieuNhap) FROM ImportReceipt r WHERE r.maPhieuNhap LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxMaPhieuByPrefix(@Param("prefix") String prefix);

    /**
     * Truy vấn có phân trang và lọc theo ngày nhập.
     */
    @Query("SELECT r FROM ImportReceipt r WHERE " +
           "(:tuNgay IS NULL OR r.ngayNhap >= :tuNgay) AND " +
           "(:denNgay IS NULL OR r.ngayNhap <= :denNgay) " +
           "ORDER BY r.createdAt DESC")
    Page<ImportReceipt> findByDateFilter(
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable);

}
