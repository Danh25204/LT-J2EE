package com.example.antique.repository;

import com.example.antique.entity.ExportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho chi tiết phiếu xuất.
 */
@Repository
public interface ExportReceiptDetailRepository extends JpaRepository<ExportReceiptDetail, Long> {

    List<ExportReceiptDetail> findByExportReceiptId(Long exportReceiptId);

    List<ExportReceiptDetail> findByAntiqueId(Long antiqueId);

}
