package com.example.antique.service;

import com.example.antique.dto.ExportReceiptDTO;
import com.example.antique.dto.ExportReceiptDetailDTO;
import com.example.antique.entity.*;
import com.example.antique.repository.AntiqueRepository;
import com.example.antique.repository.ExportReceiptRepository;
import com.example.antique.repository.InventoryRepository;
import com.example.antique.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ phiếu xuất kho.
 *
 * Điểm khác Import:
 *   - Khi tạo: KIỂM TRA TỒN KHO đủ trước khi xuất, rồi soLuongTon -= soLuong
 *   - Khi HỦY: HOÀN LẠI tồn kho soLuongTon += soLuong
 */
@Service
@RequiredArgsConstructor
public class ExportReceiptService {

    private final ExportReceiptRepository exportReceiptRepository;
    private final AntiqueRepository antiqueRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả phiếu xuất, mới nhất trước.
     */
    @Transactional(readOnly = true)
    public List<ExportReceiptDTO> findAll() {
        return exportReceiptRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm phiếu xuất theo ID.
     */
    @Transactional(readOnly = true)
    public ExportReceiptDTO findById(Long id) {
        ExportReceipt receipt = exportReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất ID: " + id));
        return convertToDTO(receipt);
    }

    /**
     * Tạo phiếu xuất mới.
     * Kiểm tra tồn kho trước, sau đó trừ số lượng.
     */
    @Transactional
    public ExportReceiptDTO create(ExportReceiptDTO dto) {
        // Kiểm tra trùng đồ cổ trong cùng phiếu
        java.util.Set<Long> antiqueIds = new java.util.HashSet<>();
        for (ExportReceiptDetailDTO detailDTO : dto.getDetails()) {
            if (!antiqueIds.add(detailDTO.getAntiqueId())) {
                throw new RuntimeException("Trùng đồ cổ ID: " + detailDTO.getAntiqueId() + " trong phiếu xuất!");
            }
        }

        ExportReceipt receipt = new ExportReceipt();

        receipt.setMaPhieuXuat(generateMaPhieu(dto.getNgayXuat()));
        receipt.setNgayXuat(dto.getNgayXuat() != null ? dto.getNgayXuat() : LocalDate.now());
        receipt.setLyDo(dto.getLyDo());
        receipt.setNguoiNhan(dto.getNguoiNhan());
        receipt.setGhiChu(dto.getGhiChu());
        receipt.setTrangThai(TrangThaiPhieuXuat.XUAT_KHO);

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + dto.getUserId()));
        receipt.setUser(user);

