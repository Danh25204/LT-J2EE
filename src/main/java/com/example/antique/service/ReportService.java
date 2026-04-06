package com.example.antique.service;

import com.example.antique.repository.AntiqueRepository;
import com.example.antique.repository.ExportReceiptRepository;
import com.example.antique.repository.ImportReceiptRepository;
import com.example.antique.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tổng hợp dữ liệu báo cáo / thống kê.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AntiqueRepository antiqueRepository;
    private final InventoryRepository inventoryRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ExportReceiptRepository exportReceiptRepository;

    // ===== DASHBOARD SUMMARY =====

    @Transactional(readOnly = true)
    public long countTotalAntiques() {
        return antiqueRepository.count();
    }

    @Transactional(readOnly = true)
    public long countMonthlyImports() {
        LocalDate now = LocalDate.now();
        return importReceiptRepository.demPhieuNhapTheoThangNam(now.getMonthValue(), now.getYear());
    }

    @Transactional(readOnly = true)
    public long countMonthlyExports() {
        LocalDate now = LocalDate.now();
        return exportReceiptRepository.demPhieuXuatTheoThangNam(now.getMonthValue(), now.getYear());
    }

    @Transactional(readOnly = true)
    public long countItemsOnLoan() {
        return exportReceiptRepository.countItemsOnLoan();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTongGiaTriKho() {
        BigDecimal val = antiqueRepository.tinhTongGiaTriKho();
        return val != null ? val : BigDecimal.ZERO;
    }

    // ===== REPORTS PAGE =====

    @Transactional(readOnly = true)
    public long countTotalAntiquesConTrongKho() {
        return antiqueRepository.countAntiquesConTrongKho();
    }

    @Transactional(readOnly = true)
    public Integer getTongSoLuongTon() {
        Integer val = inventoryRepository.tongSoLuongTon();
        return val != null ? val : 0;
    }

    /**
     * Giá trị nhập kho tháng hiện tại.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTongGiaTriNhapThang() {
        LocalDate now = LocalDate.now();
        BigDecimal val = importReceiptRepository.tongGiaTriNhapTheoThangNam(now.getMonthValue(), now.getYear());
        return val != null ? val : BigDecimal.ZERO;
    }

    /**
     * Giá trị xuất kho tháng hiện tại.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTongGiaTriXuatThang() {
        LocalDate now = LocalDate.now();
        BigDecimal val = exportReceiptRepository.tongGiaTriXuatTheoThangNam(now.getMonthValue(), now.getYear());
        return val != null ? val : BigDecimal.ZERO;
    }

    /**
     * Số lượng đồ cổ theo danh mục — trả về Map<tenLoai, count>.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCountByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = antiqueRepository.countByCategory();
        for (Object[] row : rows) {
            String tenLoai = row[0] != null ? row[0].toString() : "Không rõ";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            result.put(tenLoai, count);
        }
        return result;
    }

    /**
     * Top 6 tháng gần nhất: Map<"MM/yyyy", Map<"nhap"|"xuat", BigDecimal>>.
     */
    @Transactional(readOnly = true)
    public Map<String, Map<String, BigDecimal>> getLast6MonthsStats() {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            int m = month.getMonthValue();
            int y = month.getYear();
            String key = String.format("%02d/%d", m, y);

            BigDecimal nhap = importReceiptRepository.tongGiaTriNhapTheoThangNam(m, y);
            BigDecimal xuat = exportReceiptRepository.tongGiaTriXuatTheoThangNam(m, y);

            Map<String, BigDecimal> monthData = new LinkedHashMap<>();
            monthData.put("nhap", nhap != null ? nhap : BigDecimal.ZERO);
            monthData.put("xuat", xuat != null ? xuat : BigDecimal.ZERO);
            result.put(key, monthData);
        }
        return result;
    }
}
