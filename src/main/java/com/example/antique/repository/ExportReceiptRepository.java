package com.example.antique.repository;

import com.example.antique.entity.ExportReceipt;
import com.example.antique.entity.LyDoXuat;
import com.example.antique.entity.TrangThaiPhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho phiếu xuất kho.
 */
@Repository
public interface ExportReceiptRepository extends JpaRepository<ExportReceipt, Long> {

    Optional<ExportReceipt> findByMaPhieuXuat(String maPhieuXuat);

    boolean existsByMaPhieuXuat(String maPhieuXuat);

    List<ExportReceipt> findAllByOrderByCreatedAtDesc();

    List<ExportReceipt> findByTrangThai(TrangThaiPhieuXuat trangThai);

    List<ExportReceipt> findByLyDo(LyDoXuat lyDo);

    List<ExportReceipt> findByNgayXuatBetween(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(r.tongGiaTri), 0) FROM ExportReceipt r " +
           "WHERE MONTH(r.ngayXuat) = :thang AND YEAR(r.ngayXuat) = :nam " +
           "AND r.trangThai = 'XUAT_KHO'")
    java.math.BigDecimal tongGiaTriXuatTheoThangNam(
            @Param("thang") int thang, @Param("nam") int nam);

    @Query("SELECT COUNT(r) FROM ExportReceipt r " +
           "WHERE MONTH(r.ngayXuat) = :thang AND YEAR(r.ngayXuat) = :nam " +
           "AND r.trangThai = 'XUAT_KHO'")
    long demPhieuXuatTheoThangNam(
            @Param("thang") int thang, @Param("nam") int nam);

    @Query("SELECT COUNT(r) FROM ExportReceipt r " +
           "WHERE MONTH(r.createdAt) = :thang AND YEAR(r.createdAt) = :nam")
    long demPhieuXuatTrongThang(@Param("thang") int thang, @Param("nam") int nam);

}