        for (ExportReceiptDetailDTO detailDTO : dto.getDetails()) {
            Antique antique = antiqueRepository.findById(detailDTO.getAntiqueId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ cổ ID: " + detailDTO.getAntiqueId()));

            // Kiểm tra tồn kho đủ
            Inventory inventory = inventoryRepository.findByAntiqueId(antique.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho: " + antique.getTenDocCo()));

            if (inventory.getSoLuongTon() < detailDTO.getSoLuong()) {
                throw new RuntimeException(
                        "Không đủ tồn kho cho '" + antique.getTenDocCo()
                        + "'. Tồn kho hiện tại: " + inventory.getSoLuongTon()
                        + ", yêu cầu xuất: " + detailDTO.getSoLuong());
            }

            ExportReceiptDetail detail = new ExportReceiptDetail();
            detail.setAntique(antique);
            detail.setSoLuong(detailDTO.getSoLuong());
            detail.setDonGia(detailDTO.getDonGia());
            detail.setGhiChu(detailDTO.getGhiChu());
            detail.tinhThanhTien();
            detail.setExportReceipt(receipt);
            receipt.getDetails().add(detail);

            // Trừ tồn kho
            inventory.setSoLuongTon(inventory.getSoLuongTon() - detailDTO.getSoLuong());
            inventoryRepository.save(inventory);
        }

        receipt.tinhLaiTongGiaTri();
        ExportReceipt saved = exportReceiptRepository.save(receipt);
        return convertToDTO(saved);
    }

    /**
     * Hủy phiếu xuất — HOÀN LẠI tồn kho.
     */
    @Transactional
    public void cancel(Long id) {
        ExportReceipt receipt = exportReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất ID: " + id));

        if (receipt.getTrangThai() == TrangThaiPhieuXuat.HUY) {
            throw new RuntimeException("Phiếu đã bị hủy trước đó!");
        }

        // Hoàn lại tồn kho cho từng chi tiết
        for (ExportReceiptDetail detail : receipt.getDetails()) {
            Inventory inventory = inventoryRepository.findByAntiqueId(detail.getAntique().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy tồn kho cho: " + detail.getAntique().getTenDocCo()));
            inventory.setSoLuongTon(inventory.getSoLuongTon() + detail.getSoLuong());
            inventoryRepository.save(inventory);
        }

        receipt.setTrangThai(TrangThaiPhieuXuat.HUY);
        exportReceiptRepository.save(receipt);
    }

    /**
     * Sinh mã phiếu xuất: PX-YYYYMM-XXX
     */
    private String generateMaPhieu(LocalDate ngayXuat) {
        LocalDate date = ngayXuat != null ? ngayXuat : LocalDate.now();
        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "PX-" + yearMonth + "-";

        // Tìm số thứ tự lớn nhất đã dùng (tránh trùng khi có phiếu bị hủy)
        java.util.Optional<Integer> maxSeq = exportReceiptRepository.findAll()
                .stream()
                .filter(r -> r.getMaPhieuXuat().startsWith(prefix))
                .map(r -> {
                    String seq = r.getMaPhieuXuat().substring(prefix.length());
                    try { return Integer.parseInt(seq); } catch (NumberFormatException e) { return 0; }
                })
                .max(Integer::compareTo);

        int nextSeq = maxSeq.orElse(0) + 1;
        return prefix + String.format("%03d", nextSeq);
    }

    /**
     * Convert Entity → DTO.
     */
    private ExportReceiptDTO convertToDTO(ExportReceipt receipt) {
        ExportReceiptDTO dto = new ExportReceiptDTO();
        dto.setId(receipt.getId());
        dto.setMaPhieuXuat(receipt.getMaPhieuXuat());
        dto.setNgayXuat(receipt.getNgayXuat());
        dto.setLyDo(receipt.getLyDo());
        dto.setNguoiNhan(receipt.getNguoiNhan());
        dto.setTongGiaTri(receipt.getTongGiaTri());
        dto.setGhiChu(receipt.getGhiChu());
        dto.setTrangThai(receipt.getTrangThai());
        dto.setUserId(receipt.getUser().getId());
        dto.setUserName(receipt.getUser().getUsername());
        dto.setCreatedAt(receipt.getCreatedAt());

        List<ExportReceiptDetailDTO> details = receipt.getDetails().stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
        dto.setDetails(details);

        return dto;
    }

    private ExportReceiptDetailDTO convertDetailToDTO(ExportReceiptDetail detail) {
        ExportReceiptDetailDTO dto = new ExportReceiptDetailDTO();
        dto.setId(detail.getId());
        dto.setExportReceiptId(detail.getExportReceipt().getId());
        dto.setAntiqueId(detail.getAntique().getId());
        dto.setAntiqueName(detail.getAntique().getTenDocCo());
        dto.setAntiqueImage(detail.getAntique().getAnhChinh());
        dto.setSoLuong(detail.getSoLuong());
        dto.setDonGia(detail.getDonGia());
        dto.setThanhTien(detail.getThanhTien());
        dto.setGhiChu(detail.getGhiChu());
        return dto;
    }

    /**
     * Lấy danh sách tồn kho còn hàng (dùng cho form tạo phiếu xuất).
     */
    @Transactional(readOnly = true)
    public List<Inventory> findInventoriesWithStock() {
        return inventoryRepository.findAllWithAntique().stream()
                .filter(inv -> inv.getSoLuongTon() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Tìm user theo username (dùng cho controller).
     */
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }
}
