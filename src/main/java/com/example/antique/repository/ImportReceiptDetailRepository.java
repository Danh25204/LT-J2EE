package com.example.antique.repository;

import com.example.antique.entity.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho chi tiết phiếu nhập.
 */
@Repository
public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, Long> {

    // Tìm tất cả chi tiết của một phiếu nhập
    List<ImportReceiptDetail> findByImportReceiptId(Long importReceiptId);

    // Tìm chi tiết theo antique (kiểm tra xem đồ cổ này đã được nhập hay chưa)
    List<ImportReceiptDetail> findByAntiqueId(Long antiqueId);

}
