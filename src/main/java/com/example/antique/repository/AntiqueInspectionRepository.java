package com.example.antique.repository;

import com.example.antique.entity.AntiqueInspection;
import com.example.antique.entity.LyDoKiemTra;
import com.example.antique.entity.TrangThaiKiemTra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AntiqueInspectionRepository extends JpaRepository<AntiqueInspection, Long> {

    /**
     * Đếm số yêu cầu đang chờ kiểm tra — dùng cho badge dashboard.
     */
    long countByTrangThai(TrangThaiKiemTra trangThai);

    /**
     * Kiểm tra xem đồ cổ đã có yêu cầu chờ xử lý chưa (tránh tạo trùng).
     */
    boolean existsByAntiqueIdAndLyDoKiemTraAndTrangThai(
            Long antiqueId, LyDoKiemTra lyDoKiemTra, TrangThaiKiemTra trangThai);

    /**
     * Lấy danh sách đồ cổ cần kiểm tra (pending) — cho sidebar badge.
     */
    List<AntiqueInspection> findByTrangThaiOrderByCreatedAtDesc(TrangThaiKiemTra trangThai);

    /**
     * Query có lọc cho trang danh sách.
     * countQuery riêng vì Spring Data không thể tự deriving count
     * khi query gốc có JOIN FETCH + CASE trong ORDER BY.
     */
    @Query(value = "SELECT i FROM AntiqueInspection i " +
           "LEFT JOIN FETCH i.antique a " +
           "LEFT JOIN FETCH a.category " +
           "LEFT JOIN FETCH i.nguoiKiemTra " +
           "WHERE (:trangThai IS NULL OR i.trangThai = :trangThai) " +
           "AND (:lyDo IS NULL OR i.lyDoKiemTra = :lyDo) " +
           "AND (:tuNgay IS NULL OR i.createdAt >= :tuNgay) " +
           "AND (:denNgay IS NULL OR i.createdAt <= :denNgay) " +
           "ORDER BY " +
           "  CASE WHEN i.trangThai = com.example.antique.entity.TrangThaiKiemTra.CHO_KIEM_TRA THEN 0 ELSE 1 END, " +
           "  i.createdAt DESC",
           countQuery = "SELECT COUNT(i) FROM AntiqueInspection i " +
           "WHERE (:trangThai IS NULL OR i.trangThai = :trangThai) " +
           "AND (:lyDo IS NULL OR i.lyDoKiemTra = :lyDo) " +
           "AND (:tuNgay IS NULL OR i.createdAt >= :tuNgay) " +
           "AND (:denNgay IS NULL OR i.createdAt <= :denNgay)")
    Page<AntiqueInspection> findByFilter(
            @Param("trangThai") TrangThaiKiemTra trangThai,
            @Param("lyDo") LyDoKiemTra lyDo,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable);
}
