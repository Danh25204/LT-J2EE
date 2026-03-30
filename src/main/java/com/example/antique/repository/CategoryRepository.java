package com.example.antique.repository;

import com.example.antique.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Category.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Tìm theo mã loại (unique)
    Optional<Category> findByMaLoai(String maLoai);

    // Kiểm tra trùng mã loại khi tạo/sửa
    boolean existsByMaLoai(String maLoai);

    // Kiểm tra trùng tên loại (phân biệt hoa thường)
    boolean existsByTenLoai(String tenLoai);

    // Kiểm tra trùng tên loại (không phân biệt hoa thường)
    boolean existsByTenLoaiIgnoreCase(String tenLoai);

    // Tìm theo tên chính xác (không phân biệt hoa thường), dùng khi validate edit
    Optional<Category> findByTenLoaiIgnoreCase(String tenLoai);

    // Tất cả category sắp xếp theo tên (JOIN FETCH antiques để tránh LazyInitializationException)
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.antiques ORDER BY c.tenLoai ASC")
    List<Category> findAllByOrderByTenLoaiAsc();

    // Tìm theo tên (tìm kiếm gần đúng, không phân biệt hoa thường)
    List<Category> findByTenLoaiContainingIgnoreCase(String keyword);

    /**
     * Đếm số đồ cổ trong một danh mục.
     * Dùng JPQL (@Query) thay vì derived query vì cần JOIN.
     */
    @Query("SELECT COUNT(a) FROM Antique a WHERE a.category.id = :categoryId")
    long countAntiquesByCategoryId(Long categoryId);

}
