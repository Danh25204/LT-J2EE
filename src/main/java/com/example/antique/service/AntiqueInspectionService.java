package com.example.antique.service;

import com.example.antique.dto.AntiqueInspectionDTO;
import com.example.antique.entity.*;
import com.example.antique.repository.AntiqueInspectionRepository;
import com.example.antique.repository.InventoryRepository;
import com.example.antique.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service quản lý yêu cầu kiểm tra đồ cổ.
 *
 * Hai nguồn tạo yêu cầu:
 *  1. @Scheduled hàng ngày: quét tồn kho lâu ngày (mặc định > 30 ngày)
 *  2. Trigger thủ công: khi hủy phiếu xuất CHO_MUON (trả hàng)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AntiqueInspectionService {

    // Ngưỡng ngày tồn kho không có hoạt động → cần kiểm tra
    private static final int NGUONG_NGAY_TON_KHO = 30;

    private final AntiqueInspectionRepository inspectionRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // Scheduled job — chạy mỗi ngày lúc 8:00 sáng
    // ─────────────────────────────────────────────────────────────

    /**
     * Quét tất cả tồn kho: nếu đồ cổ có soLuongTon > 0
     * và updatedAt > NGUONG_NGAY_TON_KHO ngày → tạo yêu cầu kiểm tra.
     *
     * Dùng Propagation.REQUIRES_NEW để tách transaction riêng,
     * tránh ảnh hưởng đến các nghiệp vụ khác.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void scheduledKiemTraTonKhoLau() {
        log.info("=== [Scheduled] Bắt đầu kiểm tra tồn kho lâu ngày ===");
        // Dùng findAllWithAntique() thay findAll() để tránh N+1 khi access inv.getAntique()
        List<Inventory> allInventories = inventoryRepository.findAllWithAntique();
        LocalDateTime nguong = LocalDateTime.now().minusDays(NGUONG_NGAY_TON_KHO);
        int dem = 0;
        for (Inventory inv : allInventories) {
            if (inv.getSoLuongTon() > 0 && inv.getUpdatedAt() != null
                    && inv.getUpdatedAt().isBefore(nguong)) {
                boolean daCoAlert = inspectionRepository
                        .existsByAntiqueIdAndLyDoKiemTraAndTrangThai(
                                inv.getAntique().getId(),
                                LyDoKiemTra.TON_KHO_LAU,
                                TrangThaiKiemTra.CHO_KIEM_TRA);
                if (!daCoAlert) {
                    long soNgay = ChronoUnit.DAYS.between(inv.getUpdatedAt(), LocalDateTime.now());
                    taoYeuCauKiemTra(inv.getAntique(), LyDoKiemTra.TON_KHO_LAU,
                            (int) soNgay, null,
                            String.format("Đồ cổ '%s' tồn kho %d ngày chưa có hoạt động xuất kho. Cần kiểm tra tình trạng.",
                                    inv.getAntique().getTenDocCo(), soNgay));
                    dem++;
                }
            }
        }
        log.info("=== [Scheduled] Đã tạo {} yêu cầu kiểm tra tồn kho lâu ngày ===", dem);
    }

    // ─────────────────────────────────────────────────────────────
    // Trigger methods — gọi từ ExportReceiptService
    // ─────────────────────────────────────────────────────────────

    /**
     * Khi hủy phiếu xuất CHO_MUON → đồ cổ được trả lại → cần kiểm tra tình trạng.
     * Dùng REQUIRES_NEW để đảm bảo log dù transaction gốc rollback.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerKiemTraSauChoMuon(ExportReceipt receipt) {
        if (receipt.getLyDo() != LyDoXuat.CHO_MUON) return;
        for (ExportReceiptDetail detail : receipt.getDetails()) {
            Antique antique = detail.getAntique();
            boolean daCoAlert = inspectionRepository
                    .existsByAntiqueIdAndLyDoKiemTraAndTrangThai(
                            antique.getId(),
                            LyDoKiemTra.SAU_CHO_MUON,
                            TrangThaiKiemTra.CHO_KIEM_TRA);
            if (!daCoAlert) {
                taoYeuCauKiemTra(antique, LyDoKiemTra.SAU_CHO_MUON, null,
                        receipt.getId(),
                        String.format("Đồ cổ '%s' vừa được trả lại sau khi cho mượn (phiếu %s). " +
                                "Cần kiểm tra tình trạng trước khi nhập lại kho.",
                                antique.getTenDocCo(), receipt.getMaPhieuXuat()));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Action methods — cho Controller
    // ─────────────────────────────────────────────────────────────

    /**
     * Nhân viên đánh dấu đã hoàn thành kiểm tra, ghi kết quả.
     */
    @Transactional
    public void hoanThanhKiemTra(Long id, String ketQua) {
        AntiqueInspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu kiểm tra ID: " + id));
        if (inspection.getTrangThai() != TrangThaiKiemTra.CHO_KIEM_TRA) {
            throw new RuntimeException("Yêu cầu này đã được xử lý!");
        }
        inspection.setTrangThai(TrangThaiKiemTra.DA_KIEM_TRA);
        inspection.setKetQuaKiemTra(ketQua);
        inspection.setNgayKiemTra(LocalDateTime.now());
        inspection.setNguoiKiemTra(getCurrentUser());
        inspectionRepository.save(inspection);
    }

    /**
     * Bỏ qua yêu cầu kiểm tra.
     */
    @Transactional
    public void boQua(Long id) {
        AntiqueInspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu kiểm tra ID: " + id));
        if (inspection.getTrangThai() != TrangThaiKiemTra.CHO_KIEM_TRA) {
            throw new RuntimeException("Yêu cầu này đã được xử lý!");
        }
        inspection.setTrangThai(TrangThaiKiemTra.BO_QUA);
        inspection.setNgayKiemTra(LocalDateTime.now());
        inspection.setNguoiKiemTra(getCurrentUser());
        inspectionRepository.save(inspection);
    }

    // ─────────────────────────────────────────────────────────────
    // Query methods — cho Controller
    // ─────────────────────────────────────────────────────────────

    /**
     * Đếm số yêu cầu đang chờ — dùng cho badge trên navbar/dashboard.
     */
    @Transactional(readOnly = true)
    public long countPending() {
        return inspectionRepository.countByTrangThai(TrangThaiKiemTra.CHO_KIEM_TRA);
    }

    /**
     * Lấy danh sách có lọc + phân trang.
     */
    @Transactional(readOnly = true)
    public Page<AntiqueInspectionDTO> findPaged(
            TrangThaiKiemTra trangThai,
            LyDoKiemTra lyDo,
            LocalDate tuNgay,
            LocalDate denNgay,
            int page, int size) {

        LocalDateTime tuNgayDt = tuNgay != null ? tuNgay.atStartOfDay() : null;
        LocalDateTime denNgayDt = denNgay != null ? denNgay.atTime(LocalTime.MAX) : null;

        return inspectionRepository
                .findByFilter(trangThai, lyDo, tuNgayDt, denNgayDt, PageRequest.of(page, size))
                .map(this::convertToDTO);
    }

    // ─────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────

    private void taoYeuCauKiemTra(Antique antique, LyDoKiemTra lyDo,
                                   Integer soNgayTonKho, Long exportReceiptId, String moTa) {
        AntiqueInspection inspection = new AntiqueInspection();
        inspection.setAntique(antique);
        inspection.setLyDoKiemTra(lyDo);
        inspection.setSoNgayTonKho(soNgayTonKho);
        inspection.setExportReceiptId(exportReceiptId);
        inspection.setMoTa(moTa);
        inspection.setTrangThai(TrangThaiKiemTra.CHO_KIEM_TRA);
        inspectionRepository.save(inspection);
        log.info("Tạo yêu cầu kiểm tra: {} - {} - {}", antique.getTenDocCo(), lyDo, moTa);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private AntiqueInspectionDTO convertToDTO(AntiqueInspection i) {
        AntiqueInspectionDTO dto = new AntiqueInspectionDTO();
        dto.setId(i.getId());
        dto.setLyDoKiemTra(i.getLyDoKiemTra());
        dto.setSoNgayTonKho(i.getSoNgayTonKho());
        dto.setTrangThai(i.getTrangThai());
        dto.setMoTa(i.getMoTa());
        dto.setKetQuaKiemTra(i.getKetQuaKiemTra());
        dto.setExportReceiptId(i.getExportReceiptId());
        dto.setNgayKiemTra(i.getNgayKiemTra());
        dto.setCreatedAt(i.getCreatedAt());

        if (i.getAntique() != null) {
            Antique a = i.getAntique();
            dto.setAntiqueId(a.getId());
            dto.setMaDocCo(a.getMaDocCo());
            dto.setTenDocCo(a.getTenDocCo());
            dto.setAnhChinh(a.getAnhChinh());
            if (a.getCategory() != null) {
                dto.setCategoryName(a.getCategory().getTenLoai());
            }
            if (a.getInventory() != null) {
                dto.setSoLuongTon(a.getInventory().getSoLuongTon());
            }
        }
        if (i.getNguoiKiemTra() != null) {
            dto.setNguoiKiemTraId(i.getNguoiKiemTra().getId());
            dto.setNguoiKiemTraName(i.getNguoiKiemTra().getUsername());
        }
        return dto;
    }
}
