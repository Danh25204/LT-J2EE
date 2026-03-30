package com.example.antique.repository;

import com.example.antique.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Inventory (tồn kho).
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Tìm tồn kho theo antique_id (quan hệ 1-1)
    Optional<Inventory> findByAntiqueId(Long antiqueId);

    // Tất cả tồn kho, join antique để hiển thị thông tin
    // Sắp xếp theo tên đồ cổ
    @Query("SELECT i FROM Inventory i JOIN FETCH i.antique a ORDER BY a.tenDocCo ASC")
    List<Inventory> findAllWithAntique();

    // Tìm các đồ cổ còn tồn kho (soLuongTon > 0)
    @Query("SELECT i FROM Inventory i WHERE i.soLuongTon > 0 ORDER BY i.soLuongTon DESC")
    List<Inventory> findAllWithStock();

    // Tổng số lượng tồn kho toàn kho
    @Query("SELECT COALESCE(SUM(i.soLuongTon), 0) FROM Inventory i")
    Integer tongSoLuongTon();

}
