package com.example.antique.repository;

import com.example.antique.entity.LoaiHoatDong;
import com.example.antique.entity.WarehouseActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WarehouseActivityRepository extends JpaRepository<WarehouseActivity, Long> {

    /**
     * Lấy lịch sử có lọc theo loại hoạt động và khoảng thời gian.
     * Tất cả tham số đều nullable (không lọc khi null).
     */
    @Query(value = "SELECT a FROM WarehouseActivity a " +
            "LEFT JOIN FETCH a.user " +
            "LEFT JOIN FETCH a.antique " +
            "WHERE (:loai IS NULL OR a.loaiHoatDong = :loai) " +
            "AND (:tuNgay IS NULL OR a.createdAt >= :tuNgay) " +
            "AND (:denNgay IS NULL OR a.createdAt <= :denNgay) " +
            "AND (:keyword IS NULL OR a.moTa LIKE %:keyword% OR a.thamChieu LIKE %:keyword%) " +
            "ORDER BY a.createdAt DESC",
        countQuery = "SELECT COUNT(a) FROM WarehouseActivity a " +
            "WHERE (:loai IS NULL OR a.loaiHoatDong = :loai) " +
            "AND (:tuNgay IS NULL OR a.createdAt >= :tuNgay) " +
            "AND (:denNgay IS NULL OR a.createdAt <= :denNgay) " +
            "AND (:keyword IS NULL OR a.moTa LIKE %:keyword% OR a.thamChieu LIKE %:keyword%)")
    Page<WarehouseActivity> findByFilter(
            @Param("loai") LoaiHoatDong loai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            @Param("keyword") String keyword,
            Pageable pageable);
}
