package com.example.antique.service;

import com.example.antique.dto.WarehouseActivityDTO;
import com.example.antique.entity.*;
import com.example.antique.repository.UserRepository;
import com.example.antique.repository.WarehouseActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service quản lý lịch sử hoạt động kho.
 *
 * Được gọi từ ImportReceiptService và ExportReceiptService
 * để ghi log sau mỗi thao tác nhập/xuất/hủy.
 *
 * Dùng Propagation.REQUIRES_NEW để đảm bảo log luôn được lưu
 * dù transaction gốc có rollback hay không.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseActivityService {

    private final WarehouseActivityRepository activityRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    /** Lấy user đang đăng nhập từ SecurityContext. */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    // ─────────────────────────────────────────────────────────────
    // Log methods — gọi từ các service khác
    // ─────────────────────────────────────────────────────────────

    /**
     * Ghi log nhập kho theo phiếu nhập.
     * Ghi 1 bản ghi tổng hợp cho cả phiếu.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logNhapKho(ImportReceipt receipt) {
        WarehouseActivity activity = new WarehouseActivity();
        activity.setLoaiHoatDong(LoaiHoatDong.NHAP_KHO);
        activity.setThamChieu(receipt.getMaPhieuNhap());
        activity.setImportReceiptId(receipt.getId());
        activity.setUser(receipt.getUser());
        activity.setGiaTri(receipt.getTongGiaTri());

        int tongSoLuong = receipt.getDetails().stream()
                .mapToInt(ImportReceiptDetail::getSoLuong).sum();
        activity.setSoLuongThayDoi(tongSoLuong);

        String tenDocCo = receipt.getDetails().size() == 1
                ? receipt.getDetails().get(0).getAntique().getTenDocCo()
                : receipt.getDetails().size() + " loại đồ cổ";

        activity.setMoTa(String.format("Nhập kho %d sản phẩm (%s) theo phiếu %s%s",
                tongSoLuong,
                tenDocCo,
                receipt.getMaPhieuNhap(),
                receipt.getNguonGoc() != null ? " - Nguồn: " + receipt.getNguonGoc() : ""));

        if (receipt.getDetails().size() == 1) {
            activity.setAntique(receipt.getDetails().get(0).getAntique());
        }

        activityRepository.save(activity);
    }

    /**
     * Ghi log xuất kho theo phiếu xuất.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logXuatKho(ExportReceipt receipt) {
        WarehouseActivity activity = new WarehouseActivity();
        activity.setLoaiHoatDong(LoaiHoatDong.XUAT_KHO);
        activity.setThamChieu(receipt.getMaPhieuXuat());
        activity.setExportReceiptId(receipt.getId());
        activity.setUser(receipt.getUser());
        activity.setGiaTri(receipt.getTongGiaTri());

        int tongSoLuong = receipt.getDetails().stream()
                .mapToInt(ExportReceiptDetail::getSoLuong).sum();
        activity.setSoLuongThayDoi(-tongSoLuong);  // âm = xuất ra

        String tenDocCo = receipt.getDetails().size() == 1
                ? receipt.getDetails().get(0).getAntique().getTenDocCo()
                : receipt.getDetails().size() + " loại đồ cổ";

        String lyDo = receipt.getLyDo() != null ? " - Lý do: " + receipt.getLyDo().getLabel() : "";
        activity.setMoTa(String.format("Xuất kho %d sản phẩm (%s) theo phiếu %s%s",
                tongSoLuong, tenDocCo, receipt.getMaPhieuXuat(), lyDo));

        if (receipt.getDetails().size() == 1) {
            activity.setAntique(receipt.getDetails().get(0).getAntique());
        }

        activityRepository.save(activity);
    }

    /**
     * Ghi log hủy phiếu nhập.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logHuyNhap(ImportReceipt receipt) {
        WarehouseActivity activity = new WarehouseActivity();
        activity.setLoaiHoatDong(LoaiHoatDong.HUY_NHAP);
        activity.setThamChieu(receipt.getMaPhieuNhap());
        activity.setImportReceiptId(receipt.getId());
        activity.setUser(getCurrentUser());
        activity.setGiaTri(receipt.getTongGiaTri());

        int tongSoLuong = receipt.getDetails().stream()
                .mapToInt(ImportReceiptDetail::getSoLuong).sum();
        activity.setSoLuongThayDoi(-tongSoLuong);  // hủy nhập → tồn giảm

        activity.setMoTa(String.format("Hủy phiếu nhập %s — hoàn lại %d sản phẩm vào kho",
                receipt.getMaPhieuNhap(), tongSoLuong));

        activityRepository.save(activity);
    }

    /**
     * Ghi log hủy phiếu xuất.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logHuyXuat(ExportReceipt receipt) {
        WarehouseActivity activity = new WarehouseActivity();
        activity.setThamChieu(receipt.getMaPhieuXuat());
        activity.setExportReceiptId(receipt.getId());
        activity.setUser(getCurrentUser());
        activity.setGiaTri(receipt.getTongGiaTri());

        int tongSoLuong = receipt.getDetails().stream()
                .mapToInt(ExportReceiptDetail::getSoLuong).sum();
        activity.setSoLuongThayDoi(tongSoLuong);  // dương = hoàn lại kho

        boolean laChoMuon = receipt.getLyDo() == LyDoXuat.CHO_MUON;
        if (laChoMuon) {
            activity.setLoaiHoatDong(LoaiHoatDong.TRA_HANG);
            activity.setMoTa(String.format("Nhận lại hàng cho mượn từ %s — hoàn lại %d sản phẩm vào kho (phiếu %s)",
                    receipt.getNguoiNhan() != null ? receipt.getNguoiNhan() : "khách",
                    tongSoLuong, receipt.getMaPhieuXuat()));
        } else {
            activity.setLoaiHoatDong(LoaiHoatDong.HUY_XUAT);
            activity.setMoTa(String.format("Hủy phiếu xuất %s — hoàn lại %d sản phẩm vào kho",
                    receipt.getMaPhieuXuat(), tongSoLuong));
        }

        activityRepository.save(activity);
    }

    // ─────────────────────────────────────────────────────────────
    // Query methods — cho Controller
    // ─────────────────────────────────────────────────────────────

    /**
     * Lấy lịch sử có phân trang + lọc.
     */
    @Transactional(readOnly = true)
    public Page<WarehouseActivityDTO> findPaged(
            LoaiHoatDong loai,
            LocalDate tuNgay,
            LocalDate denNgay,
            String keyword,
            int page, int size) {

        LocalDateTime tuNgayDt = tuNgay != null ? tuNgay.atStartOfDay() : null;
        LocalDateTime denNgayDt = denNgay != null ? denNgay.atTime(LocalTime.MAX) : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        return activityRepository
                .findByFilter(loai, tuNgayDt, denNgayDt, kw, PageRequest.of(page, size))
                .map(this::convertToDTO);
    }

    // ─────────────────────────────────────────────────────────────
    // Converter
    // ─────────────────────────────────────────────────────────────

    private WarehouseActivityDTO convertToDTO(WarehouseActivity a) {
        WarehouseActivityDTO dto = new WarehouseActivityDTO();
        dto.setId(a.getId());
        dto.setLoaiHoatDong(a.getLoaiHoatDong());
        dto.setMoTa(a.getMoTa());
        dto.setThamChieu(a.getThamChieu());
        dto.setSoLuongThayDoi(a.getSoLuongThayDoi());
        dto.setGiaTri(a.getGiaTri());
        dto.setImportReceiptId(a.getImportReceiptId());
        dto.setExportReceiptId(a.getExportReceiptId());
        dto.setCreatedAt(a.getCreatedAt());

        if (a.getUser() != null) {
            dto.setUserId(a.getUser().getId());
            dto.setUserName(a.getUser().getUsername());
        }
        if (a.getAntique() != null) {
            dto.setAntiqueId(a.getAntique().getId());
            dto.setTenDocCo(a.getAntique().getTenDocCo());
            dto.setMaDocCo(a.getAntique().getMaDocCo());
        }
        return dto;
    }
}
