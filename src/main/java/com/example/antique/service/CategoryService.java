package com.example.antique.service;

import com.example.antique.dto.CategoryDTO;
import com.example.antique.entity.Category;
import com.example.antique.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý nghiệp vụ cho module Danh mục loại đồ cổ.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ─── Query ──────────────────────────────────────────────────────────────────

    /** Lấy tất cả danh mục, sắp xếp theo tên loại A→Z. */
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByTenLoaiAsc();
    }

    /** Tìm danh mục theo id; ném ngoại lệ nếu không tồn tại. */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với id = " + id));
    }

    /** Kiểm tra mã loại đã tồn tại chưa (bỏ qua bản ghi có id = excludeId khi edit). */
    public boolean existsByMaLoai(String maLoai, Long excludeId) {
        if (!categoryRepository.existsByMaLoai(maLoai)) return false;
        if (excludeId == null) return true; // tạo mới → trùng là lỗi
        // chỉnh sửa → chỉ lỗi nếu trùng với bản ghi KHÁC
        return categoryRepository.findByMaLoai(maLoai)
                .map(c -> !c.getId().equals(excludeId))
                .orElse(false);
    }

    /** Kiểm tra tên loại đã tồn tại chưa (bỏ qua bản ghi có id = excludeId khi edit). */
    public boolean existsByTenLoai(String tenLoai, Long excludeId) {
        if (!categoryRepository.existsByTenLoaiIgnoreCase(tenLoai)) return false;
        if (excludeId == null) return true;
        return categoryRepository.findByTenLoaiIgnoreCase(tenLoai)
                .map(c -> !c.getId().equals(excludeId))
                .orElse(false);
    }

    // ─── Command ─────────────────────────────────────────────────────────────────

    /** Tạo mới danh mục từ DTO. */
    @Transactional
    public Category create(CategoryDTO dto) {
        validateUnique(dto.getMaLoai(), dto.getTenLoai(), null);

        Category category = new Category();
        mapToEntity(dto, category);
        return categoryRepository.save(category);
    }

    /** Cập nhật danh mục theo id từ DTO. */
    @Transactional
    public Category update(Long id, CategoryDTO dto) {
        Category category = findById(id);
        validateUnique(dto.getMaLoai(), dto.getTenLoai(), id);

        mapToEntity(dto, category);
        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục theo id.
     * Không cho phép xóa nếu danh mục đang có đồ cổ liên kết.
     */
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);

        long soDocCo = categoryRepository.countAntiquesByCategoryId(id);
        if (soDocCo > 0) {
            throw new IllegalStateException(
                    "Không thể xóa danh mục \"" + category.getTenLoai() +
                    "\" vì đang có " + soDocCo + " đồ cổ thuộc danh mục này.");
        }

        categoryRepository.delete(category);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void validateUnique(String maLoai, String tenLoai, Long excludeId) {
        if (existsByMaLoai(maLoai, excludeId)) {
            throw new IllegalArgumentException("Mã loại \"" + maLoai + "\" đã tồn tại.");
        }
        if (existsByTenLoai(tenLoai, excludeId)) {
            throw new IllegalArgumentException("Tên loại \"" + tenLoai + "\" đã tồn tại.");
        }
    }

    private void mapToEntity(CategoryDTO dto, Category category) {
        category.setMaLoai(dto.getMaLoai().toUpperCase().trim());
        category.setTenLoai(dto.getTenLoai().trim());
        category.setMoTa(dto.getMoTa() != null ? dto.getMoTa().trim() : null);
    }

    /** Chuyển Entity → DTO để điền sẵn vào form chỉnh sửa. */
    public CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setMaLoai(category.getMaLoai());
        dto.setTenLoai(category.getTenLoai());
        dto.setMoTa(category.getMoTa());
        return dto;
    }
}
