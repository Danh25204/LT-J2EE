package com.example.antique.repository;

import com.example.antique.entity.Antique;
import com.example.antique.entity.TinhTrang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Antique.
 *
 * Kế thừa JpaSpecificationExecutor<Antique> để hỗ trợ tìm kiếm/lọc động
 * bằng Specification API (sẽ dùng ở Phase 4).
 */
@Repository
public interface AntiqueRepository
        extends JpaRepository<Antique, Long>, JpaSpecificationExecutor<Antique> {

    // Tìm theo mã đồ cổ (unique)
    Optional<Antique> findByMaDocCo(String maDocCo);

    // Kiểm tra mã đồ cổ đã tồn tại chưa
    boolean existsByMaDocCo(String maDocCo);

    // Tất cả đồ cổ, sắp theo ngày tạo mới nhất
    List<Antique> findAllByOrderByCreatedAtDesc();

    // Tìm theo danh mục
    List<Antique> findByCategoryId(Long categoryId);

    // Tìm theo tình trạng
    List<Antique> findByTinhTrang(TinhTrang tinhTrang);

    /**
     * Tìm kiếm đơn giản theo tên hoặc mã (dùng cho dropdown chọn đồ cổ khi lập phiếu).
     * Phase 4 sẽ dùng Specification cho tìm kiếm nâng cao.
     */
    @Query("SELECT a FROM Antique a WHERE " +
           "LOWER(a.tenDocCo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.maDocCo)  LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Antique> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Đếm số lượng đồ cổ theo từng danh mục (dùng cho báo cáo Phase 8).
     * Trả về List<Object[]>: [categoryName, count]
     */
    @Query("SELECT a.category.tenLoai, COUNT(a) FROM Antique a GROUP BY a.category.tenLoai ORDER BY COUNT(a) DESC")
    List<Object[]> countByCategory();

    /**
     * Tính tổng giá trị kho (gia_nhap × soLuongTon trong inventory).
     */
    @Query("SELECT COALESCE(SUM(a.giaNhap * i.soLuongTon), 0) " +
           "FROM Antique a JOIN a.inventory i " +
           "WHERE i.soLuongTon > 0")
    java.math.BigDecimal tinhTongGiaTriKho();

    /**
     * Lấy danh sách đồ cổ còn trong kho (soLuongTon > 0) dùng cho xuất kho.
     */
    @Query("SELECT a FROM Antique a JOIN a.inventory i WHERE i.soLuongTon > 0 ORDER BY a.tenDocCo")
    List<Antique> findAntiquesConTrongKho();

    /**
     * Đếm tổng số đồ cổ đang có trong kho (soLuongTon > 0).
     */
    @Query("SELECT COUNT(a) FROM Antique a JOIN a.inventory i WHERE i.soLuongTon > 0")
    long countAntiquesConTrongKho();

}
